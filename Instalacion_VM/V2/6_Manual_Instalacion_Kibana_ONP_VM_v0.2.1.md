**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Kibana en Servidor Virtual (RHEL 8)**

Instalación nativa sobre Red Hat Enterprise Linux 8

| **Versión:**        | 0.2.1      |
|---------------------|------------|
| **Fecha:**          | 2026-07-03 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.2.1       | 2026-07-03 | \<AUTOR\>   | Actualización de pasos para PROD |
| 0.2.0       | 2026-06-26 | \<AUTOR\>   | Reescritura para instalación nativa vía RPM, sin runtime de contenedores |
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Kibana en servidores virtuales RHEL 8 on-premise, usando el paquete RPM oficial de Elastic y el sistema de gestión de servicios systemd nativo de RHEL 8.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.

## 1.2 ¿Qué es Kibana?

Kibana es la interfaz de visualización y análisis de Elasticsearch. Permite explorar, filtrar y visualizar los datos almacenados en Elasticsearch a través de dashboards interactivos.

En el stack ONP, Kibana se usa principalmente para:

- **Consultar logs** de los servicios Spring Boot almacenados en los índices `onp-logs-*`.
- **Correlacionar logs con trazas** usando los campos `traceId` y `spanId` incluidos en cada línea de log.
- **Gestionar la configuración de Elasticsearch** (políticas ILM, index templates, Data Views) a través de su UI.

## 1.3 Rol de Kibana en el stack ONP

```
OTEL Collector → Elasticsearch (onp-logs-*)
                        ↑
                     Kibana ← Equipo de desarrollo / operaciones
```

## 1.4 Arquitectura por entorno

| Entorno | Nodos Prometheus | Tipo de cluster | Servidores |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 |
| QA      | 1        | Single-node     | Servidor 2 |
| PROD    | 1        | Single-node     | Servidor 4 |

> **NOTA PROD:** Kibana comparte el Servidor 4 con el nodo ES node 2. Con 8 GB de RAM y un heap ES de 3 GB, quedan aproximadamente 4 GB disponibles para OS + Kibana. Kibana consume típicamente entre 500 MB y 1 GB de RAM, por lo que la coexistencia es viable.

## 1.5 Versión de Kibana

Kibana **debe tener la misma versión principal que Elasticsearch**. Si Elasticsearch es `8.19.15`, Kibana debe ser `8.19.15`. Versiones incompatibles producen errores al conectarse.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall).
- Instalación de Kibana vía RPM.
- Configuración de Kibana.
- Verificación del despliegue.

Queda fuera del alcance:

- Creación de dashboards avanzados.
- Configuración de autenticación multi-usuario en Kibana.

# 2. Prerrequisitos

Elasticsearch debe estar instalado, operativo y con el usuario `elastic` funcionando antes de instalar Kibana.
Kibana se conecta a Elasticsearch al arrancar para verificar compatibilidad de versiones.

## 2.1 Infraestructura requerida

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 2 | 4 | 8 GB | 50 GB |
| PROD | Servidor 4 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos

| Componente |
|------------|
| Elasticsearch |

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| curl | Cualquier versión | `curl --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<KIBANA_VERSION\> | Versión de Kibana (debe coincidir con ES) | 8.19.15 |
| \<ES_HOST\> | IP o hostname del servidor Elasticsearch | |
| \<ES_PORT\> | Puerto de Elasticsearch | 9200 |
| \<ELASTIC_PASSWORD\> | Contraseña del usuario `elastic` de ES | |
| \<KIBANA_ENCRYPTION_KEY\> | Clave de cifrado para sesiones Kibana (32+ chars) | |

## Cómo generar la clave de cifrado de Kibana

Kibana requiere una clave de cifrado aleatoria de al menos 32 caracteres para proteger las sesiones y los datos almacenados en ES. Generar una:

```bash
openssl rand -hex 32
```

El resultado es una cadena de 64 caracteres hexadecimales. Usar esa cadena como valor de `<KIBANA_ENCRYPTION_KEY>`.
Anotar y guardar el valor generado, ya que podría ser necesario posteriormente.

## 2.5 Verificaciones previas
Elasticsearch debe estar instalado y operativo antes de instalar Kibana.

Ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://<ES_HOST>:9200/
```

Resultado esperado (ejemplo para desarrollo):

```json
{
  "name" : "es-node-dev",
  "cluster_name" : "onp-es-dev",
  "cluster_uuid" : "v-u9upG6Qiq_zm0BuhyyHw",
  "version" : {
    "number" : "8.19.15",
    "build_flavor" : "default",
    "build_type" : "rpm",
    "build_hash" : "d9256c374e649e04ff0fa2dafd43402d35a3fb3a",
    "build_date" : "2026-04-28T13:06:49.648073236Z",
    "build_snapshot" : false,
    "lucene_version" : "9.12.2",
    "minimum_wire_compatibility_version" : "7.17.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "You Know, for Search"
}
```

La versión debe ser `8.19.15`.

### Establecer la contraseña del usuario kibana_system

Para que Kibana pueda integrarse con ES se requiere establecer la contraseña del usuario a utilizar.
> **NOTA:** Únicamente este comando debe ejecutarse en el servidor donde está instalado Elasticsearch (para **DEV** o **QA**). Para **PROD** debe ejecutarse solamente en un nodo de Elasticsearch.
El resto de comandos de este documento se deben ejecutar en el servidor donde se está instalando Kibana.

```bash
# Esperar a que ES esté listo (puede tardar hasta 60 segundos)
sudo /usr/share/elasticsearch/bin/elasticsearch-reset-password -u kibana_system -i
```

El flag `-i` (interactive) solicita la nueva contraseña por consola. Ingresar `<KIBANA_PASSWORD>` cuando se solicite.

Resultado esperado:

```
his tool will reset the password of the [kibana_system] user.
You will be prompted to enter the password.
Please confirm that you would like to continue [y/N]y
Enter password for [kibana_system]: 
Re-enter password for [kibana_system]: 
Password for the [kibana_system] user successfully reset.
```

# 3. Preparación del servidor

## 3.1 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 5601 | TCP | UI web de Kibana y API REST |

```bash
sudo firewall-cmd --permanent --add-port=5601/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

> **NOTA:** La apertura de puertos mediante firewalld únicamente habilita el acceso a nivel del sistema operativo RHEL. Si el servidor se ejecuta dentro de una máquina virtual, deberá verificarse además la configuración de red del hipervisor (VMware, VirtualBox, Hyper-V, KVM u otro) y de los dispositivos de red intermedios para garantizar que los puertos publicados sean accesibles desde los sistemas que requieran conectarse al servicio.

# 4. Instalación del RPM

## 4.1 Instalar el paquete RPM

```bash
sudo rpm -ivh /tmp/onp-packages/rpm/kibana-8.19.15-x86_64.rpm
```

La instalación:

- Crea el usuario y grupo del sistema `kibana`.
- Instala los binarios en `/usr/share/kibana/`.
- Crea el directorio de configuración `/etc/kibana/`.
- Registra la unidad systemd `kibana.service`.

# 5. Configuración de Kibana

## 5.1 Crear el archivo de configuración
Ejecutar el siguiente comando, pero antes cambiar el valor de:
- `<ENV>`: el cual debe ser `dev` para DEV, `qa` para QA y `prod` para PROD.
- `<KIBANA_PASSWORD>` es el password del usuario kibana_system asignado previamente.
- `<ES_HOST>` es el host donde se instaló previamente ElasticSearch.
- `<ES_PORT>` es el puerto del host donde previamente se instaló ElasticSearch (Puerto asignado | 9200).
- `<KIBANA_ENCRYPTION_KEY>` es la clave generada previamente.

**Nota:** Para **PROD** el valor de `elasticsearch.hosts` debe ser una lista separada por comas para considerar a los 3 nodos de Elasticsearch.
Ejemplo:
`elasticsearch.hosts: ["http://<ES_HOST1>:<ES_PORT>","http://<ES_HOST2>:<ES_PORT>","http://<ES_HOST3>:<ES_PORT>"]`

```bash
sudo tee /etc/kibana/kibana.yml > /dev/null <<EOF
server.host: "0.0.0.0"
server.port: 5601
server.name: "kibana-onp-<ENV>"

elasticsearch.hosts: ["http://<ES_HOST>:<ES_PORT>"]
elasticsearch.username: "kibana_system"
elasticsearch.password: "<KIBANA_PASSWORD>"

xpack.encryptedSavedObjects.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.security.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.reporting.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"

i18n.locale: "en"
EOF
```

> **NOTA sobre las claves de cifrado:** Las tres claves (`encryptedSavedObjects`, `security`, `reporting`) pueden usar el mismo valor generado en la Sección 2.4. Kibana no verifica que sean distintas; usar el mismo valor es válido para este despliegue.

## 5.2 Asegurar los permisos del archivo

```bash
sudo chmod 640 /etc/kibana/kibana.yml
sudo chown root:kibana /etc/kibana/kibana.yml
sudo ls -la /etc/kibana/kibana.yml
```

Resultado esperado:

```
-rw-r----- 1 root kibana ... kibana.yml
```

## 5.3 Comprobación obligatoria de configuración HTTP
Verificar que el archivo /etc/kibana/kibana.yml contiene únicamente HTTP en la configuración de Elasticsearch:

```bash
sudo grep elasticsearch.hosts /etc/kibana/kibana.yml
```

Resultado esperado (ejemplo para desarrollo):

```
elasticsearch.hosts: ["http://localhost:9200"]
```

Si aparece `https`, corregirlo manualmente editando el archivo y reemplazandolo por `http`.

# 6. Arranque del servicio

## 6.1 Habilitar e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable kibana
sudo systemctl start kibana
```

> **NOTA:** Kibana tarda entre 60 y 120 segundos en iniciar completamente. Es normal que el servicio aparezca como `activating` durante ese tiempo.

## 6.2 Verificar que el servicio está activo
Esperar un par de minutos y luego ejecutar el siguiente comando.

```bash
sudo systemctl status kibana
```

Resultado esperado después de 90-120 segundos:

```
● kibana.service - Kibana
     Loaded: loaded (/usr/lib/systemd/system/kibana.service; enabled)
     Active: active (running) since ...
```

# 7. Verificación del despliegue

## 7.1 Verificar el health check de Kibana

Ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/status
```

Resultado esperado (fragmento de ejemplo para desarrollo):

```json
{
  "name": "kibana-onp-dev",
  "status": {
    "overall": {
      "level": "available",
      "summary": "All services are available"
    }
  }
}
```

## 7.3 Acceder a la UI de Kibana

Abrir el navegador en:

```
http://<IP_SERVIDOR_KIBANA>:5601
```

Ingresar con:
- **Usuario:** `elastic`
- **Contraseña:** `<ELASTIC_PASSWORD>`


## 7.4 Crear el Data View para logs de ONP

Un Data View le indica a Kibana qué índices de Elasticsearch mostrar en la sección Discover.

1. En la UI de Kibana, ir a **Stack Management** → **Data Views** → **Create data view**.
2. Completar el formulario:
   - **Name:** `ONP Logs - <ENTORNO>` (ej. `ONP Logs - Development`)
   - **Index pattern:** `onp-logs-*`
   - **Timestamp field:** `@timestamp`
3. Hacer clic en **Save data view to Kibana**.

Una vez creado, ir a **Discover** y seleccionar el Data View recién creado, luego filtrar por tiempo (segun corresponda a los datos de prueba enviados anteriormente durante la instalación del Colector) y presionar el botón "Refresh".
Los logs enviados anteriormente durante la instalación del OTEL Collector aparecerán en la línea de tiempo.

En este punto finaliza la instalación.


# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<ES_HOST\> | `localhost` o IP del Servidor 1 |
| \<ES_PORT\> | `9200` |
| \<ENV\> (server.name) | `dev` |

## A.1 kibana.yml completo para DEV

```yaml
server.host: "0.0.0.0"
server.port: 5601
server.name: "kibana-onp-dev"

elasticsearch.hosts: ["http://localhost:9200"]
elasticsearch.username: "elastic"
elasticsearch.password: "<ELASTIC_PASSWORD>"

xpack.encryptedSavedObjects.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.security.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.reporting.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"

i18n.locale: "en"
```

# Anexo B — Configuración específica para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<ES_HOST\> | IP del Servidor 2 (ES corre en el mismo Servidor 2) |
| \<ES_PORT\> | `9200` |
| \<ENV\> (server.name) | `qa` |

## B.1 kibana.yml completo para QA

```yaml
server.host: "0.0.0.0"
server.port: 5601
server.name: "kibana-onp-qa"

elasticsearch.hosts: ["http://<IP_SERVIDOR2>:9200"]
elasticsearch.username: "elastic"
elasticsearch.password: "<ELASTIC_PASSWORD>"

xpack.encryptedSavedObjects.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.security.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.reporting.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"

i18n.locale: "en"
```

# Anexo C — Configuración específica para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<ES_HOST\> | IP del Servidor 4 (ES node 2 corre en el mismo Servidor 4) |
| \<ES_PORT\> | `9200` |
| \<ENV\> (server.name) | `prod` |

## C.1 kibana.yml completo para PROD

```yaml
server.host: "0.0.0.0"
server.port: 5601
server.name: "kibana-onp-prod"

elasticsearch.hosts: ["http://<IP_SERVIDOR4>:9200"]
elasticsearch.username: "elastic"
elasticsearch.password: "<ELASTIC_PASSWORD>"

xpack.encryptedSavedObjects.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.security.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.reporting.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"

i18n.locale: "en"
```
