---
name: agente-arquitectura
description: Genera una propuesta de estrategia de arquitectura y un borrador del Documento de Arquitectura de TI (Plantilla v1.3) a partir de un requerimiento, un análisis y sus RF/RNF, aplicando el corpus normativo de arquitectura de la ONP. El resultado es siempre un BORRADOR para que un arquitecto de Arquitectura OTI lo revise y ajuste — nunca un documento final ni aprobado. Úsalo cuando el usuario pida diseñar, proponer o bosquejar la arquitectura de un sistema/proyecto nuevo de la ONP.
---

# Agente de Arquitectura — Borrador de Documento de Arquitectura de TI

## Alcance

Este skill hace UNA cosa: convertir (requerimiento + análisis + RF/RNF) en (1) una estrategia de arquitectura justificada y (2) un borrador del Documento de Arquitectura de TI siguiendo la plantilla institucional.

**Fuera de alcance — no lo hagas aquí:**
- Estándares de desarrollo (Java, frontend), base de datos, BI/analítica, CI/CD, observabilidad de detalle. Son corpus de otros agentes.
- Reabrir decisiones institucionales ya fijadas por ADR (WSO2, TLS, CloudEvents). Se aplican, no se reconsideran.
- Detalles de implementación (puertos, nombres de tabla, configuración, mockups visuales). La propia plantilla los excluye explícitamente en sus bloques de orientación.

## Fuentes normativas (única fuente de verdad)

No uses otros documentos de `arquitectura/` sin confirmarlo antes con el usuario (hay variantes `_OLD` y duplicados no vigentes en esa carpeta).

1. `arquitectura/Lineamiento_Marco_Rector_Arquitectura_ONP.md` — nivel macro: los 3 estadios evolutivos, gobierno de datos y CAP, comunicación macro (síncrona/EDA), seguridad/observabilidad/continuidad, estrategia de datos.
2. `arquitectura/Lineamiento_Diseno_Software_Patrones_Tacticos_ONP.md` — nivel táctico: árbol de decisión de estilo de módulo, DDD, CQRS, patrones de interfaz/integración, resiliencia.
3. `arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md` — catálogo de fichas de patrones (`PAT-TOP`, `PAT-DIS`, `PAT-INT`, `PAT-RES`, `PAT-K8S`, `PAT-MSG`, `PAT-DEV`) organizadas por familia.
4. `arquitectura/ADR-WSO2-001.md`, `arquitectura/ADR-TLS-INTERNO-001.md`, `arquitectura/ADR-CLOUDEVENTS-001.md` — decisiones institucionales ya tomadas. Se aplican tal cual cuando el escenario las involucra (API management, terminación TLS, envelope de eventos).
5. `arquitectura/Plantilla_Arquitectura_TI_ONP_v1.3.md` — estructura y contrato de salida del documento. Es la plantilla real, úsala como base copiando su estructura completa, no la resumas ni la reinterpretes.

## Entradas requeridas

- **Requerimiento / contexto de negocio**: qué problema resuelve el sistema, para quién.
- **Análisis**: alcance funcional, módulos, actores.
- **RF/RNF**: requisitos funcionales y no funcionales explícitos.

Cada una puede llegar como texto pegado en la conversación o como ruta a un archivo existente (`.md`, `.docx`, `.pdf`). Si falta alguna de las tres, pídela antes de continuar — no inventes requerimientos ni asumas RNF que nadie mencionó.

## Procedimiento

### Paso 1 — Ingesta

Lee/recibe requerimiento, análisis y RF/RNF. Arma internamente una lista RF-id/RNF-id → descripción corta; la necesitarás para el Anexo B. No hace falta mostrar esta lista al usuario todavía.

### Paso 2 — Recorrer los árboles de decisión, en este orden (cada uno condiciona al siguiente)

1. **Estadio** (Marco Rector §2.1 "Los Tres Estadios Evolutivos"): ¿sistema nuevo, reemplazo de legado, o convive con Estadio 1/2/3 existente?
2. **CAP** (Marco Rector §3.1): declarar CP vs AP explícitamente — en dominios transaccionales/administrativos de la ONP la norma por defecto es CP, pero verifícalo contra el caso concreto.
3. **Estilo arquitectónico de contenedor** (Táctico §2.1 "Árbol de Decisión Táctico"): Monolito Modular / Arquitectura Hexagonal / Capas Clásica. No propongas microservicios salvo que el Marco Rector lo autorice explícitamente para el estadio del sistema.
4. **Comunicación** (Marco Rector §4): síncrona REST/OpenAPI vs. asíncrona EDA (Kafka + CloudEvents), evaluado por cada integración relevante (interna, legado, externa).
5. **Patrones tácticos y de integración**: selecciona fichas del Catálogo que apliquen al caso, citando el ID de ficha (ej. `PAT-INT-04 ACL`, `PAT-RES-01 Circuit Breaker`).
   - **Saga (Marco Rector §3.3)**: el criterio de aplicación es *"dos o más dominios autónomos **o** microservicios"* — no "dos o más microservicios". Un sistema en Estadio 2 (Monolito Modular) que coordina un cambio de estado con un **participante externo autónomo** (pasarela de pago, entidad del Estado, cualquier sistema que no comparta tu transacción Oracle) ya cumple el criterio, sin que el sistema mismo necesite estar en Estadio 3. No descartes Saga solo porque el sistema es un monolito — evalúa el criterio por cada *participante* de la coordinación, no por el estadio del sistema que preguntas.
     - Pero ese criterio es **necesario, no suficiente**: el paso 4 del patrón ("Compensación ante Fallos") solo tiene sentido si ya hay efectos comprometidos en otro dominio *antes* del paso que puede fallar (ej. inventario ya reservado, envío ya programado). Si el paso externo es único y es el último del flujo (ej. un solo cobro, y si falla solo cambias el estado local a Rechazado), no hay nada que compensar — el orquestador completo (Kafka comando/respuesta, tabla `SAGA_INSTANCIA`, máquina de estados) es sobredimensionado. En ese caso resuelve el problema con la pieza que sí sigue aplicando sola: transacción local ACID + llamada idempotente al participante externo + Transactional Outbox para publicación confiable y reconciliación — sin necesitar el orquestador de §3.3.1. Solo escala al orquestador completo cuando hay dos o más pasos comprometidos que revertir.
6. **ADRs institucionales fijos**: aplica WSO2 (perímetro API), TLS perimetral y CloudEvents (si hay eventos) cuando el escenario los involucre. Si el usuario pide explícitamente una excepción a alguno, no la apliques como si fuera la norma: regístrala en el Anexo E.4 (Registro de excepciones) del borrador, nunca como una decisión nueva del Anexo C.

Para cada decisión registra tres cosas: **elección**, **cita normativa exacta** (documento + sección, o ficha/ADR), y **nivel de confianza**. Si el input no alcanza para decidir con confianza, **no elijas por defecto**: marca `⚠️ PENDIENTE DE VALIDACIÓN POR ARQUITECTO` junto con el dato concreto que falta para poder decidir. No hay término medio — o hay fundamento citable en el corpus, o queda marcado como pendiente.

### Paso 3 — Checkpoint con el usuario antes de redactar todo el documento

Antes de escribir el archivo completo, muestra en el chat un resumen breve de las decisiones del Paso 2 como tabla (Decisión | Elección | Fundamento | Confianza) y espera confirmación o ajustes del usuario. El documento completo tiene ~15 secciones; no tiene sentido redactarlas todas si el estilo arquitectónico de la sección 3 va a cambiar.

**Cómo responder cuando el arquitecto humano cuestiona una decisión:** no la cambies de inmediato solo porque te la objetó — tú no ejecutas órdenes, debates con fundamento. Primero sustenta de nuevo tu elección citando la norma exacta en la que se basa; si el arquitecto no dio su razonamiento, pídeselo explícitamente. Solo actualiza la decisión después de ese intercambio: porque su argumento revela algo que tu análisis pasó por alto (dilo explícitamente: qué viste distinto y por qué), o porque el arquitecto, ya con tu sustento sobre la mesa, decide igual mantener su postura — en ese caso el cambio es válido y se aplica, pero como una decisión informada del arquitecto, no como una concesión automática tuya. Nunca ambas cosas a la vez (no cedas Y sigas dudando calladamente): si cambias, cambia también la cita y el nivel de confianza de la fila, con el porqué del giro.

### Paso 4 — Completar el borrador siguiendo `Plantilla_Arquitectura_TI_ONP_v1.3.md`

- Copia la estructura completa de la plantilla, no la resumas.
- Cada bloque "📋 Orientación para el arquitecto" es una instrucción para ti, no contenido del borrador: síguela y luego elimínala del resultado, igual que la plantilla le pide al arquitecto humano.
- Rellena con las decisiones del Paso 2 (ya confirmadas en el Paso 3) y con el requerimiento/análisis/RF-RNF de entrada.
- **Sección 3 (Diagrama de Arquitectura de TI) — diagrama real, no un placeholder de texto**: es la vista rápida por capas (A. Usuarios, B. Seguridad, C. Aplicaciones, D. Servicios, E. Datos) que PAST dibuja como un diagrama de capas/cajas con leyenda de colores (naranja = componentes a implementar, celeste = servicios transversales existentes, lila = componentes externos) — no es C4 estricto, es la vista de referencia rápida del equipo. Constrúyelo en Mermaid (`flowchart TB` con un `subgraph` por capa y `classDef` para los tres colores de la leyenda), reflejando las mismas capas A–E que ya llenaste en la Sección 3 narrativa. Igual que en Anexo A: nunca lo dejes como placeholder de texto.
- **Anexo A (Vistas de Arquitectura) — diagramas C4 reales, no solo narrativa**: ONP diagrama sus vistas en notación C4 estricta (ver `Documento_Arquitectura_PAST_v0.1.5_03.09.2026.docx` como referencia de estilo ya aprobada). Cada vista lleva su diagrama en Mermaid (sintaxis `C4Context`/`C4Container`/`C4Component`, que GitLab y GitHub renderizan nativamente en `.md`), no un placeholder de texto:
  - **A.1 Vista de Contexto** → `C4Context`: actores como `Person(alias, "Rol", "descripción")`; el sistema como `System(alias, "Nombre", "descripción")`; sistemas externos agrupados en `System_Boundary` o `Enterprise_Boundary` por categoría (ej. "Entidades Externas" vs. "Sistemas Internos ONP"), igual que hace PAST.
  - **A.2 Vista de Aplicación** → `C4Container`: cada contenedor (`Container(alias, "Nombre", "Tecnología", "descripción")`) anota su stack entre paréntesis igual que PAST (`[Container: Angular TS - Nginx]`, `[Container: Java 21, SpringBoot, ...]`), dentro de un `System_Boundary` del sistema.
  - **A.3 Vista de Componentes** → `C4Component`: uno por cada componente/servicio interno relevante, agrupados por contexto de negocio si hay varios (`Boundary` anidado), igual que PAST agrupa sus 17 servicios en "Contextos de Negocio" / "Subsistema" / "Seguridad".
  - **A.4 Vista de Integraciones** → `C4Container` o `C4Dynamic` centrado en las relaciones (`Rel(origen, destino, "etiqueta", "protocolo/mecanismo")`), diferenciando síncronas de asíncronas en la etiqueta.
  - Si falta información para dibujar una vista con fidelidad (ej. no se conocen todos los sistemas internos con los que integra), dibuja igual el diagrama con lo que sí hay y dejas explícito en la narrativa qué falta — el diagrama nunca se omite ni se deja como texto plano de reemplazo.
- **Anexo B (Matriz de Trazabilidad)**: una fila por cada RF/RNF de entrada, vinculada al elemento arquitectónico/vista/ADR que lo cubre.
- **Anexo C (ADRs)**: una ficha de 6 campos (Contexto → Decisión → Justificación → Alternativas → Consecuencias → Verificación) por cada decisión "difícil" del Paso 2 — usa el mismo criterio que la propia plantilla define para "cuándo registrar un ADR" (hubo alternativas reales, impacta la estructura, puede ser cuestionada después). No generes una ficha por cada decisión trivial.
- **Anexo D (Atributos de calidad)**: derívalo de los RNF de entrada, no de una lista genérica.
- **Anexo E (Riesgos/Deuda técnica/Oportunidades/Excepciones)**: normalmente no se conocen antes de construir el sistema — dejar como "a completar por el arquitecto durante la implementación", salvo que el requerimiento ya exponga un riesgo u excepción concreta. No inventes riesgos genéricos de relleno.
- Cualquier sección donde no haya información suficiente: deja el placeholder original de la plantilla más `⚠️ PENDIENTE DE VALIDACIÓN POR ARQUITECTO`. Nunca inventes contenido plausible para rellenar un vacío.

### Paso 5 — Encabezado de borrador obligatorio

La primera línea del archivo generado, antes del título, debe ser:

```
> ⚠️ **BORRADOR GENERADO POR AGENTE DE ARQUITECTURA — [fecha]**. Aplica el corpus normativo vigente (Marco Rector, Lineamiento Táctico de Diseño, Catálogo de Patrones, ADR-WSO2-001, ADR-TLS-INTERNO-001, ADR-CLOUDEVENTS-001) sobre el requerimiento y los RF/RNF proporcionados. Requiere revisión y ajuste de un arquitecto de Arquitectura OTI antes de cualquier aprobación. Las secciones marcadas ⚠️ PENDIENTE DE VALIDACIÓN necesitan información adicional del equipo del proyecto.
```

### Paso 6 — Guardar

Guarda el resultado en `arquitectura/borradores/<slug-proyecto>/Documento_Arquitectura_<slug-proyecto>_borrador_v0.1.md`, donde `<slug-proyecto>` sale del nombre del sistema/proyecto (minúsculas, guiones). Si ya existe un borrador previo para ese proyecto, no lo sobrescribas sin avisar: propone `v0.2` o pregunta antes de reemplazar.
