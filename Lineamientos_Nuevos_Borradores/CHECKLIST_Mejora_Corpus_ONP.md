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

- [ ] **H21.1** *(deuda de corpus — no bloquea graduación; el subconjunto bloqueante se cerró en H21.3)* **57 de 444 enlaces internos** siguen sin resolver, tras bajar de 83 con la normalización de BD y BI. 🟠 **83 de 435 enlaces internos (19%) no resuelven a ningún encabezado.** Detectado al revisar `LIN-TEST-001` y verificado en todo el corpus. Se concentran en `LIN-BD-ORA-001` (26), `LIN-OBS-001` (19), `LIN-DEV-JAVA-001` (11), `LIN-TEST-001` (11) y `LIN-API-REST-001` (10). Tres causas identificadas:<br>**(a) Encabezados con formato `## sección N`** — `LIN-BD-ORA-001` y `LIN-BI-001` los titulan así, generando anclas `#sección-1-alcance-y-vigencia`, mientras sus tablas de contenido enlazan a `#1-alcance-y-vigencia`. Ningún enlace del índice de esos dos documentos funciona.<br>**(b) Tildes omitidas en el enlace** — se enlaza `#23-catalogo-centralizado…` cuando el encabezado dice «Catálogo» y el ancla conserva la tilde.<br>**(c) Guion doble por rayas y guiones en el título** — «`4.8 Mask.java — utilidad No PII`» genera `#48-maskjava--utilidad-no-pii` (doble guion al eliminarse la raya), y el enlace escribe uno solo.<br>**Nota de confianza:** la generación de anclas depende del renderizador; la verificación usó el algoritmo de GitHub/GitLab. Los tres patrones se confirmaron contra los encabezados reales, pero conviene validar una muestra en el GitLab de la ONP antes de corregir en masa. **Un primer intento de medición dio 148 rotos por despojar tildes en la normalización — cifra falsa; la correcta es 83.**
- [x] **H21.3** 🔴 **Desbloqueado.** Encabezados de `LIN-BD-ORA-001` y `LIN-BI-001` normalizados a `## N. Título`. En BD el cambio hizo funcionar los enlaces del índice **sin tocarlos**, porque ya apuntaban a `#N-…`. **En BI ocurrió lo contrario y los rompí:** sus enlaces sí seguían el formato antiguo `#sección-N-…`, así que hubo que actualizarlos — detectado por la verificación posterior, no previsto al planificar. Corregidas además 15 anclas de BD con tilde omitida o guion simple donde el encabezado genera doble. **Resultado: 55/55 y 11/11 enlaces resuelven**; ambos documentos dejan de estar bloqueados por severidad. Las citas externas por número de sección no se vieron afectadas: el linter sigue en verde.
- [ ] **H21.2** Extender el linter con una comprobación **C7** de anclas internas, una vez validado el algoritmo contra el renderizador real de la ONP. Es la única clase de enlace que hoy no se valida: C5 cubre enlaces a archivos y omite los `#ancla`.

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
- [ ] **H25.6** *(candidato, no implementado)* **Comprobación C9 — historiales de versiones ordenados cronológicamente.** El desorden ya apareció tres veces (`LIN-BD-ORA-001` dos veces, `LIN-VER-001` una), y en H20 llegó a producir una **versión duplicada** por no ver cuál era la última fila. Es mecánicamente verificable y barato.

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
- [ ] **H32.7** Alinear los diez documentos restantes a la convención `EXC-<CÓDIGO>-NNN`. Es mecánico —retitular el apartado y remitir a `GOB-MAT-001`— pero toca diez archivos y conviene hacerlo en un solo cambio.

**Estado:** `LIN-IAC-001` v0.1.3 y `LIN-BI-001` v0.1.3 pasan a **En revisión**; `GOB-MAT-001` v0.17.0. **Ningún lineamiento del corpus queda en `Borrador`.** Linter en verde.


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

- [x] **H11.1** **Cerrado en H24.** `§1.3` dejó de presentar RFC 7807 como «estándar de respuestas de error» —lo citaba como normativa sin adoptarlo— y `§4.1` explica ahora por qué la ONP no usa `application/problem+json`. Añadida la regla de que `codHttp` es réplica informativa y el status line HTTP es la fuente de verdad, con el motivo operativo: proxys, gateway, `http.server.requests` y las alertas de Kibana leen el status line, así que un `200` con `codHttp: 500` deja el error invisible en toda la cadena.
- [ ] **H11.2** Nuevo lineamiento (o sección en dueño existente): **backup/DR, RTO/RPO y gestión de capacidad** — vacío mayor para una entidad de pago de pensiones. Priorizar sobre brechas de baja prioridad (Serverless, Ambassador).
- [ ] **H11.3** Política transversal de **datos de prueba y enmascaramiento de PII** para ambientes no productivos (Ley 29733). **Avance en H30.2:** `LIN-PERF-001 §11.3` ya norma el enmascaramiento previo e irreversible al restaurar respaldos productivos, con tabla por tipo de dato — pero solo para su ámbito. Falta la política transversal, cuyo dueño natural es `LIN-SEC-APP-001`.<br>**Avance adicional en H32.4:** `LIN-BI-001 §8.3.1` cubre la capa Bronze del Lakehouse, que es la mayor concentración de datos personales replicados del corpus. Sigue faltando la política transversal.
- [x] **H11.4** **Cerrado en H24, y era peor de lo registrado.** No es que faltara definir el mecanismo: `LIN-SEC-APP-001 §7.1` ya lo exigía —«rate limiting básico configurado en la aplicación si no hay gateway»— pero `LIN-API-REST-001 §8.4` afirmaba lo contrario, que *«los servicios no implementan rate limiting internamente»*, atribuyéndolo a un gateway que sigue en PoC. La contradicción dejaba a **toda** API sin control, no solo a las internas. Corregido, con `codDetRespuesta 302` / `429` incorporado al catálogo.
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
