**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Kibana en Servidor Virtual (RHEL 8)**

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

Este manual describe el proceso completo de instalación de Kibana en servidores virtuales RHEL 8 on-premise, usando Podman como runtime de contenedores y Quadlet para la integración con systemd.

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

| Entorno | Servidor | Otros servicios en el mismo servidor |
|---------|----------|--------------------------------------|
| DEV | Servidor 1 | Stack completo |
| QA | Servidor 2 | Collector standby + ES |
| PROD | Servidor 4 | ES node 2 |

> **NOTA PROD:** Kibana comparte el Servidor 4 con el nodo ES node 2. Con 8 GB de RAM y un heap ES de 3 GB, quedan aproximadamente 4 GB disponibles para OS + Kibana. Kibana consume típicamente entre 500 MB y 1 GB de RAM, por lo que la coexistencia es viable.

## 1.5 Versión de Kibana

Kibana **debe tener la misma versión principal que Elasticsearch**. Si Elasticsearch es `8.19.15`, Kibana debe ser `8.19.15`. Versiones incompatibles producen errores al conectarse.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall).
- Despliegue de Kibana vía Podman + Quadlet.
- Configuración inicial de Kibana (Data View para logs).
- Verificación del despliegue.
- Troubleshooting de errores comunes.

Queda fuera del alcance:

- Instalación de Elasticsearch (ver Manual de Instalación Elasticsearch VM).
- Creación de dashboards avanzados.
- Configuración de autenticación multi-usuario en Kibana.

# 2. Prerrequisitos

> **⚠️ ADVERTENCIA:** Elasticsearch debe estar instalado, operativo y con el usuario `elastic` funcionando antes de instalar Kibana. Kibana se conecta a Elasticsearch al arrancar para verificar compatibilidad de versiones.

## 2.1 Infraestructura requerida

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 2 | 4 | 8 GB | 50 GB |
| PROD | Servidor 4 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos

| Componente | Verificación |
|------------|--------------|
| Elasticsearch | `curl -u elastic:<PASSWORD> http://<ES_HOST>:9200/` → devuelve versión `8.19.15` |

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
| \<KIBANA_VERSION\> | Versión de Kibana (debe coincidir con ES) | 8.19.15 |
| \<ES_HOST\> | IP o hostname del servidor Elasticsearch | |
| \<ES_PORT\> | Puerto de Elasticsearch | 9200 |
| \<ELASTIC_PASSWORD\> | Contraseña del usuario `elastic` de ES | |
| \<KIBANA_ENCRYPTION_KEY\> | Clave de cifrado para sesiones Kibana (32+ chars) | |

### Cómo generar la clave de cifrado de Kibana

Kibana requiere una clave de cifrado aleatoria de al menos 32 caracteres para proteger las sesiones y los datos almacenados en ES. Generar una:

```bash
openssl rand -hex 32
```

El resultado es una cadena de 64 caracteres hexadecimales. Usar esa cadena como valor de `<KIBANA_ENCRYPTION_KEY>`.

## 2.5 Verificaciones previas

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://<ES_HOST>:<ES_PORT>/
```

Resultado esperado:

```json
{
  "name": "es-node-dev",
  "cluster_name": "onp-es-dev",
  "version": { "number": "8.19.15" },
  "tagline": "You Know, for Search"
}
```

La versión debe ser `8.19.15`. Si es diferente, usar la misma versión en Kibana.

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
| Kibana | docker.elastic.co/kibana/kibana | 8.19.15 |

> **NOTA:** La versión de Kibana debe coincidir exactamente con la versión de Elasticsearch instalada.

## 3.2 Escenario A — GitLab Registry (QA y PROD)

```bash
podman login <GITLAB_REGISTRY_URL>

podman pull docker.elastic.co/kibana/kibana:<KIBANA_VERSION>

podman tag docker.elastic.co/kibana/kibana:<KIBANA_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/kibana:<KIBANA_VERSION>

podman push <GITLAB_REGISTRY_URL>/observabilidad/kibana:<KIBANA_VERSION>
```

## 3.3 Escenario B — Internet directo (DEV)

No se requieren pasos de mirroring. Usar directamente:

```
docker.elastic.co/kibana/kibana:<KIBANA_VERSION>
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
sudo mkdir -p /etc/observabilidad/kibana
```

## 4.3 Configurar el firewall

| Puerto | Protocolo | Descripción |
|--------|-----------|-------------|
| 5601 | TCP | UI web de Kibana y API REST |

```bash
sudo firewall-cmd --permanent --add-port=5601/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports
```

# 5. Configuración de Kibana

## 5.1 Crear el archivo de configuración

```bash
sudo tee /etc/observabilidad/kibana/kibana.yml > /dev/null <<EOF
server.host: "0.0.0.0"
server.port: 5601
server.name: "kibana-onp-<ENV>"

elasticsearch.hosts: ["http://<ES_HOST>:<ES_PORT>"]
elasticsearch.username: "elastic"
elasticsearch.password: "<ELASTIC_PASSWORD>"

xpack.encryptedSavedObjects.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.security.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"
xpack.reporting.encryptionKey: "<KIBANA_ENCRYPTION_KEY>"

monitoring.ui.container.elasticsearch.enabled: false

i18n.locale: "en"
EOF
```

> **NOTA sobre las claves de cifrado:** Las tres claves (`encryptedSavedObjects`, `security`, `reporting`) pueden usar el mismo valor generado en la Sección 2.4. Kibana no verifica que sean distintas; usar el mismo valor es válido para este despliegue.

## 5.2 Asegurar los permisos del archivo

```bash
sudo chmod 640 /etc/observabilidad/kibana/kibana.yml
sudo chown root:root /etc/observabilidad/kibana/kibana.yml
ls -la /etc/observabilidad/kibana/kibana.yml
```

Resultado esperado:

```
-rw-r----- 1 root root ... kibana.yml
```

# 6. Despliegue con Quadlet

## 6.1 Crear el archivo Quadlet

```bash
sudo tee /etc/containers/systemd/kibana.container > /dev/null <<EOF
[Unit]
Description=Kibana - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Container]
Image=<KIBANA_IMAGE>
ContainerName=kibana
Volume=/etc/observabilidad/kibana/kibana.yml:/usr/share/kibana/config/kibana.yml:ro,Z
PublishPort=5601:5601

[Service]
Restart=always
RestartSec=15
TimeoutStartSec=180

[Install]
WantedBy=multi-user.target
EOF
```

> **NOTA:** Kibana tarda más en iniciar que otros componentes (entre 60 y 120 segundos). Por eso `TimeoutStartSec` es 180. Es normal que el servicio aparezca como `activating` durante ese tiempo.

## 6.2 Recargar systemd e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable kibana
sudo systemctl start kibana
```

## 6.3 Verificar que el servicio está activo

```bash
sudo systemctl status kibana
```

Resultado esperado después de 90-120 segundos:

```
● kibana.service - Kibana - Stack de Observabilidad ONP
     Active: active (running) since ...
```

# 7. Verificación del despliegue

## 7.1 Verificar los logs de Kibana

```bash
sudo journalctl -u kibana -n 50
```

Al iniciar correctamente, Kibana muestra líneas como:

```
{"message":"[savedobjects-service] INIT complete","type":"log","@timestamp":"...","tags":["info","savedobjects-service"]}
{"message":"http server running at http://0.0.0.0:5601","type":"log","@timestamp":"...","tags":["info","http","server"]}
```

La línea **"http server running at http://0.0.0.0:5601"** confirma que Kibana está listo para recibir conexiones.

## 7.2 Verificar el health check de Kibana

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/status
```

Resultado esperado (fragmento):

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

El campo `"level": "available"` confirma que Kibana está completamente operativo.

## 7.3 Acceder a la UI de Kibana

Abrir el navegador en:

```
http://<IP_SERVIDOR_KIBANA>:5601
```

Ingresar con:
- **Usuario:** `elastic`
- **Contraseña:** `<ELASTIC_PASSWORD>`

La UI de Kibana debe cargar correctamente.

## 7.4 Crear el Data View para logs de ONP

Un Data View (anteriormente llamado Index Pattern) le indica a Kibana qué índices de Elasticsearch mostrar en la sección Discover.

1. En la UI de Kibana, ir a **Stack Management** → **Data Views** → **Create data view**.
2. Completar el formulario:
   - **Name:** `ONP Logs - <ENTORNO>` (ej. `ONP Logs - Development`)
   - **Index pattern:** `onp-logs-*`
   - **Timestamp field:** `@timestamp`
3. Hacer clic en **Save data view to Kibana**.

Una vez creado, ir a **Discover** y seleccionar el Data View recién creado. Los logs enviados por el OTEL Collector aparecerán en la línea de tiempo.

## 7.5 Actualizar los campos del Data View

Cuando los servicios incorporan nuevos campos en sus logs (por ejemplo, al agregar el `CanonicalRequestLogFilter` de la Guía de Desarrollo), Kibana no los muestra automáticamente en Discover hasta que el Data View se refresca.

**Cuándo hacer este paso:** la primera vez que se despliegue un servicio que emita el log canónico de request, o cualquier vez que aparezcan campos nuevos que Kibana no reconoce.

En la UI de Kibana navegar a: **Stack Management → Kibana → Data Views → ONP Logs - \<ENTORNO\>**

Hacer clic en el botón **Refresh field list** (ícono de recarga arriba a la derecha).

Los campos nuevos (`duration_ms`, `http.response.status_code`, `user.id`, etc.) quedarán disponibles en Discover, en los filtros y en los dashboards.

**Alternativa via API:**

```bash
# Obtener el ID del Data View
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/data_views \
  -H "kbn-xsrf: true" | grep '"id"'

# Refrescar los campos del Data View
curl -X POST -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/data_views/data_view/<DATA_VIEW_ID>/fields \
  -H "kbn-xsrf: true"
```

Resultado esperado: HTTP 200.

# 8. Troubleshooting

## 8.1 Kibana no arranca — error de conexión a Elasticsearch

### Síntoma

```bash
sudo journalctl -u kibana -n 30
# {"message":"Unable to retrieve version information from Elasticsearch nodes. ..."}
```

### Causa y solución

Kibana no puede alcanzar Elasticsearch. Verificar:

```bash
# Verificar la URL de ES en la configuración
cat /etc/observabilidad/kibana/kibana.yml | grep elasticsearch.hosts

# Probar la conectividad directamente
curl -u elastic:<ELASTIC_PASSWORD> http://<ES_HOST>:<ES_PORT>/
```

Si ES responde correctamente pero Kibana sigue fallando, verificar que no hay un firewall entre el servidor de Kibana y el de ES que bloquee el puerto 9200.

## 8.2 Kibana muestra error de versión incompatible

### Síntoma

```bash
sudo journalctl -u kibana -n 30
# "Kibana is not compatible with the current version of Elasticsearch"
```

### Causa y solución

La versión de Kibana y de Elasticsearch no coinciden. Verificar:

```bash
# Versión de Elasticsearch
curl -u elastic:<ELASTIC_PASSWORD> http://<ES_HOST>:<ES_PORT>/ | grep number

# Versión de Kibana en la imagen
sudo podman inspect kibana --format '{{.Config.Labels}}'
```

Ambas deben ser `8.19.15`. Si no coinciden, actualizar la imagen de Kibana a la versión correcta.

## 8.3 La UI no carga en el navegador

### Síntoma

El navegador no puede conectarse a `http://<IP_SERVIDOR>:5601`.

### Causas y soluciones

- Verificar que el servicio está corriendo: `sudo systemctl status kibana`
- Verificar que el firewall permite el puerto 5601: `sudo firewall-cmd --list-ports`
- Verificar que el puerto está escuchando: `sudo ss -tlnp | grep 5601`
- Kibana puede seguir iniciando (tarda hasta 2 minutos). Esperar y reintentar.

## 8.4 Comandos útiles de diagnóstico rápido

| Diagnóstico | Comando |
|-------------|---------|
| Estado del servicio | `sudo systemctl status kibana` |
| Logs en tiempo real | `sudo journalctl -u kibana -f` |
| Health check API | `curl -u elastic:<PWD> http://localhost:5601/api/status` |
| Reiniciar Kibana | `sudo systemctl restart kibana` |

# Anexo A — Configuración específica para DEV

| Placeholder | Valor DEV |
|-------------|-----------|
| \<KIBANA_IMAGE\> | `docker.elastic.co/kibana/kibana:8.19.15` (Escenario B) |
| \<ES_HOST\> | `localhost` o IP del Servidor 1 |
| \<ES_PORT\> | `9200` |
| \<ENV\> (server.name) | `dev` |

# Anexo B — Configuración específica para QA

| Placeholder | Valor QA |
|-------------|----------|
| \<KIBANA_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/kibana:8.19.15` |
| \<ES_HOST\> | IP del Servidor 2 (ES corre en el mismo Servidor 2) |
| \<ES_PORT\> | `9200` |
| \<ENV\> (server.name) | `qa` |

# Anexo C — Configuración específica para PROD

| Placeholder | Valor PROD |
|-------------|------------|
| \<KIBANA_IMAGE\> | `<GITLAB_REGISTRY_URL>/observabilidad/kibana:8.19.15` |
| \<ES_HOST\> | IP del Servidor 4 (ES node 2 corre en el mismo Servidor 4) |
| \<ES_PORT\> | `9200` |
| \<ENV\> (server.name) | `prod` |

# Anexo D — Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen

```bash
podman pull docker.elastic.co/kibana/kibana:<KIBANA_VERSION>
podman save docker.elastic.co/kibana/kibana:<KIBANA_VERSION> \
  -o kibana-<KIBANA_VERSION>.tar
```

## D.2 Transferir e importar

```bash
scp kibana-<KIBANA_VERSION>.tar <USUARIO>@<IP_SERVIDOR>:/tmp/
sudo podman load -i /tmp/kibana-<KIBANA_VERSION>.tar
sudo podman images | grep kibana
```

## D.3 Usar en el archivo Quadlet

```ini
Image=docker.elastic.co/kibana/kibana:<KIBANA_VERSION>
```
