#!/usr/bin/env python3
"""
Linter del corpus documental ONP — GOB-CHK-001 H12.2

Verifica automáticamente las clases de inconsistencia que la revisión integral
de 2026-08-05 encontró a mano. Sin dependencias externas: corre con Python 3.8+.

Comprobaciones:
  C1  Citas cruzadas         — toda cita `LIN-XXX §N.N` resuelve a una sección real
  C2  Versión del documento  — encabezado coherente con el historial de versiones
  C3  Códigos PT             — todo código PT citado existe en LIN-PAT-001 (fuente única)
  C4  Artefactos duplicados  — copias controladas idénticas a su fuente canónica
  C5  Enlaces                — sin rutas absolutas de máquina; enlaces relativos resuelven
  C6  Catálogo GOB-MAT-001  — las rutas declaradas existen y contienen el código atribuido

Uso:
    python3 herramientas/lint_corpus.py [--raiz RUTA] [--formato texto|gitlab]

Salida: código 0 si no hay errores, 1 si los hay. Las advertencias no fallan el build.
"""

import argparse
import hashlib
import os
import re
import sys
from collections import defaultdict

# --------------------------------------------------------------------------
# Configuración
# --------------------------------------------------------------------------

# Documentos excluidos de la validación de citas salientes.
# LIN-ARQ-000 está congelado: sus citas apuntan a su propia estructura histórica
# y no deben corregirse (ver GOB-CHK-001, política de cantera histórica).
EXCLUIDOS_CITAS = {"LIN-ARQ-000"}

# Archivos que no son documentos normativos.
EXCLUIDOS_ARCHIVOS = {"CHECKLIST_Mejora_Corpus_ONP.md"}

# Sufijos de documentos fuera del corpus: copias superadas, respaldos o borradores
# descartados. **No se consideran parte del corpus** y no deben citarse ni validarse.
# El corpus no debe contener archivos así: el historial de Git es el mecanismo de
# preservación (GOB-CHK-001 H8.3).
SUFIJOS_FUERA_DE_CORPUS = ("_OLD", "_old", "_BACKUP", "_bak", "_obsoleto", "_DEPRECADO")

# Directorios que no se recorren.
EXCLUIDOS_DIRS = {".git", "node_modules", "target", ".m2"}

# Rutas fuera del corpus normativo: los templates son proyectos de ejemplo con su
# propio pipeline, y `herramientas/` es instrumental. No se les aplican C1–C3.
PREFIJOS_NO_CORPUS = (
    "versionamiento/ejemplos-plantillas-gitlab/",
    "herramientas/",
)

# Artefactos duplicados: (ruta canónica, [rutas de copias controladas])
ARTEFACTOS_DUPLICADOS = [
    (
        "desarrollo/plantillas/checkstyle-onp.xml",
        [
            "versionamiento/ejemplos-plantillas-gitlab/template-backend-java/checkstyle-onp.xml",
            "versionamiento/ejemplos-plantillas-gitlab/template-backend-java-modular/checkstyle-onp.xml",
        ],
    ),
]

# Encabezados de sección que indican historial de cambios. Las citas que aparecen
# dentro de estas tablas describen numeraciones históricas a propósito y no se validan.
ENCABEZADOS_HISTORIAL = re.compile(
    r"^#{1,3}\s+(historial de versiones|control de cambios|control de versiones)",
    re.IGNORECASE,
)

# --------------------------------------------------------------------------
# Expresiones regulares
# --------------------------------------------------------------------------

# Los encabezados del corpus usan tres formatos distintos:
#   **Código:** LIN-XXX-001            (lista de definición)
#   | **Código** | LIN-XXX-001 |       (tabla)
#   ### Código: LIN-XXX-001 | Versión 0.1.9 | ...   (línea compuesta)
# El separador entre la etiqueta y el valor puede ser `:`, `**`, `|` o espacios.
# El código no siempre termina en dígitos: junto a `LIN-ARQ-001` y `GOB-MAT-001`
# conviven identificadores como `GLOSARIO-ONP`.
#
# La declaración debe estar **al inicio de la línea** (admitiendo decoración Markdown
# `#`, `*`, `|`, `>`). Sin ese anclaje, la palabra "Código" en prosa produce falsos
# positivos: el banner de `LIN-ARQ-000` dice "Nivel 3 (Micro / Código): [LIN-DEV-JAVA-001…]"
# y el documento congelado quedaba indexado con el código del estándar Java.
RE_CODIGO_DOC = re.compile(
    r"^[\s#*|>_-]*C[óo]digo[\s*:|]+`?([A-Z]{3,}(?:-[A-Z0-9]+)+)`?", re.IGNORECASE
)
RE_VERSION_ENCABEZADO = re.compile(
    r"Versi[óo]n[\s*:|]+`?v?(\d+\.\d+\.\d+|\d+\.\d+)`?", re.IGNORECASE
)

# Encabezados: "## 1. Título" / "### 1.2 Título" / "## sección 3 Título" / "#### 13.4.1 Título"
RE_ENCABEZADO_NUM = re.compile(
    r"^#{2,6}\s+(?:secci[óo]n\s+)?(\d+(?:\.\d+)*)[.\s]", re.IGNORECASE
)

# Citas: "LIN-XXX §4.2" / "LIN-XXX sección 4.2" / "LIN-XXX secciones 4.6–4.7"
# y también la forma abreviada sin marcador — "LIN-OBS-001 6", "LIN-CICD-001 13.4" —
# que usan varios documentos y que quedaba invisible al linter (GOB-CHK-001 H15).
# El separador entre código y número es obligatorio: sin él, `LIN-CICD-001` se
# partiría en código `LIN-CICD-00` + sección `1` al backtrackear el `\d+` final.
RE_CITA = re.compile(
    r"`?(LIN-[A-Z0-9-]+?-\d+|GOB-[A-Z]+-\d+)`?"          # documento citado
    r"(?:\s*,?\s*§\s*|\s+secci[oó]n(?:es)?\s+|\s+)"        # marcador (§, «sección» o espacio)
    r"(\d+(?:\.\d+)*)"                                     # número
    r"(?:\s*[–—-]\s*(\d+(?:\.\d+)*))?",                    # rango opcional
    re.IGNORECASE,
)

RE_CODIGO_PT = re.compile(r"\b(PT\d{2})\b")
RE_ENLACE_MD = re.compile(r"\[[^\]]*\]\(([^)]+)\)")
RE_RUTA_ABSOLUTA = re.compile(r"\]\((?:file://|/home/|/Users/|[A-Za-z]:\\)")


class Hallazgo:
    def __init__(self, nivel, archivo, linea, regla, mensaje):
        self.nivel = nivel          # "error" | "aviso"
        self.archivo = archivo
        self.linea = linea
        self.regla = regla
        self.mensaje = mensaje


# --------------------------------------------------------------------------
# Recolección
# --------------------------------------------------------------------------

def listar_markdown(raiz):
    for dirpath, dirnames, filenames in os.walk(raiz):
        dirnames[:] = [d for d in dirnames if d not in EXCLUIDOS_DIRS]
        for nombre in sorted(filenames):
            if not nombre.endswith(".md") or nombre in EXCLUIDOS_ARCHIVOS:
                continue
            base = nombre[:-3]
            if base.endswith(SUFIJOS_FUERA_DE_CORPUS):
                continue  # copia superada: fuera del corpus
            yield os.path.join(dirpath, nombre)


def indexar(raiz):
    """
    Indexa TODOS los markdown del corpus.

    Devuelve (docs, por_codigo):
      docs       — lista de documentos (con o sin código declarado)
      por_codigo — {CÓDIGO: doc} para resolver las citas entrantes

    El código es opcional: un documento sin él (p. ej. el Glosario o un ADR) se
    valida igual como emisor de citas y enlaces. Antes se descartaban en silencio,
    lo que dejaba 17 de 36 archivos fuera del linter.
    """
    docs = []
    por_codigo = {}
    for ruta in listar_markdown(raiz):
        rel = os.path.relpath(ruta, raiz)
        with open(ruta, encoding="utf-8") as fh:
            lineas = fh.read().split("\n")

        codigo = None
        version = None
        for linea in lineas[:40]:
            if codigo is None:
                m = RE_CODIGO_DOC.search(linea)
                if m:
                    codigo = m.group(1).upper()
            if version is None:
                m = RE_VERSION_ENCABEZADO.search(linea)
                if m:
                    version = m.group(1)

        secciones = set()
        for linea in lineas:
            m = RE_ENCABEZADO_NUM.match(linea)
            if m:
                num = m.group(1)
                secciones.add(num)
                # Una sección padre existe implícitamente si existe una hija.
                partes = num.split(".")
                for i in range(1, len(partes)):
                    secciones.add(".".join(partes[:i]))

        doc = {
            "ruta": rel,
            "codigo": codigo,
            "secciones": secciones,
            "version": version,
            "lineas": lineas,
            "es_corpus": not rel.replace(os.sep, "/").startswith(PREFIJOS_NO_CORPUS),
        }
        docs.append(doc)
        if codigo:
            # Un código identifica a un único documento: si dos lo reclaman, las citas
            # se resolverían contra el equivocado según el orden de recorrido.
            if codigo in por_codigo:
                doc["codigo_duplicado_de"] = por_codigo[codigo]["ruta"]
            else:
                por_codigo[codigo] = doc
    return docs, por_codigo


def marcar_historial(lineas):
    """Devuelve un set con los índices de línea que pertenecen a un historial de cambios."""
    dentro = False
    indices = set()
    for i, linea in enumerate(lineas):
        if linea.startswith("#"):
            dentro = bool(ENCABEZADOS_HISTORIAL.match(linea))
        if dentro:
            indices.add(i)
    return indices


# --------------------------------------------------------------------------
# Comprobaciones
# --------------------------------------------------------------------------

def c1_citas(docs, por_codigo):
    hallazgos = []
    for doc in docs:
        if doc["codigo"] in EXCLUIDOS_CITAS:
            continue
        historial = marcar_historial(doc["lineas"])
        for i, linea in enumerate(doc["lineas"]):
            if i in historial:
                # Los historiales citan numeraciones antiguas a propósito.
                continue
            for m in RE_CITA.finditer(linea):
                destino = m.group(1).upper()
                if destino in EXCLUIDOS_CITAS:
                    continue
                if destino not in por_codigo:
                    hallazgos.append(Hallazgo(
                        "aviso", doc["ruta"], i + 1, "C1",
                        f"cita a `{destino}`, documento no encontrado en el corpus"))
                    continue
                for num in (m.group(2), m.group(3)):
                    if not num:
                        continue
                    if num not in por_codigo[destino]["secciones"]:
                        hallazgos.append(Hallazgo(
                            "error", doc["ruta"], i + 1, "C1",
                            f"`{destino} §{num}` no existe "
                            f"(en {por_codigo[destino]['ruta']})"))
    return hallazgos


def c2_versiones(docs):
    hallazgos = []
    for doc in docs:
        if not doc["es_corpus"]:
            continue
        version = doc["version"]
        if doc.get("codigo_duplicado_de"):
            hallazgos.append(Hallazgo(
                "error", doc["ruta"], 1, "C2",
                f"declara el código `{doc['codigo']}`, ya usado por "
                f"`{doc['codigo_duplicado_de']}` — un código identifica a un único documento"))
        if doc["codigo"] is None:
            hallazgos.append(Hallazgo(
                "aviso", doc["ruta"], 1, "C2",
                "no declara `Código:` en el encabezado — no puede ser destino de citas"))
        if not version:
            continue
        # Versión embebida en el nombre de archivo (fuente conocida de divergencia).
        m = re.search(r"_v(\d+\.\d+(?:\.\d+)?)\.md$", doc["ruta"])
        if m and m.group(1) != version:
            hallazgos.append(Hallazgo(
                "aviso", doc["ruta"], 1, "C2",
                f"nombre de archivo declara v{m.group(1)} pero el encabezado dice v{version} "
                f"(ver GOB-CHK-001 H8)"))
    return hallazgos


def c3_codigos_pt(docs, por_codigo):
    hallazgos = []
    catalogo = por_codigo.get("LIN-PAT-001")
    if not catalogo:
        return [Hallazgo("aviso", "-", 1, "C3",
                         "LIN-PAT-001 no encontrado: no se validan códigos PT")]
    definidos = set(RE_CODIGO_PT.findall("\n".join(catalogo["lineas"])))
    matriz = por_codigo.get("GOB-MAT-001")
    if matriz:
        definidos |= set(RE_CODIGO_PT.findall("\n".join(matriz["lineas"])))

    for doc in docs:
        if not doc["es_corpus"]:
            continue
        if doc["codigo"] in ("LIN-PAT-001", "GOB-MAT-001") or doc["codigo"] in EXCLUIDOS_CITAS:
            continue
        historial = marcar_historial(doc["lineas"])
        for i, linea in enumerate(doc["lineas"]):
            if i in historial:
                continue
            for pt in RE_CODIGO_PT.findall(linea):
                if pt not in definidos:
                    hallazgos.append(Hallazgo(
                        "error", doc["ruta"], i + 1, "C3",
                        f"código `{pt}` no está definido en LIN-PAT-001 (fuente única)"))
    return hallazgos


def c4_duplicados(raiz):
    hallazgos = []
    for canonico, copias in ARTEFACTOS_DUPLICADOS:
        ruta_canon = os.path.join(raiz, canonico)
        if not os.path.isfile(ruta_canon):
            hallazgos.append(Hallazgo("error", canonico, 1, "C4",
                                      "archivo canónico no encontrado"))
            continue
        with open(ruta_canon, "rb") as fh:
            hash_canon = hashlib.sha256(fh.read()).hexdigest()
        for copia in copias:
            ruta_copia = os.path.join(raiz, copia)
            if not os.path.isfile(ruta_copia):
                hallazgos.append(Hallazgo("error", copia, 1, "C4",
                                          "copia controlada no encontrada"))
                continue
            with open(ruta_copia, "rb") as fh:
                if hashlib.sha256(fh.read()).hexdigest() != hash_canon:
                    hallazgos.append(Hallazgo(
                        "error", copia, 1, "C4",
                        f"diverge de su fuente canónica `{canonico}`"))
    return hallazgos


def c6_catalogo(docs, por_codigo, raiz):
    """
    El catálogo de GOB-MAT-001 es el índice del corpus: declara, por cada código,
    el archivo que lo contiene. Verifica que esas rutas existan y apunten al
    documento correcto.

    Nace de un caso real: al renombrar el documento congelado añadiéndole `_OLD`,
    la ruta del catálogo quedó apuntando a un archivo inexistente. C5 no lo detectó
    porque es una ruta escrita en una celda de tabla, no un enlace Markdown.
    """
    hallazgos = []
    matriz = por_codigo.get("GOB-MAT-001")
    if not matriz:
        return [Hallazgo("aviso", "-", 1, "C6",
                         "GOB-MAT-001 no encontrado: no se valida el catálogo")]

    fila = re.compile(r"^\|\s*`([A-Z][A-Z0-9-]+)`\s*\|[^|]*\|[^|]*\|\s*`([^`]+)`\s*\|")
    for i, linea in enumerate(matriz["lineas"]):
        m = fila.match(linea)
        if not m:
            continue
        codigo, ruta = m.group(1), m.group(2).strip()
        if ruta in ("—", "-", ""):
            continue                      # documento pendiente, sin archivo
        completa = os.path.join(raiz, ruta)
        if not os.path.isfile(completa):
            hallazgos.append(Hallazgo(
                "error", matriz["ruta"], i + 1, "C6",
                f"el catálogo declara `{codigo}` en `{ruta}`, archivo que no existe"))
            continue
        # El archivo existe: ¿declara el código que el catálogo le atribuye?
        real = None
        for d in docs:
            if os.path.normpath(d["ruta"]) == os.path.normpath(ruta):
                real = d["codigo"]
                break
        if real is None:
            continue                      # excluido del análisis (p. ej. sufijo _OLD)
        if real != codigo:
            hallazgos.append(Hallazgo(
                "error", matriz["ruta"], i + 1, "C6",
                f"el catálogo asigna `{codigo}` a `{ruta}`, pero ese archivo declara `{real}`"))
    return hallazgos


def c5_enlaces(docs, raiz):
    hallazgos = []
    for doc in docs:
        base = os.path.dirname(os.path.join(raiz, doc["ruta"]))
        for i, linea in enumerate(doc["lineas"]):
            if RE_RUTA_ABSOLUTA.search(linea):
                hallazgos.append(Hallazgo(
                    "error", doc["ruta"], i + 1, "C5",
                    "enlace con ruta absoluta de máquina (usar ruta relativa)"))
            for destino in RE_ENLACE_MD.findall(linea):
                if destino.startswith(("http://", "https://", "#", "mailto:")):
                    continue
                if destino.startswith(("file://", "/")):
                    continue  # ya reportado arriba
                objetivo = destino.split("#")[0]
                if not objetivo:
                    continue
                if not os.path.exists(os.path.join(base, objetivo)):
                    hallazgos.append(Hallazgo(
                        "error", doc["ruta"], i + 1, "C5",
                        f"enlace relativo roto: `{objetivo}`"))
    return hallazgos


# --------------------------------------------------------------------------
# Presentación
# --------------------------------------------------------------------------

def main():
    ap = argparse.ArgumentParser(description="Linter del corpus documental ONP")
    ap.add_argument("--raiz", default=os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    ap.add_argument("--formato", choices=["texto", "gitlab"], default="texto")
    ap.add_argument("--solo", choices=["C1", "C2", "C3", "C4", "C5", "C6"], default=None)
    args = ap.parse_args()

    raiz = os.path.abspath(args.raiz)
    docs, por_codigo = indexar(raiz)

    todos = []
    if args.solo in (None, "C1"):
        todos += c1_citas(docs, por_codigo)
    if args.solo in (None, "C2"):
        todos += c2_versiones(docs)
    if args.solo in (None, "C3"):
        todos += c3_codigos_pt(docs, por_codigo)
    if args.solo in (None, "C4"):
        todos += c4_duplicados(raiz)
    if args.solo in (None, "C5"):
        todos += c5_enlaces(docs, raiz)
    if args.solo in (None, "C6"):
        todos += c6_catalogo(docs, por_codigo, raiz)

    errores = [h for h in todos if h.nivel == "error"]
    avisos = [h for h in todos if h.nivel == "aviso"]

    n_corpus = sum(1 for d in docs if d["es_corpus"])
    print(f"Linter del corpus ONP — {len(docs)} archivos analizados "
          f"({n_corpus} del corpus normativo, {len(por_codigo)} con código)\n")

    por_regla = defaultdict(list)
    for h in todos:
        por_regla[h.regla].append(h)

    nombres = {
        "C1": "Citas cruzadas entre documentos",
        "C2": "Coherencia de versión",
        "C3": "Códigos PT contra LIN-PAT-001",
        "C4": "Artefactos duplicados vs. fuente canónica",
        "C5": "Enlaces",
        "C6": "Catálogo de GOB-MAT-001",
    }
    for regla in ("C1", "C2", "C3", "C4", "C5", "C6"):
        hs = por_regla.get(regla, [])
        n_err = sum(1 for h in hs if h.nivel == "error")
        estado = "OK" if not hs else f"{n_err} error(es), {len(hs) - n_err} aviso(s)"
        print(f"  [{regla}] {nombres[regla]:<44} {estado}")
    print()

    for h in sorted(todos, key=lambda x: (x.nivel != "error", x.archivo, x.linea)):
        etiqueta = "ERROR" if h.nivel == "error" else "aviso"
        print(f"{etiqueta:>5} {h.archivo}:{h.linea} [{h.regla}] {h.mensaje}")

    print(f"\nTotal: {len(errores)} error(es), {len(avisos)} aviso(s)")
    return 1 if errores else 0


if __name__ == "__main__":
    sys.exit(main())
