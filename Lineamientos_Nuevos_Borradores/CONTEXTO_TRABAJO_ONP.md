# Contexto de Trabajo — Framework de Arquitectura ONP

**Propósito de este documento:**
Captura el razonamiento compartido entre el arquitecto (Carlos) y el asistente (IA) sobre el framework de arquitectura ONP. Sirve como punto de reorientación cuando la conversación se pierde, cuando retomamos después de un tiempo, o cuando necesitamos recordar por qué tomamos ciertas decisiones.

No es un lineamiento. No es una norma. Es el mapa del pensamiento.

---

## 0. Si eres una IA nueva — lee esto primero

**Rol que debes asumir:** Eres un par técnico de arquitectura de software y revisor colaborativo del Especialista de Arquitectura de TI de ONP (Carlos Ormeño). Co-redactas borradores, revisas propuestas e identificas brechas. Carlos toma las decisiones finales.

**Dominio técnico esperado:**
- Patrones de arquitectura de software: GoF, PoEAA, EDA, DDD, Hexagonal, Microservicios, Saga, Circuit Breaker y familia de resiliencia
- Stack ONP: Java 21 / Spring Boot 3.x, Oracle DB, Apache Kafka, Kubernetes on-premise, Archi (ArchiMate 3.x)
- Observabilidad: OpenTelemetry, Prometheus, Grafana, Jaeger, Elasticsearch/Kibana
- Gobernanza técnica: redacción de lineamientos normativos, fichas de patrón, ADRs

**Contexto importante:** Hoy ONP trabaja arquitectura con foco en la dimensión técnica — software, plataforma, datos. El "Lineamiento del Modelo de Arquitectura de TI" referencia TOGAF 10ª ed. + Zachman como marco metodológico formal, pero en la práctica actual no se aplica la dimensión de negocio (modelado de capacidades, value streams, capas Zachman de negocio). Esto puede cambiar por decisión institucional — si Carlos lo menciona, ajusta el enfoque.

---

Antes de responder cualquier consulta sobre el framework de arquitectura ONP, lee los siguientes documentos en este orden. Son los documentos aprobados formalmente y constituyen la fuente de verdad institucional. Lo que CONTEXTO resume de ellos puede estar desactualizado si han cambiado.

| # | Documento | Ruta | Qué buscar |
|---|---|---|---|
| 1 | Lineamiento del Modelo de Arquitectura de TI | `Lineamientos_Aprobados/1.1.1. Lineamiento del modelo de Arquitectura de TI (09.06.2025).pdf` | Estructura del modelo (4 componentes: Contexto, Gobernanza, Entrega, Observación), roles (Especialista y Analista de Arquitectura), metodología (TOGAF + Zachman) |
| 2 | Visión de Arquitectura de TI v1.0 | `Lineamientos_Aprobados/1.2.1. Visión de Arquitectura de TI ver. 1.0 (14.08.2025).pdf` | Declaración de visión (flexible, escalable, segura) — 4 páginas, lectura rápida |
| 3 | Principios de Arquitectura de TI v1.0 | `Lineamientos_Aprobados/1.3.1. Documento de Principios de Arquitectura de TI v1.0 (14.10.2025)).pdf` | Los 11 principios PA0001–PA0011 con su dominio (APP / DAT / APP+DAT) |
| 4 | Lineamiento Arquitectura Patrón Apps y BD v1.0 | `Lineamientos_Aprobados/1.4.2.a Lineamiento sobre la Arquitectura patrón de Aplicaciones y Base de Datos v1.0.pdf` | **El más importante.** 8 capas, 3 estilos, 12 patrones oficiales (PT01–PT12) con ficha. LIN-ARQ-000 lo reemplazará cuando se apruebe. |
| 5 | Lineamiento Analítica de Datos v1.0 | `Lineamientos_Aprobados/1.4.3.a. Lineamiento de Explotación y Analítica de Datos v1.0.pdf` | Arquitectura para la capa analítica — relevante si el tema involucra datos o BI |

Luego lee los documentos en borrador activo (Nivel 2 y control de brechas):

| Documento | Ruta | Qué es |
|---|---|---|
| LIN-ARQ-000 | `Lineamientos_Nuevos_Borradores/arquitectura/Lineamiento_Diseno_Arquitectura_Software_ONP_v0.1.0.md` | Sucesor del lineamiento de Aplicaciones/BD. El documento más importante del trabajo en curso. Léelo completo. |
| Brecha_Framework | `Lineamientos_Nuevos_Borradores/arquitectura/Brecha_Framework_Arquitectura_ONP_v0.1.0.md` | Tablero de control de qué falta documentar. Refleja el estado actual del framework. |

Si el tema involucra un dominio técnico específico, lee también el lineamiento de Nivel 3 correspondiente. Todos dependen de LIN-ARQ-000 — ninguno puede contradecirlo:

| Código | Ruta | Dominio | Qué buscar |
|---|---|---|---|
| LIN-BUS-001 | `Lineamientos_Nuevos_Borradores/mensajeria/Lineamiento_Mensajeria_Bus_Eventos_ONP_v0.1.0.md` | Mensajería y Bus de Eventos | Kafka topics, CloudEvents v1.0, variantes Saga, DLQ, idempotencia, retry con backoff, Transactional Outbox |
| LIN-OBS-001 | `Lineamientos_Nuevos_Borradores/observabilidad/Lineamiento_Log_Trazabilidad_Observabilidad_ONP_v0.1.0.md` | Observabilidad | Logback/ECS, métricas Prometheus, trazas OTEL, orden de filtros (RequestId→SaaToken→Log), Four Golden Signals |
| LIN-API-REST-001 | `Lineamientos_Nuevos_Borradores/Web/Lineamiento_Estandar_APIs_REST_ONP_v0.1.0.md` | APIs REST | Naming conventions, versionado, paginación, manejo de errores, WSO2, contratos OpenAPI 3.0 |
| LIN-K8S-001 | `Lineamientos_Nuevos_Borradores/contenedores/Lineamiento_Contenedores_Orquestacion_ONP_v0.1.0.md` | Contenedores y Orquestación K8s | Estructura Deployment/Service/ConfigMap, namespaces ONP, readiness/liveness probes, GitLab Container Registry |
| LIN-SEC-APP-001 | `Lineamientos_Nuevos_Borradores/seguridad/Lineamiento_Seguridad_Aplicaciones_ONP_v0.1.0.md` | Seguridad en Aplicaciones | Autenticación SAA, JWT, HTTPS obligatorio, OWASP Top 10, headers de seguridad, protección de endpoints |
| LIN-DEV-JAVA-001 | `Lineamientos_Nuevos_Borradores/desarrollo/Lineamiento_Estandar_Desarrollo_Java_ONP_v0.1.0.md` | Desarrollo Java 21 / Spring Boot 3 | Estructura de paquetes, estilo de código ONP, DDD building blocks en Spring — SOLID/DRY/KISS/YAGNI pendiente completar (⚠️ Parcial) |
| LIN-BD-ORA-001 | `Lineamientos_Nuevos_Borradores/Datos/Lineamiento_Estandar_Base_de_Datos_ONP_v0.1.0.md` | Base de Datos Oracle | Naming de tablas/columnas/índices, transacciones ACID, acceso desde Spring Boot, prohibiciones de acceso directo |
| LIN-FE-ANG-001 | `Lineamientos_Nuevos_Borradores/Web/Lineamiento_Estandar_Diseno_Web_Frontend_ONP_v0.1.0.md` | Frontend Angular | Estructura de módulos, consumo de APIs REST, manejo de sesión SAA, accesibilidad |
| LIN-VER-001 | `Lineamientos_Nuevos_Borradores/versionamiento/Lineamiento_Versionamiento_Control_Cambios_ONP_v0.1.1.md` | Versionamiento y Control de Cambios | SemVer, GitFlow simplificado (feature/release/hotfix), convención de commits, MR templates GitLab |
| LIN-TEST-001 | `Lineamientos_Nuevos_Borradores/pruebas/Lineamiento_Estandar_Pruebas_ONP_v0.1.0.md` | Pruebas de Software | Pirámide de pruebas, JUnit 5 + Mockito, pruebas de integración, cobertura mínima, gates de calidad |
| LIN-PERF-001 | `Lineamientos_Nuevos_Borradores/pruebas/Lineamiento_Pruebas_Rendimiento_Carga_Estres_ONP_v0.1.0.md` | Pruebas de Rendimiento | JMeter / Gatling, SLA, pruebas de carga y estrés, criterios de aceptación de rendimiento |
| LIN-CICD-001 | `Lineamientos_Nuevos_Borradores/CICD/Lineamiento_Integracion_Entrega_Continua_ONP_v0.1.0.md` | CI/CD | Pipelines GitLab CI, stages (build/test/scan/deploy), gates de calidad automáticos, artefactos |
| LIN-IaC-001 | `Lineamientos_Nuevos_Borradores/Infraestructura/Lineamiento_Infraestructura_Código_ONP_v0.1.0.md` | Infraestructura como Código | Terraform / Ansible para K8s on-premise, gestión de secretos, provisioning reproducible |
| LIN-BI-001 | `Lineamientos_Nuevos_Borradores/Datos/Lineamiento_Explotacion_Analitica_Datos_BI_ONP_v0.1.0.md` | Business Intelligence / Analítica | Medallion architecture, Parquet, Nessie, OpenMetadata, pipelines de datos analíticos |

Documentos de trabajo adicionales útiles:

| Documento | Ruta | Rol |
|---|---|---|
| START_HERE | `Lineamientos_Nuevos_Borradores/START_HERE_Proyecto_Java_ONP.md` | Checklist de inicio rápido para un proyecto Spring Boot ONP — primer documento que lee un desarrollador |
| GLOSARIO_ONP | `Lineamientos_Nuevos_Borradores/GLOSARIO_ONP.md` | Glosario de términos técnicos y de negocio ONP — consultar ante ambigüedades de nomenclatura |
| Matriz_Propiedad_Documental | `Lineamientos_Nuevos_Borradores/Matriz_Propiedad_Documental_ONP_v0.1.0.md` | Quién es responsable de cada documento del framework |
| Plantilla_Documento_Arquitectura | `Lineamientos_Nuevos_Borradores/arquitectura/Plantilla_Documento_Arquitectura_ONP_v1.2.md` | Plantilla oficial para documentos de arquitectura de proyecto (estructura C4 conceptual + ArchiMate) |

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

| Código | Dominio | Estado |
|---|---|---|
| LIN-BUS-001 | Mensajería y Bus de Eventos — Kafka, CloudEvents, Saga, DLQ, idempotencia | Borrador |
| LIN-OBS-001 | Observabilidad — trazas OTEL, logs Logback/ECS, métricas Prometheus, filtros | Borrador |
| LIN-API-REST-001 | APIs REST — diseño, naming, versionado, paginación, WSO2, OpenAPI 3.0 | Borrador |
| LIN-K8S-001 | Contenedores y Orquestación — K8s on-premise, namespaces, probes, Registry | Borrador |
| LIN-SEC-APP-001 | Seguridad en Aplicaciones — SAA, JWT, OWASP, headers de seguridad | Borrador |
| LIN-DEV-JAVA-001 | Desarrollo Java 21 / Spring Boot 3 — estructura, DDD en Spring, SOLID (⚠️ pendiente completar) | Borrador |
| LIN-BD-ORA-001 | Base de Datos Oracle — naming, ACID, acceso desde Spring, prohibiciones | Borrador |
| LIN-FE-ANG-001 | Frontend Angular — módulos, consumo REST, sesión SAA, accesibilidad | Borrador |
| LIN-VER-001 | Versionamiento y Control de Cambios — SemVer, GitFlow, commits, MR templates | Borrador |
| LIN-TEST-001 | Pruebas de Software — pirámide, JUnit 5, Mockito, integración, cobertura mínima | Borrador |
| LIN-PERF-001 | Pruebas de Rendimiento — JMeter/Gatling, SLA, carga y estrés | Borrador |
| LIN-CICD-001 | CI/CD — pipelines GitLab CI, stages, gates de calidad, artefactos | Borrador |
| LIN-IaC-001 | Infraestructura como Código — Terraform/Ansible para K8s on-premise | Borrador |
| LIN-BI-001 | Business Intelligence / Analítica — Medallion, Parquet, Nessie, OpenMetadata | Borrador |

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
| PT05 | Retry con backoff | ✅ cubierto (LIN-ARQ-000 §3.7.2 + LIN-BUS-001 §8.5) |
| PT06 | Circuit Breaker | ✅ cubierto (LIN-ARQ-000 §3.7.3) |
| PT07 | Bulkhead | ✅ cubierto (LIN-ARQ-000 §3.7.1–3.7.2) |
| PT08 | Strangler Fig | ✅ cubierto (LIN-ARQ-000 §2.2) |
| PT09 | BFF (Backend for Frontend) | ✅ cubierto (LIN-ARQ-000 §3.8.3) |
| PT10 | Gateway-Aggregation | ✅ cubierto (LIN-ARQ-000 §3.8.2) |
| PT11 | Adapter / ACL | ✅ cubierto (LIN-ARQ-000 §3.5 y §3.9.1) |
| PT12 | Facade | ✅ cubierto (LIN-ARQ-000 §3.8.1) |

Pendientes adicionales de alta prioridad (no están en el lineamiento oficial pero son transversales):

| Código brecha | Ítem | Documento destino | Estado |
|---|---|---|---|
| PI08 | Timeout — estándar para REST y Kafka | LIN-ARQ-000 | ✅ cubierto (§3.7.1) |
| PRA06 | Design for Failure — principio transversal | LIN-ARQ-000 | ✅ cubierto (§3.7) |
| PR01–PR05 | SOLID — cómo ONP lo aplica en Java | LIN-ARQ-000 §7.1 + LIN-DEV-JAVA-001 | ⚠️ Parcial — declaración arquitectónica ✅ en §7.1; guía Java/Spring ❌ pendiente en LIN-DEV-JAVA-001 |

### Plan de Acción Unificado y Secuencial (Acordado el 2026-07-02)

Tras cerrar los bloques tácticos de DDD (`PD01` Agregados, `PD02` Entidades y Value Objects en §6.4.1) y consolidar `LIN-ARQ-000` al 100% como superconjunto de los 12 patrones oficiales, se define la siguiente secuencia ejecutiva de cierre de brechas antes de descender al código de Nivel 3:

#### Fase 1: Cierre de Brechas Restantes en Nivel 2 (`LIN-ARQ-000`) — ✅ COMPLETADA Y AUDITADA (v0.1.17)
1. **`PD10` (Published Language — Contratos de eventos + CloudEvents):** ✅ **CUBIERTO en LIN-ARQ-000 §3.9.3**. Se normó el contrato público exterior entre Bounded Contexts vía CloudEvents v1.0 (asíncrono sobre Kafka) y OpenAPI 3.0 (síncrono REST), prohibiendo exponer el modelo interno (`domain.model.*`).
2. **`PA14` (Feature Toggle — Complemento de Strangler Fig):** ✅ **CUBIERTO en LIN-ARQ-000 §2.2.1**. Se normaron los 4 tipos de toggles (*Release*, *Ops*, *Experiment*, *Permission*), el mandato de Deuda Técnica Cero para borrarlos tras liberar la migración y la integración con *Branch by Abstraction*. Se oficializó Unleash / Spring Cloud Config como estándar on-premise.
3. **`E07` (SOA — Interoperabilidad Gubernamental: PIDE, RENIEC, SUNAT):** ✅ **CUBIERTO en LIN-ARQ-000 §3.8.4**. Se normó el modelo G2G/B2G para consumo del Estado, delegando los umbrales a la Matriz de Timeouts (§3.7.1) y exigiendo aislamiento por Anti-Corruption Layer (`PT11`) y resiliencia en WSO2.

#### Fase 2: Descenso Normativo al Nivel 3 (`LIN-DEV-JAVA-001`) — ✅ COMPLETADA (v0.1.2)
4. **Cierre al 100% de Principios de Código (`PR01–PR08`) y Patrones Tácticos DDD (`PD04–PD06`):**
   - **`PR01–PR05` (SOLID en Spring Boot 3 / Java 21):** ✅ **CUBIERTO en LIN-DEV-JAVA-001 §10.4.1**. Se normó la aplicación práctica de cada principio (S/O/L/I/D) con ejemplos ONP — patrón Estrategia inyectado por Spring para OCP/DIP — y su anti-patrón prohibido correspondiente.
   - **`PR06–PR08` (DRY, KISS, YAGNI):** ✅ **CUBIERTO en LIN-DEV-JAVA-001 §10.4.2–§10.4.4**. Se normó el límite arquitectónico de DRY (duplicación de conocimiento de negocio vs. acoplamiento entre módulos), el uso de Records/Sealed Classes de Java 21 para KISS, y la prohibición de abstracciones especulativas para YAGNI.
   - **`PD04–PD06` (Repository, Domain Service, Application Service):** ✅ **CUBIERTO en LIN-DEV-JAVA-001 §11.5**. Se normaron los tres building blocks tácticos con plantillas Spring Boot concretas: Repository (puerto en dominio + adaptador `JpaRepository`/`JdbcRepository` en infraestructura), Domain Service (POJO puro registrado vía `@Configuration`/`@Bean` para preservar la pureza hexagonal) y Application Service (orquestador transaccional `@Service` que delega en el dominio y publica eventos).
   - **Cierre adicional no planificado en el punto 4 original:** se normó también la sanción de deuda técnica de Feature Toggles que había quedado pendiente desde `LIN-ARQ-000 §2.2.1` (referenciada como "§14 pendiente de redacción"), ahora cubierta en **LIN-DEV-JAVA-001 §14.6** con retiro diferenciado por tipo de toggle (Release/Experiment obligatorio a 1 sprint; Ops/Permission exentos por diseño).

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
| 2026-07-02 | §6 actualizado — se registran como cubiertos en LIN-ARQ-000 los patrones PT05, PT06, PT07, PT09, PT10, PT12, PI08, PRA06, PD08, PD09, PA07, PD01, PD02 y los principios PRA07, PRA10, PR09 tras incorporar las secciones §3.7, §3.8, §3.9, §3.10, §3.11 y §6.4.1 |
| 2026-07-02 | §5 actualizado — se documenta el Plan de Acción Unificado y Secuencial priorizando PD10 (Published Language), PA14 (Feature Toggle) y E07 (SOA Interoperabilidad Gubernamental) en LIN-ARQ-000 antes del descenso al Nivel 3 en LIN-DEV-JAVA-001 |
| 2026-07-02 | §0 expandido — añadida tabla completa de los 14 lineamientos de Nivel 3 con rutas y qué buscar en cada uno; añadidos documentos de trabajo (START_HERE, GLOSARIO, Matriz, Plantilla). §2 NIVEL 3 actualizado con todos los lineamientos. §5 corregido: PR01-PR05 pasa de ❌ pendiente a ⚠️ Parcial. |
| 2026-07-02 | §5 actualizado — se marca como cubierto el Paso 1 de la Fase 1 (`PD10` Published Language) tras ser normado y estructurado en LIN-ARQ-000 §3.9.3 |
| 2026-07-02 | §5 actualizado — se marca como cubierto el Paso 2 de la Fase 1 (`PA14` Feature Toggle) tras normarse sus 4 variantes, mandato de deuda técnica cero y Branch by Abstraction en LIN-ARQ-000 §2.2.1 |
| 2026-07-02 | §5 actualizado — se marca como cubierto el Paso 3 de la Fase 1 (`E07` SOA Gubernamental) tras normarse en LIN-ARQ-000 §3.8.4 con resiliencia extrema y ACL obligatorios |
| 2026-07-02 | §5 actualizado — se registra el cierre definitivo y auditoría de congruencia interna al 100% de la **Fase 1 (`LIN-ARQ-000 v0.1.16`)** y el inicio formal de la **Fase 2 (`LIN-DEV-JAVA-001`)** |
| 2026-07-03 | LIN-ARQ-000 bumpeado a v0.1.17 — revisión estructural: §8 restructurado en 4 grupos GoF con 5 patrones nuevos (Strategy, Observer, Command, State, Decorator); PRA09 declarado formalmente en §3.11; §5 numerado (5.1–5.4); §10.1 actualizado con EDA y DDD desglosado |
| 2026-07-06 | §5 actualizado — se registra el cierre definitivo de la **Fase 2 (`LIN-DEV-JAVA-001 v0.1.2`)**: PR01–PR05 (SOLID) y PR06–PR08 (DRY/KISS/YAGNI) en §10.4, PD04–PD06 (Repository/Domain Service/Application Service) en §11.5, y cierre adicional de la sanción de deuda técnica de Feature Toggles en §14.6 (resuelve la referencia pendiente de LIN-ARQ-000 §2.2.1). `Brecha_Framework_Arquitectura_ONP_v0.1.0.md` bumpeado a v0.1.4 reflejando estos cierres y actualizando PG01–PG03 (patrones GoF) de ❌ Pendiente a ⚠️ Parcial tras verificar cobertura parcial en LIN-ARQ-000 §8 |
