# Lineamiento de Log Centralizado, Trazabilidad y Observabilidad — ONP

---

**Código:** LIN-OBS-001
**Marco rector:** LIN-ARQ-001
**Versión:** v0.1.2
**Fecha:** 2026-07-10
**Propietario documental:** OTI / Arquitectura
**Clasificación:** Uso Interno (Técnico)
**Dirigido a:** Equipo de Desarrollo, Plataforma/Infraestructura
**Reemplaza:** Guía v0.1.2 [secciones 1](#1-objetivo-y-alcance) y 2 (Telemetría y Trazas / Logging)
**Estado:** Borrador

---

## Historial de versiones

| Versión | Fecha       | Autor       | Descripción                              |
|---------|-------------|-------------|------------------------------------------|
| 0.1.0   | 2026-05-25  | Arquitectura OTI | Versión inicial; absorbe Guía v0.1.2 [secciones 1](#1-objetivo-y-alcance)–2 |
| 0.1.1   | 2026-05-28  | Arquitectura OTI | Declara YAML como formato oficial de configuración, alinea el modelo OTEL con overrides operativos en K8s y documenta el comportamiento de la cadena de filtros ante fallos |
| 0.1.2   | 2026-07-10  | Arquitectura OTI | Migra Marco rector de `LIN-ARQ-000` (congelado) a `LIN-ARQ-001` (vigente) |

---

## Tabla de contenidos

1. [Objetivo y alcance](#1-objetivo-y-alcance)
2. [Principios rectores](#2-principios-rectores)
3. [Arquitectura de observabilidad ONP](#3-arquitectura-de-observabilidad-onp)
4. [Configuración inicial (una vez por proyecto)](#4-configuración-inicial-una-vez-por-proyecto)
5. [Trazas distribuidas](#5-trazas-distribuidas)
6. [Logs estructurados](#6-logs-estructurados)
7. [Correlación de identidades](#7-correlación-de-identidades)
8. [Métricas y health checks](#8-métricas-y-health-checks)
9. [Infraestructura de observabilidad ONP](#9-infraestructura-de-observabilidad-onp)
10. [Responsabilidades](#10-responsabilidades)
11. [Checklist de conformidad](#11-checklist-de-conformidad)
12. [Anti-patrones](#12-anti-patrones)
13. [Proceso de excepción (ADR)](#13-proceso-de-excepción-adr)
14. [Glosario](#14-glosario)
15. [Apéndice A — Atributos OTEL más usados](#apéndice-a--atributos-otel-más-usados)
16. [Apéndice B — Campos ECS en logs](#apéndice-b--campos-ecs-en-logs)
17. [Apéndice C — Convenciones de nomenclatura ONP](#apéndice-c--convenciones-de-nomenclatura-onp)

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece las normas obligatorias para la implementación de observabilidad en todos los servicios Java/Spring Boot de la ONP. Define los estándares de:

- **Trazas distribuidas**: cómo instrumentar servicios para que sus operaciones sean visibles en Jaeger.
- **Logs estructurados**: formato, campos mínimos, política de datos personales y retención.
- **Correlación de identidades**: cómo enlazar trace.id, span.id y X-Request-ID entre logs, trazas y peticiones HTTP.
- **Métricas y health checks**: endpoints Actuator, integración con Prometheus y Grafana.
- **Responsabilidades**: qué configura el desarrollador, qué configura Plataforma.

### 1.2 Alcance

**Aplica a:**
- Todo servicio web Java 21 / Spring Boot 3.5.x desplegado en K8s ONP (DEV, QA, PROD).
- Servicios nuevos: cumplimiento obligatorio antes del primer pase a DEV.
- Servicios existentes: migración programada conforme a plan de arquitectura.

**Fuente autoritativa:** Este documento es la fuente canónica de observabilidad. Los demás lineamientos (LIN-DEV-JAVA-001, LIN-API-REST-001, LIN-FE-ANG-001, LIN-CICD-001, LIN-K8S-001) referencian este documento en materia de logs, trazas y métricas — no redefinen nada aquí establecido.

**Fuera de alcance de este documento — y dónde se cubre:**

| Tema | Dónde se cubre |
|---|---|
| Configuración del OTEL Collector, Jaeger, Elasticsearch, Kibana, Prometheus, Grafana | Responsabilidad de Plataforma — ver [sección 10.2](#102-plataforma-infraestructura) de este documento |
| **Observabilidad de frontend Angular** — browser performance, Core Web Vitals, error tracking en el cliente | **LIN-FE-ANG-001 sección 15** — Observabilidad Frontend (sección existente): métricas de LCP, INP, CLS; errores de red; correlación del `X-Request-ID` propagado desde el backend hacia los logs del browser |
| **Observabilidad de PL/SQL legacy** — trazabilidad de procedures críticos | **LIN-BD-ORA-001 sección 6.0**: la trazabilidad de PL/SQL se obtiene indirectamente a través del adapter Java; el span `@NewSpan` del adapter captura la duración y el resultado del procedure, y el log estructurado del adapter incluye `trace.id`. No hay instrumentación directa dentro del procedure |

---

## 2. Principios rectores

| # | Principio | Descripción |
|---|-----------|-------------|
| P1 | **Observabilidad por defecto** | Un servicio no instrumentado es invisible para operaciones. No existe aprobación para desplegar sin telemetría desde el primer pase a DEV. |
| P2 | **Correlación completa** | Cada evento (log, traza, métrica) debe incluir los campos de correlación mínimos: `trace.id`, `span.id`, `http.request.id`, `service.name`. Sin ellos el diagnóstico en producción es imposible. |
| P3 | **No PII en logs ni trazas** | Datos personales sensibles (DNI, nombre completo, tokens, cuentas) están prohibidos en logs y atributos de span. El incumplimiento viola la Ley N.° 29733. Ver [sección 6.2](#62-politica-no-pii-datos-sensibles). |
| P4 | **Un evento, una línea** | Cada evento de log es una línea JSON completa e independiente. No se usan saltos de línea dentro del mensaje. Los stacktraces se serializa como campo único `error.stack_trace`. |
| P5 | **Responsabilidad diferenciada** | El desarrollador implementa la instrumentación; Plataforma opera el stack de observabilidad. Ningún rol sustituye al otro. |
| P6 | **Propiedad documental** | Este lineamiento es propietario de: campos de correlación, política No PII, logback-spring.xml, convenciones de span. LIN-DEV-JAVA-001 implementa pero no redefine. |

---

## 3. Arquitectura de observabilidad ONP

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        Servicio Java / Spring Boot                       │
│                                                                          │
│  ┌─────────────┐   trazas OTLP   ┌─────────────────────────────────────┐│
│  │ Micrometer  │────────────────▶│                                     ││
│  │ Tracing     │                 │         OTEL Collector              ││
│  │ + OTEL SDK  │   logs OTLP     │    (namespace: otel-{env})          ││
│  │             │────────────────▶│                                     ││
│  │ Logback     │                 │  exporters: jaeger / elastic / prom ││
│  │ + OTEL      │                 └──────────┬──────────────────────────┘│
│  │ Appender    │                            │                            │
│  └─────────────┘                            │                            │
│                                             │                            │
│  Actuator /metrics ──────────────────────── │──────▶ Prometheus scrape   │
└─────────────────────────────────────────────│───────────────────────────┘
                                              │
                    ┌─────────────────────────┼──────────────────────┐
                    │                         │                       │
                    ▼                         ▼                       ▼
              ┌──────────┐           ┌──────────────┐        ┌──────────────┐
              │  Jaeger  │           │Elasticsearch │        │ Prometheus   │
              │  (trazas)│           │   (logs)     │        │ (métricas)   │
              └────┬─────┘           └──────┬───────┘        └──────┬───────┘
                   │                        │                        │
                   ▼                        ▼                        ▼
              Jaeger UI              Kibana Discover            Grafana Dashboard
```

**Stack por entorno:**

| Componente | Namespace K8s DEV | Namespace K8s QA | Namespace K8s PROD |
|---|---|---|---|
| OTEL Collector | `otel-dev` | `otel-qa` | `otel` |
| Jaeger | `jaeger-dev` | `jaeger-qa` | `jaeger` |
| Elasticsearch + Kibana | `elastic-dev` | `elastic-qa` | `elastic` |
| Prometheus + Grafana | `monitoring` | `monitoring` | `monitoring` |

---

## 4. Configuración inicial (una vez por proyecto)

Los cambios de esta sección se realizan **una sola vez** al incorporar observabilidad al proyecto. Una vez completada la configuración, no es necesario volver a ella a menos que se actualice el stack de observabilidad.

### 4.1 Dependencias Maven

Agregar en el bloque `<dependencies>` del `pom.xml`. El parent `spring-boot-starter-parent:3.5.x` gestiona versiones de Micrometer y OpenTelemetry automáticamente — no especificar versión para esas dependencias.

```xml
<!-- ═══════════════════════════════════════════════════════════════════ -->
<!-- OBSERVABILIDAD: OpenTelemetry + Micrometer Tracing (trazas)        -->
<!-- ═══════════════════════════════════════════════════════════════════ -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
<!-- Requerido para que @NewSpan funcione en runtime vía AOP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<!-- Actuator: /health, /info, /metrics, /prometheus -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- ═══════════════════════════════════════════════════════════════════ -->
<!-- OBSERVABILIDAD: Logging estructurado JSON (ECS)                    -->
<!-- ═══════════════════════════════════════════════════════════════════ -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
<!-- Puente Logback → OTEL SDK: envía logs al OTEL Collector via OTLP -->
<!-- ADVERTENCIA: versión vinculada al OTel SDK que gestiona Spring Boot -->
<!-- Spring Boot 3.4.x → 2.10.0-alpha  |  Spring Boot 3.5.x → 2.14.0-alpha -->
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-logback-appender-1.0</artifactId>
    <version>2.14.0-alpha</version>
</dependency>
```

> **ADVERTENCIA:** El sufijo `-alpha` en `opentelemetry-logback-appender-1.0` no indica inestabilidad — indica que la API de extensión del SDK está en tier "incubating". Esta librería está ampliamente usada en producción. Usar una versión incorrecta causa `AbstractMethodError` al arrancar. Verificar la versión del OTel SDK gestionado con `mvn dependency:tree -Dincludes=io.opentelemetry:opentelemetry-sdk`.

> **NOTA:** `spring-boot-starter-web` y `spring-boot-starter-data-jpa` no se incluyen aquí; ya vienen del proyecto base. `spring-boot-starter-aop` es nuevo — sin él `@NewSpan` compila pero el span no se crea en runtime.

### 4.2 application.yml — configuración base

**Dónde:** `src/main/resources/application.yml` (archivo base sin sufijo de perfil). No repetir esta configuración en los archivos de entorno.

> **Decisión institucional de configuración:** ONP adopta `application.yml` y `application-{perfil}.yml` como formato oficial para proyectos Spring Boot nuevos. El formato `.properties` queda reservado para sistemas legacy cuya migración no esté planificada.

```yaml
# ═══════════════════════════════════════════════════════════════════════
# OBSERVABILIDAD: identificación del servicio (fuente autoritativa ONP)
# ═══════════════════════════════════════════════════════════════════════
spring:
  application:
    name: onp-<sistema>-<modulo> # Formato obligatorio (ver Apéndice C)

info:
  app:
    version: "@project.version@" # Maven la reemplaza en compilación

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

### 4.3 application-dev.yml

**Dónde:** `src/main/resources/application-dev.yml`. Obligatorio — sin este archivo el servicio arranca sin emitir telemetría en DEV.

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

### 4.4 application-qa.yml

**Dónde:** `src/main/resources/application-qa.yml`.

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

### 4.5 application-prod.yml

**Dónde:** `src/main/resources/application-prod.yml`.

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

> **ADVERTENCIA — sampling.probability:** DEV y QA usan `1.0` (100% de peticiones trazadas). PROD usa `0.1` (10%) para evitar saturar Elasticsearch con alto volumen de tráfico. Ajustar según el volumen real del servicio; un valor de `1.0` en PROD puede causar problemas de almacenamiento.

> **NOTA — SPRING_PROFILES_ACTIVE:** Plataforma es responsable de inyectar `SPRING_PROFILES_ACTIVE=dev|qa|prod` como variable de entorno en el `Deployment` de Kubernetes. Sin esta variable el servicio usa únicamente `application.yml` base y no emite telemetría. Ver [sección 10.2](#102-plataforma-infraestructura).

> **NOTA — overrides OTEL en Kubernetes:** Variables como `OTEL_EXPORTER_OTLP_ENDPOINT` o `OTEL_SERVICE_NAME` se consideran mecanismos de override operativo administrados por Plataforma en `LIN-K8S-001`. No sustituyen la configuración base versionada del proyecto.

### 4.6 logback-spring.xml

**Dónde:** `src/main/resources/logback-spring.xml`. Reemplaza cualquier configuración anterior de Logback.

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

    <!-- JSON estructurado ECS a stdout (capturado por K8s/fluentd/Collector) -->
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
            <!-- Campos MDC propagados automáticamente por OTEL y los filtros -->
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

    <!-- DEV: pe.gob.onp.* en DEBUG, frameworks en INFO -->
    <springProfile name="dev">
        <root level="INFO">
            <appender-ref ref="JSON_STDOUT"/>
            <appender-ref ref="OpenTelemetry"/>
        </root>
        <logger name="pe.gob.onp" level="DEBUG"/>
    </springProfile>

    <!-- QA y PROD: todo en INFO -->
    <springProfile name="qa,prod">
        <root level="INFO">
            <appender-ref ref="JSON_STDOUT"/>
            <appender-ref ref="OpenTelemetry"/>
        </root>
    </springProfile>
</configuration>
```

> **Nota:** `traceId` y `spanId` son los nombres de campo que inyecta Micrometer Tracing en el MDC (en `kubectl logs`). El OTEL Collector los recibe vía OTLP y los escribe en Elasticsearch con el nombre canónico ECS: `trace.id` y `span.id`. Los campos del Apéndice B corresponden a lo que aparece en Kibana, no en la consola.

### 4.7 OpenTelemetryLogbackConfig.java

**Dónde:** `src/main/java/<paquete-base>/config/OpenTelemetryLogbackConfig.java`

**Por qué es necesaria:** Spring Boot 3.5 crea el bean `OpenTelemetrySdk` con `SdkLoggerProvider` y el exportador OTLP, pero no llama `GlobalOpenTelemetry.set()`. El `OpenTelemetryAppender` en `logback-spring.xml` usa `GlobalOpenTelemetry.get()`, que sin esta llamada devuelve un no-op (los logs se descartan). El `@PostConstruct` de esta clase instala el bean correcto una vez que el contexto de Spring está inicializado.

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

### 4.8 Mask.java — utilidad No PII

**Dónde:** `src/main/java/<paquete-base>/util/Mask.java`. Sin dependencias de Spring — no lleva `@Component`. Implementa la política No PII ([sección 6.2](#62-politica-no-pii-datos-sensibles)).

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

### 4.9 CanonicalRequestLogFilter.java

**Dónde:** `src/main/java/<paquete-base>/filter/CanonicalRequestLogFilter.java`

**Qué hace:** Emite una única línea de log estructurado al finalizar cada petición HTTP con método, ruta, status, duración y usuario. Selecciona el nivel: `INFO` para 2xx, `WARN` para 4xx, `ERROR` para 5xx.

**Orden de ejecución de filtros** — la secuencia correcta es:

| Orden | Filtro | Qué establece en MDC |
|---|---|---|
| `@Order(1)` | `RequestIdFilter` ([sección 4.10](#410-requestidfilterjava)) | `http.request.id` |
| `@Order(2)` | `SaaTokenValidationFilter` (LIN-SEC-APP-001 sección 8.3) | `user.id` |
| `@Order(3)` | `CanonicalRequestLogFilter` | Lee `user.id` ya disponible en MDC; emite log canónico |

`CanonicalRequestLogFilter` debe correr **después** de `SaaTokenValidationFilter` para que `user.id` esté en MDC cuando se emita el log. Si corre antes, el campo registra `"anonymous"` aunque el usuario esté autenticado.

**Requiere:** `RequestIdFilter` ([sección 4.10](#410-requestidfilterjava)) para que `http.request.id` esté accesible. En servicios con autenticación SAA, requiere también `SaaTokenValidationFilter` (LIN-SEC-APP-001 sección 8.3) para que `user.id` esté en MDC. En servicios sin autenticación (públicos), `user.id` registra `"anonymous"`.

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
@Order(3) // Debe correr después de RequestIdFilter (@Order 1) y SaaTokenValidationFilter (@Order 2)
public class CanonicalRequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CanonicalRequestLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        StatusCapturingResponse wrapped = new StatusCapturingResponse(response);
        try {
            chain.doFilter(request, wrapped);
        } finally {
            int status = wrapped.getStatus();
            long duration = System.currentTimeMillis() - start;
            // user.id fue puesto en MDC por SaaTokenValidationFilter si el endpoint está protegido.
            // Si no hay autenticación (endpoint público), MDC.get("user.id") devuelve null → "anonymous".
            String userId = MDC.get("user.id");
            if (userId == null) MDC.put("user.id", "anonymous");
            MDC.put("http.request.method",       request.getMethod());
            MDC.put("url.path",                  request.getRequestURI());
            MDC.put("http.response.status_code", String.valueOf(status));
            MDC.put("duration_ms",               String.valueOf(duration));
            if      (status >= 500) log.error("REQUEST");
            else if (status >= 400) log.warn("REQUEST");
            else                    log.info("REQUEST");
            MDC.remove("http.request.method");
            MDC.remove("url.path");
            MDC.remove("http.response.status_code");
            MDC.remove("duration_ms");
            if (userId == null) MDC.remove("user.id"); // solo limpia si fue este filtro quien lo puso
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

**Ejemplo de log canónico — petición exitosa con usuario autenticado:**

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

### 4.10 RequestIdFilter.java

**Dónde:** `src/main/java/<paquete-base>/filter/RequestIdFilter.java`

**Qué hace:** Lee el header `X-Request-ID` de la petición entrante; si el cliente no lo envía, genera un UUID. Lo pone en MDC (`http.request.id`) y lo devuelve en el header de respuesta para que el cliente pueda correlacionarlo.

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
@Order(1) // Primer filtro en la cadena — debe correr antes de SaaTokenValidationFilter (@Order 2) y CanonicalRequestLogFilter (@Order 3)
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

### 4.11 Comportamiento de la cadena de filtros ante fallos

La cadena obligatoria es:

1. `RequestIdFilter` (`@Order(1)`)
2. `SaaTokenValidationFilter` (`@Order(2)`) cuando aplica autenticación
3. `CanonicalRequestLogFilter` (`@Order(3)`)

| Escenario | ¿Existe `http.request.id`? | ¿Existe `user.id`? | ¿Se ejecuta `CanonicalRequestLogFilter`? | Resultado esperado |
|---|---|---|---|---|
| Falla antes de entrar a `RequestIdFilter` | No | No | No | Error temprano; no hay correlación garantizada |
| `RequestIdFilter` genera o propaga ID y continúa | Sí | No o aún no | Sí, si la cadena continúa | Correlación disponible en toda la request |
| `SaaTokenValidationFilter` rechaza token faltante o inválido | Sí | No | No | Respuesta 401; `requestId` existe para trazabilidad, no hay log canónico de aplicación |
| `SaaTokenValidationFilter` falla por indisponibilidad del SAA | Sí | No | No | Respuesta 503; `requestId` existe para soporte, no hay log canónico de aplicación |
| `SaaTokenValidationFilter` valida correctamente | Sí | Sí | Sí | La request continúa con `user.id` disponible en MDC |
| `CanonicalRequestLogFilter` ejecuta normalmente | Sí | Sí o `anonymous` | Sí | Emite log canónico final con contexto completo disponible |

---

## 5. Trazas distribuidas

### 5.1 Instrumentación automática

Spring Boot con las dependencias de [sección 4.1](#41-dependencias-maven) instrumenta automáticamente los siguientes puntos sin código adicional:

| Punto de instrumentación | Span generado | Atributos clave |
|---|---|---|
| Petición HTTP entrante (`@RestController`) | Span raíz por petición | `http.method`, `http.route`, `http.status_code` |
| Consulta JPA/Hibernate a Oracle | Span hijo por query SQL | `db.system=oracle`, `db.statement` (parámetros como `?`) |
| Llamada HTTP saliente vía `RestTemplate` gestionado por Spring | Span hijo por llamada | `http.method`, `http.url`, `http.status_code` |
| Llamada HTTP saliente vía `WebClient` gestionado por Spring | Span hijo por llamada | `http.method`, `http.url`, `http.status_code` |

> **IMPORTANTE:** `RestTemplate` y `WebClient` solo se instrumentan automáticamente si el bean es gestionado por Spring (`@Bean` vía `RestTemplateBuilder` / `WebClient.Builder`). Instanciar con `new RestTemplate()` o `WebClient.create()` omite la instrumentación.

**Bean correcto para RestTemplate:**

```java
@Configuration
public class HttpConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
```

**Bean correcto para WebClient:**

```java
@Configuration
public class HttpConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}
```

### 5.2 Errores capturados — span.setStatus manual

**Caso:** Un `catch` captura la excepción. OTEL no la detecta automáticamente — el span queda verde aunque la operación falló.

**Regla:** Dentro del bloque `catch`, llamar `Span.current().setStatus()` y `Span.current().recordException()` antes de relanzar.

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;

public Pension calcularPension(String dni) {
    try {
        return repositorio.buscarPorDni(dni);
    } catch (DniNoEncontradoException e) {
        Span.current().setStatus(StatusCode.ERROR, e.getMessage());
        Span.current().recordException(e);
        throw e;
    }
}
```

| Situación | Span en Jaeger | ¿Código manual? |
|---|---|---|
| `throw` sin `catch` | ERROR automático | No |
| `catch` sin código manual | OK incorrecto (verde) | **Sí — obligatorio** |
| `catch` con `setStatus` + `recordException` | ERROR correcto (rojo) | Sí |

### 5.3 Operaciones de negocio — @NewSpan

**Cuándo:** Un método del Service realiza una operación costosa o crítica (cálculo de beneficio, validación compleja, integración externa) que debe aparecer como span independiente.

**Dónde:** Capa `@Service`. No en Controllers (el span HTTP ya existe) ni en Repositories (JPA ya los instrumenta).

```java
import io.micrometer.tracing.annotation.NewSpan;

@NewSpan("pensiones.liquidacion.calcular-beneficio")
public BigDecimal calcularBeneficio(String dni, int aniosAportados) {
    return monto;
}
```

> **ADVERTENCIA:** No abusar de `@NewSpan`. Usarlo solo en operaciones costosas o críticas. Demasiados spans manuales dificultan la lectura en Jaeger UI. Si el IDE muestra `NewSpan cannot be resolved`, ejecutar `mvn dependency:resolve`.

**Agregar atributos de negocio al span (no PII):**

```java
import io.opentelemetry.api.trace.Span;

@NewSpan("afiliaciones.registro.validar-datos")
public boolean validarDatos(SolicitudAfiliacion solicitud) {
    Span.current().setAttribute("afiliacion.tipo",    solicitud.getTipo());
    Span.current().setAttribute("afiliacion.regimen", solicitud.getRegimen());
    return ejecutarValidacion(solicitud);
}
```

### 5.4 Enriquecer el span activo — @ContinueSpan

**Cuándo:** Un sub-método auxiliar necesita agregar atributos al span activo sin crear un span hijo nuevo. Útil para validaciones rápidas.

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

| | `@NewSpan` | `@ContinueSpan` |
|---|---|---|
| Crea span hijo | Sí | No |
| Visible como nivel separado en Jaeger | Sí | No |
| Útil para | Operaciones costosas o críticas | Validaciones y pasos auxiliares rápidos |

> **ADVERTENCIA:** No usar `@ContinueSpan` si no hay un span activo en el hilo. Siempre llamar desde dentro de un método `@NewSpan` o desde un endpoint HTTP.

### 5.5 Tareas programadas — @Scheduled

**Caso:** Un job en background se ejecuta sin petición HTTP que lo origine. OTEL no tiene span padre al que enlazarlo — no genera trazas automáticamente.

**Qué aplicar:** Crear el span manualmente con `Tracer`, cerrándolo siempre en `finally`.

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

> **Por qué el `finally` es obligatorio:** Si el span no se cierra con `span.end()`, queda abierto en memoria y nunca se envía al OTEL Collector.

### 5.6 Convenciones de nomenclatura de spans

Formato obligatorio: `<sistema>.<modulo>.<operacion-en-kebab-case>`

| Ejemplo válido | Descripción |
|---|---|
| `afiliaciones.registro.validar-datos` | Validación de datos de afiliación |
| `pensiones.liquidacion.calcular-beneficio` | Cálculo del beneficio de pensión |
| `consultas.reniec.verificar-identidad` | Verificación de identidad en RENIEC |
| `consultas.sunat.obtener-ruc` | Consulta de RUC en SUNAT |
| `pensiones.batch.reconciliacion-diaria` | Job nocturno de reconciliación |

Ver tabla completa en **Apéndice C**.

### 5.7 Verificación en Jaeger

**Confirmación indirecta vía logs:** Spring Boot 3.5 no emite un mensaje explícito de "OTLP activo". La confirmación es que los campos `traceId` y `spanId` aparecen en los logs:

```json
{
  "@timestamp": "2026-...",
  "message": "Operacion completada",
  "traceId": "d611cfa9851c9f00b36acf61b2d68b93",
  "spanId": "33e564c7ce255b35",
  "service.name": "onp-<sistema>-<modulo>"
}
```

**Verificación en Jaeger UI:**

1. Realizar al menos una petición HTTP al servicio.
2. Acceder al Jaeger UI del entorno.
3. En el campo **Service** seleccionar `onp-<sistema>-<modulo>`.
4. Hacer clic en **Find Traces**.
5. Verificar que aparece la traza correspondiente a la petición.

**Anatomía de una traza esperada:**

| Span | Descripción | Generado por |
|---|---|---|
| `HTTP POST /api/v1/...` | Petición HTTP entrante | Automático |
| `SELECT ... FROM ... WHERE ...` | Consulta a Oracle via JPA | Automático |
| `HTTP GET https://reniec.gob.pe/...` | Llamada a servicio externo | Automático |
| `<sistema>.<modulo>.<operacion>` | Operación de negocio con `@NewSpan` | Manual |

> **ADVERTENCIA:** Si el servicio no aparece en Jaeger: (1) verificar que el OTEL Collector está corriendo en el namespace correspondiente; (2) verificar la URL del Collector en `application-{env}.yml`; (3) revisar logs del Collector con `kubectl logs -n otel-{env} deployment/otel-collector`.

---

## 6. Logs estructurados

### 6.1 Niveles de log

| Nivel | Cuándo usarlo | Configuración |
|---|---|---|
| `INFO` | Eventos relevantes del flujo normal del negocio: operaciones completadas, decisiones tomadas | Activo en todos los entornos |
| `DEBUG` | Detalle técnico para diagnóstico: valores intermedios, respuestas de sistemas externos | Solo en DEV (ver [sección 4.6](#46-logback-springxml)) |
| `WARN` | Situaciones inesperadas pero recuperables: timeouts con reintento, datos incompletos, fallbacks | Activo en todos los entornos |
| `ERROR` | Errores que impiden completar la operación. **Siempre incluir la excepción como segundo argumento** | Activo en todos los entornos |

**Setup del logger — obligatorio en toda clase que escriba logs:**

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(NombreDeClase.class);
```

**Uso por capa:**

**Controller (`@RestController`):**

```java
@GetMapping("/afiliaciones/{id}")
public AfiliacionDto obtener(@PathVariable Long id) {
    log.info("GET /afiliaciones/{} solicitado", id);  // INFO: entrada de petición
    return service.obtener(id);
}
// ERROR: no loggear en Controller — los errores se manejan en Service o @ExceptionHandler
```

**Service (`@Service`):**

```java
public void registrar(SolicitudAfiliacion solicitud) {
    log.info("Solicitud de afiliacion recibida para regimen: {}", solicitud.getRegimen());
    log.debug("Respuesta de RENIEC: {}", Mask.partial(response.toString(), 4)); // solo DEV
    log.warn("Timeout en SUNAT, reintentando... intento: {}", intento);
    log.error("Error al procesar solicitud de afiliacion", e); // excepción como segundo arg
}
```

**Repository (`@Repository`):** No loggear. JPA/Hibernate genera sus propios logs de query a nivel `DEBUG`. Excepción: queries nativas complejas con `DEBUG` puntual.

**Component / infraestructura:**

```java
public DatosPersona consultar(String dni) {
    log.info("Consultando RENIEC para DNI: {}", Mask.dni(dni)); // ← Mask obligatorio
    log.warn("Respuesta de RENIEC demorada {}ms, umbral: {}ms", tiempo, umbral);
    log.error("Fallo al conectar con RENIEC", e);
}
```

**Scheduled / Jobs:**

```java
public void ejecutar() {
    log.info("Job reconciliacion iniciado. Periodo: {}", periodo);
    log.warn("Registro omitido por datos incompletos. ID: {}", id);
    log.info("Job reconciliacion finalizado. Procesados: {}, Omitidos: {}, Tiempo: {}ms",
             procesados, omitidos, tiempo);
    log.error("Job reconciliacion fallido en registro ID: {}", id, e);
}
```

### 6.2 Política No PII — datos sensibles

> **OBLIGATORIO.** Está estrictamente prohibido registrar datos personales o sensibles en los logs o como atributos de span. El incumplimiento constituye una vulneración a la **Ley de Protección de Datos Personales (Ley N.° 29733)** y puede derivar en responsabilidad institucional y personal.

Usar la clase `Mask` ([sección 4.8](#48-maskjava-utilidad-no-pii)) para enmascarar datos antes de incluirlos en cualquier log o atributo.

| Dato sensible | Acción requerida | Método `Mask` | Ejemplo resultado |
|---|---|---|---|
| DNI / documento de identidad | Enmascarar | `Mask.dni(valor)` | `****1234` |
| Número de teléfono | Enmascarar | `Mask.phone(valor)` | `****5678` |
| Correo electrónico | Enmascarar | `Mask.email(valor)` | `u***@***.com` |
| Token / contraseña / PIN | **No loggear nunca** | — | Omitir completamente |
| Nombre completo | Usar ID interno | — | `ID: 1617582` |
| Número de cuenta bancaria | Enmascarar | `Mask.partial(valor, 4)` | `****9012` |
| Datos de salud o situación previsional detallada | **No loggear nunca** | — | Omitir completamente |

```java
// Correcto — datos enmascarados
log.info("Consulta recibida para DNI: {}", Mask.dni(dni));
log.debug("Email del solicitante: {}", Mask.email(email));
log.info("Cuenta origen: {}", Mask.partial(numeroCuenta, 4));

// PROHIBIDO — datos en claro
log.info("Consulta recibida para DNI: {}", dni);           // PROHIBIDO — Ley 29733
log.debug("Token de autenticacion: {}", token);             // PROHIBIDO
log.info("Nombre completo: {}", solicitud.getNombre());     // PROHIBIDO
```

### 6.3 P6Spy — queries SQL en DEV

**Solo en DEV.** P6Spy intercepta el driver JDBC para loggear cada query SQL con su duración, correlacionada con `trace.id` y `user.id`.

> **No activar en QA ni PROD.** P6Spy introduce overhead medible. En esos entornos, activarlo solo para investigación puntual de rendimiento y desactivarlo inmediatamente después.

**Dependencia (solo en pom.xml si se activa en DEV):**

```xml
<dependency>
    <groupId>p6spy</groupId>
    <artifactId>p6spy</artifactId>
    <version>3.9.1</version>
</dependency>
```

**application-dev.yml — cambiar datasource URL:**

```yaml
spring:
  datasource:
    url: jdbc:p6spy:oracle:thin:@//<HOST>:<PORT>/<SERVICE_NAME>
    driver-class-name: com.p6spy.engine.spy.P6SpyDriver
```

**src/main/resources/spy.properties:**

```properties
appender=com.p6spy.engine.spy.appender.Slf4JLogger
slf4jLogLevel=DEBUG
logMessageFormat=com.p6spy.engine.spy.appender.CustomLineFormat
customLogMessageFormat=%(executionTime)ms | %(sql)
executionThreshold=0
excludecategories=info,debug,result,resultset
```

### 6.4 Verificación en Kibana

Los logs aparecen bajo el Data View `onp-logs-*`:

| Entorno | Índice en Elasticsearch |
|---|---|
| DEV | `onp-logs-development` |
| QA | `onp-logs-quality` |
| PROD | `onp-logs-production` |

**Queries operacionales disponibles una vez activo el log canónico:**

| Pregunta | Query en Kibana Discover |
|---|---|
| ¿Qué requests generó el usuario X? | `message: "REQUEST" AND user.id: "jperez"` |
| ¿Qué endpoints están fallando? | `message: "REQUEST" AND http.response.status_code: "500"` |
| ¿Qué requests tomaron más de 1 segundo? | `message: "REQUEST" AND duration_ms > 1000` |
| ¿Todos los logs de una petición específica? | `http.request.id: "a1b2c3d4-..."` |
| ¿Errores 4xx de un servicio? | `service.name: "onp-pensiones-afiliacion" AND http.response.status_code >= 400` |
| ¿Qué hizo un usuario durante un incidente? | `user.id: "mgarcia" AND @timestamp > "2026-05-21T10:00:00"` |
| Queries SQL lentas en DEV | `log.logger: "p6spy"` (ordenar por `message` desc) |

> **ADVERTENCIA:** Si el Data View `onp-logs-*` no existe en Kibana, debe crearlo el equipo de Plataforma (ver [sección 10.2](#102-plataforma-infraestructura)).

---

## 7. Correlación de identidades

### 7.1 Campos de correlación y su origen

| Campo (en Kibana) | Campo (en MDC / consola) | Descripción | Origen |
|---|---|---|---|
| `trace.id` | `traceId` | ID único de la traza OTEL | OTEL automático → MDC |
| `span.id` | `spanId` | ID del span activo | OTEL automático → MDC |
| `http.request.id` | `http.request.id` | X-Request-ID de la petición HTTP | `RequestIdFilter` ([sección 4.10](#410-requestidfilterjava)) — @Order(1) |
| `user.id` | `user.id` | Usuario autenticado | `SaaTokenValidationFilter` (LIN-SEC-APP-001 sección 8.3) — @Order(2); `CanonicalRequestLogFilter` ([sección 4.9](#49-canonicalrequestlogfilterjava)) lo lee de MDC |
| `service.name` | `service.name` | Nombre del servicio (`onp-<s>-<m>`) | `application.yml` |

### 7.2 Pasar de un log a su traza en Jaeger

Desde cualquier línea de log en Kibana Discover, copiar el valor del campo `trace.id` e ingresarlo en Jaeger UI en el campo **Trace ID**. Muestra la traza completa de la petición que generó ese log.

### 7.3 Propagación entre microservicios

Micrometer Tracing propaga automáticamente el contexto de traza en el header `traceparent` (W3C Trace Context) en todas las llamadas HTTP salientes realizadas con `RestTemplate` o `WebClient` gestionados por Spring. No se requiere código adicional. Si el servicio destino también tiene trazas activas, Jaeger enlaza ambos servicios bajo el mismo `traceId`.

---

## 8. Métricas y health checks

### 8.1 Endpoints Actuator obligatorios

Con la configuración de [sección 4.2](#42-applicationyml-configuracion-base) (`management.endpoints.web.exposure.include=health,info,metrics,prometheus`), los siguientes endpoints deben estar activos en todos los entornos:

| Endpoint | Propósito | Consumido por |
|---|---|---|
| `GET /actuator/health` | Estado del servicio (liveness / readiness) | K8s probes, monitoreo |
| `GET /actuator/info` | Versión del servicio (`info.app.version`) | Plataforma, dashboards |
| `GET /actuator/metrics` | Listado de métricas disponibles | Diagnóstico |
| `GET /actuator/prometheus` | Métricas en formato Prometheus scrape | Prometheus / Grafana |

> **ADVERTENCIA — seguridad:** Los endpoints de Actuator **no deben ser accesibles desde Internet** ni desde redes externas. Deben estar limitados a la red interna del clúster K8s o protegidos por el API Gateway (WSO2). Ver LIN-SEC-APP-001 cuando esté disponible.

### 8.2 Métricas mínimas con Micrometer

Spring Boot + Micrometer registra automáticamente las siguientes métricas sin código adicional:

| Métrica | Descripción |
|---|---|
| `http.server.requests` | Contador y timer de todas las peticiones HTTP entrantes |
| `jvm.memory.used` / `jvm.memory.max` | Uso de memoria JVM |
| `jvm.threads.live` | Threads JVM activos |
| `hikaricp.connections.active` | Conexiones activas al pool de BD |
| `hikaricp.connections.pending` | Conexiones en espera en el pool |
| `process.cpu.usage` | CPU consumida por el proceso |
| `logback.events` | Eventos de log por nivel (INFO, WARN, ERROR) |

**Métricas de negocio personalizadas (cuando aplica):**

```java
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class AfiliacionService {

    private final Counter afiliacionesRegistradas;

    public AfiliacionService(MeterRegistry registry) {
        this.afiliacionesRegistradas = Counter.builder("onp.afiliaciones.registradas")
            .description("Total de afiliaciones registradas exitosamente")
            .tag("regimen", "SPP")
            .register(registry);
    }

    public void registrar(SolicitudAfiliacion solicitud) {
        // ... lógica ...
        afiliacionesRegistradas.increment();
    }
}
```

**Convención de nombres de métricas personalizadas:** `onp.<sistema>.<modulo>.<metrica>` en snake_case con puntos como separadores. Ejemplo: `onp.pensiones.liquidacion.calculos_completados`.

### 8.3 Liveness y Readiness en Kubernetes

Spring Boot Actuator expone probes de Kubernetes automáticamente cuando el servicio corre en K8s:

```yaml
# Fragmento del Deployment (responsabilidad de Plataforma)
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

---

## 9. Infraestructura de observabilidad ONP

### 9.1 OTEL Collector — responsabilidad de Plataforma

El OTEL Collector es un componente operado exclusivamente por Plataforma (OTI). El desarrollador configura los endpoints en `application-{env}.yml` ([sección 4.3](#43-application-devyml)–4.5); no administra el Collector directamente.

**URLs fijas por entorno (no modificar sin comunicación de Plataforma):**

| Entorno | URL base del Collector |
|---|---|
| DEV | `http://otel-collector.otel-dev.svc.cluster.local:4318` |
| QA | `http://otel-collector.otel-qa.svc.cluster.local:4318` |
| PROD | `http://otel-collector.otel.svc.cluster.local:4318` |

El Collector enruta:
- Trazas (`/v1/traces`) → Jaeger
- Logs (`/v1/logs`) → Elasticsearch
- Métricas: recolectadas via Prometheus scrape del endpoint `/actuator/prometheus`

### 9.2 Retención de datos

| Entorno | Logs (Elasticsearch) | Trazas (Jaeger) | Métricas (Prometheus) |
|---|---|---|---|
| DEV | 30 días | 30 días | 15 días |
| QA | 30 días | 30 días | 15 días |
| PROD | **90 días** | **90 días** | 90 días |

La retención es responsabilidad de Plataforma. Los índices en Elasticsearch siguen el patrón `onp-logs-{environment}`.

### 9.3 Dashboards mínimos obligatorios

Todo servicio en PROD debe tener configurados los siguientes dashboards antes del go-live:

**Grafana (métricas):** Panel mínimo con:
- Tasa de peticiones HTTP por servicio (req/s)
- Latencia P50 / P95 / P99 (`http.server.requests`)
- Tasa de errores 4xx y 5xx
- Uso de memoria JVM y conexiones activas al pool
- Métrica de liveness (`UP/DOWN`)

**Kibana (logs):** Data View `onp-logs-*` con:
- Saved search: logs del servicio (`service.name: "onp-<sistema>-<modulo>"`)
- Saved search: errores del servicio (filtro `log.level: "ERROR"`)
- Saved search: log canónico de requests (filtro `message: "REQUEST"`)

**Jaeger (trazas):**
- Búsqueda por servicio: `onp-<sistema>-<modulo>`
- Búsqueda por operación: spans `@NewSpan` de operaciones críticas

La configuración de dashboards es responsabilidad de Plataforma con insumos del equipo de desarrollo (qué métricas de negocio son relevantes).

---

## 10. Responsabilidades

### 10.1 Equipo de Desarrollo

| Responsabilidad | Artefacto | Momento |
|---|---|---|
| Configurar dependencias Maven | `pom.xml` | Al crear el proyecto |
| Crear `application-{dev,qa,prod}.yml` con URLs del Collector | `application-*.yml` | Al crear el proyecto |
| Crear `logback-spring.xml` | `src/main/resources/` | Al crear el proyecto |
| Crear `OpenTelemetryLogbackConfig.java` | `config/` | Al crear el proyecto |
| Crear `Mask.java` | `util/` | Al crear el proyecto |
| Crear `CanonicalRequestLogFilter.java` | `filter/` | Al crear el proyecto |
| Crear `RequestIdFilter.java` | `filter/` | Al crear el proyecto |
| Instrumentar operaciones de negocio críticas con `@NewSpan` | `@Service` | Al implementar la operación |
| Marcar errores capturados con `Span.current().setStatus()` | `@Service` | Al implementar try/catch |
| Instrumentar jobs `@Scheduled` con `Tracer` | `scheduler/` | Al implementar el job |
| Cumplir política No PII en todos los logs | Todo el código | Siempre |
| Verificar trazas en Jaeger antes del pase a DEV | — | Antes de cada pase |
| Verificar logs en Kibana antes del pase a DEV | — | Antes de cada pase |
| Definir métricas de negocio personalizadas (si aplica) | `@Service` | Al implementar el dominio |
| Informar a Plataforma las métricas de negocio para los dashboards | — | Antes del go-live PROD |

### 10.2 Plataforma / Infraestructura

| Responsabilidad | Artefacto | Momento |
|---|---|---|
| Operar el OTEL Collector en los tres namespaces (otel-dev, otel-qa, otel) | K8s Deployment | Infraestructura base |
| Configurar `SPRING_PROFILES_ACTIVE` en el Deployment de cada servicio | `env` del Deployment | En cada pase |
| Crear Data View `onp-logs-*` en Kibana si no existe | Kibana | Al provisionar el stack |
| Configurar retención de índices Elasticsearch (30/30/90 días) | ILM policy | Infraestructura base |
| Configurar Prometheus para hacer scrape de `/actuator/prometheus` de cada servicio | `ServiceMonitor` o `prometheus.yml` | En cada pase |
| Configurar paneles Grafana mínimos con insumos del equipo de desarrollo | Grafana | Antes del go-live PROD |
| Actualizar URLs del Collector si cambian los namespaces o el nombre del componente | Comunicación → Desarrollo | Al cambiar infraestructura |
| Responder a `kubectl logs -n otel-{env}` en caso de problemas de telemetría | — | Soporte operacional |

**Ítem de verificación obligatorio en cada pase:**

```
[ ] Verificar que el Deployment del servicio tiene configurada la variable de entorno:
      SPRING_PROFILES_ACTIVE = <dev|qa|prod>
    Responsable: Plataforma (OTI)
    Validación: kubectl get deployment <nombre-servicio> -n <namespace> \
                  -o jsonpath='{.spec.template.spec.containers[0].env}'
```

### 10.3 Arquitectura

| Responsabilidad |
|---|
| Mantener este lineamiento actualizado ante cambios del stack de observabilidad |
| Aprobar ADRs de excepción ([sección 13](#13-proceso-de-excepcion-adr)) |
| Revisar el cumplimiento en los code reviews de nuevos servicios |
| Actualizar las URLs de los Collectors en este documento cuando Plataforma las comunique |

---

## 11. Checklist de conformidad

El siguiente checklist debe completarse antes del pase a DEV de cualquier servicio nuevo.

**Configuración (una vez por proyecto):**
- [ ] Dependencias de trazas y logging presentes en `pom.xml` ([sección 4.1](#41-dependencias-maven))
- [ ] `application.yml` base con `spring.application.name=onp-<sistema>-<modulo>` ([sección 4.2](#42-applicationyml-configuracion-base))
- [ ] `application-dev.yml`, `application-qa.yml`, `application-prod.yml` con URLs del Collector ([sección 4.3](#43-application-devyml)–4.5)
- [ ] `logback-spring.xml` creado con appenders `JSON_STDOUT` y `OpenTelemetry` ([sección 4.6](#46-logback-springxml))
- [ ] `OpenTelemetryLogbackConfig.java` creado ([sección 4.7](#47-opentelemetrylogbackconfigjava))
- [ ] `Mask.java` creado en paquete `util/` ([sección 4.8](#48-maskjava-utilidad-no-pii))
- [ ] `CanonicalRequestLogFilter.java` creado en paquete `filter/` ([sección 4.9](#49-canonicalrequestlogfilterjava))
- [ ] `RequestIdFilter.java` creado en paquete `filter/` ([sección 4.10](#410-requestidfilterjava))

**Instrumentación:**
- [ ] Operaciones de negocio críticas instrumentadas con `@NewSpan` siguiendo convención `<s>.<m>.<op>` ([sección 5.3](#53-operaciones-de-negocio-newspan), [sección 5.6](#56-convenciones-de-nomenclatura-de-spans))
- [ ] Bloques `catch` que capturan errores de negocio llaman `Span.current().setStatus()` ([sección 5.2](#52-errores-capturados-spansetstatus-manual))
- [ ] Jobs `@Scheduled` instrumentados con `Tracer` y `span.end()` en `finally` ([sección 5.5](#55-tareas-programadas-scheduled))
- [ ] Política No PII aplicada en todos los logs — uso de `Mask.*` donde corresponde ([sección 6.2](#62-politica-no-pii-datos-sensibles))
- [ ] No se usa `System.out.println()` ni `System.err.println()` ([sección 12](#12-anti-patrones))
- [ ] No se logea la excepción más de una vez por fallo ([sección 12](#12-anti-patrones))
- [ ] Datos sensibles ausentes de atributos de span ([sección 5.3](#53-operaciones-de-negocio-newspan))

**Verificación:**
- [ ] `traceId` y `spanId` aparecen en los logs de consola tras al menos una petición ([sección 5.7](#57-verificacion-en-jaeger))
- [ ] El servicio aparece en Jaeger UI con sus spans ([sección 5.7](#57-verificacion-en-jaeger))
- [ ] El log canónico `message: "REQUEST"` aparece en Kibana con todos los campos ([sección 6.4](#64-verificacion-en-kibana))

**Métricas:**
- [ ] `/actuator/health` responde `{"status":"UP"}` ([sección 8.1](#81-endpoints-actuator-obligatorios))
- [ ] `/actuator/prometheus` responde con métricas en formato texto Prometheus ([sección 8.1](#81-endpoints-actuator-obligatorios))
- [ ] Plataforma confirmó que Prometheus hace scrape del servicio ([sección 10.2](#102-plataforma-infraestructura))

---

## 12. Anti-patrones

| Anti-patrón | Problema | Solución |
|---|---|---|
| `System.out.println()` / `System.err.println()` | Bypasa Logback → salida texto plano invisible en Kibana | Usar `log.info()` / `log.error()` via SLF4J |
| Saltos de línea dentro del mensaje (`\n`) | El Collector interpreta cada línea como evento separado → JSON corrupto | Mensaje sin saltos de línea; excepción como segundo argumento de `log.error()` |
| Log de excepción en múltiples capas | Tres copias del mismo stacktrace por fallo → inflación de almacenamiento | Loggear cada excepción exactamente una vez, en la capa que puede contextualizarla |
| `log.error()` para errores de negocio esperados (ej. 404) | Genera alarmas falsas, contamina el canal ERROR | Usar `log.warn()` sin stacktrace para errores recuperables esperados |
| Todo en `log.info()` | Millones de líneas por día, dashboards inutilizables | `INFO` solo para eventos de negocio; `DEBUG` para traza técnica (solo DEV) |
| DNI / token / nombre completo en logs | Violación Ley N.° 29733 | Usar `Mask.*` para datos sensibles; tokens nunca |
| `new RestTemplate()` / `WebClient.create()` | Sin interceptor de trazas → llamadas salientes invisibles en Jaeger | Usar `@Bean` con `RestTemplateBuilder` / `WebClient.Builder` |
| `catch` sin `Span.current().setStatus()` | Span verde en Jaeger aunque la operación falló | Siempre llamar `setStatus(ERROR)` + `recordException()` en catch |
| Atributos de span con datos PII | Datos sensibles en Jaeger → misma violación que en logs | Solo atributos de negocio no sensibles en `setAttribute()` |
| `@NewSpan` en cada método | Traza imposible de leer; overhead en Jaeger | Solo en operaciones costosas o críticas |
| Sampling `1.0` en PROD | Saturación de Elasticsearch con alto tráfico | Usar `0.1` en PROD; ajustar según volumen real |

---

## 13. Proceso de excepción (ADR)

Toda desviación a este lineamiento requiere un **Architecture Decision Record (ADR)** aprobado por Arquitectura antes de implementarse.

**Casos que generan ADR:**

| Caso | Justificación mínima requerida |
|---|---|
| No instrumentar un servicio con trazas | Por qué el servicio no necesita visibilidad operacional |
| Usar un stack de observabilidad diferente al ONP | Incompatibilidad técnica demostrable; plan de convergencia |
| Cambiar la política de sampling PROD a `> 0.1` | Análisis de volumen de tráfico y costo de almacenamiento |
| Deshabilitar el log canónico de request | Alternativa que provea la misma capacidad de correlación |
| Retención diferente a los valores establecidos | Marco legal o regulatorio que exija período distinto |
| Loggear datos de negocio adicionales que rozan PII | Análisis legal con DPO, técnica de anonimización aprobada |
| Usar `logback.xml` en lugar de `logback-spring.xml` | Razón técnica; garantizar mismos campos ECS y appenders |

**Estructura mínima del ADR:**

```markdown
## ADR-OBS-<NNN>: <título>
- **Fecha:** YYYY-MM-DD
- **Estado:** Propuesto / Aprobado / Rechazado
- **Responsable:** <nombre, área>
- **Contexto:** ¿Cuál es la situación que genera la necesidad de desviarse?
- **Decisión:** ¿Qué se propone hacer y por qué?
- **Alternativas descartadas:** ¿Qué otras opciones se evaluaron?
- **Consecuencias:** ¿Qué impacto operacional o de visibilidad genera?
- **Plan de convergencia:** ¿Cuándo y cómo se retoma el estándar?
- **Fecha de revisión:** <fecha en que se revisará la vigencia de esta excepción>
```

---

## 14. Glosario

| Término | Definición |
|---|---|
| **Traza (Trace)** | Registro del recorrido completo de una petición a través del sistema, desde la entrada hasta la respuesta. Agrupa todos los spans relacionados bajo un mismo `traceId`. |
| **Span** | Unidad de trabajo dentro de una traza. Tiene nombre, duración, estado (OK/ERROR) y atributos. Puede ser padre de otros spans formando un árbol. |
| **Contexto de traza (Trace Context)** | Información que conecta spans bajo la misma traza: `traceId` y `spanId`. Propagado automáticamente en headers HTTP (`traceparent`). |
| **OTEL / OpenTelemetry** | Framework de observabilidad open-source; proveedor de: SDK Java, semántica de atributos (Semantic Conventions), protocolo OTLP. |
| **OTLP** | OpenTelemetry Protocol — protocolo de exportación de trazas, logs y métricas al OTEL Collector. |
| **OTEL Collector** | Componente intermediario que recibe señales OTEL y las enruta a los backends (Jaeger, Elasticsearch, Prometheus). Operado por Plataforma. |
| **Jaeger** | Plataforma de visualización y almacenamiento de trazas distribuidas. |
| **ECS (Elastic Common Schema)** | Especificación de Elastic que define nombres de campos estándar para logs (`@timestamp`, `log.level`, `trace.id`, etc.), compatibles con las OTEL Semantic Conventions. |
| **MDC (Mapped Diagnostic Context)** | Mecanismo de SLF4J para asociar pares clave-valor al hilo actual; Logback los incluye automáticamente en cada log. |
| **Log canónico** | Línea de log única por petición HTTP que emite `CanonicalRequestLogFilter` al finalizar la request con: método, ruta, status, duración y usuario. |
| **Sampling** | Porcentaje de peticiones que generan trazas. `1.0` = 100%, `0.1` = 10%. Controla el volumen de datos en Jaeger. |
| **PII (Personally Identifiable Information)** | Datos que permiten identificar a una persona: DNI, nombre, email, teléfono, cuenta bancaria, etc. Prohibidos en logs y trazas por Ley N.° 29733. |
| **Micrometer Tracing** | Capa de abstracción de Spring Boot sobre SDKs de tracing (OTEL, Brave). Provee `@NewSpan`, `@ContinueSpan`, `Tracer`. |
| **`traceparent`** | Header HTTP estándar W3C (formato: `00-traceId-spanId-flags`) usado para propagar el contexto de traza entre servicios. |
| **X-Request-ID** | Header HTTP institucional ONP para correlacionar logs de la misma petición. Generado por `RequestIdFilter` si el cliente no lo envía. |

---

## Apéndice A — Atributos OTEL más usados

| Atributo | Descripción | Ejemplo ONP |
|---|---|---|
| `service.name` | Nombre del servicio | `onp-afiliaciones-registro` |
| `service.version` | Versión del servicio | `1.3.0` |
| `deployment.environment` | Entorno de despliegue | `development` / `quality` / `production` |
| `http.method` | Método HTTP | `GET`, `POST`, `PUT`, `DELETE` |
| `http.route` | Ruta del endpoint | `/api/v1/afiliaciones/{id}` |
| `http.status_code` | Código de respuesta HTTP | `200`, `400`, `500` |
| `db.system` | Motor de base de datos | `oracle` |
| `db.operation` | Tipo de operación SQL | `SELECT`, `INSERT`, `UPDATE` |
| `db.statement` | Sentencia SQL (parámetros enmascarados) | `SELECT ... WHERE dni=?` |
| `net.peer.name` | Host del servicio externo | `api.reniec.gob.pe` |
| `exception.type` | Clase de la excepción | `java.lang.NullPointerException` |
| `otel.status_code` | Estado del span | `OK` / `ERROR` |

---

## Apéndice B — Campos ECS en logs

**Campos presentes en todas las líneas de log de la petición** (via MDC o Logback):

| Campo (Kibana) | Descripción | Origen |
|---|---|---|
| `@timestamp` | Fecha y hora ISO 8601 UTC | Logback automático |
| `log.level` | INFO / DEBUG / WARN / ERROR | Logback automático |
| `log.logger` | Clase Java que generó el log | Logback automático |
| `message` | Mensaje del evento | Desarrollador |
| `service.name` | `onp-<sistema>-<modulo>` | `application.yml` |
| `service.version` | Versión del `pom.xml` | `application.yml` |
| `service.environment` | `development` / `quality` / `production` | `application-{env}.yml` |
| `trace.id` | ID de traza OTEL | OTEL + MDC automático |
| `span.id` | ID de span OTEL | OTEL + MDC automático |
| `http.request.id` | X-Request-ID de la petición | `RequestIdFilter` @Order(1) — puesto en MDC |
| `user.id` | Usuario autenticado | `SaaTokenValidationFilter` @Order(2) — puesto en MDC; `CanonicalRequestLogFilter` @Order(3) lo lee |
| `error.stack_trace` | Stack trace (solo en `log.error()`) | Logback automático |

**Campos presentes únicamente en el log canónico de request** (emitido por `CanonicalRequestLogFilter`, `message: "REQUEST"`):

| Campo (Kibana) | Descripción | Valores de ejemplo |
|---|---|---|
| `http.request.method` | Método HTTP | `GET`, `POST`, `PUT`, `DELETE` |
| `url.path` | Ruta del endpoint | `/api/v1/afiliaciones/1` |
| `http.response.status_code` | Status HTTP de la respuesta | `200`, `400`, `500` |
| `duration_ms` | Duración total en milisegundos | `143` |

> **Nota:** Micrometer Tracing inyecta el contexto de traza en el MDC con los nombres `traceId` y `spanId` (lo que aparece en `kubectl logs`). El OTEL Collector los recibe vía OTLP y los escribe en Elasticsearch con el nombre canónico ECS: `trace.id` y `span.id`.

---

## Apéndice C — Convenciones de nomenclatura ONP

| Concepto | Formato | Ejemplo |
|---|---|---|
| Nombre de servicio | `onp-<sistema>-<modulo>` | `onp-pensiones-liquidacion` |
| Span de negocio (`@NewSpan`) | `<sistema>.<modulo>.<operacion-en-kebab-case>` | `pensiones.liquidacion.calcular-beneficio` |
| Span de batch (`@Scheduled`) | `<sistema>.batch.<operacion-en-kebab-case>` | `pensiones.batch.reconciliacion-diaria` |
| Métrica de negocio | `onp.<sistema>.<modulo>.<metrica>` | `onp.pensiones.liquidacion.calculos_completados` |
| Entorno DEV | `deployment.environment=development` | — |
| Entorno QA | `deployment.environment=quality` | — |
| Entorno PROD | `deployment.environment=production` | — |
| Data View logs Kibana | `onp-logs-*` | Cubre DEV, QA y PROD |
| Índice DEV | `onp-logs-development` | — |
| Índice QA | `onp-logs-quality` | — |
| Índice PROD | `onp-logs-production` | — |

---

*Este documento es fuente autoritativa de observabilidad en ONP. Cualquier referencia a logs, trazas, métricas o campos de correlación en otros lineamientos apunta a este documento — no redefine sus contenidos.*

*LIN-OBS-001 — Lineamiento de Log Centralizado, Trazabilidad y Observabilidad — ONP*
