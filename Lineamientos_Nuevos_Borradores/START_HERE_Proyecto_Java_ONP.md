# START HERE — Proyecto Java ONP

**Versión:** 0.1.1  
**Fecha:** 2026-05-28  
**Estado:** Borrador operativo  
**Propósito:** punto de entrada para iniciar un proyecto Java/Spring Boot bajo los lineamientos ONP.

## 1. Orden de lectura recomendado

1. `LIN-ARQ-000` — marco rector y decisiones base.
2. `LIN-DEV-JAVA-001` — estructura del proyecto y componentes obligatorios.
3. `LIN-API-REST-001` — contrato REST, OpenAPI y `ApiResponseWrapper`.
4. `LIN-SEC-APP-001` — integración SAA y seguridad base.
5. `LIN-OBS-001` — trazas, logs estructurados, métricas y correlación.
6. `LIN-BD-ORA-001` — si el proyecto usa Oracle o PL/SQL.
7. `LIN-VER-001` — modelo de repositorio, ramas y control de cambios.
8. `LIN-CICD-001` — pipeline mínimo y capacidades activas.
9. `LIN-K8S-001` / `LIN-IAC-001` — despliegue e infraestructura, cuando aplique.

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
| Arquitectura base | `LIN-ARQ-000` |
| Desarrollo Java | `LIN-DEV-JAVA-001` |
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

- Formato oficial de configuración Spring Boot: YAML.
- Modelo primario de observabilidad: configuración versionada por perfil.
- Variables OTEL en K8s: solo override operativo administrado por Plataforma.
- Modelo de repositorio para proyectos nuevos: GitLab Flow simplificado, salvo ADR.

## 5. Punto de arranque técnico

`versionamiento/ejemplos-plantillas-gitlab/template-backend-java/`

Este template debe usarse como baseline para proyectos backend Java nuevos y adaptarse según el alcance del sistema, el modelo de BD y las capacidades activas de `LIN-CICD-001`.
