# LIN-BD-ORA-001-REF — Referencia Rápida y Checklists
## Oficina de Normalización Previsional — OTI
### Código: LIN-BD-ORA-001-REF | Versión 0.1.0 | Estado: Borrador | Complementa: LIN-BD-ORA-001 v0.1.7

---

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 0.1.0 | 2026-05-29 | OTI | Versión inicial: resumen de reglas críticas, convenciones de nomenclatura, plantillas de scripts y checklists de revisión |

---

## Tabla de contenidos

- [Resumen de reglas críticas](#resumen-de-reglas-críticas)
- [Anexo A: Tabla de convenciones de nomenclatura](#anexo-a-tabla-de-convenciones-de-nomenclatura)
- [Anexo B: Plantillas simplificadas de scripts](#anexo-b-plantillas-simplificadas-de-scripts)
- [Anexo C: Campos de auditoría — definición y reglas de llenado](#anexo-c-campos-de-auditoría--definición-y-reglas-de-llenado)
- [Anexo D: Checklist de revisión de base de datos](#anexo-d-checklist-de-revisión-de-base-de-datos)

---

## Resumen de reglas críticas

Las 20 reglas de mayor impacto de LIN-BD-ORA-001. Cada ítem indica la sección normativa de referencia.

### Diseño y modelo

| # | Regla | Ref. |
|---|-------|------|
| 1 | Toda tabla permanente nueva se diseña en **3NF**. Las excepciones (históricas, staging) se documentan en diseño. | 3.1 |
| 2 | Toda tabla permanente tiene una PK técnica `ID_<ENTIDAD> NUMBER(19)`. Las claves de negocio van en constraints `UNIQUE`. | 3.5 |
| 3 | **Borrado lógico** preferido: `IN_ACTIVO = 0`. El `DELETE` físico se reserva para temporales y staging. | 3.6 |
| 4 | Los indicadores lógicos `IN_` usan `NUMBER(1)` con `CHECK (IN (0,1))`. **No** mezclar con `VARCHAR2(1)`. | 3.4 |
| 5 | Las 6 columnas de auditoría (`ID_USUA_CREA`, `FE_USUA_CREA`, `DE_TERM_CREA`, `_MODI` × 3) son obligatorias en toda tabla permanente no temporal. | 5.1 |

### Nomenclatura

| # | Regla | Ref. |
|---|-------|------|
| 6 | Toda tabla lleva prefijo de tipo: `MAE_`, `DET_`, `MOV_`, `HIS_`, `CAT_`, `REL_`, `LOG_`, `AUD_`, `AUX_`, `TMP_`, `TRX_`. | 3.3 |
| 7 | Toda columna lleva prefijo de tipo: `C_` (texto), `N_` (numérico), `FE_` (fecha), `IN_` (flag), `ID_` (clave técnica), `DE_` (descripción/IP). | 3.4 |
| 8 | Toda FK debe tener un índice explícito. Sin índice en FK → Table Lock en `DELETE`/`UPDATE` de la maestra. | 9.1 |
| 9 | Toda tabla y columna tiene `COMMENT ON TABLE` y `COMMENT ON COLUMN` en el diccionario de datos. | 7.5 |

### Codificación PL/SQL

| # | Regla | Ref. |
|---|-------|------|
| 10 | **Bind variables obligatorias** en todo SQL dinámico. Prohibida la concatenación de literales en condiciones de filtro. | 7.2 |
| 11 | **Nunca** `SELECT *` en código de producción. Columnas explícitas siempre. Excepción: `COUNT(*)` y `%ROWTYPE`. | 7.5 |
| 12 | **Nunca** `WHEN OTHERS THEN NULL`. Todo bloque PL/SQL de nivel principal tiene sección `EXCEPTION` con `RAISE_APPLICATION_ERROR` y `FORMAT_ERROR_BACKTRACE`. | 7.4 |
| 13 | **Prohibido** lógica de negocio compleja en triggers de nuevos desarrollos. Triggers solo para auditoría, historial e integridad no modelable con constraints. | 6.0 |
| 14 | `NOCACHE` en secuencias de tablas de alta concurrencia está **prohibido**. Mínimo `CACHE 20`. Los gaps son comportamiento esperado. | 4.6 |

### Seguridad

| # | Regla | Ref. |
|---|-------|------|
| 15 | Privilegios siempre vía **roles** (`ROL_<SIS>_CONSULTA`, `_OPERACION`, `_ADMINISTRACION`, `_EJECUCION`). Nunca directo a usuarios. | 10.1 |
| 16 | La aplicación **nunca** se conecta con el usuario dueño del esquema. Siempre un usuario con privilegios mínimos. | 10.2 |
| 17 | **Prohibido** `WITH GRANT OPTION` y `WITH ADMIN OPTION`. | 10.2 |
| 18 | En pools de conexión: `CLIENT_IDENTIFIER` **obligatorio** al inicio de cada unidad de trabajo. Implementar con `DBMS_SESSION.SET_IDENTIFIER`. | 5.3 |

### Scripts y despliegue

| # | Regla | Ref. |
|---|-------|------|
| 19 | Todo script DML **sin COMMIT automático**. El COMMIT es del operador, luego de validar la comprobación final. | 8.2 |
| 20 | Toda migración define **estrategia de reversa** antes del pase. Para DDL: script compensatorio. Para DML: ROLLBACK o script compensatorio. | 8.2.1 |

---

## Anexo A: Tabla de convenciones de nomenclatura

Resumen de todas las convenciones de LIN-BD-ORA-001 en una sola tabla de referencia rápida.

| Objeto | Patrón | Ejemplo |
|--------|--------|---------|
| Tablespace datos | `TBS_DAT_<ESQUEMA>_<NN>` | `TBS_DAT_APORTACIONES_01` |
| Tablespace índices | `TBS_IDX_<ESQUEMA>_<NN>` | `TBS_IDX_APORTACIONES_01` |
| Tablespace LOB | `TBS_LOB_<ESQUEMA>_<NN>` | `TBS_LOB_EXPEDIENTES_01` |
| Tabla permanente | `<ESQUEMA>.<PREFIJO>_<NOMBRE>` | `APORTACIONES.MAE_APORTANTE` |
| Tabla temp (commit) | `<ESQUEMA>.GTT_<NOMBRE>` | `APORTACIONES.GTT_CALCULO` |
| Tabla temp (sesión) | `<ESQUEMA>.GTS_<NOMBRE>` | `APORTACIONES.GTS_CARGA` |
| Vista | `<ESQUEMA>.VW_<NOMBRE>` | `APORTACIONES.VW_APORTANTE_ACTIVO` |
| Vista materializada | `<ESQUEMA>.MVW_<NOMBRE>` | `APORTACIONES.MVW_RESUMEN_MENSUAL` |
| Índice | `IDX_<TABLA>_<NN>` | `IDX_MAE_APORTANTE_01` |
| Primary Key | `PK_<TABLA>` | `PK_MAE_APORTANTE` |
| Foreign Key | `FK_<TABLA>_<NN>` | `FK_MAE_APORTANTE_01` |
| Check | `CK_<TABLA>_<NN>` | `CK_MAE_APORTANTE_01` |
| Unique | `UK_<TABLA>_<NN>` | `UK_MAE_APORTANTE_01` |
| Not Null | `NN_<TABLA>_<NOMBRE>` | `NN_MAE_APORTANTE_C_NOMBRE` |
| Secuencia | `<ESQUEMA>.SQ_<NOMBRE>_<I>` | `APORTACIONES.SQ_APORTANTE_1` |
| Package | `<ESQUEMA>.PKG_<NOMBRE>` | `APORTACIONES.PKG_CALCULO_APORTE` |
| Stored Procedure | `<ESQUEMA>.SP_<NOMBRE>` | `APORTACIONES.SP_REGISTRAR_APORTE` |
| Function | `<ESQUEMA>.FN_<NOMBRE>` | `APORTACIONES.FN_OBTENER_PERIODO` |
| Trigger | `<ESQUEMA>.TRG_<TIPO>_<TABLA>_<TIEMPO>` | `APORTACIONES.TRG_INS_MAE_APORTANTE_AFTER` |
| DBLink | `DBL_<BD_DESTINO>_<ESQUEMA_DESTINO>` | `DBL_SIAF_PRESUPUESTO` |
| Directorio | `DIR_<NOMBRE>_<NN>` | `DIR_ARCHIVOS_ENTRADA_01` |
| Sinónimo privado | `<ESQUEMA>.SYN_<OBJETO>` | `RECAUDACION.SYN_MAE_APORTANTE` |
| Sinónimo público | `SYN_<ESQUEMA>_<OBJETO>` | `SYN_APORTACIONES_MAE_APORTANTE` |
| Job DBMS_SCHEDULER | `JOB_<ESQUEMA>_<NOMBRE>_<FRECUENCIA>` | `JOB_APORTACIONES_CIERRE_MES_MENS` |
| Rol | `ROL_<SISTEMA>_<NOMBRE>` | `ROL_APORTACIONES_CONSULTA` |
| ACL | `ACL_<BD_SISTEMA>_USERS` | `ACL_APORTACIONES_USERS` |
| Script de pase | `PP_<ORIGEN>_<NUM>_<ESQUEMA>_<TIPO>_<NN>.SQL` | `PP_REQ_1078_APORTACIONES_TB_01.SQL` |

---

## Anexo B: Plantillas simplificadas de scripts

### B.1 Plantilla — Stored Procedure

```sql
CREATE OR REPLACE PROCEDURE <ESQUEMA>.SP_<NOMBRE> (
    p_param1 IN  VARCHAR2,
    p_param2 OUT VARCHAR2
) IS
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : <Descripción>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- Parámetros
--   ENTRADA
--     p_param1  : <descripción>
--   SALIDA
--     p_param2  : <descripción>
-- Observación  : Ninguna
-- ------------------------------------------------------------
-- Modificaciones
-- Motivo       Fecha       Nombre          Descripción
-- ============================================================
    v_variable VARCHAR2(100);
BEGIN
    -- lógica del procedimiento
    NULL;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20999 AND -20000 THEN
            RAISE;
        END IF;
        RAISE_APPLICATION_ERROR(
            -20000,
            'Error en SP_<NOMBRE> [SQLCODE=' || SQLCODE || ']: ' ||
            SQLERRM || ' | ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
        );
END SP_<NOMBRE>;
/
```

### B.2 Plantilla — Function

```sql
CREATE OR REPLACE FUNCTION <ESQUEMA>.FN_<NOMBRE> (
    p_param1 IN VARCHAR2
) RETURN VARCHAR2 IS
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : <Descripción>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- Parámetros
--   ENTRADA
--     p_param1  : <descripción>
--   RETORNO     : <descripción del valor retornado>
-- Observación  : Ninguna
-- ------------------------------------------------------------
-- Modificaciones
-- Motivo       Fecha       Nombre          Descripción
-- ============================================================
    v_resultado VARCHAR2(100);
BEGIN
    RETURN v_resultado;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20999 AND -20000 THEN
            RAISE;
        END IF;
        RAISE_APPLICATION_ERROR(
            -20000,
            'Error en FN_<NOMBRE> [SQLCODE=' || SQLCODE || ']: ' ||
            SQLERRM || ' | ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
        );
END FN_<NOMBRE>;
/
```

### B.3 Plantilla — Package (Spec)

```sql
CREATE OR REPLACE PACKAGE <ESQUEMA>.PKG_<NOMBRE> IS
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : <Descripción del package>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- Observación  : Ninguna
-- ------------------------------------------------------------
-- Modificaciones
-- Motivo       Fecha       Nombre          Descripción
-- ============================================================

    PROCEDURE SP_<NOMBRE_PROC> (
        p_param1 IN VARCHAR2
    );

    FUNCTION FN_<NOMBRE_FUNC> (
        p_param1 IN VARCHAR2
    ) RETURN VARCHAR2;

END PKG_<NOMBRE>;
/
```

### B.4 Plantilla — Package Body

```sql
CREATE OR REPLACE PACKAGE BODY <ESQUEMA>.PKG_<NOMBRE> IS
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : Cuerpo del package PKG_<NOMBRE>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- Observación  : Ninguna
-- ------------------------------------------------------------
-- Modificaciones
-- Motivo       Fecha       Nombre          Descripción
-- ============================================================

    PROCEDURE SP_<NOMBRE_PROC> (p_param1 IN VARCHAR2) IS
    -- -------------------------------------------------------
    -- Descripción  : <Descripción>
    -- -------------------------------------------------------
    BEGIN
        NULL;
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20999 AND -20000 THEN
                RAISE;
            END IF;
            RAISE_APPLICATION_ERROR(
                -20000,
                'Error en SP_<NOMBRE_PROC> [SQLCODE=' || SQLCODE || ']: ' ||
                SQLERRM || ' | ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
            );
    END SP_<NOMBRE_PROC>;

    FUNCTION FN_<NOMBRE_FUNC> (p_param1 IN VARCHAR2)
    RETURN VARCHAR2 IS
    -- -------------------------------------------------------
    -- Descripción  : <Descripción>
    -- -------------------------------------------------------
        v_resultado VARCHAR2(100);
    BEGIN
        RETURN v_resultado;
    EXCEPTION
        WHEN OTHERS THEN
            IF SQLCODE BETWEEN -20999 AND -20000 THEN
                RAISE;
            END IF;
            RAISE_APPLICATION_ERROR(
                -20000,
                'Error en FN_<NOMBRE_FUNC> [SQLCODE=' || SQLCODE || ']: ' ||
                SQLERRM || ' | ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
            );
    END FN_<NOMBRE_FUNC>;

END PKG_<NOMBRE>;
/
```

### B.5 Plantilla — Trigger

```sql
CREATE OR REPLACE TRIGGER <ESQUEMA>.TRG_<TIPO>_<TABLA>_<TIEMPO>
    <TIEMPO> <TIPO>
    ON <ESQUEMA>.<TABLA>
    REFERENCING OLD AS OLD NEW AS NEW
    FOR EACH ROW
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : <Descripción>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- Observación  : Ninguna
-- ------------------------------------------------------------
-- Modificaciones
-- Motivo       Fecha       Nombre          Descripción
-- ============================================================
DECLARE
    v_variable VARCHAR2(39);
BEGIN
    NULL;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20999 AND -20000 THEN
            RAISE;
        END IF;
        RAISE_APPLICATION_ERROR(
            -20000,
            'Error en TRG_<TIPO>_<TABLA>_<TIEMPO> [SQLCODE=' || SQLCODE || ']: ' ||
            SQLERRM || ' | ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
        );
END;
/
```

### B.6 Plantilla — Creación de tabla (script de pase)

```sql
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : Creación de la tabla <ESQUEMA>.<TABLA>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- ============================================================

-- Comprobación inicial
SELECT o.object_name, o.object_type, o.status
FROM user_objects o
WHERE o.object_name = '<TABLA>';

-- Creación de tabla
CREATE TABLE <ESQUEMA>.<TABLA> (
    ID_<ENTIDAD>      NUMBER(19)     CONSTRAINT NN_<TABLA>_ID       NOT NULL,
    C_NOMBRE          VARCHAR2(100)  CONSTRAINT NN_<TABLA>_C_NOMBRE NOT NULL,
    IN_ACTIVO         NUMBER(1)      DEFAULT 1                      NOT NULL,
    -- campos de negocio adicionales ...
    ID_USUA_CREA      VARCHAR2(30)   CONSTRAINT NN_<TABLA>_CREA     NOT NULL,
    FE_USUA_CREA      TIMESTAMP      CONSTRAINT NN_<TABLA>_FECREA   NOT NULL,
    DE_TERM_CREA      VARCHAR2(39)   CONSTRAINT NN_<TABLA>_TERCREA  NOT NULL,
    ID_USUA_MODI      VARCHAR2(30),
    FE_USUA_MODI      TIMESTAMP,
    DE_TERM_MODI      VARCHAR2(39),
    CONSTRAINT CK_<TABLA>_ACTIVO CHECK (IN_ACTIVO IN (0,1)),
    CONSTRAINT PK_<TABLA> PRIMARY KEY (ID_<ENTIDAD>)
)
TABLESPACE TBS_DAT_<ESQUEMA>_01;

-- Comentarios de tabla y columnas
COMMENT ON TABLE  <ESQUEMA>.<TABLA>              IS '<Descripción de la tabla>';
COMMENT ON COLUMN <ESQUEMA>.<TABLA>.ID_<ENTIDAD> IS 'Identificador técnico único del registro';
COMMENT ON COLUMN <ESQUEMA>.<TABLA>.C_NOMBRE     IS '<Descripción>';
-- (agregar COMMENT para cada columna)

-- Comprobación final
SELECT o.object_name, o.object_type, o.status
FROM user_objects o
WHERE o.object_name = '<TABLA>';
```

### B.7 Plantilla — Script DML (actualización de datos)

```sql
-- ============================================================
-- Sistema      : <Sistema>
-- Descripción  : <Descripción del cambio de datos>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número>
-- Autor        : <Nombre>
-- ============================================================

-- Comprobación inicial
SELECT <columnas_relevantes>
FROM   <ESQUEMA>.<TABLA>
WHERE  <condicion>;

-- Actualización
UPDATE <ESQUEMA>.<TABLA>
SET
    C_CAMPO      = '<nuevo_valor>',
    ID_USUA_MODI = COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER),
    FE_USUA_MODI = SYSTIMESTAMP,
    DE_TERM_MODI = SYS_CONTEXT('USERENV', 'IP_ADDRESS')
WHERE <condicion>;

-- Comprobación final
SELECT <columnas_relevantes>
FROM   <ESQUEMA>.<TABLA>
WHERE  <condicion>;

PROMPT 'Revisar los resultados. Si son correctos, ejecutar COMMIT.';
PROMPT 'De lo contrario, ejecutar ROLLBACK.';
```

---

## Anexo C: Campos de auditoría — definición y reglas de llenado

### C.1 Definición completa

```sql
ID_USUA_CREA  VARCHAR2(30)  NOT NULL   -- Usuario Oracle que creó el registro
FE_USUA_CREA  TIMESTAMP     NOT NULL   -- Fecha y hora de creación
DE_TERM_CREA  VARCHAR2(39)  NOT NULL   -- IP/terminal de creación
ID_USUA_MODI  VARCHAR2(30)  NULL       -- Usuario de la última modificación
FE_USUA_MODI  TIMESTAMP     NULL       -- Fecha y hora de la última modificación
DE_TERM_MODI  VARCHAR2(39)  NULL       -- IP/terminal de la última modificación
```

### C.2 Reglas de llenado

| Operación | Acción sobre campos de auditoría |
|-----------|----------------------------------|
| INSERT | Llenar `_CREA` con `COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER)`, `SYSTIMESTAMP` e IP. Dejar `_MODI` en `NULL`. |
| UPDATE | Solo actualizar `_MODI`. Los campos `_CREA` **nunca se modifican**. |
| DELETE | No aplica. Para borrado lógico usar `IN_ACTIVO = 0` con UPDATE. |
| MERGE | Rama INSERT: llenar `_CREA`, `_MODI` en `NULL`. Rama UPDATE: actualizar solo `_MODI`. |

### C.3 Obtención de IP y usuario en pools

```sql
SYS_CONTEXT('USERENV', 'IP_ADDRESS')
COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER)
```

Si la conexión pasa por un pool, `USER` retorna el usuario técnico del pool, no el usuario real. La trazabilidad del usuario final se obtiene mediante el `CLIENT_IDENTIFIER` establecido por la aplicación. Ver implementación en LIN-BD-ORA-001-GIA 3.

### C.4 Tablas exentas

| Tipo | Motivo de excepción |
|------|---------------------|
| `GTT_*` / `GTS_*` | Global Temporary Tables: datos de sesión, no requieren auditoría persistente |
| Tablas de carga masiva staging | Datos transitorios sin ciclo de vida de negocio |

---

## Anexo D: Checklist de revisión de base de datos

Lista de verificación mínima antes de cualquier pase a QA o Producción.

### D.1 Diseño y modelo

- [ ] El modelo está en 3NF (o se documentó la excepción justificada).
- [ ] Todas las tablas permanentes tienen los 6 campos de auditoría.
- [ ] Los nombres de tablas y columnas siguen los prefijos de 3.3 y 3.4.
- [ ] Las claves primarias técnicas y llaves foráneas siguen las reglas de 3.5.
- [ ] Los indicadores `IN_` usan `NUMBER(1)` con `CHECK` explícito.
- [ ] Todas las tablas tienen `COMMENT ON TABLE` y `COMMENT ON COLUMN` para cada columna.
- [ ] Los constraints están nombrados según 4.5 (PK, FK, UK, CK, NN).
- [ ] Las secuencias siguen el patrón de 4.6.
- [ ] Si el esquema contiene LOBs, existe tablespace `TBS_LOB_` dedicado (3.8).

### D.2 Objetos programables

- [ ] Todo SP, FN, PKG y TRG tiene el bloque de encabezado documentado (7.3).
- [ ] No hay `SELECT *` en código de producción (salvo excepciones de 7.5, ítem 2).
- [ ] No hay concatenación de literales en SQL dinámico — se usan bind variables.
- [ ] No hay `COMMIT` dentro de funciones ni en objetos de uso general.
- [ ] Todo bloque PL/SQL tiene sección `EXCEPTION` con `RAISE_APPLICATION_ERROR`.
- [ ] La traducción de errores preserva `SQLCODE` y `FORMAT_ERROR_BACKTRACE`.
- [ ] No hay `WHEN OTHERS THEN NULL` (supresión silenciosa de errores).

### D.3 Scripts de pase

- [ ] El script sigue la nomenclatura `PP_<ORIGEN>_<NUMERO>_<ESQUEMA>_<TIPO>_<NN>.SQL`.
- [ ] El script tiene encabezado, comprobación inicial, cambio y comprobación final.
- [ ] El script usa SPOOL para registro de la ejecución.
- [ ] Los scripts DML no tienen COMMIT automático.
- [ ] Se revisó la evidencia del plan de ejecución (`DBMS_XPLAN`, `DISPLAY_CURSOR` o equivalente) de toda consulta nueva o modificada.
- [ ] Si el pase incluye carga masiva, el script incluye `DBMS_STATS.GATHER_TABLE_STATS` como paso explícito (9.5).

### D.4 Seguridad

- [ ] Los privilegios se asignan a roles, no directamente a usuarios.
- [ ] No se usó `WITH GRANT OPTION` ni `WITH ADMIN OPTION`.
- [ ] Los permisos de UPDATE se especifican a nivel de columna en la documentación del pase.
- [ ] La cuenta de aplicación no tiene el rol DBA ni privilegios de sistema innecesarios.
- [ ] El acceso aplicativo usa cuentas diferenciadas cuando el riesgo operativo o de auditoría lo exige.
- [ ] Los datos sensibles (personales) están identificados y se evaluó el cifrado necesario.
- [ ] Si hubo datos reales en ambientes no productivos, existe aprobación y evidencia de enmascaramiento.

### D.5 Administración

- [ ] La base de datos y sus esquemas están registrados en el catálogo centralizado (LIN-BD-ORA-001-GIA 2).
- [ ] El tablespace objetivo existe y tiene capacidad suficiente.
- [ ] El backup está configurado y probado para el entorno productivo.

### D.6 DBLinks (cuando el pase incluye creación o modificación de un DBLink)

- [ ] El uso del DBLink cuenta con aprobación documentada del arquitecto responsable.
- [ ] El DBLink está registrado en el catálogo centralizado (`OTI_ADMIN.CAT_DBLINK`).
- [ ] El usuario de conexión en la BD destino es dedicado exclusivamente al DBLink y tiene permisos de solo lectura.
- [ ] Las credenciales se gestionan mediante Oracle Wallet — no están expuestas en el DDL.
- [ ] El nombre sigue el patrón `DBL_<BD_DESTINO>_<ESQUEMA_DESTINO>` (4.9).
- [ ] El DBLink está configurado con `CONNECT_TIMEOUT` explícito.
- [ ] El uso está limitado exclusivamente a operaciones `SELECT`. No hay DML ni DDL remoto.
- [ ] Se verificó que no existe API, servicio o vista replicada que pueda sustituir el DBLink.
- [ ] Está declarado un responsable técnico del DBLink en el catálogo.

---

*LIN-BD-ORA-001-REF — Referencia Rápida y Checklists v0.1.0*
*OTI — Oficina de Tecnologías de la Información*
