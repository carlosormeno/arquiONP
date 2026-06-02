**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**OpenTelemetry Collector en Kubernetes (K3s)**

Usando manifests Kubernetes con opentelemetry-collector-contrib

  -----------------------------------------------------------------------
  **Versión:**                 0.1.0
  ---------------------------- ------------------------------------------
  **Fecha:**                   2026

  **Clasificación:**           Uso Interno (Técnico)

  **Área responsable:**        OTI
  -----------------------------------------------------------------------

# Historial de versiones

  -----------------------------------------------------------------------------
  **Versión**   **Fecha**    **Autor**          **Descripción**
  ------------- ------------ ------------------ -------------------------------
  0.1.0         2026         \<AUTOR\>          Versión inicial del manual

  -----------------------------------------------------------------------------

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación del OpenTelemetry Collector en un cluster Kubernetes K3s on-premise, utilizando manifests Kubernetes directos (Deployment, ConfigMap y Service) y la distribución `opentelemetry-collector-contrib`.

El documento está dirigido al equipo de Plataforma de la OTI --- ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes a todos los entornos están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es el OpenTelemetry Collector?

El OpenTelemetry Collector es un componente intermediario que recibe, procesa y exporta telemetría (trazas, métricas y logs). Actúa como hub central del stack de observabilidad: los servicios de backend envían su telemetría al Collector, y este la enruta a los backends de almacenamiento correspondientes.

Ventajas de usar un Collector frente a exportar directamente desde los servicios:

-   Los servicios quedan desacoplados de los backends. Si se cambia Jaeger por otro sistema, solo cambia la configuración del Collector, no el código de cada servicio.

-   El Collector aplica procesamiento centralizado: agrupamiento por lotes (batching), límites de memoria y enriquecimiento de atributos.

-   Un único punto de configuración para controlar el flujo de telemetría de todos los servicios.

## 1.3 ¿Qué es la distribución contrib?

El Collector existe en dos distribuciones:

  -----------------------------------------------------------------------
  **Distribución**                      **Contenido**
  ------------------------------------- ---------------------------------
  opentelemetry-collector               Solo componentes core oficiales.
                                        No incluye exporters para
                                        Jaeger, Elasticsearch ni
                                        Prometheus avanzado.

  opentelemetry-collector-contrib       Core + componentes de la
                                        comunidad y vendors. Incluye
                                        todos los exporters necesarios
                                        para el stack ONP.
  -----------------------------------------------------------------------

ONP utiliza la distribución **contrib** porque los exporters requeridos (Jaeger vía OTLP, Elasticsearch, Prometheus) solo están disponibles en ella.

## 1.4 Rol del Collector en el stack ONP

El Collector es el primer componente que recibe telemetría y el punto desde el cual se distribuye a los tres backends del stack:

```
Servicios Spring Boot (OTLP HTTP :4318 / gRPC :4317)
                        ↓
              OpenTelemetry Collector
              (namespace: otel-dev/qa/otel)
            ↙           ↓            ↘
    Trazas (OTLP)   Métricas      Logs (ECS)
         ↓          (Prometheus)       ↓
       Jaeger            ↓        Elasticsearch
  (observabilidad-*)  Prometheus       ↓
         ↓              ↓           Kibana
       Grafana  ←───────┘
```

El pipeline del Collector se configura en tres señales separadas:

-   **traces**: recibe trazas y las exporta a Jaeger vía OTLP gRPC (puerto 4317).

-   **metrics**: recibe métricas y las expone en un endpoint Prometheus (puerto 8889) para que Prometheus las recolecte.

-   **logs**: recibe logs y los exporta a Elasticsearch en formato ECS.

## 1.5 Alcance de este manual

Este manual cubre exclusivamente:

-   Preparación del namespace de cada entorno.

-   Mirroring de la imagen al GitLab Container Registry.

-   Creación del Secret de credenciales de Elasticsearch.

-   Creación del ConfigMap con el pipeline del Collector.

-   Despliegue del Collector (Deployment y Service).

-   Verificación del despliegue y del flujo de datos.

-   Troubleshooting de errores comunes.

Queda fuera del alcance de este manual:

-   Instalación de Jaeger (ver Manual de Instalación Jaeger).

-   Instalación de Prometheus (ver Manual de Instalación Prometheus).

-   Instalación de Grafana (ver Manual de Instalación Grafana).

-   Instalación y configuración de Elasticsearch y Kibana (ver Manual de Configuración ELK).

# 2. Prerrequisitos

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Verificar TODOS los prerrequisitos antes de comenzar la instalación. Una verificación incompleta es la causa más común de fallos durante el despliegue.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 2.1 Infraestructura requerida

  --------------------------------------------------------------------------------------------------
  **Componente**     **Requisito**                  **Observación**
  ------------------ ------------------------------ ---------------------------------------------------
  Kubernetes         K3s v1.27 o superior           Verificar con: kubectl version \--short

  Nodos worker       1 vCPU / 2 GB RAM por nodo     Para PROD se recomienda 2 vCPU / 4 GB RAM

  Jaeger Collector   Instalado y operativo          Namespace observabilidad-\*/observabilidad

  Elasticsearch      Versión 7.9.1                  Accesible desde el cluster

  Prometheus         Instalado y operativo          Debe poder hacer scrape al namespace del Collector
  --------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El Collector puede instalarse antes que Prometheus y Grafana. Los exporters intentarán conectarse a los backends cuando reciban telemetría, no al iniciar. Si Jaeger o Elasticsearch aún no están disponibles, el Collector reportará errores en los logs pero seguirá funcionando para los backends que sí estén activos.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 2.2 Herramientas en la máquina de despliegue

  -----------------------------------------------------------------------------------
  **Herramienta**   **Versión mínima**           **Verificación**
  ----------------- ---------------------------- ------------------------------------
  kubectl           v1.27+                       kubectl version \--client

  Docker            v24+                         docker \--version

  curl              Cualquier versión reciente   curl \--version
  -----------------------------------------------------------------------------------

## 2.3 Accesos requeridos

-   Acceso al cluster K3s con permisos de administrador (cluster-admin).

-   Acceso de escritura al GitLab Container Registry del grupo de observabilidad.

-   Credenciales de Elasticsearch: usuario exclusivo para el Collector, contraseña, URL y puerto.

-   Acceso de red desde el cluster hacia el host de Elasticsearch.

-   Acceso de red desde el cluster hacia el namespace de Jaeger (observabilidad-\*/observabilidad).

## 2.4 Información a recopilar antes de comenzar

Completar la siguiente tabla antes de ejecutar cualquier paso:

  -----------------------------------------------------------------------------------------------
  **Placeholder**             **Descripción**                          **Valor real (completar)**
  --------------------------- ---------------------------------------- ----------------------------
  \<GITLAB_REGISTRY_URL\>     URL base del GitLab Registry

  \<ES_HOST\>                 Host o IP de Elasticsearch

  \<ES_PORT\>                 Puerto de Elasticsearch                  9200 (verificar)

  \<OTEL_ES_USERNAME\>        Usuario de ES exclusivo para el Collector

  \<OTEL_ES_PASSWORD\>        Contraseña del usuario de ES

  \<JAEGER_COLLECTOR_URL\>    URL interna del Jaeger Collector          Ver sección 2.5

  \<NAMESPACE\>               Namespace del Collector                   otel-dev / otel-qa / otel

  \<COLLECTOR_VERSION\>       Versión del Collector contrib             Ver sección 3.1
  -----------------------------------------------------------------------------------------------

## 2.5 Determinar la URL del Jaeger Collector

La URL del Jaeger Collector varía según el entorno. El formato es el siguiente:

```
jaeger-onp-collector.<NAMESPACE_JAEGER>.svc.cluster.local:4317
```

  -----------------------------------------------------------------------
  **Entorno**   **URL del Jaeger Collector**
  ------------- ---------------------------------------------------------
  DEV           jaeger-onp-collector.observabilidad-dev.svc.cluster.local:4317

  QA            jaeger-onp-collector.observabilidad-qa.svc.cluster.local:4317

  PROD          jaeger-onp-collector.observabilidad.svc.cluster.local:4317
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El puerto 4317 corresponde al receptor OTLP gRPC de Jaeger, disponible desde Jaeger 1.35. Verificar que el Service de Jaeger expone este puerto con: `kubectl get svc -n <NAMESPACE_JAEGER> jaeger-onp-collector`. Si el puerto 4317 no aparece, consultar el Manual de Instalación Jaeger para habilitarlo en el CRD.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 2.6 Verificaciones previas

### 2.6.1 Verificar conectividad al cluster

```bash
kubectl cluster-info
kubectl get nodes
```

Resultado esperado:

```
NAME        STATUS   ROLES                  AGE   VERSION
<nombre>    Ready    control-plane,master   Xd    vX.XX.X
```

Todos los nodos deben aparecer en estado `Ready`.

### 2.6.2 Verificar que Jaeger Collector está operativo

```bash
kubectl get pods -n <NAMESPACE_JAEGER>
```

Resultado esperado:

```
NAME                                    READY   STATUS    RESTARTS   AGE
jaeger-onp-collector-xxxxxxxxx-xxxxx    1/1     Running   0          Xm
jaeger-onp-query-xxxxxxxxx-xxxxx        2/2     Running   0          Xm
```

Todos los pods deben estar en estado `Running`.

### 2.6.3 Verificar conectividad a Elasticsearch

```bash
curl -u <OTEL_ES_USERNAME>:<OTEL_ES_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Resultado esperado:

```json
{"cluster_name":"...","status":"green","timed_out":false,...}
```

El campo `status` debe ser `green` o `yellow`. Un valor `red` indica problemas en el cluster de Elasticsearch.

### 2.6.4 Verificar acceso al GitLab Registry

```bash
docker login <GITLAB_REGISTRY_URL>
```

Resultado esperado: Login Succeeded.

# 3. Preparación de imágenes

## Nota: ¿Qué camino seguir?

Esta sección cubre tres escenarios de acceso a imágenes. Identificar el que corresponde al entorno donde se ejecuta la instalación y seguir únicamente esa subsección:

  -----------------------------------------------------------------------
  **Escenario**                  **Cuándo usarlo**                                      **Ir a**
  ------------------------------ ------------------------------------------------------ ----------
  A --- GitLab Registry          PROD y QA. El cluster no tiene salida a internet.       Sección 3.2
                                 La imagen se publica en el registry privado.

  B --- Internet directo         DEV y pruebas. El nodo tiene acceso a internet.        Sección 3.3
                                 La imagen se descarga directamente desde Docker Hub.
                                 No se requiere registry privado.

  C --- Air-gap (sin registry    Entornos completamente aislados sin acceso a            Anexo D
  ni internet)                   internet ni registry. La imagen se transfiere
                                 al nodo como archivo .tar.
  -----------------------------------------------------------------------

## 3.1 Imagen requerida

Solo se requiere **una imagen** para la instalación del OTEL Collector:

  -----------------------------------------------------------------------------------------------
  **Componente**              **Imagen original**                              **Versión**
  --------------------------- ------------------------------------------------ --------------------
  OTel Collector Contrib      otel/opentelemetry-collector-contrib             0.120.0
  -----------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Verificar la versión estable más reciente en https://github.com/open-telemetry/opentelemetry-collector-releases/releases antes de ejecutar el mirroring. La versión indicada es la recomendada al momento de elaborar este manual. Usar siempre una versión fija (no `latest`) para garantizar reproducibilidad.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 3.2 Escenario A --- GitLab Registry (producción)

Ejecutar los siguientes pasos desde una máquina con acceso a internet y Docker instalado. Este proceso se denomina mirroring y debe ejecutarse una sola vez por versión de imagen.

### 3.2.1 Iniciar sesión en el GitLab Registry

```bash
docker login <GITLAB_REGISTRY_URL>
```

### 3.2.2 Descargar, re-taggear y publicar la imagen

```bash
# Descargar la imagen del registry público
docker pull otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION>

# Re-taggear apuntando al registry privado
docker tag otel/opentelemetry-collector-contrib:<COLLECTOR_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:<COLLECTOR_VERSION>

# Publicar en el registry privado
docker push <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:<COLLECTOR_VERSION>
```

### 3.2.3 Imagen en el registry privado

Una vez completado el mirroring, la imagen estará disponible en:

```
<GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:<COLLECTOR_VERSION>
```

Esta es la imagen que se usará en el Deployment (Sección 7.1). El bloque `imagePullSecrets` del Deployment debe estar habilitado en este escenario.

## 3.3 Escenario B --- Internet directo (pruebas y DEV)

Cuando el nodo tiene acceso a internet, la imagen se descarga directamente desde Docker Hub sin necesidad de mirroring ni de un registry privado.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** No se requieren los pasos de mirroring ni la creación del ImagePullSecret. Pasar directamente a la Sección 4. En el Deployment (Sección 7.1), usar la imagen pública directamente y mantener el bloque `imagePullSecrets` comentado.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Imagen a usar directamente en el Deployment:

  -----------------------------------------------------------------------
  **Componente**              **Imagen a usar directamente**
  --------------------------- -------------------------------------------
  OTel Collector Contrib      otel/opentelemetry-collector-contrib:0.120.0
  -----------------------------------------------------------------------

## 3.4 Escenario C --- Air-gap (sin registry ni internet)

Para entornos completamente aislados, la imagen se transfiere al nodo como archivo `.tar`. Ver el **Anexo D** para el procedimiento completo.

# 4. Preparación del namespace

## 4.1 Crear el namespace

```bash
kubectl create namespace <NAMESPACE>
```

Donde \<NAMESPACE\> es:

-   `otel-dev` --- para el entorno de desarrollo.

-   `otel-qa` --- para el entorno de calidad.

-   `otel` --- para el entorno de producción.

## 4.2 Agregar labels al namespace

```bash
kubectl label namespace <NAMESPACE> \
  app.kubernetes.io/managed-by=oti-onp \
  environment=<ENV> \
  team=oti-onp
```

Donde \<ENV\> es `dev`, `qa` o `prod` según corresponda.

## 4.3 Crear el ImagePullSecret (solo Escenario A)

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A** (GitLab Registry). En los Escenarios B y C omitir esta sección y continuar en la Sección 4.4.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

El cluster K3s necesita credenciales para acceder al GitLab Registry:

```bash
kubectl create secret docker-registry gitlab-registry-secret \
  --docker-server=<GITLAB_REGISTRY_URL> \
  --docker-username=<GITLAB_USER> \
  --docker-password=<GITLAB_TOKEN> \
  --docker-email=<GITLAB_EMAIL> \
  --namespace=<NAMESPACE>
```

## 4.4 Verificar el namespace

```bash
kubectl get namespace <NAMESPACE> --show-labels
```

Resultado esperado:

```
NAME       STATUS   AGE   LABELS
otel-dev   Active   Xs    app.kubernetes.io/managed-by=oti-onp,environment=dev,team=oti-onp
```

El namespace debe estar en estado `Active` con los tres labels definidos.

# 5. Creación del Secret de Elasticsearch

El Collector necesita las credenciales del usuario de Elasticsearch exclusivo para la recolección de logs. Estas credenciales se almacenan en un Kubernetes Secret y se inyectan como variables de entorno en el pod del Collector.

  -----------------------------------------------------------------------
  **🔴 IMPORTANTE:** Nunca incluir contraseñas en texto plano en archivos YAML que se suban a repositorios de código. El Secret debe crearse únicamente mediante comandos kubectl.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 5.1 Crear el Secret

```bash
kubectl create secret generic otel-es-credentials \
  --from-literal=OTEL_ES_USERNAME=<OTEL_ES_USERNAME> \
  --from-literal=OTEL_ES_PASSWORD=<OTEL_ES_PASSWORD> \
  --namespace=<NAMESPACE>
```

## 5.2 Verificar que el Secret fue creado

```bash
kubectl get secret otel-es-credentials -n <NAMESPACE>
```

Resultado esperado:

```
NAME                  TYPE     DATA   AGE
otel-es-credentials   Opaque   2      10s
```

# 6. Configuración del pipeline (ConfigMap)

El pipeline del Collector se define en un archivo `config.yaml` que se monta en el pod a través de un ConfigMap. Esta configuración define qué señales recibe el Collector, cómo las procesa y a dónde las envía.

## 6.1 Estructura del pipeline

El pipeline tiene tres señales independientes:

  -----------------------------------------------------------------------
  **Señal**   **Receiver**   **Processors**              **Exporter**
  ----------- -------------- --------------------------- -----------------
  traces      otlp           memory_limiter, batch       otlp/jaeger

  metrics     otlp           memory_limiter, batch       prometheus

  logs        otlp           memory_limiter, batch       elasticsearch
  -----------------------------------------------------------------------

## 6.2 Crear el ConfigMap

Los manifests del Collector se almacenan en el repositorio bajo la ruta `manifests/<NAMESPACE>/`. Para DEV: `manifests/otel-dev/`. Crear la carpeta si no existe:

```bash
mkdir -p manifests/<NAMESPACE>
```

Crear el archivo `manifests/<NAMESPACE>/01-configmap.yaml` con el siguiente contenido. Reemplazar los placeholders antes de aplicarlo:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: otel-collector-config
  namespace: <NAMESPACE>
data:
  config.yaml: |
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
        endpoint: <JAEGER_COLLECTOR_URL>
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
          username: ${env:OTEL_ES_USERNAME}
          password: ${env:OTEL_ES_PASSWORD}

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
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Los valores de `<MEMORY_LIMIT_MIB>` y `<SPIKE_LIMIT_MIB>` dependen del entorno. Ver los Anexos para los valores recomendados por entorno. Como referencia, `limit_mib` debe ser aproximadamente el 80% del límite de memoria del contenedor, y `spike_limit_mib` el 25% de `limit_mib`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El índice `onp-logs-<ENV>` define el índice de Elasticsearch donde se almacenarán los logs. Reemplazar `<ENV>` con el valor correspondiente: `development`, `quality` o `production`. Ver convención de índices en la Guía de Desarrollo, Sección 2.9.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Los valores `${env:OTEL_ES_USERNAME}` y `${env:OTEL_ES_PASSWORD}` **no son placeholders a reemplazar**. Es la sintaxis del OTEL Collector para leer variables de entorno en tiempo de ejecución. El Collector leerá automáticamente esos valores desde las variables de entorno inyectadas por el Secret `otel-es-credentials` creado en la Sección 5.1.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 6.3 Aplicar el ConfigMap

```bash
kubectl apply -f manifests/<NAMESPACE>/01-configmap.yaml
```

## 6.4 Verificar el ConfigMap

```bash
kubectl get configmap otel-collector-config -n <NAMESPACE>
```

Resultado esperado:

```
NAME                    DATA   AGE
otel-collector-config   1      10s
```

La columna `DATA: 1` confirma que el ConfigMap contiene un archivo de configuración (`config.yaml`).

# 7. Despliegue del Collector

Con el ConfigMap y el Secret creados, se procede a desplegar el Collector. El despliegue consiste en un Deployment que corre el contenedor del Collector y un Service que expone los puertos internos al cluster.

## 7.1 Crear el Deployment

Crear el archivo `manifests/<NAMESPACE>/02-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: otel-collector
  namespace: <NAMESPACE>
  labels:
    app: otel-collector
    environment: <ENV>
spec:
  replicas: <REPLICAS>
  selector:
    matchLabels:
      app: otel-collector
  template:
    metadata:
      labels:
        app: otel-collector
        environment: <ENV>
    spec:
      containers:
        - name: otel-collector
          image: <GITLAB_REGISTRY_URL>/observabilidad/otel-collector-contrib:<COLLECTOR_VERSION>
          args:
            - "--config=/etc/otel/config.yaml"
          ports:
            - name: otlp-grpc
              containerPort: 4317
            - name: otlp-http
              containerPort: 4318
            - name: prometheus
              containerPort: 8889
          env:
            - name: OTEL_ES_USERNAME
              valueFrom:
                secretKeyRef:
                  name: otel-es-credentials
                  key: OTEL_ES_USERNAME
            - name: OTEL_ES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: otel-es-credentials
                  key: OTEL_ES_PASSWORD
          volumeMounts:
            - name: config
              mountPath: /etc/otel
          resources:
            requests:
              cpu: "<CPU_REQUEST>"
              memory: "<MEMORY_REQUEST>"
            limits:
              cpu: "<CPU_LIMIT>"
              memory: "<MEMORY_LIMIT>"
          livenessProbe:
            httpGet:
              path: /
              port: 13133
            initialDelaySeconds: 15
            periodSeconds: 20
          readinessProbe:
            httpGet:
              path: /
              port: 13133
            initialDelaySeconds: 5
            periodSeconds: 10
      volumes:
        - name: config
          configMap:
            name: otel-collector-config
      # Escenario A únicamente (GitLab Registry privado):
      # imagePullSecrets:
      #   - name: gitlab-registry-secret
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El puerto 13133 es el health check endpoint del Collector. Las probes lo usan para verificar que el proceso está activo y listo. La extensión `health_check` ya está incluida en el ConfigMap de la Sección 6.2.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 7.2 Aplicar el Deployment

```bash
kubectl apply -f manifests/<NAMESPACE>/02-deployment.yaml
```

Resultado esperado:

```
deployment.apps/otel-collector created
```

## 7.3 Crear el Service

Crear el archivo `manifests/<NAMESPACE>/03-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: otel-collector
  namespace: <NAMESPACE>
  labels:
    app: otel-collector
spec:
  selector:
    app: otel-collector
  ports:
    - name: otlp-grpc
      port: 4317
      targetPort: 4317
    - name: otlp-http
      port: 4318
      targetPort: 4318
    - name: prometheus
      port: 8889
      targetPort: 8889
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El nombre del Service es `otel-collector`. Este nombre determina la URL interna que los servicios de backend usan para enviar telemetría. El formato completo es: `http://otel-collector.<NAMESPACE>.svc.cluster.local:4318/v1/traces`. Este valor debe coincidir con el configurado en `management.otlp.tracing.endpoint` de los servicios Spring Boot (ver Guía de Desarrollo, Sección 1.4).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 7.4 Aplicar el Service

```bash
kubectl apply -f manifests/<NAMESPACE>/03-service.yaml
```

Resultado esperado:

```
service/otel-collector created
```

## 7.5 Verificar el despliegue

```bash
kubectl get pods -n <NAMESPACE> -w
```

Resultado esperado (esperar entre 30 y 60 segundos):

```
NAME                             READY   STATUS    RESTARTS   AGE
otel-collector-xxxxxxxxx-xxxxx   1/1     Running   0          45s
```

```bash
kubectl get services -n <NAMESPACE>
```

Resultado esperado:

```
NAME             TYPE        CLUSTER-IP    PORT(S)
otel-collector   ClusterIP   10.x.x.x      4317/TCP,4318/TCP,8889/TCP
```

# 8. Verificación del despliegue

## 8.1 Verificar los logs del Collector

```bash
kubectl logs -n <NAMESPACE> deployment/otel-collector --tail=50
```

Al iniciar correctamente, el Collector muestra las siguientes líneas clave:

```
info    extensions/extensions.go:61     Extension started.      {"otelcol.component.id": "basicauth/es", ...}
info    extensions/extensions.go:61     Extension started.      {"otelcol.component.id": "health_check", ...}
info    otlpreceiver@.../otlp.go:116    Starting GRPC server    {"endpoint": "0.0.0.0:4317"}
info    otlpreceiver@.../otlp.go:173    Starting HTTP server    {"endpoint": "0.0.0.0:4318"}
info    healthcheck/handler.go:132      Health Check state change  {"status": "ready"}
info    service@.../service.go:281      Everything is ready. Begin running and processing data.
```

La línea **"Everything is ready. Begin running and processing data."** confirma que el Collector inició correctamente.

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Es normal ver advertencias de conexión a Elasticsearch o Jaeger durante los primeros segundos si esos componentes aún están iniciando. Si los errores persisten después de 2 minutos, revisar la sección de Troubleshooting (Sección 9).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 8.2 Verificar el health check

```bash
kubectl port-forward -n <NAMESPACE> deployment/otel-collector 13133:13133 &
```

Esperar a que aparezca el mensaje `Forwarding from 127.0.0.1:13133 -> 13133` antes de continuar. Luego:

```bash
curl http://localhost:13133/
```

Resultado esperado:

```json
{"status":"Server available","upSince":"...","uptime":"..."}
```

## 8.3 Enviar una traza de prueba

Usar el port-forward para acceder al endpoint HTTP del Collector y enviar una traza de prueba:

```bash
kubectl port-forward -n <NAMESPACE> svc/otel-collector 4318:4318 &

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

Resultado esperado: respuesta HTTP `200` con body vacío `{}`.

## 8.4 Verificar que la traza llegó a Jaeger

```bash
kubectl port-forward -n <NAMESPACE_JAEGER> svc/jaeger-onp-query 16686:16686 &
```

Abrir el navegador en `http://localhost:16686`, seleccionar el servicio `onp-test-telemetria` y hacer clic en **Find Traces**. Debe aparecer la traza `prueba-otel-collector`.

## 8.5 Verificar que las métricas están disponibles para Prometheus

```bash
kubectl port-forward -n <NAMESPACE> svc/otel-collector 8889:8889 &
curl http://localhost:8889/metrics | head -20
```

Resultado esperado: líneas de métricas en formato Prometheus (texto plano con el prefijo `onp_`).

## 8.6 Verificar que los logs llegan a Elasticsearch

```bash
curl -u <OTEL_ES_USERNAME>:<OTEL_ES_PASSWORD> \
  http://<ES_HOST>:<ES_PORT>/_cat/indices?v | grep onp-logs
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Los índices de logs solo se crean cuando el Collector recibe y exporta al menos un log. Si el índice no aparece, enviar primero trazas de prueba con un servicio instrumentado.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

# 9. Troubleshooting

## 9.1 El pod queda en estado Pending

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME                           READY   STATUS    RESTARTS
otel-collector-xxxxx           0/1     Pending   0
```

### Causas y soluciones

-   Sin recursos suficientes en los nodos: ejecutar `kubectl describe pod <nombre-pod> -n <NAMESPACE>` y revisar el campo `Events`. Si aparece `Insufficient cpu` o `Insufficient memory`, reducir los `requests` en el Deployment o agregar nodos al cluster.

-   ImagePullSecret no existe: verificar con `kubectl get secret gitlab-registry-secret -n <NAMESPACE>`. Si no existe, crearlo con el comando de la Sección 4.3.

## 9.2 Error ErrImagePull

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME                           READY   STATUS        RESTARTS
otel-collector-xxxxx           0/1     ErrImagePull  0
```

### Causas y soluciones

-   La imagen no fue subida al registry: verificar que el mirroring de la Sección 3 se completó correctamente.

-   Credenciales del registry expiradas: eliminar y recrear el ImagePullSecret con `kubectl delete secret gitlab-registry-secret -n <NAMESPACE>` y ejecutar nuevamente el comando de la Sección 4.3.

## 9.3 El Collector arranca pero no envía trazas a Jaeger

### Síntoma

En los logs del Collector aparece:

```
error exporting items, will retry: rpc error: code = Unavailable
```

### Causas y soluciones

-   URL del Jaeger Collector incorrecta en el ConfigMap: verificar el valor de `endpoint` en el exporter `otlp/jaeger`. Debe coincidir exactamente con la tabla de la Sección 2.5.

-   Puerto 4317 no expuesto en el Service de Jaeger: ejecutar `kubectl get svc -n <NAMESPACE_JAEGER> jaeger-onp-collector` y verificar que el puerto 4317 aparece en la lista. Si no aparece, consultar el Manual de Instalación Jaeger para habilitarlo.

-   Problema de red entre namespaces: ejecutar un pod de diagnóstico y probar la conectividad:

```bash
kubectl run nettest --rm -it --image=busybox -n <NAMESPACE> -- \
  wget -O- <JAEGER_COLLECTOR_URL>
```

## 9.4 El Collector no exporta logs a Elasticsearch

### Síntoma

En los logs aparece:

```
error exporting items: failed to push data: 401 Unauthorized
```

### Causas y soluciones

-   Credenciales incorrectas en el Secret: verificar con `kubectl get secret otel-es-credentials -n <NAMESPACE> -o jsonpath='{.data.OTEL_ES_USERNAME}' | base64 -d`.

-   El usuario de ES no tiene permisos de escritura sobre el índice `onp-logs-*`: verificar con el equipo de Plataforma que el usuario tiene el rol `indices:data/write/index` sobre ese patrón de índices.

## 9.5 Las métricas no aparecen en Prometheus

### Síntoma

Prometheus no muestra métricas del Collector ni de los servicios.

### Causas y soluciones

-   Prometheus no está apuntando al endpoint del Collector: verificar la configuración de scrape de Prometheus. Debe tener un job que apunte a `otel-collector.<NAMESPACE>.svc.cluster.local:8889/metrics`.

-   El Service no expone el puerto 8889: verificar con `kubectl get svc otel-collector -n <NAMESPACE>`.

## 9.6 Comandos útiles de diagnóstico rápido

  -----------------------------------------------------------------------
  **Diagnóstico**                          **Comando**
  ---------------------------------------- ------------------------------
  Ver estado del pod                       kubectl get pods -n \<NAMESPACE\>

  Ver logs del Collector (últimas 100 l.)  kubectl logs -n \<NAMESPACE\> deployment/otel-collector \--tail=100

  Ver eventos del namespace                kubectl get events -n \<NAMESPACE\> \--sort-by=\'.lastTimestamp\'

  Ver detalle de un pod con problemas      kubectl describe pod \<nombre-pod\> -n \<NAMESPACE\>

  Ver el ConfigMap aplicado                kubectl get configmap otel-collector-config -n \<NAMESPACE\> -o yaml

  Reiniciar el Collector                   kubectl rollout restart deployment/otel-collector -n \<NAMESPACE\>

  Ver variables de entorno del pod         kubectl exec -n \<NAMESPACE\> deployment/otel-collector \-- env \| grep OTEL
  -----------------------------------------------------------------------

# Anexo A --- Configuración específica para DEV

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para DEV**
  ------------------------- ---------------------------------------------
  Namespace                 otel-dev

  Cluster                   Cluster compartido DEV/QA

  Réplicas                  1

  Jaeger Collector URL      jaeger-onp-collector.observabilidad-dev.svc.cluster.local:4317

  Índice de logs en ES      onp-logs-development

  Exposición externa        kubectl port-forward (solo para diagnóstico)
  -----------------------------------------------------------------------

## A.1 Crear el namespace DEV

```bash
kubectl create namespace otel-dev
kubectl label namespace otel-dev \
  app.kubernetes.io/managed-by=oti-onp \
  environment=dev \
  team=oti-onp
```

## A.2 Valores del ConfigMap para DEV

Reemplazar los placeholders del ConfigMap (Sección 6.2) con los siguientes valores:

  -----------------------------------------------------------------------
  **Placeholder**           **Valor DEV**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             otel-dev

  \<JAEGER_COLLECTOR_URL\>  jaeger-onp-collector.observabilidad-dev.svc.cluster.local:4317

  \<MEMORY_LIMIT_MIB\>      200

  \<SPIKE_LIMIT_MIB\>       50

  \<ENV\>                   development
  -----------------------------------------------------------------------

## A.3 Recursos recomendados para DEV

Reemplazar los placeholders del Deployment (Sección 7.1) con los siguientes valores:

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- --------------------------------------------------------------------
  \<REPLICAS\>          1

  \<CPU_REQUEST\>       100m

  \<CPU_LIMIT\>         300m

  \<MEMORY_REQUEST\>    128Mi

  \<MEMORY_LIMIT\>      256Mi
  ------------------------------------------------------------------------------------------

## A.4 URL del Collector para los servicios en DEV

Una vez desplegado, los servicios Spring Boot en DEV deben configurar en su `application-dev.properties`:

```properties
management.otlp.tracing.endpoint=http://otel-collector.otel-dev.svc.cluster.local:4318/v1/traces
```

# Anexo B --- Configuración específica para QA

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para QA**
  ------------------------- ---------------------------------------------
  Namespace                 otel-qa

  Cluster                   Cluster compartido DEV/QA

  Réplicas                  1

  Jaeger Collector URL      jaeger-onp-collector.observabilidad-qa.svc.cluster.local:4317

  Índice de logs en ES      onp-logs-quality

  Exposición externa        kubectl port-forward (solo para diagnóstico)
  -----------------------------------------------------------------------

## B.1 Crear el namespace QA

```bash
kubectl create namespace otel-qa
kubectl label namespace otel-qa \
  app.kubernetes.io/managed-by=oti-onp \
  environment=qa \
  team=oti-onp
```

## B.2 Valores del ConfigMap para QA

  -----------------------------------------------------------------------
  **Placeholder**           **Valor QA**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             otel-qa

  \<JAEGER_COLLECTOR_URL\>  jaeger-onp-collector.observabilidad-qa.svc.cluster.local:4317

  \<MEMORY_LIMIT_MIB\>      200

  \<SPIKE_LIMIT_MIB\>       50

  \<ENV\>                   quality
  -----------------------------------------------------------------------

## B.3 Recursos recomendados para QA

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- --------------------------------------------------------------------
  \<REPLICAS\>          1

  \<CPU_REQUEST\>       150m

  \<CPU_LIMIT\>         400m

  \<MEMORY_REQUEST\>    128Mi

  \<MEMORY_LIMIT\>      256Mi
  ------------------------------------------------------------------------------------------

## B.4 URL del Collector para los servicios en QA

```properties
management.otlp.tracing.endpoint=http://otel-collector.otel-qa.svc.cluster.local:4318/v1/traces
```

# Anexo C --- Configuración específica para PROD

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para PROD**
  ------------------------- ---------------------------------------------
  Namespace                 otel

  Cluster                   Cluster exclusivo de producción

  Réplicas                  2 (alta disponibilidad)

  Jaeger Collector URL      jaeger-onp-collector.observabilidad.svc.cluster.local:4317

  Índice de logs en ES      onp-logs-production

  Exposición externa        No expuesto externamente (solo ClusterIP)
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** En PROD el Collector no debe exponerse fuera del cluster. Solo los servicios internos del cluster deben tener acceso a los puertos 4317 y 4318. Verificar que no existe ningún NodePort ni Ingress apuntando al Collector en producción.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## C.1 Crear el namespace PROD

```bash
kubectl create namespace otel
kubectl label namespace otel \
  app.kubernetes.io/managed-by=oti-onp \
  environment=prod \
  team=oti-onp
```

## C.2 Valores del ConfigMap para PROD

  -----------------------------------------------------------------------
  **Placeholder**           **Valor PROD**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             otel

  \<JAEGER_COLLECTOR_URL\>  jaeger-onp-collector.observabilidad.svc.cluster.local:4317

  \<MEMORY_LIMIT_MIB\>      400

  \<SPIKE_LIMIT_MIB\>       100

  \<ENV\>                   production
  -----------------------------------------------------------------------

## C.3 Recursos recomendados para PROD

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- --------------------------------------------------------------------
  \<REPLICAS\>          2

  \<CPU_REQUEST\>       300m

  \<CPU_LIMIT\>         800m

  \<MEMORY_REQUEST\>    256Mi

  \<MEMORY_LIMIT\>      512Mi
  ------------------------------------------------------------------------------------------

## C.4 URL del Collector para los servicios en PROD

```properties
management.otlp.tracing.endpoint=http://otel-collector.otel.svc.cluster.local:4318/v1/traces
```

## C.5 Verificar alta disponibilidad en PROD

Con 2 réplicas, verificar que ambos pods están corriendo:

```bash
kubectl get pods -n otel
```

Resultado esperado:

```
NAME                             READY   STATUS    RESTARTS
otel-collector-xxxxxxxxx-xxxxx   1/1     Running   0
otel-collector-xxxxxxxxx-yyyyy   1/1     Running   0
```

El Service distribuye el tráfico entre ambas réplicas automáticamente mediante el balanceo de carga interno de Kubernetes.

## C.6 Escalado automático con HPA (opcional para PROD)

El OTEL Collector es stateless — recibe, procesa y reenvía telemetría sin guardar estado — lo que lo hace apto para escalado horizontal automático mediante un **HorizontalPodAutoscaler (HPA)**.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El HPA requiere que el `metrics-server` esté instalado en el cluster. K3s lo incluye por defecto. Verificar con: `kubectl get pods -n kube-system | grep metrics-server`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Con HPA habilitado, establecer `replicas: 2` en el Deployment (mínimo base). El HPA tomará control del número de réplicas a partir de ese valor. No reducir `replicas` a 1 — el HPA respeta el mínimo declarado en su propio manifiesto.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Crear el archivo `manifests/otel/04-hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: otel-collector-hpa
  namespace: otel
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: otel-collector
  minReplicas: 2
  maxReplicas: 6
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 75
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El trigger de memoria (75%) está alineado con el `memory_limiter` del ConfigMap, que tiene `limit_mib: 400` (≈80% del límite del contenedor de 512Mi). El HPA escalará antes de que el `memory_limiter` empiece a rechazar datos.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Aplicar el HPA:

```bash
kubectl apply -f manifests/otel/04-hpa.yaml
```

Resultado esperado:

```
horizontalpodautoscaler.autoscaling/otel-collector-hpa created
```

Verificar el estado del HPA:

```bash
kubectl get hpa -n otel
```

Resultado esperado:

```
NAME                  REFERENCE                  TARGETS           MINPODS   MAXPODS   REPLICAS
otel-collector-hpa    Deployment/otel-collector  15%/70%, 30%/75%  2         6         2
```

# Anexo D --- Air-gap: transferencia de imagen sin internet ni registry

Este anexo aplica al **Escenario C**: el nodo no tiene acceso a internet y no hay un registry privado disponible. La imagen se transfiere manualmente como archivo `.tar`.

## D.1 Exportar la imagen en una máquina con internet

Ejecutar en una máquina con Docker e internet:

```bash
# Descargar la imagen
docker pull otel/opentelemetry-collector-contrib:0.120.0

# Exportar a archivo .tar
docker save otel/opentelemetry-collector-contrib:0.120.0 \
  -o otel-collector-contrib-0.120.0.tar
```

## D.2 Transferir el archivo al nodo

```bash
scp otel-collector-contrib-0.120.0.tar <USUARIO>@<IP_NODO>:/tmp/
```

## D.3 Importar la imagen en el nodo

Kubernetes usa `containerd` como runtime. Ejecutar según la distribución del cluster:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images import /tmp/otel-collector-contrib-0.120.0.tar

# Opción B — K3s
sudo k3s ctr images import /tmp/otel-collector-contrib-0.120.0.tar
```

Verificar que la imagen quedó disponible:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images ls | grep otel-collector-contrib

# Opción B — K3s
sudo k3s ctr images list | grep otel-collector-contrib
```

## D.4 Usar la imagen importada en el Deployment

En el Deployment (Sección 7.1), usar el nombre de imagen tal como fue importado:

```yaml
image: docker.io/otel/opentelemetry-collector-contrib:0.120.0
```

Mantener el bloque `imagePullSecrets` comentado. El nodo usará la imagen local sin intentar descargarla.
