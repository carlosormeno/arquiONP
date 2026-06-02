**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Grafana en Kubernetes (K3s)**

Usando manifests Kubernetes directos con auto-provisionamiento de datasources y dashboards

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

Este manual describe el proceso completo de instalación de Grafana en un cluster Kubernetes K3s on-premise, utilizando manifests Kubernetes directos con auto-provisionamiento de datasources y dashboards vía ConfigMap.

El documento está dirigido al equipo de Plataforma de la OTI --- ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Grafana?

Grafana es una plataforma de visualización y análisis de datos de código abierto. Permite crear dashboards interactivos consultando múltiples fuentes de datos (datasources) simultáneamente. En el stack ONP actúa como la interfaz unificada de observabilidad donde el equipo puede correlacionar métricas, trazas y logs.

## 1.3 Rol de Grafana en el stack ONP

Grafana se conecta a los tres backends del stack como datasources de solo lectura:

```
Grafana (namespace: grafana-dev/qa/grafana)
    ├── Prometheus  → métricas de los servicios
    ├── Jaeger      → trazas distribuidas
    └── Elasticsearch → logs de los servicios
```

Grafana no recibe datos directamente de los servicios --- consulta a los backends que ya los tienen almacenados.

## 1.4 Auto-provisionamiento

Grafana permite pre-configurar datasources y dashboards mediante archivos YAML y JSON montados como ConfigMaps. Al arrancar, Grafana los carga automáticamente sin intervención manual. Esto garantiza que cualquier reinstalación o nuevo entorno quede configurado de forma idéntica y reproducible.

## 1.5 Credenciales y Secrets de Kubernetes

Las credenciales de Grafana (usuario administrador, contraseña de Elasticsearch) se almacenan en Kubernetes Secrets. Un Secret en Kubernetes:

-   Almacena datos sensibles codificados en base64, separados de los manifests de configuración.
-   Es accesible únicamente dentro del cluster y solo a los pods que lo referencia explícitamente.
-   Su acceso está controlado por RBAC de Kubernetes.

Los Secrets de Kubernetes son el mecanismo estándar para manejar credenciales en entornos on-premise con K3s. No reemplazan a un sistema dedicado como HashiCorp Vault (que añade cifrado en reposo, rotación automática y audit log), pero son suficientes para el stack ONP interno.

## 1.6 Alcance de este manual

Este manual cubre:

-   Preparación del namespace de cada entorno.
-   Creación de Secrets para credenciales de administrador y Elasticsearch.
-   Auto-provisionamiento de datasources (Prometheus, Jaeger, Elasticsearch).
-   Auto-provisionamiento de un dashboard básico de estado de targets.
-   Despliegue de Grafana (PVC, Deployment y Service).
-   Verificación del despliegue y de los datasources.
-   Troubleshooting de errores comunes.

Queda fuera del alcance:

-   Creación de dashboards adicionales (responsabilidad del equipo de desarrollo y operaciones).
-   Configuración de alertas y notificaciones.
-   Integración con sistemas de autenticación externos (LDAP, OAuth).

# 2. Prerrequisitos

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Verificar TODOS los prerrequisitos antes de comenzar la instalación.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 2.1 Infraestructura requerida

  --------------------------------------------------------------------------------------------------
  **Componente**          **Requisito**                  **Observación**
  ----------------------- ------------------------------ -------------------------------------------
  Kubernetes              K3s v1.27 o superior           Verificar con: kubectl version \--short

  Prometheus              Instalado y operativo          Namespace prometheus-\*/prometheus

  Jaeger Query            Instalado y operativo          Namespace observabilidad-\*/observabilidad

  Elasticsearch           Instalado y operativo          Namespace elastic-\*/elastic

  StorageClass            local-path (K3s por defecto)   Verificar con: kubectl get storageclass
  --------------------------------------------------------------------------------------------------

## 2.2 Herramientas en la máquina de despliegue

  -----------------------------------------------------------------------------------
  **Herramienta**   **Versión mínima**           **Verificación**
  ----------------- ---------------------------- ------------------------------------
  kubectl           v1.27+                       kubectl version \--client

  curl              Cualquier versión reciente   curl \--version
  -----------------------------------------------------------------------------------

## 2.3 Información a recopilar antes de comenzar

  -----------------------------------------------------------------------------------------------------------------------
  **Placeholder**               **Descripción**                                    **Valor real (completar)**
  ----------------------------- -------------------------------------------------- ----------------------------
  \<GITLAB_REGISTRY_URL\>       URL base del GitLab Registry

  \<NAMESPACE\>                 Namespace de Grafana                                grafana-dev / grafana-qa / grafana

  \<NAMESPACE_PROMETHEUS\>      Namespace de Prometheus                             prometheus-dev / prometheus-qa / prometheus

  \<NAMESPACE_JAEGER\>          Namespace de Jaeger                                 observabilidad-dev / observabilidad-qa / observabilidad

  \<NAMESPACE_ES\>              Namespace de Elasticsearch                          elastic-dev / elastic-qa / elastic

  \<GRAFANA_VERSION\>           Versión de Grafana                                  Ver sección 3.1

  \<GF_ADMIN_USER\>             Usuario administrador de Grafana                    admin (recomendado)

  \<GF_ADMIN_PASSWORD\>         Contraseña del administrador de Grafana

  \<GF_ES_USERNAME\>            Usuario de Elasticsearch para el datasource

  \<GF_ES_PASSWORD\>            Contraseña del usuario de Elasticsearch

  \<LOGS_INDEX\>                Patrón de índice de logs en Elasticsearch           onp-logs-\*
  -----------------------------------------------------------------------------------------------------------------------

## 2.4 Verificaciones previas

### 2.4.1 Verificar que Prometheus está operativo

```bash
kubectl get pods -n <NAMESPACE_PROMETHEUS>
kubectl get svc -n <NAMESPACE_PROMETHEUS>
```

Resultado esperado:

```
NAME                        READY   STATUS    RESTARTS
prometheus-xxxxxxxxx-xxxxx  1/1     Running   0
```

```
NAME         TYPE        CLUSTER-IP    PORT(S)
prometheus   ClusterIP   10.x.x.x      9090/TCP
```

### 2.4.2 Verificar que Jaeger Query está operativo

```bash
kubectl get pods -n <NAMESPACE_JAEGER>
kubectl get svc -n <NAMESPACE_JAEGER> jaeger-onp-query
```

Resultado esperado:

```
NAME                                  READY   STATUS    RESTARTS
jaeger-onp-query-xxxxxxxxx-xxxxx      2/2     Running   0
```

```
NAME               TYPE        CLUSTER-IP    PORT(S)
jaeger-onp-query   ClusterIP   10.x.x.x      16686/TCP,16685/TCP,16687/TCP
```

### 2.4.3 Verificar que Elasticsearch está operativo

```bash
kubectl get pods -n <NAMESPACE_ES>
```

Resultado esperado:

```
NAME                           READY   STATUS    RESTARTS
elasticsearch-xxxxxxxxx-xxxxx  1/1     Running   0
```

# 3. Preparación de imágenes

## Nota: ¿Qué camino seguir?

  -----------------------------------------------------------------------
  **Escenario**                  **Cuándo usarlo**                                      **Ir a**
  ------------------------------ ------------------------------------------------------ ----------
  A --- GitLab Registry          PROD y QA. Sin salida a internet desde el cluster.      Sección 3.2

  B --- Internet directo         DEV y pruebas. El nodo tiene acceso a internet.        Sección 3.3

  C --- Air-gap                  Sin internet ni registry privado.                       Anexo D
  -----------------------------------------------------------------------

## 3.1 Imagen requerida

  -----------------------------------------------------------------------------------------------
  **Componente**   **Imagen original**    **Versión**
  ---------------- ---------------------- --------------------
  Grafana          grafana/grafana        11.1.0
  -----------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Verificar la versión estable más reciente en https://github.com/grafana/grafana/releases antes de ejecutar el mirroring. Usar siempre una versión fija (no `latest`).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 3.2 Escenario A --- GitLab Registry (producción)

```bash
docker login <GITLAB_REGISTRY_URL>

docker pull grafana/grafana:<GRAFANA_VERSION>

docker tag grafana/grafana:<GRAFANA_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/grafana:<GRAFANA_VERSION>

docker push <GITLAB_REGISTRY_URL>/observabilidad/grafana:<GRAFANA_VERSION>
```

La imagen en el registry privado queda disponible en:

```
<GITLAB_REGISTRY_URL>/observabilidad/grafana:<GRAFANA_VERSION>
```

## 3.3 Escenario B --- Internet directo (pruebas y DEV)

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** No se requieren pasos de mirroring. Pasar directamente a la Sección 4. En el Deployment (Sección 7.1) usar la imagen pública directamente y mantener `imagePullSecrets` comentado.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **Componente**   **Imagen a usar directamente**
  ---------------- -------------------------------------------
  Grafana          grafana/grafana:11.1.0
  -----------------------------------------------------------------------

## 3.4 Escenario C --- Air-gap

Ver **Anexo D** para el procedimiento completo de transferencia de imagen vía archivo `.tar`.

# 4. Preparación del namespace

## 4.1 Crear el namespace

```bash
kubectl create namespace <NAMESPACE>
```

Donde \<NAMESPACE\> es:

-   `grafana-dev` --- para el entorno de desarrollo.
-   `grafana-qa` --- para el entorno de calidad.
-   `grafana` --- para el entorno de producción.

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
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A**. En los Escenarios B y C omitir y continuar en la Sección 4.4.
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
NAME          STATUS   AGE   LABELS
grafana-dev   Active   Xs    app.kubernetes.io/managed-by=oti-onp,environment=dev,team=oti-onp
```

# 5. Creación de Secrets

Grafana necesita dos Secrets: uno para las credenciales del administrador y otro para las credenciales de Elasticsearch usadas en el datasource.

  -----------------------------------------------------------------------
  **🔴 IMPORTANTE:** Nunca incluir contraseñas en texto plano en archivos YAML que se suban a repositorios de código. Los Secrets deben crearse únicamente mediante comandos kubectl.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 5.1 Secret de credenciales del administrador de Grafana

```bash
kubectl create secret generic grafana-admin-credentials \
  --from-literal=GF_SECURITY_ADMIN_USER=<GF_ADMIN_USER> \
  --from-literal=GF_SECURITY_ADMIN_PASSWORD=<GF_ADMIN_PASSWORD> \
  --namespace=<NAMESPACE>
```

## 5.2 Secret de credenciales de Elasticsearch para el datasource

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** `<GF_ES_USERNAME>` y `<GF_ES_PASSWORD>` son las credenciales del usuario de Elasticsearch creadas durante la instalación de ese componente (ver Manual de Instalación de Jaeger). Si no las tienes a mano, solicítalas al responsable de la instalación de Elasticsearch o recupéralas con el siguiente comando (requiere acceso al namespace de Jaeger):

  ```bash
  kubectl get secret jaeger-es-credentials -n <NAMESPACE_JAEGER> \
    -o jsonpath='{.data.ES_PASSWORD}' | base64 -d
  ```

  Donde `<NAMESPACE_JAEGER>` es el namespace donde está instalado Jaeger (`observabilidad-dev`, `observabilidad-qa` u `observabilidad` según el entorno). Ver Sección 2.3.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

```bash
kubectl create secret generic grafana-es-credentials \
  --from-literal=GF_ES_USERNAME=<GF_ES_USERNAME> \
  --from-literal=GF_ES_PASSWORD=<GF_ES_PASSWORD> \
  --namespace=<NAMESPACE>
```

## 5.3 Verificar los Secrets

```bash
kubectl get secrets -n <NAMESPACE>
```

Resultado esperado:

```
NAME                       TYPE     DATA   AGE
grafana-admin-credentials  Opaque   2      10s
grafana-es-credentials     Opaque   2      10s
```

# 6. Auto-provisionamiento de datasources (ConfigMap)

Grafana carga automáticamente los datasources definidos en archivos YAML ubicados en `/etc/grafana/provisioning/datasources/`. Este ConfigMap define los tres datasources del stack ONP.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Los valores `${GF_ES_USERNAME}` y `${GF_ES_PASSWORD}` en el ConfigMap **no son placeholders a reemplazar**. Es la sintaxis de Grafana para leer variables de entorno en tiempo de ejecución. Grafana los resolverá desde el Secret `grafana-es-credentials` inyectado en el Deployment (Sección 7.1).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 6.1 Crear el ConfigMap de datasources

Crear el archivo `manifests/<NAMESPACE>/02-configmap-datasources.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-datasources
  namespace: <NAMESPACE>
data:
  datasources.yaml: |
    apiVersion: 1
    datasources:
      - name: Prometheus
        type: prometheus
        uid: prometheus
        url: http://prometheus.<NAMESPACE_PROMETHEUS>.svc.cluster.local:9090
        access: proxy
        isDefault: true
        jsonData:
          timeInterval: 15s

      - name: Jaeger
        type: jaeger
        uid: jaeger
        url: http://jaeger-onp-query.<NAMESPACE_JAEGER>.svc.cluster.local:16686
        access: proxy

      - name: Elasticsearch
        type: elasticsearch
        uid: elasticsearch
        url: http://elasticsearch.<NAMESPACE_ES>.svc.cluster.local:9200
        access: proxy
        basicAuth: true
        basicAuthUser: ${GF_ES_USERNAME}
        secureJsonData:
          basicAuthPassword: ${GF_ES_PASSWORD}
        jsonData:
          index: <LOGS_INDEX>
          timeField: "@timestamp"
          esVersion: "8.0.0"  # String de compatibilidad de API de Grafana para ES 8.x. No reemplazar con la versión exacta (8.19.15).
          logMessageField: message
          logLevelField: log.level
```

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Antes de aplicar, reemplazar **todos** los placeholders del archivo. En particular:

  - `<NAMESPACE>` → namespace de Grafana (`grafana-dev`, `grafana-qa` o `grafana`)
  - `<NAMESPACE_PROMETHEUS>` → namespace de Prometheus (ej. `prometheus-dev`)
  - `<NAMESPACE_JAEGER>` → namespace de Jaeger (ej. `observabilidad-dev`)
  - `<NAMESPACE_ES>` → namespace de Elasticsearch (ej. `elastic-dev`)
  - `<LOGS_INDEX>` → patrón de índice de logs en Elasticsearch (ej. `onp-logs-*`)

  Los valores `${GF_ES_USERNAME}` y `${GF_ES_PASSWORD}` **no son placeholders** — deben quedar exactamente así (ver nota al inicio de la Sección 6).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 6.2 Aplicar el ConfigMap de datasources

```bash
kubectl apply -f manifests/<NAMESPACE>/02-configmap-datasources.yaml
```

Resultado esperado:

```
configmap/grafana-datasources created
```

# 7. Auto-provisionamiento de dashboards (ConfigMap)

Este ConfigMap contiene dos elementos: la configuración del proveedor de dashboards (que indica a Grafana dónde buscar los archivos JSON) y el dashboard básico de estado de targets de Prometheus.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El dashboard provisionado mediante ConfigMap aparece automáticamente en Grafana al arrancar y no puede editarse desde la UI (está marcado como read-only). Para modificarlo hay que actualizar el ConfigMap y hacer rollout del Deployment. Los dashboards creados manualmente por el equipo en la UI sí son editables y se persisten en el PVC.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 7.1 Crear el ConfigMap de dashboards

Crear el archivo `manifests/<NAMESPACE>/03-configmap-dashboards.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grafana-dashboards
  namespace: <NAMESPACE>
data:
  provider.yaml: |
    apiVersion: 1
    providers:
      - name: onp-dashboards
        orgId: 1
        folder: ONP
        type: file
        disableDeletion: true
        updateIntervalSeconds: 30
        options:
          path: /var/lib/grafana/dashboards

  onp-prometheus-targets.json: |
    {
      "title": "ONP - Estado de Targets Prometheus",
      "uid": "onp-prometheus-targets",
      "tags": ["prometheus", "onp", "observabilidad"],
      "schemaVersion": 38,
      "version": 1,
      "panels": [
        {
          "type": "stat",
          "title": "Targets UP",
          "gridPos": {"h": 4, "w": 6, "x": 0, "y": 0},
          "datasource": {"type": "prometheus", "uid": "prometheus"},
          "targets": [{"expr": "count(up == 1)", "legendFormat": "UP"}],
          "options": {"colorMode": "background"},
          "fieldConfig": {
            "defaults": {
              "color": {"mode": "thresholds"},
              "thresholds": {
                "mode": "absolute",
                "steps": [{"color": "red", "value": null}, {"color": "green", "value": 1}]
              }
            }
          }
        },
        {
          "type": "stat",
          "title": "Targets DOWN",
          "gridPos": {"h": 4, "w": 6, "x": 6, "y": 0},
          "datasource": {"type": "prometheus", "uid": "prometheus"},
          "targets": [{"expr": "count(up == 0) or vector(0)", "legendFormat": "DOWN"}],
          "options": {"colorMode": "background"},
          "fieldConfig": {
            "defaults": {
              "color": {"mode": "thresholds"},
              "thresholds": {
                "mode": "absolute",
                "steps": [{"color": "green", "value": null}, {"color": "red", "value": 1}]
              }
            }
          }
        },
        {
          "type": "table",
          "title": "Estado de Targets",
          "gridPos": {"h": 8, "w": 24, "x": 0, "y": 4},
          "datasource": {"type": "prometheus", "uid": "prometheus"},
          "targets": [{"expr": "up", "instant": true, "format": "table"}],
          "transformations": [
            {
              "id": "organize",
              "options": {
                "renameByName": {"job": "Job", "instance": "Instancia", "Value": "Estado"}
              }
            }
          ],
          "fieldConfig": {
            "overrides": [
              {
                "matcher": {"id": "byName", "options": "Estado"},
                "properties": [
                  {"id": "custom.displayMode", "value": "color-background"},
                  {"id": "mappings", "value": [
                    {"type": "value", "options": {
                      "0": {"color": "red", "text": "DOWN"},
                      "1": {"color": "green", "text": "UP"}
                    }}
                  ]},
                  {"id": "thresholds", "value": {
                    "mode": "absolute",
                    "steps": [{"color": "red", "value": null}, {"color": "green", "value": 1}]
                  }}
                ]
              }
            ]
          }
        },
        {
          "type": "timeseries",
          "title": "Duración del Scrape por Job",
          "gridPos": {"h": 8, "w": 24, "x": 0, "y": 12},
          "datasource": {"type": "prometheus", "uid": "prometheus"},
          "targets": [{"expr": "scrape_duration_seconds", "legendFormat": "{{job}}"}],
          "fieldConfig": {"defaults": {"unit": "s"}}
        }
      ]
    }
```

## 7.2 Aplicar el ConfigMap de dashboards

```bash
kubectl apply -f manifests/<NAMESPACE>/03-configmap-dashboards.yaml
```

Resultado esperado:

```
configmap/grafana-dashboards created
```

# 8. PersistentVolumeClaim

Grafana almacena su base de datos SQLite (usuarios, dashboards creados manualmente, preferencias) en `/var/lib/grafana`. Esta información debe persistir entre reinicios del pod.

Al igual que con Prometheus, el ciclo de vida del PVC es **independiente** del pod. Reiniciar el pod no pierde los datos. Solo se pierden si el PVC es eliminado explícitamente.

## 8.1 Crear el PVC

Crear el archivo `manifests/<NAMESPACE>/04-pvc.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: grafana-data
  namespace: <NAMESPACE>
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: <STORAGE_SIZE>
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El valor de `<STORAGE_SIZE>` está definido en cada anexo de entorno: Anexo A.3 (DEV: `2Gi`), Anexo B.3 (QA: `2Gi`) y Anexo C.3 (PROD: `5Gi`).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 8.2 Aplicar el PVC

```bash
kubectl apply -f manifests/<NAMESPACE>/04-pvc.yaml
```

Resultado esperado:

```
persistentvolumeclaim/grafana-data created
```

## 8.3 Verificar el PVC

```bash
kubectl get pvc -n <NAMESPACE>
```

Resultado esperado:

```
NAME           STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS
grafana-data   Pending                                      local-path
```

El estado `Pending` es normal hasta que el pod arranque y monte el volumen.

# 9. Despliegue de Grafana

## 9.1 Crear el Deployment

Crear el archivo `manifests/<NAMESPACE>/05-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: <NAMESPACE>
  labels:
    app: grafana
    environment: <ENV>
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      labels:
        app: grafana
        environment: <ENV>
    spec:
      containers:
        - name: grafana
          image: <IMAGE>
          ports:
            - name: http
              containerPort: 3000
          env:
            - name: GF_SECURITY_ADMIN_USER
              valueFrom:
                secretKeyRef:
                  name: grafana-admin-credentials
                  key: GF_SECURITY_ADMIN_USER
            - name: GF_SECURITY_ADMIN_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: grafana-admin-credentials
                  key: GF_SECURITY_ADMIN_PASSWORD
            - name: GF_ES_USERNAME
              valueFrom:
                secretKeyRef:
                  name: grafana-es-credentials
                  key: GF_ES_USERNAME
            - name: GF_ES_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: grafana-es-credentials
                  key: GF_ES_PASSWORD
          volumeMounts:
            - name: data
              mountPath: /var/lib/grafana
            - name: datasources
              mountPath: /etc/grafana/provisioning/datasources
            - name: dashboard-provider
              mountPath: /etc/grafana/provisioning/dashboards
            - name: dashboards
              mountPath: /var/lib/grafana/dashboards
          resources:
            requests:
              cpu: "<CPU_REQUEST>"
              memory: "<MEMORY_REQUEST>"
            limits:
              cpu: "<CPU_LIMIT>"
              memory: "<MEMORY_LIMIT>"
          readinessProbe:
            httpGet:
              path: /api/health
              port: 3000
            initialDelaySeconds: 15
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /api/health
              port: 3000
            initialDelaySeconds: 30
            periodSeconds: 20
      volumes:
        - name: data
          persistentVolumeClaim:
            claimName: grafana-data
        - name: datasources
          configMap:
            name: grafana-datasources
        - name: dashboard-provider
          configMap:
            name: grafana-dashboards
            items:
              - key: provider.yaml
                path: provider.yaml
        - name: dashboards
          configMap:
            name: grafana-dashboards
            items:
              - key: onp-prometheus-targets.json
                path: onp-prometheus-targets.json
      # Escenario A únicamente (GitLab Registry privado):
      # imagePullSecrets:
      #   - name: gitlab-registry-secret
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El placeholder `<IMAGE>` debe reemplazarse con la imagen correspondiente al escenario: Escenario A: `<GITLAB_REGISTRY_URL>/observabilidad/grafana:<GRAFANA_VERSION>`. Escenario B: `grafana/grafana:11.1.0`. Escenario C: `docker.io/grafana/grafana:11.1.0`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Antes de aplicar, verificar que todos los placeholders del archivo fueron reemplazados, incluyendo `<IMAGE>`, `<ENV>`, `<CPU_REQUEST>`, `<CPU_LIMIT>`, `<MEMORY_REQUEST>` y `<MEMORY_LIMIT>`. Ver Anexos A/B/C para los valores por entorno.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 9.2 Aplicar el Deployment

```bash
kubectl apply -f manifests/<NAMESPACE>/05-deployment.yaml
```

Resultado esperado:

```
deployment.apps/grafana created
```

## 9.3 Crear el Service

Crear el archivo `manifests/<NAMESPACE>/06-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: grafana
  namespace: <NAMESPACE>
  labels:
    app: grafana
spec:
  selector:
    app: grafana
  ports:
    - name: http
      port: 3000
      targetPort: 3000
```

## 9.4 Aplicar el Service

```bash
kubectl apply -f manifests/<NAMESPACE>/06-service.yaml
```

Resultado esperado:

```
service/grafana created
```

## 9.5 Verificar el despliegue

```bash
kubectl get pods -n <NAMESPACE> -w
```

Resultado esperado (esperar entre 20 y 40 segundos):

```
NAME                       READY   STATUS    RESTARTS   AGE
grafana-xxxxxxxxx-xxxxx    1/1     Running   0          30s
```

```bash
kubectl get pvc -n <NAMESPACE>
```

Resultado esperado:

```
NAME           STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS
grafana-data   Bound    pvc-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx   2Gi        RWO            local-path
```

```bash
kubectl get services -n <NAMESPACE>
```

Resultado esperado:

```
NAME      TYPE        CLUSTER-IP    PORT(S)
grafana   ClusterIP   10.x.x.x      3000/TCP
```

# 10. Verificación del despliegue

## 10.1 Verificar los logs de Grafana

```bash
kubectl logs -n <NAMESPACE> deployment/grafana --tail=30
```

Al iniciar correctamente, Grafana muestra líneas similares a:

```
logger=settings t=... msg="Starting Grafana" version=11.1.0
logger=datasources t=... msg="Initializing datasourceQueryService"
logger=provisioning.dashboard t=... msg="Starting dashboard provisioner" name=onp-dashboards
logger=http.server t=... msg="HTTP Server Listen" address=[::]:3000
```

## 10.2 Acceder a la UI de Grafana

```bash
kubectl port-forward -n <NAMESPACE> svc/grafana 3000:3000 &
```

Esperar a que aparezca el mensaje `Forwarding from 127.0.0.1:3000 -> 3000`. Luego abrir en el navegador:

```
http://localhost:3000
```

Iniciar sesión con las credenciales del Secret `grafana-admin-credentials` (`<GF_ADMIN_USER>` / `<GF_ADMIN_PASSWORD>`).

## 10.3 Verificar los datasources

En la UI de Grafana (versión 11), navegar a **Connections → Data sources** en la barra lateral izquierda. El subítem **Data sources** muestra la lista de datasources existentes — no hacer clic en "Add new connection". También se puede acceder directamente por URL:

```
http://localhost:3000/connections/datasources
```

Resultado esperado: tres datasources configurados automáticamente:

  -----------------------------------------------------------------------
  **Datasource**   **Tipo**        **Estado esperado**
  ---------------- --------------- ------------------------------------
  Prometheus       prometheus      Marcado como default (estrella)
  Jaeger           jaeger          Visible en la lista
  Elasticsearch    elasticsearch   Visible en la lista
  -----------------------------------------------------------------------

Hacer clic en cada datasource y usar el botón **Save & test** para confirmar la conectividad.

Resultado esperado para cada uno:

```
Data source connected and labels found.   ← Prometheus
Data source connected.                    ← Jaeger
Index OK. Time field name OK.             ← Elasticsearch
```

## 10.4 Verificar el dashboard provisionado

En la UI de Grafana, navegar a **Dashboards**. Debe aparecer la carpeta **ONP** con el dashboard:

```
ONP - Estado de Targets Prometheus
```

Abrir el dashboard. Resultado esperado:

-   Panel **Targets UP**: muestra `2` en verde (prometheus y otel-collector).
-   Panel **Targets DOWN**: muestra `0` en verde.
-   Tabla **Estado de Targets**: dos filas, ambas con estado **UP** en verde.
-   Gráfico **Duración del Scrape**: dos líneas (prometheus y otel-collector).

## 10.5 Verificar el datasource de Prometheus via API

```bash
curl -u <GF_ADMIN_USER>:<GF_ADMIN_PASSWORD> \
  http://localhost:3000/api/datasources
```

Resultado esperado: un JSON con los tres datasources (`Prometheus`, `Jaeger`, `Elasticsearch`).

# 11. Troubleshooting

## 11.1 CrashLoopBackOff al arrancar

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME                    READY   STATUS             RESTARTS
grafana-xxxxx           0/1     CrashLoopBackOff   3
```

### Causas y soluciones

-   Algún placeholder no fue reemplazado en el Deployment: verificar los logs con `kubectl logs -n <NAMESPACE> deployment/grafana`.
-   El Secret `grafana-admin-credentials` o `grafana-es-credentials` no existe: verificar con `kubectl get secrets -n <NAMESPACE>`.
-   El PVC no se pudo crear: verificar con `kubectl describe pvc grafana-data -n <NAMESPACE>`.

## 11.2 Datasource Prometheus no conecta

### Síntoma

Al hacer **Save & test** en el datasource Prometheus, aparece: `Post "http://prometheus...": dial tcp: no such host`.

### Causas y soluciones

-   El nombre del namespace de Prometheus en el ConfigMap es incorrecto: verificar que `<NAMESPACE_PROMETHEUS>` coincide con el namespace real con `kubectl get svc -n <NAMESPACE_PROMETHEUS>`.
-   Prometheus no está corriendo: verificar con `kubectl get pods -n <NAMESPACE_PROMETHEUS>`.

## 11.3 Datasource Elasticsearch muestra error de autenticación

### Síntoma

Al hacer **Save & test** en el datasource Elasticsearch, aparece: `401 Unauthorized`.

### Causas y soluciones

-   Las credenciales del Secret `grafana-es-credentials` son incorrectas: verificar el usuario y contraseña con `kubectl get secret grafana-es-credentials -n <NAMESPACE> -o jsonpath='{.data.GF_ES_USERNAME}' | base64 -d`.
-   Las variables de entorno `GF_ES_USERNAME` y `GF_ES_PASSWORD` no están siendo inyectadas: verificar con `kubectl exec -n <NAMESPACE> deployment/grafana -- env | grep GF_ES`.

## 11.4 El dashboard provisionado no aparece

### Síntoma

La carpeta ONP no aparece en Dashboards tras iniciar Grafana.

### Causas y soluciones

-   El ConfigMap `grafana-dashboards` no fue aplicado: verificar con `kubectl get configmap grafana-dashboards -n <NAMESPACE>`.
-   Error en el JSON del dashboard: revisar los logs de Grafana buscando `provisioning.dashboard`.
-   Grafana aún no terminó de cargar los dashboards: esperar 30 segundos y recargar la página.

## 11.5 Comandos útiles de diagnóstico rápido

  -----------------------------------------------------------------------
  **Diagnóstico**                          **Comando**
  ---------------------------------------- ------------------------------
  Ver estado del pod                       kubectl get pods -n \<NAMESPACE\>

  Ver logs de Grafana                      kubectl logs -n \<NAMESPACE\> deployment/grafana \--tail=50

  Ver eventos del namespace                kubectl get events -n \<NAMESPACE\> \--sort-by=\'.lastTimestamp\'

  Ver variables de entorno del pod         kubectl exec -n \<NAMESPACE\> deployment/grafana \-- env \| grep GF

  Reiniciar Grafana                        kubectl rollout restart deployment/grafana -n \<NAMESPACE\>

  Verificar datasources via API            curl -u admin:\<PASSWORD\> http://localhost:3000/api/datasources

  Recargar dashboards provisionados        curl -X POST -u admin:\<PASSWORD\> http://localhost:3000/api/admin/provisioning/dashboards/reload
  -----------------------------------------------------------------------

# 12. Exposición del servicio (acceso permanente)

`kubectl port-forward` es válido únicamente para pruebas locales desde la máquina de despliegue. Para que el equipo acceda a Grafana de forma permanente se requiere uno de los siguientes métodos.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** K3s incluye Traefik como Ingress controller por defecto. No se requiere instalar ningún componente adicional para usar la Opción B.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 12.1 Opción A: NodePort (recomendado para DEV)

Agrega un Service de tipo NodePort al namespace. No reemplaza el Service ClusterIP existente — ambos coexisten.

Crear el archivo `manifests/<NAMESPACE>/07-service-nodeport.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: grafana-nodeport
  namespace: <NAMESPACE>
  labels:
    app: grafana
spec:
  type: NodePort
  selector:
    app: grafana
  ports:
    - port: 3000
      targetPort: 3000
      nodePort: 30300
```

```bash
kubectl apply -f manifests/<NAMESPACE>/07-service-nodeport.yaml
```

Verificar:

```bash
kubectl get svc -n <NAMESPACE>
```

Resultado esperado:

```
NAME               TYPE        CLUSTER-IP    PORT(S)          AGE
grafana            ClusterIP   10.x.x.x      3000/TCP         1h
grafana-nodeport   NodePort    10.x.x.x      3000:30300/TCP   10s
```

Verificar que el servicio tiene endpoints activos:

```bash
kubectl get endpoints grafana-nodeport -n <NAMESPACE>
```

El resultado debe mostrar una IP en la columna `ENDPOINTS`. Si aparece `<none>`, el selector no está matcheando los pods — verificar los labels reales con `kubectl get pods -n <NAMESPACE> --show-labels`.

Acceder desde cualquier máquina de la red interna:

```
http://<IP_NODO_K3S>:30300
```

## 12.2 Opción B: Ingress con Traefik (recomendado para QA/PROD)

Expone el servicio por nombre DNS. Requiere que el hostname esté registrado en el DNS interno de la organización, o agregado manualmente en `/etc/hosts` de las máquinas del equipo.

Crear el archivo `manifests/<NAMESPACE>/07-ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: grafana
  namespace: <NAMESPACE>
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: web
spec:
  rules:
    - host: <GRAFANA_HOSTNAME>
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: grafana
                port:
                  number: 3000
```

```bash
kubectl apply -f manifests/<NAMESPACE>/07-ingress.yaml
```

Donde `<GRAFANA_HOSTNAME>` según el entorno:

  -----------------------------------------------------------------------
  **Entorno**   **Hostname sugerido**
  ------------- ---------------------------------------------------------
  DEV           grafana-dev.onp.interno
  QA            grafana-qa.onp.interno
  PROD          grafana.onp.interno
  -----------------------------------------------------------------------

Verificar el Ingress:

```bash
kubectl get ingress -n <NAMESPACE>
```

Resultado esperado:

```
NAME      CLASS     HOSTS                        ADDRESS      PORTS   AGE
grafana   traefik   grafana-dev.onp.interno      <IP_NODO>    80      10s
```

Acceder desde cualquier máquina de la red (con el hostname resuelto en DNS o `/etc/hosts`):

```
http://grafana-dev.onp.interno
```

# Anexo A --- Configuración específica para DEV

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para DEV**
  ------------------------- ---------------------------------------------
  Namespace                 grafana-dev

  Namespace Prometheus      prometheus-dev

  Namespace Jaeger          observabilidad-dev

  Namespace Elasticsearch   elastic-dev

  Réplicas                  1

  Tamaño del PVC            2Gi

  Índice de logs en ES      onp-logs-\*
  -----------------------------------------------------------------------

## A.1 Crear el namespace DEV

```bash
kubectl create namespace grafana-dev
kubectl label namespace grafana-dev \
  app.kubernetes.io/managed-by=oti-onp \
  environment=dev \
  team=oti-onp
```

## A.2 Valores del ConfigMap de datasources para DEV

Reemplazar los placeholders del ConfigMap (Sección 6.1) con los siguientes valores:

  -----------------------------------------------------------------------
  **Placeholder**           **Valor DEV**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             grafana-dev

  \<NAMESPACE_PROMETHEUS\>  prometheus-dev

  \<NAMESPACE_JAEGER\>      observabilidad-dev

  \<NAMESPACE_ES\>          elastic-dev

  \<LOGS_INDEX\>            onp-logs-\*
  -----------------------------------------------------------------------

## A.3 Valores del PVC para DEV

  -----------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      2Gi
  -----------------------------------------------------------------------

## A.4 Recursos recomendados para DEV

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             grafana/grafana:11.1.0 (Escenario B)

  \<ENV\>               dev

  \<CPU_REQUEST\>       100m

  \<CPU_LIMIT\>         300m

  \<MEMORY_REQUEST\>    128Mi

  \<MEMORY_LIMIT\>      256Mi
  ------------------------------------------------------------------------------------------

# Anexo B --- Configuración específica para QA

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para QA**
  ------------------------- ---------------------------------------------
  Namespace                 grafana-qa

  Namespace Prometheus      prometheus-qa

  Namespace Jaeger          observabilidad-qa

  Namespace Elasticsearch   elastic-qa

  Réplicas                  1

  Tamaño del PVC            2Gi

  Índice de logs en ES      onp-logs-\*
  -----------------------------------------------------------------------

## B.1 Crear el namespace QA

```bash
kubectl create namespace grafana-qa
kubectl label namespace grafana-qa \
  app.kubernetes.io/managed-by=oti-onp \
  environment=qa \
  team=oti-onp
```

## B.2 Valores del ConfigMap de datasources para QA

  -----------------------------------------------------------------------
  **Placeholder**           **Valor QA**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             grafana-qa

  \<NAMESPACE_PROMETHEUS\>  prometheus-qa

  \<NAMESPACE_JAEGER\>      observabilidad-qa

  \<NAMESPACE_ES\>          elastic-qa

  \<LOGS_INDEX\>            onp-logs-\*
  -----------------------------------------------------------------------

## B.3 Recursos recomendados para QA

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             \<GITLAB_REGISTRY_URL\>/observabilidad/grafana:11.1.0

  \<ENV\>               qa

  \<CPU_REQUEST\>       150m

  \<CPU_LIMIT\>         400m

  \<MEMORY_REQUEST\>    256Mi

  \<MEMORY_LIMIT\>      512Mi
  ------------------------------------------------------------------------------------------

# Anexo C --- Configuración específica para PROD

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para PROD**
  ------------------------- ---------------------------------------------
  Namespace                 grafana

  Namespace Prometheus      prometheus

  Namespace Jaeger          observabilidad

  Namespace Elasticsearch   elastic

  Réplicas                  1

  Tamaño del PVC            5Gi

  Índice de logs en ES      onp-logs-\*
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Al igual que Prometheus, Grafana no es stateless (almacena su base de datos SQLite en el PVC), por lo que no aplica HPA. Se despliega con 1 réplica en todos los entornos.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## C.1 Crear el namespace PROD

```bash
kubectl create namespace grafana
kubectl label namespace grafana \
  app.kubernetes.io/managed-by=oti-onp \
  environment=prod \
  team=oti-onp
```

## C.2 Valores del ConfigMap de datasources para PROD

  -----------------------------------------------------------------------
  **Placeholder**           **Valor PROD**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             grafana

  \<NAMESPACE_PROMETHEUS\>  prometheus

  \<NAMESPACE_JAEGER\>      observabilidad

  \<NAMESPACE_ES\>          elastic

  \<LOGS_INDEX\>            onp-logs-\*
  -----------------------------------------------------------------------

## C.3 Recursos recomendados para PROD

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             \<GITLAB_REGISTRY_URL\>/observabilidad/grafana:11.1.0

  \<ENV\>               prod

  \<CPU_REQUEST\>       200m

  \<CPU_LIMIT\>         500m

  \<MEMORY_REQUEST\>    256Mi

  \<MEMORY_LIMIT\>      512Mi
  ------------------------------------------------------------------------------------------

# Anexo D --- Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen en una máquina con internet

```bash
docker pull grafana/grafana:11.1.0
docker save grafana/grafana:11.1.0 -o grafana-11.1.0.tar
```

## D.2 Transferir el archivo al nodo

```bash
scp grafana-11.1.0.tar <USUARIO>@<IP_NODO>:/tmp/
```

## D.3 Importar la imagen en el nodo

Ejecutar según el runtime del cluster:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images import /tmp/grafana-11.1.0.tar

# Opción B — K3s
sudo k3s ctr images import /tmp/grafana-11.1.0.tar
```

Verificar que la imagen quedó disponible:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images ls | grep grafana

# Opción B — K3s
sudo k3s ctr images list | grep grafana
```

## D.4 Usar la imagen importada en el Deployment

```yaml
image: docker.io/grafana/grafana:11.1.0
```

Mantener el bloque `imagePullSecrets` comentado.
