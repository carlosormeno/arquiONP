**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Jaeger en Servidor Virtual (RHEL 8)**

Instalación nativa sobre Red Hat Enterprise Linux 8

| **Versión:**        | 0.2.1      |
|---------------------|------------|
| **Fecha:**          | 2026-07-03 |
| **Clasificación:**  | Uso Interno (Técnico) |
| **Área responsable:** | OTI      |

# Historial de versiones

| **Versión** | **Fecha**  | **Autor**   | **Descripción**              |
|-------------|------------|-------------|------------------------------|
| 0.2.1       | 2026-07-03 | \<AUTOR\>   | Actualización de pasos para PROD |
| 0.2.0       | 2026-06-26 | \<AUTOR\>   | Reescritura para instalación nativa vía binario, sin runtime de contenedores |
| 0.1.0       | 2026-05-20 | \<AUTOR\>   | Versión inicial del manual   |

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Jaeger en servidores virtuales RHEL 8 on-premise, usando el binario oficial `jaeger-all-in-one` y una unidad systemd personalizada.

El documento está dirigido al equipo de Plataforma de la OTI — ONP y cubre los entornos DEV, QA y PROD.
Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

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

| Entorno | Nodos Jaeger | Tipo de cluster | Servidores |
|---------|----------|-----------------|--------------|
| DEV     | 1        | Single-node     | Servidor 1 |
| QA      | 1        | Single-node     | Servidor 1 |
| PROD    | 1        | Single-node     | Servidor 5 |

## 1.5 Jaeger All-in-One

Este manual usa el binario `jaeger-all-in-one`, que integra todos los componentes de Jaeger en un único proceso:

- **Collector**: recibe trazas del OTEL Collector vía OTLP gRPC (puerto 4317) o Thrift (puerto 14268).
- **Query**: expone la API REST y la UI web (puerto 16686).
- **Ingester**: procesa las trazas y las escribe en el backend de almacenamiento (Elasticsearch).

Esta configuración es adecuada para entornos de baja y media carga.

## 1.6 Alcance de este manual

Este manual cubre:

- Preparación del servidor (firewall, directorios).
- Instalación del binario Jaeger y creación de la unidad systemd.
- Configuración del backend Elasticsearch.
- Verificación del despliegue y del flujo de trazas.

Queda fuera del alcance:

- Despliegue de Jaeger en modo distribuido (componentes separados).

# 2. Prerrequisitos

Elasticsearch debe estar instalado y operativo antes de instalar Jaeger.
Jaeger necesita conectarse a Elasticsearch para crear sus índices al arrancar, por ello debe existir conectividad desde el servidor donde se instalará Jaeger hacia el servidor donde se encuentra instalado Elasticsearch (por el puerto `<ES_PORT>`).

## 2.1 Infraestructura requerida

| Entorno | Servidor | vCPU | RAM | /data |
|---------|----------|------|-----|-------|
| DEV | Servidor 1 | 4 | 8 GB | 50 GB |
| QA | Servidor 1 | 4 | 8 GB | 50 GB |
| PROD | Servidor 5 | 4 | 8 GB | 100 GB |

## 2.2 Componentes que deben estar operativos

| Componente |
|------------|
| Elasticsearch |

## 2.3 Software requerido

| Herramienta | Versión mínima | Verificación |
|-------------|----------------|--------------|
| RHEL | 8.8 o superior | `cat /etc/redhat-release` |
| curl | Cualquier versión | `curl --version` |
| tar | Cualquier versión | `tar --version` |

## 2.4 Información a recopilar antes de comenzar

| Placeholder | Descripción | Valor real (completar) |
|-------------|-------------|------------------------|
| \<JAEGER_VERSION\> | Versión de Jaeger | 1.58.0 |
| \<ES_HOST\> | IP o hostname del servidor Elasticsearch para DEV o QA | |
| \<ES_PORT\> | Puerto de Elasticsearch | 9200|
| \<ES_NODE1\> | IP o hostname del servidor Elasticsearch para PROD - Nodo 1 | |
| \<ES_NODE2\> | IP o hostname del servidor Elasticsearch para PROD - Nodo 2 | |
| \<ES_NODE3\> | IP o hostname del servidor Elasticsearch para PROD - Nodo 3 | |
| \<ELASTIC_PASSWORD\> | Contraseña del usuario `elastic` de Elasticsearch (ES) | |

## 2.5 Verificaciones previas

### 2.5.1 Verificar Elasticsearch

Para **DEV** y **QA** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Para **PROD** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_NODE1>:<ES_PORT>/_cluster/health
```

Resultado esperado: `{"status":"green",...}` o `{"status":"yellow",...}`.
Si el resultado es `red` o no hay respuesta, resolver el problema en Elasticsearch antes de continuar.

# 3. Preparación del servidor

## 3.1 Crear el usuario del sistema

```bash
sudo useradd --no-create-home --shell /bin/false --system jaeger

# Verificar la creación del usuario
id jaeger
```

## 3.2 Crear la estructura de directorios

```bash
sudo mkdir -p /etc/observabilidad/jaeger
```

## 3.3 Configurar el firewall

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

> **NOTA:** La apertura de puertos mediante firewalld únicamente habilita el acceso a nivel del sistema operativo RHEL. Si el servidor se ejecuta dentro de una máquina virtual, deberá verificarse además la configuración de red del hipervisor (VMware, VirtualBox, Hyper-V, KVM u otro) y de los dispositivos de red intermedios para garantizar que los puertos publicados sean accesibles desde los sistemas que requieran conectarse al servicio.

# 4. Instalación del binario

## 4.1 Extraer el archivo tar.gz

```bash
tar xzf /tmp/onp-packages/bin/jaeger-1.58.0-linux-amd64.tar.gz -C /tmp/
```

## 4.2 Instalar el binario

```bash
# Copiar el binario al directorio del sistema
sudo cp /tmp/jaeger-1.58.0-linux-amd64/jaeger-all-in-one /usr/local/bin/jaeger-all-in-one

# Asignar propietario y permisos
sudo chown jaeger:jaeger /usr/local/bin/jaeger-all-in-one
sudo chmod 755 /usr/local/bin/jaeger-all-in-one

# Verificar la instalación
/usr/local/bin/jaeger-all-in-one version
```

Resultado esperado:

```
2026/06/25 08:47:56 maxprocs: Leaving GOMAXPROCS=4: CPU quota undefined
2026/06/25 08:47:56 application version: git-commit=03136eb972baaf9c09026d0a67f4f2a1d7e84568, git-version=v1.58.0, build-date=2024-06-11T18:52:25Z
{"gitCommit":"03136eb972baaf9c09026d0a67f4f2a1d7e84568","gitVersion":"v1.58.0","buildDate":"2024-06-11T18:52:25Z"}
```

## 4.3 Limpiar archivos temporales

```bash
rm -rf /tmp/jaeger-1.58.0-linux-amd64/
```

# 5. Creación del rol y usuario de Elasticsearch para Jaeger

## 5.1 Crear el rol de Jaeger

El rol otorga permisos para:
- Consultar información básica del cluster.
- Crear índices de logs cuando sea necesario.
- Escribir documentos en los índices de logs.
- Consultar metadatos de los índices.

```bash
#Crear el archivo temporal de definición del rol
cat > /tmp/jaeger_role.json <<'EOF'
{
  "cluster": [
    "monitor",
    "manage_index_templates"
  ],
  "indices": [
    {
      "names": [
        "onp-jaeger-*"
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
cat /tmp/jaeger_role.json
```

## 5.2 Registrar el rol en Elasticsearch
Para **DEV** y **QA** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://<ES_HOST>:<ES_PORT>/_security/role/jaeger_role" \
  -H "Content-Type: application/json" \
  -d @/tmp/jaeger_role.json
```

Para **PROD** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://<ES_NODE1>:<ES_PORT>/_security/role/jaeger_role" \
  -H "Content-Type: application/json" \
  -d @/tmp/jaeger_role.json
```

Resultado esperado:

```
{"role":{"created":true}}
```

## 5.3 Verificar el rol creado

Para **DEV** y **QA** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_security/role/jaeger_role?pretty
```

Para **PROD** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_NODE1>:<ES_PORT>/_security/role/jaeger_role?pretty
```

Resultado esperado:

```
{
  "jaeger_role" : {
    "cluster" : [
      "monitor",
      "manage_index_templates"
    ],
    "indices" : [
      {
        "names" : [
          "onp-jaeger-*"
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

## 5.4 Crear el usuario de Jaeger

Ejecutar el siguiente comando, reemplazando <JAEGER_ES_PASSWORD> por la contraseña que se asignará al usuario de Jaeger para conectarse a ElasticSearch. Anotar y guardar la contraseña porque podría ser necesaria posteriormente.

```bash
cat > /tmp/jaeger_user.json <<'EOF'
{
  "password" : "<JAEGER_ES_PASSWORD>",
  "roles" : [
    "jaeger_role"
  ],
  "enabled": true,
  "full_name" : "Jaeger"
}
EOF
```

## 5.5 Registrar el usuario en Elasticsearch
El procedimiento creará el siguiente usuario de Elasticsearch, el cual será utilizado por el Jaeger para autenticarse contra Elasticsearch:

| Parámetro | Valor |
|------------|--------|
| Usuario | `jaeger` |
| Rol asignado | `jaeger_role` |

Para **DEV** y **QA** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X POST "http://<ES_HOST>:<ES_PORT>/_security/user/jaeger" \
  -H "Content-Type: application/json" \
  -d @/tmp/jaeger_user.json
```

Para **PROD** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  -X POST "http://<ES_NODE1>:<ES_PORT>/_security/user/jaeger" \
  -H "Content-Type: application/json" \
  -d @/tmp/jaeger_user.json
```

Resultado esperado:

```
{"created":true}
```

## 5.6 Verificar el usuario creado

Para **DEV** y **QA** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_security/user/jaeger?pretty
```

Para **PROD** ejecutar el siguiente comando, reemplazando `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://<ES_NODE1>:<ES_PORT>/_security/user/jaeger?pretty
```

Resultado esperado:

```
{
  "jaeger" : {
    "username" : "jaeger",
    "roles" : [
      "jaeger_role"
    ],
    "full_name" : "Jaeger",
    "email" : null,
    "metadata" : { },
    "enabled" : true
  }
}
```

## 5.7 Verificar la autenticación del usuario

Para **DEV** y **QA** ejecutar el siguiente comando, reemplazando `<JAEGER_ES_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u jaeger:<JAEGER_ES_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>
```

Para **PROD** ejecutar el siguiente comando, reemplazando `<JAEGER_ES_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u jaeger:<JAEGER_ES_PASSWORD> \
  http://<ES_NODE1>:<ES_PORT>
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
rm -f /tmp/jaeger_role.json
rm -f /tmp/jaeger_user.json
```

# 6. Creación del archivo de credenciales

Jaeger necesita las credenciales de Elasticsearch para almacenar las trazas.

Para **DEV** y **QA** ejecutar el siguiente comando, donde debe reemplazar `<ES_HOST>`, `<ES_PORT>` y `<JAEGER_ES_PASSWORD>`

```bash
sudo tee /etc/observabilidad/jaeger/jaeger.env > /dev/null <<EOF
SPAN_STORAGE_TYPE=elasticsearch
ES_SERVER_URLS=http://<ES_HOST>:<ES_PORT>
ES_USERNAME=jaeger
ES_PASSWORD=<JAEGER_ES_PASSWORD>
ES_INDEX_PREFIX=onp
JAEGER_ES_CREATE_INDEX_TEMPLATES=true
EOF
```

Para **PROD** ejecutar el siguiente comando, donde debe reemplazar `<ES_NODE1>`, `<ES_NODE2>`, `<ES_NODE3>`, `<ES_PORT>` y `<JAEGER_ES_PASSWORD>`.

```bash
sudo tee /etc/observabilidad/jaeger/jaeger.env > /dev/null <<EOF
SPAN_STORAGE_TYPE=elasticsearch
ES_SERVER_URLS=http://<ES_NODE1>:<ES_PORT>,http://<ES_NODE2>:<ES_PORT>,http://<ES_NODE3>:<ES_PORT>
ES_USERNAME=jaeger
ES_PASSWORD=<JAEGER_ES_PASSWORD>
ES_INDEX_PREFIX=onp
JAEGER_ES_CREATE_INDEX_TEMPLATES=true
EOF
```

Ejecute los siguientes comandos para configurar los permisos del archivo.
```bash
sudo chmod 600 /etc/observabilidad/jaeger/jaeger.env
sudo chown root:root /etc/observabilidad/jaeger/jaeger.env
ls -la /etc/observabilidad/jaeger/jaeger.env
```

Resultado esperado:

```
-rw------- 1 root root ... jaeger.env
```

# 7. Creación de la unidad systemd

## 7.1 Crear el archivo de servicio

```bash
sudo tee /etc/systemd/system/jaeger.service > /dev/null <<EOF
[Unit]
Description=Jaeger - Stack de Observabilidad ONP
After=network-online.target local-fs.target
Wants=network-online.target

[Service]
Type=simple
User=jaeger
Group=jaeger
EnvironmentFile=/etc/observabilidad/jaeger/jaeger.env
ExecStart=/usr/local/bin/jaeger-all-in-one
Restart=always
RestartSec=10
TimeoutStartSec=60

[Install]
WantedBy=multi-user.target
EOF
```

> **NOTA:** Jaeger lee sus parámetros de configuración desde las variables de entorno del `EnvironmentFile`. La URL de Elasticsearch, credenciales y prefijo de índice se inyectan automáticamente desde `/etc/observabilidad/jaeger/jaeger.env`.


# 8. Arranque del servicio

## 8.1 Habilitar e iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl enable jaeger
sudo systemctl start jaeger
```

## 8.2 Verificar que el servicio está activo
Esperar unos 5 segundos y luego ejecutar el siguiente comando.

```bash
sudo systemctl status jaeger
```

Resultado esperado:

```
● jaeger.service - Jaeger - Stack de Observabilidad ONP
     Loaded: loaded (/etc/systemd/system/jaeger.service; enabled)
     Active: active (running) since ...
```

## 8.3. Verificación del despliegue

```bash
curl http://localhost:14269/
```

Resultado esperado: {"status":"Server available","upSince":"2026-06-23T14:42:32.577456948-05:00","uptime":"2m37.287004718s"}
El valor de "status" igual a "Server available" es lo importante.


# 9. Crear los templates
Los templates para Elasticsearch se crearán tomando como base los templates generados de forma predeterminada por Jaeger.

## 9.1 Detener el servicio

```bash
sudo systemctl stop jaeger
```

## 9.2 Descargar las plantillas base de Elasticsearch

Para **DEV** y **QA** ejecutar los siguientes comandos, donde debe reemplazar `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> -X GET "http://<ES_HOST>:<ES_PORT>/_index_template/onp-jaeger-service?pretty" > service_base.json
curl -u elastic:<ELASTIC_PASSWORD> -X GET "http://<ES_HOST>:<ES_PORT>/_index_template/onp-jaeger-span?pretty" > span_base.json
```

Para **PROD** ejecutar los siguientes comandos, donde debe reemplazar `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> -X GET "http://<ES_NODE1>:<ES_PORT>/_index_template/onp-jaeger-service?pretty" > service_base.json
curl -u elastic:<ELASTIC_PASSWORD> -X GET "http://<ES_NODE1>:<ES_PORT>/_index_template/onp-jaeger-span?pretty" > span_base.json
```

## 9.3 Modificar el contenido de la plantilla de servicios
Los siguentes comandos permiten modificar el contenido de las plantillas base sin necesidad de edición manual.

```bash
# Remover envolturas iniciales y finales del GET para dejar un JSON plano y válido
sed -i '2,5d' service_base.json
sed -i '$d' service_base.json
sed -i '$d' service_base.json
sed -i '$d' service_base.json
```

```bash
# Inyectar Shards, Política ILM y Codec Institucional dentro de la sección "settings"
sed -i 's/"number_of_shards" : "5"/"number_of_shards" : "1",\n            "lifecycle.name": "onp-12months-policy",\n            "codec": "best_compression"/g' service_base.json
```

Antes de ejecutar el siguiente comando reemplace el valor de `<REPLICAS>` (debe ser `0` para DEV y QA y `1` para PROD).

```bash
# Inyectar la cantidad de replicas
sed -i 's/"number_of_replicas" : "1"/"number_of_replicas" : "<REPLICAS>"/g' service_base.json
```

```bash
# Elevar la prioridad a 1000 para forzar a Elasticsearch a ignorar la plantilla nativa de Jaeger
sed -i 's/"priority" : 0/"priority" : 1000/g' service_base.json
```

Comprobar el archivo modificado.
```bash
cat service_base.json
```

Resultado esperado: 

El contenido del archivo debe iniciar con: ```{ "index_patterns" : ["``` y debe terminar con ```"priority" : 1000}```


## 9.4 Modificar el contenido de la plantilla de spans
Los siguentes comandos permiten modificar el contenido de las plantillas base sin necesidad de edición manual.

```bash
# Remover envolturas iniciales y finales del GET para dejar un JSON plano y válido
sed -i '2,5d' span_base.json
sed -i '$d' span_base.json
sed -i '$d' span_base.json
sed -i '$d' span_base.json
```

```bash
# Inyectar Shards, Política ILM y Codec Institucional dentro de la sección "settings"
sed -i 's/"number_of_shards" : "5"/"number_of_shards" : "1",\n            "lifecycle.name": "onp-12months-policy",\n            "codec": "best_compression"/g' span_base.json
```

Antes de ejecutar el siguiente comando reemplace el valor de `<REPLICAS>` (debe ser `0` para DEV y QA y `1` para PROD).

```bash
# Inyectar la cantidad de replicas
sed -i 's/"number_of_replicas" : "1"/"number_of_replicas" : "<REPLICAS>"/g' span_base.json
```

```bash
# Elevar la prioridad a 1000 para forzar a Elasticsearch a ignorar la plantilla nativa de Jaeger
sed -i 's/"priority" : 0/"priority" : 1000/g' span_base.json
```

Comprobar el archivo modificado.
```bash
cat span_base.json
```

Resultado esperado: 

El contenido del archivo debe iniciar con: ```{ "index_patterns" : ["``` y debe terminar con ```"priority" : 1000}```

## 9.5 Crear los index template

Para **DEV** y **QA** ejecutar los siguientes comandos, donde debe reemplazar `<ELASTIC_PASSWORD>`, `<ES_HOST>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> -X PUT "http://<ES_HOST>:<ES_PORT>/_index_template/onp-jaeger-service-politica" -H "Content-Type: application/json" -d @service_base.json

curl -u elastic:<ELASTIC_PASSWORD> -X PUT "http://<ES_HOST>:<ES_PORT>/_index_template/onp-jaeger-span-politica" -H "Content-Type: application/json" -d @span_base.json
```

Para **PROD** ejecutar los siguientes comandos, donde debe reemplazar `<ELASTIC_PASSWORD>`, `<ES_NODE1>` y `<ES_PORT>`.

```bash
curl -u elastic:<ELASTIC_PASSWORD> -X PUT "http://<ES_NODE1>:<ES_PORT>/_index_template/onp-jaeger-service-politica" -H "Content-Type: application/json" -d @service_base.json

curl -u elastic:<ELASTIC_PASSWORD> -X PUT "http://<ES_NODE1>:<ES_PORT>/_index_template/onp-jaeger-span-politica" -H "Content-Type: application/json" -d @span_base.json
```


Resultado esperado: `{"acknowledged":true}`

## 9.6 Borrar los archivos temporales de las plantillas

```bash
rm service_base.json
rm span_base.json
```


# 10. Arranque del servicio

## 10.1 Volver a iniciar el servicio

```bash
sudo systemctl daemon-reload
sudo systemctl restart jaeger
```

## 10.2 Verificar que el servicio está activo
Esperar unos 5 segundos y luego ejecutar el siguiente comando.

```bash
sudo systemctl status jaeger
```

Resultado esperado:

```
● jaeger.service - Jaeger - Stack de Observabilidad ONP
     Loaded: loaded (/etc/systemd/system/jaeger.service; enabled)
     Active: active (running) since ...
```

El texto "Active: active (running)" es lo que se debe comprobar.

## 10.3. Verificación del despliegue

```bash
curl http://localhost:14269/
```

Resultado esperado: {"status":"Server available","upSince":"2026-06-23T14:42:32.577456948-05:00","uptime":"2m37.287004718s"}
El valor de "status" igual a "Server available" es lo importante.


# 11 Acceder a la UI de Jaeger

Abrir el navegador en:

```
http://<IP_SERVIDOR_JAEGER>:16686
```

La UI de Jaeger debe cargar correctamente, sin mensajes de error.
En este punto termina la instalación.

