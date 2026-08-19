# Matriz de Propiedad Documental — ONP

**Código:** GOB-MAT-001  
**Versión:** 0.24.0 (ver "Historial de versiones")  
**Fecha:** 2026-08-05  
**Autor:** OTI — Oficina de Tecnologías de la Información  
**Estado:** Vigente / Operativo  

---

## Propósito

Este documento define qué lineamiento es la **fuente autoritativa** de cada tema técnico y qué lineamientos son consumidores (implementan o referencian, pero no redefinen). Su objetivo es evitar duplicidad, contradicciones y ambigüedad sobre dónde está la regla oficial.

**Principio rector:**

> Cada tema tiene un único dueño. Los demás documentos pueden mencionar o implementar ese tema, pero nunca redefinirlo. Si necesitan precisar algo propio de su dominio, lo hacen referenciando al dueño y agregando solo la parte específica a su contexto.

**Última validación integral del corpus:** 2026-08-05 (revisión integral de arquitectura; hallazgos y plan de atención en `GOB-CHK-001`)

**Convención de nombres de archivo (desde 2026-08-05):**

> Los nombres de archivo **no llevan versión**. La versión vigente de un documento se declara en **un único lugar: su encabezado**, respaldada por su historial de versiones. El versionado histórico lo provee Git (commits y tags del corpus).
>
> **Motivo:** mientras la versión estuvo embebida en el nombre, los archivos quedaron congelados en `v0.1.0` mientras los encabezados avanzaban hasta `v0.1.12` — los 20 documentos del corpus divergían, y cinco documentos publicaban además una tercera versión distinta en su pie de página. Un mismo documento llegó a declarar tres versiones diferentes a la vez (`GOB-CHK-001` H8).
>
> **Sufijo `_OLD` — marca visual de «no considerar».** Un archivo con sufijo `_OLD`, `_BACKUP`, `_obsoleto` o equivalente **no rige**: no es fuente autoritativa de ningún tema, no se cita como norma en ningún lineamiento y el linter lo excluye de sus validaciones. Se conservan en el repositorio como respaldo de consulta — **no se eliminan**.
>
> El sufijo es una **ayuda de lectura**, no un estado. Marca a simple vista, sin abrir el archivo, qué no debe considerarse. Aplica a dos casos distintos:
>
> - **Copias superadas** de un documento que sigue vivo con otro nombre (ej. una versión anterior de `LIN-VER-001`). No tienen código propio ni entrada en el catálogo.
> - **Documentos `Congelados`** que sí conservan código y entrada en el catálogo por trazabilidad histórica, como `LIN-ARQ-000`: su contenido se redistribuyó entre `LIN-ARQ-001`, `LIN-DIS-001` y `LIN-DEV-JAVA-001`, y una docena de historiales lo citan como origen. Sigue siendo consultable y referenciable **como antecedente**, nunca como norma.

---

## Ciclo de vida documental

Esta sección define **cuándo un documento del corpus obliga**. Sin ella, el corpus se declara de aplicación obligatoria para fábricas y terceros (`LIN-ARQ-001 §1.1` y `§8`) mientras la mayoría de sus documentos figura como borrador — una contradicción que permite a un contratista rechazar legítimamente una observación.

> **No confundir con los estados de la matriz de propiedad.** Los estados de más abajo (`Conforme`, `Resuelto`, `Pendiente`…) describen si **un tema** está alineado entre su dueño y sus consumidores. Los de esta sección describen si **un documento** rige. Son ejes ortogonales: un documento `Vigente` puede tener temas `En borrador`, y un documento `Borrador` puede tener temas `Conforme`.

### Estados

| Estado | Significado | ¿Obliga? |
|---|---|---|
| **Borrador** | En redacción o sin auditar. Su contenido puede cambiar sin aviso. | No |
| **En revisión** | Auditoría en curso para graduar: revisión de contenido registrada y hallazgos en tratamiento. | No — pero su contenido ya es orientación válida |
| **Vigente** | Normativo. Exigible a proyectos nuevos y a modernizaciones relevantes; invocable en TDR y en criterios de aceptación. | **Sí** |
| **Deprecado** | Fue vigente y se retira. Sigue rigiendo los sistemas construidos bajo él hasta su migración; no aplica a proyectos nuevos. | Solo para lo ya construido |
| **Congelado** *(terminal)* | Cantera histórica preservada por trazabilidad. No rige, no se actualiza y **no puede citarse como norma** (ej. `LIN-ARQ-000`). | No |

```
Borrador ──► En revisión ──► Vigente ──► Deprecado
                                 │
                                 └──────► Congelado (terminal)
```

`Pendiente` no es un estado de documento: si no existe archivo, no hay documento — es una fila del catálogo sin ruta.

> ### ⚠️ Nota de transición (desde 2026-08-08)
>
> **Estado al 2026-08-09: los dos primeros documentos ya graduaron.** `LIN-OBS-001` y `LIN-TEST-001` pasaron a `Vigente` tras cumplir los cinco criterios, y con ello **son exigibles contractualmente**: los requisitos de `LIN-ARQ-001 §8.3` que dependían de ellos —evidencia de observabilidad y umbrales de cobertura— dejan de estar degradados a *recomendados*.
>
> **El resto del corpus sigue sin graduar.** Los tres que figuraban como tal —`LIN-ARQ-001`, `LIN-DIS-001` y `LIN-PAT-001`— se autodeclararon vigentes **antes de que existiera un proceso de graduación**, por lo que no cumplieron una barra que aún no estaba definida. Se reclasifican a `En revisión` junto con los documentos cuya revisión de contenido ya está cerrada.
>
> **Consecuencia que debe conocerse:** mientras dure la transición, **los lineamientos aún no graduados no son exigibles contractualmente**. Su contenido sigue siendo la orientación técnica institucional válida y debe usarse para diseñar y construir, pero no puede invocarse como criterio de rechazo formal en un TDR ni en una conformidad de entregable. Los TDR en curso que citen estos documentos deben tratarlos como referencia técnica, no como norma exigible, hasta su graduación.
>
> **Cómo se sale de la transición:** cada documento se revisa a fondo, se cierran sus hallazgos en `GOB-CHK-001`, pasa a `En revisión`, y gradúa a `Vigente` cuando cumple los cinco criterios y obtiene la aprobación que le corresponde. Es un avance documento por documento, no un acto único.

### Criterio de graduación a Vigente

Un documento gradúa cuando cumple **los cinco criterios**. Cuatro son verificables sin juicio humano:

| # | Criterio | Cómo se verifica |
|---|---|---|
| 1 | **Linter del corpus en verde** | `herramientas/lint_corpus.py` sin errores en C1–C5 (automático en CI) |
| 2 | **Revisión de contenido realizada** | Bloque de hallazgos registrado en `GOB-CHK-001`, con todos sus ítems cerrados |
| 3 | **Sin contradicciones abiertas** | Ningún tema del que sea dueño figura `Pendiente` o `Requiere ADR` en esta matriz |
| 4 | **Artefactos ejecutables verificados** | Templates, plantillas y configuraciones que el documento norma, sincronizados y probados (regla 7 de mantenimiento) |
| 5 | **Historial de versiones al día** | Última entrada corresponde al contenido actual |

El único criterio con juicio humano es el **2**: comprobar que alguien leyó el documento completo, no solo sus citas.

#### Alcance del criterio 2 — hallazgos de documento vs. de corpus

Un hallazgo puede ser propio de un documento o estar distribuido por todo el corpus. La distinción determina si bloquea la graduación:

| Alcance | Ejemplo | ¿Bloquea la graduación? |
|---|---|---|
| **De documento** | Un cliente HTTP sin timeouts; una tabla de umbrales duplicada; una cita mal dirigida | **Sí** — debe cerrarse antes de graduar |
| **De corpus** | Enlaces internos rotos por convención de encabezados; migración a identificadores estables | **No** — se registra como deuda de corpus, con responsable y fecha de cierre |

> **Excepción por severidad:** si un hallazgo de alcance corpus alcanza, **en un documento concreto**, severidad tal que compromete su uso como norma, se escala a hallazgo de ese documento y sí bloquea su graduación.

**Por qué esta distinción.** Sin ella, el criterio 2 se rompe por cualquiera de los dos extremos: si toda deuda transversal bloqueara, ningún documento graduaría hasta que el corpus entero estuviera perfecto —el mismo bloqueo circular que la regla de exigibilidad ya descartó—; y si ninguna bloqueara, podría declararse `Vigente` un documento inutilizable. La excepción por severidad es lo que evita ambos extremos, a costa de exigir un juicio explícito y registrado en `GOB-CHK-001`.

**Aplicación vigente (`GOB-CHK-001` H21 — anclas internas rotas):** no bloquea a los documentos con enlaces sueltos rotos, donde el contenido sigue siendo localizable por número de sección. **Sí bloquea a `LIN-BD-ORA-001` y `LIN-BI-001`**: por titular sus secciones como `## sección N`, **ningún enlace de su tabla de contenidos resuelve**, y un documento normativo con el índice completo inservible no es utilizable por un contratista.

### Aprobación

| Documento | Aprueba |
|---|---|
| Nivel 1 y Nivel 2 (`LIN-ARQ-001`, `LIN-DIS-001`) y el catálogo `LIN-PAT-001` | **Comité de Arquitectura de la OTI** |
| Nivel 3, transversales y documentos de gobierno | **Arquitectura OTI**, verificando los cinco criterios |

La graduación se registra en el historial de versiones del documento y en el catálogo de esta matriz, con fecha.

### Vigencia de los documentos de arquitectura de proyecto

El ciclo de vida de esta matriz gobierna los **documentos del corpus**. Los **documentos de arquitectura de cada sistema** —producidos con `GOB-PLA-001`— no son parte del corpus, pero declaran conformidad con él y por tanto envejecen cuando el corpus cambia.

**Regla:** un documento de arquitectura declara conformidad con el corpus **en la versión de esta matriz que registra en su tabla de identidad**, no con el corpus perpetuo. Debe registrar esa línea base y una fecha de próxima revisión no mayor a 12 meses.

**Disparador de mayor impacto: la graduación a `Vigente`.** Cuando un documento gradúa, lo que antes era criterio técnico pasa a ser exigible contractualmente por la regla de exigibilidad. Todo documento de arquitectura de un sistema que dependa de él debe revisarse. **Arquitectura OTI comunica cada graduación**; detectar el resto de disparadores corresponde al arquitecto responsable del sistema. El detalle está en `GOB-PLA-001 §1.5`.

> Esta regla es también la razón por la que el historial de versiones de esta matriz debe ser legible: es la fuente que un arquitecto consulta para saber qué cambió desde su línea base.

---

### Registro de decisiones y excepciones — tres instrumentos distintos

El corpus usa la palabra «ADR» para tres cosas que no son lo mismo, y confundirlas tiene consecuencias: o se llena el registro institucional de incidencias operativas, o un proyecto cree que puede dispensarse a sí mismo de un lineamiento.

| Instrumento | Identificador | Alcance | Quién lo aprueba | Dónde vive |
|---|---|---|---|---|
| **Decisión arquitectónica institucional** | `ADR-NNN` | Obliga a todo el corpus | Comité de Arquitectura | Matriz de `LIN-ARQ-001`, Apéndice A. Si requiere desarrollo extenso, además como `ADR-<TEMA>-NNN.md`, con el mismo identificador de decisión |
| **Decisión de diseño de un proyecto** | `AD-NNN` | Solo ese sistema | Arquitecto del proyecto | Anexo B del documento de arquitectura (`GOB-PLA-001`) |
| **Excepción a un lineamiento** | `EXC-<CÓDIGO>-NNN` | Solo ese sistema, con vigencia acotada | Arquitectura OTI; Seguridad Digital si afecta seguridad | Documento de arquitectura del proyecto y registro de excepciones del lineamiento afectado |

**Regla:** un `AD-NNN` **no puede dispensar del cumplimiento de un lineamiento**. Toda desviación de una norma se registra como `EXC-`, con justificación, riesgo aceptado, control compensatorio y **fecha de revisión** — nunca indefinida. El código del lineamiento afectado va en el identificador: `EXC-IAC-001`, `EXC-K8S-004`, `EXC-VER-002`.

**Sufijo por lineamiento.** El identificador no se improvisa: cada lineamiento tiene un sufijo asignado, de modo que `EXC-K8S-004` sea inequívoco sin consultar nada.

| Lineamiento | Sufijo | Lineamiento | Sufijo |
|---|---|---|---|
| `LIN-ARQ-001` | `ARQ` | `LIN-K8S-001` | `K8S` |
| `LIN-DIS-001` | `DIS` | `LIN-CICD-001` | `CICD` |
| `LIN-DEV-JAVA-001` | `JAVA` | `LIN-IAC-001` | `IAC` |
| `LIN-API-REST-001` | `API` | `LIN-BUS-001` | `BUS` |
| `LIN-FE-ANG-001` | `FE` | `LIN-VER-001` | `VER` |
| `LIN-BD-ORA-001` | `BD` | `LIN-TEST-001` | `TEST` |
| `LIN-BI-001` | `BI` | `LIN-PERF-001` | `PERF` |
| `LIN-SEC-APP-001` | `SEC` | `LIN-OBS-001` | `OBS` |

**Numeración correlativa por lineamiento y por sistema**, no global: `EXC-K8S-001` del sistema PAST y `EXC-K8S-001` del sistema Notificaciones son excepciones distintas. La excepción se identifica siempre junto al sistema que la solicita, y se registra en su documento de arquitectura (`GOB-PLA-001`, Anexo E, criterio 14).

---

### Coherencia de dependencias — regla de exigibilidad

> **Un documento `Vigente` puede referenciar un documento que no lo esté, pero no puede hacerlo exigible.**

En concreto: si un documento vigente establece un **criterio de aceptación, gate de pipeline o requisito de TDR** cuyo cumplimiento se define en otro documento, ese otro documento debe estar `Vigente`. Mientras no lo esté, el requisito se degrada a **recomendado** y debe indicarlo explícitamente.

Las referencias informativas o de contexto no tienen esta restricción.

**Consecuencia práctica:** la regla convierte las dependencias de exigibilidad en un **orden objetivo de graduación** — se gradúa primero lo que otros documentos vigentes necesitan hacer exigible. Ver la ruta de graduación en la sección siguiente.

---

## Catálogo de documentos y códigos

| Código | Documento | Estado | Archivo |
|---|---|---|---|
| `LIN-ARQ-000` | Cantera Histórica Congelada de Arquitectura | Congelado v0.1.19 | `arquitectura/Lineamiento_Diseno_Arquitectura_Software_ONP_OLD.md` |
| `LIN-ARQ-001` | Marco Rector de Arquitectura de Software (Nivel 1) | **En revisión** v0.1.14 | `arquitectura/Lineamiento_Marco_Rector_Arquitectura_ONP.md` |
| `LIN-DIS-001` | Estándar de Diseño de Software y Patrones Tácticos (Nivel 2) | **En revisión** v0.1.7 | `arquitectura/Lineamiento_Diseno_Software_Patrones_Tacticos_ONP.md` |
| `LIN-PAT-001` | Catálogo Oficial de Patrones y Fichas Técnicas de Decisión | **En revisión** v0.1.6 | `arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md` |
| `LIN-API-REST-001` | Estándar de Servicios Web y APIs REST | **En revisión** v0.1.8 | `Web/Lineamiento_Estandar_APIs_REST_ONP.md` |
| `LIN-DEV-JAVA-001` | Estándar de Desarrollo Java | **En revisión** v0.1.13 | `desarrollo/Lineamiento_Estandar_Desarrollo_Java_ONP.md` |
| `LIN-BD-ORA-001` | Estándar de Base de Datos Oracle | **En revisión** v0.1.15 | `Datos/Lineamiento_Estandar_Base_de_Datos_ONP.md` |
| `LIN-BI-001` | Lineamiento de Explotación y Analítica de Datos (BI) | **En revisión** v0.1.3 | `Datos/Lineamiento_Explotacion_Analitica_Datos_BI_ONP.md` |
| `LIN-FE-ANG-001` | Estándar de Diseño Web Frontend Angular | **En revisión** v0.1.5 | `Web/Lineamiento_Estandar_Diseno_Web_Frontend_ONP.md` |
| `LIN-OBS-001` | Lineamiento de Log Centralizado, Trazabilidad y Observabilidad | **Vigente v0.1.7** (graduado 2026-08-09) | `observabilidad/Lineamiento_Log_Trazabilidad_Observabilidad_ONP.md` |
| `LIN-SEC-APP-001` | Estándar de Seguridad en Aplicaciones | **En revisión** v0.1.8 | `seguridad/Lineamiento_Seguridad_Aplicaciones_ONP.md` |
| `LIN-TEST-001` | Estándar de Pruebas | **Vigente v0.1.6** (graduado 2026-08-09) | `pruebas/Lineamiento_Estandar_Pruebas_ONP.md` |
| `LIN-CICD-001` | Estándar de CI/CD | **En revisión** v0.1.8 | `CICD/Lineamiento_Integracion_Entrega_Continua_ONP.md` |
| `LIN-K8S-001` | Estándar de Contenedores y Orquestación | **En revisión** v0.1.18 | `contenedores/Lineamiento_Contenedores_Orquestacion_ONP.md` |
| `LIN-IAC-001` | Estándar de Infraestructura como Código | **En revisión** v0.1.4 | `Infraestructura/Lineamiento_Infraestructura_Código_ONP.md` |
| `LIN-BUS-001` | Lineamiento de Mensajería y Bus de Eventos | **En revisión** v0.1.8 | `mensajeria/Lineamiento_Mensajeria_Bus_Eventos_ONP.md` |
| `LIN-VER-001` | Estándar de Versionamiento y Control de Cambios | **En revisión** v0.1.8 | `versionamiento/Lineamiento_Versionamiento_Control_Cambios_ONP.md` |
| `LIN-PERF-001` | Estándar de Pruebas de Rendimiento, Carga y Estrés | **En revisión** v0.1.5 | `pruebas/Lineamiento_Pruebas_Rendimiento_Carga_Estres_ONP.md` |
| `LIN-DOC-001` | Estándar de Documentación y Modelado | **Pendiente** | — |
| `GLOSARIO-ONP` | Glosario transversal operativo | Vigente / Operativo v0.2.2 | `GLOSARIO_ONP.md` |

### Documentos de gobierno y apoyo

Documentos que no norman un tema técnico pero forman parte del corpus: son destino verificable de citas y están sujetos al linter (`herramientas/lint_corpus.py`).

| Código | Documento | Estado | Archivo |
|---|---|---|---|
| `GOB-MAT-001` | Matriz de Propiedad Documental (este documento) | Vigente v0.24.0 | `Matriz_Propiedad_Documental_ONP.md` |
| `GOB-INI-001` | START HERE — punto de entrada para proyectos Java | Vigente / Operativo v0.3.0 | `START_HERE_Proyecto_Java_ONP.md` |
| `GOB-PLA-001` | Plantilla institucional de Documento de Arquitectura de TI | Vigente v2.7 | `arquitectura/Plantilla_Documento_Arquitectura_ONP.md` |
| `GOB-BRE-001` | Tablero de Brechas del Framework de Arquitectura | En revisión v0.1.7 | `arquitectura/Brecha_Framework_Arquitectura_ONP.md` |
| `GOB-CHK-001` | Checklist de Mejora del Corpus Documental | En ejecución v0.1.0 | `CHECKLIST_Mejora_Corpus_ONP.md` |

### Ruta de graduación (situación al 2026-08-08)

Ningún documento está `Vigente`: la ruta describe el orden en que graduarán.

Aplicando la regla de exigibilidad, el orden de graduación **no es discrecional**: lo fija qué documentos necesitan hacer exigibles los que ya están vigentes.

**Qué ordena la prioridad.** `LIN-ARQ-001 §8.3` exige al contratista, como condición de conformidad técnica, evidencia de observabilidad según `LIN-OBS-001` y umbrales de cobertura según `LIN-TEST-001`. Por la regla de exigibilidad, **`LIN-ARQ-001` no podrá graduar mientras esos dos sigan sin graduar**: haría exigible lo que no rige. Son, por tanto, la primera prioridad — y con ellos se desbloquea el Nivel 1.

| Prioridad | Documento | Por qué | Falta para graduar |
|---|---|---|---|
| **1** | `LIN-OBS-001` | Exigido por `LIN-ARQ-001 §8.3` (evidencia de observabilidad) y prerequisito de EDA en `§4.2` | Revisión de contenido |
| **1** | `LIN-TEST-001` | Exigido por `LIN-ARQ-001 §8.3` (cobertura) y dueño de umbrales que consumen 4 documentos | Revisión de contenido |
| **2** | `LIN-DEV-JAVA-001` | Nivel 3 del modelo; el más citado del corpus | **Nada — cumple los 5 criterios** (revisión H13 cerrada) |
| **2** | `LIN-SEC-APP-001` | Transversal; controles invocados por CI/CD y APIs | **Nada — cumple los 5 criterios** (revisión H15 cerrada) |
| **3** | `LIN-BD-ORA-001` | Dueño del modelo de datos y del DDL canónico `EVT_OUTBOX` | Cerrar `GOB-CHK-001` H14.2 y H14.3 |
| **4** | `LIN-API-REST-001`, `LIN-VER-001`, `LIN-CICD-001`, `LIN-K8S-001` | Invocados por gates de pipeline y por el gate de publicación WSO2 | Revisión de contenido |
| **5** | `LIN-BUS-001`, `LIN-FE-ANG-001`, `LIN-PERF-001`, `LIN-IAC-001`, `LIN-BI-001` | Sin exigibilidad cruzada pendiente | Revisión de contenido |

---

### Registros de Decisión Arquitectónica (ADR)

Los ADR numerados `ADR-001`–`ADR-014` viven en el Apéndice A de `LIN-ARQ-001`. Los siguientes son ADR con documento propio:

| Código | Documento | Estado | Archivo |
|---|---|---|---|
| `ADR-WSO2-001` | Transición de SAA hacia WSO2 API Manager (= `ADR-015` en la matriz de `LIN-ARQ-001`) | Propuesta | `arquitectura/ADR-WSO2-001.md` |
| `ADR-CLOUDEVENTS-001` | Adopción de CloudEvents v1.0 como envelope del bus (= `ADR-013` en la matriz de `LIN-ARQ-001` — misma decisión) | Aceptada | `arquitectura/ADR-CLOUDEVENTS-001.md` |
| `ADR-TLS-INTERNO-001` | Terminación TLS en el perímetro y tráfico intra-cluster sobre HTTP (= `ADR-016` en la matriz de `LIN-ARQ-001`) | Propuesta | `arquitectura/ADR-TLS-INTERNO-001.md` |

---

## Matriz de propiedad por tema

**Estados:**
- **Conforme** — propiedad clara, sin inconsistencias entre documentos.
- **Resuelto** — existía un conflicto o brecha; corregido en la versión actual.
- **Pendiente** — requiere acción: corrección en documento existente o creación de lineamiento propietario.
- **En borrador** — lineamiento propietario existe pero no está finalizado; contenido provisional.
- **Requiere ADR** — desviación que necesita excepción formal aprobada por Arquitectura.

**Regla editorial obligatoria para los estados:**

- `Conforme` solo puede usarse cuando el documento dueño y los consumidores relevantes están alineados en la versión vigente revisada.
- `Resuelto` solo puede usarse cuando el conflicto fue efectivamente corregido y la observación identifica la evidencia mínima de cierre.
- `En borrador` no equivale a inconsistencia; indica que el documento dueño existe, pero su contenido aún puede cambiar.
- Si existe duda razonable sobre la sincronización real, se debe usar `En borrador` o `Pendiente`, no `Conforme`.

---

### 1. Contrato de respuesta estándar

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Definición de `ApiResponseWrapper<T>` | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Resuelto | `LIN-DEV-JAVA-001` sección 13.4.4 corregido: genérico `T data`, `CampoError` con `campo`/`mensaje` en español. **Cerrado también en artefactos (2026-08-05):** los templates GitLab usaban `FieldError`; sincronizados con la fuente canónica `desarrollo/plantillas/` y verificados con build real (`GOB-CHK-001` H4) |
| Códigos `codDetRespuesta` (tabla 000–502) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Resuelto | `LIN-API-REST-001` sección 4.2.1 define dueño normativo, persistencia opcional y proceso de alta/cambio |
| Códigos HTTP por tipo de operación | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Resuelto | POST exitoso corregido a `201 Created` en `LIN-API-REST-001` y `LIN-DEV-JAVA-001` |
| Implementación Spring Boot de `ApiResponseWrapper` | `LIN-DEV-JAVA-001` | — | Resuelto | `LIN-DEV-JAVA-001` sección 13.4.4 referencia `LIN-API-REST-001` como fuente del contrato |
| Modelo TypeScript `ApiResponse<T>`, `ApiError`, `ApiMeta` | `LIN-FE-ANG-001` | — | Resuelto | `LIN-FE-ANG-001` corregido: `campo`/`mensaje`; `ApiMeta` con `timestamp`, `requestId`, `version` |

---

### 2. Diseño de APIs REST

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Convenciones de URL (`kebab-case`, recursos en plural) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001` | Conforme | — |
| Versionamiento en URL (`/api/v1/`) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Conforme | — |
| Paginación — campos y estructura del response | `LIN-API-REST-001` | `LIN-FE-ANG-001` | Resuelto | `LIN-FE-ANG-001` corregido: `pagina`/`tamanio` alineados con contrato REST |
| Operaciones no CRUD (verbos en URL) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001` | Conforme | — |
| Rate limiting y throttling | `LIN-SEC-APP-001` | `LIN-API-REST-001`, `LIN-K8S-001` | Conforme | `LIN-SEC-APP-001 §7.1`–`§7.2`: básico en la aplicación mientras no haya gateway, gestionado en WSO2 cuando gradúe. **Corregido 2026-08-09:** `LIN-API-REST-001 §8.4` figuraba como dueño y afirmaba que los servicios *no* implementan rate limiting, atribuyéndolo a un gateway que sigue en PoC — el resultado era que **ninguna API tenía rate limiting** (`GOB-CHK-001` H24) |
| Resiliencia táctica en llamadas externas — timeouts, Bulkhead, Retry, Circuit Breaker | `LIN-DIS-001` | `LIN-ARQ-001`, `LIN-API-REST-001`, `LIN-SEC-APP-001` | Resuelto | `LIN-DIS-001 §6` es dueño único (matriz de timeouts por criticidad en `§6.1`). Corregido 2026-08-05: `LIN-ARQ-001 §4.3` exigía Resilience4j contradiciendo `§6.2`, y publicaba un rango propio de timeout; `LIN-API-REST-001 §8.3` publicaba un tercer par de valores. Ambos delegan ahora al dueño (`GOB-CHK-001` H2 y H3) |
| API Gateway y API Manager — plataforma WSO2 | `LIN-API-REST-001` | `LIN-ARQ-001`, `LIN-K8S-001`, `LIN-SEC-APP-001` | En borrador | `LIN-API-REST-001` secciones 2.5 y 10.3 definen plataforma y gate de publicación. La transición se documenta además en `arquitectura/ADR-WSO2-001.md` |

---

### 3. Documentación OpenAPI / Swagger

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Swagger como requisito obligatorio de entrega | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-CICD-001` | Conforme | — |
| Anotaciones `@Tag`, `@Operation`, `@ApiResponse` | `LIN-DEV-JAVA-001` | — | Conforme | Solo en `@RestController`; ver `LIN-DEV-JAVA-001` sección 13.4.6 |
| Configuración `OpenApiConfig` (bean Spring) | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Swagger deshabilitado por defecto en producción | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001`, `LIN-CICD-001` | Resuelto | `LIN-DEV-JAVA-001` sección 13.4.3 corregido a formato `.yml` en todos los entornos |

---

### 4. Observabilidad

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Observabilidad como requisito obligatorio de producción (SRE 4 Golden Signals) | `LIN-ARQ-001` | Todos | Conforme | — |
| Formato de log JSON, campos mínimos obligatorios (ECS) | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001`, `LIN-K8S-001` | Resuelto | `LIN-OBS-001` sección 6 y Apéndice B son la fuente oficial |
| Implementación Java: `logback-spring.xml`, `LogstashEncoder`, `OpenTelemetryLogbackConfig` | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` secciones 4.6–4.7; `LIN-DEV-JAVA-001` sección 10 referencia `LIN-OBS-001` como fuente autoritativa |
| `traceId`, `spanId`, OpenTelemetry, Micrometer Tracing | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001` | Resuelto | `LIN-OBS-001` sección 5 absorbe Guía v0.1.1 sección 1 |
| Header `X-Request-ID` — definición y uso en el contrato REST | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001`, `LIN-OBS-001` | Conforme | — |
| `RequestIdFilter` — implementación Spring Boot | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 4.10; pone `http.request.id` en MDC |
| `CanonicalRequestLogFilter` — log canónico de request | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 4.9 |
| `Mask.java` — utilidad No PII | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 4.8 |
| Nivel de log por entorno (`pe.gob.onp.*` → INFO) | `LIN-OBS-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-OBS-001` sección 6.1 y `logback-spring.xml` sección 4.6; `LIN-DEV-JAVA-001` sección 10 referencia |
| Métricas mínimas, Prometheus, Grafana | `LIN-OBS-001` | `LIN-K8S-001`, `LIN-CICD-001` | Resuelto | `LIN-OBS-001` sección 8 define Actuator, métricas y dashboards mínimos |
| Retención de datos (DEV/QA: 30 días, PROD: 90 días) | `LIN-OBS-001` | `LIN-K8S-001` | Resuelto | `LIN-OBS-001` sección 9.2 |
| Health checks `liveness` / `readiness` (probes K8s) | `LIN-K8S-001` | `LIN-DEV-JAVA-001`, `LIN-OBS-001` | En borrador | `LIN-OBS-001` sección 8.3 documenta fragmento de referencia; `LIN-K8S-001` es el dueño del Deployment completo |
| Política No PII en logs y trazas (Ley N.° 29733) | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | Resuelto | `LIN-OBS-001` sección 6.2; `LIN-SEC-APP-001` extenderá con otras dimensiones de privacidad |
| Convenciones de nomenclatura de servicios y spans | `LIN-OBS-001` | `LIN-DEV-JAVA-001`, `LIN-K8S-001` | Resuelto | `LIN-OBS-001` Apéndice C |

---

### 5. Autenticación y seguridad

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Modelo de seguridad institucional (SAA + AD + transición WSO2/OIDC) | `LIN-SEC-APP-001` | `LIN-ARQ-001`, `LIN-API-REST-001`, `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` sección 3 define estado actual (SAA), modelo objetivo (WSO2 PoC) y tipos de usuario. La transición formal está respaldada por `arquitectura/ADR-WSO2-001.md` |
| Autenticación — delegación al SAA | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` sección 4 — prohíbe autenticación propia por aplicación |
| Autorización — token válido ≠ permiso implícito | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001` | Conforme | `LIN-SEC-APP-001 §5.3`–`§5.4` — la app valida permisos SAA por endpoint con `hasAuthority`. **Corregido 2026-08-09:** `LIN-API-REST-001 §7.2` proponía un RBAC propio con `hasRole('ROL_…')`, contrario al numeral 4 del dueño y además inoperante en Spring Security (`GOB-CHK-001` H24) |
| Tokens SAA — emisión, validación, renovación, prohibiciones | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-API-REST-001` | En borrador | `LIN-SEC-APP-001` sección 6 — prohíbe exponer claves criptográficas y crear tokens paralelos |
| Integración SAA en apps Spring Boot — patrón `SaaTokenValidationFilter` | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001` | En borrador | `LIN-SEC-APP-001` sección 8 — flujo, filtro, cliente, prohibiciones, manejo de indisponibilidad |
| OAuth2/OIDC — flujo y decisión de arquitectura | `LIN-ARQ-001` | `LIN-API-REST-001`, `LIN-SEC-APP-001`, `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` sección 3.2 documenta el modelo objetivo; la decisión de adopción requiere ADR cuando WSO2 esté operativo |
| Configuración Spring Security | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001` | En borrador | `LIN-SEC-APP-001` sección 9.1 define configuración mínima obligatoria. **Verificado 2026-08-05:** `LIN-DEV-JAVA-001` referencia `LIN-SEC-APP-001` para el filtro SAA (sección 8.3) y para secretos (sección 12), pero **no** cita la sección 9.1 de configuración de Spring Security — brecha de referencia a cerrar en la próxima revisión de `LIN-DEV-JAVA-001` |
| Cifrado en tránsito — HTTPS y límite de confianza de red | `LIN-SEC-APP-001` | `LIN-API-REST-001`, `LIN-K8S-001` | Conforme | `LIN-SEC-APP-001 §7.1`: HTTPS obligatorio en ambientes compartidos, con dos excepciones —`localhost` y el tramo intra-cluster de `ADR-TLS-INTERNO-001`—. La excepción interna se sostiene sobre la `NetworkPolicy` obligatoria de `LIN-K8S-001 §9.1`, que **sustituye** al cifrado como control; sin ella el servicio debe servir HTTPS extremo a extremo |
| Headers de seguridad HTTP obligatorios | `LIN-SEC-APP-001` | `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | Conforme | `LIN-SEC-APP-001 §7.3` — `X-Content-Type-Options`, `X-Frame-Options`, `HSTS`, `Cache-Control`. **Corregido 2026-08-09:** `LIN-API-REST-001 §7.4` los daba como «valor recomendado» y los asignaba al gateway en PoC (`GOB-CHK-001` H24) |
| Prohibición de credenciales en código o repositorio | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-CICD-001` | En borrador | `LIN-SEC-APP-001` sección 12.2 — prohibición absoluta, incluyendo `environment.ts` Angular |
| Gestión de secretos — K8s Secrets, rotación, separación por ambiente | `LIN-SEC-APP-001` | `LIN-DEV-JAVA-001`, `LIN-K8S-001` | En borrador | `LIN-SEC-APP-001` sección 12 — dueño provisional hasta que `LIN-K8S-001` sea creado |
| Secrets en Kubernetes — propiedad definitiva | `LIN-K8S-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | En borrador | `LIN-K8S-001` asume la propiedad de K8s Secrets; `LIN-SEC-APP-001` sección 12 es consumidor |
| CORS en producción (sin `*`) | `LIN-API-REST-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | Conforme | `LIN-SEC-APP-001` sección 7.4 agrega implementación Spring Boot; `LIN-API-REST-001` es el dueño de la regla |
| Seguridad frontend (XSS, token storage, guards, CSP) | `LIN-SEC-APP-001` | `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` sección 10 define reglas base; `LIN-FE-ANG-001` las aplica en su contexto Angular |
| OWASP Top 10 como baseline y herramientas de escaneo | `LIN-SEC-APP-001` | Todos | En borrador | `LIN-SEC-APP-001` sección 13 — SonarQube, Snyk/Dependency-Check, Trivy; gates de calidad en CI/CD |
| Controles de seguridad para sistemas legacy | `LIN-SEC-APP-001` | `LIN-BD-ORA-001`, `LIN-ARQ-001` | En borrador | `LIN-SEC-APP-001` sección 14 — tabla de 6 escenarios con obligación diferenciada; SAA como capacidad legacy crítica |

---

### 6. Estructura de proyecto Java

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Decisión de estilo arquitectónico táctico (Monolito Simple / Modular / Hexagonal) | `LIN-DIS-001` | `LIN-DEV-JAVA-001`, `LIN-ARQ-001` | Conforme | — |
| Estructura de paquetes Maven por estilo | `LIN-DEV-JAVA-001` | — | Resuelto | `LIN-DEV-JAVA-001` sección 3.1 separado en tres secciones: Estilo 1 (Monolito Simple), Estilo 2 (Modular), Estilo 3 (Hexagonal) |
| Regla de dependencia entre módulos Maven | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Convenciones de nomenclatura Java | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Inyección por constructor | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Records para DTOs | `LIN-DEV-JAVA-001` | — | Conforme | — |
| Uso de Lombok | `LIN-DEV-JAVA-001` | — | Conforme | — |
| `@Transactional` — cuándo y en qué capa | `LIN-DEV-JAVA-001` | — | Conforme | — |
| `BigDecimal` para valores monetarios | `LIN-DEV-JAVA-001` | — | Conforme | — |
| `GlobalExceptionHandler` — implementación | `LIN-DEV-JAVA-001` | — | Conforme | Implementa el contrato de `LIN-API-REST-001`; ver `LIN-DEV-JAVA-001` sección 11.1 |
| Revisión de código — verificaciones específicas de Java | `LIN-DEV-JAVA-001` | `LIN-CICD-001` | Resuelto | `LIN-DEV-JAVA-001 §16.2`: Checkstyle, JaCoCo y tabla de antipatrones Java. **Corregido 2026-08-09:** este documento redefinía las reglas generales del proceso (autoaprobación, revisores, máximo 400 líneas) que pertenecen a `LIN-VER-001 §12`; el límite de tamaño, además, quedaba de facto limitado a Java, dejando sin regla a los MR de Angular, SQL o manifiestos (`GOB-CHK-001` H23) |
| Adapter para PL/SQL legacy | `LIN-DEV-JAVA-001` | `LIN-BD-ORA-001` | Resuelto | `LIN-BD-ORA-001` sección 6.0 define el patrón; `LIN-DEV-JAVA-001` sección 13.5.3 lo implementa como adaptador `JdbcRepository` |
| Análisis estático de código (PMD) | `LIN-DEV-JAVA-001` | `LIN-SEC-APP-001`, `LIN-VER-001`, `LIN-CICD-001` | Resuelto | `LIN-DEV-JAVA-001` sección 12.3 define configuración, ruleset y custom rules XPath |

---

### 7. Pruebas

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Pirámide de pruebas y distribución por estilo | `LIN-DIS-001` | `LIN-TEST-001`, `LIN-DEV-JAVA-001` | Conforme | — |
| Tipos de prueba y pirámide por estilo arquitectónico | `LIN-TEST-001` | `LIN-DIS-001`, `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001` | Conforme | `LIN-TEST-001` secciones 3–4 definen clasificación y distribución por estilo (Simple, Modular, Hexagonal) |
| Cobertura mínima por capa y estilo (umbrales) | `LIN-TEST-001` | `LIN-DEV-JAVA-001`, `LIN-FE-ANG-001`, `LIN-CICD-001` | Resuelto | `LIN-TEST-001` sección 5 — Java: ≥65–70% global; capa negocio ≥75–85%; Angular: ≥70% statements. **Corregido 2026-08-05:** `LIN-DEV-JAVA-001 §15.3` mantenía una tabla propia con "Controladores REST 70%", umbral que `LIN-TEST-001 §5.1` prohíbe explícitamente, y el gate de PR (`§16.2`) apuntaba a esa tabla — no a `§12.1`, que sí se había corregido. Ambas remiten ahora al dueño (`GOB-CHK-001` H13.1) |
| Herramientas: JUnit 5, Mockito, Testcontainers, JaCoCo | `LIN-TEST-001` | `LIN-DEV-JAVA-001` | Conforme | `LIN-TEST-001` sección 11.1 — OracleContainer `gvenzl/oracle-xe:21-slim-faststart` |
| Pruebas de contrato — obligatoriedad y herramientas | `LIN-TEST-001` | `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | Conforme | `LIN-TEST-001 §6` — tabla de casos obligatorio/recomendado; OpenAPI validation como mínimo siempre. **Desbloqueado 2026-08-09** (`GOB-CHK-001` H22.3): el gate de publicación de `LIN-API-REST-001 §10.3` no pedía evidencia de prueba de contrato pese a declararla obligatoria el dueño; incorporada al gate |
| Pruebas de caracterización — técnica y criterios | `LIN-TEST-001` | `LIN-BD-ORA-001`, `LIN-DEV-JAVA-001` | Conforme | `LIN-TEST-001` sección 13 — dueño de la técnica; `LIN-BD-ORA-001` sección 6.0 declara cuándo es obligatorio |
| Naming conventions de tests (sufijos IT, CT, Test) | `LIN-TEST-001` | `LIN-DEV-JAVA-001` | Resuelto | `LIN-TEST-001` sección 3.2 — `*Test.java` (Surefire), `*IT.java` y `*CT.java` (Failsafe) |
| Evidencias obligatorias y criterios de paso a QA/PROD | `LIN-TEST-001` | `LIN-CICD-001` | Conforme | `LIN-TEST-001 §8`–`§9` define qué debe cumplirse; `LIN-CICD-001 §19.2` lo implementa como gate. **Desbloqueado 2026-08-17** (`GOB-CHK-001` H22.3 → H27): el pipeline llamaba «sugeridos» a sus criterios y omitía siete de los once del dueño. Alineados en ambos sentidos — `LIN-TEST-001 §9` dejó además de expresarse solo en el modelo de ramas legado |
| Gates automáticos de pruebas en pipeline | `LIN-CICD-001` | `LIN-TEST-001` | En borrador | `LIN-TEST-001` sección 7 declara que las pruebas deben ser automatizables; el gate lo define LIN-CICD-001 |
| Herramientas E2E y accesibilidad Angular | `LIN-TEST-001` | `LIN-FE-ANG-001` | Resuelto | Playwright preferente; Cypress solo donde ya existe (`LIN-TEST-001` sección 3.3 tabla de herramientas y sección 12.4; `LIN-FE-ANG-001` sección 14.2); axe-core para accesibilidad (`LIN-TEST-001` sección 12.5) |
| Pruebas de carga, estrés, resistencia, spike y smoke performance | `LIN-PERF-001` | `LIN-TEST-001`, `LIN-CICD-001` | En borrador | `LIN-TEST-001` sección 1 delega a `LIN-PERF-001`; `LIN-PERF-001` sección 5 define tipos |
| Herramienta preferente JMeter; alternativas k6 y Gatling | `LIN-PERF-001` | `LIN-CICD-001` | En borrador | `LIN-PERF-001` sección 7 |
| Diseño de escenarios, usuarios concurrentes, ramp-up, duración, think time | `LIN-PERF-001` | — | En borrador | `LIN-PERF-001` sección 10 |
| Umbrales p95/p99, TPS, error rate | `LIN-PERF-001` | `LIN-CICD-001` | En borrador | LIN-CICD-001 consumirá estos umbrales como gate automático. `LIN-PERF-001` sección 9 |
| Criterios de aceptación de performance y evidencia mínima de informe | `LIN-PERF-001` | `LIN-TEST-001` | En borrador | `LIN-PERF-001` sección 9.4 y sección 13 |

---

### 8. Base de datos

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Motores permitidos (Oracle 19c estándar, 11g restringido) | `LIN-BD-ORA-001` | `LIN-ARQ-001` | Conforme | — |
| Convenciones de nomenclatura BD | `LIN-BD-ORA-001` | — | Conforme | — |
| Esquemas por dominio funcional | `LIN-BD-ORA-001` | `LIN-ARQ-001` | Conforme | — |
| Política de migraciones versionadas (Flyway/Liquibase) | `LIN-BD-ORA-001` | `LIN-CICD-001` | Conforme | — |
| PL/SQL técnico permitido (constraints, vistas, auditoría) | `LIN-BD-ORA-001` | — | Conforme | — |
| Gobierno de PL/SQL legacy con lógica de negocio | `LIN-BD-ORA-001` | `LIN-DEV-JAVA-001`, `LIN-DIS-001` | Resuelto | `LIN-BD-ORA-001` sección 6.0: categorías, inventario, adapter Java, pruebas de caracterización y checklist |
| Restricción de nueva lógica de negocio en PL/SQL | `LIN-BD-ORA-001` | `LIN-ARQ-001`, `LIN-DIS-001` | Resuelto | `LIN-BD-ORA-001` sección 6.0 define categorías restrictivas y proceso ADR para excepciones |
| Adapter Java para invocar PL/SQL legacy | `LIN-DEV-JAVA-001` | `LIN-BD-ORA-001` | Resuelto | `LIN-BD-ORA-001` sección 6.0 define el patrón `SimpleJdbcCall`; referenciado en `LIN-DEV-JAVA-001` sección 13.5.3 |
| Prohibición de acceso directo entre dominios BD | `LIN-BD-ORA-001` | `LIN-ARQ-001`, `LIN-DIS-001` | Conforme | — |

---

### 8.1 Explotación y Analítica de Datos (BI / Lakehouse)

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Arquitectura Medallion (Bronze, Silver, Gold) | `LIN-BI-001` | `LIN-ARQ-001`, `LIN-BD-ORA-001` | En borrador | Define las capas de datos, formatos (Iceberg/Parquet) e ingestas. |
| Stack tecnológico homologado para analítica | `LIN-BI-001` | `LIN-ARQ-001` | En borrador | Basado en Sección 9 de Estándares de Tecnología v2.0 (MinIO, Spark, Airflow, Trino, Nessie, etc.). |
| Control de Versiones de Datos (Nessie Branching) | `LIN-BI-001` | `LIN-VER-001` | En borrador | Estrategia de ramas (`main`, `dev`, `feature/*`) para desarrollo de pipelines aislados. |
| Calidad, Gobernanza y Linaje de Datos | `LIN-BI-001` | `LIN-OBS-001` | En borrador | Great Expectations para validaciones, OpenMetadata para glosario y PII, y OpenLineage para trazabilidad. |
| Seguridad y Control de Acceso analítico | `LIN-BI-001` | `LIN-SEC-APP-001` | En borrador | Roles en Trino, políticas en MinIO, variables de secretos en Vault y enmascaramiento PII. |

---

### 9. Frontend

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Angular como framework SPA primario | `LIN-ARQ-001` | `LIN-FE-ANG-001` | Conforme | — |
| Estructura de proyecto Angular | `LIN-FE-ANG-001` | — | Conforme | — |
| Standalone components | `LIN-FE-ANG-001` | — | Conforme | — |
| **Verificación de fronteras del Monolito Modular (pruebas de arquitectura)** | `LIN-DIS-001` (reglas) / `LIN-DEV-JAVA-001` (implementación) | `LIN-TEST-001`, `LIN-CICD-001`, `LIN-ARQ-001` | Conforme | `LIN-DIS-001 §3` y `§3.4` definen las fronteras; `LIN-DEV-JAVA-001 §15.5` las implementa como reglas ArchUnit (tipo `AT` de `LIN-TEST-001 §3.1`) y `LIN-CICD-001 §19.2` las hace bloqueantes. **Creado 2026-08-18** (`GOB-CHK-001` H37): era el único control del Monolito Modular sin verificación automática — ni el pipeline ni el grafo de servicios podían comprobarlo |
| **Identificador único de un componente desplegable (`service.name`)** | `LIN-VER-001` | `LIN-OBS-001`, `LIN-K8S-001`, `LIN-API-REST-001`, `LIN-BUS-001` | Conforme | La forma canónica es la del proyecto GitLab (`LIN-VER-001 §9.1`): `<sistema>-<tipo-componente>[-<canal>]`. El **mismo** identificador rige en `app.kubernetes.io/name` (`LIN-K8S-001 §9.3`), en el catálogo de servicios (`LIN-API-REST-001 §10.1`) y en `service.name` de la telemetría (`LIN-OBS-001 §5.8.4`). **Corregido 2026-08-18:** existían tres convenciones para lo mismo, lo que impedía reconciliar el grafo observado con los registros declarativos (`GOB-CHK-001` H36) |
| **Grafo de servicios y arquitectura observada** | `LIN-OBS-001` (mecanismo) / `LIN-ARQ-001` (gobierno) | `GOB-PLA-001`, `LIN-API-REST-001`, `LIN-DIS-001` | Conforme | `LIN-OBS-001 §5.8` norma la generación del grafo desde las trazas, con la regla de que **las métricas se generan antes del muestreo**; `LIN-ARQ-001 §5.5` norma su uso para detectar deriva entre la arquitectura declarada y la real. **Creado 2026-08-18** (`GOB-CHK-001` H35). El grafo **no es un catálogo**: es la contraparte observada de los cuatro registros declarativos del corpus |
| **Continuidad operativa — criticidad, RTO/RPO y recuperación** | `LIN-ARQ-001` | `LIN-BD-ORA-001`, `LIN-K8S-001`, `LIN-IAC-001`, `LIN-BI-001`, `LIN-BUS-001`, `LIN-PERF-001`, `GOB-PLA-001` | Conforme | `LIN-ARQ-001 §5.4` — tres bandas de criticidad con RTO/RPO objetivo, política de respaldo por componente (delega el mecanismo a cada dueño), procedimiento de recuperación y régimen de pruebas. **Creado 2026-08-17** (`GOB-CHK-001` H11.2): era la mayor brecha de contenido del corpus. **Los valores son propuesta técnica sujeta a ratificación del Comité con las áreas usuarias** |
| **Escala de criticidad de sistemas (Alta / Media / Baja)** | `LIN-ARQ-001` | `LIN-PERF-001`, `LIN-CICD-001`, `LIN-K8S-001` | Conforme | `LIN-ARQ-001 §5.4.1` define las bandas; `LIN-PERF-001 §6.4` define cómo se determina. Antes la escala se usaba en tres documentos sin fuente común |
| Core Web Vitals — umbrales obligatorios | `LIN-ARQ-001` | `LIN-FE-ANG-001`, `LIN-CICD-001`, `LIN-PERF-001` | Conforme | `LIN-ARQ-001 §7.2` publica los siete umbrales (LCP, INP, CLS, FCP, TTI, TBT, FPS). `LIN-FE-ANG-001 §15.2` reproduce cuatro como referencia declarada; `LIN-CICD-001 §9.4` los implementa como gate. **Corregido 2026-08-17:** la fila no citaba sección y omitía a `LIN-PERF-001`, que listaba las métricas por tercera vez y sin umbrales (`GOB-CHK-001` H30) |
| Interceptores HTTP (auth, errores, request ID) | `LIN-FE-ANG-001` | — | Conforme | — |
| Modelos TypeScript de respuesta API | `LIN-FE-ANG-001` | — | Resuelto | `LIN-FE-ANG-001` corregido: `ApiMeta` completo con `timestamp`, `requestId`, `version`; errores con `campo`/`mensaje` |
| Estrategia de estado (Signals, store global) | `LIN-FE-ANG-001` | — | Pendiente | Pendiente de desarrollar en `LIN-FE-ANG-001` |
| Seguridad frontend (XSS, token storage, guards, CSP) | `LIN-SEC-APP-001` | `LIN-FE-ANG-001` | En borrador | `LIN-SEC-APP-001` sección 10 define reglas base; pendiente aplicar en `LIN-FE-ANG-001` |

---

### 10. Despliegue e infraestructura

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| K8s como destino por defecto (`containerd`) | `LIN-ARQ-001` | `LIN-K8S-001`, `LIN-CICD-001` | Conforme | — |
| Dockerfile estándar (multi-stage, Alpine, no root) | `LIN-K8S-001` | `LIN-CICD-001` | En borrador | `LIN-ARQ-001` tiene referencia transitoria; dueño definitivo `LIN-K8S-001` |
| Política de namespaces, tagging de imágenes | `LIN-K8S-001` | `LIN-CICD-001` | En borrador | Dueño `LIN-K8S-001` |
| Variables de entorno vs Secrets K8s | `LIN-K8S-001` | `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001` | En borrador | Dueño `LIN-K8S-001` |
| IaC — herramienta y estructura de repositorio | `LIN-IAC-001` | `LIN-CICD-001`, `LIN-K8S-001` | En borrador | `LIN-IAC-001` en borrador; define Terraform, repositorio dedicado `oti-plataforma/infrastructure-iac`, fases de madurez y pipeline de validación |

---

### 11. Gobierno arquitectónico en 3 Niveles

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Formato y proceso de ADR | `LIN-ARQ-001` | Todos | Resuelto | Proceso de excepción (ADR) agregado en todos los lineamientos existentes (`LIN-DEV-JAVA-001 §17`) |
| Proceso de excepción a estándares (Supremacía Normativa) | `LIN-ARQ-001` | Todos | Conforme | — |
| Cuándo es obligatoria revisión de arquitectura | `LIN-ARQ-001` | — | Conforme | — |
| Escala de Estadios de Topología (1 Legacy · 2 Monolito Modular · 3 Microservicios) | `LIN-ARQ-001` | `LIN-PAT-001`, `GLOSARIO-ONP`, `Plantilla_Documento_Arquitectura` | Resuelto | `LIN-ARQ-001 §2.1` es la escala oficial. Corregida en 2026-08-05 una escala paralela 0/1/2 que circulaba en Glosario, 4 fichas de `LIN-PAT-001` y 3 guías de la Plantilla (`GOB-CHK-001` H1). Strangler Fig es estrategia de transición (`§2.2`), no un estadio |
| Códigos `PT` de patrones — fuente única | `LIN-PAT-001` | `LIN-DIS-001`, `LIN-SEC-APP-001`, `Brecha_Framework` | En borrador | `LIN-PAT-001` es autoritativo. `Brecha_Framework` mantiene asignaciones contradictorias de PT09/PT10/PT12 pendientes de corrección (`GOB-CHK-001` H6.3) |
| Categorías y ciclo de vida de Feature Toggles (PA14) | `LIN-ARQ-001` | `LIN-DEV-JAVA-001`, `LIN-VER-001`, `LIN-CICD-001` | Resuelto | `LIN-ARQ-001 §2.3` + `ADR-014` definen 4 categorías: Release y Experiment (caducidad obligatoria), Ops y Permission (pueden ser permanentes). **Ampliado 2026-08-05:** `LIN-DEV-JAVA-001 §16.6` operaba con Experiment Toggle sin respaldo del marco rector, cuya descripción de Permission mezclaba dos ciclos de vida opuestos; se incorporó Experiment al Nivel 1 y se acotó Permission a control de acceso (`GOB-CHK-001` H13.3) |
| Configuración institucional de Checkstyle | `LIN-DEV-JAVA-001` | Templates GitLab, `LIN-CICD-001` | Resuelto | Archivo canónico: `desarrollo/plantillas/checkstyle-onp.xml`; `LIN-DEV-JAVA-001 §12.1` norma los umbrales y el Anexo B lo reproduce como copia de referencia. **Corregido 2026-08-05:** el canónico y el Anexo B declaraban `LineLength` dentro de `TreeWalker` — configuración que aborta el build desde Checkstyle 8.24 — y los templates enviaban una versión reducida sin las 3 métricas obligatorias (`GOB-CHK-001` H4.4, H4.7, H13.2) |
| Guía v0.1.2 — archivada | — | `LIN-OBS-001`, `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | **Archivado** | secciones 1–2 absorbidos por `LIN-OBS-001`; sección 3 absorbido por `LIN-DEV-JAVA-001` y `LIN-API-REST-001`. La guía está marcada como archivada. El valor de "orden de inicio" se preserva en `LIN-DEV-JAVA-001 sección 1.3 — Configuración inicial de un proyecto nuevo`. No actualizar la guía. |

---

### 12. Versionamiento y control de cambios

| Tema | Dueño | Consumidores | Estado | Observación |
|---|---|---|---|---|
| Estrategia de ramas Git — modelo vigente (`ONP_DESA` → `ONP_PQA` → `ONP_QA` → `master`) | `LIN-VER-001` | `LIN-K8S-001`, `LIN-CICD-001` | En borrador | Ramas por promoción. `LIN-VER-001` sección 5 |
| Estrategia de ramas Git — modelo objetivo (GitLab Flow simplificado) | `LIN-VER-001` | `LIN-K8S-001`, `LIN-CICD-001` | En borrador | Modelo por defecto para proyectos nuevos: `main` único, ramas cortas, mismo artefacto promovido. `LIN-VER-001` sección 6 |
| Merge Requests — canal obligatorio de integración | `LIN-VER-001` | Todos | En borrador | Sin MR no hay cambio a ramas protegidas. `LIN-VER-001` sección 11 |
| Revisión de código — proceso, revisores y tamaño de MR | `LIN-VER-001` | Todos | Resuelto | `LIN-VER-001 §12` es dueño del proceso para **todo** tipo de cambio, no solo código: prohibición de autoaprobación, revisor mínimo, revisor especializado por tipo y máximo de 400 líneas |
| Tags y releases — versionamiento semántico | `LIN-VER-001` | `LIN-K8S-001`, `LIN-CICD-001` | En borrador | Todo release productivo debe tener tag semántico (`vMAJOR.MINOR.PATCH`). `LIN-VER-001` sección 14 (semver) y sección 15 (tags y releases) |
| Nomenclatura de repositorios y grupos GitLab | `LIN-VER-001` | Todos | En borrador | Grupo → Subgrupo → Proyecto. Convención de nombres en minúsculas con guion. `LIN-VER-001` secciones 8–9 |
| Obligatoriedad de versionar scripts de BD en GitLab | `LIN-VER-001` | `LIN-BD-ORA-001`, `LIN-CICD-001` | Resuelto | `LIN-VER-001 §16` — no se aceptan scripts por correo ni mensajería; todo cambio de BD va versionado en el repositorio |
| Nomenclatura y estructura de scripts de BD | `LIN-BD-ORA-001` | `LIN-VER-001`, `LIN-CICD-001` | Resuelto | `LIN-BD-ORA-001 §8` — patrón `PP_<ORIGEN>_<NUMERO>_<ESQUEMA>_<TIPO>_<NN>.SQL`, estructura obligatoria, reversa y modelo Flyway. **Separado en dos temas el 2026-08-09** (`GOB-CHK-001` H14.5): figuraba con **dos dueños simultáneos**, lo que contradice el principio rector de esta matriz. El reparto real ya estaba descrito en la observación; ahora lo refleja la columna de propiedad |
| Plantillas institucionales GitLab (Group-level Project Templates) | `LIN-VER-001` | `LIN-CICD-001` | En borrador | Plataforma implementa; Arquitectura provee ejemplos de referencia. `LIN-VER-001` Anexo F |

---

## Plan de correcciones priorizadas

Correcciones a aplicar sobre los documentos existentes antes de redactar nuevos lineamientos:

| Prioridad | Corrección | Documento a modificar | Estado |
|---|---|---|---|
| 1 | Agregar código `LIN-xxx` en el encabezado de cada documento | Todos los existentes | **Aplicado** |
| 2 | `ApiResponseWrapper<T>` con genérico — reemplazar `Object data` por `T data` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 3 | `POST` creación exitosa → `201 Created` (tabla HTTP y ejemplos) | `LIN-API-REST-001`, `LIN-DEV-JAVA-001` | **Aplicado** |
| 4 | Unificar campos JSON en español: `campo`/`mensaje` en `CampoError` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 5 | Configuración Swagger: cambiar `.properties` por `.yml` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 6 | Nivel de log producción: `INFO` para `pe.gob.onp.*`, `WARN` para frameworks | `LIN-DEV-JAVA-001` | **Aplicado** |
| 7 | Testcontainers: cambiar `PostgreSQLContainer` por `OracleContainer` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 8 | Bug Markdown sección 13.4.7 (citada como 11.4.7 antes de la renumeración interna): cerrar bloque de código del record con `@Schema` | `LIN-DEV-JAVA-001` | **Aplicado** |
| 9 | Agregar sección WSO2 API Manager (Gateway + Management) | `LIN-API-REST-001` | **Aplicado** |
| 10 | Unificar campos de paginación `pagina`/`tamanio` y `ApiMeta` completo en Frontend | `LIN-FE-ANG-001` | **Aplicado** |
| 11 | Agregar sección de adapter Java para invocar PL/SQL legacy | `LIN-BD-ORA-001` sección 6.0 | **Aplicado** |
| 12 | Agregar sección de gobierno PL/SQL legacy (catálogo, categorías, checklist) | `LIN-BD-ORA-001` sección 6.0 | **Aplicado** |
| 13 | Agregar code review como gate obligatorio (hoy sección 16) | `LIN-DEV-JAVA-001` | **Aplicado** |
| 14 | Agregar proceso de excepción (ADR) en lineamientos Java, API, BD y Frontend (en Java, hoy sección 17) | Todos los existentes | **Aplicado** |
| 15 | Crear LIN-OBS-001 absorbiendo Guía v0.1.1 secciones 1–2 (trazas, logs, correlación, métricas) | `observabilidad/Lineamiento_Log_Trazabilidad_Observabilidad_ONP.md` | **Aplicado** |
| 16 | Actualizar referencias en `LIN-DEV-JAVA-001` sección 10 para que referencien `LIN-OBS-001` como fuente de logback, campos ECS y política No PII | `LIN-DEV-JAVA-001` | **Aplicado** |
| 17 | Crear LIN-SEC-APP-001 absorbiendo seguridad de SAA, Spring Security, secrets, OWASP, legacy y frontend | `seguridad/Lineamiento_Seguridad_Aplicaciones_ONP.md` | **Aplicado** |
| 18 | Crear LIN-TEST-001 como dueño de la estrategia de pruebas: tipos, pirámide, cobertura, contrato, caracterización, E2E | `pruebas/Lineamiento_Estandar_Pruebas_ONP.md` | **Aplicado** |

---

## Catálogo de Patrones de Arquitectura ONP — entregado

> **Estado: cerrado.** El catálogo se entregó como **`LIN-PAT-001`** (`arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md`), no en la ruta `patrones/` prevista originalmente. Las fichas `PAT-*` son la referencia de decisión; la norma sigue viviendo en el documento dueño de cada tema.

**`LIN-PAT-001` es la fuente única de los códigos `PT` y de las fichas `PAT`.** Ningún documento puede asignar un código `PT` a un patrón distinto del que aquí se registra. Esta tabla es el índice de trazabilidad `PT → ficha → dueño normativo`; las secciones concretas viven en la ficha, no en esta matriz, para que una renumeración interna no rompa este índice.

| Código PT | Patrón | Ficha en `LIN-PAT-001` | Dueño normativo |
|---|---|---|---|
| PT01 | Publisher/Subscriber (Kafka) | `PAT-MSG-01` | `LIN-BUS-001` |
| PT02 | Dead Letter Queue (DLQ) | `PAT-MSG-02` | `LIN-BUS-001` |
| PT03 | Event Sourcing | — sin ficha (**no adoptado**) | `LIN-BUS-001` §4.3 — requiere ADR aprobado |
| PT04 | CQRS | `PAT-DIS-04` | `LIN-DIS-001` / `LIN-BUS-001` |
| PT05 | API Gateway / API Manager (WSO2) | `PAT-INT-05` | `LIN-API-REST-001` / `LIN-ARQ-001` |
| PT06 | Retry | — sin ficha (normado sin ficha) | `LIN-DIS-001` §6.4 (REST) / `LIN-BUS-001` §8.5 (Kafka) |
| PT07 | Circuit Breaker | `PAT-RES-01` | `LIN-DIS-001` / `LIN-API-REST-001` |
| PT08 | Bulkhead | `PAT-RES-02` | `LIN-DIS-001` / `LIN-K8S-001` |
| PT09 | Saga | `PAT-MSG-03` | `LIN-ARQ-001` / `LIN-BUS-001` |
| PT10 | Strangler Fig | `PAT-TOP-03` | `LIN-ARQ-001` |
| PT11 | Backend for Frontend (BFF) | `PAT-INT-01` | `LIN-DIS-001` |
| PT12 | Gateway-Aggregation | `PAT-INT-02` | `LIN-DIS-001` / `LIN-API-REST-001` |
| PT13 | Anti-Corruption Layer (ACL) | `PAT-INT-04` | `LIN-DIS-001` |
| PT14 | Adapter / Decorator / Strategy (GoF) | `PAT-DEV-01` | `LIN-DEV-JAVA-001` / `LIN-DIS-001` |
| PT15 | Facade Arquitectónico de Integración | `PAT-INT-03` | `LIN-DIS-001` |
| PT16 | Arquitectura Medallón | `PAT-BI-01` | `LIN-BI-001` |
| PT17 | Sidecar (multi-contenedor en el Pod) | `PAT-K8S-01` | `LIN-K8S-001` §9.4.A — prohibido en Java/Spring Boot 3 |
| PT18 | Ambassador (proxy de salida del Pod) | `PAT-K8S-02` | `LIN-K8S-001` §9.4.B — prohibido en Java/Spring Boot 3 |

**Fichas sin código PT** (patrones institucionales incorporados después de la numeración PT original): `PAT-TOP-01` (Monolito Modular), `PAT-TOP-02` (Microservicios), `PAT-DIS-01` (Hexagonal), `PAT-DIS-02` (Capas), `PAT-DIS-03` (Bounded Context y Agregados DDD), `PAT-DEV-02` (`SaaTokenValidationFilter`), `PAT-DAT-01` (Adapter PL/SQL), `PAT-DAT-02` (Transactional Outbox), `PAT-DAT-03` (CDC).

> ⚠️ **Discrepancia abierta:** el tablero `Brecha_Framework_Arquitectura_ONP` asigna `PT09` a BFF, `PT10` a Gateway-Aggregation y `PT12` a Facade, contradiciendo esta tabla y `LIN-PAT-001`. **Prevalece `LIN-PAT-001`.** La corrección del tablero está registrada en `GOB-CHK-001` (H6.3).

---

## Regla de actualización de esta matriz

Cada vez que se redacte un lineamiento nuevo o se modifique uno existente, esta matriz debe revisarse para:

1. Registrar el nuevo documento en el catálogo, con su versión vigente.
2. Verificar que no se haya duplicado un tema ya asignado a otro dueño.
3. Marcar como resueltos los conflictos corregidos.
4. Identificar nuevos conflictos o brechas que emerjan del nuevo contenido.
5. Confirmar que todo `Conforme` o `Resuelto` tenga respaldo en la versión vigente del documento citado.
6. Registrar la fecha de la última validación integral cuando el corpus cambie de forma significativa.
7. **Verificar el cierre también en los artefactos ejecutables** — templates GitLab, plantillas de código, `checkstyle-onp.xml`, manifiestos K8s. Una corrección aplicada solo en el lineamiento **no** puede marcarse `Resuelto` si el template que los proyectos usan como baseline sigue divergiendo (lección del hallazgo H4 de `GOB-CHK-001`: el contrato `ApiResponseWrapper` figuraba "Aplicado" mientras el template oficial seguía usando `FieldError`).
8. **Al renumerar secciones de un documento dueño, corregir en el mismo cambio las citas de esta matriz y de los consumidores.** Una renumeración silenciosa rompe el índice de propiedad — causa raíz de las citas obsoletas corregidas en v0.3.0.

---

## Historial de versiones

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 0.1.x | 2026-05-28 | Arquitectura OTI | Versiones iniciales de la matriz de propiedad y plan de correcciones |
| 0.2.0 | 2026-07-08 | Arquitectura OTI | Alineación al modelo de 3 Niveles de Arquitectura (`LIN-ARQ-001`, `LIN-DIS-001`, `LIN-DEV-JAVA-001`) |
| 0.8.0 | 2026-08-09 | Arquitectura OTI | **Alcance del criterio 2 precisado** (`GOB-CHK-001` H22.5): se distingue entre hallazgos **de documento** —que bloquean la graduación— y **de corpus** —que se registran como deuda con responsable y fecha—, con una **excepción por severidad**: un hallazgo transversal que comprometa el uso normativo de un documento concreto se escala y sí bloquea. Sin esta distinción el criterio se rompía por ambos extremos: exigirlo todo congelaba la graduación del corpus entero, y no exigir nada permitiría declarar `Vigente` un documento inutilizable. Aplicado a las anclas internas rotas: no bloquea en general, **sí bloquea a `LIN-BD-ORA-001` y `LIN-BI-001`**, cuyos índices completos no resuelven por titular las secciones como `## sección N` |
| 0.24.0 | 2026-08-18 | Arquitectura OTI | **Alineación completa del registro de excepciones** (`GOB-CHK-001` H38): once lineamientos titulaban su apartado «Proceso ADR para excepciones/desviaciones» sin identificador propio, de modo que una desviación de proyecto se registraba como «un ADR» — instrumento que esta matriz reserva a las decisiones institucionales del Comité. Todos usan ya `EXC-<SUFIJO>-NNN`. Se añade la **tabla de sufijos por lineamiento**, para que el identificador no quede a criterio de cada equipo, y la regla de numeración correlativa por lineamiento **y por sistema** |
| 0.23.0 | 2026-08-18 | Arquitectura OTI | Nuevo tema **verificación de fronteras del Monolito Modular**, con propiedad dividida entre `LIN-DIS-001` (las reglas) y `LIN-DEV-JAVA-001` (su implementación en ArchUnit). Cierra la deuda que dejó a la vista el trabajo del grafo de servicios: la topología por defecto de la ONP descansaba en una declaración jurada sin verificación de ninguna clase (`GOB-CHK-001` H37) |
| 0.22.0 | 2026-08-18 | Arquitectura OTI | Nuevo tema: **identificador único de un componente desplegable**. El corpus usaba tres convenciones para el mismo nombre —telemetría, proyecto GitLab y etiqueta de Kubernetes— sin que ninguna fuera declarada canónica. Se adopta la de `LIN-VER-001 §9.1` y se elimina el prefijo `onp-` de la telemetría. Sin esto, las verificaciones de `LIN-ARQ-001 §5.5` no podían automatizarse (`GOB-CHK-001` H36) |
| 0.21.0 | 2026-08-18 | Arquitectura OTI | Nuevo tema **grafo de servicios y arquitectura observada**, con propiedad dividida: el mecanismo en `LIN-OBS-001 §5.8` y el gobierno en `LIN-ARQ-001 §5.5`. Cierra un vacío de verificación —el Anexo A de `GOB-PLA-001` era una arquitectura declarada sin forma de detectar su deriva— y refuerza la regla de dependencias de `§5.4.1`, cuya verificación era hasta ahora puramente documental (`GOB-CHK-001` H35) |
| 0.20.0 | 2026-08-18 | Arquitectura OTI | Nueva regla de **vigencia de los documentos de arquitectura de proyecto**: declaran conformidad con el corpus en la versión de esta matriz que registran, no con el corpus perpetuo, y deben revisarse cuando un documento del que dependen gradúa a `Vigente` — el disparador de mayor impacto, porque cambia lo que es exigible. Arquitectura OTI comunica cada graduación. Detalle en `GOB-PLA-001 §1.5` (`GOB-CHK-001` H34.5) |
| 0.19.0 | 2026-08-17 | Arquitectura OTI | `GOB-PLA-001` v2.5: incorpora criterios de aprobación verificables (Anexo E), las declaraciones obligatorias que el corpus exigía sin que la plantilla las pidiera —Estadio, CAP, DDD, Declaración de Conformidad— y el corpus completo en `§5.2`, que listaba 6 de 19 documentos (`GOB-CHK-001` H34) |
| 0.18.0 | 2026-08-17 | Arquitectura OTI | **Cierra H11.2, la mayor brecha de contenido del corpus.** `LIN-ARQ-001 §5.4` asume la continuidad operativa: bandas de criticidad con RTO/RPO objetivo, política de respaldo por componente, recuperación a nivel de sistema y pruebas de restauración obligatorias. Se registran dos temas nuevos y se sincronizan los seis documentos afectados. La escala de criticidad, que `LIN-PERF-001`, `LIN-CICD-001` y `LIN-K8S-001` usaban sin fuente común, pasa a tener dueño |
| 0.17.0 | 2026-08-17 | Arquitectura OTI | `LIN-BI-001` v0.1.3 pasa a **En revisión**. **Con ello ningún lineamiento del corpus queda en `Borrador`** — cierra H14.6, la lectura de contenido de los 19 documentos (`GOB-CHK-001` H32) |
| 0.16.0 | 2026-08-17 | Arquitectura OTI | Se norma el **registro de decisiones y excepciones** distinguiendo tres instrumentos que el corpus venía llamando «ADR» indistintamente: `ADR-NNN` institucional, `AD-NNN` de proyecto y `EXC-<CÓDIGO>-NNN` para desviaciones de un lineamiento. Doce de los trece documentos titulaban su apartado «Proceso ADR para excepciones» sin identificador propio; solo `LIN-VER-001` había definido el formato. Sincroniza `LIN-IAC-001` v0.1.3, **En revisión** (`GOB-CHK-001` H32) |
| 0.15.0 | 2026-08-17 | Arquitectura OTI | Revisión de `GOB-PLA-001` v2.3 y reconciliación del registro de ADRs (`GOB-CHK-001` H31): el catálogo de ADRs de esta matriz y el **Apéndice A de `LIN-ARQ-001`** eran dos listas desconectadas, con una misma decisión —CloudEvents— registrada bajo dos identificadores. Cada ADR en archivo declara ahora su equivalencia con la matriz institucional |
| 0.14.0 | 2026-08-17 | Arquitectura OTI | Revisiones de `LIN-FE-ANG-001` v0.1.4 y `LIN-PERF-001` v0.1.3, ambos **En revisión** (`GOB-CHK-001` H29 y H30). La fila de **Core Web Vitals** no citaba sección de referencia —única del catálogo en ese estado— y omitía a `LIN-PERF-001` como consumidor, que listaba las métricas por tercera vez sin umbrales |
| 0.13.0 | 2026-08-17 | Arquitectura OTI | Sincroniza el catálogo tras la revisión de fondo de `LIN-BUS-001` v0.1.7 (**En revisión**) y la corrección derivada en `LIN-K8S-001` v0.1.15, que citaba mal los namespaces de Kafka (`GOB-CHK-001` H28) |
| 0.12.0 | 2026-08-17 | Arquitectura OTI | **«Evidencias obligatorias y criterios de paso a QA/PROD» pasa de `En borrador` a `Conforme`** (`GOB-CHK-001` H27). Era el segundo de los dos temas que la Fase 1 dejó deliberadamente abiertos por no poder confirmar alineación con su consumidor: `LIN-CICD-001 §19.2` titulaba «sugeridos» sus criterios de bloqueo y omitía siete de los once que `LIN-TEST-001 §9` declara bloqueantes. **Con esto no queda ningún tema abierto de la Fase 1.** Sincroniza el catálogo con `LIN-CICD-001` v0.1.6 (**En revisión**) y `LIN-TEST-001` v0.1.4 |
| 0.11.0 | 2026-08-17 | Arquitectura OTI | Incorpora `PT17` (Sidecar) y `PT18` (Ambassador) al índice de trazabilidad `PT → ficha → dueño`, tras detectarse que `LIN-K8S-001 §9.4` los normaba con los códigos `PA12`/`PA13` del **tablero de brechas**, no del catálogo oficial (`GOB-CHK-001` H26). Sincroniza el catálogo con `LIN-DIS-001` v0.1.6, `LIN-PAT-001` v0.1.6, `LIN-K8S-001` v0.1.14 (que pasa a **En revisión**) y `GOB-BRE-001` v0.1.7 |
| 0.10.0 | 2026-08-14 | Arquitectura OTI | **El catálogo estaba desactualizado en 15 de sus 21 entradas** (`GOB-CHK-001` H25): declaraba `LIN-API-REST-001` como «Borrador v0.1.5» cuando iba por «En revisión v0.1.7», y `LIN-VER-001` como Borrador tras su graduación a En revisión. No es un descuadre cosmético — el catálogo es lo que un tercero consulta para saber qué documento rige, y la **regla de exigibilidad se aplica sobre el estado declarado**: un estado equivocado permite dar por exigible lo que aún no lo es. La causa era una brecha del linter, cuya comprobación C6 valida la ruta y el código del catálogo pero no la versión ni el estado; se añade **C8** para cerrarla. Se corrige además el sentido inverso en `LIN-BD-ORA-001`, cuyo encabezado seguía en `Borrador` pese a tener la revisión cerrada |
| 0.9.0 | 2026-08-09 | Arquitectura OTI | Cierre de las dos decisiones elevadas por la revisión de `LIN-API-REST-001` (`GOB-CHK-001` H24). Se registra **`ADR-TLS-INTERNO-001`** en el catálogo de ADRs y se abre el tema **«Cifrado en tránsito — HTTPS y límite de confianza de red»**, con dueño `LIN-SEC-APP-001`: el tramo intra-cluster sobre HTTP queda admitido a condición de `NetworkPolicy` obligatoria, que **sustituye** al cifrado como control. Se actualizan además cuatro temas cuyo estado no reflejaba la realidad —rate limiting (cambia de dueño de `LIN-API-REST-001` a `LIN-SEC-APP-001`), autorización, headers de seguridad y pruebas de contrato—, este último desbloqueando uno de los dos casos que la Fase 1 dejó abiertos |
| 0.7.0 | 2026-08-09 | Arquitectura OTI | **Primeras graduaciones a Vigente** (`GOB-CHK-001` Fase 1): `LIN-OBS-001` v0.1.3 y `LIN-TEST-001` v0.1.3, tras verificar los cinco criterios. La verificación del **criterio 3** —alineación con consumidores— no fue un trámite: detectó dos desalineamientos reales que se corrigieron antes de graduar (`LIN-FE-ANG-001` citaba `LIN-TEST-001 §4.4`, que es *Microservicio*, para herramientas E2E; y `LIN-DEV-JAVA-001 §15.1` omitía el sufijo `CT`). Se actualizan los temas de la matriz cuyo dueño es `LIN-TEST-001`: cuatro pasan a `Conforme`/`Resuelto` con evidencia verificada; **dos permanecen `En borrador`** —pruebas de contrato y evidencias de paso a QA/PROD— porque no se pudo confirmar alineación real con sus consumidores, aplicando la regla editorial que exige no usar `Conforme` ante duda razonable |
| 0.6.0 | 2026-08-08 | Arquitectura OTI | **Ciclo de vida documental definido** (`GOB-CHK-001` H17). El corpus se declaraba de aplicación obligatoria para fábricas y terceros (`LIN-ARQ-001 §1.1` y `§8`) mientras **14 de 22 documentos figuraban como borrador** y no existía en ningún lugar un proceso de graduación: ni `LIN-VER-001` (versiona código, no normativa), ni la regla de mantenimiento de esta matriz, ni `LIN-DOC-001` (pendiente sin archivo). Se define: 4 estados de flujo (`Borrador → En revisión → Vigente → Deprecado`) más `Congelado` terminal; 5 criterios de graduación, 4 de ellos automatizables; aprobación por Comité de Arquitectura para Nivel 1–2 y por Arquitectura OTI para el resto; y la **regla de exigibilidad**: un documento vigente puede referenciar un borrador pero no hacerlo exigible. Esa regla revela una infracción abierta —`LIN-ARQ-001 §8.3` exige contractualmente `LIN-OBS-001` y `LIN-TEST-001`, ambos borrador— y de ella se deriva la ruta de graduación priorizada |
| 0.5.0 | 2026-08-05 | Arquitectura OTI | **Catálogo completo: todo documento del corpus declara código** (`GOB-CHK-001` H12.7). Cinco documentos carecían de campo `Código:` y por tanto no podían ser destino verificable de citas: se asignó `GOB-INI-001` (START HERE), `GOB-PLA-001` (Plantilla de Documento de Arquitectura) y `GOB-BRE-001` (Tablero de Brechas), y se declaró en el encabezado el código que los dos ADR ya llevaban en su título. El catálogo se reorganiza en tres bloques —lineamientos, documentos de gobierno y ADR con documento propio— y el linter del corpus pasa a **0 errores y 0 avisos** |
| 0.4.0 | 2026-08-05 | Arquitectura OTI | **Convención de nombres de archivo sin versión** (`GOB-CHK-001` H8): los 21 documentos del corpus se renombraron eliminando el sufijo `_vX.Y.Z`, que llevaba congelado en `v0.1.0` mientras los encabezados avanzaban hasta `v0.1.12`. Se eliminaron además las versiones embebidas en 5 pies de página, todas desactualizadas respecto a su propio encabezado. La versión vigente queda declarada en un único lugar (el encabezado) y el historial lo provee Git. Las copias con sufijo `_OLD` se conservan sin cambios y quedan fuera del corpus a efectos de validación y citas. Regla incorporada al cuerpo de esta matriz |
| 0.3.0 | 2026-08-05 | Arquitectura OTI | Reconciliación derivada de la revisión integral (`GOB-CHK-001` H5): (a) `LIN-BUS-001` incorporado al catálogo con su archivo y versión — figuraba "Pendiente / sin archivo" pese a estar vigente y ser citado por 6 documentos; (b) versiones del catálogo actualizadas (`LIN-VER-001` 0.1.5→0.1.6, `LIN-PAT-001`, `GLOSARIO-ONP`) e incorporación de `GOB-CHK-001`; (c) **11 citas obsoletas corregidas** a `LIN-DEV-JAVA-001` por renumeración interna nunca propagada (11.4.x→13.4.x, §8→§10 para logging, §9→§11.1 para `GlobalExceptionHandler`, §14→§16 para code review, §10.3→§12.3 para PMD, §8→§13.5.3 para adapter PL/SQL) y a `LIN-TEST-001` (§4.4→§3.3/§12.4 para herramientas E2E); (d) sección "Catálogo de Patrones — pendiente" cerrada y reemplazada por el índice de trazabilidad `PT → ficha PAT`, declarando a `LIN-PAT-001` fuente única de códigos `PT`; (e) reglas 7 y 8 de mantenimiento añadidas (verificar artefactos ejecutables; propagar renumeraciones); (f) versión del pie unificada con el encabezado |

---

*Matriz de Propiedad Documental — ONP · `GOB-MAT-001`*  
*OTI — Oficina de Tecnologías de la Información*
