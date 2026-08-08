# Herramientas de gobierno del corpus documental ONP

Utilidades para mantener la coherencia interna del corpus de lineamientos. No forman parte de la normativa: son instrumentos de verificación.

---

## `lint_corpus.py` — Linter del corpus

Verifica automáticamente las clases de inconsistencia que la revisión integral de 2026-08-05 detectó de forma manual (`GOB-CHK-001` H12.2). Sin dependencias externas: Python 3.8+.

### Uso

```bash
# Desde la raíz del corpus
python3 herramientas/lint_corpus.py

# Una sola comprobación
python3 herramientas/lint_corpus.py --solo C1

# Sobre otra copia del corpus
python3 herramientas/lint_corpus.py --raiz /ruta/al/corpus
```

Código de salida `0` si no hay errores, `1` si los hay. Los **avisos no rompen el build**.

### Comprobaciones

| Regla | Qué verifica | Nivel | Origen |
|---|---|---|---|
| **C1** | Toda cita `LIN-XXX §N.N` o `LIN-XXX sección N.N` resuelve a una sección real del documento citado | Error | H1, H5.3, H6, H7 — 17 citas rotas corregidas a mano |
| **C2** | La versión del encabezado coincide con la embebida en el nombre de archivo | Aviso | H8 — divergencia sistémica, 19 documentos afectados |
| **C3** | Todo código `PT` citado está definido en `LIN-PAT-001` (fuente única) | Error | H10.3 — el ida y vuelta PT06↔PT07 de `LIN-SEC-APP-001` |
| **C4** | Las copias controladas son idénticas a su fuente canónica | Error | H4.4 — los templates enviaban un `checkstyle-onp.xml` mutilado |
| **C5** | Sin rutas absolutas de máquina; los enlaces relativos resuelven | Error | H10.5, H13.5 — 13 enlaces `file:///home/carlos/...` |

### Decisiones de diseño

**Las tablas de historial se excluyen de C1 y C3.** Un `Historial de versiones` o `Control de cambios` cita a propósito numeraciones antiguas para documentar qué se corrigió; validarlas produciría ruido permanente. El linter detecta esos encabezados y omite sus líneas.

**`LIN-ARQ-000` está excluido de las citas salientes.** Es cantera histórica congelada: sus referencias apuntan a su propia estructura y no deben corregirse. Sí se le aplican C5 (enlaces) y las comprobaciones entrantes — otros documentos no pueden citarlo como norma vigente.

**Una sección padre existe si existe una hija.** Si el documento declara `### 13.4.4`, el linter acepta citas a `§13`, `§13.4` y `§13.4.4`, aunque no haya un encabezado literal `## 13.4`.

### Añadir un artefacto duplicado a C4

Editar `ARTEFACTOS_DUPLICADOS` en el script:

```python
ARTEFACTOS_DUPLICADOS = [
    ("ruta/canonica.xml", ["ruta/copia-1.xml", "ruta/copia-2.xml"]),
]
```

La fuente canónica de cada artefacto se declara en el README del template que lo consume, sección *"Artefactos normados — no personalizar"*.

---

## Regla de proceso (`GOB-CHK-001` H12.3)

> **Toda renumeración de secciones de un documento dueño debe corregir, en el mismo Merge Request, las citas de la Matriz `GOB-MAT-001` y de los documentos consumidores.**

El linter hace verificable esta regla: un MR que renumere sin propagar falla en `C1`.

Es la causa raíz documentada de la mayor parte de la deuda encontrada en la revisión integral. `LIN-DEV-JAVA-001` fue renumerado internamente (`§11.4.x` → `§13.4.x`, entre otros) sin propagar el cambio, y las citas rotas resultantes sobrevivieron en la Matriz, `LIN-FE-ANG-001` y el propio documento durante varias versiones — incluida una corrección de cobertura que se aplicó en `§12.1` mientras el gate de Pull Request seguía apuntando a la tabla contradictoria de `§15.3`.

---

## Ejecución en CI

El pipeline `.gitlab-ci.yml` de este repositorio ejecuta el linter en:

- todo Merge Request que toque `**/*.md`, el linter o los artefactos controlados;
- todo commit a la rama principal.

Un segundo job (`lint-corpus-avisos`, `allow_failure: true`) lista los avisos abiertos sin bloquear, útil mientras H8 siga pendiente.
