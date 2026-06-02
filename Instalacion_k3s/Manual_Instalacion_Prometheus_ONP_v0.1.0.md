**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Prometheus en Kubernetes (K3s)**

Usando manifests Kubernetes directos

  -----------------------------------------------------------------------
  **Versión:**                 0.1.0
  ---------------------------- ------------------------------------------
  **Fecha:**                   2026-05-13

  **Clasificación:**           Uso Interno (Técnico)

  **Área responsable:**        OTI
  -----------------------------------------------------------------------

# Historial de versiones

  -----------------------------------------------------------------------------
  **Versión**   **Fecha**    **Autor**          **Descripción**
  ------------- ------------ ------------------ -------------------------------
  0.1.0         2026-05-13   \<AUTOR\>          Versión inicial del manual

  -----------------------------------------------------------------------------

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Prometheus en un cluster Kubernetes K3s on-premise, utilizando manifests Kubernetes directos (Namespace, RBAC, ConfigMap, PVC, Deployment y Service).

El documento está dirigido al equipo de Plataforma de la OTI --- ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes a todos los entornos están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Prometheus?

Prometheus es un sistema de monitoreo y alerta de código abierto diseñado para entornos cloud-native. Recolecta métricas mediante un modelo de **scraping** (consulta activa a endpoints HTTP en formato texto plano) y las almacena en una base de datos de series de tiempo local (TSDB).

## 1.3 Rol de Prometheus en el stack ONP

En el stack ONP, Prometheus actúa como el sistema de almacenamiento y consulta de métricas. No recibe métricas directamente de los servicios Spring Boot --- esa responsabilidad recae en el OTEL Collector. Prometheus hace scrape al endpoint de métricas que el OTEL Collector expone:

```
Servicios Spring Boot (OTLP)
          ↓
  OpenTelemetry Collector
  (namespace: otel-dev/qa/otel)
          ↓ puerto 8889 (formato Prometheus)
       Prometheus
  (namespace: prometheus-dev/qa/prometheus)
          ↓
       Grafana
```

El OTEL Collector re-expone las métricas recibidas vía OTLP en formato Prometheus en el puerto 8889. Prometheus hace scrape a ese endpoint cada 15 segundos (configurable).

## 1.4 Prometheus no es stateless

A diferencia del OTEL Collector, Prometheus **no es stateless**. Almacena sus datos en una base de datos de series de tiempo local (TSDB) en un PersistentVolume. Por esta razón:

-   No aplica escalado horizontal con HPA.
-   Se despliega con **1 réplica** en todos los entornos.
-   Para alta disponibilidad en PROD se requeriría Thanos o similar, que está fuera del alcance de este stack.

## 1.5 Alcance de este manual

Este manual cubre exclusivamente:

-   Preparación del namespace de cada entorno.
-   Configuración RBAC para descubrimiento de servicios en el cluster.
-   Creación del ConfigMap con el scrape config.
-   Creación del PersistentVolumeClaim para almacenamiento de métricas.
-   Despliegue de Prometheus (Deployment y Service).
-   Verificación del despliegue y del scrape al OTEL Collector.
-   Troubleshooting de errores comunes.

Queda fuera del alcance de este manual:

-   Instalación del OTEL Collector (ver Manual de Instalación OTEL Collector).
-   Instalación de Grafana (ver Manual de Instalación Grafana).
-   Configuración de alertas y AlertManager.

# 2. Prerrequisitos

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Verificar TODOS los prerrequisitos antes de comenzar la instalación. Una verificación incompleta es la causa más común de fallos durante el despliegue.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 2.1 Infraestructura requerida

  --------------------------------------------------------------------------------------------------
  **Componente**          **Requisito**                  **Observación**
  ----------------------- ------------------------------ -------------------------------------------
  Kubernetes              K3s v1.27 o superior           Verificar con: kubectl version \--short

  Nodos worker            1 vCPU / 2 GB RAM por nodo     Para PROD se recomienda 2 vCPU / 4 GB RAM

  OTEL Collector          Instalado y operativo          Namespace otel-\*/otel

  StorageClass            local-path (K3s por defecto)   Verificar con: kubectl get storageclass
  --------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Prometheus puede instalarse antes que Grafana. Grafana consultará a Prometheus como datasource una vez instalado. Si el OTEL Collector aún no está disponible, Prometheus arrancará pero no tendrá datos hasta que el scrape pueda conectarse.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 2.2 Herramientas en la máquina de despliegue

  -----------------------------------------------------------------------------------
  **Herramienta**   **Versión mínima**           **Verificación**
  ----------------- ---------------------------- ------------------------------------
  kubectl           v1.27+                       kubectl version \--client

  curl              Cualquier versión reciente   curl \--version
  -----------------------------------------------------------------------------------

## 2.3 Accesos requeridos

-   Acceso al cluster K3s con permisos de administrador (cluster-admin).
-   Acceso de escritura al GitLab Container Registry del grupo de observabilidad (solo Escenario A).
-   Acceso de red desde el namespace de Prometheus hacia el namespace del OTEL Collector.

## 2.4 Información a recopilar antes de comenzar

Completar la siguiente tabla antes de ejecutar cualquier paso:

  -----------------------------------------------------------------------------------------------
  **Placeholder**               **Descripción**                          **Valor real (completar)**
  ----------------------------- ---------------------------------------- ----------------------------
  \<GITLAB_REGISTRY_URL\>       URL base del GitLab Registry

  \<NAMESPACE\>                 Namespace de Prometheus                   prometheus-dev / prometheus-qa / prometheus

  \<NAMESPACE_OTEL\>            Namespace del OTEL Collector              otel-dev / otel-qa / otel

  \<ENV\>                       Entorno                                   dev / qa / prod

  \<PROMETHEUS_VERSION\>        Versión de Prometheus                     Ver sección 3.1

  \<RETENTION_TIME\>            Tiempo de retención de métricas           Ver Anexos A/B/C

  \<STORAGE_SIZE\>              Tamaño del PVC                            Ver Anexos A/B/C
  -----------------------------------------------------------------------------------------------

## 2.5 Verificaciones previas

### 2.5.1 Verificar conectividad al cluster

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

### 2.5.2 Verificar que el OTEL Collector está operativo

```bash
kubectl get pods -n <NAMESPACE_OTEL>
kubectl get svc -n <NAMESPACE_OTEL>
```

Resultado esperado:

```
NAME                             READY   STATUS    RESTARTS   AGE
otel-collector-xxxxxxxxx-xxxxx   1/1     Running   0          Xm
```

```
NAME             TYPE        CLUSTER-IP    PORT(S)
otel-collector   ClusterIP   10.x.x.x      4317/TCP,4318/TCP,8889/TCP
```

El puerto `8889` debe aparecer en la lista del Service. Es el endpoint de métricas que Prometheus scrapeará.

### 2.5.3 Verificar la StorageClass disponible

```bash
kubectl get storageclass
```

Resultado esperado (K3s):

```
NAME                   PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE
local-path (default)   rancher.io/local-path   Delete          WaitForFirstConsumer
```

La StorageClass `local-path` debe estar marcada como `(default)`.

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

Solo se requiere **una imagen** para la instalación de Prometheus:

  -----------------------------------------------------------------------------------------------
  **Componente**   **Imagen original**    **Versión**
  ---------------- ---------------------- --------------------
  Prometheus       prom/prometheus        v2.53.0
  -----------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Verificar la versión estable más reciente en https://github.com/prometheus/prometheus/releases antes de ejecutar el mirroring. La versión indicada es la recomendada al momento de elaborar este manual. Usar siempre una versión fija (no `latest`) para garantizar reproducibilidad.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 3.2 Escenario A --- GitLab Registry (producción)

Ejecutar los siguientes pasos desde una máquina con acceso a internet y Docker instalado.

### 3.2.1 Iniciar sesión en el GitLab Registry

```bash
docker login <GITLAB_REGISTRY_URL>
```

### 3.2.2 Descargar, re-taggear y publicar la imagen

```bash
# Descargar la imagen del registry público
docker pull prom/prometheus:<PROMETHEUS_VERSION>

# Re-taggear apuntando al registry privado
docker tag prom/prometheus:<PROMETHEUS_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/prometheus:<PROMETHEUS_VERSION>

# Publicar en el registry privado
docker push <GITLAB_REGISTRY_URL>/observabilidad/prometheus:<PROMETHEUS_VERSION>
```

### 3.2.3 Imagen en el registry privado

Una vez completado el mirroring, la imagen estará disponible en:

```
<GITLAB_REGISTRY_URL>/observabilidad/prometheus:<PROMETHEUS_VERSION>
```

Esta es la imagen que se usará en el Deployment (Sección 7.1). El bloque `imagePullSecrets` del Deployment debe estar habilitado en este escenario.

## 3.3 Escenario B --- Internet directo (pruebas y DEV)

Cuando el nodo tiene acceso a internet, la imagen se descarga directamente desde Docker Hub.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** No se requieren los pasos de mirroring ni la creación del ImagePullSecret. Pasar directamente a la Sección 4. En el Deployment (Sección 7.1), usar la imagen pública directamente y mantener el bloque `imagePullSecrets` comentado.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Imagen a usar directamente en el Deployment:

  -----------------------------------------------------------------------
  **Componente**   **Imagen a usar directamente**
  ---------------- -------------------------------------------
  Prometheus       prom/prometheus:v2.53.0
  -----------------------------------------------------------------------

## 3.4 Escenario C --- Air-gap (sin registry ni internet)

Para entornos completamente aislados, la imagen se transfiere al nodo como archivo `.tar`. Ver el **Anexo D** para el procedimiento completo.

# 4. Preparación del namespace

## 4.1 Crear el namespace

```bash
kubectl create namespace <NAMESPACE>
```

Donde \<NAMESPACE\> es:

-   `prometheus-dev` --- para el entorno de desarrollo.
-   `prometheus-qa` --- para el entorno de calidad.
-   `prometheus` --- para el entorno de producción.

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
NAME              STATUS   AGE   LABELS
prometheus-dev    Active   Xs    app.kubernetes.io/managed-by=oti-onp,environment=dev,team=oti-onp
```

El namespace debe estar en estado `Active` con los tres labels definidos.

# 5. Configuración RBAC

Prometheus necesita permisos para descubrir servicios y pods en el cluster mediante la API de Kubernetes. Esto se configura con un ServiceAccount, un ClusterRole y un ClusterRoleBinding.

## 5.1 Crear el manifiesto RBAC

Crear el archivo `manifests/<NAMESPACE>/02-rbac.yaml`:

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: prometheus
  namespace: <NAMESPACE>
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: prometheus-<NAMESPACE>
rules:
  - apiGroups: [""]
    resources:
      - nodes
      - nodes/proxy
      - services
      - endpoints
      - pods
    verbs: ["get", "list", "watch"]
  - apiGroups: ["extensions", "networking.k8s.io"]
    resources:
      - ingresses
    verbs: ["get", "list", "watch"]
  - nonResourceURLs: ["/metrics"]
    verbs: ["get"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: prometheus-<NAMESPACE>
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: prometheus-<NAMESPACE>
subjects:
  - kind: ServiceAccount
    name: prometheus
    namespace: <NAMESPACE>
```

## 5.2 Aplicar el RBAC

```bash
kubectl apply -f manifests/<NAMESPACE>/02-rbac.yaml
```

Resultado esperado:

```
serviceaccount/prometheus created
clusterrole.rbac.authorization.k8s.io/prometheus-<NAMESPACE> created
clusterrolebinding.rbac.authorization.k8s.io/prometheus-<NAMESPACE> created
```

## 5.3 Verificar el RBAC

```bash
kubectl get serviceaccount prometheus -n <NAMESPACE>
kubectl get clusterrolebinding prometheus-<NAMESPACE>
```

Resultado esperado:

```
NAME         SECRETS   AGE
prometheus   0         10s
```

```
NAME                       ROLE                            AGE
prometheus-prometheus-dev  ClusterRole/prometheus-prometheus-dev   10s
```

# 6. Configuración del scrape (ConfigMap)

El comportamiento de Prometheus se define en el archivo `prometheus.yml`, que se monta en el pod a través de un ConfigMap. Esta configuración define con qué frecuencia scrape y qué endpoints consulta.

## 6.1 Determinar y verificar el endpoint del OTEL Collector

Antes de crear el ConfigMap, se debe confirmar que el endpoint del OTEL Collector es correcto y accesible desde el cluster.

### ¿Qué es `otel-collector.otel-dev.svc.cluster.local:8889`?

Dentro de un cluster Kubernetes, cada Service tiene un nombre DNS interno que permite que los pods se comuniquen entre sí sin necesidad de IPs fijas. El formato es:

```
<nombre-del-service>.<namespace>.svc.cluster.local:<puerto>
```

Para el OTEL Collector en DEV:

  -----------------------------------------------------------------------
  **Parte**                         **Valor**
  --------------------------------- -------------------------------------
  Nombre del Service                otel-collector
  Namespace del OTEL Collector      otel-dev
  Puerto de métricas                8889
  **URL completa**                  otel-collector.otel-dev.svc.cluster.local:8889
  -----------------------------------------------------------------------

La tabla con las URLs por entorno es:

  -----------------------------------------------------------------------
  **Entorno**   **URL del endpoint de métricas**
  ------------- ---------------------------------------------------------
  DEV           otel-collector.otel-dev.svc.cluster.local:8889

  QA            otel-collector.otel-qa.svc.cluster.local:8889

  PROD          otel-collector.otel.svc.cluster.local:8889
  -----------------------------------------------------------------------

### Verificar que el Service del OTEL Collector existe y expone el puerto 8889

Antes de continuar, confirmar que el Service existe y que el puerto 8889 está expuesto:

```bash
kubectl get svc -n <NAMESPACE_OTEL> otel-collector
```

Resultado esperado:

```
NAME             TYPE        CLUSTER-IP    PORT(S)
otel-collector   ClusterIP   10.x.x.x      4317/TCP,4318/TCP,8889/TCP
```

El puerto `8889/TCP` debe aparecer en la lista. Si no aparece, el OTEL Collector no fue instalado correctamente --- consultar el Manual de Instalación OTEL Collector.

### Verificar que el endpoint responde correctamente

Este paso simula exactamente lo que hará Prometheus al hacer scrape: una petición HTTP desde dentro del cluster al nombre DNS interno del OTEL Collector.

```bash
kubectl run nettest --rm -it --image=busybox \
  -n <NAMESPACE> -- \
  wget -O- http://otel-collector.<NAMESPACE_OTEL>.svc.cluster.local:8889/metrics 2>/dev/null | head -5
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este comando crea un pod temporal (`nettest`) en el namespace de Prometheus, ejecuta la petición y elimina el pod automáticamente al terminar. El flag `--rm` garantiza que no quede ningún residuo en el cluster.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Resultado esperado: las primeras líneas de métricas en formato Prometheus:

```
# HELP otelcol_exporter_queue_capacity Capacity of the retry queue
# TYPE otelcol_exporter_queue_capacity gauge
otelcol_exporter_queue_capacity{...} 1000
# HELP otelcol_process_cpu_seconds Total CPU user and system time in seconds
# TYPE otelcol_process_cpu_seconds counter
```

Si el comando responde con métricas, el endpoint es correcto y puede usarse en el ConfigMap. Si responde con error de conexión, revisar que el OTEL Collector está corriendo y que el namespace es el correcto.

## 6.2 Estructura del scrape config

El ConfigMap define dos jobs de scrape:

  -----------------------------------------------------------------------
  **Job**              **Target**                          **Puerto**
  -------------------- ----------------------------------- ---------------
  prometheus           El propio Prometheus (self-monitoring)   9090

  otel-collector       OTEL Collector del entorno          8889
  -----------------------------------------------------------------------

## 6.3 Crear el ConfigMap

Crear el archivo `manifests/<NAMESPACE>/03-configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
  namespace: <NAMESPACE>
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
      evaluation_interval: 15s

    scrape_configs:
      - job_name: 'prometheus'
        static_configs:
          - targets: ['localhost:9090']

      - job_name: 'otel-collector'
        static_configs:
          - targets: ['otel-collector.<NAMESPACE_OTEL>.svc.cluster.local:8889']
        metrics_path: /metrics
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El job `otel-collector` apunta al Service del OTEL Collector en su namespace correspondiente. Para DEV: `otel-collector.otel-dev.svc.cluster.local:8889`. Para QA: `otel-collector.otel-qa.svc.cluster.local:8889`. Para PROD: `otel-collector.otel.svc.cluster.local:8889`. Ver Anexos A/B/C para los valores concretos por entorno.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 6.4 Aplicar el ConfigMap

```bash
kubectl apply -f manifests/<NAMESPACE>/03-configmap.yaml
```

Resultado esperado:

```
configmap/prometheus-config created
```

## 6.5 Verificar el ConfigMap

```bash
kubectl get configmap prometheus-config -n <NAMESPACE>
```

Resultado esperado:

```
NAME                DATA   AGE
prometheus-config   1      10s
```

# 7. PersistentVolumeClaim

Prometheus almacena sus métricas en una base de datos de series de tiempo local (TSDB). Esta base de datos requiere almacenamiento persistente para sobrevivir reinicios del pod.

El ciclo de vida del PVC es **independiente** del pod. Reiniciar el pod, hacer un rollout del Deployment o ejecutar `kubectl delete pod` no elimina los datos --- el pod nuevo monta el mismo PVC y Prometheus retoma desde donde estaba. Los datos se pierden únicamente si el PVC es eliminado explícitamente.

  -----------------------------------------------------------------------
  **ℹ️ NOTA --- Limitación de local-path:** La StorageClass `local-path` de K3s almacena los datos en el disco local del nodo físico. Si ese nodo falla de forma permanente, los datos de Prometheus se pierden. Para un entorno PROD con múltiples nodos y requisitos de alta disponibilidad, se requeriría un StorageClass distribuido (NFS, Longhorn, Ceph), lo cual está fuera del alcance de este stack.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Nunca ejecutar `kubectl delete pvc prometheus-data` en un entorno con datos históricos. A diferencia del reinicio del pod, eliminar el PVC destruye los datos de forma irreversible.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 7.1 Crear el PVC

Crear el archivo `manifests/<NAMESPACE>/04-pvc.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: prometheus-data
  namespace: <NAMESPACE>
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: <STORAGE_SIZE>
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El valor de `<STORAGE_SIZE>` depende del entorno y del tiempo de retención configurado. Ver Anexos A/B/C para los valores recomendados. Como referencia: DEV usa 5Gi (15 días), QA usa 10Gi (30 días) y PROD usa 30Gi (90 días).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 7.2 Aplicar el PVC

```bash
kubectl apply -f manifests/<NAMESPACE>/04-pvc.yaml
```

Resultado esperado:

```
persistentvolumeclaim/prometheus-data created
```

## 7.3 Verificar el PVC

```bash
kubectl get pvc -n <NAMESPACE>
```

Resultado esperado:

```
NAME              STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS   AGE
prometheus-data   Pending                                      local-path     5s
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El estado `Pending` es normal en este punto. El PVC permanece en `Pending` hasta que el pod del Deployment lo monte por primera vez. K3s usa `WaitForFirstConsumer` como política de binding, lo que significa que el volumen se crea cuando el pod arranca.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

# 8. Despliegue de Prometheus

## 8.1 Crear el Deployment

Crear el archivo `manifests/<NAMESPACE>/05-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
  namespace: <NAMESPACE>
  labels:
    app: prometheus
    environment: <ENV>
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
        environment: <ENV>
    spec:
      serviceAccountName: prometheus
      containers:
        - name: prometheus
          image: <IMAGE>
          args:
            - "--config.file=/etc/prometheus/prometheus.yml"
            - "--storage.tsdb.path=/prometheus"
            - "--storage.tsdb.retention.time=<RETENTION_TIME>"
            - "--web.console.libraries=/usr/share/prometheus/console_libraries"
            - "--web.console.templates=/usr/share/prometheus/consoles"
            - "--web.enable-lifecycle"
          ports:
            - name: http
              containerPort: 9090
          volumeMounts:
            - name: config
              mountPath: /etc/prometheus
            - name: data
              mountPath: /prometheus
          resources:
            requests:
              cpu: "<CPU_REQUEST>"
              memory: "<MEMORY_REQUEST>"
            limits:
              cpu: "<CPU_LIMIT>"
              memory: "<MEMORY_LIMIT>"
          readinessProbe:
            httpGet:
              path: /-/ready
              port: 9090
            initialDelaySeconds: 10
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /-/healthy
              port: 9090
            initialDelaySeconds: 15
            periodSeconds: 20
      volumes:
        - name: config
          configMap:
            name: prometheus-config
        - name: data
          persistentVolumeClaim:
            claimName: prometheus-data
      # Escenario A únicamente (GitLab Registry privado):
      # imagePullSecrets:
      #   - name: gitlab-registry-secret
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El flag `--web.enable-lifecycle` habilita el endpoint `/-/reload` que permite recargar la configuración de Prometheus sin reiniciar el pod. Útil cuando se actualiza el ConfigMap.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El placeholder `<IMAGE>` debe reemplazarse con la imagen correspondiente al escenario: Escenario A: `<GITLAB_REGISTRY_URL>/observabilidad/prometheus:<PROMETHEUS_VERSION>`. Escenario B: `prom/prometheus:v2.53.0`. Escenario C: `docker.io/prom/prometheus:v2.53.0`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 8.2 Aplicar el Deployment

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Antes de aplicar, verificar que todos los placeholders del archivo fueron reemplazados. El placeholder `<RETENTION_TIME>` es especialmente crítico — si no se reemplaza, Prometheus falla al arrancar con el error `not a valid duration string`. Ver Anexos A/B/C para los valores por entorno.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

```bash
kubectl apply -f manifests/<NAMESPACE>/05-deployment.yaml
```

Resultado esperado:

```
deployment.apps/prometheus created
```

## 8.3 Crear el Service

Crear el archivo `manifests/<NAMESPACE>/06-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: prometheus
  namespace: <NAMESPACE>
  labels:
    app: prometheus
spec:
  selector:
    app: prometheus
  ports:
    - name: http
      port: 9090
      targetPort: 9090
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El nombre del Service es `prometheus`. La URL interna que Grafana usará como datasource es: `http://prometheus.<NAMESPACE>.svc.cluster.local:9090`. Este valor debe configurarse en Grafana al agregar el datasource de Prometheus (ver Manual de Instalación Grafana).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 8.4 Aplicar el Service

```bash
kubectl apply -f manifests/<NAMESPACE>/06-service.yaml
```

Resultado esperado:

```
service/prometheus created
```

## 8.5 Verificar el despliegue

```bash
kubectl get pods -n <NAMESPACE> -w
```

Resultado esperado (esperar entre 20 y 40 segundos):

```
NAME                          READY   STATUS    RESTARTS   AGE
prometheus-xxxxxxxxx-xxxxx    1/1     Running   0          30s
```

```bash
kubectl get pvc -n <NAMESPACE>
```

Resultado esperado (el PVC ahora debe estar Bound):

```
NAME              STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS
prometheus-data   Bound    pvc-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx   5Gi        RWO            local-path
```

```bash
kubectl get services -n <NAMESPACE>
```

Resultado esperado:

```
NAME         TYPE        CLUSTER-IP    PORT(S)
prometheus   ClusterIP   10.x.x.x      9090/TCP
```

# 9. Verificación del despliegue

## 9.1 Verificar los logs de Prometheus

```bash
kubectl logs -n <NAMESPACE> deployment/prometheus --tail=30
```

Al iniciar correctamente, Prometheus muestra líneas similares a:

```
ts=... level=info msg="Server is ready to receive web requests."
```

## 9.2 Acceder a la UI de Prometheus

```bash
kubectl port-forward -n <NAMESPACE> svc/prometheus 9090:9090 &
```

Esperar a que aparezca el mensaje `Forwarding from 127.0.0.1:9090 -> 9090` antes de continuar. Luego abrir en el navegador:

```
http://localhost:9090
```

La interfaz de Prometheus debe cargar mostrando el campo de consulta PromQL.

## 9.3 Verificar el estado de los targets

En la UI de Prometheus, navegar a **Status → Targets**.

Resultado esperado: los targets `prometheus` y `otel-collector` deben aparecer en estado **UP**.

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si el target `otel-collector` aparece en estado **DOWN**, verificar que el OTEL Collector está corriendo y que el nombre del Service y el puerto 8889 son correctos. Ver Sección 9 (Troubleshooting) para diagnóstico.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 9.4 Verificar el target via curl

```bash
curl http://localhost:9090/api/v1/targets | grep -o '"health":"[^"]*"'
```

Resultado esperado:

```
"health":"up"
"health":"up"
```

Dos targets `up` --- uno por cada job definido en el ConfigMap.

## 9.5 Ejecutar una consulta PromQL de prueba

En la UI de Prometheus (http://localhost:9090), ejecutar en el campo de consulta:

```
up
```

Resultado esperado: dos series con valor `1` --- una para `prometheus` y otra para `otel-collector`.

## 9.6 Verificar métricas del OTEL Collector

Es importante distinguir dos tipos de métricas relacionadas con el OTEL Collector:

  -----------------------------------------------------------------------
  **Puerto**   **Qué expone**                                        **Cuándo aparece**
  ------------ ----------------------------------------------------- -------------------------
  8888         Métricas internas del Collector (`otelcol_*`)         Apenas arranca el Collector
  8889         Métricas de servicios recibidas via OTLP              Solo cuando los servicios Spring Boot envían métricas
  -----------------------------------------------------------------------

Prometheus scrape el puerto **8889**. En una instalación nueva, sin servicios Spring Boot instrumentados aún, ese endpoint está vacío --- esto es el comportamiento esperado.

Para confirmar que el scrape funciona correctamente y que el endpoint responde:

```bash
curl -s http://localhost:9090/api/v1/query?query=scrape_duration_seconds | grep -o '"job":"[^"]*"'
```

Resultado esperado:

```
"job":"prometheus"
"job":"otel-collector"
```

Ambos jobs aparecen, lo que confirma que Prometheus está scrapeando correctamente ambos targets. Las métricas de los servicios Spring Boot (`onp_*`) aparecerán en Prometheus automáticamente una vez que los servicios estén instrumentados y enviando telemetría al OTEL Collector.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Las métricas internas del Collector (`otelcol_*`) están disponibles en el puerto 8888 del pod del OTEL Collector, que no está incluido en el Service ni en el scrape config de este manual. Si se requiere monitorear el estado interno del Collector, agregar un job adicional en el ConfigMap apuntando a `otel-collector.<NAMESPACE_OTEL>.svc.cluster.local:8888` y exponer ese puerto en el Service del OTEL Collector.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

# 10. Troubleshooting

## 10.1 CrashLoopBackOff por placeholder no reemplazado

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME                        READY   STATUS             RESTARTS
prometheus-xxxxx            0/1     CrashLoopBackOff   3
```

```bash
kubectl logs -n <NAMESPACE> deployment/prometheus
Error parsing command line arguments: not a valid duration string: "<RETENTION_TIME>"
```

### Causa y solución

El placeholder `<RETENTION_TIME>` en el Deployment no fue reemplazado antes de aplicar el manifiesto. Editar el archivo `manifests/<NAMESPACE>/05-deployment.yaml`, reemplazar `<RETENTION_TIME>` con el valor del Anexo correspondiente (`15d` para DEV, `30d` para QA, `90d` para PROD) y volver a aplicar:

```bash
kubectl apply -f manifests/<NAMESPACE>/05-deployment.yaml
```

Kubernetes actualizará el Deployment automáticamente y creará un pod nuevo con la configuración corregida.

## 10.2 El pod queda en estado Pending

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME                        READY   STATUS    RESTARTS
prometheus-xxxxx            0/1     Pending   0
```

### Causas y soluciones

-   Sin recursos suficientes: ejecutar `kubectl describe pod <nombre-pod> -n <NAMESPACE>` y revisar el campo `Events`.
-   PVC no puede crearse: verificar que la StorageClass `local-path` existe con `kubectl get storageclass`.

## 10.2 El target otel-collector aparece DOWN

### Síntoma

En la UI de Prometheus (Status → Targets), el job `otel-collector` aparece en rojo con estado `DOWN`.

### Causas y soluciones

-   El OTEL Collector no está corriendo: verificar con `kubectl get pods -n <NAMESPACE_OTEL>`.
-   El nombre del Service del OTEL Collector en el ConfigMap es incorrecto: verificar que `otel-collector.<NAMESPACE_OTEL>.svc.cluster.local:8889` resuelve correctamente ejecutando un pod de diagnóstico:

```bash
kubectl run nettest --rm -it --image=busybox -n <NAMESPACE> -- \
  wget -O- http://otel-collector.<NAMESPACE_OTEL>.svc.cluster.local:8889/metrics
```

-   El puerto 8889 no está expuesto en el Service del OTEL Collector: verificar con `kubectl get svc -n <NAMESPACE_OTEL> otel-collector`.

## 10.3 No aparecen métricas de los servicios Spring Boot

### Síntoma

El target `otel-collector` está UP pero no hay métricas de los servicios de backend.

### Causas y soluciones

-   Los servicios Spring Boot aún no están instrumentados con el SDK de OpenTelemetry: ver Guía de Desarrollo.
-   El OTEL Collector no está recibiendo telemetría: verificar los logs del Collector con `kubectl logs -n <NAMESPACE_OTEL> deployment/otel-collector`.

## 10.4 Error de imagen no encontrada (ErrImagePull)

### Causas y soluciones

-   Escenario A: verificar que el mirroring de la Sección 3.2 se completó y que el ImagePullSecret existe en el namespace.
-   Escenario B: verificar conectividad a internet desde el nodo con `curl https://registry-1.docker.io`.
-   Escenario C: verificar que la imagen fue importada con `sudo ctr -n k8s.io images ls | grep prometheus` (K8s estándar) o `sudo k3s ctr images list | grep prometheus` (K3s).

## 10.5 Prometheus no retiene datos tras reinicio del pod

### Síntoma

Después de un reinicio del pod de Prometheus, las métricas históricas desaparecen.

### Causa y solución

El PVC no está montado correctamente o fue eliminado. Verificar con `kubectl get pvc -n <NAMESPACE>`. El PVC debe estar en estado `Bound`. Si fue eliminado accidentalmente, los datos se pierden y el PVC debe recrearse.

## 10.6 Comandos útiles de diagnóstico rápido

  -----------------------------------------------------------------------
  **Diagnóstico**                          **Comando**
  ---------------------------------------- ------------------------------
  Ver estado del pod                       kubectl get pods -n \<NAMESPACE\>

  Ver logs de Prometheus                   kubectl logs -n \<NAMESPACE\> deployment/prometheus \--tail=50

  Ver eventos del namespace                kubectl get events -n \<NAMESPACE\> \--sort-by=\'.lastTimestamp\'

  Ver detalle de un pod con problemas      kubectl describe pod \<nombre-pod\> -n \<NAMESPACE\>

  Ver el ConfigMap aplicado                kubectl get configmap prometheus-config -n \<NAMESPACE\> -o yaml

  Recargar config sin reiniciar el pod     curl -X POST http://localhost:9090/-/reload

  Ver todos los targets                    curl http://localhost:9090/api/v1/targets

  Reiniciar Prometheus                     kubectl rollout restart deployment/prometheus -n \<NAMESPACE\>
  -----------------------------------------------------------------------

# 11. Exposición del servicio (acceso permanente)

`kubectl port-forward` es válido únicamente para pruebas locales desde la máquina de despliegue. Para que el equipo acceda a Prometheus de forma permanente se requiere uno de los siguientes métodos.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** K3s incluye Traefik como Ingress controller por defecto. No se requiere instalar ningún componente adicional para usar la Opción B.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 11.1 Opción A: NodePort (recomendado para DEV)

Agrega un Service de tipo NodePort al namespace. No reemplaza el Service ClusterIP existente — ambos coexisten.

Crear el archivo `manifests/<NAMESPACE>/06-service-nodeport.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: prometheus-nodeport
  namespace: <NAMESPACE>
  labels:
    app: prometheus
spec:
  type: NodePort
  selector:
    app: prometheus
  ports:
    - port: 9090
      targetPort: 9090
      nodePort: 30090
```

```bash
kubectl apply -f manifests/<NAMESPACE>/06-service-nodeport.yaml
```

Verificar:

```bash
kubectl get svc -n <NAMESPACE>
```

Resultado esperado:

```
NAME                  TYPE        CLUSTER-IP    PORT(S)          AGE
prometheus            ClusterIP   10.x.x.x      9090/TCP         1h
prometheus-nodeport   NodePort    10.x.x.x      9090:30090/TCP   10s
```

Verificar que el servicio tiene endpoints activos:

```bash
kubectl get endpoints prometheus-nodeport -n <NAMESPACE>
```

El resultado debe mostrar una IP en la columna `ENDPOINTS`. Si aparece `<none>`, el selector no está matcheando los pods — verificar los labels reales con `kubectl get pods -n <NAMESPACE> --show-labels`.

Acceder desde cualquier máquina de la red interna:

```
http://<IP_NODO_K3S>:30090
```

## 11.2 Opción B: Ingress con Traefik (recomendado para QA/PROD)

Expone el servicio por nombre DNS. Requiere que el hostname esté registrado en el DNS interno de la organización, o agregado manualmente en `/etc/hosts` de las máquinas del equipo.

Crear el archivo `manifests/<NAMESPACE>/06-ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: prometheus
  namespace: <NAMESPACE>
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: web
spec:
  rules:
    - host: <PROMETHEUS_HOSTNAME>
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: prometheus
                port:
                  number: 9090
```

```bash
kubectl apply -f manifests/<NAMESPACE>/06-ingress.yaml
```

Donde `<PROMETHEUS_HOSTNAME>` según el entorno:

  -----------------------------------------------------------------------
  **Entorno**   **Hostname sugerido**
  ------------- ---------------------------------------------------------
  DEV           prometheus-dev.onp.interno
  QA            prometheus-qa.onp.interno
  PROD          prometheus.onp.interno
  -----------------------------------------------------------------------

Verificar el Ingress:

```bash
kubectl get ingress -n <NAMESPACE>
```

Resultado esperado:

```
NAME         CLASS     HOSTS                           ADDRESS      PORTS   AGE
prometheus   traefik   prometheus-dev.onp.interno      <IP_NODO>    80      10s
```

Acceder desde cualquier máquina de la red (con el hostname resuelto en DNS o `/etc/hosts`):

```
http://prometheus-dev.onp.interno
```

# Anexo A --- Configuración específica para DEV

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para DEV**
  ------------------------- ---------------------------------------------
  Namespace                 prometheus-dev

  Cluster                   Cluster compartido DEV/QA

  Réplicas                  1

  OTEL Collector URL        otel-collector.otel-dev.svc.cluster.local:8889

  Retención de métricas     15d

  Tamaño del PVC            5Gi
  -----------------------------------------------------------------------

## A.1 Crear el namespace DEV

```bash
kubectl create namespace prometheus-dev
kubectl label namespace prometheus-dev \
  app.kubernetes.io/managed-by=oti-onp \
  environment=dev \
  team=oti-onp
```

## A.2 Valores del ConfigMap para DEV

Reemplazar los placeholders del ConfigMap (Sección 6.2) con los siguientes valores:

  -----------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- ---------------------------------------------
  \<NAMESPACE\>         prometheus-dev

  \<NAMESPACE_OTEL\>    otel-dev
  -----------------------------------------------------------------------

El bloque `otel-collector` del scrape config queda así para DEV:

```yaml
- job_name: 'otel-collector'
  static_configs:
    - targets: ['otel-collector.otel-dev.svc.cluster.local:8889']
  metrics_path: /metrics
```

## A.3 Valores del PVC para DEV

  -----------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      5Gi
  -----------------------------------------------------------------------

## A.4 Recursos recomendados para DEV

Reemplazar los placeholders del Deployment (Sección 8.1) con los siguientes valores:

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             prom/prometheus:v2.53.0 (Escenario B)

  \<RETENTION_TIME\>    15d

  \<CPU_REQUEST\>       100m

  \<CPU_LIMIT\>         300m

  \<MEMORY_REQUEST\>    128Mi

  \<MEMORY_LIMIT\>      256Mi
  ------------------------------------------------------------------------------------------

## A.5 URL de Prometheus para Grafana en DEV

```
http://prometheus.prometheus-dev.svc.cluster.local:9090
```

# Anexo B --- Configuración específica para QA

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para QA**
  ------------------------- ---------------------------------------------
  Namespace                 prometheus-qa

  Cluster                   Cluster compartido DEV/QA

  Réplicas                  1

  OTEL Collector URL        otel-collector.otel-qa.svc.cluster.local:8889

  Retención de métricas     30d

  Tamaño del PVC            10Gi
  -----------------------------------------------------------------------

## B.1 Crear el namespace QA

```bash
kubectl create namespace prometheus-qa
kubectl label namespace prometheus-qa \
  app.kubernetes.io/managed-by=oti-onp \
  environment=qa \
  team=oti-onp
```

## B.2 Valores del ConfigMap para QA

  -----------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- ---------------------------------------------
  \<NAMESPACE\>         prometheus-qa

  \<NAMESPACE_OTEL\>    otel-qa
  -----------------------------------------------------------------------

## B.3 Valores del PVC para QA

  -----------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      10Gi
  -----------------------------------------------------------------------

## B.4 Recursos recomendados para QA

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             \<GITLAB_REGISTRY_URL\>/observabilidad/prometheus:v2.53.0

  \<RETENTION_TIME\>    30d

  \<CPU_REQUEST\>       150m

  \<CPU_LIMIT\>         400m

  \<MEMORY_REQUEST\>    256Mi

  \<MEMORY_LIMIT\>      512Mi
  ------------------------------------------------------------------------------------------

## B.5 URL de Prometheus para Grafana en QA

```
http://prometheus.prometheus-qa.svc.cluster.local:9090
```

# Anexo C --- Configuración específica para PROD

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para PROD**
  ------------------------- ---------------------------------------------
  Namespace                 prometheus

  Cluster                   Cluster exclusivo de producción

  Réplicas                  1

  OTEL Collector URL        otel-collector.otel.svc.cluster.local:8889

  Retención de métricas     90d

  Tamaño del PVC            30Gi
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Prometheus no es stateless — almacena su base de datos TSDB en el PVC. Por esta razón no aplica HPA. Para alta disponibilidad en PROD se requeriría una solución de almacenamiento distribuido como Thanos, que está fuera del alcance de este stack.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## C.1 Crear el namespace PROD

```bash
kubectl create namespace prometheus
kubectl label namespace prometheus \
  app.kubernetes.io/managed-by=oti-onp \
  environment=prod \
  team=oti-onp
```

## C.2 Valores del ConfigMap para PROD

  -----------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- ---------------------------------------------
  \<NAMESPACE\>         prometheus

  \<NAMESPACE_OTEL\>    otel
  -----------------------------------------------------------------------

## C.3 Valores del PVC para PROD

  -----------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      30Gi
  -----------------------------------------------------------------------

## C.4 Recursos recomendados para PROD

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             \<GITLAB_REGISTRY_URL\>/observabilidad/prometheus:v2.53.0

  \<RETENTION_TIME\>    90d

  \<CPU_REQUEST\>       300m

  \<CPU_LIMIT\>         800m

  \<MEMORY_REQUEST\>    512Mi

  \<MEMORY_LIMIT\>      1Gi
  ------------------------------------------------------------------------------------------

## C.5 URL de Prometheus para Grafana en PROD

```
http://prometheus.prometheus.svc.cluster.local:9090
```

# Anexo D --- Air-gap: transferencia de imagen sin internet ni registry

Este anexo aplica al **Escenario C**: el nodo no tiene acceso a internet y no hay un registry privado disponible. La imagen se transfiere manualmente como archivo `.tar`.

## D.1 Exportar la imagen en una máquina con internet

```bash
# Descargar la imagen
docker pull prom/prometheus:v2.53.0

# Exportar a archivo .tar
docker save prom/prometheus:v2.53.0 \
  -o prometheus-v2.53.0.tar
```

## D.2 Transferir el archivo al nodo

```bash
scp prometheus-v2.53.0.tar <USUARIO>@<IP_NODO>:/tmp/
```

## D.3 Importar la imagen en el nodo

Ejecutar según el runtime del cluster:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images import /tmp/prometheus-v2.53.0.tar

# Opción B — K3s
sudo k3s ctr images import /tmp/prometheus-v2.53.0.tar
```

Verificar que la imagen quedó disponible:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images ls | grep prometheus

# Opción B — K3s
sudo k3s ctr images list | grep prometheus
```

## D.4 Usar la imagen importada en el Deployment

En el Deployment (Sección 8.1), usar:

```yaml
image: docker.io/prom/prometheus:v2.53.0
```

Mantener el bloque `imagePullSecrets` comentado.
