# LIN-CICD-001 — Lineamiento de Integración y Entrega Continua ONP

**Código:** LIN-CICD-001  
**Versión:** v0.1.2  
**Estado:** Borrador  
**Fecha:** 2026-05-28  
**Propietario documental:** Arquitectura de Software — OTI  
**Revisores sugeridos:** Desarrollo, QA, Plataforma/Infraestructura, Seguridad Digital, Arquitectura  
**Marco rector:** LIN-ARQ-000 — Marco Rector de Diseño y Arquitectura de Software  
**Herramienta institucional:** GitLab Ultimate  

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-05-28 | Arquitectura OTI | Borrador inicial del lineamiento de integración y entrega continua |
| v0.1.1 | 2026-05-28 | Arquitectura OTI | Declara el estado operacional actual de las capacidades CI/CD para adopción progresiva en GitLab Ultimate |
| v0.1.2 | 2026-05-28 | Arquitectura OTI | Normaliza el lenguaje operativo hacia plan de reversa en despliegues y elimina ambigüedad terminológica con reversa BD Oracle |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)  
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)  
3. [Principios rectores](#3-principios-rectores)  
4. [Modelo de madurez CI/CD ONP](#4-modelo-de-madurez-cicd-onp)  
5. [Reglas de adopción por tipo de proyecto](#5-reglas-de-adopción-por-tipo-de-proyecto)  
6. [Relación con GitLab Flow simplificado basado en main](#6-relación-con-gitlab-flow-simplificado-basado-en-main)  
7. [Pipeline mínimo por tipo de componente](#7-pipeline-mínimo-por-tipo-de-componente)  
8. [Pipeline backend Java](#8-pipeline-backend-java)  
9. [Pipeline frontend Angular](#9-pipeline-frontend-angular)  
10. [Pipeline de contenedores](#10-pipeline-de-contenedores)  
11. [Validaciones de pruebas](#11-validaciones-de-pruebas)  
12. [Validaciones de calidad de código](#12-validaciones-de-calidad-de-código)  
13. [Validaciones de seguridad](#13-validaciones-de-seguridad)  
    - 13.4 [Gestión de variables de pipeline](#134-gestión-de-variables-de-pipeline)  
14. [Validación de contrato OpenAPI](#14-validación-de-contrato-openapi)  
15. [Pruebas de rendimiento en pipeline](#15-pruebas-de-rendimiento-en-pipeline)  
16. [Gestión de artefactos e imágenes](#16-gestión-de-artefactos-e-imágenes)  
17. [Promoción entre ambientes](#17-promoción-entre-ambientes)  
18. [IaC y Terraform en CI/CD](#18-iac-y-terraform-en-cicd)  
19. [Gates y criterios de bloqueo](#19-gates-y-criterios-de-bloqueo)  
20. [Evidencias generadas por pipeline](#20-evidencias-generadas-por-pipeline)  
21. [Aprobaciones manuales](#21-aprobaciones-manuales)  
22. [Responsabilidades](#22-responsabilidades)  
23. [Checklist de conformidad](#23-checklist-de-conformidad)  
24. [Anti-patrones](#24-anti-patrones)  
25. [Proceso ADR para excepciones](#25-proceso-adr-para-excepciones)  
26. [Glosario](#26-glosario)  
27. [Anexos](#27-anexos)  

---

## Estado operacional actual

La adopción de CI/CD en ONP es progresiva. A la fecha de esta versión, las capacidades se interpretan así:

| Capacidad / fase | Estado actual | Observación |
|---|---|---|
| Fase 0 — control manual documentado | Activa | Modelo base vigente en proyectos heredados |
| Fase 1 — CI básico | En piloto | Compilación y pruebas automatizadas por proyecto |
| Fase 2 — calidad y seguridad básica | En piloto | PMD, Checkstyle, reportes y escaneos según madurez |
| Fase 3 — artefactos e imágenes | En piloto | Build y publicación controlada por proyecto o plantilla |
| Fase 4 — entrega controlada e IaC inicial | Pendiente | Depende de cierre operativo de `LIN-IAC-001` |
| Fase 5 — seguridad dinámica y performance | Pendiente | Depende de operacionalización plena de DAST y performance |
| Fase 6 — CD maduro | Pendiente | Depende de promoción de artefactos uniforme y gobernada |
| Fase 7 — operación avanzada | Pendiente | Capacidad objetivo de largo plazo |

**Regla de uso cruzado:** otros lineamientos pueden referenciar sin restricciones las capacidades `Activa` y `En piloto`. Las capacidades `Pendiente` deben tratarse como objetivo de arquitectura, no como requisito operativo ya implantado.

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece el modelo institucional para adoptar integración continua y entrega continua en la ONP, usando GitLab Ultimate como plataforma base para repositorios, Merge Requests, pipelines, artefactos, registros de imágenes, releases y evidencias.

Su objetivo no es imponer despliegue automático total desde la primera etapa, sino definir una ruta progresiva para automatizar controles de compilación, pruebas, calidad, seguridad, empaquetado, publicación de artefactos, construcción de imágenes, validación de infraestructura y promoción controlada entre ambientes.

La adopción de CI/CD en la ONP será progresiva. En una primera etapa, el objetivo es asegurar que todo cambio integrado al repositorio cuente con validaciones mínimas de compilación, pruebas, calidad, seguridad y trazabilidad. La entrega continua se incorporará gradualmente mediante promoción controlada de artefactos e imágenes entre ambientes.

### 1.2 Alcance

Aplica a:

| Elemento | Aplica | Observación |
|---|---:|---|
| Backend Java / Spring Boot | Sí | Maven, pruebas, cobertura, calidad, seguridad, artefacto |
| Frontend Angular | Sí | npm, build, pruebas, calidad, artefacto estático |
| APIs REST | Sí | Validación OpenAPI y contrato |
| Contenedores | Sí | Build, tag, escaneo y publicación de imagen |
| Manifiestos Kubernetes | Sí | Validación y despliegue controlado cuando aplique |
| Scripts de base de datos | Parcial | Versionamiento y trazabilidad; ejecución según proceso vigente |
| Terraform / IaC | Sí, progresivo | Validación fmt/validate/plan; apply controlado según madurez |
| Performance testing | Sí, progresivo | Ejecución de JMeter/k6/Gatling según LIN-PERF-001 |
| DAST | Sí, por criticidad | Según LIN-SEC-APP-001 y directiva de software seguro |
| GitOps | Futuro | Capacidad objetivo, no obligatoria en primera fase |
| Despliegue automático a producción | Futuro | No obligatorio inicialmente |

### 1.3 Fuera de alcance

| Tema | Documento / responsable |
|---|---|
| Estrategia de ramas, MR, releases y tags | LIN-VER-001 |
| Tipos de prueba, cobertura y evidencias funcionales | LIN-TEST-001 |
| Reglas Java, estructura Maven, Checkstyle, PMD | LIN-DEV-JAVA-001 |
| Diseño y contrato REST | LIN-API-REST-001 |
| Seguridad de aplicaciones, SAST/SCA/DAST y secretos | LIN-SEC-APP-001 |
| Contenedores, Dockerfile, K8s, probes y recursos | LIN-K8S-001 |
| Observabilidad, logs, trazas, métricas y health checks | LIN-OBS-001 |
| Performance, JMeter, escenarios, p95/p99 y umbrales | LIN-PERF-001 |
| Estándar completo de Terraform/IaC | LIN-IAC-001 |
| Administración del clúster Kubernetes | Plataforma / Infraestructura |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Diseño y Arquitectura de Software | LIN-ARQ-000 | Define modelo arquitectónico general |
| Versionamiento y Control de Cambios | LIN-VER-001 | Define ramas, MR, tags, releases y trazabilidad |
| Estándar de Desarrollo Java | LIN-DEV-JAVA-001 | Define stack Java, calidad, PMD, Checkstyle y estructura |
| Estándar de APIs REST | LIN-API-REST-001 | Define OpenAPI, contrato REST y API Manager |
| Estándar de Base de Datos Oracle | LIN-BD-ORA-001 | Define scripts, PL/SQL y control de cambios BD |
| Estándar de Frontend Angular | LIN-FE-ANG-001 | Define stack frontend, build y pruebas |
| Log, Trazabilidad y Observabilidad | LIN-OBS-001 | Define métricas, health checks, logs y trazas |
| Seguridad en Aplicaciones | LIN-SEC-APP-001 | Define controles de seguridad, SAST, SCA, DAST, secretos |
| Estándar de Pruebas | LIN-TEST-001 | Define tipos de prueba y evidencias funcionales |
| Contenedores y Orquestación | LIN-K8S-001 | Define imágenes, registry, Trivy, K8s y despliegue |
| Pruebas de Rendimiento, Carga y Estrés | LIN-PERF-001 | Define JMeter, escenarios, métricas y umbrales |
| Infraestructura como Código | LIN-IAC-001 | **Borrador**; dueño del estándar completo de Terraform, repositorio dedicado y fases de madurez |
| Directiva de Desarrollo de Software Seguro | DIR-SEC-SW-001 | Marco superior para controles de seguridad |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **Automatización progresiva** | CI/CD se adopta por fases, sin exigir automatización total desde el inicio. |
| P2 | **GitLab como plataforma base** | GitLab Ultimate será la herramienta institucional para pipelines, artefactos, registry y evidencias. |
| P3 | **Pipeline como evidencia** | Todo pipeline debe generar evidencia trazable asociada a MR, commit, tag o release. |
| P4 | **No duplicar propiedad documental** | CI/CD ejecuta controles definidos por otros lineamientos, pero no los redefine. |
| P5 | **Main siempre verificable** | En proyectos nuevos con GitLab Flow simplificado, `main` debe mantenerse integrable mediante validaciones. |
| P6 | **Mismo artefacto entre ambientes** | No se reconstruye para cada ambiente; se promueve el mismo artefacto o imagen. |
| P7 | **Gates por criticidad** | No todos los controles bloquean desde el día uno; se aplican según fase, criticidad y tipo de sistema. |
| P8 | **Seguridad desde el pipeline** | El pipeline incorpora análisis de secretos, dependencias, código e imágenes. |
| P9 | **Performance por condición** | JMeter/k6/Gatling se ejecutan por criticidad, RNF o release candidate, no en cada commit. |
| P10 | **Aprobación humana donde corresponda** | La automatización no elimina aprobaciones manuales de QA, Seguridad, Plataforma o Arquitectura cuando apliquen. |
| P11 | **Infraestructura validada antes de aplicar** | Terraform debe validarse mediante fmt/validate/plan antes de cualquier apply. |
| P12 | **Producción controlada** | El despliegue automático a producción no es obligatorio inicialmente y requiere madurez, gates y aprobación. |

---

## 4. Modelo de madurez CI/CD ONP

La ONP adopta un modelo de madurez progresivo para CI/CD. Las fases permiten avanzar desde el control manual documentado hasta la entrega continua madura.

| Fase | Nombre | Qué incluye |
|---:|---|---|
| 0 | Control manual documentado | MR, revisión técnica, evidencias manuales, tags/releases |
| 1 | CI básico | Build automático, pruebas unitarias, reporte de resultado |
| 2 | Calidad y seguridad básica | Cobertura, Checkstyle, PMD, SonarQube, SCA, secret scan, OpenAPI validation |
| 3 | Artefactos e imágenes | Build JAR/frontend, build Docker, Trivy, push a GitLab Container Registry |
| 4 | Entrega controlada e IaC inicial | Deploy controlado a DEV/QA, Terraform fmt/validate/plan, aprobación manual |
| 5 | Seguridad dinámica y performance | DAST, JMeter/k6/Gatling, retest de vulnerabilidades, pruebas por release candidate |
| 6 | CD maduro | Promoción por tags/releases, gates automáticos, despliegue controlado a producción |
| 7 | Operación avanzada | GitOps, canary/blue-green, feature flags, plan de reversa automatizado |

### 4.1 Fase 0 — Control manual documentado

Fase cubierta principalmente por `LIN-VER-001`.

Incluye:

- Merge Request obligatorio;
- revisión técnica;
- evidencias manuales de pruebas;
- tags/releases;
- trazabilidad a requerimiento o incidencia;
- revisión de impacto en API, BD, seguridad, K8s y performance.

### 4.2 Fase 1 — CI básico

Incluye:

- compilación automática;
- pruebas unitarias;
- publicación de reporte de ejecución;
- validación mínima por Merge Request.

### 4.3 Fase 2 — Calidad y seguridad básica

Incluye:

- cobertura con JaCoCo;
- Checkstyle;
- PMD;
- CPD;
- SonarQube;
- SCA;
- detección de secretos;
- validación OpenAPI.

### 4.4 Fase 3 — Artefactos e imágenes

Incluye:

- generación de artefacto backend o frontend;
- construcción de imagen;
- etiquetado explícito;
- escaneo con Trivy;
- publicación en GitLab Container Registry.

### 4.5 Fase 4 — Entrega controlada e IaC inicial

Incluye:

- despliegue controlado a DEV o QA;
- aprobaciones manuales;
- validaciones Terraform;
- `terraform plan`;
- publicación de plan como evidencia.

### 4.6 Fase 5 — Seguridad dinámica y performance

Incluye:

- DAST para aplicaciones expuestas o críticas;
- JMeter/k6/Gatling según `LIN-PERF-001`;
- retest de vulnerabilidades;
- evidencia de performance y seguridad por release candidate.

### 4.7 Fase 6 — CD maduro

Incluye:

- promoción controlada por tag/release;
- gates automáticos;
- aprobaciones integradas en GitLab;
- trazabilidad completa desde commit hasta ambiente.

### 4.8 Fase 7 — Operación avanzada

Incluye:

- GitOps;
- canary;
- blue-green;
- progressive delivery;
- feature flags;
- plan de reversa automatizado.

---

## 5. Reglas de adopción por tipo de proyecto

| Tipo de proyecto | Fase mínima | Fase objetivo |
|---|---:|---:|
| Legacy existente sin intervención mayor | 0 | 1 cuando sea viable |
| Proyecto existente con cambios menores | 0 | 1 |
| Proyecto existente con intervención mayor | 1 | 2 o 3 |
| Proyecto nuevo backend/frontend | 1 | 2 |
| Proyecto nuevo contenerizado | 1 | 3 |
| Proyecto nuevo crítico | 2 | 5 |
| Proyecto con datos sensibles o exposición externa | 2 | 5 |
| Proyecto con Terraform/IaC | 4 parcial | 4 o superior |
| Proyecto con despliegue automatizado a producción | 6 | 7 |
| Sistema de alta criticidad institucional | 5 | 6 o 7 |

### 5.1 Regla para proyectos nuevos

Todo proyecto nuevo debe nacer preparado para CI/CD y cumplir, como mínimo, Fase 1.

Si el proyecto es contenerizado, debe preparar la estructura para llegar a Fase 3.

Si el proyecto es crítico, expuesto o maneja datos sensibles, debe planificar Fase 2 y Fase 5.

### 5.2 Regla para proyectos existentes

Los proyectos existentes pueden permanecer temporalmente en Fase 0, pero deben aplicar las reglas de `LIN-VER-001` sobre Merge Requests, revisión técnica, evidencias y releases.

Cuando exista modernización relevante, deben evaluar migración a Fase 1 o superior.

---

## 6. Relación con GitLab Flow simplificado basado en main

`LIN-VER-001` define el modelo objetivo para proyectos nuevos: flujo basado en rama principal `main`, ramas de trabajo cortas, Merge Requests obligatorios, tags/releases y promoción del mismo artefacto.

`LIN-CICD-001` implementa validaciones sobre ese flujo.

### 6.1 Flujo general

```text
feature/*
   ↓ Merge Request
main
   ↓ tag / release
vX.Y.Z
   ↓ build artefacto / imagen
GitLab Package Registry / GitLab Container Registry
   ↓ promoción
DEV → QA → PROD
```

### 6.2 Regla

El pipeline debe ejecutarse, como mínimo:

- al crear o actualizar un Merge Request;
- al hacer merge a `main`;
- al crear un tag/release;
- al solicitar despliegue a un ambiente;
- al ejecutar validaciones manuales o programadas de seguridad/performance.

---

## 7. Pipeline mínimo por tipo de componente

### 7.1 Backend Java

Pipeline mínimo progresivo:

```text
validate
compile
unit-test
integration-test
coverage
quality
security
package
image-build
image-scan
publish
deploy-dev
```

### 7.2 Frontend Angular

Pipeline mínimo progresivo:

```text
install
lint
unit-test
build
quality
security
package
image-build
image-scan
publish
deploy-dev
```

### 7.3 Contenedor

```text
docker-build
image-tag
trivy-scan
push-registry
```

### 7.4 Terraform / IaC

```text
terraform-fmt
terraform-validate
terraform-plan
manual-approval
terraform-apply
```

### 7.5 Performance

```text
performance-smoke
performance-load
publish-report
```

---

## 8. Pipeline backend Java

### 8.1 Etapas sugeridas

| Etapa | Herramienta | Obligación |
|---|---|---|
| Compile | Maven | Fase 1 |
| Unit tests | JUnit 5 / Mockito | Fase 1 |
| Integration tests | Spring Boot Test / Testcontainers | Fase 2 según proyecto |
| Coverage | JaCoCo | Fase 2 |
| Style | Checkstyle | Fase 2 |
| Static analysis | PMD | Fase 2 |
| Duplication | PMD CPD | Fase 2 |
| Quality gate | SonarQube | Fase 2 |
| SCA | OWASP Dependency-Check | Fase 2 |
| Package | Maven | Fase 1 |
| Docker build | Docker / Buildah / Kaniko según plataforma | Fase 3 |
| Image scan | Trivy | Fase 3 |

### 8.2 Comandos referenciales

```bash
mvn -B clean verify
mvn -B checkstyle:check
mvn -B pmd:check
mvn -B pmd:cpd-check
mvn -B jacoco:report
```

### 8.3 Regla PMD

PMD se ejecuta como validación complementaria de calidad Java. No reemplaza a Checkstyle ni SonarQube.

| Herramienta | Propósito |
|---|---|
| Checkstyle | Convenciones, estilo, formato y nomenclatura |
| PMD | Malas prácticas, complejidad, código muerto |
| CPD | Duplicación de código |
| SonarQube | Calidad consolidada, deuda técnica y tendencias |

### 8.4 Tratamiento de fallos PMD

| Fase | Tratamiento |
|---|---|
| Fase 2 inicial | PMD reporta hallazgos y bloquea reglas críticas |
| Fase 2 madura | PMD bloquea violaciones de reglas institucionales |
| Fase 3+ | PMD/CPD forman parte del quality gate |

---

## 9. Pipeline frontend Angular

### 9.1 Etapas sugeridas

| Etapa | Herramienta | Obligación |
|---|---|---|
| Install | npm ci | Fase 1 |
| Build | Angular CLI | Fase 1 |
| Unit tests | Jest o Karma/Jasmine | Fase 1 |
| E2E | Playwright preferente | Fase 2/3 según criticidad |
| Lint | ESLint | Fase 2 |
| SCA | npm audit / herramienta aprobada | Fase 2 |
| Package | Artefacto estático | Fase 1 |
| Docker build | Nginx u runtime web aprobado | Fase 3 |
| Image scan | Trivy | Fase 3 |

### 9.2 Comandos referenciales

```bash
npm ci
npm run build
npm test -- --watch=false
npm run lint
npx playwright test
```

### 9.3 E2E

Las pruebas E2E no deben ejecutarse obligatoriamente en cada commit para todos los proyectos. Se recomienda:

| Caso | E2E |
|---|---|
| Flujo crítico | Obligatorio antes de QA/Producción |
| Frontend nuevo | Recomendado |
| Cambio visual menor | No obligatorio |
| Release candidate | Recomendado/obligatorio según criticidad |

---

## 10. Pipeline de contenedores

### 10.1 Etapas

| Etapa | Descripción |
|---|---|
| build image | Construye imagen desde Dockerfile aprobado |
| tag image | Etiqueta con versión, commit o release |
| scan image | Ejecuta Trivy o equivalente |
| push image | Publica en GitLab Container Registry |
| promote image | Promueve la misma imagen entre ambientes |

### 10.2 Regla de imagen

No se debe reconstruir una imagen distinta para cada ambiente. La misma imagen debe promoverse de DEV a QA y PROD, cambiando solo configuración externa.

### 10.3 Tags

Tags permitidos:

```text
1.2.0
1.2.0-rc.1
1.2.0-dev.45
<commit-sha>
```

Prohibido para QA/PROD:

```text
latest
```

### 10.4 Escaneo de imágenes

| Severidad | Regla sugerida |
|---|---|
| Critical | Bloquea pase salvo excepción de Seguridad |
| High | Requiere evaluación y plan de remediación |
| Medium | Registrar y corregir según planificación |
| Low | Monitorear |

---

## 11. Validaciones de pruebas

`LIN-TEST-001` define la estrategia de pruebas. `LIN-CICD-001` define su ejecución automática o semiautomática.

### 11.1 Pruebas backend

| Tipo | Herramienta | Pipeline |
|---|---|---|
| Unitarias | JUnit 5 / Mockito | Fase 1 |
| Integración | Spring Boot Test / Testcontainers | Fase 2 |
| Contrato | OpenAPI validator / Pact / Spring Cloud Contract | Fase 2/3 |
| Caracterización | JUnit + BD controlada | Según legacy |
| Cobertura | JaCoCo | Fase 2 |

### 11.2 Pruebas frontend

| Tipo | Herramienta | Pipeline |
|---|---|---|
| Unitarias | Jest o Karma/Jasmine | Fase 1 |
| E2E | Playwright | Fase 2/3 |
| Accesibilidad básica | Playwright / axe / herramienta aprobada | Fase 2/3 |
| Build | Angular CLI | Fase 1 |

### 11.3 Publicación de reportes

El pipeline debe publicar, según aplique:

- resultados de pruebas unitarias;
- resultados de integración;
- reporte de cobertura;
- reporte E2E;
- evidencias de fallos;
- logs de pipeline.

---

## 12. Validaciones de calidad de código

### 12.1 Backend Java

| Validación | Herramienta |
|---|---|
| Estilo y convenciones | Checkstyle |
| Malas prácticas | PMD |
| Duplicación | CPD |
| Cobertura | JaCoCo |
| Calidad consolidada | SonarQube |

### 12.2 Frontend Angular

| Validación | Herramienta |
|---|---|
| Lint | ESLint |
| Build estricto | Angular CLI |
| Calidad consolidada | SonarQube, si aplica |
| Dependencias | npm audit |

### 12.3 Quality gate

El quality gate debe considerar progresivamente:

- build exitoso;
- pruebas exitosas;
- cobertura mínima;
- ausencia de violaciones críticas;
- ausencia de duplicación crítica;
- deuda técnica controlada;
- no reducción significativa de cobertura;
- no incremento injustificado de issues críticos.

---

## 13. Validaciones de seguridad

`LIN-SEC-APP-001` es el dueño de los controles de seguridad. CI/CD ejecuta y publica evidencias.

### 13.1 Controles

| Control | Herramienta sugerida | Fase |
|---|---|---:|
| Secret scanning | GitLab Secret Detection / equivalente | 2 |
| SAST | GitLab SAST / SonarQube / equivalente | 2 |
| SCA | OWASP Dependency-Check / GitLab Dependency Scanning | 2 |
| Container scanning | Trivy / GitLab Container Scanning | 3 |
| DAST | GitLab DAST / OWASP ZAP / herramienta aprobada | 5 |
| Retest | Herramienta según hallazgo | 5 |

### 13.2 DAST

DAST no debe ejecutarse necesariamente en cada commit.

Se recomienda ejecutar DAST:

- antes de producción;
- sobre release candidate;
- en aplicaciones expuestas;
- en APIs críticas;
- en sistemas con datos sensibles;
- por solicitud de Seguridad Digital.

### 13.3 Criterio de bloqueo

| Hallazgo | Tratamiento |
|---|---|
| Crítico | Bloquea pase salvo excepción formal |
| Alto | Requiere remediación o plan aprobado |
| Medio | Registrar y planificar |
| Bajo | Monitorear |

### 13.4 Gestión de variables de pipeline

Las variables sensibles del pipeline (tokens, credenciales, URLs privadas) deben configurarse como variables protegidas y/o enmascaradas en **GitLab CI/CD Settings → Variables**. Nunca como valores literales en `.gitlab-ci.yml`.

#### Tipos de variable GitLab

| Tipo | Cuándo usar |
|---|---|
| **Protegida** | Solo disponible en ramas o tags protegidos (`main`, tags de release) |
| **Enmascarada** | No aparece en logs de pipeline — usar para tokens y contraseñas |
| **Protegida + enmascarada** | Valor por defecto para cualquier credencial de QA o PROD |

#### Alcance

| Nivel | Cuándo usar |
|---|---|
| Grupo GitLab | Variables comunes a múltiples proyectos del mismo sistema (ej. `SONAR_HOST_URL`, URL del registry) |
| Proyecto GitLab | Variables específicas de un proyecto (ej. token de despliegue de ese servicio) |

#### Convenciones de nombre

| Convención | Uso |
|---|---|
| `TF_VAR_*` | Variables inyectadas automáticamente en Terraform como variables de input |
| `*_TOKEN` | Tokens de autenticación (`SONAR_TOKEN`, `DEPLOY_TOKEN`) |
| `*_URL` | URLs de servicios (`SONAR_HOST_URL`, `REGISTRY_URL`) |
| `*_PASSWORD`, `*_SECRET` | Credenciales — siempre protegidas y enmascaradas |

#### Reglas

| Regla | Estado |
|---|---|
| No incluir secretos en `.gitlab-ci.yml` | Obligatorio |
| No incluir credenciales en variables CI sin enmascarar | Obligatorio |
| Usar variables protegidas para credenciales de QA/PROD | Obligatorio |
| Usar variables de grupo para valores compartidos entre proyectos | Recomendado |
| No imprimir variables enmascaradas con `echo` en scripts de pipeline | Obligatorio |
| Rotar variables según política de **LIN-SEC-APP-001 12** | Obligatorio |

> La definición de qué constituye un secreto, las prohibiciones absolutas y la política de rotación se rigen por **LIN-SEC-APP-001 12**. Esta sección define únicamente la mecánica de configuración de variables en GitLab CI/CD.
>
> Las variables de runtime de la aplicación (credenciales de BD, tokens SAA, claves de integración) se inyectan en pods mediante Kubernetes Secrets — ver **LIN-K8S-001 8**. Las variables de esta sección son exclusivamente del contexto de ejecución del pipeline.

---

## 14. Validación de contrato OpenAPI

`LIN-API-REST-001` define el contrato API. CI/CD debe validar que el contrato esté presente y sea consistente.

### 14.1 Validaciones mínimas

| Validación | Aplicabilidad |
|---|---|
| Existe `openapi.yml` o `openapi.json` | APIs REST |
| El archivo es válido | APIs REST |
| Versionamiento de API definido | APIs públicas o compartidas |
| Cambios incompatibles identificados | APIs consumidas por terceros |
| Documentación actualizada | APIs REST |

### 14.2 Cambios incompatibles

Los cambios incompatibles deben requerir:

- ADR;
- cambio de versión;
- comunicación a consumidores;
- evidencia de pruebas de contrato cuando aplique.

---

## 15. Pruebas de rendimiento en pipeline

`LIN-PERF-001` define escenarios, herramientas, métricas, umbrales y criterios. `LIN-CICD-001` define ejecución y publicación de reportes.

### 15.1 Herramientas

| Herramienta | Uso |
|---|---|
| JMeter | Preferente institucional |
| k6 | Alternativa para APIs modernas y CI/CD |
| Gatling | Alternativa para equipos Java y pruebas como código |

### 15.2 Cuándo ejecutar

| Caso | Performance en pipeline |
|---|---|
| Cada commit | No obligatorio |
| Merge Request común | No obligatorio |
| Release candidate crítico | Obligatorio |
| Sistema con RNF de performance | Obligatorio |
| API de alto consumo | Obligatorio |
| Sistema expuesto o misional | Obligatorio según criticidad |
| Cambio visual/documental | No aplica |

### 15.3 Publicación de reportes

El pipeline debe publicar o enlazar:

- reporte JMeter/k6/Gatling;
- métricas promedio, p95, p99, TPS, error rate;
- dashboard Grafana si aplica;
- logs o trazas relevantes;
- conclusión de cumplimiento.

---

## 16. Gestión de artefactos e imágenes

### 16.1 Artefactos

Los artefactos generados por pipeline pueden incluir:

- JAR;
- archivos estáticos frontend;
- reportes de pruebas;
- reportes de cobertura;
- reportes de seguridad;
- planes Terraform;
- paquetes de despliegue.

### 16.2 Imágenes

Las imágenes deben publicarse en GitLab Container Registry o registro institucional aprobado.

Cada imagen debe estar asociada a:

- commit;
- tag;
- release;
- pipeline;
- proyecto;
- fecha;
- responsable.

### 16.3 Retención

La política de retención debe ser definida por Plataforma en coordinación con Arquitectura, considerando:

- imágenes de release;
- imágenes temporales;
- artefactos de pipeline;
- reportes de seguridad;
- reportes de performance.

---

## 17. Promoción entre ambientes

### 17.1 Principio

La promoción entre ambientes debe usar el mismo artefacto o imagen.

```text
DEV → QA → PROD
```

La diferencia entre ambientes se gestiona mediante configuración externa, secrets y parámetros aprobados.

### 17.2 Ambientes

| Ambiente | Tratamiento |
|---|---|
| DEV | Despliegue automático o semiautomático permitido |
| PQA | Despliegue controlado si existe |
| QA | Despliegue con evidencias mínimas |
| PROD | Requiere aprobación y controles según criticidad |

### 17.3 Producción

El despliegue automático a producción no es obligatorio inicialmente.

Para habilitarlo se requiere:

- CI/CD Fase 6 o superior;
- gates automáticos;
- aprobaciones configuradas;
- plan de reversa definido;
- monitoreo activo;
- evidencia de pruebas;
- evidencia de seguridad;
- validación de Arquitectura/Plataforma/Seguridad según criticidad.

---

## 18. IaC y Terraform en CI/CD (Validarlo con AD)

### 18.1 Estado

Terraform es la herramienta institucional para Infraestructura como Código. `LIN-IAC-001` es el dueño del estándar completo (en borrador).

`LIN-CICD-001` define validaciones mínimas para el repositorio de IaC. El pipeline de Terraform vive en el repositorio dedicado `oti-plataforma/infrastructure-iac`, no en los repositorios de aplicación — los equipos de desarrollo no deben agregar jobs de Terraform en sus propios pipelines.

### 18.2 Validaciones mínimas

| Validación | Fase |
|---|---:|
| terraform fmt | 4 |
| terraform validate | 4 |
| terraform plan | 4 |
| publicación del plan | 4 |
| aprobación manual | 4 |
| terraform apply | 4/6 según ambiente y madurez |

### 18.3 Producción

`terraform apply` automático a producción no es obligatorio inicialmente.

Debe requerir:

- aprobación manual;
- revisión del plan;
- control de estado remoto;
- seguridad de secretos;
- trazabilidad;
- plan de reversa o reversión documentada;
- validación de Plataforma.

---

## 19. Gates y criterios de bloqueo

### 19.1 Gate por fase

| Fase | Gate mínimo |
|---:|---|
| 1 | Build y pruebas unitarias exitosas |
| 2 | Calidad, cobertura, SAST/SCA y secretos |
| 3 | Imagen construida, escaneada y publicada |
| 4 | Plan IaC validado y despliegue controlado |
| 5 | DAST/performance según criticidad |
| 6 | Gates automáticos integrados |
| 7 | Operación avanzada y plan de reversa automatizado |

### 19.2 Criterios de bloqueo sugeridos

| Criterio | Bloquea |
|---|---|
| Build falla | Sí |
| Pruebas unitarias fallan | Sí |
| Cobertura menor al umbral | Según fase |
| PMD crítico | Sí desde Fase 2 madura |
| CPD duplicación crítica | Según umbral |
| SonarQube quality gate falla | Según fase |
| Secret detectado | Sí |
| Vulnerabilidad crítica | Sí salvo excepción |
| Imagen con CVE crítico | Sí salvo excepción |
| OpenAPI inválido | Sí para APIs REST |
| DAST crítico | Sí salvo excepción |
| Performance no cumple RNF crítico | Sí salvo excepción |
| Terraform validate falla | Sí |
| Terraform plan no aprobado | Sí para apply |

### 19.3 Excepciones

Todo gate bloqueante puede tener excepción solo si existe:

- justificación;
- riesgo aceptado;
- control compensatorio;
- fecha de remediación;
- aprobación del responsable correspondiente.

---

## 20. Evidencias generadas por pipeline

El pipeline debe generar o enlazar evidencias como:

| Evidencia | Fase |
|---|---:|
| Resultado de build | 1 |
| Resultado de pruebas | 1 |
| Reporte de cobertura | 2 |
| Reporte Checkstyle | 2 |
| Reporte PMD/CPD | 2 |
| SonarQube quality gate | 2 |
| SCA/dependencias | 2 |
| Secret scan | 2 |
| Imagen construida | 3 |
| Reporte Trivy | 3 |
| Artefacto publicado | 3 |
| Terraform plan | 4 |
| Reporte DAST | 5 |
| Reporte JMeter/k6/Gatling | 5 |
| Aprobaciones | 4+ |
| Release notes | 6 |

---

## 21. Aprobaciones manuales

### 21.1 Cuándo usar aprobaciones manuales

| Caso | Aprobación |
|---|---|
| Despliegue a QA | QA / líder técnico |
| Despliegue a PROD | responsable de pase / Plataforma / Seguridad según criticidad |
| Hallazgo crítico aceptado temporalmente | Seguridad Digital |
| Excepción arquitectónica | Arquitectura |
| Apply Terraform | Plataforma |
| Performance no cumplida con observaciones | Arquitectura + proyecto |
| DAST con hallazgos | Seguridad Digital |

### 21.2 Regla

Las aprobaciones manuales deben quedar registradas en GitLab o en el expediente técnico correspondiente.

---

## 22. Responsabilidades

| Rol | Responsabilidad |
|---|---|
| Desarrollo | Configurar scripts de build/test, corregir fallos, mantener pipeline del proyecto |
| QA | Validar evidencias de pruebas, E2E y performance cuando aplique |
| Arquitectura | Definir lineamientos, revisar excepciones, validar cumplimiento técnico |
| Plataforma/Infraestructura | Administrar runners, registry, ambientes, permisos y despliegues |
| Seguridad Digital | Validar SAST/SCA/DAST, secretos, vulnerabilidades y excepciones |
| DBA / responsable BD | Revisar scripts y cambios críticos de base de datos |
| Líder técnico | Asegurar que el pipeline sea usado y que los gates se cumplan |
| Proyecto | Garantizar que evidencias estén listas para pase |

---

## 23. Checklist de conformidad

### 23.1 Proyecto

```text
[ ] Proyecto usa GitLab como repositorio oficial
[ ] Tiene Merge Requests obligatorios
[ ] Tiene ramas protegidas según LIN-VER-001
[ ] Tiene README con instrucciones de build
[ ] Tiene scripts de build reproducibles
[ ] Tiene estructura preparada para pipeline
```

### 23.2 CI básico

```text
[ ] Build automático configurado
[ ] Pruebas unitarias ejecutadas
[ ] Reporte de resultado publicado
[ ] Pipeline se ejecuta en MR
[ ] Pipeline se ejecuta en main
```

### 23.3 Calidad y seguridad

```text
[ ] JaCoCo configurado si aplica
[ ] Checkstyle configurado si aplica
[ ] PMD configurado si aplica
[ ] CPD configurado si aplica
[ ] SonarQube configurado o planificado
[ ] SCA configurado o planificado
[ ] Secret scan configurado o planificado
```

### 23.4 Contenedores

```text
[ ] Dockerfile cumple LIN-K8S-001
[ ] Imagen usa tag explícito
[ ] Imagen no usa latest en QA/PROD
[ ] Imagen es escaneada
[ ] Imagen se publica en registry institucional
```

### 23.5 Avanzado

```text
[ ] DAST definido por criticidad
[ ] Performance definido por LIN-PERF-001
[ ] Terraform fmt/validate/plan si aplica
[ ] Aprobaciones manuales configuradas
[ ] Evidencias enlazadas a release
```

---

## 24. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Pipeline solo decorativo | Falsa seguridad | Debe generar evidencia útil |
| Desplegar sin build reproducible | No hay trazabilidad | Build por pipeline |
| Desplegar imagen local | No hay control | Usar registry institucional |
| Usar latest en QA/PROD | No hay plan de reversa confiable | Prohibido |
| Saltarse pruebas por urgencia | Riesgo productivo | Requiere excepción |
| Ejecutar performance en cada commit | Pipelines lentos e ineficientes | Ejecutar por criticidad |
| Hacer DAST en cada commit | Pipelines lentos | Ejecutar por release/criticidad |
| Terraform apply sin plan aprobado | Riesgo infraestructura | Plan + aprobación |
| No publicar reportes | No hay evidencia | Publicar artefactos |
| Mezclar configuración por ambiente en código | Rompe promoción | Configuración externa |
| Despliegue automático a producción sin gates | Riesgo operativo | Requiere madurez |
| Ignorar hallazgos críticos | Riesgo seguridad | Bloquea salvo excepción |

---

## 25. Proceso ADR para excepciones

Toda excepción relevante a este lineamiento requiere ADR aprobado por Arquitectura. Si afecta seguridad, producción, infraestructura, datos sensibles o continuidad operativa, requiere validación adicional de Seguridad Digital y/o Plataforma.

### 25.1 Casos que requieren ADR

- No implementar CI básico en proyecto nuevo.
- No ejecutar PMD/Checkstyle/SonarQube donde aplique.
- Aceptar vulnerabilidad crítica temporalmente.
- Publicar imagen sin escaneo.
- Usar `latest` en QA/PROD.
- Desplegar a producción sin gates mínimos.
- Ejecutar Terraform apply sin plan aprobado.
- No ejecutar performance en sistema crítico.
- No ejecutar DAST en sistema expuesto.
- Usar herramienta alternativa no aprobada.

### 25.2 Formato mínimo

```markdown
# ADR-CICD-NNN — [Título]

## Contexto
[Descripción de la restricción o excepción requerida]

## Decisión
[Qué se permitirá excepcionalmente]

## Riesgo aceptado
[Riesgo operativo, seguridad, continuidad o arquitectura]

## Control compensatorio
[Medida temporal o alternativa]

## Fecha de revisión
[Fecha para reevaluar]

## Aprobaciones
[Arquitectura / Plataforma / Seguridad / QA, según corresponda]
```

---

## 26. Glosario

| Término | Definición |
|---|---|
| CI | Integración continua; automatización de build, pruebas y validaciones |
| CD | Entrega/despliegue continuo; promoción automatizada o controlada de artefactos |
| Pipeline | Secuencia automatizada de etapas en GitLab |
| Runner | Agente que ejecuta jobs de GitLab CI/CD |
| Job | Unidad de ejecución dentro de un pipeline |
| Stage | Agrupación lógica de jobs |
| Gate | Criterio que permite o bloquea avance |
| Artifact | Archivo generado por pipeline |
| Registry | Repositorio de imágenes de contenedor |
| SAST | Análisis estático de seguridad |
| SCA | Análisis de dependencias y componentes |
| DAST | Análisis dinámico de seguridad |
| PMD | Analizador estático Java para malas prácticas |
| CPD | Detector de código duplicado de PMD |
| Trivy | Herramienta de escaneo de imágenes y dependencias |
| JMeter | Herramienta preferente de pruebas de rendimiento |
| Terraform | Herramienta objetivo para infraestructura como código |
| GitOps | Modelo de operación donde Git es fuente de verdad para despliegue |
| Canary | Despliegue gradual a una fracción de usuarios/tráfico |
| Blue-green | Estrategia de despliegue con dos entornos equivalentes |
| Feature flag | Mecanismo para activar/desactivar funcionalidad sin cambiar código desplegado |

---

## 27. Anexos

### Anexo A — Pipeline referencial backend Java

```yaml
stages:
  - build
  - test
  - quality
  - security
  - package

build:
  stage: build
  script:
    - mvn -B clean compile

unit_test:
  stage: test
  script:
    - mvn -B test
  artifacts:
    when: always
    reports:
      junit: target/surefire-reports/*.xml

quality:
  stage: quality
  script:
    - mvn -B checkstyle:check
    - mvn -B pmd:check
    - mvn -B pmd:cpd-check
    - mvn -B jacoco:report
  artifacts:
    when: always
    paths:
      - target/site/

package:
  stage: package
  script:
    - mvn -B package -DskipTests
  artifacts:
    paths:
      - target/*.jar
```

### Anexo B — Pipeline referencial frontend Angular

```yaml
stages:
  - install
  - test
  - build

install:
  stage: install
  script:
    - npm ci

unit_test:
  stage: test
  script:
    - npm test -- --watch=false

build:
  stage: build
  script:
    - npm run build
  artifacts:
    paths:
      - dist/
```

### Anexo C — Pipeline referencial de imagen

```yaml
stages:
  - image-build
  - image-scan
  - image-push

variables:
  IMAGE_TAG: "$CI_COMMIT_SHORT_SHA"

image_build:
  stage: image-build
  script:
    - docker build -t "$CI_REGISTRY_IMAGE:$IMAGE_TAG" .

image_scan:
  stage: image-scan
  script:
    - trivy image "$CI_REGISTRY_IMAGE:$IMAGE_TAG"

image_push:
  stage: image-push
  script:
    - docker push "$CI_REGISTRY_IMAGE:$IMAGE_TAG"
```

### Anexo D — Pipeline referencial Terraform

```yaml
stages:
  - validate
  - plan
  - apply

terraform_fmt:
  stage: validate
  script:
    - terraform fmt -check -recursive

terraform_validate:
  stage: validate
  script:
    - terraform init -backend=false
    - terraform validate

terraform_plan:
  stage: plan
  script:
    - terraform init
    - terraform plan -out=tfplan
  artifacts:
    paths:
      - tfplan

terraform_apply:
  stage: apply
  when: manual
  script:
    - terraform apply tfplan
```

### Anexo E — Pipeline referencial JMeter

```yaml
stages:
  - performance

performance_jmeter:
  stage: performance
  when: manual
  script:
    - jmeter -n -t performance/jmeter/escenarios/consulta-expediente.jmx -l results.jtl -e -o performance-report
  artifacts:
    when: always
    paths:
      - results.jtl
      - performance-report/
```

### Anexo F — Variables sugeridas de pipeline

```text
APP_NAME
APP_VERSION
IMAGE_TAG
ENVIRONMENT
SONAR_HOST_URL
SONAR_TOKEN
TRIVY_SEVERITY
OPENAPI_FILE
JMETER_SCENARIO
TERRAFORM_WORKSPACE
```

### Anexo G — Estructura sugerida de proyecto automatizable

```text
.
├── .gitlab-ci.yml
├── README.md
├── Dockerfile
├── pom.xml / package.json
├── docs/
│   ├── adr/
│   └── openapi/
├── k8s/
│   ├── base/
│   └── overlays/
├── performance/
│   └── jmeter/
├── terraform/
└── db/
    ├── migration/
    └── reverse/
```
