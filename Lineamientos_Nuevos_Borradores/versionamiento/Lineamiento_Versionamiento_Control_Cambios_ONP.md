# LIN-VER-001 — Lineamiento de Versionamiento, Control de Cambios y Revisión de Código ONP

**Código:** LIN-VER-001  
**Versión:** v0.1.8  
**Estado:** En revisión  
**Fecha:** 2026-08-09  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Desarrollo, QA, Seguridad Digital, Plataforma/Infraestructura, Arquitectura  
**Marco rector:** LIN-ARQ-001 — Marco Rector de Arquitectura de Software  
**Herramienta institucional:** GitLab Ultimate  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-26 | Arquitectura OTI | Borrador inicial del lineamiento de versionamiento, control de cambios y revisión de código |
| v0.1.1 | 2026-05-27 | Arquitectura OTI | Incorpora modelo de transición: modelo vigente basado en ramas por promoción y modelo objetivo GitLab Flow simplificado (disciplinado con principios TBD) para proyectos nuevos |
| v0.1.2 | 2026-05-28 | Arquitectura OTI | Agrega guía operativa para elegir el modelo de branching y sustituye el concepto genérico de rollback BD por estrategia de reversa según el tipo de cambio |
| v0.1.3 | 2026-05-28 | Arquitectura OTI | Alinea el lenguaje de Merge Requests y evidencias al concepto de reversa/compensación para cambios de base de datos |
| v0.1.4 | 2026-05-28 | Arquitectura OTI | Completa alineación terminológica: reemplaza las tres ocurrencias restantes de "rollback" por "plan de reversa" en [sección 15.2](#152-release-notes-mínimas), [sección 18](#18-cambios-en-contenedores-y-manifiestos-kubernetes) y checklist [sección 22.4](#224-release) |
| v0.1.5 | 2026-05-28 | Arquitectura OTI | Precisa encabezado de tabla comparativa [sección 4.3](#43-comparación-visual-de-modelos): "GitLab Flow" → "GitLab Flow — ramas por ambiente" para eliminar ambigüedad con el modelo objetivo |
| v0.1.6 | 2026-07-10 | Arquitectura OTI | Migra Marco rector de `LIN-ARQ-000` (congelado) a `LIN-ARQ-001` (vigente) |
| v0.1.7 | 2026-08-09 | Arquitectura OTI | `§16.1.1` atribuye explícitamente la nomenclatura de scripts a su dueño `LIN-BD-ORA-001 §8.1`/`§8.4` en vez de presentarla como propia (`GOB-CHK-001` H14.5). El tema figuraba en la matriz con **dos dueños simultáneos**, contra su principio rector; ahora está dividido: obligatoriedad de versionar aquí, nomenclatura y estructura en el estándar de BD |
| v0.1.8 | 2026-08-09 | Arquitectura OTI | Revisión de fondo (`GOB-CHK-001` H23). (1) La [sección 12](#12-revisión-de-código) asume la propiedad del **proceso** de revisión para todo tipo de cambio e incorpora el límite de **400 líneas** que hasta ahora vivía solo en `LIN-DEV-JAVA-001 §16.4`, donde de hecho dejaba sin regla de tamaño a los MR de Angular, SQL y manifiestos. (2) La [sección 13](#13-evidencias-mínimas-por-tipo-de-cambio) decía "cobertura si aplica", degradando a opcional un umbral **obligatorio** de `LIN-TEST-001` (documento vigente); ahora remite a `LIN-TEST-001 §5.1`. (3) La [sección 15.1](#151-regla-general) declaraba obligatorio un formato de tag que excluía las pre-releases admitidas por la propia [sección 14.3](#143-pre-releases). (4) La [sección 15.3](#153-relación-con-imágenes-de-contenedor) admitía "un identificador trazable" como tag de imagen, resquicio por el que cabía `latest`, prohibido por `LIN-K8S-001 §6.3`. (5) Protected branches, Merge Requests y approval rules pasan de recomendados a **obligatorios** en la [sección 21.1](#211-capacidades-a-habilitar-desde-esta-fase): son el mecanismo que hace exigibles P2, P3 y la prohibición de autoaprobación. (6) La [sección 2](#2-normativa-y-documentos-relacionados) incorpora `LIN-DIS-001` y `GOB-MAT-001`. Se ordena cronológicamente este control de cambios y el documento pasa a **En revisión** |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)  
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)  
3. [Principios rectores](#3-principios-rectores)  
4. [Modelo institucional adoptado y transición](#4-modelo-institucional-adoptado-y-transición)  
   - [4.3 Comparación visual de modelos](#43-comparación-visual-de-modelos)  
5. [Modelo vigente: ramas institucionales por promoción](#5-modelo-vigente-ramas-institucionales-por-promoción)  
6. [Modelo objetivo: GitLab Flow simplificado](#6-modelo-objetivo-gitlab-flow-simplificado)  
7. [Criterios para elegir modelo por tipo de proyecto](#7-criterios-para-elegir-modelo-por-tipo-de-proyecto)  
8. [Modelo de repositorios GitLab](#8-modelo-de-repositorios-gitlab)  
9. [Nomenclatura de grupos, subgrupos y proyectos](#9-nomenclatura-de-grupos-subgrupos-y-proyectos)  
10. [Ramas de trabajo](#10-ramas-de-trabajo)  
11. [Merge Requests obligatorios](#11-merge-requests-obligatorios)  
12. [Revisión de código](#12-revisión-de-código)  
13. [Evidencias mínimas por tipo de cambio](#13-evidencias-mínimas-por-tipo-de-cambio)  
14. [Versionamiento semántico](#14-versionamiento-semántico)  
15. [Tags y releases](#15-tags-y-releases)  
16. [Control de cambios de base de datos](#16-control-de-cambios-de-base-de-datos)  
17. [Cambios en APIs, contratos y OpenAPI](#17-cambios-en-apis-contratos-y-openapi)  
18. [Cambios en contenedores y manifiestos Kubernetes](#18-cambios-en-contenedores-y-manifiestos-kubernetes)  
19. [Relación con ADRs](#19-relación-con-adrs)  
20. [Relación con CI/CD](#20-relación-con-cicd)  
21. [Uso de capacidades GitLab Ultimate](#21-uso-de-capacidades-gitlab-ultimate)  
22. [Checklist de conformidad](#22-checklist-de-conformidad)  
23. [Anti-patrones](#23-anti-patrones)  
24. [Proceso de excepción](#24-proceso-de-excepción)  
25. [Glosario](#25-glosario)  
26. [Anexos](#26-anexos)  

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece las reglas mínimas para organizar repositorios GitLab, controlar ramas, versionar código, revisar cambios, aprobar Merge Requests, etiquetar releases y asegurar trazabilidad entre requerimientos, incidencias, código fuente, pruebas, despliegue y decisiones de arquitectura.

Asimismo, define un modelo de transición para la ONP:

1. reconocer y ordenar el modelo vigente basado en ramas institucionales por promoción; y  
2. establecer como modelo objetivo para proyectos nuevos **GitLab Flow simplificado**, disciplinado con principios de Trunk-Based Development (TBD), incorporado de manera progresiva y alineado a la futura implementación de CI/CD.

El objetivo no es reemplazar inmediatamente el modelo vigente ni exigir automatización completa desde el primer día. El objetivo es establecer una base institucional de control de cambios aplicable desde ahora, usando GitLab como repositorio y mecanismo de revisión, y preparar el camino hacia integración continua.

### 1.2 Alcance

Aplica a:

| Elemento | Aplica |
|---|---|
| Repositorios de backend Java / Spring Boot | Sí |
| Repositorios de frontend Angular | Sí |
| Repositorios de workers, jobs o adapters | Sí |
| Repositorios de librerías internas | Sí |
| Repositorios de scripts de base de datos | Sí |
| Repositorios de manifiestos Kubernetes | Sí |
| Repositorios de documentación técnica | Sí |
| Cambios de configuración versionada | Sí |
| Cambios gestionados fuera de GitLab | No recomendado; deben migrar progresivamente |

### 1.3 Fuera de alcance

| Tema | Documento / responsable |
|---|---|
| Ejecución automática de pipelines | LIN-CICD-001 — Borrador |
| Definición de arquitectura de aplicación | LIN-ARQ-001 |
| Estándar de desarrollo Java | LIN-DEV-JAVA-001 |
| Estándar de APIs REST | LIN-API-REST-001 |
| Estándar de base de datos Oracle | LIN-BD-ORA-001 |
| Estándar frontend Angular | LIN-FE-ANG-001 |
| Seguridad en aplicaciones | LIN-SEC-APP-001 |
| Pruebas automatizadas | LIN-TEST-001 |
| Contenedores y orquestación | LIN-K8S-001 |
| Infraestructura como Código | LIN-IAC-001 — Borrador |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Arquitectura de Software | LIN-ARQ-001 | Define principios y decisiones rectoras de arquitectura |
| Lineamiento de Diseño de Software | LIN-DIS-001 | Nivel 2: define el diseño táctico que este lineamiento versiona y promueve |
| Matriz de Propiedad Documental | GOB-MAT-001 | Determina qué documento es dueño de cada tema y resuelve solapamientos |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Define reglas de implementación backend |
| Estándar de APIs REST | LIN-API-REST-001 | Define contrato, versionamiento y publicación de APIs |
| Estándar de Base de Datos Oracle | LIN-BD-ORA-001 | Define control de scripts y tratamiento de PL/SQL legacy |
| Estándar de Frontend Angular | LIN-FE-ANG-001 | Define reglas para aplicaciones frontend |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Define trazabilidad y evidencias operativas |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Define controles de seguridad y excepciones |
| Estándar de Pruebas | LIN-TEST-001 | Define evidencias de pruebas y cobertura |
| Contenedores y Orquestación | LIN-K8S-001 | Define imágenes, manifiestos, tags y despliegue en K8s |
| Directiva de Desarrollo de Software Seguro | DIR-SEC-SW-001 | Marco superior para controles de seguridad |
| CI/CD | LIN-CICD-001 | Capacidad objetivo; en fase de borrador |
| Infraestructura como Código | LIN-IAC-001 | **Borrador**; las convenciones de versionamiento semántico (`vMAJOR.MINOR.PATCH`) y etiquetado Git aplican también al repositorio `oti-plataforma/infrastructure-iac` |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **Trazabilidad completa** | Todo cambio debe poder rastrearse desde requerimiento, incidencia o tarea hasta commit, Merge Request, release y despliegue. |
| P2 | **No cambios directos en ramas protegidas** | Todo cambio debe ingresar mediante Merge Request. |
| P3 | **Revisión obligatoria** | Ningún cambio debe aprobarse sin revisión técnica. |
| P4 | **Separación por componente desplegable** | Los repositorios se organizan por componente con ciclo propio de construcción, despliegue o versionamiento. |
| P5 | **Transición controlada** | Se reconoce el modelo vigente, pero se define un modelo objetivo para nuevos proyectos. |
| P6 | **GitLab Flow simplificado como modelo objetivo** | Para proyectos nuevos, modernizaciones relevantes y componentes bajo el nuevo marco, el modelo objetivo será GitLab Flow simplificado, disciplinado con principios de Trunk-Based Development (TBD). |
| P7 | **Configuración fuera del código** | Las diferencias entre DEV, QA y PROD se gestionan mediante configuración externa, conforme a LIN-K8S-001. |
| P8 | **Evidencia mínima antes del merge** | Todo Merge Request debe indicar qué se probó, qué cambió y qué impacto tiene. |
| P9 | **Versionamiento explícito** | Todo release productivo debe contar con tag y versión identificable. |
| P10 | **Scripts de BD versionados** | Todo cambio de base de datos debe estar en GitLab; no se aceptan scripts sueltos enviados por correo o mensajería. |
| P11 | **Preparación para CI/CD** | Este lineamiento establece la base para automatización futura, sin exigir CI/CD completo en esta fase. |

---

## 4. Modelo institucional adoptado y transición

La ONP define dos modelos de versionamiento y control de cambios:

| Modelo | Uso |
|---|---|
| **Modelo vigente** | Ramas institucionales por promoción, utilizadas en proyectos existentes: `ONP_DESA`, `ONP_PQA`, `ONP_QA`, `master` o equivalentes (`dev`, `qa`, `prd`, `main`). |
| **Modelo objetivo** | GitLab Flow simplificado para proyectos nuevos, modernizaciones relevantes y componentes implementados bajo el nuevo marco de arquitectura. El modelo se disciplina con principios TBD: ramas cortas, integración frecuente y `main` siempre integrable. |

La ONP reconoce que actualmente varios proyectos usan GitLab principalmente como repositorio, con ramas asociadas a ambientes o estados de promoción. Este modelo se mantendrá para proyectos existentes mientras no exista una intervención mayor o migración planificada.

A partir de la vigencia del presente lineamiento, todo proyecto nuevo deberá adoptar **GitLab Flow simplificado** como modelo de versionamiento por defecto. El uso de ramas permanentes por ambiente en proyectos nuevos requiere justificación mediante ADR.

### 4.1 Punto de quiebre documental

Este lineamiento establece el siguiente punto de quiebre:

```text
Proyectos existentes
   → pueden mantener temporalmente el modelo vigente, aplicando controles mínimos.

Proyectos nuevos
   → deben iniciar con GitLab Flow simplificado.

Modernizaciones relevantes
   → deben evaluar migración al modelo objetivo.

Excepciones
   → requieren ADR.
```

### 4.2 Importante sobre CI/CD

La adopción inicial de GitLab Flow simplificado en ONP será **controlada y progresiva**. No se exige CI/CD pleno en esta fase.

Mientras `LIN-CICD-001` no esté implementado, todo Merge Request hacia la rama principal deberá contar con:

- evidencia manual de compilación o build local;
- evidencia de pruebas aplicables;
- revisión técnica;
- trazabilidad con requerimiento, incidencia, tarea o ADR;
- revisión especializada si afecta seguridad, BD, API, K8s u observabilidad.

La adopción de GitLab Flow simplificado pleno, con validaciones automáticas obligatorias sobre cada Merge Request, quedará asociada a la implementación de `LIN-CICD-001`.

### 4.2.1 Guía rápida de decisión para equipos

```text
¿Es un proyecto nuevo creado bajo el nuevo marco ONP?
  Sí  -> usar GitLab Flow simplificado.
  No  -> seguir evaluando.

¿Es un sistema existente que ya opera con ONP_DESA / ONP_PQA / ONP_QA / master?
  Sí  -> mantener temporalmente el modelo vigente, salvo ADR de migración.
  No  -> seguir evaluando.

¿Es una modernización relevante o un componente nuevo dentro de un sistema existente?
  Sí  -> evaluar migración al modelo objetivo y documentar la decisión.
```

### 4.3 Comparación visual de modelos

#### Modelo vigente — GitLab Flow con ramas por ambiente (hoy)

```
                        ┌─────────────────────────────────────────────────────────────────────────┐
                        │  GITLAB FLOW — RAMAS POR AMBIENTE (modelo vigente ONP)                  │
                        └─────────────────────────────────────────────────────────────────────────┘

  feature/REQ-001 ──┐
  feature/REQ-002 ──┼──MR──► ONP_DESA ──MR──► ONP_PQA ──MR──► ONP_QA ──MR──► master ──tag──► v1.2.0
  bugfix/INC-001  ──┘        │               │               │               │
  hotfix/PROD-001 ────────────────────────────────────────────────────────MR──┘

                             ▼               ▼               ▼               ▼
                        [amb. DEV]      [amb. PQA]      [amb. QA]        [PROD]

  Ramas permanentes:  ONP_DESA · ONP_PQA · ONP_QA · master
  Cada ambiente tiene su propia rama. El pase entre ambientes es una promoción de rama a rama.
  La configuración diferente por ambiente puede estar embebida en cada rama (riesgo: divergencia).
```

#### Modelo objetivo — GitLab Flow simplificado (proyectos nuevos)

```
                        ┌─────────────────────────────────────────────────────────────────────────┐
                        │  GITLAB FLOW SIMPLIFICADO — RAMA ÚNICA + AMBIENTES DE DESPLIEGUE        │
                        └─────────────────────────────────────────────────────────────────────────┘

  feature/REQ-001 ──┐
  feature/REQ-002 ──┼──MR──► main ─────────────────────────────────────────────── tag ──► v1.2.0
  bugfix/INC-001  ──┘        (trunk)                                                       │
  (ramas cortas:                                                                            │ mismo commit
   1 a 5 días)                                                                             │ misma imagen
                                                                                            ▼
                                                                    despliegue del mismo artefacto
                                                                    ───────────────────────────────►
                                                                    ONP_DESA → ONP_PQA → ONP_QA → PROD
                                                                    [ambientes de deploy, no ramas Git]

  Única rama permanente:  main
  Los ambientes ya no son ramas: son destinos de despliegue del mismo commit/imagen.
  La configuración por ambiente se gestiona externamente (ConfigMap, variables de entorno).
```

#### Diferencia clave

| Aspecto | Modelo vigente (GitLab Flow — ramas por ambiente) | Modelo objetivo (GitLab Flow simplificado) |
|---|---|---|
| Ramas permanentes | `ONP_DESA`, `ONP_PQA`, `ONP_QA`, `master` | Solo `main` |
| Pase entre ambientes | Merge Request de rama a rama | Despliegue del mismo artefacto |
| Configuración por ambiente | Puede estar en cada rama | Siempre externa (ConfigMap, env vars) |
| Duración de ramas de trabajo | Variable (puede ser larga) | Corta: 1 a 5 días |
| Ideal para | Proyectos con ciclos de release definidos | Entrega frecuente y continua |
| Estado en ONP | Proyectos existentes | Proyectos nuevos (modelo por defecto) |

---

## 5. Modelo vigente: ramas institucionales por promoción

### 5.1 Descripción

El modelo vigente corresponde a una variante de GitLab Flow basada en ramas de promoción o ramas por ambiente.

Ejemplos de ramas usadas actualmente o equivalentes:

```text
ONP_DESA / dev
ONP_PQA
ONP_QA / qa
master / main / prd
```

Este modelo puede mantenerse en proyectos existentes, pero debe ser controlado.

### 5.2 Ramas institucionales

| Rama | Uso | Regla |
|---|---|---|
| `ONP_DESA` / `dev` | Integración de desarrollo | Recibe cambios desde ramas de trabajo mediante Merge Request |
| `ONP_PQA` | Precalidad / estabilización | Recibe promociones desde `ONP_DESA` para validación previa a QA formal |
| `ONP_QA` / `qa` | Calidad formal | Recibe versión candidata para pruebas QA |
| `master` / `main` / `prd` | Producción | Solo recibe cambios aprobados desde QA o `hotfix/*` autorizado |

### 5.3 Flujo de promoción vigente

Flujo estándar:

```text
feature/* | bugfix/* | refactor/* | test/* | docs/*
        ↓ MR
ONP_DESA / dev
        ↓ MR / promoción
ONP_PQA
        ↓ MR / promoción
ONP_QA / qa
        ↓ MR aprobado
master / main / prd
        ↓ tag / release
vX.Y.Z
```

### 5.4 Reglas para el modelo vigente

| Regla | Estado |
|---|---|
| `master`, `main` o `prd` debe ser rama protegida | Obligatorio |
| `ONP_QA` o `qa` debe ser rama protegida | Obligatorio |
| `ONP_PQA` debe ser rama protegida | Obligatorio |
| `ONP_DESA` o `dev` debe ser rama protegida | Recomendado / obligatorio para proyectos críticos |
| No se permiten commits directos a ramas protegidas | Obligatorio |
| Todo cambio debe ingresar por Merge Request | Obligatorio |
| Las ramas no deben mantener configuración funcional distinta por ambiente | Obligatorio |
| Las diferencias entre ambientes se gestionan por configuración externa | Obligatorio |
| Todo pase a producción debe tener tag o release identificable | Obligatorio |

### 5.5 Responsable de configuración

La configuración de ramas protegidas en GitLab es responsabilidad de Plataforma/Infraestructura, como parte del proceso de habilitación del repositorio. El equipo de desarrollo solicita la protección al crear el proyecto; Plataforma la aplica y valida.

---

## 6. Modelo objetivo: GitLab Flow simplificado

### 6.1 Definición

**GitLab Flow simplificado** es el modelo operativo adoptado por la ONP para proyectos nuevos. Consiste en una única rama permanente de integración (`main`) y ramas de trabajo cortas y temporales que se integran frecuentemente mediante Merge Request. El mismo artefacto o imagen generado desde `main` se promueve a través de los ambientes de despliegue.

El modelo se disciplina con principios de **Trunk-Based Development (TBD)**:
- ramas de trabajo de corta duración (1 a 5 días);
- integración frecuente a `main`;
- `main` siempre en estado integrable;
- feature flags o partición incremental para trabajo incompleto.

Flujo operativo:

```text
feature corta
   ↓ MR
main
   ↓ tag / release
vX.Y.Z
   ↓ promoción del mismo artefacto
ONP_DESA → ONP_PQA → ONP_QA → PROD
```

### 6.2 Rama principal

En el modelo objetivo, la rama principal será:

```text
main
```

Reglas:

- `main` es la única rama permanente de integración.
- `main` debe mantenerse siempre integrable.
- Todo cambio entra a `main` mediante Merge Request.
- No se permiten commits directos a `main`.
- Las ramas de trabajo deben ser cortas y temporales.
- Los releases se identifican mediante tags semánticos.
- Las promociones a ambientes se realizan usando el mismo commit, tag o imagen.

### 6.3 Ramas de trabajo en GitLab Flow simplificado

Ramas permitidas:

| Tipo de rama | Uso | Ejemplo |
|---|---|---|
| `feature/*` | Nueva funcionalidad | `feature/REQ-1234-registro-notificacion` |
| `bugfix/*` | Corrección no productiva | `bugfix/INC-4567-error-validacion` |
| `hotfix/*` | Corrección urgente productiva | `hotfix/PROD-0098-correccion-token` |
| `refactor/*` | Mejora técnica sin cambio funcional | `refactor/ARQ-001-separar-adapter-saa` |
| `test/*` | Incorporación o ajuste de pruebas | `test/REQ-1234-pruebas-contrato` |
| `docs/*` | Documentación | `docs/API-002-actualizar-openapi` |
| `chore/*` | Tareas técnicas menores | `chore/BUILD-003-actualizar-dependencias` |

### 6.4 Duración de ramas

Las ramas de trabajo deben ser de corta duración.

| Tipo de rama | Duración sugerida |
|---|---:|
| `feature/*` | 1 a 5 días hábiles |
| `bugfix/*` | 1 a 3 días hábiles |
| `hotfix/*` | Lo mínimo necesario |
| `docs/*` | 1 a 3 días hábiles |
| `refactor/*` | 1 a 5 días hábiles, según alcance |

Si una funcionalidad requiere más tiempo, debe dividirse en cambios pequeños, integrables y seguros.

### 6.5 Feature flags y trabajo incompleto

En GitLab Flow simplificado, no se deben mantener ramas largas para ocultar trabajo incompleto.

Cuando un cambio no esté listo para habilitarse funcionalmente, debe usarse alguna de las siguientes estrategias:

- feature flag;
- configuración externa;
- endpoint no expuesto;
- pantalla no visible por permisos;
- código integrado pero no activado;
- división incremental del alcance.

Regla:

> No se debe romper `main` ni bloquear releases por funcionalidades incompletas.

### 6.6 GitLab Flow simplificado sin CI/CD pleno

Mientras no exista CI/CD activo, GitLab Flow simplificado en ONP será preparatorio y controlado.

Todo MR hacia `main` deberá incluir:

| Evidencia | Obligatorio |
|---|---|
| Build local exitoso | Sí |
| Pruebas unitarias aplicables | Sí |
| Pruebas de integración si aplica | Sí |
| Evidencia de revisión técnica | Sí |
| Impacto en API/BD/seguridad/K8s declarado | Sí |
| OpenAPI actualizado si aplica | Sí |
| Scripts BD versionados si aplica | Sí |
| ADR si aplica | Sí |

### 6.7 GitLab Flow simplificado pleno

GitLab Flow simplificado pleno se adoptará cuando exista CI/CD mínimo aprobado, de acuerdo con `LIN-CICD-001`.

Validaciones esperadas en esa etapa:

- build automático;
- pruebas unitarias;
- cobertura;
- pruebas de integración aplicables;
- validación OpenAPI;
- SAST/SCA;
- secret scanning;
- escaneo de imagen;
- publicación de artefacto o imagen;
- evidencias automatizadas en el Merge Request.

---

## 7. Criterios para elegir modelo por tipo de proyecto

| Tipo de proyecto | Modelo recomendado | Observación |
|---|---|---|
| Proyecto legacy existente sin intervención mayor | Modelo vigente controlado | Mantener ramas actuales con controles mínimos |
| Proyecto existente con cambios menores | Modelo vigente controlado | Evaluar mejoras graduales |
| Proyecto existente con modernización relevante | Evaluar GitLab Flow simplificado | Requiere análisis y posible ADR |
| Proyecto nuevo | GitLab Flow simplificado | Modelo por defecto |
| Nuevo componente bajo nuevo marco de arquitectura | GitLab Flow simplificado | Modelo por defecto |
| Proyecto nuevo que requiera ramas por ambiente | Excepción | Requiere ADR |
| Proyecto con CI/CD maduro | GitLab Flow simplificado pleno | Objetivo futuro |
| Sistema crítico con restricciones operativas | Evaluar caso | ADR obligatorio si no adopta GitLab Flow simplificado |

### 7.1 Decisión por defecto

```text
Proyecto nuevo → GitLab Flow simplificado.
Proyecto existente → modelo vigente controlado, con camino de migración.
Excepción → ADR.
```

---

## 8. Modelo de repositorios GitLab

### 8.1 Proyecto GitLab por componente desplegable

La ONP adopta como criterio general un proyecto GitLab por componente desplegable independiente.

Se considera componente desplegable a todo artefacto que tenga ciclo propio de construcción, versionamiento, despliegue u operación.

Ejemplos:

| Componente | Proyecto GitLab sugerido |
|---|---|
| Backend Java | `notificaciones-backend` |
| Frontend ciudadano | `notificaciones-front-ciudadano` |
| Frontend consulta | `notificaciones-front-consulta` |
| Worker | `notificaciones-worker` |
| Adapter SAA | `notificaciones-adapter-saa` |
| Job batch | `notificaciones-job-reintento` |
| Librería compartida | `onp-lib-seguridad` |
| Manifiestos Kubernetes | `notificaciones-k8s` o carpeta versionada en cada proyecto, según decisión |

### 8.2 Cuándo separar repositorios

Debe crearse un repositorio separado cuando el componente:

- se construye de forma independiente;
- se despliega de forma independiente;
- tiene versionamiento propio;
- tiene responsables o ciclo de vida distinto;
- genera una imagen de contenedor distinta;
- puede ser consumido por varios componentes;
- requiere permisos o revisores diferenciados.

### 8.3 Cuándo no separar repositorios

No se debe crear un repositorio separado por cada módulo funcional interno si dichos módulos se construyen y despliegan como una sola aplicación.

Ejemplo:

```text
Incorrecto:
notificaciones-login
notificaciones-bandeja
notificaciones-consulta
notificaciones-reportes

Correcto si todo es una sola SPA:
notificaciones-front-backoffice
```

### 8.4 Monorepo

El uso de monorepo está permitido solo con justificación mediante ADR cuando:

- los componentes tienen alta dependencia de cambios coordinados;
- existe una estructura clara de carpetas;
- se puede controlar revisión por rutas;
- se puede versionar y desplegar cada componente sin ambigüedad.

Regla:

> La práctica recomendada inicial en ONP es repositorio por componente desplegable. El monorepo requiere ADR.

---

## 9. Nomenclatura de grupos, subgrupos y proyectos

### 9.1 Convención general

Los nombres de proyectos GitLab deben escribirse en minúsculas, sin espacios, usando guion medio como separador.

Formato:

```text
<sistema>-<tipo-componente>-<canal-o-funcion>
```

Ejemplos:

```text
notificaciones-backend
notificaciones-front-ciudadano
notificaciones-front-consulta
notificaciones-adapter-saa
notificaciones-worker
past-backend
past-front-ciudadano
past-front-backoffice
```

Evitar:

```text
Notificaciones_front_ciudadano
notificacionesFrontConsulta
notificaciones front consulta
BACKEND_NOTIFICACIONES
```

### 9.2 Estructura de grupos

Estructura recomendada:

```text
APLICACIONES / <SISTEMA> / <proyecto-componente>
```

Ejemplo:

```text
APLICACIONES / NOTIFICACIONES / notificaciones-backend
APLICACIONES / NOTIFICACIONES / notificaciones-front-ciudadano
APLICACIONES / NOTIFICACIONES / notificaciones-front-consulta
```

### 9.3 Nombres heredados

Los proyectos existentes con nombres no alineados no requieren renombrado inmediato si ello afecta integraciones, permisos, rutas o despliegues.

Sin embargo, todo proyecto nuevo debe seguir la convención definida. Los proyectos existentes deberán alinearse cuando exista una intervención mayor, migración o reorganización del repositorio.

> **Nota sobre subgrupos existentes:** La estructura actual de GitLab ONP puede usar nombres en mayúsculas para subgrupos. Estos no requieren ajuste inmediato. La convención de minúsculas con guion medio aplica a proyectos nuevos y subgrupos nuevos creados a partir de la vigencia de este lineamiento.

---

## 10. Ramas de trabajo

### 10.1 Regla general

El desarrollo diario no debe realizarse directamente sobre ramas institucionales, ramas de ambiente/promoción ni sobre `main`.

Todo cambio debe iniciar en una rama de trabajo y entrar mediante Merge Request.

### 10.2 Tipos de ramas

| Tipo de rama | Uso | Ejemplo |
|---|---|---|
| `feature/*` | Nueva funcionalidad | `feature/REQ-1234-registro-notificacion` |
| `bugfix/*` | Corrección no productiva | `bugfix/INC-4567-error-validacion` |
| `hotfix/*` | Corrección urgente de producción | `hotfix/PROD-0098-correccion-token` |
| `refactor/*` | Mejora interna sin cambio funcional | `refactor/ARQ-001-separar-adapter-saa` |
| `test/*` | Incorporación o ajuste de pruebas | `test/REQ-1234-pruebas-caracterizacion` |
| `docs/*` | Documentación | `docs/API-002-actualizar-openapi` |
| `chore/*` | Tareas técnicas menores | `chore/BUILD-003-actualizar-dependencias` |

### 10.3 Convención de nombre

Formato:

```text
<tipo>/<identificador>-<descripcion-corta>
```

Ejemplos:

```text
feature/REQ-1234-registrar-notificacion
bugfix/INC-4567-corregir-validacion-token
hotfix/PROD-0098-corregir-error-autenticacion
refactor/ARQ-001-extraer-adapter-saa
test/REQ-1234-agregar-pruebas-contrato
docs/API-002-actualizar-openapi
```

### 10.4 Reglas para ramas de trabajo

- Deben partir de la rama base correspondiente:
  - `ONP_DESA` o `dev` en el modelo vigente;
  - `main` en el modelo objetivo (GitLab Flow simplificado).
- Deben tener un propósito claro y acotado.
- Deben eliminarse después del merge, salvo razón justificada.
- No deben usarse como ramas permanentes de ambiente.
- No deben contener secretos ni archivos temporales.

---

## 11. Merge Requests obligatorios

### 11.1 Regla principal

Todo cambio debe ingresar mediante Merge Request. No se permiten commits directos a ramas protegidas ni a `main`.

### 11.2 Contenido mínimo del Merge Request

Todo MR debe incluir:

- requerimiento, incidencia, tarea o ADR asociado;
- descripción del cambio;
- tipo de cambio;
- impacto funcional;
- impacto técnico;
- evidencia de pruebas;
- impacto en base de datos, si aplica;
- impacto en APIs/OpenAPI, si aplica;
- impacto en seguridad, si aplica;
- impacto en configuración o despliegue, si aplica;
- instrucciones de despliegue o plan de reversa, si aplica;
- revisor asignado.

### 11.3 Tipos de cambio

| Tipo | Descripción |
|---|---|
| Funcional | Cambia o agrega comportamiento visible para usuario |
| Técnico | Refactor, ajuste interno o mejora técnica |
| API | Cambia contrato REST, OpenAPI o payload |
| BD | Cambia tablas, índices, vistas, procedures, packages o datos de configuración |
| Seguridad | Cambia autenticación, autorización, tokens, roles, permisos o manejo de secretos |
| Observabilidad | Cambia logs, trazas, métricas o health checks |
| Frontend | Cambia UI, rutas, guards, servicios Angular |
| K8s | Cambia Dockerfile, manifiestos, ConfigMap, Secret, probes o recursos |
| Documentación | Cambia documentación técnica o arquitectura |

### 11.4 MR simplificado

Para cambios de documentación, tareas técnicas menores o ajustes internos sin impacto funcional, se permite un MR con formato simplificado: tipo de cambio, referencia y descripción breve.

Para cualquier cambio funcional, de seguridad, BD, API o K8s, el template completo es obligatorio.

### 11.5 Plantilla mínima de Merge Request

```markdown
## Descripción del cambio
[Resumen breve del cambio]

## Referencia
- Requerimiento / Incidencia / Tarea:
- ADR asociado, si aplica:

## Tipo de cambio
- [ ] Funcional
- [ ] Técnico / refactor
- [ ] API / OpenAPI
- [ ] Base de datos
- [ ] Seguridad
- [ ] Observabilidad
- [ ] Frontend
- [ ] K8s / despliegue
- [ ] Documentación

## Evidencia de pruebas
- [ ] Pruebas unitarias ejecutadas
- [ ] Pruebas de integración ejecutadas
- [ ] Pruebas E2E ejecutadas, si aplica
- [ ] Pruebas de contrato ejecutadas, si aplica
- [ ] Pruebas de caracterización ejecutadas, si aplica
- Resultado / enlace / adjunto:

## Impacto en API
- [ ] No aplica
- [ ] OpenAPI actualizado
- [ ] Cambio compatible
- [ ] Cambio incompatible — requiere ADR o versión mayor

## Impacto en base de datos
- [ ] No aplica
- [ ] Script de migración incluido
- [ ] Script de reversa o compensación incluido
- [ ] Prueba en ambiente controlado

## Impacto en seguridad
- [ ] No aplica
- [ ] Requiere revisión de Seguridad
- [ ] Maneja datos personales
- [ ] Cambia autenticación/autorización
- [ ] Cambia secretos o configuración sensible

## Impacto en despliegue
- [ ] No aplica
- [ ] Cambia Dockerfile
- [ ] Cambia manifiestos K8s
- [ ] Cambia variables de entorno
- [ ] Cambia recursos/probes

## Plan de reversa
[Cómo revertir el cambio si falla, distinguiendo despliegue y BD cuando aplique]

## Revisores
- Revisor técnico:
- Arquitectura, si aplica:
- Seguridad, si aplica:
- Plataforma, si aplica:
```

---

## 12. Revisión de código

> **Dueño del proceso de revisión.** Esta sección es la fuente autoritativa de **cómo se revisa un cambio en ONP**, cualquiera que sea el lenguaje: quién puede aprobar, cuántos revisores hacen falta, qué revisor especializado exige cada tipo de cambio y qué tamaño máximo admite un MR. Los estándares por tecnología **no redefinen estas reglas**: añaden las verificaciones propias de su stack (por ejemplo, `LIN-DEV-JAVA-001 §16.2` lista las condiciones específicas de Java — Checkstyle, JaCoCo, tabla de antipatrones).

### 12.1 Reglas mínimas

| Regla | Estado |
|---|---|
| Ningún desarrollador aprueba su propio cambio | Obligatorio |
| Todo MR debe tener al menos una revisión técnica | Obligatorio |
| Cambios críticos requieren revisión especializada | Obligatorio |
| La revisión debe verificar cumplimiento de lineamientos aplicables | Obligatorio |
| No se aprueban MRs sin descripción ni evidencia mínima | Obligatorio |
| Un MR no supera **400 líneas** de código productivo (excluidos pruebas y configuración) | Obligatorio |

> **Sobre el límite de 400 líneas.** Un MR mayor debe dividirse: los MR grandes degradan la calidad de la revisión y aumentan el riesgo de integración. La regla aplica a **todo tipo de cambio** —Java, Angular, scripts SQL, manifiestos K8s, Terraform—, no solo a código Java. Si el cambio es indivisible por naturaleza (migración masiva generada, renombrado global), se declara en la descripción del MR y se acuerda revisor adicional.

### 12.2 Revisión por tipo de cambio

| Tipo de cambio | Revisor mínimo |
|---|---|
| Código Java / Spring Boot | Líder técnico o revisor asignado |
| Código Angular | Líder técnico frontend o revisor asignado |
| API / OpenAPI | Desarrollo + Arquitectura si cambia contrato |
| Seguridad / autenticación / autorización | Desarrollo + Seguridad Digital |
| Base de datos / PL-SQL | Desarrollo + responsable BD/datos |
| Procedure legacy crítico | Desarrollo + Arquitectura + responsable técnico/funcional |
| Contenedores / K8s | Desarrollo + Plataforma o Arquitectura |
| Observabilidad | Desarrollo + Arquitectura / Plataforma |
| ADR / decisión técnica | Arquitectura |

### 12.3 Criterios de revisión

El revisor debe verificar, según aplique:

- claridad del cambio;
- ausencia de secretos;
- cumplimiento de estilo y estructura del proyecto;
- pruebas suficientes;
- impacto en contrato API;
- impacto en BD;
- impacto en seguridad;
- compatibilidad hacia atrás;
- actualización de documentación;
- scripts de reversa o compensación, cuando aplique;
- actualización de manifiestos o variables;
- necesidad de ADR.

---

## 13. Evidencias mínimas por tipo de cambio

Hasta la implementación de `LIN-CICD-001`, las evidencias mínimas se adjuntan, enlazan o describen en el Merge Request.

> **Qué se evidencia y qué se exige son cosas distintas.** Esta tabla define *qué prueba debe acompañar al MR*; los **umbrales** de cobertura y los tipos de prueba obligatorios los fija `TEST-R-001` (LIN-TEST-001 §5.1), que es el documento dueño. Ninguna fila de esta tabla releva de esos umbrales.

| Tipo de cambio | Evidencia mínima |
|---|---|
| Backend Java | Resultado de pruebas unitarias/integración y **cobertura conforme a los umbrales de `TEST-R-001` (LIN-TEST-001 §5.1)** (no es opcional) |
| Frontend Angular | Pruebas unitarias con la cobertura exigida en `TEST-R-001` (LIN-TEST-001 §5.1), build local, E2E si aplica |
| API REST | `openapi.yml` actualizado y evidencia de compatibilidad |
| Contrato API crítico | Validación OpenAPI o prueba de contrato |
| Base de datos | Script versionado, estrategia de reversa o compensación si aplica, evidencia de prueba |
| PL/SQL legacy | Pruebas de caracterización antes y después |
| Seguridad | Evidencia de revisión, escaneo o control compensatorio |
| Observabilidad | Evidencia de logs/trazas/métricas si aplica |
| K8s | Manifiestos actualizados, tag de imagen, variables/secretos documentados |
| Documentación | Archivo actualizado y revisión correspondiente |

---

## 14. Versionamiento semántico

### 14.1 Regla general

Todo release aprobado debe identificarse con una versión semántica.

Formato:

```text
MAJOR.MINOR.PATCH
```

Ejemplos:

```text
1.0.0
1.1.0
1.1.1
2.0.0
```

### 14.2 Criterio de incremento

| Tipo de cambio | Incremento |
|---|---|
| Cambio incompatible con consumidores existentes | `MAJOR` |
| Nueva funcionalidad compatible | `MINOR` |
| Corrección compatible | `PATCH` |
| Cambio documental sin impacto de ejecución | No necesariamente incrementa release |
| Cambio en infraestructura de despliegue | Según impacto |
| Cambio en contrato API incompatible | `MAJOR` o nueva versión de API |

### 14.3 Pre-releases

Se permiten versiones candidatas:

```text
1.2.0-rc.1
1.2.0-rc.2
```

Y versiones de desarrollo:

```text
1.2.0-dev.45
```

---

## 15. Tags y releases

### 15.1 Regla general

Todo pase a producción debe tener un tag Git asociado.

Formato obligatorio:

```text
v<MAJOR>.<MINOR>.<PATCH>[-<pre-release>]
```

Ejemplos:

```text
v1.0.0
v1.1.0
v1.1.1
v1.2.0-rc.1      ← candidata (sección 14.3), no apta para producción
```

> El sufijo de pre-release solo se admite en tags que **no** corresponden a un pase a producción. Todo tag productivo usa la forma `v<MAJOR>.<MINOR>.<PATCH>` sin sufijo.

### 15.2 Release notes mínimas

Todo release debe documentar:

- versión;
- fecha;
- componente;
- MRs incluidos;
- requerimientos/incidencias incluidos;
- scripts BD asociados;
- cambios API;
- cambios de seguridad;
- cambios de configuración;
- riesgos conocidos;
- plan de reversa previsto;
- responsable de release.

### 15.3 Relación con imágenes de contenedor

Cuando el componente genera imagen de contenedor, el tag de imagen debe corresponder a la versión del release. Conforme a `K8S-R-001` (LIN-K8S-001 §6.3), el tag debe ser **explícito e inmutable**: `latest` y cualquier etiqueta móvil están prohibidos en QA y Producción. Si por restricción técnica no puede usarse la versión del release, se admite un identificador inmutable y trazable al commit (por ejemplo, el SHA corto), nunca una etiqueta reutilizable.

Ejemplo:

```text
Git tag:
v1.2.0

Imagen:
registry.gitlab.onp.gob.pe/aplicaciones/notificaciones/notificaciones-backend:1.2.0
```

---

## 16. Control de cambios de base de datos

### 16.1 Regla general

Todo cambio de base de datos debe estar versionado en el repositorio correspondiente. No se aceptan cambios manuales no documentados en QA o Producción.

### 16.1.1 Modelo aplicable según tipo de sistema

ONP opera con dos modelos de scripts según el tipo de sistema. Ambos son válidos; el criterio de elección es el tipo de base de datos y su complejidad:

| Criterio | Modelo manual interactivo | Modelo versionado automatizable |
|---|---|---|
| **Cuándo aplica** | Sistemas legacy con PL/SQL complejo, packages o procedures críticos | Sistemas nuevos con DDL simple (tablas, índices, vistas) y Spring Boot |
| **Nomenclatura** | `PP_<ORIGEN>_<NUMERO>_<ESQUEMA>_<TIPO>_<NN>.SQL` | `V<VERSION>_<NNN>__<descripcion>.sql` |
| **Dónde se define** | `LIN-BD-ORA-001 §8.1` | `LIN-BD-ORA-001 §8.4` |
| **Ejecución** | Manual — DBA ejecuta en SQL*Plus con revisión previa y aplica la estrategia de reversa definida para el cambio | Manual por ahora; preparado para Flyway/Liquibase en el marco de LIN-CICD-001 |
| **Referencia** | **LIN-BD-ORA-001 sección 8** — proceso completo con PROMPT, SPOOL y validación | Esta sección |
| **PL/SQL, packages, procedures** | Obligatorio modelo manual | No aplica |

> Si un sistema nuevo incorpora PL/SQL legacy, los scripts de PL/SQL siguen el modelo manual (LIN-BD-ORA-001 sección 8) y los scripts DDL de tablas nuevas siguen el modelo versionado de esta sección. En caso de duda, documentar la decisión en ADR.

### 16.2 Estructura sugerida

```text
db/
├── migration/
│   ├── V1.2.0_001__crear_tabla_notificacion.sql
│   ├── V1.2.0_002__agregar_indice_estado.sql
│   └── V1.2.1_001__ajustar_package_validacion.sql
└── reverse/
    ├── R1.2.0_001__drop_tabla_notificacion.sql
    └── R1.2.0_002__drop_indice_estado.sql
```

### 16.3 Reglas para scripts

| Regla | Estado |
|---|---|
| Todo script debe tener versión | Obligatorio |
| Todo script debe tener descripción clara | Obligatorio |
| Todo script debe indicar orden de ejecución | Obligatorio |
| Toda migración debe tener estrategia de reversa cuando aplique | Obligatorio |
| No se envían scripts por correo como fuente oficial | Obligatorio |
| No se modifican scripts ya ejecutados en QA/PROD | Obligatorio |
| Las correcciones se hacen con un nuevo script | Obligatorio |

### 16.4 PL/SQL legacy

Cuando el cambio afecte procedures, packages o functions con lógica de negocio crítica:

- debe cumplirse `LIN-BD-ORA-001`;
- debe cumplirse `LIN-TEST-001` sobre pruebas de caracterización;
- debe adjuntarse evidencia de prueba antes y después del cambio;
- debe documentarse el comportamiento modificado.

---

## 17. Cambios en APIs, contratos y OpenAPI

### 17.1 Regla general

Todo cambio en una API REST debe actualizar el contrato OpenAPI correspondiente.

### 17.2 Cambios compatibles

Ejemplos:

- agregar campo opcional;
- agregar endpoint nuevo;
- ampliar descripción;
- agregar código de respuesta documentado sin romper consumidores.

### 17.3 Cambios incompatibles

Ejemplos:

- eliminar campo usado por consumidores;
- cambiar tipo de dato;
- cambiar estructura de respuesta;
- cambiar semántica de error;
- cambiar ruta o método HTTP;
- cambiar reglas de autenticación/autorización.

Los cambios incompatibles requieren:

- ADR;
- evaluación de versionamiento de API;
- comunicación a consumidores;
- actualización de OpenAPI;
- pruebas de contrato cuando aplique.

---

## 18. Cambios en contenedores y manifiestos Kubernetes

Todo cambio que afecte Dockerfile, imagen, manifiestos Kubernetes, ConfigMap, Secret, probes, recursos o rutas de exposición debe cumplir `LIN-K8S-001`.

Evidencia mínima:

- Dockerfile actualizado, si aplica;
- manifiestos actualizados;
- tag de imagen explícito;
- variables de entorno documentadas;
- secretos requeridos documentados sin valores;
- impacto en recursos/probes;
- plan de reversa previsto.

---

## 19. Relación con ADRs

Un ADR es obligatorio cuando el cambio:

- introduce una decisión arquitectónica relevante;
- cambia el estilo arquitectónico;
- modifica el contrato API de forma incompatible;
- introduce o elimina un componente desplegable;
- modifica integración con SAA, WSO2 o sistemas críticos;
- requiere excepción a un lineamiento;
- afecta seguridad de forma significativa;
- cambia persistencia o lógica PL/SQL crítica;
- introduce patrón arquitectónico relevante;
- propone usar ramas permanentes por ambiente en un proyecto nuevo;
- propone no adoptar GitLab Flow simplificado en un proyecto nuevo.

Los ADRs deben estar versionados en el repositorio del proyecto o en el repositorio documental definido por Arquitectura.

Estructura sugerida:

```text
docs/
└── adr/
    ├── ADR-0001-uso-bff-ciudadano.md
    ├── ADR-0002-integracion-saa-token.md
    └── ADR-0003-excepcion-modelo-ramas.md
```

---

## 20. Relación con CI/CD

CI/CD se encuentra mapeado como capacidad objetivo, con su modelo inicial en borrador.

Hasta la implementación de `LIN-CICD-001`:

- las evidencias de pruebas se adjuntan o referencian en el MR;
- la revisión técnica valida manualmente los criterios mínimos;
- los tags y releases se crean según este lineamiento;
- los artefactos e imágenes deben mantener trazabilidad;
- los scripts de BD deben estar versionados;
- los cambios críticos requieren revisión especializada.

Este lineamiento prepara el camino para que, posteriormente, `LIN-CICD-001` automatice:

- ejecución de pruebas;
- análisis de cobertura;
- análisis SAST/SCA;
- escaneo de secretos;
- escaneo de imágenes;
- validación OpenAPI;
- publicación de artefactos;
- construcción y promoción de imágenes;
- despliegue por ambiente.

### 20.1 Relación entre GitLab Flow simplificado y CI/CD

GitLab Flow simplificado funciona de forma óptima con CI/CD. Sin embargo, la ONP adoptará inicialmente un enfoque preparatorio y progresivo, sustentado en Merge Requests, revisión técnica y evidencias manuales.

El modelo de madurez completo — fases, gates, herramientas, pipelines por tipo de componente y criterios de bloqueo — está definido en `LIN-CICD-001 sección 4`.

Resumen orientativo:

| Fase | Enfoque |
|---:|---|
| 0 | Control manual documentado — cubierto por este lineamiento |
| 1–2 | CI básico + calidad y seguridad — ver `LIN-CICD-001` [sección 4.2](#42-importante-sobre-cicd)–4.3 |
| 3–4 | Artefactos, imágenes y entrega controlada — ver `LIN-CICD-001` sección 4.4–4.5 |
| 5–7 | Seguridad dinámica, performance, CD maduro y operación avanzada — ver `LIN-CICD-001` sección 4.6–4.8 |

---

## 21. Uso de capacidades GitLab Ultimate

Aunque CI/CD no esté priorizado, GitLab Ultimate debe aprovecharse progresivamente para control de cambios.

### 21.1 Capacidades a habilitar desde esta fase

Las tres primeras capacidades son **obligatorias**, no recomendadas: son el mecanismo que hace exigibles los principios P2 (no hay cambios directos en ramas protegidas), P3 (revisión obligatoria) y la prohibición de autoaprobación de la [sección 12.1](#121-reglas-mínimas). Sin ellas esas reglas dependen de la disciplina individual y no del repositorio. Las demás son recomendadas y se adoptan progresivamente.

| Capacidad | Uso | Carácter |
|---|---|---|
| Protected branches | Proteger `main`, `master`, `ONP_QA`, `ONP_PQA`, `ONP_DESA` o equivalentes | **Obligatorio** |
| Merge Requests | Canal obligatorio de integración | **Obligatorio** |
| Approval rules | Reglas de aprobación por tipo de cambio; deben configurarse de modo que el autor no pueda aprobar su propio MR | **Obligatorio** |
| CODEOWNERS | Revisores por carpeta o componente | Recomendado |
| Issues | Trazabilidad de requerimientos/incidencias | Recomendado |
| Labels | Clasificación de cambios | Recomendado |
| Milestones | Agrupación por release o entrega | Recomendado |
| Releases | Registro de versiones liberadas | Recomendado |
| Tags | Identificación de versión | Recomendado |
| Wiki / Markdown docs | Documentación técnica auxiliar | Recomendado |
| Project templates | Plantillas institucionales para nuevos proyectos | Recomendado |

### 21.2 Uso posterior

| Capacidad | Fase posterior |
|---|---|
| GitLab CI/CD | LIN-CICD-001 |
| Security scanning | LIN-CICD-001 / LIN-SEC-APP-001 |
| Container scanning | LIN-K8S-001 / LIN-CICD-001 |
| Dependency scanning | LIN-SEC-APP-001 / LIN-CICD-001 |
| Environments | LIN-CICD-001 |
| Deployments | LIN-CICD-001 |

---

## 22. Checklist de conformidad

### 22.1 Repositorio

```text
[ ] Proyecto GitLab creado por componente desplegable
[ ] Nombre del proyecto en minúsculas y con guion medio para proyectos nuevos
[ ] README mínimo actualizado
[ ] Estructura de carpetas clara
[ ] No existen secretos versionados
[ ] Para proyecto nuevo, se definió modelo GitLab Flow simplificado
[ ] Si el proyecto nuevo no usa GitLab Flow simplificado, existe ADR
```

### 22.2 Ramas

```text
[ ] Modelo de ramas identificado: vigente o GitLab Flow simplificado
[ ] Ramas protegidas configuradas
[ ] No se permite push directo a producción/main/master
[ ] No se permite push directo a ONP_QA/qa
[ ] Ramas de trabajo usan convención definida
[ ] Las ramas no contienen configuración funcional distinta por ambiente
```

### 22.3 Merge Request

```text
[ ] MR tiene descripción clara
[ ] MR referencia requerimiento/incidencia/tarea
[ ] MR identifica tipo de cambio
[ ] MR incluye evidencia de pruebas
[ ] MR incluye reporte/evidencia de análisis estático PMD sin violaciones críticas (Java)
[ ] MR declara impacto en API/BD/seguridad/K8s si aplica
[ ] MR tiene revisor asignado
[ ] MR fue aprobado por revisor distinto al autor
```

### 22.4 Release

```text
[ ] Release tiene tag semántico
[ ] Release notes mínimas documentadas
[ ] MRs incluidos identificados
[ ] Scripts BD asociados identificados
[ ] Plan de reversa documentado
```

---

## 23. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Commit directo a `main`, `master` o `prd` | Sin revisión ni trazabilidad | Prohibido |
| Commit directo a `ONP_QA` o `qa` | Salta control de promoción | Prohibido |
| Trabajar directamente en `ONP_DESA` o `dev` | Dificulta revisión y trazabilidad | Usar ramas de trabajo |
| Ramas por ambiente con código diferente | Rompe reproducibilidad | Usar configuración externa |
| MR sin descripción | No hay trazabilidad | No aprobar |
| MR sin evidencia de pruebas | Cambio no verificable | No aprobar |
| Scripts BD enviados por correo | Sin versionamiento | Prohibido |
| Modificar scripts ya ejecutados | Rompe trazabilidad histórica | Crear nuevo script |
| Tag `v1-final-final` | No trazable | Usar versionamiento semántico |
| Proyecto GitLab por módulo interno no desplegable | Fragmentación innecesaria | Proyecto por componente desplegable |
| Crear repositorios con nombres inconsistentes | Dificulta operación y automatización futura | Aplicar nomenclatura institucional |
| Aprobar el propio MR | Falta de revisión independiente | Prohibido |
| Usar ramas largas para funcionalidades incompletas | Retrasa integración y aumenta conflictos | Usar cambios pequeños y feature flags |
| Proyecto nuevo con ramas permanentes por ambiente sin ADR | Mantiene modelo no objetivo | Requiere ADR |

---

## 24. Proceso de excepción

Toda excepción a este lineamiento requiere justificación documentada. Si la excepción afecta arquitectura, se requiere aprobación de Arquitectura. Si afecta seguridad, se requiere validación de Seguridad Digital.

### 24.1 Casos típicos de excepción

- Repositorio monorepo.
- Uso de herramienta externa a GitLab.
- Proyecto con nombre heredado que no puede ajustarse.
- Necesidad temporal de commit directo por emergencia.
- Release sin tag por restricción técnica.
- Falta de evidencia automatizada en componente legacy.
- Cambio urgente en producción mediante hotfix.
- Cambio de BD sin estrategia de reversa técnica viable.
- Proyecto nuevo que solicita mantener ramas permanentes por ambiente.
- Proyecto nuevo que solicita no adoptar GitLab Flow simplificado.

### 24.2 Formato mínimo de excepción

```markdown
# EXC-VER-NNN — [Título]

## Contexto
[Qué regla no puede cumplirse]

## Justificación
[Por qué no puede cumplirse]

## Riesgo aceptado
[Qué riesgo genera]

## Control compensatorio
[Qué se hará para reducir el riesgo]

## Fecha de revisión
[Cuándo se revisará nuevamente]

## Aprobaciones
[Arquitectura / Seguridad / Plataforma, según corresponda]
```

---

## 25. Glosario

| Término | Definición |
|---|---|
| GitLab | Plataforma institucional de repositorios, colaboración y control de cambios |
| Proyecto GitLab | Repositorio asociado a un componente, librería o documentación |
| Grupo GitLab | Agrupador lógico de proyectos |
| Rama institucional | Rama que representa un estado de promoción del código |
| Rama de trabajo | Rama temporal creada para desarrollar un cambio específico |
| Merge Request | Solicitud de integración de cambios entre ramas |
| Review | Revisión técnica de código o documentación |
| Release | Versión liberada de un componente |
| Tag | Marca inmutable sobre un commit asociado a una versión |
| Versionamiento semántico | Esquema MAJOR.MINOR.PATCH |
| ADR | Architecture Decision Record |
| Hotfix | Corrección urgente sobre producción |
| CODEOWNERS | Archivo GitLab para definir responsables por rutas |
| Protected branch | Rama protegida contra cambios directos |
| GitLab Flow | Modelo de ramas y despliegue basado en GitLab, que puede usar ramas por ambiente o por promoción |
| Trunk-Based Development | Modelo donde los cambios se integran frecuentemente a una rama principal, con ramas de trabajo muy cortas |
| TBD controlado | Nombre anterior al término adoptado; ver GitLab Flow simplificado |
| GitLab Flow simplificado | Modelo operativo adoptado por ONP para proyectos nuevos: rama `main` única, ramas de trabajo cortas (1 a 5 días), mismo artefacto promovido a través de ambientes. Disciplinado con principios TBD. |
| Feature flag | Mecanismo para activar o desactivar funcionalidad sin mantener ramas largas |

---

## 26. Anexos

### Anexo A — Ejemplo de estructura de repositorio backend

```text
notificaciones-backend/
├── README.md
├── pom.xml
├── src/
│   ├── main/
│   └── test/
├── docs/
│   ├── adr/
│   └── openapi/
├── db/
│   ├── migration/
│   └── reverse/
├── k8s/
│   ├── base/
│   └── overlays/
└── Dockerfile
```

### Anexo B — Ejemplo de estructura de repositorio frontend

```text
notificaciones-front-ciudadano/
├── README.md
├── package.json
├── angular.json
├── src/
├── e2e/
├── docs/
├── k8s/
├── Dockerfile
└── nginx.conf
```

### Anexo C — Ejemplo de CODEOWNERS

> **Requisito previo:** Los usuarios y grupos referenciados (`@arquitectura-ti`, `@plataforma`, etc.) deben existir como grupos o usuarios reales en el GitLab de ONP. Validar con Plataforma los nombres exactos de grupos antes de activar este archivo.

```text
# Arquitectura
/docs/adr/ @arquitectura-ti

# APIs
/docs/openapi/ @arquitectura-ti @desarrollo-api

# Base de datos
/db/ @equipo-bd @arquitectura-ti

# Kubernetes
/k8s/ @plataforma @arquitectura-ti

# Seguridad
/src/main/java/**/security/ @seguridad-digital @arquitectura-ti
```

### Anexo D — Ejemplo de release notes

```markdown
# Release v1.2.0 — notificaciones-backend

## Fecha
2026-05-26

## Cambios incluidos
- REQ-1234 — Registro de notificación ciudadana
- INC-4567 — Corrección de validación de token
- ARQ-001 — Separación de adapter SAA

## Merge Requests
- !45
- !46
- !48

## Base de datos
- V1.2.0_001__crear_tabla_notificacion.sql
- V1.2.0_002__agregar_indice_estado.sql

## APIs
- openapi.yml actualizado
- Nuevo endpoint: POST /api/v1/notificaciones

## Seguridad
- Se mantiene validación SAA
- No se agregan nuevos secretos

## Despliegue
- Imagen: registry.gitlab.onp.gob.pe/aplicaciones/notificaciones/notificaciones-backend:1.2.0

## Plan de reversa
- Revertir a imagen 1.1.3
- Ejecutar reversa o compensación R1.2.0_001 si se requiere revertir cambio de BD
```

### Anexo E — Plantilla corta de commit convencional

> **Estado:** Este formato es recomendado, no obligatorio en esta fase. Sin CI/CD activo no es posible validarlo automáticamente. Su adopción mejora la legibilidad del historial y facilita la generación de release notes.

Formato recomendado:

```text
<tipo>: <descripción corta>
```

Tipos permitidos:

```text
feat: nueva funcionalidad
fix: corrección
docs: documentación
test: pruebas
refactor: refactorización
chore: tarea técnica
build: build o dependencias
sec: seguridad
db: base de datos
k8s: manifiestos o despliegue
```

Ejemplos:

```text
feat: agregar registro de notificacion ciudadana
fix: corregir validacion de token SAA
docs: actualizar contrato OpenAPI
test: agregar pruebas de caracterizacion para SP_VALIDAR_USUARIO
db: agregar indice para busqueda de notificaciones
k8s: ajustar readinessProbe del backend
sec: evitar almacenamiento de token en frontend
```

### Anexo F — Plantillas institucionales de proyecto GitLab

> **EJEMPLO DE REFERENCIA** — Este anexo describe el mecanismo y los tipos de plantilla institucional. Plataforma/Infraestructura es responsable de crear y mantener los proyectos plantilla reales en GitLab Ultimate.

#### F.1 Mecanismo — GitLab Group-level Project Templates

GitLab Ultimate permite configurar plantillas de proyecto a nivel de grupo. Cuando un usuario crea un nuevo proyecto dentro del grupo o subgrupo habilitado, puede seleccionar la plantilla institucional en lugar de partir desde cero.

Configuración requerida:

1. Crear subgrupo `gitlab-templates` dentro del grupo raíz.
2. Configurar en GitLab Admin → Settings → Custom project templates el grupo `gitlab-templates` como fuente.
3. Crear los proyectos plantilla dentro de `gitlab-templates`.
4. Mantener los proyectos plantilla versionados; cada cambio pasa por MR con aprobación de Arquitectura.

Ubicación sugerida:

```text
APLICACIONES/
└── gitlab-templates/
    ├── template-backend-java
    ├── template-frontend-angular
    └── template-worker-java
```

#### F.2 Tipos de plantilla institucional

| Tipo | Proyecto GitLab | Aplica a |
|---|---|---|
| Backend Java / Spring Boot | `template-backend-java` | APIs REST, adapters, servicios backend |
| Frontend Angular SPA | `template-frontend-angular` | Aplicaciones web Angular |
| Worker / Job Java | `template-worker-java` | Workers Kubernetes, jobs batch |

#### F.3 Contenido mínimo de cada plantilla

`template-backend-java` debe incluir:

| Archivo / Carpeta | Propósito |
|---|---|
| `Dockerfile` | Multi-stage build |
| `.gitignore` | Exclusiones estándar Maven/Java |
| `.gitlab/CODEOWNERS` | Grupos revisores por área |
| `.gitlab/merge_request_templates/default.md` | Plantilla MR completa |
| `.gitlab/merge_request_templates/minor.md` | Plantilla MR simplificada |
| `k8s/base/kustomization.yaml` | Base Kustomize |
| `k8s/overlays/dev/kustomization.yaml` | Overlay DEV |
| `k8s/overlays/qa/kustomization.yaml` | Overlay QA |
| `k8s/overlays/prod/kustomization.yaml` | Overlay PROD |
| `docs/adr/.gitkeep` | Carpeta para ADRs |
| `docs/openapi/.gitkeep` | Carpeta para OpenAPI |
| `db/migration/.gitkeep` | Carpeta para scripts de migración |
| `db/reverse/.gitkeep` | Carpeta para scripts de reversa o compensación |
| `README.md` | Descripción, prerrequisitos, ejecución local, contacto |

`template-frontend-angular` debe incluir:

| Archivo / Carpeta | Propósito |
|---|---|
| `Dockerfile` | Multi-stage build |
| `nginx.conf` | Configuración SPA |
| `.gitignore` | Exclusiones estándar Node/Angular |
| `.gitlab/CODEOWNERS` | Grupos revisores |
| `.gitlab/merge_request_templates/default.md` | Plantilla MR completa |
| `.gitlab/merge_request_templates/minor.md` | Plantilla MR simplificada |
| `k8s/base/kustomization.yaml` | Base Kustomize |
| `k8s/overlays/dev/kustomization.yaml` | Overlay DEV |
| `k8s/overlays/qa/kustomization.yaml` | Overlay QA |
| `k8s/overlays/prod/kustomization.yaml` | Overlay PROD |
| `docs/adr/.gitkeep` | Carpeta para ADRs |
| `e2e/.gitkeep` | Carpeta para pruebas E2E |
| `README.md` | Estructura mínima |

#### F.4 Proceso de onboarding de nuevo sistema

1. Desarrollo solicita a Plataforma la creación del repositorio indicando sistema, tipo de componente y responsable técnico.
2. Plataforma crea el proyecto en GitLab usando la plantilla institucional correspondiente.
3. Plataforma configura ramas protegidas según el modelo aplicable:
   - modelo vigente controlado, si es proyecto existente o excepción;
   - GitLab Flow simplificado, si es proyecto nuevo.
4. Desarrollo personaliza el `README.md`, ajusta grupos reales de `CODEOWNERS` y agrega contenido real.
5. El primer MR activa el flujo de revisión establecido en este lineamiento.

#### F.5 Archivos de referencia

Los archivos de ejemplo que implementan el contenido mínimo de F.3 se encuentran en:

```text
versionamiento/
└── ejemplos-plantillas-gitlab/
    ├── template-backend-java/
    │   ├── Dockerfile
    │   ├── .gitignore
    │   ├── README.md
    │   ├── .gitlab/
    │   │   ├── CODEOWNERS
    │   │   └── merge_request_templates/
    │   │       ├── default.md
    │   │       └── minor.md
    │   ├── k8s/
    │   │   ├── base/
    │   │   └── overlays/
    │   │       ├── dev/
    │   │       ├── qa/
    │   │       └── prod/
    │   ├── docs/
    │   │   ├── adr/
    │   │   └── openapi/
    │   └── db/
    │       ├── migration/
    │       └── reverse/
    └── template-frontend-angular/
        ├── Dockerfile
        ├── nginx.conf
        ├── .gitignore
        ├── README.md
        ├── .gitlab/
        │   ├── CODEOWNERS
        │   └── merge_request_templates/
        │       ├── default.md
        │       └── minor.md
        ├── k8s/
        │   ├── base/
        │   └── overlays/
        │       ├── dev/
        │       ├── qa/
        │       └── prod/
        ├── docs/
        │   └── adr/
        └── e2e/
```

> Estos archivos son **EJEMPLO DE REFERENCIA** mantenido por Arquitectura OTI. Plataforma/Infraestructura es responsable de mantener los proyectos plantilla reales en GitLab Ultimate actualizados con este contenido.
