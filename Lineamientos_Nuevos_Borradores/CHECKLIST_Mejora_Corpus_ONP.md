# Checklist de Mejora del Corpus Documental — ONP

**Código:** GOB-CHK-001
**Versión:** 0.1.0
**Fecha:** 2026-08-05
**Autor:** Arquitectura OTI (revisión asistida)
**Estado:** En ejecución
**Propósito:** Registro de hallazgos de la revisión integral del corpus de lineamientos (2026-08-05) y plan de atención punto por punto. Cada ítem se marca al ser corregido, registrando fecha y evidencia de cierre.

**Alcance de la revisión:** 22 documentos normativos (~32.000 líneas), 2 templates GitLab, plantillas Java, ADRs y documentos de gobierno (Matriz, Glosario, Brecha, START_HERE).

---

## Convención de uso

- `[ ]` pendiente · `[x]` corregido y verificado
- Cada cierre debe anotar: **fecha**, **documento(s) modificado(s)** y **versión resultante** del documento tocado.
- Al cerrar un ítem que modifica un lineamiento, actualizar también su historial de versiones y, si aplica, la Matriz `GOB-MAT-001`.

---

## PRIORIDAD 0 — Rompen la confiabilidad normativa (corregir primero)

### H1 — Dos numeraciones de "Estadios" conviven y se contradicen

- [x] **H1.1** Decidir la escala única de estadios. **Decisión (2026-08-05, Arquitectura):** la de `LIN-ARQ-001 §2.1` — Estadio 1 = Monolito Tradicional/Legacy, Estadio 2 = Monolito Modular, Estadio 3 = Microservicios Selectivos.
- [x] **H1.2** Corregir `GLOSARIO_ONP.md` (término "Estadios de Topología"): decía "Estadio 0 (Legacy), Estadio 1 (Transición), Estadio 2 (Cloud-Native)" y citaba una sección inexistente (`LIN-ARQ-001 §6`). Corregido a la escala oficial con cita `§2.1`; se aclaró que Strangler Fig es estrategia de transición, no un estadio. → Glosario v0.2.2.
- [x] **H1.3** Corregir `LIN-PAT-001` — 4 fichas usaban "Estadio 0" para legacy: `PAT-TOP-01`, `PAT-TOP-03`, `PAT-INT-04`, `PAT-DAT-03` (la mención a PAT-DAT-01 del borrador inicial fue descartada en verificación: esa ficha no usaba la escala vieja). Corregidas a "Estadio 1". → LIN-PAT-001 v0.1.5.
- [x] **H1.3b** *(hallazgo adicional del barrido)* `Plantilla_Documento_Arquitectura` usaba la escala vieja en 3 guías (vistas A.1, A.2, A.3 — líneas ~187, ~330, ~385), incluyendo la lista completa "Estadio 0 Legacy / 1 Transición / 2 Cloud-Native" con cita errónea `LIN-ARQ-001 §6`. Corregido a la escala oficial con cita `§2.1`; de paso se corrigieron las citas de ACL (`LIN-DIS-001 §6` → `§5.4`) y Strangler Fig (→ `LIN-ARQ-001 §2.2`) en la línea editada (adelanto parcial de H6). → Plantilla v2.1.
- [x] **H1.4** Barrido de verificación ejecutado (2026-08-05): cero ocurrencias de "Estadio 0" y cero usos de "Transición"/"Cloud-Native" como estadio en el corpus vigente (la única mención restante es la nota de historial de LIN-PAT-001 que documenta la corrección; `LIN-ARQ-000` congelado y `_OLD` quedan fuera de alcance por política).

**Evidencia:** `LIN-ARQ-001 §2.1` vs `GLOSARIO_ONP.md:69` vs `LIN-PAT-001` líneas 67, 97, 221, 361, 390.
**Riesgo si no se corrige:** ambigüedad contractual en TDRs con fábricas de software.

---

### H2 — Contradicción normativa Nivel 1 vs Nivel 2: Circuit Breaker en integraciones con el Estado

- [x] **H2.1** `LIN-ARQ-001 §4.3` numeral 3 reescrito: exige el **resultado** (aislamiento ante caídas mediante timeout estricto + Bulkhead + degraded mode) y delega mecanismo y umbrales en `LIN-DIS-001 §6`. Se añadió una nota explícita de que el Circuit Breaker con Resilience4j **no es exigible por ese numeral**, con el caso concreto resuelto: "un Monolito Modular que consuma RENIEC cumple este mandato sin Resilience4j". → LIN-ARQ-001 v0.1.8.
- [x] **H2.2** `LIN-DIS-001 §6` verificado: no requería cambio normativo, pero se le añadió una **declaración de propiedad documental** explícita (qué delega el Nivel 1, qué conserva `LIN-API-REST-001`, y la prohibición de que otros documentos publiquen umbrales propios). → LIN-DIS-001 v0.1.5.
- [x] **H2.3** Registrado en la Matriz como fila nueva "Resiliencia táctica en llamadas externas — dueño `LIN-DIS-001`", estado **Resuelto**, con la traza del conflicto corregido.

**Evidencia:** `LIN-ARQ-001 §4.3.3` ("es mandatorio que el cliente HTTP/SOAP tenga configurados Timeouts estrictos, Circuit Breakers (Resilience4j)") vs `LIN-DIS-001 §6.2` ("Resilience4j no es el estándar por defecto en un Monolito Modular… únicamente con ADR", con RENIEC como ejemplo de la excepción).
**Riesgo:** la Regla de Supremacía (Nivel 1 gana) forzaría Resilience4j en todo monolito que consuma RENIEC, exactamente lo que el Nivel 2 quiso evitar.

---

### H3 — Tres valores distintos de timeout para el mismo caso

- [x] **H3.1** `LIN-DIS-001 §6.1` confirmado como dueño único de la matriz de timeouts, con declaración de propiedad añadida en el encabezado de `§6`. Barrido de verificación: **cero** valores de timeout publicados fuera del dueño en el corpus vigente.
- [x] **H3.2** `LIN-API-REST-001 §8.3` corregido: eliminada la tabla propia (Connection 5s / Read 10s) y sustituida por referencia al dueño. Conserva lo que sí le corresponde como estándar REST — la respuesta `504` / `codDetRespuesta 402` ante vencimiento. → LIN-API-REST-001 v0.1.5.
- [x] **H3.3** Resuelto junto con H2.1: eliminado el rango propio "máx. 3 a 5 segundos" de `LIN-ARQ-001 §4.3`, sustituido por la prohibición de valores por defecto y la delegación en la matriz por criticidad.
- [x] **H3.4** Registrado en la Matriz (misma fila que H2.3, que cubre timeouts, Bulkhead, Retry y Circuit Breaker como un solo tema con dueño único).

**Evidencia:** `LIN-ARQ-001 §4.3` (3–5s) vs `LIN-DIS-001 §6.1` (matriz por categoría) vs `LIN-API-REST-001 §8.3` (5s/10s).

---

### H4 — El template oficial contradice el contrato normativo `ApiResponseWrapper` + copias divergentes

- [x] **H4.1** Corregido el wrapper del template simple: `FieldError` → `CampoError` (6 ocurrencias). **Precisión sobre el hallazgo original:** la divergencia era del **nombre de la clase Java**, no del JSON — los campos internos ya eran `campo`/`mensaje`, así que el contrato serializado sí se cumplía. Se corrige igual porque `LIN-DEV-JAVA-001 §13.4.4` nombra la clase explícitamente y porque `FieldError` colisiona visualmente con `org.springframework.validation.FieldError` en el `GlobalExceptionHandler`.
- [x] **H4.2** Corregida la copia del template modular (`comun/onp-common-web/`, 6 ocurrencias) y su consumidor `onp-template-boot/.../GlobalExceptionHandler.java` (3 ocurrencias). Barrido posterior: cero referencias colgantes a `FieldError`; el único `getFieldErrors()` restante es la API de Spring, correcta.
- [x] **H4.3** Fuente canónica declarada: `desarrollo/plantillas/`. Documentada en ambos READMEs con una sección nueva **"Artefactos normados — no personalizar"** que mapea archivo → fuente canónica → documento que lo norma, y advierte que apartarse requiere ADR.
- [x] **H4.4** `checkstyle-onp.xml` unificado en las 3 ubicaciones (hashes idénticos). **La divergencia no era cosmética:** los templates enviaban una versión reducida con solo 6 reglas de nomenclatura, a la que le faltaban **17 reglas**, incluidas **las tres métricas obligatorias de `LIN-DEV-JAVA-001 §12.1`** (complejidad ≤10, método ≤30 líneas, clase ≤500) más `UnusedImports`, `EqualsHashCode`, `EmptyCatchBlock`, `NeedBraces`, `MissingSwitchDefault` y `StringLiteralEquality`. En la práctica, el gate de calidad de los proyectos derivados estaba desactivado.
- [x] **H4.5** Verificado con build real (Maven 3.9 / JDK 21):<br>• `template-backend-java-modular`: `mvn compile` **SUCCESS** (9 módulos) · `mvn checkstyle:check` **SUCCESS** · `mvn test` **11 tests OK** (`AfiliadoTest`, `RegistrarAfiliacionServiceImplTest`, `AfiliacionControllerTest`, `AporteRegistradoConsumerTest`).<br>• `template-backend-java`: `mvn compile` **SUCCESS** · `mvn checkstyle:check` **SUCCESS**.<br>• 2 tests no ejecutables en esta máquina por ausencia de Docker (`AfiliadoJpaAdapterTest` y `TemplateBackendModularApplicationTests` usan Testcontainers) — fallo de entorno, no del cambio; ambos deben validarse en el runner de CI.
- [x] **H4.6** Cerrado en H5.8 — regla 7 de mantenimiento incorporada a `GOB-MAT-001`.
- [x] **H4.7** *(hallazgo crítico durante H4.5)* **El `checkstyle-onp.xml` canónico estaba roto y nunca se había ejecutado.** Declaraba `LineLength` dentro de `TreeWalker`, pero desde Checkstyle 8.24 esa regla es hija de `Checker`: cualquier proyecto que lo usara abortaba con `TreeWalker is not allowed as a parent of LineLength`. Corregido moviendo la regla al nivel `Checker` junto a `FileLength`, con comentario explicativo para que no se revierta. Esto explica por qué los templates enviaban la versión reducida — probablemente se recortó para que el build pasara, en vez de corregir la causa.
- [x] **H4.8** *(consecuencia de H4.4/H4.7)* Al activarse por primera vez las reglas completas afloraron **4 violaciones reales en el código de los templates**, invisibles hasta ahora: líneas >120 caracteres en `MontoMonetario.java`, `Dni.java`, `Afiliado.java` y en ambos `ApiResponseWrapper.java`. Todas corregidas partiendo la línea; ambos templates quedan en verde.

**Evidencia:** diff entre las 3 copias (hashes `66ac…`, `512b…`, `325f…`); `START_HERE §5` declara `template-backend-java` como baseline obligatorio → todo proyecto nuevo nace violando el contrato.

---

### H5 — La Matriz de Propiedad (GOB-MAT-001) está desactualizada

- [x] **H5.1** Catálogo: `LIN-BUS-001` pasó de "Pendiente — sin archivo" a **Borrador v0.1.5** con su archivo `mensajeria/...`. Se actualizaron además las versiones de `LIN-PAT-001` (v0.1.5) y `GLOSARIO-ONP` (v0.2.2), y se incorporó `GOB-CHK-001` al catálogo.
- [x] **H5.2** Sección "Catálogo de Patrones — pendiente" **cerrada** y reemplazada por el índice de trazabilidad `PT → ficha PAT → dueño`, con declaración explícita de `LIN-PAT-001` como **fuente única de códigos PT** (cierra también la causa de H10.3). La tabla ya no cita secciones — cita fichas, que son estables ante renumeraciones. Documentadas las 9 fichas sin código PT y el estado real de PT03 (Event Sourcing, no adoptado) y PT06 (Retry, normado sin ficha).
- [x] **H5.3** Corregidas las citas obsoletas a `LIN-DEV-JAVA-001`. **El alcance real fue mayor al previsto: 11 citas rotas, no 3.** Además de `11.4.x → 13.4.x` (4 casos), se encontraron y corrigieron: `§8 → §10` (logging/ECS/No PII, 2 casos + 1 en el plan de correcciones), `§9 → §11.1` (`GlobalExceptionHandler`), `§10.3 → §12.3` (PMD), `§8 → §13.5.3` (adapter PL/SQL, 2 casos). Se corrigió también `LIN-TEST-001 §4.4 → §3.3/§12.4` (herramientas E2E; §4.4 es "Microservicio") y se precisó `LIN-VER-001 §14 → §14 y §15`.
- [x] **H5.4** Corregida la cita "sección 14: PR obligatorio, máx 400 líneas" → `§16`. Corregidas también las filas 13 y 14 del plan de correcciones, que citaban las secciones 14 y 15 con la numeración vieja.
- [x] **H5.5** Versión de `LIN-VER-001` en el catálogo corregida de v0.1.5 → **v0.1.6**.
- [x] **H5.6** Versión del pie unificada con el encabezado. Se añadió además un **historial de versiones** — la Matriz no tenía, pese a exigirlo a todos los demás documentos.
- [x] **H5.7** Registrados en la Matriz: (a) fila nueva "Escala de Estadios de Topología" documentando el cierre de H1; (b) fila nueva "Códigos PT — fuente única" con la discrepancia abierta del tablero de Brechas; (c) nota de pendiente en artefactos sobre `ApiResponseWrapper` (H4); (d) "Última validación integral" actualizada a 2026-08-05.
- [x] **H5.8** *(añadido durante la ejecución)* Reglas de mantenimiento 7 y 8 incorporadas a la Matriz: verificar el cierre también en artefactos ejecutables (lección de H4) y propagar toda renumeración a las citas en el mismo cambio (causa raíz de H5.3).
- [x] **H5.9** *(añadido durante la ejecución)* Resuelto un TODO que la propia Matriz tenía abierto ("validar en siguiente pasada si `LIN-DEV-JAVA-001` ya referencia la sección 9.1 de Spring Security"): **no la referencia** — queda anotado como brecha de referencia a cerrar en la próxima revisión de `LIN-DEV-JAVA-001`.

---

> ✅ **Prioridad 0 completa** (H1 · H2 · H3 · H4 · H5) — cerrada el 2026-08-05. Arrastró además el cierre de H10.3 y H8.6, y generó los hallazgos nuevos H4.7, H4.8, H6.4, H10.6 y H10.7.

---

## H13 — Saneamiento de `LIN-DEV-JAVA-001` (revisión focalizada 2026-08-05)

> Contexto: revisión completa del estándar Java (3.714 líneas), el documento más citado del corpus y el que más renumeraciones internas ha sufrido. El contenido técnico es de alta calidad; el deterioro está en las capas de parches sucesivos. Dos ítems son de gravedad P0.

- [x] **H13.1** 🔴 **La contradicción de cobertura que la v0.1.8 declaró corregida sigue viva.** El changelog dice que se eliminaron de `§12.1` las filas de cobertura (servicios ≥80%, utilidades ≥90%, controladores REST ≥70%) porque la de controladores *"contradecía directamente `LIN-TEST-001 §5.1`, que exige explícitamente NO medir Controllers con umbral duro"*. `§12.1` quedó limpia, **pero la misma tabla sigue intacta en `§15.3`** — y **`§16.2`, el gate de aprobación de Pull Requests, apunta a `§15.3`**, no a `§12.1`. La corrección se aplicó en la sección que nadie consulta y quedó viva en la que gobierna el merge. **Cerrado 2026-08-05:** `§15.3` reemplazada por remisión a `LIN-TEST-001 §5.1`, conservando una tabla orientativa **sin porcentajes** (qué tipo de prueba aplica por elemento) y una advertencia explícita de que los Controllers no se miden con umbral duro. `§16.2` actualizada para que el gate de PR cite al documento dueño.
- [x] **H13.2** 🔴 **El Anexo B enseña la configuración de Checkstyle rota.** Reproduce el XML completo con `LineLength` dentro de `TreeWalker` — el mismo bug corregido en H4.7, que aborta el build con `TreeWalker is not allowed as a parent of LineLength` (Checkstyle ≥8.24). Un equipo que copie del Anexo en vez de usar los templates reproduce el fallo. **Cerrado 2026-08-05:** el bloque XML del Anexo B fue reemplazado por el contenido del archivo canónico (verificado idéntico byte a byte y validado con `xmllint`), se declaró la fuente canónica con enlace relativo y se añadió una advertencia sobre por qué `LineLength`/`FileLength` no pueden ir dentro de `TreeWalker`.
- [x] **H13.3** 🟠 **Inventa una categoría de Feature Toggle que el Nivel 1 no reconoce.** `LIN-ARQ-001 §2.3` (dueño, ADR-014) define **tres** categorías — Release, Ops, Permission. `LIN-DEV-JAVA-001 §16.6` define **cuatro**, añadiendo **Experiment Toggle** con plazo de caducidad propio. Un Nivel 3 no puede crear taxonomía normativa que el Nivel 1 no tiene. Además cita `LIN-ARQ-001 §2.2.1.A`, sección **inexistente** (los toggles están en `§2.3`). **Cerrado 2026-08-05. Decisión (Arquitectura): incorporar `Experiment Toggle` al Nivel 1.** El análisis mostró que no era invención del Nivel 3: la descripción de `Permission Toggle` en `LIN-ARQ-001 §2.3` ("habilitar características experimentales solo para un grupo beta") mezclaba **dos ciclos de vida opuestos** — el experimento caduca obligatoriamente con su veredicto, el permiso puede ser permanente. Esa mezcla es la vía por la que un flag temporal termina clasificado como permanente y se vuelve deuda indefinida. Aplicado: `§2.3` pasa a cuatro categorías con `Experiment Toggle` (caducidad obligatoria) y `Permission Toggle` acotado a control de acceso por rol/perfil; nota de distinción añadida; ampliación registrada en `ADR-014`; `LIN-DEV-JAVA-001 §16.6` alineado; citas a la sección inexistente `§2.2.1.A` redirigidas a `§2.3`.
- [x] **H13.4** 🟠 **Numeración huérfana dentro de `§16`.** La sección `### 16.6` contiene subsecciones `#### 14.6.1`, `#### 14.6.2` y `#### 14.6.3` — residuo de cuando el bloque era `§14.6` (changelog v0.1.2). Dentro de ese bloque hay además una referencia cruzada a *"la revisión de código (Sección 14.2)"*, que hoy es `§16.2`. **Cerrado 2026-08-05:** subsecciones renumeradas a `16.6.1`–`16.6.3` y referencia interna convertida en enlace a `§16.2`.
- [x] **H13.5** 🟡 **Diez enlaces a rutas absolutas de otra máquina** (`file:///home/carlos/Documentos/...`) usados como enlaces de descarga de plantillas (§13.4.2 y otras). No funcionan para ningún otro lector. Mismo problema que H10.5 en `LIN-ARQ-000`, pero mucho más frecuente aquí. **Cerrado 2026-08-05:** los 10 enlaces convertidos a `./plantillas/...`; verificado que los 10 destinos existen en el repositorio y que todos los enlaces relativos del documento resuelven.
- [x] **H13.6** 🟡 **Referencia fantasma al documento congelado.** `§13.3` dice *"Ver sección 3.4 del Lineamiento de Arquitectura (doc. interno) para la discusión ACID/CAP"* — no nombra el código del documento y apunta a la estructura de `LIN-ARQ-000` (congelado). El contenido CAP vigente está en `LIN-ARQ-001 §3`. **Cerrado 2026-08-05:** redirigida a `LIN-ARQ-001 §3` (Gobierno de Datos y Teorema CAP), añadiendo la remisión a Saga + Outbox (`§3.3`, `LIN-BUS-001 §7.3`).
- [x] **H13.7** Ver también, sobre este mismo documento: **H10.6** (clasifica a `LIN-ARQ-001` como "Nivel 2" cuando es Nivel 1, dos veces, y lo nombra con el título del documento congelado) y **H10.7** (no referencia `LIN-SEC-APP-001 §9.1` pese a ser declarado consumidor de ese tema en la Matriz).

**Observación estructural:** `LIN-DEV-JAVA-001` es el argumento más fuerte a favor de **H12.1** (IDs estables). Es el documento más citado del corpus y el que más ha renumerado: cada renumeración suya rompe media docena de consumidores — las 11 citas rotas corregidas en H5.3 vinieron todas de aquí.

---

> ✅ **H8 cerrado** (salvo H8.5, que requiere commit). Los avisos del linter bajaron de **25 a 5**; los 5 restantes son H12.7.

---

## H14 — Cobertura desigual de la revisión (detectado 2026-08-08)

> **Observación de Arquitectura:** la revisión se concentró en el núcleo arquitectónico y dejó de lado documentos de peso comparable. Es una crítica correcta y este bloque la corrige.

**Profundidad real alcanzada por documento:**

| Documento | Líneas | Nivel de revisión |
|---|---:|---|
| `LIN-ARQ-001`, `LIN-DIS-001`, `LIN-PAT-001`, `LIN-DEV-JAVA-001`, `GOB-MAT-001`, `GLOSARIO-ONP` | ~9.000 | **Contenido a fondo** (H1–H13) |
| `LIN-BD-ORA-001` | 2.007 | **Contenido a fondo** — primera pasada H14.1 |
| `LIN-TEST-001`, `LIN-VER-001` | 3.014 | Verificación dirigida (tablas de referencia, contradicciones cruzadas) — **falta lectura de contenido** |
| `LIN-CICD-001`, `LIN-K8S-001`, `LIN-OBS-001`, `LIN-FE-ANG-001`, `LIN-SEC-APP-001`, `LIN-BUS-001`, `LIN-PERF-001`, `LIN-IAC-001`, `LIN-BI-001` | ~9.000 | **Solo citas y versiones** — sin revisión de contenido |

- [x] **H14.1** 🔴 **`LIN-BD-ORA-001` incumple sus propias convenciones en 3 de sus 7 ejemplos DDL.** Las tres tablas `CAT_*` (`§2.3`, `§4.x`) las cumplen íntegramente —prefijos de columna y los 6 campos de auditoría—, pero: **(a)** `EVT_OUTBOX` (`§3.10`) no tiene **ningún** campo de auditoría pese a que `§5.1` los declara *"obligatorios en todas las tablas permanentes no temporales"* y `§5.2` solo exime GTT y staging; sus 8 columnas carecen del prefijo de tipo que exige `§3.4`. **(b)** `CATALOGO_PLSQL_LEGACY` (`§6.0`) acumula cuatro incumplimientos: el nombre debería llevar prefijo `CAT_` (`§3.3`), sus 10 columnas no llevan prefijo (`§3.4`), no tiene campos de auditoría (`§5.1`), y usa `GENERATED BY DEFAULT AS IDENTITY` — sintaxis de Oracle **12c+ que no existe en 11g**, cuando `§2.2` promete que *"las convenciones son compatibles con 11g y 19c; cuando se indica una característica exclusiva de 19c, se señala explícitamente"*. **(c)** `MOV_APORTE` (`§9.2`) usa `N_ID NUMBER` como clave primaria en vez de `ID_<ENTIDAD> NUMBER(19)` (`§3.4`, `§3.5`). Son los ejemplos que las fábricas copian: un contratista que siga el DDL de `§3.10` produce una tabla que el propio lineamiento rechazaría en revisión.
- [ ] **H14.2** 🟠 `LIN-BD-ORA-001 §1.3` (*Relación con otros documentos*) es la **única tabla de referencias del corpus que cita por nombre sin código**, y su primera fila apunta al documento **congelado** (*"Lineamiento de Diseño y Arquitectura de Software ONP"* = `LIN-ARQ-000`, con los componentes `CD01`/`CD02` que solo existen allí). También cita la *"Guía de Diseño y Programación ONP v2.0"*, documento externo al corpus. Al no usar códigos, **el linter no puede validarla**: C1 solo reconoce `LIN-*`/`GOB-*`. Reescribirla con códigos y añadir `LIN-DIS-001`, `LIN-BUS-001` (dueño del relevo Outbox) y `LIN-TEST-001` (pruebas de caracterización), que hoy faltan pese a ser consumidores declarados en `GOB-MAT-001`.
- [ ] **H14.3** Duplicación del DDL de `EVT_OUTBOX`: `LIN-BD-ORA-001 §3.10` se declara dueño del nombre y el DDL, pero `LIN-BUS-001 §7.3` reproduce el bloque completo. Verificado: hoy son idénticos, pero es la misma clase de duplicación que produjo H3 (timeouts) y H4 (checkstyle). `LIN-BUS-001` debe referenciar en vez de copiar.
- [ ] **H14.4** **Lectura de contenido pendiente** de `LIN-TEST-001` y `LIN-VER-001` (3.014 líneas). La verificación hecha hasta ahora es dirigida: tablas de referencia y contradicciones cruzadas conocidas, no revisión del cuerpo normativo.
- [ ] **H14.5** **Lectura de contenido pendiente** de los 8 documentos restantes (`LIN-SEC-APP-001` cerrado en H15) (~9.000 líneas): `LIN-CICD-001`, `LIN-K8S-001`, `LIN-OBS-001`, `LIN-FE-ANG-001`, `LIN-SEC-APP-001`, `LIN-BUS-001`, `LIN-PERF-001`, `LIN-IAC-001`, `LIN-BI-001`. Priorizar por criticidad: seguridad y observabilidad antes que BI.

---

## H15 — Revisión de contenido de `LIN-SEC-APP-001` (2026-08-08)

Primera revisión a fondo de un documento transversal fuera del núcleo arquitectónico (1.109 líneas). El contenido de gobierno —modelo SAA/WSO2, prohibiciones, controles legacy, OWASP— es sólido; los hallazgos están en los **ejemplos de código**, que son lo que las fábricas copian.

- [x] **H15.1** 🔴 **`§9.3` publicaba un contrato de error paralelo al institucional.** El `GlobalExceptionHandler` de referencia devolvía `ErrorResponse("ERR-INTERNAL", …)` en vez de `ApiResponseWrapper`, y sus códigos `ERR-INTERNAL`/`ERR-FORBIDDEN` no existen en el catálogo `codDetRespuesta` (000–502), cuyo dueño `LIN-API-REST-001 §4.2.1.d` **prohíbe expresamente crear códigos locales por sistema**. Un equipo que copiara este handler rompía el contrato de respuesta en el punto donde más importa: la ruta de error. Reescrito con `ApiResponseWrapper` y los códigos `500` y `301`, remitiendo a `LIN-DEV-JAVA-001 §11.1` como implementación completa.
- [x] **H15.2** 🔴 **`§8.4` — el cliente del SAA no tenía timeouts.** Usaba `RestTemplate` sin configuración alguna, es decir con timeouts por defecto (infinitos), **en el componente que se invoca en cada petición de cada servicio** y contra el sistema que el propio `§8.7` declara *"dependencia crítica"*. `LIN-DIS-001 §6.1` lo prohíbe de forma terminante. Con el SAA lento o caído, cada hilo del contenedor quedaba bloqueado indefinidamente: es el escenario exacto de agotamiento de hilos que `§8.7` dice querer evitar. Reescrito con `RestClient` sobre Apache HttpClient 5, con los umbrales de la categoría «ruta crítica interactiva» (connect 1.5s / read 3s) y `setMaxConnPerRoute` como Bulkhead.
- [x] **H15.3** 🟠 **`§8.3` — las respuestas del filtro quedan fuera del contrato.** `response.sendError()` delega en el manejador por defecto del contenedor, que no produce `ApiResponseWrapper`: los 401 y 503 emitidos por el filtro de seguridad no cumplen `LIN-API-REST-001 §4`. Como el filtro corre antes del `@RestControllerAdvice`, se documentó la forma conforme (serializar el wrapper sobre `response.getWriter()` o delegar en un `AuthenticationEntryPoint`) y los códigos aplicables (`401`/`300`, `503`/`400`), advirtiendo que el ejemplo usa `sendError` por brevedad y no es conforme.
- [x] **H15.4** 🟠 **`§8.5` contradecía dos secciones del propio documento.** El ejemplo de `application-dev.yml` publicaba `http://onpwasihsd01.onp.gob.pe:80/...`: **(a)** HTTP sin cifrar contra un **servidor DEV compartido**, cuando `§7.1` admite HTTP *"solo para desarrollo local en máquina del desarrollador (localhost); nunca en ambientes compartidos"*; **(b)** la URL en claro en el repositorio, cuando `§12.1` clasifica *"URL de validación de token SAA (contiene host interno)"* como **secreto** y `§12.2` prohíbe secretos en `application-*.yml`. Sustituida por variable de entorno en todos los ambientes.
- [x] **H15.5** 🟡 Cita de PMD `LIN-DEV-JAVA-001 10.3` → `§12.3`. `§10.3` existe pero es *Formato de mensajes de log*: otra cita que resuelve y apunta al tema equivocado.

---

## H16 — Mejora del linter: citas sin marcador de sección (2026-08-08)

- [x] **H16.1** **El linter ignoraba 17 citas** escritas en la forma abreviada `LIN-XXX N.N` —sin `§` ni «sección»— usada sobre todo por `LIN-SEC-APP-001` (`LIN-OBS-001 6`, `LIN-CICD-001 13.4`, `LIN-DEV-JAVA-001 10.3`…). C1 exigía el marcador, así que esas referencias nunca se validaban. Extendida la expresión para aceptar la forma abreviada. **El primer intento rompió el linter**: al hacer opcional el marcador, el motor backtrackeaba el `\d+` final del código y partía `LIN-CICD-001` en código `LIN-CICD-00` + sección `1`, generando 975 falsos positivos. Corregido exigiendo un separador explícito entre código y número. De las 17 citas recuperadas, 16 resolvían bien y 1 estaba mal dirigida (H15.5).

---

## H17 — Ciclo de vida documental: la brecha de gobierno (2026-08-08)

**Diagnóstico:** el corpus se declaraba de aplicación **obligatoria** para fábricas y terceros (`LIN-ARQ-001 §1.1` y `§8`, anexo normativo para TDR) mientras **14 de 22 documentos figuraban como Borrador** — y no existía en ningún lugar un proceso de graduación. Se buscó en los tres sitios donde debería estar: `LIN-VER-001` versiona código, no normativa; la regla de mantenimiento de `GOB-MAT-001` dice cómo mantener la matriz, no cuándo un lineamiento entra en vigor; y `LIN-DOC-001`, su dueño natural, sigue *Pendiente sin archivo*. El Comité de Arquitectura solo aparecía para aprobar ADR, nunca lineamientos. **Consecuencia práctica:** una fábrica podía rechazar legítimamente una observación alegando que el documento invocado es un borrador, y todo el saneamiento H1–H16 no cambiaba el estatus normativo de nada.

- [x] **H17.1** **Ciclo de vida definido** en `GOB-MAT-001` (decisión: vive ahí y no en `LIN-DOC-001`, para no tener que graduar un documento nuevo antes de poder graduar nada). Cuatro estados de flujo —`Borrador → En revisión → Vigente → Deprecado`— más `Congelado` como terminal. Se descarta `Pendiente` como estado de documento: sin archivo no hay documento.
- [x] **H17.2** **Criterio de graduación:** cinco requisitos, cuatro automatizables (linter en verde; revisión de contenido registrada en `GOB-CHK-001` con hallazgos cerrados; sin temas propios en `Pendiente`/`Requiere ADR`; artefactos ejecutables verificados; historial al día). Convierte este checklist en **criterio de graduación** en vez de una lista sin destino.
- [x] **H17.3** **Aprobación proporcional al equipo:** Comité de Arquitectura para Nivel 1–2 y el catálogo; Arquitectura OTI para Nivel 3, transversales y gobierno. Un proceso con comité para todo no se ejecutaría —el tablero de Brechas reconoce que hoy el arquitecto ejerce también de diseñador— y en un año habría otra vez 14 borradores.
- [x] **H17.4** **Regla de exigibilidad** (coherencia de dependencias): un documento `Vigente` puede referenciar uno que no lo esté, pero **no puede hacerlo exigible**; si un criterio de aceptación, gate o requisito de TDR depende de otro documento, ese debe estar `Vigente` o el requisito se degrada a *recomendado*. Convierte las dependencias en un **orden objetivo de graduación**.
- [x] **H17.5** **Reclasificación de estados (decisión de Arquitectura).** `LIN-ARQ-001`, `LIN-DIS-001` y `LIN-PAT-001` figuraban `Vigente` **autodeclarados antes de que existiera la barra**; pasan a `En revisión` junto con los dos documentos cuya revisión de contenido está cerrada (`LIN-DEV-JAVA-001` H13, `LIN-SEC-APP-001` H15). El resto queda `Borrador`. **Hoy ningún documento está `Vigente`** — se anotó como nota de transición explícita, incluida su consecuencia: mientras dure, ningún lineamiento es exigible contractualmente y los TDR en curso deben tratarlos como referencia técnica.
- [x] **H17.6** **Ruta de graduación derivada de la regla:** `LIN-OBS-001` y `LIN-TEST-001` son prioridad 1 porque `LIN-ARQ-001 §8.3` los hace exigibles al contratista y sin ellos el Nivel 1 no puede graduar. Sustituye la priorización por criticidad percibida por una derivada de dependencias reales.
- [x] **H17.7** **Sufijo `_OLD` y estado `Congelado` reconciliados.** Arquitectura marcó el documento congelado con sufijo `_OLD` como ayuda visual para no considerarlo por error. La convención vigente los presentaba como excluyentes (`_OLD` = fuera del corpus vs. `Congelado` = en el catálogo con código). Se documentó que el sufijo es **marca de lectura, no estado**, y que aplica a dos casos: copias superadas sin código, y documentos `Congelados` que sí conservan código y entrada en el catálogo por trazabilidad —como `LIN-ARQ-000`, citado como origen por una docena de historiales—. Corregida la ruta del catálogo, que apuntaba al archivo sin sufijo tras el renombrado.

---

## H18 — Linter: validación del catálogo (C6, 2026-08-08)

- [x] **H18.1** **Nueva comprobación C6.** El catálogo de `GOB-MAT-001` es el índice del corpus —declara qué archivo contiene cada código— y **nadie verificaba que esas rutas existan**. Se detectó al renombrar el documento congelado: la ruta del catálogo quedó apuntando a un archivo inexistente y **ningún check lo vio**, porque C5 valida enlaces Markdown y esa es una ruta escrita en una celda de tabla. C6 verifica que cada ruta declarada exista **y** que el archivo declare el código que el catálogo le atribuye. Probada con dos fallos inyectados —ruta inexistente y código mal atribuido—: detecta ambos.

---

## PRIORIDAD 1 — Estructurales (atacan la causa raíz)

### H6 — Referencias cruzadas rotas sistémicas (LIN-PAT-001, Glosario, Brecha)

- [x] **H6.1** Corregidas **9 citas de `LIN-PAT-001` al Marco Rector**. Eran el caso más engañoso del corpus: `LIN-ARQ-001 §6.1/§6.2/§6.3` **existen** —son Oracle, NoSQL y Medallion— pero las fichas las usaban para Monolito Modular, Microservicios, Strangler Fig, ACL y Saga. El linter no puede detectarlo: valida que la sección exista, no que trate del tema citado. Redirigidas a `§2.1` (estadios), `§2.2` (Strangler), `§3.3` (Saga) y `§4.3` (ACL gubernamental), y se corrigió de paso `ADR-012`→`ADR-003` en la ficha de Microservicios.
- [x] **H6.2** `GLOSARIO-ONP`: corregidas 3 citas mal dirigidas — Monolito modular (`LIN-DIS-001 §3.1`, que es *Mapa de Contextos*) ahora separa topología (`LIN-ARQ-001 §2.1`) de diseño interno (`LIN-DIS-001 §3`); Hexagonal (`§3.2`, que es *Building Blocks*) → `§2.3`; ACL (`§6`, que es *Resiliencia*) → `§5.4`, más la obligatoriedad en `LIN-ARQ-001 §4.3`. Las citas a Four Golden Signals y estadios ya se habían corregido en H12.4 y H1.
- [x] **H6.3** `GOB-BRE-001` (tablero de Brechas) reconciliado — pendiente que su propia v0.1.5 declaró «fuera de alcance». **66 citas al congelado `LIN-ARQ-000` redirigidas** a su documento vigente mediante un mapeo sección por sección (§3.7.x→`LIN-DIS-001 §6.x`, §3.8.x→`§5.x`, §7.1→`LIN-DEV-JAVA-001 §7`, etc.). **El mapeo mecánico produjo 6 citas plausibles pero equivocadas** —ACL, las cuatro filas de Saga y el Teorema CAP quedaron apuntando a la sección heredera del bloque origen, no a la del tema— corregidas a mano tras revisar fila por fila; es la clase de error que el linter no puede detectar. Resuelta además la **discrepancia de códigos PT**: el tablero asignaba `PT09`=BFF, `PT10`=Gateway-Aggregation y `PT12`=Facade contra `LIN-PAT-001`, que asigna `PT11`, `PT12` y `PT15`; se añadió la ficha `PAT-*` de cada uno y una nota declarando a `LIN-PAT-001` fuente única. → GOB-BRE-001 v0.1.6.
- [x] **H6.4** `GOB-PLA-001` (Plantilla): las dos citas al documento congelado corregidas — *«según LIN-ARQ-000/LIN-DIS-001»* → `LIN-ARQ-001 §2.1` y `LIN-DIS-001 §2`; y el mandato de deuda técnica cero → `LIN-ARQ-001 §2.3`.
- [x] **H6.5** Barrido final de citas al congelado: `LIN-TEST-001` lo declaraba como **marco rector vigente** en su tabla de normativa (corregido a `LIN-ARQ-001`, añadiendo `LIN-DIS-001` como dueño de la pirámide por estilo) y `LIN-DIS-001 §4.1` encabezaba una tabla con `§6 LIN-ARQ-000`. Las menciones restantes son legítimas: historiales que documentan la migración, y las definiciones de *Cantera Histórica* en `GOB-INI-001` y `GLOSARIO-ONP`.

### H7 — Referencias internas invertidas en LIN-API-REST-001

- [x] **H7.1** `§2.1` (nota SOAP): apuntaba a `10.3` (gate WSO2) → corregido a `10.4` (Consumo de servicios SOAP legacy).
- [x] **H7.2** Tabla de `§10.2` (ciclo de vida): las filas QA y Producción citaban «Gate sección 10.4» → corregidas a `10.3`. Las dos secciones estaban intercambiadas en ambos sentidos.
- [x] **H7.3** Resuelto en H8.4: el pie de `LIN-API-REST-001` declaraba v0.1.2 con encabezado v0.1.5; se eliminaron las versiones de todos los pies.

### H8 — Versionado de archivos incoherente con el contenido (el corpus viola su propio LIN-VER-001)

- [x] **H8.1** **Decisión (Arquitectura, 2026-08-05): la versión sale del nombre de archivo.** Queda declarada en un único lugar —el encabezado— con el historial de versiones como respaldo y Git como mecanismo de versionado. Regla incorporada al cuerpo de `GOB-MAT-001`.
- [x] **H8.2** **21 archivos renombrados con `git mv`** (historial preservado: Git los registra como renombrados, no como borrado+alta) y **39 referencias actualizadas** en `GOB-MAT-001`, `GOB-CHK-001` y el banner de `LIN-ARQ-000`. Verificado: 14 enlaces relativos comprobados, 0 rotos; el linter pasa C1 y C5 en verde.
- [x] **H8.3** **Corregido el criterio: las copias `_OLD` NO se eliminan.** El planteamiento inicial de este ítem («eliminar la copia muerta; Git conserva el historial») fue **rechazado por Arquitectura**: los archivos `_OLD` se conservan en el repositorio como respaldo de consulta y lo que corresponde es **no considerarlos** — quedan fuera de toda validación y de toda cita, no son fuente autoritativa de ningún tema y ningún lineamiento los referencia. El archivo `versionamiento/..._v0.1.0_OLD.md` llegó a eliminarse por una lectura errónea de esa instrucción y fue **restaurado íntegro** (1.239 líneas, verificado). Regla vigente incorporada a `GOB-MAT-001` y operacionalizada en el linter, que ignora los sufijos `_OLD`/`_BACKUP`/`_obsoleto`/`_DEPRECADO`.
- [x] **H8.4** Eliminadas de 6 pies de página. **Cinco de los seis estaban desactualizados respecto a su propio encabezado** (FE-ANG declaraba v0.1.0 con header v0.1.2; DEV-JAVA v0.1.2 con header v0.1.9; BD-ORA v0.1.7 con header v0.1.9; OBS v0.1.0 con header v0.1.2; API-REST v0.1.2 con header v0.1.5). Un mismo documento llegaba a declarar tres versiones distintas a la vez: nombre de archivo, encabezado y pie.
- [ ] **H8.5** Taggear releases del corpus en Git (ej. `corpus-v2026.08`) según la propia práctica que `LIN-VER-001 §15` exige al código. *(Pendiente: requiere commit previo de los cambios en curso.)*
- [x] **H8.6** *(detectado y corregido en H2/H3, 2026-08-05)* Los tres documentos de arquitectura — `LIN-ARQ-001` (Nivel 1), `LIN-DIS-001` (Nivel 2) y `LIN-PAT-001` (catálogo) — eran los **únicos del corpus sin sección de historial de versiones**, pese a que todos los demás lineamientos la tienen y a que la propia matriz exige trazabilidad. Historial incorporado a los tres. Las versiones previas quedan agregadas como rango con nota "detalle no registrado" (no es reconstruible desde los documentos). *(La Matriz `GOB-MAT-001` tenía el mismo problema y se corrigió en H5.6.)*

### H9 — Residuo de build versionado en template

- [x] **H9.1** Eliminado `template-backend-java/.m2/repository/.../spring-boot-starter-parent-3.5.0.pom.lastUpdated` — marcador interno del resolvedor de Maven que registraba una **descarga fallida** (error de resolución DNS del 2026-05-28), no contenido del template. **Confirmado con Arquitectura antes de borrar.** Recuperable desde el historial de Git.
- [x] **H9.2** `.m2/` añadido al `.gitignore` de **ambos** templates, con nota de por qué: el CI usa `-Dmaven.repo.local=.m2/repository`, así que sin la regla la caché volvería a colarse en cada ejecución local. Verificado con `git check-ignore`.

### H12 — Tooling: linter del corpus + anclas estables (causa raíz de H1, H5, H6, H7)

- [ ] **H12.1** Introducir IDs estables e independientes de la numeración para reglas importantes (estilo requirement-ID: `ARQ-R-021`). Las citas cruzadas usan el ID, no "§6.2". Empezar por las reglas más citadas (estadios, timeouts, resiliencia, wrapper, estilos por defecto). *(Pendiente: es una migración que toca todo el corpus; el linter de H12.2 ya mitiga el síntoma mientras tanto.)*
- [x] **H12.2** **Linter del corpus implementado** en `herramientas/lint_corpus.py` (Python 3.8+, sin dependencias) con 5 comprobaciones — C1 citas cruzadas, C2 coherencia de versión, C3 códigos PT, C4 artefactos duplicados, C5 enlaces — más `.gitlab-ci.yml` que lo ejecuta en cada MR que toque documentación y en la rama principal, y `herramientas/README.md` con las decisiones de diseño. **Resultado del primer uso: 8 citas rotas nuevas que la revisión manual no había encontrado** (ver H12.4). Verificado con pruebas de regresión: se inyectó un fallo de cada tipo y las 5 comprobaciones lo detectaron, con código de salida correcto.
- [x] **H12.3** Regla de proceso documentada en `herramientas/README.md`: toda renumeración de secciones de un documento dueño debe corregir, en el mismo MR, las citas de `GOB-MAT-001` y de los consumidores. El linter la hace verificable — un MR que renumere sin propagar falla en C1. *(La regla equivalente ya se incorporó a la Matriz como regla de mantenimiento 8 en H5.8.)*
- [x] **H12.4** *(hallazgos del primer uso del linter, 2026-08-05)* **8 citas rotas adicionales**, ninguna detectada en la revisión manual: `LIN-CICD-001` → `LIN-ARQ-001 §8.3.4` (inexistente; es `§8.3` numeral 4) y → `LIN-IAC-001 §18.1` (el documento solo llega a `§16`; el repositorio dedicado está en `§5`); `LIN-FE-ANG-001` → `LIN-DEV-JAVA-001 §11.4.4` (**duodécima superviviente** de la renumeración `11.4→13.4`); `LIN-DEV-JAVA-001`, `GLOSARIO-ONP` y `Plantilla_Documento_Arquitectura` → `LIN-ARQ-001 §9`/`§9.5` (la misma "sección fantasma 9.5" que `LIN-API-REST-001 v0.1.3` ya había corregido en su propio texto sin buscarla en el resto; real: `§5.3`); `Brecha_Framework` → `LIN-DEV-JAVA-001 §10.4.1` (real: `§7.1`–`§7.5`, corrección que su propia v0.1.5 declaró hecha); `Plantilla_Documento_Arquitectura` → `LIN-DIS-001 §8` (real: `§6`). Todas corregidas.
- [x] **H12.5** *(hallazgo grave del propio linter, 2026-08-05)* **La Plantilla de Documento de Arquitectura era una cuarta fuente de timeouts y arrastraba las contradicciones H2 y H3 que dábamos por cerradas.** Su tabla de atributos de calidad publicaba "timeouts mandatorios (2s conn / 3-5s read)" y exigía "Circuit Breaker / Bulkhead de Resilience4j" como si fueran obligatorios. Era invisible porque la Plantilla no declara `Código:` y el linter la descartaba. Corregida para delegar en `LIN-DIS-001 §6` con el matiz correcto (timeout y Bulkhead siempre; Circuit Breaker solo en Microservicios o bajo ADR). Barrido posterior: cero fuentes de timeout fuera del dueño.
- [x] **H12.6** *(bug del propio linter, corregido)* La primera versión **descartaba en silencio 17 de 36 archivos** — todos los que no declaran `Código:` en el encabezado, incluidos Glosario, START_HERE, ambos ADRs, el tablero de Brechas y la Plantilla de Arquitectura. Se detectó porque las pruebas de regresión inyectadas en el Glosario no dispararon ninguna alerta. Un linter que omite archivos en silencio es peor que no tenerlo: corregido para analizar todos los markdown, con el código como dato opcional usado solo para resolver citas entrantes. Se añadió `**Código:** GLOSARIO-ONP` al Glosario.
- [x] **H12.7** *(derivado — cerrado 2026-08-05)* Los cinco documentos del corpus sin campo `Código:` ya lo declaran y son destino verificable de citas: `GOB-INI-001` (START HERE), `GOB-PLA-001` (Plantilla de Documento de Arquitectura), `GOB-BRE-001` (Tablero de Brechas) y los dos ADR, que ya llevaban su código en el título pero no en el encabezado. En la Plantilla el código se declaró en un bloque de identidad **separado de la tabla que rellena cada proyecto** —antes su versión `v2.1` se confundía con la del documento derivado— y esa tabla ahora pide explícitamente la versión del documento, no la de la plantilla. Registrados en `GOB-MAT-001`, cuyo catálogo se reorganizó en tres bloques. **El linter queda en 0 errores y 0 avisos.**

---

## PRIORIDAD 2 — Evolución y menores

### H10 — Menores de consistencia

- [x] **H10.1** Terminología del modelo de ramas unificada con su documento dueño. `LIN-ARQ-001 §2.3` y `§8.1` lo llamaban «Trunk-Based Development», nombre que `LIN-VER-001 §6` **no usa como nombre del modelo**: allí el modelo es **GitLab Flow simplificado** y TBD es la *disciplina* que lo rige (ramas de 1–5 días, integración frecuente). Su glosario incluso registra «TBD controlado» como nombre anterior. Ambas menciones del Marco Rector citan ahora el nombre oficial con su sección dueña.
- [x] **H10.2** `GOB-INI-001 §5` reescrito con **criterio explícito de elección de template**. Antes declaraba `template-backend-java` (simple) como baseline único, contradiciendo la topología por defecto del corpus: todo proyecto nuevo nacía en monolito simple cuando el Estadio 2 es Monolito Modular. Ahora una tabla contrapone ambos templates (cuándo usar cada uno y su estructura) con el **modular como default**, y una regla de desempate: ante la duda, modular — migrar después obliga a reorganizar paquetes, `pom.xml` y pipeline, mientras que empezar modular con un solo componente no cuesta nada. Se añadió el aviso de que los artefactos normados no se personalizan sin ADR.
- [x] **H10.3** Códigos PT sin fuente única (síntoma: LIN-SEC-APP v0.1.2 corrigió PT06↔PT07 y v0.1.3 lo revirtió). **Cerrado en H5.2:** `LIN-PAT-001` declarado fuente única de códigos PT/PAT, con índice de trazabilidad en la Matriz. *(Queda la corrección del tablero de Brechas en H6.3.)*
- [x] **H10.4** Tres erratas corregidas: `oquestado`→`orquestado` (`LIN-ARQ-001 §3.3`), `institutcional`→`institucional` (`ADR-CLOUDEVENTS-001`) y «Estándar por **Defectos**»→«por **Defecto**» (`LIN-ARQ-001 §2.1` y `§8.2`, dos ocurrencias). Esta última no era intencional: describe el Monolito Modular como estándar por defecto, no como estándar de defectos.
- [x] **H10.5** *(cerrado durante H12, 2026-08-05)* Los 3 enlaces del banner de `LIN-ARQ-000` con rutas absolutas de otra máquina (`file:///home/carlos/...`) se convirtieron a rutas relativas y se verificó que los destinos resuelven. El linter lo cubre ahora con C5.
- [x] **H10.6** *(cerrado en H13, 2026-08-05 — resultaron ser 3 ocurrencias, no 2: §1.4 párrafo, §1.4 tabla y §17)* `LIN-DEV-JAVA-001` clasificaba a `LIN-ARQ-001` como **"Nivel 2"** — es **Nivel 1**. El error aparece dos veces (párrafo de Supremacía Jerárquica y tabla de documentos relacionados) y contradice al propio `LIN-ARQ-001 §1.2`. Es exactamente el mismo error que `LIN-SEC-APP-001` ya corrigió en su v0.1.4, lo que sugiere revisar la declaración de nivel en **todos** los lineamientos de Nivel 3. Además, la tabla nombra a `LIN-ARQ-001` como "Marco Rector de **Diseño** y Arquitectura de Software" — ese era el título de `LIN-ARQ-000`; el vigente es "Marco Rector de Arquitectura de Software".
- [x] **H10.7** *(cerrado en H13, 2026-08-05)* `LIN-DEV-JAVA-001` no referenciaba `LIN-SEC-APP-001 §9.1`. La tabla de documentos relacionados de `§1.4` se amplió con `LIN-DIS-001` (Nivel 2, que faltaba por completo), `LIN-SEC-APP-001` (§9.1 Spring Security, §8.3 filtro SAA, §12 secretos) y `LIN-TEST-001` (§5.1 cobertura).

### H11 — Posicionamiento y contenido nuevo

- [ ] **H11.1** `LIN-API-REST-001 §4`: nota explícita de posicionamiento frente a RFC 7807 (citado en §1.3 pero no adoptado): "el envelope institucional se aparta deliberadamente de RFC 7807 por compatibilidad; `codHttp` en el body es informativo — la fuente de verdad es el status line HTTP". Previene el bug clásico de responder 200 con `codHttp: 500`.
- [ ] **H11.2** Nuevo lineamiento (o sección en dueño existente): **backup/DR, RTO/RPO y gestión de capacidad** — vacío mayor para una entidad de pago de pensiones. Priorizar sobre brechas de baja prioridad (Serverless, Ambassador).
- [ ] **H11.3** Política transversal de **datos de prueba y enmascaramiento de PII** para ambientes QA (Ley 29733). Hoy solo LIN-PERF §11 lo toca parcialmente.
- [ ] **H11.4** Rate limiting interno (PI10 de la Brecha, "Parcial"): definir mecanismo mientras WSO2 siga en PoC — hoy ningún servicio interno está protegido.
- [ ] **H11.5** `LIN-DOC-001` (documentación y modelado): sigue Pendiente en la Matriz pero la `Plantilla_Documento_Arquitectura` ya existe — hay dueño natural para arrancarlo.
- [ ] **H11.6** `ADR-WSO2-001`: convertir la lista de documentos a actualizar cuando WSO2 gradúe en checklist con checkboxes (no depender de memoria).

---

## Registro de cierres

| Ítem | Fecha | Documento(s) modificado(s) | Versión resultante | Nota |
|---|---|---|---|---|
| H1 (completo) | 2026-08-05 | `GLOSARIO_ONP.md` | 0.2.2 | Definición "Estadios de Topología" reescrita con escala 1/2/3 y cita `§2.1` |
| H1 (completo) | 2026-08-05 | `arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md` | 0.1.5 | 4 fichas corregidas: Estadio 0 → Estadio 1 |
| H1 (completo) | 2026-08-05 | `arquitectura/Plantilla_Documento_Arquitectura_ONP.md` | v2.1 | 3 guías corregidas (A.1/A.2/A.3) + citas ACL/Strangler redirigidas; fila añadida al historial |
| H5 (completo) | 2026-08-05 | `Matriz_Propiedad_Documental_ONP.md` | GOB-MAT-001 v0.3.0 | Catálogo al día · 13 citas rotas corregidas · sección de patrones cerrada y convertida en índice PT→ficha · historial de versiones creado · reglas 7 y 8 de mantenimiento añadidas · estados de H1 registrados |
| H2 + H3 | 2026-08-05 | `arquitectura/Lineamiento_Marco_Rector_Arquitectura_ONP.md` | LIN-ARQ-001 v0.1.8 | §4.3 numeral 3: elimina mandato de Resilience4j y rango propio de timeout; exige resultado y delega mecanismo · historial creado |
| H2 + H3 | 2026-08-05 | `arquitectura/Lineamiento_Diseno_Software_Patrones_Tacticos_ONP.md` | LIN-DIS-001 v0.1.5 | §6: declaración de propiedad documental de la resiliencia táctica · historial creado |
| H3 | 2026-08-05 | `Web/Lineamiento_Estandar_APIs_REST_ONP.md` | LIN-API-REST-001 v0.1.5 | §8.3: elimina tabla propia 5s/10s, delega al dueño, conserva la respuesta 504/402 |
| H1 (adenda) | 2026-08-05 | `arquitectura/Lineamiento_Catalogo_Patrones_Fichas_ONP.md` | LIN-PAT-001 v0.1.5 | Historial de versiones creado (la nota de cambio estaba embebida en el encabezado) · declaración de fuente única de códigos PT |
| H2 + H3 | 2026-08-05 | `Matriz_Propiedad_Documental_ONP.md` | GOB-MAT-001 v0.3.0 | Fila nueva "Resiliencia táctica en llamadas externas" con dueño `LIN-DIS-001`, estado Resuelto |
| H4 | 2026-08-05 | `desarrollo/plantillas/checkstyle-onp.xml` | — (canónico) | **Corregido bug bloqueante:** `LineLength` movido de `TreeWalker` a `Checker` (Checkstyle ≥8.24) — el archivo nunca había podido ejecutarse |
| H4 | 2026-08-05 | `.../template-backend-java/checkstyle-onp.xml` · `.../template-backend-java-modular/checkstyle-onp.xml` | — | Sincronizados con el canónico; recuperan las 3 métricas obligatorias de `LIN-DEV-JAVA-001 §12.1` y 14 reglas más |
| H4 | 2026-08-05 | 2 × `ApiResponseWrapper.java` + `GlobalExceptionHandler.java` (templates) | — | `FieldError` → `CampoError` (15 ocurrencias) |
| H4 | 2026-08-05 | `MontoMonetario.java` · `Dni.java` · `Afiliado.java` (template modular) | — | 3 líneas >120 caracteres corregidas, detectadas al activar `LineLength` |
| H4 | 2026-08-05 | `README.md` de ambos templates | — | Sección nueva "Artefactos normados — no personalizar": mapa archivo → fuente canónica → documento que lo norma |
| H13 (completo) | 2026-08-05 | `desarrollo/Lineamiento_Estandar_Desarrollo_Java_ONP.md` | LIN-DEV-JAVA-001 v0.1.9 | §15.3+§16.2 cobertura · Anexo B sincronizado con canónico · §16.6 renumerado · §1.4+§17 Nivel 1 y 3 documentos añadidos · §13.3 CAP · 10 enlaces relativos |
| H13.3 | 2026-08-05 | `arquitectura/Lineamiento_Marco_Rector_Arquitectura_ONP.md` | LIN-ARQ-001 v0.1.9 | §2.3: `Experiment Toggle` incorporado como 4ª categoría, `Permission Toggle` acotado a control de acceso, nota de distinción; ampliación anotada en ADR-014 |
| H6 · H7 · H9 | 2026-08-05 | `LIN-PAT-001` · `GLOSARIO-ONP` · `GOB-BRE-001` · `GOB-PLA-001` · `LIN-TEST-001` · `LIN-API-REST-001` · templates | GOB-BRE-001 v0.1.6 | 78 citas mal dirigidas o al congelado corregidas · discrepancia de códigos PT resuelta · anclas internas de API-REST · residuo `.m2` eliminado y `.gitignore` actualizado |
| H10 | 2026-08-08 | `LIN-ARQ-001` · `GOB-INI-001` · `GOB-PLA-001` · `ADR-CLOUDEVENTS-001` | LIN-ARQ-001 v0.1.10 · GOB-INI-001 v0.3.0 · GOB-PLA-001 v2.2 | Terminología del modelo de ramas · criterio de elección de template · 3 erratas · 2 citas a `LIN-ARQ-001 §7` (que es Frontend) redirigidas a `§5.2` |
| H12.2 | 2026-08-05 | `herramientas/lint_corpus.py` · `herramientas/README.md` · `.gitlab-ci.yml` | — (nuevos) | Linter del corpus con 5 comprobaciones + pipeline GitLab + documentación de diseño y regla de proceso |
| H12.4 | 2026-08-05 | `CICD` · `Web/FE-ANG` · `desarrollo/DEV-JAVA` · `GLOSARIO` · `arquitectura/Brecha` · `arquitectura/Plantilla` | — | 8 citas rotas corregidas, detectadas por el linter |
| H12.5 | 2026-08-05 | `arquitectura/Plantilla_Documento_Arquitectura_ONP.md` | v2.1 | Eliminada la 4ª fuente de timeouts y el mandato de Resilience4j; delega en `LIN-DIS-001 §6` |
| H12.6 | 2026-08-05 | `GLOSARIO_ONP.md` | 0.2.2 | Añadido `**Código:** GLOSARIO-ONP` para que sea destino verificable de citas |

---

## Historial de versiones

| Versión | Fecha | Autor | Descripción |
|---|---|---|---|
| 0.1.0 | 2026-08-05 | Arquitectura OTI | Versión inicial — hallazgos de la revisión integral del corpus (H1–H12) organizados por prioridad |
