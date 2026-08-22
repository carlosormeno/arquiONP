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
- [x] **H14.2** 🟠 `§1.3` reescrita con códigos e incorporando los cuatro consumidores que faltaban (`LIN-DIS-001`, `LIN-BUS-001`, `LIN-TEST-001`, `LIN-VER-001`). Añadida nota de que la nomenclatura `CD01`/`CD02` del documento congelado no rige.
- [x] **H14.3** `LIN-BUS-001 §7.3` deja de reproducir el DDL de `EVT_OUTBOX` y remite a `LIN-BD-ORA-001 §3.10`. Conserva lo propio: el proceso de relevo y el contrato del evento.
- [x] **H14.4** Revisión de contenido de `LIN-VER-001` — **cerrada en H23** (7 hallazgos, 6 de ellos reglas que no obligaban a quien debían).
- [x] **H14.5** *(cerrado con la revisión de BD)* **Dueño doble corregido:** el tema «Versionamiento de scripts de BD» figuraba con `LIN-VER-001` **y** `LIN-BD-ORA-001` como dueños simultáneos, contra el principio rector de la matriz. Dividido en dos temas con un dueño cada uno —obligatoriedad en `LIN-VER-001 §16`, nomenclatura y estructura en `LIN-BD-ORA-001 §8`— reflejando el reparto que la observación ya describía.
- [x] **H14.6** **Cerrado en H32.** Los 19 documentos del corpus tienen revisión de contenido cerrada. Ninguno queda en `Borrador`.

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

## H19 — Revisión de contenido de `LIN-OBS-001` (2026-08-08)

Prioridad 1 de la ruta de graduación: `LIN-ARQ-001 §8.3` lo hace exigible al contratista. Es **el documento mejor escrito del corpus** —el aviso de compatibilidad de versión del appender OTLP por versión de Spring Boot, o la explicación de por qué hace falta `OpenTelemetryLogbackConfig` (Spring no llama `GlobalOpenTelemetry.set()`, y sin eso el appender descarta los logs en silencio), son detalles que ahorran horas de diagnóstico. Los hallazgos están concentrados en dos piezas de código.

- [x] **H19.1** 🔴 **Orden de filtros invertido** (decisión de Arquitectura tras evaluar costos: sin servicios en producción, el momento de menor costo para cambiar el contrato). `CanonicalRequestLogFilter` pasa a `@Order(2)` envolviendo a `SaaTokenValidationFilter` (`@Order(3)`), y la identidad viaja por el atributo de request `onp.user.id` porque el MDC ya se limpió cuando el log se emite. **Resultado: toda petición produce exactamente un log canónico**, incluidos los 401 por token inválido y los 503 por caída del SAA. Se descartó la alternativa —que el filtro de seguridad emitiera su propio log— por crear dos productores del mismo evento: la misma clase de duplicación que ya divergió tres veces en este corpus (H3 timeouts, H4 checkstyle, H14.3 `EVT_OUTBOX`). Aplicado en 4 documentos (`LIN-OBS-001 §4.9`/`§4.11`, `LIN-SEC-APP-001 §8.3`/`§8.7.1`, `LIN-DEV-JAVA-001 §1.3`, `LIN-API-REST-001 §7.1`) y en los **6 archivos de filtro de ambos templates**, verificado con build real: compile, checkstyle y 11 tests en verde.
- [x] **H19.2** 🟠 `Mask.email` endurecido: devolvía excepción con parte local vacía o dominio sin punto. Se añade una **regla de robustez explícita** al documento — ningún método de `Mask` puede lanzar excepción, porque vive en la ruta de emisión de logs y una excepción ahí tumba el registro justo al procesar un dato personal.
- [x] **H19.3** 🟡 `StatusCapturingResponse` eliminado. `response.getStatus()` (Servlet 3.0+) ya refleja los `sendError` emitidos por los filtros internos — precisamente los 401/503 que ahora sí se registran. El wrapper, además, reportaba `200` falso ante cualquier ruta que fijara el status sin pasar por `setStatus`/`sendError`.
- [x] **H19.4** 🟡 Añadida nota en `§4.3`–`§4.5`: el transporte OTLP en `http://` es intra-clúster y las exigencias de Zero Trust (`LIN-ARQ-001 §5.1`) y HTTPS (`LIN-SEC-APP-001 §7.1`) se satisfacen **a nivel de plataforma** —mTLS del *service mesh* o `NetworkPolicy`, responsabilidad de `LIN-K8S-001`—, no de aplicación. Se aclara que el servicio no configura TLS por su cuenta y que el endpoint no cambia si Plataforma habilita mTLS.

---

## H20 — Revisión de contenido de `LIN-TEST-001` (2026-08-09)

La otra prioridad 1 de la ruta de graduación. **El bloque normativo común es el mejor razonado del corpus:** `§3.4` separa *tipos* de prueba (auditables, obligatorios) de *enfoques* de diseño (TDD, BDD, test-after) y declara explícitamente que «lo que se audita es el tipo y la cobertura, no el enfoque» — evita la discusión estéril sobre metodología. Y `§4.6` reconoce que la estrategia de dominio es **ortogonal** al estilo arquitectónico, la misma distinción que `LIN-ARQ-001 §8.2` tuvo que aclarar por separado. Los umbrales de `§4` y `§5.1` se verificaron cruzados: **coinciden en los cinco estilos**.

- [x] **H20.1** 🟠 Las cinco tablas de `§4` dejan de repetir los umbrales y remiten a `§5.1` como fuente única; conservan lo que sí aportan, el tipo de prueba prioritario por capa. Añadida nota explicando por qué no se repiten, con la referencia al caso en que este mismo dato ya divergió (`LIN-DEV-JAVA-001 §15.3`, H13.1).
- [x] **H20.2** 🟡 Corregido el enlace de `§3.4`, que apuntaba a un paso numerado del capítulo PL/SQL en vez de a `§5`.

---

## H21 — Anclas internas rotas en todo el corpus (2026-08-09)

- [x] **H21.1** **Cerrado en H41.1.** 477 enlaces internos, **0 rotos**. Las tres causas eran mecánicas: tildes, guion doble por raya en el título, y retitulados no propagados al índice.
- [x] **H21.3** 🔴 **Desbloqueado.** Encabezados de `LIN-BD-ORA-001` y `LIN-BI-001` normalizados a `## N. Título`. En BD el cambio hizo funcionar los enlaces del índice **sin tocarlos**, porque ya apuntaban a `#N-…`. **En BI ocurrió lo contrario y los rompí:** sus enlaces sí seguían el formato antiguo `#sección-N-…`, así que hubo que actualizarlos — detectado por la verificación posterior, no previsto al planificar. Corregidas además 15 anclas de BD con tilde omitida o guion simple donde el encabezado genera doble. **Resultado: 55/55 y 11/11 enlaces resuelven**; ambos documentos dejan de estar bloqueados por severidad. Las citas externas por número de sección no se vieron afectadas: el linter sigue en verde.
- [x] **H21.2** **Cerrado en H41.2.** C7 implementada y validada contra el corpus; documentada la dependencia del renderizador.

---

## H22 — Fase 1: primeras graduaciones a Vigente (2026-08-09)

- [x] **H22.1** **`LIN-OBS-001` y `LIN-TEST-001` graduados a `Vigente`**, los primeros del corpus bajo el ciclo de vida de H17. Con ello los requisitos de `LIN-ARQ-001 §8.3` que dependían de ambos —evidencia de observabilidad y umbrales de cobertura— **dejan de estar degradados a recomendados y son exigibles contractualmente**.
- [x] **H22.2** **La verificación del criterio 3 encontró dos desalineamientos reales**, lo que confirma que no es un trámite: `LIN-FE-ANG-001 §14.2` citaba `LIN-TEST-001 §4.4` para herramientas E2E —`§4.4` es *Microservicio*, otra cita que resuelve y apunta al tema equivocado, la misma que ya se había corregido en la Matriz sin propagarse— y `LIN-DEV-JAVA-001 §15.1` omitía el sufijo `CT` de las pruebas de caracterización que define el dueño. Ambos corregidos **antes** de graduar.
- [x] **H22.3** **Dos temas permanecieron `En borrador` a propósito — ambos cerrados: pruebas de contrato en H24.9, evidencias y criterios de paso en H27.1.** «Pruebas de contrato» y «Evidencias obligatorias y criterios de paso a QA/PROD» no pudieron confirmarse alineados con sus consumidores: `LIN-API-REST-001` apenas menciona las pruebas de contrato y `LIN-CICD-001` aún no está revisado. Aplicando la regla editorial de la matriz —*«si existe duda razonable sobre la sincronización real, se debe usar `En borrador`, no `Conforme`»*— se dejan abiertos en vez de cerrarlos por conveniencia. Se resolverán al revisar esos dos consumidores.
- [ ] **H22.4** Graduar el **Nivel 1 y 2** (`LIN-ARQ-001`, `LIN-DIS-001`, `LIN-PAT-001`), ya sin la dependencia que los bloqueaba. Requiere aprobación del **Comité de Arquitectura**, no de Arquitectura OTI (`GOB-MAT-001`, Aprobación).
- [x] **H22.5** **Decisión fijada (Arquitectura, 2026-08-09):** el criterio 2 distingue entre hallazgos **de documento** —bloquean la graduación— y **de corpus** —se registran como deuda con responsable y fecha—, con **excepción por severidad**: un hallazgo transversal que comprometa el uso normativo de un documento concreto se escala y sí bloquea. Se optó por una regla general en vez de un caso especial para las anclas, porque el mismo problema volverá con H12.1 (IDs estables). Registrada en `GOB-MAT-001 §Ciclo de vida`.

---

## H23 — Revisión de fondo de `LIN-VER-001` (2026-08-09)

Cierra **H14.4**. El documento (1.541 líneas) se revisó completo. El patrón dominante no fueron citas rotas —el linter ya las cubre— sino **reglas que existen pero no obligan a quien deberían**.

- [x] **H23.1** **La regla de las 400 líneas por MR solo existía en `LIN-DEV-JAVA-001 §16.4`.** Verificado: `LIN-VER-001` no mencionaba la cifra en ninguna parte. El efecto práctico es que un MR de Angular, un script SQL de 2.000 líneas o un cambio masivo de manifiestos K8s **no tenían límite de tamaño**, precisamente los cambios donde una revisión superficial es más peligrosa. La causa es de propiedad documental: la matriz cargaba **dos temas de revisión de código** con dueños distintos, y el estándar de Java —que solo puede normar Java— se había quedado con las reglas generales. Corregido: `LIN-VER-001 §12` declara ser dueño del **proceso** para todo tipo de cambio (autoaprobación, revisor mínimo, revisor especializado, tamaño) y `LIN-DEV-JAVA-001 §16` remite a él conservando solo lo propio del stack (Checkstyle, JaCoCo, antipatrones). Ambos temas de la matriz reescritos.
- [x] **H23.2** **`§13` degradaba a opcional un umbral obligatorio de un documento `Vigente`.** La tabla de evidencias pedía «cobertura *si aplica*» para backend Java, mientras `LIN-TEST-001 §5.1` —graduado a `Vigente` en H22.1— la exige con umbrales fijos. Un revisor que siguiera `LIN-VER-001` podía aprobar el MR sin cobertura y estar formalmente en regla. Es la **regla de exigibilidad de `GOB-MAT-001` operando en sentido inverso**: no un documento vigente apoyándose en uno borrador, sino un borrador relajando a uno vigente. Corregido: la tabla remite al dueño y se añadió una nota que separa *qué se evidencia* de *qué umbral se exige*.
- [x] **H23.3** **`§15.1` declaraba «formato obligatorio» `v<MAJOR>.<MINOR>.<PATCH>`, que su propia `§14.3` contradice** al permitir `1.2.0-rc.1` y `1.2.0-dev.45`. Tal como estaba, todo tag de release candidate violaba el lineamiento. Corregido admitiendo el sufijo de pre-release y precisando que un tag productivo nunca lo lleva.
- [x] **H23.4** **`§15.3` admitía «un identificador trazable» como tag de imagen** — resquicio suficiente para justificar `latest`, que `LIN-K8S-001 §6.3` prohíbe expresamente en QA y Producción. Corregido exigiendo tag explícito e inmutable, con el SHA de commit como única alternativa. *(Al escribir la corrección se citó `LIN-K8S-001 §14.2`, que existe pero es «Escaneo de imágenes»: el linter la habría dado por buena. Detectada al verificar el tema y redirigida a `§6.3`, «Tags permitidos» — el mismo error de H6.1 reproducido en tiempo real.)*
- [x] **H23.5** **El mecanismo que hace exigibles P2 y P3 figuraba como «recomendado».** `§21.1` listaba *protected branches*, *Merge Requests* y *approval rules* entre capacidades opcionales de GitLab, cuando son lo único que impide técnicamente el commit directo y la autoaprobación que los principios rectores prohíben. Sin ellas las reglas dependen de la disciplina individual, no del repositorio. Las tres pasan a **obligatorias**; el resto de la tabla queda marcado como recomendado.
- [x] **H23.6** Plantilla de MR de Angular desalineada: usaba `## Rollback` donde `§11.5` normaliza `## Plan de reversa`. Era el nombre **anterior** a la corrección terminológica de v0.1.4 —sobrevivía en el template porque la migración solo tocó los de Java—. Corregido y verificado que ninguna plantilla conserva el nombre antiguo. Las demás diferencias del template Angular (Jest, `nginx.conf`, sin sección de BD) son especializaciones legítimas.
- [x] **H23.7** `§2` no listaba `LIN-DIS-001` (Nivel 2) ni `GOB-MAT-001`, pese a que el lineamiento versiona y promueve lo que aquél diseña. Añadidos. El control de cambios del documento estaba además desordenado (v0.1.7 antes de v0.1.6): reordenado cronológicamente, el mismo defecto que ya había aparecido en `LIN-BD-ORA-001`.

**Estado:** `LIN-VER-001` v0.1.8 pasa a **En revisión**. `LIN-DEV-JAVA-001` v0.1.11. Linter en verde (0 errores, 0 avisos).


---

## H24 — Revisión de fondo de `LIN-API-REST-001` (2026-08-09)

Documento de 1.074 líneas. El patrón dominante es **una plataforma en PoC usada como si estuviera operativa**: tres controles obligatorios se delegan en WSO2 y, como WSO2 no existe en producción, hoy nadie los aplica. Cierra **H11.1** y **H11.4**, y desbloquea uno de los dos temas que H22.3 dejó abiertos.

### Seguridad — controles que no se aplican en ninguna parte

- [x] **H24.1** **`§7.2` proponía un modelo de autorización contrario al de su documento dueño, con un ejemplo que además no funciona.** Titulaba la sección «RBAC», declaraba control de acceso basado en roles propios de la aplicación y ejemplificaba con `@PreAuthorize("hasRole('ROL_GESTOR_AFILIACIONES')")`. `LIN-SEC-APP-001 §5.3` numeral 4 prohíbe expresamente el RBAC propio cuando el permiso ya existe en SAA, y `§5.4` usa permisos con `hasAuthority`. El ejemplo, además, **es inoperante**: Spring Security antepone `ROLE_` a `hasRole`, de modo que buscaría el authority `ROLE_ROL_GESTOR_AFILIACIONES` y ningún usuario lo tendría — el endpoint quedaría cerrado para todos. Reescrita según el dueño, con la explicación del prefijo para que no se reintroduzca.
- [x] **H24.2** **`§7.4` degradaba a «valor recomendado» headers que `LIN-SEC-APP-001 §7.3` declara obligatorios**, y los asignaba al API Gateway. Mientras WSO2 siga en PoC no hay gateway que los inyecte, así que las respuestas salen sin `HSTS`, sin `nosniff`, sin `X-Frame-Options` y sin `Cache-Control: no-store` — este último es justamente el que evita que un proxy cachee datos previsionales. Corregido: obligatorios y emitidos por el servicio hasta que WSO2 gradúe.
- [x] **H24.3** **`§8.4` dejaba a toda API de la ONP sin rate limiting.** Afirmaba que *«los servicios no implementan rate limiting internamente — es responsabilidad del Gateway»*, contradiciendo a `LIN-SEC-APP-001 §7.1`, que exige *«rate limiting básico configurado en la aplicación si no hay gateway»*. Entre ambos documentos el control desaparecía. Corregido separando modelo objetivo y estado actual, y añadiendo `codDetRespuesta 302` / `429` al catálogo. **Ratificado por Arquitectura el 2026-08-09** conforme al proceso de `§4.2.1(c)`: se mantiene en el rango 300 (Autorización) en lugar de abrir una categoría nueva, asumiendo la ambigüedad visual con el HTTP 302 —el catálogo ya convive con ella, su `401` de Integración significa «servicio externo respondió con error», no «no autenticado»—.

### Coherencia interna — el documento se contradice consigo mismo

- [x] **H24.4** **`§2.2` prohíbe `http://` en QA y PROD; `§2.5` daba por sentado que los backend reciben tráfico intra-cluster sobre HTTP** tras la terminación TLS en el gateway. Es una topología legítima y habitual, pero no está admitida por `LIN-SEC-APP-001 §7.1`, que solo exceptúa `localhost`. No se resolvió relajando la regla de seguridad por cuenta propia: se elevó a Arquitectura. **Cerrado el 2026-08-09 con `ADR-TLS-INTERNO-001`** — se admite el tramo intra-cluster sobre HTTP como excepción acotada, **sustituyendo un control por otro, no retirándolo**: la `NetworkPolicy` pasa de *recomendada / obligatoria para críticos* a **obligatoria para todo servicio que reciba tráfico interno** (`LIN-K8S-001 §9.1` y Anexo E), y un servicio sin ella debe servir HTTPS extremo a extremo. La excepción no alcanza al tráfico hacia SAA, RENIEC, SUNAT u Oracle, ni a despliegues fuera de Kubernetes, y queda sin efecto al habilitarse la malla de servicios. El gate de publicación de `§10.3` verifica la `NetworkPolicy` antes de autorizar producción.
- [x] **H24.5** **`§9.6` produce un despliegue que nunca arranca si se cumple `§9.5`.** La sección de métricas exige separar `management.server.port` del puerto de la API para no publicar `/actuator/prometheus`; el manifiesto de ejemplo de la sección siguiente pone ambas probes en `8080`. Aplicadas las dos reglas juntas, Kubernetes recibe `404`, el pod nunca pasa a *Ready* y el rollout se queda colgado. Se documentaron las dos opciones coherentes y se hizo explícito que el valor del `Deployment` y el de `application.yml` se cambian juntos. *(**Deuda cerrada en H26.8:** `LIN-K8S-001 §15.2` fija la convención. Al detectarse, ningún documento la definía y **todos** los manifiestos del corpus —`LIN-K8S-001`, `LIN-OBS-001` y los dos templates— exponen Actuator en `8080`. Los ejemplos se dejaron en `8080` para no introducir una incoherencia nueva; falta que `LIN-K8S-001` fije la convención.)*
- [x] **H24.6** `§3.1` y `§2.5` publicaban dos formas distintas de URL canónica —`/api/v1/recurso` frente a `apis.onp.gob.pe/pensiones/v1/recurso`— sin decir cuál rige. Aclarado: la primera es la ruta que implementa el servicio, la segunda la que publica el gateway; el backend no altera su ruta para acomodarse.

### Contrato y gobernanza

- [x] **H24.7** **`§3.3.2` clasificaba como «nueva versión menor» agregar un campo obligatorio al request.** Es un cambio que **rompe a todos los consumidores existentes** —dejan de recibir `200`— y la categoría ni siquiera es representable: el esquema `/api/v{N}` solo expresa la versión mayor. La tabla se realineó con `LIN-VER-001 §17.2`–`§17.3`, incorporando los dos cambios incompatibles que faltaban (cambio de ruta o método HTTP, y cambio de reglas de autenticación/autorización) y la exigencia de ADR.
- [x] **H24.8** **`§10.2` convertía a WSO2 en la única vía a producción.** Los estados `CREATED`/`PUBLISHED` son de la plataforma, no del servicio; exigidos literalmente, ninguna API nueva podía llegar legítimamente a producción mientras el PoC no gradúe — y en la práctica eso se resuelve ignorando el gate, que es el peor desenlace posible. Definida una vía transitoria que conserva íntegros los requisitos y difiere solo los ítems que únicamente la plataforma puede satisfacer, dejándolos registrados como pendientes de migración.
- [x] **H24.9** **El gate de publicación no pedía la prueba de contrato.** `LIN-TEST-001 §6` —documento dueño, **vigente**— la declara obligatoria en todo endpoint publicado, con validación OpenAPI como mínimo siempre. La Parte A de `§10.3` no la mencionaba, y la única alusión del documento entero estaba en `§4.2.1(c)` como «si aplica». Incorporada al gate con el nivel exigido según el tipo de consumidor. **Con esto el tema «Pruebas de contrato» de la matriz pasa de `En borrador` a `Conforme`** — era uno de los dos que H22.3 dejó deliberadamente abiertos.
- [x] **H24.10** **`§1.3` citaba RFC 7807 como «Estándar de respuestas de error»** cuando la ONP usa `ApiResponseWrapper` y no `application/problem+json`. Un lector razonable concluiría que Problem Details aplica. Explicitado el posicionamiento y su motivo. Añadida además la regla de que `codHttp` es réplica informativa y el status line HTTP es la fuente de verdad: proxys, gateway, la métrica `http.server.requests` y las alertas de Kibana leen el status line, de modo que un `200` con `codHttp: 500` deja el fallo invisible en toda la cadena de observabilidad.

### Propagación de `ADR-TLS-INTERNO-001` a los artefactos ejecutables

- [x] **H24.11** Creado `k8s/base/networkpolicy.yaml` en los dos templates de backend Java y **descomentado** en su `kustomization.yaml`. Estaba comentado con la leyenda *«obligatorio para servicios críticos»*, que ya no es la regla, y remitía a `LIN-K8S-001 sección 11` — que es *Health checks*, no manifiestos: otra cita que resuelve pero apunta al tema equivocado. Corregida a `§9.1` y Anexo E en los tres templates. La política incluye egress explícito a DNS, colector OTEL, Oracle y HTTPS, con la advertencia de reemplazar `0.0.0.0/0` por CIDR reales: un egress abierto anula el control que sostiene la excepción.
- [x] **H24.12** **Cerrado en H29.6.** El `k8s/base/` del template Angular declaraba tres manifiestos inexistentes; creados junto con la `NetworkPolicy`, conformes a las correcciones de H26 y H29.

**Estado:** `LIN-API-REST-001` v0.1.7 pasa a **En revisión**; `LIN-SEC-APP-001` v0.1.7 y `LIN-K8S-001` v0.1.13 por efecto del ADR. Nuevo documento `ADR-TLS-INTERNO-001`. `LIN-OBS-001` v0.1.4 (aclaratorio, sigue **Vigente**) y `GOB-MAT-001` v0.9.0. Seis temas de `GOB-MAT-001` actualizados (rate limiting cambia de dueño a `LIN-SEC-APP-001`; se abre el tema de cifrado en tránsito). Las dos decisiones elevadas a Arquitectura quedan **cerradas**. Linter en verde.


---

## H25 — El catálogo de `GOB-MAT-001` se había desviado de la realidad (2026-08-14)

Detectado al preparar el siguiente documento a revisar, no por una comprobación: **15 de las 21 entradas del catálogo estaban desactualizadas**. El catálogo pertenece a un documento `Vigente` y es lo que un tercero —una fábrica, un contratista, un TDR— consulta para saber qué documento rige y en qué estado.

- [x] **H25.1** **Las 15 entradas corregidas.** El catálogo daba `LIN-API-REST-001` por «Borrador v0.1.5» cuando iba por «En revisión v0.1.7», y `LIN-VER-001` por Borrador después de graduarlo. Otras seis declaraban estado sin versión (`LIN-K8S-001`, `LIN-CICD-001`, `LIN-FE-ANG-001`, `LIN-PERF-001`), lo que impedía detectar cualquier desfase. → `GOB-MAT-001` v0.10.0.
- [x] **H25.2** **No era cosmético: la regla de exigibilidad se aplica sobre el estado declarado.** Un estado desfasado permite exigir contractualmente lo que aún no obliga, o lo contrario — dar por recomendado algo ya vigente. Por eso la nueva comprobación trata el estado como **error** y la versión solo como **aviso**.
- [x] **H25.3** **La causa era una brecha del linter, no un descuido.** C6 valida que la ruta del catálogo exista y contenga el código atribuido, pero **no la versión ni el estado**. Por eso el catálogo pudo desviarse a lo largo de veintitantas ediciones sin que nada saltara. Añadida **C8** — versión y estado del catálogo contra el encabezado real de cada documento. Verificada con tres fallos inyectados (desviación de estado, de versión, y el caso real de H25: el documento cambia y el catálogo no); los tres se detectan con el nivel correcto.
- [x] **H25.4** **Una desviación iba en sentido contrario.** `LIN-BD-ORA-001` tenía la revisión de contenido cerrada desde H20 y la matriz lo daba por `En revisión`, pero **su propio encabezado seguía diciendo `Borrador`**. Ahí el equivocado era el documento, no el catálogo. → v0.1.13, `En revisión`. De paso se ordenó cronológicamente su historial, que volvía a estar desordenado.
- [x] **H25.5** Documentadas C6 y C8 en `herramientas/README.md`, que solo describía C1–C5 pese a que C6 existía desde H12. Se deja constancia de que **C7 está reservado** para la comprobación de anclas internas (H21.2), pendiente de validar el algoritmo contra el renderizador real de la ONP.
- [x] **H25.6** **Cerrado en H41.3** como **C10** (C9 quedó para los IDs estables). Encontró 10 historiales desordenados en su primera ejecución.

**Estado:** `GOB-MAT-001` v0.10.0, `LIN-BD-ORA-001` v0.1.13. Linter con 7 comprobaciones, en verde.


---

## H26 — Revisión de fondo de `LIN-K8S-001` (2026-08-17)

Documento de 1.423 líneas, de buena factura en su cuerpo normativo. Los hallazgos se concentran en **los ejemplos y los códigos**, no en las reglas: lo que las fábricas copian no cumplía lo que el propio documento exige.

### Códigos: se normaba con el identificador de un inventario de vacíos

- [x] **H26.1** **`§9.4` normaba Sidecar y Ambassador como `PA12` y `PA13`** — códigos del tablero de brechas `GOB-BRE-001`, no del catálogo oficial `LIN-PAT-001`. La contradicción era circular y visible desde ambos lados: mientras un patrón figura en el tablero como `❌ Pendiente`, se está afirmando que **falta** normarlo; y el tablero seguía marcándolos pendientes «a cargo de LIN-K8S-001» cuando ese lineamiento ya los normaba en detalle. Es el mismo caso que H6.3 resolvió para BFF, Facade y Gateway-Aggregation, que quedó a medias: `LIN-PAT-001 §6` se titula «Familia 4 … (`LIN-BUS-001 / LIN-K8S-001`)» pero **no tenía ficha de ninguno de los dos únicos patrones que `LIN-K8S-001` norma por sí mismo**. Creadas las fichas `PAT-K8S-01` (`PT17`) y `PAT-K8S-02` (`PT18`), cerradas las dos brechas y registrados en el índice de trazabilidad de la matriz.
- [x] **H26.2** **El linter no podía verlo:** C3 solo validaba códigos `PT`. Un `PA12` en texto normativo pasaba sin control. Extendida a los códigos de brecha, con criterio afinado tras un primer intento demasiado ruidoso: no marca todo uso —`PR01`–`PR08` son principios SOLID y `PD04`–`PD06` building blocks DDD, ninguno tiene ni debe tener equivalente `PT`—, sino solo los que el tablero declara **`Pendiente`**, que es exactamente el caso defectuoso.
- [x] **H26.3** *(fallo latente del linter, encontrado al afinar C3)* **`marcar_historial()` no reconocía «Historial de revisiones» ni títulos numerados.** El historial de `GOB-BRE-001` se titula `## 10. Historial de revisiones`, así que **nunca se excluyó del análisis**: sus filas narran qué brechas se cerraron y con qué numeración de entonces, y tanto C1 como C3 las leían como citas vigentes. Los 13 avisos que motivaron la revisión del criterio eran todos falsos positivos de esa fuente. Corregido.

### Ejemplos: el manifiesto de referencia no cumplía el documento

- [x] **H26.4** **El `Deployment` de `§9.2` producía un pod que no arranca.** Declaraba `readOnlyRootFilesystem: true` sin montar `/tmp` — Spring Boot escribe ahí sus temporales y falla al iniciar. El remedio existía en la **nota 14.1**, 350 líneas más abajo, en una sección que el lector del ejemplo no tiene por qué haber visto. Le faltaban además dos de las cinco etiquetas que `§9.3` declara obligatorias en la sección inmediatamente siguiente.
- [x] **H26.5** **Los templates de backend Java eran peores que el ejemplo del lineamiento.** Su `deployment.yaml` usaba **`image: api-nombre-sistema:latest`** —el primer anti-patrón de la tabla de `§19` y prohibición expresa de `§6.3`—, y no declaraba **`securityContext`** (los cinco controles obligatorios de `§14.1`), **`resources`** (principio P6 y segundo anti-patrón de `§19`), `serviceAccountName`, ni cuatro de las cinco etiquetas. Su propio checklist `§18.2` pide «securityContext definido». Reescritos ambos templates; el `Service` se alineó al puerto nombrado `http`. Verificado: ningún manifiesto de template conserva `:latest`.

### Reglas: una regresión y dos degradaciones

- [x] **H26.6** **`§9.4.B` reintroducía el mandato de Resilience4j como quinta fuente.** Ordenaba resolver «Circuit Breaker y Reintentos mediante Resilience4j» para toda aplicación Java — exactamente lo que H2/H3 quitaron de `LIN-ARQ-001 §4.3` y H12.5 de la Plantilla de Arquitectura. `LIN-DIS-001 §6.2`, el dueño, dice que Resilience4j **no** es el estándar por defecto en Monolito Modular. Atribuía además el Retry a Resilience4j cuando el dueño usa Spring Retry. Ahora remite al dueño y conserva solo lo que sí le toca: **dónde** vive el control (en la JVM, nunca en un proxy adjunto al pod).
- [x] **H26.7** 🔴 **El propio documento dueño se contradecía a sí mismo, y en su parte más leída.** El árbol de decisión de `LIN-DIS-001 §2.1` —lo que un desarrollador consulta antes que el cuerpo normativo— decía *«MANDATORIO RESILIENCE4J: Timeouts (2s/3s), Circuit Breaker y Bulkhead»*, contra su propio `§6.2`, y publicaba umbrales fijos que `§6.1` había sustituido por una matriz por criticidad. Sobrevivió a H2, H3 y H12.5 porque esas correcciones buscaron el mandato en *otros* documentos. Corregido → `LIN-DIS-001` v0.1.6.
- [x] **H26.8** **`§15.1` y `§15.2` degradaban a «cuando aplique»** obligaciones de `LIN-OBS-001`, que es documento **vigente** — el mismo patrón de H23.2 y H24.2. `§15.2` fija además la **convención de puerto de Actuator**, que ningún documento del corpus definía pese a exigirla `LIN-API-REST-001 §9.5`: cierra la deuda que H24.5 dejó abierta, con las dos opciones válidas y la consecuencia de mezclarlas (probes en `404`, rollout que no converge).
- [x] **H26.9** Menores: `§18.2` y el Anexo A no incluían la `NetworkPolicy` que `ADR-TLS-INTERNO-001` volvió obligatoria; `§20` atribuía los patrones de resiliencia `PI06`–`PI09` a `LIN-ARQ-001` cuando su dueño es `LIN-DIS-001 §6`; y `§2` no listaba `LIN-DIS-001`, `LIN-PAT-001`, `LIN-VER-001`, `LIN-IAC-001` ni `GOB-MAT-001`.

**Estado:** `LIN-K8S-001` v0.1.14 pasa a **En revisión** — con ello dejan de estar bloqueadas las graduaciones de `LIN-SEC-APP-001` y `LIN-API-REST-001`, que dependen de su `§9.1`. También `LIN-DIS-001` v0.1.6, `LIN-PAT-001` v0.1.6, `GOB-BRE-001` v0.1.7 y `GOB-MAT-001` v0.11.0. Linter con 7 comprobaciones, en verde.


---

## H27 — Revisión de fondo de `LIN-CICD-001` (2026-08-17)

Documento de 1.432 líneas. Es el mejor estructurado del corpus en cuanto a propiedad documental —su principio P4 dice literalmente «CI/CD ejecuta controles definidos por otros lineamientos, pero no los redefine»— y precisamente por eso el defecto principal duele: **el apartado que materializa esa promesa no la cumplía**.

- [x] **H27.1** 🔴 **`§19.2` se titulaba «Criterios de bloqueo *sugeridos*» y omitía siete de los once que `LIN-TEST-001 §9` declara bloqueantes.** Faltaban: cobertura Angular bajo umbral, prueba de caracterización fallida, caracterización **faltante** antes de modificar un procedure legacy con lógica crítica, prueba de contrato fallida, E2E de *happy path* fallida, reporte de cobertura no generado, y opinión favorable de UFSD ausente cuando aplica Ethical Hacking. Otros tres figuraban degradados a «según fase». El dueño es un documento **vigente** que solo admite excepción por ADR firmado (`§9.3`), de modo que la excepción genérica del pipeline tampoco correspondía. Reescrito en tres tablas que separan lo que viene del dueño —sin escalamiento por fase— de lo propio del pipeline. **Con esto se cierra el segundo tema que H22.3 dejó abierto, y la Fase 1 no tiene ya ningún tema pendiente.**
- [x] **H27.2** **La confusión de fondo: se usaba la fase de madurez como si eximiera del requisito.** `§11` presentaba la cobertura como «Fase 2» sin umbral y el contrato como «Fase 2/3»; `§12.3` pedía «cobertura mínima» entre criterios que «escalan progresivamente». Un proyecto en Fase 1 podía concluir que no le aplican los umbrales de `LIN-TEST-001 §5.1`. Explicitado en ambos documentos: **la fase determina si el control se verifica en pipeline o a mano con evidencia en el MR, no si es exigible.**
- [x] **H27.3** **`§17.2` listaba `PQA` como ambiente de despliegue** entre DEV y QA. `ONP_PQA` es una **rama** de precalidad del modelo de promoción (`LIN-VER-001 §5`), no un destino con clúster propio — y `LIN-K8S-001 §4.4`, dueño de los ambientes, dedica una nota expresa a advertir que «no deben confundirse ni fusionarse». Sustituido por UAT/Preproducción, único ambiente adicional admitido y solo con ADR.
- [x] **H27.4** **`§13.3` era más laxo que el dueño en el hallazgo alto.** Lo trataba como «requiere remediación o plan aprobado», mientras `LIN-TEST-001 §9.2` sitúa críticos **y altos** en el mismo plano para el pase a Producción: sin subsanación ni retest aprobado por UFSD, bloquean. Un plan de remediación permite seguir trabajando, no pasar a Producción.
- [x] **H27.5** **El encabezado de `§18` arrastraba una nota editorial interna:** `## 18. IaC y Terraform en CI/CD (Validarlo con AD)`. Visible también en la tabla de contenido. Un barrido del corpus confirma que era la única de su tipo.
- [x] **H27.6** *(corrección en documento vigente)* **`LIN-TEST-001 §9` expresaba sus criterios solo en el modelo de ramas legado** (`ONP_DESA` → `ONP_QA`, `master`). Un proyecto nuevo bajo GitLab Flow simplificado —el modelo objetivo de `LIN-VER-001 §6`, sin ramas `ONP_*`— podía leerse fuera de alcance del documento que fija los criterios de aceptación de todo el corpus. Reexpresados por **promoción de ambiente**, válida en ambos modelos → `LIN-TEST-001` v0.1.4, sigue **Vigente**.

**Estado:** `LIN-CICD-001` v0.1.6 pasa a **En revisión**; `LIN-TEST-001` v0.1.4; `GOB-MAT-001` v0.12.0. Linter en verde.


---

## H28 — Revisión de fondo de `LIN-BUS-001` (2026-08-17)

Documento de 1.044 líneas y de las mejores del corpus en propiedad documental: `§7.3` ya remite el DDL de `EVT_OUTBOX` a su dueño en vez de copiarlo, y `§4.3` argumenta con honestidad cuándo **no** usar el bus. Los hallazgos son técnicos, y el principal está en el código que se copia.

- [x] **H28.1** 🔴 **El ejemplo de consumidor de `§8.3` perdía mensajes en silencio.** Capturaba el error recuperable, omitía deliberadamente el `acknowledge` y anotaba *«Kafka reintentará desde el mismo offset»*. Con `ack-mode: MANUAL` eso **no es lo que ocurre**: omitir el `acknowledge` no provoca reentrega — el contenedor continúa con el registro siguiente y, en cuanto uno posterior se confirma, el offset avanza por encima del fallido. El mensaje no se reintenta: se pierde, y solo reaparecería tras un rebalanceo o reinicio. Es exactamente el fallo que `§16` enumera como anti-patrón (*«Ignorar el DLQ — pérdida silenciosa de eventos de negocio»*), reproducido en el ejemplo canónico. Reescrito para que las excepciones se propaguen al `DefaultErrorHandler`.
- [x] **H28.2** **Dos mecanismos de DLQ compitiendo.** El mismo ejemplo implementaba a mano un `enviarADlq`, mientras `§8.6` define el `DefaultErrorHandler` + `DeadLetterPublishingRecoverer` como mecanismo estándar — y hasta advertía que *«el método `enviarADlq` del ejemplo 8.3 queda cubierto por este bean»*. La advertencia existía; el ejemplo defectuoso seguía ahí, y es lo que se copia. Eliminado el mecanismo manual.
- [x] **H28.3** **`§7.2` fijaba `retries: 3` y con ello anulaba el propósito de `acks: all`.** Con `enable.idempotence: true` el productor usa `Integer.MAX_VALUE` por defecto y la ventana real la acota `delivery.timeout.ms` (120 s). Fijar tres intentos hace que el envío se abandone ante una indisponibilidad transitoria del líder aunque queden más de dos minutos de plazo: se paga el coste de esperar la confirmación de las réplicas y luego se renuncia antes de tiempo. Eliminado.
- [x] **H28.4** **`§6.1` exigía «notación kebab-case» y ninguno de sus ejemplos lleva guion.** Bajo esa regla `ciclovida` tendría que ser `ciclo-vida`. La convención real —tres segmentos separados por punto, cada uno una sola palabra— quedó descrita como es.
- [x] **H28.5** **Discrepancia de namespaces entre dos documentos.** `LIN-K8S-001 §4.4` atribuía a `LIN-BUS-001` los namespaces `kafka-dev`/`kafka-qa`/**`kafka`**, cuando `LIN-BUS-001 §12.1` usa `kafka-prod`. La verificación de fondo —si el sufijo de ambiente se justifica por vivir Kafka en un clúster de plataforma compartido— **sigue abierta con Plataforma**, pero ahora ambos documentos parten del mismo dato y `LIN-BUS-001` deja constancia de la pregunta pendiente. → `LIN-K8S-001` v0.1.15.
- [x] **H28.6** Menores: el log de evento de `§11.2` omitía `span.id` pese a exigirlo `LIN-OBS-001 §6`; y `§2` no listaba `LIN-DIS-001` —cuyo `§2.3` es el prerrequisito declarado en `§1.3`—, `LIN-PAT-001`, `LIN-VER-001`, `GOB-MAT-001` ni **`ADR-CLOUDEVENTS-001`**, que es la decisión que sustenta el envelope de `§5.2`: el documento adoptaba el estándar sin citar su propio registro de decisión.

**Estado:** `LIN-BUS-001` v0.1.7 pasa a **En revisión**; `LIN-K8S-001` v0.1.15; `GOB-MAT-001` v0.13.0. Linter en verde.


---

## H29 — Revisión de fondo de `LIN-FE-ANG-001` (2026-08-17)

Documento de 1.331 líneas, muy sólido en la parte propia de Angular — la bifurcación de `§9.3` entre escenario con y sin BFF está bien argumentada. Los tres hallazgos están en las fronteras con otros documentos.

- [x] **H29.1** 🔴 **`§16.4` desactivaba un control de seguridad obligatorio de su documento dueño.** Declaraba `readOnlyRootFilesystem: false` con la nota «nginx necesita escribir en /tmp». La necesidad es real, pero `LIN-K8S-001` nota 14.1 prohíbe expresamente esa salida: *«no usar `readOnlyRootFilesystem: false` como solución general; identificar los directorios que necesitan escritura y montarlos explícitamente»*. Lo llamativo es que el propio documento **ya sabía dónde escribe nginx**: su `§16.5` lista como anti-patrón omitir `client_body_temp_path` en `/tmp`. Faltaba solo montar el volumen. Se dejaba el sistema de archivos completo escribible para evitar un `emptyDir` de 16 MB. Corregido, con `capabilities: drop: ALL` añadido y el `securityContext` marcado como obligatorio en vez de «recomendado».
- [x] **H29.2** 🔴 **`§9.6` clasificaba `200` como código de éxito.** La tabla lo listaba junto a `000` en la fila «Éxito» y, dos filas más abajo, dentro del rango `200`–`203` de error de negocio. Un componente que siguiera la primera lectura trataría un HTTP **422 — regla de negocio no cumplida** como operación exitosa: navegaría al listado mostrando confirmación de algo que el backend rechazó. En un sistema previsional eso es una confirmación falsa al ciudadano. El código de éxito con advertencias es `001`. Incorporado también `302`/429, ratificado en H24.
- [x] **H29.3** **`§14.1` publicaba su propio umbral de cobertura** — «70% de líneas» — cuando `LIN-TEST-001 §5.2`, dueño y **vigente**, define tres: *statements* ≥70%, *branches* ≥65% y *functions* ≥70%. «Líneas» ni siquiera es una de ellas. Un proyecto podía alcanzar 70 % de líneas con 40 % de *branches*, sin ejercitar la mitad de los caminos condicionales, y considerarse conforme. Es la quinta repetición del mismo patrón en el corpus (H13.1, H23.2, H26.8, H27.2).
- [x] **H29.4** **`§9.4`** no contemplaba el 429, y su interceptor tampoco advertía contra el reintento automático —que agravaría la saturación que disparó el límite—. Corregido.
- [x] **H29.5** **`error_log` de nginx apuntaba a `/var/log/nginx/error.log`** en las **tres** copias del corpus (lineamiento, `LIN-K8S-001` Anexo D y el template). Incompatible con el `readOnlyRootFilesystem: true` que ahora se exige y con la regla de logs a stdout/stderr de `LIN-K8S-001 §15.1`. Apuntan las tres a `/dev/stderr`. → `LIN-K8S-001` v0.1.16.
- [x] **H29.6** **Cierra H24.12: el `k8s/base/` del template Angular ya no está roto.** Su `kustomization.yaml` declaraba `deployment.yaml`, `service.yaml` y `configmap.yaml`, ninguno de los cuales existía — un `kustomize build` fallaba. Creados los tres más la `NetworkPolicy`, todos conformes a lo corregido en H26 y H29: tag explícito, `securityContext` completo, `resources`, las cinco etiquetas, puerto 8080 (la imagen unprivileged no puede bindear < 1024) y `emptyDir` en `/tmp`. Verificado que cada archivo declarado en el `kustomization` existe.

**Estado:** `LIN-FE-ANG-001` v0.1.3 pasa a **En revisión**; `LIN-K8S-001` v0.1.16. Linter en verde.


---

## H30 — Revisión de fondo de `LIN-PERF-001` (2026-08-17)

Documento de 1.013 líneas, con buena disciplina de propiedad documental: su `§16` delimita con claridad qué le toca a él y qué a `LIN-CICD-001`. Dos hallazgos, y el segundo importa más allá de este lineamiento.

- [x] **H30.1** **`§8.3` era la tercera lista de Core Web Vitals del corpus, sin umbrales y bajo la fórmula «cuando aplique».** `LIN-ARQ-001 §7.2` los declara **gate de bloqueo mandatorio** para promover un build a producción y publica siete umbrales; `LIN-FE-ANG-001 §15.2` reproducía cuatro sin declarar dueño; y este documento listaba tres sin ningún valor. Es el patrón que ya divergió tres veces con los timeouts (H2, H3, H12.5). Corregido en ambos: `LIN-PERF-001` remite al dueño y conserva lo que sí le es propio —medir el frontend **con el backend bajo la carga del escenario**, que es distinto del Lighthouse de laboratorio del gate—, y `LIN-FE-ANG-001` marca su tabla como referencia de trabajo, no como fuente. La fila de la matriz, además, **era la única del catálogo sin sección de referencia** y omitía a `LIN-PERF-001` como consumidor.
- [x] **H30.2** 🔴 **`§11` regulaba el contenido de los scripts y dejaba abierta la puerta principal.** Admitía datos personales reales *«sin autorización»* —es decir, con autorización sí— y su apartado de datos sensibles enumeraba lo que un script no debe contener. Pero el riesgo real de una prueba de volumen no está en el script: está en **restaurar un respaldo de producción en el ambiente de pruebas** para tener datos realistas. Para la ONP eso traslada el padrón de afiliados completo —DNI, nombres, montos de pensión, historial de aportes— a un entorno con controles más débiles, y ningún control sobre el texto del script lo impide. Ahora se exige **enmascaramiento previo e irreversible** antes de que el respaldo sea accesible desde el destino, con tabla de tratamiento por tipo de dato, y se deja explícito que la autorización de Seguridad **condiciona** el enmascaramiento, no lo sustituye.
- [x] **H30.3** `§2` no listaba `LIN-DIS-001` —cuya matriz de timeouts de `§6.1` es la referencia para interpretar la latencia observada—, `LIN-BUS-001`, `GOB-MAT-001` ni la **Ley N.° 29733**, pese a que `§11` trata datos personales.

**Sobre H11.3.** Esta revisión cubre el tratamiento de datos para **pruebas de rendimiento**, que es donde el volumen hace tentador el atajo. La política transversal de datos de prueba y enmascaramiento para todos los ambientes no productivos **sigue pendiente** y corresponde a `LIN-SEC-APP-001` como dueño de la protección de datos.

**Estado:** `LIN-PERF-001` v0.1.3 y `LIN-FE-ANG-001` v0.1.4 pasan a **En revisión**; `GOB-MAT-001` v0.14.0. Linter en verde.


---

## H31 — Revisión de fondo de `GOB-PLA-001` (Plantilla de Documento de Arquitectura) — 2026-08-17

Documento de 723 líneas y **Vigente**. Su calidad pedagógica es alta —los bloques de orientación explican bien la diferencia entre riesgo y deuda técnica, o entre atributo de calidad y requisito funcional—, pero es la plantilla que las fábricas rellenan: **sus ejemplos se copian literalmente**, y dos de ellos eran técnicamente falsos.

- [x] **H31.1** 🔴 **El ejemplo de Seguridad describía el token SAA como JWT.** `C.1` proponía *«WAF + JWT vía SAA/token para APIs internas»*, cuando `LIN-API-REST-001 §7.1` afirma expresamente lo contrario: el token SAA es **opaco**, no autocontenido y **no verificable localmente** — de ahí que exista el `SaaTokenValidationFilter` que llama a SAA en cada petición. Un arquitecto que copiara el ejemplo diseñaría validación local de un token que no la admite, y el error solo aparecería al integrar. Corregido con la referencia al filtro y al modelo de permisos.
- [x] **H31.2** **El mismo cuadro trataba `codDetRespuesta` como un header HTTP.** Es un campo del cuerpo de `ApiResponseWrapper` (`LIN-API-REST-001 §4.1`). Propagarlo como header no solo es incorrecto: sugiere una vía de correlación que no existe.
- [x] **H31.3** 🟠 **La fila de Recuperabilidad exige RTO/RPO sin que exista lineamiento al que remitirse.** Es la manifestación más concreta de **H11.2** encontrada hasta ahora: la plantilla —documento **Vigente**— obliga al arquitecto a declarar RTO y RPO, y el corpus no norma respaldo, recuperación ante desastres ni valores institucionales. No se puede cerrar la brecha desde aquí, pero sí evitar que cada proyecto invente cifras: se instruye acordarlas con el área usuaria y Plataforma, sustentarlas en un ADR y **registrar la ausencia de norma institucional como riesgo en `D.1`**.<br>**Evidencia acumulada:** `GOB-PLA-001 C.1` —plantilla **Vigente**— obliga a declarar RTO/RPO sin norma a la que remitirse (H31.3), y `LIN-K8S-001 §13.3` exige «política de backup» para todo PVC sin definirla en ninguna parte. La brecha ya está produciendo exigencias huérfanas en documentos vigentes.
- [x] **H31.4** **`D.2` citaba «Feature Toggles (PA14)»** — código del tablero de brechas, no del catálogo normativo. Mismo defecto que H26.1 con `PA12`/`PA13`. Redirigido a `LIN-ARQ-001 §2.3` y `ADR-014`, precisando además que de las cuatro categorías solo *Release* y *Experiment* caducan obligatoriamente. *(A diferencia de H26, aquí no se creó ficha `PT`: el tablero da `PA14` por documentado y no hay contradicción. Queda como candidato dar código y ficha al patrón Feature Toggle, que hoy es el único normado en Nivel 1 sin entrada en `LIN-PAT-001`.)*
- [x] **H31.5** **`B.1` no distinguía `AD-XXX` de `ADR-XXX`.** La plantilla instruye declarar en cada decisión si es *«EXCEPCIÓN a LIN-XXX»*, pero no aclaraba que una decisión de proyecto **no puede dispensar del cumplimiento de un lineamiento institucional**: eso se eleva al Comité y se registra como `ADR-`. Sin la distinción, un proyecto podía documentar su propia excepción y considerarse en regla.
- [x] **H31.6** *(hallazgo derivado, de alcance mayor que la plantilla)* **Había dos registros de ADR desconectados.** El **Apéndice A de `LIN-ARQ-001`** lleva la matriz institucional (`ADR-001`…`ADR-014`) y, en paralelo, existen tres ADR como documento propio. `ADR-013` y `ADR-CLOUDEVENTS-001` resultaron ser **la misma decisión** —misma fecha, mismo asunto— sin referenciarse entre sí, mientras `ADR-WSO2-001` y `ADR-TLS-INTERNO-001` no figuraban en la matriz pese a ser decisiones institucionales vigentes. Declarada la matriz como registro único, incorporados como `ADR-015` y `ADR-016`, y cada ADR en archivo declara ahora su identificador de matriz. → `LIN-ARQ-001` v0.1.11, cuyo historial estaba además desordenado.

**Estado:** `GOB-PLA-001` v2.3 (sigue **Vigente**), `LIN-ARQ-001` v0.1.11, `GOB-MAT-001` v0.15.0. Linter en verde.


---

## H32 — `LIN-IAC-001` y `LIN-BI-001`: cierre de la lectura del corpus (2026-08-17)

Los dos últimos documentos en `Borrador`. **Con esta revisión ningún lineamiento del corpus queda en Borrador y se cierra H14.6**, la lectura de contenido de los 19 documentos que se abrió al detectarse el sesgo de cobertura.

### `LIN-IAC-001` → v0.1.3

- [x] **H32.1** 🔴 **No se normaba el respaldo ni la recuperación del estado de Terraform.** El `tfstate` es el activo más crítico del repositorio de IaC, por encima del código: el código se reescribe, pero el estado es lo único que mapea cada recurso declarado con el recurso real. Perderlo deja a Terraform sin ese mapeo, y el siguiente `apply` puede **recrear infraestructura que ya existe o destruir la que no reconoce**. El documento decía dónde se guarda —backend HTTP de GitLab— y nada sobre versionado, respaldo, retención ni recuperación. Nueva `§6.3`: versionado verificado antes de graduar a Fase 2, respaldo externo vía `terraform state pull` en el pipeline de drift, retención de 90 días para `prod`, **prueba de restauración anual** —un respaldo cuya restauración nunca se probó no es un respaldo— y control del `force-unlock`.
- [x] **H32.2** **Dos modelos de madurez con la misma nomenclatura.** `LIN-IAC-001` numera «Fase 0…5» y `LIN-CICD-001` «Fase 0…7»; ambos se citan mutuamente sin calificar la escala. `LIN-CICD-001 §18.2` dice que el `apply` va en «Fase 4/6» — imposible saber de qué escala habla, y la Fase 5 de IaC (GitOps) no equivale a la Fase 5 de CI/CD (seguridad dinámica). Toda cita queda calificada, con advertencia explícita en `§4`.
- [x] **H32.3** `§10.3` exigía documentar un cambio manual de emergencia «en un ADR» — ver H32.5.

### `LIN-BI-001` → v0.1.3

- [x] **H32.4** 🔴 **El enmascaramiento PII protegía solo al consumidor analítico.** `§8.3` exigía *dynamic data masking* en Trino para la capa Gold. Pero **Bronze es por diseño una réplica cruda e inalterada de las bases transaccionales**: el padrón de afiliados completo con DNI, nombres, domicilios, montos de pensión e historial de aportes — y `§8.2` autoriza a Ingeniería de Datos a leerla entera. Proteger Gold y dejar Bronze abierta es proteger la copia transformada del dato mientras el original queda accesible. Nueva `§8.3.1`: clasificación PII **desde la ingesta** (sin ella no se promueve a Silver), cifrado en reposo de los tres buckets, acceso nominal auditado, retención declarada por dominio y enmascaramiento previo obligatorio al replicar hacia ambientes no productivos.
- [x] **H32.5** **`§1.3` apoyaba todo el stack homologado en un documento sin código y externo al corpus** — el «Lineamiento de Estándares de Tecnología v2.0», citado como `N/A`. No está sujeto al ciclo de vida documental ni al linter, y sus cambios no se propagan. Declarado su estatus explícitamente; incorporados además `LIN-PAT-001` (ficha `PAT-BI-01`/`PT16`, que el documento no citaba pese a normar Medallion), `LIN-BUS-001`, `LIN-K8S-001`, `LIN-CICD-001`, `LIN-OBS-001` y `GOB-MAT-001`.

### Hallazgo transversal — tres instrumentos llamados «ADR»

- [x] **H32.6** **Doce de trece documentos usaban el ADR como instrumento de excepción de proyecto.** Solo `LIN-VER-001 §24.2` había definido un identificador propio (`EXC-VER-NNN`); el resto titula su apartado «Proceso ADR para excepciones» sin identificador. Tras H31.6 —que estableció `ADR-NNN` como registro **institucional** del Comité— eso dejaba un vacío: la excepción de un proyecto no cabe en el registro institucional (sería absurdo llevar allí cada desviación de cada sistema) y tampoco es un `AD-NNN` de diseño, porque H31.5 estableció que un `AD-NNN` no dispensa de un lineamiento. Normados los tres instrumentos en `GOB-MAT-001` con su alcance, aprobador y ubicación. `LIN-IAC-001` y `LIN-BI-001` ya adoptan `EXC-<CÓDIGO>-NNN`.
- [x] **H32.7** **Cerrado en H38.** Los once lineamientos restantes usan `EXC-<SUFIJO>-NNN`, con tabla de sufijos en `GOB-MAT-001` para que el identificador no quede a criterio de cada equipo.

**Estado:** `LIN-IAC-001` v0.1.3 y `LIN-BI-001` v0.1.3 pasan a **En revisión**; `GOB-MAT-001` v0.17.0. **Ningún lineamiento del corpus queda en `Borrador`.** Linter en verde.


---

## H33 — Continuidad operativa: cierre de H11.2 (2026-08-17)

La mayor brecha de contenido del corpus. **Decisión de Arquitectura (2026-08-17):** el contenido va íntegro a `LIN-ARQ-001 §5.4`, sin crear documento nuevo. *(Se planteó la alternativa de un `LIN-DRP-001` transversal, argumentando que el Nivel 1 exige resultados y delega mecanismos; Arquitectura optó por no ampliar el mapa documental. La sección se estructuró para acotar el efecto: la política por componente **delega el mecanismo** a cada dueño en vez de reproducirlo.)*

### El diagnóstico era menos grave y más preciso de lo registrado

- [x] **H33.1** **No era cierto que el corpus no normara nada.** `LIN-BD-ORA-001 §11.2` ya tenía respaldo de Oracle bien normado —RMAN, completo semanal con 4 semanas de retención, incremental diario, archive logs continuos y **prueba de restauración semestral**— y `LIN-IAC-001 §6.3` acababa de cubrir el estado de Terraform. Lo que faltaba era lo que hacía inaplicables esas piezas: **una clasificación institucional de criticidad con RTO/RPO objetivo**. Los tres documentos que tocaban el tema remitían a «acordarlo con el área de negocio», de modo que cada proyecto inventaba cifras, no había forma de evaluar un TDR y Plataforma no podía dimensionar.

### Lo que se incorporó

- [x] **H33.2** **Tres bandas de criticidad con RTO/RPO objetivo** (`§5.4.1`), reutilizando la escala Alta/Media/Baja que `LIN-PERF-001 §9.3` ya usaba — **no se creó una cuarta clasificación**. Alta: RTO ≤ 4 h, RPO ≤ 15 min (cálculo y pago de pensiones). Media: ≤ 8 h / ≤ 1 h (afiliación, expedientes, ventanilla). Baja: ≤ 24 h / ≤ 24 h.
- [x] **H33.3** 🔑 **La regla que más valor aporta: un sistema no puede comprometer un RTO/RPO mejor que el de sus dependencias.** Si el cálculo de pensión se apoya en una base con RPO de 1 hora, su RPO real es 1 hora por mucho que declare 15 minutos. La verificación es obligatoria al declarar el atributo en `GOB-PLA-001 C.1`. Sin esta regla, los RTO/RPO declarados serían aspiraciones no verificables.
- [x] **H33.4** **Política de respaldo por componente** (`§5.4.2`) que delega el mecanismo a cada dueño y señala los puntos ciegos: la retención de un tópico Kafka **no es un respaldo** sino una ventana de reproceso; Bronze del Lakehouse es reconstruible pero **el tiempo de reconstrucción es el RTO real** del BI; y un respaldo de datos sin los secretos para levantar el servicio no permite recuperar.
- [x] **H33.5** **Recuperación a nivel de sistema** (`§5.4.3`): respaldar componentes no es recuperar un servicio. Obligatorio para criticidad Alta y Media declarar orden de recuperación y dependencias, comportamiento esperado de las dependencias fuera del control de la ONP (SAA, RENIEC, PIDE), punto de recuperación verificable y responsable.
- [x] **H33.6** **Régimen de pruebas de restauración obligatorio** (`§5.4.4`), con una exigencia que cambia su naturaleza: la prueba debe **medir el tiempo real de recuperación** y contrastarlo con el RTO declarado. *Un RTO declarado que nunca se midió es una aspiración, no un compromiso.*

### Propagación

- [x] **H33.7** Cerradas las tres exigencias huérfanas que motivaron el hallazgo: `GOB-PLA-001 C.1` ya remite al dueño y precisa qué declarar; `LIN-K8S-001 §13.3` deriva su «política de backup» de la banda de criticidad; `LIN-IAC-001 §6.3` actualiza su nota de brecha abierta.
- [x] **H33.8** **`LIN-BD-ORA-001 §11.2` recibió una advertencia que no tenía:** sus mínimos de respaldo **no satisfacen un RPO de 15 minutos**, que es el de la banda Alta. Una base que soporte cálculo o pago de pensiones necesita archive logs más frecuentes o replicación, y no puede acogerse solo a esos mínimos. Es el tipo de desalineamiento que solo aparece al confrontar dos documentos.
- [x] **H33.9** **La escala de criticidad pasa a tener dueño.** `LIN-PERF-001`, `LIN-CICD-001` y `LIN-K8S-001` usaban Alta/Media/Baja sin fuente común. `LIN-ARQ-001 §5.4.1` define las bandas; `LIN-PERF-001 §6.4` conserva cómo se determinan.

### Lo que queda fuera de mi alcance

- [ ] **H33.10** 🔴 **Ratificar los valores de RTO/RPO.** Están marcados en el documento como **propuesta técnica**. RTO y RPO no son decisiones de arquitectura: expresan cuánto puede estar caído un servicio y cuánta información puede perderse, y eso lo decide el negocio. Requiere **Comité de Arquitectura con Pensiones, Aportes y Atención al Ciudadano**, y validación de Plataforma sobre viabilidad. Mientras no se ratifiquen rigen como valores por defecto.

**Estado:** `LIN-ARQ-001` v0.1.12, `GOB-PLA-001` v2.4, `LIN-BD-ORA-001` v0.1.14, `LIN-K8S-001` v0.1.17, `LIN-IAC-001` v0.1.4, `LIN-PERF-001` v0.1.4, `GOB-MAT-001` v0.18.0. Linter en verde.


---

## H34 — `GOB-PLA-001`: de guía de redacción a instrumento verificable (2026-08-17)

Evaluación de la plantilla **como instrumento normativo**, no como texto. El diagnóstico: es un excelente documento pedagógico —26 bloques de orientación que anticipan el error real, no lo obvio— y una plantilla normativa incompleta. **Enseñaba a redactar y no permitía verificar lo redactado**, siendo el artefacto que las fábricas entregan y Arquitectura aprueba.

- [x] **H34.1** 🔴 **No tenía criterios de aceptación.** Existía el campo «Aprobado por» y nada que le dijera al aprobador qué verificar; la aprobación dependía del criterio de quien firmara, que es justo lo que un marco normativo debe eliminar. Siete lineamientos del corpus tienen «Checklist de conformidad»; la plantilla no. Nuevo **Anexo E** con 23 criterios verificables agrupados en completitud, declaraciones obligatorias, conformidad normativa y consistencia interna, más registro de la revisión. Es el **único anexo que no se elimina** del entregable: queda como evidencia. Incluye regla de bloqueo — criticidad Alta no aprueba con ítems pendientes; los ítems 9, 14 y 17 bloquean en toda criticidad.
- [x] **H34.2** 🔴 **Omitía cuatro declaraciones que el corpus exige.** No pedía el **Estadio** del sistema —clasificación primaria de `LIN-ARQ-001 §2.1`, de la que cuelgan los 6 criterios de microservicio—, ni la **declaración CAP** que `§3.1` hace obligatoria para todo módulo distribuido, ni la evaluación de **DDD** de `LIN-DIS-001 §3.0`, ni la **Declaración de Conformidad del `README.md`**. Esta última es la más grave en la práctica: `LIN-CICD-001 §12.5` **la verifica en el pipeline y bloquea el pase**, de modo que un arquitecto podía completar la plantilla entera y correctamente y aun así fallar el gate sin haberse enterado de que la obligación existía. Nueva `§1.4`.
- [x] **H34.3** **`§5.2` listaba 6 de 19 documentos del corpus** — omitiendo `LIN-K8S-001` pese a ser Kubernetes el destino mandatorio, y `LIN-TEST-001` y `LIN-VER-001`, ambos con obligaciones exigibles. Sustituida por el corpus completo con su estado, la advertencia de qué significa `Vigente` frente a `En revisión` para un TDR, y la exigencia de declarar **por qué** un documento no aplica en vez de omitirlo en silencio.

**Dos hallazgos de diseño quedan a decisión de Arquitectura, no corregidos:**

- [x] **H34.4** **Resuelto (2026-08-18) haciendo `§3` derivado, no eliminándolo.** Suprimirlo habría dejado sin lectura a Gerencia y Stakeholders, que la propia tabla de audiencias dirige allí y que no leen ArchiMate. El **Anexo A pasa a ser la fuente autoritativa** —es el modelo mantenido en Archi, versionable y con niveles de abstracción explícitos— y `§3` queda como lectura derivada con tres reglas: ante divergencia prevalece el Anexo A; `§3` **no puede contener ningún elemento ausente del modelo** (la vía por la que ambas representaciones divergen es la adición, no la contradicción); y se actualiza siempre después. Resuelto además el caso de la **Capa de Seguridad**, única sin vista propia en el Anexo A: sus elementos viven distribuidos entre las vistas de Contexto e Infraestructura, y `§3.B` agrupa esa lectura. El ítem 18 del Anexo E se corrigió para verificar en **un solo sentido** — el modelo puede tener detalle que la narrativa no recoja, nunca al revés.
- [x] **H34.5** **Resuelto (2026-08-18) con línea base declarada y disparadores de revisión.** La tabla de identidad incorpora **línea base normativa** —la versión de `GOB-MAT-001` consultada— y **próxima revisión** a 12 meses o menos. La nueva `§1.5` define cinco disparadores de revisión obligatoria, siendo el de mayor impacto que **un documento del que el sistema depende gradúe a `Vigente`**: en ese momento lo que era criterio técnico pasa a ser exigible contractualmente. Igual de importante es lo que se declaró que **no** obliga —erratas, cambios en documentos no aplicables, versiones que no alteran una regla invocada—, porque sin ese límite la regla degeneraría en revisar por cada parche y se incumpliría por inaplicable. La regla marco quedó en `GOB-MAT-001`, dueño del ciclo de vida, con la obligación de que **Arquitectura OTI comunique cada graduación**.

**Dato de composición:** 733 líneas, de las cuales 292 eran orientación (40%) y 108 *placeholders*. La plantilla estaba optimizada para quien escribe, no para quien revisa — de ahí H34.1 y H34.2.

**Estado:** `GOB-PLA-001` v2.5 (sigue **Vigente**), `GOB-MAT-001` v0.19.0. Linter en verde; anclas del índice verificadas.


---

## H35 — Grafo de servicios y arquitectura observada (2026-08-18)

Propuesta de Arquitectura (Carlos Ormeño): incorporar el *service graph* al corpus aprovechando OTEL y las trazas existentes. Se adoptó con **propiedad dividida** — mecanismo en `LIN-OBS-001 §5.8`, gobierno en `LIN-ARQ-001 §5.5` — porque son dos temas con dueños naturalmente distintos.

### Por qué aporta

- [x] **H35.1** **Cierra un vacío de verificación que la revisión de `GOB-PLA-001` había dejado abierto.** El Anexo A es una arquitectura **declarada**: refleja lo que el arquitecto dibujó en Archi, no lo que el sistema hace. El corpus no tenía forma de detectar la deriva entre ambas. El grafo derivado de trazas provee la topología **observada**, y contrastarla es a la arquitectura lo que el `terraform plan` programado de `LIN-IAC-001 §10` es a la infraestructura.
- [x] **H35.2** **Refuerza la regla de dependencias de H33.3, cuya verificación era puramente documental.** «Un sistema no puede comprometer un RTO/RPO mejor que el de sus dependencias» dependía de que el arquitecto recordara todas sus dependencias. Ahora una dependencia observada y no declarada **invalida** esa verificación. `LIN-ARQ-001 §5.5.1` fija cinco contrastes obligatorios semestrales para criticidad Alta y Media: dependencias no declaradas, integridad del inventario de recuperación, **elusión del ACL**, servicios fuera del catálogo de `LIN-API-REST-001 §10.1` y exposición sin gateway.
- [x] **H35.3** **Regla de tratamiento explícita:** ante una dependencia observada y no declarada se corrige el documento **o el código** — nunca se normaliza la desviación por el hecho de estar en producción. Sin esa regla, el contraste degeneraría en actualizar el diagrama para que coincida con lo que haya.

### El hallazgo técnico que condicionaba todo

- [x] **H35.4** 🔑 **Con el `sampling 0.1` de `LIN-OBS-001 §4.5`, un grafo ingenuo habría sido engañoso.** Un endpoint de alto tráfico aparece siempre; **un batch nocturno hacia SUNAT tendría un 10% de probabilidad de aparecer**. Una arista ausente sería ambigua entre «no existe» y «se descartó» — y las dependencias de baja frecuencia son justamente las que nadie recuerda y las que rompen un plan de recuperación. **Regla incorporada:** el conector `servicegraph` se sitúa en el pipeline que recibe **todos** los spans y el muestreo se aplica solo en el exportador hacia Jaeger. El coste de almacenamiento no aumenta, porque el grafo produce métricas agregadas. Con el stack actual —Collector, Prometheus, Grafana— **no hace falta añadir Tempo ni ningún componente**.

### Límites declarados, para que no se le pida lo que no puede dar

- [x] **H35.5** **No ve las fronteras internas del Monolito Modular.** Las llamadas entre módulos ocurren en el mismo proceso y no generan spans. Como el Estadio 2 es la topología por defecto, el grafo de un sistema típico de la ONP tendrá pocos nodos internos y muchas aristas externas — ahí está su valor, no en el interior.
- [x] **H35.6** **No sustituye al análisis estático.** Las importaciones entre fronteras prohibidas de `LIN-DIS-001 §3.4` son una propiedad del código, no del tráfico. `LIN-CICD-001 §12.5` ya reconocía que el pipeline no puede validar el contenido de la Declaración de Conformidad; el grafo tampoco.
- [x] **H35.7** **No es un quinto catálogo.** El corpus ya mantiene cuatro registros declarativos —bases de datos y PL/SQL, servicios REST, tópicos de eventos y datasets en OpenMetadata—. El grafo es la contraparte **observada** que los valida, no otro inventario que mantener a mano.

### Deuda que este trabajo dejó a la vista

- [x] **H35.8** **Cerrado en H37.** `LIN-DEV-JAVA-001 §15.5` normaliza las pruebas de arquitectura con ArchUnit; nuevo tipo `AT` en `LIN-TEST-001` y criterio de bloqueo en `LIN-CICD-001 §19.2`.

**Estado:** `LIN-OBS-001` v0.1.5 (sigue **Vigente**), `LIN-ARQ-001` v0.1.13, `GOB-PLA-001` v2.7, `GOB-MAT-001` v0.21.0. Linter en verde — de hecho **C1 detectó la referencia adelantada a `LIN-ARQ-001 §5.5`** mientras aún no existía, que es exactamente para lo que se construyó.


---

## H36 — Identidad de los nodos del grafo (2026-08-18)

Surge de una pregunta de Arquitectura al revisar H35: *«¿no hay que validar que las APIs expongan el grafo en sus metadatos, para obtener la información de manera dinámica?»*. La respuesta directa es **no** —el grafo se deriva de las trazas, no de los contratos; pedirlo vía OpenAPI sería el quinto catálogo manual que H35.7 descartó, y además OpenAPI declara lo que una API *ofrece*, no lo que *consume*—. Pero la pregunta apuntaba a un problema real de metadatos, desplazado del contrato a la telemetría, y **la verificación lo confirmó**.

- [x] **H36.1** 🔴 **El corpus tenía tres convenciones para el mismo identificador.** `LIN-OBS-001 §5.7` ilustraba `service.name` como `onp-<sistema>-<modulo>`; `LIN-VER-001 §9.1` nombra el proyecto GitLab `<sistema>-<tipo-componente>`; `LIN-K8S-001 §9.3` usa `app.kubernetes.io/name` con otra forma. El grafo agrupa nodos por `service.name`, de modo que **un nodo observado no se podía casar automáticamente** con su entrada en el catálogo de servicios ni con su Deployment — y las cinco verificaciones de `LIN-ARQ-001 §5.5.1` que acabábamos de normar habrían sido manuales y frágiles. Se adopta como canónica la forma del proyecto GitLab y se **elimina el prefijo `onp-`**, redundante dentro de la institución. Nuevo tema en la matriz con dueño `LIN-VER-001`.
- [x] **H36.2** **Los servicios externos no tenían nombre lógico.** RENIEC, SUNAT, PIDE y SAA no emiten trazas: aparecen solo por los atributos del span del cliente. El corpus definía `net.peer.name` (el host) pero no `peer.service`, así que RENIEC figuraría como `api.reniec.gob.pe` y una entidad con varios hosts o balanceadores produciría **varios nodos distintos** — arruinando justamente el inventario de dependencias externas, que es donde vive el riesgo de la ONP y lo que H33.3 obliga a verificar. Añadido `peer.service` como atributo obligatorio en clientes salientes, con lista cerrada: `reniec`, `sunat`, `sbs`, `mef`, `pide`, `saa`, `wso2`.

**Por qué importa el orden:** sin H36, el grafo de H35 se habría configurado y habría producido un mapa visualmente correcto pero **no reconciliable** con los registros del corpus. Es el tipo de defecto que solo aparece al preguntarse cómo se automatiza la verificación, no al diseñar la captura.

**Estado:** `LIN-OBS-001` v0.1.6 (sigue **Vigente**), `GOB-MAT-001` v0.22.0. Linter en verde.


---

## H37 — Pruebas de arquitectura: el Monolito Modular pasa a ser verificable (2026-08-18)

Cierra **H35.8**, la deuda que dejó a la vista el trabajo del grafo de servicios. Es el hallazgo de gobierno más incómodo de todo el ejercicio: **la topología por defecto de la ONP descansaba entera en la palabra de una persona.**

### El vacío

- [x] **H37.1** 🔴 **El único control de las fronteras del Monolito Modular era una declaración jurada, sin verificación de ninguna clase.** `LIN-ARQ-001 §8.3` numeral 4 exige al Tech Lead certificar en el `README.md` la ausencia de importaciones entre fronteras prohibidas (`LIN-DIS-001 §3.4`). Los tres controles del corpus que podrían comprobarlo, no podían: `LIN-CICD-001 §12.5` **lo admitía por escrito** —solo verifica que el texto exista—, el grafo de servicios no lo ve porque las llamadas entre módulos son in-process (H35.6), y Maven tampoco, por la razón que importa: **basta añadir la dependencia al `pom.xml` para que el compilador la acepte**. La frontera entre Bounded Contexts es una decisión de diseño, no una barrera técnica.

### La solución

- [x] **H37.2** **`LIN-DEV-JAVA-001 §15.5` normaliza las pruebas de arquitectura con ArchUnit**, escritas contra la estructura real de los templates (`pe.gob.onp.<sistema>.<módulo>.<capa>`, módulos `onp-<modulo>-{api,application,domain,infrastructure,messaging}` y `onp-common-domain`). Seis reglas mínimas: tres de gobierno del Shared Kernel —sin `@Entity`, sin `*Service` de negocio, sin puertos ni clientes—, una de **aislamiento entre Bounded Contexts** y dos de pureza del dominio. Son pruebas JUnit: se ejecutan en la fase de pruebas y **fallan como cualquier otra**, sin job dedicado.
- [x] **H37.3** **Se documentó explícitamente qué añade ArchUnit sobre lo que Maven ya impide**, con tabla comparativa. Sin esa distinción la sección parecería redundante en un reactor multi-módulo, y se habría descartado por eso — cuando en realidad cubre justo lo que el compilador no ve.
- [x] **H37.4** **Nuevo tipo de prueba `AT` en `LIN-TEST-001 §3.1`** (documento **Vigente**), y criterio de bloqueo incorporado a `LIN-CICD-001 §19.2`.
- [x] **H37.5** **Corregido el límite que `LIN-CICD-001 §12.5` daba por permanente.** Decía que la verificación automática de fronteras quedaba «fuera del alcance»; ahora existe, y el límite admitido se acota a lo que efectivamente no es automatizable. `LIN-ARQ-001 §8.3` deja constancia de que la declaración jurada **conserva su valor pero deja de ser el único control**.
- [x] **H37.6** **Las excepciones no pueden ser silenciosas.** Un `@ArchIgnore` exige una excepción `EXC-DIS-NNN` registrada con control compensatorio y fecha de revisión. Sin esa regla, desactivar una regla de arquitectura sería un cambio de una línea que nadie vuelve a mirar — el modo habitual en que estos controles mueren.

**Estado:** `LIN-DEV-JAVA-001` v0.1.12, `LIN-TEST-001` v0.1.5 (**Vigente**), `LIN-DIS-001` v0.1.7, `LIN-CICD-001` v0.1.7, `LIN-ARQ-001` v0.1.14, `GOB-MAT-001` v0.23.0. Linter en verde.


---

## H38 — Alineación completa del registro de excepciones (2026-08-18)

Cierra **H32.7**. Once lineamientos titulaban su apartado «Proceso ADR para excepciones» o «para desviaciones» **sin definir identificador**, de modo que una desviación de proyecto se registraba genéricamente como «un ADR» — instrumento que H31.6 y H32.6 reservaron a las decisiones **institucionales** del Comité. Sin corregirlo, la distinción de los tres instrumentos quedaba escrita en `GOB-MAT-001` y desmentida en once documentos.

- [x] **H38.1** **Los once alineados a `EXC-<SUFIJO>-NNN`**: `LIN-FE-ANG-001`, `LIN-API-REST-001`, `LIN-BD-ORA-001`, `LIN-DEV-JAVA-001`, `LIN-SEC-APP-001`, `LIN-CICD-001`, `LIN-BUS-001`, `LIN-TEST-001`, `LIN-PERF-001`, `LIN-K8S-001` y `LIN-OBS-001`. Los dieciséis lineamientos con apartado de excepción usan ya el formato. Dos recibieron aprobador adicional por la naturaleza de lo que norman: `LIN-SEC-APP-001` exige validación de **Seguridad Digital**, y `LIN-K8S-001` la de **Plataforma y Seguridad** cuando la desviación afecta al clúster.
- [x] **H38.2** **Añadida la tabla de sufijos por lineamiento en `GOB-MAT-001`.** Sin ella, cada equipo habría inventado el suyo —`EXC-JAVA` o `EXC-DEV`, `EXC-K8S` o `EXC-CONT`— y el identificador dejaría de ser inequívoco. Se fija además que la **numeración es correlativa por lineamiento y por sistema**, no global: `EXC-K8S-001` de PAST y `EXC-K8S-001` de Notificaciones son excepciones distintas, porque la excepción pertenece al sistema que la solicita.

### Un fallo del linter, no del corpus

- [x] **H38.3** **La tabla de sufijos disparó 16 errores falsos.** C6 y C8 reconocían como entrada de catálogo **cualquier fila que empezara por un código entre comillas invertidas**, en cualquier parte de `GOB-MAT-001`. La nueva tabla tiene esa forma, así que el linter concluyó que el catálogo declaraba a `LIN-ARQ-001` en un archivo llamado `K8S`. El defecto era del linter y ya afectaba en potencia al índice `PT → ficha`, que tiene la misma estructura. Corregido acotando ambas comprobaciones a las líneas comprendidas en las secciones de catálogo. Verificado con dos pruebas: una desviación real dentro del catálogo **sí** se detecta, y una fila con forma de catálogo fuera de él **no** dispara.

**Estado:** once documentos versionados, entre ellos los dos **Vigentes** (`LIN-OBS-001` v0.1.7, `LIN-TEST-001` v0.1.6). `GOB-MAT-001` v0.24.0. Linter en verde con las siete comprobaciones.


---

## H39 — Identificadores estables de regla (2026-08-19)

Cierra **H12.1**, abierto desde la primera revisión. Ataca la **causa raíz** del defecto más repetido de todo el ejercicio: una cita `§6.2` no falla cuando el documento citado renumera — **sigue resolviendo y apunta a otro tema**, en silencio. C1 no puede detectarlo porque verifica que la sección exista, no que trate de lo citado. Ocurrió en H6.1 (nueve citas), H12.4 (ocho), H22.2, H24.4 y H26.9.

- [x] **H39.1** **La selección se midió, no se intuyó.** Se contaron las citas por sección en todo el corpus: `LIN-ARQ-001 §2.1` (Estadios) recibe **29 citas desde 11 documentos** — una renumeración habría roto silenciosamente las 29. Le siguen `LIN-TEST-001 §5.1` (22), `LIN-DIS-001 §3.4` (16) y `LIN-SEC-APP-001 §7.1` (14). Se asignó ID a las **28 reglas de mayor tráfico**, no a todas: un ID por párrafo sería ruido. La regla de adopción es que una regla adquiere ID cuando la citan varios documentos.
- [x] **H39.2** **240 citas migradas en 24 documentos** al formato `` `ARQ-R-001` (LIN-ARQ-001 §2.1) ``: el ID identifica la regla, el número de sección queda como ayuda de navegación para quien lee en papel.
- [x] **H39.3** **Comprobación C9**, que es lo que da valor real al ID: verifica que todo ID citado esté declarado, que **ninguno esté declarado dos veces** —resolvería a la regla equivocada— y avisa de los declarados que nadie cita, señal de migración a medias. El índice `ID → documento §sección` se **genera** (`--indice`), no se mantiene a mano: una tabla manual volvería a divergir, que es justo el problema que se resuelve.
- [x] **H39.4** **Verificado con tres pruebas inyectadas**, incluida la que motivó todo el trabajo: se renumeró `LIN-ARQ-001 §2.1 → §2.9` y **C9 permaneció en verde**, con el índice resolviendo `ARQ-R-001` a su nueva ubicación. Las 29 citas sobrevivieron a la renumeración sin tocar ninguna.

### Dos defectos propios, corregidos

- [x] **H39.5** **La migración dejó 191 backticks sueltos.** Cuando la cita original venía entre comillas invertidas, mi patrón consumía la de apertura y no la de cierre, produciendo `` `ARQ-R-001` (LIN-ARQ-001 §2.1)` ``. Detectado al inspeccionar una muestra del resultado —no por el linter, que no valida formato Markdown— y reparado en los 24 documentos.
- [x] **H39.6** **C9 marcó como duplicado el ejemplo de sintaxis de `GOB-MAT-001`.** El linter no excluía los bloques de código cercados, de modo que un ejemplo se contaba como declaración real. Añadida `marcar_bloques_codigo()`; el filtro convenía además a C1 y C3, que tenían la misma exposición latente.

### Incidente durante la verificación

- [x] **H39.7** ⚠️ **Un `git checkout --` de mi prueba de regresión revirtió `LIN-ARQ-001` al último commit**, descartando los marcadores y las citas migradas de ese archivo. El daño fue acotado porque el commit `9f86e38` ya contenía v0.1.14 con `§5.4`, `§5.5` y los ADR — solo se perdió el trabajo de H39 sobre ese documento, que se rehízo y verificó. **Lección aplicable:** las pruebas de regresión del linter deben restaurar desde una copia propia, nunca desde Git, porque Git restaura al último commit y no al estado previo a la prueba.

**Estado:** 28 identificadores declarados, 240 citas migradas, linter con **ocho comprobaciones** en verde. `GOB-MAT-001` v0.25.0.


---

## H40 — Política transversal de datos personales (2026-08-21)

Cierra **H11.3**. El corpus protegía el dato **en producción** —`LIN-SEC-APP-001 §11.3` lo clasifica, `§11.4` audita el acceso, `LIN-OBS-001` prohíbe PII en logs— y dejaba abierto el punto por el que realmente se fuga: **la copia hacia DEV o QA**.

- [x] **H40.1** 🔴 **`SEC-R-003` (LIN-SEC-APP-001 §11.5).** Restaurar un respaldo productivo es el atajo natural para poblar un ambiente inferior, y en la ONP significa trasladar el padrón de afiliados completo —DNI, nombres, domicilios, montos, historial de aportes, datos de salud— a entornos con controles más débiles, más usuarios y sin auditoría. Regla general: **ningún ambiente no productivo contiene datos personales reales**, con enmascaramiento **irreversible y previo** a que el destino pueda leer el respaldo. El orden importa: enmascarar después de restaurar significa que el dato estuvo expuesto.
- [x] **H40.2** **La consistencia referencial es lo que hace viable la regla.** Enmascarar cada tabla por separado rompe los `JOIN` y produce un ambiente inservible, lo que empuja al equipo a pedir una excepción — y así es como estas políticas mueren. Se exige que el mismo valor de entrada produzca el mismo de salida en todo el conjunto. Los montos y periodos **pueden conservarse**: dejan de ser dato personal al no ser atribuibles, y son necesarios para probar cálculos previsionales.
- [x] **H40.3** **Plataforma como control de paso**, no solo como ejecutor: ningún respaldo productivo llega a un ambiente inferior sin pasar por el enmascaramiento. Las excepciones exigen aprobación de **Seguridad de la Información** y **fecha de borrado del dato**, no solo de revisión de la excepción. Se declaró además qué no es justificación válida —urgencia, falta de tiempo, que el ambiente sea interno—: la Ley N.° 29733 no distingue por ambiente.
- [x] **H40.4** **Dos documentos habían normado por su cuenta el fragmento que les tocaba** declarando la brecha como pendiente: `LIN-PERF-001 §11.3` (H30.2) y `LIN-BI-001 §8.3.1` (H32.4). Ambos remiten ahora al dueño y conservan lo propio de su dominio.

---

## H41 — Cierre de la deuda de tooling y anclas (2026-08-21)

Cierra **H21.1**, **H21.2**, **H25.6** y **H11.6**.

- [x] **H41.1** **Las 79 anclas rotas reparadas — 477 enlaces internos, 0 rotos.** La medición previa daba 57 sobre 444; el crecimiento del corpus añadió más. Tres causas mecánicas: tilde omitida en el enlace (`#57-verificacion` contra un encabezado «Verificación»), guion simple donde el título genera **doble** por llevar raya, y retitulados que no se propagaron al índice — incluidos los seis que dejó el propio H38 al renombrar los apartados de excepción. El daño real no era estético: **la tabla de contenido de varios documentos era inservible**, y un documento normativo con índice roto no lo puede usar un contratista.
- [x] **H41.2** **C7 implementada** (H21.2). El algoritmo se validó contra el corpus completo y se documentó su regla menos obvia: las **tildes se conservan** —son alfanuméricas— y una raya `—` produce guion doble. Queda declarado que, ante discrepancia con el GitLab real de la ONP, manda el renderizador. *(Detectó de inmediato un enlace de ejemplo en su propio README, que hubo que reescribir.)*
- [x] **H41.3** **C10 implementada** (H25.6, renumerada porque C9 ya se usó para los IDs estables). **Encontró 10 historiales desordenados en su primera ejecución** — incluidos los de dos documentos `Vigente`—, todos ordenados. No es cosmético: en H20 el desorden llevó a crear una **versión duplicada**, porque la última fila de la tabla no era la última versión.
- [x] **H41.4** **Checklist de graduación de WSO2** (H11.6). `ADR-WSO2-001` remitía a «actualizar `LIN-SEC-APP-001`, `LIN-API-REST-001` y este ADR» — tres documentos, cuando la transición toca **trece puntos** en ocho documentos. Se enumeraron con casilla, incluidos los menos evidentes: que el token deja de ser opaco y pasa a JWT (obliga a revisar toda mención en el corpus), que `ADR-TLS-INTERNO-001` tiene la graduación como disparador declarado, y que las APIs ya en producción **se registran conservando su estado, no se rehacen**.

**Estado:** linter con **diez comprobaciones** en verde. `LIN-SEC-APP-001` v0.1.9, `LIN-PERF-001` v0.1.6, `LIN-BI-001` v0.1.4, `GOB-MAT-001` v0.27.0.


---

## H42 — `LIN-DOC-001`: el mapa documental queda completo (2026-08-21)

Cierra **H11.5**, el último documento que `GOB-MAT-001` declaraba `Pendiente`. **Ningún documento del corpus queda sin elaborar.**

- [x] **H42.1** **El documento se escribió por sustracción, no por acumulación.** El riesgo evidente era redactar un tratado que repitiera lo que ya norman otros nueve documentos —Javadoc, OpenAPI, ADR, contratos de evento, catálogos, ciclo de vida—. Se inventarió primero lo que **ya tenía dueño** y `§1.3` lo declara fuera de alcance explícitamente. Lo que queda es lo que nadie normaba.
- [x] **H42.2** 🔴 **Ni el `README.md` ni el runbook estaban normados en ninguna parte.** El README es lo primero que lee quien hereda un sistema, y el corpus solo exigía que contuviera la Declaración de Conformidad —una sección— sin decir nada del resto. `DOC-R-001` fija ocho secciones mínimas, incluida la que más falta hace cuando el sistema se hereda: **qué dependencias externas tiene y qué ocurre si no están disponibles**.
- [x] **H42.3** 🔴 **El runbook de operación no existía como artefacto.** `ARQ-R-006` (LIN-ARQ-001 §5.4.3) exige un procedimiento de recuperación para criticidad Alta y Media, pero nada decía dónde vive ni qué más debe saber quien atiende un incidente de madrugada. `DOC-R-003` lo norma con una regla que lo mantiene vivo: **todo incidente que requiera intervención humana obliga a revisar el runbook** — si el procedimiento documentado no funcionó, se corrige. Un runbook que no crece tras los incidentes no se está usando.
- [x] **H42.4** **La notación de modelado tenía autoridad implícita.** «Archi (ArchiMate 3.x) — estándar aprobado en la ONP» constaba en **una nota al margen de `GOB-PLA-001`**, no en un lineamiento. `DOC-R-002` la eleva a norma, añade Mermaid para diagramas embebidos —que `LIN-BI-001` ya usaba sin respaldo— y fija la regla que evita artefactos muertos: **la fuente de todo diagrama se versiona**; un `.png` suelto nadie puede modificarlo y acaba sustituido por otro que dice algo distinto.
- [x] **H42.5** **Se aclaró la relación con C4**, que `LIN-ARQ-001 §1.2` usa como referencia conceptual de los tres niveles. C4 es un modelo de niveles de abstracción, no una notación de dibujo: en la ONP esos niveles se materializan con las vistas ArchiMate. No se exige producir diagramas «C4» aparte — la ambigüedad habría llevado a algún proyecto a duplicar el modelado.
- [x] **H42.6** **Regla de obsolescencia (P4):** documentación desactualizada es **peor** que ausente, porque induce a error con apariencia de autoridad. Un artefacto que ya no se mantiene se marca retirado o se elimina — Git conserva el historial.

**Estado:** `LIN-DOC-001` v0.1.0 **En revisión**; `GOB-PLA-001` v2.8, `LIN-K8S-001` v0.1.19, `GOB-MAT-001` v0.28.0. Linter en verde con las diez comprobaciones.


---

## H43 — Versionado del corpus como conjunto (2026-08-21)

Cierra **H8.5**, abierto desde la primera revisión a la espera de que hubiera algo estable que etiquetar.

- [x] **H43.1** **El problema no era técnico sino de exigibilidad.** Un TDR o un contrato no invoca «`LIN-K8S-001` v0.1.19»: invoca *el corpus vigente en una fecha*. Sin marca del conjunto, reconstruir qué reglas regían cuando se firmó un contrato exige revisar 22 historiales por separado. Normado en `GOB-MAT-001`.
- [x] **H43.2** **Se adoptó el formato `v<MAJOR>.<MINOR>.<PATCH>` que `LIN-VER-001 §15.1` exige al código**, descartando el `corpus-v2026.08` que este checklist proponía originalmente. No cumplía la norma del propio corpus, y si el corpus se etiquetara con un formato propio la primera pregunta razonable de una fábrica sería por qué ella sí debe cumplirlo. Es el mismo tipo de incoherencia que H8 encontró en los nombres de archivo.
- [x] **H43.3** **`MAJOR` incrementa cuando un documento gradúa a `Vigente`**, no cuando cambia mucho contenido. El criterio es qué se puede exigir contractualmente: una graduación altera el conjunto de reglas invocables en un TDR, y eso es un cambio incompatible para quien contrata.
- [x] **H43.4** **Etiquetado como `v0.9.0`, no `v1.0.0`.** El corpus está **completo** —ningún documento en `Borrador` ni `Pendiente`— pero **no graduado**: solo `LIN-OBS-001` y `LIN-TEST-001` son `Vigente`, y por la regla de exigibilidad los otros quince no son invocables en un TDR. Un `v1.0.0` se leería como «ya rige» y no rige todavía. El `v1.0.0` queda reservado a la graduación del Comité (H22.4), de modo que la versión signifique algo verificable.

**Estado:** `GOB-MAT-001` v0.29.0. Tag `v0.9.0` creado en local, **sin push** — la publicación al remoto es decisión de Arquitectura.


---

## H44 — Auditoría de cierre: qué queda realmente pendiente (2026-08-21)

Balance solicitado por Arquitectura tras cerrar 41 bloques. Se auditó el estado real —no el declarado en este checklist— y aparecieron **tres deudas que no estaban registradas**.

- [x] **H44.1** 🔴 **`LIN-ARQ-001 §5.5.3` afirmaba tener un vacío que ya se había cerrado.** Decía que la verificación de fronteras internas del Monolito Modular «requeriría análisis estático… control del que el corpus **aún no dispone**». Dejó de ser cierto **tres días después**, cuando H37 incorporó las pruebas ArchUnit en `LIN-DEV-JAVA-001 §15.5`, sin que la sección se actualizara. Es exactamente el defecto que `GOB-PLA-001 §1.5` norma para los documentos de proyecto —declarar conformidad con un corpus que ya cambió— **reproducido dentro del propio marco rector**. Corregido: ambos controles quedan declarados complementarios y no sustituibles. → `LIN-ARQ-001` v0.1.15.
- [ ] **H44.2** **37 temas de `GOB-MAT-001` siguen `En borrador`**, sobre un total de ~150. No es un defecto de contenido: es que su **alineación con los consumidores nunca se verificó**, y la regla editorial de la matriz obliga a usar `En borrador` ante duda razonable en vez de declarar `Conforme` por conveniencia. Se concentran en `LIN-SEC-APP-001` (8), `LIN-VER-001` (6) y `LIN-CICD-001` (5) — los tres documentos con más consumidores. **Es trabajo de verificación, no de redacción**, y es el criterio 3 de graduación: cerrarlos es requisito para que esos documentos puedan graduar.
- [ ] **H44.3** **Dos deudas técnicas declaradas dentro de documentos, no en este checklist:**<br>• **Respaldo de `PersistentVolume`** — `ARQ-R-006` (LIN-ARQ-001 §5.4.2) lo marca «Pendiente de normar» y remite a `LIN-K8S-001 §13.3`, que exige declarar una «política de backup» sin definir su contenido. Hoy se resuelve por la banda de criticidad, pero falta el mecanismo (frecuencia, herramienta, retención).<br>• **Namespaces de infraestructura compartida** — `LIN-K8S-001 §4.4` pide a **Plataforma** confirmar si `otel-*` y `kafka-*` viven en un clúster compartido, lo que justificaría el sufijo de ambiente que la propia norma considera anti-patrón. Sin esa confirmación, dos documentos conservan una convención que puede ser incorrecta.
- [ ] **H44.4** **Los dos ADR de mayor alcance siguen en estado `Propuesta`:** `ADR-WSO2-001` y `ADR-TLS-INTERNO-001`. El segundo importa más de lo que parece: **sostiene la excepción de tráfico intra-cluster sobre HTTP**, y mientras no lo apruebe Seguridad, esa excepción se aplica sin respaldo formal.

- [x] **H44.5** **Creado `GOB-EST-001` — Estado y Continuidad del Corpus.** Este checklist es un registro **cronológico** de 44 bloques y ~2.900 líneas: sirve para saber *por qué* se hizo cada corrección, no para retomar el trabajo. `GOB-EST-001` consolida el estado verificado, las **decisiones cerradas que no deben re-litigarse** —el mayor riesgo en una retomada es volver a discutirlas—, los cinco frentes pendientes con contexto para arrancar en frío, el orden recomendado y los riesgos conocidos. Remite a este checklist para el detalle en vez de copiarlo.
- [x] **H44.6** *(detectado al auditar)* **Tema duplicado en `GOB-MAT-001`:** «Seguridad frontend (XSS, token storage, guards, CSP)» figura en las líneas 371 y 463, ambas con dueño `LIN-SEC-APP-001` pero con **redacción distinta en la columna de evidencia** — una dice que `LIN-FE-ANG-001` ya las aplica, la otra que está pendiente. Registrado en `GOB-EST-001 §4.3` como primer paso de la verificación de temas.

**Estado:** el corpus está completo y verificado mecánicamente; lo que resta es **verificación humana y aprobación formal**, no redacción. Punto de retomada: **`GOB-EST-001`**.


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
- [x] **H8.5** **Cerrado en H43.** Corpus etiquetado `v0.9.0` con el formato que `LIN-VER-001 §15.1` exige al código; regla de versionado del conjunto normada en `GOB-MAT-001`, con `MAJOR` ligado a la graduación de documentos.
- [x] **H8.6** *(detectado y corregido en H2/H3, 2026-08-05)* Los tres documentos de arquitectura — `LIN-ARQ-001` (Nivel 1), `LIN-DIS-001` (Nivel 2) y `LIN-PAT-001` (catálogo) — eran los **únicos del corpus sin sección de historial de versiones**, pese a que todos los demás lineamientos la tienen y a que la propia matriz exige trazabilidad. Historial incorporado a los tres. Las versiones previas quedan agregadas como rango con nota "detalle no registrado" (no es reconstruible desde los documentos). *(La Matriz `GOB-MAT-001` tenía el mismo problema y se corrigió en H5.6.)*

### H9 — Residuo de build versionado en template

- [x] **H9.1** Eliminado `template-backend-java/.m2/repository/.../spring-boot-starter-parent-3.5.0.pom.lastUpdated` — marcador interno del resolvedor de Maven que registraba una **descarga fallida** (error de resolución DNS del 2026-05-28), no contenido del template. **Confirmado con Arquitectura antes de borrar.** Recuperable desde el historial de Git.
- [x] **H9.2** `.m2/` añadido al `.gitignore` de **ambos** templates, con nota de por qué: el CI usa `-Dmaven.repo.local=.m2/repository`, así que sin la regla la caché volvería a colarse en cada ejecución local. Verificado con `git check-ignore`.

### H12 — Tooling: linter del corpus + anclas estables (causa raíz de H1, H5, H6, H7)

- [x] **H12.1** **Cerrado en H39.** 28 reglas de mayor tráfico con identificador estable `<SUFIJO>-R-NNN`, 240 citas migradas y comprobación C9 que las verifica. Renumerar una sección ya no rompe sus citas.
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

- [x] **H11.1** **Cerrado en H24.** `§1.3` dejó de presentar RFC 7807 como «estándar de respuestas de error» —lo citaba como normativa sin adoptarlo— y `§4.1` explica ahora por qué la ONP no usa `application/problem+json`. Añadida la regla de que `codHttp` es réplica informativa y el status line HTTP es la fuente de verdad, con el motivo operativo: proxys, gateway, `http.server.requests` y las alertas de Kibana leen el status line, así que un `200` con `codHttp: 500` deja el error invisible en toda la cadena.
- [x] **H11.2** **Cerrado en H33.** `LIN-ARQ-001 §5.4` asume la continuidad operativa: bandas de criticidad con RTO/RPO objetivo, política de respaldo por componente, recuperación a nivel de sistema y pruebas de restauración con medición del tiempo real. **Pendiente la ratificación de los valores por el Comité con las áreas usuarias (H33.10)** — los números son propuesta técnica, no decisión de arquitectura.
- [x] **H11.3** **Cerrado en H40.** `SEC-R-003` (LIN-SEC-APP-001 §11.5) norma los datos personales en ambientes no productivos: regla general sin excepción por conveniencia, tratamiento por tipo de dato con consistencia referencial, Plataforma como control de paso, alternativas preferentes y excepciones que exigen aprobación de Seguridad de la Información más **fecha de borrado del dato**. `LIN-PERF-001` y `LIN-BI-001` remiten al dueño.
- [x] **H11.4** **Cerrado en H24, y era peor de lo registrado.** No es que faltara definir el mecanismo: `LIN-SEC-APP-001 §7.1` ya lo exigía —«rate limiting básico configurado en la aplicación si no hay gateway»— pero `LIN-API-REST-001 §8.4` afirmaba lo contrario, que *«los servicios no implementan rate limiting internamente»*, atribuyéndolo a un gateway que sigue en PoC. La contradicción dejaba a **toda** API sin control, no solo a las internas. Corregido, con `codDetRespuesta 302` / `429` incorporado al catálogo.
- [x] **H11.5** **Cerrado en H42.** `LIN-DOC-001` v0.1.0 creado y en el catálogo. Con él, **ningún documento del corpus queda `Pendiente`**.
- [x] **H11.6** **Cerrado en H41.4.** `ADR-WSO2-001` lleva checklist con 13 puntos en 8 documentos, frente a los 3 que enumeraba antes.

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
