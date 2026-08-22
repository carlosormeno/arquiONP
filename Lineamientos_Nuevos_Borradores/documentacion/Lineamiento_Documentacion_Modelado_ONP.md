# LIN-DOC-001 — Lineamiento de Documentación y Modelado ONP

**Código:** LIN-DOC-001
**Versión:** v0.1.0
**Estado:** En revisión
**Fecha:** 2026-08-21
**Propietario documental:** Arquitectura de Software — OTI
**Revisores sugeridos:** Desarrollo, Arquitectura, Plataforma/Infraestructura, QA
**Marco rector:** LIN-ARQ-001 — Marco Rector de Arquitectura de Software

---

## Control de cambios

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| v0.1.0 | 2026-08-21 | Arquitectura OTI | Versión inicial (`GOB-CHK-001` H42). Cubre el último documento que el mapa declaraba `Pendiente`. Norma **qué documentación produce un proyecto, dónde vive, con qué notación y quién la mantiene**, sin redefinir lo que ya tiene dueño: Javadoc (`LIN-DEV-JAVA-001 §9`), OpenAPI (`LIN-API-REST-001 §6`), el documento de arquitectura (`GOB-PLA-001`), los ADR (`LIN-ARQ-001` Apéndice A) y el ciclo de vida del corpus (`GOB-MAT-001`) |

---

## Tabla de contenido

1. [Objetivo y alcance](#1-objetivo-y-alcance)
2. [Normativa y documentos relacionados](#2-normativa-y-documentos-relacionados)
3. [Principios rectores](#3-principios-rectores)
4. [Inventario documental mínimo por proyecto](#4-inventario-documental-mínimo-por-proyecto)
5. [Dónde vive la documentación](#5-dónde-vive-la-documentación)
6. [README del repositorio](#6-readme-del-repositorio)
7. [Notación y herramientas de modelado](#7-notación-y-herramientas-de-modelado)
8. [Documentación de operación (runbook)](#8-documentación-de-operación-runbook)
9. [Mantenimiento y obsolescencia](#9-mantenimiento-y-obsolescencia)
10. [Checklist de conformidad](#10-checklist-de-conformidad)
11. [Anti-patrones](#11-anti-patrones)
12. [Proceso de excepción (`EXC-DOC-NNN`)](#12-proceso-de-excepción-exc-doc-nnn)

---

## 1. Objetivo y alcance

### 1.1 Objetivo

Definir **qué documentación debe producir un proyecto de software en la ONP, dónde vive, con qué notación se modela y quién la mantiene**.

Este lineamiento **no redefine** el contenido de los artefactos que ya tienen dueño: define el conjunto, su ubicación y su ciclo de vida. Su razón de ser es que hasta ahora esas decisiones estaban implícitas —cada proyecto documentaba lo que consideraba— y la documentación dispersa o desactualizada es indistinguible de la inexistente.

### 1.2 Alcance

Aplica a todo proyecto de software desarrollado por la OTI o por terceros bajo su supervisión, incluidos los sistemas heredados cuando sean objeto de intervención mayor.

### 1.3 Fuera de alcance

| Tema | Documento dueño |
|---|---|
| Contenido del documento de arquitectura | `GOB-PLA-001` |
| Decisiones arquitectónicas institucionales (ADR) | `LIN-ARQ-001`, Apéndice A |
| Ciclo de vida y estados del **corpus normativo** | `GOB-MAT-001` |
| Javadoc y comentarios de código | `LIN-DEV-JAVA-001 §9` |
| Contrato OpenAPI de una API | `LIN-API-REST-001 §6` |
| Contrato de eventos y catálogo de tópicos | `LIN-BUS-001`, Apéndices A y B |
| Catálogo de datasets analíticos (OpenMetadata) | `LIN-BI-001 §7.2` |
| Catálogo de bases de datos y objetos PL/SQL | `LIN-BD-ORA-001` |
| Documentación de la excepción de un lineamiento | El lineamiento afectado + `GOB-MAT-001` |

---

## 2. Normativa y documentos relacionados

| Documento | Código | Relación |
|---|---|---|
| Marco Rector de Arquitectura de Software | `LIN-ARQ-001` | Exige el documento de arquitectura como entregable de aceptación (`ARQ-R-008`) |
| Plantilla de Documento de Arquitectura | `GOB-PLA-001` | Formato obligatorio del principal entregable documental |
| Matriz de Propiedad Documental | `GOB-MAT-001` | Ciclo de vida del corpus y registro de decisiones y excepciones |
| Versionamiento y Control de Cambios | `LIN-VER-001` | La documentación se versiona como el código: MR, revisión, trazabilidad |
| Estándar de Desarrollo Java | `LIN-DEV-JAVA-001` | Javadoc, comentarios y estructura del repositorio |
| Estándar de APIs REST | `LIN-API-REST-001` | OpenAPI como contrato y catálogo de servicios |
| Contenedores y Orquestación | `LIN-K8S-001` | Ficha de despliegue por ambiente (`§16`) |
| Log, Trazabilidad y Observabilidad | `LIN-OBS-001` | Dashboards y evidencia operativa que el runbook referencia |

---

## 3. Principios rectores

| # | Principio | Descripción |
|---|---|---|
| P1 | **La documentación vive con el código** | Salvo excepción justificada, el artefacto se versiona en el repositorio del sistema y evoluciona en el mismo Merge Request que el cambio que lo afecta. |
| P2 | **Un solo dueño por artefacto** | Cada documento tiene un responsable nominal. La documentación sin dueño no se mantiene. |
| P3 | **Se documenta la decisión, no el detalle obvio** | Lo que el código ya expresa no se duplica en prosa: se documenta el *porqué*, la restricción y lo que no es evidente. |
| P4 | **Documentación desactualizada es peor que ausente** | Induce a error con apariencia de autoridad. Ante la imposibilidad de mantener un artefacto, se retira y se declara retirado. |
| P5 | **Generar antes que redactar** | Si un artefacto puede derivarse del código o de la telemetría, se genera. Lo generado no diverge. |
| P6 | **Notación única por tipo de vista** | Un mismo tipo de diagrama se modela siempre con la misma notación, para que sea comparable entre sistemas. |

---

## 4. Inventario documental mínimo por proyecto

Lo que un proyecto **debe** entregar depende de su naturaleza y de su criticidad (`ARQ-R-006` — LIN-ARQ-001 §5.4.1).

| Artefacto | Obligatorio | Dónde vive | Documento dueño del formato |
|---|---|---|---|
| **Documento de Arquitectura** | Todo sistema nuevo o con intervención mayor | Repositorio del sistema, `/docs` | `GOB-PLA-001` |
| **`README.md`** | Todo repositorio, sin excepción | Raíz del repositorio | §6 de este lineamiento |
| **Contrato OpenAPI** | Todo servicio que exponga endpoints HTTP | Repositorio, generado desde el código | `LIN-API-REST-001 §6` |
| **Contrato de evento** | Todo productor de eventos Kafka | Repositorio + catálogo institucional | `LIN-BUS-001`, Apéndice A |
| **Scripts de BD versionados** | Todo sistema con persistencia propia | Repositorio, carpeta de migraciones | `LIN-BD-ORA-001`, `LIN-VER-001 §16` |
| **Manifiestos de despliegue** | Todo sistema desplegado en Kubernetes | Repositorio, `k8s/` | `LIN-K8S-001`, Anexo A |
| **Ficha de despliegue por ambiente** | Todo sistema en QA o PROD | Repositorio del sistema | `LIN-K8S-001 §16` |
| **Runbook de operación** | Criticidad **Alta o Media** | Repositorio del sistema, `/docs` | §8 de este lineamiento |
| **ADR de proyecto (`AD-NNN`)** | Cuando hay decisión con alternativas reales | Anexo B del Documento de Arquitectura | `GOB-PLA-001` |
| **Excepciones (`EXC-<SUF>-NNN`)** | Cuando se desvía de un lineamiento | Anexo E del Documento de Arquitectura | `GOB-MAT-001` |

> **Un sistema de criticidad Baja no queda eximido del Documento de Arquitectura**: queda eximido del runbook y puede completar el documento con menos profundidad, pero las **declaraciones obligatorias** de `GOB-PLA-001 §1.4` —Estadio, CAP, DDD, criticidad— se exigen siempre.

---

## 5. Dónde vive la documentación

### 5.1 Regla general

La documentación técnica de un sistema vive **en su repositorio**, versionada junto al código:

```text
<repositorio>/
├── README.md                      ← punto de entrada obligatorio (§6)
├── docs/
│   ├── arquitectura.md            ← Documento de Arquitectura (GOB-PLA-001)
│   ├── runbook.md                 ← operación (§8), si criticidad Alta o Media
│   ├── despliegue.md              ← ficha por ambiente (LIN-K8S-001 §16)
│   └── modelos/                   ← fuentes de los diagramas (§7)
│       └── arquitectura.archimate
├── openapi.yml                    ← si expone APIs
└── k8s/                           ← manifiestos
```

**Por qué en el repositorio y no en una wiki:** el documento pasa por el mismo Merge Request que el cambio que lo afecta, de modo que actualizarlo es parte de la revisión de código (`LIN-VER-001 §12`) y no una tarea posterior que nadie hace. Un wiki editable fuera del flujo de cambios es la primera documentación que se desactualiza.

### 5.2 Qué no vive en el repositorio

| Artefacto | Dónde vive | Motivo |
|---|---|---|
| Catálogos institucionales (servicios, eventos, bases de datos, datasets) | Herramienta institucional | Son transversales: un catálogo por repositorio no es un catálogo |
| Documentación funcional y de negocio | Gestor documental de la OTI | No es documentación técnica |
| Evidencias de pruebas y despliegue | Merge Request o expediente técnico | Son evidencia de un evento, no documentación viva |
| Cualquier dato sensible, credencial o secreto | **En ninguna parte del repositorio** | `LIN-SEC-APP-001 §12` |

---

## 6. README del repositorio

> 🔖 **`DOC-R-001`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

Todo repositorio tiene un `README.md` en su raíz. Es lo primero que lee quien llega al proyecto —incluido el equipo que lo herede dentro de tres años— y su ausencia o su desactualización obligan a reconstruir por lectura de código lo que debería estar escrito.

### 6.1 Contenido mínimo obligatorio

| Sección | Qué debe contener |
|---|---|
| **Qué es** | Una descripción en dos o tres líneas: qué hace el sistema y a qué proceso de negocio sirve |
| **Estado** | Activo / en construcción / en deprecación, y a qué ambientes está desplegado |
| **Cómo se levanta en local** | Prerrequisitos, comandos exactos y cómo verificar que arrancó |
| **Cómo se ejecutan las pruebas** | Comando de pruebas unitarias y de integración |
| **Dependencias externas** | Sistemas de los que depende (SAA, Oracle, RENIEC, Kafka…) y qué ocurre si no están disponibles |
| **Enlace al Documento de Arquitectura** | Ruta relativa a `docs/arquitectura.md` |
| **Responsables** | Responsable técnico y responsable funcional, con área |
| **Declaración de Conformidad** | Formato obligatorio de `ARQ-R-008` (LIN-ARQ-001 §8.3), verificado por `LIN-CICD-001 §12.5` |

### 6.2 Lo que no va en el README

Detalle que se desactualiza sin que nadie lo note: listas exhaustivas de endpoints —para eso está el OpenAPI—, esquemas de base de datos, valores de configuración por ambiente y capturas de pantalla de la aplicación.

---

## 7. Notación y herramientas de modelado

> 🔖 **`DOC-R-002`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

### 7.1 Notación por tipo de vista

| Tipo de vista | Notación | Herramienta | Dónde se usa |
|---|---|---|---|
| **Vistas de arquitectura del sistema** (contexto, aplicación, componentes, infraestructura) | **ArchiMate 3.x** | **Archi** | Anexo A de `GOB-PLA-001` — es la **fuente autoritativa** de la arquitectura |
| **Diagramas embebidos en documentación** (flujos, secuencias, topologías simples) | **Mermaid** | Cualquier editor; GitLab lo renderiza nativamente | Cuerpo de los documentos `.md` del corpus y de los proyectos |
| **Modelo de datos** | Diagrama entidad-relación | Herramienta del DBA | `LIN-BD-ORA-001` |

**Regla:** un mismo tipo de vista se modela siempre con la misma notación. Un diagrama de contexto en PowerPoint, otro en draw.io y otro en ArchiMate no son comparables entre sistemas, que es justamente para lo que sirve tener una arquitectura documentada.

### 7.2 Diagrama como código frente a herramienta gráfica

| Criterio | Elección |
|---|---|
| El diagrama acompaña a un texto y cambia con él | **Mermaid**, embebido en el `.md`: se versiona en el mismo commit y se revisa en el mismo MR |
| El diagrama es el modelo de la arquitectura y se contrasta contra la realidad | **ArchiMate en Archi**: tiene semántica, relaciones tipadas y niveles de abstracción |
| El diagrama es una imagen suelta sin fuente | **No se acepta** — ver anti-patrones |

**La fuente del diagrama se versiona siempre.** Un `.png` sin su fuente es un artefacto muerto: nadie puede modificarlo y se sustituye por otro que dice algo distinto.

### 7.3 Relación con C4

`LIN-ARQ-001 §1.2` usa el **C4 Model** como referencia conceptual para explicar los tres niveles del corpus (contexto → contenedores → componentes). C4 es un **modelo de niveles de abstracción**, no una notación de dibujo: en la ONP esos niveles se materializan con las vistas ArchiMate de `GOB-PLA-001` Anexo A. No se exige producir diagramas «C4» aparte.

---

## 8. Documentación de operación (runbook)

> 🔖 **`DOC-R-003`** — *identificador estable de esta regla; cítese este código y no el número de sección (`GOB-MAT-001`)*

Obligatorio para sistemas de criticidad **Alta o Media** (`ARQ-R-006` — LIN-ARQ-001 §5.4.1). Es el documento que se consulta **durante un incidente**, no antes: se escribe para alguien con prisa que no conoce el sistema.

### 8.1 Contenido mínimo

| Sección | Qué debe contener |
|---|---|
| **Cómo saber que está sano** | Qué dashboard mirar, qué métrica indica normalidad y cuál es su rango esperado (`LIN-OBS-001 §8`) |
| **Síntomas conocidos y qué hacer** | Tabla de síntoma → causa probable → acción, alimentada por los incidentes reales que ocurran |
| **Dependencias y su degradación** | Qué deja de funcionar si cae SAA, Oracle, Kafka o una entidad externa, y si el sistema puede operar degradado |
| **Procedimiento de recuperación** | Orden de arranque y dependencias (`ARQ-R-006` — LIN-ARQ-001 §5.4.3) |
| **Cómo revertir un despliegue** | Comando o procedimiento concreto, no «hacer rollback» |
| **A quién escalar** | Responsable técnico, responsable funcional y vía de contacto fuera de horario |

### 8.2 Regla de actualización

**Todo incidente de producción que requiera intervención humana obliga a revisar el runbook.** Si el síntoma no estaba, se añade; si el procedimiento documentado no funcionó, se corrige. Un runbook que no crece tras los incidentes no se está usando.

---

## 9. Mantenimiento y obsolescencia

### 9.1 La documentación se revisa en el mismo MR que el cambio

`LIN-VER-001 §12` exige revisión de código; la documentación afectada entra en esa misma revisión. Un MR que cambia un contrato, una dependencia externa o un procedimiento de despliegue **y no toca la documentación correspondiente** es un MR incompleto.

### 9.2 Revisión periódica

| Artefacto | Revisión mínima |
|---|---|
| Documento de Arquitectura | Anual, o ante disparador de `GOB-PLA-001 §1.5` |
| Runbook | Tras cada incidente que requiera intervención, y anual si no hubo |
| `README.md` | Cuando cambien prerrequisitos, comandos o responsables |

### 9.3 Retirar antes que dejar podrir

Un artefacto que ya no se mantiene se marca como **retirado**, con fecha y motivo, o se elimina del repositorio —Git conserva el historial—. Conservar documentación desactualizada sin advertencia es peor que no tenerla: quien la lea tomará decisiones sobre información falsa creyendo que es vigente (P4).

---

## 10. Checklist de conformidad

```text
[ ] README.md presente, con las ocho secciones obligatorias de §6.1
[ ] Declaración de Conformidad presente y firmada en el README
[ ] Documento de Arquitectura en docs/, conforme a GOB-PLA-001
[ ] Declaraciones obligatorias completas (Estadio, CAP, DDD, criticidad)
[ ] Runbook presente si la criticidad es Alta o Media
[ ] Fuentes de todos los diagramas versionadas en el repositorio
[ ] OpenAPI generado y versionado, si el sistema expone APIs
[ ] Ficha de despliegue por ambiente documentada
[ ] Ningún dato sensible, credencial ni secreto en la documentación
[ ] Responsable técnico y funcional nombrados
```

---

## 11. Anti-patrones

| Anti-patrón | Riesgo | Regla |
|---|---|---|
| Documentación en una wiki fuera del flujo de cambios | Se desactualiza sin que nadie lo note | Vive en el repositorio (P1) |
| Diagrama `.png` sin su fuente versionada | Nadie puede modificarlo; se sustituye por otro divergente | Versionar la fuente (§7.2) |
| README que solo dice `mvn spring-boot:run` | Quien hereda el sistema reconstruye todo leyendo código | Contenido mínimo de `DOC-R-001` (§6.1) |
| Documentar en prosa lo que el código ya dice | Duplica el mantenimiento y diverge | Documentar el porqué (P3) |
| «La documentación se hace al final del proyecto» | No se hace, o se hace sin memoria de las decisiones | Entra en el MR del cambio (§9.1) |
| Runbook escrito una vez y nunca actualizado | Falla justo cuando se necesita | Revisión tras cada incidente (§8.2) |
| Copiar el Documento de Arquitectura de otro proyecto | Declara decisiones que no se tomaron y criticidad que no corresponde | Declaraciones propias verificadas en `GOB-PLA-001` Anexo E |
| Capturas de pantalla en la documentación técnica | Envejecen a la primera versión de la interfaz | Describir el comportamiento, no la pantalla |

---

## 12. Proceso de excepción (`EXC-DOC-NNN`)

> **Instrumento correcto: `EXC-DOC-NNN`, no un ADR.** Conforme a `GOB-MAT-001` (Registro de decisiones y excepciones), la desviación de este lineamiento **en un proyecto concreto** se registra como excepción con vigencia acotada y **fecha de revisión**, nunca indefinida. El `ADR-NNN` queda reservado a decisiones **institucionales** del Comité de Arquitectura. La excepción se aprueba por Arquitectura OTI y se registra en el documento de arquitectura del sistema (`GOB-PLA-001`, Anexo E, criterio 14).

**Casos que típicamente requieren excepción:**

- Sistema heredado cuya documentación no puede reconstruirse sin un esfuerzo desproporcionado.
- Documentación que debe residir fuera del repositorio por restricción de clasificación de la información.
- Producto de terceros sin acceso al código, donde el Documento de Arquitectura se limita a la integración.

**No es excepción válida:** la falta de tiempo del proyecto. La documentación es parte del entregable, no un añadido posterior (`ARQ-R-008` — LIN-ARQ-001 §8.3).

---

*LIN-DOC-001 — Lineamiento de Documentación y Modelado ONP*
*OTI — Oficina de Tecnologías de la Información*
