# Template Backend Java ONP

> **REFERENCIA INSTITUCIONAL — LIN-VER-001 / LIN-CICD-001 / LIN-OBS-001**
> Esta plantilla ya incluye un scaffold mínimo ejecutable. Antes de usarla en un proyecto real, reemplazar placeholders (`nombre-sistema`, `api-nombre-sistema`, paquete Java, datos de contacto) y ajustar seguridad, BD y despliegue según el contexto del sistema.

## Descripción

Plantilla institucional para iniciar servicios backend Java/Spring Boot alineados a los lineamientos ONP. Incluye:

- estructura base Spring Boot;
- `ApiResponseWrapper`;
- OpenAPI;
- filtros `RequestId` y log canónico;
- stub de integración SAA;
- observabilidad básica;
- pipeline GitLab mínimo;
- Dockerfile;
- estructura `db/migration` y `db/reverse`;
- manifiestos Kustomize base.

**Tipo:** Backend Java / Spring Boot  
**Propietario de la plantilla:** Arquitectura / Plataforma OTI  
**Lineamientos base:** `LIN-DEV-JAVA-001`, `LIN-API-REST-001`, `LIN-SEC-APP-001`, `LIN-OBS-001`, `LIN-VER-001`, `LIN-CICD-001`

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java JDK | 21 | Eclipse Temurin recomendado |
| Maven | 3.9+ | |
| Docker | 24+ | Para build de imagen |
| kubectl + kustomize | 1.27+ / 5+ | Para despliegue en K8s |

## Ejecución local

```bash
# Compilar y ejecutar pruebas
mvn verify

# Ejecutar localmente con perfil local o dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

> La plantilla incluye `application.yml`, `application-dev.yml`, `application-qa.yml` y `application-prod.yml`.
> Si se requiere perfil `local`, debe crearse fuera del estándar versionado y nunca incluir secretos en el repositorio.

`mvn verify` falla el build si la cobertura de línea cae por debajo de `${jacoco.coverage.minimum}` (0.65 — mínimo normado para Monolito Simple en `TEST-R-001` (LIN-TEST-001 §5.1)). Este proyecto es un ejemplo de **Monolito Simple**; si el sistema real crece a Monolito Modular u Hexagonal, revisar el umbral correspondiente en la misma sección antes de subirlo.

## Estructura del proyecto

```
src/
├── main/java/pe/gob/onp/[sistema]/
│   ├── config/          # OpenAPI y OTEL logback
│   ├── controller/      # Controladores REST
│   ├── dto/common/      # ApiResponseWrapper
│   ├── exception/       # GlobalExceptionHandler
│   └── filter/          # RequestId, SAA y log canónico
├── main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-qa.yml
│   ├── application-prod.yml
│   └── logback-spring.xml
└── test/java/           # Pruebas unitarias e integración
docs/
├── adr/                 # Architecture Decision Records
└── openapi/             # Contratos OpenAPI
db/
├── migration/           # Scripts de migración versionados
└── reverse/             # Scripts de reversa o compensación
k8s/
├── base/                # Manifiestos base (Kustomize)
└── overlays/            # Overlays por ambiente: dev, qa, prod
```

## Registro de imagen

Las imágenes se publican en el registro institucional GitLab:

```
registry.gitlab.onp.gob.pe/aplicaciones/nombre-sistema/api-nombre-sistema:<version>
```

Ver LIN-K8S-001 para el proceso completo de construcción y promoción de imágenes.

## Qué debe personalizar el equipo

1. Renombrar `spring.application.name`.
2. Reemplazar el paquete `pe.gob.onp.template.backend`.
3. Sustituir el stub de `SaaTokenValidationFilter` por la integración real con SAA.
4. Ajustar endpoints OTEL por entorno si Plataforma lo indica.
5. Reemplazar manifiestos K8s placeholder por los del sistema real.
6. Definir si el proyecto usará `db/migration` únicamente o un modelo mixto con scripts manuales, según `LIN-BD-ORA-001`.

## Artefactos normados — no personalizar

Los siguientes archivos son **copias controladas** de una fuente canónica institucional. No deben modificarse en el proyecto derivado: si un equipo necesita apartarse de ellos, requiere ADR aprobado por Arquitectura OTI.

| Archivo en este template | Fuente canónica | Documento que lo norma |
|---|---|---|
| `checkstyle-onp.xml` | `desarrollo/plantillas/checkstyle-onp.xml` | `LIN-DEV-JAVA-001 §12.1` — complejidad ciclomática ≤ 10, método ≤ 30 líneas, clase ≤ 500 líneas, línea ≤ 120 caracteres |
| `ApiResponseWrapper.java` (contrato: `codHttp`, `codDetRespuesta`, `menDetRespuesta`, `data`, `errors` con `CampoError{campo, mensaje}`, `meta`) | `desarrollo/plantillas/ApiResponseWrapper.java` | `LIN-API-REST-001 §4` (contrato) y `LIN-DEV-JAVA-001 §13.4.4` (implementación) |

> El nombre de la clase de error es **`CampoError`** con campos `campo`/`mensaje`, en español, según el contrato institucional. No usar `FieldError` — además de apartarse de la norma, colisiona visualmente con `org.springframework.validation.FieldError` en el `GlobalExceptionHandler`.

## Contacto

- **Líder técnico:** [nombre]
- **Arquitectura OTI:** arquitectura@onp.gob.pe
- **Plataforma/Infraestructura:** [canal interno]
