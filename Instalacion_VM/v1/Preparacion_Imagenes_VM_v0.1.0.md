**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**PREPARACIÓN DE IMÁGENES**

**Stack de Observabilidad ONP — Servidor Virtual (RHEL 8)**

Carga de imágenes al GitLab Container Registry antes de la instalación

| **Versión:**        | 0.1.0      |
|---------------------|------------|
| **Fecha:**          | 2026-05-20 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del documento |

# 1. Propósito del documento

Este documento describe el proceso de descarga y carga de todas las imágenes de contenedor necesarias para el stack de observabilidad ONP en el GitLab Container Registry.

**Este proceso debe ejecutarse una sola vez, antes de iniciar cualquier instalación.** Una vez que las imágenes están en el registry privado, los servidores de QA y PROD pueden descargarlas sin necesidad de acceso a internet.

Los servidores de QA y PROD de ONP **no tienen salida directa a internet**. Por eso las imágenes deben descargarse desde una máquina con acceso a internet y publicarse en el registry privado de GitLab, desde donde los servidores las descargarán durante la instalación.

## 1.1 Relación con los manuales de instalación

Cada manual de instalación (`Manual_Instalacion_*_VM_v0.1.0.md`) documenta cómo usar las imágenes una vez que están en el registry. Este documento cubre el paso previo: cargarlas.

```
[Este documento]          [Manuales de instalación]
Descargar imágenes   →    Instalar cada componente
del registry público      usando las imágenes del
y publicarlas en          registry privado
GitLab Registry
```

# 2. Prerrequisitos

## 2.1 Máquina de trabajo

El proceso debe ejecutarse desde una **máquina con acceso a internet** y con Podman instalado. Puede ser:

- Una laptop o PC del equipo de plataforma con acceso a internet.
- Un servidor intermedio (bastion) que tenga salida a internet.

Esta máquina **no** necesita ser uno de los servidores del stack.

## 2.2 Software requerido en la máquina de trabajo

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| Podman | 4.0 o superior | `podman --version` |
| Acceso a internet | — | `curl https://registry-1.docker.io/v2/` → HTTP 401 (respuesta esperada) |

> **NOTA:** Si la máquina de trabajo es Windows o Mac, se puede usar Docker Desktop en lugar de Podman para realizar el mirroring. Los comandos `docker pull`, `docker tag` y `docker push` son equivalentes a los comandos de Podman mostrados en este documento.

## 2.3 Accesos requeridos

- Acceso de escritura al GitLab Container Registry del grupo de observabilidad.
- URL, usuario y token de acceso al GitLab Registry.

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<GITLAB_REGISTRY_URL\> | URL base del GitLab Registry | |
| \<GITLAB_USER\> | Usuario con acceso de escritura al registry | |
| \<GITLAB_TOKEN\> | Token de acceso personal (o contraseña) del usuario | |

### Cómo obtener el token de acceso en GitLab

1. Ingresar a GitLab con el usuario correspondiente.
2. Ir a **User Settings** → **Access Tokens**.
3. Crear un token con los scopes: `read_registry` y `write_registry`.
4. Copiar el token generado — solo se muestra una vez.

# 3. Imágenes requeridas

La siguiente tabla lista todas las imágenes necesarias para el stack completo. Estas son las versiones probadas y documentadas en los manuales de instalación.

| Componente | Imagen origen (internet) | Versión | Imagen destino (GitLab) |
|------------|--------------------------|---------|-------------------------|
| Elasticsearch | `docker.elastic.co/elasticsearch/elasticsearch` | 8.19.15 | `<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15` |
| Kibana | `docker.elastic.co/kibana/kibana` | 8.19.15 | `<GITLAB_REGISTRY_URL>/observabilidad/kibana:8.19.15` |
| OTEL Collector | `otel/opentelemetry-collector-contrib` | 0.120.0 | `<GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:0.120.0` |
| Jaeger | `jaegertracing/all-in-one` | 1.58.0 | `<GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0` |
| Grafana | `grafana/grafana` | 11.4.0 | `<GITLAB_REGISTRY_URL>/observabilidad/grafana:11.4.0` |
| Prometheus | `prom/prometheus` | 2.55.0 | `<GITLAB_REGISTRY_URL>/observabilidad/prometheus:2.55.0` |

> **NOTA sobre versiones:** Las versiones indicadas son las documentadas en los manuales de instalación versión 0.1.0. Si se actualiza una versión, repetir el proceso de mirroring para esa imagen y actualizar la versión correspondiente en el manual de instalación.

# 4. Proceso de mirroring

## 4.1 Iniciar sesión en el GitLab Registry

Ejecutar una sola vez al comienzo del proceso:

```bash
podman login <GITLAB_REGISTRY_URL> \
  --username <GITLAB_USER> \
  --password <GITLAB_TOKEN>
```

Resultado esperado:

```
Login Succeeded!
```

Si el login falla, verificar que el token tiene los scopes `read_registry` y `write_registry` y que la URL del registry es correcta.

## 4.2 Elasticsearch

```bash
# Descargar desde el registry público de Elastic
podman pull docker.elastic.co/elasticsearch/elasticsearch:8.19.15

# Re-taggear apuntando al registry privado de ONP
podman tag docker.elastic.co/elasticsearch/elasticsearch:8.19.15 \
  <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15

# Publicar en el registry privado
podman push <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15
```

Resultado esperado del `push`:

```
Writing manifest to image destination
Storing signatures
```

## 4.3 Kibana

```bash
podman pull docker.elastic.co/kibana/kibana:8.19.15

podman tag docker.elastic.co/kibana/kibana:8.19.15 \
  <GITLAB_REGISTRY_URL>/observabilidad/kibana:8.19.15

podman push <GITLAB_REGISTRY_URL>/observabilidad/kibana:8.19.15
```

## 4.4 OTEL Collector

```bash
podman pull otel/opentelemetry-collector-contrib:0.120.0

podman tag otel/opentelemetry-collector-contrib:0.120.0 \
  <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:0.120.0

podman push <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:0.120.0
```

## 4.5 Jaeger

```bash
podman pull jaegertracing/all-in-one:1.58.0

podman tag jaegertracing/all-in-one:1.58.0 \
  <GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0

podman push <GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0
```

## 4.6 Grafana

```bash
podman pull grafana/grafana:11.4.0

podman tag grafana/grafana:11.4.0 \
  <GITLAB_REGISTRY_URL>/observabilidad/grafana:11.4.0

podman push <GITLAB_REGISTRY_URL>/observabilidad/grafana:11.4.0
```

## 4.7 Prometheus

```bash
podman pull prom/prometheus:2.55.0

podman tag prom/prometheus:2.55.0 \
  <GITLAB_REGISTRY_URL>/observabilidad/prometheus:2.55.0

podman push <GITLAB_REGISTRY_URL>/observabilidad/prometheus:2.55.0
```

# 5. Verificación

## 5.1 Verificar las imágenes locales

Al terminar el proceso, verificar que todas las imágenes están presentes en la máquina de trabajo:

```bash
podman images | grep -E "elasticsearch|kibana|otel-collector|jaeger|grafana|prometheus"
```

Resultado esperado:

```
docker.elastic.co/elasticsearch/elasticsearch   8.19.15   ...   1.1 GB
docker.elastic.co/kibana/kibana                 8.19.15   ...   1.3 GB
otel/opentelemetry-collector-contrib            0.120.0   ...   310 MB
jaegertracing/all-in-one                        1.58.0    ...   95 MB
grafana/grafana                                 11.4.0    ...   430 MB
prom/prometheus                                 2.55.0    ...   280 MB
<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch   8.19.15   ...
<GITLAB_REGISTRY_URL>/observabilidad/kibana          8.19.15   ...
<GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib   0.120.0   ...
<GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one        1.58.0    ...
<GITLAB_REGISTRY_URL>/observabilidad/grafana                  11.4.0    ...
<GITLAB_REGISTRY_URL>/observabilidad/prometheus               2.55.0    ...
```

Deben aparecer tanto las imágenes con el tag original (origen) como las re-taggeadas (destino GitLab).

## 5.2 Verificar las imágenes en GitLab

Ingresar a GitLab y navegar a:

```
<URL_GITLAB> → Grupo de observabilidad → Packages & Registries → Container Registry
```

Verificar que aparecen los 6 repositorios con sus respectivas versiones:

| Repositorio en GitLab | Versión |
|-----------------------|---------|
| `observabilidad/elasticsearch` | 8.19.15 |
| `observabilidad/kibana` | 8.19.15 |
| `observabilidad/otel-collector-contrib` | 0.120.0 |
| `observabilidad/jaeger-all-in-one` | 1.58.0 |
| `observabilidad/grafana` | 11.4.0 |
| `observabilidad/prometheus` | 2.55.0 |

## 5.3 Verificar que los servidores pueden descargar las imágenes

Desde uno de los servidores QA o PROD (sin salida a internet), verificar que puede autenticarse y descargar del registry privado:

```bash
# En el servidor QA o PROD
podman login <GITLAB_REGISTRY_URL> \
  --username <GITLAB_USER> \
  --password <GITLAB_TOKEN>

# Probar descarga de una imagen (usar la más pequeña: Jaeger ~95 MB)
podman pull <GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0

# Verificar que descargó correctamente
podman images | grep jaeger
```

Si este paso falla, verificar con el equipo de infraestructura que los servidores tienen acceso de red al servidor de GitLab (no a internet, sino al servidor GitLab interno).

# 6. Consideraciones adicionales

## 6.1 Espacio en disco requerido en la máquina de trabajo

El proceso de mirroring descarga todas las imágenes a la máquina de trabajo antes de publicarlas. El espacio requerido es aproximado:

| Imagen | Tamaño aproximado |
|--------|-------------------|
| Elasticsearch 8.19.15 | ~1.1 GB |
| Kibana 8.19.15 | ~1.3 GB |
| OTEL Collector 0.120.0 | ~310 MB |
| Jaeger 1.58.0 | ~95 MB |
| Grafana 11.4.0 | ~430 MB |
| Prometheus 2.55.0 | ~280 MB |
| **Total** | **~3.5 GB** |

> **NOTA:** Podman comparte las capas de imagen entre tags. Las imágenes re-taggeadas para GitLab no ocupan espacio adicional en disco — comparten las mismas capas que las imágenes originales. El espacio total requerido es ~3.5 GB, no el doble.

## 6.2 Limpiar las imágenes locales tras el mirroring

Una vez verificado que todas las imágenes están en GitLab, se pueden eliminar las imágenes locales de la máquina de trabajo para liberar espacio:

```bash
# Eliminar imágenes con el tag de GitLab (los originals se eliminan automáticamente
# si no tienen otras referencias)
podman rmi <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:8.19.15
podman rmi <GITLAB_REGISTRY_URL>/observabilidad/kibana:8.19.15
podman rmi <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:0.120.0
podman rmi <GITLAB_REGISTRY_URL>/observabilidad/jaeger-all-in-one:1.58.0
podman rmi <GITLAB_REGISTRY_URL>/observabilidad/grafana:11.4.0
podman rmi <GITLAB_REGISTRY_URL>/observabilidad/prometheus:2.55.0

# Verificar que se liberó el espacio
podman images
```

## 6.3 Cuándo repetir este proceso

El mirroring debe repetirse cuando:

- Se actualiza la versión de algún componente del stack.
- Se agrega un nuevo componente al stack.

En ese caso, repetir únicamente los pasos de la Sección 4 correspondientes al componente actualizado. No es necesario volver a cargar las imágenes que no cambiaron.

# 7. Siguiente paso

Una vez verificado que las 6 imágenes están disponibles en el GitLab Registry, proceder con la instalación de los componentes en el orden recomendado:

| Orden | Manual | Motivo |
|-------|--------|--------|
| 1 | `Manual_Instalacion_Elasticsearch_ONP_VM_v0.1.0.md` | Base de almacenamiento — todo lo demás depende de él |
| 2 | `Manual_Instalacion_OtelCollector_ONP_VM_v0.1.0.md` | Receptor de telemetría de los servicios |
| 3 | `Manual_Instalacion_Jaeger_ONP_VM_v0.1.0.md` | Requiere ES operativo |
| 4 | `Manual_Instalacion_Kibana_ONP_VM_v0.1.0.md` | Requiere ES operativo |
| 5 | `Manual_Instalacion_Prometheus_ONP_VM_v0.1.0.md` | Independiente, pero conviene tener el Collector activo |
| 6 | `Manual_Instalacion_Grafana_ONP_VM_v0.1.0.md` | Requiere Prometheus y Jaeger operativos para configurar datasources |
