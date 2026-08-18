# Brecha del Framework de Arquitectura ONP

**Código:** GOB-BRE-001
**Versión:** 0.1.7
**Fecha:** 2026-07-06
**Responsable:** Arquitectura OTI
**Estado:** En revisión

Este documento registra los patrones, estilos, principios y decisiones arquitectónicas identificados como necesarios para el framework ONP pero que aún no están documentados formalmente en ningún lineamiento vigente. Sirve como lista de trabajo para ir cerrando las brechas punto por punto.

> **Códigos `PT`:** la fuente única es `LIN-PAT-001` (índice de trazabilidad en `GOB-MAT-001`). Este tablero usa códigos `PA`/`PI`/`PD`/`PR`/`PRA` propios para organizar las brechas; cuando cita un `PT` debe coincidir con el catálogo.

**Convención de estado:**
- `❌ Pendiente` — no existe ninguna documentación
- `⚠️ Parcial` — mencionado o implícito en algún lineamiento pero sin definición formal
- `✅ Documentado` — cubierto formalmente en un lineamiento vigente

---

## 1. Estilos Arquitectónicos
*Cómo se organiza internamente un sistema*

| # | Estilo | Estado | Documento destino | Notas |
|---|---|---|---|---|
| E01 | Arquitectura en Capas (Layered) | ✅ Documentado | LIN-DIS-001 §2.2 | |
| E02 | Arquitectura Hexagonal (Ports & Adapters) | ✅ Documentado | LIN-DIS-001 §2.3 | |
| E03 | Monolito Puro | ✅ Documentado | LIN-ARQ-001 §2.1 | |
| E04 | Monolito Modular | ✅ Documentado | LIN-ARQ-001 §2.1 | Destino por defecto ONP |
| E05 | Microservicios | ✅ Documentado | LIN-ARQ-001 §2.1 | Con criterios de extracción |
| E06 | Arquitectura Orientada a Eventos (EDA) | ✅ Documentado | LIN-ARQ-001 §4.2 | |
| E07 | Arquitectura Orientada a Servicios (SOA) | ✅ Documentado | LIN-ARQ-001 §4.3 | Modelo mandatorio para interoperabilidad B2G/G2G (PIDE, RENIEC, SUNAT) con resiliencia y ACL obligatorios |
| E08 | Serverless | ❌ Pendiente | LIN-ARQ-001 | Evaluar si aplica a largo plazo en ONP |
| E09 | Pipe and Filter | ❌ Pendiente | LIN-ARQ-001 | Revisar si aplica para pipelines de datos |

---

## 2. Patrones de Arquitectura
*Soluciones recurrentes a problemas de estructura a nivel de sistema*

| # | Patrón | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PA01 | Strangler Fig | ✅ Documentado | LIN-ARQ-001 §2.2 | Migración de legacy |
| PA02 | Anti-Corruption Layer (ACL) | ✅ Documentado | LIN-DIS-001 §5.4 + LIN-ARQ-001 §4.3 | Obligatorio con RENIEC, SUNAT, PIDE |
| PA03 | Saga — coreografía | ✅ Documentado | LIN-ARQ-001 §3.3 + LIN-BUS-001 §9.1 | |
| PA04 | Saga — orquestación | ✅ Documentado | LIN-ARQ-001 §3.3 + LIN-BUS-001 §9.2 | |
| PA05 | Saga sobre monolitos (Kafka + REST) | ✅ Documentado | LIN-ARQ-001 §3.3.1 + LIN-BUS-001 §9.4 | Variante ONP — parque actual |
| PA06 | Transactional Outbox | ✅ Documentado | LIN-ARQ-001 §3.3 + LIN-BUS-001 §7.3 | |
| PA07 | CQRS (Event-Driven) | ✅ Documentado | LIN-DIS-001 §4.2 | Dos variantes: Outbox+Kafka (estándar) y CDC+Kafka (agnóstico al motor). Adopción mediante ADR obligatorio. |
| PA08 | API Gateway / API Manager | ✅ Documentado | LIN-API-REST-001 §2.5 | WSO2 — estado PoC (ADR-WSO2-001) |
| PA09 | BFF (Backend for Frontend) | ✅ Documentado | LIN-DIS-001 §5.1 | Patrón oficial **PT11** (ficha `PAT-INT-01`) para adaptación de canales y mediación SSO frente a WSO2. |
| PA10 | Facade (arquitectura) | ✅ Documentado | LIN-DIS-001 §5.3 | Patrón oficial **PT15** (ficha `PAT-INT-03`) para ocultar complejidad externa/legada en Monolito Modular. |
| PA11 | Gateway-Aggregation | ✅ Documentado | LIN-DIS-001 §5.2 | Patrón oficial **PT12** (ficha `PAT-INT-02`) intra-JVM para Monolito Modular y vía red en Microservicios. |
| PA12 | Sidecar | ✅ Documentado | LIN-K8S-001 §9.4.A | Patrón oficial **PT17** (ficha `PAT-K8S-01`). Prohibido en Java/Spring Boot 3; admitido solo para cajas negras que escriben bitácora en archivo, y reservado a la malla de servicios cuando exista |
| PA13 | Ambassador | ✅ Documentado | LIN-K8S-001 §9.4.B | Patrón oficial **PT18** (ficha `PAT-K8S-02`). Prohibido en Java/Spring Boot 3 —la resiliencia saliente se resuelve en la JVM según `LIN-DIS-001 §6`—; admitido solo en monolitos heredados no-Java bajo Strangler Fig |
| PA14 | Feature Toggle (Strangler complemento) | ✅ Documentado | LIN-ARQ-001 §2.3 | Normado en 4 variantes para Trunk-Based Development, Kill-Switch y Branch by Abstraction |

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
| PI06 | Circuit Breaker | ✅ Documentado | LIN-DIS-001 §6.2 | Estándar y condicionales (Estadio 3 Microservicios o excepción en Monolito Modular) |
| PI07 | Retry con backoff exponencial | ✅ Documentado | LIN-DIS-001 §6.3 + LIN-BUS-001 §8.5 | Cubierto para REST y Kafka |
| PI08 | Timeout | ✅ Documentado | LIN-DIS-001 §6.1 | Matriz de timeouts por criticidad y demanda |
| PI09 | Bulkhead | ✅ Documentado | LIN-DIS-001 §6.3 | Aislamiento por pools de conexión HTTP/JDBC |
| PI10 | Rate Limiting (patrón de diseño) | ⚠️ Parcial | LIN-API-REST-001 | Cubierto en WSO2 Gateway, falta para consumo interno |
| PI11 | Competing Consumers | ❌ Pendiente | LIN-BUS-001 | Múltiples instancias consumiendo el mismo tópico |
| PI12 | Content-Based Router | ❌ Pendiente | LIN-BUS-001 | Enrutamiento de mensajes según contenido |
| PI13 | Correlación de trazas productor→consumidor | ✅ Documentado | LIN-BUS-001 §11.1 + LIN-OBS-001 | Via `traceparent` W3C |

---

## 4. Patrones de Dominio (DDD)
*Para modelar lógica de negocio compleja*

| # | Patrón | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PD01 | Aggregate | ✅ Documentado | LIN-DIS-001 §3.2 | Guía normativa en contexto Spring: transacciones y eventos en Agregado Raíz |
| PD02 | Entity / Value Object | ✅ Documentado | LIN-DIS-001 §3.2 | Diferenciación estricta con `@Entity` JPA; records autovalidados en Java 21 |
| PD03 | Domain Event | ✅ Documentado | LIN-DIS-001 §3.2 + LIN-BUS-001 §5 | |
| PD04 | Repository | ✅ Documentado | LIN-DEV-JAVA-001 §13.5.3 | Puerto en capa de dominio + adaptador `JpaRepository`/`JdbcRepository` en infraestructura; traducción de excepciones Oracle a jerarquía limpia |
| PD05 | Domain Service | ✅ Documentado | LIN-DEV-JAVA-001 §13.5.2 | POJO puro sin anotaciones de Spring/JPA; registro obligatorio vía `@Configuration`/`@Bean` para preservar la pureza hexagonal |
| PD06 | Application Service | ✅ Documentado | LIN-DEV-JAVA-001 §13.5.1 | Orquestador transaccional (`@Service` + `@Transactional`); delega en el Domain Service y publica eventos de dominio |
| PD07 | Bounded Context | ✅ Documentado | LIN-DIS-001 §3 | |
| PD08 | Context Map | ✅ Documentado | LIN-DIS-001 §3.1 | Contrato y relaciones entre Bounded Contexts en Monolito Modular |
| PD09 | Shared Kernel | ✅ Documentado | LIN-DIS-001 §3.4 | Estándar y tabla de elementos permitidos/prohibidos en módulo común |
| PD10 | Published Language | ✅ Documentado | LIN-BUS-001 §5.2 + LIN-API-REST-001 | Contratos CloudEvents v1.0 / Schema Registry (asíncrono) y OpenAPI 3.0 (síncrono) |

---

## 5. Patrones de Diseño (GoF)
*Soluciones a problemas recurrentes a nivel de código*

| # | Categoría | Patrones | Estado | Documento destino | Notas |
|---|---|---|---|---|---|
| PG01 | Creacionales | Factory Method, Abstract Factory, Builder, Singleton, Prototype | ⚠️ Parcial | LIN-DEV-JAVA-001 §8.2 | Factory Method, Builder y Singleton cubiertos con ficha (§8.2.1–8.2.3). Abstract Factory y Prototype sin ficha — baja prioridad, poco uso en el estilo ONP |
| PG02 | Estructurales | Adapter, Facade, Decorator, Proxy, Composite, Bridge, Flyweight | ⚠️ Parcial | LIN-DEV-JAVA-001 §8.1 | Adapter, Decorator y Facade cubiertos con ficha (§8.1.1–8.1.3). Proxy, Composite, Bridge y Flyweight sin ficha — evaluar si el framework los necesita. Nota: el Facade aquí es el patrón GoF de nivel de código — el Facade de nivel de arquitectura está en PA10. |
| PG03 | Comportamiento | Strategy, Observer, Command, Template Method, Chain of Responsibility, State, Iterator, Mediator | ⚠️ Parcial | LIN-DEV-JAVA-001 §8.3 | Strategy, Observer, Command y State cubiertos con ficha (§8.3.1–8.3.4) — los más usados en Spring. Template Method, Chain of Responsibility, Iterator y Mediator sin ficha |

---

## 6. Principios de Diseño
*Guías para escribir buen código*

| # | Principio | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PR01 | SOLID — Single Responsibility | ✅ Documentado | LIN-DEV-JAVA-001 §7.1 | Declaración arquitectónica en LIN-DEV-JAVA-001 §7; aplicación práctica en Spring y anti-patrón God Object en LIN-DEV-JAVA-001 §7.1 (renumerado desde §10.4.1 — corrección 2026-07-10). |
| PR02 | SOLID — Open/Closed | ✅ Documentado | LIN-DEV-JAVA-001 §7.2 | Ídem PR01 — ejemplo de patrón Estrategia inyectado por Spring para evitar condicionales en cadena |
| PR03 | SOLID — Liskov Substitution | ✅ Documentado | LIN-DEV-JAVA-001 §7.3 | Ídem PR01 |
| PR04 | SOLID — Interface Segregation | ✅ Documentado | LIN-DEV-JAVA-001 §7.4 | Ídem PR01 |
| PR05 | SOLID — Dependency Inversion | ✅ Documentado | LIN-DEV-JAVA-001 §7.5, §13.5 | Declaración en LIN-DEV-JAVA-001 §7; guía de inyección por constructor y puertos de dominio en LIN-DEV-JAVA-001 §7.5 y §13.5. |
| PR06 | DRY (Don't Repeat Yourself) | ✅ Documentado | LIN-DEV-JAVA-001 §12.4.2 | Declarado en LIN-DEV-JAVA-001 §12.4; criterio ONP de duplicación de conocimiento de negocio vs. acoplamiento entre módulos en LIN-DEV-JAVA-001 §12.4.2. |
| PR07 | KISS (Keep It Simple) | ✅ Documentado | LIN-DEV-JAVA-001 §12.4.3 | Ídem PR06 — uso de Records/Sealed Classes de Java 21 y prohibición de sobre-ingeniería |
| PR08 | YAGNI (You Ain't Gonna Need It) | ✅ Documentado | LIN-DEV-JAVA-001 §12.4.4 | Ídem PR06 — prohibición de abstracciones especulativas sin requerimiento formal |
| PR09 | Separation of Concerns | ✅ Documentado | LIN-DIS-001 §1.2 | Declarado formalmente en `LIN-DIS-001 §1.2` y aplicado en `LIN-DEV-JAVA-001 §12.4` con ejemplos de violación |
| PR10 | Law of Demeter | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR11 | Tell Don't Ask | ❌ Pendiente | LIN-DEV-JAVA-001 | |

---

## 7. Principios Arquitectónicos
*Restricciones y decisiones de alto nivel que gobiernan el sistema*

| # | Principio | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PRA01 | Teorema CAP (CP vs AP) | ✅ Documentado | LIN-ARQ-001 §3.1 | Obligatorio en extracción de microservicios |
| PRA02 | Consistencia eventual | ✅ Documentado | LIN-ARQ-001 §4.2 + LIN-BUS-001 | |
| PRA03 | Idempotencia | ✅ Documentado | LIN-BUS-001 §8.4 | |
| PRA04 | Observabilidad como requisito (4 pilares) | ✅ Documentado | LIN-ARQ-001 §5.3 + LIN-OBS-001 | |
| PRA05 | Contract First | ✅ Documentado | LIN-API-REST-001 + LIN-BUS-001 P3 | APIs y eventos |
| PRA06 | Design for Failure | ✅ Documentado | LIN-DIS-001 §6 | Cubierto en Monolito Modular y Microservicios |
| PRA07 | Loose Coupling / High Cohesion | ✅ Documentado | LIN-DIS-001 §1.2 | Declarado formalmente con tabla de aplicación por decisión arquitectónica |
| PRA08 | Zero Trust | ⚠️ Parcial | LIN-SEC-APP-001 | Implícito, falta declaración formal |
| PRA09 | Inmutabilidad de eventos | ✅ Documentado | LIN-DIS-001 §1.2 | Declarado formalmente en `LIN-DIS-001 §1.2` con tabla de decisiones; referenciado normativamente en `LIN-BUS-001 §5.2` (Published Language) |
| PRA10 | Single Source of Truth (por dominio) | ✅ Documentado | LIN-DIS-001 §1.2 | Declarado formalmente — write model es autoritativo, read models son proyecciones derivadas |
| PRA11 | Trazabilidad completa (audit trail) | ⚠️ Parcial | LIN-OBS-001 + LIN-BUS-001 | Cubierto para observabilidad, falta para negocio |

---

## 8. Resumen de brechas por prioridad

### Alta prioridad — afectan lo que ya está en construcción

| Código | Brecha | Estado |
|---|---|---|
| PI06 | Circuit Breaker — resiliencia en llamadas REST y Kafka | ✅ Cerrada (LIN-DIS-001 §6.2) |
| PI08 | Timeout — sin estándar definido | ✅ Cerrada (LIN-DIS-001 §6.1) |
| PRA06 | Design for Failure — principio transversal a todo | ✅ Cerrada (LIN-DIS-001 §6) |
| PR01–PR05 | SOLID — base del código Java ONP | ✅ Cerrada (`LIN-DIS-001 §1.2` + `LIN-DEV-JAVA-001 §7.1`–`§7.5`) |

### Media prioridad — necesarios al avanzar hacia microservicios

| Código | Brecha | Estado |
|---|---|---|
| PA09 | BFF (Backend for Frontend) — PT11 oficial | ✅ Cerrada (LIN-DIS-001 §5.1) |
| PA10 | Facade (arquitectura) — PT15 oficial | ✅ Cerrada (LIN-DIS-001 §5.3) |
| PA11 | Gateway-Aggregation — PT12 oficial | ✅ Cerrada (LIN-DIS-001 §5.2) |
| PA12 | Sidecar — PT17 oficial | ✅ Cerrada (LIN-K8S-001 §9.4.A, ficha `PAT-K8S-01`) |
| PA13 | Ambassador — PT18 oficial | ✅ Cerrada (LIN-K8S-001 §9.4.B, ficha `PAT-K8S-02`) |
| PD08 | Context Map | ✅ Cerrada (LIN-DIS-001 §3.1) |
| PD09 | Shared Kernel | ✅ Cerrada (LIN-DIS-001 §3.4) |
| PI09 | Bulkhead | ✅ Cerrada (LIN-DIS-001 §6.3) |
| PA07 | CQRS — detalle de implementación | ✅ Cerrada (LIN-DIS-001 §4.2) |
| PD04–PD06 | Repository, Domain Service, Application Service — building blocks tácticos en Spring | ✅ Cerrada (LIN-DEV-JAVA-001 §13.5) |

### Baja prioridad — complementan el framework a largo plazo

| Código | Brecha |
|---|---|
| PG01–PG03 | Patrones GoF — parcialmente cerrados en LIN-DEV-JAVA-001 §8 (los de mayor uso en Spring); patrones restantes de baja prioridad, se asume conocimiento del equipo |
| E08–E09 | Serverless, Pipe and Filter |
| PR10–PR11 | Law of Demeter, Tell Don't Ask |

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
| v0.1.1 | 2026-07-02 | Arquitectura OTI | Cierra brechas de alta prioridad PI06, PI08, PRA06 y media prioridad PI07, PI09 normadas en LIN-DIS-001 §6 |
| v0.1.2 | 2026-07-02 | Arquitectura OTI | Cierra brechas de patrones oficiales PT09 (PA09), PT10 (PA11) y PT12 (PA10) normadas en LIN-ARQ-000 §3.8 |
| v0.1.3 | 2026-07-02 | Arquitectura OTI | Cierra brechas de patrones DDD en Monolito Modular PD08 (Context Map) y PD09 (Shared Kernel) normadas en LIN-ARQ-000 §3.9 |
| v0.1.4 | 2026-07-06 | Arquitectura OTI | Cierra brechas de Nivel 3 en LIN-DEV-JAVA-001 v0.1.2: PR01–PR08 (SOLID + DRY/KISS/YAGNI, §10.4) y PD04–PD06 (Repository/Domain Service/Application Service, §11.5). Actualiza PG01–PG03 de ❌ Pendiente a ⚠️ Parcial tras verificar cobertura de Adapter/Decorator/Facade, Factory Method/Builder/Singleton y Strategy/Observer/Command/State en LIN-DEV-JAVA-001 §8 |
| v0.1.5 | 2026-07-10 | Arquitectura OTI | Corrige citas a LIN-DEV-JAVA-001 en filas PR01–PR09 y PD04–PD06: las secciones §10.4.x y §11.5.x citadas en v0.1.4 fueron renumeradas internamente en revisiones posteriores de LIN-DEV-JAVA-001 sin que este tablero se actualizara. Ubicación real verificada: SOLID → §7.1–§7.5, DRY/KISS/YAGNI → §12.4.2–§12.4.4, Repository/Domain Service/Application Service → §13.5.1–§13.5.3. Pendiente: el resto de filas de este tablero sigue citando `LIN-ARQ-000` (congelado desde 2026-07-07) en vez de `LIN-ARQ-001`/`LIN-DIS-001`/`LIN-PAT-001` — reconciliación completa fuera de alcance de esta corrección puntual |
| v0.1.7 | 2026-08-17 | Arquitectura OTI | Cierra **PA12 (Sidecar)** y **PA13 (Ambassador)**, que este tablero seguía marcando `❌ Pendiente` pese a estar normados en detalle en `LIN-K8S-001 §9.4` desde hacía versiones. El caso era además circular: `LIN-K8S-001` los identificaba con los códigos `PA12`/`PA13` **de este tablero de brechas** en vez de con códigos oficiales, porque el catálogo `LIN-PAT-001` no tenía ficha para ellos. Resuelto con las fichas `PAT-K8S-01` (`PT17`) y `PAT-K8S-02` (`PT18`) (`GOB-CHK-001` H26) |
| v0.1.6 | 2026-08-05 | Arquitectura OTI | Reconciliación completa de citas (`GOB-CHK-001` H6.3): **66 referencias al documento congelado `LIN-ARQ-000`** redirigidas a su documento vigente (`LIN-ARQ-001`, `LIN-DIS-001`, `LIN-DEV-JAVA-001`, `LIN-BUS-001`) — pendiente reconocido desde v0.1.5 y declarado entonces «fuera de alcance». Corregida además la **discrepancia de códigos PT**: el tablero asignaba `PT09` a BFF, `PT10` a Gateway-Aggregation y `PT12` a Facade, contradiciendo a `LIN-PAT-001` (fuente única), que asigna `PT11`=BFF, `PT12`=Gateway-Aggregation y `PT15`=Facade; se añade la referencia a la ficha `PAT-*` de cada uno |
