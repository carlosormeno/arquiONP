**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**OpenTelemetry Collector en Servidor Virtual (RHEL 8)**

Usando Podman + Quadlet + Keepalived sobre Red Hat Enterprise Linux 8

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

Este manual describe el proceso completo de instalación del OpenTelemetry Collector en servidores virtuales RHEL 8 on-premise, usando Podman como runtime de contenedores, Quadlet para la integración con systemd, y Keepalived para alta disponibilidad activo/pasivo en los entornos QA y PROD.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es el OpenTelemetry Collector?

El OpenTelemetry Collector es un componente intermediario que recibe, procesa y exporta telemetría (trazas, métricas y logs). Actúa como hub central del stack de observabilidad: los servicios de backend envían su telemetría al Collector, y este la enruta a los backends de almacenamiento correspondientes.

Ventajas de usar un Collector frente a exportar directamente desde los servicios:

- Los servicios quedan desacoplados de los backends. Si se cambia Jaeger por otro sistema, solo cambia la configuración del Collector, no el código de cada servicio.
- El Collector aplica procesamiento centralizado: agrupamiento por lotes (batching), límites de memoria y enriquecimiento de atributos.
- Un único punto de configuración para controlar el flujo de telemetría de todos los servicios.

## 1.3 Rol del Collector en el stack ONP

```
Servicios Spring Boot (OTLP HTTP :4318)
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
- **metrics**: recibe métricas y las expone en un endpoint Prometheus (puerto 8889) para que Prometheus las recolecte.
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

Cuando el nodo activo falla (proceso caído, servidor reiniciado, red cortada), Keepalived detecta el fallo en segundos y mueve la VIP al nodo pasivo. Los servicios Spring Boot no necesitan cambiar su configuración — siguen apuntando a la misma VIP.

| Entorno | Nodos Collector | HA | Mecanismo |
|---------|-----------------|----|-----------| 
| DEV     | 1 (Servidor 1)  | No | — |
| QA      | 2 (Servidores 1 y 2) | Sí | Keepalived activo/pasivo |
| PROD    | 2 (Servidores 1 y 2) | Sí | Keepalived activo/pasivo |

## 1.5 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios).
- Instalación y configuración de Podman y Quadlet en RHEL 8.
- Configuración del pipeline del Collector.
- Despliegue del Collector como servicio systemd vía Quadlet.
- Instalación y configuración de Keepalived para HA activo/pasivo (QA y PROD).
- Verificación del despliegue y del flujo de datos.
- Troubleshooting de errores comunes.

Queda fuera del alcance:

- Instalación de Jaeger (ver Manual de Instalación Jaeger VM).
- Instalación de Prometheus (ver Manual de Instalación Prometheus VM).
- Instalación de Elasticsearch (ver Manual de Instalación Elasticsearch VM).

# 2. Prerrequisitos

> **⚠️ ADVERTENCIA:** Verificar TODOS los prerrequisitos antes de comenzar. Elasticsearch y Jaeger deben estar instalados y operativos antes de instalar el Collector. El Collector intentará conectarse a ellos al iniciar.

## 2.1 Infraestructura requerida por entorno

| Entorno | Servidor(es) | Rol en el servidor |
|---------|-------------|-------------------|
| DEV | Servidor 1 | Stack completo (Collector + Jaeger + Grafana + Prometheus) |
| QA | Servidor 1 | Collector activo + Keepalived + Jaeger + Grafana + Prometheus |
| QA | Servidor 2 | Collector standby + Keepalived + ES + Kibana |
| PROD | Servidor 1 | Collector activo + Keepalived + Prometheus + Grafana |
| PROD | Servidor 2 | Collector standby + Keepalived |

## 2.2 Componentes que deben estar operativos antes de instalar el Collector

| Componente | Verificación |
|------------|--------------|
| Elasticsearch | `curl -u elastic:<PASSWORD> http://<ES_HOST>:9200/_cluster/health` → status green/yellow |
| Jaeger | `curl http://<JAEGER_HOST>:14269/` → respuesta 200 |

> **NOTA:** Si Elasticsearch o Jaeger aún no están disponibles, el Collector puede instalarse igualmente — reportará errores de conexión en los logs pero seguirá funcionando para los backends que sí estén activos. Sin embargo, se recomienda tenerlos operativos para una verificación completa.

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| Podman | 4.4 o superior | `podman --version` |
| Keepalived | 2.x (solo QA y PROD) | `keepalived --version` |
| curl | Cualquier versión | `curl --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<GITLAB_REGISTRY_URL\> | URL base del GitLab Registry | |
| \<COLLECTOR_VERSION\> | Versión del Collector contrib | 0.120.0 |
| \<ES_HOST\> | IP o hostname del servidor Elasticsearch | |
| \<ES_PORT\> | Puerto de Elasticsearch | 9200 |
| \<OTEL_ES_USERNAME\> | Usuario de ES exclusivo para el Collector | |
| \<OTEL_ES_PASSWORD\> | Contraseña del usuario de ES | |
| \<JAEGER_HOST\> | IP o hostname del servidor Jaeger | |
| \<VIP_COLLECTOR\> | IP virtual de Keepalived (solo QA y PROD) | |
| \<IP_COLLECTOR_NODE1\> | IP real del nodo activo (solo QA y PROD) | |
| \<IP_COLLECTOR_NODE2\> | IP real del nodo pasivo (solo QA y PROD) | |
| \<NETWORK_INTERFACE\> | Interfaz de red del servidor (ej. eth0, ens3) | |

### Cómo identificar la interfaz de red

```bash
ip link show
```

La interfaz de red es la que tiene la IP del servidor. Generalmente aparece como `eth0`, `ens3`, `ens192` u otro nombre. Usar ese nombre en el parámetro `<NETWORK_INTERFACE>`.

## 2.5 Verificaciones previas

### 2.5.1 Verificar Elasticsearch

```bash
curl -u <OTEL_ES_USERNAME>:<OTEL_ES_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Resultado esperado: `{"status":"green",...}` o `{"status":"yellow",...}`.

### 2.5.2 Verificar Jaeger

```bash
curl http://<JAEGER_HOST>:14269/
```

Resultado esperado: respuesta HTTP 200 (aunque el body esté vacío).

### 2.5.3 Verificar la versión de Podman

```bash
podman --version
```

Resultado esperado: `podman version 4.4.x` o superior. Si es inferior, ver Sección 4.1.

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
| OTel Collector Contrib | otel/opentelemetry-collector-contrib | 0.120.0 |

> **NOTA:** Verificar la versión estable más reciente en https://github.com/open-telemetry/opentelemetry-collector-releases/releases antes de ejecutar el mirroring. Usar siempre una versión fija, nunca `latest`.

## 3.2 Escenario A — GitLab Registry (QA y PROD)

```bash
podman login <GITLAB_REGISTRY_URL>

podman pull otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION>

podman tag otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:<COLLECTOR_VERSION>

podman push <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:<COLLECTOR_VERSION>
```

## 3.3 Escenario B — Internet directo (DEV)

No se requieren pasos de mirroring. Pasar directamente a la Sección 4. Usar la imagen pública directamente:

```
otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION>
```

## 3.4 Escenario C — Air-gap

Ver **Anexo D** para el procedimiento completo.

# 4. Preparación del servidor

Estos pasos se ejecutan en **cada servidor** donde se instalará el Collector.

## 4.1 Instalar o actualizar Podman a versión 4.4+

```bash
podman --version
```

Si la versión es inferior a 4.4 o no está instalado:

```bash
sudo dnf module reset container-tools -y
sudo dnf module enable container-tools:rhel8 -y
sudo dnf install -y podman
podman --version
```

## 4.2 Crear la estructura de directorios

```bash
# Directorio de configuración del Collector
sudo mkdir -p /etc/observabilidad/otel-collector
```

## 4.3 Configurar el firewall

El Collector usa los siguientes puertos:

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 4317 | TCP | OTLP gRPC — recibe telemetría de los servicios |
| 4318 | TCP | OTLP HTTP — recibe telemetría de los servicios |
| 8889 | TCP | Métricas Prometheus — expone métricas para que Prometheus las recolecte |
| 13133 | TCP | Health check interno del Collector |

```bash
sudo firewall-cmd --permanent --add-port=4317/tcp
sudo firewall-cmd --permanent --add-port=4318/tcp
sudo firewall-cmd --permanent --add-port=8889/tcp
sudo firewall-cmd --permanent --add-port=13133/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

Resultado esperado:

```
4317/tcp 4318/tcp 8889/tcp 13133/tcp
```

# 5. Creación del archivo de credenciales

El Collector necesita las credenciales del usuario de Elasticsearch para exportar logs. Estas credenciales se almacenan en un archivo de variables de entorno protegido.

> **🔴 IMPORTANTE:** Este archivo contiene credenciales en texto plano. Nunca subirlo a un repositorio de código.

## 5.1 Crear el archivo de credenciales

```bash
sudo tee /etc/observabilidad/otel-collector/otel-collector.env > /dev/null <<EOF
OTEL_ES_USERNAME=<OTEL_ES_USERNAME>
OTEL_ES_PASSWORD=<OTEL_ES_PASSWORD>
EOF
```

## 5.2 Asegurar los permisos

```bash
sudo chmod 600 /etc/observabilidad/otel-collector/otel-collector.env
sudo chown root:root /etc/observabilidad/otel-collector/otel-collector.env

ls -la /etc/observabilidad/otel-collector/otel-collector.env
```

Resultado esperado:

```
-rw------- 1 root root 65 may 20 10:20 /etc/observabilidad/otel-collector/otel-collector.env
```

# 6. Configuración del pipeline (config.yaml)

El pipeline del Collector se define en un archivo `config.yaml`. Esta configuración define qué señales recibe el Collector, cómo las procesa y a dónde las envía.

## 6.1 Estructura del pipeline

| Señal | Receiver | Processors | Exporter |
|-------|----------|------------|----------|
| traces | otlp | memory_limiter, batch | otlp/jaeger |
| metrics | otlp | memory_limiter, batch | prometheus |
| logs | otlp | memory_limiter, batch | elasticsearch |

## 6.2 Crear el archivo config.yaml

```bash
sudo tee /etc/observabilidad/otel-collector/config.yaml > /dev/null <<EOF
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  memory_limiter:
    limit_mib: <MEMORY_LIMIT_MIB>
    spike_limit_mib: <SPIKE_LIMIT_MIB>
    check_interval: 5s
  batch:
    timeout: 5s
    send_batch_size: 512

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
      exporters: [otlp/jaeger]
    metrics:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [prometheus]
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [elasticsearch]
EOF
```

> **NOTA:** Los valores `${env:OTEL_ES_USERNAME}` y `${env:OTEL_ES_PASSWORD}` **no son placeholders a reemplazar**. Es la sintaxis del OTEL Collector para leer variables de entorno en tiempo de ejecución. El Collector leerá automáticamente esos valores desde el archivo `otel-collector.env` creado en la Sección 5.1.

> **NOTA sobre `<ENV>`:** Reemplazar con el nombre del entorno según la siguiente tabla:

| Entorno | Valor de \<ENV\> |
|---------|-----------------|
| DEV | development |
| QA | quality |
| PROD | production |

## 6.3 Verificar el archivo de configuración

```bash
cat /etc/observabilidad/otel-collector/config.yaml
```

Confirmar que todos los placeholders fueron reemplazados con los valores reales del entorno.

# 7. Despliegue con Quadlet

## 7.1 Crear el archivo Quadlet

```bash
sudo tee /etc/containers/systemd/otel-collector.container > /dev/null <<EOF
[Unit]
Description=OpenTelemetry Collector - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<COLLECTOR_IMAGE>
ContainerName=otel-collector
EnvironmentFile=/etc/observabilidad/otel-collector/otel-collector.env
Volume=/etc/observabilidad/otel-collector/config.yaml:/etc/otel/config.yaml:ro,Z
Exec=--config=/etc/otel/config.yaml
PublishPort=4317:4317
PublishPort=4318:4318
PublishPort=8889:8889
PublishPort=13133:13133

[Service]
Restart=always
RestartSec=5
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

Donde `<COLLECTOR_IMAGE>` es la imagen según el escenario (ver Anexos A, B y C).

## 7.2 Recargar systemd e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable otel-collector
sudo systemctl start otel-collector
```

## 7.3 Verificar que el servicio está activo

```bash
sudo systemctl status otel-collector
```

Resultado esperado:

```
● otel-collector.service - OpenTelemetry Collector - Stack de Observabilidad ONP
     Loaded: loaded (/etc/containers/systemd/otel-collector.container; generated)
     Active: active (running) since Tue 2026-05-20 10:25:00 PET; 30s ago
   Main PID: 23456 (conmon)
```

# 8. Verificación del despliegue

## 8.1 Verificar los logs del Collector

```bash
sudo journalctl -u otel-collector -n 50
```

Al iniciar correctamente, el Collector muestra las siguientes líneas clave:

```
info    extensions/extensions.go:61     Extension started.    {"otelcol.component.id": "basicauth/es"}
info    extensions/extensions.go:61     Extension started.    {"otelcol.component.id": "health_check"}
info    otlpreceiver    Starting GRPC server    {"endpoint": "0.0.0.0:4317"}
info    otlpreceiver    Starting HTTP server    {"endpoint": "0.0.0.0:4318"}
info    healthcheck/handler.go    Health Check state change    {"status": "ready"}
info    service    Everything is ready. Begin running and processing data.
```

La línea **"Everything is ready. Begin running and processing data."** confirma que el Collector inició correctamente.

Para seguir los logs en tiempo real:

```bash
sudo journalctl -u otel-collector -f
```

## 8.2 Verificar el health check

```bash
curl http://localhost:13133/
```

Resultado esperado:

```json
{"status":"Server available","upSince":"2026-05-20T10:25:00Z","uptime":"30s"}
```

## 8.3 Enviar una traza de prueba

```bash
curl -X POST http://localhost:4318/v1/traces \
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
          "traceId": "5b8aa5a2d2c872e8321cf37308d69df2",
          "spanId": "051581bf3cb55c13",
          "name": "prueba-otel-collector",
          "kind": 1,
          "startTimeUnixNano": "1700000000000000000",
          "endTimeUnixNano":   "1700000001000000000",
          "status": {"code": 1}
        }]
      }]
    }]
  }'
```

Resultado esperado: respuesta HTTP `200` con body `{}`.

## 8.4 Verificar que la traza llegó a Jaeger

Acceder a la UI de Jaeger en `http://<JAEGER_HOST>:16686`, seleccionar el servicio `onp-test-telemetria` y hacer clic en **Find Traces**. Debe aparecer la traza `prueba-otel-collector`.

## 8.5 Verificar que las métricas están disponibles

```bash
curl http://localhost:8889/metrics | head -20
```

Resultado esperado: líneas de métricas en formato Prometheus (texto plano con el prefijo `onp_`).

## 8.6 Verificar que los logs llegan a Elasticsearch

```bash
curl -u <OTEL_ES_USERNAME>:<OTEL_ES_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cat/indices?v | grep onp-logs
```

> **NOTA:** El índice `onp-logs-<ENV>` (o el alias) solo aparece cuando el Collector ha exportado al menos un log. Si el índice no aparece, enviar primero una petición a un servicio Spring Boot instrumentado para generar logs reales.

# 9. Configuración de alta disponibilidad con Keepalived

Esta sección aplica **únicamente a QA y PROD**. En DEV el Collector corre en instancia única sin Keepalived.

Keepalived es un software de balanceo de carga y alta disponibilidad para Linux. En este caso se usa para gestionar una **IP virtual (VIP)**: una dirección IP que flota entre dos servidores. Cuando el servidor activo falla, Keepalived mueve la VIP al servidor pasivo automáticamente en segundos.

Los servicios Spring Boot configuran el endpoint del Collector apuntando a la VIP. No necesitan cambiar su configuración cuando ocurre un failover.

## 9.1 Instalar Keepalived en ambos nodos

Ejecutar en **ambos servidores** del par activo/pasivo:

```bash
sudo dnf install -y keepalived

# Verificar la instalación
keepalived --version
```

Resultado esperado: `Keepalived v2.x.x ...`

## 9.2 Configurar el firewall para VRRP

Keepalived usa el protocolo VRRP (Virtual Router Redundancy Protocol) para comunicarse entre los dos nodos. VRRP es un protocolo IP independiente (no TCP ni UDP).

```bash
# Permitir el protocolo VRRP entre los dos nodos del par
sudo firewall-cmd --permanent --add-rich-rule='rule protocol value="vrrp" accept'
sudo firewall-cmd --reload

# Verificar
sudo firewall-cmd --list-rich-rules
```

Resultado esperado:

```
rule protocol value="vrrp" accept
```

## 9.3 Configurar Keepalived — Nodo Activo

Ejecutar en el **servidor designado como activo** (Servidor 1 en QA/PROD):

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

Donde:
- `<NETWORK_INTERFACE>` es la interfaz de red del servidor (ej. `eth0`, `ens192`). Ver Sección 2.4.
- `<KEEPALIVED_PASSWORD>` es una contraseña compartida entre los dos nodos (8 caracteres máximo). Definir una contraseña propia.
- `<VIP_COLLECTOR>` es la IP virtual que usarán los servicios Spring Boot como endpoint del Collector.

## 9.4 Configurar Keepalived — Nodo Pasivo

Ejecutar en el **servidor designado como pasivo** (Servidor 2 en QA/PROD):

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

> **NOTA:** La única diferencia entre el nodo activo y el pasivo es `state MASTER` vs `state BACKUP` y `priority 100` vs `priority 90`. El nodo con mayor prioridad gana la VIP al arrancar.

> **NOTA sobre `virtual_router_id`:** El valor `51` debe ser el mismo en ambos nodos y único en el segmento de red. Si hay otros servicios con Keepalived en la misma red, asegurarse de que no usen el mismo `virtual_router_id`.

## 9.5 Cómo funciona el health check

El script `chk_otel_collector` verifica que el endpoint de health check del Collector responde correctamente (`curl -sf http://localhost:13133/`). Si el Collector falla (proceso caído, error interno), el script falla y Keepalived reduce la prioridad del nodo activo en 20 puntos (de 100 a 80). El nodo pasivo tiene prioridad 90, por lo que toma control de la VIP automáticamente.

## 9.6 Iniciar Keepalived en ambos nodos

Ejecutar en **ambos servidores**:

```bash
sudo systemctl enable keepalived
sudo systemctl start keepalived
```

## 9.7 Verificar que Keepalived está activo

```bash
sudo systemctl status keepalived
```

Resultado esperado:

```
● keepalived.service - LVS and VRRP High Availability Monitor
     Active: active (running)
```

Verificar los logs de Keepalived para confirmar el estado del nodo:

```bash
sudo journalctl -u keepalived -n 30
```

En el nodo activo, los logs deben mostrar:

```
Keepalived_vrrp    VRRP_Instance(VI_OTEL) Transition to MASTER STATE
Keepalived_vrrp    VRRP_Instance(VI_OTEL) Entering MASTER STATE
```

En el nodo pasivo:

```
Keepalived_vrrp    VRRP_Instance(VI_OTEL) Entering BACKUP STATE
```

## 9.8 Verificar que la VIP está asignada al nodo activo

Ejecutar en el **nodo activo**:

```bash
ip addr show <NETWORK_INTERFACE>
```

La VIP debe aparecer en la lista de direcciones de la interfaz:

```
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP>
    inet <IP_REAL_NODO_ACTIVO>/24 brd ... scope global eth0
    inet <VIP_COLLECTOR>/32 scope global secondary eth0
```

En el nodo pasivo, la VIP **no debe aparecer**.

## 9.9 Verificar el health check del Collector a través de la VIP

Desde cualquier servidor de la red interna:

```bash
curl http://<VIP_COLLECTOR>:13133/
```

Resultado esperado:

```json
{"status":"Server available","upSince":"...","uptime":"..."}
```

## 9.10 Probar el failover

> **⚠️ ADVERTENCIA:** Este paso interrumpe el Collector activo temporalmente. Realizarlo en un momento de bajo tráfico en QA. En PROD, planificar una ventana de mantenimiento.

Simular un fallo del Collector en el nodo activo:

```bash
# En el nodo activo: detener el Collector
sudo systemctl stop otel-collector
```

Verificar en el nodo pasivo que tomó el control de la VIP (puede tardar 10-15 segundos):

```bash
# En el nodo pasivo
ip addr show <NETWORK_INTERFACE>
# La VIP debe aparecer ahora en este nodo
```

Verificar que el Collector responde a través de la VIP (ahora desde el nodo pasivo):

```bash
curl http://<VIP_COLLECTOR>:13133/
```

Debe responder correctamente desde el nodo pasivo.

Restaurar el servicio en el nodo activo original:

```bash
# En el nodo activo: reiniciar el Collector
sudo systemctl start otel-collector
```

> **NOTA:** Al restaurar el nodo activo, la VIP **vuelve automáticamente** al nodo con mayor prioridad (el activo original, priority 100). Este comportamiento se llama "preemption" y está habilitado por defecto en Keepalived.

# 10. Troubleshooting

## 10.1 El servicio no arranca — imagen no encontrada

### Síntoma

```bash
sudo journalctl -u otel-collector -n 20
# Error: initializing source docker://...: ... manifest unknown
```

### Causa y solución

La imagen no fue descargada o publicada correctamente en el registry. Verificar que el proceso de mirroring de la Sección 3 se completó:

```bash
# Verificar que la imagen existe localmente
sudo podman images | grep otel-collector

# Si no existe, repetir el proceso de pull del Escenario correspondiente
```

## 10.2 El Collector arranca pero no envía trazas a Jaeger

### Síntoma

En los logs del Collector aparece:

```
error exporting items, will retry: rpc error: code = Unavailable desc = connection refused
```

### Causas y soluciones

- **Jaeger no está corriendo**: verificar con `sudo systemctl status jaeger` en el servidor de Jaeger.
- **El hostname o puerto de Jaeger es incorrecto**: verificar el valor de `endpoint` en el exporter `otlp/jaeger` del `config.yaml`:

  ```bash
  grep -A2 "otlp/jaeger" /etc/observabilidad/otel-collector/config.yaml
  ```

- **Firewall en el servidor de Jaeger**: verificar que el puerto 4317 está abierto en el servidor de Jaeger:

  ```bash
  curl -v telnet://<JAEGER_HOST>:4317
  ```

## 10.3 El Collector no exporta logs a Elasticsearch

### Síntoma

```
error exporting items: failed to push data: 401 Unauthorized
```

### Causa y solución

Las credenciales de Elasticsearch son incorrectas. Verificar el contenido del archivo env:

```bash
sudo cat /etc/observabilidad/otel-collector/otel-collector.env
```

Comparar con las credenciales del usuario en Elasticsearch. Si son correctas, verificar que el usuario tiene permisos de escritura sobre el índice `onp-logs-*`.

## 10.4 La VIP de Keepalived no flota correctamente

### Síntoma

Al detener el Collector en el nodo activo, la VIP no aparece en el nodo pasivo.

### Causas y soluciones

- **El protocolo VRRP está bloqueado en el firewall**: verificar en ambos nodos:

  ```bash
  sudo firewall-cmd --list-rich-rules
  # Debe aparecer: rule protocol value="vrrp" accept
  ```

- **El `virtual_router_id` no coincide entre los dos nodos**: verificar que ambos archivos `keepalived.conf` tienen el mismo valor.

- **La contraseña de autenticación no coincide**: verificar que `auth_pass` es idéntica en ambos nodos (exactamente los mismos caracteres, incluyendo mayúsculas/minúsculas).

- **El script de health check falla aunque el Collector esté corriendo**: verificar que el puerto 13133 responde localmente:

  ```bash
  curl -sf http://localhost:13133/
  # Debe devolver JSON con status available
  ```

## 10.5 Comandos útiles de diagnóstico rápido

| Diagnóstico | Comando |
|-------------|---------|
| Estado del Collector | `sudo systemctl status otel-collector` |
| Logs en tiempo real | `sudo journalctl -u otel-collector -f` |
| Estado de Keepalived | `sudo systemctl status keepalived` |
| Logs de Keepalived | `sudo journalctl -u keepalived -n 30` |
| Ver si la VIP está asignada | `ip addr show <INTERFACE> \| grep <VIP>` |
| Health check del Collector | `curl http://localhost:13133/` |
| Health check a través de la VIP | `curl http://<VIP_COLLECTOR>:13133/` |
| Ver config activa del Collector | `cat /etc/observabilidad/otel-collector/config.yaml` |
| Reiniciar el Collector | `sudo systemctl restart otel-collector` |
| Reiniciar Keepalived | `sudo systemctl restart keepalived` |

# Anexo A — Configuración específica para DEV

| Parámetro | Valor para DEV |
|-----------|----------------|
| Servidor | Servidor 1 (stack completo) |
| HA con Keepalived | No |
| Instancias del Collector | 1 |

## A.1 Valores para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<COLLECTOR_IMAGE\> | `otel/opentelemetry-collector-contrib:0.120.0` (Escenario B) |
| \<MEMORY_LIMIT_MIB\> | `200` |
| \<SPIKE_LIMIT_MIB\> | `50` |
| \<JAEGER_HOST\> | `localhost` o IP del Servidor 1 |
| \<ES_HOST\> | `localhost` o IP del Servidor 1 |
| \<ES_PORT\> | `9200` |
| \<ENV\> | `development` |

## A.2 URL del Collector para los servicios en DEV

Los servicios Spring Boot en DEV deben configurar en `application-dev.properties`:

```properties
management.otlp.tracing.endpoint=http://<IP_SERVIDOR1>:4318/v1/traces
management.otlp.logging.endpoint=http://<IP_SERVIDOR1>:4318/v1/logs
```

# Anexo B — Configuración específica para QA

| Parámetro | Valor para QA |
|-----------|---------------|
| Servidores | Servidor 1 (activo) y Servidor 2 (pasivo) |
| HA con Keepalived | Sí — activo/pasivo |
| VIP del Collector | \<VIP_COLLECTOR\> |

## B.1 Valores para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<COLLECTOR_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:0.120.0` |
| \<MEMORY_LIMIT_MIB\> | `200` |
| \<SPIKE_LIMIT_MIB\> | `50` |
| \<JAEGER_HOST\> | IP del Servidor 1 (Jaeger corre en el mismo servidor que el Collector activo) |
| \<ES_HOST\> | IP del Servidor 2 (ES corre en el Servidor 2) |
| \<ES_PORT\> | `9200` |
| \<ENV\> | `quality` |

## B.2 Orden de instalación en QA

1. Instalar el Collector en el Servidor 1 (Secciones 4 a 7).
2. Instalar el Collector en el Servidor 2 (Secciones 4 a 7) — mismo `config.yaml`.
3. Instalar y configurar Keepalived en ambos servidores (Sección 9).
4. Verificar el failover (Sección 9.10).

## B.3 URL del Collector para los servicios en QA

```properties
management.otlp.tracing.endpoint=http://<VIP_COLLECTOR>:4318/v1/traces
management.otlp.logging.endpoint=http://<VIP_COLLECTOR>:4318/v1/logs
```

# Anexo C — Configuración específica para PROD

| Parámetro | Valor para PROD |
|-----------|-----------------|
| Servidores | Servidor 1 (activo) y Servidor 2 (pasivo) |
| HA con Keepalived | Sí — activo/pasivo |
| VIP del Collector | \<VIP_COLLECTOR\> |

## C.1 Valores para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<COLLECTOR_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:0.120.0` |
| \<MEMORY_LIMIT_MIB\> | `400` |
| \<SPIKE_LIMIT_MIB\> | `100` |
| \<JAEGER_HOST\> | IP del Servidor 5 (Jaeger corre en Servidor 5) |
| \<ES_HOST\> | IP del Servidor 3, 4 o 5 (cualquier nodo ES del cluster) |
| \<ES_PORT\> | `9200` |
| \<ENV\> | `production` |

> **NOTA para PROD:** En PROD el Collector puede apuntar a cualquiera de los 3 nodos ES en `<ES_HOST>`. Elasticsearch distribuye las escrituras internamente. Para mayor resiliencia, se puede listar los 3 nodos en el campo `endpoints` de la sección `elasticsearch` del `config.yaml`:
>
> ```yaml
> elasticsearch:
>   endpoints:
>     - http://<IP_ES_NODE1>:9200
>     - http://<IP_ES_NODE2>:9200
>     - http://<IP_ES_NODE3>:9200
> ```

## C.2 Orden de instalación en PROD

1. Instalar el Collector en el Servidor 1 (Secciones 4 a 7).
2. Instalar el Collector en el Servidor 2 (Secciones 4 a 7) — mismo `config.yaml`.
3. Instalar y configurar Keepalived en ambos servidores (Sección 9).
4. Verificar el failover (Sección 9.10).

## C.3 URL del Collector para los servicios en PROD

```properties
management.otlp.tracing.endpoint=http://<VIP_COLLECTOR>:4318/v1/traces
management.otlp.logging.endpoint=http://<VIP_COLLECTOR>:4318/v1/logs
```

# Anexo D — Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen en una máquina con internet

```bash
podman pull otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION>
podman save otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION> \
  -o otel-collector-contrib-<COLLECTOR_VERSION>.tar
```

## D.2 Transferir el archivo al servidor destino

```bash
scp otel-collector-contrib-<COLLECTOR_VERSION>.tar <USUARIO>@<IP_SERVIDOR>:/tmp/
```

## D.3 Importar la imagen en el servidor destino

```bash
sudo podman load -i /tmp/otel-collector-contrib-<COLLECTOR_VERSION>.tar

# Verificar que la imagen quedó disponible
sudo podman images | grep otel-collector-contrib
```

## D.4 Usar la imagen importada en el archivo Quadlet

```ini
Image=docker.io/otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION>
```
