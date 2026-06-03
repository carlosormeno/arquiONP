# Matriz de Propiedad Documental — ONP

**Código:** GOB-MAT-001  
**Versión:** 0.1.2  
**Fecha:** 2026-05-28  
**Autor:** OTI — Oficina de Tecnologías de la Información  
**Estado:** Borrador de trabajo interno  

---

## Propósito

Este documento define qué lineamiento es la **fuente autoritativa** de cada tema técnico y qué lineamientos son consumidores (implementan o referencian, pero no redefinen). Su objetivo es evitar duplicidad, contradicciones y ambigüedad sobre dónde está la regla oficial.

**Principio rector:**

> Cada tema tiene un único dueño. Los demás documentos pueden mencionar o implementar ese tema, pero nunca redefinirlo. Si necesitan precisar algo propio de su dominio, lo hacen referenciando al dueño y agregando solo la parte específica a su contexto.

**Última validación integral del corpus:** 2026-05-28

---

## Catálogo de documentos y códigos

| Código | Documento | Estado | Archivo |
|---|---|---|---|
| `LIN-ARQ-000` | Marco Rector de Diseño y Arquitectura de Software | Borrador | `arquitectura/Lineamiento_Diseno_Arquitectura_Software_ONP_v0.1.0.md` |
| `LIN-API-REST-001` | Estándar de Servicios Web y APIs REST | Borrador | `Web/Lineamiento_Estandar_APIs_REST_ONP_v0.1.0.md` |
| `LIN-DEV-JAVA-001` | Estándar de Desarrollo Java | Borrador | `desarrollo/Lineamiento_Estandar_Desarrollo_Java_ONP_v0.1.0.md` |
| `LIN-BD-ORA-001` | Estándar de Base de Datos Oracle | Borrador | `Datos/Lineamiento_Estandar_Base_de_Datos_ONP_v0.1.0.md` |
| `LIN-FE-ANG-001` | Estándar de Diseño Web Frontend Angular | Borrador | `Web/Lineamiento_Estandar_Diseno_Web_Frontend_ONP_v0.1.0.md` |
| `LIN-OBS-001` | Lineamiento de Log Centralizado, Trazabilidad y Observabilidad | **Borrador** | `observabilidad/Lineamiento_Log_Trazabilidad_Observabilidad_ONP_v0.1.0.md` |
| `LIN-SEC-APP-001` | Estándar de Seguridad en Aplicaciones | **Borrador** | `seguridad/Lineamiento_Seguridad_Aplicaciones_ONP_v0.1.0.md` |
| `LIN-TEST-001` | Estándar de Pruebas | **Borrador** | `pruebas/Lineamiento_Estandar_Pruebas_ONP_v0.1.0.md` |
| `LIN-CICD-001` | Estándar de CI/CD | Borrador | `CICD/Lineamiento_Integracion_Entrega_Continua_ONP_v0.1.0.md` |
| `LIN-K8S-001` | Estándar de Contenedores y Orquestación | Borrador | `contenedores/Lineamiento_Contenedores_Orquestacion_ONP_v0.1.0.md` |
| `LIN-IAC-001` | Estándar de Infraestructura como Código | **Borrador** | `Infraestructura/Lineamiento_Infraestructura_Código_ONP_v0.1.0.md` |
| `LIN-BUS-001` | Estándar de Bus de Eventos | **Pendiente** | — |
| `LIN-VER-001` | Estándar de Versionamiento y Control de Cambios | Borrador v0.1.5 | `versionamiento/Lineamiento_Versionamiento_Control_Cambios_ONP_v0.1.1.md` |
| `LIN-PERF-001` | Estándar de Pruebas de Rendimiento, Carga y Estrés | Borrador | `pruebas/Lineamiento_Pruebas_Rendimiento_Carga_Estres_ONP_v0.1.0.md` |
| `LIN-DOC-001` | Estándar de Documentación y Modelado | **Pendiente** | — |
| `GLOSARIO-ONP` | Glosario transversal operativo | Borrador operativo | `GLOSARIO_ONP.md` |

---

## Matriz de propiedad por tema

**Estados:**
- **Conforme** — propiedad clara, sin inconsistencias entre documentos.
- **Resuelto** — existía un conflicto o brecha; corregido en la versión actual.
- **Pendiente** — requiere acción: corrección en documento existente o creación de lineamiento propietario.
- **En borrador** — lineamiento propietario existe pero no está finalizado; contenido provisional.
- **Requiere ADR** — desviación que necesita excepción formal aprobada por Arquitectura.

**Regla editorial obligatoria para los estados:**

- `Conforme` solo puede usarse cuando el documento dueño y los consumidores relevantes están alineados en la versión vigente revisada.
- `Resuelto` solo puede usarse cuando el conflicto fue efectivamente corregido y la observación identifica la evidencia mínima de cierre.
- `En borrador` no equivale a inconsistencia; indica que el documento dueño existe, pero su contenido aún puede cambiar.
- Si existe duda razonable sobre la sincronización real, se debe usar `En borrador` o `Pendiente`, no `Conforme`.

---

### 1. Contrato de respuesta estándar

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Definición de `ApiResponseWrapper<T>` | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Resuelto | `LIN-DEV-JAVA-001` sección 11.4.4 corregido: genérico `T data`, `CampoError` con `campo`/`mensaje` en español |
| Códigos `codDetRespuesta` (tabla 000–502) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Resuelto | `LIN-API-REST-001` sección 4.2.1 define dueño normativo, persistencia opcional y proceso de alta/cambio |
| Códigos HTTP por tipo de operación | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Resuelto | POST exitoso corregido a `201 Created` en `LIN-API-REST-001` y `LIN-DEV-JAVA-001` |
| Implementación Spring Boot de `ApiResponseWrapper` | `LIN-DEV-JAVA-001` | — | Resuelto | `LIN-DEV-JAVA-001` sección 11.4.4 referencia `LIN-API-REST-001` como fuente del contrato |
| Modelo TypeScript `ApiResponse<T>`, `ApiError`, `ApiMeta` | `LIN-FE-ANG-001` | — | Resuelto | `LIN-FE-ANG-001` corregido: `campo`/`mensaje`; `ApiMeta` con `timestamp`, `requestId`, `version` |

---

### 2. Diseño de APIs REST

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Convenciones de URL (`kebab-case`, recursos en plural) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001` | Conforme | — |
| Versionamiento en URL (`/api/v1/`) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Conforme | — |
| Paginación — campos y estructura del response | `LIN-API-REST-001` | `LIN-FE-ANG-001` | Resuelto | `LIN-FE-ANG-001` corregido: `pagina`/`tamanio` alineados con contrato REST |
| Operaciones no CRUD (verbos en URL) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001` | Conforme | — |
| Rate limiting y throttling | `LIN-API-REST-001` | `LIN-K8S-001`, `LIN-SEC-APP-001` | En borrador | Definición en `LIN-SEC-APP-001` y `LIN-K8S-001` (ambos en borrador) |
| API Gateway y API Manager — plataforma WSO2 | `LIN-API-REST-001` | `LIN-ARQ-000`, `LIN-K8S-001`, `LIN-SEC-APP-001` | En borrador | `LIN-API-REST-001` secciones 2.5 y 10.3 definen plataforma y gate de publicación. La transición se documenta además en `arquitectura/ADR-WSO2-001.md` |

---

### 3. Documentación OpenAPI / Swagger

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Swagger como requisito obligatorio de entrega | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-CICD-001` | Conforme | — |
| Anotaciones `@Tag`, `@Operation`, `@ApiResponse` | `LIN-DEV-JAVA-001` | — | Conforme | Solo en `@RestController`; ver `LIN-DEV-JAVA-001` sección 11.4 |
| Configuración `OpenApiConfig` (bean Spring) | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Swagger deshabilitado por defecto en producción | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001`, `LIN-CICD-001` | Resuelto | `LIN-DEV-JAVA-001` sección 11.4.3 corregido a formato `.yml` en todos los entornos |

---

### 4. Observabilidad

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Observabilidad como requisito obligatorio de producción | `LIN-ARQ-000` | Todos | Conforme | — |
| Formato de log JSON, campos mínimos obligatorios (ECS) | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001`, `LIN-K8S-001` | Resuelto | `LIN-OBS-001` [sección 6](#6-estructura-de-proyecto-java) y Apéndice B son la fuente oficial |
| Implementación Java: `logback-spring.xml`, `LogstashEncoder`, `OpenTelemetryLogbackConfig` | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` secciones 4.6–4.7; `LIN-DEV-JAVA-001` [sección 8](#8-base-de-datos) referencia `LIN-OBS-001` |
| `traceId`, `spanId`, OpenTelemetry, Micrometer Tracing | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001` | Resuelto | `LIN-OBS-001` [sección 5](#5-autenticacion-y-seguridad) absorbe Guía v0.1.1 [sección 1](#1-contrato-de-respuesta-estandar) |
| Header `X-Request-ID` — definición y uso en el contrato REST | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001`, `LIN-OBS-001` | Conforme | — |
| `RequestIdFilter` — implementación Spring Boot | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 4.10; pone `http.request.id` en MDC |
| `CanonicalRequestLogFilter` — log canónico de request | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 4.9 |
| `Mask.java` — utilidad No PII | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 4.8 |
| Nivel de log por entorno (`pe.gob.onp.*` → INFO) | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 6.1 y `logback-spring.xml` sección 4.6; `LIN-DEV-JAVA-001` [sección 8](#8-base-de-datos) referencia |
| Métricas mínimas, Prometheus, Grafana | `LIN-OBS-001` | `LIN-K8S-001`, `LIN-CICD-001` | Resuelto | `LIN-OBS-001` [sección 8](#8-base-de-datos) define Actuator, métricas y dashboards mínimos |
| Retención de datos (DEV/QA: 30 días, PROD: 90 días) | `LIN-OBS-001` | `LIN-K8S-001` | Resuelto | `LIN-OBS-001` sección 9.2 |
| Health checks `liveness` / `readiness` (probes K8s) | `LIN-K8S-001` | `LIN-DEV-JAVA-001`, `LIN-OBS-001` | En borrador | `LIN-OBS-001` sección 8.3 documenta fragmento de referencia; `LIN-K8S-001` es el dueño del Deployment completo |
| Política No PII en logs y trazas (Ley N.° 29733) | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | Resuelto | `LIN-OBS-001` sección 6.2; `LIN-SEC-APP-001` extenderá con otras dimensiones de privacidad |
| Convenciones de nomenclatura de servicios y spans | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-K8S-001` | Resuelto | `LIN-OBS-001` Apéndice C |

---

### 5. Autenticación y seguridad

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Modelo de seguridad institucional (SAA + AD + transición WSO2/OIDC) | `LIN-SEC-APP-001` | `LIN-ARQ-000`, `LIN-API-REST-001`, `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` [sección 3](#3-documentacion-openapi-swagger) define estado actual (SAA), modelo objetivo (WSO2 PoC) y tipos de usuario. La transición formal está respaldada por `arquitectura/ADR-WSO2-001.md` |
| Autenticación — delegación al SAA | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` [sección 4](#4-observabilidad) — prohíbe autenticación propia por aplicación |
| Autorización — token válido ≠ permiso implícito | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001` | En borrador | `LIN-SEC-APP-001` [sección 5](#5-autenticacion-y-seguridad) — la app valida permisos SAA por endpoint |
| Tokens SAA — emisión, validación, renovación, prohibiciones | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001` | En borrador | `LIN-SEC-APP-001` [sección 6](#6-estructura-de-proyecto-java) — prohíbe exponer claves criptográficas y crear tokens paralelos |
| Integración SAA en apps Spring Boot — patrón `SaaTokenValidationFilter` | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001` | En borrador | `LIN-SEC-APP-001` [sección 8](#8-base-de-datos) — flujo, filtro, cliente, prohibiciones, manejo de indisponibilidad |
| OAuth2/OIDC — flujo y decisión de arquitectura | `LIN-ARQ-000` | `LIN-API-REST-001`, `LIN-SEC-APP-001`, `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` sección 3.2 documenta el modelo objetivo; la decisión de adopción requiere ADR cuando WSO2 esté operativo |
| Configuración Spring Security | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001` | En borrador | `LIN-SEC-APP-001` sección 9.1 define configuración mínima obligatoria; validar en siguiente pasada si `LIN-DEV-JAVA-001` ya referencia esta sección explícitamente |
| Headers de seguridad HTTP obligatorios | `LIN-SEC-APP-001` | `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | En borrador | `LIN-SEC-APP-001` sección 7.3 — `X-Content-Type-Options`, `X-Frame-Options`, `HSTS`, etc. |
| Prohibición de credenciales en código o repositorio | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-CICD-001` | En borrador | `LIN-SEC-APP-001` sección 12.2 — prohibición absoluta, incluyendo `environment.ts` Angular |
| Gestión de secretos — K8s Secrets, rotación, separación por ambiente | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-K8S-001` | En borrador | `LIN-SEC-APP-001` [sección 12](#12-versionamiento-y-control-de-cambios) — dueño provisional hasta que `LIN-K8S-001` sea creado |
| Secrets en Kubernetes — propiedad definitiva | `LIN-K8S-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | En borrador | `LIN-K8S-001` asume la propiedad de K8s Secrets; `LIN-SEC-APP-001` [sección 12](#12-versionamiento-y-control-de-cambios) es consumidor |
| CORS en producción (sin `*`) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | Conforme | `LIN-SEC-APP-001` sección 7.4 agrega implementación Spring Boot; `LIN-API-REST-001` es el dueño de la regla |
| Seguridad frontend (XSS, token storage, guards, CSP) | `LIN-SEC-APP-001` | `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` [sección 10](#10-despliegue-e-infraestructura) define reglas base; `LIN-FE-ANG-001` las aplica en su contexto Angular |
| OWASP Top 10 como baseline y herramientas de escaneo | `LIN-SEC-APP-001` | Todos | En borrador | `LIN-SEC-APP-001` sección 13 — SonarQube, Snyk/Dependency-Check, Trivy; gates de calidad en CI/CD |
| Controles de seguridad para sistemas legacy | `LIN-SEC-APP-001` | `LIN-BD-ORA-001`, `LIN-ARQ-000` | En borrador | `LIN-SEC-APP-001` sección 14 — tabla de 6 escenarios con obligación diferenciada; SAA como capacidad legacy crítica |

---

### 6. Estructura de proyecto Java

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Decisión de estilo arquitectónico (Monolito Simple / Modular / Hexagonal) | `LIN-ARQ-000` | `LIN-DEV-JAVA-001` | Conforme | — |
| Estructura de paquetes Maven por estilo | `LIN-DEV-JAVA-001` | — | Resuelto | `LIN-DEV-JAVA-001` sección 3.1 separado en tres secciones: Estilo 1 (Monolito Simple), Estilo 2 (Modular), Estilo 3 (Hexagonal) |
| Regla de dependencia entre módulos Maven | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Convenciones de nomenclatura Java | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Inyección por constructor | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Records para DTOs | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Uso de Lombok | `LIN-DEV-JAVA-001` | — | Conforme | — |
| `@Transactional` — cuándo y en qué capa | `LIN-DEV-JAVA-001` | — | Conforme | — |
| `BigDecimal` para valores monetarios | `LIN-DEV-JAVA-001` | — | Conforme | — |
| `GlobalExceptionHandler` — implementación | `LIN-DEV-JAVA-001` | — | Conforme | Implementa el contrato de `LIN-API-REST-001`; referencia incluida en [sección 9](#9-frontend) |
| Code review como gate obligatorio | `LIN-DEV-JAVA-001` | `LIN-CICD-001` | Resuelto | `LIN-DEV-JAVA-001` sección 14: PR obligatorio, sin auto-aprobación, máx 400 líneas |
| Adapter para PL/SQL legacy | `LIN-DEV-JAVA-001` | `LIN-BD-ORA-001` | Resuelto | `LIN-BD-ORA-001` sección 6.0 define el patrón; `LIN-DEV-JAVA-001` [sección 8](#8-base-de-datos) referencia |
| Análisis estático de código (PMD) | `LIN-DEV-JAVA-001` | `LIN-SEC-APP-001`, `LIN-VER-001`, `LIN-CICD-001` | Resuelto | `LIN-DEV-JAVA-001` sección 10.3 define configuración, ruleset y custom rules XPath |

---

### 7. Pruebas

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Pirámide de pruebas y distribución por estilo | `LIN-ARQ-000` | `LIN-TEST-001`, `LIN-DEV-JAVA-001` | Conforme | — |
| Tipos de prueba y pirámide por estilo arquitectónico | `LIN-TEST-001` | `LIN-ARQ-000`, `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | En borrador | `LIN-TEST-001` [secciones 3](#3-documentacion-openapi-swagger)–4 definen clasificación y distribución por estilo (Simple, Modular, Hexagonal) |
| Cobertura mínima por capa y estilo (umbrales) | `LIN-TEST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001`, `LIN-CICD-001` | En borrador | `LIN-TEST-001` [sección 5](#5-autenticacion-y-seguridad) — Java: ≥65–70% global; capa negocio ≥75–85%; Angular: ≥70% statements |
| Herramientas: JUnit 5, Mockito, Testcontainers, JaCoCo | `LIN-TEST-001` | `LIN-DEV-JAVA-001` | En borrador | `LIN-TEST-001` sección 11.1 — OracleContainer `gvenzl/oracle-xe:21-slim-faststart` |
| Pruebas de contrato — obligatoriedad y herramientas | `LIN-TEST-001` | `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | En borrador | `LIN-TEST-001` [sección 6](#6-estructura-de-proyecto-java) — tabla de casos obligatorio/recomendado; OpenAPI validation como mínimo siempre |
| Pruebas de caracterización — técnica y criterios | `LIN-TEST-001` | `LIN-BD-ORA-001`, `LIN-DEV-JAVA-001` | En borrador | `LIN-TEST-001` sección 13 — dueño de la técnica; `LIN-BD-ORA-001` sección 6.0 declara cuándo es obligatorio |
| Naming conventions de tests (sufijos IT, CT, Test) | `LIN-TEST-001` | `LIN-DEV-JAVA-001` | En borrador | `LIN-TEST-001` sección 3.2 — `*Test.java` (Surefire), `*IT.java` y `*CT.java` (Failsafe) |
| Evidencias obligatorias y criterios de paso a QA/PROD | `LIN-TEST-001` | `LIN-CICD-001` | En borrador | `LIN-TEST-001` [secciones 8](#8-base-de-datos)–9 — qué artefactos deben existir; LIN-CICD-001 define cuándo se validan |
| Gates automáticos de pruebas en pipeline | `LIN-CICD-001` | `LIN-TEST-001` | En borrador | `LIN-TEST-001` [sección 7](#7-pruebas) declara que las pruebas deben ser automatizables; el gate lo define LIN-CICD-001 |
| Herramientas E2E y accesibilidad Angular | `LIN-TEST-001` | `LIN-FE-ANG-001` | En borrador | Playwright preferente; Cypress solo donde ya existe (`LIN-TEST-001` sección 4.4, `LIN-FE-ANG-001` sección 14.2); axe-core para accesibilidad |
| Pruebas de carga, estrés, resistencia, spike y smoke performance | `LIN-PERF-001` | `LIN-TEST-001`, `LIN-CICD-001` | En borrador | `LIN-TEST-001` [sección 1](#1-contrato-de-respuesta-estandar) delega a `LIN-PERF-001`; `LIN-PERF-001` [sección 5](#5-autenticacion-y-seguridad) define tipos |
| Herramienta preferente JMeter; alternativas k6 y Gatling | `LIN-PERF-001` | `LIN-CICD-001` | En borrador | `LIN-PERF-001` [sección 7](#7-pruebas) |
| Diseño de escenarios, usuarios concurrentes, ramp-up, duración, think time | `LIN-PERF-001` | — | En borrador | `LIN-PERF-001` [sección 10](#10-despliegue-e-infraestructura) |
| Umbrales p95/p99, TPS, error rate | `LIN-PERF-001` | `LIN-CICD-001` | En borrador | LIN-CICD-001 consumirá estos umbrales como gate automático. `LIN-PERF-001` [sección 9](#9-frontend) |
| Criterios de aceptación de performance y evidencia mínima de informe | `LIN-PERF-001` | `LIN-TEST-001` | En borrador | `LIN-PERF-001` sección 9.4 y sección 13 |

---

### 8. Base de datos

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Motores permitidos (Oracle 19c estándar, 11g restringido) | `LIN-BD-ORA-001` | `LIN-ARQ-000` | Conforme | — |
| Convenciones de nomenclatura BD | `LIN-BD-ORA-001` | — | Conforme | — |
| Esquemas por dominio funcional | `LIN-BD-ORA-001` | `LIN-ARQ-000` | Conforme | — |
| Política de migraciones versionadas (Flyway/Liquibase) | `LIN-BD-ORA-001` | `LIN-CICD-001` | Conforme | — |
| PL/SQL técnico permitido (constraints, vistas, auditoría) | `LIN-BD-ORA-001` | — | Conforme | — |
| Gobierno de PL/SQL legacy con lógica de negocio | `LIN-BD-ORA-001` | `LIN-DEV-JAVA-001`, `LIN-ARQ-000` | Resuelto | `LIN-BD-ORA-001` sección 6.0: categorías, inventario, adapter Java, pruebas de caracterización y checklist |
| Restricción de nueva lógica de negocio en PL/SQL | `LIN-BD-ORA-001` | `LIN-ARQ-000` | Resuelto | `LIN-BD-ORA-001` sección 6.0 define categorías restrictivas y proceso ADR para excepciones |
| Adapter Java para invocar PL/SQL legacy | `LIN-DEV-JAVA-001` | `LIN-BD-ORA-001` | Resuelto | `LIN-BD-ORA-001` sección 6.0 define el patrón `SimpleJdbcCall`; referenciado en `LIN-DEV-JAVA-001` [sección 8](#8-base-de-datos) |
| Prohibición de acceso directo entre dominios BD | `LIN-BD-ORA-001` | `LIN-ARQ-000` | Conforme | — |

---

### 9. Frontend

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Angular como framework SPA primario | `LIN-ARQ-000` | `LIN-FE-ANG-001` | Conforme | — |
| Estructura de proyecto Angular | `LIN-FE-ANG-001` | — | Conforme | — |
| Standalone components | `LIN-FE-ANG-001` | — | Conforme | — |
| Core Web Vitals — umbrales obligatorios | `LIN-ARQ-000` | `LIN-FE-ANG-001`, `LIN-CICD-001` | Conforme | — |
| Interceptores HTTP (auth, errores, request ID) | `LIN-FE-ANG-001` | — | Conforme | — |
| Modelos TypeScript de respuesta API | `LIN-FE-ANG-001` | — | Resuelto | `LIN-FE-ANG-001` corregido: `ApiMeta` completo con `timestamp`, `requestId`, `version`; errores con `campo`/`mensaje` |
| Estrategia de estado (Signals, store global) | `LIN-FE-ANG-001` | — | Pendiente | Pendiente de desarrollar en `LIN-FE-ANG-001` |
| Seguridad frontend (XSS, token storage, guards, CSP) | `LIN-SEC-APP-001` | `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` [sección 10](#10-despliegue-e-infraestructura) define reglas base; pendiente aplicar en `LIN-FE-ANG-001` |

---

### 10. Despliegue e infraestructura

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| K8s como destino por defecto | `LIN-ARQ-000` | `LIN-K8S-001`, `LIN-CICD-001` | Conforme | — |
| Dockerfile estándar (multi-stage, Alpine, no root) | `LIN-K8S-001` | `LIN-CICD-001` | En borrador | `LIN-ARQ-000` tiene referencia transitoria; dueño definitivo `LIN-K8S-001` |
| Política de namespaces, tagging de imágenes | `LIN-K8S-001` | `LIN-CICD-001` | En borrador | Dueño `LIN-K8S-001` |
| Variables de entorno vs Secrets K8s | `LIN-K8S-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | En borrador | Dueño `LIN-K8S-001` |
| IaC — herramienta y estructura de repositorio | `LIN-IAC-001` | `LIN-CICD-001`, `LIN-K8S-001` | En borrador | `LIN-IAC-001` en borrador; define Terraform, repositorio dedicado `oti-plataforma/infrastructure-iac`, fases de madurez y pipeline de validación |

---

### 11. Gobierno arquitectónico

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Formato y proceso de ADR | `LIN-ARQ-000` | Todos | Resuelto | Proceso de excepción (ADR) agregado en todos los lineamientos existentes: `LIN-DEV-JAVA-001` sección 15, `LIN-API-REST-001`, `LIN-BD-ORA-001`, `LIN-FE-ANG-001`, `LIN-OBS-001` sección 13 |
| Proceso de excepción a estándares | `LIN-ARQ-000` | Todos | Conforme | — |
| Cuándo es obligatoria revisión de arquitectura | `LIN-ARQ-000` | — | Conforme | — |
| Guía v0.1.2 — archivada | — | `LIN-OBS-001`, `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | **Archivado** | [secciones 1](#1-contrato-de-respuesta-estandar)–2 absorbidos por `LIN-OBS-001`; [sección 3](#3-documentacion-openapi-swagger) absorbido por `LIN-DEV-JAVA-001` y `LIN-API-REST-001`. La guía está marcada como archivada. El valor de "orden de inicio" se preserva en `LIN-DEV-JAVA-001 sección 1.3 — Configuración inicial de un proyecto nuevo`. No actualizar la guía. |

---

### 12. Versionamiento y control de cambios

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Estrategia de ramas Git — modelo vigente (`ONP_DESA` → `ONP_PQA` → `ONP_QA` → `master`) | `LIN-VER-001` | `LIN-K8S-001`, `LIN-CICD-001` | En borrador | Ramas por promoción. `LIN-VER-001` [sección 5](#5-autenticacion-y-seguridad) |
| Estrategia de ramas Git — modelo objetivo (GitLab Flow simplificado) | `LIN-VER-001` | `LIN-K8S-001`, `LIN-CICD-001` | En borrador | Modelo por defecto para proyectos nuevos: `main` único, ramas cortas, mismo artefacto promovido. `LIN-VER-001` [sección 6](#6-estructura-de-proyecto-java) |
| Merge Requests — canal obligatorio de integración | `LIN-VER-001` | Todos | En borrador | Sin MR no hay cambio a ramas protegidas. `LIN-VER-001` [sección 11](#11-gobierno-arquitectonico) |
| Revisión de código — reglas y revisores por tipo de cambio | `LIN-VER-001` | Todos | En borrador | `LIN-VER-001` [sección 12](#12-versionamiento-y-control-de-cambios) |
| Tags y releases — versionamiento semántico | `LIN-VER-001` | `LIN-K8S-001`, `LIN-CICD-001` | En borrador | Todo release productivo debe tener tag semántico (`vMAJOR.MINOR.PATCH`). `LIN-VER-001` sección 14 |
| Nomenclatura de repositorios y grupos GitLab | `LIN-VER-001` | Todos | En borrador | Grupo → Subgrupo → Proyecto. Convención de nombres en minúsculas con guion. `LIN-VER-001` [sección 8](#8-base-de-datos)–[sección 9](#9-frontend) |
| Versionamiento de scripts de BD en GitLab | `LIN-VER-001`, `LIN-BD-ORA-001` | — | En borrador | No se aceptan scripts por correo o mensajería. Criterio de naming en `LIN-BD-ORA-001` [sección 8](#8-base-de-datos); obligatoriedad en `LIN-VER-001` P10 |
| Plantillas institucionales GitLab (Group-level Project Templates) | `LIN-VER-001` | `LIN-CICD-001` | En borrador | Plataforma implementa; Arquitectura provee ejemplos de referencia. `LIN-VER-001` Anexo F |

---

## Plan de correcciones priorizadas

Correcciones a aplicar sobre los documentos existentes antes de redactar nuevos lineamientos:

| Prioridad | Corrección | Documento a modificar | Estado |
|---|---|---|---|
| 1 | Agregar código `LIN-xxx` en el encabezado de cada documento | Todos los existentes | **Aplicado** |
| 2 | `ApiResponseWrapper<T>` con genérico — reemplazar `Object data` por `T data` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 3 | `POST` creación exitosa → `201 Created` (tabla HTTP y ejemplos) | `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | **Aplicado** |
| 4 | Unificar campos JSON en español: `campo`/`mensaje` en `CampoError` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 5 | Configuración Swagger: cambiar `.properties` por `.yml` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 6 | Nivel de log producción: `INFO` para `pe.gob.onp.*`, `WARN` para frameworks | `LIN-DEV-JAVA-001` | **Aplicado** |
| 7 | Testcontainers: cambiar `PostgreSQLContainer` por `OracleContainer` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 8 | Bug Markdown sección 11.4.7: cerrar bloque de código del record con `@Schema` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 9 | Agregar sección WSO2 API Manager (Gateway + Management) | `LIN-API-REST-001` | **Aplicado** |
| 10 | Unificar campos de paginación `pagina`/`tamanio` y `ApiMeta` completo en Frontend | `LIN-FE-ANG-001` | **Aplicado** |
| 11 | Agregar sección de adapter Java para invocar PL/SQL legacy | `LIN-BD-ORA-001` sección 6.0 | **Aplicado** |
| 12 | Agregar sección de gobierno PL/SQL legacy (catálogo, categorías, checklist) | `LIN-BD-ORA-001` sección 6.0 | **Aplicado** |
| 13 | Agregar sección 14 code review como gate obligatorio | `LIN-DEV-JAVA-001` | **Aplicado** |
| 14 | Agregar sección 15 proceso de excepción (ADR) en lineamientos Java, API, BD y Frontend | Todos los existentes | **Aplicado** |
| 15 | Crear LIN-OBS-001 absorbiendo Guía v0.1.1 [secciones 1](#1-contrato-de-respuesta-estandar)–2 (trazas, logs, correlación, métricas) | `observabilidad/Lineamiento_Log_Trazabilidad_Observabilidad_ONP_v0.1.0.md` | **Aplicado** |
| 16 | Actualizar referencias en `LIN-DEV-JAVA-001` [sección 8](#8-base-de-datos) para que referencien `LIN-OBS-001` como fuente de logback, campos ECS y política No PII | `LIN-DEV-JAVA-001` | **Aplicado** |
| 17 | Crear LIN-SEC-APP-001 absorbiendo seguridad de SAA, Spring Security, secrets, OWASP, legacy y frontend | `seguridad/Lineamiento_Seguridad_Aplicaciones_ONP_v0.1.0.md` | **Aplicado** |
| 18 | Crear LIN-TEST-001 como dueño de la estrategia de pruebas: tipos, pirámide, cobertura, contrato, caracterización, E2E | `pruebas/Lineamiento_Estandar_Pruebas_ONP_v0.1.0.md` | **Aplicado** |

---

## Catálogo de Patrones de Arquitectura ONP — pendiente

Deliverable separado de los lineamientos. Los lineamientos referencian las fichas; las fichas no son normas sino referencia.

**Archivo destino:** `patrones/Catalogo_Patrones_Arquitectura_ONP_v0.1.0.md`

| Código | Patrón | Cobertura actual | Dueño natural |
|---|---|---|---|
| PT01 | Publisher/Subscriber | ❌ Sin ficha | LIN-BUS-001 |
| PT02 | Dead Letter Queue | ⚠️ Citado en LIN-ARQ-000 sección 3.4 | LIN-BUS-001 |
| PT03 | Event Sourcing | ❌ Sin ficha | LIN-BUS-001 |
| PT04 | CQRS | ⚠️ Mencionado en LIN-ARQ-000 sección 3.4 | LIN-BUS-001 / LIN-ARQ-000 |
| PT05 | API Gateway | ⚠️ Diagrama en LIN-ARQ-000 sección 2.2; sección en LIN-API-REST-001 sección 2.5 | LIN-API-REST-001 |
| PT06 | Retry | ❌ Sin ficha | LIN-API-REST-001 / LIN-BUS-001 |
| PT07 | Circuit Breaker | ⚠️ Citado como habilidad en LIN-ARQ-000 sección 10 | LIN-API-REST-001 |
| PT08 | Bulkhead | ❌ Sin ficha | LIN-K8S-001 |
| PT09 | Saga | ⚠️ Citado en LIN-ARQ-000 sección 3.4, delegado a LIN-BUS-001 | LIN-BUS-001 |
| PT10 | Strangler Fig | ✅ Sección sección 2.2 en LIN-ARQ-000 — falta formato ficha | LIN-ARQ-000 |
| PT11 | Backend for Frontend | ❌ Sin ficha | LIN-ARQ-000 |
| PT12 | Gateway Aggregation | ❌ Sin ficha | LIN-API-REST-001 / LIN-ARQ-000 |
| PT13 | Anti-Corruption Layer | ✅ Sección sección 6.3 en LIN-ARQ-000 — falta formato ficha | LIN-ARQ-000 |
| PT14 | Adapter | ✅ Sección sección 6.2 en LIN-ARQ-000 — falta formato ficha | LIN-ARQ-000 |
| PT15 | Facade | ❌ Sin ficha | LIN-ARQ-000 |
| PT16 | Medallón | ❌ Sin ficha | LIN-BD-ORA-001 / LIN-ARQ-000 |

**Formato de cada ficha:** problema que resuelve · solución · cuándo usar en ONP · cuándo NO usar · estructura / diagrama · consecuencias y trade-offs · patrones relacionados · ejemplo de referencia.

---

## Regla de actualización de esta matriz

Cada vez que se redacte un lineamiento nuevo o se modifique uno existente, esta matriz debe revisarse para:

1. Registrar el nuevo documento en el catálogo.
2. Verificar que no se haya duplicado un tema ya asignado a otro dueño.
3. Marcar como resueltos los conflictos corregidos.
4. Identificar nuevos conflictos o brechas que emerjan del nuevo contenido.
5. Confirmar que todo `Conforme` o `Resuelto` tenga respaldo en la versión vigente del documento citado.
6. Registrar la fecha de la última validación integral cuando el corpus cambie de forma significativa.

---

*Matriz de Propiedad Documental — ONP v0.1.2*  
*OTI — Oficina de Tecnologías de la Información*
