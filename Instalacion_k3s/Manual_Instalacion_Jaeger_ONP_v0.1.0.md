**OFICINA DE NORMALIZACIÓN PREVISIONAL**

Oficina de Tecnologías de la Información

**MANUAL DE INSTALACIÓN**

**Jaeger en Kubernetes (K3s)**

Usando Jaeger Operator con Elasticsearch como backend

  -----------------------------------------------------------------------
  **Versión:**                 0.1.0
  ---------------------------- ------------------------------------------
  **Fecha:**                   2026-05-12

  **Clasificación:**           Uso Interno (Técnico)

  **Área responsable:**        OTI
  -----------------------------------------------------------------------

# Historial de versiones

  -----------------------------------------------------------------------------
  **Versión**   **Fecha**    **Autor**          **Descripción**
  ------------- ------------ ------------------ -------------------------------
  0.1.0         2026-05-12   <AUTOR>          Versión inicial del manual

  -----------------------------------------------------------------------------

# 1. Introducción y arquitectura

## 1.1 Propósito del documento

Este manual describe el proceso completo de instalación de Jaeger en un cluster Kubernetes K3s on-premise, utilizando el Jaeger Operator como método de despliegue y Elasticsearch 7.9.1 como backend de almacenamiento de trazas.

El documento está dirigido al equipo de Plataforma de la OTI --- ONP y cubre los entornos DEV, QA y PROD. Los pasos comunes a todos los entornos están en el cuerpo principal del manual. Las configuraciones específicas por entorno se encuentran en los Anexos A, B y C.

## 1.2 ¿Qué es Jaeger?

Jaeger es una plataforma de trazabilidad distribuida de código abierto, originalmente desarrollada por Uber Technologies. Permite monitorear y depurar transacciones en sistemas distribuidos mediante el seguimiento del flujo de una solicitud a través de múltiples servicios.

Jaeger implementa el estándar OpenTracing y es compatible con el protocolo OTLP (OpenTelemetry Protocol), lo que lo convierte en el backend de trazas natural para stacks basados en OpenTelemetry Collector.

## 1.3 ¿Qué es el Jaeger Operator?

El Jaeger Operator es un controlador de Kubernetes que gestiona el ciclo de vida de instancias Jaeger mediante un Custom Resource Definition (CRD) denominado Jaeger. En lugar de desplegar manualmente cada componente (Collector, Query, Agent), el Operator los genera y mantiene automáticamente a partir de una declaración YAML de alto nivel.

Ventajas del uso del Operator frente a manifests directos:

-   Gestión unificada de todos los componentes Jaeger en un solo objeto.

-   Reconciliación automática: si un componente falla o se desvía del estado deseado, el Operator lo corrige.

-   Soporte para múltiples estrategias de despliegue: allInOne (para dev) y production (para producción con Elasticsearch).

-   Simplifica las actualizaciones: basta con cambiar la versión en el CRD.

## 1.4 Arquitectura de observabilidad

Jaeger forma parte del stack completo de observabilidad de ONP. El flujo de datos es el siguiente:

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** La arquitectura completa contempla tres pilares de observabilidad: trazas (Jaeger), métricas (Prometheus) y logs (Elasticsearch/Kibana). Este manual cubre únicamente la instalación de Jaeger. Los demás componentes se documentan en manuales separados.
  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Flujo de datos:

-   La aplicación (servicio web Java/Spring Boot) instrumentada envía telemetría al OTEL Collector mediante el protocolo OTLP (gRPC puerto 4317 / HTTP puerto 4318).

-   El OTEL Collector recibe la telemetría y la enruta según el tipo: trazas a Jaeger, métricas a Prometheus, logs a Elasticsearch.

-   Jaeger almacena las trazas en Elasticsearch y las expone a través de su interfaz web (Jaeger Query UI).

-   Grafana consulta tanto Jaeger como Prometheus para construir dashboards unificados de observabilidad.

-   Kibana consulta Elasticsearch para visualizar los logs de aplicación.

## 1.5 Alcance de este manual

Este manual cubre exclusivamente:

-   Preparación del entorno Kubernetes (namespace, secretos).

-   Mirroring de imágenes al GitLab Container Registry.

-   Instalación del Cert-Manager (dependencia del Jaeger Operator).

-   Instalación del Jaeger Operator.

-   Despliegue de la instancia Jaeger con backend Elasticsearch 7.9.1.

-   Verificación del despliegue.

-   Troubleshooting de errores comunes.

Queda fuera del alcance de este manual:

-   Instalación y configuración de Elasticsearch (se asume que ya existe).

-   Instalación del OTEL Collector.

-   Instalación de Prometheus.

-   Instalación de Grafana.

# 2. Prerrequisitos

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Verificar TODOS los prerrequisitos antes de comenzar la instalación. Una verificación incompleta es la causa más común de fallos durante el despliegue.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 2.1 Infraestructura requerida

  --------------------------------------------------------------------------------------------------
  **Componente**   **Requisito mínimo**          **Observación**
  ---------------- ----------------------------- ---------------------------------------------------
  Kubernetes       K3s v1.27 o superior          Verificar con: kubectl version \--short

  Nodos worker     2 vCPU / 4 GB RAM por nodo    Para entorno PROD se recomienda 4 vCPU / 8 GB RAM

  Almacenamiento   StorageClass disponible       Verificar con: kubectl get storageclass

  Elasticsearch    Versión 7.9.1                 Debe estar accesible desde el cluster

  Traefik          Incluido en K3s por defecto   Solo requerido para PROD (Ingress)
  --------------------------------------------------------------------------------------------------

## 2.2 Herramientas en la máquina de despliegue

Las siguientes herramientas deben estar instaladas en la máquina desde donde se ejecutará el manual:

  -----------------------------------------------------------------------------------
  **Herramienta**   **Versión mínima**           **Verificación**
  ----------------- ---------------------------- ------------------------------------
  kubectl           v1.27+                       kubectl version \--client

  Docker            v24+                         docker \--version

  curl              Cualquier versión reciente   curl \--version

  openssl           Cualquier versión reciente   openssl version
  -----------------------------------------------------------------------------------

## 2.3 Accesos requeridos

Antes de comenzar, el ejecutor del manual debe contar con:

-   Acceso al cluster K3s con permisos de administrador (cluster-admin).

-   Acceso de escritura al GitLab Container Registry del grupo de observabilidad.

-   Acceso a internet o red interna desde la máquina de despliegue (para descarga de imágenes).

-   Credenciales de Elasticsearch: usuario, contraseña, URL y puerto.

-   Acceso de red desde el cluster hacia el host de Elasticsearch (verificar firewall).

## 2.4 Información a recopilar antes de comenzar

Completar la siguiente tabla antes de ejecutar cualquier paso. Los valores recogidos aquí se usarán a lo largo del manual:

  ----------------------------------------------------------------------------------------------
  **Placeholder**           **Descripción**                         **Valor real (completar)**
  ------------------------- --------------------------------------- ----------------------------
  \<GITLAB_REGISTRY_URL\>   URL base del GitLab Registry            

  \<ES_HOST\>               Host o IP de Elasticsearch              

  \<ES_PORT\>               Puerto de Elasticsearch                 9200 (verificar)

  \<ES_USERNAME\>           Usuario de Elasticsearch para Jaeger    

  \<ES_PASSWORD\>           Contraseña del usuario de ES            

  \<ES_TLS\>                ¿ES tiene TLS habilitado?               true / false

  \<ES_SKIP_VERIFY\>        ¿Certificado ES es autofirmado?         true / false

  \<CLUSTER_NAME\>          Nombre del cluster K3s                  

  \<NODEPORT\>              Puerto NodePort para QA (30000-32767)   Solo para QA

  \<INGRESS_HOST\>          Hostname para Ingress de PROD           Solo para PROD

  \<BASIC_AUTH_USER\>       Usuario para Basic Auth PROD            Solo para PROD

  \<BASIC_AUTH_PASS\>       Contraseña para Basic Auth PROD         Solo para PROD

  \<ES_MAJOR_VERSION\>      Versión mayor de Elasticsearch          `8` (Elasticsearch 8.19.15 en el stack ONP)

  \<ES_NUM_SHARDS\>         Número de shards primarios por índice   Ver nota en Sección 8.1

  \<ES_NUM_REPLICAS\>       Número de réplicas por shard            Ver nota en Sección 8.1
  ----------------------------------------------------------------------------------------------

## 2.5 Verificaciones previas

Ejecutar los siguientes comandos para confirmar que el entorno está listo:

### 2.5.1 Verificar conectividad al cluster

```bash
kubectl cluster-info
kubectl get nodes
```

Resultado esperado: todos los nodos deben aparecer en estado Ready.

### 2.5.2 Verificar conectividad a Elasticsearch

```bash
curl -u <ES_USERNAME>:<ES_PASSWORD> http://<ES_HOST>:<ES_PORT>/_cluster/health
```

Resultado esperado: respuesta JSON con status green o yellow. Si el resultado es red o hay error de conexión, detener la instalación y coordinar con el equipo de Plataforma.

### 2.5.3 Verificar StorageClass disponible

```bash
kubectl get storageclass
```

Resultado esperado: al menos una StorageClass listada, idealmente marcada como (default).

### 2.5.4 Verificar acceso al GitLab Registry

```bash
docker login <GITLAB_REGISTRY_URL>
```

Ingresar las credenciales cuando se soliciten. Resultado esperado: Login Succeeded.

# 3. Preparación de imágenes

## Nota: ¿Qué camino seguir?

Esta sección cubre tres escenarios de acceso a imágenes. Identificar el que corresponde al entorno donde se ejecuta la instalación y seguir únicamente esa subsección:

  -----------------------------------------------------------------------
  **Escenario**                  **Cuándo usarlo**                                      **Ir a**
  ------------------------------ ------------------------------------------------------ ----------
  A --- GitLab Registry          PROD y QA. El cluster no tiene salida a internet.       Sección 3.2
                                 Todas las imágenes se publican en el registry privado.

  B --- Internet directo         DEV y pruebas. El nodo tiene acceso a internet.        Sección 3.3
                                 Las imágenes se descargan directamente desde los
                                 registries públicos. No se requiere registry privado.

  C --- Air-gap (sin registry    Entornos completamente aislados sin acceso a            Anexo D
  ni internet)                   internet ni registry. Las imágenes se transfieren
                                 al nodo como archivos .tar.
  -----------------------------------------------------------------------

## 3.1 Imágenes requeridas

Las siguientes **7 imágenes** son necesarias para la instalación completa. Las imágenes `jaeger-all-in-one` y `jaeger-agent` **no se requieren**: `all-in-one` solo aplica a la estrategia `allInOne` (no usada en ONP) y `jaeger-agent` está deprecado desde Jaeger 1.35.

  -----------------------------------------------------------------------------------------------
  **Componente**            **Imagen original**                             **Versión**
  ------------------------- ----------------------------------------------- ---------------------
  Cert-Manager Controller   quay.io/jetstack/cert-manager-controller        v1.14.4

  Cert-Manager Webhook      quay.io/jetstack/cert-manager-webhook           v1.14.4

  Cert-Manager CAInjector   quay.io/jetstack/cert-manager-cainjector        v1.14.4

  Cert-Manager StartupAPI   quay.io/jetstack/cert-manager-startupapicheck   v1.14.4

  Jaeger Operator           jaegertracing/jaeger-operator                   1.56.0

  Jaeger Collector          jaegertracing/jaeger-collector                  1.56.0

  Jaeger Query              jaegertracing/jaeger-query                      1.56.0
  -----------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** Verificar las versiones más recientes estables en https://github.com/jaegertracing/jaeger-operator/releases antes de ejecutar el mirroring. Las versiones indicadas arriba son las recomendadas al momento de elaborar este manual.
  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 3.2 Escenario A --- GitLab Registry (producción)

Ejecutar los siguientes pasos desde una máquina con acceso a internet y Docker instalado. Este proceso se denomina mirroring y debe ejecutarse una sola vez por versión de imagen.

### 3.2.1 Iniciar sesión en el GitLab Registry

```bash
docker login <GITLAB_REGISTRY_URL>
```

### 3.2.2 Mirroring del Cert-Manager

```bash
# Descargar imágenes de Cert-Manager
docker pull quay.io/jetstack/cert-manager-controller:v1.14.4
docker pull quay.io/jetstack/cert-manager-webhook:v1.14.4
docker pull quay.io/jetstack/cert-manager-cainjector:v1.14.4
docker pull quay.io/jetstack/cert-manager-startupapicheck:v1.14.4
# Re-taggear apuntando al registry privado
docker tag quay.io/jetstack/cert-manager-controller:v1.14.4 \
<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-controller:v1.14.4
docker tag quay.io/jetstack/cert-manager-webhook:v1.14.4 \
<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-webhook:v1.14.4
docker tag quay.io/jetstack/cert-manager-cainjector:v1.14.4 \
<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-cainjector:v1.14.4
docker tag quay.io/jetstack/cert-manager-startupapicheck:v1.14.4 \
<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-startupapicheck:v1.14.4
# Publicar en el registry privado
docker push <GITLAB_REGISTRY_URL>/observabilidad/cert-manager-controller:v1.14.4
docker push <GITLAB_REGISTRY_URL>/observabilidad/cert-manager-webhook:v1.14.4
docker push <GITLAB_REGISTRY_URL>/observabilidad/cert-manager-cainjector:v1.14.4
docker push <GITLAB_REGISTRY_URL>/observabilidad/cert-manager-startupapicheck:v1.14.4
```

### 3.2.3 Mirroring del Jaeger Operator y componentes

```bash
# Descargar imágenes de Jaeger
docker pull jaegertracing/jaeger-operator:1.56.0
docker pull jaegertracing/jaeger-collector:1.56.0
docker pull jaegertracing/jaeger-query:1.56.0
# Re-taggear apuntando al registry privado
docker tag jaegertracing/jaeger-operator:1.56.0 \
<GITLAB_REGISTRY_URL>/observabilidad/jaeger-operator:1.56.0
docker tag jaegertracing/jaeger-collector:1.56.0 \
<GITLAB_REGISTRY_URL>/observabilidad/jaeger-collector:1.56.0
docker tag jaegertracing/jaeger-query:1.56.0 \
<GITLAB_REGISTRY_URL>/observabilidad/jaeger-query:1.56.0
# Publicar en el registry privado
docker push <GITLAB_REGISTRY_URL>/observabilidad/jaeger-operator:1.56.0
docker push <GITLAB_REGISTRY_URL>/observabilidad/jaeger-collector:1.56.0
docker push <GITLAB_REGISTRY_URL>/observabilidad/jaeger-query:1.56.0
```

### 3.2.4 Tabla resumen de imágenes en el registry privado

  -------------------------------------------------------------------------------------------------------
  **Componente**            **Imagen en registry privado**
  ------------------------- -----------------------------------------------------------------------------
  Cert-Manager Controller   \<GITLAB_REGISTRY_URL\>/observabilidad/cert-manager-controller:v1.14.4

  Cert-Manager Webhook      \<GITLAB_REGISTRY_URL\>/observabilidad/cert-manager-webhook:v1.14.4

  Cert-Manager CAInjector   \<GITLAB_REGISTRY_URL\>/observabilidad/cert-manager-cainjector:v1.14.4

  Cert-Manager StartupAPI   \<GITLAB_REGISTRY_URL\>/observabilidad/cert-manager-startupapicheck:v1.14.4

  Jaeger Operator           \<GITLAB_REGISTRY_URL\>/observabilidad/jaeger-operator:1.56.0

  Jaeger Collector          \<GITLAB_REGISTRY_URL\>/observabilidad/jaeger-collector:1.56.0

  Jaeger Query              \<GITLAB_REGISTRY_URL\>/observabilidad/jaeger-query:1.56.0
  -------------------------------------------------------------------------------------------------------

### 3.2.5 Crear el ImagePullSecret

El cluster K3s necesita credenciales para acceder al GitLab Registry. Este Secret debe crearse en cada namespace donde se desplieguen componentes del stack.

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=<NAMESPACE>
```

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** Este comando debe ejecutarse una vez por namespace. En este manual se ejecutará nuevamente en la sección de creación de namespaces (Sección 6), donde se indicará el namespace correspondiente a cada entorno.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 3.3 Escenario B --- Internet directo (pruebas y DEV)

Cuando el nodo tiene acceso a internet, las imágenes se descargan directamente desde los registries públicos sin necesidad de mirroring ni de un registry privado.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** No se requieren los pasos de mirroring ni la creación del ImagePullSecret. Pasar directamente a la Sección 4. En las secciones siguientes del manual, donde se indique una imagen con el prefijo `<GITLAB_REGISTRY_URL>/observabilidad/`, usar en su lugar el nombre público de la imagen según la tabla de la Sección 3.1. En el CRD de Jaeger (Sección 8.1) omitir el bloque `imagePullSecrets`.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Referencia de nombres públicos de imagen para este escenario:

  -----------------------------------------------------------------------
  **Componente**            **Imagen a usar directamente**
  ------------------------- ---------------------------------------------
  Cert-Manager Controller   quay.io/jetstack/cert-manager-controller:v1.14.4

  Cert-Manager Webhook      quay.io/jetstack/cert-manager-webhook:v1.14.4

  Cert-Manager CAInjector   quay.io/jetstack/cert-manager-cainjector:v1.14.4

  Cert-Manager StartupAPI   quay.io/jetstack/cert-manager-startupapicheck:v1.14.4

  Jaeger Operator           jaegertracing/jaeger-operator:1.56.0

  Jaeger Collector          jaegertracing/jaeger-collector:1.56.0

  Jaeger Query              jaegertracing/jaeger-query:1.56.0
  -----------------------------------------------------------------------

El manifest de Cert-Manager descargado en la Sección 4 ya referencia las imágenes públicas correctas --- no requiere ningún reemplazo de URLs. El manifest del Jaeger Operator (Sección 5.3) también usa las imágenes públicas por defecto.

## 3.4 Escenario C --- Air-gap (sin registry ni internet)

Para entornos completamente aislados, las imágenes se transfieren al nodo como archivos `.tar`. Ver el **Anexo D** para el procedimiento completo.

# 4. Instalación del Cert-Manager

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **🔴 IMPORTANTE:** El Jaeger Operator requiere Cert-Manager para gestionar los certificados TLS de sus webhooks de validación. Sin Cert-Manager, el Operator no arrancará correctamente.
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 4.1 Verificar si Cert-Manager ya está instalado

Antes de instalar, verificar si Cert-Manager ya existe en el cluster:

```bash
kubectl get pods -n cert-manager
```

Si el resultado muestra pods en estado Running, Cert-Manager ya está instalado. Pasar directamente a la Sección 5.

Si el namespace no existe o no hay pods, continuar con la instalación.

## 4.2 Crear el namespace de Cert-Manager

```bash
kubectl create namespace cert-manager
```

## 4.3 Crear el ImagePullSecret para Cert-Manager

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A (GitLab Registry)**. En los Escenarios B (internet directo) y C (air-gap), omitir esta sección completa y continuar en la Sección 4.4.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=cert-manager
```

## 4.4 Descargar el manifest de Cert-Manager

```bash
curl -L https://github.com/cert-manager/cert-manager/releases/download/v1.14.4/cert-manager.yaml \
-o cert-manager.yaml
```

## 4.5 Actualizar referencias de imágenes en el manifest

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A (GitLab Registry)**. En los Escenarios B y C, el manifest ya referencia las imágenes públicas correctas — omitir esta sección y continuar en la Sección 4.6.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Debido a que el cluster no tiene acceso a internet, se deben reemplazar las referencias de imágenes públicas por las del registry privado. Ejecutar los siguientes reemplazos:

```bash
sed -i \'s|quay.io/jetstack/cert-manager-controller:v1.14.4|<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-controller:v1.14.4|g\' cert-manager.yaml
sed -i \'s|quay.io/jetstack/cert-manager-webhook:v1.14.4|<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-webhook:v1.14.4|g\' cert-manager.yaml
sed -i \'s|quay.io/jetstack/cert-manager-cainjector:v1.14.4|<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-cainjector:v1.14.4|g\' cert-manager.yaml
sed -i \'s|quay.io/jetstack/cert-manager-startupapicheck:v1.14.4|<GITLAB_REGISTRY_URL>/observabilidad/cert-manager-startupapicheck:v1.14.4|g\' cert-manager.yaml
```

## 4.6 Aplicar el manifest

```bash
kubectl apply -f cert-manager.yaml
```

## 4.7 Verificar la instalación

Esperar a que todos los pods estén en estado Running. Esto puede tomar entre 1 y 3 minutos:

```bash
kubectl get pods -n cert-manager -w
```

Resultado esperado:

```bash
NAME READY STATUS RESTARTS AGE
cert-manager-xxxxxxxxx-xxxxx 1/1 Running 0 2m
cert-manager-cainjector-xxxxxxxxx-xxxxx 1/1 Running 0 2m
cert-manager-webhook-xxxxxxxxx-xxxxx 1/1 Running 0 2m
```

  ---------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si algún pod permanece en estado Pending o CrashLoopBackOff, revisar la sección de Troubleshooting (Sección 10) antes de continuar.
  ---------------------------------------------------------------------------------------------------------------------------------------------------------

  ---------------------------------------------------------------------------------------------------------------------------------------------------------

## 4.8 Verificar el webhook de Cert-Manager

```bash
kubectl get validatingwebhookconfiguration
```

Debe aparecer cert-manager-webhook en la lista. Esto confirma que el webhook está registrado y Cert-Manager está operativo.

# 5. Instalación del Jaeger Operator

## 5.1 Crear el namespace para el Operator

El Jaeger Operator se instala en su propio namespace denominado observability, que es el namespace que el Operator monitorea por defecto para los CRDs Jaeger:

```bash
kubectl create namespace observability
```

## 5.2 Crear el ImagePullSecret en el namespace del Operator

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A (GitLab Registry)**. En los Escenarios B y C, omitir y continuar en la Sección 5.3.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=observability
```

## 5.3 Instalar los CRDs del Jaeger Operator

Los CRDs definen el tipo de objeto Jaeger en Kubernetes. Deben instalarse antes que el Operator:

```bash
kubectl create -f https://github.com/jaegertracing/jaeger-operator/releases/download/v1.56.0/jaeger-operator.yaml \
-n observability
```

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si el cluster no tiene acceso a internet para descargar el YAML directamente, descargar el archivo en la máquina de despliegue con curl y aplicarlo con kubectl apply -f jaeger-operator.yaml -n observability
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

```bash
# Alternativa sin acceso a internet desde el cluster:
curl -L https://github.com/jaegertracing/jaeger-operator/releases/download/v1.56.0/jaeger-operator.yaml \
-o jaeger-operator.yaml
# Reemplazar imagen del Operator
sed -i \'s|jaegertracing/jaeger-operator:1.56.0|<GITLAB_REGISTRY_URL>/observabilidad/jaeger-operator:1.56.0|g\' jaeger-operator.yaml
kubectl apply -f jaeger-operator.yaml -n observability
```

## 5.4 Verificar que el Operator está corriendo

```bash
kubectl get pods -n observability -w
```

  -----------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** El manifest de Jaeger Operator 1.56.0 referencia la imagen `gcr.io/kubebuilder/kube-rbac-proxy:v0.13.1` que dejó de estar disponible cuando Google deprecó el registry `gcr.io/kubebuilder/`. Si el pod queda en estado `ImagePullBackOff` con 1/2 contenedores listos, aplicar el siguiente parche antes de continuar:

  ```bash
  kubectl patch deployment jaeger-operator -n observability \
    --type='json' \
    -p='[{"op": "replace", "path": "/spec/template/spec/containers/1/image", "value": "quay.io/brancz/kube-rbac-proxy:v0.13.1"}]'
  ```

  La imagen equivalente se encuentra en `quay.io/brancz/kube-rbac-proxy`, que es el repositorio oficial actual del proyecto. Verificado en instalación real el 2026-05-12.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

Resultado esperado (esperar entre 1 y 2 minutos):

```bash
NAME READY STATUS RESTARTS AGE
jaeger-operator-xxxxxxxxx-xxxxx 1/1 Running 0 90s
```

## 5.5 Verificar los CRDs registrados

```bash
kubectl get crds | grep jaeger
```

Resultado esperado:

```
jaegers.jaegertracing.io 2026-xx-xx
```

La presencia de este CRD confirma que el Operator está correctamente instalado y listo para crear instancias Jaeger.

## 5.6 Verificar los logs del Operator

```bash
kubectl logs -n observability deployment/jaeger-operator
```

Revisar que no haya errores críticos. Es normal ver mensajes informativos sobre el inicio del controlador y la suscripción a eventos del cluster.

# 6. Creación del namespace de telemetría

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** El namespace a crear depende del entorno de destino. Ver los Anexos A, B y C para los valores específicos por entorno. Esta sección documenta los pasos comunes.
  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 6.1 Crear el namespace

```bash
kubectl create namespace <NAMESPACE_TELEMETRI>
```

Donde \<NAMESPACE_TELEMETRI\> es:

-   observabilidad-dev --- para el entorno de desarrollo.

-   observabilidad-qa --- para el entorno de calidad.

-   observabilidad --- para el entorno de producción.

## 6.2 Agregar labels al namespace

Los labels permiten identificar el namespace y habilitar funcionalidades como inyección automática de sidecars en el futuro:

```properties
kubectl label namespace <NAMESPACE_TELEMETRI> \
app.kubernetes.io/managed-by=jaeger-operator \
environment=<ENV> \
team=oti-onp
```

Donde \<ENV\> es dev, qa o prod según corresponda.

## 6.3 Crear el ImagePullSecret en el namespace

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este paso aplica **únicamente al Escenario A (GitLab Registry)**. En los Escenarios B y C, omitir y continuar en la Sección 6.4.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=<NAMESPACE_TELEMETRI>
```

## 6.4 Verificar el namespace

```bash
kubectl get namespace <NAMESPACE_TELEMETRI> \--show-labels
```

Resultado esperado: el namespace aparece con estado Active y los labels definidos en el paso anterior.

# 7. Configuración del Secret de Elasticsearch

El Jaeger Operator necesita las credenciales de Elasticsearch para inyectarlas como variables de entorno en los pods del Collector y Query. Estas credenciales se almacenan en un Kubernetes Secret.

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **🔴 IMPORTANTE:** Nunca incluir contraseñas en texto plano en archivos YAML que se suban a repositorios de código. El Secret debe crearse mediante comandos kubectl o mediante un sistema de gestión de secretos.
  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 7.1 Crear el Secret de credenciales de Elasticsearch

Ejecutar el siguiente comando reemplazando los valores con las credenciales reales proporcionadas por el equipo de Plataforma:

```bash
kubectl create secret generic jaeger-es-credentials \
\--from-literal=ES_PASSWORD=<ES_PASSWORD> \
\--from-literal=ES_USERNAME=<ES_USERNAME> \
\--namespace=<NAMESPACE_TELEMETRI>
```

## 7.2 Verificar que el Secret fue creado

```bash
kubectl get secret jaeger-es-credentials -n <NAMESPACE_TELEMETRI>
```

Resultado esperado:

```
NAME TYPE DATA AGE
jaeger-es-credentials Opaque 2 10s
```

## 7.3 (Opcional) Crear el Secret del certificado CA de Elasticsearch

Si Elasticsearch tiene TLS habilitado con un certificado autofirmado o de una CA interna, es necesario agregar el certificado CA para que Jaeger pueda validarlo:

```bash
# Guardar el certificado CA en un archivo local
# (Solicitarlo al equipo de Plataforma)
# Nombre del archivo: es-ca.crt
kubectl create secret generic jaeger-es-ca \
\--from-file=ca-bundle.crt=./es-ca.crt \
\--namespace=<NAMESPACE_TELEMETRI>
```

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si \<ES_TLS\> es false o \<ES_SKIP_VERIFY\> es true, este paso puede omitirse. Sin embargo, **se recomienda encarecidamente validar el certificado en entornos productivos**.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# 8. Despliegue de la instancia Jaeger

Con el Operator instalado y el Secret de Elasticsearch creado, se procede a desplegar la instancia Jaeger mediante el CRD. El Operator detectará el objeto Jaeger y creará automáticamente todos los componentes necesarios.

## 8.1 Crear el archivo de definición de la instancia Jaeger

Crear el archivo `jaeger-instance.yaml` en el directorio de manifests del entorno correspondiente. Se recomienda mantener todos los manifests organizados por entorno:

```
manifests/
├── observabilidad-dev/
│   └── jaeger-instance.yaml
├── observabilidad-qa/
│   └── jaeger-instance.yaml
└── observabilidad/
    └── jaeger-instance.yaml
```

```bash
mkdir -p manifests/<NAMESPACE_TELEMETRI>
```

Crear el archivo con el siguiente contenido:

El valor de `<JAEGER_COLLECTOR_IMAGE>` y `<JAEGER_QUERY_IMAGE>` depende del escenario de imágenes seguido en la Sección 3:

  -----------------------------------------------------------------------
  **Escenario**          **\<JAEGER_COLLECTOR_IMAGE\>**                                      **\<JAEGER_QUERY_IMAGE\>**
  ---------------------- ------------------------------------------------------------------- -------------------------------------------------------------------
  A --- GitLab Registry  \<GITLAB_REGISTRY_URL\>/observabilidad/jaeger-collector:1.56.0      \<GITLAB_REGISTRY_URL\>/observabilidad/jaeger-query:1.56.0

  B --- Internet directo jaegertracing/jaeger-collector:1.56.0                               jaegertracing/jaeger-query:1.56.0

  C --- Air-gap          jaegertracing/jaeger-collector:1.56.0 (importada vía .tar)          jaegertracing/jaeger-query:1.56.0 (importada vía .tar)
  -----------------------------------------------------------------------

```yaml
apiVersion: jaegertracing.io/v1
kind: Jaeger
metadata:
name: jaeger-onp
namespace: <NAMESPACE_TELEMETRI>
spec:
strategy: production
# ── Imágenes ────────────────────────────────────────────────────────
# Ver tabla de valores por escenario (A/B/C) arriba de este bloque
collector:
image: <JAEGER_COLLECTOR_IMAGE>
replicas: 1
resources:
requests:
cpu: \"200m\"
memory: \"256Mi\"
limits:
cpu: \"500m\"
memory: \"512Mi\"
# ── Configuración del Query (UI) ────────────────────────────────────
query:
image: <JAEGER_QUERY_IMAGE>
replicas: 1
resources:
requests:
cpu: \"100m\"
memory: \"128Mi\"
limits:
cpu: \"300m\"
memory: \"256Mi\"
# ── Configuración de almacenamiento ─────────────────────────────────
storage:
type: elasticsearch
secretName: jaeger-es-credentials
options:
es.server-urls: http://<ES_HOST>:<ES_PORT>
es.version: \"<ES_MAJOR_VERSION>\"
es.index-prefix: jaeger-onp
es.num-shards: \"<ES_NUM_SHARDS>\"
es.num-replicas: \"<ES_NUM_REPLICAS>\"
es.tls.enabled: \"<ES_TLS>\"
es.tls.skip-host-verify: \"<ES_SKIP_VERIFY>\"
# ── Deshabilitar Spark Dependencies ─────────────────────────────────
# El Operator crea este CronJob automáticamente. Requiere un cluster
# Apache Spark que no forma parte del stack ONP. Deshabilitar siempre.
dependencies:
enabled: false
# ── ImagePullSecrets ─────────────────────────────────────────────────
# SOLO Escenario A (GitLab Registry): incluir este bloque
# Escenarios B (internet directo) y C (air-gap): eliminar estas líneas
imagePullSecrets:
\- name: gitlab-registry-secret
```

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** Los valores de resources indicados arriba corresponden a un entorno base. Ver los Anexos para los valores recomendados por entorno (DEV/QA usan recursos mínimos, PROD usa recursos dimensionados).
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA --- es.version:** Usar el número de versión mayor de Elasticsearch: `"8"` para Elasticsearch 8.x (8.19.15 en el stack ONP). Un valor incorrecto provoca errores de compatibilidad en la API de índices.
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA --- es.num-replicas:** En un cluster Elasticsearch de un solo nodo (single-node), usar siempre `"0"`. Con `"1"` o más, los índices quedan en estado `yellow` porque ES no puede alojar réplicas en el mismo nodo que el shard primario. En entornos PROD con múltiples nodos ES, usar `"1"` como mínimo para garantizar alta disponibilidad.
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA --- es.num-shards:** Para entornos DEV y QA con un solo nodo ES, usar `"1"`. Para PROD con múltiples nodos, usar `"3"` como valor base (ajustar según el volumen de trazas).
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA --- dependencies.enabled: false:** El Jaeger Operator crea automáticamente un CronJob llamado `jaeger-onp-spark-dependencies` que analiza trazas para construir un grafo de dependencias entre servicios. Este job requiere un cluster Apache Spark que no forma parte del stack ONP. Sin esta configuración, el CronJob falla en cada ejecución generando pods en estado `Error`. Deshabilitar siempre en todos los entornos ONP.
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 8.2 Aplicar el manifest

```bash
kubectl apply -f manifests/<NAMESPACE_TELEMETRI>/jaeger-instance.yaml
```

Resultado esperado:

```bash
jaeger.jaegertracing.io/jaeger-onp created
```

## 8.3 Monitorear la creación de los componentes

El Operator tardará entre 1 y 3 minutos en crear todos los componentes. Monitorear el progreso con:

```bash
kubectl get pods -n <NAMESPACE_TELEMETRI> -w
```

Resultado esperado cuando el despliegue esté completo:

```bash
NAME READY STATUS RESTARTS
jaeger-onp-collector-xxxxxxxxx-xxxxx 1/1 Running 0
jaeger-onp-query-xxxxxxxxx-xxxxx 1/1 Running 0
```

## 8.4 Verificar los Services creados por el Operator

```bash
kubectl get services -n <NAMESPACE_TELEMETRI>
```

Resultado esperado:

> NAME TYPE CLUSTER-IP PORT(S)
>
> jaeger-onp-collector ClusterIP 10.x.x.x 4317/TCP,4318/TCP,14267/TCP,14268/TCP,14250/TCP
>
> jaeger-onp-collector-headless ClusterIP None 4317/TCP,4318/TCP,14267/TCP,14268/TCP,14250/TCP
>
> jaeger-onp-query ClusterIP 10.x.x.x 16686/TCP,16685/TCP

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** Los puertos más importantes son: 4317 (OTLP gRPC --- usado por el OTEL Collector para enviar trazas), 4318 (OTLP HTTP), 14250 (gRPC Jaeger nativo), 14268 (HTTP Jaeger nativo) y 16686 (Jaeger Query UI).
  --------------------------------------------------------------------------------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si el puerto 4317 no aparece en el Service `jaeger-onp-collector`, el OTEL Collector no podrá enviar trazas a Jaeger. Con Jaeger Operator 1.56 el receptor OTLP está habilitado por defecto. Si no aparece, verificar que la versión del Operator es 1.35 o superior y que el CRD Jaeger no tiene ninguna configuración que lo deshabilite explícitamente.
  --------------------------------------------------------------------------------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------

# 9. Verificación del despliegue

## 9.1 Verificar el estado de todos los componentes

```bash
kubectl get all -n <NAMESPACE_TELEMETRI>
```

Todos los pods deben estar en estado Running y los Deployments deben mostrar READY 1/1.

## 9.2 Verificar los logs del Collector

```bash
kubectl logs -n <NAMESPACE_TELEMETRI> deployment/jaeger-onp-collector \--tail=50
```

Buscar en los logs la línea que confirma la conexión a Elasticsearch:

```bash
# Resultado esperado (fragmento):
\...msg=\"Starting main loop\"\...
\...msg=\"Connected to Elasticsearch\"\...
```

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si aparece el error connection refused o no such host hacia Elasticsearch, verificar que la URL y puerto en el Secret sean correctos y que el firewall permita la conexión desde los pods hacia el host de ES.
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## 9.3 Verificar los logs del Query

```bash
kubectl logs -n <NAMESPACE_TELEMETRI> deployment/jaeger-onp-query \--tail=50
```

No deben aparecer errores de conexión a Elasticsearch.

## 9.4 Acceder al Jaeger UI

El método de acceso depende del entorno. Ver el Anexo correspondiente para las instrucciones específicas. El método más universal para verificar es el port-forward:

```bash
kubectl port-forward -n <NAMESPACE_TELEMETRI> svc/jaeger-onp-query 16686:16686
```

Abrir el navegador en http://localhost:16686. Si la interfaz de Jaeger carga correctamente, el despliegue es exitoso.

## 9.5 Verificar la creación de índices en Elasticsearch

Jaeger crea índices automáticamente en Elasticsearch al recibir las primeras trazas. Verificar que los índices se crearon:

```bash
curl -u <ES_USERNAME>:<ES_PASSWORD> \
http://<ES_HOST>:<ES_PORT>/_cat/indices?v | grep jaeger
```

Nota: los índices solo aparecen después de que Jaeger haya recibido al menos una traza. Si aún no se han enviado trazas, el resultado puede estar vacío.

## 9.6 Enviar una traza de prueba

Para confirmar el flujo completo, se envía una traza de prueba usando el endpoint Zipkin del Collector (formato JSON, compatible con `curl` sin dependencias adicionales):

```bash
# Port-forward al endpoint Zipkin del Collector
kubectl port-forward -n <NAMESPACE_TELEMETRI> svc/jaeger-onp-collector 9411:9411 &

# Enviar una traza de prueba en formato Zipkin v2
curl -X POST http://localhost:9411/api/v2/spans \
  -H "Content-Type: application/json" \
  -d '[{
    "traceId": "a3ce929d0e0e4736a3ce929d0e0e4736",
    "id": "a3ce929d0e0e4736",
    "name": "test-manual-span",
    "timestamp": '"$(date +%s%6N)"',
    "duration": 1000,
    "localEndpoint": {"serviceName": "servicio-prueba-onp"}
  }]'
```

La respuesta esperada es HTTP 202 sin body. Luego verificar en el Jaeger UI (http://localhost:16686) que la traza aparece bajo el servicio `servicio-prueba-onp`.

# 10. Troubleshooting

## 10.1 El Operator no arranca (CrashLoopBackOff)

### Síntoma

```bash
kubectl get pods -n observability
NAME READY STATUS RESTARTS
jaeger-operator-xxxxxxxxx-xxxxx 0/1 CrashLoopBackOff 5
```

### Causas y soluciones

-   Cert-Manager no está instalado o no está completamente listo: ejecutar kubectl get pods -n cert-manager y esperar a que todos estén Running antes de instalar el Operator.

-   Los webhooks de Cert-Manager no respondieron: ejecutar kubectl delete pod -n cert-manager \--all para forzar el reinicio de Cert-Manager y esperar a que se recupere.

-   El ImagePullSecret no existe o tiene credenciales incorrectas: verificar con kubectl get secret gitlab-registry-secret -n observability.

## 10.2 Los pods de Jaeger quedan en Pending

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME READY STATUS RESTARTS
jaeger-onp-collector-xxxxx 0/1 Pending 0
```

### Causas y soluciones

-   Sin recursos suficientes en los nodos: ejecutar kubectl describe pod \<nombre-pod\> -n \<NAMESPACE\> y revisar el campo Events. Si aparece Insufficient cpu o Insufficient memory, reducir los requests en el CRD Jaeger o agregar nodos al cluster.

-   Sin StorageClass disponible: ejecutar kubectl get storageclass y verificar que existe al menos una.

## 10.3 Error de conexión a Elasticsearch

### Síntoma

```bash
# En los logs del Collector:
error: failed to connect to Elasticsearch: dial tcp <ES_HOST>:<ES_PORT>: connect: connection refused
```

### Causas y soluciones

-   URL o puerto incorrectos en el CRD: verificar el valor de es.server-urls en el objeto Jaeger con kubectl get jaeger jaeger-onp -n \<NAMESPACE\> -o yaml.

-   Credenciales incorrectas: verificar el Secret con kubectl get secret jaeger-es-credentials -n \<NAMESPACE\> -o jsonpath=\'{.data.ES_USERNAME}\' \| base64 -d.

-   Firewall bloqueando la conexión: solicitar al equipo de Plataforma que verifique las reglas de red entre el cluster y el host de Elasticsearch.

## 10.4 Error de imagen no encontrada (ErrImagePull)

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME READY STATUS RESTARTS
jaeger-onp-collector-xxxxx 0/1 ErrImagePull 0
```

### Causas y soluciones

-   La imagen no fue subida al registry: verificar que el mirroring de la Sección 3 se completó correctamente.

-   El ImagePullSecret no existe en el namespace: ejecutar kubectl get secret gitlab-registry-secret -n \<NAMESPACE\>. Si no existe, crearlo con el comando de la Sección 6.3.

-   Credenciales del registry expiradas o incorrectas: actualizar el Secret con kubectl delete secret gitlab-registry-secret -n \<NAMESPACE\> y recrearlo.

## 10.5 El Jaeger UI no muestra trazas

### Síntoma

El UI carga correctamente pero al buscar servicios no aparece ninguno o aparece Service unavailable.

### Causas y soluciones

-   No se han enviado trazas aún: el UI solo muestra servicios que han enviado al menos una traza. Ejecutar la prueba de la Sección 9.6.

-   El OTEL Collector no está apuntando al Collector de Jaeger: verificar la configuración del OTEL Collector (Manual 1) y que el exporter de Jaeger apunte al Service jaeger-onp-collector:14250.

-   Los índices de Elasticsearch no se crearon: ejecutar el comando de verificación de la Sección 9.5.

## 10.6 Pods `jaeger-onp-es-index-cleaner` en estado Error

### Síntoma

```bash
kubectl get pods -n <NAMESPACE>
NAME                                         READY   STATUS      RESTARTS
jaeger-onp-es-index-cleaner-XXXXXXX-xxxxx   0/1     Error       0
jaeger-onp-es-index-cleaner-XXXXXXX-yyyyy   0/1     Completed   0
```

### Qué es este pod

El Jaeger Operator crea automáticamente un CronJob llamado `jaeger-onp-es-index-cleaner` que borra índices de Elasticsearch más antiguos de 7 días. Se ejecuta una vez al día según el schedule configurado en el CRD.

### Pods en Error históricos (normal)

Los pods en estado `Error` de ejecuciones anteriores a que Elasticsearch estuviera completamente disponible son normales. Kubernetes los limpia automáticamente manteniendo solo el último fallo (`failedJobsHistoryLimit: 1`). Si hay varios pods Error pero al menos uno `Completed`, el job está funcionando correctamente y los residuos desaparecerán solos.

### Si los pods Error siguen acumulándose después de la instalación

Si no hay ningún pod `Completed` y los errores continúan, revisar los logs del job:

```bash
kubectl logs -n <NAMESPACE> <nombre-pod-es-index-cleaner>
```

Causas comunes:

- Elasticsearch no está disponible: verificar con `kubectl get pods -n elastic-dev`.
- Credenciales incorrectas en el Secret `jaeger-es-credentials`: verificar usuario y contraseña.
- El índice aún no existe (no se han recibido trazas): es normal en instalaciones nuevas. El job fallará hasta que Jaeger escriba al menos un índice en ES.

## 10.7 Comandos útiles de diagnóstico rápido  

  -----------------------------------------------------------------------------------------------------------------------------
  **Diagnóstico**                                    **Comando**
  -------------------------------------------------- --------------------------------------------------------------------------
  Ver estado de todos los recursos en el namespace   kubectl get all -n \<NAMESPACE\>

  Ver eventos del namespace (errores recientes)      kubectl get events -n \<NAMESPACE\> \--sort-by=\'.lastTimestamp\'

  Ver detalle de un pod con problemas                kubectl describe pod \<nombre-pod\> -n \<NAMESPACE\>

  Ver logs del Operator                              kubectl logs -n observability deployment/jaeger-operator

  Ver el CRD Jaeger generado                         kubectl get jaeger jaeger-onp -n \<NAMESPACE\> -o yaml

  Reiniciar el Operator                              kubectl rollout restart deployment/jaeger-operator -n observability

  Reiniciar el Collector                             kubectl rollout restart deployment/jaeger-onp-collector -n \<NAMESPACE\>
  -----------------------------------------------------------------------------------------------------------------------------

# 11. Exposición del servicio (acceso permanente)

`kubectl port-forward` es válido únicamente para pruebas locales desde la máquina de despliegue. Para que el equipo acceda al Jaeger UI de forma permanente se requiere uno de los siguientes métodos.

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** K3s incluye Traefik como Ingress controller por defecto. No se requiere instalar ningún componente adicional para usar la Opción B.
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## 11.1 Opción A: NodePort (recomendado para DEV)

Agrega un Service de tipo NodePort al namespace. No reemplaza el Service ClusterIP existente — ambos coexisten.

Crear el archivo `manifests/<NAMESPACE>/nodeport-jaeger-query.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: jaeger-query-nodeport
  namespace: <NAMESPACE>
  labels:
    app: jaeger
spec:
  type: NodePort
  selector:
    app.kubernetes.io/component: query
    app.kubernetes.io/instance: <NOMBRE_INSTANCIA_JAEGER>
  ports:
    - port: 16686
      targetPort: 16686
      nodePort: 30686
```

> **⚠️ ADVERTENCIA — El selector debe coincidir con los labels reales del pod:**
> El Jaeger Operator genera los pods con labels que dependen del nombre de la instancia Jaeger definida en el CRD (campo `metadata.name`). Antes de aplicar el YAML, verificar los labels reales del pod Query con:
>
> ```bash
> kubectl get pods -n <NAMESPACE> -l app.kubernetes.io/component=query --show-labels
> ```
>
> El valor de `app.kubernetes.io/instance` debe coincidir exactamente con el nombre de tu instancia Jaeger (por ejemplo `jaeger-onp`). Si el selector no coincide, el servicio quedará sin endpoints y el Jaeger UI no será accesible aunque el pod esté en estado `Running`.
>
> Para verificar que el servicio tiene endpoints después de aplicarlo:
>
> ```bash
> kubectl get endpoints jaeger-query-nodeport -n <NAMESPACE>
> ```
>
> El resultado debe mostrar una IP en la columna `ENDPOINTS`. Si aparece `<none>`, el selector no está matcheando — revisar los labels del pod con el comando anterior.

```bash
kubectl apply -f manifests/<NAMESPACE>/nodeport-jaeger-query.yaml
```

Verificar:

```bash
kubectl get svc -n <NAMESPACE>
```

Resultado esperado (entre los servicios existentes):

```
NAME                    TYPE        CLUSTER-IP    PORT(S)           AGE
jaeger-query-nodeport   NodePort    10.x.x.x      16686:30686/TCP   10s
```

Acceder desde cualquier máquina de la red interna:

```
http://<IP_NODO_K3S>:30686
```

## 11.2 Opción B: Ingress con Traefik (recomendado para QA/PROD)

Expone el servicio por nombre DNS. Requiere que el hostname esté registrado en el DNS interno de la organización, o agregado manualmente en `/etc/hosts` de las máquinas del equipo.

Crear el archivo `manifests/<NAMESPACE>/ingress-jaeger-query.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: jaeger-query
  namespace: <NAMESPACE>
  annotations:
    traefik.ingress.kubernetes.io/router.entrypoints: web
spec:
  rules:
    - host: <JAEGER_HOSTNAME>
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: jaeger-onp-query
                port:
                  number: 16686
```

```bash
kubectl apply -f manifests/<NAMESPACE>/ingress-jaeger-query.yaml
```

Donde `<JAEGER_HOSTNAME>` según el entorno:

  -----------------------------------------------------------------------
  **Entorno**   **Hostname sugerido**
  ------------- ---------------------------------------------------------
  DEV           jaeger-dev.onp.interno
  QA            jaeger-qa.onp.interno
  PROD          jaeger.onp.interno
  -----------------------------------------------------------------------

Verificar el Ingress:

```bash
kubectl get ingress -n <NAMESPACE>
```

Resultado esperado:

```
NAME           CLASS     HOSTS                      ADDRESS      PORTS   AGE
jaeger-query   traefik   jaeger-dev.onp.interno     <IP_NODO>    80      10s
```

Acceder desde cualquier máquina de la red (con el hostname resuelto en DNS o `/etc/hosts`):

```
http://jaeger-dev.onp.interno
```

# Anexo A --- Configuración específica para DEV

  -----------------------------------------------------------------------
  **Parámetro**          **Valor para DEV**
  ---------------------- ------------------------------------------------
  Namespace              observabilidad-dev

  Cluster                Cluster compartido DEV/QA

  Estrategia Jaeger      production

  Exposición del UI      kubectl port-forward

  Autenticación          Ninguna

  Replicas Collector     1

  Replicas Query         1
  -----------------------------------------------------------------------

## A.1 Crear el namespace DEV

```bash
kubectl create namespace observabilidad-dev
kubectl label namespace observabilidad-dev environment=dev team=oti-onp
```

## A.2 Crear el ImagePullSecret en observabilidad-dev

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=observabilidad-dev
```

## A.3 Recursos recomendados para DEV

En DEV se usan recursos mínimos para no consumir capacidad del cluster compartido. Ajustar el CRD con los siguientes valores:

  ------------------------------------------------------------------------------------------
  **Componente**   **CPU Request**   **CPU Limit**   **Memory Request**   **Memory Limit**
  ---------------- ----------------- --------------- -------------------- ------------------
  Collector        100m              200m            128Mi                256Mi

  Query            50m               100m            64Mi                 128Mi
  ------------------------------------------------------------------------------------------

## A.4 Acceder al Jaeger UI en DEV

En DEV el acceso al UI se realiza exclusivamente mediante port-forward. Este método es temporal y requiere mantener el terminal abierto mientras se usa:

```bash
kubectl port-forward -n observabilidad-dev svc/jaeger-onp-query 16686:16686
```

Abrir el navegador en: http://localhost:16686

  ----------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** El port-forward se cierra cuando se interrumpe el proceso (Ctrl+C) o se cierra el terminal. No es adecuado para uso permanente.
  ----------------------------------------------------------------------------------------------------------------------------------------------

  ----------------------------------------------------------------------------------------------------------------------------------------------

# Anexo B --- Configuración específica para QA

  -----------------------------------------------------------------------
  **Parámetro**          **Valor para QA**
  ---------------------- ------------------------------------------------
  Namespace              observabilidad-qa

  Cluster                Cluster compartido DEV/QA

  Estrategia Jaeger      production

  Exposición del UI      NodePort

  Autenticación          Ninguna

  Replicas Collector     1

  Replicas Query         1
  -----------------------------------------------------------------------

## B.1 Crear el namespace QA

```bash
kubectl create namespace observabilidad-qa
kubectl label namespace observabilidad-qa environment=qa team=oti-onp
```

## B.2 Crear el ImagePullSecret en observabilidad-qa

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=observabilidad-qa
```

## B.3 Configurar exposición NodePort

Para exponer el Jaeger UI en QA mediante NodePort, agregar la siguiente sección al CRD Jaeger (jaeger-instance.yaml) antes de aplicarlo:

```
query:
serviceType: NodePort
```

Una vez desplegado, obtener el NodePort asignado:

```bash
kubectl get svc -n observabilidad-qa jaeger-onp-query
```

Resultado esperado:

```
NAME TYPE CLUSTER-IP EXTERNAL-IP PORT(S)
jaeger-onp-query NodePort 10.x.x.x <none> 16686:<NODEPORT>/TCP
```

Acceder al UI desde cualquier máquina con acceso a la red del cluster:

```
http://<IP-NODO-K3S>:<NODEPORT>
```

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ℹ️ NOTA:** El NodePort asignado por Kubernetes es un número entre 30000 y 32767. Si se desea un puerto fijo, especificarlo en el CRD con nodePort: \<NODEPORT\> bajo spec.query.
  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## B.4 Recursos recomendados para QA

  ------------------------------------------------------------------------------------------
  **Componente**   **CPU Request**   **CPU Limit**   **Memory Request**   **Memory Limit**
  ---------------- ----------------- --------------- -------------------- ------------------
  Collector        150m              300m            128Mi                256Mi

  Query            100m              200m            128Mi                256Mi
  ------------------------------------------------------------------------------------------

# Anexo C --- Configuración específica para PROD

  -----------------------------------------------------------------------
  **Parámetro**          **Valor para PROD**
  ---------------------- ------------------------------------------------
  Namespace              observabilidad

  Cluster                Cluster exclusivo de producción

  Estrategia Jaeger      production

  Exposición del UI      Ingress con Traefik

  Autenticación          Basic Auth (mínimo) u OAuth2 Proxy

  Replicas Collector     2 (alta disponibilidad)

  Replicas Query         2 (alta disponibilidad)
  -----------------------------------------------------------------------

## C.1 Crear el namespace PROD

```bash
kubectl create namespace observabilidad
kubectl label namespace observabilidad environment=prod team=oti-onp
```

## C.2 Crear el ImagePullSecret en observabilidad

```bash
kubectl create secret docker-registry gitlab-registry-secret \
\--docker-server=<GITLAB_REGISTRY_URL> \
\--docker-username=<GITLAB_USER> \
\--docker-password=<GITLAB_TOKEN> \
\--docker-email=<GITLAB_EMAIL> \
\--namespace=observabilidad
```

## C.3 Verificar Traefik en K3s

K3s incluye Traefik como Ingress Controller por defecto. Verificar que está activo:

```bash
kubectl get pods -n kube-system | grep traefik
```

Resultado esperado: un pod traefik-xxxxxxxxx-xxxxx en estado Running.

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si Traefik fue deshabilitado durante la instalación de K3s o fue reemplazado por otro Ingress Controller (nginx, etc.), la configuración del Ingress en esta sección debe ajustarse según el controlador instalado. Verificar con el equipo de Plataforma.
  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## C.4 Configurar autenticación Basic Auth

Paso 1: Generar el hash de la contraseña para Basic Auth:

```bash
# Instalar htpasswd si no está disponible
# En Ubuntu/Debian: sudo apt-get install apache2-utils
htpasswd -nb <BASIC_AUTH_USER> <BASIC_AUTH_PASS>
```

El comando produce una salida similar a: usuario:\$apr1\$xxxxxxxx\$xxxxxxxxxxxxxxxxx

Paso 2: Crear el Secret con las credenciales hasheadas:

```bash
kubectl create secret generic jaeger-basic-auth \
\--from-literal=users=\'<HASH_GENERADO_EN_PASO_1>\' \
\--namespace=observabilidad
```

Paso 3: Crear el Middleware de Traefik para Basic Auth:

```yaml
apiVersion: traefik.io/v1alpha1
kind: Middleware
metadata:
name: jaeger-basic-auth
namespace: observabilidad
spec:
basicAuth:
secret: jaeger-basic-auth
# Guardar como jaeger-auth-middleware.yaml y aplicar:
kubectl apply -f jaeger-auth-middleware.yaml
```

## C.5 Configurar el Ingress con Traefik

Agregar la siguiente sección al CRD Jaeger para habilitar el Ingress:

> ingress:
>
> enabled: true
>
> annotations:
>
> traefik.ingress.kubernetes.io/router.middlewares: observabilidad-jaeger-basic-auth@kubernetescrd
>
> hosts:
>
> \- \<INGRESS_HOST\>

Alternativamente, crear un Ingress separado:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
name: jaeger-ingress
namespace: observabilidad
annotations:
traefik.ingress.kubernetes.io/router.middlewares: observabilidad-jaeger-basic-auth@kubernetescrd
spec:
rules:
\- host: <INGRESS_HOST>
http:
paths:
\- path: /
pathType: Prefix
backend:
service:
name: jaeger-onp-query
port:
number: 16686
```

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **⚠️ ADVERTENCIA:** Si la institución cuenta con un proveedor de identidad (Keycloak, Azure AD u otro IDP), se recomienda reemplazar el Basic Auth por OAuth2 Proxy para una autenticación más robusta y centralizada. Coordinar con el equipo de Plataforma para su configuración.
  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

## C.6 Recursos recomendados para PROD

  ------------------------------------------------------------------------------------------
  **Componente**   **CPU Request**   **CPU Limit**   **Memory Request**   **Memory Limit**
  ---------------- ----------------- --------------- -------------------- ------------------
  Collector (x2)   500m              1000m           512Mi                1Gi

  Query (x2)       300m              500m            256Mi                512Mi
  ------------------------------------------------------------------------------------------

## C.7 Verificar el Ingress

```bash
kubectl get ingress -n observabilidad
```

Resultado esperado:

```
NAME CLASS HOSTS ADDRESS PORTS
jaeger-ingress <none> <INGRESS_HOST> <IP-NODO> 80
```

Acceder al UI desde el navegador en: http://\<INGRESS_HOST\>

El navegador debe solicitar usuario y contraseña (Basic Auth configurado en C.4).

# Anexo D --- Instalación en entorno air-gap (sin registry ni internet)

Este anexo cubre el caso en que el nodo no tiene acceso a internet y no existe un registry privado disponible. Las imágenes se transfieren al nodo como archivos `.tar` y se importan directamente en el runtime de contenedores (containerd).

  -----------------------------------------------------------------------
  **ℹ️ NOTA:** Este procedimiento requiere una máquina auxiliar con acceso a internet y Docker instalado, desde donde se descargan y empaquetan las imágenes. Luego se transfieren al nodo por cualquier medio disponible (scp, USB, carpeta compartida, etc.).
  -----------------------------------------------------------------------

  -----------------------------------------------------------------------

## D.1 Descargar y empaquetar las imágenes (máquina con internet)

Ejecutar en la máquina auxiliar:

```bash
# Cert-Manager
docker pull quay.io/jetstack/cert-manager-controller:v1.14.4
docker pull quay.io/jetstack/cert-manager-webhook:v1.14.4
docker pull quay.io/jetstack/cert-manager-cainjector:v1.14.4
docker pull quay.io/jetstack/cert-manager-startupapicheck:v1.14.4

# Jaeger
docker pull jaegertracing/jaeger-operator:1.56.0
docker pull jaegertracing/jaeger-collector:1.56.0
docker pull jaegertracing/jaeger-query:1.56.0

# Empaquetar en archivos .tar
docker save quay.io/jetstack/cert-manager-controller:v1.14.4        -o cert-manager-controller.tar
docker save quay.io/jetstack/cert-manager-webhook:v1.14.4           -o cert-manager-webhook.tar
docker save quay.io/jetstack/cert-manager-cainjector:v1.14.4        -o cert-manager-cainjector.tar
docker save quay.io/jetstack/cert-manager-startupapicheck:v1.14.4   -o cert-manager-startupapicheck.tar
docker save jaegertracing/jaeger-operator:1.56.0                     -o jaeger-operator.tar
docker save jaegertracing/jaeger-collector:1.56.0                    -o jaeger-collector.tar
docker save jaegertracing/jaeger-query:1.56.0                        -o jaeger-query.tar
```

## D.2 Transferir los archivos al nodo

Copiar los 7 archivos `.tar` al nodo. Ejemplo usando scp:

```bash
scp *.tar <USUARIO>@<IP-NODO-K3S>:/tmp/jaeger-images/
```

## D.3 Importar las imágenes en el nodo

Kubernetes usa containerd internamente. Ejecutar según la distribución del cluster:

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images import /tmp/jaeger-images/cert-manager-controller.tar
sudo ctr -n k8s.io images import /tmp/jaeger-images/cert-manager-webhook.tar
sudo ctr -n k8s.io images import /tmp/jaeger-images/cert-manager-cainjector.tar
sudo ctr -n k8s.io images import /tmp/jaeger-images/cert-manager-startupapicheck.tar
sudo ctr -n k8s.io images import /tmp/jaeger-images/jaeger-operator.tar
sudo ctr -n k8s.io images import /tmp/jaeger-images/jaeger-collector.tar
sudo ctr -n k8s.io images import /tmp/jaeger-images/jaeger-query.tar

# Opción B — K3s
sudo k3s ctr images import /tmp/jaeger-images/cert-manager-controller.tar
sudo k3s ctr images import /tmp/jaeger-images/cert-manager-webhook.tar
sudo k3s ctr images import /tmp/jaeger-images/cert-manager-cainjector.tar
sudo k3s ctr images import /tmp/jaeger-images/cert-manager-startupapicheck.tar
sudo k3s ctr images import /tmp/jaeger-images/jaeger-operator.tar
sudo k3s ctr images import /tmp/jaeger-images/jaeger-collector.tar
sudo k3s ctr images import /tmp/jaeger-images/jaeger-query.tar
```

## D.4 Verificar que las imágenes están disponibles

```bash
# Opción A — Kubernetes estándar con containerd
sudo ctr -n k8s.io images ls | grep -E "cert-manager|jaeger"

# Opción B — K3s
sudo k3s ctr images list | grep -E "cert-manager|jaeger"
```

Resultado esperado: las 7 imágenes aparecen listadas con su nombre y versión.

## D.5 Continuar con la instalación

Una vez importadas las imágenes, continuar desde la Sección 4 del manual. Tener en cuenta:

-   En el manifest de Cert-Manager (Sección 4): **no reemplazar** las URLs de las imágenes --- los nombres ya coinciden con las imágenes importadas.

-   En el manifest del Jaeger Operator (Sección 5.3): **no reemplazar** la imagen del Operator.

-   En el CRD de Jaeger (Sección 8.1): usar los nombres públicos (`jaegertracing/jaeger-collector:1.56.0`, `jaegertracing/jaeger-query:1.56.0`) --- son los mismos que se importaron.

-   **Omitir** la creación del ImagePullSecret y el bloque `imagePullSecrets` del CRD.
