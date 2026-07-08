# START HERE — Proyecto Java ONP

**Versión:** 0.2.0 (Alineado a 3 Niveles de Arquitectura)  
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

- Estilo arquitectónico por defecto en nuevos proyectos: **Monolito Modular** o **Arquitectura Hexagonal / Limpia** (`LIN-DIS-001`).
- Plataforma de despliegue inmutable: **Kubernetes (K8s) con CRI `containerd`** (`LIN-ARQ-001 §7`).
- Formato oficial de configuración Spring Boot: YAML (`application.yml`, perfiles `-dev`, `-qa`, `-prod`).
- Modelo primario de observabilidad: OpenTelemetry con correlación canónica (`traceId`, `X-Request-ID`, Four Golden Signals).
- Variables OTEL en K8s: solo override operativo administrado por Plataforma.
- Modelo de repositorio para proyectos nuevos: GitLab Flow simplificado con MR obligatorio (`LIN-VER-001`), salvo ADR aprobado.

## 5. Punto de arranque técnico

`versionamiento/ejemplos-plantillas-gitlab/template-backend-java/`

Este template debe usarse como baseline para proyectos backend Java nuevos y adaptarse según el alcance del sistema, el modelo de BD y las capacidades activas de `LIN-CICD-001`.
