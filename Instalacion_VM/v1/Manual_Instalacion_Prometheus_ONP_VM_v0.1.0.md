**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Prometheus en Servidor Virtual (RHEL 8)**

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

Este manual describe el proceso completo de instalación de Prometheus en servidores virtuales RHEL 8 on-premise, usando Podman como runtime de contenedores y Quadlet para la integración con systemd.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.

## 1.2 ¿Qué es Prometheus?

Prometheus es un sistema de monitoreo y alerta de código abierto. Recolecta métricas de los servicios a través de un modelo de **pull**: periódicamente consulta (scrape) los endpoints de métricas de los componentes del stack y almacena las series temporales en su base de datos local.

En el stack ONP, Prometheus recolecta métricas de:

- **OTEL Collector**: métricas del Collector (trazas recibidas, exportadas, errores, latencia de procesamiento) disponibles en el puerto 8889.
- **Servicios Spring Boot** (si se configura el scrape de Actuator): métricas de JVM, HTTP y negocio.

Estas métricas son consultadas por Grafana para construir dashboards de observabilidad.

## 1.3 Rol de Prometheus en el stack ONP

```
OTEL Collector (:8889/metrics)  →  Prometheus  →  Grafana
Servicios Spring Boot (:8080/actuator/prometheus)  ↗
```

## 1.4 Arquitectura por entorno

| Entorno | Servidor | Otros servicios en el mismo servidor |
|---------|----------|--------------------------------------|
| DEV | Servidor 1 | Stack completo |
| QA | Servidor 1 | Collector activo + Jaeger + Grafana |
| PROD | Servidor 1 | Collector activo + Grafana |

## 1.5 Persistencia de datos

Prometheus almacena sus series temporales en un directorio de datos local (`/prometheus`). Este directorio se monta en `/data/prometheus` del servidor para que los datos persistan entre reinicios del contenedor.

Por defecto, Prometheus retiene datos durante **15 días**. Este valor se puede ajustar con el flag `--storage.tsdb.retention.time` sin necesidad de redeploy (solo reiniciar el servicio).

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios, permisos).
- Configuración del archivo `prometheus.yml` con los targets de scrape.
- Despliegue de Prometheus vía Podman + Quadlet.
- Verificación del despliegue y de los targets de scrape.
- Troubleshooting de errores comunes.

Queda fuera del alcance:

- Instalación de Grafana (ver Manual de Instalación Grafana VM).
- Configuración de alertas con Alertmanager.
- Federación de instancias Prometheus.

# 2. Prerrequisitos

## 2.1 Infraestructura requerida

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 1 | 4 | 8 GB | 50 GB |
| PROD | Servidor 1 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos

Prometheus puede instalarse antes que el OTEL Collector. Si un target de scrape no está disponible al arrancar, Prometheus lo marca como `DOWN` en su UI y reintenta periódicamente. Cuando el target se levante, el scrape comenzará automáticamente.

| Componente | Verificación |
|------------|--------------|
| OTEL Collector | `curl http://<COLLECTOR_HOST>:8889/metrics` → líneas de métricas en formato texto |

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
| \<PROMETHEUS_VERSION\> | Versión de Prometheus | 2.55.0 |
| \<COLLECTOR_HOST\> | IP o hostname del servidor del OTEL Collector (o VIP en QA/PROD) | |
| \<SERVICES_HOSTS\> | IPs o hostnames de los servicios Spring Boot a monitorear | |

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
| Prometheus | prom/prometheus | 2.55.0 |

## 3.2 Escenario A — GitLab Registry (QA y PROD)

```bash
podman login <GITLAB_REGISTRY_URL>

podman pull prom/prometheus:<PROMETHEUS_VERSION>

podman tag prom/prometheus:<PROMETHEUS_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/prometheus:<PROMETHEUS_VERSION>

podman push <GITLAB_REGISTRY_URL>/observabilidad/prometheus:<PROMETHEUS_VERSION>
```

## 3.3 Escenario B — Internet directo (DEV)

No se requieren pasos de mirroring. Usar directamente:

```
prom/prometheus:<PROMETHEUS_VERSION>
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

Prometheus almacena sus series temporales en el directorio `/prometheus` dentro del contenedor. Este directorio se monta en `/data/prometheus` del servidor.

```bash
# Directorio de datos persistentes de Prometheus
sudo mkdir -p /data/prometheus

# Directorio de configuración
sudo mkdir -p /etc/observabilidad/prometheus

# Prometheus corre internamente como UID 65534 (usuario nobody en la imagen oficial)
sudo chown -R 65534:65534 /data/prometheus

# Verificar los permisos
ls -la /data/ | grep prometheus
```

Resultado esperado:

```
drwxr-xr-x 2 65534 65534 4096 may 20 10:45 prometheus
```

## 4.3 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 9090 | TCP | UI web de Prometheus y API REST |

```bash
sudo firewall-cmd --permanent --add-port=9090/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

# 5. Configuración de Prometheus

## 5.1 Crear el archivo prometheus.yml

El archivo `prometheus.yml` define los **jobs de scrape**: quién scrapeará Prometheus, con qué frecuencia y a qué endpoints.

```bash
sudo tee /etc/observabilidad/prometheus/prometheus.yml > /dev/null <<EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    environment: '<ENV>'

scrape_configs:

  # Prometheus se monitorea a sí mismo
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # Métricas del OTEL Collector
  - job_name: 'otel-collector'
    static_configs:
      - targets: ['<COLLECTOR_HOST>:8889']
    metrics_path: '/metrics'
EOF
```

Donde:
- `<ENV>` es el nombre del entorno: `development`, `quality` o `production`. Este label se añade a todas las métricas recolectadas por esta instancia de Prometheus.
- `<COLLECTOR_HOST>` es la IP del OTEL Collector (o la VIP de Keepalived en QA y PROD).

> **NOTA:** Para agregar scrape de servicios Spring Boot, añadir un job adicional al archivo. Ver Sección 5.2.

## 5.2 Agregar scrape de servicios Spring Boot (opcional)

Si los servicios Spring Boot tienen `spring-boot-starter-actuator` con el endpoint Prometheus habilitado, añadir el siguiente bloque al archivo `prometheus.yml`:

```yaml
  # Métricas de servicios Spring Boot
  - job_name: 'spring-boot-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - '<IP_SERVICIO_1>:8080'
          - '<IP_SERVICIO_2>:8080'
```

> **NOTA:** Para agregar o quitar targets de Spring Boot, editar el archivo `prometheus.yml` y reiniciar el servicio (`sudo systemctl restart prometheus`). El reinicio de Prometheus es rápido (menos de 5 segundos) y no pierde datos históricos.

## 5.3 Verificar la sintaxis del archivo de configuración

Antes de desplegar, verificar que el archivo YAML está bien formado:

```bash
# Verificar la estructura del archivo
cat /etc/observabilidad/prometheus/prometheus.yml
```

Confirmar que no hay errores de indentación ni placeholders sin reemplazar.

# 6. Despliegue con Quadlet

## 6.1 Crear el archivo Quadlet

```bash
sudo tee /etc/containers/systemd/prometheus.container > /dev/null <<EOF
[Unit]
Description=Prometheus - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<PROMETHEUS_IMAGE>
ContainerName=prometheus
Volume=/etc/observabilidad/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro,Z
Volume=/data/prometheus:/prometheus:Z
Exec=--config.file=/etc/prometheus/prometheus.yml --storage.tsdb.path=/prometheus --storage.tsdb.retention.time=15d --web.enable-lifecycle
PublishPort=9090:9090

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

> **NOTA sobre `--web.enable-lifecycle`:** Este flag habilita el endpoint `/-/reload` que permite recargar la configuración de Prometheus sin reiniciar el contenedor. Es útil para agregar nuevos targets sin interrumpir el servicio.
>
> **NOTA sobre `--storage.tsdb.retention.time=15d`:** Define cuántos días de métricas retiene Prometheus. Cambiar este valor requiere reiniciar el servicio (no el redeploy del contenedor). Para cambiarlo: editar el Quadlet, ejecutar `sudo systemctl daemon-reload` y `sudo systemctl restart prometheus`.

## 6.2 Recargar systemd e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable prometheus
sudo systemctl start prometheus
```

## 6.3 Verificar que el servicio está activo

```bash
sudo systemctl status prometheus
```

Resultado esperado:

```
● prometheus.service - Prometheus - Stack de Observabilidad ONP
     Active: active (running) since ...
```

# 7. Verificación del despliegue

## 7.1 Verificar los logs de Prometheus

```bash
sudo journalctl -u prometheus -n 30
```

Al iniciar correctamente, los logs muestran:

```
ts=... level=info msg="Starting Prometheus Server" version="(version=2.55.0, ...)"
ts=... level=info msg="Completed loading of configuration file" filename=/etc/prometheus/prometheus.yml
ts=... level=info msg="Server is ready to receive web requests."
```

La línea **"Server is ready to receive web requests."** confirma que Prometheus inició correctamente.

## 7.2 Verificar el health check

```bash
curl http://localhost:9090/-/healthy
```

Resultado esperado:

```
Prometheus Server is Healthy.
```

## 7.3 Acceder a la UI de Prometheus

Abrir el navegador en:

```
http://<IP_SERVIDOR_PROMETHEUS>:9090
```

La UI de Prometheus carga sin necesidad de autenticación.

## 7.4 Verificar el estado de los targets de scrape

En la UI de Prometheus, ir a **Status** → **Targets**.

Todos los targets configurados en `prometheus.yml` deben aparecer en estado `UP`:

```
otel-collector   http://<COLLECTOR_HOST>:8889/metrics   UP   15s ago
prometheus       http://localhost:9090/metrics           UP   15s ago
```

Un target en estado `DOWN` indica que Prometheus no puede alcanzar el endpoint. El campo `Error` muestra el motivo específico.

## 7.5 Verificar que hay métricas del Collector

En la UI, ir a **Graph** e ingresar la siguiente consulta en el campo de expresión:

```
otelcol_receiver_accepted_spans_total
```

Hacer clic en **Execute**. Si el Collector está enviando trazas, deben aparecer series temporales con valores numéricos.

# 8. Recarga de configuración sin reinicio

Cuando se agrega un nuevo target al `prometheus.yml`, es posible recargar la configuración sin interrumpir el servicio:

```bash
# Editar el archivo de configuración
sudo nano /etc/observabilidad/prometheus/prometheus.yml

# Recargar la configuración via API (sin reiniciar el contenedor)
curl -X POST http://localhost:9090/-/reload
```

Resultado esperado: respuesta HTTP `200` (sin body).

Verificar en la UI (**Status** → **Targets**) que el nuevo target aparece.

> **NOTA:** Si hay un error en el archivo YAML, el endpoint `/-/reload` devuelve HTTP `400` con el mensaje de error. La configuración anterior permanece activa hasta que se corrija el error.

# 9. Troubleshooting

## 9.1 Error de permisos en /data/prometheus

### Síntoma

```bash
sudo journalctl -u prometheus -n 20
# opening storage failed ... permission denied
```

### Causa y solución

El directorio `/data/prometheus` no tiene el propietario correcto:

```bash
sudo chown -R 65534:65534 /data/prometheus
sudo systemctl restart prometheus
```

## 9.2 Los targets aparecen en estado DOWN

### Síntoma

En la UI (**Status** → **Targets**), el OTEL Collector aparece como `DOWN`.

### Causas y soluciones

- **El Collector no está corriendo**: verificar `sudo systemctl status otel-collector` en el servidor del Collector.
- **La IP del Collector es incorrecta**: verificar el valor de `<COLLECTOR_HOST>` en el `prometheus.yml`.
- **El firewall en el servidor del Collector bloquea el puerto 8889**: en el servidor del Collector ejecutar:

  ```bash
  sudo firewall-cmd --list-ports | grep 8889
  ```

  Si no aparece, agregar el puerto:

  ```bash
  sudo firewall-cmd --permanent --add-port=8889/tcp
  sudo firewall-cmd --reload
  ```

## 9.3 La UI no carga en el navegador

### Causas y soluciones

- Verificar que el servicio está corriendo: `sudo systemctl status prometheus`
- Verificar que el firewall permite el puerto 9090: `sudo firewall-cmd --list-ports`
- Verificar que el puerto está escuchando: `sudo ss -tlnp | grep 9090`

## 9.4 Comandos útiles de diagnóstico rápido

| Diagnóstico | Comando |
|-------------|---------|
| Estado del servicio | `sudo systemctl status prometheus` |
| Logs en tiempo real | `sudo journalctl -u prometheus -f` |
| Health check | `curl http://localhost:9090/-/healthy` |
| Ver targets activos | `curl http://localhost:9090/api/v1/targets` |
| Recargar configuración | `curl -X POST http://localhost:9090/-/reload` |
| Reiniciar Prometheus | `sudo systemctl restart prometheus` |
| Ver uso de disco datos | `du -sh /data/prometheus` |

# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<PROMETHEUS_IMAGE\> | `prom/prometheus:2.55.0` (Escenario B) |
| \<ENV\> | `development` |
| \<COLLECTOR_HOST\> | `localhost` o IP del Servidor 1 |

## A.1 prometheus.yml completo para DEV

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    environment: 'development'

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'otel-collector'
    static_configs:
      - targets: ['localhost:8889']
    metrics_path: '/metrics'
```

# Anexo B — Configuración específica para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<PROMETHEUS_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/prometheus:2.55.0` |
| \<ENV\> | `quality` |
| \<COLLECTOR_HOST\> | VIP de Keepalived del Collector en QA |

## B.1 prometheus.yml completo para QA

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    environment: 'quality'

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'otel-collector'
    static_configs:
      - targets: ['<VIP_COLLECTOR>:8889']
    metrics_path: '/metrics'
```

# Anexo C — Configuración específica para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<PROMETHEUS_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/prometheus:2.55.0` |
| \<ENV\> | `production` |
| \<COLLECTOR_HOST\> | VIP de Keepalived del Collector en PROD |

## C.1 prometheus.yml completo para PROD

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    environment: 'production'

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'otel-collector'
    static_configs:
      - targets: ['<VIP_COLLECTOR>:8889']
    metrics_path: '/metrics'
```

# Anexo D — Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen

```bash
podman pull prom/prometheus:<PROMETHEUS_VERSION>
podman save prom/prometheus:<PROMETHEUS_VERSION> \
  -o prometheus-<PROMETHEUS_VERSION>.tar
```

## D.2 Transferir e importar

```bash
scp prometheus-<PROMETHEUS_VERSION>.tar <USUARIO>@<IP_SERVIDOR>:/tmp/
sudo podman load -i /tmp/prometheus-<PROMETHEUS_VERSION>.tar
sudo podman images | grep prometheus
```

## D.3 Usar en el archivo Quadlet

```ini
Image=docker.io/prom/prometheus:<PROMETHEUS_VERSION>
```
