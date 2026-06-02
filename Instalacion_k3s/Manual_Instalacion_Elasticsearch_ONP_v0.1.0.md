**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Elasticsearch en Kubernetes (K3s)**

Usando manifests Kubernetes directos sin ECK Operator

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

Este manual describe el proceso completo de instalación de Elasticsearch en un cluster Kubernetes K3s on-premise, utilizando manifests Kubernetes directos sin el ECK Operator (Elastic Cloud on Kubernetes).

El documento está dirigido al equipo de Plataforma de la OTI --- ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Elasticsearch?

Elasticsearch es un motor de búsqueda y análisis distribuido basado en Apache Lucene. Permite almacenar, buscar y analizar grandes volúmenes de datos en tiempo real. En el stack ONP actúa como backend de almacenamiento compartido para dos componentes:

-   **Jaeger**: almacena las trazas distribuidas de los servicios.
-   **Kibana**: consulta los logs enviados por el OTEL Collector.

## 1.3 Rol de Elasticsearch en el stack ONP

```
OTEL Collector
    └── logs pipeline   → Elasticsearch (índice: onp-logs-<entorno>)

Jaeger Operator
    └── traces storage  → Elasticsearch (índices: jaeger-onp-*)

Kibana          → consulta onp-logs-*
Grafana         → consulta onp-logs-* (datasource Elasticsearch)
```

Elasticsearch es un componente de **infraestructura compartida**: múltiples componentes del stack dependen de él. Debe instalarse antes que Jaeger y Kibana.

## 1.4 Por qué Elasticsearch no es stateless

Elasticsearch almacena todos sus datos (índices, trazas, logs) en el directorio `/usr/share/elasticsearch/data`. Esta información debe persistir entre reinicios del pod.

Al igual que Prometheus y Grafana, el ciclo de vida del PVC es **independiente** del pod: reiniciar el pod no pierde los datos. Solo se pierden si el PVC es eliminado explícitamente con `kubectl delete pvc`.

## 1.5 Configuración de seguridad

Elasticsearch 8.x tiene la seguridad habilitada por defecto (`xpack.security.enabled=true`). En el stack ONP se usa la siguiente configuración:

  -----------------------------------------------------------------------
  **Parámetro**                             **Valor**   **Motivo**
  ----------------------------------------- ----------- -----------------
  xpack.security.enabled                    true        Autenticación activa (usuario elastic)

  xpack.security.http.ssl.enabled           false       TLS HTTP desactivado para simplificar la comunicación interna en el cluster

  xpack.security.transport.ssl.enabled      false       TLS transport desactivado (un solo nodo, sin riesgo de intercepción interna)
  -----------------------------------------------------------------------

El usuario por defecto es `elastic` (superusuario). Su contraseña se almacena en un Secret de Kubernetes.

## 1.6 Alcance de este manual

Este manual cubre:

-   Preparación del namespace de cada entorno.
-   Creación del Secret con la contraseña del usuario `elastic`.
-   Despliegue de Elasticsearch como un Deployment de un solo nodo con PVC.
-   Verificación del despliegue y de la autenticación.
-   Exposición del servicio (NodePort e Ingress).
-   Troubleshooting de errores comunes.

Queda fuera del alcance:

-   Configuración de clusters multi-nodo.
-   Habilitación de TLS HTTP o transport.
-   Gestión de usuarios adicionales de Elasticsearch (más allá del usuario `elastic`).
-   Instalación del ECK Operator.

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

  Nodo K3s                Mínimo 4GB RAM disponibles     Elasticsearch requiere al menos 2GB de heap JVM

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

  \<NAMESPACE\>                 Namespace de Elasticsearch                          elastic-dev / elastic-qa / elastic

  \<ES_VERSION\>                Versión de Elasticsearch                            8.19.15

  \<ELASTIC_PASSWORD\>          Contraseña del usuario administrador `elastic`
  -----------------------------------------------------------------------------------------------------------------------

## 2.4 Verificaciones previas

### 2.4.1 Verificar vm.max_map_count en el nodo

Elasticsearch requiere que el parámetro del kernel `vm.max_map_count` sea al menos `262144`. En el stack ONP esto se gestiona mediante un initContainer que lo configura automáticamente al arrancar el pod (ver Sección 7.1). Sin embargo, se recomienda verificar el valor actual del nodo:

```bash
kubectl debug node/<NOMBRE_NODO> -it --image=busybox -- sysctl vm.max_map_count
```

Resultado esperado:

```
vm.max_map_count = 262144
```

Si el valor es inferior, el initContainer lo corregirá al desplegar Elasticsearch. No es necesario intervenir manualmente.

# 3. Preparación de imágenes

## Nota: ¿Qué camino seguir?

  -----------------------------------------------------------------------
  **Escenario**                  **Cuándo usarlo**                                      **Ir a**
  ------------------------------ ------------------------------------------------------ ----------
  A --- GitLab Registry          PROD y QA. Sin salida a internet desde el cluster.      Sección 3.2

  B --- Internet directo         DEV y pruebas. El nodo tiene acceso a internet.        Sección 3.3

  C --- Air-gap                  Sin internet ni registry privado.                       Anexo D
  -----------------------------------------------------------------------

## 3.1 Imágenes requeridas

  -----------------------------------------------------------------------------------------------
  **Componente**       **Imagen original**                                         **Versión**
  -------------------- ----------------------------------------------------------- --------------------
  Elasticsearch        docker.elastic.co/elasticsearch/elasticsearch               8.19.15

  Init container       docker.io/busybox                                           1.36
  -----------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Verificar la versión estable más reciente en https://www.elastic.co/downloads/elasticsearch antes de ejecutar el mirroring. Usar siempre una versión fija (no `latest`).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 3.2 Escenario A --- GitLab Registry (producción)

```bash
docker login <GITLAB_REGISTRY_URL>

# Elasticsearch
docker pull docker.elastic.co/elasticsearch/elasticsearch:<ES_VERSION>
docker tag docker.elastic.co/elasticsearch/elasticsearch:<ES_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:<ES_VERSION>
docker push <GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:<ES_VERSION>

# Init container (busybox)
docker pull busybox:1.36
docker tag busybox:1.36 <GITLAB_REGISTRY_URL>/observabilidad/busybox:1.36
docker push <GITLAB_REGISTRY_URL>/observabilidad/busybox:1.36
```

Las imágenes en el registry privado quedan disponibles en:

```
<GITLAB_REGISTRY_URL>/observabilidad/elasticsearch:<ES_VERSION>
<GITLAB_REGISTRY_URL>/observabilidad/busybox:1.36
```

## 3.3 Escenario B --- Internet directo (pruebas y DEV)

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** No se requieren pasos de mirroring. Pasar directamente a la Sección 4. En el Deployment (Sección 7.1) usar las imágenes públicas directamente y mantener `imagePullSecrets` comentado.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **Componente**       **Imagen a usar directamente**
  -------------------- -------------------------------------------
  Elasticsearch        docker.elastic.co/elasticsearch/elasticsearch:8.19.15

  Init container       busybox:1.36
  -----------------------------------------------------------------------

## 3.4 Escenario C --- Air-gap

Ver **Anexo D** para el procedimiento completo de transferencia de imágenes vía archivo `.tar`.

# 4. Preparación del namespace

## 4.1 Crear el namespace

```bash
kubectl create namespace <NAMESPACE>
```

Donde \<NAMESPACE\> es:

-   `elastic-dev` --- para el entorno de desarrollo.
-   `elastic-qa` --- para el entorno de calidad.
-   `elastic` --- para el entorno de producción.

## 4.2 Agregar labels al namespace

```bash
kubectl label namespace <NAMESPACE> \
  app.kubernetes.io/managed-by=oti-onp \
  environment=<ENV> \
  team=oti-onp
```

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
elastic-dev   Active   Xs    app.kubernetes.io/managed-by=oti-onp,environment=dev,team=oti-onp
```

# 5. Creación del Secret

Elasticsearch 8.x crea el usuario `elastic` (superusuario) automáticamente al iniciar. La contraseña de ese usuario se define mediante el Secret `elasticsearch-credentials`.

  -----------------------------------------------------------------------
  **🔴 IMPORTANTE:** Nunca incluir contraseñas en texto plano en archivos YAML que se suban a repositorios de código. Los Secrets deben crearse únicamente mediante comandos kubectl.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 5.1 Crear el Secret

```bash
kubectl create secret generic elasticsearch-credentials \
  --from-literal=ELASTIC_PASSWORD=<ELASTIC_PASSWORD> \
  --namespace=<NAMESPACE>
```

## 5.2 Verificar el Secret

```bash
kubectl get secret elasticsearch-credentials -n <NAMESPACE>
```

Resultado esperado:

```
NAME                       TYPE     DATA   AGE
elasticsearch-credentials  Opaque   1      10s
```

# 6. PersistentVolumeClaim

Elasticsearch almacena todos sus índices en `/usr/share/elasticsearch/data`. Esta información debe persistir entre reinicios del pod.

## 6.1 Crear el PVC

Crear el archivo `manifests/<NAMESPACE>/03-pvc.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: elasticsearch-data
  namespace: <NAMESPACE>
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: <STORAGE_SIZE>
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El valor de `<STORAGE_SIZE>` está definido en cada anexo de entorno: Anexo A.3 (DEV: `10Gi`), Anexo B.3 (QA: `30Gi`) y Anexo C.3 (PROD: `100Gi`).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 6.2 Aplicar el PVC

```bash
kubectl apply -f manifests/<NAMESPACE>/03-pvc.yaml
```

Resultado esperado:

```
persistentvolumeclaim/elasticsearch-data created
```

## 6.3 Verificar el PVC

```bash
kubectl get pvc -n <NAMESPACE>
```

Resultado esperado:

```
NAME                 STATUS    VOLUME   CAPACITY   ACCESS MODES   STORAGECLASS
elasticsearch-data   Pending                                      local-path
```

El estado `Pending` es normal hasta que el pod arranque y monte el volumen.

# 7. Despliegue de Elasticsearch

## 7.1 Crear el Deployment

Crear el archivo `manifests/<NAMESPACE>/04-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: elasticsearch
  namespace: <NAMESPACE>
  labels:
    app: elasticsearch
    environment: <ENV>
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: elasticsearch
  template:
    metadata:
      labels:
        app: elasticsearch
        environment: <ENV>
    spec:
      initContainers:
        # Elasticsearch requiere vm.max_map_count >= 262144 en el nodo host.
        # Este initContainer lo configura automáticamente antes de que ES arranque.
        - name: increase-vm-max-map
          image: <BUSYBOX_IMAGE>
          command: ["sysctl", "-w", "vm.max_map_count=262144"]
          securityContext:
            privileged: true
      containers:
        - name: elasticsearch
          image: <ES_IMAGE>
          ports:
            - name: http
              containerPort: 9200
            - name: transport
              containerPort: 9300
          env:
            - name: discovery.type
              value: single-node
            - name: ES_JAVA_OPTS
              value: "<ES_JAVA_OPTS>"
            - name: ELASTIC_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: elasticsearch-credentials
                  key: ELASTIC_PASSWORD
            - name: xpack.security.enabled
              value: "true"
            - name: xpack.security.http.ssl.enabled
              value: "false"
            - name: xpack.security.transport.ssl.enabled
              value: "false"
          volumeMounts:
            - name: data
              mountPath: /usr/share/elasticsearch/data
          resources:
            requests:
              cpu: "<CPU_REQUEST>"
              memory: "<MEMORY_REQUEST>"
            limits:
              cpu: "<CPU_LIMIT>"
              memory: "<MEMORY_LIMIT>"
          readinessProbe:
            exec:
              command:
                - sh
                - -c
                - "curl -sf -u elastic:${ELASTIC_PASSWORD} http://localhost:9200/_cluster/health"
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 10
      volumes:
        - name: data
          persistentVolumeClaim:
            claimName: elasticsearch-data
      # Escenario A únicamente (GitLab Registry privado):
      # imagePullSecrets:
      #   - name: gitlab-registry-secret
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Los placeholders `<ES_IMAGE>` y `<BUSYBOX_IMAGE>` deben reemplazarse con las imágenes del escenario correspondiente. Ver tabla en los Anexos A/B/C. Los placeholders de recursos (`<CPU_REQUEST>`, `<MEMORY_REQUEST>`, etc.) también están en los Anexos.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Antes de aplicar, verificar que todos los placeholders fueron reemplazados: `<NAMESPACE>`, `<ENV>`, `<ES_IMAGE>`, `<BUSYBOX_IMAGE>`, `<ES_JAVA_OPTS>`, `<CPU_REQUEST>`, `<CPU_LIMIT>`, `<MEMORY_REQUEST>` y `<MEMORY_LIMIT>`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 7.2 Aplicar el Deployment

```bash
kubectl apply -f manifests/<NAMESPACE>/04-deployment.yaml
```

Resultado esperado:

```
deployment.apps/elasticsearch created
```

## 7.3 Crear el Service

Crear el archivo `manifests/<NAMESPACE>/05-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: elasticsearch
  namespace: <NAMESPACE>
  labels:
    app: elasticsearch
spec:
  selector:
    app: elasticsearch
  ports:
    - name: http
      port: 9200
      targetPort: 9200
```

## 7.4 Aplicar el Service

```bash
kubectl apply -f manifests/<NAMESPACE>/05-service.yaml
```

Resultado esperado:

```
service/elasticsearch created
```

## 7.5 Verificar el despliegue

Elasticsearch tarda entre 30 y 60 segundos en arrancar. Monitorear el estado del pod:

```bash
kubectl get pods -n <NAMESPACE> -w
```

Resultado esperado (esperar hasta que el initContainer termine y el pod quede Running):

```
NAME                             READY   STATUS     RESTARTS   AGE
elasticsearch-xxxxxxxxx-xxxxx    0/1     Init:0/1   0          5s
elasticsearch-xxxxxxxxx-xxxxx    0/1     PodInitializing   0   10s
elasticsearch-xxxxxxxxx-xxxxx    0/1     Running    0          15s
elasticsearch-xxxxxxxxx-xxxxx    1/1     Running    0          45s
```

```bash
kubectl get pvc -n <NAMESPACE>
```

Resultado esperado:

```
NAME                 STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS
elasticsearch-data   Bound    pvc-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx   10Gi       RWO            local-path
```

```bash
kubectl get services -n <NAMESPACE>
```

Resultado esperado:

```
NAME            TYPE        CLUSTER-IP    PORT(S)
elasticsearch   ClusterIP   10.x.x.x      9200/TCP
```

# 8. Verificación del despliegue

## 8.1 Verificar los logs de Elasticsearch

```bash
kubectl logs -n <NAMESPACE> deployment/elasticsearch --tail=20
```

Al iniciar correctamente, los últimos logs muestran líneas similares a:

```
{"@timestamp":"2026-05-13T...","log.level":"INFO","message":"started","service.name":"ES_ECS","cluster.name":"docker-cluster","node.name":"elasticsearch-..."}
```

La línea clave es `"message":"started"`.

## 8.2 Verificar el health del cluster

```bash
kubectl port-forward -n <NAMESPACE> svc/elasticsearch 9200:9200 &
```

Esperar a que aparezca el mensaje `Forwarding from 127.0.0.1:9200 -> 9200`. Luego ejecutar:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:9200/_cluster/health?pretty
```

Resultado esperado:

```json
{
  "cluster_name" : "docker-cluster",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 0,
  "active_shards" : 0,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 0
}
```

El campo `"status": "green"` confirma que el cluster está sano.

## 8.3 Verificar la autenticación

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/
```

Resultado esperado:

```json
{
  "name" : "elasticsearch-xxxxxxxxx-xxxxx",
  "cluster_name" : "docker-cluster",
  "version" : {
    "number" : "8.19.15",
    ...
  },
  "tagline" : "You Know, for Search"
}
```

## 8.4 Verificar los índices existentes

Cuando Jaeger ya esté instalado y en operación, se pueden listar los índices creados:

```bash
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:9200/_cat/indices?v | grep jaeger
```

Resultado esperado (fragmento):

```
health status index                    uuid   pri rep docs.count
green  open   jaeger-onp-service-2026-05-13  ...  1   0   15
green  open   jaeger-onp-span-2026-05-13     ...  1   0   42
```

# 9. Configuración de retención de logs (ILM)

Sin una política de retención, los índices de logs (`onp-logs-*`) crecen indefinidamente hasta agotar el disco. Elasticsearch gestiona esto mediante **ILM (Index Lifecycle Management)**: una política que elimina automáticamente los índices más antiguos de un número determinado de días.

  -----------------------------------------------------------------------
  **⚠️ IMPORTANTE:** Este paso debe ejecutarse **antes** de que el OTEL Collector comience a enviar logs. Los índices creados antes de aplicar la política no la heredan automáticamente.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 9.1 Crear la política ILM

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_ilm/policy/onp-logs-policy" \
  -H "Content-Type: application/json" \
  -d '{
    "policy": {
      "phases": {
        "hot": {
          "min_age": "0ms",
          "actions": {}
        },
        "delete": {
          "min_age": "<RETENTION_DAYS>",
          "actions": {
            "delete": {}
          }
        }
      }
    }
  }'
```

Donde `<RETENTION_DAYS>` según el entorno:

  -----------------------------------------------------------------------
  **Entorno**   **Valor**   **Descripción**
  ------------- ----------- ---------------------------------------------
  DEV           30d         30 días de retención
  QA            30d         30 días de retención
  PROD          90d         90 días de retención (ajustable con el uso)
  -----------------------------------------------------------------------

Resultado esperado:

```json
{"acknowledged":true}
```

## 9.2 Crear el index template

El index template aplica automáticamente la política ILM y el codec de compresión `best_compression` a todos los índices nuevos que coincidan con el patrón `onp-logs-*`.

> **NOTA sobre `best_compression`:** El codec por defecto de Elasticsearch es LZ4 (rápido pero mayor tamaño en disco). El codec `best_compression` usa zstd/deflate y reduce el tamaño de los índices entre un 30 y 40% adicional. Para datos de logs históricos, el ahorro en disco supera ampliamente el costo en velocidad de escritura.

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_index_template/onp-logs-template" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["onp-logs-*"],
    "template": {
      "settings": {
        "codec": "best_compression",
        "index.lifecycle.name": "onp-logs-policy",
        "number_of_shards": 1,
        "number_of_replicas": 0
      },
      "mappings": {
        "properties": {
          "duration_ms":               { "type": "long" },
          "http.response.status_code": { "type": "short" }
        }
      }
    }
  }'
```

> **NOTA sobre los mappings explícitos:** Los campos `duration_ms` y `http.response.status_code` llegan a Elasticsearch como strings (vienen del MDC, que solo maneja strings). Sin un mapping explícito, Elasticsearch los clasificaría como `keyword` y las queries de rango en Kibana (`duration_ms > 1000`, `http.response.status_code >= 400`) no funcionarían. El mapping explícito como `long` y `short` hace que Elasticsearch coercione el string al tipo numérico automáticamente.

Resultado esperado:

```json
{"acknowledged":true}
```

## 9.3 Verificar la política y el template

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_ilm/policy/onp-logs-policy?pretty"
```

Resultado esperado (fragmento):

```json
{
  "onp-logs-policy" : {
    "policy" : {
      "phases" : {
        "hot" : { ... },
        "delete" : {
          "min_age" : "30d",
          "actions" : { "delete" : { } }
        }
      }
    }
  }
}
```

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_index_template/onp-logs-template?pretty" | grep -A3 "lifecycle"
```

Resultado esperado:

```
"index.lifecycle.name" : "onp-logs-policy"
```

## 9.4 Ajustar la retención en PROD

Para modificar los días de retención en cualquier momento (sin interrumpir el servicio), re-ejecutar el comando de la Sección 9.1 con el nuevo valor de `<RETENTION_DAYS>`. ILM aplica el cambio en el siguiente ciclo de evaluación (cada 10 minutos por defecto).

## 9.5 Agregar nuevos patrones de índice

El index template creado en la Sección 9.2 cubre únicamente el patrón `onp-logs-*`. Si en el futuro se incorporan nuevos componentes que generen índices con un patrón diferente (por ejemplo `onp-metrics-*` o el nombre de un nuevo servicio), esos índices **no heredarán** automáticamente la política ILM.

Para cubrirlos, crear un nuevo index template específico para ese patrón:

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_index_template/<NOMBRE_TEMPLATE>" \
  -H "Content-Type: application/json" \
  -d '{
    "index_patterns": ["<NUEVO_PATRON>"],
    "template": {
      "settings": {
        "codec": "best_compression",
        "index.lifecycle.name": "onp-logs-policy",
        "number_of_shards": 1,
        "number_of_replicas": 0
      }
    }
  }'
```

Donde:
- `<NOMBRE_TEMPLATE>` es un nombre descriptivo del template (ej. `onp-metrics-template`).
- `<NUEVO_PATRON>` es el patrón de índices a cubrir (ej. `onp-metrics-*`).

Resultado esperado:

```json
{"acknowledged":true}
```

> **NOTA:** No es necesario crear una nueva política ILM. La política `onp-logs-policy` ya existe y puede reutilizarse para cualquier patrón nuevo. Si el nuevo componente requiere una retención diferente, crear primero una nueva política con `PUT /_ilm/policy/<NOMBRE_POLITICA>` y referenciarla en el template.

# 10. Exposición del servicio (acceso permanente)

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Elasticsearch es un servicio interno del cluster. En condiciones normales de operación no se expone fuera del cluster. Las secciones 9.1 y 9.2 son opcionales y solo se aplican si el equipo de operaciones necesita acceder a la API de Elasticsearch directamente desde fuera del cluster para diagnóstico.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 9.1 Opción A: NodePort (acceso por IP del nodo)

Crear el archivo `manifests/<NAMESPACE>/06-service-nodeport.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: elasticsearch-nodeport
  namespace: <NAMESPACE>
  labels:
    app: elasticsearch
spec:
  type: NodePort
  selector:
    app: elasticsearch
  ports:
    - port: 9200
      targetPort: 9200
      nodePort: 30920
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
NAME                      TYPE        CLUSTER-IP    PORT(S)          AGE
elasticsearch             ClusterIP   10.x.x.x      9200/TCP         1h
elasticsearch-nodeport    NodePort    10.x.x.x      9200:30920/TCP   10s
```

Verificar que el servicio tiene endpoints activos:

```bash
kubectl get endpoints elasticsearch-nodeport -n <NAMESPACE>
```

El resultado debe mostrar una IP en la columna `ENDPOINTS`. Si aparece `<none>`, el selector no está matcheando los pods — verificar los labels reales con `kubectl get pods -n <NAMESPACE> --show-labels`.

Acceder desde cualquier máquina de la red interna:

```
http://<IP_NODO_K3S>:30920
```

## 9.2 Opción B: Ingress con Traefik (acceso por nombre DNS)

Crear el archivo `manifests/<NAMESPACE>/06-ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: elasticsearch
  namespace: <NAMESPACE>
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: web
spec:
  rules:
    - host: <ES_HOSTNAME>
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: elasticsearch
                port:
                  number: 9200
```

```bash
kubectl apply -f manifests/<NAMESPACE>/06-ingress.yaml
```

Donde `<ES_HOSTNAME>` según el entorno:

  -----------------------------------------------------------------------
  **Entorno**   **Hostname sugerido**
  ------------- ---------------------------------------------------------
  DEV           elasticsearch-dev.onp.interno
  QA            elasticsearch-qa.onp.interno
  PROD          elasticsearch.onp.interno
  -----------------------------------------------------------------------

# 10. Troubleshooting

## 10.1 Pod en CrashLoopBackOff por vm.max_map_count insuficiente

### Síntoma

```
kubectl get pods -n <NAMESPACE>
NAME                             READY   STATUS             RESTARTS
elasticsearch-xxxxxxxxx-xxxxx    0/1     CrashLoopBackOff   3
```

En los logs aparece:

```
max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

### Causa y solución

El initContainer no pudo ejecutarse con privilegios (posible restricción de seguridad en el cluster). Verificar que el initContainer tiene `securityContext.privileged: true`:

```bash
kubectl describe pod <nombre-pod> -n <NAMESPACE> | grep -A5 "Init Containers"
```

Si el initContainer sí se ejecutó pero el error persiste, configurar el valor directamente en el nodo:

```bash
sudo sysctl -w vm.max_map_count=262144
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

## 10.2 Pod en OOMKilled

### Síntoma

```
kubectl get pods -n <NAMESPACE>
NAME                             READY   STATUS      RESTARTS
elasticsearch-xxxxxxxxx-xxxxx    0/1     OOMKilled   2
```

### Causa y solución

El límite de memoria del container es insuficiente. El heap JVM (`ES_JAVA_OPTS`) debe ser como máximo la mitad del límite de memoria del container.

Verificar la relación: si `ES_JAVA_OPTS=-Xms1g -Xmx1g`, el límite del container debe ser al menos `2Gi`.

Actualizar los recursos en el Deployment y reaplicar:

```bash
kubectl apply -f manifests/<NAMESPACE>/04-deployment.yaml
kubectl rollout restart deployment/elasticsearch -n <NAMESPACE>
```

## 10.3 Error de autenticación (401 Unauthorized)

### Síntoma

```bash
curl -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/
# {"error":{"reason":"missing authentication credentials",...},"status":401}
```

### Causas y soluciones

-   La contraseña del Secret es incorrecta. Verificar con:

    ```bash
    kubectl get secret elasticsearch-credentials -n <NAMESPACE> \
      -o jsonpath='{.data.ELASTIC_PASSWORD}' | base64 -d
    ```

-   La variable de entorno `ELASTIC_PASSWORD` no se está inyectando. Verificar con:

    ```bash
    kubectl exec -n <NAMESPACE> deployment/elasticsearch -- env | grep ELASTIC_PASSWORD
    ```

## 10.4 PVC queda en estado Pending indefinidamente

### Síntoma

```bash
kubectl get pvc -n <NAMESPACE>
NAME                 STATUS    VOLUME   CAPACITY
elasticsearch-data   Pending
```

Pasados más de 5 minutos sin cambiar a `Bound`.

### Causa y solución

Con la StorageClass `local-path` de K3s, el PVC queda en `Pending` hasta que el pod lo monte por primera vez (`WaitForFirstConsumer`). Verificar que el Deployment también fue aplicado:

```bash
kubectl get pods -n <NAMESPACE>
```

Si el pod existe pero el PVC sigue en Pending, revisar los eventos:

```bash
kubectl describe pvc elasticsearch-data -n <NAMESPACE>
```

## 10.5 CrashLoopBackOff al actualizar la imagen (node.lock)

### Síntoma

Al hacer `kubectl set image` o `kubectl apply` con una nueva versión, el pod nuevo queda en CrashLoopBackOff con el error:

```
Lock held by another program: /usr/share/elasticsearch/data/node.lock
```

### Causa y solución

La estrategia por defecto `RollingUpdate` intenta arrancar el pod nuevo antes de terminar el viejo. Como ambos comparten el mismo PVC (`ReadWriteOnce`), Elasticsearch impide que dos instancias accedan al mismo directorio de datos.

El Deployment debe usar `strategy: type: Recreate`, que termina el pod viejo antes de arrancar el nuevo. Esta configuración está incluida en el Deployment del manual (Sección 7.1).

Si el Deployment ya está desplegado sin esta estrategia, aplicar el manifest actualizado resuelve el problema:

```bash
kubectl apply -f manifests/<NAMESPACE>/04-deployment.yaml
```

## 10.6 Comandos útiles de diagnóstico rápido

  -----------------------------------------------------------------------
  **Diagnóstico**                          **Comando**
  ---------------------------------------- ------------------------------
  Ver estado del pod                       kubectl get pods -n \<NAMESPACE\>

  Ver logs de Elasticsearch                kubectl logs -n \<NAMESPACE\> deployment/elasticsearch \--tail=50

  Ver eventos del namespace                kubectl get events -n \<NAMESPACE\> \--sort-by=\'.lastTimestamp\'

  Ver variables de entorno del pod         kubectl exec -n \<NAMESPACE\> deployment/elasticsearch \-- env \| grep ELASTIC

  Health del cluster                       curl -u elastic:\<PASSWORD\> http://localhost:9200/\_cluster/health?pretty

  Listar índices                           curl -u elastic:\<PASSWORD\> http://localhost:9200/\_cat/indices?v

  Reiniciar Elasticsearch                  kubectl rollout restart deployment/elasticsearch -n \<NAMESPACE\>
  -----------------------------------------------------------------------

# Anexo A --- Configuración específica para DEV

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para DEV**
  ------------------------- ---------------------------------------------
  Namespace                 elastic-dev

  Tamaño del PVC            10Gi

  Heap JVM                  -Xms1g -Xmx1g

  Retención de logs (ILM)   30d
  -----------------------------------------------------------------------

## A.1 Crear el namespace DEV

```bash
kubectl create namespace elastic-dev
kubectl label namespace elastic-dev \
  app.kubernetes.io/managed-by=oti-onp \
  environment=dev \
  team=oti-onp
```

## A.2 Valores del PVC para DEV

  -----------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      10Gi
  -----------------------------------------------------------------------

## A.3 Recursos recomendados para DEV

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- --------------------------------------------------------------------
  \<ES_IMAGE\>          docker.elastic.co/elasticsearch/elasticsearch:8.19.15 (Escenario B)

  \<BUSYBOX_IMAGE\>     busybox:1.36 (Escenario B)

  \<ENV\>               dev

  \<ES_JAVA_OPTS\>      -Xms1g -Xmx1g

  \<CPU_REQUEST\>       500m

  \<CPU_LIMIT\>         1000m

  \<MEMORY_REQUEST\>    2Gi

  \<MEMORY_LIMIT\>      2Gi
  ------------------------------------------------------------------------------------------

# Anexo B --- Configuración específica para QA

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para QA**
  ------------------------- ---------------------------------------------
  Namespace                 elastic-qa

  Tamaño del PVC            30Gi

  Heap JVM                  -Xms2g -Xmx2g

  Retención de logs (ILM)   30d
  -----------------------------------------------------------------------

## B.1 Crear el namespace QA

```bash
kubectl create namespace elastic-qa
kubectl label namespace elastic-qa \
  app.kubernetes.io/managed-by=oti-onp \
  environment=qa \
  team=oti-onp
```

## B.2 Valores del PVC para QA

  -----------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      30Gi
  -----------------------------------------------------------------------

## B.3 Recursos recomendados para QA

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- --------------------------------------------------------------------
  \<ES_IMAGE\>          \<GITLAB_REGISTRY_URL\>/observabilidad/elasticsearch:8.19.15

  \<BUSYBOX_IMAGE\>     \<GITLAB_REGISTRY_URL\>/observabilidad/busybox:1.36

  \<ENV\>               qa

  \<ES_JAVA_OPTS\>      -Xms2g -Xmx2g

  \<CPU_REQUEST\>       1000m

  \<CPU_LIMIT\>         2000m

  \<MEMORY_REQUEST\>    3Gi

  \<MEMORY_LIMIT\>      4Gi
  ------------------------------------------------------------------------------------------

# Anexo C --- Configuración específica para PROD

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para PROD**
  ------------------------- ---------------------------------------------
  Namespace                 elastic

  Tamaño del PVC            100Gi

  Heap JVM                  -Xms4g -Xmx4g

  Retención de logs (ILM)   90d (ajustable — ver Sección 9.4)
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Elasticsearch no es stateless (almacena todos los índices en el PVC), por lo que no aplica HPA. Se despliega con 1 réplica en todos los entornos. Para alta disponibilidad se requiere un cluster multi-nodo, lo cual está fuera del alcance de este manual.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## C.1 Crear el namespace PROD

```bash
kubectl create namespace elastic
kubectl label namespace elastic \
  app.kubernetes.io/managed-by=oti-onp \
  environment=prod \
  team=oti-onp
```

## C.2 Valores del PVC para PROD

  -----------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- ---------------------------------------------
  \<STORAGE_SIZE\>      100Gi
  -----------------------------------------------------------------------

## C.3 Recursos recomendados para PROD

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- --------------------------------------------------------------------
  \<ES_IMAGE\>          \<GITLAB_REGISTRY_URL\>/observabilidad/elasticsearch:8.19.15

  \<BUSYBOX_IMAGE\>     \<GITLAB_REGISTRY_URL\>/observabilidad/busybox:1.36

  \<ENV\>               prod

  \<ES_JAVA_OPTS\>      -Xms4g -Xmx4g

  \<CPU_REQUEST\>       2000m

  \<CPU_LIMIT\>         4000m

  \<MEMORY_REQUEST\>    6Gi

  \<MEMORY_LIMIT\>      8Gi
  ------------------------------------------------------------------------------------------

# Anexo D --- Air-gap: transferencia de imágenes sin internet ni registry

## D.1 Exportar las imágenes en una máquina con internet

```bash
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.19.15
docker save docker.elastic.co/elasticsearch/elasticsearch:8.19.15 \
  -o elasticsearch-8.19.15.tar

docker pull busybox:1.36
docker save busybox:1.36 -o busybox-1.36.tar
```

## D.2 Transferir los archivos al nodo

```bash
scp elasticsearch-8.19.15.tar <USUARIO>@<IP_NODO>:/tmp/
scp busybox-1.36.tar <USUARIO>@<IP_NODO>:/tmp/
```

## D.3 Importar las imágenes en el nodo

Ejecutar según el runtime del cluster:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images import /tmp/elasticsearch-8.19.15.tar
sudo ctr -n k8s.io images import /tmp/busybox-1.36.tar

# Opción B — K3s
sudo k3s ctr images import /tmp/elasticsearch-8.19.15.tar
sudo k3s ctr images import /tmp/busybox-1.36.tar
```

Verificar que las imágenes quedaron disponibles:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images ls | grep -E "elasticsearch|busybox"

# Opción B — K3s
sudo k3s ctr images list | grep -E "elasticsearch|busybox"
```

## D.4 Usar las imágenes importadas en el Deployment

Reemplazar en el Deployment:

  -----------------------------------------------------------------------
  **Placeholder**       **Valor air-gap**
  --------------------- -------------------------------------------------------------------
  \<ES_IMAGE\>          docker.elastic.co/elasticsearch/elasticsearch:8.19.15

  \<BUSYBOX_IMAGE\>     docker.io/library/busybox:1.36
  -----------------------------------------------------------------------
