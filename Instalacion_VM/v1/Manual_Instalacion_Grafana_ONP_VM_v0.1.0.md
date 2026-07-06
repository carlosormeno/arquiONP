**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Grafana en Servidor Virtual (RHEL 8)**

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

Este manual describe el proceso completo de instalación de Grafana en servidores virtuales RHEL 8 on-premise, usando Podman como runtime de contenedores y Quadlet para la integración con systemd.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.

## 1.2 ¿Qué es Grafana?

Grafana es una plataforma de visualización y análisis de métricas. Permite crear dashboards interactivos conectando múltiples fuentes de datos (datasources). En el stack ONP actúa como la consola de observabilidad unificada:

- **Métricas**: lee de Prometheus (métricas de los servicios y del OTEL Collector).
- **Trazas**: se conecta a Jaeger para visualizar trazas distribuidas.
- **Logs**: opcionalmente puede conectarse a Elasticsearch o Loki para visualizar logs.

## 1.3 Rol de Grafana en el stack ONP

```
Prometheus (métricas)  →  Grafana  ←  Jaeger (trazas)
                              ↑
                      Elasticsearch (logs, opcional)
```

## 1.4 Arquitectura por entorno

| Entorno | Servidor | Otros servicios en el mismo servidor |
|---------|----------|--------------------------------------|
| DEV | Servidor 1 | Stack completo |
| QA | Servidor 1 | Collector activo + Jaeger + Prometheus |
| PROD | Servidor 1 | Collector activo + Prometheus |

## 1.5 Persistencia de datos

Grafana almacena sus dashboards, datasources y configuración en una base de datos SQLite embebida (por defecto) ubicada en `/var/lib/grafana`. Esta información debe persistir entre reinicios del contenedor, por lo que se monta un volumen en el directorio `/data/grafana` del servidor.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios, permisos).
- Despliegue de Grafana vía Podman + Quadlet.
- Configuración de datasources (Prometheus y Jaeger).
- Verificación del despliegue.
- Troubleshooting de errores comunes.

Queda fuera del alcance:

- Instalación de Prometheus (ver Manual de Instalación Prometheus VM).
- Instalación de Jaeger (ver Manual de Instalación Jaeger VM).
- Creación de dashboards personalizados.
- Configuración de autenticación LDAP o SSO.

# 2. Prerrequisitos

## 2.1 Infraestructura requerida

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 1 | 4 | 8 GB | 50 GB |
| PROD | Servidor 1 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos

Grafana puede instalarse antes que Prometheus y Jaeger. Los datasources se configuran en la UI y Grafana muestra un error de conexión temporal si el datasource no está disponible al arrancar, pero funciona en cuanto el datasource esté operativo.

| Componente | Verificación |
|------------|--------------|
| Prometheus (recomendado) | `curl http://<PROMETHEUS_HOST>:9090/-/healthy` → `Prometheus Server is Healthy` |
| Jaeger (recomendado) | `curl http://<JAEGER_HOST>:14269/` → HTTP 200 |

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
| \<GRAFANA_VERSION\> | Versión de Grafana | 11.4.0 |
| \<GRAFANA_ADMIN_PASSWORD\> | Contraseña del usuario admin de Grafana | |
| \<PROMETHEUS_HOST\> | IP o hostname del servidor Prometheus | |
| \<JAEGER_HOST\> | IP o hostname del servidor Jaeger | |

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
| Grafana | grafana/grafana | 11.4.0 |

## 3.2 Escenario A — GitLab Registry (QA y PROD)

```bash
podman login <GITLAB_REGISTRY_URL>

podman pull grafana/grafana:<GRAFANA_VERSION>

podman tag grafana/grafana:<GRAFANA_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/grafana:<GRAFANA_VERSION>

podman push <GITLAB_REGISTRY_URL>/observabilidad/grafana:<GRAFANA_VERSION>
```

## 3.3 Escenario B — Internet directo (DEV)

No se requieren pasos de mirroring. Usar directamente:

```
grafana/grafana:<GRAFANA_VERSION>
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

Grafana almacena su base de datos interna y configuración en `/var/lib/grafana` dentro del contenedor. Este directorio se monta en `/data/grafana` del servidor para que los datos persistan entre reinicios.

```bash
# Directorio de datos persistentes de Grafana
sudo mkdir -p /data/grafana

# Directorio de configuración
sudo mkdir -p /etc/observabilidad/grafana

# Grafana corre internamente como UID 472 (usuario grafana en la imagen oficial)
sudo chown -R 472:472 /data/grafana

# Verificar los permisos
ls -la /data/ | grep grafana
```

Resultado esperado:

```
drwxr-xr-x 2 472 472 4096 may 20 10:30 grafana
```

## 4.3 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 3000 | TCP | UI web de Grafana y API REST |

```bash
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

# 5. Configuración de Grafana

## 5.1 Crear el archivo de entorno

```bash
sudo tee /etc/observabilidad/grafana/grafana.env > /dev/null <<EOF
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=<GRAFANA_ADMIN_PASSWORD>
GF_SERVER_HTTP_PORT=3000
GF_SERVER_DOMAIN=<IP_SERVIDOR_GRAFANA>
GF_ANALYTICS_REPORTING_ENABLED=false
GF_ANALYTICS_CHECK_FOR_UPDATES=false
EOF
```

```bash
sudo chmod 600 /etc/observabilidad/grafana/grafana.env
sudo chown root:root /etc/observabilidad/grafana/grafana.env
```

> **NOTA sobre `GF_ANALYTICS_REPORTING_ENABLED` y `GF_ANALYTICS_CHECK_FOR_UPDATES`:** Ambas se establecen en `false` para evitar que Grafana intente conectarse a internet para reportar telemetría anónima o verificar actualizaciones. Esto es especialmente importante en entornos sin salida a internet (QA y PROD).

# 6. Despliegue con Quadlet

## 6.1 Crear el archivo Quadlet

```bash
sudo tee /etc/containers/systemd/grafana.container > /dev/null <<EOF
[Unit]
Description=Grafana - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<GRAFANA_IMAGE>
ContainerName=grafana
EnvironmentFile=/etc/observabilidad/grafana/grafana.env
Volume=/data/grafana:/var/lib/grafana:Z
PublishPort=3000:3000

[Service]
Restart=always
RestartSec=10
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

## 6.2 Recargar systemd e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable grafana
sudo systemctl start grafana
```

## 6.3 Verificar que el servicio está activo

```bash
sudo systemctl status grafana
```

Resultado esperado:

```
● grafana.service - Grafana - Stack de Observabilidad ONP
     Active: active (running) since ...
```

# 7. Verificación del despliegue

## 7.1 Verificar los logs de Grafana

```bash
sudo journalctl -u grafana -n 30
```

Al iniciar correctamente, los logs muestran:

```
logger=http.server t=... level=info msg="HTTP Server Listen" address=[::]:3000 protocol=http subUrl= socket=
```

## 7.2 Acceder a la UI de Grafana

Abrir el navegador en:

```
http://<IP_SERVIDOR_GRAFANA>:3000
```

Ingresar con:
- **Usuario:** `admin`
- **Contraseña:** `<GRAFANA_ADMIN_PASSWORD>`

## 7.3 Configurar el datasource de Prometheus

1. En la UI, ir a **Connections** → **Data sources** → **Add new data source**.
2. Seleccionar **Prometheus**.
3. Completar:
   - **Name:** `Prometheus ONP`
   - **Prometheus server URL:** `http://<PROMETHEUS_HOST>:9090`
4. Hacer clic en **Save & test**.

Resultado esperado: mensaje `"Successfully queried the Prometheus API."`.

## 7.4 Configurar el datasource de Jaeger

1. En la UI, ir a **Connections** → **Data sources** → **Add new data source**.
2. Seleccionar **Jaeger**.
3. Completar:
   - **Name:** `Jaeger ONP`
   - **URL:** `http://<JAEGER_HOST>:16686`
4. Hacer clic en **Save & test**.

Resultado esperado: mensaje `"Data source connected and labels found."`.

# 8. Troubleshooting

## 8.1 Error de permisos en /data/grafana

### Síntoma

```bash
sudo journalctl -u grafana -n 20
# GF_PATHS_DATA='/var/lib/grafana' is not writable
```

### Causa y solución

El directorio `/data/grafana` no tiene el propietario correcto:

```bash
sudo chown -R 472:472 /data/grafana
sudo systemctl restart grafana
```

## 8.2 La UI no carga en el navegador

### Causas y soluciones

- Verificar que el servicio está corriendo: `sudo systemctl status grafana`
- Verificar que el firewall permite el puerto 3000: `sudo firewall-cmd --list-ports`
- Verificar que el puerto está escuchando: `sudo ss -tlnp | grep 3000`

## 8.3 El datasource de Prometheus falla

### Síntoma

Al hacer "Save & test" del datasource, aparece error de conexión.

### Causa y solución

- Verificar que Prometheus está corriendo en el servidor correcto.
- Probar la conectividad directamente: `curl http://<PROMETHEUS_HOST>:9090/-/healthy`
- Si hay un firewall entre los servidores, verificar que el puerto 9090 está abierto.

## 8.4 Comandos útiles de diagnóstico rápido

| Diagnóstico | Comando |
|-------------|---------|
| Estado del servicio | `sudo systemctl status grafana` |
| Logs en tiempo real | `sudo journalctl -u grafana -f` |
| Health check API | `curl http://localhost:3000/api/health` |
| Reiniciar Grafana | `sudo systemctl restart grafana` |
| Ver uso de disco datos | `du -sh /data/grafana` |

# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<GRAFANA_IMAGE\> | `grafana/grafana:11.4.0` (Escenario B) |
| \<PROMETHEUS_HOST\> | `localhost` o IP del Servidor 1 |
| \<JAEGER_HOST\> | `localhost` o IP del Servidor 1 |

# Anexo B — Configuración específica para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<GRAFANA_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/grafana:11.4.0` |
| \<PROMETHEUS_HOST\> | IP del Servidor 1 (Prometheus corre en el mismo Servidor 1) |
| \<JAEGER_HOST\> | IP del Servidor 1 (Jaeger corre en el mismo Servidor 1) |

# Anexo C — Configuración específica para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<GRAFANA_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/grafana:11.4.0` |
| \<PROMETHEUS_HOST\> | IP del Servidor 1 (Prometheus corre en el mismo Servidor 1) |
| \<JAEGER_HOST\> | IP del Servidor 5 (Jaeger corre en Servidor 5) |

# Anexo D — Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen

```bash
podman pull grafana/grafana:<GRAFANA_VERSION>
podman save grafana/grafana:<GRAFANA_VERSION> \
  -o grafana-<GRAFANA_VERSION>.tar
```

## D.2 Transferir e importar

```bash
scp grafana-<GRAFANA_VERSION>.tar <USUARIO>@<IP_SERVIDOR>:/tmp/
sudo podman load -i /tmp/grafana-<GRAFANA_VERSION>.tar
sudo podman images | grep grafana
```

## D.3 Usar en el archivo Quadlet

```ini
Image=docker.io/grafana/grafana:<GRAFANA_VERSION>
```
