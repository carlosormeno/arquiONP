> **⚠ DOCUMENTO ARCHIVADO — Referencia histórica**
> Este documento cumplió su propósito como guía de implementación transitoria. Su contenido fue absorbido por los lineamientos institucionales:
> - §§1–2 (Telemetría, Trazas, Logging) → **LIN-OBS-001**
> - §3 (ApiResponseWrapper, filtros, OpenAPI) → **LIN-DEV-JAVA-001** y **LIN-API-REST-001**
>
> Para iniciar un proyecto nuevo, seguir **LIN-DEV-JAVA-001 §1.3 — Configuración inicial de un proyecto nuevo**.
> Este archivo se conserva únicamente como referencia histórica. No actualizar.

---

**GUÍA PARA DESARROLLO** *(Archivada)*

**Java 21 --- Spring Boot 3.5**

Telemetría y Trazas · Logging · Documentación de APIs

  -----------------------------------------------------------------------
  **Versión:**                 0.1.2
  ---------------------------- ------------------------------------------
  **Fecha:**                   2026-05-26

  **Estado:**                  **ARCHIVADA** — ver LIN-DEV-JAVA-001 §1.3

  **Clasificación:**           Uso Interno (Técnico)

  **Dirigido a:**              Equipo de Desarrollo (referencia histórica)

  **Área responsable:**        OTI
  -----------------------------------------------------------------------

# Historial de versiones

  -------------------------------------------------------------------------------------
  **Versión**   **Fecha**      **Autor**    **Descripción**
  ------------- -------------- ------------ ---------------------------------------
  0.1.0         2026-05-13     Arquitectura OTI    Versión inicial (borrador)

  0.1.1         2026-05-20     Arquitectura OTI    Correcciones: snippet ApiResponseWrapper completo (constructor y getters); imports agregados en OpenApiConfig; aclaración traceId/trace.id en A.2; índice logs unificado a onp-logs-* en sección 2.4 y A.3.

  0.1.2         2026-05-26     Arquitectura OTI    Sincronización con lineamientos: corrección orden de filtros (RequestIdFilter @Order(1) → SaaTokenValidationFilter @Order(2) → CanonicalRequestLogFilter @Order(3)); adición de Marco normativo; reemplazo de bloques IMPORTANTE normativos por referencias a LIN-OBS-001 y LIN-API-REST-001.

  -------------------------------------------------------------------------------------

# Marco normativo

Esta guía es el **complemento de implementación** de los lineamientos institucionales ONP. Los lineamientos establecen el **qué** (reglas y requisitos obligatorios). Esta guía establece el **cómo** (código concreto para Spring Boot 3.5). En caso de conflicto, el lineamiento prevalece.

| Sección de esta guía | Lineamiento autoritativo |
|---|---|
| §1 — Telemetría y Trazas | LIN-OBS-001 — Lineamiento de Log, Trazabilidad y Observabilidad |
| §2 — Logging | LIN-OBS-001 — Lineamiento de Log, Trazabilidad y Observabilidad |
| §3 — Documentación de APIs | LIN-API-REST-001 — Lineamiento Estándar de APIs REST |

---

# Sección 1 --- Telemetría y Trazas

La telemetría distribuida permite conocer en tiempo real qué está haciendo cada servicio, cuánto tarda cada operación y dónde ocurren los errores. En ONP, las trazas se implementan sobre OpenTelemetry (OTEL) y se visualizan en Jaeger.

Esta sección cubre todo lo necesario para que un servicio web Java/Spring Boot emita trazas al stack de observabilidad institucional: dependencias, configuración, convenciones y casos de uso con ejemplos de código.

## 1.1 Introducción

### 1.1.1 ¿Qué es la instrumentación?

Instrumentar un servicio significa agregar capacidad de observabilidad al código para que, durante la ejecución, el servicio emita señales que describan qué está haciendo, cuánto tarda y si ocurrió algún error.

En ONP, la instrumentación produce trazas distribuidas: registros detallados del recorrido de cada petición HTTP a través del servicio, incluyendo las consultas a Oracle, las llamadas a servicios externos (RENIEC, SUNAT, etc.) y las operaciones de negocio relevantes.

  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **IMPORTANTE:** La instrumentación con OpenTelemetry es obligatoria para todos los servicios web desplegados en DEV, QA y PROD. Ver **LIN-OBS-001** para los requisitos normativos completos. Un servicio no instrumentado es invisible para el equipo de operaciones y dificulta el diagnóstico de problemas en producción.
  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### 1.1.2 Cómo encaja con el stack de observabilidad ONP

El flujo completo una vez que el servicio está instrumentado es el siguiente:

-   El servicio web recibe una petición HTTP.

-   La librería OTEL de Spring Boot genera automáticamente los spans (HTTP, SQL, llamadas externas).

-   Los spans se envían al OTEL Collector del entorno correspondiente.

-   El OTEL Collector enruta las trazas a Jaeger para su almacenamiento en Elasticsearch.

-   El equipo de desarrollo y operaciones consulta las trazas en el Jaeger UI.

### 1.1.3 Base documental

La instrumentación se basa en las OpenTelemetry Semantic Conventions, que son a las trazas lo que OpenAPI es a la documentación de APIs: una especificación formal que garantiza que los atributos sean consistentes e interoperables entre herramientas.

  -----------------------------------------------------------------------------------------------------------------
  **Documento**                        **URL**
  ------------------------------------ ----------------------------------------------------------------------------
  OpenTelemetry Semantic Conventions   https://opentelemetry.io/docs/specs/semconv/

  Spring Boot Actuator + OTEL          https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/actuator.html

  Micrometer Tracing                   https://micrometer.io/docs/tracing

  OpenTelemetry Java SDK               https://opentelemetry.io/docs/languages/java/
  -----------------------------------------------------------------------------------------------------------------

## 1.2 Conceptos clave

Esta sección explica los términos que el desarrollador encontrará en el código y en el Jaeger UI. No es necesario conocerlos en profundidad para instrumentar un servicio, pero sí para interpretar correctamente lo que se ve en Jaeger.

### 1.2.1 Traza (Trace)

Una traza representa el recorrido completo de una petición a través del sistema. Agrupa todos los pasos que ocurrieron para atenderla, desde que entró hasta que se devolvió la respuesta. En el Jaeger UI, cada fila en los resultados de búsqueda es una traza.

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **NOTA:** Ejemplo: cuando un usuario consulta su estado de pensión, esa petición genera una traza que incluye la validación del DNI, la consulta a Oracle y la llamada a RENIEC para verificar identidad.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### 1.2.2 Span

Un span es una unidad de trabajo dentro de una traza. Cada operación relevante genera su propio span: recibir la petición HTTP, ejecutar una consulta SQL, llamar a un servicio externo, ejecutar un método de negocio. Cada span tiene un nombre, una duración, un estado (OK o ERROR) y atributos que describen su contexto.

  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **NOTA:** Ejemplo: dentro de la traza anterior habría un span para \'POST /api/consulta-pension\' (15ms), otro para \'SELECT \* FROM PENSION WHERE DNI=?\' (8ms) y otro para \'GET reniec.gob.pe/verifica\' (120ms).
  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### 1.2.3 Contexto de traza (Trace Context)

El contexto de traza es la información que permite conectar todos los spans de una misma petición bajo la misma traza. Incluye un traceId y un spanId únicos. Spring Boot propaga este contexto automáticamente en las cabeceras HTTP salientes (W3C Trace Context: traceparent). No es necesario hacerlo manualmente.

### 1.2.4 Instrumentación automática vs. manual

  ------------------------------------------------------------------------------------------------------------------------------------
  **Tipo**        **Qué cubre**                                                                                  **Requiere código**
  --------------- ---------------------------------------------------------------------------------------------- ---------------------
  Automática      Peticiones HTTP entrantes y salientes, consultas JPA/SQL, llamadas RestTemplate/WebClient      No

  Manual          Operaciones de negocio que el desarrollador quiere visibilizar explícitamente con \@NewSpan   Sí (mínimo)
  ------------------------------------------------------------------------------------------------------------------------------------

## 1.3 Configuración inicial del proyecto

Esta sección contiene los cambios únicos que se realizan una sola vez al incorporar telemetría al proyecto: las dependencias en el `pom.xml` y los archivos de configuración de cada entorno. Una vez completada, no es necesario volver a ella.

### 1.3.1 Dependencias Maven

Agregar las siguientes dependencias al `pom.xml` del servicio, dentro del bloque `<dependencies>...</dependencies>`. Spring Boot 3.5 gestiona automáticamente las versiones compatibles; no es necesario especificar versiones manualmente.

Las siguientes dependencias son nuevas — no vienen en un proyecto generado con Spring Initializr. Agregarlas al `pom.xml`:

```xml
<!-- ═══════════════════════════════════════════════════════════ -->
<!-- OBSERVABILIDAD --- OpenTelemetry + Micrometer Tracing -->
<!-- ═══════════════════════════════════════════════════════════ -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
<!-- AOP: requerido para que @NewSpan funcione en runtime -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

Además, verificar que `spring-boot-starter-actuator` esté presente. Spring Initializr lo incluye si se seleccionó al crear el proyecto; si no está, agregarlo:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Notas:**
- `spring-boot-starter-web` ya viene en el proyecto desde Spring Initializr — **no agregarlo de nuevo**, causaría duplicado.
- `spring-boot-starter-aop` es requerido para que la anotación `@NewSpan` funcione en runtime. Sin él, la anotación compila pero el span no se crea.
- `spring-boot-starter-data-jpa` **solo agregarlo si el servicio usa Oracle u otra base de datos**. No incluirlo en servicios sin base de datos — Spring Boot intentará configurar un datasource al arrancar y fallará.

Verificar que el parent del proyecto sea Spring Boot 3.5:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.XX</version>
    <relativePath/>
</parent>
```

> **ADVERTENCIA:** No agregar versiones explícitas a las dependencias de Micrometer y OpenTelemetry si se usa el parent de Spring Boot 3.5. Spring Boot gestiona las versiones compatibles automáticamente a través de su BOM.

### 1.3.2 application.yml — configuración base

**Dónde:** Solo en `src/main/resources/application.yml` (el archivo base sin sufijo de perfil). Spring Boot lo carga siempre, sin importar el entorno activo. No repetir esta configuración en los archivos de cada entorno.

Agregar el siguiente bloque. Si `spring.application.name` ya existe en el archivo (lo genera Spring Initializr), verificar que siga la convención ONP y no duplicarlo:

```yaml
# ═══════════════════════════════════════════════════════════════
# OBSERVABILIDAD --- OpenTelemetry
# ═══════════════════════════════════════════════════════════════
spring:
  application:
    name: onp-<sistema>-<modulo>

info:
  app:
    version: "@project.version@"

management:
  info:
    env:
      enabled: true
  otlp:
    tracing:
      export:
        protocol: http/protobuf
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

**Notas:**
- `spring.application.name: onp-<sistema>-<modulo>` → nombre del servicio en Jaeger, Prometheus y logs. Formato obligatorio: prefijo `onp-`, sistema en minúsculas, módulo en minúsculas (ej. `onp-pensiones-liquidacion`). Ver tabla completa en **Apéndice A.3**.
- `info.app.version: "@project.version@"` → Maven reemplaza esto automáticamente con el valor de `<version>` del `pom.xml` al compilar. No modificar manualmente.
- `management.otlp.tracing.export.protocol: http/protobuf` → protocolo HTTP (puerto 4318) para enviar trazas al OTEL Collector. Alternativa: `grpc` (puerto 4317).
- `management.endpoints.web.exposure.include` → expone los endpoints de Actuator: `health` (estado), `info` (versión), `metrics` (métricas internas), `prometheus` (formato scrape para Prometheus).

Los tres archivos de entorno (DEV, QA, PROD) deben existir en el repositorio desde el inicio. Kubernetes inyecta la variable `SPRING_PROFILES_ACTIVE=dev` (o `qa` o `prod`) y Spring Boot carga automáticamente el archivo correspondiente.

**Sobre las URLs del OTEL Collector:** Las URLs de las secciones siguientes siguen el formato estándar de Kubernetes:

```
http://<nombre-servicio>.<namespace>.svc.cluster.local:<puerto>/v1/<señal>
```

Son valores fijos definidos por el equipo de Plataforma (OTI) e iguales para todos los servicios ONP. Si en algún momento cambian los namespaces o el nombre del Collector, Plataforma comunicará los nuevos valores.

### 1.3.3 DEV --- application-dev.yml

**Obligatorio.** Sin este archivo el servicio no sabe a qué OTEL Collector enviar trazas y logs en DEV — arrancará pero no emitirá telemetría.

**Dónde:** `src/main/resources/application-dev.yml`

```yaml
management:
  otlp:
    tracing:
      endpoint: http://otel-collector.otel-dev.svc.cluster.local:4318/v1/traces
    logging:
      endpoint: http://otel-collector.otel-dev.svc.cluster.local:4318/v1/logs
  tracing:
    sampling:
      probability: 1.0

otel:
  resource:
    attributes: deployment.environment=development

app:
  environment: development
```

### 1.3.4 QA --- application-qa.yml

**Obligatorio.** Mismo criterio que DEV — sin este archivo el servicio no emite telemetría en QA.

**Dónde:** `src/main/resources/application-qa.yml`

```yaml
management:
  otlp:
    tracing:
      endpoint: http://otel-collector.otel-qa.svc.cluster.local:4318/v1/traces
    logging:
      endpoint: http://otel-collector.otel-qa.svc.cluster.local:4318/v1/logs
  tracing:
    sampling:
      probability: 1.0

otel:
  resource:
    attributes: deployment.environment=quality

app:
  environment: quality
```

### 1.3.5 PROD --- application-prod.yml

**Obligatorio.** Mismo criterio. El `sampling.probability: 0.1` en PROD significa que solo se traza el 10% de las peticiones para evitar saturar Elasticsearch con alto volumen de tráfico.

**Dónde:** `src/main/resources/application-prod.yml`

```yaml
management:
  otlp:
    tracing:
      endpoint: http://otel-collector.otel.svc.cluster.local:4318/v1/traces
    logging:
      endpoint: http://otel-collector.otel.svc.cluster.local:4318/v1/logs
  tracing:
    sampling:
      probability: 0.1

otel:
  resource:
    attributes: deployment.environment=production

app:
  environment: production
```

> **ADVERTENCIA:** El sampling.probability define qué porcentaje de peticiones se traza. En DEV y QA usar 1.0 (100%). En PROD empezar con 0.1 (10%) y ajustar según el volumen real. Un valor de 1.0 en PROD con alto tráfico puede saturar Elasticsearch.

### 1.3.6 Activar el perfil en Kubernetes

> **Nota:** Este paso lo ejecuta el equipo de Plataforma (OTI), no el desarrollador. Se incluye aquí para que el desarrollador entienda el mecanismo completo y pueda coordinarlo en cada pase a DEV, QA o PROD.

Para que Spring Boot cargue el `application-dev.properties` correcto al arrancar en Kubernetes, Plataforma debe agregar la siguiente variable de entorno en el `Deployment` del servicio:

```yaml
containers:
  - name: onp-<sistema>-<modulo>
    image: <IMAGE>
    env:
      - name: SPRING_PROFILES_ACTIVE
        value: dev  # "dev", "qa" o "prod" según el entorno de despliegue
```

Sin esta variable, Spring Boot carga únicamente `application.properties` base y el servicio arranca sin emitir telemetría.

**Responsabilidades en cada pase:**

| Responsable   | Tarea |
|---------------|-------|
| Desarrollador | Crear y mantener `application-dev.properties`, `application-qa.properties` y `application-prod.properties` en el proyecto |
| Plataforma    | Configurar `SPRING_PROFILES_ACTIVE` con el valor correcto en el Deployment de Kubernetes de cada entorno |

**Documento de pase — ítem obligatorio:**

```
[ ] Verificar que el Deployment del servicio tenga configurada la variable de entorno:
      SPRING_PROFILES_ACTIVE = <dev|qa|prod>
    Responsable: Plataforma (OTI)
    Validación: kubectl get deployment <nombre-servicio> -n <namespace> -o jsonpath='{.spec.template.spec.containers[0].env}'
```

## 1.4 Instrumentación

Cada sub-punto sigue la misma estructura: **Caso** (cuándo aplica), **Qué aplicar** (qué código o configuración se necesita), **Dónde aplicar** (capa y archivo concreto), ejemplo de código, y **Qué aparece en Jaeger** para verificar el resultado.

> **NOTA — Rutas de archivo en esta guía:** Los ejemplos usan `<paquete-base>` para indicar el paquete raíz del proyecto. Reemplazarlo con el paquete real convirtiendo los puntos en barras. Por ejemplo, `pe.gob.onp.afiliaciones` → `src/main/java/pe/gob/onp/afiliaciones/...`.

---

### 1.4.1 Petición HTTP entrante (automático)

**Caso:** El servicio recibe una petición HTTP desde un cliente, navegador u otro microservicio. Es el punto de entrada de toda traza.

**Qué aplicar:** Nada. Spring Boot instrumenta automáticamente todos los endpoints anotados con `@RestController`.

**Dónde aplicar:** No aplica — no hay código que agregar en ninguna capa.

**Qué aparece en Jaeger:** Un span raíz con los siguientes atributos:

| Atributo OTEL | Valor ejemplo |
|---|---|
| http.method | POST |
| http.route | /api/afiliaciones |
| http.status_code | 200 |
| service.name | onp-afiliaciones-registro |

---

### 1.4.2 Consulta a BD via JPA/Hibernate (automático)

**Caso:** El servicio consulta o persiste datos en Oracle a través de un `@Repository` con JPA/Hibernate.

**Qué aplicar:** Nada. Hibernate genera automáticamente un span hijo para cada query SQL ejecutado.

**Dónde aplicar:** No aplica — no hay código que agregar al Repository.

```java
@Repository
public interface AfiliadoRepository extends JpaRepository<Afiliado, Long> {
    Optional<Afiliado> findByDni(String dni); // trazado automáticamente
}
```

**Qué aparece en Jaeger:** Un span hijo bajo el span HTTP con `db.system = oracle`, `db.statement` con la query SQL y la duración exacta de la consulta.

> **NOTA:** Por seguridad, Hibernate enmascara los valores de los parámetros en el `db.statement` (muestra `?` en lugar del valor real). Esto es el comportamiento correcto y no debe modificarse.

---

### 1.4.3 Llamada HTTP saliente con RestTemplate (semi-automático)

**Caso:** El servicio llama a un sistema externo (RENIEC, SUNAT, otro microservicio ONP) usando `RestTemplate`. Micrometer Tracing propaga el header `traceparent` en la llamada saliente para que Jaeger muestre la cadena completa entre servicios.

**Qué aplicar:** Registrar `RestTemplate` como `@Bean` gestionado por Spring. Si se instancia con `new RestTemplate()`, el interceptor de trazas no se inyecta y las llamadas salientes quedan invisibles.

**Dónde aplicar:** En una clase `@Configuration` en el paquete `config/`. Si no existe, crear `src/main/java/<paquete-base>/config/HttpConfig.java`.

```java
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
```

Luego inyectar y usar en el Service:

```java
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReniecService {

    private final RestTemplate restTemplate;

    public ReniecService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DatosPersona consultarDni(String dni) {
        String url = "https://api.reniec.gob.pe/personas/" + dni;
        return restTemplate.getForObject(url, DatosPersona.class);
    }
}
```

**Qué aparece en Jaeger:** Un span hijo con `http.method`, `http.url` y `http.status_code`. Si el servicio destino también tiene trazas activas, Jaeger enlaza ambos servicios bajo el mismo `traceId` mostrando la cadena completa.

> **IMPORTANTE:** Nunca usar `new RestTemplate()`. Si se instancia manualmente, Spring no puede inyectar el interceptor de trazas.

---

### 1.4.4 Manejo de errores y excepciones

Este punto tiene dos sub-casos con comportamiento distinto según si la excepción es capturada o no.

#### Caso A — Excepción no capturada (automático)

**Caso:** Un método lanza una excepción sin bloque `catch` que la capture. OTEL detecta que el método terminó con error y marca el span como `ERROR` automáticamente.

**Qué aplicar:** Nada. No se requiere código adicional.

**Dónde aplicar:** No aplica.

```java
// Sin try/catch → OTEL marca el span ERROR automáticamente
public void operacionConError() {
    throw new RuntimeException("Error de prueba para verificar trazas en Jaeger");
}
```

**Qué aparece en Jaeger:** El span se muestra en rojo con `otel.status_code = ERROR` y el stack trace adjunto.

#### Caso B — Excepción capturada con try/catch (manual)

**Caso:** Un método captura la excepción con `try/catch`. OTEL no la detecta porque desde su perspectiva el método terminó sin error — el span queda verde aunque haya un error de negocio.

**Qué aplicar:** Dentro del bloque `catch`, llamar a `Span.current().setStatus()` y `Span.current().recordException()` antes de relanzar la excepción.

**Dónde aplicar:** En la capa **Service** (`@Service`), dentro del bloque `catch`. No en el Controller ni en el Repository.

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

public Pension calcularPension(String dni) {
    try {
        return repositorio.buscarPorDni(dni);
    } catch (DniNoEncontradoException e) {
        Span.current().setStatus(StatusCode.ERROR, e.getMessage());
        Span.current().recordException(e); // adjunta el stack trace al span en Jaeger
        throw e;
    }
}
```

**Qué aparece en Jaeger:** El span se muestra en rojo con el stack trace adjunto, igual que en el Caso A.

---

**Resumen: cuándo necesitas código**

| Situación | Span en Jaeger | ¿Código manual? |
|---|---|---|
| `throw` sin `catch` | ERROR automático | No |
| `catch` sin código manual | OK incorrecto (verde) | **Sí** |
| `catch` con `setStatus` + `recordException` | ERROR correcto | Sí |
| `catch` que maneja sin relanzar | OK (span termina normal) | Solo si quieres registrarlo |

> **Error silencioso:** Sin el código manual en el `catch`, el HTTP devuelve 404/500 pero Jaeger muestra todos los spans verdes — imposible detectar en qué servicio falló la cadena en una arquitectura de microservicios.

---

### 1.4.5 Operación de negocio con @NewSpan (manual)

**Caso:** Un método del Service realiza una operación costosa o crítica (cálculo de beneficio, validación compleja, integración externa) que debe aparecer como span independiente en Jaeger para medir su duración y detectar cuellos de botella.

**Qué aplicar:** Anotar el método con `@NewSpan("nombre.del.span")` siguiendo el formato `<sistema>.<modulo>.<operacion-en-kebab-case>`.

**Dónde aplicar:** En métodos de la capa **Service** (`@Service`). No en Controllers (la petición HTTP ya tiene su span automático) ni en Repositories (JPA ya los instrumenta).

```java
import io.micrometer.tracing.annotation.NewSpan;

@NewSpan("pensiones.liquidacion.calcular-beneficio")
public BigDecimal calcularBeneficio(String dni, int aniosAportados) {
    // lógica de negocio crítica
    return monto;
}
```

**Qué aparece en Jaeger:** Un span hijo con el nombre configurado y la duración exacta del método.

**Convenciones de nombre** (`<sistema>.<modulo>.<operacion-en-kebab-case>`):

| Ejemplo válido | Descripción |
|---|---|
| afiliaciones.registro.validar-datos | Validación de datos de afiliación |
| pensiones.liquidacion.calcular-beneficio | Cálculo del beneficio de pensión |
| consultas.reniec.verificar-identidad | Verificación de identidad en RENIEC |
| consultas.sunat.obtener-ruc | Consulta de RUC en SUNAT |

Ver tabla completa en **Apéndice A.3**.

> **ADVERTENCIA:** No abusar de `@NewSpan`. Usarlo solo en operaciones costosas o críticas. Demasiados spans manuales dificultan la lectura en Jaeger UI.

> **¿Qué pasa si no tengo capa Service?** En servicios muy simples el span HTTP del Controller es suficiente. Cuando el servicio tenga validaciones, cálculos o llamadas externas, crear la capa Service y aplicar `@NewSpan` en sus métodos.

> **Importante:** Si el IDE muestra `NewSpan cannot be resolved to a type`, ejecutar `mvn dependency:resolve` o recargar el proyecto Maven desde el IDE.

---

### 1.4.6 Atributos personalizados en un span

**Caso:** Se necesita agregar datos de contexto de negocio al span (tipo de afiliación, régimen, módulo, etc.) para facilitar el diagnóstico en Jaeger sin tener que buscar en los logs.

**Qué aplicar:** Dentro del método, llamar a `Span.current().setAttribute("clave", valor)` con los datos relevantes. Solo incluir datos que no sean sensibles.

**Dónde aplicar:** En métodos de la capa **Service** (`@Service`), dentro de un método anotado con `@NewSpan` o durante cualquier traza activa.

```java
import io.opentelemetry.api.trace.Span;

@NewSpan("afiliaciones.registro.validar-datos")
public boolean validarDatos(SolicitudAfiliacion solicitud) {
    Span.current().setAttribute("afiliacion.tipo", solicitud.getTipo());
    Span.current().setAttribute("afiliacion.regimen", solicitud.getRegimen());
    return ejecutarValidacion(solicitud);
}
```

**Qué aparece en Jaeger:** Los atributos aparecen en la pestaña "Tags" del span al hacer clic sobre él en el Jaeger UI.

> **IMPORTANTE:** Nunca incluir datos sensibles (DNI, contraseñas, tokens) como atributos de span. Ver política No PII en la Sección 2.

---

### 1.4.7 WebClient — llamada HTTP saliente reactiva (semi-automático)

**Caso:** El servicio usa `WebClient` (API reactiva) en lugar de `RestTemplate` para llamadas HTTP salientes. El principio es el mismo que en 1.4.3: la instrumentación automática solo funciona si el bean es gestionado por Spring.

**Qué aplicar:** Registrar `WebClient` como `@Bean` usando `WebClient.Builder` inyectado por Spring. No usar `WebClient.create()` directamente.

**Dónde aplicar:** En una clase `@Configuration` en el paquete `config/`. Puede ser la misma clase que el `RestTemplate` del 1.4.3 si el proyecto usa ambos, aunque se recomienda elegir uno y ser consistente.

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
```

Luego inyectar y usar en el Service:

```java
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SunatService {

    private final WebClient webClient;

    public SunatService(WebClient webClient) {
        this.webClient = webClient;
    }

    public DatosRuc consultarRuc(String ruc) {
        return webClient.get()
                .uri("https://api.sunat.gob.pe/ruc/" + ruc)
                .retrieve()
                .bodyToMono(DatosRuc.class)
                .block();
    }
}
```

**Qué aparece en Jaeger:** Igual que en 1.4.3: un span hijo con `http.method`, `http.url` y `http.status_code`, enlazado al servicio destino si también tiene trazas activas.

> **IMPORTANTE:** Nunca usar `WebClient.create()`. Si se instancia sin el builder inyectado por Spring, la propagación del `traceparent` no funcionará.

---

### 1.4.8 Tareas programadas con @Scheduled (manual)

**Caso:** Un job en background se ejecuta sin petición HTTP que lo origine (proceso nocturno, reconciliación, envío de notificaciones). OTEL no tiene span padre al que enlazarlo, por lo que no genera trazas automáticamente.

**Qué aplicar:** Crear el span manualmente usando `Tracer` dentro del método `@Scheduled`, cerrándolo siempre en el bloque `finally`.

**Dónde aplicar:** En la clase anotada con `@Scheduled`, típicamente en el paquete `scheduler/` o `job/`.

```java
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReconciliacionJob {

    private final Tracer tracer;

    public ReconciliacionJob(Tracer tracer) {
        this.tracer = tracer;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutar() {
        Span span = tracer.nextSpan().name("pensiones.batch.reconciliacion-diaria").start();
        try (var ws = tracer.withSpan(span)) {
            span.tag("batch.tipo", "reconciliacion");
            // lógica del job
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

**Qué aparece en Jaeger:** Una traza independiente sin span padre HTTP, con su propio `traceId`. Se puede buscar por nombre de operación (`pensiones.batch.reconciliacion-diaria`) en el Jaeger UI.

> **Por qué el `finally` es obligatorio:** Si el span no se cierra con `span.end()`, queda abierto en memoria y nunca se envía al OTEL Collector. El `finally` garantiza el cierre aunque el job falle.

---

### 1.4.9 @ContinueSpan — enriquecer el span activo (manual)

**Caso:** Un sub-método auxiliar necesita agregar atributos al span que ya está activo, sin crear un span hijo nuevo. Útil para validaciones rápidas o pasos secundarios cuya duración no justifica un nivel extra en Jaeger.

**Qué aplicar:** Anotar el método con `@ContinueSpan` y marcar con `@SpanTag` los parámetros que deben agregarse como atributos al span activo.

**Dónde aplicar:** En métodos de la capa **Service** (`@Service`) llamados desde un método que ya tiene un span activo (anotado con `@NewSpan` o llamado desde un endpoint HTTP).

```java
import io.micrometer.tracing.annotation.ContinueSpan;
import io.micrometer.tracing.annotation.SpanTag;

@ContinueSpan
public void validarRequisitos(
        @SpanTag("afiliacion.regimen") String regimen,
        @SpanTag("afiliacion.tipo") String tipo) {
    if (regimen == null || regimen.isBlank()) {
        throw new RequisitosInvalidosException("Régimen requerido");
    }
}
```

**Qué aparece en Jaeger:** Los valores de `regimen` y `tipo` aparecen como atributos en el span padre activo. No se crea un nivel adicional en la traza.

**Diferencia clave entre `@NewSpan` y `@ContinueSpan`:**

| | `@NewSpan` | `@ContinueSpan` |
|---|---|---|
| Crea span hijo | Sí | No |
| Visible como nivel separado en Jaeger | Sí | No |
| Útil para | Operaciones costosas o críticas | Validaciones y pasos auxiliares rápidos |
| `@SpanTag` en parámetros | Sí | Sí |

> **ADVERTENCIA:** No usar `@ContinueSpan` si no hay un span activo en el hilo — el comportamiento es indefinido. Siempre llamar desde dentro de un método `@NewSpan` o desde un endpoint HTTP.

## 1.5 Verificación de la instrumentación

### 1.5.1 Verificar en los logs de la aplicación

Spring Boot 3.5 no emite un mensaje explícito de "OTLP activo". La confirmación indirecta se obtiene verificando que el servicio arrancó correctamente y que, al realizar una petición, el `traceId` aparece en los logs:

```json
{
  "@timestamp": "2026-...",
  "message": "Operacion completada",
  "log.logger": "pe.gob.onp...",
  "traceId": "d611cfa9851c9f00b36acf61b2d68b93",
  "spanId": "33e564c7ce255b35",
  "service.name": "onp-<sistema>-<modulo>"
}
```

Si `traceId` y `spanId` aparecen en el log, el puente OTEL–Logback está activo y las trazas se están generando. Si están ausentes, verificar que las dependencias de la Sección 1.3.1 están en el `pom.xml` y que el perfil activo es el correcto.

### 1.5.2 Verificar en el Jaeger UI

1.  Realizar al menos una petición HTTP al servicio.

2.  Acceder al Jaeger UI del entorno correspondiente (ver Manual de Instalación Jaeger, Sección 9).

3.  En el campo Service seleccionar el nombre del servicio (onp-\<sistema\>-\<modulo\>).

4.  Hacer clic en Find Traces.

5.  Verificar que aparece la traza correspondiente a la petición realizada.

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **ADVERTENCIA:** Si el servicio no aparece en Jaeger UI verificar: (1) que el OTEL Collector está corriendo en el namespace correspondiente, (2) que la URL del Collector en application.properties es correcta, (3) que la petición llegó al servicio. Revisar logs del OTEL Collector con kubectl logs.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

### 1.5.3 Interpretar una traza en Jaeger UI

  -----------------------------------------------------------------------------------------------
  **Span**                               **Qué representa**                    **Generado por**
  -------------------------------------- ------------------------------------- ------------------
  HTTP POST /api/endpoint                Petición HTTP entrante al servicio    Automático

  SELECT \... FROM \... WHERE \...       Consulta a Oracle via JPA             Automático

  HTTP GET https://reniec.gob.pe/\...    Llamada al servicio externo           Automático

  \<sistema\>.\<modulo\>.\<operacion\>   Operación de negocio con \@NewSpan   Manual
  -----------------------------------------------------------------------------------------------

# Sección 2 --- Logging

Los logs son el segundo pilar de observabilidad. Mientras las trazas responden ¿qué recorrido siguió la petición?, los logs responden ¿qué eventos ocurrieron y cuál fue su contexto exacto?. Ambos se complementan: el trace.id presente en cada log permite pasar directamente de una línea en Kibana a la traza correspondiente en Jaeger.

Los servicios web de ONP escriben logs en formato JSON estructurado y los exportan directamente al OTEL Collector via OTLP (protocolo HTTP, puerto 4318). El OTEL Collector los reenvía a Elasticsearch, donde son visualizables en Kibana. El flujo es: **Servicio → OTEL Collector → Elasticsearch → Kibana**.

El formato JSON lo gestiona Logback. La exportación al Collector se configura en el `application.properties` de cada entorno (ver Sección 1.3). La retención de logs en Elasticsearch es responsabilidad del equipo de Plataforma (DEV: 30 días, QA: 30 días, PROD: 90 días).

## 2.1 Introducción

El formato de los logs sigue el Elastic Common Schema (ECS), que es el estándar de Elastic para garantizar que los campos sean reconocidos automáticamente por Kibana sin configuración adicional. Los campos que cada log incluye automáticamente (timestamp, service.name, trace.id, etc.) están documentados en el **Apéndice A.2**.

Referencia oficial ECS: https://www.elastic.co/guide/en/ecs/current

> **Alineación con OpenTelemetry Semantic Conventions:** Los nombres de campo usados en esta guía (`http.request.method`, `url.path`, `http.response.status_code`, `user.id`, `service.name`, `trace.id`, `span.id`) coinciden con las [OpenTelemetry Semantic Conventions](https://opentelemetry.io/docs/specs/semconv/), que desde 2023 están alineadas con ECS. Esta convergencia no es accidental: garantiza que los campos sean reconocibles por herramientas del ecosistema OTEL (Jaeger, Grafana, Collector) sin configuración adicional, y que los logs puedan correlacionarse directamente con las trazas exportadas por el agente Java de OpenTelemetry.

## 2.2 Configuración inicial del proyecto

Esta sección contiene los cambios que se realizan **una sola vez** al incorporar logging estructurado al proyecto: la dependencia Maven, el archivo de configuración de Logback y la clase utilitaria de enmascaramiento. Completar los tres antes de escribir cualquier log en el código.

### 2.2.1 Dependencias Maven

Agregar dentro del bloque `<dependencies>` del `pom.xml`:

```xml
<!-- Logstash Encoder: formato JSON para Logback compatible con ECS -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>

<!-- Puente Logback → OTEL SDK: envía logs al OTEL Collector vía OTLP -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-logback-appender-1.0</artifactId>
    <version>2.14.0-alpha</version>
</dependency>
```

> **Nota sobre la versión `-alpha`:** El sufijo `-alpha` en artefactos de `io.opentelemetry.instrumentation` **no indica inestabilidad de código** — indica que la API de extensión del SDK está en tier "incubating". Esta librería es ampliamente usada en producción.

> **ADVERTENCIA — La versión del appender depende de la versión de Spring Boot:** Esta dependencia debe compilar contra el mismo OTel SDK que gestiona Spring Boot. Usar una versión incorrecta causa `AbstractMethodError` al arrancar. La compatibilidad es:
>
> | Spring Boot | OTel SDK gestionado | Versión del appender |
> |---|---|---|
> | 3.4.x | ~1.44.x | `2.10.0-alpha` |
> | 3.5.x | 1.49.x | `2.14.0-alpha` |
>
> Para verificar qué OTel SDK gestiona el proyecto antes de definir la versión:
> ```bash
> mvn dependency:tree -Dincludes=io.opentelemetry:opentelemetry-sdk
> ```
> La versión del SDK (ej. `1.49.0`) determina qué versión del appender usar. **No usar versiones superiores a las indicadas** — también causan `AbstractMethodError`.

### 2.2.2 logback-spring.xml

**Cuándo:** Crear este archivo una sola vez por proyecto. Configura Logback para emitir logs en formato JSON compatible con ECS, incluyendo automáticamente los campos `trace.id` y `span.id` de OTEL en cada línea de log.

**Dónde:** Crear el archivo `src/main/resources/logback-spring.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="APP_NAME"
        source="spring.application.name"/>
    <springProperty scope="context" name="APP_VERSION"
        source="info.app.version" defaultValue="unknown"/>
    <springProperty scope="context" name="APP_ENV"
        source="app.environment" defaultValue="development"/>

    <!-- Envía logs al OTEL Collector vía OTLP. Requiere OpenTelemetryLogbackConfig. -->
    <appender name="OpenTelemetry"
        class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
        <captureExperimentalAttributes>true</captureExperimentalAttributes>
    </appender>

    <appender name="JSON_STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>
            {
                "service.name": "${APP_NAME}",
                "service.version": "${APP_VERSION}",
                "service.environment": "${APP_ENV}"
            }
            </customFields>
            <fieldNames>
                <timestamp>@timestamp</timestamp>
                <level>log.level</level>
                <logger>log.logger</logger>
                <thread>process.thread.name</thread>
                <stackTrace>error.stack_trace</stackTrace>
            </fieldNames>
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
            <includeMdcKeyName>http.request.id</includeMdcKeyName>
            <includeMdcKeyName>user.id</includeMdcKeyName>
            <includeMdcKeyName>http.request.method</includeMdcKeyName>
            <includeMdcKeyName>url.path</includeMdcKeyName>
            <includeMdcKeyName>http.response.status_code</includeMdcKeyName>
            <includeMdcKeyName>duration_ms</includeMdcKeyName>
        </encoder>
    </appender>

    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="JSON_STDOUT"/>
            <appender-ref ref="OpenTelemetry"/>
        </root>
        <logger name="pe.gob.onp" level="DEBUG"/>
    </springProfile>
    <springProfile name="qa,prod">
        <root level="INFO">
            <appender-ref ref="JSON_STDOUT"/>
            <appender-ref ref="OpenTelemetry"/>
        </root>
    </springProfile>
</configuration>
```

> **Nota:** Los campos `traceId` y `spanId` se inyectan automáticamente en el MDC por OTEL. Cada línea de log los incluirá sin código adicional, permitiendo pasar directamente de un log en Kibana a su traza en Jaeger. Requiere que las dependencias de OTEL estén presentes (ver Sección 1.3).

### 2.2.3 Clase utilitaria Mask

**Cuándo:** Crear esta clase una sola vez por proyecto, antes de escribir cualquier log que involucre datos del usuario. Es la implementación de la política No PII (ver 2.3.2) — sin ella, el programador no tiene forma segura de enmascarar datos sensibles.

**Dónde:** Crear el archivo `src/main/java/<paquete-base>/util/Mask.java`. Es una clase utilitaria pura sin dependencias de Spring — no lleva `@Component`.

```java
public final class Mask {

    private Mask() {}

    public static String dni(String value) {
        if (value == null || value.length() < 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }

    public static String phone(String value) {
        if (value == null || value.length() < 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }

    public static String email(String value) {
        if (value == null || !value.contains("@")) return "***@***.***";
        String[] parts = value.split("@");
        return parts[0].charAt(0) + "***@***" +
            parts[1].substring(parts[1].lastIndexOf('.'));
    }

    public static String partial(String value, int visibleChars) {
        if (value == null || value.length() <= visibleChars) return "****";
        return "****" + value.substring(value.length() - visibleChars);
    }
}
```

### 2.2.4 OpenTelemetryLogbackConfig

**Cuándo:** Crear esta clase una sola vez por proyecto. Conecta el `OpenTelemetryAppender` de Logback con la instancia `OpenTelemetry` gestionada por Spring Boot. Sin esta clase, el appender captura los logs pero los descarta porque Spring Boot no registra su instancia como global de OTel.

**Dónde:** `src/main/java/<paquete-base>/config/OpenTelemetryLogbackConfig.java`

```java
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryLogbackConfig {

    private final OpenTelemetry openTelemetry;

    public OpenTelemetryLogbackConfig(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @PostConstruct
    public void installLogbackAppender() {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
```

> **Por qué es necesaria:** Spring Boot 3.5 crea el bean `OpenTelemetrySdk` (con `SdkLoggerProvider` y el exportador OTLP) pero **no llama** `GlobalOpenTelemetry.set()`. El `OpenTelemetryAppender` en `logback-spring.xml` usa `GlobalOpenTelemetry.get()`, que sin esta llamada devuelve un no-op. El `@PostConstruct` de esta clase instala el bean correcto después de que el contexto de Spring está inicializado.

### 2.2.5 Filtro de log canónico (CanonicalRequestLogFilter)

**Cuándo:** Crear este filtro una sola vez por proyecto, junto con el `RequestIdFilter`. Implementa el **log canónico de request**: emite una única línea de log estructurado al finalizar cada petición HTTP con todos los campos relevantes — método, ruta, status, duración y usuario. Además propaga el campo `user.id` en el MDC para que todas las líneas de log de esa petición incluyan automáticamente el usuario autenticado.

**Dónde:** `src/main/java/<paquete-base>/filter/CanonicalRequestLogFilter.java`.

**Requiere:** El `RequestIdFilter` (sección 3.3.2) y el `SaaTokenValidationFilter` (sección 3.3.3) deben ejecutarse antes que este filtro (`@Order(1)` y `@Order(2)` respectivamente). Este filtro lee el campo `user.id` directamente del MDC — que el `SaaTokenValidationFilter` ya pobló — por lo que no depende de Spring Security para resolver el usuario.

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(3)
public class CanonicalRequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CanonicalRequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        // user.id ya fue puesto en el MDC por SaaTokenValidationFilter @Order(2).
        // Este filtro lo lee del MDC — no consulta SecurityContextHolder.
        StatusCapturingResponse wrapped = new StatusCapturingResponse(response);
        try {
            chain.doFilter(request, wrapped);
        } finally {
            int status = wrapped.getStatus();
            long duration = System.currentTimeMillis() - start;
            // Campos del log canónico se agregan al MDC temporalmente para que
            // el OpenTelemetryAppender los capture como atributos OTEL hacia ES.
            MDC.put("http.request.method",       request.getMethod());
            MDC.put("url.path",                  request.getRequestURI());
            MDC.put("http.response.status_code", String.valueOf(status));
            MDC.put("duration_ms",               String.valueOf(duration));
            if (status >= 500)      log.error("REQUEST");
            else if (status >= 400) log.warn("REQUEST");
            else                    log.info("REQUEST");
            MDC.remove("http.request.method");
            MDC.remove("url.path");
            MDC.remove("http.response.status_code");
            MDC.remove("duration_ms");
            MDC.remove("user.id");
        }
    }

    private static class StatusCapturingResponse extends HttpServletResponseWrapper {
        private int status = 200;

        StatusCapturingResponse(HttpServletResponse response) { super(response); }

        @Override public void setStatus(int sc)                              { status = sc; super.setStatus(sc); }
        @Override public void sendError(int sc) throws IOException           { status = sc; super.sendError(sc); }
        @Override public void sendError(int sc, String m) throws IOException { status = sc; super.sendError(sc, m); }
        @Override public int  getStatus()                                    { return status; }
    }
}
```

**Qué hace:**

1. **Captura el usuario autenticado** desde Spring Security al inicio de la petición y lo coloca en MDC — todas las líneas de log de esa request incluirán `user.id` automáticamente.
2. **Envuelve la response** con `StatusCapturingResponse` para poder leer el status HTTP al finalizar (el status no es accesible directamente hasta que la respuesta se compromete).
3. **Mide la duración total** de la petición, incluyendo Spring Security, lógica de negocio y serialización de la respuesta.
4. **Emite una única línea de log** al finalizar la petición con todos los campos como campos JSON independientes — no embebidos en el texto del mensaje.
5. **Selecciona el nivel de log** según el status HTTP: `INFO` para 2xx, `WARN` para 4xx, `ERROR` para 5xx.

**Ejemplo de log canónico — petición exitosa:**

```json
{
  "@timestamp": "2026-05-21T10:00:00.000Z",
  "log.level": "INFO",
  "message": "REQUEST",
  "http.request.method": "POST",
  "url.path": "/api/v1/afiliaciones",
  "http.response.status_code": "201",
  "duration_ms": "143",
  "user.id": "jperez",
  "http.request.id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "trace.id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "service.name": "onp-pensiones-afiliacion"
}
```

**Ejemplo de log canónico — error interno:**

```json
{
  "@timestamp": "2026-05-21T10:01:00.000Z",
  "log.level": "ERROR",
  "message": "REQUEST",
  "http.request.method": "GET",
  "url.path": "/api/v1/afiliaciones/9999",
  "http.response.status_code": "500",
  "duration_ms": "38",
  "user.id": "mgarcia",
  "http.request.id": "b2c3d4e5-f6a7-8901-bcde-fa2345678901",
  "trace.id": "5cf03f4688c45eb7b4df030e1f1f5847",
  "service.name": "onp-pensiones-afiliacion"
}
```

> **Nota sobre el orden de filtros (LIN-OBS-001):** Los tres filtros de observabilidad deben registrarse en este orden:
>
> | @Order | Filtro | Responsabilidad |
> |---|---|---|
> | 1 | `RequestIdFilter` | Genera o propaga `X-Request-ID`; lo pone en MDC como `http.request.id` |
> | 2 | `SaaTokenValidationFilter` | Valida el token SAA; pone `user.id` en MDC |
> | 3 | `CanonicalRequestLogFilter` | Lee `user.id` de MDC y emite el log canónico al finalizar la petición |
>
> Todos corren dentro del contexto de Spring Security (posterior al `FilterChainProxy`, orden -100) — `SecurityContextHolder` y el MDC del `RequestIdFilter` están disponibles cuando el `CanonicalRequestLogFilter` los necesita.

### 2.2.6 Logs de query SQL con P6Spy

**Cuándo:** Configurar una sola vez por proyecto, únicamente en el entorno DEV. Permite ver en Kibana qué queries SQL ejecutó cada request, cuánto tardaron y correlacionarlas con el `trace.id` y `user.id` de la petición.

> **No activar en QA ni PROD.** P6Spy intercepta cada query a nivel del driver JDBC e introduce overhead medible. En esos entornos, activarlo solo si se investiga un problema de rendimiento específico y desactivarlo inmediatamente después.

**Dónde:** Tres cambios: `pom.xml`, `application-dev.properties` y `src/main/resources/spy.properties`.

**1. Dependencia Maven:**

```xml
<!-- P6Spy: intercepta el driver JDBC para loggear queries con duración -->
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

**2. `application-dev.properties` — cambiar el URL del datasource:**

```properties
# Prefijo p6spy: activa el interceptor. P6Spy delega al driver real automáticamente.
spring.datasource.url=jdbc:p6spy:<DRIVER>://<HOST>:<PORT>/<BASE_DE_DATOS>
spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver
```

Reemplazar `<DRIVER>` con el tipo de base de datos del proyecto: `postgresql`, `sqlserver`, `mysql`, etc.

**3. `src/main/resources/spy.properties`:**

```properties
# Redirige los logs de P6Spy a SLF4J — el output sale como JSON a través de Logback
appender=com.p6spy.engine.spy.appender.Slf4JLogger
slf4jLogLevel=DEBUG

# Formato: duración en ms seguida del SQL ejecutado
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(executionTime)ms | %(sql)

# Solo loggear queries que tarden más de este umbral (ms). 0 = loggear todas.
executionThreshold=0

# Excluir eventos sin valor diagnóstico
excludecategories=info,debug,result,resultset
```

**Ejemplo de log de query SQL en Kibana:**

```json
{
  "@timestamp": "2026-05-21T10:00:00.120Z",
  "log.level": "DEBUG",
  "log.logger": "p6spy",
  "message": "143ms | SELECT a.id, a.regimen, a.estado FROM afiliados a WHERE a.id = 1",
  "trace.id": "4bf92f3577b34da6a3ce929d0e0e4736",
  "http.request.id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "user.id": "jperez",
  "service.name": "onp-pensiones-afiliacion"
}
```

Los campos `trace.id`, `http.request.id` y `user.id` aparecen automáticamente porque están en el MDC de la petición. El SQL y su duración quedan en el campo `message`.

**Buscar queries lentas en Kibana (DEV):**

Filtrar en Discover con: `log.logger: "p6spy"`. Ordenar por `@timestamp` y revisar la columna `message` para identificar queries con duración elevada. Para investigar una query específica lenta, anotar su `trace.id` y buscarlo en Jaeger para ver el contexto completo de la petición.

## 2.3 Uso en el código

Una vez completada la configuración inicial (2.2), aplicar las siguientes reglas en cada clase que escriba logs.

### 2.3.1 Niveles de log

| Nivel | Cuándo usarlo | Configuración |
|---|---|---|
| INFO | Eventos relevantes del flujo normal del negocio | Activo en todos los entornos |
| DEBUG | Detalle técnico para diagnóstico | Activo solo en DEV |
| WARN | Situaciones inesperadas pero recuperables | Activo en todos los entornos |
| ERROR | Errores que impiden completar la operación. Siempre con stacktrace | Activo en todos los entornos |

**Setup del logger — obligatorio en toda clase que use logs**

Antes de usar `log.info`, `log.error`, etc., cada clase debe declarar el logger. SLF4J viene incluido con `spring-boot-starter-web` — no requiere dependencia adicional.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Primer campo de la clase:
private static final Logger log = LoggerFactory.getLogger(NombreDeLaClase.class);
```

Reemplazar `NombreDeLaClase` con el nombre exacto de la clase donde se declara.

---

**Dónde usar cada nivel por capa:**

**Controller (`@RestController`)**
- `INFO` → entrada de la petición con parámetros de ruta (no PII). Útil para correlacionar con trazas en Jaeger.
- `WARN` → parámetros inválidos o situaciones anómalas que no llegaron al Service.
- `ERROR` → no loggear aquí. Los errores se manejan en el Service o en el `@ExceptionHandler` global.
- No repetir en el Controller lo que el Service ya loggeó.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AfiliacionController {

    private static final Logger log = LoggerFactory.getLogger(AfiliacionController.class);

    @GetMapping("/afiliaciones/{id}")
    public AfiliacionDto obtener(@PathVariable Long id) {
        log.info("GET /afiliaciones/{} solicitado", id);
        return service.obtener(id);
    }
}
```

**Service (`@Service`)**
- `INFO` → eventos de negocio: inicio de operación, resultado exitoso, decisiones de negocio.
- `DEBUG` → detalle técnico (solo DEV): valores intermedios, respuestas de sistemas externos.
- `WARN` → situaciones anómalas pero recuperables: reintentos, fallbacks, datos incompletos.
- `ERROR` → fallos que impiden completar la operación, siempre con la excepción como segundo parámetro.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AfiliacionService {

    private static final Logger log = LoggerFactory.getLogger(AfiliacionService.class);

    public void registrar(SolicitudAfiliacion solicitud) {
        log.info("Solicitud de afiliacion recibida para regimen: {}", solicitud.getRegimen());
        log.debug("Respuesta de RENIEC: {}", Mask.partial(response.toString(), 4));
        log.warn("Timeout en SUNAT, reintentando... intento: {}", intento);
        log.error("Error al procesar solicitud de afiliacion", e);
    }
}
```

**Repository (`@Repository`)**
- No loggear. JPA/Hibernate genera sus propios logs de query a nivel `DEBUG`.
- Excepción: si hay una query nativa con lógica compleja, un `DEBUG` puntual es aceptable.

**Component (`@Component`) y clases de infraestructura**
- Misma lógica que el Service: `INFO` para eventos relevantes, `WARN` para situaciones anómalas, `ERROR` con excepción.
- Aplica a interceptores HTTP, listeners de eventos, convertidores, clientes de sistemas externos.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ReniecClient {

    private static final Logger log = LoggerFactory.getLogger(ReniecClient.class);

    public DatosPersona consultar(String dni) {
        log.info("Consultando RENIEC para DNI: {}", Mask.dni(dni));
        log.warn("Respuesta de RENIEC demorada {}ms, umbral: {}ms", tiempo, umbral);
        log.error("Fallo al conectar con RENIEC", e);
    }
}
```

**Scheduled / Jobs (`@Scheduled`)**
- `INFO` → inicio y fin del job con métricas (registros procesados, omitidos, tiempo total).
- `WARN` → registros individuales con problemas no críticos que no detienen el job.
- `ERROR` → fallos que detienen o comprometen el resultado del job, siempre con excepción.

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ReconciliacionJob {

    private static final Logger log = LoggerFactory.getLogger(ReconciliacionJob.class);

    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutar() {
        log.info("Job reconciliacion iniciado. Periodo: {}", periodo);
        log.warn("Registro omitido por datos incompletos. ID: {}", id);
        log.info("Job reconciliacion finalizado. Procesados: {}, Omitidos: {}, Tiempo: {}ms",
                 procesados, omitidos, tiempo);
        log.error("Job reconciliacion fallido en registro ID: {}", id, e);
    }
}
```

### 2.3.2 Política No PII — datos sensibles

Está estrictamente prohibido registrar datos personales o sensibles en los logs. El incumplimiento constituye una vulneración a la Ley de Protección de Datos Personales (Ley N.° 29733).

Usar la clase `Mask` (creada en 2.2.3) para enmascarar datos antes de incluirlos en cualquier log.

| Dato sensible | Acción requerida | Método Mask | Ejemplo resultado |
|---|---|---|---|
| DNI / documento de identidad | Enmascarar | `Mask.dni(valor)` | `****1234` |
| Número de teléfono | Enmascarar | `Mask.phone(valor)` | `****5678` |
| Correo electrónico | Enmascarar | `Mask.email(valor)` | `u***@***.com` |
| Token / contraseña | No loggear nunca | — | Omitir completamente |
| Nombre completo | Usar ID interno | — | `ID: 1617582` |
| Número de cuenta | Enmascarar | `Mask.partial(valor, 4)` | `****9012` |

**Ejemplos de uso:**

```java
// Correcto — datos enmascarados
log.info("Consulta recibida para DNI: {}", Mask.dni(dni));
log.debug("Email del solicitante: {}", Mask.email(email));
log.info("Cuenta origen: {}", Mask.partial(numeroCuenta, 4));

// Incorrecto — datos en claro
log.info("Consulta recibida para DNI: {}", dni);           // PROHIBIDO
log.debug("Token de autenticacion: {}", token);             // PROHIBIDO
log.info("Nombre completo: {}", solicitud.getNombre());     // PROHIBIDO
```

### 2.3.3 Anti-patrones a evitar

Los siguientes anti-patrones son comunes en proyectos Java enterprise y tienen consecuencias concretas en producción: costos de almacenamiento, alertas ruidosas, imposibilidad de hacer queries en Kibana o riesgo legal.

---

**1. Logs multilínea**

Un log con saltos de línea en el mensaje puede romperse en el aggregator: el Collector lo interpreta como eventos separados, corrompiendo los campos JSON.

El `LogstashEncoder` configurado en la Sección 2.2.2 garantiza que cada evento es una sola línea JSON. Sin embargo, si el desarrollador embebe saltos de línea manualmente en el mensaje, ese contenido llega corrompido al Collector.

```java
// Correcto — mensaje sin saltos de línea
log.info("Solicitud rechazada. Motivo: {}", motivo);

// Incorrecto — el \n dentro del mensaje rompe el evento
log.info("Solicitud rechazada.\nMotivo: {}\nFecha: {}", motivo, fecha);
```

Para loggear errores con stacktrace, usar siempre la excepción como segundo argumento de `log.error()`. El `LogstashEncoder` la serializa como una cadena única en el campo `error.stack_trace`, sin romper el evento:

```java
// Correcto — stacktrace como segundo argumento
log.error("Error al procesar solicitud", e);

// Incorrecto — construir el mensaje manualmente con la traza
log.error("Error: " + e.getMessage() + "\n" + e.getStackTrace()[0]);
```

---

**2. Stacktraces gigantes**

Un stacktrace completo de una excepción Spring puede superar las 80 líneas. Indexado en Elasticsearch, infla el almacenamiento y contrarresta el beneficio de `best_compression`.

La causa más frecuente es re-loggear la misma excepción en múltiples capas: el Repository lanza, el Service logea y relanza, el Controller logea de nuevo. El resultado son tres copias del mismo stacktrace por fallo.

**Regla:** loggear cada excepción exactamente una vez, en la capa que la puede contextualizar con información de negocio.

```java
// Correcto — log con contexto en la capa de servicio, sin re-loggear
@Service
public class AfiliacionService {
    public void registrar(SolicitudAfiliacion solicitud) {
        try {
            repo.guardar(solicitud);
        } catch (DataAccessException e) {
            log.error("Fallo al persistir solicitud para regimen: {}", solicitud.getRegimen(), e);
            throw new AfiliacionException("Error interno al registrar la solicitud", e);
        }
    }
}

// Incorrecto — la excepción se logea en servicio y luego otra vez en el controller
// o en el @ExceptionHandler, produciendo duplicados
```

Para errores esperados y recuperables (recurso no encontrado, validación fallida), loggear solo el mensaje sin stacktrace — el stacktrace no aporta información en esos casos:

```java
// Correcto — WARN sin stacktrace para error de negocio esperado
log.warn("Afiliado no encontrado para ID: {}", id);
throw new RecursoNoEncontradoException("Afiliado no encontrado");

// Incorrecto — stacktrace de una excepción de negocio esperada
log.error("Afiliado no encontrado", new RecursoNoEncontradoException("..."));
```

---

**3. Loggear todo en INFO**

En un servicio con 100 peticiones por segundo, loggear cada entrada de método, cada parámetro y cada estado intermedio en INFO genera millones de líneas por día. Esto satura los dashboards, hace imposible detectar eventos reales y aumenta el costo de almacenamiento.

`INFO` es para **eventos de negocio** — decisiones tomadas, estados cambiados, operaciones completadas. No para traza de ejecución técnica.

```java
// Correcto — INFO solo para eventos relevantes del negocio
log.info("Solicitud de afiliacion registrada. ID: {}, Regimen: {}", id, regimen);

// Incorrecto — INFO para traza de ejecución interna
log.info("Entrando al metodo registrar()");
log.info("Validando solicitud...");
log.info("Solicitud valida, procediendo...");
log.info("Guardando en base de datos...");
log.info("Guardado exitoso");
log.info("Saliendo del metodo registrar()");
```

Si se necesita ese nivel de detalle para diagnóstico, usar `DEBUG` — está desactivado en QA y PROD por configuración (ver Sección 2.2.2).

---

**4. PII en logs**

Ver Sección 2.3.2. El incumplimiento tiene consecuencias legales bajo la Ley N.° 29733 de Protección de Datos Personales.

---

**5. Logs no estructurados**

`System.out.println()`, concatenación manual de strings y appenders de texto plano producen logs que no pueden consultarse por campo en Kibana. En un entorno cloud-native, los logs no estructurados son deuda técnica que obliga a parsear texto para extraer información.

El `LogstashEncoder` configurado en la Sección 2.2.2 garantiza que todos los logs emitidos por Logback salgan en JSON. Lo que debe evitarse es eludir Logback:

```java
// Correcto — a través de SLF4J/Logback, sale como JSON estructurado
log.info("Monto calculado: {}", monto);

// Incorrecto — bypasa Logback, sale como texto plano, invisible en Kibana
System.out.println("Monto calculado: " + monto);
System.err.println("Error: " + e.getMessage());
```

## 2.4 Verificación en Kibana

Una vez que el servicio esté desplegado y procesando peticiones, los logs aparecerán en Kibana bajo el patrón de índice `onp-logs-*`, que cubre todos los entornos:

| Entorno | Índice en Elasticsearch |
|---|---|
| DEV | onp-logs-development |
| QA | onp-logs-quality |
| PROD | onp-logs-production |

El OTEL Collector crea el índice automáticamente cuando llegan los primeros logs. Para verificar:

1. Acceder al Kibana UI del entorno correspondiente (ver Manual de Instalación Kibana, Sección 9).
2. Ir a **Discover** y seleccionar el Data View `onp-logs-*`.
3. Buscar por `service.name: "onp-<sistema>-<modulo>"` para filtrar los logs del servicio.
4. Verificar que los campos `trace.id` y `span.id` aparecen en cada log — confirma que la correlación OTEL funciona.

> **ADVERTENCIA:** Si el Data View `onp-logs-*` no existe en Kibana, debe crearlo el equipo de Plataforma o el responsable del stack de observabilidad (ver Manual de Instalación Kibana, Sección 10.3).

### 2.4.1 Verificar el log canónico de request

Cada petición HTTP emite una línea de log canónico al finalizar (emitida por `CanonicalRequestLogFilter`). Para verificar:

1. Ir a **Discover**, seleccionar el Data View `onp-logs-*`.
2. Agregar el filtro: `message: "REQUEST"`.
3. Verificar que cada request aparece como una línea con los campos `http.request.method`, `url.path`, `http.response.status_code`, `duration_ms` y `user.id`.

**Queries operacionales disponibles una vez que el log canónico está activo:**

| Pregunta | Query en Kibana |
|---|---|
| ¿Qué requests generó el usuario X? | `message: "REQUEST" AND user.id: "jperez"` |
| ¿Qué endpoints están fallando? | `message: "REQUEST" AND http.response.status_code: "500"` |
| ¿Qué requests tomaron más de 1 segundo? | `message: "REQUEST" AND duration_ms > 1000` |
| ¿Todos los logs de una petición específica? | `http.request.id: "a1b2c3d4-..."` |
| ¿Errores 4xx de un servicio? | `service.name: "onp-pensiones-afiliacion" AND http.response.status_code >= 400` |
| ¿Qué hizo un usuario durante un incidente? | `user.id: "mgarcia" AND @timestamp > "2026-05-21T10:00:00"` |

**Pasar de un log a su traza en Jaeger:**

Desde cualquier línea de log (canónico o de negocio), copiar el valor del campo `trace.id` e ingresarlo en Jaeger UI en el campo **Trace ID**. Esto muestra la traza completa de la petición que generó ese log.

# Sección 3 --- Documentación de APIs

Los servicios web de ONP exponen sus funcionalidades mediante APIs REST utilizando JSON como formato de intercambio de datos. **La documentación de cada API es obligatoria y no negociable**: todo servicio que exponga endpoints HTTP debe documentarlos siguiendo el estándar OpenAPI versión 3.0, visualizable a través de Swagger UI.

Esta sección define la configuración base, las anotaciones requeridas y el estándar de respuestas que deben seguir todos los servicios web.

## 3.1 Introducción

La documentación de APIs se basa en la especificación OpenAPI 3.0, el estándar de la industria para describir APIs REST de forma legible tanto para humanos como para herramientas automatizadas. En ONP, la documentación se genera automáticamente mediante anotaciones en el código usando SpringDoc, y es visualizable a través del Swagger UI.

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **IMPORTANTE:** La documentación OpenAPI/Swagger es obligatoria para todo servicio que exponga endpoints HTTP. Ver **LIN-API-REST-001** para los requisitos normativos completos. Un servicio sin documentación Swagger no se considera completo y no debe pasar a revisión de código.
  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

**Alcance — dónde aplica esta sección:**

Las anotaciones de Swagger se colocan **únicamente en las clases que manejan peticiones HTTP**, es decir, en los `@RestController` (o `@Controller`) del proyecto. No se documentan con Swagger:

- La capa Service (`@Service`) — es lógica interna, no expone endpoints.
- La capa Repository (`@Repository`) — es acceso a datos, no expone endpoints.
- Componentes y jobs (`@Component`, `@Scheduled`) — no son APIs REST.

Si el proyecto tiene múltiples Controllers, **cada uno debe estar documentado**.

Referencia oficial: https://spec.openapis.org/oas/v3.0.3

## 3.2 Configuración inicial del proyecto

### 3.2.1 Dependencia Maven

Agregar dentro del bloque `<dependencies>` del `pom.xml`:

```xml
<!-- SpringDoc: genera OpenAPI 3.0 y Swagger UI para Spring Boot 3.x -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

### 3.2.2 Configuración del Swagger UI

**Dónde:** Crear el archivo `src/main/java/<paquete-base>/config/OpenApiConfig.java`. Es una clase nueva — no modifica ninguna clase existente.

```java
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI(
            @Value("${spring.application.name}") String appName,
            @Value("${info.app.version:1.0.0}") String version,
            @Value("${app.description:Servicio web ONP}") String desc) {
        return new OpenAPI()
            .info(new Info()
                .title(appName)
                .version(version)
                .description(desc)
                .contact(new Contact()
                    .name("OTI --- Innovacion y Desarrollo")
                    .email("oti@onp.gob.pe")))
            .externalDocs(new ExternalDocumentation()
                .description("Documentacion interna ONP")
                .url("https://gitlab.onp.gob.pe"));
    }
}
```

### 3.2.3 Configuración por entorno

**application-dev.properties y application-qa.properties:**

```properties
swagger.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.api-docs.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
```

**application-prod.properties:**

```properties
swagger.enabled=${SWAGGER_ENABLED:false}
springdoc.swagger-ui.enabled=${SWAGGER_ENABLED:false}
springdoc.api-docs.enabled=${SWAGGER_ENABLED:false}
```

Para activar Swagger temporalmente en PROD sin redesplegar:

```yaml
env:
  - name: SWAGGER_ENABLED
    value: "true" # cambiar a false para volver a deshabilitarlo
```

> **ADVERTENCIA:** Recordar deshabilitar Swagger en PROD (SWAGGER_ENABLED=false) una vez terminada la actividad. La exposición permanente del API spec en producción es un riesgo de seguridad.

## 3.3 Implementación

Las siguientes anotaciones son obligatorias en **cada `@RestController` del proyecto**. Seguir el orden de esta sección: los puntos 3.3.1 y 3.3.2 crean las clases base que los demás puntos usan — deben existir antes de empezar a anotar endpoints.

---

### 3.3.1 Clase ApiResponseWrapper

**Cuándo:** Crear esta clase una sola vez por proyecto, antes de implementar cualquier endpoint. Es el tipo de retorno estándar que todos los Controllers usarán. Sin ella, las anotaciones Swagger de los endpoints no compilarán.

**Dónde:** Crear el archivo `src/main/java/<paquete-base>/dto/common/ApiResponseWrapper.java`. Es un DTO puro sin anotaciones de Spring.

```java
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Estructura de respuesta estandar ONP")
public class ApiResponseWrapper<T> {

    private Integer codHttp;
    private String codDetRespuesta;
    private String menDetRespuesta;
    private T data;
    private List<CampoError> errors;
    private Meta meta;

    public ApiResponseWrapper(Integer codHttp, String codDetRespuesta, String menDetRespuesta,
                              T data, List<CampoError> errors,
                              String requestId, String version) {
        this.codHttp = codHttp;
        this.codDetRespuesta = codDetRespuesta;
        this.menDetRespuesta = menDetRespuesta;
        this.data = data;
        this.errors = errors;
        this.meta = new Meta(Instant.now().toString(), requestId, version);
    }

    public static <T> ApiResponseWrapper<T> ok(T data, String requestId, String version) {
        return new ApiResponseWrapper<>(200, "000",
            "Operacion completada correctamente.", data, null, requestId, version);
    }

    public static <T> ApiResponseWrapper<T> error(int codHttp, String codDet,
            String msg, List<CampoError> errors, String requestId, String version) {
        return new ApiResponseWrapper<>(codHttp, codDet, msg, null, errors, requestId, version);
    }

    public Integer getCodHttp() { return codHttp; }
    public String getCodDetRespuesta() { return codDetRespuesta; }
    public String getMenDetRespuesta() { return menDetRespuesta; }
    public T getData() { return data; }
    public List<CampoError> getErrors() { return errors; }
    public Meta getMeta() { return meta; }

    @Schema(description = "Metadatos de la respuesta")
    public static class Meta {
        private String timestamp;
        private String requestId;
        private String version;

        public Meta(String timestamp, String requestId, String version) {
            this.timestamp = timestamp;
            this.requestId = requestId;
            this.version = version;
        }

        public String getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
        public String getVersion() { return version; }
    }

    @Schema(description = "Error de validacion de un campo")
    public static class CampoError {
        private String campo;
        private String mensaje;

        public CampoError(String campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public String getCampo() { return campo; }
        public String getMensaje() { return mensaje; }
    }
}
```

> **NOTA:** Esta clase no usa Lombok (`@Data`) para evitar dependencias adicionales. Si el proyecto ya tiene Lombok en el `pom.xml`, los getters y constructores pueden reemplazarse con `@Data` y `@AllArgsConstructor`.

---

### 3.3.2 Filtro de correlación RequestIdFilter

**Cuándo:** Crear este filtro una sola vez por proyecto, antes de implementar cualquier endpoint. Habilita el campo `meta.requestId` en todas las respuestas y lo propaga en los logs para correlacionar peticiones entre servicios.

**Dónde:** Crear el archivo `src/main/java/<paquete-base>/filter/RequestIdFilter.java`. Spring lo detecta automáticamente con `@Component` y lo aplica a todas las peticiones entrantes.

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest req,
            HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String requestId = req.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put("http.request.id", requestId);
        res.setHeader(HEADER, requestId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("http.request.id");
        }
    }
}
```

**Qué hace:**

1. Lee el header `X-Request-ID` de la petición entrante.
2. Si el cliente no lo envía, genera un UUID propio.
3. Agrega el `requestId` al MDC para que aparezca en todos los logs de esa petición.
4. Devuelve el `X-Request-ID` en el header de la respuesta para que el cliente pueda correlacionarlo.

---

### 3.3.3 Estructura de respuesta estándar

Todos los servicios web de ONP deben retornar sus respuestas usando `ApiResponseWrapper` (creado en 3.3.1). Esta estructura garantiza consistencia entre servicios y facilita la correlación con logs y trazas.

| Campo | Tipo | Descripción | Obligatorio |
|---|---|---|---|
| codHttp | Integer | Código de estado HTTP de la respuesta | Sí |
| codDetRespuesta | String | Código de respuesta institucional ONP (3 dígitos) | Sí |
| menDetRespuesta | String | Mensaje descriptivo del resultado | Sí |
| data | Object/Array/null | Resultado de la operación. Null en caso de error | Sí |
| errors | Array/null | Lista de errores de validación. Null si no aplica | Solo en validación |
| meta.timestamp | String | Fecha y hora de la respuesta en ISO 8601 UTC | Sí |
| meta.requestId | String | ID de correlación de la petición (X-Request-ID) | Sí |
| meta.version | String | Versión del servicio que respondió | Sí |

**Respuesta exitosa (200):**

```json
{
  "codHttp": 200,
  "codDetRespuesta": "000",
  "menDetRespuesta": "Operacion completada correctamente.",
  "data": { "idSolicitud": "SOL-2025-001234", "estado": "REGISTRADO" },
  "errors": null,
  "meta": { "timestamp": "2025-01-15T10:30:00Z",
            "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "version": "1.3.0" }
}
```

**Respuesta de error de validación (400):**

```json
{
  "codHttp": 400,
  "codDetRespuesta": "100",
  "menDetRespuesta": "Error de validacion en los datos enviados.",
  "data": null,
  "errors": [
    { "campo": "regimen", "mensaje": "El campo es obligatorio." },
    { "campo": "fechaInicioLaboral", "mensaje": "El formato debe ser ISO 8601." }
  ],
  "meta": { "timestamp": "2025-01-15T10:30:01Z",
            "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            "version": "1.3.0" }
}
```

---

### 3.3.4 Tabla de códigos codDetRespuesta[^1]

> **Referencia normativa:** Esta tabla es una copia de referencia rápida. La tabla completa y autoritativa se encuentra en **LIN-API-REST-001**. En caso de discrepancia, LIN-API-REST-001 prevalece.

| Código | Categoría | Descripción | codHttp |
|---|---|---|---|
| 000 | Éxito | Operación completada correctamente | 200 |
| 001 | Éxito parcial | Operación completada con advertencias | 200 |
| 100 | Validación | Error de validación en los datos enviados | 400 |
| 101 | Validación | Campo obligatorio ausente | 400 |
| 102 | Validación | Formato de dato incorrecto | 400 |
| 103 | Validación | Valor fuera del rango permitido | 400 |
| 200 | Negocio | Regla de negocio no cumplida | 422 |
| 201 | Negocio | Recurso no encontrado | 404 |
| 202 | Negocio | Recurso ya existe (duplicado) | 409 |
| 203 | Negocio | Estado inválido para la operación | 422 |
| 300 | Autorización | No autenticado | 401 |
| 301 | Autorización | No autorizado para esta operación | 403 |
| 400 | Integración | Servicio externo no disponible | 503 |
| 401 | Integración | Servicio externo respondió con error | 502 |
| 402 | Integración | Timeout en llamada a servicio externo | 504 |
| 500 | Sistema | Error interno del servidor | 500 |
| 501 | Sistema | Error de conexión a base de datos | 500 |
| 502 | Sistema | Error de configuración del servicio | 500 |

---

### 3.3.5 Anotaciones en el Controller

**Cuándo:** Una vez creadas `ApiResponseWrapper` y `RequestIdFilter`, anotar cada clase `@RestController` con `@Tag`.

**Dónde:** En la clase `@RestController`, antes de la declaración de la clase.

```java
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Afiliaciones",
     description = "Operaciones de registro y consulta de afiliados.")
@RestController
@RequestMapping("/api/v1/afiliaciones")
public class AfiliacionController { }
```

**Reglas para `@Tag`:**

| Atributo | Regla | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|---|
| `name` | Nombre del dominio de negocio en singular, en español, con mayúscula inicial. Sin la palabra "Controller" ni "API". | `Afiliaciones` | `AfiliacionController`, `API Afiliados` |
| `description` | Frase que describa qué agrupa el Controller. Terminar con punto. | `Operaciones de registro y consulta de afiliados.` | `Controller de afiliaciones`, `Endpoints de afiliados` |

> Si el proyecto tiene varios Controllers, cada uno debe tener un `name` distinto. Swagger UI agrupa los endpoints por ese valor — si dos Controllers comparten el mismo `name`, sus endpoints se mezclan en un solo grupo en la UI.

---

### 3.3.6 Anotaciones en endpoints

**Dónde:** Sobre cada método del Controller. Los imports son los mismos para todos los métodos HTTP:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
```

---

#### GET — Consulta por ID

Sin cuerpo de request. Documenta 200 cuando se encuentra el recurso y 404 cuando no existe.

```java
@Operation(
    summary = "Obtener afiliado por ID",
    description = "Retorna los datos de un afiliado a partir de su identificador interno.")
@ApiResponses({
    @ApiResponse(responseCode = "200",
        description = "Afiliado encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "404",
        description = "Afiliado no encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponseWrapper> obtener(@PathVariable Long id) { }
```

---

#### POST — Creación de recurso

Con cuerpo de request (`@RequestBody`). Documenta 400 para errores de validación en los datos enviados.

```java
@Operation(
    summary = "Registrar nueva solicitud de afiliacion",
    description = "Recibe los datos del solicitante, valida su elegibilidad " +
                  "y registra la solicitud en el sistema.")
@ApiResponses({
    @ApiResponse(responseCode = "201",
        description = "Solicitud registrada correctamente.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "400",
        description = "Error de validacion en los datos enviados.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@PostMapping
public ResponseEntity<ApiResponseWrapper> registrar(
        @RequestBody @Valid SolicitudAfiliacionRequest request) { }
```

---

#### PUT — Actualización de recurso

Combina `@PathVariable` (identificador) con `@RequestBody` (datos a actualizar). Documenta 400 y 404.

```java
@Operation(
    summary = "Actualizar datos de afiliado",
    description = "Actualiza los datos de un afiliado existente. " +
                  "Solo se modifican los campos incluidos en el cuerpo de la peticion.")
@ApiResponses({
    @ApiResponse(responseCode = "200",
        description = "Afiliado actualizado correctamente.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "400",
        description = "Error de validacion en los datos enviados.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "404",
        description = "Afiliado no encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@PutMapping("/{id}")
public ResponseEntity<ApiResponseWrapper> actualizar(
        @PathVariable Long id,
        @RequestBody @Valid ActualizarAfiliacionRequest request) { }
```

---

#### DELETE — Eliminación de recurso

Sin cuerpo de request ni de response. Devuelve 204 (sin contenido) en caso de éxito.

```java
@Operation(
    summary = "Eliminar afiliado",
    description = "Elimina el registro de un afiliado del sistema. La operacion es irreversible.")
@ApiResponses({
    @ApiResponse(responseCode = "204",
        description = "Afiliado eliminado correctamente."),
    @ApiResponse(responseCode = "404",
        description = "Afiliado no encontrado.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
    @ApiResponse(responseCode = "500",
        description = "Error interno del servidor.",
        content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
})
@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminar(@PathVariable Long id) { }
```

---

**Referencia rápida de códigos HTTP por método:**

| Método | 200 | 204 | 400 | 404 | 500 |
|---|---|---|---|---|---|
| GET | Recurso encontrado | — | — | No encontrado | Error servidor |
| POST | Operación exitosa | — | Datos inválidos | — | Error servidor |
| PUT | Actualizado | — | Datos inválidos | No encontrado | Error servidor |
| DELETE | — | Eliminado OK | — | No encontrado | Error servidor |

> **NOTA:** El 204 (DELETE exitoso) no lleva cuerpo de respuesta — por eso `ResponseEntity<Void>` y sin `content` en el `@ApiResponse`.

**Reglas para `@Operation`:**

| Atributo | Regla | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|---|
| `summary` | Verbo en infinitivo + objeto. Máximo 80 caracteres. Sin punto final. | `Registrar nueva solicitud de afiliacion` | `POST afiliacion`, `registrar()` |
| `description` | Qué recibe, qué valida, qué retorna. Terminar con punto. Opcional si el `summary` es claro. | `Recibe los datos del solicitante, valida su elegibilidad y registra la solicitud.` | Repetir el summary, dejar vacío |

**Reglas para `@ApiResponse`:**

| Atributo | Regla | Ejemplo correcto | Ejemplo incorrecto |
|---|---|---|---|
| `responseCode` | String con comillas. Documentar siempre 200/204, 400 (si hay body), 404 (si busca por ID), 500. | `"200"`, `"404"` | `200` sin comillas, omitir el 500 |
| `description` | Cuándo ocurre esa respuesta. Terminar con punto. | `Afiliado no encontrado.` | `Not found`, `404`, `Error` |
| `content` | Siempre `ApiResponseWrapper.class`. Omitir solo en 204. | Ver ejemplos | Omitir en 200, usar clase de dominio |

---

### 3.3.7 Anotaciones en modelos de request y response

**Cuándo:** Al crear o modificar clases DTO de request y response. Permite que Swagger UI muestre descripciones y ejemplos de cada campo en el formulario de prueba.

**Dónde:** En las clases DTO del paquete `dto/`.

```java
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de la solicitud de afiliacion")
public class SolicitudAfiliacionRequest {

    @Schema(description = "Tipo de regimen de afiliacion",
            example = "SPP",
            allowableValues = {"SPP", "SNP"})
    @NotBlank
    private String regimen;

    @Schema(description = "Fecha de inicio laboral en formato ISO 8601",
            example = "2024-01-15")
    @NotNull
    private LocalDate fechaInicioLaboral;
}
```

## 3.4 Verificación

Una vez desplegado el servicio, acceder al Swagger UI en la URL correspondiente:

  ------------------------------------------------------------------------------------------
  **Entorno**   **URL de acceso**                                **Estado**
  ------------- ------------------------------------------------ ---------------------------
  DEV           http://\<host-dev\>:\<puerto\>/swagger-ui.html   Siempre activo

  QA            http://\<host-qa\>:\<puerto\>/swagger-ui.html    Siempre activo

  PROD          http://\<host-prod\>/swagger-ui.html             Deshabilitado por defecto
  ------------------------------------------------------------------------------------------

Verificar que:

1. El título del Swagger UI muestra el nombre del servicio (ej. `onp-pensiones-liquidacion`).
2. Todos los Controllers con `@Tag` aparecen como grupos de endpoints.
3. Cada endpoint tiene su `summary` y los `responseCode` documentados.
4. La estructura de respuesta referencia `ApiResponseWrapper`.

# Anexo A --- Referencia rápida

## A.1 Atributos OTEL más usados en trazas

  -------------------------------------------------------------------------------------------------------
  **Atributo**             **Descripción**                           **Ejemplo ONP**
  ------------------------ ----------------------------------------- ------------------------------------
  service.name             Nombre del servicio                       onp-afiliaciones-registro

  service.version          Versión del servicio                      1.3.0

  deployment.environment   Entorno de despliegue                     development / quality / production

  http.method              Método HTTP                               GET, POST, PUT, DELETE

  http.route               Ruta del endpoint                         /api/v1/afiliaciones/{id}

  http.status_code         Código de respuesta HTTP                  200, 400, 500

  db.system                Motor de base de datos                    oracle

  db.operation             Tipo de operación SQL                     SELECT, INSERT, UPDATE

  db.statement             Sentencia SQL (parámetros enmascarados)   SELECT \... WHERE dni=?

  net.peer.name            Host del servicio externo                 api.reniec.gob.pe

  exception.type           Clase de la excepción                     java.lang.NullPointerException

  otel.status_code         Estado del span                           OK / ERROR
  -------------------------------------------------------------------------------------------------------

## A.2 Campos ECS en logs

Los campos se dividen en dos grupos según en qué líneas aparecen:

**Campos presentes en todas las líneas de log de la petición** (via MDC o Logback):

  ------------------------------------------------------------------------------------
  **Campo**             **Descripción**                      **Origen**
  --------------------- ------------------------------------ -------------------------
  \@timestamp           Fecha y hora ISO 8601 UTC            Logback automático

  log.level             INFO / DEBUG / WARN / ERROR          Logback automático

  log.logger            Clase Java que generó el log         Logback automático

  message               Mensaje del evento                   Desarrollador

  service.name          onp-\<sistema\>-\<modulo\>           application.properties

  service.version       Versión del pom.xml                  application.properties

  service.environment   development / quality / production   application.properties

  trace.id              ID de traza OTEL                     OTEL + MDC automático

  span.id               ID de span OTEL                      OTEL + MDC automático

  http.request.id       X-Request-ID de la petición          RequestIdFilter (MDC)

  user.id               Usuario autenticado                  CanonicalRequestLogFilter (MDC)

  error.stack_trace     Stack trace (solo ERROR)             Logback automático
  ------------------------------------------------------------------------------------

**Campos presentes únicamente en el log canónico de request** (emitido por `CanonicalRequestLogFilter`, `message: "REQUEST"`):

  ------------------------------------------------------------------------------------
  **Campo**                    **Descripción**                **Valores de ejemplo**
  ---------------------------- ------------------------------ ------------------------
  http.request.method          Método HTTP                    GET, POST, PUT, DELETE

  url.path                     Ruta del endpoint              /api/v1/afiliaciones/1

  http.response.status\_code   Status HTTP de la respuesta    200, 400, 500

  duration\_ms                 Duración total en milisegundos 143
  ------------------------------------------------------------------------------------

> **NOTA — Nombres de campo según el destino:** Micrometer Tracing inyecta el contexto de traza en el MDC con los nombres `traceId` y `spanId`. Estos son los nombres que aparecen en `kubectl logs` (salida JSON_STDOUT de Logback). El OTEL Collector los recibe vía OTLP (appender `OpenTelemetry`) y los escribe en Elasticsearch con el nombre canónico ECS: `trace.id` y `span.id`. Los campos de la tabla anterior corresponden a los que aparecen en Kibana, no en la consola.

## A.3 Convenciones de nomenclatura ONP

  -------------------------------------------------------------------------------------------------------------
  **Concepto**         **Formato**                                   **Ejemplo**
  -------------------- --------------------------------------------- ------------------------------------------
  Nombre de servicio   onp-\<sistema\>-\<modulo\>                    onp-pensiones-liquidacion

  Span de negocio      \<sistema\>.\<modulo\>.\<operacion\>          pensiones.liquidacion.calcular-beneficio

  Entorno DEV          deployment.environment=development            ---

  Entorno QA           deployment.environment=quality                ---

  Entorno PROD         deployment.environment=production             ---

  Data View logs       onp-logs-*                                    Cubre DEV, QA y PROD
  -------------------------------------------------------------------------------------------------------------

## A.4 Tabla de codDetRespuesta --- resumen rápido

  -------------------------------------------------------------------------------
  **Código**   **Categoría**   **Descripción**                      **codHttp**
  ------------ --------------- ------------------------------------ -------------
  000          Éxito           Operación completada correctamente   200

  100          Validación      Error de validación en los datos     400

  200          Negocio         Regla de negocio no cumplida         422

  201          Negocio         Recurso no encontrado                404

  300          Autorización    No autenticado                       401

  301          Autorización    No autorizado                        403

  400          Integración     Servicio externo no disponible       503

  500          Sistema         Error interno del servidor           500
  -------------------------------------------------------------------------------

  ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  **NOTA:** Para la tabla completa de codDetRespuesta consultar la sección 3.3.4. Para la especificación completa de atributos OTEL: https://opentelemetry.io/docs/specs/semconv/
  ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

  ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

[^1]: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
