**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Kibana en Kubernetes (K3s)**

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

Este manual describe el proceso completo de instalación de Kibana en un cluster Kubernetes K3s on-premise, utilizando manifests Kubernetes directos.

El documento está dirigido al equipo de Plataforma de la OTI --- ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes están en el cuerpo principal. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Kibana?

Kibana es la interfaz de visualización y exploración de datos del stack Elastic. Permite consultar, filtrar y analizar los logs almacenados en Elasticsearch mediante búsquedas, dashboards y herramientas de desarrollo.

## 1.3 Rol de Kibana en el stack ONP

```
OTEL Collector → Elasticsearch (índice: onp-logs-<entorno>)
                        ↑
                     Kibana  ← equipo de operaciones y desarrollo
```

Kibana es la interfaz principal para explorar los **logs** del stack ONP. No recibe datos directamente de los servicios — consulta a Elasticsearch que ya los tiene almacenados.

## 1.4 Kibana es stateless

A diferencia de Elasticsearch, Prometheus o Grafana, Kibana **no necesita PVC**. Toda su configuración persistente (data views, dashboards, configuraciones guardadas) se almacena en el índice `.kibana` dentro de Elasticsearch. Reiniciar el pod de Kibana no pierde ningún dato.

## 1.5 Usuario kibana_system

Kibana usa el usuario built-in `kibana_system` de Elasticsearch para sus operaciones internas. Este usuario tiene permisos limitados y dedicados — es la práctica correcta en lugar de usar el superusuario `elastic`. Su contraseña se configura en Elasticsearch antes de desplegar Kibana (Sección 5).

## 1.6 Claves de cifrado

Kibana 8.x requiere tres claves de cifrado de mínimo 32 caracteres para proteger objetos guardados, tokens de sesión y reportes. Sin estas claves, Kibana arranca pero muestra advertencias y ciertas funciones no están disponibles. En el stack ONP se configuran desde el inicio mediante un Secret de Kubernetes.

## 1.7 Alcance de este manual

Este manual cubre:

-   Activación del usuario `kibana_system` en Elasticsearch.
-   Creación de Secrets para credenciales y claves de cifrado.
-   ConfigMap con la configuración de Kibana (`kibana.yml`).
-   Despliegue de Kibana (sin PVC) y Service.
-   Exposición mediante NodePort e Ingress.
-   Creación de la Data View para logs (`onp-logs-*`).
-   Troubleshooting de errores comunes.

Queda fuera del alcance:

-   Creación de dashboards de logs (responsabilidad del equipo de desarrollo y operaciones).
-   Configuración de alertas en Kibana.
-   Integración con sistemas de autenticación externos (LDAP, SSO).

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

  Elasticsearch           8.19.15 operativo              Ver Manual de Instalación de Elasticsearch

  ILM configurado         Política onp-logs-policy       Ver Sección 9 del Manual de Elasticsearch

  StorageClass            No requerida                   Kibana no usa PVC
  --------------------------------------------------------------------------------------------------

## 2.2 Herramientas en la máquina de despliegue

  -----------------------------------------------------------------------------------
  **Herramienta**   **Versión mínima**           **Verificación**
  ----------------- ---------------------------- ------------------------------------
  kubectl           v1.27+                       kubectl version \--client

  curl              Cualquier versión reciente   curl \--version

  openssl           Cualquier versión reciente   openssl version
  -----------------------------------------------------------------------------------

## 2.3 Información a recopilar antes de comenzar

  -----------------------------------------------------------------------------------------------------------------------
  **Placeholder**                  **Descripción**                                    **Valor real (completar)**
  -------------------------------- -------------------------------------------------- ----------------------------
  \<GITLAB_REGISTRY_URL\>          URL base del GitLab Registry

  \<NAMESPACE\>                    Namespace del stack Elastic (compartido con ES)      elastic-dev / elastic-qa / elastic

  \<KIBANA_VERSION\>               Versión de Kibana                                   8.19.15

  \<ELASTIC_PASSWORD\>             Contraseña del usuario `elastic` de ES

  \<KIBANA_SYSTEM_PASSWORD\>       Contraseña nueva para el usuario `kibana_system`

  \<KIBANA_ENCRYPTION_KEY_1\>      Clave de cifrado para objetos guardados (32+ chars) Ver Sección 6.1

  \<KIBANA_ENCRYPTION_KEY_2\>      Clave de cifrado para seguridad (32+ chars)         Ver Sección 6.1

  \<KIBANA_ENCRYPTION_KEY_3\>      Clave de cifrado para reportes (32+ chars)          Ver Sección 6.1

  \<KIBANA_HOSTNAME\>              URL pública de Kibana. DEV: `kubectl get nodes -o wide` → columna `INTERNAL-IP` + `:30560`. QA/PROD: hostname del Ingress (ej. `kibana-dev.onp.interno`)
  -----------------------------------------------------------------------------------------------------------------------

## 2.4 Verificaciones previas

### 2.4.1 Verificar que Elasticsearch está operativo

```bash
kubectl get pods -n <NAMESPACE>
```

Resultado esperado:

```
NAME                             READY   STATUS    RESTARTS
elasticsearch-xxxxxxxxx-xxxxx    1/1     Running   0
```

### 2.4.2 Verificar la versión de Elasticsearch

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> http://localhost:9200/ | grep '"number"'
```

Resultado esperado:

```
"number" : "8.19.15",
```

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** La versión de Kibana debe coincidir **exactamente** con la versión de Elasticsearch. Una discrepancia de versión impide que Kibana arranque.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

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
  **Componente**   **Imagen original**                          **Versión**
  ---------------- -------------------------------------------- --------------------
  Kibana           docker.elastic.co/kibana/kibana               8.19.15
  -----------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** La versión de Kibana debe ser idéntica a la de Elasticsearch instalada. Verificar la versión activa con el comando de la Sección 2.4.2.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 3.2 Escenario A --- GitLab Registry (producción)

```bash
docker login <GITLAB_REGISTRY_URL>

docker pull docker.elastic.co/kibana/kibana:<KIBANA_VERSION>

docker tag docker.elastic.co/kibana/kibana:<KIBANA_VERSION> \
  <GITLAB_REGISTRY_URL>/observabilidad/kibana:<KIBANA_VERSION>

docker push <GITLAB_REGISTRY_URL>/observabilidad/kibana:<KIBANA_VERSION>
```

## 3.3 Escenario B --- Internet directo (pruebas y DEV)

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** No se requieren pasos de mirroring. Pasar directamente a la Sección 4. En el Deployment (Sección 8.1) usar la imagen pública directamente y mantener `imagePullSecrets` comentado.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **Componente**   **Imagen a usar directamente**
  ---------------- -------------------------------------------
  Kibana           docker.elastic.co/kibana/kibana:8.19.15
  -----------------------------------------------------------------------

## 3.4 Escenario C --- Air-gap

Ver **Anexo D** para el procedimiento completo de transferencia de imagen vía archivo `.tar`.

# 4. Verificación del namespace

Kibana comparte el namespace con Elasticsearch. El namespace ya fue creado durante la instalación de Elasticsearch (ver Manual de Instalación de Elasticsearch, Sección 4).

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Si el namespace aún no existe, crearlo siguiendo la Sección 4 del Manual de Instalación de Elasticsearch antes de continuar.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 4.1 Crear el ImagePullSecret (solo Escenario A)

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A**. En los Escenarios B y C omitir y continuar en la Sección 4.2. Si el ImagePullSecret ya fue creado durante la instalación de Elasticsearch en este mismo namespace, omitir este paso.
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

## 4.2 Verificar el namespace

```bash
kubectl get namespace <NAMESPACE> --show-labels
```

Resultado esperado:

```
NAME          STATUS   AGE   LABELS
elastic-dev    Active   Xs    app.kubernetes.io/managed-by=oti-onp,environment=dev,team=oti-onp
```

# 5. Activación del usuario kibana_system en Elasticsearch

El usuario `kibana_system` es un usuario built-in de Elasticsearch con permisos específicos para Kibana. Antes de desplegar Kibana hay que establecer su contraseña.

  -----------------------------------------------------------------------
  **🔴 IMPORTANTE:** Este paso se ejecuta **en el namespace de Elasticsearch**, no en el de Kibana. Requiere la contraseña del usuario `elastic`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 5.1 Verificar que kibana_system existe y está habilitado

El usuario `kibana_system` es un usuario built-in de Elasticsearch 8.x — viene incluido por defecto y no hay que crearlo. Solo hay que verificar que está habilitado antes de establecer su contraseña:

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_security/user/kibana_system" | grep '"enabled"'
```

Resultado esperado:

```
"enabled":true
```

Si no aparece `"enabled":true`, revisar según el caso:

**Caso A — Elasticsearch no está corriendo**

Verificar el estado del pod:

```bash
kubectl get pods -n <NAMESPACE>
```

Si el pod no está en estado `Running`, revisar los logs:

```bash
kubectl logs -n <NAMESPACE> deployment/elasticsearch --tail=50
```

Resolver el error antes de continuar (ver Sección 10 de la Guía de Instalación de Elasticsearch).

**Caso B — Credenciales del usuario `elastic` incorrectas**

Verificar el valor real almacenado en el Secret:

```bash
kubectl get secret elasticsearch-credentials -n <NAMESPACE> \
  -o jsonpath='{.data.ELASTIC_PASSWORD}' | base64 -d
echo
```

Si el valor es distinto al usado en el comando de verificación, repetir el comando usando la contraseña correcta. Si se desconoce la contraseña, se puede cambiar con:

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD_ACTUAL> \
  -X POST "http://localhost:9200/_security/user/elastic/_password" \
  -H "Content-Type: application/json" \
  -d '{"password":"<NUEVA_PASSWORD>"}'
```

Luego actualizar el Secret para mantener coherencia:

```bash
kubectl create secret generic elasticsearch-credentials \
  --from-literal=ELASTIC_PASSWORD=<NUEVA_PASSWORD> \
  -n <NAMESPACE> \
  --dry-run=client -o yaml | kubectl apply -f -
```

**Caso C — Resultado vacío (sin `"enabled"` en la respuesta)**

El usuario `kibana_system` podría estar deshabilitado explícitamente (raro en instalaciones limpias). Habilitarlo con:

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  -X PUT "http://localhost:9200/_security/user/kibana_system/_enable"
```

Resultado esperado:

```json
{"found":true}
```

Repetir la verificación del paso 5.1 antes de continuar.

## 5.2 Establecer la contraseña de kibana_system

> **Importante:** Elegir una contraseña fuerte para `<KIBANA_SYSTEM_PASSWORD>` y anotarla — esta **misma contraseña** se usará al crear el Secret `kibana-es-credentials` en la Sección 6. Si no coinciden, Kibana no podrá autenticarse contra Elasticsearch.

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  -X POST "http://localhost:9200/_security/user/kibana_system/_password" \
  -H "Content-Type: application/json" \
  -d '{"password": "<KIBANA_SYSTEM_PASSWORD>"}'
```

Resultado esperado:

```
Defaulted container "elasticsearch" out of: elasticsearch, increase-vm-max-map (init)
{}
```

> **Nota:** El mensaje `Defaulted container "elasticsearch"...` es informativo de kubectl (el pod tiene un init container, kubectl indica cuál contenedor usó). No es un error. La respuesta relevante es `{}`, que indica que la contraseña fue establecida correctamente.

## 5.3 Verificar el usuario kibana_system

La verificación se hace en dos partes:

**Parte 1 — Confirmar que kibana_system sigue habilitado** (usando el superusuario `elastic`):

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -u elastic:<ELASTIC_PASSWORD> \
  "http://localhost:9200/_security/user/kibana_system" | grep '"enabled"'
```

Resultado esperado:

```
"enabled":true
```

**Parte 2 — Confirmar que la contraseña es correcta** (usando `kibana_system`):

```bash
kubectl exec -n <NAMESPACE> deployment/elasticsearch -- \
  curl -s -o /dev/null -w "%{http_code}" \
  -u kibana_system:<KIBANA_SYSTEM_PASSWORD> \
  "http://localhost:9200/"
```

Resultado esperado:

```
200
```

> **Nota:** El usuario `kibana_system` no tiene permisos para consultar la API de seguridad (`_security/user/...`) — eso es correcto por diseño. Un código `200` en el endpoint raíz confirma que la contraseña es válida. Un código `401` indica contraseña incorrecta — repetir la Sección 5.2.

# 6. Creación de Secrets

## 6.1 Generar las claves de cifrado

Kibana requiere tres claves de cifrado independientes de mínimo 32 caracteres. Generarlas con:

```bash
openssl rand -hex 32  # Ejecutar tres veces, una por cada clave
```

Guardar los tres valores generados — se usarán en el siguiente paso.

  -----------------------------------------------------------------------
  **🔴 IMPORTANTE:** Las claves de cifrado deben ser estables. Si se cambian después de que Kibana esté en uso, los objetos guardados cifrados (sesiones, alertas) quedarán inaccesibles.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 6.2 Secret de credenciales de Elasticsearch

```bash
kubectl create secret generic kibana-es-credentials \
  --from-literal=KIBANA_SYSTEM_PASSWORD=<KIBANA_SYSTEM_PASSWORD> \
  --namespace=<NAMESPACE>
```

## 6.3 Secret de claves de cifrado

```bash
kubectl create secret generic kibana-encryption-keys \
  --from-literal=KIBANA_ENCRYPTION_KEY_1=<KIBANA_ENCRYPTION_KEY_1> \
  --from-literal=KIBANA_ENCRYPTION_KEY_2=<KIBANA_ENCRYPTION_KEY_2> \
  --from-literal=KIBANA_ENCRYPTION_KEY_3=<KIBANA_ENCRYPTION_KEY_3> \
  --namespace=<NAMESPACE>
```

## 6.4 Verificar los Secrets

```bash
kubectl get secrets -n <NAMESPACE>
```

Resultado esperado:

```
NAME                      TYPE     DATA   AGE
kibana-es-credentials     Opaque   1      10s
kibana-encryption-keys    Opaque   3      10s
```

# 7. ConfigMap de configuración (kibana.yml)

Antes de crear el archivo, obtener la IP del nodo donde corre K3s:

```bash
kubectl get nodes -o wide
```

La columna `INTERNAL-IP` contiene la IP a usar. El valor de `<KIBANA_HOSTNAME>` será:

- **Con NodePort (DEV/QA):** `<IP_NODO>:30560` — ejemplo: `192.168.1.10:30560`
- **Con Ingress:** el hostname configurado en el Ingress — ejemplo: `kibana-dev.onp.interno`

Crear el archivo `manifests/<NAMESPACE>/03-kibana-configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kibana-config
  namespace: <NAMESPACE>
data:
  kibana.yml: |
    server.name: kibana
    server.host: "0.0.0.0"
    server.publicBaseUrl: "http://<KIBANA_HOSTNAME>"

    elasticsearch.hosts: ["http://elasticsearch.<NAMESPACE>.svc.cluster.local:9200"]
    elasticsearch.username: "kibana_system"
    elasticsearch.password: "${KIBANA_SYSTEM_PASSWORD}"

    xpack.encryptedSavedObjects.encryptionKey: "${KIBANA_ENCRYPTION_KEY_1}"
    xpack.security.encryptionKey: "${KIBANA_ENCRYPTION_KEY_2}"
    xpack.reporting.encryptionKey: "${KIBANA_ENCRYPTION_KEY_3}"

    logging.root.level: warn
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Los valores `${KIBANA_SYSTEM_PASSWORD}`, `${KIBANA_ENCRYPTION_KEY_1}`, `${KIBANA_ENCRYPTION_KEY_2}` y `${KIBANA_ENCRYPTION_KEY_3}` **no son placeholders a reemplazar**. Es la sintaxis de Kibana para leer variables de entorno en tiempo de ejecución. Kibana los resolverá desde los Secrets inyectados en el Deployment (Sección 8.1).

  El placeholder `<KIBANA_HOSTNAME>` sí debe reemplazarse con el hostname o IP de acceso. Ver Sección 9 y Anexos para los valores por entorno.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Antes de aplicar, reemplazar los placeholders: `<NAMESPACE>`, `<NAMESPACE>` y `<KIBANA_HOSTNAME>`. Ver Anexos A/B/C para los valores por entorno.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

```bash
kubectl apply -f manifests/<NAMESPACE>/03-kibana-configmap.yaml
```

Resultado esperado:

```
configmap/kibana-config created
```

# 8. Despliegue de Kibana

## 8.1 Crear el Deployment

Crear el archivo `manifests/<NAMESPACE>/04-kibana-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kibana
  namespace: <NAMESPACE>
  labels:
    app: kibana
    environment: <ENV>
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kibana
  template:
    metadata:
      labels:
        app: kibana
        environment: <ENV>
    spec:
      containers:
        - name: kibana
          image: <IMAGE>
          ports:
            - name: http
              containerPort: 5601
          env:
            - name: KIBANA_SYSTEM_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kibana-es-credentials
                  key: KIBANA_SYSTEM_PASSWORD
            - name: KIBANA_ENCRYPTION_KEY_1
              valueFrom:
                secretKeyRef:
                  name: kibana-encryption-keys
                  key: KIBANA_ENCRYPTION_KEY_1
            - name: KIBANA_ENCRYPTION_KEY_2
              valueFrom:
                secretKeyRef:
                  name: kibana-encryption-keys
                  key: KIBANA_ENCRYPTION_KEY_2
            - name: KIBANA_ENCRYPTION_KEY_3
              valueFrom:
                secretKeyRef:
                  name: kibana-encryption-keys
                  key: KIBANA_ENCRYPTION_KEY_3
          volumeMounts:
            - name: config
              mountPath: /usr/share/kibana/config/kibana.yml
              subPath: kibana.yml
          resources:
            requests:
              cpu: "<CPU_REQUEST>"
              memory: "<MEMORY_REQUEST>"
            limits:
              cpu: "<CPU_LIMIT>"
              memory: "<MEMORY_LIMIT>"
          readinessProbe:
            httpGet:
              path: /api/status
              port: 5601
            initialDelaySeconds: 60
            periodSeconds: 15
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: /api/status
              port: 5601
            initialDelaySeconds: 120
            periodSeconds: 30
      volumes:
        - name: config
          configMap:
            name: kibana-config
      # Escenario A únicamente (GitLab Registry privado):
      # imagePullSecrets:
      #   - name: gitlab-registry-secret
```

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** El placeholder `<IMAGE>` debe reemplazarse con la imagen del escenario correspondiente. Ver Anexos A/B/C. Kibana tarda entre 60 y 90 segundos en arrancar — el `initialDelaySeconds` de los probes está ajustado para esto.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Antes de aplicar, verificar que todos los placeholders fueron reemplazados: `<NAMESPACE>`, `<ENV>`, `<IMAGE>`, `<CPU_REQUEST>`, `<CPU_LIMIT>`, `<MEMORY_REQUEST>` y `<MEMORY_LIMIT>`. Ver Anexos A/B/C.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 8.2 Aplicar el Deployment

```bash
kubectl apply -f manifests/<NAMESPACE>/04-kibana-deployment.yaml
```

Resultado esperado:

```
deployment.apps/kibana created
```

## 8.3 Crear el Service ClusterIP

Crear el archivo `manifests/<NAMESPACE>/05-kibana-service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: kibana
  namespace: <NAMESPACE>
  labels:
    app: kibana
spec:
  selector:
    app: kibana
  ports:
    - name: http
      port: 5601
      targetPort: 5601
```

## 8.4 Aplicar el Service

```bash
kubectl apply -f manifests/<NAMESPACE>/05-kibana-service.yaml
```

Resultado esperado:

```
service/kibana created
```

## 8.5 Verificar el despliegue

Kibana tarda entre 60 y 90 segundos en arrancar. Monitorear:

```bash
kubectl get pods -n <NAMESPACE> -w
```

Resultado esperado:

```
NAME                      READY   STATUS    RESTARTS   AGE
kibana-xxxxxxxxx-xxxxx    1/1     Running   0          90s
```

```bash
kubectl get services -n <NAMESPACE>
```

Resultado esperado:

```
NAME     TYPE        CLUSTER-IP    PORT(S)
kibana   ClusterIP   10.x.x.x      5601/TCP
```

# 9. Exposición del servicio

Kibana es una interfaz de usuario — el equipo debe acceder a ella desde fuera del cluster. A diferencia de Elasticsearch (que es interno), la exposición de Kibana es **obligatoria**.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** K3s incluye Traefik como Ingress controller por defecto. No se requiere instalar ningún componente adicional para usar la Opción B.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 9.1 Opción A: NodePort (recomendado para DEV)

Crear el archivo `manifests/<NAMESPACE>/06-kibana-nodeport.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: kibana-nodeport
  namespace: <NAMESPACE>
  labels:
    app: kibana
spec:
  type: NodePort
  selector:
    app: kibana
  ports:
    - port: 5601
      targetPort: 5601
      nodePort: 30560
```

```bash
kubectl apply -f manifests/<NAMESPACE>/06-kibana-nodeport.yaml
```

Verificar:

```bash
kubectl get svc -n <NAMESPACE>
```

Resultado esperado:

```
NAME             TYPE        CLUSTER-IP    PORT(S)          AGE
kibana           ClusterIP   10.x.x.x      5601/TCP         5m
kibana-nodeport  NodePort    10.x.x.x      5601:30560/TCP   10s
```

Verificar que el servicio tiene endpoints activos:

```bash
kubectl get endpoints kibana-nodeport -n <NAMESPACE>
```

El resultado debe mostrar una IP en la columna `ENDPOINTS`. Si aparece `<none>`, el selector no está matcheando los pods — verificar los labels reales con `kubectl get pods -n <NAMESPACE> --show-labels`.

Acceder desde cualquier máquina de la red interna:

```
http://<IP_NODO_K3S>:30560
```

Kibana mostrará una pantalla de inicio de sesión. Usar las credenciales del superusuario de Elasticsearch:

- **Usuario:** `elastic`
- **Contraseña:** la almacenada en el Secret `elasticsearch-credentials`

Para obtener la contraseña:

```bash
kubectl get secret elasticsearch-credentials -n <NAMESPACE> \
  -o jsonpath='{.data.ELASTIC_PASSWORD}' | base64 -d
echo
```

## 9.2 Opción B: Ingress con Traefik (recomendado para QA/PROD)

Crear el archivo `manifests/<NAMESPACE>/06-kibana-ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: kibana
  namespace: <NAMESPACE>
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: web
spec:
  rules:
    - host: <KIBANA_HOSTNAME>
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: kibana
                port:
                  number: 5601
```

```bash
kubectl apply -f manifests/<NAMESPACE>/06-kibana-ingress.yaml
```

Donde `<KIBANA_HOSTNAME>` según el entorno:

  -----------------------------------------------------------------------
  **Entorno**   **Hostname sugerido**
  ------------- ---------------------------------------------------------
  DEV           elastic-dev.onp.interno
  QA            elastic-qa.onp.interno
  PROD          kibana.onp.interno
  -----------------------------------------------------------------------

Verificar:

```bash
kubectl get ingress -n <NAMESPACE>
```

Resultado esperado:

```
NAME     CLASS     HOSTS                     ADDRESS      PORTS   AGE
kibana   traefik   elastic-dev.onp.interno    <IP_NODO>    80      10s
```

# 10. Verificación del despliegue

## 10.1 Verificar los logs de Kibana

```bash
kubectl logs -n <NAMESPACE> deployment/kibana --tail=20
```

Al iniciar correctamente, Kibana muestra líneas similares a:

```
[INFO ][plugins.licensing] License information fetched successfully
[INFO ][kibana]  http server running at http://0.0.0.0:5601
```

La línea clave es `http server running at`.

## 10.2 Acceder a la UI de Kibana

Abrir en el navegador la URL según el método de exposición elegido (Sección 9):

```
http://<IP_NODO_K3S>:30560          ← NodePort (DEV)
http://elastic-dev.onp.interno        ← Ingress (QA/PROD)
```

Iniciar sesión con el usuario `elastic` y su contraseña (`<ELASTIC_PASSWORD>`).

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Kibana solicita iniciar sesión con el usuario `elastic` (superusuario) para la administración de la plataforma. El usuario `kibana_system` es interno — no se usa para iniciar sesión en la UI.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 10.3 Crear la Data View para logs

> **⚠️ PREREQUISITO — No ejecutar este paso aún si los servicios backend no están instrumentados.**
>
> La Data View `onp-logs-*` solo puede crearse cuando ya existe al menos un índice con ese patrón en Elasticsearch. Ese índice se crea automáticamente la primera vez que el OTEL Collector recibe logs de un servicio y los reenvía a ES.
>
> Para que esto ocurra se requiere completar previamente:
> 1. **Instrumentar los servicios backend** (Spring Boot u otros) con el agente Java de OpenTelemetry, habilitando el exportador de logs (`otel.logs.exporter=otlp`).
> 2. **Levantar los servicios** y generar tráfico para que comiencen a emitir logs.
> 3. **Verificar** que el OTEL Collector recibió y procesó los logs hacia ES.
>
> Si se intenta crear la Data View antes de esto, Kibana mostrará el error: *"Name must match one or more data streams, indices, or index aliases"*. Esto no indica un problema con la instalación — solo que aún no hay datos indexados.
>
> Una vez completados los pasos anteriores, continuar con esta sección.

La Data View conecta Kibana con los índices de logs del OTEL Collector.

En la UI de Kibana navegar a: **Management → Stack Management → Kibana → Data Views → Create data view**

Completar los campos:

  -----------------------------------------------------------------------
  **Campo**         **Valor**
  ----------------- -----------------------------------------------------
  Name              ONP Logs
  Index pattern     onp-logs-\*
  Timestamp field   @timestamp
  -----------------------------------------------------------------------

Hacer clic en **Save data view to Kibana**.

### Alternativa: crear la Data View via API

```bash
kubectl port-forward -n <NAMESPACE> svc/kibana 5601:5601 &
```

Esperar a que aparezca `Forwarding from 127.0.0.1:5601 -> 5601`. Luego:

```bash
curl -X POST -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/data_views/data_view \
  -H "Content-Type: application/json" \
  -H "kbn-xsrf: true" \
  -d '{
    "data_view": {
      "title": "onp-logs-*",
      "name": "ONP Logs",
      "timeFieldName": "@timestamp"
    }
  }'
```

Resultado esperado:

```json
{"data_view":{"id":"...","title":"onp-logs-*","name":"ONP Logs","timeFieldName":"@timestamp",...}}
```

## 10.4 Verificar los logs en Discover

En la UI de Kibana navegar a: **Analytics → Discover**

Seleccionar la Data View **ONP Logs**. Si el OTEL Collector ya está enviando logs, aparecerán los registros en la vista de tiempo. Si no hay logs aún, el índice `onp-logs-*` aparecerá vacío hasta que la aplicación comience a enviar datos.

## 10.5 Actualizar los campos del Data View

Cuando los servicios incorporan nuevos campos en sus logs (por ejemplo, al agregar el `CanonicalRequestLogFilter` de la Guía de Desarrollo), Kibana no los muestra automáticamente en Discover hasta que el Data View se refresca.

**Cuándo hacer este paso:** la primera vez que se despliegue un servicio que emita el log canónico de request, o cualquier vez que aparezcan campos nuevos que Kibana no reconoce.

En la UI de Kibana navegar a: **Stack Management → Kibana → Data Views → ONP Logs**

Hacer clic en el botón **Refresh field list** (ícono de recarga arriba a la derecha).

Los campos nuevos (`duration_ms`, `http.response.status_code`, `user.id`, etc.) quedarán disponibles en Discover, en los filtros y en los dashboards.

**Alternativa via API:**

```bash
kubectl port-forward -n <NAMESPACE> svc/kibana 5601:5601 &

# Obtener el ID del Data View (del resultado del paso 10.3 o buscar con:)
curl -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/data_views \
  -H "kbn-xsrf: true" | grep '"id"'

# Refrescar los campos del Data View
curl -X POST -u elastic:<ELASTIC_PASSWORD> \
  http://localhost:5601/api/data_views/data_view/<DATA_VIEW_ID>/fields \
  -H "kbn-xsrf: true"
```

Resultado esperado: HTTP 200.

# 11. Troubleshooting

## 11.1 Kibana queda en CrashLoopBackOff

### Síntoma

```
kubectl get pods -n <NAMESPACE>
NAME                 READY   STATUS             RESTARTS
kibana-xxxxx         0/1     CrashLoopBackOff   3
```

### Causas y soluciones

-   La versión de Kibana no coincide con la de Elasticsearch: verificar ambas versiones.
-   El Secret `kibana-es-credentials` no existe o tiene la clave incorrecta: verificar con `kubectl get secrets -n <NAMESPACE>`.
-   Las claves de cifrado tienen menos de 32 caracteres: verificar con `kubectl get secret kibana-encryption-keys -n <NAMESPACE> -o jsonpath='{.data.KIBANA_ENCRYPTION_KEY_1}' | base64 -d | wc -c`.

## 11.2 Kibana arranca pero no conecta a Elasticsearch

### Síntoma

En los logs aparece:

```
[ERROR][elasticsearch-service] Unable to retrieve version information from Elasticsearch nodes.
```

### Causas y soluciones

-   La URL de Elasticsearch en el ConfigMap es incorrecta: verificar `<NAMESPACE>` en `kibana.yml`.
-   La contraseña de `kibana_system` es incorrecta: repetir el paso de la Sección 5.1 con la contraseña correcta.
-   Elasticsearch no está corriendo: verificar con `kubectl get pods -n <NAMESPACE>`.

## 11.3 La Data View no encuentra índices

### Síntoma

Al crear la Data View `onp-logs-*`, Kibana indica que no hay índices que coincidan.

### Causas y soluciones

-   El OTEL Collector aún no ha enviado logs: es normal si la aplicación Spring Boot no está instrumentada aún. Los índices se crean al llegar el primer log.
-   El OTEL Collector no está corriendo: verificar con `kubectl get pods -n otel-dev`.
-   El nombre del índice en el OTEL Collector no coincide con `onp-logs-*`: verificar `logs_index` en el ConfigMap del OTEL Collector.

## 11.4 Advertencias de claves de cifrado al arrancar

### Síntoma

En los logs aparece:

```
[WARN] Generating a random key for xpack.encryptedSavedObjects.encryptionKey.
```

### Causa y solución

Las claves de cifrado no están configuradas. Verificar que el Secret `kibana-encryption-keys` existe y que las variables de entorno están siendo inyectadas:

```bash
kubectl exec -n <NAMESPACE> deployment/kibana -- env | grep KIBANA_ENCRYPTION
```

## 11.5 Comandos útiles de diagnóstico rápido

  -----------------------------------------------------------------------
  **Diagnóstico**                          **Comando**
  ---------------------------------------- ------------------------------
  Ver estado del pod                       kubectl get pods -n \<NAMESPACE\>

  Ver logs de Kibana                       kubectl logs -n \<NAMESPACE\> deployment/kibana \--tail=50

  Ver eventos del namespace                kubectl get events -n \<NAMESPACE\> \--sort-by=\'.lastTimestamp\'

  Ver variables de entorno del pod         kubectl exec -n \<NAMESPACE\> deployment/kibana \-- env \| grep KIBANA

  Reiniciar Kibana                         kubectl rollout restart deployment/kibana -n \<NAMESPACE\>

  Verificar estado via API                 curl -u elastic:\<PASSWORD\> http://localhost:5601/api/status
  -----------------------------------------------------------------------

# Anexo A --- Configuración específica para DEV

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para DEV**
  ------------------------- ---------------------------------------------
  Namespace                 elastic-dev

  Namespace Elasticsearch   elastic-dev

  Kibana hostname           \<IP\_NODO\_K3S\>:30560 (NodePort)
  -----------------------------------------------------------------------

## A.1 Crear el namespace DEV

```bash
kubectl create namespace elastic-dev
kubectl label namespace elastic-dev \
  app.kubernetes.io/managed-by=oti-onp \
  environment=dev \
  team=oti-onp
```

## A.2 Valores del ConfigMap para DEV

  -----------------------------------------------------------------------
  **Placeholder**           **Valor DEV**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             elastic-dev

  \<NAMESPACE_ES\>          elastic-dev

  \<KIBANA_HOSTNAME\>       \<IP\_NODO\_K3S\>:30560
  -----------------------------------------------------------------------

## A.3 Recursos recomendados para DEV

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor DEV**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             docker.elastic.co/kibana/kibana:8.19.15 (Escenario B)

  \<ENV\>               dev

  \<CPU_REQUEST\>       300m

  \<CPU_LIMIT\>         500m

  \<MEMORY_REQUEST\>    512Mi

  \<MEMORY_LIMIT\>      1Gi
  ------------------------------------------------------------------------------------------

# Anexo B --- Configuración específica para QA

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para QA**
  ------------------------- ---------------------------------------------
  Namespace                 elastic-qa

  Namespace Elasticsearch   elastic-qa

  Kibana hostname           elastic-qa.onp.interno (Ingress)
  -----------------------------------------------------------------------

## B.1 Crear el namespace QA

```bash
kubectl create namespace elastic-qa
kubectl label namespace elastic-qa \
  app.kubernetes.io/managed-by=oti-onp \
  environment=qa \
  team=oti-onp
```

## B.2 Valores del ConfigMap para QA

  -----------------------------------------------------------------------
  **Placeholder**           **Valor QA**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             elastic-qa

  \<NAMESPACE_ES\>          elastic-qa

  \<KIBANA_HOSTNAME\>       elastic-qa.onp.interno
  -----------------------------------------------------------------------

## B.3 Recursos recomendados para QA

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor QA**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             \<GITLAB_REGISTRY_URL\>/observabilidad/kibana:8.19.15

  \<ENV\>               qa

  \<CPU_REQUEST\>       500m

  \<CPU_LIMIT\>         1000m

  \<MEMORY_REQUEST\>    1Gi

  \<MEMORY_LIMIT\>      1500Mi
  ------------------------------------------------------------------------------------------

# Anexo C --- Configuración específica para PROD

  -----------------------------------------------------------------------
  **Parámetro**             **Valor para PROD**
  ------------------------- ---------------------------------------------
  Namespace                 elastic

  Namespace Elasticsearch   elastic

  Kibana hostname           kibana.onp.interno (Ingress)
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Al igual que Elasticsearch, Kibana no aplica HPA. Es stateless en Kubernetes pero depende de un único Elasticsearch — escalar Kibana sin escalar ES no mejora el rendimiento.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## C.1 Crear el namespace PROD

```bash
kubectl create namespace kibana
kubectl label namespace kibana \
  app.kubernetes.io/managed-by=oti-onp \
  environment=prod \
  team=oti-onp
```

## C.2 Valores del ConfigMap para PROD

  -----------------------------------------------------------------------
  **Placeholder**           **Valor PROD**
  ------------------------- ---------------------------------------------
  \<NAMESPACE\>             kibana

  \<NAMESPACE_ES\>          elastic

  \<KIBANA_HOSTNAME\>       kibana.onp.interno
  -----------------------------------------------------------------------

## C.3 Recursos recomendados para PROD

  ------------------------------------------------------------------------------------------
  **Placeholder**       **Valor PROD**
  --------------------- --------------------------------------------------------------------
  \<IMAGE\>             \<GITLAB_REGISTRY_URL\>/observabilidad/kibana:8.19.15

  \<ENV\>               prod

  \<CPU_REQUEST\>       500m

  \<CPU_LIMIT\>         1000m

  \<MEMORY_REQUEST\>    1Gi

  \<MEMORY_LIMIT\>      2Gi
  ------------------------------------------------------------------------------------------

# Anexo D --- Air-gap: transferencia de imagen sin internet ni registry

## D.1 Exportar la imagen en una máquina con internet

```bash
docker pull docker.elastic.co/kibana/kibana:8.19.15
docker save docker.elastic.co/kibana/kibana:8.19.15 -o kibana-8.19.15.tar
```

## D.2 Transferir el archivo al nodo

```bash
scp kibana-8.19.15.tar <USUARIO>@<IP_NODO>:/tmp/
```

## D.3 Importar la imagen en el nodo

Ejecutar según el runtime del cluster:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images import /tmp/kibana-8.19.15.tar

# Opción B — K3s
sudo k3s ctr images import /tmp/kibana-8.19.15.tar
```

Verificar que la imagen quedó disponible:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images ls | grep kibana

# Opción B — K3s
sudo k3s ctr images list | grep kibana
```

## D.4 Usar la imagen importada en el Deployment

  -----------------------------------------------------------------------
  **Placeholder**   **Valor air-gap**
  ----------------- -------------------------------------------------------------------
  \<IMAGE\>         docker.elastic.co/kibana/kibana:8.19.15
  -----------------------------------------------------------------------
