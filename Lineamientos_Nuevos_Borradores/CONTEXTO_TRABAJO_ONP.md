# Contexto de Trabajo — Framework de Arquitectura ONP

**Propósito de este documento:**
Captura el razonamiento compartido entre el arquitecto (Carlos) y el asistente (IA) sobre el framework de arquitectura ONP. Sirve como punto de reorientación cuando la conversación se pierde, cuando retomamos después de un tiempo, o cuando necesitamos recordar por qué tomamos ciertas decisiones.

No es un lineamiento. No es una norma. Es el mapa del pensamiento.

---

## 0. Si eres una IA nueva — lee esto primero

Antes de responder cualquier consulta sobre el framework de arquitectura ONP, lee los siguientes documentos en este orden. Son los documentos aprobados formalmente y constituyen la fuente de verdad institucional. Lo que CONTEXTO resume de ellos puede estar desactualizado si han cambiado.

| # | Documento | Ruta | Qué buscar |
|---|---|---|---|
| 1 | Lineamiento del Modelo de Arquitectura de TI | `Lineamientos_Aprobados/1.1.1. Lineamiento del modelo de Arquitectura de TI (09.06.2025).pdf` | Estructura del modelo (4 componentes: Contexto, Gobernanza, Entrega, Observación), roles (Especialista y Analista de Arquitectura), metodología (TOGAF + Zachman) |
| 2 | Visión de Arquitectura de TI v1.0 | `Lineamientos_Aprobados/1.2.1. Visión de Arquitectura de TI ver. 1.0 (14.08.2025).pdf` | Declaración de visión (flexible, escalable, segura) — 4 páginas, lectura rápida |
| 3 | Principios de Arquitectura de TI v1.0 | `Lineamientos_Aprobados/1.3.1. Documento de Principios de Arquitectura de TI v1.0 (14.10.2025)).pdf` | Los 11 principios PA0001–PA0011 con su dominio (APP / DAT / APP+DAT) |
| 4 | Lineamiento Arquitectura Patrón Apps y BD v1.0 | `Lineamientos_Aprobados/1.4.2.a Lineamiento sobre la Arquitectura patrón de Aplicaciones y Base de Datos v1.0.pdf` | **El más importante.** 8 capas, 3 estilos, 12 patrones oficiales (PT01–PT12) con ficha. LIN-ARQ-000 lo reemplazará cuando se apruebe. |
| 5 | Lineamiento Analítica de Datos v1.0 | `Lineamientos_Aprobados/1.4.3.a. Lineamiento de Explotación y Analítica de Datos v1.0.pdf` | Arquitectura para la capa analítica — relevante si el tema involucra datos o BI |

Luego lee los documentos en borrador activo:

| Documento | Ruta | Qué es |
|---|---|---|
| LIN-ARQ-000 | `Lineamientos_Nuevos_Borradores/arquitectura/Lineamiento_Diseno_Arquitectura_Software_ONP_v0.1.0.md` | Sucesor del lineamiento de Aplicaciones/BD. El documento más importante del trabajo en curso. |
| Brecha_Framework | `Lineamientos_Nuevos_Borradores/arquitectura/Brecha_Framework_Arquitectura_ONP_v0.1.0.md` | Tablero de control de qué falta documentar. Refleja el estado actual del framework. |

Con eso tienes el contexto suficiente para trabajar sin asumir cosas que no son ciertas.

---

## 1. Dónde estamos y hacia dónde vamos

ONP tiene hoy un parque de aplicativos mayoritariamente **monolíticos**, algunos con décadas de antigüedad. El objetivo no es reemplazarlos de golpe — es evolucionar ordenadamente hacia una arquitectura moderna, sostenible y operable.

El camino definido es:

```
Monolito puro (legacy)
    ↓  Strangler Fig
Monolito Modular          ← destino por defecto hoy
    ↓  Extracción gradual (criterios explícitos en LIN-ARQ-000)
Microservicios            ← futuro, cuando el contexto lo justifique
```

EDA, Saga, Kafka, CloudEvents — todo esto ya está pensado y documentado porque **el presente de ONP ya lo necesita**, no porque querramos microservicios. Cinco monolitos con BDs propias que necesitan coordinarse es un problema real hoy.

---

## 2. Los documentos y su rol

### La jerarquía completa

```
NIVEL 0 — Documento apex (fuente de verdad del modelo)
└── Lineamiento del Modelo de Arquitectura de TI (06/06/2025)
        Define la estructura, roles, artefactos y metodología (TOGAF 10ª ed. + Zachman)
        del modelo de arquitectura ONP. De él se derivan todos los demás documentos.
        ↓
NIVEL 1 — Documentos aprobados formalmente (vigentes)
├── Visión de Arquitectura de TI v1.0 (14.08.2025)
├── Principios de Arquitectura de TI v1.0 (14.10.2025)
├── Lineamiento Arquitectura Patrón Aplicaciones y BD v1.0  ← será reemplazado por LIN-ARQ-000
└── Lineamiento Explotación y Analítica de Datos v1.0
        ↓  LIN-ARQ-000 reemplaza el Lineamiento de Aplicaciones/BD al ser aprobado
NIVEL 2 — Lineamiento rector de Arquitectura de TI (en borrador — sucesor del Nivel 1 de Aplicaciones/BD)
└── LIN-ARQ-000 — Lineamiento de Diseño y Arquitectura de Software
        ↓  Los demás lineamientos se alinean a LIN-ARQ-000 y al Nivel 1
NIVEL 3 — Lineamientos de dominio específico
├── LIN-BUS-001 — Mensajería y Bus de Eventos
├── LIN-OBS-001 — Observabilidad
├── LIN-API-REST-001 — APIs REST
├── LIN-K8S-001 — Contenedores y Orquestación
├── LIN-SEC-APP-001 — Seguridad en Aplicaciones
├── LIN-DEV-JAVA-001 — Estándar de Desarrollo Java
└── otros lineamientos de dominio...
```

**Regla fundamental:** ningún lineamiento de Nivel 3 puede contradecir a LIN-ARQ-000 ni a los documentos de Nivel 1. Son la implementación específica de un dominio dentro del gobierno general.

---

### Cadena de roles

El framework está diseñado para servir a tres roles distintos:

```
Arquitecto   → Define estilos, patrones y decisiones de sistema (LIN-ARQ-000)
     ↓
Diseñador    → Materializa esas decisiones en artefactos por proyecto:
               diagramas, modelos de dominio, contratos, diseño de clases
     ↓
Desarrollador → Implementa siguiendo el diseño (LIN-DEV-JAVA-001 y lineamientos de dominio)
```

**Estado actual:** el arquitecto ejerce también el rol de diseñador por capacidad de personal. Esto es temporal — en el futuro el diseñador será una persona separada con su propio lineamiento (`LIN-DISENO-001`, pendiente de crear cuando el rol se formalice).

**Implicación para el framework:** cuando `LIN-DISENO-001` exista, deberá definir qué artefactos produce el diseñador antes de que comience el desarrollo, en qué herramienta y con qué nivel de detalle según el tipo de proyecto.

---

### NIVEL 0 — Documento apex

| Documento | Fecha aprobación | Rol |
|---|---|---|
| Lineamiento del Modelo de Arquitectura de TI | 06/06/2025 | Documento apex. Define la estructura, roles, artefactos y metodología (TOGAF 10ª ed. + Zachman) del modelo de arquitectura ONP. De él se derivan todos los demás documentos. |

---

### NIVEL 1 — Documentos aprobados

Estos documentos **ya están aprobados y vigentes**. Son la base institucional sobre la que se construye todo el framework.

| Documento | Fecha aprobación | Rol |
|---|---|---|
| Visión de Arquitectura de TI v1.0 | 14.08.2025 | Define hacia dónde va la arquitectura ONP a largo plazo |
| Principios de Arquitectura de TI v1.0 | 14.10.2025 | Declara los 11 principios (PA0001–PA0011) que gobiernan toda decisión técnica |
| Lineamiento Arquitectura Patrón Aplicaciones y BD v1.0 | 29.12.2025 | Define capas, estilos y 12 patrones oficiales — **será reemplazado por LIN-ARQ-000 al aprobarse** |
| Lineamiento Explotación y Analítica de Datos v1.0 | 24.12.2025 | Define el estándar para la capa analítica |

**Nota:** Durante la revisión de estos documentos y considerando a dónde queremos llegar, podemos sugerir mejoras a los documentos aprobados. Por ejemplo, un principio muy genérico que necesita precisión, o principios nuevos que surjan del trabajo en curso.

> **Verificación realizada (2026-07-01):** se leyeron los documentos de Nivel 0 y Nivel 1. No se encontraron contradicciones con lo construido en LIN-ARQ-000 ni en los lineamientos de dominio. El mecanismo ADR está sancionado formalmente por el Lineamiento de Aplicaciones/BD §9.

---

### NIVEL 2 — LIN-ARQ-000 — Lineamiento de Diseño y Arquitectura de Software

**Es el sucesor del Lineamiento de Arquitectura Patrón para Aplicaciones y BD.** Cuando sea aprobado formalmente, reemplazará ese documento de Nivel 1. Hasta entonces opera en borrador como la norma de trabajo del equipo de arquitectura.

Su rol específico:
- Reemplazar y ampliar el Lineamiento de Aplicaciones/BD con decisiones ONP concretas
- Agregar estilos no contemplados en el lineamiento aprobado (Hexagonal, Monolito Modular, evolución gradual)
- Establecer reglas ONP explícitas sobre cuándo y cómo usar cada patrón ("en ONP se hace así, punto")
- Registrar ADRs que documentan el razonamiento detrás de cada decisión importante

Lo que NO hace: contradecir el Lineamiento Modelo (NIVEL 0) ni los Principios de Arquitectura de TI (NIVEL 1).

---

### NIVEL 3 — Lineamientos de dominio

Cada uno cubre un dominio técnico específico y debe alinearse tanto a LIN-ARQ-000 como a los documentos de Nivel 1.

| Lineamiento | Dominio |
|---|---|
| LIN-BUS-001 | Mensajería y Bus de Eventos — Kafka, CloudEvents, Saga, DLQ |
| LIN-OBS-001 — Observabilidad | Trazas, logs, métricas, health checks |
| LIN-API-REST-001 | APIs REST — diseño, versionado, WSO2 |
| LIN-K8S-001 | Contenedores, orquestación, despliegue |
| LIN-SEC-APP-001 | Seguridad en aplicaciones, autenticación, SAA |
| LIN-DEV-JAVA-001 | Estándar de desarrollo Java + Spring Boot |

---

### Documentos de trabajo (no normativos)

| Documento | Rol |
|---|---|
| Brecha_Framework_Arquitectura_ONP_v0.1.0.md | Tablero de control — qué falta documentar en el framework |
| CONTEXTO_TRABAJO_ONP.md | Este documento — mapa del razonamiento compartido |
| ADR-WSO2-001, ADR-CLOUDEVENTS-001 | Decisiones de arquitectura puntuales con su razonamiento |

---

### Insumos de referencia (no normativos, no se citan formalmente)

| Documento | Rol |
|---|---|
| Arquitectura de referencia v0.1 (Avance a Dic.2025).docx | Borrador de ideas del equipo de arquitectura. Útil para no partir de cero, pero LIN-ARQ-000 puede coincidir, mejorar o corregir lo que dice sin necesidad de justificarlo. |

---

## 3. Principios de cómo escribimos los lineamientos

**El nivel de detalle y didáctica depende de la audiencia del lineamiento.**
No todos los lineamientos son iguales. Algunos dirigen al arquitecto que ya domina los patrones; otros llegan a un desarrollador que viene de otra empresa, otra cultura, otro estilo. El principio rector es: **cada lineamiento prescribe siempre, y enseña tanto como su audiencia lo necesite.**

| Lineamiento | Audiencia | Estilo |
|---|---|---|
| LIN-ARQ-000 | Arquitecto | Prescriptivo — decisiones y reglas; el "por qué" va en el ADR |
| LIN-BUS-001 | Arquitecto + desarrollador senior | Prescriptivo con ejemplos técnicos de configuración |
| LIN-OBS-001 | Desarrollador + operaciones | Prescriptivo con ejemplos de configuración y snippets |
| LIN-DEV-JAVA-001 | Desarrollador | Prescriptivo + didáctico + ejemplos de código ONP |

**El lenguaje siempre es prescriptivo, no sugerido.**
- "se recomienda" → "se usa"
- "idealmente" → "es obligatorio"
- "considerar" → "se debe" o "se prohíbe"

**El "por qué" de una decisión arquitectónica va en el ADR, no en el lineamiento.**
El lineamiento dice qué hacer. Si alguien quiere entender la decisión de fondo, lee el ADR correspondiente.

**La diferencia entre prescribir y enseñar:**

> ❌ "Se recomienda usar el patrón Repository para abstraer el acceso a datos"
>
> ✅ (prescribir) "En ONP, el Repository es un puerto en la capa de dominio. La implementación JPA va en infraestructura. No va en el servicio de aplicación."
>
> ✅ (prescribir + enseñar, para LIN-DEV-JAVA-001) Lo anterior, más un ejemplo de código que muestra cómo se estructura en un proyecto Spring Boot ONP real.

**Los patrones GoF y principios SOLID se exigen, no se explican desde cero.**
Son competencia esperada del desarrollador. Lo que sí va en el lineamiento es la decisión ONP de cómo aplicarlos cuando esa decisión no es obvia — y en LIN-DEV-JAVA-001 se ilustra con ejemplos concretos del estilo ONP.

---

## 4. Decisiones tomadas hasta ahora

### EDA no requiere microservicios
EDA es una decisión de comunicación, no de estructura interna. El Monolito Modular puede usar EDA para comunicarse con sistemas externos o coordinar flujos Saga con otros aplicativos. Esto está documentado en LIN-ARQ-000 §3.6.

### Saga aplica sobre monolitos
El parque actual de ONP — monolitos con BDs propias — justifica Saga. La variante documentada: orquestador + Kafka como canal + REST como mecanismo de ejecución en cada monolito. El monolito no sabe que está en una Saga. LIN-ARQ-000 §3.6 + LIN-BUS-001 §9.4.

### CloudEvents v1.0 como estándar de envelope
Adoptado para garantizar interoperabilidad futura con otras instituciones del Estado y el ecosistema cloud-native. El envelope propietario anterior fue reemplazado. ADR-CLOUDEVENTS-001 documenta la decisión.

### WSO2 API Manager como plataforma objetivo
Hoy en PoC — no operativo. SAA + SaaTokenValidationFilter es el mecanismo vigente. LIN-API-REST-001 §2.5 refleja esto con claridad. ADR-WSO2-001 gobierna la transición.

### Los patrones de la brecha no son todos obligatorios
La brecha es un mapa, no una lista de tareas. Varios ítems pueden quedarse sin documentar si ONP no los va a necesitar en su roadmap actual.

---

## 5. Lo que sigue (plan de trabajo)

### Contexto del plan — LIN-ARQ-000 como superconjunto

LIN-ARQ-000 debe ser un **superconjunto** del Lineamiento de Arquitectura Patrón para Aplicaciones y BD v1.0 (el documento que reemplazará). Eso significa que debe contener **todo** lo que ese lineamiento tiene, más las decisiones ONP adicionales ya construidas. El Lineamiento de Aplicaciones/BD define 12 patrones oficiales (PT01–PT12) con ficha. LIN-ARQ-000 debe cubrirlos todos — no redefinirlos desde cero, sino adoptarlos con las reglas ONP específicas.

### Verificado (2026-07-01)
- Lectura de documentos NIVEL 0 y NIVEL 1 completada — sin contradicciones con lo construido.
- Mecanismo ADR sancionado formalmente por §9 del Lineamiento de Aplicaciones/BD.

### Inmediato — completar LIN-ARQ-000 como sucesor

Los 12 patrones oficiales del Lineamiento de Aplicaciones/BD deben estar todos en LIN-ARQ-000:

| Código oficial | Patrón | Estado en LIN-ARQ-000 |
|---|---|---|
| PT01 | Publisher/Subscriber | ✅ cubierto (LIN-BUS-001 §5) |
| PT02 | Dead Letter Queue | ✅ cubierto (LIN-BUS-001 §8.5–8.7) |
| PT03 | Event Sourcing | ❌ pendiente |
| PT04 | API Gateway | ✅ cubierto (LIN-API-REST-001 §2.5) |
| PT05 | Retry con backoff | ✅ parcial (LIN-BUS-001 §8.5) |
| PT06 | Circuit Breaker | ❌ pendiente — **alta prioridad** |
| PT07 | Bulkhead | ❌ pendiente |
| PT08 | Strangler Fig | ✅ cubierto (LIN-ARQ-000 §2.2) |
| PT09 | BFF (Backend for Frontend) | ❌ pendiente |
| PT10 | Gateway-Aggregation | ❌ pendiente |
| PT11 | Adapter / ACL | ✅ cubierto (LIN-ARQ-000 §3.5) |
| PT12 | Facade | ❌ pendiente |

Pendientes adicionales de alta prioridad (no están en el lineamiento oficial pero son transversales):

| Código brecha | Ítem | Documento destino |
|---|---|---|
| PI08 | Timeout — estándar para REST y Kafka | LIN-ARQ-000 |
| PRA06 | Design for Failure — principio transversal | LIN-ARQ-000 |
| PR01–PR05 | SOLID — cómo ONP lo aplica en Java | LIN-DEV-JAVA-001 |

### Mediano plazo — cuando el roadmap lo exija
Context Map, Shared Kernel, CQRS detallado — cuando la extracción de microservicios lo justifique.

### Pendiente de decisión
- ¿Qué ítems de la brecha quedan definitivamente fuera del scope ONP actual?
- ¿Quién es el responsable de documentar cada ítem pendiente?
- ¿Cuándo se revisa formalmente el estado de la brecha?

---

## 6. Lo que este documento NO es

- No es un lineamiento
- No es una norma
- No reemplaza a LIN-ARQ-000 ni a ningún otro lineamiento
- No es exhaustivo — se actualiza cuando el razonamiento evoluciona

---

## 7. Historial de actualizaciones

| Fecha | Qué se agregó o cambió |
|---|---|
| 2026-07-01 | Versión inicial — captura del razonamiento acumulado en la sesión de trabajo |
| 2026-07-01 | §3 actualizado — lineamientos prescriben siempre, enseñan según audiencia; tabla de estilos por lineamiento |
| 2026-07-01 | §2 actualizado — jerarquía corregida: NIVEL 0 (Lineamiento Modelo, apex), LIN-ARQ-000 como sucesor de Aplicaciones/BD; verificación de alineación con Nivel 0 y Nivel 1 completada |
