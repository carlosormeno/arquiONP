**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Elasticsearch en Servidor Virtual (RHEL 8)**

Usando Podman + Quadlet sobre Red Hat Enterprise Linux 8

| **Versión:**        | 0.1.0      |
|---------------------|------------|
| **Fecha:**          | 2026-05-20 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Elasticsearch en servidores virtuales RHEL 8 on-premise, usando Podman como runtime de contenedores y Quadlet para la integración con systemd.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Elasticsearch?

Elasticsearch es un motor de búsqueda y análisis distribuido basado en Apache Lucene. Permite almacenar, buscar y analizar grandes volúmenes de datos en tiempo real. En el stack ONP actúa como backend de almacenamiento compartido para dos componentes:

- **OTEL Collector**: envía logs de los servicios al índice `onp-logs-<entorno>`.
- **Jaeger**: almacena las trazas distribuidas en los índices `jaeger-onp-*`.
- **Kibana**: consulta y visualiza los logs almacenados.

## 1.3 Rol de Elasticsearch en el stack ONP

```
OTEL Collector
    └── logs pipeline   → Elasticsearch (índice: onp-logs-<entorno>)

Jaeger
    └── traces storage  → Elasticsearch (índices: jaeger-onp-*)

Kibana          → consulta onp-logs-*
Grafana         → consulta métricas vía Prometheus
```

Elasticsearch es un componente de **infraestructura compartida**: múltiples componentes del stack dependen de él. Debe instalarse y verificarse antes que Jaeger y Kibana.

## 1.4 Arquitectura por entorno

La cantidad de nodos Elasticsearch varía según el entorno:

| Entorno | Nodos ES | Tipo de cluster | Servidor(es) |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 (stack completo) |
| QA      | 1        | Single-node     | Servidor 2 (ES + Kibana) |
| PROD    | 3        | Cluster HA      | Servidores 3, 4 y 5 |

> **Por qué 3 nodos en PROD:** Con 2 nodos, Elasticsearch no puede alcanzar quórum de forma confiable si un nodo cae — el cluster puede quedar en estado `red` y rechazar escrituras. Con 3 nodos, el sistema tolera la pérdida de 1 nodo sin interrupciones.

## 1.5 Por qué Podman y Quadlet

**Podman** es el runtime de contenedores oficial de RHEL 8. A diferencia de Docker, no requiere un daemon central en ejecución permanente, lo que reduce el consumo de recursos y mejora la postura de seguridad (los contenedores corren sin privilegios adicionales del sistema).

**Quadlet** es el mecanismo de integración de Podman con systemd. Permite administrar contenedores exactamente igual que cualquier otro servicio del sistema operativo:

```bash
systemctl start elasticsearch    # iniciar
systemctl stop elasticsearch     # detener
systemctl restart elasticsearch  # reiniciar
journalctl -u elasticsearch      # ver logs
```

## 1.6 Configuración de seguridad

Elasticsearch 8.x tiene la seguridad habilitada por defecto. En el stack ONP se usa la siguiente configuración:

| Parámetro | Valor | Motivo |
|-----------|-------|--------|
| `xpack.security.enabled` | true | Autenticación activa (usuario elastic) |
| `xpack.security.http.ssl.enabled` | false | TLS HTTP desactivado para simplificar la comunicación interna en red corporativa |
| `xpack.security.transport.ssl.enabled` | false | TLS transport desactivado (red interna confiable) |

## 1.7 Estrategia de retención y compresión

Este manual configura Elasticsearch con:

- **`best_compression`**: codec de compresión que reduce el tamaño de los índices entre 30 y 40% adicional respecto al codec por defecto (LZ4). Ideal para datos históricos que no requieren acceso frecuente.
- **ILM (Index Lifecycle Management)**: política automática que controla el ciclo de vida de los índices. Se configura con rollover a los 30 días y eliminación a los 12 meses.
- **Rollover configurable sin redeploy**: la política ILM se actualiza vía API REST. No es necesario reiniciar Elasticsearch ni el Collector para cambiar los períodos de retención.

## 1.8 Alcance de este manual

Este manual cubre:

- Preparación del servidor (kernel, directorios, firewall).
- Instalación y configuración de Podman y Quadlet en RHEL 8.
- Despliegue de Elasticsearch como servicio systemd vía Quadlet.
- Verificación del despliegue.
- Configuración de retención: `best_compression`, ILM y rollover.
- Troubleshooting de errores comunes.

Queda fuera del alcance:

- Habilitación de TLS HTTP o transport entre nodos.
- Gestión de usuarios adicionales de Elasticsearch.
- Configuración de snapshots a almacenamiento externo.
- Instalación de Kibana, Jaeger o el OTEL Collector (ver sus respectivos manuales).

# 2. Prerrequisitos

> **⚠️ ADVERTENCIA:** Verificar TODOS los prerrequisitos antes de comenzar la instalación. Una verificación incompleta es la causa más común de fallos durante el despliegue.

## 2.1 Infraestructura requerida por entorno

| Entorno | Servidores | vCPU | RAM | Filesystem /data |
|---------|------------|------|-----|------------------|
| DEV     | 1          | 4    | 8 GB | 50 GB |
| QA      | 1 (Servidor 2) | 4 | 8 GB | 50 GB |
| PROD    | 3 (Servidores 3, 4 y 5) | 4 c/u | 8 GB c/u | 100 GB c/u |

> **NOTA:** El filesystem `/data` debe estar montado y disponible antes de comenzar. Verificar con `df -h /data`. Si no aparece, contactar al equipo de infraestructura.

## 2.2 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| Podman | 4.4 o superior | `podman --version` |
| curl | Cualquier versión reciente | `curl --version` |

> **NOTA:** Podman 4.4+ es requerido para Quadlet. RHEL 8.8 incluye Podman 4.4 en el módulo `container-tools`. Si la versión instalada es inferior, ver Sección 4.1.

## 2.3 Accesos requeridos

- Acceso SSH al servidor con usuario con privilegios `sudo`.
- Acceso al GitLab Container Registry del grupo de observabilidad (para QA y PROD).
- Acceso de red entre los 3 nodos ES de PROD (puertos 9200 y 9300).

## 2.4 Información a recopilar antes de comenzar

Completar la siguiente tabla antes de ejecutar cualquier paso:

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<GITLAB_REGISTRY_URL\> | URL base del GitLab Registry | |
| \<ELASTIC_PASSWORD\> | Contraseña del usuario administrador `elastic` | |
| \<ES_VERSION\> | Versión de Elasticsearch | 8.19.15 |
| \<IP_ES_NODE1\> | IP del Servidor 3 (solo PROD) | |
| \<IP_ES_NODE2\> | IP del Servidor 4 (solo PROD) | |
| \<IP_ES_NODE3\> | IP del Servidor 5 (solo PROD) | |

## 2.5 Verificaciones previas

### 2.5.1 Verificar el filesystem /data

```bash
df -h /data
```

Resultado esperado:

```
Filesystem      Size  Used Avail Use% Mounted on
/dev/sdX        50G   1G   49G   2%  /data
```

Si `/data` no aparece, el filesystem no está montado. Detener y contactar a infraestructura.

### 2.5.2 Verificar la versión de Podman

```bash
podman --version
```

Resultado esperado:

```
podman version 4.4.x
```

Si la versión es inferior a 4.4, continuar en la Sección 4.1 para actualizar.

### 2.5.3 Verificar conectividad entre nodos (solo PROD)

Ejecutar desde el Servidor 3 hacia los otros dos nodos:

```bash
ping -c 3 <IP_ES_NODE2>
ping -c 3 <IP_ES_NODE3>
```

Ambos deben responder. Si alguno no responde, verificar las reglas de firewall entre servidores con el equipo de infraestructura antes de continuar.

# 3. Preparación de imágenes

## Nota: ¿Qué camino seguir?

| Escenario | Cuándo usarlo | Ir a |
|-----------|---------------|------|
| A — GitLab Registry | QA y PROD. Sin salida a internet desde el servidor. | Sección 3.2 |
| B — Internet directo | DEV y pruebas. El servidor tiene acceso a internet. | Sección 3.3 |
| C — Air-gap | Sin internet ni registry privado. | Anexo D |

## 3.1 Imagen requerida

| Componente | Imagen original | Versión |
|------------|-----------------|---------|
| Elasticsearch | docker.elastic.co/elasticsearch/elasticsearch | 8.19.15 |

> **NOTA:** Verificar la versión estable más reciente en https://www.elastic.co/downloads/elasticsearch antes de ejecutar el mirroring. Usar siempre una versión fija, nunca `latest`.

## 3.2 Escenario A — GitLab Registry (QA y PROD)

Ejecutar desde una máquina con acceso a internet y Podman instalado. Este proceso se realiza una sola vez por versión de imagen.

```bash
# Iniciar sesión en el registry privado
podman login <GITLAB_REGISTRY_URL>

# Descargar la imagen del registry público
podman pull docker.elastic.co/elasticsearch/elasticsearch:<ES_VERSION>

# Re-taggear apuntando al registry privado
podman tag docker.elastic.co/elasticsearch/elasticsearch:<ES_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:<ES_VERSION>

# Publicar en el registry privado
podman push <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:<ES_VERSION>
```

La imagen queda disponible en:

```
<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:<ES_VERSION>
```

## 3.3 Escenario B — Internet directo (DEV)

No se requieren pasos de mirroring. Pasar directamente a la Sección 4. En la Sección 7 usar la imagen pública directamente:

```
docker.elastic.co/elasticsearch/elasticsearch:<ES_VERSION>
```

## 3.4 Escenario C — Air-gap

Ver **Anexo D** para el procedimiento completo de transferencia de imágenes vía archivo `.tar`.

# 4. Preparación del servidor

Estos pasos se ejecutan en **cada servidor** donde se instalará Elasticsearch.

## 4.1 Instalar o actualizar Podman a versión 4.4+

Verificar si Podman ya está instalado con la versión correcta:

```bash
podman --version
```

Si la versión es inferior a 4.4 o no está instalado:

```bash
# Habilitar el módulo container-tools con la versión más reciente
sudo dnf module reset container-tools -y
sudo dnf module enable container-tools:rhel8 -y

# Instalar Podman
sudo dnf install -y podman

# Verificar la versión instalada
podman --version
```

Resultado esperado: `podman version 4.4.x` o superior.

## 4.2 Configurar vm.max_map_count

Elasticsearch requiere que el parámetro del kernel `vm.max_map_count` sea al menos `262144`. Este parámetro debe configurarse en el sistema operativo del servidor, no en el contenedor.

```bash
# Verificar el valor actual
sysctl vm.max_map_count
```

Resultado esperado: `vm.max_map_count = 262144`

Si el valor es inferior (el valor por defecto en RHEL 8 es 65530):

```bash
# Aplicar el cambio inmediatamente (sin reiniciar)
sudo sysctl -w vm.max_map_count=262144

# Hacerlo persistente entre reinicios del servidor
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf

# Verificar que el archivo fue creado correctamente
cat /etc/sysctl.d/99-elasticsearch.conf
```

Resultado esperado del último comando:

```
vm.max_map_count=262144
```

Verificar que el valor está activo:

```bash
sysctl vm.max_map_count
```

Resultado esperado: `vm.max_map_count = 262144`

## 4.3 Crear la estructura de directorios

```bash
# Directorio de datos de Elasticsearch en el filesystem /data
sudo mkdir -p /data/elasticsearch

# Directorio de configuración de Elasticsearch
sudo mkdir -p /etc/observabilidad/elasticsearch

# Elasticsearch corre internamente como UID 1000 (usuario elasticsearch).
# El directorio de datos debe ser propiedad de ese UID.
sudo chown -R 1000:1000 /data/elasticsearch

# Verificar los permisos
ls -la /data/ | grep elasticsearch
```

Resultado esperado:

```
drwxr-xr-x 2 1000 1000 4096 may 20 10:00 elasticsearch
```

## 4.4 Configurar el firewall

Elasticsearch usa dos puertos TCP:

- **9200**: API HTTP (consultas, indexación, administración).
- **9300**: Protocolo de transporte para comunicación entre nodos del cluster (solo PROD).

```bash
# Puerto API HTTP (requerido en todos los entornos)
sudo firewall-cmd --permanent --add-port=9200/tcp

# Puerto de transporte entre nodos (solo para PROD — cluster de 3 nodos)
sudo firewall-cmd --permanent --add-port=9300/tcp

# Aplicar los cambios
sudo firewall-cmd --reload

# Verificar los puertos habilitados
sudo firewall-cmd --list-ports
```

Resultado esperado (PROD):

```
9200/tcp 9300/tcp
```

# 5. Creación del archivo de credenciales

Elasticsearch 8.x requiere una contraseña para el usuario `elastic` (superusuario). En el entorno de VM, las credenciales se almacenan en un archivo de variables de entorno protegido por permisos del sistema operativo.

> **🔴 IMPORTANTE:** Este archivo contiene la contraseña en texto plano. Nunca subirlo a un repositorio de código. Protegerlo con permisos estrictos.

## 5.1 Crear el archivo de credenciales

```bash
sudo tee /etc/observabilidad/elasticsearch/elasticsearch.env > /dev/null <<EOF
ELASTIC_PASSWORD=<ELASTIC_PASSWORD>
EOF
```

## 5.2 Asegurar los permisos del archivo

```bash
# Solo el propietario root puede leer el archivo
sudo chmod 600 /etc/observabilidad/elasticsearch/elasticsearch.env
sudo chown root:root /etc/observabilidad/elasticsearch/elasticsearch.env

# Verificar los permisos
ls -la /etc/observabilidad/elasticsearch/elasticsearch.env
```

Resultado esperado:

```
-rw------- 1 root root 35 may 20 10:05 /etc/observabilidad/elasticsearch/elasticsearch.env
```

# 6. Configuración de Elasticsearch

La configuración de Elasticsearch se define en un archivo `elasticsearch.yml` que se monta dentro del contenedor. La estructura del archivo varía entre entornos:

- **DEV y QA**: nodo único (`discovery.type: single-node`). Sin comunicación entre nodos.
- **PROD**: cluster de 3 nodos. Cada nodo tiene su propio `elasticsearch.yml` con su nombre y la lista de nodos del cluster.

## 6.1 Configuración para DEV y QA (single-node)

```bash
sudo tee /etc/observabilidad/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-<ENV>
node.name: es-node-<ENV>
network.host: 0.0.0.0
http.port: 9200
discovery.type: single-node
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
EOF
```

Donde `<ENV>` es `dev` para DEV o `qa` para QA. Ver valores exactos en los Anexos A y B.

## 6.2 Configuración para PROD — Nodo 1 (Servidor 3)

```bash
sudo tee /etc/observabilidad/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-prod
node.name: es-node-1
network.host: 0.0.0.0
http.port: 9200
transport.port: 9300
discovery.seed_hosts:
  - <IP_ES_NODE1>:9300
  - <IP_ES_NODE2>:9300
  - <IP_ES_NODE3>:9300
cluster.initial_master_nodes:
  - es-node-1
  - es-node-2
  - es-node-3
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
EOF
```

## 6.3 Configuración para PROD — Nodo 2 (Servidor 4)

```bash
sudo tee /etc/observabilidad/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-prod
node.name: es-node-2
network.host: 0.0.0.0
http.port: 9200
transport.port: 9300
discovery.seed_hosts:
  - <IP_ES_NODE1>:9300
  - <IP_ES_NODE2>:9300
  - <IP_ES_NODE3>:9300
cluster.initial_master_nodes:
  - es-node-1
  - es-node-2
  - es-node-3
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
EOF
```

## 6.4 Configuración para PROD — Nodo 3 (Servidor 5)

```bash
sudo tee /etc/observabilidad/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-prod
node.name: es-node-3
network.host: 0.0.0.0
http.port: 9200
transport.port: 9300
discovery.seed_hosts:
  - <IP_ES_NODE1>:9300
  - <IP_ES_NODE2>:9300
  - <IP_ES_NODE3>:9300
cluster.initial_master_nodes:
  - es-node-1
  - es-node-2
  - es-node-3
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
EOF
```

> **NOTA sobre `cluster.initial_master_nodes`:** Este parámetro solo se usa durante la formación inicial del cluster (bootstrap). Una vez que los 3 nodos se han unido por primera vez, este parámetro ya no tiene efecto. Es seguro dejarlo en el archivo.

## 6.5 Verificar el archivo de configuración

```bash
cat /etc/observabilidad/elasticsearch/elasticsearch.yml
```

Confirmar que el contenido coincide con el entorno que se está configurando.

# 7. Despliegue con Quadlet

Quadlet permite a Podman gestionar contenedores como servicios de systemd. Se define un archivo `.container` que describe cómo ejecutar el contenedor, y systemd lo administra igual que cualquier otro servicio del sistema.

## 7.1 Crear el archivo Quadlet

Los archivos Quadlet del sistema se ubican en `/etc/containers/systemd/`. Crear el archivo para Elasticsearch:

```bash
sudo tee /etc/containers/systemd/elasticsearch.container > /dev/null <<EOF
[Unit]
Description=Elasticsearch - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<ES_IMAGE>
ContainerName=elasticsearch
EnvironmentFile=/etc/observabilidad/elasticsearch/elasticsearch.env
Volume=/data/elasticsearch:/usr/share/elasticsearch/data:Z
Volume=/etc/observabilidad/elasticsearch/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml:ro,Z
PublishPort=9200:9200
PublishPort=9300:9300
Environment=ES_JAVA_OPTS=<ES_JAVA_OPTS>

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=120

[Install]
WantedBy=multi-user.target
EOF
```

Donde:
- `<ES_IMAGE>` es la imagen a usar según el escenario (ver Anexos A, B y C).
- `<ES_JAVA_OPTS>` es la configuración del heap JVM (ver Anexos A, B y C).
- Las opciones `:Z` en los volúmenes permiten que SELinux autorice el acceso del contenedor a los directorios del host.

## 7.2 Recargar systemd y habilitar el servicio

Después de crear o modificar un archivo Quadlet, es necesario recargar los daemons de systemd para que detecte el nuevo servicio:

```bash
# Recargar systemd para detectar el archivo .container
sudo systemctl daemon-reload

# Habilitar el servicio para que arranque automáticamente al iniciar el servidor
sudo systemctl enable elasticsearch

# Iniciar el servicio
sudo systemctl start elasticsearch
```

> **NOTA:** Elasticsearch tarda entre 30 y 60 segundos en iniciar completamente. Es normal que el servicio aparezca como `activating` durante ese tiempo.

## 7.3 Verificar que el servicio está activo

```bash
sudo systemctl status elasticsearch
```

Resultado esperado:

```
● elasticsearch.service - Elasticsearch - Stack de Observabilidad ONP
     Loaded: loaded (/etc/containers/systemd/elasticsearch.container; generated)
     Active: active (running) since Tue 2026-05-20 10:15:00 PET; 45s ago
   Main PID: 12345 (conmon)
```

El campo `Active: active (running)` confirma que el contenedor está en ejecución.

## 7.4 Iniciar el cluster PROD (los 3 nodos)

En PROD, el cluster solo se forma cuando los 3 nodos están activos simultáneamente. El orden de arranque es:

1. Iniciar el servicio en los 3 servidores (Secciones 7.1 y 7.2), preferiblemente en paralelo o en rápida sucesión.
2. El cluster puede tardar hasta 2 minutos en elegir un nodo master y quedar en estado `green`.

```bash
# Verificar que el cluster se formó correctamente (ejecutar en cualquier nodo)
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:9200/_cluster/health?pretty
```

El campo `"number_of_nodes": 3` confirma que los 3 nodos se unieron al cluster.

# 8. Verificación del despliegue

## 8.1 Verificar los logs de Elasticsearch

```bash
# Ver los últimos 50 registros del log
sudo journalctl -u elasticsearch --no-pager -n 50
```

Al iniciar correctamente, los logs muestran líneas como:

```
{"@timestamp":"2026-05-20T10:15:30.000Z","log.level":"INFO","message":"started","service.name":"ES_ECS","cluster.name":"onp-es-dev","node.name":"es-node-dev"}
```

La línea clave es `"message":"started"`. Si no aparece después de 90 segundos, revisar la Sección 10 de Troubleshooting.

Para seguir los logs en tiempo real:

```bash
sudo journalctl -u elasticsearch -f
```

Presionar `Ctrl+C` para detener el seguimiento.

## 8.2 Verificar el health del cluster

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:9200/_cluster/health?pretty
```

Resultado esperado para **DEV y QA** (single-node):

```json
{
  "cluster_name" : "onp-es-dev",
  "status" : "green",
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 0,
  "unassigned_shards" : 0
}
```

Resultado esperado para **PROD** (cluster 3 nodos):

```json
{
  "cluster_name" : "onp-es-prod",
  "status" : "green",
  "number_of_nodes" : 3,
  "number_of_data_nodes" : 3,
  "active_primary_shards" : 0,
  "unassigned_shards" : 0
}
```

El campo `"status": "green"` confirma que el cluster está sano. Un valor `"yellow"` indica que hay shards sin réplica (normal en single-node). Un valor `"red"` indica problema — revisar la Sección 10.

## 8.3 Verificar la autenticación

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/
```

Resultado esperado:

```json
{
  "name" : "es-node-dev",
  "cluster_name" : "onp-es-dev",
  "version" : {
    "number" : "8.19.15"
  },
  "tagline" : "You Know, for Search"
}
```

Si la respuesta es `401 Unauthorized`, la contraseña es incorrecta. Verificar el contenido del archivo `/etc/observabilidad/elasticsearch/elasticsearch.env`.

## 8.4 Verificar los índices existentes

Cuando el OTEL Collector y Jaeger estén instalados y operativos, se pueden listar los índices creados:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:9200/_cat/indices?v
```

Resultado esperado (una vez el stack completo esté operativo):

```
health status index                          uuid   pri rep docs.count
green  open   onp-logs-development-000001    ...    1   0   1247
green  open   jaeger-onp-span-2026-05-20     ...    1   0   3892
```

# 9. Configuración de retención y compresión

Sin una política de gestión de datos, los índices crecen indefinidamente hasta agotar el disco. Esta sección configura:

1. Un **index template** con `best_compression` que se aplica automáticamente a todos los índices nuevos.
2. Una **política ILM** con rollover cada 30 días y eliminación a los 12 meses.
3. El **bootstrap inicial** del índice con un alias de escritura para que el Collector apunte al alias y el rollover funcione de forma transparente.

> **⚠️ IMPORTANTE:** Ejecutar estos pasos **antes** de que el OTEL Collector comience a enviar logs. Los índices creados antes de aplicar el template no heredan la configuración de compresión ni la política ILM automáticamente.

> **NOTA sobre la retención de trazas:** Jaeger crea sus propios índices (`jaeger-onp-*`). Para aplicar la misma política de 12 meses a las trazas, crear un template adicional para el patrón `jaeger-onp-*`. Ver Sección 9.5.

## 9.1 ¿Qué es best_compression?

Elasticsearch soporta dos codecs de compresión para almacenar datos en disco:

| Codec | Algoritmo | Velocidad | Tamaño en disco |
|-------|-----------|-----------|-----------------|
| `default` | LZ4 | Más rápido | Mayor |
| `best_compression` | zstd/deflate | Levemente más lento | 30–40% menor |

El impacto en velocidad de `best_compression` es perceptible principalmente en escrituras intensivas. Para datos de logs y trazas históricos, el ahorro en disco supera ampliamente ese costo.

## 9.2 Crear la política ILM

La política ILM define las reglas automáticas del ciclo de vida de los índices:

- **Fase hot**: el índice recibe escrituras activamente. El rollover ocurre cuando el índice tiene 30 días o supera 50 GB — lo que ocurra primero.
- **Fase warm**: el índice ya no recibe escrituras. Se compacta (force merge) para liberar espacio adicional.
- **Fase delete**: el índice se elimina cuando tiene 365 días desde su creación.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_ilm/policy/onp-12months-policy" \
  -H "Content-Type: application/json" \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {
            "rollover": {
              "max_age": "30d",
              "max_primary_shard_size": "50gb"
            }
          }
        },
        "warm": {
          "min_age": "30d",
          "actions": {
            "forcemerge": {
              "max_num_segments": 1
            }
          }
        },
        "delete": {
          "min_age": "365d",
          "actions": {
            "delete": {}
          }
        }
      }
    }
  }'
```

Resultado esperado:

```json
{"acknowledged":true}
```

## 9.3 Crear el index template para logs

El index template aplica automáticamente `best_compression` y la política ILM a todos los índices que coincidan con el patrón `onp-logs-*`:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_index_template/onp-logs-template" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["onp-logs-*"],
    "template": {
      "settings": {
        "codec": "best_compression",
        "index.lifecycle.name": "onp-12months-policy",
        "index.lifecycle.rollover_alias": "onp-logs-<ENV>",
        "number_of_shards": 1,
        "number_of_replicas": <REPLICAS>
      },
      "mappings": {
        "properties": {
          "duration_ms":               { "type": "long" },
          "http.response.status_code": { "type": "short" }
        }
      }
    }
  }'
```

Donde:
- `<ENV>` es el nombre del entorno: `development`, `quality` o `production`.
- `<REPLICAS>` es `0` para DEV y QA (single-node, no hay nodo para la réplica), o `1` para PROD (cada shard tiene una copia en otro nodo).

> **NOTA sobre los mappings explícitos:** Los campos `duration_ms` y `http.response.status_code` del log canónico llegan a Elasticsearch como strings (provienen del MDC, que solo maneja strings). Sin un mapping explícito, ES los clasificaría como `keyword` y las queries de rango en Kibana (`duration_ms > 1000`, `http.response.status_code >= 400`) no funcionarían. El mapping explícito como `long` y `short` hace que Elasticsearch coercione el string al tipo numérico automáticamente.

Resultado esperado:

```json
{"acknowledged":true}
```

## 9.4 Crear el index template para trazas (Jaeger)

Aplicar la misma política y compresión a los índices de Jaeger:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_index_template/jaeger-onp-template" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["jaeger-onp-*"],
    "template": {
      "settings": {
        "codec": "best_compression",
        "index.lifecycle.name": "onp-12months-policy",
        "number_of_shards": 1,
        "number_of_replicas": <REPLICAS>
      }
    }
  }'
```

> **NOTA:** Los índices de Jaeger no usan alias de escritura porque Jaeger los gestiona directamente con fechas (ej. `jaeger-onp-span-2026-05-20`). Por esto no se configura `rollover_alias` para el template de Jaeger — la política ILM los eliminará por edad (365 días) sin rollover.

Resultado esperado:

```json
{"acknowledged":true}
```

## 9.5 Bootstrap del índice inicial con alias de escritura

Para que el rollover funcione, el OTEL Collector debe escribir en un **alias**, no en un índice directamente. El alias `onp-logs-<ENV>` apunta siempre al índice activo más reciente. Cuando el índice llega a 30 días o 50 GB, ILM crea el siguiente índice automáticamente y mueve el alias — sin intervención manual y sin que el Collector cambie su configuración.

Crear el primer índice y configurar el alias de escritura:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/onp-logs-<ENV>-000001" \
  -H "Content-Type: application/json" \
  -d '{
    "aliases": {
      "onp-logs-<ENV>": {
        "is_write_index": true
      }
    }
  }'
```

Donde `<ENV>` es `development`, `quality` o `production`.

Resultado esperado:

```json
{"acknowledged":true,"shards_acknowledged":true,"index":"onp-logs-development-000001"}
```

## 9.6 Verificar la política, el template y el alias

Verificar que la política ILM fue creada:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_ilm/policy/onp-12months-policy?pretty" | \
  grep -A5 '"delete"'
```

Resultado esperado:

```json
"delete" : {
  "min_age" : "365d",
  "actions" : {
    "delete" : { }
  }
}
```

Verificar que el index template fue creado:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_index_template/onp-logs-template?pretty" | \
  grep -E '"codec"|"lifecycle"'
```

Resultado esperado:

```
"codec" : "best_compression",
"index.lifecycle.name" : "onp-12months-policy",
```

Verificar que el alias de escritura está activo:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_cat/aliases?v&name=onp-logs-<ENV>"
```

Resultado esperado:

```
alias              index                        filter routing.index routing.search is_write_index
onp-logs-development onp-logs-development-000001 -      -            -              true
```

La columna `is_write_index: true` confirma que el alias está listo para recibir escrituras.

## 9.7 Cómo ajustar la retención sin redeploy

Para cambiar el período de retención en cualquier momento (por ejemplo, de 12 meses a 6 meses), re-ejecutar el comando de la Sección 9.2 con el nuevo valor en el campo `"min_age"` de la fase `delete`:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_ilm/policy/onp-12months-policy" \
  -H "Content-Type: application/json" \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {
            "rollover": {
              "max_age": "30d",
              "max_primary_shard_size": "50gb"
            }
          }
        },
        "warm": {
          "min_age": "30d",
          "actions": {
            "forcemerge": {
              "max_num_segments": 1
            }
          }
        },
        "delete": {
          "min_age": "180d",
          "actions": {
            "delete": {}
          }
        }
      }
    }
  }'
```

ILM aplica el cambio en el siguiente ciclo de evaluación, que ocurre cada 10 minutos por defecto. No es necesario reiniciar Elasticsearch ni el OTEL Collector.

Para cambiar el intervalo de rollover (por ejemplo, de 30 días a 7 días), modificar el campo `"max_age"` de la fase `hot` y re-ejecutar el mismo comando.

## 9.8 Agregar nuevos patrones de índice

Los index templates creados en las Secciones 9.3 y 9.4 cubren únicamente los patrones `onp-logs-*` y `jaeger-onp-*`. Si en el futuro se incorporan nuevos componentes que generen índices con un patrón diferente (por ejemplo `onp-metrics-*` o el nombre de un nuevo servicio), esos índices **no heredarán** automáticamente `best_compression` ni la política ILM.

Para cubrirlos, crear un nuevo index template específico para ese patrón:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_index_template/<NOMBRE_TEMPLATE>" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["<NUEVO_PATRON>"],
    "template": {
      "settings": {
        "codec": "best_compression",
        "index.lifecycle.name": "onp-12months-policy",
        "number_of_shards": 1,
        "number_of_replicas": <REPLICAS>
      }
    }
  }'
```

Donde:
- `<NOMBRE_TEMPLATE>` es un nombre descriptivo del template (ej. `onp-metrics-template`).
- `<NUEVO_PATRON>` es el patrón de índices a cubrir (ej. `onp-metrics-*`).
- `<REPLICAS>` es `0` para DEV y QA (single-node) o `1` para PROD (cluster 3 nodos).

Resultado esperado:

```json
{"acknowledged":true}
```

> **NOTA:** No es necesario crear una nueva política ILM. La política `onp-12months-policy` ya existe y puede reutilizarse para cualquier patrón nuevo. Si el nuevo componente requiere una retención diferente (por ejemplo 6 meses en vez de 12), crear primero una nueva política con `PUT /_ilm/policy/<NOMBRE_POLITICA>` y referenciarla en el template.

# 10. Troubleshooting

## 10.1 El servicio no arranca — vm.max_map_count insuficiente

### Síntoma

```bash
sudo systemctl status elasticsearch
# Active: failed (Result: exit-code)

sudo journalctl -u elasticsearch -n 20
# max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

### Causa y solución

El parámetro del kernel no fue configurado correctamente. Ejecutar:

```bash
sudo sysctl -w vm.max_map_count=262144
sudo systemctl start elasticsearch
```

Para que persista entre reinicios, verificar que el archivo `/etc/sysctl.d/99-elasticsearch.conf` existe y contiene `vm.max_map_count=262144`.

## 10.2 Error de permisos en /data/elasticsearch

### Síntoma

```bash
sudo journalctl -u elasticsearch -n 20
# java.io.IOException: failed to obtain node locks, tried [/usr/share/elasticsearch/data]
# ... Permission denied
```

### Causa y solución

El directorio `/data/elasticsearch` no tiene el propietario correcto. Elasticsearch corre internamente como UID 1000:

```bash
sudo chown -R 1000:1000 /data/elasticsearch
sudo systemctl restart elasticsearch
```

## 10.3 Error de autenticación (401 Unauthorized)

### Síntoma

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/
# {"error":{"reason":"missing authentication credentials"},"status":401}
```

### Causas y soluciones

- La contraseña en el archivo `.env` no coincide con la que se usó al crear el contenedor por primera vez. Si Elasticsearch ya inicializó su base de datos con una contraseña anterior, cambiar la contraseña en el `.env` no la actualiza automáticamente.

  Para resetear la contraseña:

  ```bash
  # Entrar al contenedor
  sudo podman exec -it elasticsearch bash
  
  # Dentro del contenedor, cambiar la contraseña del usuario elastic
  bin/elasticsearch-reset-password -u elastic -i
  ```

- Verificar el contenido del archivo env:

  ```bash
  sudo cat /etc/observabilidad/elasticsearch/elasticsearch.env
  ```

## 10.4 Los nodos PROD no se unen al cluster

### Síntoma

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/_cluster/health?pretty
# "number_of_nodes": 1   (en vez de 3)
```

### Causas y soluciones

- **Puerto 9300 bloqueado entre servidores**: verificar que el firewall permite el tráfico en el puerto 9300 desde los otros nodos:

  ```bash
  sudo firewall-cmd --list-ports
  # Debe mostrar 9300/tcp
  
  # Probar conectividad al puerto 9300 del nodo destino
  curl -v telnet://<IP_ES_NODE2>:9300
  ```

- **Las IPs en `discovery.seed_hosts` son incorrectas**: verificar el contenido del `elasticsearch.yml` en cada nodo:

  ```bash
  cat /etc/observabilidad/elasticsearch/elasticsearch.yml | grep seed_hosts
  ```

- **Los nombres de nodo en `cluster.initial_master_nodes` no coinciden con `node.name`**: cada nodo debe tener exactamente el mismo valor en `cluster.initial_master_nodes` que en su propio `node.name`. El valor es case-sensitive.

## 10.5 El servicio arranca pero el health es red

### Síntoma

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/_cluster/health?pretty
# "status": "red"
```

### Causa y solución

Estado `red` indica que hay shards primarios no asignados. En un cluster recién creado, esto puede ocurrir si los templates o índices del bootstrap tienen una configuración de réplicas superior al número de nodos disponibles.

Verificar los shards no asignados:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_cat/shards?v&h=index,shard,prirep,state,unassigned.reason"
```

Si el `unassigned.reason` es `INDEX_CREATED` y hay un shard primario sin asignar, el nodo que debería tenerlo no está disponible. Verificar el estado de todos los nodos.

## 10.6 Comandos útiles de diagnóstico rápido

| Diagnóstico | Comando |
|-------------|---------|
| Estado del servicio | `sudo systemctl status elasticsearch` |
| Logs en tiempo real | `sudo journalctl -u elasticsearch -f` |
| Últimos 50 logs | `sudo journalctl -u elasticsearch -n 50` |
| Health del cluster | `curl -u elastic:<PASSWORD> http://localhost:9200/_cluster/health?pretty` |
| Estado de los nodos | `curl -u elastic:<PASSWORD> http://localhost:9200/_cat/nodes?v` |
| Listar índices | `curl -u elastic:<PASSWORD> http://localhost:9200/_cat/indices?v` |
| Ver aliases | `curl -u elastic:<PASSWORD> http://localhost:9200/_cat/aliases?v` |
| Ver política ILM | `curl -u elastic:<PASSWORD> http://localhost:9200/_ilm/policy/onp-12months-policy?pretty` |
| Reiniciar ES | `sudo systemctl restart elasticsearch` |
| Ver uso de disco | `df -h /data/elasticsearch` |

# Anexo A — Configuración específica para DEV

| Parámetro | Valor para DEV |
|-----------|----------------|
| Servidor | Servidor 1 (stack completo) |
| Tipo de cluster | Single-node |
| Heap JVM | -Xms2g -Xmx2g |
| Réplicas de shards | 0 |
| Retención ILM | 365d |

## A.1 Preparar el servidor DEV

```bash
# Configurar vm.max_map_count
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf
sudo sysctl -w vm.max_map_count=262144

# Crear directorios
sudo mkdir -p /data/elasticsearch /etc/observabilidad/elasticsearch
sudo chown -R 1000:1000 /data/elasticsearch

# Firewall (solo HTTP, no hay cluster)
sudo firewall-cmd --permanent --add-port=9200/tcp
sudo firewall-cmd --reload
```

## A.2 Valores para DEV

Usar los siguientes valores al completar los pasos del manual:

| Placeholder | Valor DEV |
|-------------|-----------|
| \<ES_IMAGE\> | `docker.elastic.co/elasticsearch/elasticsearch:8.19.15` (Escenario B) |
| \<ES_JAVA_OPTS\> | `-Xms2g -Xmx2g` |
| \<ENV\> (elasticsearch.yml) | `dev` |
| \<ENV\> (índice) | `development` |
| \<REPLICAS\> | `0` |

## A.3 Archivo elasticsearch.yml para DEV

```yaml
cluster.name: onp-es-dev
node.name: es-node-dev
network.host: 0.0.0.0
http.port: 9200
discovery.type: single-node
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
```

## A.4 Archivo Quadlet para DEV

```ini
[Unit]
Description=Elasticsearch - Stack de Observabilidad ONP (DEV)
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=docker.elastic.co/elasticsearch/elasticsearch:8.19.15
ContainerName=elasticsearch
EnvironmentFile=/etc/observabilidad/elasticsearch/elasticsearch.env
Volume=/data/elasticsearch:/usr/share/elasticsearch/data:Z
Volume=/etc/observabilidad/elasticsearch/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml:ro,Z
PublishPort=9200:9200
Environment=ES_JAVA_OPTS=-Xms2g -Xmx2g

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=120

[Install]
WantedBy=multi-user.target
```

# Anexo B — Configuración específica para QA

| Parámetro | Valor para QA |
|-----------|---------------|
| Servidor | Servidor 2 (ES + Kibana) |
| Tipo de cluster | Single-node |
| Heap JVM | -Xms2g -Xmx2g |
| Réplicas de shards | 0 |
| Retención ILM | 365d |

## B.1 Preparar el servidor QA (Servidor 2)

```bash
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf
sudo sysctl -w vm.max_map_count=262144
sudo mkdir -p /data/elasticsearch /etc/observabilidad/elasticsearch
sudo chown -R 1000:1000 /data/elasticsearch
sudo firewall-cmd --permanent --add-port=9200/tcp
sudo firewall-cmd --reload
```

## B.2 Valores para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<ES_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15` (Escenario A) |
| \<ES_JAVA_OPTS\> | `-Xms2g -Xmx2g` |
| \<ENV\> (elasticsearch.yml) | `qa` |
| \<ENV\> (índice) | `quality` |
| \<REPLICAS\> | `0` |

## B.3 Archivo elasticsearch.yml para QA

```yaml
cluster.name: onp-es-qa
node.name: es-node-qa
network.host: 0.0.0.0
http.port: 9200
discovery.type: single-node
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
```

## B.4 Archivo Quadlet para QA

```ini
[Unit]
Description=Elasticsearch - Stack de Observabilidad ONP (QA)
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15
ContainerName=elasticsearch
EnvironmentFile=/etc/observabilidad/elasticsearch/elasticsearch.env
Volume=/data/elasticsearch:/usr/share/elasticsearch/data:Z
Volume=/etc/observabilidad/elasticsearch/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml:ro,Z
PublishPort=9200:9200
Environment=ES_JAVA_OPTS=-Xms2g -Xmx2g

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=120

[Install]
WantedBy=multi-user.target
```

# Anexo C — Configuración específica para PROD

| Parámetro | Valor para PROD |
|-----------|-----------------|
| Servidores | Servidor 3 (ES node 1), Servidor 4 (ES node 2 + Kibana), Servidor 5 (ES node 3 + Jaeger) |
| Tipo de cluster | Cluster 3 nodos |
| Heap JVM — Servidor 3 | -Xms4g -Xmx4g (ES dedicado) |
| Heap JVM — Servidor 4 | -Xms3g -Xmx3g (comparte con Kibana) |
| Heap JVM — Servidor 5 | -Xms3g -Xmx3g (comparte con Jaeger) |
| Réplicas de shards | 1 |
| Retención ILM | 365d |

## C.1 Preparar los 3 servidores PROD

Ejecutar en **cada uno** de los 3 servidores (Servidores 3, 4 y 5):

```bash
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf
sudo sysctl -w vm.max_map_count=262144
sudo mkdir -p /data/elasticsearch /etc/observabilidad/elasticsearch
sudo chown -R 1000:1000 /data/elasticsearch
sudo firewall-cmd --permanent --add-port=9200/tcp
sudo firewall-cmd --permanent --add-port=9300/tcp
sudo firewall-cmd --reload
```

## C.2 Valores para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<ES_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15` |
| \<REPLICAS\> | `1` |
| \<ENV\> (índice) | `production` |

## C.3 Archivos Quadlet para PROD

**Servidor 3 (ES node 1 — dedicado):**

```ini
[Unit]
Description=Elasticsearch Node 1 - Stack de Observabilidad ONP (PROD)
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15
ContainerName=elasticsearch
EnvironmentFile=/etc/observabilidad/elasticsearch/elasticsearch.env
Volume=/data/elasticsearch:/usr/share/elasticsearch/data:Z
Volume=/etc/observabilidad/elasticsearch/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml:ro,Z
PublishPort=9200:9200
PublishPort=9300:9300
Environment=ES_JAVA_OPTS=-Xms4g -Xmx4g

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=180

[Install]
WantedBy=multi-user.target
```

**Servidor 4 (ES node 2 — comparte con Kibana) y Servidor 5 (ES node 3 — comparte con Jaeger):**

Mismo archivo pero con `ES_JAVA_OPTS=-Xms3g -Xmx3g`.

## C.4 Orden de arranque del cluster PROD

1. Crear los archivos `elasticsearch.yml` específicos de cada nodo (Secciones 6.2, 6.3 y 6.4) en sus respectivos servidores.
2. Crear los archivos Quadlet en los 3 servidores.
3. Ejecutar en los 3 servidores (en paralelo o en rápida sucesión, dentro de un margen de 2 minutos):

   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now elasticsearch
   ```

4. Verificar el cluster desde cualquier nodo:

   ```bash
   curl -u elastic:<ELASTIC_PASSWORD> \
     http://localhost:9200/_cluster/health?pretty
   ```

   Esperar hasta que `"status"` sea `"green"` y `"number_of_nodes"` sea `3`.

# Anexo D — Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen en una máquina con internet

```bash
# Descargar la imagen
podman pull docker.elastic.co/elasticsearch/elasticsearch:8.19.15

# Exportar a archivo .tar
podman save docker.elastic.co/elasticsearch/elasticsearch:8.19.15 \
  -o elasticsearch-8.19.15.tar
```

## D.2 Transferir el archivo al servidor destino

```bash
scp elasticsearch-8.19.15.tar <USUARIO>@<IP_SERVIDOR>:/tmp/
```

## D.3 Importar la imagen en el servidor destino

```bash
# Importar la imagen en el almacén de Podman del sistema
sudo podman load -i /tmp/elasticsearch-8.19.15.tar

# Verificar que la imagen quedó disponible
sudo podman images | grep elasticsearch
```

Resultado esperado:

```
docker.elastic.co/elasticsearch/elasticsearch   8.19.15   abc123def456   2 weeks ago   1.1 GB
```

## D.4 Usar la imagen importada en el archivo Quadlet

En el campo `Image` del archivo `.container`, usar el nombre exacto de la imagen tal como aparece en `podman images`:

```ini
Image=docker.elastic.co/elasticsearch/elasticsearch:8.19.15
```

No se requiere login a ningún registry — Podman usará la imagen local directamente.
