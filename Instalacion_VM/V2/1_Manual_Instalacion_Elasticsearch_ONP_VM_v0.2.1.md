**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Elasticsearch (ES) en Servidor Virtual (RHEL 8)**

Instalación nativa sobre Red Hat Enterprise Linux 8

| **Versión:**        | 0.2.1     |
|---------------------|------------|
| **Fecha:**          | 2026-07-03 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.2.1       | 2026-07-03 | \<AUTOR\>   | Actualización de pasos para PROD |
| 0.2.0       | 2026-06-17 | \<AUTOR\>   | Reescritura para instalación nativa vía RPM, sin runtime de contenedores |
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Elasticsearch en servidores virtuales RHEL 8 on-premise, usando el paquete RPM oficial de Elastic y el sistema de gestión de servicios systemd nativo de RHEL 8.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.
Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Elasticsearch?

Elasticsearch es un motor de búsqueda y análisis distribuido basado en Apache Lucene. Permite almacenar, buscar y analizar grandes volúmenes de datos en tiempo real. En el stack ONP actúa como backend de almacenamiento compartido para los siguientes componentes:

- **OTEL Collector**: envía logs de los servicios al índice `onp-logs-<entorno>`.
- **Jaeger**: almacena las trazas distribuidas en los índices `onp-jaeger-*`.
- **Kibana**: consulta y visualiza los logs almacenados.

## 1.3 Rol de Elasticsearch en el stack ONP

```
OTEL Collector
    └── logs pipeline   → Elasticsearch (índice: onp-logs-*)

Jaeger
    └── traces storage  → Elasticsearch (índices: onp-jaeger-*)

Kibana          → consulta onp-logs-*
Grafana         → consulta métricas vía Prometheus
```

Elasticsearch es un componente de **infraestructura compartida**: múltiples componentes del stack dependen de él.

## 1.4 Arquitectura por entorno

| Entorno | Nodos ES | Tipo de cluster | Servidores |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 |
| QA      | 1        | Single-node     | Servidor 2 |
| PROD    | 3        | Cluster HA      | Servidores 3, 4 y 5 |

> **Por qué 3 nodos en PROD:** Con 2 nodos, Elasticsearch no puede alcanzar quórum de forma confiable si un nodo cae — el cluster puede quedar en estado `red` y rechazar escrituras. Con 3 nodos, el sistema tolera la pérdida de 1 nodo sin interrupciones.

## 1.5 Instalación nativa con RPM

El paquete RPM oficial de Elasticsearch para RHEL 8 incluye:

- El binario de Elasticsearch con la JVM embebida (no se requiere instalar Java por separado).
- La creación automática del usuario y grupo del sistema `elasticsearch`.
- Una unidad systemd lista para uso con `systemctl`.
- La estructura de directorios de configuración en `/etc/elasticsearch/`.

## 1.6 Configuración de seguridad

Elasticsearch 8.x tiene la seguridad habilitada por defecto. En el stack ONP se usa la siguiente configuración:

| Parámetro | Valor | Motivo |
|-----------|-------|--------|
| `xpack.security.enabled` | true | Autenticación activa (usuario elastic) |
| `xpack.security.http.ssl.enabled` | false | TLS HTTP desactivado para simplificar la comunicación interna en red corporativa |
| `xpack.security.transport.ssl.enabled` | false | TLS transport desactivado (red interna confiable) |

## 1.7 Estrategia de retención y compresión

Este manual configura Elasticsearch con:

- **best_compression**: codec de compresión que reduce el tamaño de los índices entre 30 y 40% adicional respecto al codec por defecto (LZ4).
- **ILM (Index Lifecycle Management)**: política automática que controla el ciclo de vida de los índices. Se configura con rollover a los 30 días y eliminación a los 12 meses.
- **Rollover configurable sin redeploy**: la política ILM se actualiza vía API REST. No es necesario reiniciar Elasticsearch para cambiar los períodos de retención.

## 1.8 Alcance de este manual

Este manual cubre:

- Preparación del servidor (kernel, directorios, firewall).
- Instalación de Elasticsearch via RPM en RHEL 8.
- Configuración del servicio systemd nativo.
- Verificación del despliegue.
- Configuración de retención: 'best_compression', ILM y rollover.

Queda fuera del alcance:

- Habilitación de TLS HTTP o transport entre nodos.
- Gestión de usuarios adicionales de Elasticsearch.
- Configuración de snapshots a almacenamiento externo.

# 2. Prerequisitos

> **⚠️ ADVERTENCIA:** Verificar TODOS los prerequisitos antes de comenzar la instalación. Una verificación incompleta es la causa más común de fallos durante el despliegue.

## 2.1 Infraestructura requerida por entorno

| Entorno | Servidores | vCPU | RAM | Filesystem /data |
|---------|------------|------|-----|------------------|
| DEV     | 1 (Servidor 1) | 4    | 8 GB | 50 GB |
| QA      | 1 (Servidor 2) | 4 | 8 GB | 50 GB |
| PROD    | 3 (Servidores 3, 4 y 5) | 4 c/u | 8 GB c/u | 100 GB c/u |

> **NOTA:** El filesystem `/data` debe estar montado y disponible antes de comenzar. Verificar con `df -h /data`. Si no aparece, contactar al equipo de infraestructura.

## 2.2 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| curl | Cualquier versión reciente | `curl --version` |

> **NOTA:** El paquete RPM de Elasticsearch incluye su propia JVM. No es necesario instalar Java por separado.

## 2.3 Accesos requeridos

- Acceso SSH al servidor con usuario con privilegios `sudo`.
- El archivo RPM de Elasticsearch en `/tmp/onp-packages/rpm/` (ver `0_Preparacion_Paquetes_VM_v0.2.0.md`).
- Solo para PROD, acceso de red entre los 3 nodos (puertos 9200 y 9300).

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<ELASTIC_PASSWORD\> | Definir la contraseña que se asignará al usuario administrador `elastic` | |
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

### 2.5.2 Verificar la disponibilidad del RPM

```bash
ls -lh /tmp/onp-packages/rpm/elasticsearch-8.19.15-x86_64.rpm
```

Resultado esperado: el archivo existe con un tamaño cercano a 648 MB.

### 2.5.3 Verificar conectividad entre nodos (solo PROD)

Ejecutar desde el Servidor 3 hacia los otros dos nodos:

```bash
ping -c 3 <IP_ES_NODE2>
ping -c 3 <IP_ES_NODE3>
```
Además del ping realizar también una prueba específica del puerto 9300.

Ambos deben responder. Si alguno no responde, verificar las reglas de firewall entre servidores con el equipo de infraestructura antes de continuar.

# 3. Preparación del servidor

Estos pasos se ejecutan en **cada servidor** donde se instalará Elasticsearch.

## 3.1 Configurar vm.max_map_count

Elasticsearch requiere que el parámetro del kernel `vm.max_map_count` sea al menos `262144`.

```bash
# Verificar el valor actual
sysctl vm.max_map_count
```

Si el valor es inferior a 262144 (el valor por defecto en RHEL 8 es 65530):

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

## 3.2 Crear el directorio de datos

```bash
# Directorio de datos de Elasticsearch en el filesystem /data
sudo mkdir -p /data/elasticsearch
```

Los permisos del directorio se asignarán después de la instalación del RPM, cuando el usuario `elasticsearch` exista en el sistema.

## 3.3 Configurar el firewall

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

> **NOTA:** La apertura de puertos mediante firewalld únicamente habilita el acceso a nivel del sistema operativo RHEL. Si el servidor se ejecuta dentro de una máquina virtual, deberá verificarse además la configuración de red del hipervisor (VMware, VirtualBox, Hyper-V, KVM u otro) y de los dispositivos de red intermedios para garantizar que los puertos publicados sean accesibles desde los sistemas que requieran conectarse al servicio.

# 4. Instalación del RPM

## 4.1 Instalar el paquete RPM

```bash
sudo rpm -ivh /tmp/onp-packages/rpm/elasticsearch-8.19.15-x86_64.rpm
```

La instalación:

- Crea el usuario y grupo del sistema `elasticsearch`.
- Instala los binarios en `/usr/share/elasticsearch/`.
- Crea el directorio de configuración `/etc/elasticsearch/`.
- Registra la unidad systemd `elasticsearch.service`.

Resultado esperado al final de la instalación (no ejecutar los comandos que se muestran):

```
### NOT starting on installation, please execute the following statements to configure elasticsearch service to start automatically using systemd
 sudo systemctl daemon-reload
 sudo systemctl enable elasticsearch.service
### You can start elasticsearch service by executing
 sudo systemctl start elasticsearch.service
```

## 4.2 Asignar permisos al directorio de datos

El usuario `elasticsearch` fue creado por el RPM. Asignarle la propiedad del directorio de datos:

```bash
sudo chown -R elasticsearch:elasticsearch /data/elasticsearch

# Verificar los permisos
ls -la /data/ | grep elasticsearch
```

Resultado esperado (el tamaño y fecha puede variar):

```
drwxr-xr-x 2 elasticsearch elasticsearch 4096 jun 17 10:00 elasticsearch
```

# 5. Configuración de Elasticsearch

La configuración de Elasticsearch se define en `/etc/elasticsearch/elasticsearch.yml` y la estructura del archivo varía entre entornos:

- **DEV y QA**: nodo único (`discovery.type: single-node`). Sin comunicación entre nodos.
- **PROD**: cluster de 3 nodos. Cada nodo tiene su propio `elasticsearch.yml` con su nombre y la lista de nodos del cluster.

## 5.1 Configuración para DEV y QA (single-node)
Ejecutar el siguiente comando, pero antes cambiar el valor de `<ENV>`, el cual debe ser `dev` para DEV o `qa` para QA. Ver valores exactos en los Anexos A y B.

```bash
sudo tee /etc/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-<ENV>
node.name: es-node-<ENV>
network.host: 0.0.0.0
http.port: 9200
discovery.type: single-node
path.data: /data/elasticsearch
path.logs: /var/log/elasticsearch
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
EOF
```


## 5.2 Configuración para PROD — Nodo 1 (Servidor 3)

Ejecutar los siguientes comandos para generar y copiar las llaves.

```bash
sudo /usr/share/elasticsearch/bin/elasticsearch-certutil cert --silent --pem --self-signed --out /tmp/elastic-certificates.zip
sudo unzip /tmp/elastic-certificates.zip -d /tmp/elastic-certs/
sudo mkdir -p /etc/elasticsearch/certs/
sudo cp /tmp/elastic-certs/instance/instance.crt /tmp/elastic-certs/instance/instance.key /etc/elasticsearch/certs/ 
```

Ejecutar los siguientes comandos para copiar los 2 archivos creados hacia los otros dos nodos en /tmp, para ello reemplazar `<USUARIO>`, `<IP_ES_NODE2>` y `<IP_ES_NODE3>`.

```bash
scp /tmp/elastic-certs/instance/instance.crt /tmp/elastic-certs/instance/instance.key <USUARIO>@<IP_ES_NODE2>:/tmp/
scp /tmp/elastic-certs/instance/instance.crt /tmp/elastic-certs/instance/instance.key <USUARIO>@<IP_ES_NODE3>:/tmp/
```

Eliminar el directorio y archivo temporal.

```bash
sudo rm -rf /tmp/elastic-certs/
sudo rm /tmp/elastic-certificates.zip
```

Asignar la propiedad al usuario del proceso.

```bash
sudo chown -R elasticsearch:elasticsearch /etc/elasticsearch/certs/
sudo chmod 750 /etc/elasticsearch/certs/
sudo chmod -R u=rw,g=r,o= /etc/elasticsearch/certs/
```

Ejecutar el siguiente comando, reemplazando los valores de `<IP_ES_NODE1>`, `<IP_ES_NODE2>` y `<IP_ES_NODE3>`

```bash
sudo tee /etc/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-prod
node.name: es-node-1
network.host: <IP_ES_NODE1>
http.port: 9200
transport.port: 9300
path.data: /data/elasticsearch
path.logs: /var/log/elasticsearch
discovery.seed_hosts:
  - "<IP_ES_NODE1>"
  - "<IP_ES_NODE2>"
  - "<IP_ES_NODE3>"
cluster.initial_master_nodes:
  - es-node-1
  - es-node-2
  - es-node-3

# --- CAPA DE SEGURIDAD INTERNA ---
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false

# Cifrado TLS para el puerto de transporte 9300
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate
xpack.security.transport.ssl.key: certs/instance.key
xpack.security.transport.ssl.certificate: certs/instance.crt
EOF
```

## 5.3 Configuración para PROD — Nodo 2 (Servidor 4)

Ejecutar los siguientes comandos para copiar las llaves.

```bash
sudo mkdir -p /etc/elasticsearch/certs/
sudo cp /tmp/instance.crt /tmp/instance.key /etc/elasticsearch/certs/ 
sudo rm /tmp/instance.crt
sudo rm /tmp/instance.key
```

Asignar la propiedad al usuario del proceso.

```bash
sudo chown -R elasticsearch:elasticsearch /etc/elasticsearch/certs/
sudo chmod 750 /etc/elasticsearch/certs/
sudo chmod -R u=rw,g=r,o= /etc/elasticsearch/certs/
```

Ejecutar el siguiente comando, reemplazando los valores de `<IP_ES_NODE1>`, `<IP_ES_NODE2>` y `<IP_ES_NODE3>`

```bash
sudo tee /etc/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-prod
node.name: es-node-2
network.host: <IP_ES_NODE2>
http.port: 9200
transport.port: 9300
path.data: /data/elasticsearch
path.logs: /var/log/elasticsearch
discovery.seed_hosts:
  - "<IP_ES_NODE1>"
  - "<IP_ES_NODE2>"
  - "<IP_ES_NODE3>"
cluster.initial_master_nodes:
  - es-node-1
  - es-node-2
  - es-node-3

# --- CAPA DE SEGURIDAD INTERNA ---
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false

# Cifrado TLS para el puerto de transporte 9300
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate
xpack.security.transport.ssl.key: certs/instance.key
xpack.security.transport.ssl.certificate: certs/instance.crt
EOF
```

## 5.4 Configuración para PROD — Nodo 3 (Servidor 5)

Ejecutar los siguientes comandos para copiar las llaves.

```bash
sudo mkdir -p /etc/elasticsearch/certs/
sudo cp /tmp/instance.crt /tmp/instance.key /etc/elasticsearch/certs/ 
sudo rm /tmp/instance.crt
sudo rm /tmp/instance.key
```

Asignar la propiedad al usuario del proceso.

```bash
sudo chown -R elasticsearch:elasticsearch /etc/elasticsearch/certs/
sudo chmod 750 /etc/elasticsearch/certs/
sudo chmod -R u=rw,g=r,o= /etc/elasticsearch/certs/
```

Ejecutar el siguiente comando, reemplazando los valores de `<IP_ES_NODE1>`, `<IP_ES_NODE2>` y `<IP_ES_NODE3>`

```bash
sudo tee /etc/elasticsearch/elasticsearch.yml > /dev/null <<EOF
cluster.name: onp-es-prod
node.name: es-node-3
network.host: <IP_ES_NODE3>
http.port: 9200
transport.port: 9300
path.data: /data/elasticsearch
path.logs: /var/log/elasticsearch
discovery.seed_hosts:
  - "<IP_ES_NODE1>"
  - "<IP_ES_NODE2>"
  - "<IP_ES_NODE3>"
cluster.initial_master_nodes:
  - es-node-1
  - es-node-2
  - es-node-3

# --- CAPA DE SEGURIDAD INTERNA ---
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false

# Cifrado TLS para el puerto de transporte 9300
xpack.security.transport.ssl.enabled: true
xpack.security.transport.ssl.verification_mode: certificate
xpack.security.transport.ssl.key: certs/instance.key
xpack.security.transport.ssl.certificate: certs/instance.crt
EOF
```

> **NOTA sobre `cluster.initial_master_nodes`:** Este parámetro solo se usa durante la formación inicial del cluster (bootstrap). Una vez que los 3 nodos se han unido por primera vez, este parámetro ya no tiene efecto. Mantenerlo no suele causar problemas en operación normal, pero se recomienda retirarlo después de la formación inicial del cluster.

## 5.5 Configurar el heap JVM

La memoria JVM se configura en un archivo dedicado dentro de `/etc/elasticsearch/jvm.options.d/`. Crear el archivo con los valores del entorno:

```bash
sudo tee /etc/elasticsearch/jvm.options.d/heap.options > /dev/null <<EOF
-Xms<HEAP_SIZE>
-Xmx<HEAP_SIZE>
EOF
```

Donde `<HEAP_SIZE>` es el tamaño del heap. Ver valores por entorno en los Anexos A, B y C (Secciones A.4, B.4 y C.3).

> **REGLA del heap:** El heap nunca debe superar el 50% de la RAM disponible para Elasticsearch, ni exceder 32 GB.

## 5.6 Asegurar los permisos de configuración

Comandos para **DEV** y **QA**.

```bash
# Definir a root como dueño de la estructura y a elasticsearch como el grupo
sudo chown -R root:elasticsearch /etc/elasticsearch/
sudo chown -R elasticsearch:elasticsearch /etc/elasticsearch/elasticsearch.keystore

# Asegurar los accesos correctos del sistema y del servicio
sudo chmod 755 /etc/elasticsearch/
sudo chmod 640 /etc/elasticsearch/elasticsearch.yml
sudo chmod 660 /etc/elasticsearch/elasticsearch.keystore
```

Comandos para **PROD**.

```bash
# Asignación de propietarios (Proteger la raíz y liberar los componentes dinámicos)
sudo chown -R root:elasticsearch /etc/elasticsearch/
sudo chown -R elasticsearch:elasticsearch /etc/elasticsearch/certs/
sudo chown -R elasticsearch:elasticsearch /etc/elasticsearch/elasticsearch.keystore

# Asignación de privilegios (Lectura global al directorio base, hermético para las llaves)
sudo chmod 755 /etc/elasticsearch/
sudo chmod 750 /etc/elasticsearch/certs/
sudo chmod 640 /etc/elasticsearch/elasticsearch.yml
sudo chmod 640 /etc/elasticsearch/certs/*
sudo chmod 660 /etc/elasticsearch/elasticsearch.keystore
```

## 5.7 Verificar el archivo de configuración

```bash
sudo cat /etc/elasticsearch/elasticsearch.yml
```

Confirmar que el contenido coincide con el entorno y configuración realizada hasta el momento.


> **NOTA:** Solamente para PROD, realizar los pasos anteriores hasta este punto en los servidores 3, 4 y 5. Luego de ello recién continuar con los pasos siguientes en paralelo en los tres servidores.

# 6. Arranque del servicio

## 6.1 Habilitar e iniciar el servicio

```bash
# Recargar los daemons de systemd para registrar el servicio
sudo systemctl daemon-reload

# Habilitar el servicio para que arranque automáticamente al iniciar el servidor
sudo systemctl enable elasticsearch

# Iniciar el servicio
sudo systemctl start elasticsearch
```

> **NOTA:** Elasticsearch tarda entre 30 y 60 segundos en iniciar completamente. Es normal que el servicio aparezca como `activating` durante ese tiempo.

## 6.2 Verificar que el servicio está activo

```bash
sudo systemctl status elasticsearch
```

Resultado esperado:

```
● elasticsearch.service - Elasticsearch
     Loaded: loaded (/usr/lib/systemd/system/elasticsearch.service; enabled)
     Active: active (running) since Tue 2026-06-17 10:15:00 PET; 45s ago
   Main PID: 12345 (java)
```

El campo `Active: active (running)` confirma que Elasticsearch está en ejecución.

## 6.3 Establecer la contraseña del usuario elastic

En la primera arrancada, Elasticsearch genera automáticamente una contraseña para el usuario `elastic`. Resetearla a la contraseña definida en la planificación (`<ELASTIC_PASSWORD>`):

Para **DEV** y **QA** ejecutar el siguiente comando.

```bash
# Esperar a que ES esté listo (puede tardar hasta 60 segundos)
sudo /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic -i
```

Para **PROD** ejecutar el siguiente comando, solamente en uno de los nodos.
```bash
# Esperar a que ES esté listo (puede tardar hasta 60 segundos)
sudo /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic -i --url http://<IP_ES_NODE1>:9200
```

El flag `-i` (interactive) solicita la nueva contraseña por consola. Ingresar `<ELASTIC_PASSWORD>` cuando se solicite.

Resultado esperado:

```
This tool will reset the password of the [elastic] user.
You will be prompted to enter the password.
Please confirm that you would like to continue [y/N] y
Enter password for [elastic]: 
Re-enter password for [elastic]: 
Password for the [elastic] user successfully reset.
```

# 7. Verificación del despliegue

## 7.1 Verificar la autenticación

Para **DEV** y **QA** ejecutar el siguiente comando.

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.

Para **PROD** ejecutar los siguientes comandos.

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://<IP_ES_NODE1>:9200/
curl -u elastic:<ELASTIC_PASSWORD> http://<IP_ES_NODE2>:9200/
curl -u elastic:<ELASTIC_PASSWORD> http://<IP_ES_NODE3>:9200/
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>`, `<IP_ES_NODE2>` y `<IP_ES_NODE3>` son las direcciones IP de los 3 nodos de Elasticsearch.

Resultado esperado (ejemplo para desarrollo):

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

Si la respuesta es `401 Unauthorized`, la contraseña es incorrecta. Repetir el paso 6.3.

## 7.2 Verificar el health del cluster

Para **DEV** y **QA** ejecutar el siguiente comando.

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/_cluster/health?pretty
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.

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

Para **PROD** ejecutar los siguientes comandos.

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://<IP_ES_NODE1>:9200/_cluster/health?pretty
curl -u elastic:<ELASTIC_PASSWORD> http://<IP_ES_NODE2>:9200/_cluster/health?pretty
curl -u elastic:<ELASTIC_PASSWORD> http://<IP_ES_NODE3>:9200/_cluster/health?pretty
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>`, `<IP_ES_NODE2>` y `<IP_ES_NODE3>` son las direcciones IP de los 3 nodos de Elasticsearch.

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

Lo importante es que "status" sea "green" y que el nombre del cluster con el número de nodos coincida con lo definido para cada ambiente.

# 8. Configuración de retención y compresión

Sin una política de gestión de datos, los índices crecen indefinidamente hasta agotar el disco. Para evitar ello esta sección configura:

1. Un **index template** con `best_compression` que se aplica automáticamente a todos los índices nuevos.
2. Una **política ILM** con rollover cada 30 días y eliminación a los 12 meses.
3. El **bootstrap inicial** del índice con un alias de escritura para que el Collector apunte al alias y el rollover funcione de forma transparente.

## 8.1 Crear la política ILM

Para **DEV** y **QA** ejecutar el siguiente comando.

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
Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.

Resultado esperado: `{"acknowledged":true}`


Para **PROD** ejecutar el siguiente comando, solamente en uno de los nodos.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://<IP_ES_NODE1>:9200/_ilm/policy/onp-12months-policy" \
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
Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>` es la dirección IP del nodo 1 de ElasticSearch.

Resultado esperado: `{"acknowledged":true}`

## 8.2 Crear el index template para logs

Para **DEV** y **QA** ejecutar el siguiente comando.

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
        "number_of_replicas": 0
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

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<ENV>` es el nombre del entorno: `development` o `quality`.

Resultado esperado: `{"acknowledged":true}`

Para **PROD** ejecutar el siguiente comando, solamente en uno de los nodos.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://<IP_ES_NODE1>:9200/_index_template/onp-logs-template" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["onp-logs-*"],
    "template": {
      "settings": {
        "codec": "best_compression",
        "index.lifecycle.name": "onp-12months-policy",
        "index.lifecycle.rollover_alias": "onp-logs-production",
        "number_of_shards": 1,
        "number_of_replicas": 1
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

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>` es la dirección IP del nodo 1 de ElasticSearch.

Resultado esperado: `{"acknowledged":true}`


## 8.3 Bootstrap del índice inicial con alias de escritura

Para **DEV** y **QA** ejecutar el siguiente comando.

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

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<ENV>` es `development` o `quality`.

Resultado esperado: `{"acknowledged":true,"shards_acknowledged":true,"index":"onp-logs-development-000001"}`

Para **PROD** ejecutar el siguiente comando, solamente en uno de los nodos.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://<IP_ES_NODE1>:9200/onp-logs-production-000001" \
  -H "Content-Type: application/json" \
  -d '{
    "aliases": {
      "onp-logs-production": {
        "is_write_index": true
      }
    }
  }'
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>` es la dirección IP del nodo 1 de ElasticSearch.

Resultado esperado: `{"acknowledged":true,"shards_acknowledged":true,"index":"onp-logs-production-000001"}`


## 8.4 Verificar la política

Para **DEV** y **QA** ejecutar el siguiente comando.

```bash
# Verificar la política ILM
curl -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_ilm/policy/onp-12months-policy?pretty" | grep -A5 '"delete"'
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.

Para **PROD** ejecutar el siguiente comando, solamente en uno de los nodos.

```bash
# Verificar la política ILM
curl -u elastic:<ELASTIC_PASSWORD> \
  "http://<IP_ES_NODE1>:9200/_ilm/policy/onp-12months-policy?pretty" | grep -A5 '"delete"'
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>` es la dirección IP del nodo 1 de ElasticSearch.

Resultado esperado:
`
...
"delete" : {
          "min_age" : "365d",
...
`


## 8.5 Verificar el template

Para **DEV** y **QA** ejecutar el siguiente comando.

```bash
# Verificar el template
curl -u elastic:<ELASTIC_PASSWORD> \
"http://localhost:9200/_index_template/onp-logs-template?pretty"
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.

Resultado esperado (ejemplo para desarrollo):
`
...
"index" : {
              "lifecycle" : {
                "name" : "onp-12months-policy",
                "rollover_alias" : "onp-logs-development"
              },
              "codec" : "best_compression",
...
`

Para **PROD** ejecutar el siguiente comando, solamente en uno de los nodos.

```bash
# Verificar el template
curl -u elastic:<ELASTIC_PASSWORD> \
"http://<IP_ES_NODE1>:9200/_index_template/onp-logs-template?pretty"
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>` es la dirección IP del nodo 1 de ElasticSearch.

Resultado esperado:
`
...
"index" : {
              "lifecycle" : {
                "name" : "onp-12months-policy",
                "rollover_alias" : "onp-logs-production"
              },
              "codec" : "best_compression",
...
`

## 8.6 Verificar el alias

Para **DEV** y **QA** ejecutar el siguiente comando.

```bash
# Verificar el alias de escritura
curl -u elastic:<ELASTIC_PASSWORD> \
"http://localhost:9200/_alias/onp-logs-<ENV>?pretty"  
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<ENV>` es el nombre del entorno: `development` o `quality`.

Resultado esperado (ejemplo para desarrollo):
`
{
  "onp-logs-development-000001" : {
    "aliases" : {
      "onp-logs-development" : {
        "is_write_index" : true
      }
    }
  }
}
`

Para **PROD** ejecutar el siguiente comando.

```bash
# Verificar el alias de escritura
curl -u elastic:<ELASTIC_PASSWORD> \
"http://<IP_ES_NODE1>:9200/_alias/onp-logs-production?pretty"  
```

Donde se debe reemplazar lo siguiente:
- `<ELASTIC_PASSWORD>` es el password del usuario elastic.
- `<IP_ES_NODE1>` es la dirección IP del nodo 1 de ElasticSearch.

Resultado esperado:
`
{
  "onp-logs-production-000001" : {
    "aliases" : {
      "onp-logs-production" : {
        "is_write_index" : true
      }
    }
  }
}
`

El atributo `is_write_index: true` confirma que el alias está listo.
En este punto la instalación ha finalizado.


# Anexo A — Configuración específica para DEV

| Parámetro | Valor para DEV |
|-----------|----------------|
| Servidor | Servidor 1 (stack completo) |
| Tipo de cluster | Single-node |
| Heap JVM | -Xms2g / -Xmx2g |
| Réplicas de shards | 0 |
| Retención ILM | 365d |

## A.1 Preparar el servidor DEV

```bash
# Configurar vm.max_map_count
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf
sudo sysctl -w vm.max_map_count=262144

# Crear directorio de datos
sudo mkdir -p /data/elasticsearch

# Firewall (solo HTTP, no hay cluster)
sudo firewall-cmd --permanent --add-port=9200/tcp
sudo firewall-cmd --reload
```

## A.2 Valores para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<HEAP_SIZE\> | `2g` |
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
path.data: /data/elasticsearch
path.logs: /var/log/elasticsearch
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
```

## A.4 Archivo heap.options para DEV

```
/etc/elasticsearch/jvm.options.d/heap.options:

-Xms2g
-Xmx2g
```

# Anexo B — Configuración específica para QA

| Parámetro | Valor para QA |
|-----------|---------------|
| Servidor | Servidor 2 (ES + Kibana) |
| Tipo de cluster | Single-node |
| Heap JVM | -Xms2g / -Xmx2g |
| Réplicas de shards | 0 |
| Retención ILM | 365d |

## B.1 Preparar el servidor QA (Servidor 2)

```bash
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf
sudo sysctl -w vm.max_map_count=262144
sudo mkdir -p /data/elasticsearch
sudo firewall-cmd --permanent --add-port=9200/tcp
sudo firewall-cmd --reload
```

## B.2 Valores para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<HEAP_SIZE\> | `2g` |
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
path.data: /data/elasticsearch
path.logs: /var/log/elasticsearch
xpack.security.enabled: true
xpack.security.http.ssl.enabled: false
xpack.security.transport.ssl.enabled: false
```

## B.4 Archivo heap.options para QA

```
/etc/elasticsearch/jvm.options.d/heap.options:

-Xms2g
-Xmx2g
```

# Anexo C — Configuración específica para PROD

| Parámetro | Valor para PROD |
|-----------|-----------------|
| Servidores | Servidor 3 (ES node 1), Servidor 4 (ES node 2 + Kibana), Servidor 5 (ES node 3 + Jaeger) |
| Tipo de cluster | Cluster 3 nodos |
| Heap JVM — Servidor 3 | -Xms4g / -Xmx4g (ES dedicado) |
| Heap JVM — Servidor 4 | -Xms3g / -Xmx3g (comparte con Kibana) |
| Heap JVM — Servidor 5 | -Xms3g / -Xmx3g (comparte con Jaeger) |
| Réplicas de shards | 1 |
| Retención ILM | 365d |

## C.1 Preparar los 3 servidores PROD

Ejecutar en **cada uno** de los 3 servidores (Servidores 3, 4 y 5):

```bash
echo "vm.max_map_count=262144" | sudo tee /etc/sysctl.d/99-elasticsearch.conf
sudo sysctl -w vm.max_map_count=262144
sudo mkdir -p /data/elasticsearch
sudo firewall-cmd --permanent --add-port=9200/tcp
sudo firewall-cmd --permanent --add-port=9300/tcp
sudo firewall-cmd --reload
```

## C.2 Valores para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<REPLICAS\> | `1` |
| \<ENV\> (índice) | `production` |

## C.3 Archivos heap.options para PROD

**Servidor 3 (ES node 1 — dedicado):**

```
/etc/elasticsearch/jvm.options.d/heap.options:

-Xms4g
-Xmx4g
```

**Servidor 4 (ES node 2 — comparte con Kibana) y Servidor 5 (ES node 3 — comparte con Jaeger):**

```
/etc/elasticsearch/jvm.options.d/heap.options:

-Xms3g
-Xmx3g
```

## C.4 Orden de arranque del cluster PROD

1. Crear los archivos `elasticsearch.yml` específicos de cada nodo (Secciones 5.2, 5.3 y 5.4) en sus respectivos servidores.
2. Instalar el RPM y configurar el heap en los 3 servidores (Secciones 4 y 5.5).
3. Iniciar el servicio en los 3 servidores (en paralelo o en rápida sucesión, dentro de un margen de 2 minutos):

   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable --now elasticsearch
   ```

4. Establecer la contraseña `elastic` en los 3 nodos (Sección 6.3).
5. Verificar el cluster desde cualquier nodo:

   ```bash
   curl -u elastic:<ELASTIC_PASSWORD> \
     http://localhost:9200/_cluster/health?pretty
   ```

   Esperar hasta que `"status"` sea `"green"` y `"number_of_nodes"` sea `3`.
