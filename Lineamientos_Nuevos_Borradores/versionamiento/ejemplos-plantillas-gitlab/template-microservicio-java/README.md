# Template Microservicio Java ONP

> **REFERENCIA INSTITUCIONAL — LIN-VER-001 Anexo F / LIN-DEV-JAVA-001 §14.1/§14.3 / LIN-DIS-001 §6 / LIN-ARQ-001 §2.1**
> Esta plantilla ya incluye un scaffold mínimo ejecutable. Antes de usarla en un proyecto real, reemplazar placeholders (`nombre-microservicio`, paquete Java `pe.gob.onp.template.defuncion`, dominio de ejemplo "verificación de defunción", tabla `TB_CONSULTA_DEFUNCION`, endpoint externo de Registro Civil, datos de contacto) y ajustar seguridad, BD y despliegue según el contexto del sistema real.

## Qué demuestra esta plantilla

Es el ejemplo físico del **Microservicio independiente (Estadio 3, `ARQ-R-001`)** que faltaba en este directorio. Ya existían:

| Plantilla | Topología | Estructura Maven |
|---|---|---|
| `template-backend-java` | Monolito Simple, Arquitectura en Capas | 1 módulo |
| `template-backend-java-modular` | Monolito Modular (Estadio 2), Hexagonal por componente | Reactor multi-módulo (`-domain`/`-application`/`-infrastructure`/`-api` × N componentes) |
| `template-worker-java` | Worker / Job / CronJob | 1 módulo |
| **`template-microservicio-java`** (esta) | **Microservicio independiente (Estadio 3)**, Arquitectura Hexagonal | **1 solo módulo Maven** |

## Cuándo usar esta plantilla vs. las otras

| Pregunta | `template-backend-java` (Capas) | `template-backend-java-modular` (Hexagonal multi-módulo) | `template-microservicio-java` (esta) |
|---|---|---|---|
| ¿El sistema es candidato a microservicio? | No | No (es el Estadio 2 por defecto) | **Sí — validado contra los 6 criterios de `ARQ-R-001` (ver abajo)** |
| ¿Cuántos Bounded Contexts tiene? | 0 (soporte simple, *Transaction Script*) | Varios, ensamblados en un solo Pod | **Uno — el propio microservicio es el Bounded Context** |
| ¿Despliegue? | Un Pod, un sistema | Un Pod, todos los módulos ensamblados (`-boot`) | **Un Pod dedicado, propio, con su propia BD** |
| ¿Estructura de paquetes? | `controller/service/repository/entity` (capas horizontales) | `domain/application/infrastructure/api` × módulo Maven físico | `domain/application/infrastructure` (paquetes, no módulos Maven separados) |
| ¿Circuit Breaker (Resilience4j)? | No aplica | Excepcional, requiere ADR (`DIS-R-009`) | **Obligatorio** (`DIS-R-009`) |

**Regla de decisión (`LIN-DEV-JAVA-001 §14.1`, tabla "Estructura Maven"):** el Monolito Modular (`template-backend-java-modular`) es el estándar por defecto para todo sistema nuevo. Solo se extrae un módulo a Microservicio independiente (esta plantilla) cuando se cumplen **simultáneamente** los 6 criterios de `ARQ-R-001` (`LIN-ARQ-001 §2.1`):

1. **Dominio autónomo** — Bounded Context limpio, mínimos intercambios síncronos con el resto.
2. **Soberanía de datos** — esquema/instancia de BD propia (aquí: `TB_CONSULTA_DEFUNCION`, sin *joins* con tablas de otros sistemas).
3. **Escalamiento asimétrico comprobado** — picos ≥3x el resto del sistema.
4. **Ciclo de vida independiente** — pases a producción frecuentes sin arriesgar el core previsional.
5. **Tolerancia a consistencia eventual** — el negocio acepta retrasos de propagación.
6. **Capacidad operativa y SRE instalada** — observabilidad distribuida, CI/CD 100% automatizado.

> Si falta **uno solo** de los 6 criterios, la funcionalidad debe permanecer dentro del Monolito Modular (`template-backend-java-modular`) — no se extrae.

## Por qué "un solo módulo Maven" y no 4 sub-módulos

`LIN-DEV-JAVA-001 §14.1` permite, para un Microservicio (Estadio 3), **dos** variantes de estructura:

- 4 sub-módulos Maven (`-domain`/`-application`/`-infrastructure`/`-api`) bajo una raíz propia con su propio `-boot` — la misma estructura interna que ya demuestra `template-backend-java-modular` para cada componente de negocio dentro del reactor.
- **Un solo módulo Maven** con la separación aplicada mediante **paquetes Java estancos** (`domain/`, `application/`, `infrastructure/`) — la variante que demuestra esta plantilla, recomendada para "microservicios acotados" (el caso típico: un Bounded Context pequeño y bien definido, como la verificación de defunción de este ejemplo).

La pureza hexagonal (dominio y aplicación sin Spring/JPA) es la **misma regla** en ambas variantes — aquí se hace cumplir con ArchUnit en el mismo (único) módulo, en vez de en un módulo `-boot` separado.

## Dominio de ejemplo: verificación de defunción

Un microservicio de consulta/validación acotado, con un solo Aggregate Root (`ConsultaDefuncion`), deliberadamente distinto del dominio "afiliación" de `template-backend-java-modular` — para que se note que es un ejemplo "extraído" (un Bounded Context pequeño) y no un sistema completo:

- **Caso de uso:** verificar si una persona (por DNI) figura como fallecida en el Registro Civil (RENIEC) — insumo típico previo a la autorización de pago de una pensión (suspensión por fallecimiento no reportado).
- **Puerto de entrada:** `ConsultarEstadoDefuncionUseCase`.
- **Puerto de salida #1 (persistencia):** `ConsultaDefuncionRepository` — cachea el historial de verificaciones en Oracle (`TB_CONSULTA_DEFUNCION`), con una regla de negocio de vigencia de 24 horas (`ConsultaDefuncion#esVigente`) para no golpear el servicio externo en cada consulta.
- **Puerto de salida #2 (cliente externo):** `RegistroCivilDefuncionClient` — consulta el Registro Civil por HTTP, implementado por `RegistroCivilHttpAdapter` con **Resilience4j real** (`@CircuitBreaker` + `@Bulkhead`) sobre Apache HttpClient 5, a diferencia del `ReniecHttpAdapter` conceptual de otros documentos: aquí el Circuit Breaker es obligatorio, no opcional/comentado (`DIS-R-009`).

## Estructura del proyecto (`LIN-DEV-JAVA-001 §14.1`, variante de un solo módulo)

```
src/
├── main/java/pe/gob/onp/template/defuncion/
│   ├── domain/
│   │   ├── model/           # ConsultaDefuncion (Aggregate Root), Dni, EstadoDefuncion (VOs)
│   │   ├── port/in/         # ConsultarEstadoDefuncionUseCase
│   │   ├── port/out/        # ConsultaDefuncionRepository, RegistroCivilDefuncionClient
│   │   └── exception/       # Jerarquía de excepciones de dominio propia
│   ├── application/usecase/ # ConsultarEstadoDefuncionServiceImpl — POJO puro
│   └── infrastructure/
│       ├── web/             # Controller, DTOs HTTP, GlobalExceptionHandler, ApiResponseWrapper
│       ├── persistence/     # Entity JPA, Spring Data repo, Adapter, Mapper
│       ├── client/          # RegistroCivilHttpAdapter (Resilience4j) + DTO del contrato externo
│       └── config/          # @Configuration: wiring del caso de uso, cliente HTTP, OpenAPI, OTEL
│   └── TemplateMicroservicioApplication.java
├── main/resources/          # application*.yml (perfiles de ambiente), logback-spring.xml
└── test/java/                # Unitarias + ArchUnit + prueba de Circuit Breaker
docs/adr/
db/
├── migration/                # Flyway — V{MAJOR}.{MINOR}.{PATCH}__descripcion.sql (LIN-BD-ORA-001 §8.4)
└── reverse/                   # Reversa manual — U{MAJOR}.{MINOR}.{PATCH}__descripcion.sql
k8s/
├── base/                     # Deployment + Service + ConfigMap + NetworkPolicy (sin Job/CronJob)
└── overlays/                 # dev, qa, prod
```

## Regla de pureza hexagonal — verificada por ArchUnit, no solo documentada

`domain/` y `application/` son POJOs puros. Prohibido `@Service`, `@Component`, `@Autowired`, `@Transactional`, JPA, Jackson **y Resilience4j** en esos dos paquetes — el Circuit Breaker/Bulkhead es un detalle exclusivo del Adapter de infraestructura (`infrastructure.client.RegistroCivilHttpAdapter`), nunca del puerto de salida puro que declara el dominio. El wiring a Spring (incluida `@Transactional`) ocurre exclusivamente en `infrastructure.config.UseCaseConfig`, en clases `@Configuration` con métodos `@Bean`.

`src/test/java/.../architecture/ArquitecturaHexagonalTest.java` hace cumplir esto con **7 reglas ArchUnit** que corren en cada `mvn test`:

1. `domain`/`application` no dependen de JPA.
2. `domain`/`application` no dependen de Spring.
3. `domain`/`application` no dependen de Jackson.
4. `domain`/`application` no dependen de Resilience4j (regla propia de esta plantilla, no presente en `template-backend-java-modular`, donde Resilience4j es excepcional).
5. `domain`/`application` no dependen de `infrastructure`.
6. `domain` no depende de `application`.
7. Arquitectura en capas: `infrastructure` → `application` → `domain`, nunca al revés (regla `Architectures.layeredArchitecture()` de ArchUnit).

Verificado ejecutando el build real: `mvn test` reporta **"Tests run: 7"** en esta clase (ver "Resultado real de la verificación" abajo) — no es una intención documental, es un gate que rompe el build si alguien introduce una violación.

## Circuit Breaker + Bulkhead obligatorios (`LIN-DIS-001 §6.2/§6.3`, `DIS-R-009`)

A diferencia de `template-backend-java-modular` (donde Resilience4j es **excepcional** y requiere ADR aprobado por Arquitectura), en un Microservicio el Circuit Breaker es **obligatorio** para todo punto de salida hacia un servicio externo. `RegistroCivilHttpAdapter` lo demuestra con:

- `@CircuitBreaker(name = "registroCivil", fallbackMethod = "consultarFallback")` — máquina de estados que replica la tabla normativa de `§6.2` (ventana móvil de 100 peticiones, apertura si >50% fallan, 30s abierto, 10 peticiones de prueba en semi-abierto).
- `@Bulkhead(name = "registroCivil")` — semáforo de 50 llamadas concurrentes, en capa adicional al pool de conexiones de Apache HttpClient 5 (ver Javadoc de la clase para el razonamiento de las dos capas).
- Timeout fino vía Apache HttpClient 5 (`RegistroCivilClientConfig`) — categoría "Alta demanda / Ruta crítica interactiva" de `§6.1` (connect ≤1.5s, read ≤3s), coherente con `§6.4` ("fail-fast, sin reintento" para esa categoría).
- El *fallback* traduce cualquier fallo técnico a `ServicioRegistroCivilNoDisponibleException`, que `GlobalExceptionHandler` mapea a HTTP 503.

## Cobertura de pruebas

`LIN-TEST-001 §5.1` (`TEST-R-001`) norma la fila **"Microservicio"**: ≥70% instrucción global, dominio/casos de uso ≥85%. Resultado real de esta plantilla (`target/site/jacoco/jacoco.csv`, ver detalle abajo):

- **Global (BUNDLE):** gate `jacoco:check` en `mvn verify` — **cumplido** ("All coverage checks have been met").
- **`domain` (model + exception):** 29/31 líneas cubiertas ≈ **93.5%**.
- **`application.usecase`:** 10/10 líneas cubiertas — **100%**.

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java JDK | 21 | Eclipse Temurin recomendado |
| Maven | 3.9+ | |
| Docker | 24+ | Para build de imagen |
| kubectl + kustomize | 1.27+ / 5+ | Para despliegue en K8s |
| Oracle (local, remoto o Testcontainers) | — | Necesario para `mvn verify` con pruebas de integración reales — no incluidas en este scaffold, ver "Qué NO incluye este scaffold" |

## Ejecución local

```bash
mvn verify                                              # compila, prueba, cobertura, checkstyle
mvn spring-boot:run -Dspring-boot.run.profiles=dev       # ejecuta localmente

curl http://localhost:8080/api/v1/defunciones/12345678
```

## Qué NO incluye este scaffold (a propósito)

- **Prueba de contexto Spring completo (`@SpringBootTest`) del tipo `TemplateBackendApplicationTests`.** Este microservicio declara un `DataSource` real (Oracle). Cargar el contexto completo exigiría Testcontainers Oracle con acceso a Docker, no disponible en el entorno de verificación offline de este scaffold — misma decisión ya tomada por `template-worker-java` en esta sesión. `infrastructure.web.ConsultaDefuncionControllerTest` sí usa `@WebMvcTest` (contexto acotado, sin datasource) y prueba el controlador real.
- **Prueba de integración `@DataJpaTest` + Testcontainers Oracle** para `ConsultaDefuncionJpaAdapter` (a diferencia de `AfiliadoJpaAdapterTest` en `template-backend-java-modular`, que sí la trae). `ConsultaDefuncionJpaAdapterTest` es una prueba unitaria con Mockito sobre `ConsultaDefuncionJpaRepository` — mismo motivo offline. Un equipo que adopte esta plantilla debe agregar su propia prueba de integración con Testcontainers Oracle antes de pasar a QA (`LIN-TEST-001`).
- **WireMock** para la prueba del Circuit Breaker. No está en el repositorio Maven local de este entorno offline. Se usó `MockRestServiceServer` (incluido en `spring-boot-starter-test`) para simular el servidor externo, combinado con la API programática de Resilience4j (`CircuitBreaker.executeSupplier`) para demostrar la máquina de estados real reaccionando a fallos reales del adapter real — ver `RegistroCivilHttpAdapterTest` y su Javadoc para el detalle exacto de qué se prueba y por qué.

## Decisiones de diseño resueltas

1. **[Cerrado] `maven-surefire-plugin` 3.5.3 (heredado de `spring-boot-starter-parent:3.5.0`) no ejecuta las pruebas de ArchUnit.** Con la versión heredada, `mvn test` reportaba `Tests run: 0` para `ArquitecturaHexagonalTest` — sin ningún error visible. Se fijó `<version>3.5.4</version>` explícitamente en el `pom.xml` de esta plantilla. `LIN-DEV-JAVA-001 §14.5` (Plugins estándar obligatorios en CI) ya fija esta versión para todo proyecto, con la explicación completa del hallazgo.
2. **[Pendiente] Conflicto entre `checkstyle-onp.xml` (`ConstantName`, exige MAYÚSCULAS) y la convención `lower_snake_case` de ArchUnit para campos `@ArchTest`.** `template-backend-java-modular` no choca con esta regla solo porque su `maven-checkstyle-plugin` raíz no fija `includeTestSourceDirectory=true` (no analiza tests); esta plantilla sí lo fija (mismo criterio que `template-worker-java`), así que el conflicto aparece aquí primero. Se resolvió localmente renombrando los campos a MAYÚSCULAS_CON_GUION_BAJO (cumple `checkstyle-onp.xml`, que es copia controlada y no se modifica desde esta plantilla) — sigue pendiente como hallazgo de gobierno: unificar el criterio de `includeTestSourceDirectory` entre plantillas, o documentar explícitamente en `LIN-DEV-JAVA-001` la convención de nombres esperada para reglas `@ArchTest`.
3. **[Cerrado] `Resilience4j` no distingue un umbral de fallos propio para el estado SEMI-ABIERTO.** `LIN-DIS-001 §6.2` describía el semi-abierto como "si al menos 8 de 10 tienen éxito" (20% de fallo), distinto y más estricto que el 50% del estado CERRADO — irreconciliable con la librería, que solo expone un `failureRateThreshold` único compartido por ambos estados. **Cerrado**: `LIN-DIS-001 §6.2` se corrigió para usar el mismo umbral del 50% (≥5 de 10 exitosas) en ambos estados, alineado con lo que Resilience4j permite configurar por anotación sin código a medida. `failure-rate-threshold: 50` en `application.yml` ya es la norma, no una desviación.
4. **[Cerrado] `images.name` en los overlays de Kustomize debe ser la ruta COMPLETA de la imagen, no el nombre corto.** Se detectó que `k8s/overlays/dev` de `template-backend-java-modular` tenía este mismo defecto: su `images.name: api-nombre-sistema` no hacía match contra la imagen completa del `Deployment` base, así que el overlay de `dev` nunca sustituía el tag `1.0.0` por `dev-latest`. **Cerrado**: corregido en esta plantilla y también en `template-backend-java`, `template-backend-java-modular` y `template-worker-java` (las 9 overlays dev/qa/prod afectadas), verificado comparando la salida real de `kubectl kustomize` antes/después. Registrado en `LIN-VER-001` v0.1.9.
5. **Timeout de Resilience4j:** no se usó `@TimeLimiter` porque el adapter es síncrono (`RestClient`) y `@TimeLimiter` exige que el método decorado devuelva `CompletableFuture` — el timeout ya lo garantiza Apache HttpClient 5 (`responseTimeout`), consistente con `LIN-DIS-001 §6.1` ("todo adaptador configura estos controles con Apache HttpClient 5").

## Verificación realizada

```bash
mvn compile              # BUILD SUCCESS
mvn test                 # 24 pruebas, 0 fallos (incluye 7 de ArchUnit y 5 del Circuit Breaker)
mvn checkstyle:check      # 0 violaciones
mvn verify                # BUILD SUCCESS — gate JaCoCo cumplido, jar reempaquetado por Spring Boot
kubectl kustomize k8s/base            # válido
kubectl kustomize k8s/overlays/dev    # válido — tag de imagen sustituido correctamente
kubectl kustomize k8s/overlays/qa     # válido
kubectl kustomize k8s/overlays/prod   # válido
```

## Qué debe personalizar el equipo

1. Renombrar `spring.application.name`, el paquete `pe.gob.onp.template.defuncion` y el `artifactId`/`groupId` del `pom.xml` al microservicio real (convención `onp-{modulo}`, `LIN-DEV-JAVA-001 §14.2`).
2. Reemplazar el dominio de ejemplo (verificación de defunción) por el Bounded Context real, manteniendo la separación `domain`/`application`/`infrastructure` y las 7 reglas ArchUnit.
3. Reemplazar `onp.cliente.registro-civil.*` y `RegistroCivilHttpAdapter` por el/los cliente(s) externo(s) real(es) del microservicio — cada uno con su propia instancia Resilience4j nombrada.
4. Ajustar los umbrales de `resilience4j.circuitbreaker`/`bulkhead` en `application.yml` según la categoría real de demanda del servicio externo (`LIN-DIS-001 §6.1`).
5. Reemplazar manifiestos K8s placeholder por los del sistema real y decidir `replicas`/recursos por ambiente.
6. Ajustar la `NetworkPolicy` a los namespaces reales.
7. Ajustar endpoints OTEL por entorno si Plataforma lo indica.
8. Agregar pruebas de integración con Testcontainers Oracle antes de pasar a QA (ver "Qué NO incluye este scaffold").

## Contacto

- **Líder técnico:** [nombre]
- **Arquitectura OTI:** arquitectura@onp.gob.pe
- **Plataforma/Infraestructura:** [canal interno]
