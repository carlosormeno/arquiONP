# Lineamiento de Mensajería y Bus de Eventos — ONP

---

**Código:** LIN-BUS-001  
**Marco rector:** LIN-ARQ-001  
**Versión:** v0.1.7  
**Fecha:** 2026-08-17  
**Propietario documental:** OTI / Arquitectura  
**Clasificación:** Uso Interno (Técnico)  
**Dirigido a:** Equipos de Desarrollo, Plataforma/Infraestructura, Arquitectura  
**Estado:** En revisión  

---

## Historial de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| v0.1.0 | 2026-06-05 | Arquitectura OTI | Versión inicial. Formaliza la regla transitoria de LIN-ARQ-000 sección 3.7 y establece el estándar de uso de Kafka como broker institucional |
| v0.1.1 | 2026-07-09 | Arquitectura OTI | Corrige la cita colgante de §9.4 hacia el documento congelado `LIN-ARQ-000 §3.6`: el patrón completo de la variante Saga sobre monolitos ahora vive en `LIN-ARQ-001 §3.3.1` (Marco Rector vigente) |
| v0.1.2 | 2026-07-09 | Arquitectura OTI | Nombra explícitamente el patrón DDD Lenguaje Publicado (*Published Language*) en §5, del cual el envelope CloudEvents y sus reglas de evolución son la implementación institucional |
| v0.1.3 | 2026-07-09 | Arquitectura OTI | Completa §4.3 con las 2 situaciones faltantes de "cuándo NO usar el bus" (desacoplar sin análisis, observabilidad inmadura) y añade advertencias sobre Event Sourcing (no está en la lista de patrones, no es CQRS), ausentes en todo el ecosistema tras la redistribución del documento congelado |
| v0.1.4 | 2026-07-09 | Arquitectura OTI | Corrige §8.6: `ExponentialBackOffWithMaxRetries` fue retirada de Spring Framework 6.x — el ejemplo no compilaba contra el stack vigente (Spring Boot 3.x). Se reemplaza por `ExponentialBackOff.setMaxAttempts(int)`, validado con build real de Maven en `template-backend-java-modular` |
| v0.1.6 | 2026-08-09 | Arquitectura OTI | `§7.3` deja de reproducir el DDL de `EVT_OUTBOX` y remite a su dueño `LIN-BD-ORA-001 §3.10` (`GOB-CHK-001` H14.3). Las dos copias eran idénticas, pero es la misma estructura que ya divergió tres veces en el corpus; este lineamiento conserva lo suyo: el proceso de relevo y el contrato del evento |
| v0.1.7 | 2026-08-17 | Arquitectura OTI | Revisión de fondo (`GOB-CHK-001` H28). **(1) El ejemplo de consumidor de `§8.3` perdía mensajes.** Capturaba el error recuperable, omitía el `acknowledge` y anotaba «Kafka reintentará desde el mismo offset» — con `ack-mode: MANUAL` eso **no ocurre**: el contenedor sigue con el registro siguiente y, al confirmarse uno posterior, el offset avanza por encima del fallido. Además implementaba a mano un `enviarADlq` que compite con el `DefaultErrorHandler` de `§8.6`, único mecanismo soportado. Reescrito para que las excepciones se propaguen. **(2) `§7.2` fijaba `retries: 3`**, que con la idempotencia activa acorta a tres intentos una ventana de 120 s gobernada por `delivery.timeout.ms` y contradice el propósito de `acks: all`. Eliminado. **(3) `§6.1` exigía «notación kebab-case»** cuando ninguno de sus ejemplos lleva guion y `ciclovida` sería `ciclo-vida` bajo esa regla; se describe la convención real. **(4)** El log de evento de `§11.2` omitía `span.id`; `§12.1` deja constancia de la verificación de namespaces pendiente con Plataforma (`LIN-K8S-001 §4.4`, que además citaba mal estos namespaces y se corrigió); y `§2` no listaba `LIN-DIS-001`, `LIN-PAT-001`, `LIN-VER-001`, `GOB-MAT-001` ni **`ADR-CLOUDEVENTS-001`**, que es la decisión que sustenta el envelope de `§5.2`. El documento pasa a **En revisión** |
| v0.1.5 | 2026-07-14 | Arquitectura OTI | Corrige 5 citas residuales al documento congelado `LIN-ARQ-000` que quedaron sin migrar en la reconciliación de marco rector: §1.1, §1.3 (Hexagonal → `LIN-DIS-001 §2.3`), tabla de §2, principio P7 y regla de ADR en §9.4 — todas redirigidas a `LIN-ARQ-001 §3.3`/`§4.2` según corresponda |

---

## Tabla de contenidos

1. [Objetivo y alcance](#1-objetivo-y-alcance)
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)
3. [Principios rectores](#3-principios-rectores)
4. [Arquitectura de mensajería ONP](#4-arquitectura-de-mensajería-onp)
5. [Diseño de eventos](#5-diseño-de-eventos)
6. [Diseño de tópicos](#6-diseño-de-tópicos)
7. [Productores](#7-productores)
8. [Consumidores](#8-consumidores)
9. [Transacciones distribuidas — Saga y Outbox](#9-transacciones-distribuidas--saga-y-outbox)
10. [Seguridad](#10-seguridad)
11. [Observabilidad](#11-observabilidad)
12. [Despliegue en K8s](#12-despliegue-en-k8s)
13. [Gobierno del bus](#13-gobierno-del-bus)
14. [Responsabilidades](#14-responsabilidades)
15. [Checklist de conformidad](#15-checklist-de-conformidad)
16. [Anti-patrones](#16-anti-patrones)
17. [Proceso de excepción (ADR)](#17-proceso-de-excepción-adr)
18. [Glosario](#18-glosario)
- [Apéndice A — Contrato de eventos (plantilla)](#apéndice-a--contrato-de-eventos-plantilla)
- [Apéndice B — Catálogo de eventos (estructura)](#apéndice-b--catálogo-de-eventos-estructura)

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Este lineamiento establece los estándares obligatorios para el diseño, implementación, operación y gobierno del bus de eventos institucional de la ONP, basado en Apache Kafka. Define:

- El broker institucional aprobado y su modelo de operación.
- Las normas de diseño de eventos, tópicos, productores y consumidores.
- Los patrones de consistencia distribuida aplicables (Saga, Transactional Outbox).
- Los requisitos de seguridad, observabilidad y gobierno documental.

**Este lineamiento formaliza y reemplaza la regla transitoria de mensajería que originalmente estableció el documento congelado `LIN-ARQ-000` (sección 3.7).** Su vigencia levanta la restricción de adopción ad hoc de mensajería para los sistemas que cumplan con las normas aquí definidas; el gobierno vigente de EDA a nivel macro está en `LIN-ARQ-001 §4.2`.

### 1.2 Alcance

**Aplica a:**
- Todo sistema que publique o consuma eventos a través del bus de eventos institucional.
- Nuevos sistemas diseñados con EDA (Event-Driven Architecture) desde el inicio.
- Módulos del Monolito Modular que se extraigan como microservicios y requieran comunicación asíncrona entre contextos.
- Integraciones asíncronas entre sistemas internos de ONP.

**Fuera de alcance:**

| Tema | Dónde se cubre |
|---|---|
| Eventos de dominio internos en memoria dentro del mismo proceso | No requieren broker — son implementación interna del módulo |
| Scheduling local y jobs batch | LIN-DEV-JAVA-001 |
| Observabilidad general del servicio (trazas, logs, métricas JVM) | LIN-OBS-001 |
| Despliegue general de servicios en K8s | LIN-K8S-001 |
| Integraciones sincrónicas REST entre servicios | LIN-API-REST-001 |

### 1.3 Prerrequisito arquitectónico

De acuerdo con `LIN-DIS-001 §2.3`, todo módulo que use mensajería para coordinar transacciones distribuidas debe haber adoptado previamente una **Arquitectura Hexagonal**. El broker no es un atajo para omitir esa frontera — los ports y adapters hacen explícita la separación entre el dominio y la infraestructura de mensajería.

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relevancia |
|---|---|---|
| Lineamiento Marco Rector de Arquitectura de Software | LIN-ARQ-001 | Marco rector — EDA (§4.2), Saga con Transactional Outbox (§3.3) |
| Lineamiento Log, Trazabilidad y Observabilidad | LIN-OBS-001 | **Prerequisito para adoptar EDA** (LIN-ARQ-001 §4.2) — correlación de trazas productor→consumidor en Jaeger, política No PII, campos de log estructurado obligatorios (§11) |
| Lineamiento de Contenedores y Orquestación | LIN-K8S-001 | Despliegue de Kafka y servicios consumidores/productores en K8s |
| Lineamiento de Seguridad en Aplicaciones | LIN-SEC-APP-001 | Autenticación, autorización y gestión de secretos |
| Lineamiento Estándar Desarrollo Java | LIN-DEV-JAVA-001 | Implementación de productores y consumidores en Spring Boot |
| Lineamiento Estándar de Pruebas | LIN-TEST-001 | Pruebas de sistemas con dependencias de broker |
| Lineamiento Estándar Base de Datos | LIN-BD-ORA-001 | Tabla OUTBOX en Oracle — diseño y consideraciones ACID |
| Lineamiento de Diseño de Software | LIN-DIS-001 | Nivel 2 — Arquitectura Hexagonal (§2.3), prerrequisito de §1.3, y CQRS (§4) |
| Catálogo Oficial de Patrones | LIN-PAT-001 | Fichas `PAT-MSG-01` (Publisher/Subscriber, `PT01`), `PAT-MSG-02` (DLQ, `PT02`) y `PAT-MSG-03` (Sagas, `PT09`) |
| Versionamiento y Control de Cambios | LIN-VER-001 | Control de cambios del contrato de eventos y evidencias en el Merge Request |
| ADR de adopción de CloudEvents | ADR-CLOUDEVENTS-001 | **Decisión que sustenta el envelope de §5.2** |
| Matriz de Propiedad Documental | GOB-MAT-001 | Determina qué documento es dueño de cada tema |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **Un broker institucional** | ONP opera un único broker Kafka institucional. No se aprueban brokers paralelos por proyecto sin ADR aprobado por Arquitectura OTI. |
| P2 | **Evento = cambio de estado de negocio** | Solo se publican eventos que representen cambios de estado reales del dominio. No se usa el broker como canal de comandos RPC ni como log de auditoría interna. |
| P3 | **Contrato primero** | Todo evento debe tener un contrato documentado (Apéndice A) y registrado en el catálogo institucional (Apéndice B) antes de ser publicado en producción. |
| P4 | **Eventos pequeños** | Un evento contiene solo los identificadores y datos mínimos necesarios. Si el consumidor necesita más datos, consulta directamente al servicio productor. |
| P5 | **Consistencia eventual explícita** | El uso de mensajería implica consistencia eventual. Esta decisión debe ser explícita en el diseño y aceptada por el negocio antes de adoptar EDA. |
| P6 | **Idempotencia en consumidores** | Todo consumidor debe estar diseñado para procesar el mismo evento más de una vez sin efectos secundarios indeseados. |
| P7 | **Observabilidad del flujo asíncrono** | Cada evento publicado y consumido debe tener trazabilidad completa en Jaeger. Un flujo EDA sin trazas no está listo para producción. `LIN-ARQ-001 §4.2` establece la madurez de LIN-OBS-001 como condición previa a la adopción de EDA. |
| P8 | **Gobierno centralizado** | Arquitectura OTI aprueba la creación de tópicos y el contrato de eventos. Plataforma crea y opera el broker. Seguridad OTI valida las ACLs. |

---

## 4. Arquitectura de mensajería ONP

### 4.1 Broker institucional

ONP adopta **Apache Kafka** como broker de mensajería institucional. La selección se basa en:

- Alta durabilidad mediante replicación configurable por tópico.
- Soporte nativo para grupos de consumidores y procesamiento paralelo.
- Retención configurable por tópico, lo que permite replay de eventos ante fallos.
- Ecosistema maduro para K8s mediante el operador Strimzi.

### 4.2 Topología

```
┌────────────────────────────────────────────────────────────────────────┐
│                       Bus de Eventos ONP — Kafka                       │
│                                                                        │
│  ┌──────────────────────────────────────────────────────────────────┐ │
│  │  expedientes.ciclovida.presentado          (3 particiones)       │ │
│  │  aportes.cuenta.actualizada                (3 particiones)       │ │
│  │  prestaciones.pension.aprobada             (3 particiones)       │ │
│  │  expedientes.ciclovida.presentado.dlq      (1 partición)         │ │
│  │  ...                                                              │ │
│  └──────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│  Autenticación: SASL/SCRAM-SHA-512   Cifrado: TLS                     │
│  Réplicas: mínimo 3 en producción    Operador: Strimzi                │
└────────────────────────────────────────────────────────────────────────┘
           ▲                                          │
           │  produce (Outbox Relay)                  │  consume
           │                                          ▼
┌──────────────────────┐                  ┌──────────────────────────┐
│  Servicio Productor  │                  │   Servicio Consumidor    │
│                      │                  │                          │
│  @Transactional      │                  │  Grupo: {svc}-grp        │
│  ├─ Oracle (dominio) │                  │  Commit: manual          │
│  └─ EVT_OUTBOX       │                  │  Idempotente             │
└──────────────────────┘                  └──────────────────────────┘
```

### 4.3 Cuándo usar el bus

El bus de eventos es el canal correcto cuando:

| Condición | Ejemplo |
|---|---|
| La acción del productor está completa sin importar cuándo reacciona el consumidor | Notificaciones, auditoría, sincronización entre contextos |
| La consistencia eventual es aceptable para el flujo de negocio | El dato puede estar levemente desactualizado por un período corto |
| Se necesita desacoplar dos bounded contexts sin dependencia de despliegue | El productor no conoce ni depende del ciclo de vida del consumidor |
| Se coordina una transacción distribuida entre microservicios | Patrón Saga — ver sección 9 |

El bus **no** es el canal correcto cuando:

| Situación | Canal correcto |
|---|---|
| Lógica core que requiere ACID (cálculo de pensión, liquidación, aportes) | `@Transactional` en el Monolito Modular |
| El productor necesita la respuesta del consumidor para continuar | REST sincrónico — LIN-API-REST-001 |
| Comunicación interna dentro de un mismo módulo o proceso | Llamada directa en memoria |
| Desacoplar por desacoplar, sin análisis de consistencia | El diseño del evento, el esquema de compensación y la operabilidad del broker tienen un costo real — desacoplar sin justificación no es una mejora arquitectónica |
| Equipo sin observabilidad madura para sistemas asíncronos | EDA es opaco sin trazas distribuidas que conecten el trace del productor con el del consumidor. Ningún flujo entra a producción sin trazabilidad completa (`LIN-OBS-001`) |

**Event Sourcing no está en la lista de patrones aplicables.** Almacenar el estado como secuencia de eventos introduce complejidad operativa (versionado de esquemas, proyecciones, *replay*) que supera el beneficio en los sistemas actuales de ONP. Su adopción requiere ADR aprobado por Arquitectura OTI. **Event Sourcing tampoco es CQRS** — son patrones distintos y no intercambiables; CQRS (`LIN-DIS-001 §4`) separa modelos de lectura/escritura sin cambiar cómo se persiste el estado de escritura.

---

## 5. Diseño de eventos

Este envelope y sus reglas de evolución (§5.5) son la implementación institucional del patrón DDD **Lenguaje Publicado (*Published Language*)**: un contrato de datos explícito y versionado que un productor expone a sus consumidores, de forma que ningún consumidor dependa de la estructura interna del productor. Para comunicación síncrona, el Lenguaje Publicado equivalente es el contrato OpenAPI (`LIN-API-REST-001`).

### 5.1 Qué es un evento

Un evento representa un **hecho que ocurrió** en el dominio: es inmutable, tiene un timestamp y un identificador único. No es un comando (orden de hacer algo) ni una consulta (solicitud de datos).

```
✓  ExpedientePresentado        ← hecho ocurrido
✓  AporteRegistrado            ← hecho ocurrido
✗  RegistrarAporte             ← esto es un comando
✗  ObtenerExpediente           ← esto es una consulta
```

Revisar el catálogo institucional (Apéndice B) antes de definir un nuevo evento, para evitar duplicidad con eventos ya existentes de otros sistemas.

### 5.2 Estructura mínima — envelope del evento

El envelope ONP es **conforme a CloudEvents v1.0** (especificación CNCF). El cumplimiento de este estándar garantiza interoperabilidad con herramientas del ecosistema cloud-native y con sistemas externos (otras instituciones del Estado) sin necesidad de adaptadores propietarios.

Todo evento publicado en el bus debe incluir el siguiente envelope:

```json
{
  "specversion":     "1.0",
  "id":              "550e8400-e29b-41d4-a716-446655440000",
  "source":          "/onp/expedientes",
  "type":            "pe.gob.onp.expedientes.ciclovida.presentado",
  "time":            "2026-06-05T10:30:00Z",
  "datacontenttype": "application/json",
  "dataschema":      "/onp/schemas/expedientes/ciclovida/presentado/1.0",
  "traceparent":     "00-abc123def456abc123def456abc12345-00f067aa0ba902b7-01",
  "data": {
    "expedienteId": "EXP-2026-00123"
  }
}
```

| Campo | CloudEvents | Tipo | Obligatorio | Descripción |
|---|---|---|---|---|
| `specversion` | Obligatorio | String | Sí | Siempre `"1.0"` — identifica la versión de la spec CloudEvents |
| `id` | Obligatorio | UUID v4 | Sí | Identificador único del evento — permite detectar duplicados en consumidores |
| `source` | Obligatorio | URI | Sí | Contexto de origen: `/onp/{servicio}` (ej. `/onp/expedientes`, `/onp/aportes`) |
| `type` | Obligatorio | String | Sí | Tipo del evento en formato reverse-DNS: `pe.gob.onp.{dominio}.{clasificacion}.{descripcion}`. El tópico Kafka es el mismo sin el prefijo `pe.gob.onp.` (§6.1) |
| `time` | Opcional | ISO 8601 UTC | Sí | Momento en que ocurrió el hecho de negocio |
| `datacontenttype` | Opcional | String | Sí | Siempre `"application/json"` |
| `dataschema` | Opcional | URI | Sí | URI del esquema del evento con su versión: `/onp/schemas/{dominio}/{clasificacion}/{descripcion}/{version}` |
| `traceparent` | Extensión tracing | String | Sí | Traza W3C TraceContext: `{ver}-{traceId32hex}-{spanId16hex}-{flags}` — propaga la traza al consumidor (ver sección 11) |
| `data` | Obligatorio | Object | Sí | Datos del evento. Solo identificadores y datos mínimos |

> **`traceparent`** es la extensión oficial de distributed tracing de CloudEvents (CNCF). El formato W3C TraceContext (`00-<traceId>-<spanId>-<flags>`) es el mismo que usa Micrometer/OTEL internamente — no requiere transformación.

### 5.3 Política No PII

El payload **no debe contener datos personales sensibles**: DNI completo, nombre completo, datos bancarios, datos de salud. Se usan identificadores internos. Si el consumidor necesita datos personales, los obtiene mediante consulta REST al servicio origen. Ver política No PII en LIN-OBS-001.

### 5.4 Tamaño del evento

- Tamaño máximo recomendado: **256 KB** por mensaje.
- Si el consumidor necesita datos adicionales al identificador, realiza una consulta REST al servicio productor — no se engrosa el payload.
- Eventos mayores a 256 KB deben justificarse en el contrato (Apéndice A).

### 5.5 Evolución del contrato

Los contratos evolucionan. Las reglas de compatibilidad son:

| Tipo de cambio | Compatibilidad | Acción requerida |
|---|---|---|
| Agregar campo opcional al payload | Compatible hacia atrás | Incrementar versión menor (`1.0` → `1.1`) — consumidores existentes no se rompen |
| Eliminar campo o cambiar su tipo | Incompatible | Nueva versión mayor (`1.0` → `2.0`) — coordinar con todos los consumidores antes de desplegar |
| Renombrar campo | Incompatible | Igual que eliminación |

Un cambio incompatible requiere notificación a todos los consumidores conocidos y actualización del catálogo antes de desplegar en producción.

---

## 6. Diseño de tópicos

### 6.1 Nomenclatura

Los nombres de tópicos siguen el patrón:

```
{dominio}.{clasificacion}.{descripcion}
```

- Todo en **minúsculas**, con los tres segmentos separados por **punto**.
- Cada segmento es una sola palabra, sin separadores internos (`ciclovida`, no `ciclo-vida` ni `cicloVida`).
- Sin verbos — los tópicos representan hechos, no acciones.
- Sin abreviaturas que no sean de uso institucional.

| Segmento | Descripción | Ejemplos |
|---|---|---|
| `dominio` | Bounded context o módulo de negocio productor | `expedientes`, `aportes`, `prestaciones`, `notificaciones` |
| `clasificacion` | Subdivisión dentro del dominio | `ciclovida`, `cuenta`, `pension`, `sms` |
| `descripcion` | El hecho ocurrido, en participio pasado | `presentado`, `actualizada`, `aprobada`, `enviado` |

**Ejemplos válidos:**
```
expedientes.ciclovida.presentado
aportes.cuenta.actualizada
prestaciones.pension.aprobada
notificaciones.sms.enviado
```

**Dead Letter Queue:** todo tópico de negocio tiene un DLQ asociado con el sufijo `.dlq`:
```
expedientes.ciclovida.presentado.dlq
```

### 6.2 Configuración

La creación de tópicos es responsabilidad de **Plataforma**, previa solicitud con el contrato aprobado por Arquitectura. Los parámetros mínimos a definir:

| Parámetro | DEV / QA | Producción | Descripción |
|---|---|---|---|
| `num.partitions` | 3 | Según volumen estimado (mínimo 3) | Grado de paralelismo de consumo |
| `replication.factor` | 1 | 3 | Réplicas para durabilidad ante fallos |
| `retention.ms` | 7 días | Según política del dominio | Tiempo de retención; permite replay |
| `min.insync.replicas` | 1 | 2 | Réplicas mínimas en sincronía para aceptar escrituras |

La retención predeterminada es **7 días**. Valores mayores deben justificarse en el contrato del tópico.

### 6.3 Orden de eventos y clave de partición

Kafka garantiza el orden **solo dentro de una partición**. Si un conjunto de eventos debe procesarse en el orden en que se produjeron (ej. `CuentaCreada`, `DepositoRealizado`, `RetiroRealizado` de la misma cuenta), todos deben publicarse con la **misma clave de partición** — habitualmente el identificador único de la entidad de negocio.

```java
kafkaTemplate.send(topic, entidadId.toString(), evento);
//                         ↑ clave de partición
```

---

## 7. Productores

### 7.1 Dependencias Maven

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

La versión es gestionada por `spring-boot-starter-parent` — no especificar versión explícita.

### 7.2 Configuración obligatoria

```yaml
spring:
  kafka:
    producer:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                              # confirmación de todas las réplicas in-sync
      properties:
        enable.idempotence: true             # exactamente un write por mensaje
        max.in.flight.requests.per.connection: 5
        delivery.timeout.ms: 120000
```

**`acks: all`** es obligatorio. Sin esta configuración el broker puede confirmar recepción antes de que las réplicas hayan persistido el mensaje — hay riesgo de pérdida ante fallo del líder.

**`enable.idempotence: true`** garantiza que los reintentos del productor no generen duplicados en Kafka.

**No se fija `retries`.** Con la idempotencia activa, el productor usa `Integer.MAX_VALUE` como valor por defecto y la ventana real de reintentos la acota `delivery.timeout.ms` (120 s). Fijar `retries: 3` —como hacía la versión anterior— hace que el envío se dé por fallido tras tres intentos aunque queden más de dos minutos de plazo, lo que contradice el propósito de `acks: all`: se acepta el coste de esperar la confirmación de las réplicas y luego se abandona antes de tiempo ante una indisponibilidad transitoria del líder.

### 7.3 Transactional Outbox — publicación garantizada

El productor **nunca publica directamente al broker** desde la lógica de negocio. Usa el patrón **Transactional Outbox**:

```
┌──────────────────────────────────────────────────────────────────┐
│  @Transactional                                                  │
│                                                                  │
│  1. Actualizar estado del agregado en Oracle                     │
│  2. Insertar el evento en EVT_OUTBOX (misma transacción)         │
│                                                                  │
│  COMMIT ──► ambas operaciones confirman o ninguna               │
└──────────────────────────────────────────────────────────────────┘
                         │
                         ▼  proceso separado (Outbox Relay)
             ┌───────────────────────────┐
             │  Lee EVT_OUTBOX           │
             │  Publica el evento        │  ──► Kafka
             │  Marca ESTADO = ENVIADO   │
             └───────────────────────────┘
```

**Por qué es obligatorio:** si el productor publica directamente y el proceso falla entre el `COMMIT` de BD y la publicación al broker, el evento se pierde silenciosamente. Con Outbox, la publicación es eventual pero garantizada.

**Estructura de la tabla `EVT_OUTBOX`:** el DDL canónico, sus convenciones de nomenclatura y su excepción declarada a los campos de auditoría viven en **`LIN-BD-ORA-001 §3.10`**, dueño del modelo de datos. **Este lineamiento no lo reproduce**: mantenerlo en un único lugar evita que ambas copias diverjan. Lo que sí define aquí es el **proceso de relevo** hacia Kafka (más abajo) y el contrato del evento publicado (`§5.2`).

El Outbox Relay puede implementarse como un `@Scheduled` de Spring que consulta registros en estado `PENDIENTE` y los publica al broker. En volúmenes altos, Plataforma puede configurar CDC (Change Data Capture) sobre la tabla como alternativa.

---

## 8. Consumidores

### 8.1 Configuración obligatoria

```yaml
spring:
  kafka:
    consumer:
      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
      group-id: ${spring.application.name}-grp
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false              # confirmación manual obligatoria
      properties:
        spring.json.trusted.packages: "pe.gob.onp.*"
    listener:
      ack-mode: MANUAL
```

**`enable-auto-commit: false`** es obligatorio. El commit automático confirma el offset antes de que el procesamiento termine — si el proceso falla, el mensaje se marca como procesado y se pierde.

### 8.2 Grupo de consumidores

Todas las instancias de un servicio que consuman un tópico deben pertenecer al **mismo grupo de consumidores**. Kafka asigna cada partición a exactamente un miembro del grupo — esto garantiza que cada evento sea procesado una sola vez por el grupo, aunque el servicio tenga múltiples réplicas en K8s.

**Convención de nombre:** `{nombre-servicio}-grp`

```
Servicio: onp-notificaciones
Grupo:    onp-notificaciones-grp
```

### 8.3 Commit manual

```java
@KafkaListener(
    topics = "expedientes.ciclovida.presentado",
    groupId = "${spring.application.name}-grp"
)
public void procesarExpedientePresentado(
        ConsumerRecord<String, EventEnvelope> record,
        Acknowledgment acknowledgment) {

    procesarEvento(record.value());     // si lanza, lo gestiona DefaultErrorHandler (§8.6)
    acknowledgment.acknowledge();       // commit solo si el procesamiento fue exitoso
}
```

> **El listener no captura excepciones.** Deja que se propaguen: el `DefaultErrorHandler` de [§8.6](#86-configuracion-de-dlq-en-spring-kafka) aplica el backoff exponencial, reintenta el mismo registro y, agotados los intentos, lo publica en la DLQ y confirma el offset. Esa es la única implementación soportada; el consumidor no distingue a mano entre error recuperable y no recuperable ni publica en la DLQ por su cuenta.
>
> **Por qué se retiró el ejemplo anterior.** Tenía dos defectos que se copiaban al proyecto. El primero es grave: capturaba el error recuperable, no confirmaba el offset y anotaba *«Kafka reintentará desde el mismo offset»*, lo cual **no ocurre**. Con `ack-mode: MANUAL`, omitir el `acknowledge` no provoca reentrega: el contenedor sigue con el registro siguiente, y en cuanto uno posterior se confirma, el offset avanza por encima del fallido. **El mensaje no se reintenta — se pierde**, y solo reaparecería tras un rebalanceo o reinicio. El segundo defecto era normativo: implementaba a mano un `enviarADlq` que compite con el mecanismo de `§8.6` (`GOB-CHK-001` H28).

### 8.4 Idempotencia

Todo consumidor debe ser **idempotente**: procesar el mismo evento más de una vez debe producir el mismo resultado que procesarlo una sola vez.

| Estrategia | Cuándo usar |
|---|---|
| Verificar estado antes de actuar | Antes de enviar una notificación, verificar que no haya sido enviada |
| Usar `id` como clave de idempotencia | Registrar los `id` (UUID v4) procesados y rechazar duplicados |
| Operaciones naturalmente idempotentes | `UPDATE ... SET estado = 'X' WHERE id = ? AND estado != 'X'` |

### 8.5 Reintentos y Dead Letter Queue

**Errores recuperables** (BD temporalmente no disponible, timeout): no hacer `acknowledge`. Kafka reintentará desde el mismo offset. Usar backoff exponencial para no sobrecargar el sistema de destino.

**Errores no recuperables** (mensaje malformado, regla de negocio violada, error permanente): hacer `acknowledge` para liberar la partición y enviar el mensaje a la **DLQ** del tópico (`{tópico}.dlq`).

Los mensajes en DLQ deben ser monitoreados activamente. Un backlog creciente en DLQ es una alerta operativa de primer nivel — indica un problema en la lógica del consumidor o en el formato del evento.

### 8.6 Configuración de DLQ en Spring Kafka

Spring Kafka provee `DeadLetterPublishingRecoverer` como mecanismo estándar para enviar mensajes fallidos a la DLQ. Se configura como un `@Bean` junto a `DefaultErrorHandler`, que reemplaza al antiguo `SeekToCurrentErrorHandler`.

```java
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Publica en {tópico-original}.dlq conservando la clave de partición
        DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".dlq", -1));
                //                                                         ↑ -1 → Kafka elige la partición

        // Backoff exponencial: 3 intentos, espera inicial 1s, multiplicador 2 (1s → 2s → 4s)
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxAttempts(3);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
```

> **Nota de compatibilidad:** la clase `ExponentialBackOffWithMaxRetries` (usada en versiones anteriores de Spring Framework) fue retirada — `setMaxAttempts(int)` se fusionó directamente en `ExponentialBackOff`. Si el proyecto compila contra Spring Framework 6.x (Spring Boot 3.x), usar `ExponentialBackOff` como en el ejemplo de arriba.

`DefaultErrorHandler` gestiona automáticamente el `acknowledge` tras agotar los reintentos y delega en el `recoverer` — el método `enviarADlq` del ejemplo 8.3 queda cubierto por este bean sin necesidad de implementarlo manualmente.

**Metadatos que se preservan en la DLQ:**

Spring Kafka añade cabeceras al mensaje enviado a la DLQ que permiten trazarlo hasta el origen:

| Cabecera | Contenido |
|---|---|
| `kafka_dlt-original-topic` | Tópico de origen |
| `kafka_dlt-original-partition` | Partición de origen |
| `kafka_dlt-original-offset` | Offset en el tópico de origen |
| `kafka_dlt-exception-message` | Mensaje de la excepción que causó el fallo |
| `kafka_dlt-exception-stacktrace` | Stack trace completo |

### 8.7 Replay desde DLQ

Un mensaje en DLQ está retenido 7 días (retención por defecto). Para reprocesarlo una vez corregido el defecto:

**Opción 1 — Relay manual con herramienta CLI (kafka-console-consumer + producer):**
```bash
# Leer desde la DLQ y publicar de vuelta al tópico principal
kafka-console-consumer \
  --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic expedientes.ciclovida.presentado.dlq \
  --from-beginning \
  --timeout-ms 5000 | \
kafka-console-producer \
  --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --topic expedientes.ciclovida.presentado
```

**Opción 2 — Consumidor de replay dedicado (recomendado para producción):**

Desplegar temporalmente un consumidor con `group-id` distinto que lea la DLQ y reenvíe cada mensaje al tópico principal usando `KafkaTemplate`. Este enfoque permite filtrar mensajes, transformar el payload si fue necesario corregirlo, y registrar trazabilidad de la operación.

```java
@KafkaListener(topics = "expedientes.ciclovida.presentado.dlq",
               groupId = "onp-expedientes-replay-grp")
public void replay(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
    kafkaTemplate.send("expedientes.ciclovida.presentado", record.key(), record.value());
    ack.acknowledge();
    log.info("Mensaje replay offset={} key={}", record.offset(), record.key());
}
```

> El consumidor de replay debe desactivarse tras completar la operación para evitar doble procesamiento continuo.

---

## 9. Transacciones distribuidas — Saga y Outbox

Cuando un flujo de negocio abarca múltiples microservicios, cada uno con su propia base de datos, no se pueden usar transacciones ACID. El patrón **Saga** coordina el flujo garantizando la consistencia eventual mediante operaciones de compensación.

### 9.1 Saga por coreografía

Cada servicio emite un evento al completar su paso; el siguiente servicio lo consume y reacciona. No hay coordinador central.

```
Servicio A                    Kafka                   Servicio B
    │                           │                          │
    │── AporteRegistrado ──►    │                          │
    │                           │── AporteRegistrado ──►  │
    │                           │                          │ procesa
    │                           │  ◄── CuentaActualizada ──│
    │  ◄── CuentaActualizada ───│                          │
```

**Cuándo usar:** flujos con pocos pasos y baja complejidad condicional.

**Compensación:** si un paso falla, el servicio emite un evento de compensación que revierte los pasos anteriores. Las compensaciones deben estar documentadas en el ADR del flujo.

### 9.2 Saga por orquestación

Un orquestador central emite comandos y reacciona a eventos de respuesta. El flujo es más trazable, pero introduce un coordinador con estado propio.

```
Orquestador
    │
    ├── Comando: RegistrarAporte ──► Servicio A
    │   ◄── AporteRegistrado ─────── Servicio A
    │
    ├── Comando: ActualizarCuenta ──► Servicio B
    │   ◄── CuentaActualizada ─────── Servicio B
    │
    └── Saga completada
```

**Cuándo usar:** flujos con muchos pasos, condiciones complejas o múltiples rutas de compensación donde la coreografía se vuelve difícil de seguir.

### 9.3 Reglas ONP para Saga

- Todo flujo Saga debe estar documentado como ADR con el diagrama de pasos y las compensaciones explícitas.
- Todo paso de Saga que persista datos usa **Transactional Outbox** (sección 7.3) para garantizar que el evento se publica solo si la transacción local confirma.
- Las compensaciones deben ser idempotentes — pueden ejecutarse más de una vez ante reintentos.

### 9.4 Variante: Saga por orquestación sobre aplicativos monolíticos

Esta sección documenta la implementación Kafka de la variante Saga definida en **`LIN-ARQ-001` §3.3.1**. Aplica cuando los participantes son aplicativos monolíticos que exponen servicios REST — no microservicios puros.

El patrón completo (diagrama, roles, tabla de estado `SAGA_INSTANCIA`, garantías de cada participante) está en **`LIN-ARQ-001` §3.3.1**. Esta sección cubre únicamente la parte Kafka: convención de tópicos y envelope.

#### Nomenclatura de tópicos Saga

Los tópicos Saga son distintos a los tópicos de dominio (§6.1). Su convención es:

```
onp.saga.{flujo}.{paso}.comando
onp.saga.{flujo}.{paso}.respuesta
onp.saga.{flujo}.{paso}.compensacion
```

Ejemplos para un flujo de pensión completa:
```
onp.saga.pension-completa.paso1.comando
onp.saga.pension-completa.paso1.respuesta
onp.saga.pension-completa.paso1.compensacion
onp.saga.pension-completa.paso2.comando
```

> Los tópicos Saga no siguen la convención `{dominio}.{clasificacion}.{descripcion}` de §6.1 porque no son eventos de dominio — son mensajes de coordinación de flujo.

#### Envelope CloudEvents extendido para Saga

El envelope base CloudEvents v1.0 (§5.2) se extiende con atributos de contexto Saga. El comando que el orquestador publica:

```json
{
  "specversion":     "1.0",
  "id":              "UUID v4 del mensaje",
  "source":          "/onp/orquestador-saga",
  "type":            "pe.gob.onp.saga.pension-completa.paso1.comando",
  "time":            "2026-06-08T10:30:00Z",
  "datacontenttype": "application/json",
  "traceparent":     "00-{traceId}-{spanId}-01",
  "sagaid":          "UUID del flujo completo",
  "sagapaso":        "1",
  "sagaflujo":       "pension-completa",
  "data": {
    "operacion":  "REGISTRAR_APORTE",
    "parametros": { "cuentaId": "CTA-123", "monto": 500.00 }
  }
}
```

La respuesta que el monolito participante publica:

```json
{
  "specversion":  "1.0",
  "id":           "UUID v4 nuevo",
  "source":       "/onp/monolito-aportes",
  "type":         "pe.gob.onp.saga.pension-completa.paso1.respuesta",
  "time":         "2026-06-08T10:30:01Z",
  "datacontenttype": "application/json",
  "traceparent":  "00-{mismo traceId}-{nuevo spanId}-01",
  "sagaid":       "mismo UUID del comando",
  "sagapaso":     "1",
  "sagaflujo":    "pension-completa",
  "data": {
    "estado":      "OK",
    "operacionId": "APO-2026-00123"
  }
}
```

| Atributo de extensión | Tipo | Descripción |
|---|---|---|
| `sagaid` | UUID | Identificador del flujo Saga completo — mismo valor en todos los pasos |
| `sagapaso` | String | Número de paso dentro del flujo (`"1"`, `"2"`, ...) |
| `sagaflujo` | String | Nombre del flujo (`"pension-completa"`, `"afiliacion"`) |

#### Reglas adicionales para tópicos Saga

- Los tópicos Saga los crea **Plataforma** igual que los tópicos de dominio — no se crean ad hoc.
- El `sagaId` debe usarse como **clave de partición** para garantizar orden de mensajes dentro del mismo flujo.
- Si un paso falla y el mensaje va a DLQ, el orquestador inicia compensación — no espera el replay del DLQ.
- Cada flujo Saga debe documentarse como **ADR** con el diagrama de pasos y las compensaciones explícitas (ver `LIN-ARQ-001 §3.3`).

---

## 10. Seguridad

### 10.1 Autenticación al broker

La comunicación con Kafka usa **SASL/SCRAM-SHA-512** con **TLS** en todos los ambientes. No se permite conexión sin autenticación ni en texto plano.

```yaml
spring:
  kafka:
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: SCRAM-SHA-512
      sasl.jaas.config: >
        org.apache.kafka.common.security.scram.ScramLoginModule required
        username="${KAFKA_USERNAME}"
        password="${KAFKA_PASSWORD}";
      ssl.truststore.location: ${KAFKA_TRUSTSTORE_PATH}
      ssl.truststore.password: ${KAFKA_TRUSTSTORE_PASSWORD}
```

Las credenciales se gestionan como **Kubernetes Secrets** y se inyectan vía variables de entorno. Nunca en código fuente ni en archivos de configuración versionados en el repositorio.

### 10.2 Autorización por tópico

Cada servicio tiene permisos explícitos sobre los tópicos que produce y consume. Plataforma gestiona las ACLs en Kafka:

| Permiso | Quién lo tiene |
|---|---|
| `WRITE` sobre un tópico | Solo el servicio productor declarado en el contrato |
| `READ` sobre un tópico | Solo los servicios consumidores declarados en el contrato |
| Administración de tópicos (crear, modificar, eliminar) | Solo Plataforma |

Cualquier cambio en los permisos de un tópico requiere solicitud a Plataforma con el contrato actualizado y aprobado por Arquitectura OTI.

### 10.3 No PII en eventos

Los eventos no deben contener datos personales sensibles en el payload. Ver política No PII en LIN-OBS-001. Los consumidores que necesiten datos personales los obtienen mediante consulta REST al servicio origen.

---

## 11. Observabilidad

### 11.1 Propagación de trazas en flujos asíncronos

El productor incluye el campo `traceparent` en el envelope del evento (sección 5.2) con el valor W3C TraceContext del span activo (`00-{traceId}-{spanId}-{flags}`). El consumidor lo extrae e inyecta en el contexto de traza de Micrometer antes de procesar el evento, de modo que el span del consumidor aparezca como hijo del span del productor en Jaeger. Micrometer/OTEL entiende el formato `traceparent` de forma nativa — no se requiere transformación manual.

El flujo completo `Productor → Kafka → Consumidor` debe ser visible como una traza continua en Jaeger antes de pasar a producción.

### 11.2 Logs de eventos

Cada publicación y consumo de evento debe registrar un log estructurado. Los campos mínimos son los definidos en LIN-OBS-001 más los campos de contexto del evento:

```json
{
  "level":           "INFO",
  "message":         "evento publicado",
  "service.name":    "onp-expedientes",
  "trace.id":        "abc123def456abc123def456abc12345",
  "span.id":         "00f067aa0ba902b7",
  "event.id":        "550e8400-e29b-41d4-a716-446655440000",
  "event.type":      "pe.gob.onp.expedientes.ciclovida.presentado",
  "event.dataschema": "/onp/schemas/expedientes/ciclovida/presentado/1.0"
}
```

### 11.3 Métricas del broker

Plataforma configura la exportación de métricas Kafka a Prometheus. Los siguientes indicadores se monitorean en Grafana con alertas definidas:

| Métrica | Alerta |
|---|---|
| Consumer lag por grupo y tópico | Lag creciente por encima del umbral configurado por tópico |
| Tasa de errores de producción (`record-error-rate`) | Errores > 0 en ventana de 5 minutos |
| Mensajes en DLQ | Backlog creciente en cualquier DLQ |
| Disponibilidad del broker (brokers activos vs. esperados) | Broker caído |

El equipo de desarrollo es responsable de definir los umbrales de lag aceptables para cada tópico en el contrato del evento.

---

## 12. Despliegue en K8s

### 12.1 Kafka en K8s

ONP opera Kafka en K8s mediante el operador **Strimzi**. La gestión del clúster es responsabilidad exclusiva de Plataforma. Los equipos de desarrollo no acceden directamente a los nodos Kafka.

```
Namespace: kafka-{env}
Operador:  Strimzi Kafka Operator
Clúster:   kafka-onp
Modo:      KRaft (sin ZooKeeper, Kafka 3.x)
```

| Componente | Namespace DEV | Namespace QA | Namespace PROD |
|---|---|---|---|
| Kafka Cluster | `kafka-dev` | `kafka-qa` | `kafka-prod` |
| Schema Registry (futuro) | `kafka-dev` | `kafka-qa` | `kafka-prod` |

> **Sufijo de ambiente — verificación pendiente con Plataforma.** `LIN-K8S-001 §4.4` norma que el ambiente **no** forma parte del nombre del namespace de aplicación, porque ya está implícito en el clúster. Estos namespaces de infraestructura conservan el sufijo, y ese mismo lineamiento pide a Plataforma confirmar si Kafka vive en un clúster de plataforma compartido —en cuyo caso el sufijo está justificado y se mantiene— o en el clúster de aplicaciones de cada ambiente, en cuyo caso corresponde normalizar a `kafka`. La decisión no afecta a los servicios consumidores, que resuelven por la variable `KAFKA_BOOTSTRAP_SERVERS`.

### 12.2 Configuración de servicios productores y consumidores

Los servicios que producen o consumen eventos se despliegan como cualquier servicio en K8s (ver LIN-K8S-001). Las credenciales Kafka se inyectan como Kubernetes Secrets:

```yaml
env:
  - name: KAFKA_BOOTSTRAP_SERVERS
    value: kafka-onp-kafka-bootstrap.kafka-prod.svc.cluster.local:9093
  - name: KAFKA_USERNAME
    valueFrom:
      secretKeyRef:
        name: kafka-credentials-{servicio}
        key: username
  - name: KAFKA_PASSWORD
    valueFrom:
      secretKeyRef:
        name: kafka-credentials-{servicio}
        key: password
  - name: KAFKA_TRUSTSTORE_PATH
    value: /etc/kafka/certs/truststore.jks
  - name: KAFKA_TRUSTSTORE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: kafka-tls-{servicio}
        key: truststore-password
```

---

## 13. Gobierno del bus

### 13.1 Proceso de incorporación

Para usar el bus de eventos en un nuevo sistema o flujo, el equipo debe completar los siguientes pasos en orden:

1. **ADR aprobado por Arquitectura OTI** que justifique el uso de EDA para el caso de uso concreto.
2. **Revisar el catálogo institucional** (Apéndice B) para verificar que no exista un evento equivalente de otro sistema.
3. **Elaborar el contrato de eventos** (Apéndice A) para cada evento que el servicio produce o consume.
4. **Aprobación del contrato** por Arquitectura OTI.
5. **Solicitud a Plataforma** para creación del tópico con los parámetros del contrato.
6. **Validación de ACLs** por Seguridad OTI.
7. **Registro en el catálogo** una vez el tópico esté creado en producción.

### 13.2 Catálogo de eventos

El catálogo institucional registra todos los tópicos activos en el bus. Es la fuente de verdad para evitar duplicidad y facilitar la integración entre sistemas. Su mantenimiento es responsabilidad de Arquitectura OTI con contribución de los equipos productores.

Revisar el catálogo **antes** de solicitar un nuevo tópico — puede existir un evento equivalente producido por otro sistema que ya cubra la necesidad.

### 13.3 Deprecación de tópicos

- Un tópico no se elimina mientras tenga consumidores activos.
- La deprecación requiere notificación formal a todos los consumidores conocidos con plazo mínimo de **30 días hábiles**.
- La migración a una nueva versión del contrato sigue las reglas de versionado de la sección 5.5.
- El tópico deprecado pasa a estado `Deprecado` en el catálogo con la fecha efectiva de cierre.

---

## 14. Responsabilidades

| Responsabilidad | Arquitectura OTI | Desarrollo | Plataforma | Seguridad OTI |
|---|---|---|---|---|
| Aprobar uso de EDA (ADR) | ✓ | | | |
| Revisar y aprobar contrato de eventos | ✓ | | | |
| Diseñar contrato de eventos | | ✓ | | |
| Registrar eventos en el catálogo | ✓ | ✓ | | |
| Crear y configurar tópicos | | | ✓ | |
| Gestionar ACLs de Kafka | | | ✓ | ✓ |
| Gestionar credenciales (K8s Secrets) | | | ✓ | |
| Implementar productor con Outbox | | ✓ | | |
| Implementar consumidor idempotente | | ✓ | | |
| Operar y mantener clúster Kafka | | | ✓ | |
| Monitorear lag, DLQ y disponibilidad del broker | | ✓ (umbral por tópico) | ✓ (infraestructura) | |
| Auditar permisos y accesos | | | | ✓ |

---

## 15. Checklist de conformidad

### Diseño y gobierno
- [ ] ADR aprobado por Arquitectura OTI para el uso de EDA
- [ ] Catálogo revisado — no existe evento equivalente previo
- [ ] Contrato de eventos completo y aprobado para cada evento producido/consumido
- [ ] Nombre del tópico sigue la nomenclatura `dominio.clasificacion.descripcion`
- [ ] DLQ definido para cada tópico de negocio
- [ ] Particiones, retención y clave de partición estimadas y documentadas en el contrato
- [ ] Tópico registrado en el catálogo institucional

### Implementación — Productor
- [ ] `acks: all` configurado
- [ ] `enable.idempotence: true` configurado
- [ ] Patrón Transactional Outbox implementado — no hay publicación directa al broker desde lógica de negocio
- [ ] Tabla `EVT_OUTBOX` creada con la estructura mínima de la sección 7.3
- [ ] Campo `traceparent` (W3C TraceContext) incluido en el envelope de cada evento

### Implementación — Consumidor
- [ ] `enable-auto-commit: false` configurado
- [ ] Commit manual implementado — solo confirma tras procesamiento exitoso
- [ ] Lógica de idempotencia implementada
- [ ] Manejo diferenciado de errores recuperables (no acknowledge) y no recuperables (DLQ)
- [ ] Grupo de consumidores nombrado según la convención `{servicio}-grp`

### Seguridad y operabilidad
- [ ] Credenciales Kafka gestionadas como K8s Secrets — no en código ni en archivos versionados
- [ ] SASL/SCRAM-SHA-512 + TLS configurado
- [ ] No PII en el payload del evento verificado
- [ ] Traza productor → consumidor visible en Jaeger antes del pase a producción
- [ ] Umbrales de lag definidos en el contrato y alertas configuradas en Grafana
- [ ] Proceso de atención de DLQ definido (¿reintento manual?, ¿descarte con log?)

---

## 16. Anti-patrones

| Anti-patrón | Problema | Solución |
|---|---|---|
| Publicar directamente al broker sin Outbox | Si el proceso falla entre el `COMMIT` de BD y la publicación, el evento se pierde silenciosamente | Transactional Outbox (sección 7.3) |
| `enable-auto-commit: true` | El offset se confirma antes de que el procesamiento termine — pérdida de mensajes ante fallo | `enable-auto-commit: false` + commit manual (sección 8.3) |
| Consumidor no idempotente | Ante reintento o replay, el evento se procesa dos veces con efectos duplicados | Diseñar idempotencia desde el inicio (sección 8.4) |
| Usar el bus como canal de comandos RPC | Introduce latencia y complejidad para comunicación que requiere respuesta inmediata | REST sincrónico — LIN-API-REST-001 |
| Eventos grandes con el modelo completo del dominio | Acoplamiento de datos entre productor y consumidor | Eventos pequeños con identificadores; el consumidor consulta lo que necesita |
| Múltiples servicios en el mismo grupo de consumidores | Kafka distribuye mensajes entre miembros del grupo — un servicio deja de recibir eventos que le corresponden | Un grupo por servicio consumidor |
| Tópicos ad hoc sin contrato ni catálogo | Proliferación de tópicos, duplicidad, opacidad del ecosistema EDA | Todo tópico requiere contrato aprobado y registro en catálogo antes de producción |
| Credenciales Kafka en `application.yml` versionado | Exposición de credenciales en el repositorio de código | K8s Secrets inyectados como variables de entorno (sección 10.1) |
| Ignorar el DLQ | Mensajes fallidos sin atención — pérdida silenciosa de eventos de negocio | Monitorear DLQ con alerta en Grafana; definir proceso de atención |
| No propagar el `traceparent` en el envelope | El flujo asíncrono queda invisible en Jaeger — diagnóstico imposible en producción | Incluir `traceparent` (W3C TraceContext) en el envelope (sección 5.2) y extraerlo en el consumidor (sección 11.1) |

---

## 17. Proceso de excepción (ADR)

Toda desviación a las normas de este lineamiento requiere un ADR aprobado por Arquitectura OTI. El ADR debe documentar:

1. La norma de la que se desvía y la razón técnica concreta.
2. Los riesgos de la desviación y cómo se mitigan.
3. El plazo de convergencia al estándar.
4. El responsable técnico y funcional.

Los ADR de excepción se registran en el repositorio del proyecto bajo `docs/adr/` y se notifican a Arquitectura OTI para su registro central.

---

## 18. Glosario

| Término | Definición en el contexto ONP |
|---|---|
| **Broker** | Servidor que recibe, almacena y distribuye los eventos. En ONP: Apache Kafka |
| **Tópico** | Canal lógico en Kafka al que se publican eventos de un mismo tipo |
| **Partición** | Subdivisión de un tópico. Kafka garantiza el orden dentro de una partición |
| **Réplica** | Copia de una partición en otro broker. Garantiza durabilidad ante fallos |
| **Productor** | Servicio que publica eventos al broker |
| **Consumidor** | Servicio que lee y procesa eventos del broker |
| **Grupo de consumidores** | Conjunto de instancias que cooperan para procesar los eventos de un tópico; Kafka asigna cada partición a un solo miembro del grupo |
| **Consumer lag** | Diferencia entre el último offset publicado y el último offset confirmado por el consumidor |
| **Offset** | Posición de un mensaje dentro de una partición de Kafka |
| **DLQ (Dead Letter Queue)** | Tópico especial donde se envían los mensajes que no pudieron procesarse correctamente |
| **Transactional Outbox** | Patrón que garantiza que un evento se publica al broker solo si la transacción de BD confirma |
| **Saga** | Patrón de coordinación de transacciones distribuidas mediante eventos y compensaciones |
| **Idempotencia** | Propiedad de una operación que produce el mismo resultado si se ejecuta una o más veces |
| **SASL/SCRAM** | Protocolo de autenticación segura para Kafka |
| **ACL** | Access Control List — permisos de lectura/escritura por tópico y usuario en Kafka |
| **KRaft** | Modo de Kafka sin dependencia de ZooKeeper (Kafka 3.x en adelante) |
| **Strimzi** | Operador Kubernetes para desplegar y gestionar clústeres Apache Kafka |
| **EDA** | Event-Driven Architecture — estilo arquitectónico donde los componentes se comunican mediante eventos |
| **CDC** | Change Data Capture — mecanismo de captura de cambios en BD, alternativa al polling del Outbox Relay |

---

## Apéndice A — Contrato de eventos (plantilla)

El contrato de eventos documenta todos los eventos que un sistema produce y consume a través del bus. Debe completarse y ser aprobado por Arquitectura OTI antes de solicitar la creación de un tópico. Actualizar ante cualquier cambio de contrato.

---

**Sistema:** [Nombre del sistema]  
**Código del contrato:** [LIN-BUS-CONTRATO-{SISTEMA}-v{VERSION}]  
**Versión:** [x.y]  
**Fecha:** [YYYY-MM-DD]  
**Elaborado por:** [Equipo de desarrollo]  
**Aprobado por:** [Arquitectura OTI]  
**Descripción:** [Propósito del sistema en el contexto del bus de eventos]  

---

### Servidores

| # | Nombre | Tipo | Versión | Host | Puerto | Autenticación |
|---|---|---|---|---|---|---|
| 1 | kafka-onp | Kafka | 3.x | kafka-onp-kafka-bootstrap.kafka-{env}.svc.cluster.local | 9093 | SASL/SCRAM-SHA-512 + TLS |

### Tópicos del sistema

| # | Tópico | Descripción | Particiones | Réplicas | Retención | Clave de partición | DLQ |
|---|---|---|---|---|---|---|---|
| 1 | | | | | | | Sí |

### Estructura del evento

**Tópico:** `{dominio}.{clasificacion}.{descripcion}`  
**Versión del contrato:** `1.0`  
**Tamaño máximo estimado:** [N] KB  

```json
{
  "specversion":     "1.0",
  "id":              "string (UUID v4)",
  "source":          "/onp/{servicio}",
  "type":            "pe.gob.onp.{dominio}.{clasificacion}.{descripcion}",
  "time":            "string (ISO 8601 UTC)",
  "datacontenttype": "application/json",
  "dataschema":      "/onp/schemas/{dominio}/{clasificacion}/{descripcion}/1.0",
  "traceparent":     "string (W3C TraceContext: 00-{traceId32hex}-{spanId16hex}-01)",
  "data": {
    "id":     "string — identificador interno de la entidad",
    "campo2": "tipo — descripción del campo"
  }
}
```

| Atributo del payload | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | String | Sí | Identificador interno de la entidad |

### Operaciones de envío (produce)

| # | Tópico | Versión | Condición de publicación | Clave de partición |
|---|---|---|---|---|
| 1 | | 1.0 | | |

### Operaciones de recepción (consume)

| # | Tópico | Versión | Grupo de consumidores | Acción al recibir |
|---|---|---|---|---|
| 1 | | 1.0 | {servicio}-grp | |

### Umbrales de observabilidad

| Indicador | Umbral de alerta |
|---|---|
| Consumer lag máximo aceptable | [N] mensajes |
| Latencia máxima productor → consumidor | [N] segundos |

---

## Apéndice B — Catálogo de eventos (estructura)

El catálogo institucional de eventos es mantenido por Arquitectura OTI. Registra todos los tópicos activos en el bus, los sistemas productores y los consumidores conocidos.

**Ubicación:** repositorio central de arquitectura — `docs/catalogo-eventos/catalogo.md`

**Propósito:** evitar duplicidad de tópicos, facilitar la integración entre sistemas y mantener visibilidad del ecosistema EDA de la ONP.

### Campos de cada entrada en el catálogo

| Campo | Descripción |
|---|---|
| **Tópico** | Nombre completo |
| **Versión activa** | Versión del contrato vigente |
| **Sistema productor** | Servicio que publica el evento |
| **Sistemas consumidores** | Servicios que leen el tópico |
| **Estado** | Activo / En prueba / Deprecado |
| **Fecha de creación en producción** | |
| **Fecha de deprecación** | Si aplica |
| **ADR asociado** | Referencia al ADR que aprobó el uso del tópico |
| **Contrato** | Ruta al documento de contrato |

### Ejemplo de entrada

| Campo | Valor |
|---|---|
| Tópico | `expedientes.ciclovida.presentado` |
| Versión activa | `1.0` |
| Sistema productor | `onp-expedientes` |
| Sistemas consumidores | `onp-notificaciones`, `onp-prestaciones` |
| Estado | Activo |
| Fecha de creación | 2026-06-05 |
| ADR asociado | ADR-013 |
| Contrato | `docs/contratos/expedientes-ciclovida-presentado-v1.0.md` |
