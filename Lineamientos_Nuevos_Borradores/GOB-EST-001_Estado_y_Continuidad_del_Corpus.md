# GOB-EST-001 — Estado y Continuidad del Corpus de Arquitectura ONP

**Código:** GOB-EST-001
**Versión:** v1.0.0
**Estado:** Vigente / Operativo
**Fecha de corte:** 2026-08-21
**Corpus a esta fecha:** `v0.9.0` (tag Git) · `GOB-MAT-001` v0.29.0
**Propietario:** Arquitectura de Software — OTI

---

## Cómo usar este documento

Este es el **punto de retomada**. Está escrito para alguien —incluido su autor dentro de seis meses— que necesita continuar el trabajo sin releer 26.000 líneas.

| Documento | Para qué |
|---|---|
| **`GOB-EST-001`** (este) | Dónde estamos, qué falta, en qué orden y qué **no** volver a discutir |
| `GOB-CHK-001` | Registro cronológico de los 44 bloques de hallazgos, con el detalle de cada corrección y **por qué** se hizo |
| `GOB-MAT-001` | Qué documento es dueño de cada tema; la fuente de verdad del corpus |

> Este documento **no duplica** el checklist: remite a él. Duplicarlo produciría dos registros que divergen, que es el defecto que este ejercicio corrigió veinte veces.

---

## 1. Estado actual, verificado

### 1.1 Cifras

| Métrica | Valor |
|---|---|
| Documentos del corpus | 17 lineamientos + 5 de gobierno + 3 ADR |
| Líneas totales | ~26.200 |
| Estado: `Vigente` | **2** — `LIN-OBS-001`, `LIN-TEST-001` |
| Estado: `En revisión` | **15** |
| Estado: `Borrador` / `Pendiente` | **0** |
| Bloques de hallazgos cerrados | 44 (232 ítems) |
| Comprobaciones automáticas | 10, todas en verde |
| Temas de `GOB-MAT-001` sin verificar (`En borrador`) | **37** de ~150 |

### 1.2 Verificación en un comando

```bash
python3 herramientas/lint_corpus.py            # 10 comprobaciones; debe dar 0 errores
python3 herramientas/lint_corpus.py --indice   # mapa ID estable → documento §sección
```

**Si esto no está en verde, no se avanza en nada más.** El linter es el único control que no depende de que alguien recuerde revisar.

### 1.3 Qué significa que solo 2 documentos sean `Vigente`

Por la **regla de exigibilidad** de `GOB-MAT-001`, un documento `En revisión` obliga como criterio técnico pero **no puede invocarse como criterio de aceptación en un TDR**. Hoy, quince de diecisiete lineamientos no son contractualmente exigibles.

**Esa es la brecha principal del corpus, y no se cierra escribiendo más: se cierra graduando.**

---

## 2. Qué se hizo, y por qué importa

Resumen por naturaleza. El detalle de cada uno está en `GOB-CHK-001`.

### 2.1 Coherencia entre documentos

Se corrigieron **~95 citas** rotas o mal dirigidas. El patrón dominante no fue la cita rota —esa falla y se ve— sino **la cita que sigue resolviendo y apunta a otro tema**: `LIN-PAT-001` citaba `LIN-ARQ-001 §6.1` (Oracle) para Monolito Modular, y nueve fichas heredaron el error.

También se eliminaron fuentes paralelas del mismo dato: los timeouts estaban en **cuatro** documentos con valores distintos, la cobertura en cuatro, los Core Web Vitals en tres.

### 2.2 Defectos que rompían en ejecución

No eran problemas de redacción — el código de ejemplo es lo que las fábricas copian:

| Defecto | Dónde | Efecto |
|---|---|---|
| Consumidor Kafka que **perdía mensajes** | `LIN-BUS-001 §8.3` | Prometía reintento; con `ack-mode: MANUAL` el offset avanzaba y el mensaje se perdía |
| `readOnlyRootFilesystem` sin volumen `/tmp` | `LIN-K8S-001 §9.2` | El pod no arranca |
| `codDetRespuesta 200` como éxito | `LIN-FE-ANG-001 §9.6` | Un HTTP 422 se mostraba al ciudadano como operación exitosa |
| Token SAA descrito como JWT | `GOB-PLA-001 C.1` | Diseñar validación local de un token que no la admite |
| `hasRole('ROL_…')` | `LIN-API-REST-001 §7.2` | Spring antepone `ROLE_`: el endpoint queda cerrado para todos |
| `image: …:latest` | Templates | Prohibido por el propio corpus |

### 2.3 Vacíos de contenido cerrados

- **Continuidad operativa** (`ARQ-R-006`): bandas de criticidad con RTO/RPO, política de respaldo por componente, recuperación a nivel de sistema y pruebas de restauración **con medición del tiempo real**.
- **Datos personales en ambientes no productivos** (`SEC-R-003`): el corpus protegía el dato en producción y dejaba abierta la copia a DEV/QA, que es por donde se fuga.
- **`LIN-DOC-001`**: inventario documental, README y runbook, ninguno normado antes.
- **Verificación de fronteras del Monolito Modular**: `LIN-DEV-JAVA-001 §15.5` (ArchUnit). Era el único control de la topología por defecto que descansaba en una declaración jurada sin verificación.
- **Grafo de servicios** (`LIN-OBS-001 §5.8` + `LIN-ARQ-001 §5.5`): contraste entre arquitectura declarada y observada.

### 2.4 Gobierno

Ciclo de vida documental con 5 estados y criterios de graduación; regla de exigibilidad; tres instrumentos de decisión distinguidos (`ADR-NNN` institucional, `AD-NNN` de proyecto, `EXC-<SUF>-NNN` de excepción); identificadores estables de regla; y el linter, que pasó de no existir a diez comprobaciones.

---

## 3. Decisiones cerradas — no volver a discutirlas

En una retomada, el mayor riesgo es re-litigar lo ya decidido. Estas decisiones están tomadas y **documentadas con su motivo**:

| Decisión | Dónde consta |
|---|---|
| La versión de un documento vive en el encabezado, **no en el nombre del archivo** | `GOB-MAT-001` · H8.1 |
| Los archivos `_OLD` **se conservan**, no se eliminan; quedan fuera de toda validación y cita | `GOB-MAT-001` · H8.3 |
| Circuit Breaker con Resilience4j **no** es el estándar por defecto en Monolito Modular | `DIS-R-009` · H2 |
| Un hallazgo de **corpus** no bloquea la graduación de un documento; uno de **documento** sí | `GOB-MAT-001` · H22.5 |
| La continuidad operativa vive en `LIN-ARQ-001 §5.4`, **sin crear `LIN-DRP-001`** | H33 |
| El corpus se etiqueta con semver (`v0.9.0`), no con calendario | `GOB-MAT-001` · H43.2 |
| `§3` de `GOB-PLA-001` es **derivado**; el Anexo A es la fuente autoritativa | `GOB-PLA-001` · H34.4 |
| El grafo de servicios **no es un catálogo**: es la contraparte observada de los cuatro existentes | `LIN-OBS-001 §5.8.5` · H35.7 |

---

## 4. Pendientes

Cinco frentes. Dos requieren aprobación externa; tres son trabajo que puede retomarse de inmediato.

### 4.1 🔴 Graduar Nivel 1 y 2 — Comité de Arquitectura *(H22.4)*

**Es el pendiente de mayor impacto.** Sin graduación, el corpus no es exigible contractualmente.

- **Qué aprobar:** `LIN-ARQ-001`, `LIN-DIS-001`, `LIN-PAT-001`.
- **Quién:** Comité de Arquitectura (no Arquitectura OTI en solitario — `GOB-MAT-001`, Aprobación).
- **Material ya preparado:** los 5 criterios de graduación en `GOB-MAT-001`; el checklist verificable del Anexo E de `GOB-PLA-001`; la trazabilidad completa en `GOB-CHK-001`.
- **Efecto:** desbloquea el `v1.0.0` del corpus y hace invocables las reglas en TDR.

### 4.2 🔴 Ratificar los valores de RTO/RPO *(H33.10)*

Los valores de `ARQ-R-006` (LIN-ARQ-001 §5.4.1) están marcados **como propuesta técnica**, no como norma.

- **Por qué no puede decidirlo Arquitectura:** RTO y RPO expresan cuánto puede estar caído un servicio y cuánta información puede perderse. Es decisión de negocio.
- **Quién:** Comité con **Pensiones, Aportes y Atención al Ciudadano**, más validación de Plataforma sobre viabilidad.
- **Advertencia registrada:** los mínimos de respaldo de `LIN-BD-ORA-001 §11.2` **no satisfacen un RPO de 15 minutos** —el de criticidad Alta—. Una base que soporte cálculo o pago de pensiones necesita archive logs más frecuentes o replicación.

### 4.3 🟠 Verificar los 37 temas `En borrador` de la Matriz *(H44.2)* — **retomable ya**

No falta contenido: falta **verificar que el dueño y sus consumidores digan lo mismo**. La regla editorial de la matriz obliga a usar `En borrador` ante duda razonable en vez de declarar `Conforme` por conveniencia.

| Dueño | Temas | Por qué importa |
|---|---|---|
| `LIN-SEC-APP-001` | 8 | Es el documento con más consumidores; su graduación depende de esto |
| `LIN-VER-001` | 6 | Todos los temas de branching y MR |
| `LIN-PERF-001` | 5 | Umbrales y escenarios |
| `LIN-BI-001` | 5 | Todo el bloque de Lakehouse |
| `LIN-K8S-001` | 5 | Secrets, probes, namespaces |
| Otros | 8 | `LIN-CICD-001`, `LIN-IAC-001`, `LIN-PAT-001`, `LIN-API-REST-001`, `LIN-ARQ-001` |

**Cómo se cierra un tema:** leer el documento dueño y cada consumidor listado, confirmar que la regla es la misma en ambos, corregir si no lo es, y cambiar el estado a `Conforme` con la evidencia en la celda. Es el **criterio 3 de graduación**.

**Defecto detectado al auditar:** el tema *«Seguridad frontend (XSS, token storage, guards, CSP)»* está **duplicado** en `GOB-MAT-001` (líneas 371 y 463) con redacción distinta en la columna de evidencia. Consolidarlo es lo primero de este frente.

### 4.4 🟠 Dos mecanismos sin definir *(H44.3)* — **retomable ya**

| Deuda | Estado | Qué falta |
|---|---|---|
| **Respaldo de `PersistentVolume`** | `ARQ-R-006` (LIN-ARQ-001 §5.4.2) lo marca «Pendiente de normar»; `LIN-K8S-001 §13.3` exige declarar «política de backup» sin definirla | Frecuencia, herramienta y retención. Hoy se resuelve por banda de criticidad, que es un criterio pero no un mecanismo |
| **Namespaces de infraestructura compartida** | `LIN-K8S-001 §4.4` pide a **Plataforma** confirmar si `otel-*` y `kafka-*` viven en clúster compartido | Respuesta de Plataforma. Sin ella, dos documentos conservan un sufijo de ambiente que la propia norma considera anti-patrón |

### 4.5 🟠 Aprobar los dos ADR en estado `Propuesta` *(H44.4)*

| ADR | Por qué urge |
|---|---|
| **`ADR-TLS-INTERNO-001`** | **Sostiene la excepción de tráfico intra-cluster sobre HTTP.** Mientras Seguridad no lo apruebe, esa excepción se aplica sin respaldo formal |
| `ADR-WSO2-001` | Gobierna la transición SAA → WSO2. Lleva checklist de graduación con 13 puntos en 8 documentos (H41.4) |

---

## 5. Orden recomendado para continuar

1. **Consolidar el tema duplicado** de `GOB-MAT-001` (§4.3) — cinco minutos, y evita arrastrarlo.
2. **Verificar los 37 temas `En borrador`**, empezando por `LIN-SEC-APP-001` y `LIN-VER-001`: son los que más consumidores tienen y los que bloquean más graduaciones.
3. **Definir el mecanismo de respaldo de PVC** (§4.4), que es el único vacío de contenido que queda.
4. **Llevar al Comité** el paquete completo: graduación de Nivel 1 y 2, ratificación de RTO/RPO y aprobación de los dos ADR. Conviene llevarlo junto, no por partes.
5. **Etiquetar `v1.0.0`** cuando la graduación ocurra (`MAJOR` incrementa al graduar — `GOB-MAT-001`).

---

## 6. Riesgos conocidos

| Riesgo | Mitigación existente |
|---|---|
| **El corpus envejece sin que nadie lo note.** Un documento de arquitectura declara conformidad con reglas que ya cambiaron | `GOB-PLA-001 §1.5`: línea base declarada y disparadores de revisión. **Ocurrió dentro del propio marco rector** (H44.1) — la mitigación existe, la disciplina de aplicarla no está probada |
| **Las verificaciones semestrales de `LIN-ARQ-001 §5.5` no tienen responsable operativo** | Están normadas; falta asignar quién las ejecuta y con qué evidencia |
| **El grafo de servicios y ArchUnit están normados pero no implementados** en ningún sistema real | La norma existe; la primera implementación validará si las reglas son aplicables |
| **Nadie ha probado el corpus con una fábrica real** | El Anexo E de `GOB-PLA-001` es el primer instrumento pensado para el revisor; su utilidad se comprobará en la primera revisión real |

---

## 7. Convenciones a respetar al continuar

- **Nunca eliminar archivos** sin autorización explícita. Los `_OLD` se conservan.
- **Un cambio en un documento dueño obliga a propagarlo** a sus consumidores en el mismo cambio (`GOB-MAT-001`, regla de mantenimiento 8).
- **Toda corrección se registra en `GOB-CHK-001`** con su motivo, no solo con lo corregido.
- **El linter debe quedar en verde** antes de dar por cerrado cualquier trabajo.
- **Las pruebas del linter restauran desde copia propia, nunca con `git checkout`** — Git restaura al último commit, no al estado previo a la prueba (lección de H39.7).

---

*GOB-EST-001 — Estado y Continuidad del Corpus de Arquitectura ONP*
*OTI — Oficina de Tecnologías de la Información · corte al 2026-08-21*
