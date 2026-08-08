# START HERE — Proyecto Java ONP

**Código:** GOB-INI-001  
**Versión:** 0.3.0 (Criterio de elección de template; citas corregidas)  
**Fecha:** 2026-07-08  
**Estado:** Vigente / Operativo  
**Propósito:** punto de entrada para iniciar un proyecto Java/Spring Boot bajo los lineamientos ONP federados en 3 niveles de abstracción.

## 1. Orden de lectura recomendado

1. `LIN-ARQ-001` — Nivel 1: Marco Rector de Arquitectura de Software (topología K8s/containerd, estadios, gobierno y SRE).
2. `LIN-DIS-001` — Nivel 2: Estándar de Diseño de Software y Patrones Tácticos (Hexagonal, Monolito Modular, DDD, CQRS, Resiliencia).
3. `LIN-DEV-JAVA-001` — Nivel 3: Estándar de Desarrollo Java / GoF / SOLID y estructura de proyecto en Spring Boot 3.
4. `LIN-API-REST-001` — contrato REST, OpenAPI y `ApiResponseWrapper`.
5. `LIN-SEC-APP-001` — integración SAA, Zero Trust y seguridad base.
6. `LIN-OBS-001` — trazas, logs estructurados ECS, métricas Four Golden Signals y correlación.
7. `LIN-BD-ORA-001` — si el proyecto usa Oracle 19c o PL/SQL.
8. `LIN-VER-001` — modelo de repositorio, ramas y control de cambios (GitLab Flow simplificado).
9. `LIN-CICD-001` — pipeline mínimo y capacidades activas.
10. `LIN-K8S-001` / `LIN-IAC-001` — despliegue sobre cluster containerd e infraestructura como código.

> **Nota institucional:** El antiguo documento `LIN-ARQ-000` ha sido congelado como **Cantera Histórica (`v0.1.19`)** y no se utiliza como referencia de diseño para nuevos proyectos. Su contenido rige ahora desglosado en `LIN-ARQ-001`, `LIN-DIS-001` y `LIN-DEV-JAVA-001`.

## 2. Mínimo para primer pase a DEV

Un proyecto backend nuevo debe tener como mínimo:

- estructura de proyecto Spring Boot definida;
- `application.yml`, `application-dev.yml`, `application-qa.yml`, `application-prod.yml`;
- `OpenApiConfig`;
- `ApiResponseWrapper`;
- `GlobalExceptionHandler`;
- `RequestIdFilter`;
- `SaaTokenValidationFilter` si el servicio no es público;
- `CanonicalRequestLogFilter`;
- `logback-spring.xml`;
- dependencias OTEL / Micrometer;
- Dockerfile base;
- repositorio GitLab con MR obligatorio;
- pipeline mínimo activo o en piloto según `LIN-CICD-001`.

## 3. Mapa rápido tema -> documento dueño

| Tema | Documento dueño |
|---|---|
| Arquitectura (Macro / Gobierno / K8s) | `LIN-ARQ-001` |
| Diseño Táctico (Capas / Hexagonal / DDD) | `LIN-DIS-001` |
| **Catálogo de Patrones y Fichas con Criterio de Selección** | `LIN-PAT-001` |
| Desarrollo Java / GoF / SOLID | `LIN-DEV-JAVA-001` |
| Contrato REST / OpenAPI | `LIN-API-REST-001` |
| Seguridad / SAA | `LIN-SEC-APP-001` |
| Observabilidad | `LIN-OBS-001` |
| Base de datos Oracle | `LIN-BD-ORA-001` |
| Versionamiento GitLab | `LIN-VER-001` |
| CI/CD | `LIN-CICD-001` |
| Contenedores / K8s | `LIN-K8S-001` |
| IaC / Terraform | `LIN-IAC-001` |
| Pruebas funcionales | `LIN-TEST-001` |
| Performance | `LIN-PERF-001` |

## 4. Decisiones ya cerradas para proyectos nuevos

- Topología por defecto en nuevos proyectos: **Monolito Modular** (Estadio 2, `LIN-ARQ-001 §2.1`). El diseño interno del módulo —Capas o Hexagonal— se decide con el árbol de decisión de `LIN-DIS-001 §2`.
- Plataforma de despliegue inmutable: **Kubernetes (K8s) con CRI `containerd`** (`LIN-ARQ-001 §5.2`).
- Formato oficial de configuración Spring Boot: YAML (`application.yml`, perfiles `-dev`, `-qa`, `-prod`).
- Modelo primario de observabilidad: OpenTelemetry con correlación canónica (`traceId`, `X-Request-ID`, Four Golden Signals).
- Variables OTEL en K8s: solo override operativo administrado por Plataforma.
- Modelo de repositorio para proyectos nuevos: GitLab Flow simplificado con MR obligatorio (`LIN-VER-001`), salvo ADR aprobado.

## 5. Punto de arranque técnico — qué template usar

Existen dos templates institucionales. **El default es el modular**, coherente con la topología por defecto de `LIN-ARQ-001 §2.1` (Estadio 2, Monolito Modular).

| Template | Cuándo usarlo | Estructura |
|---|---|---|
| **`template-backend-java-modular`** *(por defecto)* | Todo sistema nuevo que abarque **2 o más dominios funcionales**, o que sea candidato a extraer alguno como microservicio. Es el arranque coherente con el Estadio 2. | Reactor Maven multi-módulo con fronteras explícitas: `domain` / `application` / `infrastructure` / `api` / `boot`, más `comun/` como Shared Kernel (`LIN-DIS-001 §3.4`). |
| **`template-backend-java`** *(simple)* | Sistema de **un solo dominio** sin candidatura a microservicio: módulos de soporte, mantenedores de catálogos, cruds administrativos o flujos lineales sin invariantes cruzados (`LIN-DIS-001 §2.2`). | Módulo Maven único en capas: `controller` / `service` / `repository` / `entity`. |

> **Criterio de decisión:** si dudas entre ambos, elige el **modular**. Partir de un módulo único y migrar después a multi-módulo obliga a reorganizar paquetes, `pom.xml` y pipeline; empezar modular y mantener un solo componente no tiene coste. `LIN-DEV-JAVA-001 §3.1` marca como señal de alarma que un `service` del template simple supere las ~300 líneas: es indicio de que el sistema necesitaba el modular.

Cualquiera de los dos se adapta según el alcance del sistema, el modelo de BD y las capacidades activas de `LIN-CICD-001`. Los archivos marcados como **artefactos normados** en el README de cada template (`checkstyle-onp.xml`, `ApiResponseWrapper`) no se personalizan sin ADR.
