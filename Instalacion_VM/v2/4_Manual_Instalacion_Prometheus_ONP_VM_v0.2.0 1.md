**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Prometheus en Servidor Virtual (RHEL 8)**

Instalación nativa sobre Red Hat Enterprise Linux 8

| **Versión:**        | 0.2.0      |
|---------------------|------------|
| **Fecha:**          | 2026-06-26 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.2.0       | 2026-06-26 | \<AUTOR\>   | Reescritura para instalación nativa vía binario, sin runtime de contenedores |
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Prometheus en servidores virtuales RHEL 8 on-premise, usando el binario oficial de Prometheus y una unidad systemd personalizada.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.

## 1.2 ¿Qué es Prometheus?

Prometheus es un sistema de monitoreo y alerta de código abierto. Recolecta métricas de los servicios a través de un modelo de **pull**: periódicamente consulta (scrape) los endpoints de métricas de los componentes del stack y almacena las series temporales en su base de datos local.

En el stack ONP, Prometheus recolecta métricas de:

- **OTEL Collector**: métricas del Collector disponibles en el puerto 8889.

Estas métricas son consultadas por Grafana para construir dashboards de observabilidad.

## 1.3 Rol de Prometheus en el stack ONP

```
OTEL Collector (:8889/metrics)  →  Prometheus  →  Grafana
```

## 1.4 Arquitectura por entorno

| Entorno | Nodos Prometheus | Tipo de cluster | Servidores |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 |
| QA      | 1        | Single-node     | Servidor 1 |
| PROD    | 1        | Single-node     | Servidor 1 |

## 1.5 Persistencia de datos

Prometheus almacena sus series temporales en `/data/prometheus`. Por defecto retiene datos durante **15 días**. Este valor se puede ajustar sin reinstalar — solo requiere modificar la unidad systemd y reiniciar el servicio.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios, permisos).
- Instalación del binario de Prometheus y creación de la unidad systemd.
- Configuración del archivo `prometheus.yml` con los targets de scrape.
- Verificación del despliegue y de los targets de scrape.

Queda fuera del alcance:

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

| Componente | Verificación |
|------------|--------------|
| OTEL Collector | `sudo systemctl status otel-collector` → En el contenido se debe indicar `Active: active (running)` |

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| curl | Cualquier versión | `curl --version` |
| tar | Cualquier versión | `tar --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<PROMETHEUS_VERSION\> | Versión de Prometheus | 2.55.0 |
| \<COLLECTOR_HOST\> | IP o hostname del servidor del OTEL Collector (o VIP en QA/PROD) | |

# 3. Preparación del servidor

## 3.1 Crear el usuario y grupo del sistema

Prometheus no instala un usuario del sistema automáticamente. Crearlo manualmente antes de la instalación:

```bash
sudo useradd --no-create-home --shell /bin/false --system prometheus

# Verificar la creación del usuario
id prometheus
```

## 3.2 Crear la estructura de directorios

```bash
# Directorio de datos persistentes de Prometheus
sudo mkdir -p /data/prometheus

# Directorio de configuración
sudo mkdir -p /etc/prometheus

# Asignar propietario al directorio de datos
sudo chown -R prometheus:prometheus /data/prometheus

# Verificar los permisos
ls -la /data/ | grep prometheus
```

Resultado esperado:

```
drwxr-xr-x 2 prometheus prometheus 4096 jun 17 10:45 prometheus
```

## 3.3 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 9090 | TCP | UI web de Prometheus y API REST |

```bash
sudo firewall-cmd --permanent --add-port=9090/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

> **NOTA:** La apertura de puertos mediante firewalld únicamente habilita el acceso a nivel del sistema operativo RHEL. Si el servidor se ejecuta dentro de una máquina virtual, deberá verificarse además la configuración de red del hipervisor (VMware, VirtualBox, Hyper-V, KVM u otro) y de los dispositivos de red intermedios para garantizar que los puertos publicados sean accesibles desde los sistemas que requieran conectarse al servicio.

# 4. Instalación del binario

## 4.1 Extraer el archivo tar.gz

```bash
tar xzf /tmp/onp-packages/bin/prometheus-2.55.0.linux-amd64.tar.gz \
  -C /tmp/
```

## 4.2 Instalar el binario

```bash
# Copiar el binario al directorio del sistema
sudo cp /tmp/prometheus-2.55.0.linux-amd64/prometheus /usr/local/bin/prometheus

# Asignar propietario y permisos
sudo chown prometheus:prometheus /usr/local/bin/prometheus
sudo chmod 755 /usr/local/bin/prometheus

# Verificar la instalación
/usr/local/bin/prometheus --version
```

Resultado esperado:

```
prometheus, version 2.55.0 (branch: HEAD, ...)
```

## 4.3 Limpiar archivos temporales

```bash
rm -rf /tmp/prometheus-2.55.0.linux-amd64/
```

# 5. Configuración de Prometheus

## 5.1 Crear el archivo prometheus.yml

Ejecutar el siguiente comando, reemplazando `<ENV>` y `<COLLECTOR_HOST>`.
Donde:
- `<ENV>` es el nombre del entorno: `development`, `quality` o `production`.
- `<COLLECTOR_HOST>` es la IP del OTEL Collector (en DEV) o la VIP de Keepalived (en QA y PROD).

```bash
sudo tee /etc/prometheus/prometheus.yml > /dev/null <<EOF
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

## 5.3 Asignar permisos y verificar la configuración

```bash
sudo chown prometheus:prometheus /etc/prometheus/prometheus.yml
sudo chmod 640 /etc/prometheus/prometheus.yml

# Verificar la estructura del archivo
sudo cat /etc/prometheus/prometheus.yml
```

# 6. Creación de la unidad systemd

## 6.1 Crear el archivo de servicio

```bash
sudo tee /etc/systemd/system/prometheus.service > /dev/null <<EOF
[Unit]
Description=Prometheus - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Service]
Type=simple
User=prometheus
Group=prometheus
ExecStart=/usr/local/bin/prometheus \
  --config.file=/etc/prometheus/prometheus.yml \
  --storage.tsdb.path=/data/prometheus \
  --storage.tsdb.retention.time=15d \
  --web.enable-lifecycle
Restart=always
RestartSec=10
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

> **NOTA sobre `--web.enable-lifecycle`:** Habilita el endpoint `/-/reload` para recargar la configuración sin reiniciar el servicio. Útil para agregar nuevos targets.
>
> **NOTA sobre `--storage.tsdb.retention.time=15d`:** Para cambiar posteriormente la retención, editar este valor en el archivo de servicio, ejecutar `sudo systemctl daemon-reload` y luego `sudo systemctl restart prometheus`.

# 7. Arranque del servicio

## 7.1 Habilitar e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable prometheus
sudo systemctl start prometheus
```

## 7.2 Verificar que el servicio está activo
Esperar unos 5 segundos y luego ejecutar el siguente comando.

```bash
sudo systemctl status prometheus
```

Resultado esperado:

```
● prometheus.service - Prometheus - Stack de Observabilidad ONP
     Loaded: loaded (/etc/systemd/system/prometheus.service; enabled)
     Active: active (running) since ...
```

# 8. Verificación del despliegue

## 8.1 Verificar los logs de Prometheus

```bash
sudo journalctl -u prometheus -n 30
```

Al iniciar correctamente:

```
ts=... level=info msg="Starting Prometheus Server" version="(version=2.55.0, ...)"
ts=... level=info msg="Completed loading of configuration file" filename=/etc/prometheus/prometheus.yml
ts=... level=info msg="Server is ready to receive web requests."
```

## 8.2 Verificar el health check

```bash
curl http://localhost:9090/-/healthy
```

Resultado esperado: `Prometheus Server is Healthy.`

## 8.3 Acceder a la UI de Prometheus

Abrir el navegador en:

```
http://<IP_SERVIDOR_PROMETHEUS>:9090
```

Donde:
- `<IP_SERVIDOR_PROMETHEUS>` es la dirección IP del servidor donde se instaló prometheus.

## 8.4 Verificar el estado de los targets de scrape

Después de un par de minutos, en la UI, ir a **Status** → **Targets**. Todos los targets configurados deben aparecer en estado `UP`.
En este punto finaliza la instalación.


# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
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
