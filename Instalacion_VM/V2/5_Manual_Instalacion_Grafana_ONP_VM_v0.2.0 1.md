**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Grafana en Servidor Virtual (RHEL 8)**

Instalación nativa sobre Red Hat Enterprise Linux 8

| **Versión:**        | 0.2.0      |
|---------------------|------------|
| **Fecha:**          | 2026-06-26 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.2.0       | 2026-06-26 | \<AUTOR\>   | Reescritura para instalación nativa vía RPM, sin runtime de contenedores |
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Grafana en servidores virtuales RHEL 8 on-premise, usando el paquete RPM oficial de Grafana y el sistema de gestión de servicios systemd nativo de RHEL 8.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.

## 1.2 ¿Qué es Grafana?

Grafana es una plataforma de visualización y análisis de métricas. Permite crear dashboards interactivos conectando múltiples fuentes de datos (datasources). En el stack ONP actúa como la consola de observabilidad unificada:

- **Métricas**: lee de Prometheus (métricas de los servicios y del OTEL Collector).
- **Trazas**: se conecta a Jaeger para visualizar trazas distribuidas.
- **Logs**: opcionalmente puede conectarse a Elasticsearch para visualizar logs.

## 1.3 Rol de Grafana en el stack ONP

```
Prometheus (métricas)  →  Grafana  ←  Jaeger (trazas)
                              ↑
                      Elasticsearch (logs, opcional)
```

## 1.4 Arquitectura por entorno

| Entorno | Nodos Prometheus | Tipo de cluster | Servidores |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 |
| QA      | 1        | Single-node     | Servidor 1 |
| PROD    | 1        | Single-node     | Servidor 1 |

## 1.5 Persistencia de datos

Grafana almacena sus dashboards, datasources y configuración en una base de datos SQLite embebida ubicada en su directorio de datos. Esta información persiste entre reinicios del servicio en el directorio `/data/grafana` del servidor.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios, permisos).
- Instalación de Grafana vía RPM.
- Configuración de datasources (Prometheus y Jaeger).
- Verificación del despliegue.

Queda fuera del alcance:

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

| Componente | Verificación |
|------------|--------------|
| Prometheus (recomendado) | `curl http://<PROMETHEUS_HOST>:9090/-/healthy` → `Prometheus Server is Healthy` |
| Jaeger (recomendado) | `curl http://<JAEGER_HOST>:14269/` → {"status":"Server available",...} |

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| curl | Cualquier versión | `curl --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<GRAFANA_VERSION\> | Versión de Grafana | 11.4.0 |
| \<HOST_SERVIDOR_GRAFANA\> | Host o IP donde se instalará Grafana |  |
| \<GRAFANA_ADMIN_PASSWORD\> | Contraseña que se asignará al usuario admin de Grafana | |
| \<PROMETHEUS_HOST\> | IP o hostname del servidor Prometheus | |
| \<JAEGER_HOST\> | IP o hostname del servidor Jaeger | |

# 3. Preparación del servidor

## 3.1 Crear la estructura de directorios

Grafana almacena su base de datos interna y configuración en su directorio de datos. Usaremos `/data/grafana` para mantener los datos en el filesystem dedicado:

```bash
# Directorio de datos persistentes de Grafana
sudo mkdir -p /data/grafana

# El usuario grafana es creado por el RPM. Esta asignación de permisos
# se completará después de instalar el RPM en la Sección 4.2.
```

## 3.2 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 3000 | TCP | UI web de Grafana y API REST |

```bash
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

> **NOTA:** La apertura de puertos mediante firewalld únicamente habilita el acceso a nivel del sistema operativo RHEL. Si el servidor se ejecuta dentro de una máquina virtual, deberá verificarse además la configuración de red del hipervisor (VMware, VirtualBox, Hyper-V, KVM u otro) y de los dispositivos de red intermedios para garantizar que los puertos publicados sean accesibles desde los sistemas que requieran conectarse al servicio.

# 4. Instalación del RPM

## 4.1 Instalar el paquete RPM

```bash
sudo rpm -ivh /tmp/onp-packages/rpm/grafana-11.4.0-1.x86_64.rpm
```

La instalación:

- Crea el usuario y grupo del sistema `grafana`.
- Instala los binarios en `/usr/share/grafana/`.
- Crea el directorio de configuración `/etc/grafana/`.
- Registra la unidad systemd `grafana-server.service`.

## 4.2 Asignar permisos al directorio de datos

```bash
sudo chown -R grafana:grafana /data/grafana

# Verificar los permisos
ls -la /data/ | grep grafana
```

Resultado esperado:

```
drwxr-xr-x 2 grafana grafana 4096 jun 17 10:30 grafana
```

# 5. Configuración de Grafana

## 5.1 Configurar el archivo grafana.ini

Ejecutar el siguiente comando, reemplazando `<HOST_SERVIDOR_GRAFANA>` y `<GRAFANA_ADMIN_PASSWORD>` por los valores correspondientes.

```bash
sudo tee /etc/grafana/grafana.ini > /dev/null <<EOF
[paths]
data = /data/grafana
logs = /var/log/grafana
plugins = /data/grafana/plugins

[server]
http_port = 3000
domain = <HOST_SERVIDOR_GRAFANA>

[security]
admin_user = admin
admin_password = <GRAFANA_ADMIN_PASSWORD>

[analytics]
reporting_enabled = false
check_for_updates = false
EOF
```

> **NOTA sobre `reporting_enabled` y `check_for_updates`:** Ambas se establecen en `false` para evitar que Grafana intente conectarse a internet para reportar telemetría anónima o verificar actualizaciones. Esto es especialmente importante en entornos sin salida a internet (QA y PROD).

## 5.2 Asegurar los permisos del archivo de configuración

```bash
sudo chmod 640 /etc/grafana/grafana.ini
sudo chown root:grafana /etc/grafana/grafana.ini
ls -la /etc/grafana/grafana.ini
```

Resultado esperado:

```
-rw-r----- 1 root grafana ... grafana.ini
```

# 6. Arranque del servicio

## 6.1 Habilitar e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable grafana-server
sudo systemctl start grafana-server
```

## 6.2 Verificar que el servicio está activo

Despues de unos 5 segundos ejecutar el siguiente comando.

```bash
sudo systemctl status grafana-server
```

Resultado esperado:

```
● grafana-server.service - Grafana instance
     Loaded: loaded (/usr/lib/systemd/system/grafana-server.service; enabled)
     Active: active (running) since ...
```

# 7. Verificación del despliegue

## 7.1 Verificar los logs de Grafana

```bash
sudo journalctl -u grafana-server -n 30
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

1. En la UI, ir a **Connections** → **Data sources** → **Add data source**.
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

Resultado esperado: mensaje `"Data source connected and services found."`.

En este punto finaliza la instalación.


# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<PROMETHEUS_HOST\> | `localhost` o IP del Servidor 1 |
| \<JAEGER_HOST\> | `localhost` o IP del Servidor 1 |

# Anexo B — Configuración específica para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<PROMETHEUS_HOST\> | IP del Servidor 1 (Prometheus corre en el mismo Servidor 1) |
| \<JAEGER_HOST\> | IP del Servidor 1 (Jaeger corre en el mismo Servidor 1) |

# Anexo C — Configuración específica para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<PROMETHEUS_HOST\> | IP del Servidor 1 (Prometheus corre en el mismo Servidor 1) |
| \<JAEGER_HOST\> | IP del Servidor 5 (Jaeger corre en Servidor 5) |
