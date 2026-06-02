# LIN-VER-001 — Lineamiento de Versionamiento, Control de Cambios y Revisión de Código ONP

**Código:** LIN-VER-001  
**Versión:** v0.1.0  
**Estado:** Borrador  
**Fecha:** 2026-05-26  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Desarrollo, QA, Seguridad Digital, Plataforma/Infraestructura, Arquitectura  
**Marco rector:** LIN-ARQ-000 — Marco Rector de Diseño y Arquitectura de Software  
**Herramienta institucional:** GitLab Ultimate  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-26 | Arquitectura OTI | Borrador inicial del lineamiento de versionamiento, control de cambios y revisión de código |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)  
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)  
3. [Principios rectores](#3-principios-rectores)  
4. [Modelo institucional adoptado](#4-modelo-institucional-adoptado)  
5. [Modelo de repositorios GitLab](#5-modelo-de-repositorios-gitlab)  
6. [Nomenclatura de grupos, subgrupos y proyectos](#6-nomenclatura-de-grupos-subgrupos-y-proyectos)  
7. [Modelo de ramas institucionales](#7-modelo-de-ramas-institucionales)  
8. [Ramas de trabajo](#8-ramas-de-trabajo)  
9. [Merge Requests obligatorios](#9-merge-requests-obligatorios)  
10. [Revisión de código](#10-revisión-de-código)  
11. [Evidencias mínimas por tipo de cambio](#11-evidencias-mínimas-por-tipo-de-cambio)  
12. [Versionamiento semántico](#12-versionamiento-semántico)  
13. [Tags y releases](#13-tags-y-releases)  
14. [Control de cambios de base de datos](#14-control-de-cambios-de-base-de-datos)  
15. [Cambios en APIs, contratos y OpenAPI](#15-cambios-en-apis-contratos-y-openapi)  
16. [Cambios en contenedores y manifiestos Kubernetes](#16-cambios-en-contenedores-y-manifiestos-kubernetes)  
17. [Relación con ADRs](#17-relación-con-adrs)  
18. [Relación con CI/CD](#18-relación-con-cicd)  
19. [Uso de capacidades GitLab Ultimate](#19-uso-de-capacidades-gitlab-ultimate)  
20. [Checklist de conformidad](#20-checklist-de-conformidad)  
21. [Anti-patrones](#21-anti-patrones)  
22. [Proceso de excepción](#22-proceso-de-excepción)  
23. [Glosario](#23-glosario)  
24. [Anexos](#24-anexos)
    - Anexo F — Plantillas institucionales de proyecto GitLab  

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece las reglas mínimas para organizar repositorios GitLab, controlar ramas, versionar código, revisar cambios, aprobar Merge Requests, etiquetar releases y asegurar trazabilidad entre requerimientos, incidencias, código fuente, pruebas, despliegue y decisiones de arquitectura.

El objetivo no es reemplazar a CI/CD ni exigir automatización inmediata. El objetivo es establecer una base institucional de control de cambios que pueda aplicarse desde ahora, usando GitLab como repositorio y mecanismo de revisión.

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
| Ejecución automática de pipelines | LIN-CICD-001 — mapeado, no priorizado |
| Definición de arquitectura de aplicación | LIN-ARQ-000 |
| Estándar de desarrollo Java | LIN-DEV-JAVA-001 |
| Estándar de APIs REST | LIN-API-REST-001 |
| Estándar de base de datos Oracle | LIN-BD-ORA-001 |
| Estándar frontend Angular | LIN-FE-ANG-001 |
| Seguridad en aplicaciones | LIN-SEC-APP-001 |
| Pruebas automatizadas | LIN-TEST-001 |
| Contenedores y orquestación | LIN-K8S-001 |
| Infraestructura como Código | LIN-IAC-001 — mapeado, no priorizado |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Diseño y Arquitectura de Software | LIN-ARQ-000 | Define principios y decisiones rectoras de arquitectura |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Define reglas de implementación backend |
| Estándar de APIs REST | LIN-API-REST-001 | Define contrato, versionamiento y publicación de APIs |
| Estándar de Base de Datos Oracle | LIN-BD-ORA-001 | Define control de scripts y tratamiento de PL/SQL legacy |
| Estándar de Frontend Angular | LIN-FE-ANG-001 | Define reglas para aplicaciones frontend |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Define trazabilidad y evidencias operativas |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Define controles de seguridad y excepciones |
| Estándar de Pruebas | LIN-TEST-001 | Define evidencias de pruebas y cobertura |
| Contenedores y Orquestación | LIN-K8S-001 | Define imágenes, manifiestos, tags y despliegue en K8s |
| Directiva de Desarrollo de Software Seguro | DIR-SEC-SW-001 | Marco superior para controles de seguridad |
| CI/CD | LIN-CICD-001 | Capacidad objetivo; no priorizada en esta fase |
| Infraestructura como Código | LIN-IAC-001 | Capacidad objetivo; no priorizada en esta fase |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **Trazabilidad completa** | Todo cambio debe poder rastrearse desde requerimiento, incidencia o tarea hasta commit, Merge Request, release y despliegue. |
| P2 | **No cambios directos en ramas protegidas** | Todo cambio debe ingresar mediante Merge Request. |
| P3 | **Revisión obligatoria** | Ningún cambio debe aprobarse sin revisión técnica. |
| P4 | **Separación por componente desplegable** | Los repositorios se organizan por componente con ciclo propio de construcción, despliegue o versionamiento. |
| P5 | **Ramas institucionales como promoción** | `ONP_DESA`, `ONP_PQA`, `ONP_QA` y `master` representan estados de promoción, no configuraciones diferentes por ambiente. |
| P6 | **Configuración fuera del código** | Las diferencias entre DEV, QA y PROD se gestionan mediante configuración externa, conforme a LIN-K8S-001. |
| P7 | **Evidencia mínima antes del merge** | Todo Merge Request debe indicar qué se probó, qué cambió y qué impacto tiene. |
| P8 | **Versionamiento explícito** | Todo release productivo debe contar con tag y versión identificable. |
| P9 | **Scripts de BD versionados** | Todo cambio de base de datos debe estar en GitLab; no se aceptan scripts sueltos enviados por correo o mensajería. |
| P10 | **Preparación para CI/CD** | Este lineamiento establece la base para automatización futura, sin exigir CI/CD completo en esta fase. |

---

## 4. Modelo institucional adoptado

La ONP adopta un modelo de repositorios por componente desplegable y un flujo de ramas institucionales para promoción de cambios.

Las ramas `ONP_DESA`, `ONP_PQA`, `ONP_QA` y `master` se mantienen como ramas de integración/promoción. El desarrollo diario no debe realizarse directamente sobre dichas ramas, sino mediante ramas de trabajo integradas por Merge Request.

Este lineamiento no reemplaza a CI/CD. Establece la base mínima de control de cambios que posteriormente podrá ser automatizada por `LIN-CICD-001`.

Flujo general:

```text
rama de trabajo
   ↓ Merge Request
ONP_DESA
   ↓ promoción
ONP_PQA
   ↓ promoción
ONP_QA
   ↓ aprobación
master
   ↓ tag / release
vX.Y.Z
```

Si el proyecto no utiliza `ONP_PQA`, el flujo mínimo será:

```text
rama de trabajo → ONP_DESA → ONP_QA → master → tag / release
```

---

## 5. Modelo de repositorios GitLab

### 5.1 Proyecto GitLab por componente desplegable

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

### 5.2 Cuándo separar repositorios

Debe crearse un repositorio separado cuando el componente:

- se construye de forma independiente;
- se despliega de forma independiente;
- tiene versionamiento propio;
- tiene responsables o ciclo de vida distinto;
- genera una imagen de contenedor distinta;
- puede ser consumido por varios componentes;
- requiere permisos o revisores diferenciados.

### 5.3 Cuándo no separar repositorios

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

### 5.4 Monorepo

El uso de monorepo está permitido solo con justificación mediante ADR cuando:

- los componentes tienen alta dependencia de cambios coordinados;
- existe una estructura clara de carpetas;
- se puede controlar revisión por rutas;
- se puede versionar y desplegar cada componente sin ambigüedad.

Ejemplo:

```text
notificaciones/
├── backend/
├── frontend-ciudadano/
├── frontend-consulta/
├── worker/
└── k8s/
```

Regla:

> La práctica recomendada inicial en ONP es repositorio por componente desplegable. El monorepo requiere ADR.

---

## 6. Nomenclatura de grupos, subgrupos y proyectos

### 6.1 Convención general

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

### 6.2 Estructura de grupos

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

### 6.3 Nombres heredados

Los proyectos existentes con nombres no alineados no requieren renombrado inmediato si ello afecta integraciones, permisos, rutas o despliegues.

Sin embargo, todo proyecto nuevo debe seguir la convención definida. Los proyectos existentes deberán alinearse cuando exista una intervención mayor, migración o reorganización del repositorio.

> **Nota sobre subgrupos existentes:** La estructura actual de GitLab ONP usa nombres en mayúsculas para subgrupos (por ejemplo, `APLICACIONES / NOTIFICACION_ELECTRONICA / BACKEND`). Estos no requieren ajuste inmediato. La convención de minúsculas con guion medio aplica a **proyectos nuevos** y a **subgrupos nuevos** creados a partir de la vigencia de este lineamiento. Los subgrupos existentes se alinearán cuando se realice una reorganización planificada.

---

## 7. Modelo de ramas institucionales

### 7.1 Ramas institucionales

Las ramas institucionales representan estados de promoción del código y no deben contener diferencias funcionales propias de ambiente.

| Rama | Uso | Regla |
|---|---|---|
| `ONP_DESA` | Integración de desarrollo | Recibe cambios desde ramas de trabajo mediante Merge Request |
| `ONP_PQA` | Precalidad / estabilización | Recibe promociones desde `ONP_DESA`, si aplica en el proyecto |
| `ONP_QA` | Calidad formal | Recibe versión candidata para pruebas QA |
| `master` | Producción | Solo recibe cambios aprobados desde `ONP_QA` o `hotfix/*` autorizado |

### 7.2 Flujo de promoción

Flujo estándar:

```text
feature/* | bugfix/* | refactor/* | test/* | docs/*
        ↓ MR
ONP_DESA
        ↓ MR / promoción
ONP_PQA
        ↓ MR / promoción
ONP_QA
        ↓ MR aprobado
master
        ↓ tag / release
vX.Y.Z
```

Flujo mínimo si no se usa `ONP_PQA`:

```text
feature/* | bugfix/* | refactor/* | test/* | docs/*
        ↓ MR
ONP_DESA
        ↓ MR / promoción
ONP_QA
        ↓ MR aprobado
master
        ↓ tag / release
vX.Y.Z
```

### 7.3 Reglas para ramas institucionales

| Regla | Estado |
|---|---|
| `master` debe ser rama protegida | Obligatorio |
| `ONP_QA` debe ser rama protegida | Obligatorio |
| `ONP_PQA` debe ser rama protegida si existe | Obligatorio |
| `ONP_DESA` debe ser rama protegida | Recomendado / obligatorio para proyectos críticos |
| No se permiten commits directos a ramas protegidas | Obligatorio |
| Todo cambio ingresa por Merge Request | Obligatorio |
| Las ramas no deben mantener configuración distinta por ambiente | Obligatorio |

> **Responsable de configuración:** La configuración de ramas protegidas en GitLab es responsabilidad de **Plataforma/Infraestructura**, como parte del proceso de habilitación del repositorio. El equipo de desarrollo solicita la protección al crear el proyecto; Plataforma la aplica y valida.

---

## 8. Ramas de trabajo

### 8.1 Regla general

El desarrollo diario no debe realizarse directamente sobre `ONP_DESA`, `ONP_PQA`, `ONP_QA` ni `master`. Todo cambio debe iniciar en una rama de trabajo.

### 8.2 Tipos de ramas

| Tipo de rama | Uso | Ejemplo |
|---|---|---|
| `feature/*` | Nueva funcionalidad | `feature/REQ-1234-registro-notificacion` |
| `bugfix/*` | Corrección no productiva | `bugfix/INC-4567-error-validacion` |
| `hotfix/*` | Corrección urgente de producción | `hotfix/PROD-0098-correccion-token` |
| `refactor/*` | Mejora interna sin cambio funcional | `refactor/ARQ-001-separar-adapter-saa` |
| `test/*` | Incorporación o ajuste de pruebas | `test/REQ-1234-pruebas-caracterizacion` |
| `docs/*` | Documentación | `docs/API-002-actualizar-openapi` |
| `chore/*` | Tareas técnicas menores | `chore/BUILD-003-actualizar-dependencias` |

### 8.3 Convención de nombre

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

### 8.4 Reglas para ramas de trabajo

- Deben partir de la rama institucional correspondiente, normalmente `ONP_DESA`.
- Deben tener un propósito claro y acotado.
- Deben eliminarse después del merge, salvo razón justificada.
- No deben usarse como ramas permanentes de ambiente.
- No deben contener secretos ni archivos temporales.

---

## 9. Merge Requests obligatorios

### 9.1 Regla principal

Todo cambio debe ingresar a una rama institucional mediante Merge Request. No se permiten commits directos a ramas protegidas.

### 9.2 Contenido mínimo del Merge Request

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
- instrucciones de despliegue o rollback, si aplica;
- revisor asignado.

### 9.3 Tipos de cambio

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

### 9.4 Plantilla mínima de Merge Request

> **MR simplificado:** Para cambios de documentación (`docs/`), tareas técnicas menores (`chore/`) o ajustes internos sin impacto funcional, se permite un MR con formato simplificado: tipo de cambio, referencia y descripción breve. No es necesario completar todas las secciones si claramente no aplican. Para cualquier cambio funcional, de seguridad, BD, API o K8s, el template completo es obligatorio.

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
- [ ] Script de rollback incluido
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

## Rollback
[Cómo revertir el cambio si falla]

## Revisores
- Revisor técnico:
- Arquitectura, si aplica:
- Seguridad, si aplica:
- Plataforma, si aplica:
```

---

## 10. Revisión de código

### 10.1 Reglas mínimas

| Regla | Estado |
|---|---|
| Ningún desarrollador aprueba su propio cambio | Obligatorio |
| Todo MR debe tener al menos una revisión técnica | Obligatorio |
| Cambios críticos requieren revisión especializada | Obligatorio |
| La revisión debe verificar cumplimiento de lineamientos aplicables | Obligatorio |
| No se aprueban MRs sin descripción ni evidencia mínima | Obligatorio |

### 10.2 Revisión por tipo de cambio

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

### 10.3 Criterios de revisión

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
- scripts de rollback;
- actualización de manifiestos o variables;
- necesidad de ADR.

---

## 11. Evidencias mínimas por tipo de cambio

Hasta la implementación de `LIN-CICD-001`, las evidencias mínimas se adjuntan, enlazan o describen en el Merge Request.

| Tipo de cambio | Evidencia mínima |
|---|---|
| Backend Java | Resultado de pruebas unitarias/integración, cobertura si aplica |
| Frontend Angular | Pruebas unitarias, build local, E2E si aplica |
| API REST | `openapi.yml` actualizado y evidencia de compatibilidad |
| Contrato API crítico | Validación OpenAPI o prueba de contrato |
| Base de datos | Script versionado, script rollback si aplica, evidencia de prueba |
| PL/SQL legacy | Pruebas de caracterización antes y después |
| Seguridad | Evidencia de revisión, escaneo o control compensatorio |
| Observabilidad | Evidencia de logs/trazas/métricas si aplica |
| K8s | Manifiestos actualizados, tag de imagen, variables/secretos documentados |
| Documentación | Archivo actualizado y revisión correspondiente |

---

## 12. Versionamiento semántico

### 12.1 Regla general

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

### 12.2 Criterio de incremento

| Tipo de cambio | Incremento |
|---|---|
| Cambio incompatible con consumidores existentes | `MAJOR` |
| Nueva funcionalidad compatible | `MINOR` |
| Corrección compatible | `PATCH` |
| Cambio documental sin impacto de ejecución | No necesariamente incrementa release |
| Cambio en infraestructura de despliegue | Según impacto |
| Cambio en contrato API incompatible | `MAJOR` o nueva versión de API |

### 12.3 Pre-releases

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

## 13. Tags y releases

### 13.1 Regla general

Todo pase a producción debe tener un tag Git asociado.

Formato obligatorio:

```text
v<MAJOR>.<MINOR>.<PATCH>
```

Ejemplos:

```text
v1.0.0
v1.1.0
v1.1.1
```

### 13.2 Release notes mínimas

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
- rollback previsto;
- responsable de release.

### 13.3 Relación con imágenes de contenedor

Cuando el componente genera imagen de contenedor, el tag de imagen debe corresponder a la versión del release o a un identificador trazable.

Ejemplo:

```text
Git tag:
v1.2.0

Imagen:
registry.gitlab.onp.gob.pe/aplicaciones/notificaciones/notificaciones-backend:1.2.0
```

---

## 14. Control de cambios de base de datos

### 14.1 Regla general

Todo cambio de base de datos debe estar versionado en el repositorio correspondiente. No se aceptan cambios manuales no documentados en QA o Producción.

### 14.2 Estructura sugerida

```text
db/
├── migration/
│   ├── V1.2.0_001__crear_tabla_notificacion.sql
│   ├── V1.2.0_002__agregar_indice_estado.sql
│   └── V1.2.1_001__ajustar_package_validacion.sql
└── rollback/
    ├── R1.2.0_001__drop_tabla_notificacion.sql
    └── R1.2.0_002__drop_indice_estado.sql
```

### 14.3 Reglas para scripts

| Regla | Estado |
|---|---|
| Todo script debe tener versión | Obligatorio |
| Todo script debe tener descripción clara | Obligatorio |
| Todo script debe indicar orden de ejecución | Obligatorio |
| Todo script debe tener rollback cuando aplique | Obligatorio |
| No se envían scripts por correo como fuente oficial | Obligatorio |
| No se modifican scripts ya ejecutados en QA/PROD | Obligatorio |
| Las correcciones se hacen con un nuevo script | Obligatorio |

### 14.4 PL/SQL legacy

Cuando el cambio afecte procedures, packages o functions con lógica de negocio crítica:

- debe cumplirse `LIN-BD-ORA-001`;
- debe cumplirse `LIN-TEST-001` sobre pruebas de caracterización;
- debe adjuntarse evidencia de prueba antes y después del cambio;
- debe documentarse el comportamiento modificado.

---

## 15. Cambios en APIs, contratos y OpenAPI

### 15.1 Regla general

Todo cambio en una API REST debe actualizar el contrato OpenAPI correspondiente.

### 15.2 Cambios compatibles

Ejemplos:

- agregar campo opcional;
- agregar endpoint nuevo;
- ampliar descripción;
- agregar código de respuesta documentado sin romper consumidores.

### 15.3 Cambios incompatibles

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

## 16. Cambios en contenedores y manifiestos Kubernetes

Todo cambio que afecte Dockerfile, imagen, manifiestos Kubernetes, ConfigMap, Secret, probes, recursos o rutas de exposición debe cumplir `LIN-K8S-001`.

Evidencia mínima:

- Dockerfile actualizado, si aplica;
- manifiestos actualizados;
- tag de imagen explícito;
- variables de entorno documentadas;
- secretos requeridos documentados sin valores;
- impacto en recursos/probes;
- rollback previsto.

---

## 17. Relación con ADRs

Un ADR es obligatorio cuando el cambio:

- introduce una decisión arquitectónica relevante;
- cambia el estilo arquitectónico;
- modifica el contrato API de forma incompatible;
- introduce o elimina un componente desplegable;
- modifica integración con SAA, WSO2 o sistemas críticos;
- requiere excepción a un lineamiento;
- afecta seguridad de forma significativa;
- cambia persistencia o lógica PL/SQL crítica;
- introduce patrón arquitectónico relevante.

Los ADRs deben estar versionados en el repositorio del proyecto o en el repositorio documental definido por Arquitectura.

Estructura sugerida:

```text
docs/
└── adr/
    ├── ADR-0001-uso-bff-ciudadano.md
    ├── ADR-0002-integracion-saa-token.md
    └── ADR-0003-excepcion-cobertura-legacy.md
```

---

## 18. Relación con CI/CD

CI/CD se encuentra mapeado como capacidad objetivo, pero no priorizada en esta fase.

Hasta la aprobación de `LIN-CICD-001`:

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

---

## 19. Uso de capacidades GitLab Ultimate

Aunque CI/CD no esté priorizado, GitLab Ultimate debe aprovecharse progresivamente para control de cambios.

### 19.1 Capacidades recomendadas desde esta fase

| Capacidad | Uso |
|---|---|
| Protected branches | Proteger `master`, `ONP_QA`, `ONP_PQA`, `ONP_DESA` |
| Merge Requests | Canal obligatorio de integración |
| Approval rules | Reglas de aprobación por tipo de cambio |
| CODEOWNERS | Revisores por carpeta o componente |
| Issues | Trazabilidad de requerimientos/incidencias |
| Labels | Clasificación de cambios |
| Milestones | Agrupación por release o entrega |
| Releases | Registro de versiones liberadas |
| Tags | Identificación de versión |
| Wiki / Markdown docs | Documentación técnica auxiliar |

### 19.2 Uso posterior

| Capacidad | Fase posterior |
|---|---|
| GitLab CI/CD | LIN-CICD-001 |
| Security scanning | LIN-CICD-001 / LIN-SEC-APP-001 |
| Container scanning | LIN-K8S-001 / LIN-CICD-001 |
| Dependency scanning | LIN-SEC-APP-001 / LIN-CICD-001 |
| Environments | LIN-CICD-001 |
| Deployments | LIN-CICD-001 |

---

## 20. Checklist de conformidad

### 20.1 Repositorio

```text
[ ] Proyecto GitLab creado por componente desplegable
[ ] Nombre del proyecto en minúsculas y con guion medio
[ ] README mínimo actualizado
[ ] Estructura de carpetas clara
[ ] No existen secretos versionados
```

### 20.2 Ramas

```text
[ ] Ramas institucionales definidas
[ ] Ramas protegidas configuradas
[ ] No se permite push directo a master
[ ] No se permite push directo a ONP_QA
[ ] Ramas de trabajo usan convención definida
```

### 20.3 Merge Request

```text
[ ] MR tiene descripción clara
[ ] MR referencia requerimiento/incidencia/tarea
[ ] MR identifica tipo de cambio
[ ] MR incluye evidencia de pruebas
[ ] MR declara impacto en API/BD/seguridad/K8s si aplica
[ ] MR tiene revisor asignado
[ ] MR fue aprobado por revisor distinto al autor
```

### 20.4 Release

```text
[ ] Release tiene tag semántico
[ ] Release notes mínimas documentadas
[ ] MRs incluidos identificados
[ ] Scripts BD asociados identificados
[ ] Rollback documentado
```

---

## 21. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Commit directo a `master` | Sin revisión ni trazabilidad | Prohibido |
| Commit directo a `ONP_QA` | Salta control de promoción | Prohibido |
| Trabajar directamente en `ONP_DESA` | Dificulta revisión y trazabilidad | Usar ramas de trabajo |
| Ramas por ambiente con código diferente | Rompe reproducibilidad | Usar configuración externa |
| MR sin descripción | No hay trazabilidad | No aprobar |
| MR sin evidencia de pruebas | Cambio no verificable | No aprobar |
| Scripts BD enviados por correo | Sin versionamiento | Prohibido |
| Modificar scripts ya ejecutados | Rompe trazabilidad histórica | Crear nuevo script |
| Tag `v1-final-final` | No trazable | Usar versionamiento semántico |
| Proyecto GitLab por módulo interno no desplegable | Fragmentación innecesaria | Proyecto por componente desplegable |
| Crear repositorios con nombres inconsistentes | Dificulta operación y automatización futura | Aplicar nomenclatura institucional |
| Aprobar el propio MR | Falta de revisión independiente | Prohibido |

---

## 22. Proceso de excepción

Toda excepción a este lineamiento requiere justificación documentada. Si la excepción afecta arquitectura, se requiere aprobación de Arquitectura. Si afecta seguridad, se requiere validación de Seguridad Digital.

### 22.1 Casos típicos de excepción

- Repositorio monorepo.
- Uso de herramienta externa a GitLab.
- Proyecto con nombre heredado que no puede ajustarse.
- Necesidad temporal de commit directo por emergencia.
- Release sin tag por restricción técnica.
- Falta de evidencia automatizada en componente legacy.
- Cambio urgente en producción mediante hotfix.
- Cambio de BD sin rollback técnico viable.

### 22.2 Formato mínimo de excepción

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

## 23. Glosario

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

---

## 24. Anexos

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
│   └── rollback/
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

> **Requisito previo:** Los usuarios y grupos referenciados (`@arquitectura-ti`, `@plataforma`, etc.) deben existir como grupos o usuarios reales en el GitLab de ONP. Si el grupo no existe, CODEOWNERS no aplica la regla silenciosamente — no genera error, simplemente no asigna revisor. Validar con Plataforma los nombres exactos de grupos antes de activar este archivo.

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

## Rollback
- Revertir a imagen 1.1.3
- Ejecutar rollback R1.2.0_001 si se requiere revertir cambio de BD
```

### Anexo E — Plantilla corta de commit convencional

> **Estado:** Este formato es **recomendado**, no obligatorio en esta fase. Sin CI/CD activo no es posible validarlo automáticamente. Su adopción mejora la legibilidad del historial y facilita la generación de release notes. LIN-CICD-001 podrá incorporar validación automática (commit-lint) cuando esté priorizado.

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

---

### Anexo F — Plantillas institucionales de proyecto GitLab

> **EJEMPLO DE REFERENCIA** — Este anexo describe el mecanismo y los tipos de plantilla institucional. Los archivos físicos de ejemplo se ubican en la carpeta `ejemplos-plantillas-gitlab/` adjunta a este lineamiento. **Plataforma/Infraestructura es responsable de crear y mantener los proyectos plantilla reales en GitLab Ultimate.**

#### F.1 Mecanismo — GitLab Group-level Project Templates

GitLab Ultimate permite configurar plantillas de proyecto a nivel de grupo. Cuando un usuario crea un nuevo proyecto dentro del grupo (o subgrupo habilitado), puede seleccionar la plantilla institucional en lugar de partir desde cero.

**Configuración requerida (responsabilidad de Plataforma):**

1. Crear subgrupo `gitlab-templates` dentro del grupo raíz (ej. `APLICACIONES/gitlab-templates`).
2. Configurar en GitLab Admin → Settings → Custom project templates el grupo `gitlab-templates` como fuente.
3. Crear los proyectos plantilla dentro de `gitlab-templates` (un proyecto por tipo).
4. Mantener los proyectos plantilla versionados; cada cambio pasa por MR con aprobación de Arquitectura.

**Ubicación en GitLab:**

```
APLICACIONES/
└── gitlab-templates/
    ├── template-backend-java        ← Proyecto plantilla Spring Boot
    ├── template-frontend-angular    ← Proyecto plantilla Angular SPA
    └── template-worker-java         ← Proyecto plantilla worker/job (futuro)
```

#### F.2 Tipos de plantilla institucional

| Tipo | Proyecto GitLab | Aplica a |
|---|---|---|
| Backend Java / Spring Boot | `template-backend-java` | Microservicios, APIs REST, adapters |
| Frontend Angular SPA | `template-frontend-angular` | Aplicaciones web Angular |
| Worker / Job Java | `template-worker-java` | Workers Kubernetes, jobs batch (futuro) |

#### F.3 Contenido mínimo de cada plantilla

**`template-backend-java` debe incluir:**

| Archivo / Carpeta | Propósito |
|---|---|
| `Dockerfile` | Multi-stage build (build + runtime JRE distroless) |
| `.gitignore` | Exclusiones estándar Maven/Java |
| `.gitlab/CODEOWNERS` | Grupos revisores por área (Arquitectura, QA, Seguridad) |
| `.gitlab/merge_request_templates/default.md` | Plantilla MR completa (§9.3) |
| `.gitlab/merge_request_templates/minor.md` | Plantilla MR simplificada (§9.4) |
| `k8s/base/kustomization.yaml` | Base Kustomize vacía |
| `k8s/overlays/dev/kustomization.yaml` | Overlay para ONP_DESA |
| `k8s/overlays/qa/kustomization.yaml` | Overlay para ONP_QA |
| `k8s/overlays/prod/kustomization.yaml` | Overlay para master/producción |
| `docs/adr/.gitkeep` | Carpeta para Architecture Decision Records |
| `docs/openapi/.gitkeep` | Carpeta para contratos OpenAPI |
| `db/migration/.gitkeep` | Carpeta para scripts Flyway/Liquibase |
| `db/rollback/.gitkeep` | Carpeta para scripts de rollback |
| `README.md` | Estructura mínima: descripción, prerrequisitos, ejecución local, contacto |

**`template-frontend-angular` debe incluir:**

| Archivo / Carpeta | Propósito |
|---|---|
| `Dockerfile` | Multi-stage build (Node build + nginx runtime) |
| `nginx.conf` | Configuración SPA con `try_files` (ver LIN-K8S-001 Anexo D) |
| `.gitignore` | Exclusiones estándar Node/Angular |
| `.gitlab/CODEOWNERS` | Grupos revisores |
| `.gitlab/merge_request_templates/default.md` | Plantilla MR completa |
| `.gitlab/merge_request_templates/minor.md` | Plantilla MR simplificada |
| `k8s/base/kustomization.yaml` | Base Kustomize vacía |
| `k8s/overlays/dev/kustomization.yaml` | Overlay para ONP_DESA |
| `k8s/overlays/qa/kustomization.yaml` | Overlay para ONP_QA |
| `k8s/overlays/prod/kustomization.yaml` | Overlay para master/producción |
| `docs/adr/.gitkeep` | Carpeta para Architecture Decision Records |
| `e2e/.gitkeep` | Carpeta para pruebas E2E Playwright |
| `README.md` | Estructura mínima |

#### F.4 Proceso de onboarding de nuevo sistema

1. **Equipo de desarrollo** solicita a Plataforma la creación del repositorio indicando: nombre del sistema, tipo (backend/frontend/worker), responsable técnico.
2. **Plataforma** crea el proyecto en GitLab usando la plantilla institucional correspondiente, configura ramas protegidas (`ONP_DESA`, `ONP_PQA`, `ONP_QA`, `master`) y asigna el grupo CODEOWNERS inicial.
3. **Equipo de desarrollo** personaliza el `README.md`, ajusta el `CODEOWNERS` con los grupos reales del sistema y elimina las carpetas `.gitkeep` al agregar contenido real.
4. **Primer MR** al repositorio activa el flujo de revisión establecido en §9 de este lineamiento.

#### F.5 Referencia a archivos de ejemplo

Los archivos físicos de ejemplo se ubican en:

```
Lineamientos_Nuevos_Borradores/versionamiento/ejemplos-plantillas-gitlab/
├── template-backend-java/
│   ├── .gitlab/
│   │   ├── merge_request_templates/
│   │   │   ├── default.md
│   │   │   └── minor.md
│   │   └── CODEOWNERS
│   ├── docs/adr/.gitkeep
│   ├── docs/openapi/.gitkeep
│   ├── db/migration/.gitkeep
│   ├── db/rollback/.gitkeep
│   ├── k8s/
│   │   ├── base/kustomization.yaml
│   │   └── overlays/{dev,qa,prod}/kustomization.yaml
│   ├── Dockerfile
│   ├── .gitignore
│   └── README.md
└── template-frontend-angular/
    ├── .gitlab/
    │   ├── merge_request_templates/
    │   │   ├── default.md
    │   │   └── minor.md
    │   └── CODEOWNERS
    ├── docs/adr/.gitkeep
    ├── e2e/.gitkeep
    ├── k8s/
    │   ├── base/kustomization.yaml
    │   └── overlays/{dev,qa,prod}/kustomization.yaml
    ├── Dockerfile
    ├── nginx.conf
    ├── .gitignore
    └── README.md
```

> Estos archivos son **ejemplos de referencia** producidos por Arquitectura OTI. Plataforma/Infraestructura es responsable de trasladar y mantener el contenido real en los proyectos plantilla de GitLab Ultimate.
