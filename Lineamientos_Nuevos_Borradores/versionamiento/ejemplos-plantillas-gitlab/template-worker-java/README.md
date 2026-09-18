# Template Worker Java ONP

> **REFERENCIA INSTITUCIONAL — LIN-VER-001 Anexo F / LIN-K8S-001 §4.1 / LIN-BUS-001 / LIN-DIS-001 §4.1**
> Esta plantilla ya incluye un scaffold mínimo ejecutable. Antes de usarla en un proyecto real, reemplazar placeholders (`nombre-sistema`, `worker-nombre-sistema`, paquete Java, tópico Kafka, tabla `TB_REGISTRO_PENDIENTE`, datos de contacto) y ajustar seguridad, BD y despliegue según el contexto del sistema.

## Worker vs. Job puntual vs. Job recurrente

`LIN-K8S-001 §4.1` (tabla "Tipos de workload permitidos") distingue tres cosas que suelen confundirse. Esta plantilla las demuestra por separado, con manifiestos K8s independientes en `k8s/base/`:

| Variante | Qué es | Ejemplo en esta plantilla | Manifiesto K8s | Perfil Spring |
|---|---|---|---|---|
| **Worker** | Consumidor de cola, proceso asíncrono **continuo** | `TareaSolicitadaConsumer` (listener Kafka) | `k8s/base/deployment.yaml` | `worker` |
| **Job puntual** | Migración, carga controlada, tarea batch de **una sola ejecución** | `ActualizacionMasivaJobRunner` | `k8s/base/job.yaml` | `job` |
| **Job recurrente** | Tarea programada (la misma tarea batch, con periodicidad) | `ActualizacionMasivaJobRunner` (misma clase) | `k8s/base/cronjob.yaml` | `job` |

**Punto clave:** un Job puntual y un Job recurrente son **el mismo tipo de aplicación Java**, ejecutada por un `Job` o por un `CronJob` según si la tarea se dispara una vez o según un `schedule`. No hay dos clases Java distintas para eso — sería duplicar código que solo difiere en quién lo invoca. La diferencia vive enteramente en el manifiesto K8s, no en el código.

El Worker sí es un tipo de aplicación distinto en código (un proceso que nunca termina por sí mismo, con estado de conexión a un broker) — por eso tiene su propia clase y su propio perfil.

## Perfiles y variantes de workload

Esta plantilla usa un único `pom.xml` y un único jar ejecutable para las tres variantes (a diferencia de tener tres proyectos separados). La variante activa la decide `SPRING_PROFILES_ACTIVE`, combinando ambiente + variante, ej. `SPRING_PROFILES_ACTIVE=prod,worker` o `SPRING_PROFILES_ACTIVE=prod,job`:

- `worker` activa `TareaSolicitadaConsumer` (`@Profile("worker")`) y reactiva el servidor web embebido (`application-worker.yml`) — únicamente para que Actuator exponga `/actuator/health/*` y `/actuator/prometheus`, como exige `LIN-K8S-001 §11.1/§11.2` sobre el `Deployment` continuo.
- `job` activa `ActualizacionMasivaJobRunner` (`@Profile("job")`). No expone HTTP: `application.yml` fija `spring.main.web-application-type: none` por defecto precisamente para este caso — un proceso de vida corta no necesita levantar un servidor embebido. `Job`/`CronJob` tampoco llevan `readinessProbe`/`livenessProbe` en `LIN-K8S-001 §4.1`, así que no hace falta.

Ver `TemplateWorkerApplication` (Javadoc) para el detalle de qué bean activa cada perfil.

## Descripción

Plantilla institucional para iniciar workers y jobs batch Java/Spring Boot alineados a los lineamientos ONP. Incluye:

- consumidor Kafka de ejemplo con envelope CloudEvents, idempotencia y Dead Letter Queue (LIN-BUS-001 §8);
- job batch de ejemplo con el patrón Table Module (LIN-DIS-001 §4.1);
- observabilidad básica (logs JSON + OTEL);
- pipeline GitLab mínimo;
- Dockerfile;
- migraciones Flyway versionadas (LIN-BD-ORA-001 §8.4) y estructura `db/reverse`;
- manifiestos Kustomize base para las tres variantes de workload.

**Tipo:** Worker / Job Java  
**Propietario de la plantilla:** Arquitectura / Plataforma OTI  
**Lineamientos base:** `LIN-DEV-JAVA-001`, `LIN-BUS-001`, `LIN-DIS-001`, `LIN-BD-ORA-001`, `LIN-OBS-001`, `LIN-VER-001`, `LIN-CICD-001`, `LIN-K8S-001`

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java JDK | 21 | Eclipse Temurin recomendado |
| Maven | 3.9+ | |
| Docker | 24+ | Para build de imagen |
| kubectl + kustomize | 1.27+ / 5+ | Para despliegue en K8s |
| Kafka (local o remoto) | — | Solo necesario para probar el perfil `worker` end-to-end |
| Oracle (local, remoto o Testcontainers) | — | Necesario para `mvn verify` con pruebas de integración reales — no incluidas en este scaffold, ver "Qué NO incluye este scaffold" |

## Ejecución local

```bash
# Compilar y ejecutar pruebas (unitarias — no requieren Kafka ni Oracle reales)
mvn verify

# Ejecutar localmente como Worker (perfil de ambiente + variante)
mvn spring-boot:run -Dspring-boot.run.profiles=dev,worker

# Ejecutar localmente como Job puntual (termina el proceso al finalizar)
mvn spring-boot:run -Dspring-boot.run.profiles=dev,job
```

> La plantilla incluye `application.yml`, `application-dev.yml`, `application-qa.yml`, `application-prod.yml` (perfiles de AMBIENTE) y `application-worker.yml`/`application-job.yml` (perfiles de VARIANTE, se combinan con los anteriores). Si se requiere perfil `local`, debe crearse fuera del estándar versionado y nunca incluir secretos en el repositorio.

`mvn verify` falla el build si la cobertura de línea cae por debajo de `${jacoco.coverage.minimum}` (0.65). Ver la sección "Cobertura de pruebas" más abajo para el razonamiento — no es la misma justificación que en `template-backend-java`.

## Qué NO incluye este scaffold (a propósito)

- **Prueba de contexto Spring completo (`@SpringBootTest`) del tipo `TemplateBackendApplicationTests`.** Esta plantilla sí declara un `DataSource` real (JPA + Oracle, para idempotencia y para el job batch), a diferencia de `template-backend-java`, que no tiene JPA. Cargar el contexto completo aquí exigiría una base de datos disponible (Testcontainers Oracle) en cada `mvn test`. Se optó por pruebas unitarias aisladas (Mockito / sin Spring) sobre cada clase de ejemplo — coherente con `LIN-TEST-001 §4.5` (EDA), que prioriza la unitaria sin broker, y con el mismo patrón ya usado en `onp-afiliacion-messaging` de `template-backend-java-modular` (declara `testcontainers` en el `pom.xml` pero el test que trae es puramente unitario). Un equipo que adopte esta plantilla debe agregar sus propias pruebas de integración con Testcontainers (Kafka + Oracle) antes de pasar a QA, conforme a `LIN-TEST-001 §4.5` ("Consumer end-to-end contra broker" e "Idempotencia del consumidor", ambas obligatorias de integración).
- **Servicio real detrás de `ProcesadorTareaService`.** Es un stub que solo loguea — cada equipo lo reemplaza por el caso de uso real.

## Estructura del proyecto

```
src/
├── main/java/pe/gob/onp/[sistema]/
│   ├── config/            # OTEL logback, manejo de errores Kafka (retry + DLQ)
│   ├── messaging/         # EJEMPLO WORKER: consumidor Kafka, idempotencia, DTO CloudEvents
│   │   ├── dto/
│   │   ├── dedup/
│   │   └── exception/
│   └── batch/              # EJEMPLO JOB (puntual y recurrente): Table Module + ApplicationRunner
├── main/resources/
│   ├── application.yml           # base (todas las variantes/ambientes)
│   ├── application-dev.yml       # ambiente
│   ├── application-qa.yml        # ambiente
│   ├── application-prod.yml      # ambiente
│   ├── application-worker.yml    # variante: reactiva servidor web para Actuator
│   ├── application-job.yml       # variante: sin servidor web (hereda de application.yml)
│   └── logback-spring.xml
└── test/java/               # Pruebas unitarias — ver "Qué NO incluye este scaffold"
docs/
└── adr/                     # Architecture Decision Records
db/
├── migration/                # Flyway — nomenclatura V{MAJOR}.{MINOR}.{PATCH}__descripcion.sql (LIN-BD-ORA-001 §8.4)
└── reverse/                   # Reversa manual — nomenclatura U{MAJOR}.{MINOR}.{PATCH}__descripcion.sql
k8s/
├── base/
│   ├── deployment.yaml        # Worker continuo
│   ├── job.yaml                # Job puntual — NO en kustomization.yaml, se invoca ad hoc (ver el archivo)
│   ├── cronjob.yaml             # Job recurrente
│   ├── configmap.yaml
│   ├── networkpolicy.yaml       # solo pods "worker" — ver el archivo para el razonamiento
│   └── kustomization.yaml
└── overlays/                    # dev, qa, prod
```

## Cobertura de pruebas

`LIN-TEST-001 §5.1` (`TEST-R-001`) norma la fila **"Worker / Job Batch"**: ≥65% instrucción global, ≥80% en la lógica de procesamiento (*Table Module*, `LIN-DIS-001 §4.1`) — que en este proyecto es `ActualizacionMasivaJobRunner`/`RegistroPendienteTableModule`. El handler Kafka (`TareaSolicitadaConsumer`) queda cubierto por el mismo umbral global de esta fila. (Esta fila se incorporó a `LIN-TEST-001` a partir de la construcción de este scaffold — antes no existía.)

## Registro de imagen

Las imágenes se publican en el registro institucional GitLab:

```
registry.gitlab.onp.gob.pe/aplicaciones/nombre-sistema/worker-nombre-sistema:<version>
```

La misma imagen sirve las tres variantes — no se construye una imagen distinta por variante (ver "Perfiles y variantes de workload"). Ver `LIN-K8S-001` para el proceso completo de construcción y promoción de imágenes.

## Qué debe personalizar el equipo

1. Renombrar `spring.application.name` y el paquete `pe.gob.onp.template.worker`.
2. Reemplazar el tópico Kafka `sistema.dominio.tarea-solicitada` por el real, y el envelope `TareaSolicitadaEvent` por el contrato CloudEvents real del evento consumido (LIN-BUS-001 §5.2).
3. Reemplazar el cuerpo de `ProcesadorTareaService` por el caso de uso de negocio real.
4. Reemplazar la regla de `RegistroPendienteTableModule` y el esquema de `TB_REGISTRO_PENDIENTE` por el proceso batch real del sistema — o eliminar la variante Job/CronJob completa si el sistema solo necesita un Worker (y viceversa: eliminar `messaging/` y `deployment.yaml` si solo necesita Job/CronJob). Este scaffold trae las tres variantes juntas solo con fines de referencia — un sistema real normalmente no necesita las tres a la vez.
5. Decidir si conviene separar el Worker y el Job/CronJob en dos artefactos Maven independientes (dos `pom.xml`, dos imágenes) en vez de un único jar con perfiles, si sus ciclos de despliegue divergen mucho en la práctica — ver "Decisión: un solo jar con perfiles" más abajo.
6. Ajustar la `NetworkPolicy` a los namespaces reales, y confirmar con Plataforma el namespace real de Kafka (ver la nota de gobernanza de `LIN-K8S-001 §4.4` sobre `kafka-dev`/`kafka-qa`/`kafka-prod`, referenciada también en `k8s/base/networkpolicy.yaml`).
7. Ajustar endpoints OTEL por entorno si Plataforma lo indica.
8. Reemplazar manifiestos K8s placeholder por los del sistema real, y decidir el `schedule` real de `cronjob.yaml`.

## Decisión: un solo jar con perfiles

`LIN-VER-001 Anexo F` describe `template-worker-java` como una única plantilla para las tres variantes (`Deployment`, `Job`, `CronJob`), sin indicar si deben ser artefactos Maven separados. Se optó por **un único `pom.xml` y un único jar**, seleccionando la variante en tiempo de ejecución con perfiles Spring (`worker`/`job`), en vez de tres módulos Maven independientes, porque:

- Evita triplicar `pom.xml`, `Dockerfile` y pipeline para un scaffold que ya es un ejemplo de referencia, no un sistema real con historiales de despliegue divergentes.
- El código compartido (config OTEL, manejo de errores Kafka, dependencias) vive en un solo lugar.
- Si en un sistema real el Worker y el Job terminan evolucionando a ritmos muy distintos (releases, on-call, tamaño de equipo), separarlos en dos artefactos —cada uno con su propio `pom.xml` como en `template-backend-java-modular`— es una decisión de Arquitectura documentable en ADR, no un cambio estructural de esta plantilla.

## Artefactos normados — no personalizar

Los siguientes archivos son **copias controladas** de una fuente canónica institucional. No deben modificarse en el proyecto derivado: si un equipo necesita apartarse de ellos, requiere ADR aprobado por Arquitectura OTI.

| Archivo en este template | Fuente canónica | Documento que lo norma |
|---|---|---|
| `checkstyle-onp.xml` | `desarrollo/plantillas/checkstyle-onp.xml` | `LIN-DEV-JAVA-001 §12.1` — complejidad ciclomática ≤ 10, método ≤ 30 líneas, clase ≤ 500 líneas, línea ≤ 120 caracteres |

> A diferencia de `template-backend-java`, este scaffold no trae `ApiResponseWrapper` — el worker no expone API REST de negocio (ver "Decisión: `spring-boot-starter-web` solo para Actuator" abajo).

## Decisión: `spring-boot-starter-web` solo para Actuator

El worker no expone API REST de negocio, pero `pom.xml` sí incluye `spring-boot-starter-web`. Motivo: Spring Boot Actuator no puede exponer sus endpoints por HTTP sin una pila web (MVC o WebFlux) en el classpath — no existe una forma "headless" de exponer `/actuator/health/*` por HTTP sin ella. El perfil `worker` (`Deployment` continuo) la necesita para cumplir las probes obligatorias de `LIN-K8S-001 §11.1/§11.2`. El perfil `job` fuerza `spring.main.web-application-type=none` para que, aunque la dependencia esté en el classpath, no se levante ningún servidor embebido en un proceso de vida corta (ver `application.yml`).

## Contacto

- **Líder técnico:** [nombre]
- **Arquitectura OTI:** arquitectura@onp.gob.pe
- **Plataforma/Infraestructura:** [canal interno]
