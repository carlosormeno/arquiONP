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
| **C3** | Todo código `PT` citado está definido en `LIN-PAT-001` (fuente única). Además, un código de brecha (`PA`/`PI`/`PD`/`PR`/`PG`) inexistente en `GOB-BRE-001` es error, y uno que el tablero declare `Pendiente` es aviso | Error / Aviso | H10.3 — el ida y vuelta PT06↔PT07. **Ampliada en H26:** `K8S-R-003` (LIN-K8S-001 §9.4) normaba Sidecar y Ambassador con los códigos `PA12`/`PA13` del tablero de brechas, invisible para el linter |
| **C4** | Las copias controladas son idénticas a su fuente canónica | Error | H4.4 — los templates enviaban un `checkstyle-onp.xml` mutilado |
| **C5** | Sin rutas absolutas de máquina; los enlaces relativos resuelven | Error | H10.5, H13.5 — 13 enlaces `file:///home/carlos/...` |
| **C6** | Las rutas del catálogo de `GOB-MAT-001` existen y contienen el código atribuido | Error | H8.3 — al renombrar el documento congelado, la ruta del catálogo quedó apuntando a un archivo inexistente |
| **C8** | La versión y el estado que el catálogo declara coinciden con el encabezado real | Error (estado) / Aviso (versión) | H25 — **15 de 21 entradas desactualizadas**; el catálogo daba `LIN-API-REST-001` por «Borrador v0.1.5» cuando iba por «En revisión v0.1.7» |
| **C9** | Todo identificador estable de regla (`ARQ-R-001`) citado está declarado, y ninguno lo está dos veces | Error / Aviso (declarado sin citar) | H39 — desacopla la identidad de una regla de su número de sección |

### Decisiones de diseño

**Un código de brecha no es un código normativo.** `GOB-BRE-001` inventaría vacíos: mientras un patrón figura ahí como `Pendiente`, se está afirmando que *falta* normarlo. Si un lineamiento lo norma citando ese código, el tablero queda contradiciendo a la norma. C3 lo detecta, pero solo para brechas abiertas: muchos códigos (`PR01`–`PR08` son principios SOLID, `PD04`–`PD06` building blocks DDD) no tienen ni deben tener equivalente `PT`.

**Las tablas de historial se excluyen de C1 y C3.** Un `Historial de versiones`, `Control de cambios` o `Historial de revisiones` —con o sin numeración— cita a propósito numeraciones antiguas para documentar qué se corrigió; validarlas produciría ruido permanente. El linter detecta esos encabezados y omite sus líneas.

**`LIN-ARQ-000` está excluido de las citas salientes.** Es cantera histórica congelada: sus referencias apuntan a su propia estructura y no deben corregirse. Sí se le aplican C5 (enlaces) y las comprobaciones entrantes — otros documentos no pueden citarlo como norma vigente.

**El estado desviado es error; la versión desviada, aviso.** C8 distingue ambos casos porque no cuestan lo mismo. Una versión desfasada en el catálogo desorienta; un **estado** desfasado cambia qué es contractualmente exigible, porque la regla de exigibilidad de `GOB-MAT-001` se aplica sobre el estado declarado — dar por `Vigente` lo que sigue en `Borrador` permite exigir en un TDR algo que aún no obliga.

**Índice de reglas:** `python3 herramientas/lint_corpus.py --indice` imprime el mapa `ID → documento §sección`. **Se genera, no se mantiene a mano**: una tabla manual volvería a divergir, que es el problema que los IDs resuelven.

**No hay C7.** El número está reservado para la comprobación de anclas internas (`GOB-CHK-001` H21.2), pendiente de validar el algoritmo de generación contra el renderizador real del GitLab de la ONP. C8 se numeró después para no ocupar ese hueco.

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
