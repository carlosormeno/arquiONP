**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Jaeger en Servidor Virtual (RHEL 8)**

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

Este manual describe el proceso completo de instalación de Jaeger en servidores virtuales RHEL 8 on-premise, usando Podman como runtime de contenedores y Quadlet para la integración con systemd.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Jaeger?

Jaeger es una plataforma de trazabilidad distribuida de código abierto. Permite rastrear el flujo de una petición a través de múltiples servicios, identificar cuellos de botella de rendimiento y diagnosticar errores en sistemas distribuidos.

En el stack ONP:

- Los servicios Spring Boot generan trazas con OpenTelemetry y las envían al OTEL Collector.
- El OTEL Collector las reenvía a Jaeger vía OTLP gRPC.
- Jaeger las almacena en Elasticsearch y las expone a través de su UI web.

## 1.3 Rol de Jaeger en el stack ONP

```
Servicios Spring Boot (OpenTelemetry)
        ↓
  OTEL Collector (otlp/jaeger exporter)
        ↓ OTLP gRPC :4317
      Jaeger
        ↓
  Elasticsearch (índices jaeger-onp-*)
        ↓
  Jaeger UI (:16686) → Grafana (datasource Jaeger)
```

## 1.4 Arquitectura por entorno

| Entorno | Servidor | Otros servicios en el mismo servidor |
|---------|----------|--------------------------------------|
| DEV | Servidor 1 | Stack completo |
| QA | Servidor 1 | Collector activo + Grafana + Prometheus |
| PROD | Servidor 5 | ES node 3 |

## 1.5 Jaeger All-in-One

Este manual usa la imagen `jaeger-all-in-one`, que integra todos los componentes de Jaeger en un único proceso:

- **Collector**: recibe trazas del OTEL Collector vía OTLP gRPC (puerto 4317) o Thrift (puerto 14268).
- **Query**: expone la API REST y la UI web (puerto 16686).
- **Ingester**: procesa las trazas y las escribe en el backend de almacenamiento (Elasticsearch).

Esta configuración es adecuada para entornos de baja y media carga. Para volúmenes muy altos en producción, los componentes pueden separarse, pero eso está fuera del alcance de este manual.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios).
- Despliegue de Jaeger con backend Elasticsearch vía Podman + Quadlet.
- Verificación del despliegue y del flujo de trazas.
- Troubleshooting de errores comunes.

Queda fuera del alcance:

- Instalación de Elasticsearch (ver Manual de Instalación Elasticsearch VM).
- Instalación del OTEL Collector (ver Manual de Instalación OTEL Collector VM).
- Despliegue de Jaeger en modo distribuido (componentes separados).

# 2. Prerrequisitos

> **⚠️ ADVERTENCIA:** Elasticsearch debe estar instalado y operativo antes de instalar Jaeger. Jaeger necesita conectarse a Elasticsearch para crear sus índices al arrancar.

## 2.1 Infraestructura requerida

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 1 | 4 | 8 GB | 50 GB |
| PROD | Servidor 5 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos

| Componente | Verificación |
|------------|--------------|
| Elasticsearch | `curl -u elastic:<PASSWORD> http://<ES_HOST>:9200/_cluster/health` → status green/yellow |

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| Podman | 4.4 o superior | `podman --version` |
| curl | Cualquier versión | `curl --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<GITLAB_REGISTRY_URL\> | URL base del GitLab Registry | |
| \<JAEGER_VERSION\> | Versión de Jaeger | 1.58.0 |
| \<ES_HOST\> | IP o hostname del servidor Elasticsearch | |
| \<ES_PORT\> | Puerto de Elasticsearch | 9200 |
| \<ELASTIC_PASSWORD\> | Contraseña del usuario `elastic` de ES | |

## 2.5 Verificaciones previas

### 2.5.1 Verificar Elasticsearch

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Resultado esperado: `{"status":"green",...}` o `{"status":"yellow",...}`. Si el resultado es `red` o no hay respuesta, resolver el problema en ES antes de continuar.

### 2.5.2 Verificar la versión de Podman

```bash
podman --version
```

Resultado esperado: `podman version 4.4.x` o superior.

# 3. Preparación de imágenes

## Nota: ¿Qué camino seguir?

| Escenario | Cuándo usarlo | Ir a |
|-----------|---------------|------|
| A — GitLab Registry | QA y PROD. Sin salida a internet. | Sección 3.2 |
| B — Internet directo | DEV y pruebas. | Sección 3.3 |
| C — Air-gap | Sin internet ni registry. | Anexo D |

## 3.1 Imagen requerida

| Componente | Imagen original | Versión |
|------------|-----------------|---------|
| Jaeger All-in-One | jaegertracing/all-in-one | 1.58.0 |

> **NOTA:** Verificar la versión estable más reciente en https://github.com/jaegertracing/jaeger/releases antes de ejecutar el mirroring.

## 3.2 Escenario A — GitLab Registry (QA y PROD)

```bash
podman login <GITLAB_REGISTRY_URL>

podman pull jaegertracing/all-in-one:<JAEGER_VERSION>

podman tag jaegertracing/all-in-one:<JAEGER_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:<JAEGER_VERSION>

podman push <GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:<JAEGER_VERSION>
```

## 3.3 Escenario B — Internet directo (DEV)

No se requieren pasos de mirroring. Usar directamente:

```
jaegertracing/all-in-one:<JAEGER_VERSION>
```

## 3.4 Escenario C — Air-gap

Ver **Anexo D**.

# 4. Preparación del servidor

## 4.1 Instalar o actualizar Podman

```bash
podman --version
```

Si es inferior a 4.4:

```bash
sudo dnf module reset container-tools -y
sudo dnf module enable container-tools:rhel8 -y
sudo dnf install -y podman
podman --version
```

## 4.2 Crear la estructura de directorios

```bash
sudo mkdir -p /etc/observabilidad/jaeger
```

## 4.3 Configurar el firewall

Jaeger expone los siguientes puertos:

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 4317 | TCP | OTLP gRPC — recibe trazas del OTEL Collector |
| 14268 | TCP | Jaeger Thrift HTTP — receptor alternativo |
| 16686 | TCP | UI web de Jaeger y API REST de consulta |
| 14269 | TCP | Health check y métricas internas de Jaeger |

```bash
sudo firewall-cmd --permanent --add-port=4317/tcp
sudo firewall-cmd --permanent --add-port=14268/tcp
sudo firewall-cmd --permanent --add-port=16686/tcp
sudo firewall-cmd --permanent --add-port=14269/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

# 5. Creación del archivo de credenciales

Jaeger necesita las credenciales de Elasticsearch para almacenar las trazas.

```bash
sudo tee /etc/observabilidad/jaeger/jaeger.env > /dev/null <<EOF
SPAN_STORAGE_TYPE=elasticsearch
ES_SERVER_URLS=http://<ES_HOST>:<ES_PORT>
ES_USERNAME=elastic
ES_PASSWORD=<ELASTIC_PASSWORD>
ES_INDEX_PREFIX=jaeger-onp
EOF
```

```bash
sudo chmod 600 /etc/observabilidad/jaeger/jaeger.env
sudo chown root:root /etc/observabilidad/jaeger/jaeger.env
ls -la /etc/observabilidad/jaeger/jaeger.env
```

Resultado esperado:

```
-rw------- 1 root root ... jaeger.env
```

> **NOTA sobre `ES_INDEX_PREFIX`:** El prefijo `jaeger-onp` hace que Jaeger cree índices con el patrón `jaeger-onp-span-<fecha>` y `jaeger-onp-service-<fecha>`. El index template de Elasticsearch configurado en el Manual de Instalación Elasticsearch VM ya cubre el patrón `jaeger-onp-*` con `best_compression` y retención de 12 meses.

# 6. Despliegue con Quadlet

## 6.1 Crear el archivo Quadlet

```bash
sudo tee /etc/containers/systemd/jaeger.container > /dev/null <<EOF
[Unit]
Description=Jaeger - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<JAEGER_IMAGE>
ContainerName=jaeger
EnvironmentFile=/etc/observabilidad/jaeger/jaeger.env
PublishPort=4317:4317
PublishPort=14268:14268
PublishPort=16686:16686
PublishPort=14269:14269

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

Donde `<JAEGER_IMAGE>` es la imagen según el escenario (ver Anexos A, B y C).

## 6.2 Recargar systemd e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable jaeger
sudo systemctl start jaeger
```

## 6.3 Verificar que el servicio está activo

```bash
sudo systemctl status jaeger
```

Resultado esperado:

```
● jaeger.service - Jaeger - Stack de Observabilidad ONP
     Active: active (running) since ...
```

# 7. Verificación del despliegue

## 7.1 Verificar los logs de Jaeger

```bash
sudo journalctl -u jaeger -n 50
```

Al iniciar correctamente, Jaeger muestra líneas como:

```
{"level":"info","msg":"Starting jaeger-all-in-one","version":{"gitHash":"...","gitVersion":"v1.58.0"}}
{"level":"info","msg":"listening","transport":"grpc","host-port":"0.0.0.0:4317"}
{"level":"info","msg":"listening","transport":"http","host-port":"0.0.0.0:16686"}
{"level":"info","msg":"Health Check state change","status":"ready"}
```

La línea con `"status":"ready"` confirma que Jaeger inició correctamente.

## 7.2 Verificar el health check

```bash
curl http://localhost:14269/
```

Resultado esperado: respuesta HTTP `200`.

## 7.3 Verificar que Jaeger creó sus índices en Elasticsearch

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cat/indices?v | grep jaeger
```

Resultado esperado:

```
health status index                            uuid   pri rep docs.count
green  open   jaeger-onp-service-<fecha>       ...    1   0   0
green  open   jaeger-onp-span-<fecha>          ...    1   0   0
```

> **NOTA:** Los índices aparecen vacíos hasta que el OTEL Collector comience a enviar trazas reales.

## 7.4 Acceder a la UI de Jaeger

Abrir el navegador en:

```
http://<IP_SERVIDOR_JAEGER>:16686
```

La UI debe cargar correctamente. En el selector de servicio ("Service"), seleccionar un servicio y hacer clic en "Find Traces". Inicialmente aparecerá vacío hasta que haya trazas almacenadas.

# 8. Troubleshooting

## 8.1 Jaeger no arranca — error de conexión a Elasticsearch

### Síntoma

```bash
sudo journalctl -u jaeger -n 20
# {"level":"error","msg":"Failed to create primary storage","error":"...connection refused"}
```

### Causa y solución

Elasticsearch no está disponible en la URL configurada. Verificar:

```bash
# Verificar el archivo de credenciales
sudo cat /etc/observabilidad/jaeger/jaeger.env

# Probar conectividad a Elasticsearch
curl -u elastic:<ELASTIC_PASSWORD> http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Si ES está operativo pero el host/puerto en el env file es incorrecto, corregir y reiniciar:

```bash
sudo systemctl restart jaeger
```

## 8.2 La UI de Jaeger no carga

### Síntoma

El navegador no puede conectarse a `http://<IP_SERVIDOR>:16686`.

### Causas y soluciones

- Verificar que el servicio está corriendo: `sudo systemctl status jaeger`
- Verificar que el firewall permite el puerto 16686: `sudo firewall-cmd --list-ports`
- Verificar que el puerto está escuchando: `sudo ss -tlnp | grep 16686`

## 8.3 Las trazas no aparecen en la UI

### Síntoma

La UI de Jaeger carga pero no muestra trazas al buscar.

### Causas y soluciones

- El OTEL Collector no está enviando trazas a Jaeger. Verificar los logs del Collector: `sudo journalctl -u otel-collector -n 30`
- El Collector está enviando al puerto o host incorrecto. Verificar la sección `otlp/jaeger` del `config.yaml` del Collector.

## 8.4 Comandos útiles de diagnóstico rápido

| Diagnóstico | Comando |
|-------------|---------|
| Estado del servicio | `sudo systemctl status jaeger` |
| Logs en tiempo real | `sudo journalctl -u jaeger -f` |
| Health check | `curl http://localhost:14269/` |
| Índices de Jaeger en ES | `curl -u elastic:<PWD> http://<ES_HOST>:9200/_cat/indices?v \| grep jaeger` |
| Reiniciar Jaeger | `sudo systemctl restart jaeger` |

# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<JAEGER_IMAGE\> | `jaegertracing/all-in-one:1.58.0` (Escenario B) |
| \<ES_HOST\> | `localhost` o IP del Servidor 1 |
| \<ES_PORT\> | `9200` |

# Anexo B — Configuración específica para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<JAEGER_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0` |
| \<ES_HOST\> | IP del Servidor 2 (ES corre en Servidor 2) |
| \<ES_PORT\> | `9200` |

# Anexo C — Configuración específica para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<JAEGER_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0` |
| \<ES_HOST\> | IP del Servidor 3 (o cualquier nodo ES del cluster) |
| \<ES_PORT\> | `9200` |

> **NOTA PROD:** Para mayor resiliencia, configurar el ES con todos los nodos del cluster:
>
> ```bash
> ES_SERVER_URLS=http://<IP_ES_NODE1>:9200,http://<IP_ES_NODE2>:9200,http://<IP_ES_NODE3>:9200
> ```

# Anexo D — Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen

```bash
podman pull jaegertracing/all-in-one:<JAEGER_VERSION>
podman save jaegertracing/all-in-one:<JAEGER_VERSION> \
  -o jaeger-all-in-one-<JAEGER_VERSION>.tar
```

## D.2 Transferir e importar

```bash
scp jaeger-all-in-one-<JAEGER_VERSION>.tar <USUARIO>@<IP_SERVIDOR>:/tmp/
sudo podman load -i /tmp/jaeger-all-in-one-<JAEGER_VERSION>.tar
sudo podman images | grep jaeger
```

## D.3 Usar en el archivo Quadlet

```ini
Image=docker.io/jaegertracing/all-in-one:<JAEGER_VERSION>
```
