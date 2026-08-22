# LIN-K8S-001 — Lineamiento de Contenedores y Orquestación ONP

**Código:** LIN-K8S-001  
**Versión:** v0.1.19  
**Estado:** En revisión  
**Fecha:** 2026-08-09  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Plataforma/Infraestructura, Seguridad Digital, Desarrollo, Arquitectura  
**Marco rector:** LIN-ARQ-001 — Marco Rector de Arquitectura de Software  
**Antecedente institucional:** INFORME-000082-2023-OTI.ID — Lineamientos de contenedores y orquestador de contenedores  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-26 | Arquitectura OTI | Borrador inicial del lineamiento de contenedores y orquestación |
| v0.1.1 | 2026-05-28 | Arquitectura OTI | Normaliza el lenguaje de despliegue hacia plan de reversa y elimina ambigüedad con rollback de base de datos |
| v0.1.2 | 2026-07-06 | Arquitectura OTI | Incorpora sección 4.3 (Runtime de contenedores: containerd/crictl) para reconciliar con LIN-ARQ-000 §11.1 y ADR-009, que ya establecían containerd como runtime de producción sin que este lineamiento lo reflejara operativamente. Añade términos al glosario. |
| v0.1.3 | 2026-07-06 | Arquitectura OTI | Incorpora sección 4.4 (Convención de nombres de namespace: `<sistema>-<componente>`, sin sufijo de ambiente, confirmado un clúster K8s por ambiente). Cierra el vacío señalado en `Matriz_Propiedad_Documental` donde este lineamiento figuraba como dueño de la política de namespaces sin haberla definido. Señala como pendiente de verificación con Plataforma la posible inconsistencia de sufijos de ambiente en namespaces de infraestructura compartida (`otel-{env}` en LIN-OBS-001, `kafka-{env}` en LIN-BUS-001) |
| v0.1.4 | 2026-07-06 | Arquitectura OTI | Incorpora sección 9.4 normando los Patrones Multi-Contenedor en el Pod: Sidecar (PA12) y Ambassador (PA13), reconciliando su uso con LIN-OBS-001 (OTEL Collector centralizado) y LIN-ARQ-000 §2.2/§3.7 (resiliencia nativa en JVM vs. migración Strangler Fig de sistemas legacy no-Java). |
| v0.1.5 | 2026-07-06 | Arquitectura OTI | Reconcilia gobierno de Secrets (§8.1) asumiendo la propiedad normativa definitiva según la Matriz de Propiedad Documental. Resuelve observación sobre sufijos de ambiente en namespaces compartidos (§4.4) como transición operativa. Incorpora blindaje de supremacía jerárquica de LIN-ARQ-000 en §2 y §20. |
| v0.1.6 | 2026-07-06 | Arquitectura OTI | Corrige el manifiesto de ejemplo de la Excepción 1 de Sidecar (§9.4): reemplaza el registro `registry.gitlab.com` (incorrecto) por `registry.gitlab.onp.gob.pe` (institucional, ya definido en §6.1); alinea namespace, labels y nombres al sistema de referencia `past` y a la convención `<sistema>-<componente>` de §4.4. Suaviza la nota de §4.4 sobre namespaces compartidos: ya no asume que el sufijo de ambiente en `otel-{env}`/`kafka-{env}` responde a una transición operativa — queda explícitamente pendiente de confirmación con Plataforma |
| v0.1.7 | 2026-07-06 | Arquitectura OTI | Incorpora en §4.4 un ambiente UAT/Preproducción opcional por proyecto (entre QA y PROD), reconciliando con `LIN-PERF-001 §12` y `LIN-SEC-APP-001` que ya lo asumían informalmente, y con la Plantilla de Documento de Arquitectura que ya lo permite condicionalmente. Distingue explícitamente este ambiente del `PQA` legado de `LIN-VER-001` (etapa de rama, no ambiente de despliegue). Actualiza §10.3 (réplicas mínimas) y §16 (documentación por ambiente) para contemplarlo cuando exista |
| v0.1.8 | 2026-07-06 | Arquitectura OTI | Exige ADR aprobado por Arquitectura y Plataforma para adoptar el ambiente opcional UAT/Preproducción (§4.4), en coherencia con el mismo patrón de gate ya usado para CQRS y BD no relacional en LIN-ARQ-000; agrega detonadores válidos y no válidos, y contenido mínimo del ADR. Añade el caso a la lista de §20.1 |
| v0.1.9 | 2026-07-09 | Arquitectura OTI | Corrige las citas colgantes hacia el documento congelado `LIN-ARQ-000 §11.1`: el estadio de Transición (Docker Compose) y el runtime containerd de producción ahora citan `LIN-ARQ-001 §5.2` (Marco Rector vigente) |
| v0.1.10 | 2026-07-09 | Arquitectura OTI | Añade en §10.3 la diferenciación de réplicas mínimas y SLO por estilo arquitectónico (Monolito Modular 99.0% / Microservicio 99.5%), que solo existía en el documento congelado |
| v0.1.11 | 2026-07-10 | Arquitectura OTI | Corrige el ejemplo de imagen frontend en §5.4 y el Anexo D: reemplaza `nginx:stable-alpine` en puerto 80 (root) por `nginxinc/nginx-unprivileged:1.27-alpine` en puerto 8080 con directorios temporales en `/tmp`, alineando con `LIN-FE-ANG-001 §16` y con la propia regla de este documento (§14.1, `runAsNonRoot: true`) — el ejemplo previo violaba su propia norma y contradecía el anti-patrón explícito de §16.5 |
| v0.1.12 | 2026-07-14 | Arquitectura OTI | Corrige las 9 citas residuales al documento congelado `LIN-ARQ-000` que quedaron sin migrar en v0.1.9 (que solo corrigió §11.1): §1.3, la cláusula de supremacía jerárquica de §2 (que además decía erróneamente que el marco rector es "Nivel 2" — es Nivel 1), tabla de §2, detonador NoSQL de §5.2, atribución de los patrones Sidecar/Ambassador de §9.4 (son normados por este propio documento, no por el marco rector), reglas de resiliencia y Strangler Fig de §9.4.2/9.4.3, y la cláusula de supremacía del proceso ADR en §20. Todas redirigidas a `LIN-ARQ-001` (§2.1, §2.2, §4.3, §6.2) y `LIN-DIS-001 §6` según corresponda |
| v0.1.13 | 2026-08-09 | Arquitectura OTI | `§9.1` y el Anexo E: la `NetworkPolicy` pasa de *recomendada / obligatoria para críticos* a **obligatoria para todo servicio que reciba tráfico interno**. No es un endurecimiento aislado: `ADR-TLS-INTERNO-001` admite tráfico intra-cluster sobre HTTP y la restricción de red es el control que **sustituye** al cifrado en ese tramo (`GOB-CHK-001` H24.4) |
| v0.1.14 | 2026-08-17 | Arquitectura OTI | Revisión de fondo (`GOB-CHK-001` H26). **(1) `§9.4` normaba Sidecar y Ambassador con los códigos `PA12`/`PA13` del tablero de brechas `GOB-BRE-001`**, que es un inventario de vacíos: mientras un patrón figura ahí se está declarando que *falta* normarlo. Pasan a `PT17`/`PT18` con fichas `PAT-K8S-01` y `PAT-K8S-02` en `LIN-PAT-001`, y las brechas se cierran. **(2) `§9.4.B` reintroducía el mandato de Resilience4j** que ya se había eliminado de `LIN-ARQ-001 §4.3` y de la Plantilla de Arquitectura: era la quinta fuente del mismo control, y además atribuía el Retry a Resilience4j cuando el dueño (`LIN-DIS-001 §6.3`) usa Spring Retry. Ahora remite al dueño y conserva solo lo propio —*dónde* vive el control, no cuál es—. **(3) El `Deployment` de referencia de `§9.2` no cumplía este documento:** le faltaban dos de las cinco etiquetas obligatorias de `§9.3` y, sobre todo, declaraba `readOnlyRootFilesystem: true` sin el volumen `/tmp` que exige la nota 14.1 — copiado tal cual, **el pod no arranca**. **(4) `§15.1` y `§15.2` degradaban a «cuando aplique»** obligaciones de `LIN-OBS-001`, que es documento **vigente**. `§15.2` fija además la convención de puerto de Actuator que ningún documento del corpus definía, pese a que `LIN-API-REST-001 §9.5` la exige (`GOB-CHK-001` H24.5). **(5)** `§18.2` y el Anexo A no incluían la `NetworkPolicy` que `§9.1` volvió obligatoria; `§20` atribuía los patrones de resiliencia a `LIN-ARQ-001` en vez de a `LIN-DIS-001 §6`; y `§2` no listaba `LIN-DIS-001`, `LIN-PAT-001`, `LIN-VER-001`, `LIN-IAC-001` ni `GOB-MAT-001`. El documento pasa a **En revisión** |
| v0.1.15 | 2026-08-17 | Arquitectura OTI | La nota de gobernanza de `§4.4` atribuía a `LIN-BUS-001` los namespaces `kafka-dev`/`kafka-qa`/**`kafka`**, cuando ese documento usa `kafka-prod` en producción. La verificación pendiente con Plataforma sigue abierta, pero ahora parte del dato correcto (`GOB-CHK-001` H28) |
| v0.1.16 | 2026-08-17 | Arquitectura OTI | El `nginx.conf` del Anexo D dirigía `error_log` a `/var/log/nginx/error.log`, incompatible con el `readOnlyRootFilesystem: true` que exige `§14.1` y con la regla de logs a stdout/stderr de `§15.1`. Apunta ahora a `/dev/stderr` (`GOB-CHK-001` H29) |
| v0.1.17 | 2026-08-17 | Arquitectura OTI | `§13.3` exigía declarar una «política de backup» para todo PVC sin que ningún documento del corpus la definiera. Ahora se deriva de la banda de criticidad de `LIN-ARQ-001 §5.4.1`: todo PVC de un sistema de criticidad Alta o Media requiere respaldo acorde a su RPO **o** justificación documentada de que el dato es reconstruible (`GOB-CHK-001` H11.2) |
| v0.1.18 | 2026-08-18 | Arquitectura OTI | El apartado de excepción titulaba «Proceso ADR para desviaciones» y no definía identificador: una desviación de este lineamiento se registraba como «un ADR», instrumento que `GOB-MAT-001` reserva a las decisiones **institucionales** del Comité. Pasa a **`EXC-K8S-NNN`**, con vigencia acotada y fecha de revisión obligatoria (`GOB-CHK-001` H38) |
| v0.1.19 | 2026-08-21 | Arquitectura OTI | `§16` conecta la ficha de despliegue con el inventario documental de `LIN-DOC-001 §4` y con el runbook de `DOC-R-003`, que no existía como artefacto normado (`GOB-CHK-001` H42) |

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
20. [Proceso ADR para desviaciones](#20-proceso-de-excepción-exc-k8s-nnn)  
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
| Definición de arquitectura de aplicación | LIN-ARQ-001 |
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

> **Importante:** **Supremacía Jerárquica del Marco Rector (LIN-ARQ-001):**  
> `LIN-ARQ-001` es el **documento rector de jerarquía superior (Nivel 1)** que rige de manera absoluta sobre todos los estándares tácticos de Nivel 2 (`LIN-DIS-001`) y de implementación de Nivel 3 (incluyendo el presente documento, `LIN-DEV-JAVA-001`, `LIN-OBS-001`, `LIN-SEC-APP-001`, etc. — ver el modelo de 3 niveles en `LIN-ARQ-001 §1.2`). Este lineamiento implementa de forma táctica y operativa en Kubernetes y contenedores los principios arquitectónicos y patrones de despliegue normados en `LIN-ARQ-001` y, para los patrones tácticos multi-contenedor (Sidecar, Ambassador), en este mismo documento (§9.4). **Ante cualquier vacío, conflicto o presunta discrepancia de interpretación entre este documento y el marco rector, prevalecerán siempre y en todo momento los mandatos, patrones y directivas de LIN-ARQ-001.**

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Arquitectura de Software | LIN-ARQ-001 | Define Kubernetes como destino objetivo y estilos de despliegue |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Define stack Java/Spring Boot y configuración de aplicación |
| Estándar de APIs REST | LIN-API-REST-001 | Define exposición de APIs y relación con WSO2 API Manager |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Define logs, métricas, health checks y trazas |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Define secretos, escaneo, usuario no root y controles de seguridad |
| Estándar de Pruebas | LIN-TEST-001 | Define evidencias de pruebas antes de pase |
| Estándar de Diseño de Software | LIN-DIS-001 | Nivel 2: dueño de la resiliencia táctica (§6), que este lineamiento no redefine |
| Catálogo Oficial de Patrones | LIN-PAT-001 | Fuente única de los códigos `PT` y de las fichas de decisión, incluidas `PAT-K8S-01` y `PAT-K8S-02` |
| Versionamiento y Control de Cambios | LIN-VER-001 | Norma el versionado de los manifiestos y la revisión por Merge Request |
| Infraestructura como Código | LIN-IAC-001 | **Borrador**: dueño de Terraform y del clúster declarativo |
| Matriz de Propiedad Documental | GOB-MAT-001 | Determina qué documento es dueño de cada tema |
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

El clúster de Kubernetes de la ONP usa **containerd** como container runtime de producción — no Docker Engine. Esta decisión ya está sancionada en `LIN-ARQ-001 §5.2` (ADR-009) y se reproduce aquí porque afecta directamente cómo Desarrollo y Plataforma operan e inspeccionan contenedores.

| Aspecto | Regla |
|---|---|
| Runtime en DEV local | Docker Engine (o Podman) — libre elección del desarrollador |
| Runtime en Transición | Docker Engine + Docker Compose (ver `LIN-ARQ-001 §5.2`) — etapa temporal, no es destino final |
| Runtime en QA y PROD (clúster K8s) | **containerd** — único runtime soportado por Plataforma |
| Construcción de imágenes | El `Dockerfile` sigue siendo el estándar de construcción en todos los casos (ver sección 5). Produce imágenes OCI estándar, compatibles con containerd sin cambios |
| Inspección de contenedores en nodos QA/PROD | `crictl`, no `docker`. El comando `docker` no existe ni aplica en los nodos del clúster |

> **Qué NO cambia para Desarrollo:** el Dockerfile, el proceso de build local con Docker y las pruebas de la imagen en el equipo del desarrollador siguen siendo iguales. La diferencia de runtime es puramente operativa, del lado de Plataforma, y ocurre únicamente en los nodos del clúster — no afecta cómo se construye ni cómo se prueba una imagen antes de subirla al registro.

> **Anti-patrón:** documentar procedimientos de troubleshooting o runbooks que asuman `docker exec`, `docker logs` o `docker ps` contra un nodo de QA/PROD. En esos ambientes el equivalente es `crictl exec`, `crictl logs`, `crictl ps` (o `kubectl exec`/`kubectl logs` a nivel de pod, que es la vía preferente para Desarrollo).

### 4.4 Convención de nombres de namespace

ONP opera **un clúster Kubernetes independiente por ambiente**. El modelo base para todo sistema nuevo es **DEV → QA → PROD**.

**Ambiente adicional opcional — UAT / Preproducción:** se admite un cuarto ambiente entre QA y PROD, nombrado **UAT** o **Preproducción** según lo defina el proyecto, siguiendo el mismo principio de aislamiento (clúster independiente, misma convención de namespace de esta sección). No es un estándar universal — es una excepción al modelo base de 3 ambientes y, como toda excepción en este lineamiento (ver sección 20), **requiere ADR aprobado por Arquitectura y Plataforma antes de aprovisionarse**.

*Detonadores válidos* (al menos uno, con evidencia concreta del proyecto):
- Requisito regulatorio o contractual de un ambiente formal de aceptación de usuario (UAT) previo a Producción.
- Necesidad de pruebas de rendimiento/carga en condiciones equivalentes a PROD que QA no puede proveer por volumen de datos, topología de red o recursos (ver `LIN-PERF-001 §12`).
- Ventana de validación final pre-lanzamiento para sistemas de alta criticidad (ej. cálculo de pensiones) donde un defecto en PROD tiene consecuencia legal o financiera severa.

*Lo que NO es un detonador válido:* "por si acaso", costumbre de otros proyectos, o preferencia del equipo sin evidencia de una necesidad concreta — igual que se exige en la adopción de BD no relacional (`LIN-ARQ-001 §6.2`).

El ADR debe declarar: el detonador que lo justifica; si el ambiente es permanente para el sistema o temporal (ej. solo durante una migración), y en ese caso su criterio de retiro; y quién lo opera (Plataforma aprovisiona el clúster, Desarrollo lo usa). Una vez aprobado, el ambiente se documenta en la ficha de despliegue del sistema (ver sección 16), igual que ya lo contempla la Plantilla de Documento de Arquitectura ("si existe UAT también debe incluirse").

> **Distinción con el modelo legado `PQA`:** el ambiente UAT/Preproducción aquí descrito **no es lo mismo** que `ONP_PQA` del modelo vigente de ramas Git (`LIN-VER-001 §5`) — aquel es una etapa de precalidad/estabilización *entre DEV y QA*, ligada a una rama, no un ambiente de despliegue con clúster propio. No deben confundirse ni fusionarse.

El ambiente **nunca** forma parte del nombre del namespace de aplicación — ya está implícito en a qué clúster pertenece, sin importar cuántos ambientes tenga el proyecto.

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

> **Nota de Gobernanza — Namespaces Compartidos Pendientes de Verificar:** Al existir un clúster independiente por ambiente, incluir el sufijo de ambiente en namespaces de negocio es un anti-patrón redundante — así se norma en esta sección. Para los namespaces de infraestructura compartida ya documentados en `LIN-OBS-001` (`otel-dev`, `otel-qa`, `otel`) y `LIN-BUS-001` (`kafka-dev`, `kafka-qa`, `kafka-prod` — ver `LIN-BUS-001 §12.1`; una versión anterior de esta nota los citaba como `kafka` sin sufijo en producción, lo que no correspondía a lo que ese documento dice), este lineamiento **no asume** la razón del sufijo: se recomienda que Plataforma confirme si esos componentes viven en un clúster de plataforma compartido o híbrido distinto al de las aplicaciones. Si ese es el caso, el sufijo está justificado y debe mantenerse; si no, esos dos documentos deberían normalizarse a `otel` y `kafka` sin sufijo, sin afectar a las aplicaciones consumidoras (que resuelven por DNS interno o configuración de ambiente en `LIN-OBS-001 §10.2`).

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

Las aplicaciones Angular se construyen como artefacto estático y se sirven mediante un contenedor web. **Fuente autoritativa:** la especificación completa de la imagen, el `nginx.conf` y los manifiestos Kubernetes está en `LIN-FE-ANG-001 §16`. Esta sección resume únicamente la regla que aplica de forma transversal a todo contenedor del clúster (§14.1: `runAsNonRoot: true`) y el ejemplo mínimo consistente con ella.

**Regla obligatoria:** ejecutar Nginx en el puerto 80 requiere privilegios de root, lo que viola directamente `runAsNonRoot: true` (§14.1) e impide el arranque del pod en el clúster corporativo. La imagen base debe ser `nginxinc/nginx-unprivileged`, nunca `nginx`/`nginx:alpine` oficial.

Ejemplo referencial (coincide con `LIN-FE-ANG-001 §16.3`):

```dockerfile
# Build Angular
FROM node:20-alpine AS build
WORKDIR /workspace

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Runtime web — imagen no root, puerto no privilegiado
FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY --from=build /workspace/dist/<nombre-proyecto>/browser /usr/share/nginx/html

# Configuración nginx para SPA Angular (ver Anexo D)
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080
```

> La imagen `nginx-unprivileged` ya ejecuta Nginx como `uid 101`; no se declara `USER` explícitamente. El Service/Deployment deben referenciar el puerto `8080` (`targetPort: 8080`) y el `securityContext` debe fijar `runAsUser: 101` — ver `LIN-FE-ANG-001 §16.4`.

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

> 🔖 **`K8S-R-001`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

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

> **Mandato de Propiedad Documental:** En cumplimiento con la Matriz de Propiedad Documental, `LIN-K8S-001` asume la **propiedad normativa definitiva** sobre la provisión, inyección y ciclo de vida operativo de los Kubernetes Secrets en todos los clústeres ONP, siendo `LIN-SEC-APP-001 §12` el consumidor normativo que rige las políticas de cifrado, rotación y complejidad. Toda variable sensible de runtime (credenciales de BD, tokens SAA, llaves privadas) se inyectará obligatoriamente a través de Secrets y nunca mediante ConfigMaps, variables en duro o variables de pipeline CI/CD.

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

> 🔖 **`K8S-R-002`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

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
| `NetworkPolicy` | **Obligatorio para todo servicio que reciba tráfico dentro del cluster** (ver nota) |
| `PodDisruptionBudget` | Recomendado para servicios críticos |
| `ResourceQuota` / `LimitRange` | Responsabilidad de Plataforma por namespace |

> **Por qué la `NetworkPolicy` dejó de ser recomendada.** `ADR-TLS-INTERNO-001` admite que el tráfico entre el punto de terminación TLS y el pod destino viaje sobre HTTP dentro del cluster. Esa excepción se sostiene **únicamente** porque el acceso a la red interna está restringido: la `NetworkPolicy` es el control que sustituye al cifrado, no un refuerzo opcional. Un servicio sin ella no puede acogerse a la excepción y debe servir HTTPS extremo a extremo. Política mínima en el [Anexo E](#anexo-e--networkpolicy-mínima-para-servicio-backend).

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
    app.kubernetes.io/component: api
    app.kubernetes.io/managed-by: kustomize
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
        app.kubernetes.io/component: api
        app.kubernetes.io/managed-by: kustomize
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
          # readOnlyRootFilesystem exige montar /tmp: Spring Boot escribe ahí
          # sus temporales y sin este volumen el contenedor no arranca (nota 14.1)
          volumeMounts:
            - name: tmp
              mountPath: /tmp
      volumes:
        - name: tmp
          emptyDir:
            medium: Memory
            sizeLimit: 64Mi
```

> Este manifiesto declara las cinco etiquetas obligatorias de [§9.3](#93-etiquetas-obligatorias) y el volumen temporal que exige la [nota 14.1](#nota-141--readonlyrootfilesystem-con-escritura-temporal). Ambas cosas faltaban en versiones anteriores: copiado tal cual, el pod fallaba al arrancar porque `readOnlyRootFilesystem: true` impedía a Spring Boot escribir en `/tmp`, y el remedio vivía 350 líneas más abajo (`GOB-CHK-001` H26).

### 9.3 Etiquetas obligatorias

Usar etiquetas estándar:

```yaml
app.kubernetes.io/name: <nombre-componente>
app.kubernetes.io/part-of: <sistema>
app.kubernetes.io/version: <version>
app.kubernetes.io/component: <api|frontend|worker|job>
app.kubernetes.io/managed-by: <equipo-o-herramienta>
```

### 9.4 Patrones Multi-Contenedor en el Pod: Sidecar (PT17) y Ambassador (PT18)

> 🔖 **`K8S-R-003`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

En Kubernetes, la unidad atómica de despliegue es el **Pod**. Por regla general y en estricta coherencia con el principio de separación de responsabilidades (§4.2), **un pod en la ONP debe contener un único contenedor de negocio (estilo 1 Pod = 1 Contenedor)**.

No obstante, este lineamiento norma dos patrones tácticos de despliegue multi-contenedor (*Multi-Container Pod Patterns*): **Sidecar (`PT17`, ficha `PAT-K8S-01`)** y **Ambassador (`PT18`, ficha `PAT-K8S-02`)**, cuyas fichas de decisión viven en el catálogo oficial `LIN-PAT-001 §6`.

> **Sobre los códigos.** Versiones anteriores identificaban estos patrones como `PA12` y `PA13`. Esos códigos pertenecen al **tablero de brechas** `GOB-BRE-001`, que es un inventario de vacíos por cerrar, no el catálogo normativo: mientras un patrón figura ahí, se está declarando que *falta* normarlo. Los códigos oficiales de patrón son los `PT`, y su fuente única es `LIN-PAT-001` (`GOB-CHK-001` H26). Para prevenir sobre-ingeniería, desperdicio de recursos computacionales (CPU/RAM) y colisiones con otros lineamientos del framework institucional, su adopción se rige por las siguientes reglas binarias de aplicación:

#### A. Patrón Sidecar (PT17) — Co-procesos de Apoyo y Observabilidad

El patrón **Sidecar** adjunta un contenedor secundario al contenedor principal del pod para extender sus capacidades (recolección de logs, proxying, sincronización de secretos o monitoreo) sin modificar el código de la aplicación principal.

1. **Regla General en ONP (Prohibido para Java / Spring Boot 3):** Para aplicaciones construidas en Java 21 / Spring Boot 3 según `LIN-DEV-JAVA-001` y `LIN-OBS-001`, **queda terminantemente prohibido** desplegar contenedores sidecar para observabilidad o envío de logs/trazas. El SDK de OpenTelemetry integrado en la aplicación emite telemetría directamente vía red (OTLP) hacia el **OTEL Collector centralizado y compartido** operado por Plataforma en el namespace `otel` / `otel-{env}` (`LIN-OBS-001 §9.1`). Introducir un sidecar por pod en servicios nativos es un anti-patrón que duplica consumo de memoria y saturación de red.
2. **Excepción Legítima 1 (Cajas Negras / COTS / Sistemas Legacy):** El patrón Sidecar se autoriza *únicamente* cuando se contenerizan aplicaciones comerciales de terceros (COTS) o sistemas legacy (no-Java o sin acceso al código fuente) que no soportan el protocolo OTLP ni escriben en estándar `stdout`/`stderr`, sino en archivos locales de bitácora. En este escenario, se desplegará un contenedor sidecar ligero (ej. Fluent Bit) compartiendo un volumen `emptyDir` con el contenedor principal para leer la bitácora y reenviarla al OTEL Collector centralizado.
3. **Excepción Legítima 2 (mTLS y Service Mesh):** Se reserva el uso institucional del patrón Sidecar para cuando la Plataforma habilite formalmente una malla de servicios (*Service Mesh*, ej. Envoy/Istio), donde la infraestructura inyectará proxies de red de forma transparente para terminación mTLS y seguridad cero-confianza (*Zero Trust*) entre pods.

**Ejemplo de Manifiesto para Excepción de Logs en Caja Negra (Sidecar + emptyDir):**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: past-legado-adapter
  namespace: past-adapter
  labels:
    app.kubernetes.io/name: past-legado-adapter
    app.kubernetes.io/part-of: past
    app.kubernetes.io/component: adapter
spec:
  replicas: 2
  selector:
    matchLabels:
      app.kubernetes.io/name: past-legado-adapter
  template:
    metadata:
      labels:
        app.kubernetes.io/name: past-legado-adapter
        app.kubernetes.io/part-of: past
        app.kubernetes.io/component: adapter
    spec:
      containers:
        # 1. Contenedor Principal (Caja Negra / Legacy que escribe en archivo)
        - name: app-legacy
          image: registry.gitlab.onp.gob.pe/aplicaciones/past/legado-adapter:1.0.0
          volumeMounts:
            - name: logs-vol
              mountPath: /var/log/app
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
        # 2. Contenedor Sidecar (PT17 - Agente de reenvío de logs hacia OTEL Collector)
        - name: log-collector-sidecar
          image: registry.gitlab.onp.gob.pe/plataforma/infra/fluent-bit:2.2.0
          volumeMounts:
            - name: logs-vol
              mountPath: /var/log/app
              readOnly: true
          resources:
            requests:
              memory: "64Mi"
              cpu: "50m"
            limits:
              memory: "128Mi"
              cpu: "100m"
      volumes:
        - name: logs-vol
          emptyDir: {}
```

#### B. Patrón Ambassador (PT18) — Proxy de Salida para Resiliencia e Integración

El patrón **Ambassador** actúa como un proxy de red local dentro del pod que media y blinda todo el tráfico saliente (*outbound traffic*) desde la aplicación hacia sistemas externos, APIs, bases de datos o servicios heredados.

1. **Regla General en ONP (Prohibido para Java / Spring Boot 3):** En coherencia con **`ARQ-R-004` (LIN-ARQ-001 §4.3)** (Interoperabilidad Gubernamental y SOA) y **`DIS-R-007` (LIN-DIS-001 §6)** (Resiliencia Táctica), para aplicaciones construidas en Java 21 / Spring Boot 3, **está estrictamente prohibido utilizar un Ambassador sidecar para gestionar resiliencia o conectividad saliente**. Toda la resiliencia de integración hacia sistemas externos (RENIEC, SUNAT, PIDE) o servicios WSO2 debe resolverse **dentro de la JVM**, en la capa de infraestructura del software (`pe.gob.onp.<sistema>.<modulo>.infrastructure.client.*`).

   > **El mecanismo lo define `DIS-R-007` (LIN-DIS-001 §6), documento dueño de la resiliencia táctica** — este lineamiento no lo redefine. En resumen, y sin sustituir al dueño: *Timeout* siempre obligatorio y *Bulkhead* por defecto con **Apache HttpClient 5** (`setMaxConnPerRoute`); *Retry* con **Spring Retry**; y **Circuit Breaker con Resilience4j solo en Microservicios, o en Monolito Modular bajo ADR** (`DIS-R-009` (LIN-DIS-001 §6.2)). Fichas `PAT-RES-01` y `PAT-RES-02`.

   Lo que este lineamiento sí norma es **dónde** vive ese control: dentro del proceso Java, nunca en un proxy de red adjunto al pod. Ningún desarrollador Java debe delegar, duplicar ni configurar políticas de reintento o circuit breaker en un contenedor Ambassador.
2. **Única Excepción Legítima (Strangler Fig sobre Monolitos Legacy No-Java):** En el marco de la hoja de ruta de modernización institucional (**`LIN-ARQ-001 §2.2`** Strangler Fig y **§2.1** Estadio 1 — Monolito Tradicional), cuando se contenericen sistemas heredados (ej. monolitos en JBoss, WebLogic, C++ o frameworks antiguos) cuyo código fuente no puede ser refactorizado o modificado para incorporar políticas de resiliencia o seguridad moderna, se autoriza el despliegue de un **Ambassador sidecar** (ej. Envoy, Envoy-based Proxy o WSO2 Microgateway ligero) en el pod. En este escenario —y solo en este—, el Ambassador asumirá la terminación mTLS, rotación de cabeceras, timeouts y reintentos hacia el exterior, protegiendo al monolito heredado sin necesidad de reescribir su lógica interna.

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
| UAT / Preproducción (si existe) | 2 — debe reflejar el comportamiento real de PROD |
| PROD | 2 para servicios críticos |

Los servicios críticos no deben operar con una sola réplica en Producción salvo excepción aprobada.

La tabla anterior diferencia por **ambiente**. Adicionalmente, el **estilo arquitectónico** (`LIN-ARQ-001 §2`) fija el SLO mínimo exigible en Producción — un Microservicio, al depender de red entre servicios, tiene mayor riesgo de indisponibilidad parcial y exige un SLO más estricto que un Monolito Modular:

| Estilo arquitectónico | Réplicas mínimas en PROD | SLO mínimo |
|---|---:|---:|
| Monolito Modular | 2 | 99.0% |
| Microservicio | 2 | 99.5% |

Los SLOs se definen y miden con el stack de observabilidad OTEL/Prometheus/Grafana — ver `LIN-OBS-001`.

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

> **La «política de backup» se deriva de la banda de criticidad del sistema** (`LIN-ARQ-001 §5.4.1`), no se define por proyecto. Todo PVC de un sistema de criticidad **Alta o Media** requiere respaldo con frecuencia coherente con su RPO, **o** justificación documentada de por qué el dato es reconstruible sin él. Un PVC sin ninguna de las dos cosas no puede aprobarse para producción.

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

Los logs deben cumplir `LIN-OBS-001`, que es documento **vigente** y no admite excepción en estos campos:

- JSON estructurado en formato ECS (`LIN-OBS-001 §6`);
- `trace.id`;
- `span.id`;
- `http.request.id`;
- `service.name`;
- sin PII;
- sin tokens;
- sin secretos.

### 15.2 Métricas

Todo servicio Spring Boot expone métricas por Actuator/Prometheus. No es condicional: `LIN-OBS-001` y `LIN-API-REST-001 §9.5` lo exigen sin excepción, y un servicio sin métricas no está listo para producción.

El endpoint `/actuator/prometheus` **no debe quedar accesible desde fuera del clúster** (§12.2). Para lograrlo hay dos caminos válidos y el proyecto debe elegir uno explícitamente:

| Opción | Cómo | Consecuencia en el manifiesto |
|---|---|---|
| **Puerto único** *(la que asumen los ejemplos de este lineamiento y del corpus)* | Actuator en el mismo puerto de la aplicación, protegido por `NetworkPolicy` (§9.1) e Ingress que no publica `/actuator` | Las probes apuntan al puerto `http` — como en §9.2 |
| **Puerto de gestión separado** | `management.server.port` distinto del puerto de la API | **Las probes deben apuntar a ese puerto**, y el `containerPort` correspondiente debe declararse. Dejarlas en el puerto de la aplicación produce `404`, el pod nunca pasa a *Ready* y el despliegue no converge |

> La convención no estaba fijada en ningún documento del corpus, pese a que `LIN-API-REST-001 §9.5` exige separar el puerto o restringir por red (`GOB-CHK-001` H24.5). Queda fijada aquí, que es el documento dueño de los manifiestos.

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

> La **ficha de despliegue** de esta sección forma parte del inventario documental mínimo de todo proyecto (`LIN-DOC-001 §4`), que define además dónde vive y cuándo se revisa. El **runbook de operación** —obligatorio para criticidad Alta o Media— lo norma `DOC-R-003` (LIN-DOC-001 §8).

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

> Si el proyecto usa un ambiente adicional de **UAT / Preproducción** (ver §4.4), debe documentarse con los mismos campos de esta tabla.

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
[ ] NetworkPolicy definida (obligatoria si recibe tráfico interno — §9.1)
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

## 20. Proceso de excepción (`EXC-K8S-NNN`)

> **Instrumento correcto: `EXC-K8S-NNN`, no un ADR.** Conforme a `GOB-MAT-001` (Registro de decisiones y excepciones), la desviación de un lineamiento **en un proyecto concreto** se registra como excepción con vigencia acotada y **fecha de revisión**, nunca indefinida. El `ADR-NNN` queda reservado a decisiones **institucionales** del Comité de Arquitectura, que obligan a todo el corpus; llevar allí cada desviación de cada sistema vaciaría de valor ese registro. La excepción se aprueba por Arquitectura OTI, con **Plataforma y Seguridad** cuando la desviación afecte al clúster y se registra en el documento de arquitectura del sistema (`GOB-PLA-001`, Anexo E, criterio 14).


> **Importante:** **Gobernanza y Supremacía de LIN-ARQ-001:** En estricta coherencia con la supremacía jerárquica del marco rector de **Nivel 1**, ningún ADR podrá ser aprobado ni será válido si contraviene los principios arquitectónicos fundamentales o los mandatos rectores de **LIN-ARQ-001**, ni las reglas de resiliencia táctica de **`DIS-R-007` (LIN-DIS-001 §6)**, que es su documento dueño, salvo autorización expresa y excepcional de la Dirección de Arquitectura de la OTI.

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
- Adoptar un ambiente adicional de UAT/Preproducción para el proyecto (ver §4.4).

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
│   ├── networkpolicy.yaml
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

**Puerto y usuario no root:** la imagen base es `nginxinc/nginx-unprivileged` (§5.4), que ejecuta como `uid 101` y no puede bindear puertos privilegiados (`<1024`). Por eso este `nginx.conf` escucha en `8080`, no en `80`, y redirige los directorios temporales de trabajo (`client_body_temp_path` y similares) a `/tmp`, que sí es escribible por un usuario no root. Configuración completa y autoritativa en `LIN-FE-ANG-001 §16.2`.

```nginx
# nginx.conf — configuración mínima para Angular SPA (usuario no root, puerto 8080)
worker_processes auto;
# stderr explícito: bajo `readOnlyRootFilesystem: true` nginx no puede crear el
# archivo, y LIN-K8S-001 sección 15.1 exige logs a stdout/stderr de todos modos.
error_log  /dev/stderr warn;
pid        /tmp/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include      /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Directorios temporales fuera de /var/run — accesibles por uid 101
    client_body_temp_path /tmp/client_temp;
    proxy_temp_path       /tmp/proxy_temp_path;
    fastcgi_temp_path     /tmp/fastcgi_temp;
    uwsgi_temp_path       /tmp/uwsgi_temp;
    scgi_temp_path        /tmp/scgi_temp;

    sendfile       on;
    keepalive_timeout 65;

    # Compresión
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    server {
        listen 8080;
        server_name _;

        root /usr/share/nginx/html;
        index index.html;

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
}
```

Este archivo debe copiarse en el Dockerfile de Angular:

```dockerfile
COPY nginx.conf /etc/nginx/nginx.conf
```

### Anexo E — NetworkPolicy mínima para servicio backend

La `NetworkPolicy` restringe el tráfico de red a nivel de pod. Es **obligatoria para todo servicio que reciba tráfico dentro del cluster** (ver 9.1): es el control que sustituye al cifrado en el tramo interno admitido por `ADR-TLS-INTERNO-001`.

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
