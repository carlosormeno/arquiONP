# Brecha del Framework de Arquitectura ONP

**Fecha:** 2026-07-06
**Responsable:** Arquitectura OTI
**Estado:** En revisión

Este documento registra los patrones, estilos, principios y decisiones arquitectónicas identificados como necesarios para el framework ONP pero que aún no están documentados formalmente en ningún lineamiento vigente. Sirve como lista de trabajo para ir cerrando las brechas punto por punto.

**Convención de estado:**
- `❌ Pendiente` — no existe ninguna documentación
- `⚠️ Parcial` — mencionado o implícito en algún lineamiento pero sin definición formal
- `✅ Documentado` — cubierto formalmente en un lineamiento vigente

---

## 1. Estilos Arquitectónicos
*Cómo se organiza internamente un sistema*

| # | Estilo | Estado | Documento destino | Notas |
|---|---|---|---|---|
| E01 | Arquitectura en Capas (Layered) | ✅ Documentado | LIN-ARQ-000 §3.1 | |
| E02 | Arquitectura Hexagonal (Ports & Adapters) | ✅ Documentado | LIN-ARQ-000 §3.2 | |
| E03 | Monolito Puro | ✅ Documentado | LIN-ARQ-000 §3.3 | |
| E04 | Monolito Modular | ✅ Documentado | LIN-ARQ-000 §3.4 | Destino por defecto ONP |
| E05 | Microservicios | ✅ Documentado | LIN-ARQ-000 §3.5 | Con criterios de extracción |
| E06 | Arquitectura Orientada a Eventos (EDA) | ✅ Documentado | LIN-ARQ-000 §3.6 | |
| E07 | Arquitectura Orientada a Servicios (SOA) | ✅ Documentado | LIN-ARQ-000 §3.8.4 | Modelo mandatorio para interoperabilidad B2G/G2G (PIDE, RENIEC, SUNAT) con resiliencia y ACL obligatorios |
| E08 | Serverless | ❌ Pendiente | LIN-ARQ-000 | Evaluar si aplica a largo plazo en ONP |
| E09 | Pipe and Filter | ❌ Pendiente | LIN-ARQ-000 | Revisar si aplica para pipelines de datos |

---

## 2. Patrones de Arquitectura
*Soluciones recurrentes a problemas de estructura a nivel de sistema*

| # | Patrón | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PA01 | Strangler Fig | ✅ Documentado | LIN-ARQ-000 §2.2 | Migración de legacy |
| PA02 | Anti-Corruption Layer (ACL) | ✅ Documentado | LIN-ARQ-000 §3.5 | Obligatorio con RENIEC, SUNAT, PIDE |
| PA03 | Saga — coreografía | ✅ Documentado | LIN-ARQ-000 §3.6 + LIN-BUS-001 §9.1 | |
| PA04 | Saga — orquestación | ✅ Documentado | LIN-ARQ-000 §3.6 + LIN-BUS-001 §9.2 | |
| PA05 | Saga sobre monolitos (Kafka + REST) | ✅ Documentado | LIN-ARQ-000 §3.6 + LIN-BUS-001 §9.4 | Variante ONP — parque actual |
| PA06 | Transactional Outbox | ✅ Documentado | LIN-ARQ-000 §3.6 + LIN-BUS-001 §7.3 | |
| PA07 | CQRS (Event-Driven) | ✅ Documentado | LIN-ARQ-000 §3.10 | Dos variantes: Outbox+Kafka (estándar) y CDC+Kafka (agnóstico al motor). Adopción mediante ADR obligatorio. |
| PA08 | API Gateway / API Manager | ✅ Documentado | LIN-API-REST-001 §2.5 | WSO2 — estado PoC (ADR-WSO2-001) |
| PA09 | BFF (Backend for Frontend) | ✅ Documentado | LIN-ARQ-000 §3.8.3 | Patrón oficial PT09 para adaptación de canales y mediación SSO frente a WSO2. |
| PA10 | Facade (arquitectura) | ✅ Documentado | LIN-ARQ-000 §3.8.1 | Patrón oficial PT12 para ocultar complejidad externa/legada en Monolito Modular. |
| PA11 | Gateway-Aggregation | ✅ Documentado | LIN-ARQ-000 §3.8.2 | Patrón oficial PT10 intra-JVM para Monolito Modular y vía red en Microservicios. |
| PA12 | Sidecar | ❌ Pendiente | LIN-K8S-001 | Relevante para observabilidad y mTLS en K8s |
| PA13 | Ambassador | ❌ Pendiente | LIN-K8S-001 | Proxy de salida en K8s |
| PA14 | Feature Toggle (Strangler complemento) | ✅ Documentado | LIN-ARQ-000 §2.2.1 | Normado en 4 variantes para Trunk-Based Development, Kill-Switch y Branch by Abstraction |

---

## 3. Patrones de Integración
*Cómo se comunican los sistemas entre sí*

| # | Patrón | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PI01 | REST sincrónico | ✅ Documentado | LIN-API-REST-001 | |
| PI02 | Mensajería asíncrona (Kafka) | ✅ Documentado | LIN-BUS-001 | |
| PI03 | CloudEvents v1.0 | ✅ Documentado | LIN-BUS-001 §5.2 + ADR-CLOUDEVENTS-001 | |
| PI04 | Dead Letter Queue (DLQ) | ✅ Documentado | LIN-BUS-001 §8.5–8.7 | |
| PI05 | Idempotent Consumer | ✅ Documentado | LIN-BUS-001 §8.4 | |
| PI06 | Circuit Breaker | ✅ Documentado | LIN-ARQ-000 §3.7.3 | Estándar y condicionales (Estadio 3 Microservicios o excepción en Monolito Modular) |
| PI07 | Retry con backoff exponencial | ✅ Documentado | LIN-ARQ-000 §3.7.2 + LIN-BUS-001 §8.5 | Cubierto para REST y Kafka |
| PI08 | Timeout | ✅ Documentado | LIN-ARQ-000 §3.7.1 | Matriz de timeouts por criticidad y demanda |
| PI09 | Bulkhead | ✅ Documentado | LIN-ARQ-000 §3.7.2 | Aislamiento por pools de conexión HTTP/JDBC |
| PI10 | Rate Limiting (patrón de diseño) | ⚠️ Parcial | LIN-API-REST-001 | Cubierto en WSO2 Gateway, falta para consumo interno |
| PI11 | Competing Consumers | ❌ Pendiente | LIN-BUS-001 | Múltiples instancias consumiendo el mismo tópico |
| PI12 | Content-Based Router | ❌ Pendiente | LIN-BUS-001 | Enrutamiento de mensajes según contenido |
| PI13 | Correlación de trazas productor→consumidor | ✅ Documentado | LIN-BUS-001 §11.1 + LIN-OBS-001 | Via `traceparent` W3C |

---

## 4. Patrones de Dominio (DDD)
*Para modelar lógica de negocio compleja*

| # | Patrón | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PD01 | Aggregate | ✅ Documentado | LIN-ARQ-000 §6.4.1 | Guía normativa en contexto Spring: transacciones y eventos en Agregado Raíz |
| PD02 | Entity / Value Object | ✅ Documentado | LIN-ARQ-000 §6.4.1 | Diferenciación estricta con `@Entity` JPA; records autovalidados en Java 21 |
| PD03 | Domain Event | ✅ Documentado | LIN-ARQ-000 + LIN-BUS-001 | |
| PD04 | Repository | ✅ Documentado | LIN-DEV-JAVA-001 §13.5.3 | Puerto en capa de dominio + adaptador `JpaRepository`/`JdbcRepository` en infraestructura; traducción de excepciones Oracle a jerarquía limpia |
| PD05 | Domain Service | ✅ Documentado | LIN-DEV-JAVA-001 §13.5.2 | POJO puro sin anotaciones de Spring/JPA; registro obligatorio vía `@Configuration`/`@Bean` para preservar la pureza hexagonal |
| PD06 | Application Service | ✅ Documentado | LIN-DEV-JAVA-001 §13.5.1 | Orquestador transaccional (`@Service` + `@Transactional`); delega en el Domain Service y publica eventos de dominio |
| PD07 | Bounded Context | ✅ Documentado | LIN-ARQ-000 | |
| PD08 | Context Map | ✅ Documentado | LIN-ARQ-000 §3.9.1 | Contrato y relaciones entre Bounded Contexts en Monolito Modular |
| PD09 | Shared Kernel | ✅ Documentado | LIN-ARQ-000 §3.9.2 | Estándar y tabla de elementos permitidos/prohibidos en módulo común |
| PD10 | Published Language | ✅ Documentado | LIN-ARQ-000 §3.9.3 + LIN-BUS-001 §5.2 | Contratos CloudEvents v1.0 / Schema Registry (asíncrono) y OpenAPI 3.0 (síncrono) |

---

## 5. Patrones de Diseño (GoF)
*Soluciones a problemas recurrentes a nivel de código*

| # | Categoría | Patrones | Estado | Documento destino | Notas |
|---|---|---|---|---|---|
| PG01 | Creacionales | Factory Method, Abstract Factory, Builder, Singleton, Prototype | ⚠️ Parcial | LIN-ARQ-000 §8.2 | Factory Method, Builder y Singleton cubiertos con ficha (§8.2.1–8.2.3). Abstract Factory y Prototype sin ficha — baja prioridad, poco uso en el estilo ONP |
| PG02 | Estructurales | Adapter, Facade, Decorator, Proxy, Composite, Bridge, Flyweight | ⚠️ Parcial | LIN-ARQ-000 §8.1 | Adapter, Decorator y Facade cubiertos con ficha (§8.1.1–8.1.3). Proxy, Composite, Bridge y Flyweight sin ficha — evaluar si el framework los necesita. Nota: el Facade aquí es el patrón GoF de nivel de código — el Facade de nivel de arquitectura está en PA10. |
| PG03 | Comportamiento | Strategy, Observer, Command, Template Method, Chain of Responsibility, State, Iterator, Mediator | ⚠️ Parcial | LIN-ARQ-000 §8.3 | Strategy, Observer, Command y State cubiertos con ficha (§8.3.1–8.3.4) — los más usados en Spring. Template Method, Chain of Responsibility, Iterator y Mediator sin ficha |

---

## 6. Principios de Diseño
*Guías para escribir buen código*

| # | Principio | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PR01 | SOLID — Single Responsibility | ✅ Documentado | LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 §7.1 | Declaración arquitectónica en LIN-ARQ-000 §7.1; aplicación práctica en Spring y anti-patrón God Object en LIN-DEV-JAVA-001 §7.1 (renumerado desde §10.4.1 — corrección 2026-07-10). |
| PR02 | SOLID — Open/Closed | ✅ Documentado | LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 §7.2 | Ídem PR01 — ejemplo de patrón Estrategia inyectado por Spring para evitar condicionales en cadena |
| PR03 | SOLID — Liskov Substitution | ✅ Documentado | LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 §7.3 | Ídem PR01 |
| PR04 | SOLID — Interface Segregation | ✅ Documentado | LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 §7.4 | Ídem PR01 |
| PR05 | SOLID — Dependency Inversion | ✅ Documentado | LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 §7.5, §13.5 | Declaración en LIN-ARQ-000 §7.1; guía de inyección por constructor y puertos de dominio en LIN-DEV-JAVA-001 §7.5 y §13.5. |
| PR06 | DRY (Don't Repeat Yourself) | ✅ Documentado | LIN-ARQ-000 §7.2 + LIN-DEV-JAVA-001 §12.4.2 | Declarado en LIN-ARQ-000 §7.2; criterio ONP de duplicación de conocimiento de negocio vs. acoplamiento entre módulos en LIN-DEV-JAVA-001 §12.4.2. |
| PR07 | KISS (Keep It Simple) | ✅ Documentado | LIN-ARQ-000 §7.2 + LIN-DEV-JAVA-001 §12.4.3 | Ídem PR06 — uso de Records/Sealed Classes de Java 21 y prohibición de sobre-ingeniería |
| PR08 | YAGNI (You Ain't Gonna Need It) | ✅ Documentado | LIN-ARQ-000 §7.2 + LIN-DEV-JAVA-001 §12.4.4 | Ídem PR06 — prohibición de abstracciones especulativas sin requerimiento formal |
| PR09 | Separation of Concerns | ✅ Documentado | LIN-ARQ-000 §3.11 + §7.2 | Declarado formalmente en §3.11 y referenciado en §7.2 con ejemplos de violación |
| PR10 | Law of Demeter | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR11 | Tell Don't Ask | ❌ Pendiente | LIN-DEV-JAVA-001 | |

---

## 7. Principios Arquitectónicos
*Restricciones y decisiones de alto nivel que gobiernan el sistema*

| # | Principio | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PRA01 | Teorema CAP (CP vs AP) | ✅ Documentado | LIN-ARQ-000 §3.5 | Obligatorio en extracción de microservicios |
| PRA02 | Consistencia eventual | ✅ Documentado | LIN-ARQ-000 §3.6 + LIN-BUS-001 | |
| PRA03 | Idempotencia | ✅ Documentado | LIN-BUS-001 §8.4 | |
| PRA04 | Observabilidad como requisito (4 pilares) | ✅ Documentado | LIN-ARQ-000 + LIN-OBS-001 | |
| PRA05 | Contract First | ✅ Documentado | LIN-API-REST-001 + LIN-BUS-001 P3 | APIs y eventos |
| PRA06 | Design for Failure | ✅ Documentado | LIN-ARQ-000 §3.7 | Cubierto en Monolito Modular y Microservicios |
| PRA07 | Loose Coupling / High Cohesion | ✅ Documentado | LIN-ARQ-000 §3.11 | Declarado formalmente con tabla de aplicación por decisión arquitectónica |
| PRA08 | Zero Trust | ⚠️ Parcial | LIN-SEC-APP-001 | Implícito, falta declaración formal |
| PRA09 | Inmutabilidad de eventos | ✅ Documentado | LIN-ARQ-000 §3.11 + §3.9.3 | Declarado formalmente en §3.11 con tabla de decisiones; referenciado normativamente en §3.9.3 (Published Language) |
| PRA10 | Single Source of Truth (por dominio) | ✅ Documentado | LIN-ARQ-000 §3.11 | Declarado formalmente — write model es autoritativo, read models son proyecciones derivadas |
| PRA11 | Trazabilidad completa (audit trail) | ⚠️ Parcial | LIN-OBS-001 + LIN-BUS-001 | Cubierto para observabilidad, falta para negocio |

---

## 8. Resumen de brechas por prioridad

### Alta prioridad — afectan lo que ya está en construcción

| Código | Brecha | Estado |
|---|---|---|
| PI06 | Circuit Breaker — resiliencia en llamadas REST y Kafka | ✅ Cerrada (LIN-ARQ-000 §3.7.3) |
| PI08 | Timeout — sin estándar definido | ✅ Cerrada (LIN-ARQ-000 §3.7.1) |
| PRA06 | Design for Failure — principio transversal a todo | ✅ Cerrada (LIN-ARQ-000 §3.7) |
| PR01–PR05 | SOLID — base del código Java ONP | ✅ Cerrada (LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 §10.4.1) |

### Media prioridad — necesarios al avanzar hacia microservicios

| Código | Brecha | Estado |
|---|---|---|
| PA09 | BFF (Backend for Frontend) — PT09 oficial | ✅ Cerrada (LIN-ARQ-000 §3.8.3) |
| PA10 | Facade (arquitectura) — PT12 oficial | ✅ Cerrada (LIN-ARQ-000 §3.8.1) |
| PA11 | Gateway-Aggregation — PT10 oficial | ✅ Cerrada (LIN-ARQ-000 §3.8.2) |
| PA12 | Sidecar | ❌ Pendiente (LIN-K8S-001) |
| PD08 | Context Map | ✅ Cerrada (LIN-ARQ-000 §3.9.1) |
| PD09 | Shared Kernel | ✅ Cerrada (LIN-ARQ-000 §3.9.2) |
| PI09 | Bulkhead | ✅ Cerrada (LIN-ARQ-000 §3.7.2) |
| PA07 | CQRS — detalle de implementación | ✅ Cerrada (LIN-ARQ-000 §3.10) |
| PD04–PD06 | Repository, Domain Service, Application Service — building blocks tácticos en Spring | ✅ Cerrada (LIN-DEV-JAVA-001 §13.5) |

### Baja prioridad — complementan el framework a largo plazo

| Código | Brecha |
|---|---|
| PG01–PG03 | Patrones GoF — parcialmente cerrados en LIN-ARQ-000 §8 (los de mayor uso en Spring); patrones restantes de baja prioridad, se asume conocimiento del equipo |
| E08–E09 | Serverless, Pipe and Filter |
| PR10–PR11 | Law of Demeter, Tell Don't Ask |
| PA13 | Ambassador |

---

## 9. Lineamientos pendientes por rol

Lineamientos identificados como necesarios para roles que aún no tienen cobertura en el framework.

| Código futuro | Rol objetivo | Estado | Condición para crearlo |
|---|---|---|---|
| LIN-DISENO-001 | Diseñador de Software | ❌ Pendiente | Cuando el rol de diseñador se formalice como persona separada del arquitecto. Debe definir: artefactos obligatorios por proyecto, herramienta de modelado institucional, nivel de detalle según tipo de proyecto, vinculación con ADRs y con el código. |

> **Nota:** hoy el arquitecto ejerce también el rol de diseñador. Este ítem es previsión para cuando el equipo crezca y los roles se separen.

---

## 10. Historial de revisiones



| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-07-01 | Arquitectura OTI | Versión inicial — brecha identificada a partir de análisis del framework vigente |
| v0.1.1 | 2026-07-02 | Arquitectura OTI | Cierra brechas de alta prioridad PI06, PI08, PRA06 y media prioridad PI07, PI09 normadas en LIN-ARQ-000 §3.7 |
| v0.1.2 | 2026-07-02 | Arquitectura OTI | Cierra brechas de patrones oficiales PT09 (PA09), PT10 (PA11) y PT12 (PA10) normadas en LIN-ARQ-000 §3.8 |
| v0.1.3 | 2026-07-02 | Arquitectura OTI | Cierra brechas de patrones DDD en Monolito Modular PD08 (Context Map) y PD09 (Shared Kernel) normadas en LIN-ARQ-000 §3.9 |
| v0.1.4 | 2026-07-06 | Arquitectura OTI | Cierra brechas de Nivel 3 en LIN-DEV-JAVA-001 v0.1.2: PR01–PR08 (SOLID + DRY/KISS/YAGNI, §10.4) y PD04–PD06 (Repository/Domain Service/Application Service, §11.5). Actualiza PG01–PG03 de ❌ Pendiente a ⚠️ Parcial tras verificar cobertura de Adapter/Decorator/Facade, Factory Method/Builder/Singleton y Strategy/Observer/Command/State en LIN-ARQ-000 §8 |
| v0.1.5 | 2026-07-10 | Arquitectura OTI | Corrige citas a LIN-DEV-JAVA-001 en filas PR01–PR09 y PD04–PD06: las secciones §10.4.x y §11.5.x citadas en v0.1.4 fueron renumeradas internamente en revisiones posteriores de LIN-DEV-JAVA-001 sin que este tablero se actualizara. Ubicación real verificada: SOLID → §7.1–§7.5, DRY/KISS/YAGNI → §12.4.2–§12.4.4, Repository/Domain Service/Application Service → §13.5.1–§13.5.3. Pendiente: el resto de filas de este tablero sigue citando `LIN-ARQ-000` (congelado desde 2026-07-07) en vez de `LIN-ARQ-001`/`LIN-DIS-001`/`LIN-PAT-001` — reconciliación completa fuera de alcance de esta corrección puntual |
