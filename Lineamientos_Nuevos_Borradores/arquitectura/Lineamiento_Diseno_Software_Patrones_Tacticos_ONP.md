# Lineamiento de Estándar de Diseño de Software y Patrones Tácticos en la ONP

**Código:** LIN-DIS-001  
**Versión:** 0.1.8  
**Fecha:** 2026-08-05  
**Autor:** Oficina de Tecnologías de la Información — ONP  
**Estado:** En revisión

---

## Historial de versiones

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 0.1.0 – 0.1.4 | 2026-07-08 a 2026-07-09 | Arquitectura OTI | Versiones iniciales del estándar táctico derivadas del desglose de `LIN-ARQ-000` en el modelo de 3 niveles. *(Detalle por versión no registrado — este historial se incorpora en v0.1.5.)* |
| 0.1.5 | 2026-08-05 | Arquitectura OTI | Declara explícitamente a `§6` como **documento dueño** de la resiliencia táctica (umbrales de timeout, Bulkhead, Retry y condiciones de adopción de Circuit Breaker), tras corregirse en `LIN-ARQ-001 §4.3` un mandato de Resilience4j que contradecía a `§6.2` y un rango de timeout propio que divergía de la matriz de `§6.1`. Se deja constancia de qué delega el Nivel 1 y qué conserva `LIN-API-REST-001 §8.3` (`GOB-CHK-001` H2 y H3) |
| 0.1.6 | 2026-08-17 | Arquitectura OTI | **El diagrama de decisión de `§2.1` contradecía a `§6`, dentro de este mismo documento.** Decía «MANDATORIO RESILIENCE4J: Timeouts (2s/3s), Circuit Breaker y Bulkhead» —justo lo que v0.1.5 había eliminado de `LIN-ARQ-001 §4.3`— y publicaba umbrales fijos que `§6.1` sustituyó por una matriz por criticidad. El defecto estaba en la parte más leída del documento: el árbol de decisión que un desarrollador consulta antes que el cuerpo normativo. Corregido para reflejar `§6.1`–`§6.3` (`GOB-CHK-001` H26) |
| 0.1.7 | 2026-08-18 | Arquitectura OTI | `§3.4` deja constancia de que el gobierno del Shared Kernel pasa a ser **verificable automáticamente** mediante las pruebas de arquitectura de `LIN-DEV-JAVA-001 §15.5`, y de que el aislamiento entre Bounded Contexts —que Maven no impide, porque basta declarar la dependencia— tiene ahora una regla que lo comprueba (`GOB-CHK-001` H37) |
| 0.1.8 | 2026-09-18 | Arquitectura OTI | `§6.2` describía el estado SEMI-ABIERTO del Circuit Breaker con un umbral de éxito (8 de 10, 20% de fallo) distinto y más estricto que el 50% del estado CERRADO. Hallazgo real al construir `template-microservicio-java`: `Resilience4j` (`CircuitBreakerConfig`) no expone un `failureRateThreshold` independiente por estado — configurar el umbral más estricto en semi-abierto lo habría aplicado también a cerrado, contradiciendo la fila de arriba. Corregido a un único umbral del 50% en ambos estados (≥5 de 10 exitosas), alineado con lo que la librería de referencia permite configurar por anotación sin código a medida |

---

## Glosario de Términos

| Término / Acrónimo | Definición y Contexto Operativo en la ONP |
|---|---|
| **ACL (*Anti-Corruption Layer*)** | Patrón de integración que interpone un traductor (DTO ajeno → `Mapper` → modelo propio) entre el dominio de un módulo y un sistema externo o legado, evitando que su modelo contamine el dominio limpio. Mandatorio al consumir JBoss legacy o entidades externas (SUNAT, RENIEC, PIDE); se implementa con tres componentes coordinados: el DTO ajeno, el `Mapper` traductor y el `Adapter` de salida (`§5.4`, `DIS-R-006`). |
| **Active Record** | Estrategia de lógica de dominio donde la propia entidad JPA (`@Entity`) contiene getters/setters y validaciones simples de su registro. Aplicable a catálogos enriquecidos o tablas maestras sin invariantes que crucen múltiples tablas (`§4.1`). |
| **Adapter (*Adaptador*)** | Implementación técnica concreta (`@RestController`, `@Repository` JPA, `RestClient`) que consume un puerto de entrada o implementa un puerto de salida en Arquitectura Hexagonal, viviendo fuera del paquete `domain/` (`§2.3`). |
| **Aggregate Root (*Raíz de Agregado*)** | Entidad principal de un clúster de entidades y Value Objects interconectados que garantiza la consistencia transaccional ACID local de todo el árbol. Los repositorios de dominio (`Port Out`) solo cargan y guardan Aggregate Roots completos, nunca entidades sueltas (`§3.2`). |
| **Application (*Capa de Orquestación*)** | Paquete que implementa los Puertos de Entrada (`port.in`) del dominio: recibe la solicitud, orquesta uno o más Agregados e invoca los Puertos de Salida necesarios. Tan libre de framework como `domain/` — es un POJO sin `@Service` ni `@Transactional`; el cableado a Spring y la demarcación transaccional viven en `infrastructure/`. La lógica de negocio permanece en el dominio, aquí solo se coordina (`§2.3`). |
| **ArchUnit** | Framework de pruebas de arquitectura que verifica reglas de dependencia entre paquetes en tiempo de compilación. En la ONP implementa automáticamente las prohibiciones del Shared Kernel y el aislamiento del paquete `domain/` (`LIN-DEV-JAVA-001 §15.5`, `§3.4`). |
| **Arquitectura en Capas (*Layered Architecture*)** | Estilo arquitectónico interno con dependencia unidireccional `api -> service -> domain -> repository`. Estándar para módulos de soporte simples, CRUD o Active Record con menos de 3 integraciones externas (`§2.2`). |
| **Arquitectura Hexagonal (*Ports & Adapters*)** | Patrón que aísla la lógica de dominio (`domain/`) detrás de puertos (interfaces) e implementaciones de infraestructura (adaptadores), prohibiendo cualquier `import` de frameworks técnicos dentro del dominio. Obligatorio para el Core Previsional, módulos con 3+ integraciones externas y candidatos a microservicio (`§2.3`, `DIS-R-001`). |
| **BFF (*Backend for Frontend*)** | Capa que consolida y adapta llamados a múltiples servicios internos en un único payload a la medida de una interfaz específica (web o móvil), evitando el *Frontend Chatterness & Overfetching*. Obligatorio cuando un canal consolida 3+ módulos o requiere mediación de seguridad tipo *Token Handler* (`§5.1`, `DIS-R-005`). |
| **Bounded Context** | Límite explícito de un subdominio de negocio en DDD dentro del cual un modelo de datos y reglas tienen significado único y soberano. En el Monolito Modular de la ONP, los límites de cada módulo Maven corresponden exactamente a un Bounded Context (`§3`, `DIS-R-002`). |
| **Bulkhead** | Patrón de resiliencia que acota el pool de conexiones o hilos dedicado a un proveedor externo, evitando que su saturación agote los recursos compartidos del servicio. En Monolito Modular se resuelve por defecto con `setMaxConnPerRoute` de Apache HttpClient 5, sin Resilience4j (`§6.3`). |
| **Búsqueda de Texto Difuso** | Búsqueda por texto libre con tolerancia a variaciones de escritura, filtros combinados y facetas, ordenada por relevancia (*scoring*) — ej. buscar "pension" y encontrar "pensión". No viable de forma óptima en un motor relacional; se resuelve con **Elasticsearch** (`§2.1` Pregunta 4, `§4.2`). |
| **CDC (*Change Data Capture*)** | Variante de sincronización de CQRS que captura cambios directamente de los logs transaccionales del motor de base de datos (Debezium + Kafka Connect) sin código en la aplicación. Agnóstica al código pero requiere infraestructura adicional y, sobre Oracle, validación previa con los DBA (`§4.2`, Variante B). |
| **Chatty API / N+1 Calls / Overfetching** | Familia de antipatrones de comunicación excesiva entre un consumidor y el backend: múltiples peticiones secuenciales (*Chatty / N+1*) o payloads sobredimensionados (*Overfetching*) que degradan el rendimiento en el navegador o cliente. BFF y Gateway-Aggregation son los patrones que los previenen (`§5.1`, `§5.2`). |
| **Circuit Breaker** | Patrón de resiliencia con máquina de estados (Cerrado / Abierto / Semi-Abierto) que interrumpe llamadas hacia una dependencia que falla repetidamente. Obligatorio con Resilience4j en Microservicios; excepcional bajo ADR en Monolito Modular, solo cuando el timeout + Bulkhead no bastan (`§6.2`, `DIS-R-009`). |
| **Conformista (*Conformist*)** | Tipo de relación de Context Map donde el consumidor adopta sin cambios el modelo del proveedor. Permitido únicamente para catálogos generales o maestros universales (Ubigeo, Tipos de Documento) (`§3.1`). |
| **Context Map (*Mapa de Contextos*)** | Modelo DDD que rige cómo interactúan dos Bounded Contexts entre sí. La ONP regula cuatro tipos de relación: Cliente-Proveedor, ACL, Conformista y Caminos Separados (`§3.1`). |
| **CQRS (*Command Query Responsibility Segregation*)** | Patrón que separa físicamente el modelo de escritura (ACID/Oracle) del modelo de lectura (desnormalizado/NoSQL), sincronizados por Transactional Outbox + Kafka o CDC. Mandatorio cuando las necesidades de indexación y rendimiento de lectura y escritura son opuestas (`§4.2`, `DIS-R-004`). |
| **DDD (*Domain-Driven Design*)** | Enfoque de diseño para dominios de negocio complejos mediante Bounded Contexts, Agregados, Value Objects y Domain Events. Solo se adopta en la ONP cuando se cumplen simultáneamente seis (06) criterios de gobernanza (`§3.0`); de lo contrario se usa Transaction Script o Active Record. |
| **Design for Failure** | Principio rector de la resiliencia táctica: todo llamado por red es inherentemente falible, por lo que el diseño debe asumir el fallo del proveedor externo en vez de confiar en su disponibilidad. Rige toda la `§6`. |
| **Documento 360° (*Vista 360°*)** | Vista agregada y consolidada de una entidad previsional que reúne en un solo documento datos que en el modelo relacional viven repartidos en múltiples tablas — ej. un expediente completo con todos sus anexos, aportes y liquidaciones. No viable de forma performante en Oracle vía *joins*; se resuelve con **MongoDB** como store documental (`§2.1` Pregunta 4, `§4.2`). |
| **Domain Model** | Estrategia de lógica de dominio más compleja de las cuatro descritas en `§4.1`: modelo rico orientado a objetos con Aggregate Roots, Entities y Value Objects que protegen sus invariantes transaccionales aislados de la infraestructura. Reservada al núcleo previsional core. |
| **Domain Service (*Servicio de Dominio*)** | Building block DDD que encapsula lógica que involucra o coordina dos o más Aggregate Roots sin pertenecer exclusivamente a ninguno (ej. `CalculadorActuarialDomainService`) (`§3.2`). |
| **Entity (*Entidad de Dominio*)** | Building block DDD con identidad única que persiste en el tiempo y contiene la lógica transaccional y mutaciones controladas del negocio. No es una entidad JPA: vive en `domain/model/` sin anotaciones de persistencia (`§3.2`, `§3.3`). |
| **Facade Arquitectónico de Integración** | Patrón que oculta la complejidad y verbosidad de un sistema externo heredado (SOAP/XML/mTLS) detrás de una interfaz Java limpia orientada al caso de uso, evitando la *Leaky Abstraction*. Se ubica en `infrastructure/adapter/out/facade/` (`§5.3`). |
| **Fail-fast** | Estrategia de timeout que aborta rápido una llamada externa para liberar hilos en lugar de esperar, usada en categorías de alta demanda y ruta crítica interactiva (RENIEC, SAA) (`§6.1`). |
| **Gateway-Aggregation** | Patrón que combina llamadas a múltiples repositorios o servicios internos para construir un único payload consolidado, sin lógica transaccional propia. Previene el antipatrón *Chatter API / N+1 Calls* (`§5.2`). |
| **Leaky Abstraction (*Fuga de Abstracción*)** | Antipatrón donde los detalles técnicos de un tercero (librerías XML, protocolos SOAP) invaden la capa de aplicación y contaminan los servicios de negocio. El patrón Facade existe para prevenirlo (`§5.3`). |
| **Port (*Puerto*)** | Interfaz Java que define un contrato dentro del paquete `domain/` en Arquitectura Hexagonal: un puerto de entrada (`port.in`) expresa lo que el exterior puede solicitar al dominio; un puerto de salida (`port.out`) expresa lo que el dominio requiere de la infraestructura (`§2.3`). |
| **Relación Cliente-Proveedor (*Customer-Supplier / Upstream-Downstream*)** | Tipo de relación de Context Map donde el proveedor (`Upstream`) expone un contrato estable vía paquete público `application.api`, y el consumidor (`Downstream`) tiene prohibido acceder a sus paquetes `domain` o `infrastructure` (`§3.1`). |
| **Retry (*Reintentos*)** | Mecanismo de resiliencia para mitigar microcortes en lecturas idempotentes. En Monolito Modular se resuelve con Spring Retry simple, sin Resilience4j; prohibido reintentar automáticamente operaciones de escritura no idempotentes (`§6.4`). |
| **SAA** | Sistema institucional de Administración de Accesos de la ONP (definido en `LIN-ARQ-001`); emite el token de autenticación usado por frontends y backends. Referenciado aquí como ejemplo de servicio de alta demanda en la matriz de timeouts (`§6.1`). |
| **Screaming Architecture** | Organización opcional de paquetes dentro de una capa (típicamente `application/`) por intención de negocio en vez de por tipo técnico, para que la estructura de carpetas "grite" qué hace el sistema. Se aplica cuando el volumen de funcionalidades de una capa lo justifica; no es mandatoria (`§2.3.1`). |
| **Separate Ways (*Caminos Separados*)** | Tipo de relación de Context Map donde dos Bounded Contexts no tienen relación de negocio coherente. Prohibido acoplarlos en código Java; su comunicación eventual debe ser 100% asíncrona vía Kafka (`§3.1`). |
| **Shared Kernel (*Núcleo Compartido*)** | Módulo Maven (`onp-common-domain`) que centraliza código transversal estrictamente limitado a Value Objects universales, jerarquía de excepciones raíz y primitivas core — nunca entidades JPA, lógica de negocio o puertos de persistencia. Gobernado y verificado automáticamente con pruebas ArchUnit (`§3.4`, `DIS-R-003`). |
| **Table Module** | Estrategia de lógica de dominio donde un único objeto calcula reglas masivas sobre toda una colección o tabla en memoria simultáneamente, en vez de instanciar miles de objetos individuales. Reservada a procesos batch y cálculos actuariales masivos (`§4.1`). |
| **Token Handler** | Patrón de mediación de seguridad donde el BFF gestiona sesiones o cookies seguras con el cliente e inyecta el token institucional al llamar al API Manager (WSO2), evitando que el frontend maneje directamente el token de acceso (`§5.1.1`). |
| **Transaction Script** | Estrategia de lógica de dominio más simple: cada caso de uso es un método procedimental que invoca queries secuenciales y cálculos lineales. Reservada a módulos CRUD de soporte administrativo sin reglas cruzadas de estado (`§4.1`). |
| **Transactional Outbox** | Patrón de sincronización estándar primario de CQRS: dentro de la misma transacción ACID de Oracle se inserta el evento en la tabla `EVT_OUTBOX`, y un proceso de relevo lo publica de forma garantizada en Kafka (`§4.2`, Variante A). |
| **Value Object (*Objeto de Valor*)** | Building block DDD inmutable sin identidad propia, comparado por sus atributos, implementado mandatoriamente con Java Records (ej. `MontoMonetario`) (`§3.2`). |
| **Vertical Slicing** | Organización de un módulo complejo en slices verticales (uno por Aggregate Root o capacidad de negocio), cada uno con su propio `domain/`, `application/`, `infrastructure.in/` e `infrastructure.out/`, en vez de un único conjunto de esas cuatro carpetas para todo el módulo. Reduce el acoplamiento accidental entre conceptos de negocio no relacionados en módulos con muchos agregados (`§2.3.1`). |

---

## 1. Introducción y Relación con el Nivel 1 (`LIN-ARQ-001`)

### 1.1 Del Nivel 1 (Macro) al Nivel 2 (Táctico): El Interior del Contenedor

Mientras que el Marco Rector (`LIN-ARQ-001`) decide **cuáles** son los sistemas, en qué estadios se ubican (Monolito Modular vs. Microservicios) y bajo qué protocolos de red interactúan, el presente estándar de Nivel 2 (`LIN-DIS-001`) prescribe **cómo diseñar la estructura interna de cada uno de esos módulos o microservicios**.

> **Plantilla asociada:** el Documento de Arquitectura de TI del sistema (`Plantilla_Arquitectura_TI_ONP_v1.3.md`) declara el estilo macro. El diseño detallado —donde se verifica la aplicación de este estándar por componente— se elabora con `Plantilla_Documento_Diseno_ONP.md` (Documento de Diseño), cuya `§3.4` cubre paso a paso el estilo interno, los criterios de DDD, el Context Map, CQRS y la resiliencia táctica de cada componente/Bounded Context del sistema.

```
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 1: MARCO RECTOR (`LIN-ARQ-001`)                                    │
│ Decisión: "El módulo onp-aportes es un Bounded Context del Monolito      │
│           Modular y expone servicios REST / Kafka CloudEvents v1.0".     │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Define el límite externo
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 2: ESTÁNDAR TÁCTICO (`LIN-DIS-001` - PRESENTE DOCUMENTO)           │
│ Decisión: "Dentro de onp-aportes se aplica Arquitectura Hexagonal con    │
│           aislamiento de dominio, Agregados DDD, y patrón ACL para SOA". │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Guía el diseño de paquetes e interfaces
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 3: ESTÁNDAR DE DESARROLLO JAVA (`LIN-DEV-JAVA-001`)                │
│ Decisión: "La interfaz AportesPort se implementa en AportesJpaAdapter    │
│           usando @Service, @Transactional y Patrones GoF Spring".        │
└──────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Principios Rectores Tácticos

Todo diseño modular interno y estructuración de clases en los sistemas ONP debe responder a cuatro principios innegociables:

1. **Alta Cohesión y Bajo Acoplamiento (`PRA07`):** Un paquete o módulo (`pe.gob.onp.core.expedientes`) debe agrupar elementos íntimamente relacionados por un único propósito de negocio (Alta Cohesión). A la vez, debe conocer y depender del menor número posible de otros paquetes institucionales (Bajo Acoplamiento).
2. **Inmutabilidad de Eventos (`PRA09` / *Event Immutability*):** Un evento publicado en el bus de mensajería es inmutable. Una vez publicado, su contenido no puede modificarse ni eliminarse — si la realidad de negocio cambia, se publica un nuevo evento que corrige o complementa al anterior. Gobierna el diseño de eventos de dominio, el `Transactional Outbox` (`ARQ-R-003` (LIN-ARQ-001 §3.3)) y la Política de Compatibilidad Total del Schema Registry (`LIN-ARQ-001 §4.2`).
3. **Fuente Única de Verdad Táctica (`PRA10` / *Single Source of Truth*):** Dentro de un sistema, cada concepto previsional (ej. `Expediente`, `Pensionista`, `Liquidacion`) es soberano de una sola entidad o agregado. Prohibido duplicar el estado autoritativo de un mismo dato en múltiples tablas o módulos internos.
4. **Separación de Responsabilidades (`PR09` / *Separation of Concerns*):** Las reglas de presentación (JSON/DTOs), la orquestación transaccional (`Use Cases`), las reglas puras del negocio (`Entities`/`Value Objects`) y la infraestructura de acceso a datos (`JPA`/`SQL`) son capas ontológicamente distintas que jamás deben mezclarse en una misma clase Java.

---

## 2. Árbol de Decisión Táctico y Estilos Arquitectónicos de Módulo

La estructura de carpetas, dependencias y patrones de un módulo Java no se elige por estética ni moda, sino por la complejidad funcional y transaccional de su requerimiento.

### 2.1 Árbol de Decisión Táctico ("¿Cuándo aplicar qué concepto?")

Antes de crear un nuevo paquete, clase o servicio, el Arquitecto y el Tech Lead deben responder estas **5 preguntas diagnósticas de Sí/No** para determinar las piezas exactas del diseño:

```
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│ PREGUNTA 1: ¿Qué Estilo Arquitectónico Interno necesita mi Módulo (`§2.2 / §2.3`)?        │
│                                                                                            │
│  ¿Es un CRUD de soporte simple, catálogo auxiliar o pantalla plana sin reglas complejas?    │
│    ├── SÍ ──► ARQUITECTURA EN CAPAS CLÁSICA (`api -> service -> repository`).              │
│    └── NO ──► (Es Core Previsional, tiene 3+ integraciones o se separará a Microservicio)  │
│               └──► ARQUITECTURA HEXAGONAL + DOMINIO PURO (`§2.3`).                         │
└─────────────────────────────────────────────┬──────────────────────────────────────────────┘
                                              ▼
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│ PREGUNTA 2: ¿Cómo modelar las Entidades y Lógica en Memoria (`§4.1`)?                      │
│                                                                                            │
│  • Flujo lineal sin estado mutante (exportar Excel, validar texto) ──► TRANSACTION SCRIPT  │
│  • Tabla referencial independiente por registro ─────────────────────► ACTIVE RECORD / JPA  │
│  • Proceso masivo o cálculo actuarial por lotes (Batch 50k+ reg.) ───► TABLE MODULE         │
│  • Core transaccional previsional con invariantes ACID locales ──────► DOMAIN MODEL (DDD)   │
└─────────────────────────────────────────────┬──────────────────────────────────────────────┘
                                              ▼
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│ PREGUNTA 3: ¿Qué Patrón de Interfaz e Integración Exterior aplico (`§5`)?                  │
│                                                                                            │
│  • ¿Consumo un legacy JBoss / PL-SQL o una entidad externa (SUNAT/RENIEC)? ──► ACL (`§5.4`)│
│  • ¿Una pantalla web/móvil llama a 4 APIs y necesita un payload unificado? ──► BFF (`§5.1`)│
│  • ¿El servicio recopila datos estáticos de 5 tablas internas para DTO? ─────► GATEWAY AGG.│
│  • ¿Me comunico asíncronamente con otros Bounded Contexts? ──────────────────► KAFKA / EVT │
└─────────────────────────────────────────────┬──────────────────────────────────────────────┘
                                              ▼
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│ PREGUNTA 4: ¿Necesito CQRS o Sincronización de Lectura (`§4.2`)?                          │
│                                                                                            │
│  ¿Las búsquedas ciudadanas requieren texto difuso, o documentos 360° no viables en Oracle? │
│    ├── SÍ ──► CQRS + OUTBOX / DEBEZIUM hacia MongoDB (360°), Redis (<2ms) o ElasticSearch. │
│    └── NO ──► MODELO RELACIONAL TRADICIONAL ÚNICO EN ORACLE (No aplicar CQRS).             │
└─────────────────────────────────────────────┬──────────────────────────────────────────────┘
                                              ▼
┌────────────────────────────────────────────────────────────────────────────────────────────┐
│ PREGUNTA 5: ¿Qué patrones de Resiliencia Táctica necesito (`§6`)?                          │
│                                                                                            │
│  ¿Llamo a un servicio por red (RENIEC, SUNAT, pasarela, WS externo)?                       │
│    ├── SÍ ──► SIEMPRE: Timeout (`§6.1`, matriz por criticidad) + Bulkhead vía HttpClient 5.│
│    │          Retry con Spring Retry (`§6.4`). Circuit Breaker con Resilience4j SOLO si    │
│    │          es Microservicio (Estadio 3) o hay ADR aprobado (`§6.2`).                    │
│    └── NO ──► (Llamada interna en memoria o BD local) -> No aplica resiliencia de red.     │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```

> **Sobre "texto difuso" y "documentos 360°" (Pregunta 4):** *texto difuso* se refiere a búsqueda por texto libre con tolerancia a variaciones de escritura, filtros combinados y facetas (ej. buscar "pension" y encontrar "pensión", con resultados ordenados por relevancia) — resuelto con Elasticsearch. *Documento 360°* se refiere a una vista agregada y consolidada de una entidad (ej. un expediente completo con todos sus anexos, aportes y liquidaciones en un solo documento) — resuelto con MongoDB. Ambos casos se detallan con su justificación técnica en la tabla de `§4.2`.

### 2.2 Arquitectura en Capas (*Layered Architecture*)

**Cuándo usar en la ONP:** Módulos de soporte, cruds administrativos simples, catálogos auxiliares o flujos lineales que no encierran reglas previsionales ni validaciones de negocio complejas. Adoptar cuando, simultáneamente: la lógica del módulo es simple o moderada (CRUD, Transaction Script o Active Record — `§4.1`); el módulo tiene **menos de 3 integraciones externas**; y no se prevé cambio de tecnología de persistencia ni de protocolo de entrada. Un módulo con esta arquitectura puede vivir indefinidamente en el Monolito Modular; si más adelante se convierte en candidato a microservicio, se evalúa contra los seis (06) criterios de `ARQ-R-001` (LIN-ARQ-001 §2.1).

```
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.api (Presentación / REST)   │
└────────────────────────────┬─────────────────────────────┘
                             │ DTOs de Entrada / Salida
                             ▼
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.service (Aplicación)        │
└────────────────────────────┬─────────────────────────────┘
                             │ Orquesta y llama directamente
                             ▼
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.domain (Dominio)             │
│ Entidades y reglas de negocio — sin anotaciones Spring/JPA│
└────────────────────────────┬─────────────────────────────┘
                             │ Usa interfaces que Infraestructura implementa
                             ▼
┌──────────────────────────────────────────────────────────┐
│ pe.gob.onp.soporte.catalogos.repository (Infraestructura/ │
│ Persistencia JPA, clientes HTTP, MQ)                      │
└──────────────────────────────────────────────────────────┘
```

- **Regla de Dependencia en Capas:** La dependencia fluye unidireccionalmente hacia abajo: `api -> service -> domain -> repository`. Queda prohibido que un `Controller` en `api` acceda directamente a un `Repository` saltándose las capas `service`/`domain`. La capa `domain` no depende de `repository` — usa interfaces (puertos) que `repository` implementa.
- **Nota de escala:** en módulos muy simples (CRUD sin reglas de negocio propias) la capa `domain` puede reducirse a las entidades JPA sin una clase de reglas separada — pero la capa sigue existiendo conceptualmente y las validaciones de negocio, si aparecen, van ahí, nunca en el `service` ni en el `Controller`.

### 2.3 Arquitectura Hexagonal (*Ports & Adapters*)

> 🔖 **`DIS-R-001`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

**Cuándo usar en la ONP:** **Obligatorio** para todos los módulos del *Core Previsional* (Aportes, Pensiones, Expedientes, Liquidaciones, Tesorería), módulos con tres o más integraciones externas y cualquier módulo candidato a extraerse en el futuro como un microservicio independiente (*Estadio 3*).

```
         ADAPTADORES DE ENTRADA           APLICACIÓN + DOMINIO (Núcleo Hexagonal)          ADAPTADORES DE SALIDA
         (pe.gob.onp.*.infrastructure.in)     (pe.gob.onp.*.application / .domain)     (pe.gob.onp.*.infrastructure.out)
       ┌──────────────────────────────┐     ┌──────────────────────────────┐     ┌──────────────────────────────┐
       │                              │     │   ┌──────────────────────┐   │     │                              │
───────┼─► [@RestController]          ├─────┼───► [RegistrarAportePort]│   │     │   [PensionistaJpaAdapter]    ├─────► [Oracle DB]
       │   (AporteController)         │     │   │     (Port in)        │   │     │               │              │
       │                              │     │   └──────────┬───────────┘   │     │               ▲              │
       │                              │     │              │               │     │               │              │
───────┼─► [KafkaEventListener]       │     │              ▼               │     │   ┌───────────┴──────────┐   │
       │   (AporteConsumerAdapter)    │     │   ┌──────────────────────┐   │     │   │PensionistaRepository │   │
       └──────────────────────────────┘     │   │RegistrarAporteService│   │     │   │      (Port out)      │   │
                                            │   │    (Use Case)        │   │     │   └──────────────────────┘   │
                                            │   └──────────┬───────────┘   │     │                              │
                                            │╌╌╌ application/ · domain/ ╌╌╌│     │                              │
                                            │              │               │     │   ┌──────────────────────┐   │
                                            │              ▼               │     │   │  ReniecHttpAdapter   ├─────► [RENIEC SOA]
                                            │   ┌──────────────────────┐   │     │   │      (Adapter)       │   │
                                            │   │ Aporte / Pensionista │   │     │   └───────────▲──────────┘   │
                                            │   │ (Agregado / Entidad) │   │     │               │              │
                                            │   └──────────────────────┘   │     │   ┌───────────┴──────────┐   │
                                            │                              │     │   │  ConsultaReniecPort  │   │
                                            │                              │     │   │      (Port out)      │   │
                                            └──────────────────────────────┘     └───┴──────────────────────┴───┘
```

#### Regla de Oro del Paquete `domain` (Aislamiento Absoluto)
> **Mandato Arquitectónico Innegociable:** El paquete `domain/` (tanto sus modelos como sus interfaces o puertos) es **Java puro desprovisto de dependencias de infraestructura o frameworks técnicos**. Queda **terminantemente prohibido** que cualquier clase o interfaz ubicada dentro de `domain/` contenga sentencias `import` hacia:
> - `jakarta.persistence.*` / `javax.persistence.*` (`@Entity`, `@Table`, `@Id`)
> - `org.springframework.*` (`@Autowired`, `@Service`, `@Transactional`, `@Component`)
> - `com.fasterxml.jackson.*` (`@JsonProperty`, `@JsonIgnore`)
> **El incumplimiento de esta regla rompe el modelo hexagonal, acopla el negocio a la base de datos y causará el rechazo inmediato del Pull Request en revisión de código.**
>
> **Esta misma prohibición rige también sobre `application/`.** La capa de aplicación (ver abajo) es un POJO tan libre de framework como el dominio; el cableado hacia Spring —incluida la demarcación transaccional— no ocurre ahí, sino en `infrastructure/`.

#### La Capa `application` (Orquestación de Casos de Uso)
Entre los adaptadores de entrada y el dominio puro vive el paquete `application/`, que **no existía como capa explícita en versiones previas de este estándar** y que el diagrama de `§2.3` ya distinguía visualmente (el `Use Case` sobre el `Agregado/Entidad`) sin nombrarla. Contiene las clases que implementan los `Puertos de Entrada` (ej. `RegistrarAporteService implements RegistrarAporteUseCase`): reciben la solicitud, orquestan uno o más Agregados del dominio e invocan los `Puertos de Salida` que necesiten. **Es un POJO — sin `@Service`, sin `@Transactional`, sin ningún estereotipo de Spring —, recibe sus puertos de salida por inyección de constructor y se prueba con JUnit puro, sin contenedor.** El cableado hacia el contenedor de Spring, incluida la demarcación transaccional, ocurre exclusivamente en `infrastructure/`, en una clase `@Configuration` que declara el caso de uso como `@Bean @Transactional`, exponiéndolo siempre por el tipo del puerto, nunca por el tipo concreto. **La lógica de negocio y las invariantes viven en el dominio, no aquí** — la capa `application` coordina, no decide reglas de negocio. Es también donde vive el paquete `application.api` que un módulo expone como contrato público a otros *Bounded Contexts* en una relación Cliente-Proveedor (`§3.1`).

#### Puertos vs. Adaptadores en la ONP
- **Puertos de Entrada (`port.in`):** Interfaces Java, definidas en `domain/`, que expresan lo que el exterior puede solicitarle al dominio (ej. `RegistrarAporteUseCase`, `AprobarExpedienteUseCase`). Las implementa una clase POJO de `application/`.
- **Puertos de Salida (`port.out`):** Interfaces Java dentro de `domain/` que expresan lo que el negocio requiere de la infraestructura externa para funcionar (ej. `PensionistaRepository`, `ConsultaReniecPort`, `NotificacionPort`).
- **Adaptadores (`infrastructure.in/out`):** Implementaciones técnicas concretas fuera del dominio (`@RestController`, `@Repository` de Spring Data JPA, `RestClient` HTTP) que consumen un puerto de entrada (vía el `@Bean` que expone la clase de `application/` que lo implementa) o implementan un puerto de salida. Es también aquí, en una clase `@Configuration` de `infrastructure/`, donde el POJO de `application/` entra al contenedor de Spring.

#### Regla de Oro del Paquete `infrastructure` (Frontera Técnica)
> **Mandato Arquitectónico:** `infrastructure/` (tanto `in/` como `out/`) es el único lugar del módulo donde es obligatorio depender de frameworks y tecnología externa — Spring, JPA/Hibernate, `RestClient`, clientes Kafka. A cambio, un `Adapter` tiene **prohibido**:
> - Contener **reglas o validaciones de negocio.** Esas viven en `domain/`; un `Adapter` solo traduce y transporta, nunca decide.
> - Propagar hacia `application/` o `domain/` **tipos, excepciones o estructuras propias de la tecnología o el tercero** (DTOs XML ajenos, excepciones de Hibernate, respuestas HTTP crudas). Se traducen dentro del propio adaptador antes de cruzar el puerto — es el mismo principio que gobierna la Capa Anticorrupción (`§5.4`).
> - **Invertir la dependencia.** Ningún `import` desde `domain/` o `application/` puede apuntar a una clase de `infrastructure/`. La dependencia fluye siempre de infraestructura hacia el dominio: es el adaptador quien implementa el puerto del dominio, no el dominio quien conoce a su adaptador.
> **El incumplimiento de esta regla convierte al adaptador en una *Leaky Abstraction* (`§5.3`) que contamina el negocio con detalles técnicos ajenos.**

#### 2.3.1 Organización para Módulos Complejos: Vertical Slicing y Screaming Architecture

Cuando un módulo (Bounded Context) tiene **muchos Aggregate Roots o casos de uso** — más de los que caben cómodamente en un único `domain/` / `application/` / `infrastructure.in/` / `infrastructure.out/` plano —, la estructura de `§2.3` se repite **por cada slice vertical** (agrupación funcional o agregado) en lugar de mezclar todo en las mismas cuatro carpetas técnicas:

```
pe.gob.onp.core.expedientes/
├── registro/                    ◄── Slice vertical: "Registro de Expediente"
│   ├── domain/                  (RegistroExpediente, DocumentoAdjunto, Value Objects del slice)
│   ├── application/             (RegistrarExpedienteService — implementa el Port In del slice)
│   ├── infrastructure/in/       (RegistroExpedienteController)
│   └── infrastructure/out/      (RegistroExpedienteJpaAdapter)
├── revision/                    ◄── Slice vertical: "Revisión de Expediente"
│   ├── domain/
│   ├── application/
│   ├── infrastructure/in/
│   └── infrastructure/out/
└── cierre/                      ◄── Slice vertical: "Cierre de Expediente"
    ├── domain/
    ├── application/
    ├── infrastructure/in/
    └── infrastructure/out/
```

- **Cuándo aplicar:** módulos con múltiples Aggregate Roots independientes, o cuyo `domain/` plano ya es difícil de navegar y aumenta el riesgo de acoplamiento accidental entre conceptos de negocio no relacionados.
- **Regla:** cada slice conserva íntegra la Regla de Oro (su `domain/` sin dependencias de framework). Un slice no importa clases del `domain/` de otro slice directamente — si necesitan comunicarse, lo hacen vía puertos, igual que si fueran módulos distintos.

**Screaming Architecture (opcional, dentro de cada capa):** cuando una capa de un slice concentra muchas funcionalidades (ej. un `application/` con más de una decena de casos de uso), es opcional subdividirla por intención de negocio en vez de dejarla plana — que el nombre del paquete "grite" qué hace, no de qué framework depende:

```
application/
├── registrar/     (RegistrarExpedienteService, RegistrarExpedienteCommand)
├── validar/       (ValidarDocumentacionService)
└── notificar/     (NotificarCiudadanoService)
```

Esta subdivisión es una decisión de organización interna del equipo — no es mandatoria — y se adopta cuando el volumen de clases dentro de una capa lo justifica.

---

## 3. Domain-Driven Design (DDD) en el Monolito Modular

> 🔖 **`DIS-R-002`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

En sistemas construidos bajo el **Monolito Modular (*Estadio 2*)**, los límites de cada módulo corresponden exactamente a un **Subdominio o Bounded Context de DDD**.

### 3.0 Criterios de Gobernanza — Cuándo Aplicar DDD

DDD **solo se aplica en la ONP cuando se cumplen simultáneamente los seis (06) criterios siguientes**. Si falta alguno, se usa Transaction Script o Active Record (**[§4.1](#41-comparativa-de-las-4-estrategias-de-lógica-de-dominio)**) — DDD no se adopta por moda ni por preferencia del equipo.

| # | Criterio obligatorio |
|---|---|
| 1 | El sistema es un **sistema core** de ONP (ej. cálculo de pensiones, gestión de aportes, liquidación). No aplica a sistemas de soporte administrativo. |
| 2 | El dominio tiene **reglas de negocio complejas** que cambian frecuentemente y que los expertos del negocio no pueden expresar en términos simples. |
| 3 | Existe un **experto de dominio disponible** (funcionario ONP con conocimiento profundo) que puede colaborar activamente durante el diseño. |
| 4 | El equipo de desarrollo tiene **experiencia previa con DDD**. No se aplica DDD como experimento de aprendizaje en producción. |
| 5 | El módulo tiene **vida útil larga** (más de 5 años) y se prevén cambios frecuentes en las reglas de negocio. |
| 6 | El *bounded context* está **claramente delimitado** y no comparte lógica de dominio con otros contextos. |

Cuando los seis (06) criterios se cumplen, los conceptos DDD aplicables son los *Building Blocks Tácticos* descritos en **[3.2](#32-building-blocks-tácticos-del-dominio)**.

### 3.1 Mapa de Contextos (*Context Map*) y Relaciones Permitidas

El *Context Map* rige cómo interactúan dos módulos institucionales (*Bounded Contexts*) entre sí. En la ONP se regulan cuatro tipos de relaciones tácticas (definiciones en el Glosario):

| Tipo de Relación en DDD | Regla Táctica de Implementación en la ONP |
|---|---|
| **Cliente-Proveedor** | El módulo proveedor expone únicamente un paquete público `application.api` con DTOs de lectura/escritura (`records`). Prohibido que el consumidor acceda al paquete `domain` o `infrastructure` del proveedor. |
| **Capa Anticorrupción (ACL)** | **Mandatoria** siempre que un módulo nuevo consuma servicios heredados de JBoss o entidades externas (SUNAT, RENIEC, PIDE). Ver sección `§5.4`. |
| **Conformista** | **Permitido únicamente** para consumo de catálogos generales, tablas referenciales o maestros universales (ej. Ubigeo, Tipos de Documento, Tablas de Regímenes Básicos). |
| **Caminos Separados** | Prohibido acoplar en código Java (`pom.xml`) subdominios divergentes (ej. Mesa de Partes Virtual y Reservas Actuariales). Su eventual comunicación debe ser 100% asíncrona vía bus Apache Kafka (`ADR-012`). |

### 3.2 Building Blocks Tácticos del Dominio

Dentro de la capa de dominio puro (`domain/model/`) de un *Bounded Context*, se utilizan cuatro patrones de modelado:

```java
// 1. VALUE OBJECT (Objeto de Valor): Inmutable, sin identidad, comparado por sus atributos.
// Implementación mandatoria con Java Records.
public record MontoMonetario(BigDecimal valor, String moneda) {
    public MontoMonetario {
        Objects.requireNonNull(valor, "Valor monetario no puede ser nulo");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto monetario no puede ser negativo");
        }
    }
    public MontoMonetario sumar(MontoMonetario otro) {
        if (!this.moneda.equals(otro.moneda)) throw new IllegalArgumentException("Monedas incompatibles");
        return new MontoMonetario(this.valor.add(otro.valor), this.moneda);
    }
}

// 2. ENTITY (Entidad de Dominio): Posee identidad única (ExpedienteId) que persiste en el tiempo.
// Contiene la lógica transaccional y mutaciones controladas. NO es una entidad JPA.
public class Expediente {
    private final ExpedienteId id;
    private EstadoExpediente estado;
    private final List<DocumentoAdjunto> documentos = new ArrayList<>();

    public Expediente(ExpedienteId id, EstadoExpediente estadoInicial) {
        this.id = id;
        this.estado = estadoInicial;
    }

    public void presentarParaRevision() {
        if (this.documentos.isEmpty()) {
            throw new ReglaNegocioVioladaException("No se puede presentar un expediente sin documentos");
        }
        this.estado = this.estado.toRevision();
    }
}

// 3. AGGREGATE ROOT (Raíz de Agregado): Entidad principal que encapsula un clúster de entidades y 
// Value Objects interconectados, garantizando la consistencia transaccional (ACID local) de todo el árbol.
// REGLA: Los repositorios de dominio (Port Out) SOLO cargan y guardan Aggregate Roots completos.
public class PensionistaAggregateRoot {
    private final PensionistaId id;
    private final DatosPersonales datosPersonales;
    private final List<Beneficiario> beneficiarios; // Solo modificable vía métodos del Aggregate Root
}

// 4. DOMAIN SERVICE (Servicio de Dominio): Lógica que involucra o coordina dos o más Aggregate Roots
// sin pertenecer exclusivamente a ninguno de ellos. (Ej: CalculadorActuarialDomainService).
```

### 3.3 Desacoplamiento Estricto: Entidad de Dominio Puro vs. Entidad JPA

Una confusión común en desarrolladores es intentar usar una clase con anotación `@Entity` de JPA como el `Aggregate Root` o la entidad pura del negocio. **Esta práctica está prohibida en Arquitectura Hexagonal y DDD en la ONP**.

| Dimensión | Entidad de Dominio Puro (`pe.gob.onp.*.domain.model`) | Entidad de Persistencia JPA (`pe.gob.onp.*.infrastructure.out.persistence.entity`) |
|---|---|---|
| **Propósito** | Encapsular las reglas previsionales, invariantes y transiciones de estado en memoria. | Representar fielmente la estructura tabular de las tablas en la base de datos Oracle (`TB_EXPEDIENTE`). |
| **Anotaciones** | **Ninguna.** Cero anotaciones `@Entity`, `@Table`, `@Column` o `@Getter/@Setter` de Lombok para romper encapsulamiento. | `@Entity`, `@Table(name="...")`, `@Id`, `@SequenceGenerator`, `@Version` y anotaciones JPA. |
| **Constructor** | Constructores de negocio que exigen datos válidos por construcción. | Constructor vacío (`protected` o `public`) exigido por la especificación JPA/Hibernate. |
| **Relaciones** | Relaciones conceptuales puras (ej. listas de Value Objects inmutables o IDs de otros agregados). | `@OneToMany`, `@ManyToOne`, `@JoinColumn`, `FetchType.LAZY`. |
| **Sincronización** | Traducida de ida y vuelta en la capa de infraestructura por un `Mapper` / `Adapter`. | Persistida físicamente en Oracle mediante `Spring Data JPA Repository`. |

### 3.4 Gobierno del Núcleo Compartido (*Shared Kernel — `onp-common-domain`*)

> 🔖 **`DIS-R-003`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

En el Monolito Modular, para no duplicar código transversal pero evitar introducir un monolito oculto dentro de una librería compartida, la OTI gobierna con celo militar el contenido del módulo Maven `onp-common-domain` (*Shared Kernel - PD09*).

| Elemento Técnico | Estado en el Shared Kernel (`onp-common-domain`) | Ejemplos Institucionales Autorizados / Prohibidos |
|---|---|---|
| **Value Objects Universales** | **✅ PERMITIDO** | Tipos primitivos transversales al Estado: `Dni`, `Ruc`, `MontoMonetario`, `PeriodoTributario`, `NumeroExpediente`. |
| **Jerarquía de Excepciones Raíz** | **✅ PERMITIDO** | Excepciones base de arquitectura: `OnpDomainException`, `RecursoNoEncontradoException`, `ReglaNegocioVioladaException`. |
| **Primitivas y Envolventes Core** | **✅ PERMITIDO** | Clases base de paginación (`PageResult<T>`), interfaces marcador (`DomainEvent`, `AggregateRoot`). |
| **Entidades JPA (`@Entity`)** | **❌ PROHIBIDO TERMINANTEMENTE** | Ninguna tabla de Oracle puede mapearse en la librería común. Cada módulo (`onp-expedientes`, `onp-aportes`) es dueño exclusivo de sus entidades JPA. |
| **Lógica / Servicios Previsionales** | **❌ PROHIBIDO TERMINANTEMENTE** | Clases como `CalculoPensionService` o `ValidadorDeudaService` deben pertenecer exclusivamente a sus *Bounded Contexts*. |
| **Puertos de Persistencia o JPA** | **❌ PROHIBIDO TERMINANTEMENTE** | Prohibido poner interfaces de repositorios o clientes HTTP en el *Shared Kernel*. |

> **Estas prohibiciones son verificables automáticamente.** `LIN-DEV-JAVA-001 §15.5` implementa las reglas de esta tabla como pruebas de arquitectura ArchUnit (tipo `AT`), que fallan la compilación ante una `@Entity`, un `*Service` de negocio o un puerto en `onp-common-domain`. Hasta 2026-08-18 el cumplimiento dependía únicamente de la declaración jurada del Tech Lead (`ARQ-R-008` (LIN-ARQ-001 §8.3) numeral 4), sin verificación de ninguna clase.

---

## 4. Patrones Tácticos de Lógica de Negocio y Consulta

### 4.1 Comparativa de las 4 Estrategias de Lógica de Dominio

La OTI reconoce cuatro estrategias para organizar la lógica transaccional, ordenadas según la complejidad del requerimiento del submódulo:

| Estrategia de Lógica de Dominio | Definición Táctica y Estructura | Cuándo Aplicar en Módulos de la ONP |
|---|---|---|
| **1. Transaction Script** | Cada caso de uso es un método procedimental en un Servicio (`@Service @Transactional`) que invoca secuencialmente queries sobre tablas y realiza cálculos lineales directos. | Módulos CRUD de soporte administrativo, generación de registros simples o flujos de carga masiva sin reglas cruzadas de estado. |
| **2. Active Record** | La entidad que mapea la fila de la base de datos (`@Entity` JPA) contiene getters/setters y encapsula en sí misma pequeñas validaciones simples y operaciones locales de su propio registro. | Módulos intermedios de catálogos enriquecidos o tablas maestras del sistema donde no existen invariantes complejos que abarquen múltiples tablas. |
| **3. Table Module** | Un único objeto o clase de servicio organiza y calcula reglas masivas sobre **toda una colección o tabla de registros en memoria** al mismo tiempo, en lugar de instanciar miles de objetos individuales. | **Procesos por lotes (*Batch*) y Cálculos Actuariales Masivos.** Ej: Cálculo simultáneo de devengados para 50,000 pensionistas en una planilla mensual de pago sin desbordar el *Heap* de la JVM. |
| **4. Domain Model (DDD)** | Un modelo rico orientado a objetos con `Aggregate Roots`, `Entities` y `Value Objects` que protegen estrictamente sus invariantes transaccionales en memoria aislados de la infraestructura. | **Núcleo Previsional Core:** Expedientes, Liquidaciones, Trámites de Jubilación y Aportes, donde una transición inválida causa graves perjuicios económicos o legales al Estado. |

### 4.2 Patrón CQRS — Separación de Modelos de Escritura y Lectura (*PA07*)

> 🔖 **`DIS-R-004`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

El patrón **CQRS (*Command Query Responsibility Segregation*)** se adopta mandatoriamente cuando las operaciones que consultan un dato (*Queries*) y las que lo modifican (*Commands*) tienen necesidades de indexación, latencia y rendimiento tan opuestas que usar una sola tabla en Oracle degrada ambos extremos.

```
┌────────────────────────────────────────────────────────────────────────┐
│ LADO DE ESCRITURA (Command Side - ACID / Relacional)                   │
│                                                                        │
│ [Command: RegistrarAporte] ──► [Handler] ──► [Oracle DB: TB_APORTE]    │
│                                                     │                  │
└─────────────────────────────────────────────────────┼──────────────────┘
                                                      │ Sincronización Eventual
                                                      ▼
┌────────────────────────────────────────────────────────────────────────┐
│ LADO DE LECTURA (Query Side - Desnormalizado / NoSQL)                  │
│                                                                        │
│ [Query: BuscarExpediente360] ◄── [Read Repo] ◄── [MongoDB / Redis]     │
└────────────────────────────────────────────────────────────────────────┘
```

#### Variantes Aprobadas de Sincronización en CQRS
1. **Variante A — Transactional Outbox + Kafka (Estándar Primario):** Dentro de la misma transacción ACID de Oracle donde se modifica la tabla del negocio, se inserta un registro de evento en la tabla `EVT_OUTBOX` (DDL canónico en `LIN-BD-ORA-001 §3.10`). Un proceso asíncrono o *relay* publica el evento en Apache Kafka y un consumidor proyecta la vista en el motor de lectura NoSQL.
2. **Variante B — CDC (*Change Data Capture*) + Kafka (Agnóstico al Código):** Se utiliza una herramienta a nivel de motor de base de datos (**Debezium + Kafka Connect**) que captura los cambios en tiempo real leyendo directamente los logs binarios transaccionales — cero código en la aplicación, requiere Kafka Connect como infraestructura adicional. Implicaciones por motor:

   | Motor | Mecanismo CDC | Consideraciones en ONP |
   |---|---|---|
   | **Oracle** (estándar ONP) | LogMiner o XStream | Requiere *Supplemental Logging* habilitado. Puede implicar revisión de licenciamiento Oracle. **Toda implementación sobre Oracle requiere validación previa con los DBA.** |
   | PostgreSQL | WAL (*Write-Ahead Log*) | Sin costo de licencia adicional; habilitación simple (`wal_level = logical`). Referencia para el caso excepcional en que un ADR autorice un motor no-Oracle. |
   | MySQL / MariaDB | Binlog | Configuración estándar, ampliamente soportado. Referencia para el mismo caso excepcional. |

#### Elección del Store de Lectura (`Read Model`)
| Patrón de Consulta Táctico en el Caso de Uso | Store de Lectura Asignado | Justificación Táctica |
|---|---|---|
| **Búsqueda puntual por clave o ID** (`DNI`, `NumeroExpediente`). | **Redis / Key-Value** | Latencia sub-milisegundo (< 2ms). Proyección pre-cacheada en memoria de los datos del ciudadano. |
| **Documento Agregado Complejo 360°** (Expediente completo con todos sus anexos, aportes y liquidaciones consolidados). | **MongoDB** | Estructura documental desnormalizada (`JSON / BSON`). Evita ejecutar *Queries* relacionales pesadas con 8+ *JOINs* sobre tablas históricas en Oracle durante el horario de atención ciudadana. |
| **Búsqueda por Texto Libre difuso, filtros combinados y facetas.** | **Elasticsearch** | Índices invertidos y *scoring* de relevancia no viables de forma óptima en motores relacionales o documentales clásicos. |
| **Consultas que requieren *joins* moderados y el modelo relacional sigue siendo el correcto.** | **Read replica relacional** | La complejidad relacional no desaparece al proyectar — forzar un store no relacional aquí solo añade traducción innecesaria. |
| **Agregaciones analíticas, reportes, tendencias históricas.** | **Capa Gold del Medallion** (`LIN-ARQ-001 §6.3`, `LIN-BI-001`) | El read model operacional (Redis/MongoDB/Elasticsearch) no está pensado para agregaciones históricas masivas; ese rol lo cumple el Lakehouse. |

---

## 5. Patrones Tácticos de Interfaz, Agregación e Integración

### 5.1 Patrón BFF (*Backend for Frontend — PT11*)

> 🔖 **`DIS-R-005`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

Para evitar que una interfaz de usuario SPA (Angular) realice 10 peticiones REST secuenciales desde el navegador para armar una sola pantalla, o que reciba DTOs genéricos con 80 campos innecesarios, se prescribe el patrón **BFF (*Backend for Frontend*)**.

```
┌──────────────────────────────────────┐     ┌──────────────────────────────────────┐
│  SPA ANGULAR (Mesa de Partes Web)    │     │  APP MÓVIL (Mesa de Partes Móvil)    │
└──────────────────┬───────────────────┘     └──────────────────┬───────────────────┘
                   │ 1 Petición REST / DTO a Medida             │ 1 Petición REST / DTO Ligero
                   ▼                                            ▼
┌───────────────────────────────────────────────────────────────────────────────────┐
│ CAPA BFF (pe.gob.onp.bff.mesapartes.* - Spring Boot en Kubernetes)                │
│ • Agrega y consolida llamados hacia 3 servicios internos (Expedientes, Aportes)   │
│ • Limpia, formatea y reduce el DTO exclusivamente a lo que la UI requiere       │
└──────────────────────────────────────────┬────────────────────────────────────────┘
                                           │ Orquestación interna gRPC / REST
                                           ▼
┌───────────────────────────────────────────────────────────────────────────────────┐
│ MICROSERVICIOS Y MÓDULOS DEL CORE PREVISIONAL (APIs de Dominio)                   │
└───────────────────────────────────────────────────────────────────────────────────┘
```

- **Propósito:** Desacoplar la experiencia de usuario (UI web/móvil) de la complejidad interna del ecosistema de microservicios o módulos, ofreciendo una API construida a la medida exacta de las vistas.
- **Cuándo usar en la ONP:** Cuando una interfaz SPA Angular (`Mesa de Partes Web`) o una aplicación móvil necesita consolidar en una sola pantalla información que internamente proviene de 3 o más módulos/microservicios (ej. Expedientes, Aportes y Datos Personales), o cuando los DTOs del core devuelven 80 campos pesados y el frontend solo requiere 10.
- **Cuándo NO usar (Antipatrón):** Prohibido usar el BFF cuando existe una relación 1:1 simple donde la UI consume exactamente el mismo DTO que expone el servicio de dominio (crear un BFF pasamanos es sobreingeniería innecesaria). Prohibido absolutamente meter reglas transaccionales ACID o acceso a bases de datos Oracle dentro de un BFF.
- **Ubicación en Capas Java:** Se implementa en un proyecto o contenedor independiente de la capa externa (`pe.gob.onp.bff.*`), o en el módulo de presentación pública del Monolito Modular.
- **Antipatrón que previene:** El *Frontend Chatterness & Overfetching*, que degrada el tiempo de carga en navegadores y expone metadatos sensibles del dominio en la red pública.

#### 5.1.1 Mediación de Seguridad frente al API Manager (*Token Handler*)

El BFF es el punto donde se implementa el patrón **Token Handler** frente al API Manager institucional (WSO2) y las políticas de SSO: el BFF gestiona sesiones ligeras o cookies seguras (`HttpOnly`) con la SPA o App Móvil, intercepta las peticiones e inyecta los tokens criptográficos institucionales (`Authorization: Bearer <token>`) al llamar al API Manager. Esto protege los tokens contra ataques XSS en el navegador (`LIN-SEC-APP-001 §10.1`) y simplifica la seguridad del canal — el frontend nunca maneja directamente el token de acceso al core.

**Cuándo adoptarlo:** cuando el sistema presta servicios a dos o más canales de consumo diferenciados (ej. Portal Web Angular + App Móvil nativa) con requisitos de seguridad distintos, o cuando un canal requiere mediación especializada frente al API Manager. Si el proyecto tiene un único canal estándar y no hay mediación SSO externa, la propia capa `controller/` del Monolito Modular sirve el contrato directamente — no se construye un BFF Token Handler separado.

### 5.2 Patrón Gateway-Aggregation (*PT12*)

- **Propósito:** Combinar llamadas y recopilar datos estáticos desde múltiples repositorios, servicios internos o puertos independientes para construir y devolver un único payload consolidado.
- **Cuándo usar en la ONP:** Cuando una operación interna (ej. *Cargar Datos del Ciudadano para Solicitud*) necesita consultar el maestro de afiliados, el historial de aportes recientes y el estado del último expediente para armar un DTO compuesto sin hacer que la UI o el consumidor realicen peticiones dispersas.
- **Cuándo NO usar (Antipatrón):** Prohibido usar `GatewayAggregator` como un "escondite" para mezclar lógica transaccional y mutaciones transaccionales complejas de varios agregados. Su única responsabilidad es consultar, agregar y transformar en un DTO.
- **Ubicación en Capas Java:** Se implementa dentro de `application/service/aggregator/` (o en la capa `service` del Monolito Modular) como un componente sin estado (`@Service`).
- **Antipatrón que previene:** El *Chatter API / N+1 Calls*, donde el consumidor de la API debe hacer 5 peticiones por red y pegar los JSON localmente para obtener una sola vista del dato.

### 5.3 Patrón Facade Arquitectónico de Integración (*PT15*)

- **Propósito:** Ocultar la complejidad, heterogeneidad técnica, protocolos de seguridad y verbosidad de un sistema externo o ecosistema heredado detrás de una interfaz Java limpia y orientada a la intención del caso de uso.
- **Cuándo usar en la ONP:** Al consumir servicios con protocolos pesados o antiguos (`SOAP / XML / mTLS / WS-Security` como PIDE, SUNAT o JBoss legacy) donde se requiere autenticación por sobre XML, manejo de sesiones, o una secuencia de 3 peticiones SOAP previas antes de obtener el dato final.
- **Cuándo NO usar (Antipatrón):** Prohibido crear un Facade para llamadas simples REST que ya devuelven JSON limpio y no requieren transformación ni orquestación de protocolo ajeno (en cuyo caso basta un `RestClientAdapter` directo).
- **Ubicación en Capas Java:** Se ubica estrictamente dentro de la capa de infraestructura externa: `infrastructure/adapter/out/facade/`.
- **Antipatrón que previene:** El *Leaky Abstraction (Fuga de Abstracción)*, donde los detalles técnicos y librerías XML del tercero invaden la capa de aplicación y contaminan los servicios del negocio de la ONP.

### 5.4 Patrón Anti-Corruption Layer (*ACL — PT13*) en Integraciones

> 🔖 **`DIS-R-006`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

En toda integración con sistemas legados o externos, la Capa Anticorrupción (ACL) se implementa de forma obligatoria mediante tres componentes coordinados dentro de `infrastructure/adapter/out/`:

```java
// 1. EL DTO DE RESPUESTA DEL TERCERO (Estructura ajena, con nombres de campos y formatos propios)
public class ReniecPersonaResponseXml {
    public String numDni;
    public String primerNombre;
    public String apePaterno;
    public String apeMaterno;
    public String fecNac; // Ejemplo: formato "dd/MM/yyyy" ajeno al estándar ONP
}

// 2. EL MAPPER TRADUCTOR (La verdadera Anti-Corruption Layer)
@Component
public class ReniecAclMapper {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DatosPersona toDomain(ReniecPersonaResponseXml response) {
        if (response == null) throw new RecursoNoEncontradoException("RENIEC retornó vacío");
        LocalDate fechaNac = LocalDate.parse(response.fecNac, FMT);
        return new DatosPersona(
            new Dni(response.numDni),
            response.primerNombre,
            response.apePaterno + " " + response.apeMaterno,
            fechaNac
        );
    }
}

// 3. EL ADAPTER DE SALIDA QUE CONSUME EL PUERTO DE DOMINIO Y EJECUTA LA TRADUCCIÓN
@Repository
@RequiredArgsConstructor
public class ReniecHttpAdapter implements ConsultaPersonaExternalPort {
    private final RestClient reniecClient;
    private final ReniecAclMapper aclMapper;

    @Override
    public DatosPersona consultarPorDni(Dni dni) {
        ReniecPersonaResponseXml response = reniecClient.get()
            .uri("/persona/{dni}", dni.valor())
            .retrieve()
            .body(ReniecPersonaResponseXml.class);
        return aclMapper.toDomain(response); // Aislamiento garantizado: el dominio solo ve DatosPersona
    }
}
```

---

## 6. Resiliencia Táctica en Comunicaciones Externas (*Design for Failure*)

> 🔖 **`DIS-R-007`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

Todo llamado por red desde un adaptador de infraestructura hacia un servicio externo o base de datos es inherentemente falible. Para evitar efectos dominó y colapsos en cascada dentro del clúster de Kubernetes, es mandatoria la implementación de tolerancia a fallos — **pero no todo lo hace `Resilience4j`**. El *Timeout* estricto (§6.1) es siempre obligatorio en cualquier estilo. El *Bulkhead* (§6.3) y el *Retry* se resuelven por defecto con Apache HttpClient 5 y Spring Retry — sin Resilience4j. El *Circuit Breaker* formal con Resilience4j (§6.2) es obligatorio solo en Microservicios, y excepcional con ADR en Monolito Modular.

> **Propiedad documental:** esta sección `§6` es el **documento dueño** de la resiliencia táctica en comunicaciones externas — umbrales de timeout, mecanismo de Bulkhead, política de Retry y condiciones de adopción de Circuit Breaker (`GOB-MAT-001`). `ARQ-R-004` (LIN-ARQ-001 §4.3) exige el *resultado* (aislamiento ante caídas de terceros del Estado) y delega aquí el mecanismo; `LIN-API-REST-001 §8.3` norma únicamente la respuesta REST ante el vencimiento (`504` / `codDetRespuesta 402`). Ningún otro documento publica valores de timeout propios: si un lineamiento necesita precisar umbrales para su dominio, los referencia desde `§6.1` y agrega solo la especificidad de su contexto.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ADAPTADOR DE SALIDA ONP (ReniecHttpAdapter / PagoExternalAdapter)           │
│                                                                             │
│  [Petición Saliente]                                                        │
│         │                                                                   │
│         ▼                                                                   │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ BULKHEAD (Aislamiento de Hilos)                                       │  │
│  │ Limita a máx. 20 hilos concurrentes hacia este proveedor externo.     │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     ▼                                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ CIRCUIT BREAKER (Resilience4j)                                        │  │
│  │ Evalúa ventana de 100 peticiones. Si >50% fallan -> APERTURA CIRCUITO │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     ▼                                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ TIMEOUT ESTRICTO                                                      │  │
│  │ Connection Timeout: 2 segundos / Read Timeout: 3 segundos máx.        │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
└─────────────────────────────────────┼───────────────────────────────────────┘
                                      ▼
                      [ RED EXTERIOR / SERVICIO RENIEC ]
```

### 6.1 Estrategia de Timeouts Finos

> 🔖 **`DIS-R-008`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

Queda **terminantemente prohibido** dejar clientes HTTP, JDBC o SOAP con *timeouts* por defecto (infinitos o > 30 segundos). En lugar de un rango genérico único, el timeout se configura según la **demanda concurrente** y la **criticidad en la ruta interactiva del ciudadano**:

| Categoría del Servicio Externo | Ejemplo en ONP | Perfil de Demanda | Connection Timeout | Read Timeout | Estrategia ante Fallo |
|---|---|---|---|---|---|
| **Alta demanda / Ruta crítica interactiva** | RENIEC, SAA (Token), validación DNI en ventanilla virtual | Muy alta (cientos de req/min); bloquea directamente al ciudadano en pantalla | `≤ 1.5-2s` | `2-3s` | *Fail-fast*: abortar rápido para liberar hilos; notificar en UI o consultar caché temporal si existe |
| **Demanda media / Consulta de negocio** | PIDE, consulta de deudas, padrones externos | Media; consultas durante la tramitación interna de un expediente | `≤ 2s` | `4-6s` | Reintento simple acotado (solo si es lectura idempotente, ver §6.4) |
| **Baja demanda / Proceso asíncrono o diferido** | SUNAT, PLAME, conciliaciones por lote | Baja concurrencia en tiempo real, ejecutado por *workers* en segundo plano | `≤ 3s` | `10-15s` | Mayor tolerancia; ante fallo persistente, derivar a cola o DLQ |

Todo adaptador configura explícitamente estos tres controles juntos con **Apache HttpClient 5** (`HttpComponentsClientHttpRequestFactory`), que en una sola configuración provee connection timeout, read timeout y el límite de conexiones por ruta (Bulkhead, §6.3).

### 6.2 Patrón Circuit Breaker (Cortacircuitos)

> 🔖 **`DIS-R-009`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

**`Resilience4j` (`resilience4j-spring-boot3`) no es el estándar por defecto en un Monolito Modular.** Su adopción está condicionada:

1. **Criterio primario (Microservicios — Estadio 3):** el Circuit Breaker formal es **obligatorio** cuando el sistema opera como microservicio (extracción validada según `ARQ-R-001` (LIN-ARQ-001 §2.1)). Con múltiples servicios llamándose por red, una máquina de estados formal con *fallback* es indispensable para evitar colapsos en cascada.
2. **Criterio de excepción en Monolito Modular:** un Monolito Modular puede adoptar Resilience4j **únicamente** para envolver el *Adapter* de salida hacia un proveedor externo que cumpla **ambas** condiciones simultáneamente: **(a)** volumetría masiva en ruta crítica interactiva (cientos de llamadas por minuto en la ruta directa del ciudadano, ej. RENIEC en ventanilla virtual), y **(b)** necesidad de corte automático sin intento de red (*fast fail*) porque el timeout (§6.1) + Bulkhead (§6.3) no bastan — bajo carga alta con el proveedor caído, los *slots* del pool se agotan igual esperando el timeout en cada intento.
   > **Lo que NO justifica el ADR:** necesitar Bulkhead o Retry — ambos ya están cubiertos por defecto (§6.3 y §6.4), sin Resilience4j. Esta adopción excepcional en Monolito Modular requiere **ADR aprobado por Arquitectura** y se justifica exclusivamente por la máquina de estados del Circuit Breaker.

Donde aplica (Microservicios siempre; Monolito Modular bajo ADR), el *Circuit Breaker* protege todo punto de salida hacia servicios de terceros (SUNAT, RENIEC, PIDE, pasarelas bancarias) con la siguiente máquina de estados obligatoria:

| Estado del Circuit Breaker | Comportamiento Táctico y Condición de Transición |
|---|---|
| **CERRADO (*Closed*)** | Funcionamiento normal. El tráfico fluye libremente. Si la tasa de fallos o lentitud (>3s) supera el **50% en una ventana móvil de 100 peticiones**, el cortacircuitos salta al estado *ABIERTO*. |
| **ABIERTO (*Open*)** | **Interrupción inmediata de llamadas por red.** Durante **30 segundos**, el adaptador rechaza las peticiones localmente y ejecuta la lógica de contingencia (*Fallback* — ej. retornar error amigable o dato en caché), protegiendo los hilos de la JVM de quedarse esperando. |
| **SEMI-ABIERTO (*Half-Open*)** | Al vencer los 30 segundos, el cortacircuitos permite el paso de **10 peticiones de prueba** hacia el servidor externo, evaluadas con **el mismo umbral de fallo del estado CERRADO (50%)**: si al menos 5 tienen éxito, el circuito se restablece a *CERRADO*; si continúan fallando, regresa a *ABIERTO* por otros 30 segundos. |

> **Por qué el mismo 50%, y no un umbral propio más estricto para semi-abierto.** `Resilience4j` (`CircuitBreakerConfig`) no expone un `failureRateThreshold` independiente por estado: el mismo umbral configurado evalúa tanto la ventana de 100 peticiones en *CERRADO* como las 10 peticiones de prueba en *SEMI-ABIERTO*. Exigir aquí un umbral distinto (ej. 8 de 10) obligaría a una implementación a medida fuera de la configuración estándar por anotación que rige el resto de este patrón — verificado construyendo `template-microservicio-java`, donde configurar 80% de éxito en semi-abierto habría requerido ese mismo umbral también en cerrado, contradiciendo la fila de arriba.

### 6.3 Patrón Bulkhead (Compartimentación)

El patrón *Bulkhead* aisla recursos en compartimentos para evitar que la lentitud o caída de un servicio secundario (ej. generador externo de reportes PDF) acapare todos los hilos del Tomcat/container del microservicio principal, tumbando la atención de los trámites core.

**Sin Bulkhead:** el contenedor tiene, digamos, 200 hilos disponibles para atender peticiones. Si el generador externo de reportes PDF se cae y cada llamada tarda 30 segundos en agotar su timeout, y llegan 200 solicitudes de reportes en ese lapso, **los 200 hilos del contenedor quedan ocupados esperando una respuesta que nunca llega** — incluidos los hilos que deberían atender trámites core que no tienen nada que ver con el generador de PDF, porque ya no queda ninguno libre. Un servicio secundario tumba todo el sistema.

**Con Bulkhead:** el cliente HTTP hacia el generador de PDF tiene su propio pool acotado (ej. máximo 15 conexiones). Al llegar la conexión número 16, la petición falla de inmediato en vez de consumir un hilo del contenedor a la espera del timeout completo. El resto de los hilos —y por tanto los trámites core— siguen atendiéndose con normalidad.

```java
@Configuration
public class ClienteHttpConfig {

    // Compartimento propio para el generador de reportes PDF (servicio secundario, no crítico)
    @Bean
    public RestClient reportesPdfClient() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(15);
        connManager.setDefaultMaxPerRoute(15); // máx. 15 conexiones simultáneas hacia ESTE proveedor
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        return RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)).build();
    }

    // Compartimento propio para RENIEC (ruta crítica interactiva) — independiente del anterior
    @Bean
    public RestClient reniecClient() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(50);
        connManager.setDefaultMaxPerRoute(50);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        return RestClient.builder().requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)).build();
    }
}
// Si reportesPdfClient se agota porque el proveedor está caído, reniecClient conserva
// sus 50 conexiones intactas: son compartimentos independientes, el problema de uno
// no se propaga al otro.
```

- **Implementación por defecto (sin Resilience4j):** el aislamiento queda resuelto en la configuración del cliente HTTP de infraestructura (`clienteRestFactory`) mediante `setMaxConnPerRoute` de **Apache HttpClient 5**, como en el ejemplo — un pool máximo acotado de conexiones por proveedor externo. Este es el mecanismo estándar en Monolito Modular; no requiere ADR.
- **`ThreadPoolBulkhead` / `SemaphoreBulkhead` de Resilience4j:** solo aplica bajo el mismo criterio de excepción del Circuit Breaker (§6.2) o de forma obligatoria en Microservicios.

### 6.4 Reintentos (Retry)

Para mitigar microcortes en lecturas idempotentes, se prefiere **Spring Retry** simple — no Resilience4j — como mecanismo por defecto en Monolito Modular. **Prohibido** reintentar automáticamente operaciones de escritura (`POST`, `PUT`) no idempotentes.

**Elegibilidad de reintento:** solo se reintentan fallos de **infraestructura** — timeout, error de conexión, `503`/`504`. **Nunca** se reintenta un `4xx` (la petición está mal formada o no autorizada; reintentarla no cambia el resultado) ni un `500` con cuerpo de error de negocio. Reintentar lo que no es transitorio solo añade latencia y carga innecesaria sobre un proveedor que ya está fallando.

**Mínimos y máximos por categoría (coherente con la matriz de `§6.1`):**

| Categoría (`§6.1`) | ¿Se reintenta? | Máx. intentos totales | Backoff |
|---|---|---|---|
| **Alta demanda / ruta crítica interactiva** (ej. RENIEC, SAA) | **No — Fail-fast.** El timeout ya es agresivo (`§6.1`); un reintento aquí duplica la carga justo cuando el proveedor está degradado y no hay margen de tiempo sin romper la experiencia interactiva del ciudadano. | 1 (sin reintento) | N/A |
| **Demanda media / consulta de negocio** (ej. PIDE, consulta de deudas) | Sí, si es lectura idempotente | **2** (1 inicial + 1 reintento) | Exponencial con *jitter*: `delay` inicial 300–500 ms, `multiplier` 2 |
| **Baja demanda / proceso asíncrono o diferido** (ej. SUNAT, PLAME por lote) | Sí | **3** (1 inicial + 2 reintentos) | Exponencial con *jitter*: `delay` inicial 1–2 s, `multiplier` 2; agotados los intentos, deriva a cola o DLQ (`§6.1`) |

**Regla de presupuesto:** la suma de todos los intentos y sus esperas **no puede exceder el Read Timeout declarado para esa categoría en `§6.1`** — el reintento existe para absorber un microcorte, no para convertir una operación de 3 segundos en una de 15.

```java
// Categoría "Demanda media" (ej. PIDE) — 2 intentos en total: 1 inicial + 1 reintento
@Retryable(
    retryFor = { java.net.SocketTimeoutException.class, org.springframework.web.client.ResourceAccessException.class },
    maxAttempts = 2,
    backoff = @Backoff(delay = 500, multiplier = 2))
public DatosPersona consultarDeuda(String dni) { ... }
```

El `Retry` de Resilience4j solo aplica bajo el mismo criterio de excepción del Circuit Breaker (§6.2) o de forma obligatoria en Microservicios.
