# Catálogo Oficial de Patrones y Fichas Técnicas de Arquitectura, Diseño Táctico y Programación en la ONP

**Código:** LIN-PAT-001  
**Versión:** 0.1.6  
**Fecha:** 2026-08-05  
**Autor:** Oficina de Tecnologías de la Información — ONP  
**Estado:** En revisión / Catálogo Institucional Transversal — pendiente de graduación a Vigente (`GOB-MAT-001`, Ciclo de vida documental)  
**Clasificación:** Catálogo normativo de toma de decisiones. Articula y consolida los criterios de selección para las decisiones de Nivel 1 (`LIN-ARQ-001`), Nivel 2 (`LIN-DIS-001`), Nivel 3 (`LIN-DEV-JAVA-001`) y dominios transversales (`LIN-BD-ORA-001`, `LIN-API-REST-001`, `LIN-BI-001`, `LIN-BUS-001`). De uso diario obligatorio para Arquitectos, Tech Leads y Desarrolladores.

> **Fuente única de códigos `PT`:** este catálogo es autoritativo para la asignación de códigos `PT01`–`PT16` y de las fichas `PAT-*`. Ningún documento puede asignar un código `PT` a un patrón distinto del registrado aquí. El índice de trazabilidad `PT → ficha → dueño normativo` se mantiene en `GOB-MAT-001`.

---

## Historial de versiones

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 0.1.0 – 0.1.4 | 2026-07-08 a 2026-07-10 | Arquitectura OTI | Versiones iniciales del catálogo de fichas de decisión. *(Detalle por versión no registrado — este historial se incorpora en v0.1.5.)* |
| 0.1.5 | 2026-08-05 | Arquitectura OTI | Corrige 4 fichas (`PAT-TOP-01`, `PAT-TOP-03`, `PAT-INT-04`, `PAT-DAT-03`) que usaban "Estadio 0" para legacy: la escala oficial de `LIN-ARQ-001 §2.1` es Estadio 1 = Legacy, 2 = Monolito Modular, 3 = Microservicios (`GOB-CHK-001` H1). Declara explícitamente al catálogo como fuente única de códigos `PT`, tras detectarse asignaciones contradictorias en el tablero de Brechas (`GOB-CHK-001` H6.3). Se incorpora este historial de versiones |
| 0.1.6 | 2026-08-17 | Arquitectura OTI | Incorpora las fichas **`PAT-K8S-01` (Sidecar — `PT17`)** y **`PAT-K8S-02` (Ambassador — `PT18`)**. La Familia 4 declaraba a `LIN-K8S-001` entre sus dueños normativos pero **no contenía ninguna ficha de los dos únicos patrones que ese lineamiento norma por sí mismo**: `LIN-K8S-001 §9.4` los identificaba con los códigos `PA12`/`PA13` del tablero de brechas `GOB-BRE-001`, que es un inventario de vacíos, no el catálogo oficial. Es el mismo caso ya resuelto en v0.1.5 para BFF, Facade y Gateway-Aggregation, que quedó a medias (`GOB-CHK-001` H26) |

---

## 1. Introducción y Guía de Uso del Catálogo

El presente catálogo transforma los lineamientos conceptuales de la Oficina de Tecnologías de la Información (OTI) en un **Checklist Ejecutivo de Decisión Diaria**. 

Cada vez que un Arquitecto de Software, Diseñador/Tech Lead o Desarrollador analiza el *Documento de Especificación de Requisitos Funcionales y No Funcionales* de un proyecto, debe consultar este catálogo para determinar qué componentes, estilos o patrones encender.

### 1.1 Regla de Oro del Criterio de Selección

> **Mandato de Gobierno (`PRA10` / Supremacía Normativa):**  
> 1. Si los requisitos del proyecto cumplen el **`✅ Criterio de Selección`** de una ficha, la adopción del patrón o componente es **obligatoria**.  
> 2. Si el proyecto cae en el **`❌ Criterio de Exclusión (Antipatrón)`**, queda **terminantemente prohibida** su implementación.  
> 3. Cualquier excepción o desviación respecto al criterio de selección formal requiere la redacción de un **ADR (*Architecture Decision Record*)** y la firma de aprobación de la Dirección de Arquitectura de la OTI.

---

## 2. Índice Rápido de Fichas por Familia Institucional

| Código de Ficha | Nombre del Patrón / Estilo | Nivel / Dominio | Dueño Normativo |
|---|---|---|---|
| **`PAT-TOP-01`** | Monolito Modular (*Estadio 2*) | Nivel 1 — Topología Macro | `LIN-ARQ-001` |
| **`PAT-TOP-02`** | Microservicios Cloud-Native (*Estadio 3*) | Nivel 1 — Topología Macro | `LIN-ARQ-001` |
| **`PAT-TOP-03`** | Strangler Fig (*PT10*) | Nivel 1 — Transición Legacy | `LIN-ARQ-001` |
| **`PAT-DIS-01`** | Arquitectura Hexagonal (*Ports & Adapters*) | Nivel 2 — Diseño Táctico | `LIN-DIS-001` |
| **`PAT-DIS-02`** | Arquitectura en Capas (*Layered Architecture*) | Nivel 2 — Diseño Táctico | `LIN-DIS-001` |
| **`PAT-DIS-03`** | Bounded Context & Agregados DDD | Nivel 2 — Modelado de Dominio | `LIN-DIS-001` |
| **`PAT-DIS-04`** | CQRS (*Command Query Responsibility Segregation — PT04*) | Nivel 2 — Persistencia y Consulta | `LIN-DIS-001` / `LIN-BUS-001` |
| **`PAT-INT-01`** | Backend for Frontend (*BFF — PT11*) | Nivel 2 — Interfaz y Presentación | `LIN-DIS-001` |
| **`PAT-INT-02`** | Gateway-Aggregation (*PT12*) | Nivel 2 — Agregación de Servicios | `LIN-DIS-001` / `LIN-API-REST-001` |
| **`PAT-INT-03`** | Facade Arquitectónico de Integración (*PT15*) | Nivel 2 — Integración Exterior | `LIN-DIS-001` |
| **`PAT-INT-04`** | Anti-Corruption Layer (*ACL — PT13*) | Nivel 2 — Integración y Legacy | `LIN-DIS-001` |
| **`PAT-INT-05`** | API Gateway y Publicación WSO2 (*PT05*) | Transversal — Seguridad y Red | `LIN-API-REST-001` / `LIN-ARQ-001` |
| **`PAT-RES-01`** | Circuit Breaker (*PT07 — Resilience4j*) | Nivel 2 — Resiliencia Táctica | `LIN-DIS-001` / `LIN-API-REST-001` |
| **`PAT-RES-02`** | Bulkhead (*PT08 — Aislamiento de Hilos*) | Nivel 2 / K8s — Resiliencia | `LIN-DIS-001` / `LIN-K8S-001` |
| **`PAT-K8S-01`** | Sidecar — Co-proceso de apoyo en el Pod (*PT17*) | Nivel 3 / K8s — Despliegue | `LIN-K8S-001` |
| **`PAT-K8S-02`** | Ambassador — Proxy de salida en el Pod (*PT18*) | Nivel 3 / K8s — Despliegue | `LIN-K8S-001` |
| **`PAT-MSG-01`** | Publisher/Subscriber en Kafka (*PT01*) | Transversal — Mensajería | `LIN-BUS-001` |
| **`PAT-MSG-02`** | Dead Letter Queue (*DLQ — PT02*) | Transversal — Resiliencia Asíncrona | `LIN-BUS-001` |
| **`PAT-MSG-03`** | Sagas Distribuidas (*PT09*) | Nivel 1 / Mensajería — Transacción | `LIN-ARQ-001` / `LIN-BUS-001` |
| **`PAT-DEV-01`** | Patrones GoF Tácticos (*PT14 — Adapter / Decorator / Strategy*) | Nivel 3 — Programación Java | `LIN-DEV-JAVA-001` / `LIN-DIS-001` |
| **`PAT-DEV-02`** | Filtro de Seguridad y Token SAA (`SaaTokenValidationFilter`) | Nivel 3 / Seguridad — Autenticación | `LIN-SEC-APP-001` / `LIN-DEV-JAVA-001` |
| **`PAT-DAT-01`** | Adapter Java para PL/SQL Legacy (`SimpleJdbcCall`) | Nivel 3 / BD — Acceso a Datos | `LIN-DEV-JAVA-001` / `LIN-BD-ORA-001` |
| **`PAT-DAT-02`** | Transactional Outbox Table (`EVT_OUTBOX`) | Transversal — Persistencia / Bus | `LIN-BD-ORA-001` / `LIN-BUS-001` |
| **`PAT-DAT-03`** | Change Data Capture (*CDC* Debezium / LogMiner) | Transversal — Integración de Datos | `LIN-BD-ORA-001` / `LIN-BUS-001` |
| **`PAT-BI-01`** | Arquitectura Medallón (*PT16* — Bronze, Silver, Gold) | Transversal — Explotación / BI | `LIN-BI-001` |

---

## 3. Familia 1: Topología y Estilos Arquitectónicos Macro (`LIN-ARQ-001`)

### Ficha PAT-TOP-01: Monolito Modular (*Estadio 2*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-TOP-01` (Nivel 1 — Topología Macro) |
| **Nombre** | **Monolito Modular (*Estadio 2*)** |
| **Capa / Dominio** | Topología de Despliegue e Interconexión de Sistemas (`ARQ-R-001` (LIN-ARQ-001 §2.1)) |
| **Descripción** | Aplicación desplegable en una sola unidad física en Kubernetes (`Pod`), pero organizada internamente en módulos estancos independientes (*Bounded Contexts*) con aislamiento estricto de código, esquemas de base de datos divididos por dominio y comunicación regulada en memoria. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• El sistema abarca entre 2 y 6 dominios funcionales fuertemente colaborativos (ej. Sistema Previsional Core que gestiona Expedientes, Aportes y Pensionistas en transacciones coordinadas).<br>• El equipo técnico asignado tiene menos de 15 desarrolladores y comparte un pipeline CI/CD unificado.<br>• Es un nuevo desarrollo que reemplaza un sistema legacy (*Estadio 1*) y busca alta modularidad sin asumir la complejidad operacional extrema ni la latencia de red de múltiples microservicios distribuidos. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Un único submódulo o función concentra más del 85% del tráfico o de la carga computacional y requiere escalar horizontalmente en K8s de forma aislada sin duplicar el resto del sistema (en ese caso $\rightarrow$ *Microservicio PAT-TOP-02*).<br>• Se permite que las clases de un paquete accedan libremente a clases internas o tablas `JPA` de otro paquete sin pasar por las interfaces de contrato `application.api`. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 + Spring Boot 3 (`Maven Multi-Module` organizado con `onp-common-domain` para primitivas transversales) sobre contenedor Linux Alpine en `containerd/K8s`. |
| **📖 Referencia Oficial** | `ARQ-R-001` (LIN-ARQ-001 §2.1) (Estadio 2) y `DIS-R-002` (LIN-DIS-001 §3) |

---

### Ficha PAT-TOP-02: Microservicios Cloud-Native (*Estadio 3*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-TOP-02` (Nivel 1 — Topología Macro) |
| **Nombre** | **Microservicios Cloud-Native (*Estadio 3*)** |
| **Capa / Dominio** | Topología de Despliegue Distribuido (`ARQ-R-001` (LIN-ARQ-001 §2.1)) |
| **Descripción** | Descomposición de un sistema en servicios pequeños, autónomos y altamente especializados, donde cada microservicio encapsula una única capacidad de negocio previsional, posee su propia base de datos exclusiva y se despliega y escala de forma 100% independiente en Kubernetes. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• El componente tiene requerimientos de escalabilidad masiva y latencia asimétrica respecto al resto de la institución (ej. Motor Actuarial de Cálculo de Pensiones en épocas de pago masivo o Consulta Ciudadana Web en picos masivos).<br>• La solución es mantenida por equipos de desarrollo independientes (*Two-Pizza Teams*) con ciclos de entrega de software (`Releases`) dispares.<br>• El servicio debe evolucionar tecnológicamente de forma aislada sin afectar la estabilidad ni reiniciar el clúster transaccional core. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se intenta crear un "nanoservicio" o un CRUD de 3 tablas que requiere hacer 5 llamadas REST sincrónicas a otros microservicios para completar una sola operación transaccional básica (antipatrón *Distributed Monolith*). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 + Spring Boot 3 / Spring Cloud o Go 1.22+ (si aplica alto rendimiento concurrente) en Pods K8s gestionados por API Gateway WSO2 y trazas OpenTelemetry. |
| **📖 Referencia Oficial** | `ARQ-R-001` (LIN-ARQ-001 §2.1) (Estadio 3, con los 6 criterios de extracción) y `ADR-003` |

---

### Ficha PAT-TOP-03: Patrón Strangler Fig (*PT10*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-TOP-03` / `PT10` (Nivel 1 — Transición y Modernización) |
| **Nombre** | **Patrón Strangler Fig (Higuera Estranguladora)** |
| **Capa / Dominio** | Estrategia de Migración y Despliegue Progresivo (`LIN-ARQ-001 §2.2`) |
| **Descripción** | Estrategia de modernización progresiva que consiste en crear una fachada o enrutador perimetral (`API Gateway / Proxy`) que desvía gradualmente las nuevas peticiones hacia los nuevos módulos construidos en Java 21 / K8s, estrangulando y reduciendo por fases el tráfico hacia el sistema legacy monolítico (JBoss / Oracle Forms) hasta apagarlo por completo. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Modernización de sistemas previsionales críticos legacy (*Estadio 1*) donde un reemplazo total "de un solo golpe" (*Big Bang*) representa un riesgo de interrupción inaceptable para el pago de pensiones o la atención ciudadana.<br>• Coexistencia obligatoria de largo plazo (>6 meses) entre la nueva arquitectura en contenedores y las bases de datos transaccionales heredadas. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• El sistema a reemplazar es pequeño, no tiene dependencias cruzadas complejas y puede reescribirse y migrarse por completo en un solo *sprint* de 3 semanas o durante una ventana de mantenimiento programada de fin de semana. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | API Gateway WSO2 / NGINX Ingress Controller en Kubernetes + Capas Anticorrupción (`ACL`) y sincronización `CDC/Debezium` con Oracle 19c. |
| **📖 Referencia Oficial** | `LIN-ARQ-001 §2.2` y `ADR-004` |

---

## 4. Familia 2: Estilos Tácticos y Modelado de Dominio (`LIN-DIS-001`)

### Ficha PAT-DIS-01: Arquitectura Hexagonal (*Ports & Adapters*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DIS-01` (Nivel 2 — Diseño Táctico Interno) |
| **Nombre** | **Arquitectura Hexagonal (*Ports & Adapters*)** |
| **Capa / Dominio** | Estructuración Interna del Módulo o Contenedor (`DIS-R-001` (LIN-DIS-001 §2.3)) |
| **Descripción** | Estilo de diseño táctico que aísla por completo la lógica de negocio previsional pura dentro del paquete `domain/`, comunicándola con el exterior exclusivamente a través de interfaces explícitas (*Puertos de Entrada `port.in` y de Salida `port.out`*), las cuales son implementadas en la periferia por *Adaptadores* técnicos (`infrastructure.in/out`). |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• El módulo pertenece al *Core Previsional* (Aportes, Pensiones, Expedientes, Liquidaciones, Tesorería) donde las reglas de negocio son complejas y estrictamente regidas por ley.<br>• El módulo interactúa con 3 o más sistemas externos heterogéneos (RENIEC, SUNAT, bancos, PIDE, JBoss legacy).<br>• El módulo se concibe para ser extraído o desacoplado en el futuro hacia un microservicio independiente (*Estadio 3*). |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• El módulo es un CRUD administrativo simple de catálogos auxiliares o tablas maestras planas sin reglas previsionales transaccionales (aplicar Hexagonal en un ABM simple genera sobreingeniería innecesaria).<br>• Se contamina la capa de dominio (`domain/`) incluyendo anotaciones `@Entity` o `@Table` de JPA o dependencias `@Autowired` de Spring. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 (`Records` inmutables para DTOs y Value Objects) + Spring Boot 3 con inyección por constructor (`@RequiredArgsConstructor`) en adaptadores. |
| **📖 Referencia Oficial** | `DIS-R-001` (LIN-DIS-001 §2.3) y `LIN-DEV-JAVA-001 §8` |

---

### Ficha PAT-DIS-02: Arquitectura en Capas Clásica (*Layered Architecture*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DIS-02` (Nivel 2 — Diseño Táctico Interno) |
| **Nombre** | **Arquitectura en Capas Clásica (*Layered Architecture*)** |
| **Capa / Dominio** | Estructuración Interna de Componentes de Soporte (`LIN-DIS-001 §2.2`) |
| **Descripción** | Estilo tradicional de estructuración interna en 3 o 4 capas apiladas unidireccionalmente: Presentación (`api`), Aplicación/Servicio (`service`) y Persistencia/Acceso a Datos (`repository`). |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Módulos de soporte técnico, mantenedores de catálogos generales, tablas paramétricas o cruds administrativos simples.<br>• Flujos de procesamiento lineal sin cálculos actuariales ni invariantes transaccionales cruzados entre múltiples tablas. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se desarrollan funcionalidades críticas del *Core Previsional* (ej. cálculo de devengados o aprobación de pensión).<br>• Se permite que un `@RestController` en `api` acceda directamente a un `JpaRepository` saltándose la capa transaccional `service`. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 + Spring Boot 3 (`@RestController -> @Service -> @Repository`) conectado a Oracle 19c. |
| **📖 Referencia Oficial** | `LIN-DIS-001 §2.2` |

---

### Ficha PAT-DIS-03: Bounded Context & Agregados DDD

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DIS-03` (Nivel 2 — Modelado de Dominio Táctico) |
| **Nombre** | **Bounded Context & Agregados DDD (*Domain-Driven Design*)** |
| **Capa / Dominio** | Modelado de Entidades y Consistencia Transaccional (`DIS-R-002` (LIN-DIS-001 §3)) |
| **Descripción** | Delimitación estricta del significado de los conceptos dentro de un límite lingüístico y transaccional (*Bounded Context*), organizando las clases de dominio en clústeres fuertemente cohesivos liderados por una única entidad soberana (*Raíz de Agregado / Aggregate Root*) que garantiza la consistencia ACID de todo su árbol de subentidades y *Value Objects*. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Sistemas con alta complejidad en sus reglas y lógica de negocio donde un mismo concepto (ej. `Expediente` o `Persona`) tiene reglas y transiciones de estado completamente distintas según el departamento que lo atiende (Mesa de Partes vs. Liquidaciones vs. Legal).<br>• Necesidad de proteger invariantes donde la modificación de una sub-entidad (ej. agregar una resolución a un expediente) debe revalidar y actualizar el estado global del trámite completo en una única transacción. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se modelan tablas relacionales planas sin comportamiento mutante (estilo *Active Record / Table Module*).<br>• Se intenta compartir la misma entidad o `@Entity` JPA entre varios *Bounded Contexts* rompiendo la soberanía de la Fuente Única de Verdad (`PRA10`). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21: `Records` obligatorios para todos los *Value Objects* inmutables (`MontoMonetario`, `Dni`, `Periodo`), y clases `Entity/AggregateRoot` con constructores de negocio estrictos. |
| **📖 Referencia Oficial** | `LIN-DIS-001 §3.2` |

---

### Ficha PAT-DIS-04: Patrón CQRS (*PT04*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DIS-04` / `PT04` (Nivel 2 — Persistencia Táctica y Consulta) |
| **Nombre** | **Segregación de Responsabilidades de Mando y Consulta (*CQRS*)** |
| **Capa / Dominio** | Arquitectura de Datos y Consulta de Alto Rendimiento (`DIS-R-004` (LIN-DIS-001 §4.2)) |
| **Descripción** | Patrón que separa físicamente el modelo de datos utilizado para procesar mutaciones y transacciones ACID de negocio (*Command Side — Oracle 19c*) del modelo de datos desnormalizado optimizado exclusivamente para lecturas, búsquedas rápidas o reportes (*Query Side — MongoDB / Redis / Elasticsearch*). |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Operaciones de consulta ciudadana web masiva donde buscar un *Expediente 360°* en Oracle requiere ejecutar *JOINs* relacionales pesados sobre 8+ tablas históricas degradando el rendimiento transaccional en horario pico.<br>• Requerimientos de búsqueda por texto libre difuso, facetas y filtros combinados instantáneos no viables en motores relacionales.<br>• Requerimiento de latencia sub-milisegundo (< 2ms) para validaciones frecuentes de identidad por DNI o estado de pensionista. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• El volumen de consultas y de escrituras es moderado y puede manejarse sin contención ni bloqueo dentro de una sola tabla o esquema relacional en Oracle con índices `B-Tree` o `Bitmap` adecuadamente construidos.<br>• El caso de uso no puede tolerar en absoluto la latencia de "sincronización eventual" (ej. en el momento exacto de un cargo en cuenta bancaria). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Oracle 19c (Escritura ACID) $\rightarrow$ *Transactional Outbox / Debezium CDC* $\rightarrow$ Apache Kafka $\rightarrow$ MongoDB (Documento 360°), Redis (Caché DNI) o Elasticsearch. |
| **📖 Referencia Oficial** | `DIS-R-004` (LIN-DIS-001 §4.2) y `LIN-BUS-001` |

---

## 5. Familia 3: Patrones de Interfaz, Agregación e Integración Exterior (`LIN-DIS-001 / LIN-API-REST-001`)

### Ficha PAT-INT-01: Backend for Frontend (*BFF — PT11*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-INT-01` / `PT11` (Nivel 2 — Interfaz y Presentación) |
| **Nombre** | **Backend for Frontend (*BFF*)** |
| **Capa / Dominio** | Orquestación de Presentación Pública (`DIS-R-005` (LIN-DIS-001 §5.1)) |
| **Descripción** | Capa de agregación y presentación ligera dedicada y construida a la medida exacta de una interfaz de usuario cliente específica (ej. *BFF Mesa de Partes Web Angular* vs. *BFF App Móvil ONP*), que consolida llamados a múltiples microservicios internos y reduce la carga del payload devolviendo solo lo que la vista requiere. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Una interfaz SPA Angular o aplicación móvil requiere realizar más de 3 peticiones REST por red dispersas para construir una sola pantalla de atención al ciudadano.<br>• Los servicios transaccionales internos devuelven DTOs con 80+ atributos técnicos o sensibles y el frontend solo necesita mostrar 10 en su tabla visual.<br>• Necesidad de adaptar protocolos entre el navegador (`HTTPS/REST/JSON`) y los servicios internos (`gRPC / Kafka / SOAP`).<br>• Necesidad de mediación de seguridad frente al API Manager WSO2 mediante el patrón **Token Handler** (`LIN-DIS-001 §5.1.1`): el BFF gestiona cookies `HttpOnly` con el frontend e inyecta el `Authorization: Bearer` hacia el core. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Existe una relación 1:1 simple donde la UI consume exactamente el mismo DTO transaccional que expone el servicio de dominio.<br>• Se introduce en el BFF lógica transaccional, mutaciones ACID de base de datos Oracle o reglas de cálculo de pensión (el BFF es **presentación y agregación pura**). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 + Spring Boot 3 (`pe.gob.onp.bff.*`) expuesto a través de API Gateway WSO2 y consumiendo APIs internas vía `RestClient` y Resilience4j. |
| **📖 Referencia Oficial** | `DIS-R-005` (LIN-DIS-001 §5.1)-5.1.1` y `LIN-FE-ANG-001` |

---

### Ficha PAT-INT-02: Gateway-Aggregation (*PT12*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-INT-02` / `PT12` (Nivel 2 — Agregación Interna) |
| **Nombre** | **Gateway-Aggregation** |
| **Capa / Dominio** | Lógica de Dispersión y Recopilación en Capa Aplicación (`LIN-DIS-001 §5.2`) |
| **Descripción** | Componente interno (`GatewayAggregator`) que intercepta una solicitud entrante en un módulo, realiza llamadas de dispersión concurrentes o secuenciales hacia múltiples repositorios de base de datos, puertos de dominio o servicios internos, combinando los resultados y devolviendo un único DTO consolidado al consumidor. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Un caso de uso de lectura o consulta interna que necesita combinar datos estáticos de 3 o más esquemas o repositorios internos independientes (ej. consultar datos del ciudadano, su último historial laboral y su estado en planillas para la pantalla de inicio del analista de trámites).<br>• Necesidad de paralelizar llamadas de lectura en hilos virtuales (`Virtual Threads`) para reducir el tiempo total de respuesta de una consulta compleja. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se utiliza la agregación para encadenar escrituras o modificaciones transaccionales sobre múltiples agregados DDD (en cuyo caso se debe utilizar una *Saga Distribuida `PAT-MSG-03`* o transacciones locales coordinadas). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 (`CompletableFuture` o Virtual Threads de Java 21 con `@Service` sin estado en `application/service/aggregator/`). |
| **📖 Referencia Oficial** | `LIN-DIS-001 §5.2` |

---

### Ficha PAT-INT-03: Facade Arquitectónico de Integración (*PT15*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-INT-03` / `PT15` (Nivel 2 — Integración Exterior) |
| **Nombre** | **Facade Arquitectónico de Integración** |
| **Capa / Dominio** | Adaptadores de Infraestructura Externa (`LIN-DIS-001 §5.3`) |
| **Descripción** | Componente estructural ubicado en el límite exterior de la infraestructura (`infrastructure/adapter/out/facade/`) que oculta y encapsula la complejidad técnica, heterogeneidad de protocolos, criptografía (`WS-Security / mTLS / XML Signature`) y verbosidad de un ecosistema heredado o externo detrás de un puerto de dominio Java limpio y conciso. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Integración con servicios de entidades externas del Estado (PIDE, RENIEC, SUNAT, SBS, Contraloría) o sistemas JBoss legacy que emplean protocolos SOAP complejos con autenticación y cifrado a nivel de mensaje (`WS-Security`).<br>• Escenarios donde para obtener un solo dato útil del tercero se requiere ejecutar internamente un saludo de sesión (`login/token`), una consulta de ticket y la descarga del payload XML en 3 pasos secuenciales por red. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se consume un servicio web REST moderno interno de la ONP que ya devuelve un JSON limpio, cumple el estándar `LIN-API-REST-001` y puede invocarse con una sola línea de `RestClient` directo sin transformación de protocolo. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 (`RestClient` / `JaxWsPortProxyFactoryBean`) con almacén de llaves institucionales (`KeyStore/TrustStore`) gestionado mediante `Secrets K8s / HashiCorp Vault`. |
| **📖 Referencia Oficial** | `LIN-DIS-001 §5.3` |

---

### Ficha PAT-INT-04: Anti-Corruption Layer (*ACL — PT13*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-INT-04` / `PT13` (Nivel 2 — Aislamiento y Traducción) |
| **Nombre** | **Anti-Corruption Layer (*Capa Anticorrupción — ACL*)** |
| **Capa / Dominio** | Integración Táctica e Interoperabilidad Legacy (`DIS-R-006` (LIN-DIS-001 §5.4)) |
| **Descripción** | Capa de traducción e impermeabilización transaccional que intercepta las respuestas o eventos provenientes de un sistema tercero o legacy, transformando sus modelos de datos arcaicos, nombres cripticos o formatos de fecha incompatibles hacia objetos puros del dominio moderno (`Records` de `domain/model/`), impidiendo que el modelo ajeno corrompa la semántica interna de la ONP. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• Consumo de servicios de terceros externos (RENIEC, SUNAT, bancos) donde las tablas o XML devuelven campos como `fec_nac_per` en formato `"dd/MM/yyyy"` que deben traducirse inmediatamente a `LocalDate fechaNacimiento` antes de tocar la lógica del negocio.<br>• Lectura o invocación de procedimientos almacenados heredados (*Legacy PL/SQL*) del Estadio 1. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• La comunicación ocurre entre dos módulos modernos (*Bounded Contexts*) del mismo Monolito Modular ONP que comparten el `Shared Kernel` (`onp-common-domain`) y exponen contratos DTO estandarizados por la OTI. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 (`Mappers` de traducción y adaptadores implementados dentro del paquete `infrastructure/adapter/out/acl/`). |
| **📖 Referencia Oficial** | `DIS-R-006` (LIN-DIS-001 §5.4) y `ARQ-R-004` (LIN-ARQ-001 §4.3) (ACL mandatoria en interoperabilidad gubernamental) |

---

### Ficha PAT-INT-05: API Gateway y Publicación WSO2 (*PT05*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-INT-05` / `PT05` (Transversal — Seguridad y Red) |
| **Nombre** | **API Gateway y API Manager (*WSO2 Platform*)** |
| **Capa / Dominio** | Frontera y Gobernanza de Exposición de Servicios (`API-R-001` (LIN-API-REST-001 §2.5)) |
| **Descripción** | Componente institucional único perimetral e interno que actúa como puerta de enlace, centralizando la exposición de todos los servicios REST, aplicando políticas obligatorias de seguridad perimetral (`OAuth2 / OIDC / Mutual TLS`), enrutamiento dinámico hacia Kubernetes, cuotas de tráfico (*Rate Limiting*) y analítica de consumo sin que el microservicio deba procesarlo en su código. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• Exposición de cualquier servicio REST o API hacia aplicaciones clientes front-end (Angular, Móvil), hacia otras instituciones del Estado (PIDE) o entre dominios internos institucionales.<br>• Necesidad de control de acceso, versionado de contratos (`v1`, `v2`), auditoría de peticiones y estrangulamiento de tráfico por consumidor para proteger la infraestructura. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se trata de comunicación interna de baja latencia dentro del mismo *Pod* en Kubernetes (ej. llamadas de clases locales) o entre contenedores colocalizados donde la penalización de un hop adicional de red por el Gateway no es justificable. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | **WSO2 API Manager / WSO2 Micro Integrator** homologado en `ADR-WSO2-001` e integrado al Sistema de Seguridad Institucional (`SAA / AD`). |
| **📖 Referencia Oficial** | `API-R-001` (LIN-API-REST-001 §2.5), `LIN-SEC-APP-001 §3` y `ADR-WSO2-001` |

---

## 6. Familia 4: Patrones de Resiliencia, Asincronía y Mensajería (`LIN-BUS-001 / LIN-K8S-001`)

### Ficha PAT-RES-01: Patrón Circuit Breaker (*PT07*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-RES-01` / `PT07` (Nivel 2 / Resiliencia Táctica) |
| **Nombre** | **Circuit Breaker (*Cortacircuitos Resilience4j*)** |
| **Capa / Dominio** | Tolerancia a Fallos en Adaptadores de Salida (`DIS-R-009` (LIN-DIS-001 §6.2)) |
| **Descripción** | Mecanismo de protección que supervisa continuamente las llamadas por red hacia servicios externos o bases de datos. Si detecta que la tasa de fallos o latencia supera el 50% en una ventana de 100 peticiones, "abre el circuito" cortando las llamadas salientes durante 30 segundos y ejecutando una respuesta de contingencia inmediata (*Fallback*) para evitar agotar los hilos de la JVM. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• **Microservicios (Estadio 3):** toda llamada por red saliente (`HTTP / REST / SOAP / JDBC`) hacia otro servicio o sistema externo.<br>• **Monolito Modular:** solo con **ADR aprobado**, y únicamente cuando el adaptador de salida cumple **ambas** condiciones simultáneamente: (a) volumetría masiva en ruta crítica interactiva, y (b) necesidad de corte automático sin intento de red (*fast fail*) porque el timeout + pool de conexiones no basta. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Llamadas a métodos en memoria dentro del mismo módulo o invocaciones a clases transaccionales locales donde no hay I/O de red involucrado.<br>• **En Monolito Modular, sin ADR aprobado** — no es el estándar por defecto fuera de Microservicios; Bulkhead y Retry se resuelven por defecto sin Resilience4j (ver `PAT-RES-02`). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | **Resilience4j Spring Boot Starter** configurado mediante `application.yml` o anotaciones `@CircuitBreaker(name = "reniecService", fallbackMethod = "fallbackReniec")`. |
| **📖 Referencia Oficial** | `DIS-R-009` (LIN-DIS-001 §6.2) y `LIN-API-REST-001` |

---

### Ficha PAT-RES-02: Patrón Bulkhead (*PT08*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-RES-02` / `PT08` (Nivel 2 / Resiliencia en Kubernetes) |
| **Nombre** | **Bulkhead (*Aislamiento de Compartimentos / Hilos*)** |
| **Capa / Dominio** | Compartimentación de Recursos en la JVM (`LIN-DIS-001 §6.3`) |
| **Descripción** | Aislamiento y particionamiento del <i>pool</i> de hilos o conexiones concurrentes asignados a la atención de un servicio externo secundario lentificado (ej. generación externa de PDF o consulta a servicio bancario lento), garantizando que si dicho servicio colapsa, solo agote su pequeño *pool* asignado dejando el resto de la aplicación intacto. **En Monolito Modular, esto es el comportamiento por defecto de `setMaxConnPerRoute` en Apache HttpClient 5 — no requiere Resilience4j ni ADR.** |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Por defecto (sin Resilience4j, sin ADR):** todo adaptador de salida en Monolito Modular configura `setMaxConnPerRoute` en su `RestClient`/`HttpClient` — este es el mecanismo estándar.<br>**`ThreadPoolBulkhead`/`SemaphoreBulkhead` de Resilience4j se DEBE usar obligatoriamente solo si:**<br>• **Microservicios (Estadio 3):** siempre.<br>• **Monolito Modular:** únicamente bajo el mismo ADR de excepción que habilita Circuit Breaker (`PAT-RES-01`). |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar Resilience4j Bulkhead cuando:**<br>• El aislamiento ya está resuelto por `setMaxConnPerRoute` de HttpClient 5 y no existe ADR aprobado para la excepción.<br>• El microservicio es altamente especializado, tiene una sola tarea y se ejecuta en un *Pod* dedicado de K8s donde la compartimentación ya la garantiza el límite de CPU/Memoria del contenedor del orquestador (`LIN-K8S-001`). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | **Por defecto:** Apache HttpClient 5 (`setMaxConnPerRoute`). **Bajo ADR o en Microservicios:** Resilience4j `ThreadPoolBulkhead` / `SemaphoreBulkhead` con máximos concurrentes estrictos y colas acotadas (`maxThreadPoolSize = 15`). |
| **📖 Referencia Oficial** | `LIN-DIS-001 §6.3` y `LIN-K8S-001` |

---

### Ficha PAT-K8S-01: Patrón Sidecar (*PT17*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-K8S-01` / `PT17` (Nivel 3 / K8s — Patrón multi-contenedor en el Pod) |
| **Nombre** | **Sidecar (*Co-proceso de Apoyo y Observabilidad*)** |
| **Capa / Dominio** | Composición del Pod en Kubernetes (`LIN-K8S-001 §9.4.A`) |
| **Descripción** | Contenedor secundario adjunto al contenedor principal del Pod que extiende sus capacidades —recolección de bitácoras, proxy, sincronización de secretos— sin modificar el código de la aplicación. Comparte red y volúmenes con el contenedor principal. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Solo en dos escenarios:**<br>• **Caja negra / COTS / legacy no-Java** que escribe bitácoras en archivo y no soporta OTLP: sidecar ligero (Fluent Bit) sobre `emptyDir` compartido, reenviando al OTEL Collector centralizado.<br>• **Malla de servicios**, cuando Plataforma la habilite formalmente: el proxy lo inyecta la infraestructura, no el equipo de desarrollo. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **Prohibido en aplicaciones Java 21 / Spring Boot 3.** El SDK de OpenTelemetry embebido emite telemetría por OTLP directamente al colector centralizado (`LIN-OBS-001 §9.1`); un sidecar por pod duplica memoria y satura la red sin aportar nada. **La regla por defecto en ONP es 1 Pod = 1 contenedor de negocio.** |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Fluent Bit (bitácoras de caja negra). Envoy/Istio queda reservado a la malla institucional cuando exista. |
| **📖 Referencia Oficial** | `LIN-K8S-001 §9.4.A` |

---

### Ficha PAT-K8S-02: Patrón Ambassador (*PT18*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-K8S-02` / `PT18` (Nivel 3 / K8s — Patrón multi-contenedor en el Pod) |
| **Nombre** | **Ambassador (*Proxy de Salida del Pod*)** |
| **Capa / Dominio** | Composición del Pod en Kubernetes (`LIN-K8S-001 §9.4.B`) |
| **Descripción** | Proxy de red local dentro del Pod que media todo el tráfico saliente de la aplicación hacia sistemas externos, asumiendo mTLS, cabeceras, timeouts y reintentos fuera del proceso de negocio. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Un único escenario:** contenerización de monolitos heredados **no-Java** (JBoss, WebLogic, C++) en el marco de Strangler Fig (`LIN-ARQ-001 §2.2`), cuyo código no puede modificarse para incorporar resiliencia o seguridad moderna. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **Prohibido en aplicaciones Java 21 / Spring Boot 3.** La resiliencia saliente se resuelve dentro de la JVM según `DIS-R-007` (LIN-DIS-001 §6), que es el documento dueño: Timeout y Bulkhead siempre con Apache HttpClient 5, Retry con Spring Retry, y Circuit Breaker con Resilience4j **solo** en Microservicios o bajo ADR. Delegar esas políticas a un proxy de red las duplica y las saca del control del equipo. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Envoy o WSO2 Microgateway ligero, únicamente en el escenario de excepción. |
| **📖 Referencia Oficial** | `LIN-K8S-001 §9.4.B` y `DIS-R-007` (LIN-DIS-001 §6) |

---

### Ficha PAT-MSG-01: Publisher/Subscriber en Kafka (*PT01*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-MSG-01` / `PT01` (Transversal — Mensajería y Eventos) |
| **Nombre** | **Publisher/Subscriber (*Pub/Sub en Apache Kafka*)** |
| **Capa / Dominio** | Comunicación Asíncrona Desacoplada (`ADR-012` / `LIN-BUS-001`) |
| **Descripción** | Patrón de comunicación en el cual un productor de datos publica un evento de dominio inmutable en un *tópico* de Apache Kafka sin conocer la identidad ni la cantidad de sistemas que lo consumen. Los consumidores (*Subscribers*) leen los eventos de manera asíncrona y a su propio ritmo de procesamiento. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Comunicación entre dominios heterogéneos o *Bounded Contexts* separados donde la operación desencadenada no requiere que el usuario espere el resultado en la pantalla para continuar (ej. *Aporte Registrado* $\rightarrow$ notificar a Tesorería, recalcular puntaje del ciudadano y generar acuse en segundo plano).<br>• Necesidad de absorber picos masivos de tráfico transaccional amortiguando las escrituras en un búfer durable antes de impactar las tablas relacionales de Oracle (`Buffer/Load Leveling`). |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• La operación es estrictamente sincrónica de lectura o consulta en tiempo real donde la interfaz web o el ciudadano requiere el dato exacto al instante (ej. consulta del saldo actual de un pensionista en pantalla o validación de vigencia de DNI). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | **Apache Kafka Clúster Institucional** con formato de cabeceras y payload mandatorio estándar **CloudEvents v1.0 JSON** + `Spring Kafka Listener/Template`. |
| **📖 Referencia Oficial** | `ADR-012`, `LIN-BUS-001` y `LIN-DIS-001 §3.1` |

---

### Ficha PAT-MSG-02: Dead Letter Queue (*DLQ — PT02*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-MSG-02` / `PT02` (Transversal — Resiliencia Asíncrona) |
| **Nombre** | **Dead Letter Queue (*Cola de Mensajes Muertos — DLQ*)** |
| **Capa / Dominio** | Gestión de Errores y Tolerancia a Fallos en Eventos (`LIN-BUS-001`) |
| **Descripción** | Tópico o cola secundaria especializada de retención en Apache Kafka hacia la cual son desviados automáticamente los eventos que un servicio consumidor no logró procesar exitosamente después de agotar su política finita de reintentos (*Retries*), preservando la secuencia del tópico principal sin bloquear el procesamiento del resto de mensajes de otros ciudadanos. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• **Todo consumidor asíncrono (*Kafka Consumer/Listener*)** en sistemas de la ONP que procese eventos financieros, previsionales o de expedientes donde un registro defectuoso o una indisponibilidad temporal de Oracle no debe descartar el dato ni paralizar el hilo del *Consumer Group*. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Se consumen flujos de telemetría, logs efímeros o métricas de corto plazo donde la pérdida de un paquete individual no altera la contabilidad ni la legalidad institucional. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Spring Kafka `DeadLetterPublishingRecoverer` + Tópico con sufijo institucional `.dlq` en minúscula (ej. `aportes.cuenta.actualizada.dlq`, siguiendo la convención de nomenclatura de tópicos de dominio `LIN-BUS-001 §6.1` — sin el prefijo `onp.`, reservado exclusivamente a tópicos Saga en `LIN-BUS-001 §9.4`) monitoreado con alertas de SRE en Kibana/Grafana. |
| **📖 Referencia Oficial** | `LIN-BUS-001 §6.1, §8.5–8.7` |

---

### Ficha PAT-MSG-03: Sagas Distribuidas (*PT09*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-MSG-03` / `PT09` (Nivel 1 / Mensajería Transaccional) |
| **Nombre** | **Patrón Saga Distribuida (*Orquestada o Coreografiada*)** |
| **Capa / Dominio** | Coordinación Transaccional entre Microservicios (`ARQ-R-003` (LIN-ARQ-001 §3.3)) |
| **Descripción** | Secuencia coordinada de transacciones locales independientes en múltiples microservicios o *Bounded Contexts*, donde cada paso actualiza una base de datos local y publica un evento. Si un paso posterior falla por reglas de negocio, la Saga ejecuta transacciones de **compensación en reversa** para deshacer limpiamente los cambios previos ya confirmados, manteniendo la consistencia eventual. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Flujos de negocio transaccionales de largo alcance que cruzan múltiples microservicios o dominios de base de datos Oracle aislados donde el bloqueo distribuido (`2-Phase Commit / 2PC`) no es viable ni soportado en arquitectura Cloud-Native.<br>• *Ejemplo en ONP:* Flujo de Liquidación de Jubilación (Paso 1: Bloquear Reserva en Tesorería $\rightarrow$ Paso 2: Generar Resolución Legal $\rightarrow$ Paso 3: Si falla Resolución Legal, ejecutar *Compensación Liberar Reserva en Tesorería*). |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Toda la transacción ocurre dentro del mismo módulo del Monolito Modular y sobre el mismo esquema de base de datos Oracle transaccional donde un simple `@Transactional` (ACID nativo) garantiza la consistencia sin complejidad distribuida. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Orquestación mediante **Spring StateMachine / Apache Camel** o Coreografía sobre **Apache Kafka (`CloudEvents`) + Outbox Table** con idempotencia estricta por `X-Request-ID`. |
| **📖 Referencia Oficial** | `ARQ-R-003` (LIN-ARQ-001 §3.3) (Saga con Transactional Outbox) y `LIN-BUS-001 §9` |

---

## 7. Familia 5: Patrones de Programación, Persistencia y Analítica (`LIN-DEV-JAVA-001 / LIN-BD-ORA-001 / LIN-BI-001`)

### Ficha PAT-DEV-01: Patrones GoF Tácticos (*PT14*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DEV-01` / `PT14` (Nivel 3 — Programación Java 21) |
| **Nombre** | **Patrones GoF Tácticos (*Strategy / Factory / Adapter / Decorator*)** |
| **Capa / Dominio** | Estructuración y Diseño de Clases Java (`LIN-DEV-JAVA-001 §8`) |
| **Descripción** | Conjunto de patrones de diseño orientados a objetos (*Gang of Four*) obligatorios para eliminar bloques condicionales gigantescos (`switch-case` interminables), gestionar instanciaciones complejas e interponer adaptadores que separen la lógica de negocio de las implementaciones técnicas. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• **Patrón Strategy:** Cuando un cálculo previsional tiene múltiples variantes o algoritmos alternativos según el régimen (ej. `CalculadoraPensionStrategy` implementada por `Regimen19990Strategy`, `Regimen20530Strategy`, `Regimen25897Strategy`).<br>• **Patrón Factory:** Para la creación instanciada de objetos de dominio o agregados complejos que requieren validar invariantes al nacer.<br>• **Patrón Decorator:** Para agregar capacidades transversales (ej. auditoría fina o métricas personalizadas) sobre un servicio existente sin alterar su código base. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• El caso de uso es lineal, directo y no tiene variaciones algorítmicas (crear una jerarquía de `Strategy` con una sola implementación posible es sobreingeniería por especulación *YAGNI*). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 (`Sealed Classes / Interfaces`, `Pattern Matching para Switch` de Java 21 e inyección por lista o mapa de beans en Spring Boot (`Map<String, PensionStrategy>`)). |
| **📖 Referencia Oficial** | `LIN-DEV-JAVA-001 §8` |

---

### Ficha PAT-DEV-02: Filtro y Validación de Token SAA (`SaaTokenValidationFilter`)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DEV-02` (Nivel 3 / Seguridad — Autenticación en Aplicación) |
| **Nombre** | **Filtro de Validación y Contexto SAA (*SaaTokenValidationFilter*)** |
| **Capa / Dominio** | Seguridad de Endpoints REST en Spring Boot (`SEC-R-002` (LIN-SEC-APP-001 §8.3)) |
| **Descripción** | Patrón e implementación de filtro de seguridad en Spring Security (`OncePerRequestFilter`) que intercepta toda petición HTTP entrante al contenedor de la aplicación, extrae el token de autorización del header `Authorization: Bearer <token>`, valida criptográficamente su autenticidad e inyecta el contexto del usuario institucional o ciudadano (`SecurityContextHolder`) disponible para los controladores. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• **Toda aplicación o microservicio Spring Boot de la ONP** que exponga endpoints REST/HTTP protegidos y reciba tráfico desde el API Gateway WSO2 o front-ends institucionales.<br>• Queda **terminantemente prohibido** que una aplicación desarrolle su propia tabla de usuarios, contraseñas o genere tokens JWT paralelos fuera de la delegación al SAA/WSO2. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Endpoints explícitamente catalogados como públicos o de salud operacional (`/actuator/health`, `/actuator/info` o `Liveness/Readiness` de Kubernetes). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Spring Security 6+ sobre Java 21 integrado con cliente oficial o validación OIDC/JWT hacia **SAA / WSO2 Identity Server**. |
| **📖 Referencia Oficial** | `SEC-R-002` (LIN-SEC-APP-001 §8.3) (fuente autoritativa del filtro; no vive en `LIN-DEV-JAVA-001`) |

---

### Ficha PAT-DAT-01: Adapter Java para PL/SQL Legacy (`SimpleJdbcCall`)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DAT-01` (Nivel 3 / Base de Datos — Integración Legacy) |
| **Nombre** | **Adapter Java para PL/SQL Legacy (*SimpleJdbcCall*)** |
| **Capa / Dominio** | Persistencia e Interoperabilidad con Bases de Datos (`LIN-BD-ORA-001 §6`) |
| **Descripción** | Patrón técnico que estandariza la invocación segura, robusta y performante de Procedimientos Almacenados (`Stored Procedures / Functions`) y Paquetes `PL/SQL` heredados en Oracle 19c/11g desde código Java 21, aislando los tipos de datos nativos de Oracle (`ARRAY`, `REF CURSOR`, `CLOB`) dentro de un adaptador exclusivo (`infrastructure/adapter/out/persistence/plsql/`). |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Invocación obligatoria de lógica previsional heredada transitoriamente alojada en procedimientos `PL/SQL` técnicos catalogados como permitidos o en proceso de migración de largo plazo según `BD-R-001` (LIN-BD-ORA-001 §6.0).<br>• Procesamiento por lotes o consultas donde la salida sea un `REF CURSOR` relacional masivo que debe mapearse hacia `Records` Java. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Desarrollo de **nueva lógica de negocio transaccional** (prohibida terminantemente en PL/SQL en proyectos nuevos; toda nueva regla debe programarse en Java 21 en la capa `domain/`).<br>• Acceso a tablas relacionales modernas donde `Spring Data JPA / Hibernate` o `JdbcTemplate` directo resuelve el CRUD de forma estándar. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Java 21 + **Spring `SimpleJdbcCall` / `SqlParameterSource`** con gestión estricta de conexiones del *Pool HikariCP* (`close()` explícito de cursores). |
| **📖 Referencia Oficial** | `BD-R-001` (LIN-BD-ORA-001 §6.0) y `LIN-DEV-JAVA-001 §8` |

---

### Ficha PAT-DAT-02: Transactional Outbox Table (`EVT_OUTBOX`)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DAT-02` (Transversal — Persistencia / Bus de Eventos) |
| **Nombre** | **Transactional Outbox Table (*Bandeja de Salida Transaccional*)** |
| **Capa / Dominio** | Garantía de Entrega de Eventos y Consistencia ACID (`LIN-BD-ORA-001 / LIN-BUS-001`) |
| **Descripción** | Patrón arquitectónico que previene la pérdida o la condición de carrera al publicar eventos asíncronos hacia Apache Kafka. En lugar de enviar el mensaje a Kafka por red directamente desde el servicio Java (lo cual falla si la transacción de base de datos luego hace *rollback*), el servicio inserta el evento en la tabla relacional local `EVT_OUTBOX` **dentro de la misma transacción ACID de Oracle** que modifica el dato del negocio. Un proceso *Relay/CDC* posterior lee la tabla y lo publica de forma segura al bus con garantía *At-Least-Once*. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• **Todo caso de uso transaccional en Oracle (`@Transactional`)** que como consecuencia de su éxito deba publicar o emitir un evento hacia Apache Kafka o el bus de servicios para notificar a otros subdominios (ej. *Aporte Registrado*, *Expediente Aprobado*, *Resolución Emitida*). |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• El servicio actúa únicamente como un <i>proxy</i> o pasarela sin persistencia local de datos en Oracle (ej. un servicio de pasarela que solo recibe un REST y publica un evento directo en Kafka sin transacción local previa). |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | Oracle 19c (`EVT_OUTBOX` con índices por estado `PENDIENTE/ENVIADO`) + Spring Boot JPA/JdbcTemplate + **Debezium CDC o Polling Scheduled Relay**. |
| **📖 Referencia Oficial** | `LIN-BD-ORA-001 §3.10` (DDL canónico), `LIN-BUS-001 §7.3` (relevo) y `DIS-R-004` (LIN-DIS-001 §4.2) |

---

### Ficha PAT-DAT-03: Change Data Capture (*CDC* Debezium / LogMiner)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-DAT-03` (Transversal — Integración de Datos en Tiempo Real) |
| **Nombre** | **Change Data Capture (*Captura de Cambios de Datos — CDC*)** |
| **Capa / Dominio** | Replicación Transaccional No Invasiva (`LIN-BD-ORA-001 / LIN-BI-001`) |
| **Descripción** | Mecanismo de integración a nivel de motor de base de datos que lee y captura los cambios (`INSERT, UPDATE, DELETE`) directamente desde los registros binarios transaccionales (`Redo Logs / LogMiner / XStream` en Oracle) sin ejecutar consultas ni alterar las tablas operativas, convirtiendo las mutaciones en eventos continuos transmitidos hacia Apache Kafka o el Data Lakehouse. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar si el análisis de requisitos determina:**<br>• Replicación o sincronización en tiempo real desde bases de datos relacionales transaccionales críticas de Oracle hacia stores de lectura NoSQL de *CQRS (`PAT-DIS-04`)* o hacia la capa *Bronze* de analítica (`PAT-BI-01`).<br>• Extracción no invasiva de datos transaccionales desde sistemas heredados (*Estadio 1*) que no pueden ser modificados en código para emitir eventos de aplicación. |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• No se cuenta con la validación o autorización formal de los Administradores de Base de Datos (`DBA`) respecto a la sobrecarga computacional o el licenciamiento de `LogMiner / Supplemental Logging` en el servidor Oracle transaccional de producción. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | **Debezium Oracle Connector + Kafka Connect Clúster** conectado hacia Apache Kafka Institucional e inyectando en MinIO/Iceberg. |
| **📖 Referencia Oficial** | `LIN-BD-ORA-001`, `LIN-BI-001` y `DIS-R-004` (LIN-DIS-001 §4.2) |

---

### Ficha PAT-BI-01: Arquitectura Medallón (*PT16*)

| Campo | Especificación Normativa ONP |
|---|---|
| **Código** | `PAT-BI-01` / `PT16` (Transversal — Explotación de Datos y BI) |
| **Nombre** | **Arquitectura Medallón (*Capa Bronze, Silver y Gold*)** |
| **Capa / Dominio** | Data Lakehouse y Gobernanza Analítica (`LIN-BI-001 §4`) |
| **Descripción** | Estructuración del almacenamiento analítico de datos en tres zonas de madurez y calidad progresiva: **Bronze (Cruda/Ingesta Inmutable)**, **Silver (Limpieza, Depuración y Modelo Relacional Conforme)** y **Gold (Agregada, Marts Dimensional para Explotación y BI)** sobre formatos abiertos de alto rendimiento. |
| **✅ Criterio de Selección<br>*(¿Cuándo usar en ONP?)*** | **Se DEBE usar obligatoriamente si el análisis de requisitos determina:**<br>• **Toda plataforma, pipeline de ingesta o solución de analítica de datos, inteligencia de negocios (`BI`) o reportería institucional masiva** desarrollada sobre el ecosistema *Data Lakehouse* de la OTI (`MinIO / Apache Iceberg / Spark / Trino`). |
| **❌ Criterio de Exclusión<br>*(¿Cuándo NO usar?)*** | **NO usar cuando:**<br>• Bases de datos o esquemas netamente transaccionales operativos (`OLTP`) de aplicaciones en vivo en Oracle 19c donde el diseño debe regirse por la Tercera Forma Normal (3NF) o modelos de dominio DDD. |
| **🛠️ Stack / Herramienta<br>Homologada en ONP** | **MinIO (Object Store)** + **Apache Iceberg / Parquet** con catálogos de versionado **Nessie**, procesados por **Apache Spark / Airflow** y consultados mediante **Trino SQL**. |
| **📖 Referencia Oficial** | `LIN-BI-001 §4` y `Estándares de Tecnología v2.0 §9` |

---

## 8. Matriz de Trazabilidad Cruzada y Dependencias

Para finalizar el gobierno de selección, todo proyecto debe verificar las dependencias cruzadas entre fichas:

| Si seleccionas el Patrón / Componente... | Debes verificar o encender obligatoriamente... | Justificación Táctica / Arquitectónica |
|---|---|---|
| **`PAT-TOP-02` (Microservicios K8s)** | `PAT-INT-05` (API Gateway WSO2) + `PAT-RES-01` (Circuit Breaker) | Un microservicio sin API Gateway queda expuesto y sin gobierno de red, y sin Circuit Breaker causará fallas en cascada en el clúster K8s. |
| **`PAT-TOP-03` (Strangler Fig)** | `PAT-INT-04` (Capa Anticorrupción ACL) | La transición progresiva exige aislar las estructuras legacy de las nuevas entidades en Java 21. |
| **`PAT-DIS-01` (Hexagonal)** | `PAT-DEV-01` (Patrones GoF) + `PAT-DIS-03` (DDD / Records) | Los puertos de entrada/salida y el dominio puro requieren construirse con Records inmutables y adaptadores limpios. |
| **`PAT-DIS-04` (CQRS)** | `PAT-DAT-02` (Outbox Table) o `PAT-DAT-03` (CDC Debezium) + `PAT-MSG-01` (Kafka) | Segregar lecturas de escrituras en bases de datos separadas es imposible de mantener consistente sin propagación por eventos transaccionales. |
| **`PAT-INT-01` (BFF)** | `PAT-DEV-02` (Filtro SAA) + `PAT-INT-02` (Gateway-Aggregation) | El BFF consolida llamadas hacia el backend (`Aggregator`) asegurando cada sesión con el token de identidad SAA. |
| **`PAT-BI-01` (Medallón Iceberg)** | `PAT-DAT-03` (CDC Debezium) + `PAT-MSG-01` (Kafka CloudEvents) | La ingesta hacia la capa Bronze requiere una captura continua y no invasiva de eventos transaccionales. |
