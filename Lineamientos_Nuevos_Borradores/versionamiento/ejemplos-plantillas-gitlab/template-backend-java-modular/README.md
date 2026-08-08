# Template Backend Java ONP — Monolito Modular

> **REFERENCIA INSTITUCIONAL — LIN-DEV-JAVA-001 §14 / LIN-DIS-001 §2-3 / LIN-VER-001 / LIN-CICD-001 / LIN-OBS-001**
> Esta plantilla incluye un scaffold multi-módulo ejecutable con UN componente de negocio de referencia (`afiliacion`). Antes de usarla en un proyecto real: renombrar `pe.gob.onp.template` / `onp-template` al sistema real, renombrar `afiliacion` al primer subdominio real, y **duplicar la carpeta `componentes/afiliacion/` por cada Bounded Context adicional** que el sistema necesite.

## Cuándo usar esta plantilla (vs. `template-backend-java`)

| | `template-backend-java` | `template-backend-java-modular` (esta) |
|---|---|---|
| Estilo | Monolito Simple (`PAT-DIS-02`) | **Monolito Modular (`PAT-TOP-01`) — estándar por defecto en ONP** |
| Módulos Maven | 1 | 8 (`onp-common-domain`, `onp-common-web`, 5 × componente, `-boot`) |
| Cuándo usar | Sistema de soporte simple, CRUD sin reglas de negocio complejas | Todo sistema previsional nuevo (`LIN-ARQ-001 §2.1`), salvo excepción justificada |

Si el sistema tiene **un único subdominio simple y sin candidatura a microservicio**, considerar `template-backend-java` en su lugar. Ante la duda, el Monolito Modular es el default institucional.

## Descripción

Incluye, funcionando de extremo a extremo:

- estructura Maven multi-módulo jerárquica (`LIN-DEV-JAVA-001 §14.1`);
- un componente de negocio completo (`afiliacion`) con sus 5 sub-módulos (`-domain`, `-application`, `-infrastructure`, `-api`, `-messaging`), Agregado Raíz con transiciones de estado, Value Objects, puertos/adaptadores, Mapper Entity↔Dominio y wiring vía `@Configuration`/`@Bean`;
- `onp-common-domain` (Shared Kernel de dominio) y `onp-common-web` (contrato `ApiResponseWrapper`);
- consumer Kafka (`onp-afiliacion-messaging`) con envelope CloudEvents v1.0, idempotencia por `id` del evento, política No PII en el payload, reintentos con backoff exponencial y Dead Letter Queue (`LIN-BUS-001 §5, §8`);
- pruebas en las 5 capas: unitaria pura en `-domain`, unitaria con Mockito en `-application`/`-messaging`, `@DataJpaTest` + Testcontainers Oracle en `-infrastructure`, `@WebMvcTest` en `-api`, y `@SpringBootTest` de ensamblaje completo en `-boot`;
- migración Flyway versionada (`db/migration`) — `ddl-auto=validate`, nunca `update` (`LIN-BD-ORA-001`);
- filtros institucionales (`RequestId`, SAA, log canónico), `GlobalExceptionHandler` con mapeo de excepciones de dominio a HTTP, observabilidad OTEL/Logback, OpenAPI;
- gate de cobertura JaCoCo **diferenciado por capa**: 85% en `-domain`/`-application`, 70% en `-infrastructure`/`-api`/`-boot` (`LIN-TEST-001 §5.1`);
- pipeline GitLab, Dockerfile multi-stage consciente del reactor Maven, manifiestos Kustomize por ambiente.

**Tipo:** Backend Java / Spring Boot — Monolito Modular
**Propietario de la plantilla:** Arquitectura / Plataforma OTI
**Lineamientos base:** `LIN-DEV-JAVA-001`, `LIN-DIS-001`, `LIN-ARQ-001`, `LIN-API-REST-001`, `LIN-SEC-APP-001`, `LIN-OBS-001`, `LIN-BD-ORA-001`, `LIN-TEST-001`, `LIN-VER-001`, `LIN-CICD-001`

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java JDK | 21 | Eclipse Temurin recomendado |
| Maven | 3.9+ | |
| Docker | 24+ | Para build de imagen y Testcontainers (Oracle) |
| kubectl + kustomize | 1.27+ / 5+ | Para despliegue en K8s |

## Ejecución local

```bash
# Compilar y ejecutar pruebas de TODO el reactor (los 6 módulos)
mvn verify

# Compilar un módulo específico y sus dependencias (útil en desarrollo iterativo)
mvn -pl componentes/afiliacion/onp-afiliacion-application -am verify

# Ejecutar localmente (perfil dev) — requiere Oracle accesible (ORACLE_URL/ORACLE_USER/ORACLE_PASSWORD)
mvn -pl onp-template-boot spring-boot:run -Dspring-boot.run.profiles=dev
```

`mvn verify` falla si algún módulo cae bajo su umbral de cobertura (`LIN-TEST-001 §5.1`) o si Checkstyle encuentra violaciones. Las pruebas de `-infrastructure` y `-boot` requieren Docker disponible (Testcontainers levanta un Oracle real — `gvenzl/oracle-xe:21-slim-faststart`).

## Estructura del proyecto

```
onp-template/                              ← POM padre (packaging: pom)
├── comun/
│   ├── onp-common-domain/                 ← Shared Kernel de dominio (LIN-DIS-001 §3.4)
│   └── onp-common-web/                    ← ApiResponseWrapper (contrato LIN-API-REST-001)
├── componentes/
│   └── afiliacion/                        ← COMPONENTE DE NEGOCIO DE REFERENCIA
│       ├── onp-afiliacion-domain/         ← Agregado, puertos, excepciones — cero Spring/JPA
│       ├── onp-afiliacion-application/    ← Casos de uso POJO, Command, DTO interno
│       ├── onp-afiliacion-infrastructure/ ← Entity JPA, Mapper, Adapter, wiring @Bean
│       ├── onp-afiliacion-api/            ← Controller REST — driving adapter HTTP
│       └── onp-afiliacion-messaging/      ← Consumer Kafka — driving adapter por evento
│           ├── dto/                       ← Envelope CloudEvents (AporteRegistradoEvent)
│           ├── dedup/                     ← Idempotencia técnica (TB_EVENTO_PROCESADO)
│           └── config/                    ← Reintentos + Dead Letter Queue
├── onp-template-boot/                     ← Ensamblador (único .jar ejecutable)
│   └── src/main/java/pe/gob/onp/template/boot/
│       ├── config/                        ← OpenAPI, OTEL/Logback
│       ├── filter/                        ← RequestId, SAA, log canónico
│       └── exception/                     ← GlobalExceptionHandler (todo el sistema)
├── docs/{adr,openapi}/
├── db/{migration,reverse}/                ← Scripts DBA de referencia (fuera del classpath)
├── k8s/{base,overlays}/
├── Dockerfile
└── .gitlab-ci.yml
```

## Cómo funciona el consumer Kafka de referencia

`onp-afiliacion-messaging` escucha el tópico `aportes.registro.aporte-registrado` (publicado por un hipotético componente `aportes` en otro sistema) y activa al afiliado correspondiente. Es la referencia de un **adapter de entrada (*driving*) no-HTTP**: mismo principio que `-api` — solo conoce el puerto de dominio `ActivarAfiliacionPorAporteUseCase`, nunca `-infrastructure` ni el resto del dominio.

Puntos normativos que este ejemplo aplica (`LIN-BUS-001`):

- **Envelope CloudEvents v1.0** (`§5.2`) tipado, no un `JsonNode` navegado a mano.
- **Política No PII** (`§5.3`): el payload lleva `afiliadoId` (identificador interno), nunca el DNI.
- **Ack manual** (`§8.1`, `§8.3`): `enable-auto-commit: false` — el commit se hace explícitamente después de procesar, no antes.
- **Idempotencia por `id` del evento** (`§8.4`): tabla técnica `TB_EVENTO_PROCESADO`, independiente de si el caso de uso es naturalmente idempotente.
- **Reintentos + DLQ** (`§8.5-8.6`): `DefaultErrorHandler` con backoff exponencial (3 intentos) y `DeadLetterPublishingRecoverer`; `AfiliadoNoEncontradoException` está marcada como no reintentable — va directo a `aportes.registro.aporte-registrado.dlq` sin gastar los 3 intentos, porque reintentar no arregla un `afiliadoId` que no existe.

Para agregar un consumer nuevo en otro componente: crear `onp-{componente}-messaging` con la misma estructura (`dto/`, `dedup/`, `config/`), registrarlo en el `pom.xml` raíz y como dependencia de `onp-template-boot`.

## Cómo agregar un segundo componente de negocio

1. Duplicar `componentes/afiliacion/` a `componentes/{nuevo-modulo}/`, renombrando artifactIds, paquetes Java y clases. Descartar `-messaging` si el componente no consume eventos.
2. Registrar los nuevos módulos (4 u 5, según el punto anterior) en `<modules>` del `pom.xml` raíz.
3. Agregar `onp-{nuevo-modulo}-api`, `onp-{nuevo-modulo}-infrastructure` y, si aplica, `onp-{nuevo-modulo}-messaging` como dependencias de `onp-template-boot/pom.xml` (regla de gobernanza #1, `LIN-DEV-JAVA-001 §14.1`).
4. **Nunca** hacer que un módulo de `{nuevo-modulo}` importe `application`, `infrastructure` o `api` de `afiliacion` — solo su `-domain`, si necesita leer datos en memoria (regla de gobernanza #2). Para todo lo demás, comunicación por eventos Kafka (`ADR-012`).

## Qué debe personalizar el equipo

1. Renombrar `groupId`/`artifactId` de `pe.gob.onp.template` / `onp-template` al sistema real, en los 9 `pom.xml`.
2. Renombrar el componente `afiliacion` (carpetas, paquetes, artifactIds, tabla `TB_AFILIADO`) al primer subdominio real del sistema. Si el componente no necesita reaccionar a eventos externos, eliminar `onp-afiliacion-messaging` (y su dependencia en `-boot`) — no es obligatorio como `-api`.
3. Sustituir el stub de `SaaTokenValidationFilter` por la integración real con SAA.
4. Ajustar endpoints OTEL por entorno si Plataforma lo indica.
5. Reemplazar manifiestos K8s placeholder por los del sistema real.
6. Revisar el umbral `jacoco.coverage.minimum` / `jacoco.coverage.minimum.domain` si el sistema justifica un mínimo mayor al normado en `LIN-TEST-001 §5.1`.

## Artefactos normados — no personalizar

Los siguientes archivos son **copias controladas** de una fuente canónica institucional. No deben modificarse en el proyecto derivado: si un equipo necesita apartarse de ellos, requiere ADR aprobado por Arquitectura OTI.

| Archivo en este template | Fuente canónica | Documento que lo norma |
|---|---|---|
| `checkstyle-onp.xml` (raíz del reactor) | `desarrollo/plantillas/checkstyle-onp.xml` | `LIN-DEV-JAVA-001 §12.1` — complejidad ciclomática ≤ 10, método ≤ 30 líneas, clase ≤ 500 líneas, línea ≤ 120 caracteres |
| `comun/onp-common-web/.../ApiResponseWrapper.java` (contrato: `codHttp`, `codDetRespuesta`, `menDetRespuesta`, `data`, `errors` con `CampoError{campo, mensaje}`, `meta`) | `desarrollo/plantillas/ApiResponseWrapper.java` | `LIN-API-REST-001 §4` (contrato) y `LIN-DEV-JAVA-001 §13.4.4` (implementación) |

> El nombre de la clase de error es **`CampoError`** con campos `campo`/`mensaje`, en español, según el contrato institucional. No usar `FieldError` — además de apartarse de la norma, colisiona visualmente con `org.springframework.validation.FieldError` en el `GlobalExceptionHandler`, que consume `getBindingResult().getFieldErrors()` de Spring en la misma línea.

## Contacto

- **Líder técnico:** [nombre]
- **Arquitectura OTI:** arquitectura@onp.gob.pe
- **Plataforma/Infraestructura:** [canal interno]
