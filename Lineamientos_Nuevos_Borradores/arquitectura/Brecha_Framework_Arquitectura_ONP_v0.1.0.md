# Brecha del Framework de Arquitectura ONP

**Fecha:** 2026-07-01
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
| E07 | Arquitectura Orientada a Servicios (SOA) | ❌ Pendiente | LIN-ARQ-000 | Relevante para integración con entidades del Estado |
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
| PA07 | CQRS (Event-Driven) | ⚠️ Parcial | LIN-ARQ-000 §3.6 | Solo mencionado, sin detalle de implementación |
| PA08 | API Gateway / API Manager | ✅ Documentado | LIN-API-REST-001 §2.5 | WSO2 — estado PoC (ADR-WSO2-001) |
| PA09 | BFF (Backend for Frontend) | ❌ Pendiente | LIN-API-REST-001 o LIN-ARQ-000 | Relevante cuando frontend y móvil coexistan. PT09 oficial. |
| PA10 | Facade (arquitectura) | ❌ Pendiente | LIN-ARQ-000 | Componente que oculta complejidad de múltiples sistemas externos (ej. fachada RENIEC+SUNAT+SBS). Ya se usa en ONP sin definición formal. PT12 oficial. Ver también PG02 (GoF, nivel de código). |
| PA11 | Gateway-Aggregation | ❌ Pendiente | LIN-ARQ-000 | Agrega respuestas de múltiples servicios en una sola respuesta al cliente. PT10 oficial. |
| PA12 | Sidecar | ❌ Pendiente | LIN-K8S-001 | Relevante para observabilidad y mTLS en K8s |
| PA13 | Ambassador | ❌ Pendiente | LIN-K8S-001 | Proxy de salida en K8s |
| PA14 | Feature Toggle (Strangler complemento) | ❌ Pendiente | LIN-ARQ-000 | Útil en migraciones graduales |

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
| PI06 | Circuit Breaker | ❌ Pendiente | LIN-ARQ-000 o LIN-RESILIENCIA-001 | **Alta prioridad** — aplica ya en REST y Kafka |
| PI07 | Retry con backoff exponencial | ⚠️ Parcial | LIN-BUS-001 §8.5 | Mencionado para Kafka, falta estándar para REST |
| PI08 | Timeout | ❌ Pendiente | LIN-ARQ-000 o LIN-RESILIENCIA-001 | **Alta prioridad** — sin estándar definido |
| PI09 | Bulkhead | ❌ Pendiente | LIN-RESILIENCIA-001 | Aislamiento de fallos entre pools de recursos |
| PI10 | Rate Limiting (patrón de diseño) | ⚠️ Parcial | LIN-API-REST-001 | Cubierto en WSO2 Gateway, falta para consumo interno |
| PI11 | Competing Consumers | ❌ Pendiente | LIN-BUS-001 | Múltiples instancias consumiendo el mismo tópico |
| PI12 | Content-Based Router | ❌ Pendiente | LIN-BUS-001 | Enrutamiento de mensajes según contenido |
| PI13 | Correlación de trazas productor→consumidor | ✅ Documentado | LIN-BUS-001 §11.1 + LIN-OBS-001 | Via `traceparent` W3C |

---

## 4. Patrones de Dominio (DDD)
*Para modelar lógica de negocio compleja*

| # | Patrón | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PD01 | Aggregate | ⚠️ Parcial | LIN-ARQ-000 | Mencionado, sin guía de implementación |
| PD02 | Entity / Value Object | ⚠️ Parcial | LIN-ARQ-000 | Mencionado, sin guía de implementación |
| PD03 | Domain Event | ✅ Documentado | LIN-ARQ-000 + LIN-BUS-001 | |
| PD04 | Repository | ⚠️ Parcial | LIN-DEV-JAVA-001 | Implícito en Hexagonal, no definido explícitamente |
| PD05 | Domain Service | ⚠️ Parcial | LIN-DEV-JAVA-001 | Implícito, sin guía concreta |
| PD06 | Application Service | ⚠️ Parcial | LIN-DEV-JAVA-001 | Implícito, sin guía concreta |
| PD07 | Bounded Context | ✅ Documentado | LIN-ARQ-000 | |
| PD08 | Context Map | ❌ Pendiente | LIN-ARQ-000 | Cómo se relacionan los BCs entre sí |
| PD09 | Shared Kernel | ❌ Pendiente | LIN-ARQ-000 | Modelo compartido entre dos BCs |
| PD10 | Published Language | ❌ Pendiente | LIN-ARQ-000 + LIN-BUS-001 | Relacionado con CloudEvents y contratos de eventos |

---

## 5. Patrones de Diseño (GoF)
*Soluciones a problemas recurrentes a nivel de código*

| # | Categoría | Patrones | Estado | Documento destino | Notas |
|---|---|---|---|---|---|
| PG01 | Creacionales | Factory Method, Abstract Factory, Builder, Singleton, Prototype | ❌ Pendiente | LIN-DEV-JAVA-001 | Se asume conocimiento del equipo; prioridad baja |
| PG02 | Estructurales | Adapter, Facade, Decorator, Proxy, Composite, Bridge, Flyweight | ❌ Pendiente | LIN-DEV-JAVA-001 | Adapter y Facade son los más relevantes en Hexagonal. Nota: Facade aquí es el patrón GoF de nivel de código — el Facade de nivel de arquitectura (componente que oculta sistemas externos) está en PA10. |
| PG03 | Comportamiento | Strategy, Observer, Command, Template Method, Chain of Responsibility, State, Iterator, Mediator | ❌ Pendiente | LIN-DEV-JAVA-001 | Strategy y Observer son los más usados en Spring |

---

## 6. Principios de Diseño
*Guías para escribir buen código*

| # | Principio | Estado | Documento destino | Notas |
|---|---|---|---|---|
| PR01 | SOLID — Single Responsibility | ❌ Pendiente | LIN-DEV-JAVA-001 | **Alta prioridad** — base de todo el código Java ONP |
| PR02 | SOLID — Open/Closed | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR03 | SOLID — Liskov Substitution | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR04 | SOLID — Interface Segregation | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR05 | SOLID — Dependency Inversion | ❌ Pendiente | LIN-DEV-JAVA-001 | Clave para Hexagonal |
| PR06 | DRY (Don't Repeat Yourself) | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR07 | KISS (Keep It Simple) | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR08 | YAGNI (You Ain't Gonna Need It) | ❌ Pendiente | LIN-DEV-JAVA-001 | |
| PR09 | Separation of Concerns | ⚠️ Parcial | LIN-ARQ-000 | Implícito en Hexagonal |
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
| PRA06 | Design for Failure | ❌ Pendiente | LIN-ARQ-000 | **Alta prioridad** — atraviesa EDA, Saga, microservicios |
| PRA07 | Loose Coupling / High Cohesion | ⚠️ Parcial | LIN-ARQ-000 | Implícito, no declarado formalmente |
| PRA08 | Zero Trust | ⚠️ Parcial | LIN-SEC-APP-001 | Implícito, falta declaración formal |
| PRA09 | Inmutabilidad de eventos | ⚠️ Parcial | LIN-BUS-001 | Implícito, no declarado formalmente |
| PRA10 | Single Source of Truth (por dominio) | ❌ Pendiente | LIN-ARQ-000 | Cada dato tiene un único sistema de registro |
| PRA11 | Trazabilidad completa (audit trail) | ⚠️ Parcial | LIN-OBS-001 + LIN-BUS-001 | Cubierto para observabilidad, falta para negocio |

---

## 8. Resumen de brechas por prioridad

### Alta prioridad — afectan lo que ya está en construcción

| Código | Brecha |
|---|---|
| PI06 | Circuit Breaker — resiliencia en llamadas REST y Kafka |
| PI08 | Timeout — sin estándar definido |
| PRA06 | Design for Failure — principio transversal a todo |
| PR01–PR05 | SOLID — base del código Java ONP |

### Media prioridad — necesarios al avanzar hacia microservicios

| Código | Brecha |
|---|---|
| PA09 | BFF (Backend for Frontend) — PT09 oficial |
| PA10 | Facade (arquitectura) — PT12 oficial |
| PA11 | Gateway-Aggregation — PT10 oficial |
| PA12 | Sidecar |
| PD08 | Context Map |
| PD09 | Shared Kernel |
| PI09 | Bulkhead |
| PA07 | CQRS — detalle de implementación |

### Baja prioridad — complementan el framework a largo plazo

| Código | Brecha |
|---|---|
| PG01–PG03 | Patrones GoF — se asume conocimiento del equipo |
| E07–E09 | SOA, Serverless, Pipe and Filter |
| PR06–PR11 | DRY, KISS, YAGNI y otros principios de diseño |
| PA13–PA14 | Ambassador, Feature Toggle |

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
