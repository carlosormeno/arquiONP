**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**PREPARACIÓN DE PAQUETES E INSTALADORES**

**Stack de Observabilidad ONP — Servidor Virtual (RHEL 8)**

Descarga y transferencia de paquetes RPM y binarios antes de la instalación

| **Versión:**        | 0.2.0      |
|---------------------|------------|
| **Fecha:**          | 2026-06-26 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.2.0       | 2026-06-17 | \<AUTOR\>   | Reescritura para instalación nativa (RPM + binarios, sin contenedores) |

# 1. Propósito del documento

Este documento describe el proceso de descarga de todos los paquetes e instaladores necesarios para el stack de observabilidad ONP en sus versiones nativas para RHEL 8.

**Este proceso debe ejecutarse una sola vez, antes de iniciar cualquier instalación.** Una vez que los paquetes están disponibles en el servidor (o en un repositorio local), los servidores de DEV, QA y PROD pueden instalarse sin necesidad de acceso a internet.
Los paquetes deben descargarse desde una máquina con acceso a internet y transferirse a los servidores mediante SCP o medio físico.

## 1.1 Tipos de paquetes

El stack utiliza dos tipos de instaladores:

| Tipo | Componentes | Formato |
|------|-------------|---------|
| RPM (gestor de paquetes) | Elasticsearch, Kibana, Grafana | `.rpm` |
| Binario (instalación manual) | Prometheus, Jaeger, OTEL Collector | `.tar.gz` |

Los componentes RPM (Elasticsearch, Kibana, Grafana) se gestionan con `dnf`/`rpm` y crean automáticamente usuarios del sistema, directorios y unidades systemd. Los binarios requieren una instalación manual documentada en cada manual de instalación.

## 1.2 Relación con los manuales de instalación

```
[Este documento]          [Manuales de instalación]
Descargar paquetes   →    Instalar cada componente
y transferirlos a         usando los archivos
los servidores            descargados
```

# 2. Prerrequisitos

## 2.1 Máquina de trabajo

El proceso debe ejecutarse desde una **máquina con acceso a internet**. Puede ser:

- Una laptop o PC del equipo de plataforma con acceso a internet.
- Un servidor intermedio (bastion) que tenga salida a internet.

Esta máquina **no** necesita ser uno de los servidores del stack.

## 2.2 Software requerido en la máquina de trabajo

| Herramienta | Verificación |
|-------------|--------------|
| `curl` o `wget` | `curl --version` |
| `ssh` / `scp` | `ssh -V` |
| Acceso a internet | `curl https://www.elastic.co` → respuesta HTTP |

## 2.3 Accesos requeridos

- Acceso SSH a los servidores de DEV, QA y PROD para la transferencia de archivos.
- Suficiente espacio en `/tmp` de los servidores destino (mínimo 5 GB recomendado).

# 3. Proceso de descarga

## 3.1 Crear el directorio de trabajo

Crear el directorio "onp-packages".
Dentro del directorio "onp-packages" crear los subdirectorios "rpm" y "bin"

## 3.2 Descargar los paquetes RPM
En las siguientes URLs se encuentran los archivos que deben ser descargados.
Los archivos de extensión *.rpm guardarlos en el subdirectorio "rpm" creado previamente.
Verificar que ningún archivo tenga 0 bytes, de ser así volver a descargarlo.

```bash
# Elasticsearch
curl -L -o rpm/elasticsearch-8.19.15-x86_64.rpm \
  https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.19.15-x86_64.rpm

# Kibana
curl -L -o rpm/kibana-8.19.15-x86_64.rpm \
  https://artifacts.elastic.co/downloads/kibana/kibana-8.19.15-x86_64.rpm

# Grafana
curl -L -o rpm/grafana-11.4.0-1.x86_64.rpm \
  https://dl.grafana.com/oss/release/grafana-11.4.0-1.x86_64.rpm
```

## 3.3 Descargar los binarios
En las siguientes URLs se encuentran los archivos que deben ser descargados.
Los archivos de extensión *.gz guardarlos en el subdirectorio "bin" creado previamente.
Verificar que ningún archivo tenga 0 bytes, de ser así volver a descargarlo.

```bash
# Prometheus
curl -L -o bin/prometheus-2.55.0.linux-amd64.tar.gz \
  https://github.com/prometheus/prometheus/releases/download/v2.55.0/prometheus-2.55.0.linux-amd64.tar.gz

# Jaeger All-in-One
curl -L -o bin/jaeger-1.58.0-linux-amd64.tar.gz \
  https://github.com/jaegertracing/jaeger/releases/download/v1.58.0/jaeger-1.58.0-linux-amd64.tar.gz

# OTEL Collector Contrib
curl -L -o bin/otelcol-contrib_0.120.0_linux_amd64.tar.gz \
  https://github.com/open-telemetry/opentelemetry-collector-releases/releases/download/v0.120.0/otelcol-contrib_0.120.0_linux_amd64.tar.gz
```

# 4. Verificación de los archivos descargados

## 4.1 Verificar que los RPM no están corruptos
En la ventana de comandos posicionarse en el directorio donde se descargaron los archivos *.rpm y luego ejecutar los siguientes comandos para importar las claves públicas de los archivos.

```bash
sudo rpm --import https://artifacts.elastic.co/GPG-KEY-elasticsearch
sudo rpm --import https://rpm.grafana.com/gpg.key
```

Luego ejecutar los siguientes comandos para verificar que los RPM no están corruptos.

```bash
rpm -K elasticsearch-8.19.15-x86_64.rpm
rpm -K kibana-8.19.15-x86_64.rpm
rpm -K grafana-11.4.0-1.x86_64.rpm
```

Resultado esperado para cada comando:

```
rpm/elasticsearch-8.19.15-x86_64.rpm: digests signatures OK
```

## 4.2 Verificar que los archivos tar.gz no están corruptos
En la ventana de comandos posicionarse en el directorio donde se descargaron los archivos *.gz y luego ejecutar los siguientes comandos

```bash
tar -tzf prometheus-2.55.0.linux-amd64.tar.gz | head -5
tar -tzf jaeger-1.58.0-linux-amd64.tar.gz | head -5
tar -tzf otelcol-contrib_0.120.0_linux_amd64.tar.gz | head -5
```

Si algún archivo está corrupto, el comando devuelve un error. En ese caso, repetir la descarga del archivo afectado.

# 5. Transferencia a los servidores

## 5.1 Transferir los paquetes a cada servidor

Los RPMs y binarios deben estar disponibles en cada servidor donde se instalará el componente. Transferir todo el directorio "onp-packages", con todos sus subdirectorios y archivos, usando SCP:

```bash
# Transferir todos los archivos al directorio /tmp del servidor destino
scp -r ~/onp-packages/ <USUARIO>@<IP_SERVIDOR>:/tmp/onp-packages/
```

Donde `<USUARIO>` es el usuario con acceso SSH y `<IP_SERVIDOR>` es la IP del servidor de destino.

## 5.2 Distribución de componentes a instalar en cada servidor

| Entorno | Servidor | Componentes a instalar |
|----------|---------|----------------------|
| DEV | Servidor 1 | Todos (Elasticsearch, Kibana, OTEL Collector, Jaeger, Prometheus, Grafana) |
| QA | Servidor 1 | OTEL Collector (activo) + Jaeger + Prometheus + Grafana |
| QA | Servidor 2 | Elasticsearch + Kibana + OTEL Collector (standby) |
| PROD | Servidor 1 | OTEL Collector (activo) + Prometheus + Grafana |
| PROD | Servidor 2 | OTEL Collector (standby) |
| PROD | Servidor 3 | Elasticsearch (nodo 1) |
| PROD | Servidor 4 | Elasticsearch (nodo 2) + Kibana |
| PROD | Servidor 5 | Elasticsearch (nodo 3) + Jaeger |



## 5.3 Verificar la transferencia en el servidor destino

Desde el servidor destino:

```bash
ls -lh /tmp/onp-packages/rpm/
ls -lh /tmp/onp-packages/bin/
```

Los archivos deben aparecer con el mismo tamaño que en la máquina de trabajo.

# 6. Consideraciones adicionales

## 6.1 Espacio en disco requerido en la máquina de trabajo

| Archivo | Tamaño aproximado |
|---------|-------------------|
| elasticsearch-8.19.15-x86_64.rpm | ~647 MB |
| kibana-8.19.15-x86_64.rpm | ~382 MB |
| grafana-11.4.0-1.x86_64.rpm | ~119 MB |
| prometheus-2.55.0.linux-amd64.tar.gz | ~106 MB |
| jaeger-1.58.0-linux-amd64.tar.gz | ~129 MB |
| otelcol-contrib_0.120.0_linux_amd64.tar.gz | ~77 MB |
| **Total** | **~1.5 GB** |

## 6.2 Escenario Air-gap (sin red entre la máquina de trabajo y los servidores)

Si no hay conectividad SSH entre la máquina de trabajo y los servidores, los archivos pueden transferirse mediante un dispositivo de almacenamiento físico (USB, disco externo):

1. Copiar el directorio `~/onp-packages/` al dispositivo de almacenamiento.
2. Montar el dispositivo en el servidor destino.
3.  Copiar el directorio `~/onp-packages/` al directorio `/tmp/onp-packages/` del servidor.

# 7. Siguiente paso

Una vez verificado que los paquetes están disponibles en cada servidor, proceder con la instalación de los componentes en el orden recomendado:

Orden de instalación para DEV (1 nodo)
| Orden | Servidor   | Componentes a instalar |
|-------|------------|------------------------|
|1      | Servidor 1 | Elasticsearch |
|2      | Servidor 1 | Jaeger |
|3      | Servidor 1 | OTEL Collector |
|4      | Servidor 1 | Prometheus |
|5      | Servidor 1 | Grafana |
|6      | Servidor 1 | Kibana |

Orden de instalación para QA (2 nodos)
| Orden | Servidor   | Componentes a instalar |
|-------|------------|------------------------|
|1      | Servidor 2 | Elasticsearch |
|2      | Servidor 1 | Jaeger |
|3      | Servidor 1 | OTEL Collector |
|4      | Servidor 2 | OTEL Collector (standby) |
|5      | Servidor 1 | Prometheus |
|6      | Servidor 1 | Grafana |
|7      | Servidor 2 | Kibana |

Orden de instalación para PROD (5 nodos)
| Orden | Servidor   | Componentes a instalar |
|-------|------------|------------------------|
|1      | Servidor 3 | Elasticsearch (nodo 1) |
|2      | Servidor 4 | Elasticsearch (nodo 2) |
|3      | Servidor 5 | Elasticsearch (nodo 3) |
|4      | Servidor 5 | Jaeger |
|5      | Servidor 1 | OTEL Collector (activo) |
|6      | Servidor 2 | OTEL Collector (standby) |
|7      | Servidor 1 | Prometheus |
|8      | Servidor 1 | Grafana |
|9      | Servidor 4 | Kibana |
