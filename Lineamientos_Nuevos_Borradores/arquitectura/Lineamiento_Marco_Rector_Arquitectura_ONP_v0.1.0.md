# Lineamiento Marco Rector de Arquitectura de Software en la ONP

**Código:** LIN-ARQ-001  
**Versión:** 0.1.7  
**Fecha:** 2026-07-14  
**Autor:** Oficina de Tecnologías de la Información — ONP  
**Estado:** Vigente / Estándar de Nivel 1  
**Clasificación:** Marco rector institucional. Documento supremo en la jerarquía del modelo de 3 niveles de la OTI. Establece las decisiones macro, Hacia Dónde y Por Qué de la arquitectura de sistemas. Todo lineamiento táctico (Nivel 2) y de implementación o código (Nivel 3) está supeditado a las reglas y directivas declaradas en el presente documento.

---

## 1. Marco General y Gobierno

### 1.1 Propósito y Alcance

El presente Marco Rector de Arquitectura de Software define las políticas generales, la topología de sistemas y los estándares de alto nivel para el ecosistema tecnológico de la Oficina de Normalización Previsional (ONP). Su aplicación es **estrictamente obligatoria** para:
1. Proyectos de desarrollo de software nuevo (internos o contratados).
2. Proyectos de modernización y refactorización de sistemas existentes o legados.
3. Adquisición y contratación de servicios de desarrollo de software a medida (Fábricas de Software y Terceros).

**Relación con documentos institucionales previos:**

| Documento previo | Cómo se relaciona con este Marco Rector |
|---|---|
| Lineamiento de Estándares de Tecnología v2.0 | Define el stack tecnológico obligatorio: Java + Spring Boot + Maven para backend nuevo. Este Marco Rector respeta esa decisión y la operacionaliza en los tres niveles de la jerarquía documental. |
| Lineamiento sobre Arquitectura Patrón de Apps y BD v1.0 | Define estilos y fichas de patrones. Este Marco Rector lo extiende (añade Monolito Modular, Hexagonal, DDD) y aporta la profundidad de implementación que el documento previo no tenía — desarrollada en `LIN-DIS-001` y `LIN-DEV-JAVA-001`. |
| Arquitectura de referencia v0.1 (Dic. 2025) | Incorpora patrones adicionales (CQRS, Saga, Anti-Corruption Layer, Strangler Fig). Este Marco Rector los integra dentro de un marco de uso coherente y normado. |

**Audiencia principal:** Arquitectos de TI de ONP.

**Audiencia secundaria:** Líderes técnicos y desarrolladores senior contratados (Locadores), Fábricas de Software. El personal contratado recibe el conjunto de lineamientos formales derivados de este Marco Rector (Nivel 2 y Nivel 3), no este documento directamente.

### 1.2 Supremacía Jerárquica: El Modelo de 3 Niveles (C4 Model)

Para evitar la saturación normativa y el acoplamiento conceptual, la OTI adopta un modelo documental jerárquico inspirado en las dimensiones estructurales del **C4 Model** (Contexto / Contenedor / Componente / Código):

```
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 1: MARCO RECTOR DE ARQUITECTURA DE SOFTWARE (LIN-ARQ-001)          │
│ • Dimensión: Contexto y Contenedores (Macro / Estructural / Gobierno)    │
│ • Audiencia: Arquitectos Empresariales, Jefes de Proyecto, Líderes OTI   │
│ • Propósito: Decidir topologías, estadios, CAP, protocolos y seguridad   │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Gobierna y delimita
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 2: ESTÁNDAR DE DISEÑO DE SOFTWARE Y PATRONES TÁCTICOS (LIN-DIS-001)│
│ • Dimensión: Componentes e Interacciones Internas (Táctico / Modular)    │
│ • Audiencia: Tech Leads, Arquitectos de Software, Desarrolladores Senior │
│ • Propósito: Capas, Hexagonal, DDD, CQRS, BFF, ACL y Resiliencia táctica │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │ Aterriza en constructores
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ NIVEL 3: ESTÁNDAR DE DESARROLLO JAVA Y CÓDIGO (LIN-DEV-JAVA-001)         │
│ • Dimensión: Código, Sintaxis y Frameworks (Micro / Implementación)      │
│ • Audiencia: Programadores Java, Fábricas de Software, Especialistas QA  │
│ • Propósito: SOLID, Patrones GoF en Spring Boot, pom.xml, JUnit, Clean   │
└──────────────────────────────────────────────────────────────────────────┘
```

> **Regla de Supremacía:** Si existiese un conflicto aparente entre una decisión de codificación o diseño táctico con las normas dictadas en este Marco Rector (Nivel 1), **prevalece siempre el Nivel 1**. Ningún proyecto puede justificar una desviación arquitectónica basándose en preferencias o utilidades técnicas de nivel inferior.

### 1.3 Registro de Decisiones Arquitectónicas (ADR)

Toda desviación de las directivas declaradas en este documento (por ejemplo: adopción de un motor NoSQL específico, extracción de un microservicio, o despliegue en máquina virtual en lugar de Kubernetes) **debe ser justificada formalmente** mediante un **ADR (*Architecture Decision Record*)**.

El ADR es un documento formal y trazable que debe contener obligatoriamente las siguientes secciones:
1. **Contexto y Problema:** Qué limitación o requisito particular impide cumplir con el estándar por defecto.
2. **Opciones Evaluadas:** Al menos dos alternativas arquitectónicas analizadas.
3. **Decisión Tomada:** El camino arquitectónico seleccionado.
4. **Justificación Técnica Medible:** Cifras de latencia, concurrencia, volumen de I/O o restricciones contractuales que sustentan la decisión.
5. **Consecuencias y Riesgos:** Impacto operativo, costo de licenciamiento, deuda técnica asumida y plan de mitigación.

La aprobación del ADR corresponde exclusivamente al **Comité de Arquitectura de la OTI**. Sin un ADR aprobado y firmado en el repositorio institucional, la arquitectura desviada se considerará un **anti-patrón y bloqueará el pase a producción en los gates de CI/CD**.

---

## 2. Hoja de Ruta y Topología de los Sistemas ONP

La evolución arquitectónica de los sistemas institucionales se organiza en tres estadios claramente acotados.

### 2.1 Los Tres Estadios Evolutivos

```
ESTADIO 1                             ESTADIO 2                             ESTADIO 3
Monolito Tradicional                  Monolito Modular                      Microservicios Selectivos
(Legacy en JBoss/WebLogic)     ───►   (Estándar NUEVO POR DEFECTO)   ───►   (Excepción Regulada por ADR)
• Sin separación de dominio           • Fronteras estrictas de dominio      • Aislamiento físico (Pod K8s)
• Acoplamiento en BD                  • Módulos Maven independientes        • BD propia por servicio
• Escalamiento vertical pesado        • Cero red / latencia en memoria      • Red y eventualidad (CAP)
```

#### Estadio 1: Monolito Tradicional (*Legacy*)
Sistemas históricos que operan en servidores de aplicaciones tradicionales (JBoss, Oracle WebLogic) con bases de datos relacionales compartidas sin separación de esquemas por módulo.
- **Política:** No se permite la creación de nuevos sistemas bajo este esquema. Los sistemas existentes en Estadio 1 deben entrar en un plan gradual de modernización hacia el Estadio 2 utilizando el patrón de migración *Strangler Fig*.

#### Estadio 2: Monolito Modular (Estándar por Defectos para Todo Proyecto Nuevo)
Es la topología arquitectónica mandatoria para la construcción de nuevos sistemas en la ONP (*ADR-002*). Consiste en una única aplicación desplegable (un solo contenedor OCI en Kubernetes), pero estructurada internamente en **módulos Maven con fronteras explícitas y aislamiento estricto de dominio**.
- **Por qué es el estándar por defecto:** El 85% de los sistemas institucionales de la ONP poseen requerimientos de transaccionalidad ACID (cálculo actuarial, planillas de pensiones, liquidaciones) y concurrencia moderada. El Monolito Modular brinda la disciplina de diseño de un sistema distribuido (alto acoplamiento interno prohibido, APIs claras por subdominio) sin pagar el altísimo costo operativo de latencia de red, serialización continua, transacciones distribuidas complejas y orquestación masiva de infraestructura.

#### Estadio 3: Microservicios Selectivos (Excepción Regulada por 6 Criterios)
La arquitectura de microservicios **no es el punto de partida ni el estado de madurez obligatorio** en la ONP (*ADR-003*). Un módulo del Monolito Modular solo podrá separarse y desplegarse como un Microservicio independiente (Estadio 3) si y solo si cumple **simultáneamente con los siguientes SEIS criterios técnicos verificables**:

| # | Criterio de Extracción | Umbral o Requisito Medible |
|---|---|---|
| **1** | **Dominio Autónomo (*Bounded Context* limpios)** | El módulo posee una alta cohesión interna y se comunica con el resto del sistema por interfaces contractuales claras, con mínimos intercambios síncronos. |
| **2** | **Soberanía de Datos e Independencia** | El módulo puede operar con su propio esquema o instancia de base de datos dedicada. Prohibido extraer un servicio que compita o haga *joins* directos sobre tablas compartidas en Oracle. |
| **3** | **Escalamiento Asimétrico Comprobado** | El módulo experimenta picos de carga en CPU o Memoria que superan en al menos **3x** a la carga del resto del sistema (ejemplo: Motor de Cálculo Actuarial vs. Módulo de Consultas de Catálogos). |
| **4** | **Ciclo de Vida y Despliegue Independiente** | El módulo requiere pases a producción continuos y frecuentes (ej. varias veces por semana) sin arriesgar la disponibilidad del core previsional. |
| **5** | **Tolerancia a la Consistencia Eventual** | Las operaciones que vinculan al módulo con otros dominios no exigen transacciones ACID distribuidas inmediatas. El negocio acepta explícitamente retrasos de propagación (consistencia eventual). |
| **6** | **Capacidad Operativa y SRE Instalada** | El equipo del proyecto o la fábrica cuenta con observabilidad distribuida (OpenTelemetry/Jaeger), trazabilidad de errores, alertas y pipelines de CI/CD automatizados al 100%. |

> **Mandato:** Si falta tan solo UNO de los 6 criterios, la funcionalidad **debe permanecer dentro del Monolito Modular**.

> **Nota de alcance (`ADR-003`):** estos 6 criterios rigen **exclusivamente la extracción a microservicio**. No condicionan ni sustituyen los 6 criterios de adopción de Domain-Driven Design, que son una decisión independiente y están definidos en `LIN-DIS-001 §3.0`. Un módulo puede adoptar DDD sin ser candidato a microservicio, y un microservicio puede construirse sin DDD si su lógica de dominio no lo amerita.

### 2.2 Estrategia de Migración de Sistemas Legados (*Strangler Fig*)

Para modernizar sistemas del Estadio 1 hacia el Estadio 2 (o Estadio 3 cuando esté debidamente justificado), la ONP prohíbe las reescrituras tipo *Big-Bang*. Se adopta oficialmente el patrón de migración **Strangler Fig (Higuera Estranguladora — ADR-004)**:

```
┌────────────────────────────────────────────────────────────┐
│ CLIENTE / FRONTEND SPA / USUARIOS ONP                      │
└─────────────────────────────┬──────────────────────────────┘
                              │ HTTPS / OpenAPI
                              ▼
┌────────────────────────────────────────────────────────────┐
│ API GATEWAY INSTITUCIONAL (Enrutamiento por Ruta / Intercepción)│
└──────────────┬──────────────────────────────┬──────────────┘
               │ Rutas migradas (/api/v1/new) │ Rutas legadas (/api/v1/old)
               ▼                              ▼
┌──────────────────────────────┐    ┌────────────────────────┐
│ NUEVO MONOLITO MODULAR / MS  │    │ SISTEMA LEGACY (JBoss) │
│ (Kubernetes + Spring Boot 3) │    │ (Sin modificación)     │
└──────────────────────────────┘    └────────────────────────┘
```

1. Se despliega una fachada de intercepción o **API Gateway** frente al sistema legado.
2. Las nuevas funcionalidades o submódulos refactorizados se construyen en el **Nuevo Sistema (Estadio 2)**.
3. El Gateway redirige progresivamente el tráfico de las rutas migradas hacia el nuevo sistema, dejando que el legacy maneje únicamente lo no refactorizado.
4. Cuando el legacy deja de recibir tráfico, se apaga y desinstala limpiamente de los servidores de aplicaciones de la institución.

### 2.3 Gobierno Institucional de Feature Toggles (*Unleash*)

El patrón *Strangler Fig* y el desarrollo continuo en Trunk-Based Development (*LIN-VER-001*) exigen desacoplar el **despliegue de código** de la **liberación de funcionalidades al usuario**. Para ello, es mandatorio el uso de **Feature Toggles (PA14)** gestionados mediante la plataforma estándar on-premise de la institución: **Unleash (*ADR-014*)**.

| Categoría del Toggle | Propósito Técnico | Ciclo de Vida Máximo Permitido | Acción de Gobierno al Vencer |
|---|---|---|---|
| **Release Toggle** | Ocultar una funcionalidad incompleta o en migración en la rama principal (*main*). | **2 semanas tras el go-live** de la funcionalidad. | **Eliminación obligatoria** en el siguiente *pull request* de refactorización. |
| **Ops Toggle** | Apagar o degradar un comportamiento pesado ante caídas o degradación de terceros (ej. consulta RENIEC en línea). | **Indefinido / Permanente** (gobernado por SRE). | Revisión semestral de vigencia y umbrales en Grafana. |
| **Permission Toggle** | Habilitar características experimentales solo para un grupo beta de usuarios internos o auditores. | **Hasta fin de la campaña** de pruebas o auditoría. | Eliminación del flag al generalizar la función. |

> **Regla de Cero Deuda Técnica en Toggles:** Un Release Toggle que permanece en el código fuente por más de 30 días después del pase a producción se clasifica como **deuda técnica crítica**. El pipeline de análisis estático (SonarQube) emitirá una alerta de bloqueo para el módulo si detecta flags de liberación caducos en las comprobaciones de Unleash.

**Alternativa ligera y restricción de SaaS externo:**
- **Alternativa ligera:** para servicios simples o Release Toggles, se permite el uso de **Spring Cloud Config** con flags condicionales en YAML, siempre que no se requieran cambios en tiempo sub-segundo sin recarga de contexto.
- **Servicios SaaS externos:** el uso de plataformas en la nube como **LaunchDarkly** está **restringido** y requiere un ADR aprobado conjuntamente por Arquitectura OTI y la Oficina de Seguridad de la Información, ya que implican salida de red externa e intercambio de telemetría incompatible con entornos on-premise cerrados (*ADR-014*).

**Branch by Abstraction:** cuando un Feature Toggle reemplaza progresivamente una implementación existente (no solo oculta una nueva), se combina con el patrón **Branch by Abstraction**: se define una interfaz en `domain.port.out` y un *Router* de infraestructura que decide, según el toggle, cuál implementación invocar. Esto evita ramas de código de larga vida (*long-lived branches*) durante la migración.

---

## 3. Gobierno de Datos y Teorema CAP en Sistemas Distribuidos

Cuando un sistema opera en el Estadio 3 (Microservicios) o interactúa de forma distribuida, el comportamiento ante fallos de red es gobernado implacablemente por el **Teorema CAP**.

### 3.1 Obligatoriedad de Declaración CAP (*CP vs. AP*)

El Teorema CAP postula que en un sistema distribuido propenso a particiones de red (**P**), solo se puede elegir una de dos garantías: **Consistencia Fuerte (C)** o **Disponibilidad Alta (A)**.

En la ONP, **todo microservicio o módulo distribuido debe declarar formalmente su elección CAP en el ADR de arquitectura (*ADR-008*)**:

```
                       ┌─────────────────────────┐
                       │  PARTICIÓN DE RED (P)   │
                       │  (Inevitable en Cloud/  │
                       │   Sistemas Distribuidos)│
                       └────────────┬────────────┘
                                    │ ¿Qué prioriza el negocio ante el fallo?
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
┌──────────────────────────────────────┐     ┌──────────────────────────────────────┐
│ SISTEMA CP (Consistencia Priorizada) │     │ SISTEMA AP (Disponibilidad Priorizada│
│ • Bloquea operaciones de escritura   │     │ • Acepta escrituras locales e        │
│   hasta restaurar la red o consenso. │     │   inconsistencias temporales.        │
│ • Uso ONP: Cálculo Actuarial, Cuenta │     │ • Uso ONP: Catálogos, Mesa de Partes │
│   Individual, Pagos y Liquidaciones. │     │   Virtual, Notificaciones y Logs.    │
└──────────────────────────────────────┘     └──────────────────────────────────────┘
```

| Elección CAP | Comportamiento del Sistema ante Partición de Red | Cuándo Elegir en la ONP |
|---|---|---|
| **CP (Consistente y Tolerante a Particiones)** | El servicio **rechaza las peticiones (HTTP 503 / Error)** si no puede garantizar que el dato escrito se replique o actualice en la fuente de verdad. Se prioriza que **nunca existan datos financieros erróneos o desincronizados**, aunque el sistema deje de responder temporalmente. | Módulos del Core Previsional: Cálculo de Reserva Matemática, Registro de Aportes Monetarios, Emisión de Resoluciones de Liquidación y Tesorería. |
| **AP (Disponible y Tolerante a Particiones)** | El servicio **responde siempre usando datos locales o cachés**, aceptando que puedan estar temporalmente desactualizados con respecto a otras instancias. La consistencia eventual se regularizará cuando la red se restablezca. | Módulos de Interacción Continua: Consulta de Catálogos Públicos, Recepción Inicial en Mesa de Partes Virtual, Generación de Reportes de Lectura y envío de Notificaciones. |

### 3.2 Política sobre Transacciones Distribuidas (*Prohibición del 2PC*)

Queda **terminantemente prohibido** el uso de transacciones distribuidas con protocolos de compromiso en dos fases (**Two-Phase Commit — 2PC / XA**) entre microservicios o bases de datos físicas independientes.
- **Razón Arquitectónica:** El protocolo 2PC introduce bloqueos de filas y tablas de larga duración a través de la red, multiplicando la latencia, agotando los pools de conexiones transaccionales en Oracle y colapsando la disponibilidad del sistema ante cualquier micro-corte o lentitud de un servicio participante.

### 3.3 Patrón Saga con Transactional Outbox (Consistencia Eventual)

Para mantener coherencia de negocio entre dos o más dominios autónomos o microservicios sin violar la prohibición de 2PC, se establece como norma obligatoria la adopción del **Patrón Saga (oquestado o coreografiado)** respaldado por el patrón **Transactional Outbox (*LIN-DIS-001 §4.2*, DDL en *LIN-BD-ORA-001 §3.10*, relevo en *LIN-BUS-001 §7.3*)**.

1. **Transacciones Locales ACID:** Cada servicio participante ejecuta su modificación de estado exclusivamente sobre su base de datos local dentro de una transacción ACID propia.
2. **Tabla Outbox:** En la misma transacción local del negocio, se inserta el evento del cambio en la tabla `EVT_OUTBOX` de la base de datos local.
3. **Publicación Asíncrona:** Un proceso de relevo (*Relay* / Debezium / Kafka Connect) lee la tabla `EVT_OUTBOX` y publica de forma garantizada (*At-Least-Once*) el evento hacia el bus de mensajería (Apache Kafka).
4. **Compensación ante Fallos:** Si una etapa posterior del flujo de negocio falla, el Saga orquestador o la coreografía emite **Eventos de Compensación** que instruyen a los servicios previos a ejecutar transacciones locales inversas para anular el efecto de la operación.

#### 3.3.1 Variante ONP: Saga por orquestación sobre aplicativos monolíticos (Kafka + REST)

El patrón Saga no requiere microservicios. En ONP, donde el parque de aplicativos está compuesto mayoritariamente por monolitos con base de datos propia, Saga se implementa combinando **Kafka como canal de transporte** y **REST como mecanismo de ejecución** en cada participante — esta es la variante de corto/mediano plazo mientras el parque no ha sido extraído a microservicios.

```
Orquestador (app separada — gestor del flujo y estado)
    │
    ├──► topic: onp.saga.{flujo}.paso1.comando
    │         └──► Monolito A consume → llama su propio POST /api/v1/... → persiste en su BD
    │              └──► publica en: onp.saga.{flujo}.paso1.respuesta { estado: OK|FALLO }
    │                                       ↑
    │              Orquestador lee respuesta─┘
    │              Si OK → avanza al paso 2
    │              Si FALLO → inicia compensaciones en orden inverso
    │
    ├──► topic: onp.saga.{flujo}.paso2.comando
    │    ...
    └──► Saga COMPLETADA / Saga FALLIDA (todo compensado)
```

**Roles y responsabilidades:**

| Componente | Responsabilidad |
|---|---|
| **Orquestador** | Mantiene el estado de la Saga en BD propia. Publica comandos en Kafka. Lee respuestas. Decide avanzar o compensar. |
| **Kafka** | Canal de transporte desacoplado. El orquestador no conoce la URL de cada monolito — solo el tópico. |
| **Monolito participante** | Consume el comando Kafka. Llama su propio servicio REST interno. Publica la respuesta. No sabe que está en una Saga. |

**Estado que el orquestador debe persistir:**

```sql
SAGA_INSTANCIA
├── saga_id          UUID      -- identificador del flujo completo
├── tipo             VARCHAR   -- ej. 'PENSION_COMPLETA'
├── estado           VARCHAR   -- INICIADA | EN_PROGRESO | COMPENSANDO | COMPLETADA | FALLIDA
├── paso_actual      INTEGER
├── contexto         JSON      -- IDs de cada operación ejecutada por paso (para compensación)
├── creado_en        TIMESTAMP
└── actualizado_en   TIMESTAMP
```

**Lo que cada monolito participante debe garantizar:**

- **Idempotencia:** si el comando llega más de una vez (reintento por timeout), el monolito no duplica el efecto. Usar `sagaId` + número de paso como clave de idempotencia.
- **Endpoint de compensación:** operación de negocio inversa con auditoría. No es un `DELETE` físico — es una reversión con trazabilidad.
- **Desconocimiento de la Saga:** el servicio REST del monolito es una operación normal. El conocimiento del flujo completo vive solo en el orquestador.

**Cuándo usar esta variante:**

| Criterio | Saga REST + Kafka (esta variante) | Saga microservicios pura |
|---|---|---|
| Participantes | Monolitos existentes con BD propia | Microservicios extraídos con BD propia |
| Adopción | Baja — los monolitos solo agregan consumer/producer Kafka | Mayor — requiere diseño de microservicio completo |
| Cuándo aplica en ONP | Corto/mediano plazo — parque actual de aplicativos | Largo plazo — tras extracción de microservicios |

> El detalle de la convención de tópicos Saga y la estructura de CloudEvents extendido está en **`LIN-BUS-001` sección 9.4**.

---

## 4. Estilos Macro de Comunicación e Interoperabilidad

Los sistemas de la ONP no operan aislados. La interacción entre contenedores, módulos y entidades externas se rige por dos protocolos primarios según la naturaleza de la operación.

### 4.1 Comunicación Sincrónica (*OpenAPI 3.0+ como Lenguaje Publicado*)

La comunicación síncrona (**REST sobre HTTPS**) se reserva exclusivamente para consultas inmediatas en tiempo real desde interfaces de usuario (SPAs), pasarelas externas o integraciones donde la respuesta es bloqueante para la continuación de la acción del usuario.

- **Estándar:** **OpenAPI 3.0+** en formato YAML o JSON.
- **Gobierno:** Toda API REST debe estar formalmente especificada y publicada en el Portal institucional de APIs (**WSO2 API Manager / Swagger Hub**). Prohibido exponer endpoints REST sin contrato OpenAPI validado en el pipeline de CI/CD.
- **Inmutabilidad de Contratos:** El versionamiento es obligatorio en la ruta del recurso (`/api/v1/...`). Una versión publicada no podrá alterar sus tipos de datos de salida ni eliminar campos (`Full Compatibility`); todo cambio disruptivo obliga a emitir `/api/v2/...`.

### 4.2 Arquitectura Orientada a Eventos — EDA (*Apache Kafka + CloudEvents v1.0*)

La comunicación asíncrona mediante paso de mensajes es el mecanismo mandatorio para el desacoplamiento inter-módulos en Monolito Modular y comunicación inter-servicios en Estadio 3.

- **Broker Institucional:** **Apache Kafka (*ADR-012 / LIN-BUS-001*)**. Prohibido introducir brokers alternativos (RabbitMQ, ActiveMQ) sin un ADR aprobado.
- **Estándar de Envolvente del Mensaje:** **CloudEvents v1.0 (CNCF — ADR-013)**. Todo evento emitido al bus debe usar la estructura estándar, encapsulando el payload de dominio dentro del atributo `data`:

```json
{
  "specversion": "1.0",
  "id": "e45a9b12-88cd-4166-b258-001122334455",
  "source": "urn:onp:core:expedientes",
  "type": "pe.gob.onp.core.expedientes.expedienteaprobado.v1",
  "time": "2026-07-08T15:00:00Z",
  "datacontenttype": "application/json",
  "data": {
    "numeroExpediente": "EXP-2026-0099182",
    "dniPensionista": "08241578",
    "regimen": "REGIMEN_19990"
  }
}
```

- **Gobierno del Esquema:** La definición y evolución de los esquemas del atributo `data` (JSON Schema o Avro) están regidas por el **Confluent Schema Registry** institucional bajo la política de compatibilidad **Full Compatibility**.

### 4.3 Interoperabilidad Gubernamental y SOA (*PIDE, RENIEC, SUNAT*)

Las integraciones con entidades externas del Estado Peruano (Plataforma de Interoperabilidad del Estado - PIDE, RENIEC, SUNAT) operan frecuentemente bajo protocolos heredados o específicos (**SOAP / XML / WS-Security / mTLS**).

1. **Aislamiento de Seguridad (Zero Trust):** Toda comunicación con terceros gubernamentales debe transitar obligatoriamente a través de la pasarela de seguridad perimetral (**WSO2 API Gateway / mTLS mutual proxy**), nunca directamente desde un contenedor de aplicación de backend.
2. **Capa Anticorrupción (ACL Mandatoria):** Según el principio de protección de fronteras (*ADR-005* y *PT13 en LIN-DIS-001*), el módulo que consume un servicio PIDE o RENIEC no puede propagar los DTOs XML o estructuras de terceros al interior de su lógica de negocio. Debe implementar un `Adapter` de infraestructura que traduzca de forma quirúrgica la respuesta exterior hacia el modelo inmutable de la ONP (`DatosPersona`, `Dni`).
3. **Aislamiento ante Caídas Exteriores:** Dado que los servicios externos del Estado experimentan caídas impredecibles, es mandatorio que el cliente HTTP/SOAP tenga configurados **Timeouts estrictos (máx. 3 a 5 segundos)**, **Circuit Breakers (Resilience4j)** y estrategias de contingencia o *degraded mode* (por ejemplo, permitir registro manual provisorio con validación diferida si RENIEC está caído).

---

## 5. Matriz de Seguridad, Resiliencia y Observabilidad Macro

### 5.1 Defensa en Profundidad y Zero Trust

La seguridad arquitectónica en la ONP se rige por el principio de **Zero Trust (Cero Confianza)**: la ubicación de un contenedor en una red interna de Kubernetes no le otorga acceso implícito a ningún recurso.
- **Autenticación e Identidad:** Las peticiones entre el frontend SPA y los backends institucionales transitan validadas por el token institucional **SAA** o estándar **OAuth2 / OIDC** vía WSO2 API Manager (`LIN-SEC-APP-001`).
- **Principio de Mínimo Privilegio:** Los contenedores en Kubernetes corren obligatoriamente con usuarios **no root** (`UID > 1000`) y con sistemas de archivos de solo lectura donde sea posible. Las credenciales de base de datos y llaves API se inyectan dinámicamente mediante **Kubernetes Secrets / HashiCorp Vault**, estando estrictamente prohibida su presencia en archivos de configuración o código fuente (`git`).

### 5.2 Topología y Destino de Despliegue (*Kubernetes vs. Máquina Virtual*)

El clúster de **Kubernetes con containerd (*ADR-009*)** es el destino de producción mandatorio y por defecto para todo sistema nuevo o migrado en la ONP (*ADR-011*).

```
┌──────────────────────────────────────────────────────────────────────────┐
│ KUBERNETES ONP (containerd runtime - DESTINO DE PRODUCCIÓN POR DEFECTO)  │
│ • Alta disponibilidad nativa (Deployment con réplicas ≥ 2)               │
│ • Auto-healing mediante Liveness y Readiness Probes                      │
│ • Orquestación, escalamiento horizontal e Ingress centralizado           │
└────────────────────────────────────▲─────────────────────────────────────┘
                                     │ Excepción estricta bajo ADR
┌────────────────────────────────────┴─────────────────────────────────────┘
│ MÁQUINA VIRTUAL DEDICADA (VM - SOLO BAJO LOS 4 CRITERIOS DE EXCEPCIÓN)   │
│ 1. Base de datos o motor transaccional con I/O extremo (ej. Oracle RAC)  │
│ 2. Dependencia obligatoria de hardware físico o dongle MAC-locked        │
│ 3. Aplicación COTS/Legacy cerrada incompatible con contenedores Linux    │
│ 4. Exigencia regulatorio-legal estricta de aislamiento físico sin K8s    │
└──────────────────────────────────────────────────────────────────────────┘
```

> **Mandato:** No se permite alegar "desconocimiento de Kubernetes por parte del proveedor" como criterio para solicitar una máquina virtual. La capacitación técnica es responsabilidad contractual del proveedor.

**Estadio de Transición (runtime temporal, no destino final):** durante el desarrollo local, el runtime de contenedores es de libre elección (Docker Engine o Podman). Entre el build y el despliegue en el clúster QA/PROD, `LIN-K8S-001` define un estadio operativo intermedio con Docker Engine + Docker Compose — es una etapa temporal, nunca un destino de producción. El `Dockerfile` es el estándar de construcción en todos los casos; el runtime de QA/PROD es exclusivamente **containerd**, gestionado con `crictl`. El detalle operativo completo de este estadio está en `LIN-K8S-001`.

**Aprovisionamiento declarativo (IaC):** la infraestructura del clúster Kubernetes y sus recursos asociados se aprovisionan de forma declarativa mediante Terraform en un repositorio dedicado, según el modelo de madurez y las fases de adopción definidas en `LIN-IAC-001`.

### 5.3 Observabilidad Institucional (*Marco Google SRE Four Golden Signals*)

La observabilidad es un **requisito de arquitectura de producción, no una opción de soporte (*ADR-010*)**. Ningún sistema podrá ser liberado a producción si no expone las telemetrías necesarias para supervisar las **Cuatro Señales Doradas (*Four Golden Signals*) del marco SRE de Google**:

| Señal Dorada SRE | Qué Mide en la Arquitectura ONP | Mecanismo e Implementación Técnica Mandatoria |
|---|---|---|
| **Latencia (*Latency*)** | Tiempo que tarda el sistema en atender una petición HTTP o evento, diferenciando peticiones exitosas de erróneas. | `http.server.requests` (Micrometer/Prometheus). Medición obligatoria de percentiles **P50, P95 y P99** en los dashboards de Grafana. |
| **Tráfico (*Traffic*)** | Demanda concurrente y volumen transaccional en un instante de tiempo. | Tasa de peticiones por segundo (`req/sec`) o tasa de eventos consumidos de tópicos Kafka (`records-consumed-rate`). |
| **Errores (*Errors*)** | Tasa de fallos explícitos (códigos HTTP 5xx, 4xx) e implícitos (excepciones de dominio no controladas). | Monitoreo del ratio `(peticiones_error / peticiones_totales) * 100`. Alertas disparadas ante tasas de error > 0.5% en ventana de 5 minutos. |
| **Saturación (*Saturation*)** | Grado de carga o agotamiento de los recursos más restringidos del contenedor o servicio. | Monitoreo continuo de: Uso de Memoria Heap JVM (`jvm.memory.used`), hilos activos, saturación del **Pool de Conexiones HikariCP hacia Oracle (`hikaricp.connections.pending`)** y CPU del pod. |

La instrumentación técnica debe integrarse al ecosistema de observabilidad institucional delegado y normalizado en **LIN-OBS-001**:
- **Trazas Distribuidas:** OpenTelemetry Collector / Jaeger (saturación de encabezados W3C `traceparent` en todo salto de red).
- **Logs Estructurados:** Formato JSON estricto (`LogstashEncoder` en Logback) con inyecciones de `traceId`, `spanId` y `X-Request-ID`.
- **Health Checks:** Probes de Kubernetes (`/actuator/health/liveness` y `/actuator/health/readiness`) activos en cada pod.

---

## 6. Estrategia Macro de Datos y Base de Datos

### 6.1 Oracle como Estándar Transaccional ACID

La base de datos relacional **Oracle** es el **pilar transaccional indiscutible** y la **Fuente Única de Verdad (*Single Source of Truth*) por defecto** de la ONP para todos los sistemas de negocio previsional, contable, actuarial y financiero (`LIN-BD-ORA-001`). Su cumplimiento ACID es obligatorio en operaciones que afecten saldos, aportes, liquidaciones y expedientes de los pensionistas.

### 6.2 Base de Datos No Relacional Complementaria (NoSQL)

Las tecnologías NoSQL no compiten ni reemplazan al motor Oracle. Actúan exclusivamente como **capas de almacenamiento especializadas y complementarias** para resolver patrones de acceso donde el modelo relacional degrada el rendimiento de la aplicación o del motor central.

Su adopción requiere **ADR aprobado** y debe sustentarse en la verificación fehaciente de al menos uno de los siguientes detonadores medibles:

| Detonador Verificado | Tipo NoSQL Recomendado | Tecnología de Referencia ONP | Caso de Uso Aprobado en el Ecosistema |
|---|---|---|---|
| **Patrón de búsqueda y consulta no CRUD** (Búsqueda por texto libre, facetas, scoring, similitud difusa sobre millones de documentos). | **Search Store** | **Elasticsearch** | Búsqueda masiva y filtrado avanzado de expedientes previsionales y resoluciones digitales. (Ya operativo en observabilidad para logs/trazas - *LIN-OBS-001*). |
| **Proyecciones ricas y desnormalizadas para baja latencia en lectura** (Vistas consolidadas con estructuras anidadas variables). | **Document Store** | **MongoDB** | Lado de lectura (*Read Model*) en arquitecturas **CQRS** para expedientes o carpetas ciudadanas autocontenidas (`§3.10 LIN-DIS-001`). |
| **Datos temporales de ciclo de vida corto y TTL** (Lookup sub-milisegundo por clave). | **Key-Value Store** | **Redis** | Almacenamiento en caché de tokens institucionales (SAA), sesiones de usuario, contadores de *rate limiting* y catálogos estáticos de consulta frecuente. |
| **Volumen de escritura supera lo que Oracle ACID puede sostener** (tablas `LOG_`/`HIS_` con millones de inserciones diarias que generan contención; consistencia eventual aceptable). | **Column-family Store** | **Apache Cassandra** | Escritura masiva de eventos y *audit logs* de alto volumen. Requiere ADR — sin uso productivo actual en el ecosistema ONP. |
| **Métricas de negocio con altísima frecuencia de escritura** (no confundir con métricas de infraestructura, ya cubiertas por Prometheus — *LIN-OBS-001*). | **Time-series Store** | **InfluxDB / TimescaleDB** | Series temporales de negocio (ej. variación intradía de indicadores actuariales). Requiere ADR — sin uso productivo actual en el ecosistema ONP. |

**Lo que NO es un detonador válido:** "NoSQL escala mejor" sin evidencia de que Oracle sea el cuello de botella; "el equipo quiere aprender la tecnología"; "la arquitectura de referencia del proveedor la usa"; "es más simple que modelar en relacional" — la simplicidad de escritura no compensa la pérdida de ACID ni las garantías de integridad referencial.

**Regla de gobierno para la adopción:**

1. **ADR aprobado por Arquitectura OTI** con el detonador verificado, el tipo de BD seleccionado y la justificación técnica.
2. **Nuevo lineamiento específico** para la tecnología NoSQL adoptada, equivalente a `LIN-BD-ORA-001` para Oracle, que cubra diseño de datos, operación, seguridad y observabilidad.
3. **Validación de Plataforma** sobre viabilidad operativa en Kubernetes (`LIN-K8S-001`): *backups*, monitoreo, alta disponibilidad.
4. **Piloto controlado** antes de uso productivo, con criterios de éxito documentados y fecha de evaluación.

Mientras el lineamiento específico de la tecnología no exista, su uso productivo no está autorizado.

### 6.3 Dominio Complementario: Business Intelligence y Analítica (Medallion)

Para separar totalmente la carga analítica y de reportes masivos del procesamiento transaccional en tiempo real (OLTP Oracle), la institución opera una plataforma analítica basada en **Arquitectura Medallion (*LIN-BI-001*)**:

```
[ ORACLE OLTP / KAFKA EVENTS ] ──(ELT / CDC)──► [ BRONZE (Raw Data) ] ──► [ SILVER (Clean / Filtered) ] ──► [ GOLD (BI Ready / Parquet) ]
                                                                                                                     │
                                                                                                        [ OpenMetadata & Nessie ]
```

- La capa **Gold** en formato tabular **Parquet / Apache Nessie** constituye la fuente autoritativa para dashboards ejecutivos, analítica avanzada e inteligencia de negocios.
- **Prohibición de Reportes Masivos en el Core OLTP:** Queda estrictamente prohibido ejecutar consultas de reportes agregados, inteligencia de negocios o extracciones masivas sin paginar directamente sobre las bases de datos operacionales de Oracle en horario laboral. Tales procesos deben consumir las capas Silver o Gold del Lakehouse (*LIN-BI-001*).
- **Relación con CQRS operacional (§6.2):** el Medallion no es una rama aislada del negocio transaccional — la capa **Gold** es también la fuente natural para los **read models analíticos de CQRS** (reportes, dashboards, análisis histórico), mientras que Redis/MongoDB/Elasticsearch sirven los read models operacionales de baja latencia (`LIN-DIS-001 §4.2`). Ambos son proyecciones derivadas de la misma fuente de verdad transaccional en Oracle; se diferencian por el patrón de consulta que sirven, no por ser mecanismos independientes.

---

## 7. Estrategia Macro de Frontend

### 7.1 Arquitectura Desacoplada (SPA vs. Backend)

El desarrollo frontend en la ONP se estructura obligatoriamente como una **Single Page Application (SPA) separada físicamente del backend**:
- **Repositorios y Pipelines Independientes:** El código frontend reside en su propio repositorio Git, posee su propio pipeline CI/CD y genera un artefacto estático (HTML/CSS/JS) servido en su propio contenedor (NGINX en Kubernetes via Ingress).
- **Framework Primario Mandatorio:** **Angular 17+ con TypeScript Estricto (*ADR-006 / LIN-FE-ANG-001*)**. La adopción de frameworks alternativos (React o Vue) se restringe a excepciones altamente fundamentadas con ADR previo y aprobado.
- **Seguridad SAA:** La autenticación se gestiona del lado cliente mediante el token institucional **SAA** o token **OAuth2/OIDC** emitido por WSO2, el cual es inyectado como cabecera `Authorization: Bearer <token>` en cada llamado hacia el backend.

### 7.2 Umbrales Core Web Vitals como Gates en CI/CD

La calidad de experiencia de usuario y el rendimiento del frontend se miden institucionalmente bajo el marco **Core Web Vitals de Google y métricas complementarias de Lighthouse (*ADR-007*)**.

El cumplimiento de los siguientes umbrales en entornos de prueba integrados es un **gate de bloqueo mandatorio para la promoción del build de frontend a producción (`LIN-CICD-001`)**:

| Categoría de Métrica | Métrica / Indicador | Descripción Técnica | Umbral Máximo Permitido en ONP |
|---|---|---|---|
| **Core Web Vitals** | **LCP** (*Largest Contentful Paint*) | Tiempo que tarda en renderizarse y ser completamente visible el elemento de contenido más grande dentro del viewport del usuario. | **< 2.5 segundos** |
| **Core Web Vitals** | **INP** (*Interaction to Next Paint*) | Latencia total de respuesta visual del navegador ante una interacción del usuario (clic, teclado, toque en pantalla). | **< 200 milisegundos** |
| **Core Web Vitals** | **CLS** (*Cumulative Layout Shift*) | Puntuación que cuantifica los saltos o reordenamientos visuales inesperados de elementos DOM durante la carga de la pantalla. | **< 0.1 (Estabilidad Visual Alta)** |
| **Lighthouse / UX** | **FCP** (*First Contentful Paint*) | Tiempo transcurrido hasta que el navegador muestra la primera pieza de texto o contenido visual del DOM. | **< 1.8 segundos** |
| **Lighthouse / UX** | **TTI** (*Time to Interactive*) | Tiempo que requiere la página para volverse 100% interactiva sin hilos bloqueados ni latencias residuales en JS. | **< 3.5 segundos** |
| **Lighthouse / UX** | **TBT** (*Total Blocking Time*) | Suma de todo el tiempo en que el hilo principal (*main thread*) del navegador estuvo bloqueado por tareas mayores a 50ms. | **< 200 milisegundos** |
| **Lighthouse / UX** | **FPS de Animaciones** | Fluidez visual de micro-animaciones, modales, transiciones de CSS y desplazamientos de barras o tablas. | **≥ 60 cuadros por segundo (FPS)** |

> El detalle de las optimizaciones obligatorias en Angular (Lazy Loading por rutas, Tree Shaking, bloques deferibles `@defer`, pre-conexión de assets y prohibición de manipulación directa del DOM o el uso abusivo de `setTimeout`) se documenta exhaustivamente en el **Nivel 3: `LIN-FE-ANG-001`**.

---

## 8. Perfiles de Contratación y Requisitos TDR por Estilo Arquitectónico

Dado que la totalidad del software previsional en la ONP es desarrollado mediante contratación con Terceros y Fábricas de Software, este apartado se constituye como **anexo normativo de cumplimiento para la redacción de Términos de Referencia (TDRs)** y evaluaciones técnicas de licitación.

### 8.1 Requisitos Base del Equipo de Proyecto (Obligatorio Transversal)
Todo profesional o equipo asignado por la empresa contratista a proyectos de desarrollo Java en la institución deberá acreditar competencias avanzadas en:
- **Java 21 LTS:** Dominio de registros (`records`), clases selladas (`sealed classes`), coincidencias de patrones (`pattern matching`) e hilos virtuales (`virtual threads`).
- **Spring Boot 3.x:** Configuración por perfiles, inyección de dependencias avanzada, `spring-boot-starter-actuator` y transaccionalidad declarativa con `@Transactional`.
- **Ecosistema y Herramientas:** Maven (construcción multi-módulo), Git (flujo Trunk-Based / Conventional Commits), Docker (construcción multi-stage) y pruebas unitarias automáticas con JUnit 5 y Mockito.

### 8.2 Exigencias Diferenciadas según Topología del Proyecto

| Topología del Proyecto Licitado | Competencias y Habilidades Especializadas Requeridas | Señales de Alarma / Anti-Patrones en la Evaluación Técnica |
|---|---|---|
| **Transaction Script / Active Record** *(mantenimiento de sistemas simples o legados)* | • JPA/Hibernate, Spring Data, manejo transaccional declarativo. | No conoce `@Transactional` o usa `SELECT *`; no distingue transacción declarativa de programática. |
| **Estadio 2: Monolito Modular** *(Estándar por Defectos en ONP)* | • Arquitectura modular Maven y gobierno de fronteras de paquetes.<br>• Principios SOLID aplicados rigurosamente a clases y servicios (`LIN-DEV-JAVA-001 §7`).<br>• Capacidad para aislar subdominios sin incurrir en dependencias circulares. | Desconoce el impacto de acoplar paquetes de dominio entre sí; usa comodines de importación o no logra explicar cómo evitar ciclos en dependencias Maven multi-módulo. |
| **Arquitectura Hexagonal** *(candidato a microservicio)* | • Patrón Hexagonal (*Ports & Adapters*) estricto con inversión de dependencias (`LIN-DIS-001 §2.3`).<br>• Pruebas de dominio puro sin contenedor Spring. | Mezcla lógica de negocio en Controllers o Repositories; no logra aislar el dominio del framework en pruebas unitarias. |
| **Estadio 3: Microservicios** | • Spring Cloud o diseño *Kubernetes-native*, Circuit Breaker, Trazabilidad Distribuida (OpenTelemetry).<br>• Transacciones distribuidas eventuales (Patrón Saga y Outbox, `§3.3`). | Intenta usar `2PC` o transacciones bloqueantes entre servicios; desconoce el Teorema CAP, Saga o cómo operar en consistencia eventual. |
| **Domain-Driven Design (DDD)** *(solo cuando aplican los 6 criterios de `LIN-DIS-001 §3.0`)* | • Bounded Contexts, Agregados, Value Objects, Domain Events, CQRS básico. | No puede distinguir un Agregado de una entidad JPA; propone DDD para un CRUD simple sin justificar los 6 criterios de gobernanza. |
| **Desarrollo Frontend SPA Angular** | • Angular 17+ con TypeScript estricto, programación reactiva con RxJS (`Signals`, `Observables`).<br>• Optimización extrema para cumplimiento de Core Web Vitals (LCP, INP, CLS) y pruebas en Lighthouse.<br>• Diseño responsivo y buenas prácticas de seguridad (gestión limpia del token SAA). | Manipula directamente el DOM mediante `document.getElementById()`; utiliza `any` en TypeScript; abusa de `setTimeout(fn, 0)` para hackear el ciclo de detección de cambios (*Change Detection*) de Angular. |

> **Nota de alcance:** esta tabla agrupa perfiles de contratación/TDR y mezcla dos dimensiones distintas a propósito — estilo arquitectónico (Monolito Modular, Hexagonal, Microservicios) y estrategia de lógica de dominio (Transaction Script/Active Record, DDD) — porque ambas son relevantes para evaluar competencias de un candidato. Para gates de cobertura de pruebas **no se usa esta tabla**: `LIN-TEST-001 §5.1` define los umbrales por estilo arquitectónico (Monolito Simple, Monolito Modular, Hexagonal, Microservicio, EDA), y `LIN-TEST-001 §4.6` explica por qué la estrategia de lógica de dominio (Transaction Script, DDD) es una dimensión ortogonal que modula el *foco* de las pruebas unitarias pero no tiene un porcentaje de cobertura propio.

### 8.3 Criterios Técnicos de Aceptación y Entrega Formal de Software
Para que la OTI o el Área Usuaria otorgue la **conformidad técnica y aceptación formal de un entregable de software contratado**, el contratista deberá adjuntar y aprobar las siguientes evidencias en el pipeline CI/CD:
1. **Informe de SonarQube:** 0 vulnerabilidades de seguridad (*Security Hotspots / Blocker / Critical*), cero deuda técnica caduca en *Unleash Feature Toggles*, y cumplimiento de la cobertura mínima de pruebas automáticas según el estilo arquitectónico del proyecto — el umbral exacto por estilo es normado exclusivamente en **`LIN-TEST-001 §5.1`** (dueño de este tema; no se duplica aquí para evitar que ambos documentos queden desalineados).
2. **Evidencia de Cumplimiento Core Web Vitals:** Reporte automatizado de Lighthouse en el pipeline CI/CD demostrando un LCP < 2.5s, INP < 200ms y CLS < 0.1 en las pantallas entregadas.
3. **Evidencia de Observabilidad Completa:** Captura de pantalla y traza de prueba funcional ejecutada en el clúster de QA donde se compruebe la presencia simultánea de las cuatro señales en el Dashboard de Grafana (`LIN-OBS-001`) y la traza distribuida continua en Jaeger sin cortes de context propagation.
4. **Declaración de Conformidad con LIN-ARQ-001:** Declaración jurada técnica en el `README.md` del repositorio firmada por el Tech Lead de la fábrica, certificando la ausencia de importaciones entre fronteras prohibidas en el Monolito Modular (`LIN-DIS-001 §3.4`).

### 8.4 Validación Técnica en el Proceso de Contratación

Prueba técnica recomendada por perfil, a aplicar durante la evaluación de ingreso del personal propuesto por la fábrica de software:

| Perfil | Prueba técnica | Tiempo |
|---|---|---|
| Base + Transaction Script | Implementar un CRUD con validaciones de negocio, manejo de excepciones y pruebas unitarias. | 3 horas |
| Monolito Modular | Diseñar la estructura de módulos Maven para un sistema dado y justificar las decisiones de fronteras. | 2 horas |
| Hexagonal | Implementar un *port* de salida con su *adapter* y prueba de integración con WireMock. | 4 horas |
| Microservicios | Diseñar la comunicación entre dos servicios incluyendo manejo de fallo (Circuit Breaker) y trazabilidad. | 3 horas |
| DDD | Modelar un Agregado con su lógica de dominio, eventos de dominio y pruebas unitarias puras (sin Spring). | 5 horas |

---

## Apéndice A — Matriz de Decisiones Arquitectónicas (ADRs de Referencia Institucional)

La siguiente tabla compendia las decisiones históricas y vigentes adoptadas por el Comité de Arquitectura de la OTI, las cuales sustentan y dan fuerza normativa al presente Marco Rector (`LIN-ARQ-001`) y sus lineamientos derivados:

| ID del ADR | Título y Decisión Arquitectónica Registrada | Fecha | Estado |
|---|---|---|---|
| **ADR-001** | **Stack Backend Oficial:** Java LTS (actual 21) + Spring Boot 3.x + Apache Maven es el ecosistema de codificación mandatorio para todo backend institucional. | 2026-05-21 | Aceptada / Vigente |
| **ADR-002** | **Monolito Modular por Defecto:** Se adopta el Monolito Modular como la topología por defecto para todo proyecto nuevo de software previsional. | 2026-05-21 | Aceptada / Vigente |
| **ADR-003** | **Criterios de Microservicios y DDD (dos tablas independientes bajo un mismo ADR):** (a) La extracción de un módulo a microservicio exige el cumplimiento simultáneo de los 6 criterios de `§2.1` (dominio autónomo, soberanía de datos, escalamiento asimétrico, ciclo de vida independiente, consistencia eventual, capacidad SRE). (b) La adopción plena de DDD táctico exige el cumplimiento simultáneo de los 6 criterios de `LIN-DIS-001 §3.0` (sistema core, reglas complejas, experto de dominio disponible, equipo con experiencia previa, vida útil larga, bounded context delimitado). **Ambas tablas se evalúan de forma independiente** — cumplir una no implica cumplir la otra; un microservicio no requiere DDD, y un módulo del Monolito Modular puede adoptar DDD sin extraerse. | 2026-05-21 | Aceptada / Vigente |
| **ADR-004** | **Patrón Strangler Fig para Migración:** Toda migración y modernización de sistemas del Estadio 1 (JBoss/Legacy) debe realizarse gradualmente usando la Higuera Estranguladora. | 2026-05-21 | Aceptada / Vigente |
| **ADR-005** | **Anti-Corruption Layer (ACL) Gubernamental:** Es obligatorio implementar una capa ACL en el lado del consumidor para toda consulta o intercambio de datos con RENIEC, SUNAT y PIDE. | 2026-05-21 | Aceptada / Vigente |
| **ADR-006** | **Angular como Framework Primario SPA:** Angular es el estándar primario por defecto para todo frontend web nuevo; React o Vue requieren justificación por ADR. | 2026-05-21 | Aceptada / Vigente |
| **ADR-007** | **Core Web Vitals en CI/CD:** Se adoptan las métricas LCP (<2.5s), INP (<200ms) y CLS (<0.1) como gates obligatorios de calidad de UI/UX antes del pase a producción. | 2026-05-21 | Aceptada / Vigente |
| **ADR-008** | **Declaración Obligatoria CAP (CP vs AP):** Todo microservicio o módulo distribuido debe declarar y sustentar en su ADR de extracción si prioriza Consistencia (CP) o Disponibilidad (AP). | 2026-05-21 | Aceptada / Vigente |
| **ADR-009** | **Runtime de Contenedores containerd:** El clúster Kubernetes de producción opera con `containerd`. Docker Engine solo está autorizado en entornos locales o etapa transitoria. | 2026-05-21 | Aceptada / Vigente |
| **ADR-010** | **Observabilidad como Pilar de Producción:** Trazas distribuidas, logs estructurados JSON, métricas (Four Golden Signals) y probes K8s son obligatorios antes del go-live. | 2026-05-21 | Aceptada / Vigente |
| **ADR-011** | **Kubernetes como Destino por Defecto:** K8s es el destino habitual; el uso de Máquinas Virtuales dedicadas se limita a 4 criterios técnicos excepcionales con ADR. | 2026-05-21 | Aceptada / Vigente |
| **ADR-012** | **Apache Kafka como Broker Institucional:** Se oficializa a Apache Kafka (`LIN-BUS-001`) como el único canal institucional de mensajería y eventos asíncronos para EDA. | 2026-06-05 | Aceptada / Vigente |
| **ADR-013** | **CloudEvents v1.0 como Estándar de Eventos:** Todo evento publicado en los tópicos institucionales de Kafka debe ajustarse a la especificación estándar CNCF CloudEvents v1.0. | 2026-06-08 | Aceptada / Vigente |
| **ADR-014** | **Unleash para Feature Toggles On-Premise:** Se adopta Unleash self-hosted como herramienta oficial para Feature Toggles en Trunk-Based Development (*LIN-VER-001*). | 2026-07-02 | Aceptada / Vigente |

---

## Apéndice B — Glosario Rápido de Arquitectura y Gobernanza

| Término / Acrónimo | Definición y Contexto Operativo en la ONP |
|---|---|
| **2PC (*Two-Phase Commit*)** | Protocolo transaccional de compromiso en dos etapas para coordinar cambios transaccionales en bases de datos distribuidas. **Estrictamente prohibido entre microservicios** en la ONP para evitar bloqueos y latencias en red; reemplazado por el patrón **Saga**. |
| **ACID** | Propiedades transaccionales de *Atomicidad, Consistencia, Aislamiento y Durabilidad*. Garantía mandatoria del motor **Oracle** (`@Transactional`) para los cálculos y saldos del core previsional. |
| **ACL (*Anti-Corruption Layer*)** | Patrón de diseño que actúa como escudo protector mediante clases traductoras (`Mappers`/`Adapters`), evitando que modelos externos de terceros o legados contaminen el dominio limpio de la OTI. |
| **ADR (*Architecture Decision Record*)** | Documento auditable en Git que formaliza el contexto, evaluación y justificación técnica para adoptar una decisión o permitir una desviación excepcional del estándar institucional. |
| **Bounded Context** | Límite explícito de un subdominio o módulo en Domain-Driven Design (DDD) dentro del cual un modelo de datos y reglas de negocio tienen significado único, cohesivo y soberano (`LIN-DIS-001`). |
| **C4 Model** | Metodología de modelado arquitectónico de software creada por Simon Brown que estructura la documentación en 4 niveles de abstracción: *Contexto, Contenedor, Componente y Código*. |
| **CAP (Teorema CAP)** | Teorema que demuestra la imposibilidad de garantizar de forma simultánea Consistencia (C), Disponibilidad (A) y Tolerancia a Particiones (P) ante cortes de red en sistemas distribuidos. |
| **CloudEvents v1.0** | Especificación abierta de la *Cloud Native Computing Foundation* (CNCF) para describir datos de eventos de forma universal, facilitando la interoperabilidad entre servicios y buses institucionales. |
| **Core Web Vitals** | Marco de Google que mide la velocidad del rendimiento real del usuario y la respuesta de la interfaz en tres dimensiones: carga visual (LCP), interactividad (INP) y estabilidad de diseño (CLS). |
| **CQRS (*Command Query Responsibility Segregation*)** | Patrón de diseño táctico que separa físicamente los modelos y rutas de escritura de datos (*Commands*) respecto de las rutas de lectura e informes (*Queries*), optimizándolas en almacenes distintos. |
| **EDA (*Event-Driven Architecture*)** | Estilo arquitectónico basado en la producción, detección y consumo asíncrono de eventos de negocio a través de un bus centralizado (**Apache Kafka** en la ONP). |
| **Feature Toggle / Unleash** | Técnica de ingeniería de software para activar o apagar funcionalidades en tiempo de ejecución sin redesplegar código, gestionada de forma centralizada mediante la plataforma on-premise **Unleash**. |
| **Four Golden Signals** | Las cuatro señales de monitoreo recomendadas por la ingeniería SRE de Google para supervisar la salud de un servicio de producción: **Latencia, Tráfico, Errores y Saturación**. |
| **Monolito Modular** | Topología arquitectónica donde el sistema corre en un único proceso y contenedor, pero mantiene internamente una estricta separación modular con paquetes independientes y cero acoplamientos ocultos. |
| **OpenTelemetry / Jaeger** | Estándar e infraestructura institucional para recopilar, generar y exportar datos de telemetría, trazas distribuidas y métricas desde los contenedores hacia las consolas de Jaeger y Grafana (`LIN-OBS-001`). |
| **Saga (Patrón Saga)** | Mecanismo de gestión transaccional para sistemas distribuidos basado en una secuencia de transacciones locales coordinadas mediante eventos asíncronos o coreografía, con operaciones de compensación ante fallos. |
| **Strangler Fig (*Higuera Estranguladora*)** | Patrón de migración de software que reemplaza progresivamente piezas funcionales de un sistema heredado por nuevas aplicaciones, enrutando el tráfico desde un API Gateway hasta apagar el legado. |
| **Transactional Outbox** | Patrón para emitir mensajes y eventos al bus de forma 100% confiable, guardando primero el evento en una tabla de la misma base de datos relacional dentro de la transacción local del negocio. |
