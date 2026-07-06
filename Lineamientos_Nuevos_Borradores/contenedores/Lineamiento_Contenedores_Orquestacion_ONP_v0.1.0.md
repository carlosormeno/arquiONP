# LIN-K8S-001 — Lineamiento de Contenedores y Orquestación ONP

**Código:** LIN-K8S-001  
**Versión:** v0.1.3  
**Estado:** Borrador  
**Fecha:** 2026-07-06  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Plataforma/Infraestructura, Seguridad Digital, Desarrollo, Arquitectura  
**Marco rector:** LIN-ARQ-000 — Marco Rector de Diseño y Arquitectura de Software  
**Antecedente institucional:** INFORME-000082-2023-OTI.ID — Lineamientos de contenedores y orquestador de contenedores  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-26 | Arquitectura OTI | Borrador inicial del lineamiento de contenedores y orquestación |
| v0.1.1 | 2026-05-28 | Arquitectura OTI | Normaliza el lenguaje de despliegue hacia plan de reversa y elimina ambigüedad con rollback de base de datos |
| v0.1.2 | 2026-07-06 | Arquitectura OTI | Incorpora sección 4.3 (Runtime de contenedores: containerd/crictl) para reconciliar con LIN-ARQ-000 §11.1 y ADR-009, que ya establecían containerd como runtime de producción sin que este lineamiento lo reflejara operativamente. Añade términos al glosario. |
| v0.1.3 | 2026-07-06 | Arquitectura OTI | Incorpora sección 4.4 (Convención de nombres de namespace: `<sistema>-<componente>`, sin sufijo de ambiente, confirmado un clúster K8s por ambiente). Cierra el vacío señalado en `Matriz_Propiedad_Documental` donde este lineamiento figuraba como dueño de la política de namespaces sin haberla definido. Señala como pendiente de verificación con Plataforma la posible inconsistencia de sufijos de ambiente en namespaces de infraestructura compartida (`otel-{env}` en LIN-OBS-001, `kafka-{env}` en LIN-BUS-001) |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)  
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)  
3. [Principios rectores](#3-principios-rectores)  

**Parte I — Construcción y empaquetado de contenedores**

4. [Modelo de uso de contenedores en ONP](#4-modelo-de-uso-de-contenedores-en-onp)  
5. [Construcción de imágenes de contenedor](#5-construcción-de-imágenes-de-contenedor)  
6. [Repositorio, versionamiento y promoción de imágenes](#6-repositorio-versionamiento-y-promoción-de-imágenes)  
7. [Configuración por ambiente](#7-configuración-por-ambiente)  
8. [Secretos y datos sensibles](#8-secretos-y-datos-sensibles)  

**Parte II — Orquestación con Kubernetes**

9. [Manifiestos Kubernetes mínimos](#9-manifiestos-kubernetes-mínimos)  
10. [Recursos: requests, limits y escalamiento](#10-recursos-requests-limits-y-escalamiento)  
11. [Health checks y ciclo de vida del pod](#11-health-checks-y-ciclo-de-vida-del-pod)  
12. [Red, servicios e ingreso](#12-red-servicios-e-ingreso)  
13. [Persistencia y volúmenes](#13-persistencia-y-volúmenes)  
14. [Seguridad del contenedor y del pod](#14-seguridad-del-contenedor-y-del-pod)  
15. [Observabilidad mínima](#15-observabilidad-mínima)  

**Parte III — Gobierno y conformidad**

16. [Documentación obligatoria por ambiente](#16-documentación-obligatoria-por-ambiente)  
17. [Relación con CI/CD e IaC](#17-relación-con-cicd-e-iac)  
18. [Checklist de conformidad](#18-checklist-de-conformidad)  
19. [Anti-patrones](#19-anti-patrones)  
20. [Proceso ADR para desviaciones](#20-proceso-adr-para-desviaciones)  
21. [Glosario](#21-glosario)  
22. [Anexos](#22-anexos)
    - Anexo A — Estructura sugerida de manifiestos (Kustomize / Helm)
    - Anexo B — Ejemplo de `secret.example.yaml`
    - Anexo C — Comando referencial de escaneo con Trivy
    - Anexo D — nginx.conf mínimo para Angular SPA
    - Anexo E — NetworkPolicy mínima para servicio backend
    - Anexo F — Relación con el antecedente 2023  

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece las reglas técnicas mínimas para construir, empaquetar, configurar, desplegar y operar aplicaciones contenerizadas en la ONP.

Su propósito es convertir los criterios generales existentes sobre contenedores y orquestador de contenedores en reglas verificables, alineadas con el marco de arquitectura, seguridad, observabilidad, pruebas y desarrollo de software de la ONP.

El lineamiento busca garantizar que las soluciones contenerizadas sean:

- mantenibles;
- portables;
- seguras;
- observables;
- configurables por ambiente;
- reproducibles;
- operables en Kubernetes;
- trazables desde la imagen hasta el despliegue.

### 1.2 Alcance

Aplica a:

| Componente | Aplica |
|---|---|
| Servicios backend Java / Spring Boot | Sí |
| Aplicaciones frontend Angular empaquetadas como contenedor web | Sí |
| Jobs o workers contenerizados | Sí |
| Adaptadores de integración desplegados como contenedores | Sí |
| Servicios auxiliares de aplicación | Sí |
| Soluciones nuevas desplegadas en Kubernetes | Sí |
| Modernización de componentes legacy hacia contenedores | Sí, cuando aplique |
| Bases de datos productivas dentro de contenedores | No, salvo ambientes de desarrollo/prueba o excepción aprobada |
| Herramientas de plataforma administradas por Infraestructura | Solo como referencia, no como estándar de aplicación |

### 1.3 Fuera de alcance

| Tema | Documento / responsable |
|---|---|
| Definición de arquitectura de aplicación | LIN-ARQ-000 |
| Convenciones de código Java | LIN-DEV-JAVA-001 |
| Contrato de APIs REST | LIN-API-REST-001 |
| Observabilidad de aplicación | LIN-OBS-001 |
| Seguridad de aplicación | LIN-SEC-APP-001 |
| Pruebas automatizadas | LIN-TEST-001 |
| Pipelines CI/CD | LIN-CICD-001 — Borrador |
| Infraestructura como Código | LIN-IAC-001 — Borrador |
| Administración del clúster Kubernetes | Plataforma / Infraestructura |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Diseño y Arquitectura de Software | LIN-ARQ-000 | Define Kubernetes como destino objetivo y estilos de despliegue |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Define stack Java/Spring Boot y configuración de aplicación |
| Estándar de APIs REST | LIN-API-REST-001 | Define exposición de APIs y relación con WSO2 API Manager |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Define logs, métricas, health checks y trazas |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Define secretos, escaneo, usuario no root y controles de seguridad |
| Estándar de Pruebas | LIN-TEST-001 | Define evidencias de pruebas antes de pase |
| Directiva de Desarrollo de Software Seguro | DIR-SEC-SW-001 | Marco superior de controles de seguridad de software |
| Informe de Lineamientos de Contenedores y Orquestador | INFORME-000082-2023-OTI.ID | Antecedente institucional |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **Imagen inmutable** | Una imagen no se modifica entre ambientes. La misma imagen se promueve de DEV a QA y PROD cambiando solo configuración externa. |
| P2 | **Una responsabilidad por contenedor** | Un contenedor ejecuta un único proceso principal de aplicación. No se empaquetan múltiples aplicaciones independientes en una misma imagen. |
| P3 | **Configuración externa** | La configuración de ambiente se inyecta mediante variables, ConfigMaps, Secrets o mecanismo aprobado; no va dentro de la imagen. |
| P4 | **Sin secretos en imágenes** | Ninguna imagen contiene credenciales, tokens, claves, certificados privados ni archivos de configuración sensibles. |
| P5 | **Seguridad por defecto** | Los contenedores se ejecutan sin privilegios, sin usuario root y con permisos mínimos, salvo excepción documentada. |
| P6 | **Recursos explícitos** | Todo workload define `requests` y `limits` de CPU y memoria. |
| P7 | **Observabilidad desde el primer despliegue** | Todo contenedor debe emitir logs a stdout/stderr, exponer health checks y permitir monitoreo. |
| P8 | **Declarativo y versionado** | Los manifiestos Kubernetes se versionan como código del proyecto o repositorio de despliegue. |
| P9 | **Rollback posible** | Todo despliegue debe permitir volver a una versión anterior conocida. |
| P10 | **Alineamiento documental** | Este lineamiento define cómo empaquetar y desplegar; no redefine seguridad, pruebas, APIs ni observabilidad. |

---

---

## Parte I — Construcción y empaquetado de contenedores

---

## 4. Modelo de uso de contenedores en ONP

### 4.1 Tipos de workload permitidos

| Tipo | Ejemplo | Recurso Kubernetes sugerido |
|---|---|---|
| API backend | Servicio Spring Boot REST | `Deployment` + `Service` |
| Frontend web | Angular servido por Nginx u otro servidor web | `Deployment` + `Service` + `Ingress` si aplica |
| Worker | Consumidor de cola, proceso asíncrono | `Deployment` |
| Job puntual | Migración, carga controlada, tarea batch puntual | `Job` |
| Job recurrente | Tarea programada | `CronJob` |
| Adapter | Integración con SAA, Oracle, SOAP, servicios externos | `Deployment` o `Job`, según naturaleza |

### 4.2 Separación de responsabilidades

| Responsabilidad | Desarrollo | Plataforma / Infraestructura | Arquitectura | Seguridad |
|---|---:|---:|---:|---:|
| Dockerfile de la aplicación | R | A | C | C |
| Manifiestos base de aplicación | R | A | C | C |
| Namespace, cuotas y políticas del clúster | C | R/A | C | C |
| Gestión de secretos de plataforma | C | R | C | A |
| Escaneo de imagen | R | C | C | A |
| Observabilidad técnica | R | R | A | C |
| Revisión de excepción | C | C | A | A si afecta seguridad |

Leyenda: **R** responsable, **A** aprueba, **C** consulta.

> Para proyectos que siguen el lineamiento estándar, Plataforma valida el Dockerfile y los manifiestos antes del despliegue. Arquitectura interviene como aprobador únicamente en revisiones de excepción (ADR) o en la primera adopción de un tipo de workload no contemplado previamente.

### 4.3 Runtime de contenedores en el clúster ONP

El clúster de Kubernetes de la ONP usa **containerd** como container runtime de producción — no Docker Engine. Esta decisión ya está sancionada en `LIN-ARQ-000 §11.1` (ADR-009) y se reproduce aquí porque afecta directamente cómo Desarrollo y Plataforma operan e inspeccionan contenedores.

| Aspecto | Regla |
|---|---|
| Runtime en DEV local | Docker Engine (o Podman) — libre elección del desarrollador |
| Runtime en Transición | Docker Engine + Docker Compose (ver LIN-ARQ-000 §11.1) — etapa temporal, no es destino final |
| Runtime en QA y PROD (clúster K8s) | **containerd** — único runtime soportado por Plataforma |
| Construcción de imágenes | El `Dockerfile` sigue siendo el estándar de construcción en todos los casos (ver sección 5). Produce imágenes OCI estándar, compatibles con containerd sin cambios |
| Inspección de contenedores en nodos QA/PROD | `crictl`, no `docker`. El comando `docker` no existe ni aplica en los nodos del clúster |

> **Qué NO cambia para Desarrollo:** el Dockerfile, el proceso de build local con Docker y las pruebas de la imagen en el equipo del desarrollador siguen siendo iguales. La diferencia de runtime es puramente operativa, del lado de Plataforma, y ocurre únicamente en los nodos del clúster — no afecta cómo se construye ni cómo se prueba una imagen antes de subirla al registro.

> **Anti-patrón:** documentar procedimientos de troubleshooting o runbooks que asuman `docker exec`, `docker logs` o `docker ps` contra un nodo de QA/PROD. En esos ambientes el equivalente es `crictl exec`, `crictl logs`, `crictl ps` (o `kubectl exec`/`kubectl logs` a nivel de pod, que es la vía preferente para Desarrollo).

### 4.4 Convención de nombres de namespace

ONP opera **un clúster Kubernetes independiente por ambiente** (DEV, QA, PROD). El ambiente **nunca** forma parte del nombre del namespace de aplicación — ya está implícito en a qué clúster pertenece.

**Estructura:**

```text
<sistema>-<componente>
```

| Elemento | Fuente del valor | Ejemplo |
|---|---|---|
| `<sistema>` | Mismo nombre de sistema usado en la ruta del registro de imágenes (§6.2) y en la etiqueta `app.kubernetes.io/part-of` (§9.3) | `past`, `notificacion_electronica` |
| `<componente>` | Mismo vocabulario del tipo de workload (§4.1) y de la etiqueta `app.kubernetes.io/component` (§9.3) | `backend`, `frontend`, `worker`, `job` |

Ejemplos:

```text
past-backend
past-frontend
notificacion_electronica-backend
notificacion_electronica-worker
```

**Reglas:**

- Un namespace agrupa todos los recursos desplegables de un mismo `<sistema>` + `<componente>` dentro de un ambiente — no se crea un namespace nuevo por cada `Deployment` individual.
- La creación, cuotas (`ResourceQuota`/`LimitRange`) y políticas del namespace son responsabilidad de Plataforma (ver RACI en §4.2). Desarrollo **declara** el namespace en sus manifiestos versionados (Kustomize/Helm, ver Anexo A) pero no lo crea ni administra directamente en el clúster.
- Todo namespace debe quedar documentado en la ficha de despliegue del sistema (ver sección 16).

> **Inconsistencia detectada, pendiente de verificar con Plataforma:** los namespaces de infraestructura compartida ya documentados en `LIN-OBS-001` (`otel-dev`, `otel-qa`, `otel`) y `LIN-BUS-001` (`kafka-dev`, `kafka-qa`, `kafka`) sí llevan sufijo de ambiente. Si cada ambiente es un clúster independiente, ese sufijo sería redundante — salvo que esos componentes de plataforma vivan en un clúster compartido distinto al de las aplicaciones de negocio. Este lineamiento no asume una respuesta; se recomienda que Arquitectura confirme con Plataforma si esos dos documentos deben corregirse para alinearse a la convención sin sufijo, o si existe en efecto un clúster de plataforma compartido que justifica la excepción.

---

## 5. Construcción de imágenes de contenedor

### 5.1 Reglas generales

Toda imagen de aplicación debe cumplir:

1. Construcción reproducible.
2. Imagen base permitida o aprobada.
3. Etiquetado trazable.
4. Sin secretos embebidos.
5. Usuario no root.
6. Dependencias mínimas.
7. Exposición explícita de puerto.
8. Logs por stdout/stderr.
9. Health endpoint disponible cuando aplique.
10. Escaneo de vulnerabilidades antes de QA/PROD.

### 5.2 Dockerfile multi-stage

Los proyectos Java deben usar Dockerfile multi-stage para separar compilación y ejecución.

Ejemplo referencial:

```dockerfile
# Etapa de build — solo compilación y empaquetado
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

# -DskipTests: las pruebas NO se ejecutan aquí.
# La imagen se construye solo después de que las pruebas pasaron
# en el pipeline de CI/CD (ver LIN-CICD-001). Este stage es
# exclusivamente de compilación y empaquetado.
RUN mvn -B clean package -DskipTests

# Etapa runtime — imagen final mínima
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

> Nota: Este ejemplo es referencial. La imagen base institucional debe ser definida o validada por Plataforma y Seguridad. Las pruebas automatizadas se ejecutan en el pipeline **antes** de invocar el build de la imagen — nunca dentro del Dockerfile.
>
> Las imágenes Alpine usan `musl libc` en lugar de `glibc`. Para la mayoría de proyectos Spring Boot esto no genera problemas, pero si el proyecto tiene dependencias nativas que requieren `glibc`, se debe usar la variante Debian (`maven:3.9-eclipse-temurin-21` / `eclipse-temurin:21-jre`) y documentar la justificación. La variante a usar debe ser validada por Plataforma y Seguridad como imagen base institucional aprobada.

### 5.3 Reglas para imágenes Java

| Regla | Obligatorio |
|---|---|
| Usar Java 21 para proyectos nuevos | Sí |
| Usar imagen JRE para runtime, no SDK completo | Sí |
| No incluir código fuente en imagen final | Sí |
| No ejecutar Maven dentro del contenedor final | Sí |
| No incluir `.git`, `.m2`, `target` completo ni archivos temporales innecesarios | Sí |
| Exponer puerto de aplicación de forma explícita | Sí |
| Usar usuario no root | Sí |
| Configurar timezone solo si existe necesidad justificada | Recomendado |
| Usar `SPRING_PROFILES_ACTIVE` externo | Sí |

### 5.4 Reglas para imágenes Frontend Angular

Las aplicaciones Angular se construyen como artefacto estático y se sirven mediante un contenedor web.

Ejemplo referencial:

```dockerfile
# Build Angular
FROM node:20-alpine AS build
WORKDIR /workspace

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Runtime web
FROM nginx:stable-alpine
COPY --from=build /workspace/dist/ /usr/share/nginx/html

# Configuración nginx para SPA Angular (ver Anexo D)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

> **Importante:** Sin una configuración nginx apropiada, cualquier recarga directa de una ruta Angular (`/pensiones/consulta`) devuelve 404 porque Nginx busca el archivo físico. El Anexo D incluye la configuración mínima obligatoria.

Reglas:

| Regla | Obligatorio |
|---|---|
| No incluir `node_modules` en la imagen final | Sí |
| No incluir secretos en `environment.ts` | Sí |
| No exponer rutas internas de backend en el bundle si no corresponden al frontend | Sí |
| Servir solo artefactos estáticos necesarios | Sí |
| Configurar headers de seguridad en servidor web cuando aplique | Sí |
| Usar imagen base aprobada | Sí |

### 5.5 Minimización de imagen

Se debe:

- agrupar instrucciones `RUN` cuando aplique;
- limpiar cachés de instalación;
- evitar dependencias innecesarias;
- evitar paquetes de diagnóstico en imágenes productivas;
- no instalar herramientas como `curl`, `wget`, `vim`, `netcat` salvo necesidad justificada;
- preferir imágenes ligeras aprobadas.

---

## 6. Repositorio, versionamiento y promoción de imágenes

### 6.1 Registro de imágenes

El registro institucional de contenedores de la ONP es el **GitLab Container Registry** incluido en la licencia GitLab Ultimate. Toda imagen debe publicarse en este registro antes de ser desplegada en QA o PROD. No se permite desplegar imágenes construidas localmente ni provenientes de registros externos no aprobados en ambientes QA o PROD.

| Aspecto | Valor institucional |
|---|---|
| Registro institucional | GitLab Container Registry (GitLab Ultimate) |
| URL base | `registry.gitlab.onp.gob.pe` (definida por Plataforma) |
| Autenticación | Token de deploy o credenciales de CI/CD gestionadas por GitLab |
| Integración CI/CD | Capacidad objetivo — mientras no se implemente el pipeline automático de LIN-CICD-001, la construcción y publicación de imágenes se realiza mediante proceso controlado manual o semiautomático (ver 17) |

> La configuración del pipeline que construye, etiqueta y empuja imágenes al registro se define en LIN-CICD-001. Este lineamiento define qué registro usar y cómo nombrar las imágenes.

### 6.2 Nomenclatura de imágenes

La URL del registro **no es una convención libre** — GitLab la asigna automáticamente según la estructura de grupos y proyectos. No se debe inventar una ruta distinta a la que GitLab genera.

Estructura institucional de GitLab ONP:

```text
<grupo-raiz> / <sistema> / <proyecto-componente>
```

| Nivel | Ejemplo | Descripción |
|---|---|---|
| Grupo raíz | `aplicaciones` | Área o dominio institucional |
| Subgrupo (sistema) | `notificacion_electronica` | Sistema o aplicación |
| Proyecto (componente) | `backend`, `frontend`, `worker` | Componente desplegable |

URL de imagen resultante:

```text
registry.gitlab.onp.gob.pe/<grupo>/<sistema>/<componente>:<version>
```

Ejemplos reales:

```text
registry.gitlab.onp.gob.pe/aplicaciones/notificacion_electronica/backend:1.2.0
registry.gitlab.onp.gob.pe/aplicaciones/notificacion_electronica/frontend:1.2.0
registry.gitlab.onp.gob.pe/aplicaciones/past/backend:3.1.0
registry.gitlab.onp.gob.pe/aplicaciones/past/frontend:3.1.0
```

> Si un proyecto GitLab genera múltiples imágenes (monorepo), se usa el sufijo de imagen dentro del mismo proyecto: `registry.gitlab.onp.gob.pe/<grupo>/<sistema>/<proyecto>/api:1.0.0`. Esto debe justificarse — lo natural es un proyecto GitLab por componente desplegable.

### 6.3 Tags permitidos

| Tag | Uso |
|---|---|
| `1.2.3` | Versión semántica estable |
| `1.2.3-rc.1` | Release candidate |
| `1.2.3-dev.45` | Build de desarrollo |
| `<commit-sha>` | Trazabilidad técnica |
| `latest` | Prohibido en QA y PROD |

Regla:

> En QA y Producción está prohibido usar el tag `latest`. Todo despliegue debe usar una versión explícita e identificable.

### 6.4 Promoción entre ambientes

La misma imagen debe promoverse entre ambientes sin reconstruirse. La diferencia entre ambientes se resuelve mediante configuración externa (ConfigMap, Secret, variables de entorno), no mediante imágenes distintas.

```text
DEV → QA → PROD
```

> La estrategia de ramas Git (nombres, flujo, protección) se rige por LIN-VER-001. Este lineamiento únicamente exige que la imagen promovida entre ambientes sea la misma — construida una sola vez, identificada por un tag explícito e inmutable.

---

## 7. Configuración por ambiente

### 7.1 Principio

La imagen debe ser agnóstica del ambiente. La configuración se inyecta en tiempo de despliegue.

### 7.2 Configuración no sensible

Usar `ConfigMap` o variables de entorno para:

- nombre de ambiente;
- URLs internas no sensibles;
- configuración de logging no sensible;
- flags funcionales no sensibles;
- timeouts;
- parámetros de operación no secretos.

Ejemplo:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: past-api-config
data:
  SPRING_PROFILES_ACTIVE: "qa"
  APP_ENVIRONMENT: "quality"
  API_TIMEOUT_MS: "5000"
```

### 7.3 Configuración sensible

Usar `Secret`, Vault o mecanismo aprobado para:

- credenciales de base de datos;
- tokens de integración;
- claves privadas;
- certificados;
- passwords;
- cadenas de conexión sensibles;
- credenciales SAA o URLs críticas si contienen información sensible.

Ejemplo:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: past-api-secret
type: Opaque
stringData:
  DB_USERNAME: "usuario_app"
  DB_PASSWORD: "cambiar-en-plataforma"
```

> Los valores reales de secretos no deben versionarse en repositorios.

---

## 8. Secretos y datos sensibles

### 8.1 Reglas obligatorias

| Regla | Estado |
|---|---|
| No guardar secretos en Dockerfile | Obligatorio |
| No guardar secretos en imagen | Obligatorio |
| No guardar secretos en repositorio Git | Obligatorio |
| No incluir secretos en `application.properties`, `application.yml` o `environment.ts` | Obligatorio |
| Inyectar secretos desde mecanismo aprobado | Obligatorio |
| Rotar secretos ante sospecha de exposición | Obligatorio |
| No imprimir secretos en logs | Obligatorio |

### 8.2 Relación con Seguridad

La definición de qué constituye un secreto y cómo debe protegerse se rige por `LIN-SEC-APP-001`. Este lineamiento define únicamente la forma de inyectarlos y evitar su inclusión en imágenes o manifiestos versionados.

---

---

## Parte II — Orquestación con Kubernetes

---

## 9. Manifiestos Kubernetes mínimos

### 9.1 Recursos mínimos por aplicación

Toda aplicación desplegada en Kubernetes debe contar, como mínimo, con:

| Recurso | Obligatorio |
|---|---|
| `Deployment` o `StatefulSet` según naturaleza | Sí |
| `Service` para exposición interna | Sí, si recibe tráfico |
| `ConfigMap` | Sí, si tiene configuración no sensible |
| `Secret` | Sí, si usa configuración sensible |
| `Ingress` o publicación vía API Manager | Solo si expone tráfico externo |
| `HorizontalPodAutoscaler` | Según criticidad y demanda |
| `ServiceAccount` propio | Recomendado / obligatorio si usa permisos |
| `NetworkPolicy` | Recomendado; obligatorio para servicios críticos |
| `PodDisruptionBudget` | Recomendado para servicios críticos |
| `ResourceQuota` / `LimitRange` | Responsabilidad de Plataforma por namespace |

### 9.2 Deployment mínimo de referencia

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: past-api-afiliacion
  labels:
    app.kubernetes.io/name: past-api-afiliacion
    app.kubernetes.io/part-of: past
    app.kubernetes.io/version: "1.0.0"
spec:
  replicas: 2
  selector:
    matchLabels:
      app.kubernetes.io/name: past-api-afiliacion
  template:
    metadata:
      labels:
        app.kubernetes.io/name: past-api-afiliacion
        app.kubernetes.io/part-of: past
        app.kubernetes.io/version: "1.0.0"
    spec:
      serviceAccountName: past-api-afiliacion
      containers:
        - name: app
          image: registry.gitlab.onp.gob.pe/aplicaciones/past/api-afiliacion:1.0.0
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 8080
          envFrom:
            - configMapRef:
                name: past-api-afiliacion-config
            - secretRef:
                name: past-api-afiliacion-secret
          resources:
            requests:
              cpu: "250m"
              memory: "512Mi"
            limits:
              cpu: "1000m"
              memory: "1Gi"
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 20
            periodSeconds: 10
            timeoutSeconds: 3
            failureThreshold: 3
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 60
            periodSeconds: 20
            timeoutSeconds: 3
            failureThreshold: 3
          securityContext:
            allowPrivilegeEscalation: false
            runAsNonRoot: true
            readOnlyRootFilesystem: true
            capabilities:
              drop:
                - ALL
```

### 9.3 Etiquetas obligatorias

Usar etiquetas estándar:

```yaml
app.kubernetes.io/name: <nombre-componente>
app.kubernetes.io/part-of: <sistema>
app.kubernetes.io/version: <version>
app.kubernetes.io/component: <api|frontend|worker|job>
app.kubernetes.io/managed-by: <equipo-o-herramienta>
```

---

## 10. Recursos: requests, limits y escalamiento

### 10.1 Requests y limits

Todo contenedor debe declarar recursos:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "512Mi"
  limits:
    cpu: "1000m"
    memory: "1Gi"
```

Reglas:

| Regla | Estado |
|---|---|
| Todo pod define `requests.cpu` | Obligatorio |
| Todo pod define `requests.memory` | Obligatorio |
| Todo pod define `limits.cpu` | Obligatorio |
| Todo pod define `limits.memory` | Obligatorio |
| No se aceptan recursos ilimitados en QA/PROD | Obligatorio |
| Los valores deben ajustarse por medición real | Recomendado |

### 10.2 Escalamiento horizontal

El `HorizontalPodAutoscaler` debe considerarse para servicios con carga variable o criticidad alta.

Ejemplo:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: past-api-afiliacion
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: past-api-afiliacion
  minReplicas: 2
  maxReplicas: 6
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

### 10.3 Réplicas mínimas

| Ambiente | Réplicas mínimas sugeridas |
|---|---:|
| DEV | 1 |
| QA | 1 o 2 según pruebas |
| PROD | 2 para servicios críticos |

Los servicios críticos no deben operar con una sola réplica en Producción salvo excepción aprobada.

---

## 11. Health checks y ciclo de vida del pod

### 11.1 Probes obligatorias

| Probe | Uso | Obligatorio |
|---|---|---|
| `readinessProbe` | Determina si el pod puede recibir tráfico | Sí |
| `livenessProbe` | Determina si el pod debe reiniciarse | Sí |
| `startupProbe` | Para aplicaciones de arranque lento | Según caso |

### 11.2 Spring Boot Actuator

Servicios Spring Boot deben exponer:

```text
/actuator/health
/actuator/health/readiness
/actuator/health/liveness
/actuator/info
/actuator/prometheus
```

La exposición y seguridad de estos endpoints se rige por `LIN-OBS-001` y `LIN-SEC-APP-001`.

### 11.3 Shutdown controlado

Las aplicaciones deben soportar terminación controlada:

```yaml
terminationGracePeriodSeconds: 30
```

Para Spring Boot, se recomienda habilitar graceful shutdown:

```properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

---

## 12. Red, servicios e ingreso

### 12.1 Service interno

Todo backend que reciba tráfico dentro del clúster debe exponerse mediante `Service`.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: past-api-afiliacion
spec:
  type: ClusterIP
  selector:
    app.kubernetes.io/name: past-api-afiliacion
  ports:
    - name: http
      port: 80
      targetPort: http
```

### 12.2 Ingress y exposición externa

Reglas:

| Caso | Exposición |
|---|---|
| API REST institucional | Preferentemente vía WSO2 API Manager |
| Frontend web | Ingress o mecanismo institucional aprobado |
| Servicio interno entre componentes | Service `ClusterIP` |
| Servicio administrativo o actuator | No exponer públicamente |
| API externa directa sin gateway | Prohibido salvo ADR |

### 12.3 HostNetwork

Queda prohibido el uso de `hostNetwork: true` salvo excepción técnica documentada y aprobada por Plataforma, Arquitectura y Seguridad.

### 12.4 Puertos

Todo contenedor debe declarar explícitamente sus puertos. No se deben usar puertos aleatorios o no documentados en QA/PROD.

---

## 13. Persistencia y volúmenes

### 13.1 Principio

Las aplicaciones deben ser preferentemente **stateless**. La persistencia de datos de negocio debe residir en bases de datos o servicios de almacenamiento externos, no en el filesystem local del contenedor.

### 13.2 Uso permitido de volúmenes

| Uso | Permitido |
|---|---|
| Archivos temporales controlados | Sí, si se limpia correctamente |
| Cache no crítica | Sí, si puede regenerarse |
| Archivos subidos por usuario | Solo si existe almacenamiento persistente aprobado |
| Logs de aplicación | No, deben ir a stdout/stderr |
| Base de datos productiva de negocio | No, salvo arquitectura aprobada |
| Certificados o secretos montados | Sí, mediante Secret o mecanismo aprobado |

### 13.3 PVC

El uso de `PersistentVolumeClaim` requiere justificación:

```text
- qué dato se persiste;
- por qué no puede estar en BD u object storage;
- política de backup;
- retención;
- criticidad;
- responsable.
```

---

## 14. Seguridad del contenedor y del pod

### 14.1 Reglas obligatorias

| Control | Regla |
|---|---|
| Usuario no root | `runAsNonRoot: true` |
| Escalamiento de privilegios | `allowPrivilegeEscalation: false` |
| Capabilities Linux | `drop: ["ALL"]` salvo excepción |
| Contenedor privilegiado | Prohibido |
| Docker socket | Prohibido montar `/var/run/docker.sock` |
| Root filesystem | `readOnlyRootFilesystem: true` cuando la aplicación lo permita; usar `emptyDir` para directorios temporales si la aplicación necesita escribir (ver nota 14.1) |
| Secrets | No en imagen ni repositorio |
| Imágenes | Escaneo antes de QA/PROD |
| HostPath | Prohibido salvo excepción |
| HostNetwork | Prohibido salvo excepción |

#### Nota 14.1 — readOnlyRootFilesystem con escritura temporal

Spring Boot y algunas aplicaciones escriben archivos temporales en `/tmp`. Con `readOnlyRootFilesystem: true`, esas escrituras fallan. La solución es montar un volumen temporal en memoria:

```yaml
containers:
  - name: app
    securityContext:
      readOnlyRootFilesystem: true
    volumeMounts:
      - name: tmp
        mountPath: /tmp
volumes:
  - name: tmp
    emptyDir:
      medium: Memory   # tmpfs — no persiste, se limpia al reiniciar el pod
      sizeLimit: 64Mi
```

No usar `readOnlyRootFilesystem: false` como solución general. Identificar los directorios que necesitan escritura y montarlos explícitamente.

### 14.2 Escaneo de imágenes

Toda imagen debe ser escaneada con Trivy o herramienta equivalente antes de pasar a QA y Producción.

Criterio sugerido:

| Severidad | Regla |
|---|---|
| Critical | Bloquea pase salvo excepción de Seguridad |
| High | Requiere evaluación y plan de remediación |
| Medium | Registrar y corregir según planificación |
| Low | Monitorear |

Mientras el pipeline de `LIN-CICD-001` no esté plenamente implementado, el escaneo puede ejecutarse de forma manual o semiautomática, adjuntando evidencia en el Merge Request o expediente técnico.

### 14.3 ServiceAccount

Cada aplicación debe usar un `ServiceAccount` específico si requiere permisos en el clúster. No debe usar permisos amplios ni cuentas compartidas sin justificación.

---

## 15. Observabilidad mínima

### 15.1 Logs

Las aplicaciones deben escribir logs a `stdout/stderr`. No deben escribir logs en archivos internos del contenedor como mecanismo principal.

Los logs deben cumplir `LIN-OBS-001`:

- JSON estructurado cuando aplique;
- `trace.id`;
- `span.id`;
- `http.request.id`;
- `service.name`;
- sin PII;
- sin tokens;
- sin secretos.

### 15.2 Métricas

Servicios Spring Boot deben exponer métricas por Actuator/Prometheus cuando aplique.

### 15.3 Trazas

Servicios backend deben integrarse con OpenTelemetry según `LIN-OBS-001`.

### 15.4 Dashboards

Para sistemas críticos, Plataforma y Desarrollo deben coordinar dashboards mínimos:

- estado de pods;
- reinicios;
- consumo CPU/memoria;
- latencia;
- tasa de error;
- saturación;
- health checks;
- logs de error.

---

---

## Parte III — Gobierno y conformidad

---

## 16. Documentación obligatoria por ambiente

Cada solución contenerizada debe documentar por ambiente:

| Campo | DEV | QA | PROD |
|---|---|---|---|
| Nombre de aplicación | Sí | Sí | Sí |
| Namespace | Sí | Sí | Sí |
| Nombre de imagen | Sí | Sí | Sí |
| Versión/tag de imagen | Sí | Sí | Sí |
| Puerto expuesto | Sí | Sí | Sí |
| Variables de entorno no sensibles | Sí | Sí | Sí |
| Secretos requeridos, sin valores | Sí | Sí | Sí |
| ConfigMaps | Sí | Sí | Sí |
| Servicios externos consumidos | Sí | Sí | Sí |
| Base de datos consumida | Sí | Sí | Sí |
| Recursos requests/limits | Sí | Sí | Sí |
| Probes configuradas | Sí | Sí | Sí |
| Ingress/API Manager | Según caso | Según caso | Según caso |
| Responsable técnico | Sí | Sí | Sí |

---

## 17. Relación con CI/CD e IaC

CI/CD se encuentra mapeado como capacidad objetivo (en borrador).

Hasta la implementación de `LIN-CICD-001`:

- las imágenes pueden construirse mediante proceso controlado manual o semiautomático;
- las evidencias de pruebas, seguridad y escaneo deben adjuntarse al Merge Request o expediente técnico;
- los tags de imagen deben ser explícitos;
- no se permite desplegar imágenes sin trazabilidad.

`LIN-IAC-001` (en borrador) es el dueño del estándar de Terraform e infraestructura declarativa. El código IaC vive en el repositorio dedicado `oti-plataforma/infrastructure-iac`, que es la fuente de verdad para la definición declarativa de los clústeres Kubernetes gestionados con Terraform.

Mientras `LIN-IAC-001` no esté oficializado:

- los manifiestos Kubernetes deben versionarse en GitLab;
- todo cambio en manifiestos debe pasar por revisión técnica mediante Merge Request;
- los cambios de infraestructura del clúster son responsabilidad de Plataforma;
- no se deben aplicar cambios manuales no documentados en QA/PROD.

---

## 18. Checklist de conformidad

### 18.1 Imagen

```text
[ ] Dockerfile multi-stage cuando aplique
[ ] Imagen final no contiene código fuente ni herramientas innecesarias
[ ] Imagen no contiene secretos
[ ] Imagen ejecuta con usuario no root
[ ] Tag explícito, no latest
[ ] Imagen publicada en registro institucional
[ ] Imagen escaneada con Trivy o equivalente
```

### 18.2 Kubernetes

```text
[ ] Deployment/Job/CronJob definido
[ ] Service definido si recibe tráfico
[ ] ConfigMap definido para configuración no sensible
[ ] Secret definido para configuración sensible
[ ] resources.requests y resources.limits definidos
[ ] readinessProbe definida
[ ] livenessProbe definida
[ ] securityContext definido
[ ] No usa hostNetwork
[ ] No monta docker.sock
[ ] No usa privileged
[ ] Logs a stdout/stderr
```

### 18.3 Operación

```text
[ ] Variables por ambiente documentadas
[ ] Secretos requeridos documentados sin valores
[ ] Puertos documentados
[ ] Dependencias externas documentadas
[ ] Base de datos o servicios consumidos documentados
[ ] Plan de reversa definido
[ ] Responsable técnico definido
```

---

## 19. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Usar `latest` en QA/PROD | No hay trazabilidad ni plan de reversa seguro | Prohibido |
| Construir imagen distinta por ambiente | Rompe inmutabilidad y reproducibilidad | Usar misma imagen y configuración externa |
| Guardar secretos en Dockerfile | Exposición de credenciales | Prohibido |
| Ejecutar como root | Mayor impacto ante compromiso | Prohibido salvo excepción |
| No definir resources | Riesgo de saturación del nodo | Obligatorio requests/limits |
| No definir probes | Kubernetes no puede gestionar salud del pod | Obligatorio |
| Escribir logs en archivos locales | Pérdida de logs y mala operación | Logs a stdout/stderr |
| Montar `docker.sock` | Escalada de privilegios al nodo | Prohibido |
| Usar `hostNetwork` sin justificación | Riesgo de red y colisión de puertos | Prohibido salvo ADR |
| Empaquetar varias aplicaciones en una imagen | Acoplamiento operativo | Una responsabilidad por contenedor |
| Usar PVC para estado que debería estar en BD | Diseño stateful innecesario | Justificar persistencia |

---

## 20. Proceso ADR para desviaciones

Toda desviación relevante requiere ADR aprobado por Arquitectura. Si afecta seguridad, requiere además validación de Seguridad Digital conforme a la Directiva de Desarrollo de Software Seguro.

### 20.1 Casos que requieren ADR

- Ejecutar contenedor como root.
- Usar imagen base no aprobada.
- Usar `hostNetwork`.
- Usar `hostPath`.
- No declarar requests/limits.
- No contar con liveness/readiness probes.
- Usar PVC para persistencia de negocio.
- Exponer API sin WSO2/API Manager cuando debería estar gestionada.
- No escanear imagen antes de QA/PROD.
- Usar tag `latest` fuera de desarrollo local.

### 20.2 Formato mínimo

```markdown
# ADR-K8S-NNN — [Título]

## Contexto
[Descripción de la restricción o excepción requerida]

## Decisión
[Qué se permitirá excepcionalmente]

## Riesgo aceptado
[Riesgo operativo, seguridad o arquitectura]

## Control compensatorio
[Medida temporal o alternativa]

## Fecha de revisión
[Fecha para reevaluar]

## Aprobaciones
[Arquitectura / Plataforma / Seguridad, según corresponda]
```

---

## 21. Glosario

| Término | Definición |
|---|---|
| Contenedor | Unidad ejecutable que empaqueta aplicación y dependencias necesarias |
| Imagen | Plantilla inmutable desde la cual se crean contenedores |
| Registry | Repositorio de imágenes de contenedor |
| containerd | Container runtime de bajo nivel usado por el clúster Kubernetes de la ONP en QA y PROD (ver 4.3). No requiere Docker Engine para ejecutar contenedores |
| crictl | Herramienta de línea de comandos para inspeccionar contenedores gestionados por containerd en los nodos del clúster — reemplaza a `docker` en QA/PROD (ver 4.3) |
| Pod | Unidad mínima desplegable en Kubernetes |
| Deployment | Recurso Kubernetes que gestiona réplicas de pods |
| Service | Recurso Kubernetes que expone pods dentro del clúster |
| Ingress | Recurso de entrada HTTP/HTTPS hacia servicios del clúster |
| ConfigMap | Configuración no sensible |
| Secret | Configuración sensible |
| Probe | Verificación de salud del contenedor |
| Request | Recurso mínimo reservado |
| Limit | Recurso máximo permitido |
| PVC | Reclamo de volumen persistente |
| HPA | Escalador horizontal de pods |
| Namespace | Segmentación lógica dentro del clúster |
| SecurityContext | Configuración de seguridad del pod o contenedor |

---

## 22. Anexos

### Anexo A — Estructura sugerida de manifiestos

**Kustomize** es el mecanismo de referencia institucional para gestionar diferencias entre ambientes sin duplicar manifiestos ni imágenes. **Helm** está permitido en proyectos que lo justifiquen mediante ADR — especialmente para componentes con alta parametrización o charts de terceros.

```text
k8s/
├── base/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.example.yaml
│   └── hpa.yaml
├── overlays/
│   ├── dev/
│   │   └── kustomization.yaml
│   ├── qa/
│   │   └── kustomization.yaml
│   └── prod/
│       └── kustomization.yaml
└── README.md
```

### Anexo B — Ejemplo de `secret.example.yaml`

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: past-api-secret
type: Opaque
stringData:
  DB_USERNAME: "<definido-por-plataforma>"
  DB_PASSWORD: "<definido-por-plataforma>"
```

> Este archivo es solo plantilla. No debe contener valores reales.

### Anexo C — Comando referencial de escaneo con Trivy

```bash
trivy image registry.gitlab.onp.gob.pe/aplicaciones/past/api-afiliacion:1.0.0
```

Criterio mínimo:

```bash
trivy image --severity CRITICAL,HIGH registry.gitlab.onp.gob.pe/aplicaciones/past/api-afiliacion:1.0.0
```

### Anexo D — nginx.conf mínimo para Angular SPA

Las aplicaciones Angular son Single Page Applications. Nginx debe redirigir cualquier ruta al `index.html` para que el router de Angular tome el control. Sin esta configuración, las recargas directas de rutas devuelven 404.

```nginx
# nginx.conf — configuración mínima para Angular SPA
server {
    listen 80;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Compresión
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # Seguridad — headers básicos (ver LIN-SEC-APP-001 para apps web)
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header Cache-Control "no-store" always;

    location / {
        # Regla crítica para SPA: si el archivo o directorio no existe,
        # redirigir a index.html para que Angular Router maneje la ruta
        try_files $uri $uri/ /index.html;
    }

    # Archivos estáticos con cache agresivo (hash en nombre de archivo por Angular CLI)
    location ~* \.(js|css|png|jpg|gif|ico|woff2?)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # No exponer archivos de configuración
    location ~ /\. {
        deny all;
    }
}
```

Este archivo debe copiarse en el Dockerfile de Angular:

```dockerfile
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

### Anexo E — NetworkPolicy mínima para servicio backend

La `NetworkPolicy` restringe el tráfico de red a nivel de pod. Para servicios críticos es obligatoria (ver 9.1).

Ejemplo: permitir tráfico solo desde el Ingress Controller y desde otros pods del mismo sistema, bloquear todo lo demás.

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: past-api-afiliacion-netpol
  namespace: past
spec:
  podSelector:
    matchLabels:
      app.kubernetes.io/name: past-api-afiliacion
  policyTypes:
    - Ingress
    - Egress

  ingress:
    # Permitir tráfico desde el Ingress Controller (o WSO2 gateway)
    - from:
        - namespaceSelector:
            matchLabels:
              kubernetes.io/metadata.name: ingress-nginx
      ports:
        - protocol: TCP
          port: 8080
    # Permitir tráfico desde otros pods del mismo sistema (past), mismo namespace
    - from:
        - podSelector:
            matchLabels:
              app.kubernetes.io/part-of: past
      ports:
        - protocol: TCP
          port: 8080

  egress:
    # Permitir salida a Oracle (puerto típico)
    - to:
        - ipBlock:
            cidr: 10.0.0.0/8   # ajustar al rango de BD institucional
      ports:
        - protocol: TCP
          port: 1521
    # Permitir DNS
    - ports:
        - protocol: UDP
          port: 53
```

> **Nota sobre comunicación entre namespaces:** El selector `podSelector` sin `namespaceSelector` aplica únicamente al mismo namespace. Si el consumidor reside en un namespace distinto, se debe combinar `namespaceSelector` + `podSelector` y la configuración debe ser validada por Plataforma/Infraestructura:
>
> ```yaml
> - from:
>     - namespaceSelector:
>         matchLabels:
>           kubernetes.io/metadata.name: otro-namespace
>       podSelector:
>         matchLabels:
>           app.kubernetes.io/name: consumidor
> ```
>
> Los CIDRs, namespaces y labels exactos deben ser acordados con Plataforma según la topología de red del clúster.

### Anexo F — Relación con el antecedente 2023

El Informe N.° 000082-2023-OTI.ID planteó lineamientos iniciales para contenedores y orquestador de contenedores, incluyendo:

- una solución tecnológica por contenedor;
- uso de repositorio de imágenes;
- reducción del tamaño de imágenes;
- dependencias necesarias;
- almacenamiento externo para datos persistentes;
- configuración externa;
- red del contenedor;
- documentación por ambiente;
- archivos declarativos YAML/JSON;
- límites de recursos;
- escalabilidad;
- plan de reversa;
- gestión de secretos;
- monitoreo;
- diseño de red;
- documentación de clúster por ambiente.

El presente lineamiento desarrolla dichos criterios en reglas técnicas verificables, alineadas con los lineamientos actuales de arquitectura, seguridad, observabilidad, pruebas y desarrollo.
