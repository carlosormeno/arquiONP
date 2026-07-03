**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**OpenTelemetry Collector en Servidor Virtual (RHEL 8)**

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
| 0.2.0       | 2026-06-26 | \<AUTOR\>   | Reescritura para instalación nativa vía binario + Keepalived en QA/PROD, sin runtime de contenedores |
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación del OpenTelemetry Collector en servidores virtuales RHEL 8 on-premise, usando el binario oficial `otelcol-contrib`, una unidad systemd personalizada, y Keepalived para alta disponibilidad activo/pasivo en los entornos QA y PROD.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.

## 1.2 ¿Qué es el OpenTelemetry Collector?

El OpenTelemetry Collector es un componente intermediario que recibe, procesa y exporta telemetría (trazas, métricas y logs). Actúa como hub central del stack de observabilidad: los servicios de backend envían su telemetría al Collector, y este la enruta a los backends de almacenamiento correspondientes.

Ventajas de usar un Collector frente a exportar directamente desde los servicios:

- Los servicios quedan desacoplados de los backends. Si se cambia Jaeger por otro sistema, solo cambia la configuración del Collector, no el código de cada servicio.
- El Collector aplica procesamiento centralizado: batching, límites de memoria y enriquecimiento de atributos.
- Un único punto de configuración para controlar el flujo de telemetría de todos los servicios.

## 1.3 Rol del Collector en el stack ONP

```
Servicios Spring Boot (OTLP HTTP :4328)
              ↓
    OpenTelemetry Collector
    (VIP Keepalived en QA/PROD)
   ↙           ↓            ↘
Trazas       Métricas      Logs
  ↓          (Puerto 8889)   ↓
Jaeger       Prometheus  Elasticsearch
```

El pipeline del Collector se configura en tres señales separadas:

- **traces**: recibe trazas y las exporta a Jaeger vía OTLP gRPC (puerto 4317).
- **metrics**: recibe métricas y las expone en un endpoint Prometheus (puerto 8889).
- **logs**: recibe logs y los exporta a Elasticsearch en formato ECS.

## 1.4 Arquitectura de alta disponibilidad con Keepalived

En QA y PROD se despliegan **dos instancias del Collector** con Keepalived gestionando una **IP virtual (VIP)**. Solo un nodo está activo a la vez (activo/pasivo):

```
Servicios Spring Boot
        ↓
   VIP Keepalived (IP virtual flotante)
        ↓
  ┌─────────────────────────────────┐
  │  Nodo Activo    │  Nodo Pasivo  │
  │  (recibe tráfico)│  (en espera)  │
  └─────────────────────────────────┘
```

| Entorno | Nodos Colector | Tipo de cluster | Servidores |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 |
| QA      | 2        | Cluster HA     | Servidor 1, 2 |
| PROD    | 2        | Cluster HA     | Servidor 1, 2 |

## 1.5 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios).
- Instalación del binario `otelcol-contrib` y creación de la unidad systemd.
- Configuración del pipeline del Collector.
- Instalación y configuración sin  Keepalived para DEV.
- Instalación y configuración de Keepalived para HA activo/pasivo (QA y PROD).
- Verificación del despliegue y del flujo de datos.

# 2. Prerrequisitos

- Elasticsearch y Jaeger deben estar instalados y operativos antes de instalar el Collector.
- El Collector intentará conectarse a ellos al iniciar.
- Para QA y PROD, los dos servidores donde se instalará el colector deben pertenecer a la misma red y poder intercambiar paquetes VRRP. La VIP debe pertenecer al mismo segmento de red que las direcciones IP físicas de ambos nodos.

## 2.1 Infraestructura requerida por entorno

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 2 | 4 | 8 GB | 50 GB |
| PROD | Servidor 1 | 4 | 8 GB | 100 GB |
| PROD | Servidor 2 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos antes de instalar el Collector

| Componente |
|------------|
| Elasticsearch |
| Jaeger |

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| Keepalived | 2.x (solo para QA y PROD) | `keepalived --version` |
| curl | Cualquier versión | `curl --version` |
| tar | Cualquier versión | `tar --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<COLLECTOR_VERSION\> | Versión del Collector contrib | 0.120.0 |
| \<ES_HOST\> | IP o hostname del servidor Elasticsearch | |
| \<ES_PORT\> | Puerto de Elasticsearch | 9200 |
| \<ELASTIC_PASSWORD\> | Contraseña del usuario `elastic` de Elasticsearch (ES) | |
| \<JAEGER_HOST\> | IP o hostname del servidor Jaeger | |
| \<KEEPALIVED_PASSWORD\> | Es una contraseña compartida entre los dos nodos de Keepalived. (solo QA y PROD) | |
| \<VIP_COLLECTOR\> | Dirección IP virtual (VIP) administrada por Keepalived (solo QA y PROD). Debe ser una dirección IP libre, perteneciente a la misma subred que las direcciones IP físicas de ambos servidores, y no debe estar asignada ni ser utilizada por ningún otro dispositivo de la red.| |
| \<IP_COLLECTOR_NODE1\> | IP real del nodo activo (solo QA y PROD) | |
| \<IP_COLLECTOR_NODE2\> | IP real del nodo pasivo (solo QA y PROD) | |
| \<NETWORK_INTERFACE\> | Interfaz de red del servidor (ej. eth0, ens3)(solo QA y PROD) | |


### Cómo identificar la interfaz de red (solo QA y PROD)

```bash
ip link show
```

La interfaz de red es la que tiene la IP del servidor. Generalmente aparece como `eth0`, `ens3`, `ens192` u otro nombre.

## 2.5 Verificaciones previas

### 2.5.1 Verificar Elasticsearch

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Resultado esperado: `{"status":"green",...}` o `{"status":"yellow",...}`.

### 2.5.2 Verificar Jaeger

```bash
curl http://<JAEGER_HOST>:14269/
```

Resultado esperado: {"status":"Server available",...}

# 3. Preparación del servidor

Estos pasos se ejecutan en **cada servidor** donde se instalará el Collector.

## 3.1 Crear el usuario del sistema

```bash
sudo useradd --no-create-home --shell /bin/false --system otel-collector

# Verificar la creación del usuario
id otel-collector
```

## 3.2 Crear la estructura de directorios

```bash
sudo mkdir -p /etc/observabilidad/otel-collector
sudo mkdir -p /tmp/otelcol-contrib
```

## 3.3 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 4327 | TCP | OTLP gRPC — recibe telemetría de los servicios |
| 4328 | TCP | OTLP HTTP — recibe telemetría de los servicios |
| 8889 | TCP | Métricas Prometheus — expone métricas para que Prometheus las recolecte |
| 13133 | TCP | Health check interno del Collector |

```bash
sudo firewall-cmd --permanent --add-port=4327/tcp
sudo firewall-cmd --permanent --add-port=4328/tcp
sudo firewall-cmd --permanent --add-port=8889/tcp
sudo firewall-cmd --permanent --add-port=13133/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

> **NOTA:** La apertura de puertos mediante firewalld únicamente habilita el acceso a nivel del sistema operativo RHEL. Si el servidor se ejecuta dentro de una máquina virtual, deberá verificarse además la configuración de red del hipervisor (VMware, VirtualBox, Hyper-V, KVM u otro) y de los dispositivos de red intermedios para garantizar que los puertos publicados sean accesibles desde los sistemas que requieran conectarse al servicio.

# 4. Instalación del binario
Estos pasos se ejecutan en cada servidor donde se instalará el Collector.

## 4.1 Extraer el archivo tar.gz

```bash
sudo tar xzf /tmp/onp-packages/bin/otelcol-contrib_0.120.0_linux_amd64.tar.gz \
  -C /tmp/otelcol-contrib/
```

## 4.2 Instalar el binario

```bash
# Copiar el binario al directorio del sistema
sudo cp /tmp/otelcol-contrib/otelcol-contrib /usr/local/bin/otelcol-contrib

# Asignar propietario y permisos
sudo chown otel-collector:otel-collector /usr/local/bin/otelcol-contrib
sudo chmod 755 /usr/local/bin/otelcol-contrib

# Verificar la instalación
/usr/local/bin/otelcol-contrib --version
```

Resultado esperado:

```
otelcol-contrib version 0.120.0
```

## 4.3 Limpiar archivos temporales

```bash
sudo rm -rf /tmp/otelcol-contrib/
```

# 5. Creación del rol y usuario de Elasticsearch para el Collector
Los pasos de esta sección deben ejecutarse en:
- DEV
- QA (**solamente al instalar en el Servidor 1** donde OTEL Collector está activo)
- PROD (**solamente al instalar en el Servidor 1** donde OTEL Collector está activo)

## 5.1 Crear el rol del Collector

El rol otorga permisos para:
- Consultar información básica del cluster.
- Crear índices de logs cuando sea necesario.
- Escribir documentos en los índices de logs.
- Consultar metadatos de los índices.

```bash
#Crear el archivo temporal de definición del rol
cat > /tmp/otel_collector_role.json <<'EOF'
{
  "cluster": [
    "monitor",
    "manage_index_templates"
  ],
  "indices": [
    {
      "names": [
        "onp-logs-*"
      ],
      "privileges": [
        "create_index",
        "create",
        "index",
        "write",
        "view_index_metadata",
        "read",
        "manage"
      ]
    }
  ]
}
EOF
```

```bash
#Verificar el contenido:
cat /tmp/otel_collector_role.json
```

## 5.2 Registrar el rol en Elasticsearch

Ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://<ES_HOST>:<ES_PORT>/_security/role/otel_collector_role" \
  -H "Content-Type: application/json" \
  -d @/tmp/otel_collector_role.json
```

Resultado esperado:

```
{"role":{"created":true}}
```

## 5.3 Verificar el rol creado
Ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_security/role/otel_collector_role?pretty
```

Resultado esperado:

```
{
  "otel_collector_role" : {
    "cluster" : [
      "monitor",
      "manage_index_templates"
    ],
    "indices" : [
      {
        "names" : [
          "onp-logs-*"
        ],
        "privileges" : [
          "create_index",
          "create",
          "index",
          "write",
          "view_index_metadata",
          "read",
          "manage"
        ],
        "allow_restricted_indices" : false
      }
    ],
    "applications" : [ ],
    "run_as" : [ ],
    "metadata" : { },
    "transient_metadata" : {
      "enabled" : true
    }
  }
}
```

## 5.4 Crear el usuario del Collector

Ejecutar el siguiente comando, reemplazando `<OTEL_ES_PASSWORD>` por la contraseña que se asignará al usuario del colector para conectarse a ElasticSearch. Anotar y guardar la contraseña porque podría ser necesaria posteriormente.

```bash
cat > /tmp/otel_collector_user.json <<'EOF'
{
  "password" : "<OTEL_ES_PASSWORD>",
  "roles" : [
    "otel_collector_role"
  ],
  "enabled": true,
  "full_name" : "OpenTelemetry Collector"
}
EOF
```

## 5.5 Registrar el usuario en Elasticsearch
El procedimiento creará el siguiente usuario de Elasticsearch, el cual será utilizado por el OpenTelemetry Collector para autenticarse contra Elasticsearch:

| Parámetro | Valor |
|------------|--------|
| Usuario | `otel_collector` |
| Rol asignado | `otel_collector_role` |

Ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X POST "http://<ES_HOST>:<ES_PORT>/_security/user/otel_collector" \
  -H "Content-Type: application/json" \
  -d @/tmp/otel_collector_user.json
```

Resultado esperado:

```
{"created":true}
```

## 5.6 Verificar el usuario creado
Ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_security/user/otel_collector?pretty
```

Resultado esperado:

```
{
  "otel_collector" : {
    "username" : "otel_collector",
    "roles" : [
      "otel_collector_role"
    ],
    "full_name" : "OpenTelemetry Collector",
    "email" : null,
    "metadata" : { },
    "enabled" : true
  }
}
```

## 5.7 Verificar la autenticación del usuario

Ejecutar el siguiente comando, reemplazando `<OTEL_ES_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

```bash
curl -u otel_collector:<OTEL_ES_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>
```

Resultado esperado (ejemplo para desarrollo):

```
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

La respuesta confirma que el usuario puede autenticarse correctamente contra Elasticsearch.

## 5.8 Eliminar los archivos temporales

```bash
rm -f /tmp/otel_collector_role.json
rm -f /tmp/otel_collector_user.json
```

# 6. Creación del archivo de credenciales

Estos pasos se ejecutan en cada servidor donde se instalará el Collector.

> **🔴 IMPORTANTE:** Este archivo contiene credenciales en texto plano. Nunca subirlo a un repositorio de código.

Ejecutar el siguiente comando, reemplazando `<OTEL_ES_PASSWORD>` por la contraseña del usuario otel_collector creado previamente.

```bash
sudo tee /etc/observabilidad/otel-collector/otel-collector.env > /dev/null <<EOF
OTEL_ES_USERNAME=otel_collector
OTEL_ES_PASSWORD=<OTEL_ES_PASSWORD>
EOF
```

Ejecutar los siguientes comandos para configurar permisos del archivo.

```bash
sudo chmod 600 /etc/observabilidad/otel-collector/otel-collector.env
sudo chown root:root /etc/observabilidad/otel-collector/otel-collector.env
ls -la /etc/observabilidad/otel-collector/otel-collector.env
```

Resultado esperado:

```
-rw------- 1 root root 65 jun 17 10:20 /etc/observabilidad/otel-collector/otel-collector.env
```

# 7. Configuración del pipeline (config.yaml)

Estos pasos se ejecutan en cada servidor donde se instalará el Collector.

## 7.1 Estructura del pipeline

| Señal | Receiver | Processors | Exporter |
|-------|----------|------------|----------|
| traces | otlp | memory_limiter, batch | otlp/jaeger, debug |
| metrics | otlp | memory_limiter, batch | prometheus |
| logs | otlp | memory_limiter, attributes/timestamp, batch | elasticsearch |

## 7.2 Crear el archivo config.yaml

Ejecutar el siguiente comando, reemplazando `<MEMORY_LIMIT_MIB>`, `<SPIKE_LIMIT_MIB>`, `<JAEGER_HOST>`, `<ES_HOST>`, `<ES_PORT>` y `<ENV>` con los valores correspondientes al ambiente, según lo indicado en los anexos, en los puntos A.1, B.1 y C.1.

Para **PROD** en la sección de endpoints para Elasticsearch se deben agregar dos endpoints más y sus valores se deben reemplazar por las direcciones IP y puerto de los 3 nodos de Elasticsearch. Ejemplo:

```bash
exporters:
...
  elasticsearch:
    endpoints:
      - http://<ES_HOST1>:<ES_PORT>
      - http://<ES_HOST2>:<ES_PORT>
      - http://<ES_HOST3>:<ES_PORT>
...
```


**NOTA:** Los valores `${env:OTEL_ES_USERNAME}` y `${env:OTEL_ES_PASSWORD}` **no son placeholders a reemplazar**. Es la sintaxis del OTEL Collector para leer variables de entorno en tiempo de ejecución. El Collector leerá automáticamente esos valores desde el archivo `otel-collector.env`.


```bash
sudo tee /etc/observabilidad/otel-collector/config.yaml > /dev/null <<EOF
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4327
      http:
        endpoint: 0.0.0.0:4328

processors:
  memory_limiter:
    limit_mib: <MEMORY_LIMIT_MIB>
    spike_limit_mib: <SPIKE_LIMIT_MIB>
    check_interval: 5s
  batch:
    timeout: 5s
    send_batch_size: 512
  attributes/timestamp:
    actions:
      - key: "@timestamp"
        action: insert
        from_attribute: time_unix_nano
exporters:
  otlp/jaeger:
    endpoint: <JAEGER_HOST>:4317
    tls:
      insecure: true
  prometheus:
    endpoint: "0.0.0.0:8889"
    namespace: onp
  elasticsearch:
    endpoints:
      - http://<ES_HOST>:<ES_PORT>       
    logs_index: onp-logs-<ENV>
    auth:
      authenticator: basicauth/es
    mapping:
      mode: ecs
  debug:
    verbosity: detailed

extensions:
  health_check:
    endpoint: 0.0.0.0:13133
  basicauth/es:
    client_auth:
      username: \${env:OTEL_ES_USERNAME}
      password: \${env:OTEL_ES_PASSWORD}

service:
  extensions: [health_check, basicauth/es]
  pipelines:
    traces:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [otlp/jaeger, debug]
    metrics:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [prometheus]
    logs:
      receivers: [otlp]
      processors: [memory_limiter, attributes/timestamp, batch]
      exporters: [elasticsearch]
EOF
```

## 7.3 Asignar permisos y verificar la configuración

```bash
sudo chown otel-collector:otel-collector /etc/observabilidad/otel-collector/config.yaml
sudo chmod 640 /etc/observabilidad/otel-collector/config.yaml

# Verificar el archivo
sudo cat /etc/observabilidad/otel-collector/config.yaml
```

# 8. Creación de la unidad systemd
Estos pasos se ejecutan en cada servidor donde se instalará el Collector.

## 8.1 Crear el archivo de servicio

```bash
sudo tee /etc/systemd/system/otel-collector.service > /dev/null <<EOF
[Unit]
Description=OpenTelemetry Collector - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Service]
Type=simple
User=otel-collector
Group=otel-collector
EnvironmentFile=/etc/observabilidad/otel-collector/otel-collector.env
ExecStart=/usr/local/bin/otelcol-contrib --config=/etc/observabilidad/otel-collector/config.yaml
Restart=always
RestartSec=5
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

# 9. Arranque del servicio

Estos pasos se ejecutan en cada servidor donde se instalará el Collector.

## 9.1 Habilitar e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable otel-collector
sudo systemctl start otel-collector
```

## 9.2 Verificar que el servicio está activo
Esperar unos 5 segundos y ejecutar el siguiente comando.

```bash
sudo systemctl status otel-collector
```

Resultado esperado:

```
● otel-collector.service - OpenTelemetry Collector - Stack de Observabilidad ONP
     Loaded: loaded (/etc/systemd/system/otel-collector.service; enabled)
     Active: active (running) since Tue 2026-06-17 10:25:00 PET; 30s ago
```

# 10. Verificación del despliegue

Estos pasos se ejecutan en cada servidor donde se instalará el Collector.

## 10.1 Verificar el health check

```bash
curl http://localhost:13133/
```

Resultado esperado:

```json
{"status":"Server available", ...}
```

## 10.2 Verificar el pipeline de trazas

Enviar una traza de prueba al Collector.
Para QA y PROD cambiar el texto "onp-test-telemetria" por "onp-test-telemetria-1" cuando instalacion que se está realizando es en el servidor 1 y "onp-test-telemetria-2" cuando instalacion que se está realizando es en el servidor 2.

```bash
curl -X POST http://localhost:4328/v1/traces \
  -H 'Content-Type: application/json' \
  -d '{
    "resourceSpans": [{
      "resource": {
        "attributes": [{
          "key": "service.name",
          "value": {"stringValue": "onp-test-telemetria"}
        }]
      },
      "scopeSpans": [{
        "spans": [{
          "traceId": "8b8aa5a2d2c872e8321cf37308d69df5",
          "spanId": "081581bf3cb55c16",
          "name": "prueba-tiempo-real-oti",
          "kind": 1,
          "startTimeUnixNano": "'$(date +%s)'000000000",
          "endTimeUnixNano": "'$(date +%s)'500000000",
          "status": {"code": 1}
        }]
      }]
    }]
  }'  
```

Resultado esperado: respuesta HTTP `200` con body `{}` o {"partialSuccess":{}}.

## 10.3 Verificar que la traza llegó a Jaeger

Acceder a `http://<JAEGER_HOST>:16686`, seleccionar el servicio `onp-test-telemetria-1` o `onp-test-telemetria-2` (según el valor modificado del punto anterior) y buscar trazas.

Resultado esperado: Se debe mostrar la traza de prueba.



## 10.4 Verificar la exposición de métricas

Enviar una métrica de prueba al Collector.
Para QA y PROD cambiar el texto "onp-test-metrica" por "onp-test-metrica-1" cuando instalacion que se está realizando es en el servidor 1 y "onp-test-metrica-2" cuando instalacion que se está realizando es en el servidor 2.

```bash
curl -X POST http://localhost:4328/v1/metrics \
  -H 'Content-Type: application/json' \
  -d '{
    "resourceMetrics": [{
      "resource": {
        "attributes": [{
          "key": "service.name",
          "value": {"stringValue": "onp-test-metrica"}
        }]
      },
      "scopeMetrics": [{
        "metrics": [{
          "name": "demo_metric_requests",
          "sum": {
            "aggregationTemporality": 2,
            "isMonotonic": true,
            "dataPoints": [{
              "asInt": "10",
              "timeUnixNano": "'$(date +%s)'000000000"
            }]
          }
        }]
      }]
    }]
  }'
```

Resultado esperado: ```"partialSuccess":{}}```

Inmediatamente después, ejecutar el siguiente comando.

```bash
curl -s http://localhost:8889/metrics | grep demo
```

Resultado esperado: líneas de métricas en formato Prometheus con el prefijo `onp_`. similar a lo siguiente.

```
# HELP onp_demo_metric_requests_total 
# TYPE onp_demo_metric_requests_total counter
onp_demo_metric_requests_total{job="onp-test-metrica-1"} 10
```

## 10.5 Verificar el funcionamiento del pipeline de logs

Enviar un log de prueba al Collector.
Para QA y PROD cambiar el texto "onp-test-logs" por "onp-test-logs-1" cuando instalacion que se está realizando es en el servidor 1 y "onp-test-logs-2" cuando instalacion que se está realizando es en el servidor 2.

```bash
curl -X POST http://localhost:4328/v1/logs \
  -H 'Content-Type: application/json' \
  -d '{
    "resourceLogs": [{
      "resource": {
        "attributes": [{
          "key": "service.name",
          "value": {"stringValue": "onp-test-logs"}
        }]
      },
      "scopeLogs": [{
        "logRecords": [{
          "timeUnixNano": "'$(date +%s)'000000000",
          "severityText": "INFO",
          "body": {
            "stringValue": "Este es un log de prueba desde OTEL Collector"
          },
          "attributes": [{
            "key": "log.source",
            "value": {"stringValue": "manual-test"}
          }]
        }]
      }]
    }]
  }'
```

Resultado esperado: `{"partialSuccess":{}}`

## 10.6 Verificar que la traza llegó a ElasticSearch

Ejecutar el siguiente comando, reemplazando los valores de `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.
Para **PROD** reemplazar `<ES_HOST>` por la dirección IP de uno de los nodos de Elasticsearch.

Para **QA** y **PROD** cambiar el texto "onp-test-logs" por "onp-test-logs-1" cuando instalacion que se está realizando es en el servidor 1 y "onp-test-logs-2" cuando instalacion que se está realizando es en el servidor 2.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
"http://<ES_HOST>:<ES_PORT>/onp-logs-*/_search?pretty" \
-H "Content-Type: application/json" \
-d '{
  "size": 5,
  "query": {
    "match": {
      "service.name": "onp-test-logs"
    }
  }
}'
```

Resultado esperado: similar al siguiente.
```
{
  "took" : 2,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 1,
      "relation" : "eq"
    },
    "max_score" : 0.8630463,
    "hits" : [
      {
        "_index" : "onp-logs-development-000001",
        "_id" : "Lw3E_54Bi4KJybS6Cy3m",
        "_score" : 0.8630463,
        "_source" : {
          "@timestamp" : "2026-06-25T17:11:22.000000000Z",
          "agent" : {
            "name" : "otlp"
          },
          "log" : {
            "level" : "INFO",
            "source" : "manual-test"
          },
          "message" : "Este es un log de prueba desde OTEL Collector",
          "service" : {
            "name" : "onp-test-logs-1"
          }
        }
      }
    ]
  }
}
```


# 11. Configuración de alta disponibilidad con Keepalived (Solo para QA y PROD)

Esta sección aplica **únicamente a QA y PROD**. En DEV el Collector corre en instancia única sin Keepalived.

## 11.1 Instalar Keepalived en ambos nodos

```bash
sudo dnf install -y keepalived

# Verificar la instalación
keepalived --version
```

## 11.2 Configurar el firewall para VRRP

```bash
sudo firewall-cmd --permanent --add-rich-rule='rule protocol value="vrrp" accept'
sudo firewall-cmd --reload
sudo firewall-cmd --list-rich-rules
```

## 11.3 Configurar Keepalived — Nodo Activo (Servidor 1)

Ejecutar el siguiente comando reemplazando `<NETWORK_INTERFACE>`, `<KEEPALIVED_PASSWORD>` y `<VIP_COLLECTOR>`.

```bash
sudo tee /etc/keepalived/keepalived.conf > /dev/null <<EOF
global_defs {
  router_id otel_collector_ha
  script_user root
  enable_script_security
}

vrrp_script chk_otel_collector {
  script "/usr/bin/curl -sf http://localhost:13133/"
  interval 5
  weight -20
  fall 2
  rise 2
}

vrrp_instance VI_OTEL {
  state MASTER
  interface <NETWORK_INTERFACE>
  virtual_router_id 51
  priority 100
  advert_int 1
  authentication {
    auth_type PASS
    auth_pass <KEEPALIVED_PASSWORD>
  }
  virtual_ipaddress {
    <VIP_COLLECTOR>
  }
  track_script {
    chk_otel_collector
  }
}
EOF
```

## 11.4 Configurar Keepalived — Nodo Pasivo (Servidor 2)
Ejecutar el siguiente comando reemplazando `<NETWORK_INTERFACE>`, `<KEEPALIVED_PASSWORD>` y `<VIP_COLLECTOR>`.

```bash
sudo tee /etc/keepalived/keepalived.conf > /dev/null <<EOF
global_defs {
  router_id otel_collector_ha
  script_user root
  enable_script_security
}

vrrp_script chk_otel_collector {
  script "/usr/bin/curl -sf http://localhost:13133/"
  interval 5
  weight -20
  fall 2
  rise 2
}

vrrp_instance VI_OTEL {
  state BACKUP
  interface <NETWORK_INTERFACE>
  virtual_router_id 51
  priority 90
  advert_int 1
  authentication {
    auth_type PASS
    auth_pass <KEEPALIVED_PASSWORD>
  }
  virtual_ipaddress {
    <VIP_COLLECTOR>
  }
  track_script {
    chk_otel_collector
  }
}
EOF
```

> **NOTA:** La diferencia entre nodo activo y pasivo es `state MASTER` vs `state BACKUP` y `priority 100` vs `priority 90`.
Antes de continuar con los siguientes pasos, asegúrese de haber realizado los pasos de este manual hasta este punto para ambos nodos.

## 11.5 Iniciar Keepalived en ambos nodos

```bash
sudo systemctl enable keepalived
sudo systemctl start keepalived
```

## 11.6 Verificar el estado de Keepalived
Esperar unos 5 segundos y ejecutar los siguientes comandos.

```bash
sudo systemctl status keepalived
sudo journalctl -u keepalived -n 100
```

En el nodo activo:
```
Keepalived_vrrp    VRRP_Instance(VI_OTEL) Entering MASTER STATE
```

En el nodo pasivo:
```
Keepalived_vrrp    VRRP_Instance(VI_OTEL) Entering BACKUP STATE
```

## 11.7 Verificar que la VIP está asignada al nodo activo

```bash
ip addr show <NETWORK_INTERFACE>
```

La VIP debe aparecer en la lista de direcciones del nodo activo.

## 11.8 Verificar el health check a través de la VIP

```bash
curl http://<VIP_COLLECTOR>:13133/
```

## 11.9 Probar el failover

Detener el servidor 1 (nodo activo).

En el nodo pasivo esperar 15 segundos y luego ejecutar el siguiente comando para verificar que tomó la VIP. Reemplazar el valor de `<NETWORK_INTERFACE>`.

```bash
ip addr show <NETWORK_INTERFACE>
```

Ejecute el siguiente comando para verificar el Collector a través de la VIP (ahora desde el pasivo)
```bash
curl http://<VIP_COLLECTOR>:13133/
```

Volver a iniciar el servidor 1 (nodo activo).

En el el servidor 1 (nodo activo) ejecutar el siguente comando hasta que el servicio esté activo (running).

```bash
sudo systemctl status keepalived
```

En el nodo activo esperar 15 segundos y luego ejecutar el siguiente comando para verificar que tomó la VIP. Reemplazar el valor de `<NETWORK_INTERFACE>`.

```bash
ip addr show <NETWORK_INTERFACE>
```

Ejecute el siguiente comando desde ambos nodos para verificar el Collector a través de la VIP.

```bash
curl http://<VIP_COLLECTOR>:13133/
```


# Anexo A — Configuración específica para DEV

| Parámetro | Valor para DEV |
|-----------|----------------|
| Servidor | Servidor 1 (stack completo) |
| HA con Keepalived | No |

## A.1 Valores para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<MEMORY_LIMIT_MIB\> | `200` |
| \<SPIKE_LIMIT_MIB\> | `50` |
| \<JAEGER_HOST\> | `127.0.0.1` o IP del Servidor 1 |
| \<ES_HOST\> | `localhost` o IP del Servidor 1 |
| \<ES_PORT\> | `9200` |
| \<ENV\> | `development` |

## A.2 URL del Collector para los servicios en DEV

```properties
management.otlp.tracing.endpoint=http://<IP_SERVIDOR1>:4328/v1/traces
management.otlp.logging.endpoint=http://<IP_SERVIDOR1>:4328/v1/logs
```

# Anexo B — Configuración específica para QA

| Parámetro | Valor para QA |
|-----------|---------------|
| Servidores | Servidor 1 (activo) y Servidor 2 (pasivo) |
| HA con Keepalived | Sí |

## B.1 Valores para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<MEMORY_LIMIT_MIB\> | `200` |
| \<SPIKE_LIMIT_MIB\> | `50` |
| \<JAEGER_HOST\> | IP del Servidor 1 |
| \<ES_HOST\> | IP del Servidor 2 |
| \<ES_PORT\> | `9200` |
| \<ENV\> | `quality` |

## B.2 URL del Collector para los servicios en QA

```properties
management.otlp.tracing.endpoint=http://<VIP_COLLECTOR>:4328/v1/traces
management.otlp.logging.endpoint=http://<VIP_COLLECTOR>:4328/v1/logs
```

# Anexo C — Configuración específica para PROD

| Parámetro | Valor para PROD |
|-----------|-----------------|
| Servidores | Servidor 1 (activo) y Servidor 2 (pasivo) |
| HA con Keepalived | Sí |

## C.1 Valores para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<MEMORY_LIMIT_MIB\> | `400` |
| \<SPIKE_LIMIT_MIB\> | `100` |
| \<JAEGER_HOST\> | IP del Servidor 5 |
| \<ES_HOST\> | IP del Servidor 3, 4 o 5 |
| \<ES_PORT\> | `9200` |
| \<ENV\> | `production` |

> **NOTA para PROD:** Para mayor resiliencia en el exporter de Elasticsearch, listar los 3 nodos del cluster:
>
> ```yaml
> elasticsearch:
>   endpoints:
>     - http://<IP_ES_NODE1>:9200
>     - http://<IP_ES_NODE2>:9200
>     - http://<IP_ES_NODE3>:9200
> ```

## C.2 URL del Collector para los servicios en PROD

```properties
management.otlp.tracing.endpoint=http://<VIP_COLLECTOR>:4328/v1/traces
management.otlp.logging.endpoint=http://<VIP_COLLECTOR>:4328/v1/logs
```
