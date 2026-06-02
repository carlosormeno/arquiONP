# LIN-BD-ORA-001 — Estándar de Base de Datos Oracle ONP
## Oficina de Normalización Previsional — OTI
### Código: LIN-BD-ORA-001 | Versión 0.1.0 | Estado: Borrador | Marco rector: LIN-ARQ-000

---

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 0.1.0 | 2026-05-22 | OTI | Versión inicial |

---

## Tabla de contenidos

- [1 Alcance y vigencia](#1-alcance-y-vigencia)
- [2 Plataforma de base de datos](#2-plataforma-de-base-de-datos)
- [3 Diseño del modelo de datos](#3-diseño-del-modelo-de-datos)
- [4 Nomenclatura de objetos](#4-nomenclatura-de-objetos)
- [5 Campos de auditoría](#5-campos-de-auditoría)
- [6 Objetos programables PL/SQL](#6-objetos-programables-plsql)
- [7 Estándares de codificación PL/SQL](#7-estándares-de-codificación-plsql)
- [8 Scripts de despliegue y control de cambios](#8-scripts-de-despliegue-y-control-de-cambios)
- [9 Optimización y rendimiento](#9-optimización-y-rendimiento)
- [10 Seguridad](#10-seguridad)
- [11 Administración y operación](#11-administración-y-operación)
- [Documentos complementarios](#documentos-complementarios)

---

## Cómo usar este documento

Este documento contiene exclusivamente las **reglas normativas** que todo equipo debe cumplir. Para cada tarea concreta, usa la ruta correspondiente:

| Quiero… | Ir a… |
|---------|-------|
| Entender qué se exige y por qué | Este documento — leer 1 a 11 |
| Implementar los catálogos OTI_ADMIN en base de datos | **LIN-BD-ORA-001-GIA 2** |
| Configurar `CLIENT_IDENTIFIER` en Spring Boot | **LIN-BD-ORA-001-GIA 3** |
| Ver el adapter Java para invocar PL/SQL legacy | **LIN-BD-ORA-001-GIA 5** |
| Ejecutar queries de diagnóstico AWR / ASH | **LIN-BD-ORA-001-GIA 6** |
| Consultar convenciones de nomenclatura de un vistazo | **LIN-BD-ORA-001-REF Anexo A** |
| Copiar una plantilla de SP, FN, PKG, TRG o tabla | **LIN-BD-ORA-001-REF Anexo B** |
| Revisar un pase antes de subirlo a QA o Producción | **LIN-BD-ORA-001-REF Anexo D** |

---

## 1 Alcance y vigencia

### 1.1 Propósito

Este lineamiento establece los estándares de diseño, nomenclatura, programación, seguridad y administración que deben seguir todas las bases de datos desarrolladas, mantenidas o contratadas por la Oficina de Tecnologías de la Información (OTI) de la ONP.

El objetivo es garantizar que los objetos de base de datos producidos por distintos equipos —internos y contratistas— sean coherentes, auditables y mantenibles con el mismo criterio. Asegura además que la capa de datos sea consistente con el componente **CD01 (Base de datos relacional)** definido en el Lineamiento de Diseño y Arquitectura de Software de la ONP.

### 1.2 Ámbito de aplicación

Este estándar aplica a:

- Todos los sistemas con base de datos Oracle administrados por la OTI-ONP.
- Proyectos nuevos y módulos nuevos incorporados a sistemas existentes.
- Contratistas y proveedores que diseñen, desarrollen o mantengan bases de datos bajo contrato para la ONP.
- Scripts de despliegue (DDL y DML) entregados en cualquier pase a entornos de precalidad (ONP_PQA), QA (ONP_QA) o producción (master).

**No aplica** a:

- Bases de datos de terceros sobre las cuales la ONP no tiene control de diseño (ej: sistemas externos de integración).
- Entornos de prueba local/sandbox del desarrollador cuando no constituyan un pase formal.

### 1.3 Relación con otros documentos

| Documento | Relación |
|-----------|----------|
| Lineamiento de Diseño y Arquitectura de Software ONP | Marco arquitectónico; define CD01 y CD02 en la Capa de Datos |
| Lineamiento Estándar de Desarrollo Java ONP | Complementa las convenciones de persistencia (JPA/JDBC) |
| Lineamiento Estándar de APIs REST ONP | Define cómo la capa de aplicación consume la BD |
| Guía de Diseño y Programación ONP v2.0 | Documento de referencia técnica interno del que este lineamiento es actualización |
| **LIN-BD-ORA-001-GIA** | Complemento operativo: script de instalación de esquema OTI_ADMIN, patrón CLIENT_IDENTIFIER en Spring Boot e índice detallado de secciones |

---

## 2 Plataforma de base de datos

### 2.1 Oracle 19c — estándar vigente

**Oracle Database 19c** es la plataforma relacional estándar de la ONP. Todo sistema nuevo o en proceso de modernización debe usar Oracle 19c.

Oracle 19c es la plataforma relacional institucional vigente de la ONP para sistemas nuevos y modernizaciones. Las fechas de soporte del fabricante deben administrarse por OTI/DBA como referencia operativa y actualizarse fuera del cuerpo normativo cuando cambien.

### 2.2 Oracle 11g — legado con restricción de uso

Oracle 11g permanece en producción para sistemas legacy que no han completado su proceso de migración. Su uso está **restringido** bajo las siguientes condiciones:

- Solo se admiten **mantenimientos correctivos** sobre sistemas existentes en 11g.
- No se inician proyectos nuevos en Oracle 11g.
- Los objetos y scripts de mantenimiento para sistemas 11g deben seguir las mismas convenciones de este lineamiento.
- Los equipos a cargo de sistemas en 11g deben mantener un plan de migración hacia Oracle 19c coordinado con la OTI.

> Las convenciones de este lineamiento son compatibles con Oracle 11g y Oracle 19c. Cuando se indica una característica exclusiva de 19c, se señala explícitamente.

### 2.3 Catálogo centralizado de bases de datos

La OTI mantiene un catálogo centralizado y actualizado de todas las bases de datos administradas. Cada registro incluye:

| Campo | Descripción |
|-------|-------------|
| Nombre de BD | Identificador único |
| Versión Oracle | 19c / 11g |
| Propósito | Sistema al que da soporte |
| Esquemas activos | Lista de esquemas y sus responsables |
| Ambiente | Producción (master) / QA (ONP_QA) / Precalidad (ONP_PQA) / Desarrollo (ONP_DESA) |
| Responsable técnico | Contacto DBA y líder de desarrollo |
| Estado | Activo / Legado / En migración |

El catálogo es de registro obligatorio. Ninguna base de datos que sirva a un sistema productivo de la ONP puede operar sin estar registrada en el catálogo.

**Modelo de datos del catálogo** (esquema `OTI_ADMIN` o equivalente centralizado designado por la OTI):

```sql
CREATE TABLE OTI_ADMIN.CAT_BASE_DATOS (
    ID_BASE_DATOS         NUMBER(19)    CONSTRAINT NN_CAT_BDATOS_ID    NOT NULL,
    C_NOMBRE              VARCHAR2(30)  CONSTRAINT NN_CAT_BDATOS_NOM   NOT NULL,
    C_VERSION_ORACLE      VARCHAR2(10)  CONSTRAINT NN_CAT_BDATOS_VER   NOT NULL,
    C_PROPOSITO           VARCHAR2(200) CONSTRAINT NN_CAT_BDATOS_PROP  NOT NULL,
    C_ESQUEMAS_ACTIVOS    VARCHAR2(500),
    C_AMBIENTE            VARCHAR2(20)  CONSTRAINT NN_CAT_BDATOS_AMB   NOT NULL,
    C_RESPONSABLE_DBA     VARCHAR2(100) CONSTRAINT NN_CAT_BDATOS_DBA   NOT NULL,
    C_RESPONSABLE_DESA    VARCHAR2(100) CONSTRAINT NN_CAT_BDATOS_DESA  NOT NULL,
    C_ESTADO              VARCHAR2(20)  CONSTRAINT NN_CAT_BDATOS_EST   NOT NULL,
    C_CIFRADO_RED         VARCHAR2(50),
    FE_REGISTRO           TIMESTAMP     CONSTRAINT NN_CAT_BDATOS_FEREG NOT NULL,
    ID_USUA_CREA          VARCHAR2(30)  CONSTRAINT NN_CAT_BDATOS_UCREA NOT NULL,
    FE_USUA_CREA          TIMESTAMP     CONSTRAINT NN_CAT_BDATOS_FCREA NOT NULL,
    DE_TERM_CREA          VARCHAR2(39)  CONSTRAINT NN_CAT_BDATOS_TCREA NOT NULL,
    ID_USUA_MODI          VARCHAR2(30),
    FE_USUA_MODI          TIMESTAMP,
    DE_TERM_MODI          VARCHAR2(39),
    CONSTRAINT PK_CAT_BASE_DATOS PRIMARY KEY (ID_BASE_DATOS),
    CONSTRAINT UK_CAT_BDATOS_01  UNIQUE (C_NOMBRE),
    CONSTRAINT CK_CAT_BDATOS_VER CHECK (C_VERSION_ORACLE IN ('19c','11g')),
    CONSTRAINT CK_CAT_BDATOS_AMB CHECK (C_AMBIENTE IN (
        'PRODUCCION','QA','PRECALIDAD','DESARROLLO')),
    CONSTRAINT CK_CAT_BDATOS_EST CHECK (C_ESTADO IN (
        'ACTIVO','LEGADO','EN_MIGRACION'))
)
TABLESPACE TBS_DAT_OTI_ADMIN_01;
```

### 2.4 Redefinición basada en ediciones (EBR - Edition-Based Redefinition)

Para sistemas nuevos y modernizaciones bajo Oracle 19c que requieran alta disponibilidad y despliegues con cero tiempo de inactividad (*Zero Downtime*), se debe considerar el uso de **Edition-Based Redefinition (EBR)**:
- EBR permite que coexistan múltiples versiones (ediciones) de objetos PL/SQL, vistas y sinónimos de manera simultánea en la misma base de datos.
- Las aplicaciones pueden migrar de una versión a otra de forma transparente y sin interrumpir la operación de los usuarios.
- El uso de EBR requiere coordinación previa con el equipo de DBA-OTI para habilitar el soporte de ediciones en los esquemas correspondientes.

---

## 3 Diseño del modelo de datos

### 3.1 Normalización

Los modelos de datos de sistemas transaccionales deben diseñarse en **Tercera Forma Normal (3NF)**:

- **1NF**: Todos los atributos son atómicos; no hay grupos repetitivos.
- **2NF**: Todo atributo no clave depende completamente de la clave primaria (elimina dependencias parciales).
- **3NF**: Ningún atributo no clave depende transitivamente de otro atributo no clave.

**Excepciones admitidas:**
- Desnormalización controlada para tablas históricas o de reporte que justifiquen la decisión por razones de rendimiento documentadas en el diseño.
- Tablas de carga/staging que por naturaleza requieren estructuras planas.

### 3.2 Esquemas por dominio funcional

Cada dominio funcional del sistema debe tener su propio esquema Oracle. El esquema agrupa objetos por contexto de negocio y permite aplicar controles de acceso coherentes.

Ejemplos de esquemas por dominio:

| Esquema | Dominio |
|---------|---------|
| `APORTACIONES` | Gestión de aportes previsionales |
| `PRESTACIONES` | Cálculo y pago de pensiones |
| `EXPEDIENTES` | Gestión documentaria |
| `RECAUDACION` | Cobranza y recaudación |
| `SEGURIDAD` | Control de acceso de usuarios |

Los nombres de esquema deben ser en **MAYÚSCULAS**, sin espacios, sin caracteres especiales, y no deben referenciar nombres de proveedores, marcas de hardware ni tecnologías.

### 3.3 Tipos de tablas — prefijos

Toda tabla debe llevar un prefijo que identifica su naturaleza dentro del modelo:

| Prefijo | Tipo | Descripción |
|---------|------|-------------|
| `MAE_` | Maestra | Tablas de entidades principales del negocio |
| `DET_` | Detalle | Tablas dependientes de una maestra |
| `AUD_` | Auditoría | Tablas de registro de cambios (historial de DML) |
| `CAT_` | Catálogo | Tablas de listas de valores y parámetros |
| `AUX_` | Auxiliar | Tablas de soporte a procesos específicos |
| `REL_` | Relación | Tablas de asociación muchos-a-muchos |
| `LOG_` | Log | Tablas de registro de eventos y errores |
| `MOV_` | Movimiento | Tablas de transacciones y movimientos |
| `HIS_` | Histórico | Tablas de datos históricos o archivados |
| `TMP_` | Temporal | Tablas temporales de sesión (GTT) |
| `TRX_` | Transacción externa | Datos recibidos de sistemas externos |

### 3.4 Prefijos de columnas

Todas las columnas deben llevar un prefijo que indica su tipo de dato:

| Prefijo | Tipo de dato | Ejemplo |
|---------|-------------|---------|
| `C_` | Alfanumérico corto (VARCHAR2, CHAR) | `C_NOMBRE`, `C_DESCRIPCION` |
| `N_` | Numérico (NUMBER) | `N_IMPORTE`, `N_CANTIDAD` |
| `FE_` | Fecha/hora (DATE, TIMESTAMP) | `FE_NACIMIENTO`, `FE_PROCESO` |
| `IN_` | Indicador lógico (flag booleano) | `IN_ACTIVO`, `IN_VIGENTE` |
| `ID_` | Identificador técnico o clave foránea numérica | `ID_APORTANTE`, `ID_PERIODO` |
| `DE_` | Descripción textual, terminal o IP de origen | `DE_TERMINAL`, `DE_DESCRIPCION` |

Reglas canónicas de tipado:

- Los identificadores técnicos `ID_` deben usar **`NUMBER(19)`** como estándar institucional en tablas nuevas.
- Si un identificador de negocio es alfanumérico (`DNI`, código externo, UUID textual, correlativo documental), se nombra con el prefijo de su tipo real (`C_` si es texto), no con `ID_`.
- Toda clave foránea debe usar exactamente el mismo tipo, precisión y escala que la clave primaria referenciada.
- Los indicadores lógicos (`IN_`) usan **`NUMBER(1)`** con valores `1` (verdadero) y `0` (falso). No se debe mezclar `VARCHAR2(1)` y `NUMBER(1)` entre sistemas.
- Todo indicador `IN_` debe tener un `CHECK` explícito (`IN (0,1)`) salvo que el motor o el diseño imponga una restricción equivalente verificable.

> **Excepción canónica — campos de auditoría:** Los campos `ID_USUA_CREA` e `ID_USUA_MODI` definidos en 5.1 usan el prefijo `ID_` con tipo `VARCHAR2(30)`. Almacenan el identificador textual del usuario Oracle o del `CLIENT_IDENTIFIER`, no una clave foránea numérica. Esta excepción es parte del estándar institucional y **no requiere ADR**.

### 3.5 Claves primarias, claves foráneas y claves de negocio

Reglas para diseño relacional de tablas nuevas:

- Toda tabla permanente debe tener una **clave primaria técnica**.
- La clave primaria técnica debe ser, como estándar, una columna `ID_<ENTIDAD>` de tipo `NUMBER(19)`.
- Las claves primarias no deben codificar semántica de negocio, estado, año, sede ni otros atributos cambiantes.
- Las claves naturales o de negocio relevantes para integridad funcional deben modelarse con restricciones `UNIQUE` independientes.
- Las tablas de relación muchos-a-muchos (`REL_`) pueden usar PK compuesta únicamente cuando la cardinalidad y el ciclo de vida del registro lo justifiquen claramente.
- No se admiten claves foráneas polimórficas ni columnas que referencien múltiples tablas según un tipo auxiliar.
- Toda FK debe tener una regla explícita de nulabilidad y comportamiento funcional. Si se requiere borrado en cascada o puesta en nulo, debe justificarse en diseño y documentarse en el pase.

### 3.6 Borrado lógico y borrado físico

Como regla general para tablas de negocio transaccional de la ONP:

- Se debe **preferir borrado lógico** mediante `IN_ACTIVO = 0` o una columna de estado equivalente del dominio.
- Cuando se aplique borrado lógico, el cambio debe actualizar los campos de auditoría `_MODI`.
- El borrado físico (`DELETE`) se reserva para tablas temporales, staging, logs efímeros o procesos de depuración/mantenimiento expresamente autorizados.
- Si un proceso requiere purga física periódica de datos, esta debe quedar documentada con su criterio de retención, impacto funcional y estrategia de reversa.

### 3.7 Temporalidad y zona horaria

- Para trazabilidad de negocio y auditoría, el tipo por defecto es `TIMESTAMP`.
- Usar `DATE` solo cuando la hora/minuto/segundo no tenga relevancia funcional.
- Si un sistema integra usuarios o procesos en múltiples zonas horarias, usar `TIMESTAMP WITH LOCAL TIME ZONE` o `TIMESTAMP WITH TIME ZONE` según el caso, con la decisión documentada en diseño.
- La aplicación, los jobs y los procesos batch deben definir explícitamente la zona horaria de referencia cuando el cálculo funcional dependa del tiempo.

### 3.8 Tipos de datos LOB

Oracle soporta dos tipos de objeto grande (LOB) relevantes para los sistemas ONP:

| Tipo | Uso | Prefijo de columna |
|------|-----|-------------------|
| `CLOB` | Contenido de texto largo: documentos, descripciones extensas, texto libre, XML | `C_` |
| `BLOB` | Contenido binario: archivos escaneados, imágenes, PDFs, documentos firmados digitalmente | Sin prefijo estándar — usar nombre descriptivo (`_DOC`, `_FILE`, `_IMG`) según el dominio. El prefijo será formalizado en revisión posterior. |

**Reglas de almacenamiento:**

- Las columnas LOB deben almacenarse en un tablespace dedicado `TBS_LOB_<ESQUEMA>_<NN>` (ver 4.1), separado del tablespace de datos y de índices.
- En Oracle 19c, usar **SecureFiles** como formato de almacenamiento LOB. Es más eficiente que BasicFiles en compresión, deduplicación e I/O.
- Si el contenido supera los 4 KB en promedio, almacenar fuera de línea de la tabla:

```sql
C_CONTENIDO  CLOB,
CONSTRAINT ...
LOB (C_CONTENIDO) STORE AS SECUREFILE (
    TABLESPACE TBS_LOB_EXPEDIENTES_01
    DISABLE STORAGE IN ROW
    COMPRESS
)
```

- No definir columnas LOB en tablas de alta concurrencia transaccional sin análisis previo de rendimiento e impacto en undo/redo.
- Los campos de auditoría (5.1) aplican a las tablas con columnas LOB del mismo modo que a cualquier tabla permanente.
- Las columnas LOB no participan en índices B-Tree estándar. Para búsqueda de texto libre sobre `CLOB`, coordinar con el DBA la habilitación de **Oracle Text** (esquema CTXSYS). Su implementación se detallará en revisión posterior para el esquema EXPEDIENTES.

> Esta sección cubre los aspectos base de tablespace y formato de almacenamiento. La guía completa de SecureFiles, Oracle Text y estrategia de compresión/deduplicación se madurará en revisiones posteriores del lineamiento.

---

## 4 Nomenclatura de objetos

Todas las reglas de esta sección aplican a objetos creados bajo los esquemas de la ONP. Los nombres de objetos no deben referenciar proveedores, marcas ni tecnologías externas.

### 4.1 Tablespaces

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Datos | `TBS_DAT_<ESQUEMA>_<NN>` | `TBS_DAT_APORTACIONES_01` |
| Índices | `TBS_IDX_<ESQUEMA>_<NN>` | `TBS_IDX_APORTACIONES_01` |
| LOB | `TBS_LOB_<ESQUEMA>_<NN>` | `TBS_LOB_EXPEDIENTES_01` |

- `<NN>`: número secuencial de dos dígitos comenzando en 01.
- Los objetos de las cuentas SYS y SYSTEM deben residir exclusivamente en el tablespace SYSTEM.

### 4.2 Tablas

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Permanente | `<ESQUEMA>.<PREFIJO>_<NOMBRE>` | `APORTACIONES.MAE_APORTANTE` |
| Temporal global (commit) | `<ESQUEMA>.GTT_<NOMBRE>` | `APORTACIONES.GTT_CALCULO_TEMP` |
| Temporal global (sesión) | `<ESQUEMA>.GTS_<NOMBRE>` | `APORTACIONES.GTS_CARGA_SESION` |

- `<PREFIJO>`: uno de los prefijos de 3.3.
- Longitud máxima del nombre: 30 caracteres (límite Oracle 11g/12c; Oracle 19c admite 128 pero se mantiene 30 para portabilidad).

### 4.3 Vistas y Vistas Materializadas

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Vista | `<ESQUEMA>.VW_<NOMBRE>` | `APORTACIONES.VW_APORTANTE_ACTIVO` |
| Vista materializada | `<ESQUEMA>.MVW_<NOMBRE>` | `APORTACIONES.MVW_RESUMEN_MENSUAL` |

- Longitud máxima del nombre: 30 caracteres.

**Cuándo usar vista materializada (`MVW_`):**

| Criterio | Aplica |
|---|---|
| Consulta de reporte pesada ejecutada frecuentemente sobre tablas transaccionales grandes | Sí |
| Replicación de datos hacia esquema de solo lectura (OLAP, reporting) | Sí |
| Datos que toleran latencia respecto a la fuente (no tiempo real) | Sí |
| Fuente de datos como alternativa desacoplada a un DBLink | Sí |
| Consultas operacionales en tiempo real que requieren datos al instante | No — usar vista normal |

**Parámetros mínimos a definir al crear una vista materializada:**

- `REFRESH`: `FAST` cuando existen materialize view logs activos sobre las tablas fuente; `COMPLETE` como fallback.
- `ON DEMAND` o `ON COMMIT`: preferir `ON DEMAND` con `DBMS_MVIEW.REFRESH` programado vía `DBMS_SCHEDULER`. Usar `ON COMMIT` solo si la latencia cero es un requerimiento funcional verificado.
- Los materialize view logs requeridos para `REFRESH FAST` deben crearse explícitamente y documentarse en el script de pase.

### 4.4 Índices

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| General | `IDX_<TABLA>_<NN>` | `IDX_MAE_APORTANTE_01` |

- `<NN>`: número secuencial para distinguir múltiples índices sobre la misma tabla.

### 4.5 Constraints

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Primary Key | `PK_<TABLA>` | `PK_MAE_APORTANTE` |
| Foreign Key | `FK_<TABLA>_<NN>` | `FK_MAE_APORTANTE_01` |
| Check | `CK_<TABLA>_<NN>` | `CK_MAE_APORTANTE_01` |
| Unique | `UK_<TABLA>_<NN>` | `UK_MAE_APORTANTE_01` |
| Not Null | `NN_<TABLA>_<NOMBRE>` | `NN_MAE_APORTANTE_C_NOMBRE` |

> Todos los prefijos de constraint llevan `_` como separador. Esta regla unifica el patrón y evita ambigüedades en nombres largos de tabla.

**Compatibilidad con límites de longitud (Oracle 11g/12c):**
Si el nombre de una restricción (especialmente del tipo Not Null `NN_`) supera los **30 caracteres** debido a la longitud de la tabla y columna, se debe abreviar de forma descriptiva el nombre de la tabla o columna en la restricción (ej: `NN_<TABLA_ABREV>_<COL_ABREV>`) o usar un secuencial (ej: `NN_<TABLA_ABREV>_<NN>`) para no violar el límite de longitud y mantener la compatibilidad con el entorno legacy.

### 4.6 Secuencias

| Patrón | Ejemplo |
|--------|---------|
| `<ESQUEMA>.SQ_<NOMBRE>_<I>` | `APORTACIONES.SQ_APORTANTE_1` |

- `<I>`: incremento de la secuencia (normalmente `1`).

**Parámetros estándar obligatorios:**

| Parámetro | Valor estándar | Cuándo ajustar |
|---|---|---|
| `INCREMENT BY` | `1` | Solo si el negocio requiere saltos explícitos |
| `START WITH` | `1` | Ajustar en migraciones cuando ya existen datos en la tabla destino |
| `NOCYCLE` | Obligatorio para PKs y claves de negocio | Solo `CYCLE` en secuencias auxiliares sin impacto en integridad referencial |
| `NOORDER` | Predeterminado para instancia única | Usar `ORDER` únicamente en ambientes Oracle RAC con requerimiento de estricta secuencialidad |
| `CACHE` | `20` para OLTP / `100` para procesos batch masivos | Incrementar si se detecta contención en inserción concurrente alta |
| `NOMINVALUE` / `NOMAXVALUE` | Estándar | No limitar el rango salvo requerimiento específico documentado |

> `NOCACHE` está **prohibido** en tablas de alta concurrencia. La ausencia de caché genera contención en el diccionario de datos bajo carga. Los gaps en la secuencia por rollback o reinicio de instancia son comportamiento esperado y aceptado — no constituyen un defecto.

**Regla de uso para claves primarias:**

- En tablas nuevas, la estrategia institucional preferida para poblar claves primarias técnicas es **secuencia Oracle + asignación explícita en la capa de persistencia o procedimiento de inserción**.
- El uso de columnas `GENERATED AS IDENTITY` en Oracle 19c es admisible solo cuando el componente no requiere compatibilidad operativa con 11g, replicación que dependa del valor previo o patrones de carga que exijan control explícito de la secuencia.
- La aplicación no debe inventar algoritmos propios de generación de claves numéricas si la base de datos ya gobierna la unicidad mediante secuencias.

### 4.7 Packages, Stored Procedures y Functions

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Package (spec) | `<ESQUEMA>.PKG_<NOMBRE>` | `APORTACIONES.PKG_CALCULO_APORTE` |
| Stored Procedure | `<ESQUEMA>.SP_<NOMBRE>` | `APORTACIONES.SP_REGISTRAR_APORTE` |
| Function | `<ESQUEMA>.FN_<NOMBRE>` | `APORTACIONES.FN_OBTENER_PERIODO` |

### 4.8 Triggers

| Patrón | Ejemplo |
|--------|---------|
| `<ESQUEMA>.TRG_<TIPO>_<TABLA>_<TIEMPO>` | `APORTACIONES.TRG_INS_MAE_APORTANTE_AFTER` |

- `<TIPO>`: `INS`, `UPD`, `DEL`, `DDL`, `DB`
- `<TIEMPO>`: `BEFORE`, `AFTER`, `INSTEAD`

### 4.9 DBLinks y Directorios

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| DBLink | `DBL_<BD_DESTINO>_<ESQUEMA_DESTINO>` | `DBL_SIAF_PRESUPUESTO` |
| Directorio | `DIR_<NOMBRE>_<NN>` | `DIR_ARCHIVOS_ENTRADA_01` |

#### 4.9.1 Condiciones para el uso de DBLinks

El uso de DBLinks no está prohibido, pero está **restringido** a escenarios justificados. Solo se autoriza un DBLink cuando se cumplen todas las siguientes condiciones:

**Justificación arquitectónica**

- No existe una alternativa viable con esfuerzo razonable (API REST, replicación, ETL).
- El uso está acotado a integración con sistemas **legados** internos a la ONP que no exponen servicios.
- El DBLink debe ser aprobado por el arquitecto responsable antes de su creación. No es una decisión de nivel de proyecto.

**Restricciones técnicas obligatorias**

- El DBLink solo puede usarse para operaciones de **lectura** (`SELECT`). Queda **prohibido** realizar DML (INSERT, UPDATE, DELETE) ni DDL sobre la base de datos destino a través de un DBLink.
- El usuario de conexión en la base de datos destino debe ser **dedicado exclusivamente al DBLink** y tener permisos de solo lectura sobre los objetos accedidos.
- Las credenciales no deben quedar expuestas en el DDL. Deben gestionarse mediante Oracle Wallet o mecanismo equivalente aprobado por la OTI/DBA.
- Debe configurarse un `CONNECT_TIMEOUT` explícito para evitar bloqueos en cascada ante indisponibilidad del destino.
- El nombre debe seguir el patrón de la sección 4.9: `DBL_<BD_DESTINO>_<ESQUEMA_DESTINO>`.

**Gobernanza y registro**

- Todo DBLink debe estar registrado en el catálogo centralizado de la OTI con los siguientes campos:

| Campo | Descripción |
|-------|-------------|
| Nombre | Identificador del DBLink (`DBL_...`) |
| Base de datos destino | Sistema al que se conecta |
| Propósito | Justificación del uso |
| Objetos accedidos | Tablas o vistas consultadas |
| Responsable técnico | DBA y líder del sistema origen |
| Fecha de creación | Fecha en que fue aprobado y creado |
| Revisión vigente | Fecha de la última validación de necesidad |

- Debe existir un **responsable declarado** del DBLink. Si el responsable cambia, el registro debe actualizarse.
- El equipo responsable debe revisar anualmente si el DBLink sigue siendo necesario o puede ser reemplazado por una solución más desacoplada.

**Modelo de datos del catálogo de DBLinks** (esquema `OTI_ADMIN` o equivalente centralizado):

```sql
CREATE TABLE OTI_ADMIN.CAT_DBLINK (
    ID_DBLINK              NUMBER(19)    CONSTRAINT NN_CAT_DBLINK_ID    NOT NULL,
    C_NOMBRE               VARCHAR2(128) CONSTRAINT NN_CAT_DBLINK_NOM   NOT NULL,
    C_BD_DESTINO           VARCHAR2(30)  CONSTRAINT NN_CAT_DBLINK_BD    NOT NULL,
    C_PROPOSITO            VARCHAR2(500) CONSTRAINT NN_CAT_DBLINK_PROP  NOT NULL,
    C_OBJETOS_ACCEDIDOS    VARCHAR2(500) CONSTRAINT NN_CAT_DBLINK_OBJ   NOT NULL,
    C_RESP_TECNICO         VARCHAR2(100) CONSTRAINT NN_CAT_DBLINK_RESP  NOT NULL,
    FE_CREACION            TIMESTAMP     CONSTRAINT NN_CAT_DBLINK_FCR   NOT NULL,
    FE_REVISION_VIGENTE    TIMESTAMP,
    C_ESTADO               VARCHAR2(20)  CONSTRAINT NN_CAT_DBLINK_EST   NOT NULL,
    C_OBSERVACIONES        VARCHAR2(500),
    ID_USUA_CREA           VARCHAR2(30)  CONSTRAINT NN_CAT_DBLINK_UCREA NOT NULL,
    FE_USUA_CREA           TIMESTAMP     CONSTRAINT NN_CAT_DBLINK_FCREA NOT NULL,
    DE_TERM_CREA           VARCHAR2(39)  CONSTRAINT NN_CAT_DBLINK_TCREA NOT NULL,
    ID_USUA_MODI           VARCHAR2(30),
    FE_USUA_MODI           TIMESTAMP,
    DE_TERM_MODI           VARCHAR2(39),
    CONSTRAINT PK_CAT_DBLINK     PRIMARY KEY (ID_DBLINK),
    CONSTRAINT UK_CAT_DBLINK_01  UNIQUE (C_NOMBRE),
    CONSTRAINT CK_CAT_DBLINK_EST CHECK (C_ESTADO IN (
        'ACTIVO','EN_REVISION','DESACTIVADO'))
)
TABLESPACE TBS_DAT_OTI_ADMIN_01;
```

**Condiciones de exclusión — no se autoriza el uso de DBLink si:**

- El sistema destino es externo a la infraestructura de la ONP.
- La operación requiere escritura o control transaccional distribuido.
- Ya existe una API, servicio o vista replicada que expone los datos requeridos.
- El sistema destino está en proceso de migración o modernización activa.

> Los DBLinks representan un acoplamiento directo entre bases de datos. Su uso debe ser transitorio cuando sea posible, con un plan de reemplazo por integración vía servicios a mediano plazo.

### 4.10 Roles y ACLs

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Rol | `ROL_<SISTEMA>_<NOMBRE>` | `ROL_APORTACIONES_CONSULTA` |
| ACL | `ACL_<BD_SISTEMA>_USERS` | `ACL_APORTACIONES_USERS` |

### 4.11 Sinónimos

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Sinónimo privado | `<ESQUEMA>.SYN_<OBJETO>` | `RECAUDACION.SYN_MAE_APORTANTE` |
| Sinónimo público | `SYN_<ESQUEMA>_<OBJETO>` | `SYN_APORTACIONES_MAE_APORTANTE` |

**Cuándo usar sinónimos:**

- Para desacoplar un esquema consumidor de la ubicación física del objeto, manteniendo un punto de resolución controlado por DBA o por el dueño del esquema.
- Para desacoplar el código del nombre físico del objeto destino: si el objeto fuente cambia de esquema, solo se actualiza el sinónimo, no el código dependiente.
- Para exponer una vista o tabla de solo lectura a un esquema de reporting sin otorgar privilegios directos sobre el objeto base.

**Restricciones:**

- La regla general del estándar sigue siendo **referenciar objetos con esquema explícito** en SQL y PL/SQL propio del sistema.
- El uso de sinónimos es una excepción controlada para integración entre esquemas, compatibilidad con legado o desacoplamiento operativo aprobado.
- Los **sinónimos públicos** solo pueden crearse con aprobación del DBA. Afectan a todos los usuarios de la instancia y pueden generar conflictos de nombres. Preferir siempre sinónimos privados.
- Un sinónimo no debe apuntar a un objeto inexistente. Verificar existencia del objeto destino antes de crear el sinónimo.
- El objeto destino del sinónimo debe estar registrado en el catálogo si pertenece a un esquema distinto al del sistema consumidor.

> Si un sinónimo reemplaza un acceso que antes se hacía vía DBLink, documentar el cambio en el catálogo de DBLinks como parte de la estrategia de desacoplamiento.

### 4.12 Jobs de DBMS_SCHEDULER

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Job | `JOB_<ESQUEMA>_<NOMBRE>_<FRECUENCIA>` | `JOB_APORTACIONES_CIERRE_MES_MENS` |

Abreviaturas de frecuencia:

| Abreviatura | Frecuencia |
|---|---|
| `DIAR` | Diaria |
| `SEMA` | Semanal |
| `MENS` | Mensual |
| `ANUA` | Anual |
| `HORA` | Por hora |
| `DEMA` | A demanda (no recurrente) |

**Requisitos mínimos para todo job productivo:**

- Debe estar registrado en el catálogo centralizado de la OTI con: nombre, esquema, propósito, frecuencia, tiempo de ejecución esperado, responsable técnico y responsable funcional.
- Debe generar log de ejecución con resultado (`SUCCEEDED` / `FAILED`) consultable vía `DBA_SCHEDULER_JOB_LOG` o almacenado en una tabla institucional de log.
- Debe tener alertas configuradas ante fallo dirigidas al DBA y al responsable técnico del sistema.
- Si la ejecución supera el tiempo esperado, debe generar alerta de latencia.
- El job debe ser idempotente o tener manejo de reintento controlado documentado.

**Modelo de datos del catálogo de jobs** (esquema `OTI_ADMIN` o equivalente centralizado):

```sql
CREATE TABLE OTI_ADMIN.CAT_JOB_SCHEDULER (
    ID_JOB                 NUMBER(19)    CONSTRAINT NN_CAT_JOB_ID       NOT NULL,
    C_NOMBRE               VARCHAR2(128) CONSTRAINT NN_CAT_JOB_NOM      NOT NULL,
    C_ESQUEMA              VARCHAR2(30)  CONSTRAINT NN_CAT_JOB_ESQ      NOT NULL,
    C_PROPOSITO            VARCHAR2(500) CONSTRAINT NN_CAT_JOB_PROP     NOT NULL,
    C_FRECUENCIA           VARCHAR2(10)  CONSTRAINT NN_CAT_JOB_FREC     NOT NULL,
    N_TIEMPO_ESPERADO_SEG  NUMBER(10)    CONSTRAINT NN_CAT_JOB_TIEMP    NOT NULL,
    C_RESP_TECNICO         VARCHAR2(100) CONSTRAINT NN_CAT_JOB_RTEC     NOT NULL,
    C_RESP_FUNCIONAL       VARCHAR2(100) CONSTRAINT NN_CAT_JOB_RFUN     NOT NULL,
    C_ESTADO               VARCHAR2(20)  CONSTRAINT NN_CAT_JOB_EST      NOT NULL,
    C_OBSERVACIONES        VARCHAR2(500),
    ID_USUA_CREA           VARCHAR2(30)  CONSTRAINT NN_CAT_JOB_UCREA    NOT NULL,
    FE_USUA_CREA           TIMESTAMP     CONSTRAINT NN_CAT_JOB_FCREA    NOT NULL,
    DE_TERM_CREA           VARCHAR2(39)  CONSTRAINT NN_CAT_JOB_TCREA    NOT NULL,
    ID_USUA_MODI           VARCHAR2(30),
    FE_USUA_MODI           TIMESTAMP,
    DE_TERM_MODI           VARCHAR2(39),
    CONSTRAINT PK_CAT_JOB_SCHEDULER PRIMARY KEY (ID_JOB),
    CONSTRAINT UK_CAT_JOB_01        UNIQUE (C_NOMBRE),
    CONSTRAINT CK_CAT_JOB_FREC      CHECK (C_FRECUENCIA IN (
        'DIAR','SEMA','MENS','ANUA','HORA','DEMA')),
    CONSTRAINT CK_CAT_JOB_EST       CHECK (C_ESTADO IN (
        'ACTIVO','DESACTIVADO','EN_REVISION'))
)
TABLESPACE TBS_DAT_OTI_ADMIN_01;
```

**Prohibiciones:**

- No crear jobs bajo las cuentas `SYS` o `SYSTEM`.
- No usar `DBMS_JOB` (API deprecada). Usar exclusivamente `DBMS_SCHEDULER`.
- No dejar jobs en estado `DISABLED` en producción sin una fecha de reactivación documentada y aprobada.

---

## 5 Campos de auditoría

### 5.1 Definición

Las siguientes seis columnas son **obligatorias en todas las tablas permanentes no temporales**:

| Columna | Tipo | Nulabilidad | Descripción |
|---------|------|-------------|-------------|
| `ID_USUA_CREA` | `VARCHAR2(30)` | `NOT NULL` | Usuario que creó el registro |
| `FE_USUA_CREA` | `TIMESTAMP` | `NOT NULL` | Fecha y hora de creación |
| `DE_TERM_CREA` | `VARCHAR2(39)` | `NOT NULL` | Terminal/IP de creación |
| `ID_USUA_MODI` | `VARCHAR2(30)` | `NULL` | Usuario que realizó la última modificación |
| `FE_USUA_MODI` | `TIMESTAMP` | `NULL` | Fecha y hora de la última modificación |
| `DE_TERM_MODI` | `VARCHAR2(39)` | `NULL` | Terminal/IP de la última modificación |

### 5.2 Excepciones

Los campos de auditoría **no aplican** a:

- `GLOBAL TEMPORARY TABLE` (GTT / GTS).
- Tablas de carga/staging usadas exclusivamente en procesos batch de carga masiva sin uso transaccional posterior.

### 5.3 Reglas de llenado

**En sentencias INSERT:**

```sql
INSERT INTO esquema.tabla (
    ...,
    ID_USUA_CREA,
    FE_USUA_CREA,
    DE_TERM_CREA,
    ID_USUA_MODI,
    FE_USUA_MODI,
    DE_TERM_MODI
) VALUES (
    ...,
    COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER),
    SYSTIMESTAMP,
    SYS_CONTEXT('USERENV', 'IP_ADDRESS'),
    NULL,
    NULL,
    NULL
);
```

**En sentencias UPDATE** (solo se actualizan los campos `_MODI`; los `_CREA` nunca se modifican):

```sql
UPDATE esquema.tabla
SET
    C_CAMPO = :v_campo,
    ID_USUA_MODI = COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER),
    FE_USUA_MODI = SYSTIMESTAMP,
    DE_TERM_MODI = SYS_CONTEXT('USERENV', 'IP_ADDRESS')
WHERE ID_<ENTIDAD> = :v_id;
```

> **Trazabilidad en Pools de Conexiones:** En aplicaciones modernas que se conectan mediante un pool de base de datos común (donde la función `USER` retorna el usuario técnico del pool y no al usuario real de negocio), es **obligatorio** configurar el identificador del usuario de la sesión desde la aplicación mediante `DBMS_SESSION.SET_IDENTIFIER` para que `SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER')` capture la identidad real del usuario final.

**Regla de implementación obligatoria para software:**

- Todo componente de acceso a datos institucional (JDBC, JPA, batch, job o integración) debe establecer `CLIENT_IDENTIFIER` al inicio de la unidad de trabajo y limpiarlo al liberar la conexión al pool.
- No se admite delegar la trazabilidad únicamente a que cada sentencia `INSERT`/`UPDATE` recuerde llenar manualmente los campos de auditoría si el framework o la capa de persistencia permite centralizarlo.
- Para nuevos desarrollos, se debe preferir un mecanismo transversal verificable: trigger simple de auditoría, interceptor de persistencia, procedimiento de acceso institucional o capa común equivalente.

---

## 6 Objetos programables PL/SQL

### 6.0 Gobierno de lógica de negocio en PL/SQL

#### Contexto

La ONP cuenta con procedures, packages y functions que contienen lógica de negocio crítica activa en producción: cálculos de pensión, validación de aportes, liquidaciones y otras reglas del dominio previsional. Esta lógica no puede retirarse ni modificarse sin análisis de impacto, pruebas de regresión y aprobación técnica.

El objetivo de esta sección no es eliminar ese legado, sino **gobernarlo**: hacerlo visible, controlado, encapsulado y progresivamente reemplazable cuando exista justificación real.

#### Categorías de uso PL/SQL

| Categoría | Descripción | Estado |
|---|---|---|
| **Técnico permitido** | Constraints, índices, vistas, triggers de auditoría, tablas históricas | Permitido bajo estándar 6 y 7 |
| **Batch permitido** | Procesos masivos o intensivos en datos donde PL/SQL tiene ventaja técnica real | Permitido con justificación documentada en ADR |
| **Legacy crítico** | Lógica de negocio existente (cálculos, reglas, liquidaciones) en procedures/packages activos | Permitido; requiere inventario, encapsulamiento y pruebas de caracterización |
| **Nueva lógica de negocio core** | Nueva regla de negocio core implementada en PL/SQL | **Restringido** — requiere ADR aprobado por Arquitectura |
| **Trigger con lógica de negocio compleja** | Lógica de negocio embebida en trigger | **Prohibido** para nuevos desarrollos |
| **Lógica duplicada Java/PL/SQL** | La misma regla implementada en dos lugares | **Prohibido** salvo transición documentada con fecha de cierre |

#### Catálogo de objetos PL/SQL con lógica de negocio

Todo procedure, package o function que contenga lógica de negocio debe estar registrado en el catálogo institucional. Los campos mínimos son:

| Campo | Descripción |
|---|---|
| Objeto | Nombre completo (`ESQUEMA.PKG_NOMBRE.PROCEDURE`) |
| Tipo | Package / Stored Procedure / Function |
| Sistema consumidor | Aplicación, batch, job o integración que lo invoca |
| Propósito funcional | Qué regla o proceso de negocio ejecuta |
| Entradas | Parámetros, tablas leídas, dependencias externas |
| Salidas | Resultado, cursores, códigos de error, tablas modificadas |
| Criticidad | Alta / Media / Baja |
| Dueño funcional | Área usuaria responsable del proceso |
| Dueño técnico | Equipo responsable del código |
| Frecuencia | Online / Batch diario / Mensual / Demanda |
| Estrategia futura | Mantener / Encapsular / Refactorizar / Reemplazar / Retirar |

**Modelo mínimo recomendado del catálogo** (tabla institucional o registro equivalente):

```sql
CREATE TABLE CATALOGO_PLSQL_LEGACY (
    ID_CATALOGO           NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ESQUEMA               VARCHAR2(30)   NOT NULL,
    NOMBRE_OBJETO         VARCHAR2(128)  NOT NULL,
    TIPO_OBJETO           VARCHAR2(20)   NOT NULL,
    SISTEMA_CONSUMIDOR    VARCHAR2(100)  NOT NULL,
    RESPONSABLE_TECNICO   VARCHAR2(100)  NOT NULL,
    DESCRIPCION_FUNCIONAL VARCHAR2(1000) NOT NULL,
    CATEGORIA_USO         VARCHAR2(30)   NOT NULL,
    ESTADO_REGISTRO       VARCHAR2(20)   NOT NULL,
    FECHA_REGISTRO        DATE           NOT NULL,
    OBSERVACIONES         VARCHAR2(1000)
);
```

> **Nota de implementación:** La tabla anterior es una referencia de campos mínimos. La implementación productiva en el esquema `OTI_ADMIN` debe aplicar las convenciones de nomenclatura de columnas (3.4), los 6 campos de auditoría (5.1) y el tipo `TIMESTAMP` para fechas (3.7). El DDL completo alineado con el estándar se encuentra en **LIN-BD-ORA-001-GIA 2.4**.

#### Estrategia de convivencia con sistemas Java modernos

Cuando un servicio Java necesite invocar lógica de negocio que reside en un procedure/package legacy, debe hacerlo a través de un **adapter de infraestructura** claramente identificado. La lógica legacy no debe invocarse directamente desde capas de aplicación o dominio.

```
Application Service (Java)
        ↓
Port de salida (interfaz — capa domain)
        ↓
Adapter PL/SQL (capa infrastructure)
        ↓
Package / Procedure Oracle legacy
```

> El adapter Spring Boot de referencia (`CalculoPensionOracleAdapter`) está en **LIN-BD-ORA-001-GIA 5** con instrucciones de configuración.

**Reglas para adapters que invocan PL/SQL legacy:**

- El adapter es la única clase que conoce el nombre del procedure/package
- Los errores Oracle (`ORA-XXXXX`) se traducen a excepciones de dominio antes de salir del adapter
- La duración de la llamada se registra con log estructurado (`DEBUG` en desarrollo, `INFO` si supera umbral de latencia)
- El adapter tiene pruebas de integración con `OracleContainer` (Testcontainers) que validan el contrato de entrada/salida

#### Pruebas de caracterización

Todo cambio en un procedure de categoría **"Legacy crítico"** requiere pruebas de caracterización escritas **antes** del cambio: mínimo 3 casos (base, borde, error conocido) con resultados tomados de ejecución real. Son la red de seguridad del refactor.

> Ver **LIN-BD-ORA-001-GIA 5** para la guía completa con Testcontainers + OracleContainer.

#### Checklist mínimo para un procedure legacy crítico

- [ ] Registrado en el catálogo PL/SQL con todos los campos requeridos
- [ ] Dueño técnico y funcional identificados
- [ ] Su invocación desde Java está encapsulada en un adapter de infraestructura
- [ ] Los errores Oracle se traducen a excepciones de aplicación en el adapter
- [ ] **Pruebas de caracterización escritas** (mínimo 3 casos) antes de cualquier cambio funcional
- [ ] Existe al menos una prueba de integración que valida el contrato del adapter
- [ ] La llamada genera log estructurado con duración y resultado

---

### 6.1 Cuándo usar cada tipo de objeto

| Objeto | Cuándo usarlo |
|--------|---------------|
| **Stored Procedure** | Lógica de negocio con efectos secundarios (INSERT, UPDATE, DELETE). No retorna valor directo. |
| **Function** | Cálculos y consultas que retornan un valor escalar. No debe tener efectos secundarios (DML). |
| **Package** | Agrupar procedimientos y funciones relacionados por dominio funcional. Reduce dependencias y mejora la gestión de privilegios. |
| **Trigger** | Solo para: (a) llenado automático de campos de auditoría, (b) mantenimiento de tablas históricas/AUD_, (c) validaciones de integridad no modelables con constraints. Evitar lógica de negocio compleja en triggers. |
| **View** | Simplificar acceso a consultas complejas o restringir columnas visibles por rol. |

### 6.2 Reglas de diseño de objetos programables

- Todo objeto programable debe pertenecer a un esquema de negocio. Ningún objeto de aplicación reside en los esquemas SYS o SYSTEM.
- Los packages son la forma preferida de empaquetar lógica: agrupa spec y body bajo un nombre único, facilita la gestión de dependencias y la compilación incremental.
- Una function no debe emitir `COMMIT` ni `ROLLBACK`. El control transaccional es responsabilidad del llamador o del stored procedure de nivel superior.
- Los stored procedures tampoco emiten `COMMIT` salvo que sean el punto de cierre de una unidad lógica de trabajo claramente definida y documentada.
- Toda excepción no controlada debe capturarse, registrarse o enriquecer su contexto funcional, y relanzarse preservando la mayor cantidad posible de diagnóstico técnico (ver 7.4).

---

## 7 Estándares de codificación PL/SQL

### 7.1 Formato y estilo

| Regla | Detalle |
|-------|---------|
| **Keywords SQL** | Siempre en MAYÚSCULAS: `SELECT`, `FROM`, `WHERE`, `INSERT`, `UPDATE`, `DELETE`, `BEGIN`, `END`, `IF`, `THEN`, `ELSE`, `LOOP`, `CURSOR`, `DECLARE`, etc. |
| **Nombres de objetos** | En el estilo dominante del repositorio o sistema. Si no existe convención previa, preferir minúsculas dentro del código: `aportaciones.mae_aportante`, `n_importe` |
| **Variables** | En MAYÚSCULAS con prefijo: `V_` para variables locales, `P_` para parámetros (`V_NOMBRE`, `P_ID_PERIODO`) |
| **Longitud de línea** | Máximo 80 caracteres por línea |
| **Indentación** | TAB equivalente a 3 espacios |
| **Comentarios** | Solo con `--`. No usar bloques `/* */` salvo en encabezados de objeto |
| **Alias de tablas** | Obligatorio en consultas con más de una tabla. El alias debe ser descriptivo o abreviatura reconocible |

### 7.2 Bind variables

El uso de bind variables es **obligatorio** en todo código PL/SQL y SQL dinámico. Queda prohibida la concatenación de literales para construir condiciones de filtro.

**Incorrecto:**
```sql
-- Vulnerable a SQL injection y evita el plan cache
EXECUTE IMMEDIATE
    'SELECT * FROM mae_aportante WHERE n_dni = ' || p_dni;
```

**Correcto:**
```sql
EXECUTE IMMEDIATE
    'SELECT * FROM mae_aportante WHERE n_dni = :1'
    USING p_dni;
```

En SQL estático, el uso de bind variables ocurre a través de variables PL/SQL, parámetros o cursores:

```sql
CURSOR c_aportante IS
    SELECT id_aportante, c_nombre
    FROM aportaciones.mae_aportante
    WHERE n_dni = p_dni
      AND in_activo = 1;
```

### 7.3 Documentación de encabezado

Todo objeto programable (SP, FN, PKG spec, PKG body, TRG) debe comenzar con el siguiente bloque de encabezado:

```sql
-- ============================================================
-- Sistema      : <Nombre del sistema>
-- Descripción  : <Descripción del propósito del objeto>
-- Fecha Crea   : DD/MM/YYYY
-- Requerimiento: <Número de requerimiento o ticket>
-- Autor        : <Nombre completo del autor>
-- Parámetros
--   ENTRADA
--     <param>  : <descripción>
--   SALIDA
--     <param>  : <descripción>
-- Observación  : <notas adicionales o Ninguna>
-- ------------------------------------------------------------
-- Modificaciones
-- Motivo       Fecha       Nombre          Descripción
-- ============================================================
```

El bloque de modificaciones se actualiza en cada cambio posterior. Nunca se sobreescribe el encabezado original.

### 7.4 Manejo de excepciones

Todo bloque PL/SQL de nivel principal debe incluir una sección EXCEPTION que capture errores inesperados:

- Los códigos de error de aplicación deben estar en el rango `-20000` a `-20999`.
- El mensaje debe identificar el objeto donde ocurre el error.
- Cuando se traduzca una excepción, el mensaje debe incluir contexto funcional suficiente y el `backtrace` técnico mediante `DBMS_UTILITY.FORMAT_ERROR_BACKTRACE` o mecanismo equivalente.
- Si el error ya es un `RAISE_APPLICATION_ERROR` emitido por una capa inferior y no requiere traducción adicional, se debe propagar sin reemplazarlo innecesariamente.
- Nunca suprimir excepciones con un bloque `WHEN OTHERS THEN NULL`.

```sql
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(
            -20001,
            'No se encontró información en <NombreObjeto>. ' ||
            DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
        );
    WHEN TOO_MANY_ROWS THEN
        RAISE_APPLICATION_ERROR(
            -20002,
            'Se encontraron múltiples registros en <NombreObjeto>. ' ||
            DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
        );
    WHEN OTHERS THEN
        IF SQLCODE BETWEEN -20999 AND -20000 THEN
            RAISE;
        END IF;

        RAISE_APPLICATION_ERROR(
            -20000,
            'Error en <NombreObjeto> [SQLCODE=' || SQLCODE || ']: ' ||
            SQLERRM || ' | ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE
        );
END;
```

### 7.5 Buenas prácticas de codificación

Las siguientes reglas son de cumplimiento obligatorio en todo código PL/SQL y SQL producido para la ONP:

1. **Especificar el esquema por defecto**: `SELECT ... FROM aportaciones.mae_aportante`, nunca `SELECT ... FROM mae_aportante` sin esquema, salvo cuando exista un sinónimo aprobado según 4.11.
2. **No usar `SELECT *`**: Listar explícitamente las columnas requeridas. Excepción: uso de `COUNT(*)` o cursores con `%ROWTYPE`.
3. **Columnas explícitas en INSERT**: Siempre especificar la lista de columnas en sentencias INSERT.
4. **No usar `COMMIT` dentro de objetos almacenados** salvo que sean el punto de cierre documentado de una unidad de trabajo.
5. **Verificar existencia eficientemente**: Evitar `SELECT COUNT(*) INTO v_cnt` para comprobar existencia cuando no se necesita el conteo. Preferir `EXISTS`, `SELECT 1 ... FETCH FIRST 1 ROW ONLY`, cursores o acceso directo con manejo de `NO_DATA_FOUND`, según el caso.
6. **Operador `=` en lugar de `IN` con valor único**: Para comparación con un solo valor literal usar `=`. Reservar `IN` para listas reales.
7. **`GROUP BY` sin aliases**: No usar alias de columnas en la cláusula `GROUP BY`. Repetir la expresión o usar la posición numérica.
8. **No convertir tipos implícitamente**: Elegir el tipo de dato adecuado para evitar conversiones automáticas que invalidan índices.
9. **Dimensionar `VARCHAR2` correctamente**: Definir solo el tamaño necesario para almacenar el dato. Oversizing desperdicia memoria en SGA/PGA.
10. **Insertar `NULL` explícito**: Usar la palabra reservada `NULL` en INSERT para distinguir una inserción nula de un olvido.
11. **Cada asignación SET en línea separada**: En UPDATE, cada columna del SET va en su propia línea para facilitar lectura y auditoría.
12. **`COMMENT ON TABLE` y `COMMENT ON COLUMN` obligatorios**: Todo objeto de tabla y cada columna deben tener su comentario descriptivo registrado en el diccionario de datos.
13. **Priorizar claridad semántica en el SQL**: El orden de tablas y predicados debe favorecer mantenibilidad. Las decisiones de rendimiento se validan con plan de ejecución real, no por el orden textual del `FROM`.
14. **`TERMOUT OFF` para scripts de volumen**: Usar `SET TERMOUT OFF` cuando el script procesa más de 1000 registros o emite más de 1000 DML, para reducir la carga en el cliente.
15. **Evitar roles DBA en cuentas de aplicación**: Las cuentas de servicio que usa la aplicación no deben tener más privilegios de los estrictamente necesarios.
16. **Analizar siempre el plan de ejecución**: Antes de un pase a QA, todo SQL nuevo o modificado debe tener evidencia revisada con `DBMS_XPLAN.DISPLAY` o `DBMS_XPLAN.DISPLAY_CURSOR` cuando exista ejecución real representativa.
17. **Crear solo los índices necesarios**: Un índice innecesario penaliza INSERT/UPDATE/DELETE sin beneficio en la consulta objetivo.
18. **No conectarse directamente al esquema**: La aplicación debe conectarse con un usuario que tiene los privilegios de acceso necesarios al esquema, nunca con el usuario dueño del esquema.
19. **Objetos de SYS/SYSTEM solo en tablespace SYSTEM**: No crear objetos de aplicación en los tablespaces de sistema.
20. **No otorgar privilegios con `WITH GRANT OPTION`** ni roles con `WITH ADMIN OPTION`.

### 7.6 Sentencia MERGE (UPSERT)

El comando `MERGE` combina lógica de `INSERT` y `UPDATE` en una sola sentencia. Se admite cuando la lógica requiere insertar un registro si no existe o actualizarlo si ya existe, y **ambas ramas son posibles en ejecución real**. No usar `MERGE` como sustituto de un `INSERT` o `UPDATE` simple cuando solo una rama es posible.

**Reglas de uso obligatorias:**

- El uso de bind variables es **obligatorio** en las condiciones `ON` y en los valores de las cláusulas `WHEN MATCHED` y `WHEN NOT MATCHED` (misma regla que 7.2).
- Los campos de auditoría se llenan según la rama ejecutada:
  - `WHEN NOT MATCHED THEN INSERT`: llenar los seis campos, con `_MODI` en `NULL` (mismo criterio que INSERT en 5.3).
  - `WHEN MATCHED THEN UPDATE`: actualizar solo los campos `_MODI`; los `_CREA` **nunca se modifican**.

```sql
MERGE INTO aportaciones.mae_aportante tgt
USING (
    SELECT :v_dni     AS n_dni,
           :v_nombre  AS c_nombre
    FROM   dual
) src
ON (tgt.n_dni = src.n_dni)
WHEN MATCHED THEN
    UPDATE SET
        tgt.c_nombre     = src.c_nombre,
        tgt.id_usua_modi = COALESCE(SYS_CONTEXT('USERENV',
                               'CLIENT_IDENTIFIER'), USER),
        tgt.fe_usua_modi = SYSTIMESTAMP,
        tgt.de_term_modi = SYS_CONTEXT('USERENV', 'IP_ADDRESS')
WHEN NOT MATCHED THEN
    INSERT (
        n_dni,      c_nombre,
        id_usua_crea, fe_usua_crea, de_term_crea,
        id_usua_modi, fe_usua_modi, de_term_modi
    )
    VALUES (
        src.n_dni,  src.c_nombre,
        COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER),
        SYSTIMESTAMP,
        SYS_CONTEXT('USERENV', 'IP_ADDRESS'),
        NULL, NULL, NULL
    );
```

**Restricciones:**

- `MERGE` no emite `COMMIT` implícito. El control transaccional sigue las mismas reglas que `INSERT` y `UPDATE` (ver ítem 4 de 7.5).
- En tablas particionadas donde la clave de partición aparece en la condición `ON`, validar el plan de ejecución para confirmar que Oracle aplica *Partition Pruning*.
- Incluir evidencia del plan de ejecución en la documentación del pase cuando el `MERGE` afecte tablas con más de 100.000 filas (misma exigencia que 9.3).

---

## 8 Scripts de despliegue y control de cambios

### 8.1 Nomenclatura de scripts

Todo script de base de datos que forma parte de un pase formal debe seguir el siguiente patrón:

```
PP_<ORIGEN>_<NUMERO>_<ESQUEMA>_<TIPO>_<NN>.SQL
```

| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| `PP` | Prefijo fijo de pase a producción | `PP` |
| `<ORIGEN>` | Sistema o proyecto de origen | `REQ`, `INC`, `PRJ` |
| `<NUMERO>` | Número de requerimiento o ticket | `1078` |
| `<ESQUEMA>` | Esquema Oracle afectado | `APORTACIONES` |
| `<TIPO>` | Tipo de script | `TB` (tabla), `IDX` (índice), `SP` (procedure), `PKG` (package), `TRG` (trigger), `DML` (datos), `DDL` (estructura), `FN` (función) |
| `<NN>` | Número de orden dentro del mismo pase | `01`, `02` |

**Ejemplos:**
```
PP_REQ_1078_APORTACIONES_TB_01.SQL    -- Creación de tabla
PP_REQ_1078_APORTACIONES_DDL_02.SQL   -- DDL complementario
PP_REQ_1078_APORTACIONES_DML_03.SQL   -- Carga de datos
PP_REQ_1078_APORTACIONES_SP_04.SQL    -- Stored procedure
```

### 8.2 Estructura obligatoria de un script

Todo script de pase debe seguir esta estructura:

```sql
-- Encabezado del script (ver plantilla en LIN-BD-ORA-001-REF Anexo B)
PROMPT 'Ingrese ruta y nombre del archivo log: ';
ACCEPT sRutaLog;
SPOOL &sRutaLog;

-- Identificación del script
SELECT 'INICIO <NOMBRE_SCRIPT>' FROM dual;
SELECT USER || '@' || d.name || ' at ' ||
       TO_CHAR(SYSDATE, 'DD-MON-YYYY HH24:MI:SS')
FROM sys.v_$database d;

-- Script de comprobación INICIAL
-- (estado del objeto/datos ANTES del cambio)
...

-- Cambio (DDL o DML)
...

-- Script de comprobación FINAL
-- (confirma que el cambio se aplicó correctamente)
...

-- Cierre
SELECT 'FIN <NOMBRE_SCRIPT>' FROM dual;
PROMPT 'Si los cambios son correctos, ejecutar COMMIT.';
PROMPT 'De lo contrario, ejecutar ROLLBACK.';
PROMPT 'Ejecutar SPOOL OFF;';
```

Reglas adicionales:
- Los scripts DML **no incluyen COMMIT automático**. El COMMIT es responsabilidad del operador que ejecuta el script, luego de validar las comprobaciones finales.
- Los scripts DDL en Oracle generan un COMMIT implícito; esto debe tenerse en cuenta en la planificación del pase.
- Cada script debe poder ejecutarse de forma idempotente cuando sea posible (uso de `CREATE OR REPLACE`, validación previa de existencia con vistas de diccionario aprobadas).
- El script no debe depender de privilegios DBA si va a ser ejecutado por un usuario aplicativo, operador de despliegue o esquema sin acceso a `DBA_%`. En esos casos, usar `ALL_%` o `USER_%` según corresponda.

### 8.2.1 Estrategia de reversa obligatoria

Toda migración de base de datos debe definir una **estrategia de reversa obligatoria**, adecuada al tipo de cambio:

| Tipo de cambio | Reversa exigida |
|---|---|
| DML transaccional | `ROLLBACK` controlado o script compensatorio equivalente |
| DDL reversible | Script DDL compensatorio documentado (`DROP`, `ALTER` inverso, recreación controlada) |
| Cambio no reversible o con pérdida potencial de datos | Procedimiento de restauración física/lógica aprobado con DBA |

> **Regla:** no usar la palabra “rollback” como sinónimo universal de reversa en Oracle cuando el cambio incluye DDL con `COMMIT` implícito.

### 8.2.2 Modelo operativo según tipo de sistema

La ONP admite tres modelos operativos de cambios de base de datos. La elección no es discrecional: depende del tipo de sistema, del peso de PL/SQL legacy y del mecanismo real de pase.

| Tipo de sistema | Modelo operativo | Repositorio esperado | Tipo de cambios esperados | Evidencia mínima adicional |
|---|---|---|---|---|
| Sistema nuevo | Modelo versionado | `db/migration/` y `db/reverse/` en el repositorio del componente | DDL/DML versionado, cambios simples y trazables | Script versionado, reversa o compensación, prueba en ambiente controlado |
| Sistema mixto | Modelo híbrido controlado | `db/migration/`, `db/reverse/` y paquete de pase manual para componentes legacy | DDL/DML versionado para objetos nuevos; scripts manuales o controlados para PL/SQL legacy | Separación explícita entre cambio versionado y cambio manual, evidencia cruzada, decisión documentada |
| Sistema legacy con PL/SQL crítico | Modelo manual gobernado | Paquete formal de pase, scripts de comprobación, catálogo PL/SQL y evidencia DBA | Packages, procedures, funciones, vistas complejas, cargas o ajustes no automatizables | Pruebas de caracterización, validación DBA y responsable funcional/técnico |

#### a) Sistema nuevo

Aplica a componentes nuevos o módulos nuevos sin dependencia crítica de PL/SQL legacy. En este caso:

- los scripts DDL/DML deben versionarse en `db/migration/`;
- toda reversa o compensación aplicable debe ubicarse en `db/reverse/`;
- el modelo de repositorio y nomenclatura se alinea con `LIN-VER-001`;
- el template institucional backend Java es la referencia base para la estructura del proyecto.

#### b) Sistema mixto

Aplica cuando el sistema combina desarrollo nuevo con integración o convivencia parcial con PL/SQL existente. En este caso:

- los objetos nuevos de tablas, índices, vistas simples o datos de configuración deben seguir el modelo versionado;
- los cambios sobre packages, procedures o lógica legacy crítica siguen el modelo manual gobernado;
- el Merge Request y el documento de pase deben distinguir claramente qué parte del cambio es versionada y cuál requiere ejecución manual;
- si existe duda sobre la frontera entre ambos modelos, la decisión debe quedar documentada en ADR o en la excepción aprobada del proyecto.

#### c) Sistema legacy con PL/SQL crítico

Aplica a sistemas cuya lógica de negocio reside mayoritariamente en procedures, packages o funciones con comportamiento sensible o no completamente especificado. En este caso:

- no se debe forzar el modelo versionado como sustituto artificial del pase manual;
- el catálogo PL/SQL institucional es obligatorio para los objetos impactados;
- todo cambio debe incluir scripts de comprobación inicial/final, evidencia de caracterización y estrategia de reversa aprobada con DBA;
- cuando exista modernización parcial, cada nuevo componente debe evaluar si puede adoptar el modelo versionado sin comprometer la operación legacy.

#### d) Reglas de decisión obligatorias

- Si el cambio afecta solo DDL/DML nuevo y trazable, usar modelo versionado.
- Si el cambio toca PL/SQL legacy crítico, usar modelo manual gobernado.
- Si el cambio mezcla ambos, usar modelo híbrido y documentar la separación operativa.
- Ningún proyecto puede mezclar modelos sin dejar explícito en el pase qué ejecuta el pipeline, qué ejecuta el operador y qué valida el DBA.

#### e) Relación con `LIN-VER-001` y la plantilla backend

- `LIN-VER-001` define la estructura institucional del repositorio y la gobernanza del cambio.
- Este lineamiento define qué tipo de cambio BD corresponde a cada contexto técnico.
- La plantilla `template-backend-java` usa `db/migration/` y `db/reverse/` como baseline para sistemas nuevos; no reemplaza el modelo manual exigido para PL/SQL legacy.

### 8.3 Entornos de despliegue

Los scripts siguen la cadena:

```
Desarrollo (ONP_DESA) → Precalidad (ONP_PQA) → QA (ONP_QA) → Producción (master)
```

- Los permisos de UPDATE a nivel de columna se especifican explícitamente en el documento de pase a QA/Producción.
- No se ejecutan scripts de carga masiva en servidores de producción en horario de mayor demanda.
- Los paquetes de pase se aíslan por ambiente para facilitar el traslado y la trazabilidad.

---

## 9 Optimización y rendimiento

### 9.1 Diseño de índices

- Crear índices sobre columnas que aparecen frecuentemente en cláusulas `WHERE`, `JOIN` y `ORDER BY` con **alta selectividad** (cardinalidad alta respecto al total de filas).
- **Indexación obligatoria de llaves foráneas (FK):** Todas las columnas que actúan como llaves foráneas deben contar obligatoriamente con un índice. Esto evita que operaciones `UPDATE` o `DELETE` sobre la tabla maestra realicen un bloqueo exclusivo de la tabla detalle completa (*Table Lock*), previniendo contenciones graves y *deadlocks*.
- **Evitar índices redundantes:** No crear índices individuales sobre columnas que ya forman el prefijo de un índice compuesto existente. Por ejemplo, si ya existe un índice sobre `(C_ID_EMPRESA, FE_PROCESO)`, es redundante crear un índice independiente sobre `C_ID_EMPRESA` ya que el compuesto cubre dicho filtro.
- Evaluar el plan de ejecución de las consultas críticas antes de decidir qué índices crear, idealmente con estadísticas vigentes y evidencia de ejecución real cuando exista.
- Monitorear en producción el uso real de los índices. Un índice que no se usa de forma sostenida debe reevaluarse y eliminarse si no existe justificación operativa, contractual o estacional.
- Los índices compuestos deben definirse según los predicados reales de búsqueda, joins y ordenamiento. La selectividad es un criterio principal, pero no el único.
- Las columnas de tipo `IN_` (flags) no son buenas candidatas a índices simples por su baja selectividad; considerar índices de función o índices de bitmap solo en esquemas de solo lectura (OLAP, no OLTP).

### 9.2 Particionamiento

El particionamiento aplica cuando se cumple **al menos uno** de los siguientes criterios. El tamaño en disco no es el criterio principal: una tabla de 10 GB consultada por rango de fecha puede beneficiarse del particionamiento antes que una de 500 GB con acceso aleatorio.

| Criterio | Umbral orientativo |
|---|---|
| Volumen de filas | Tablas que superen o proyecten superar **50 millones de filas** |
| Patrón de acceso por rango | Tablas consultadas frecuentemente por rango de fecha o valor discreto (`FE_PROCESO`, región, estado) |
| Ciclo de vida de datos | Tablas candidatas a purga o archivado parcial por período (históricas, movimientos) |
| Mantenimiento operativo | Tablas donde `TRUNCATE PARTITION` o eliminación por período mejora significativamente el mantenimiento |

Las estrategias admitidas son:

| Estrategia | Cuándo usarla |
|------------|---------------|
| **Range** | Datos temporales con consultas por rango de fecha (tablas históricas por año/mes). La más común en sistemas previsionales. |
| **List** | Datos clasificados por un valor discreto con distribución equilibrada (región, tipo, estado) |
| **Hash** | Distribución uniforme sin criterio natural de rango o lista. Útil en tablas de alta concurrencia de inserción. |
| **Composite** | Combinación de Range + Hash o Range + List cuando el particionamiento simple es insuficiente |
| **Interval** | Extensión automática de particiones Range por período fijo (mes, año). **Recomendado en Oracle 19c** para tablas que crecen continuamente. |

**Estrategia INTERVAL — recomendada en Oracle 19c:**

```sql
-- Particionamiento mensual automático sobre tabla de movimientos
CREATE TABLE APORTACIONES.MOV_APORTE (
    N_ID        NUMBER    NOT NULL,
    FE_PROCESO  DATE      NOT NULL
    -- resto de columnas ...
)
PARTITION BY RANGE (FE_PROCESO)
INTERVAL (NUMTOYMINTERVAL(1, 'MONTH'))
(
    PARTITION P_HISTORICO VALUES LESS THAN (DATE '2020-01-01')
)
TABLESPACE TBS_DAT_APORTACIONES_01;
```

`INTERVAL` elimina la necesidad de crear particiones futuras manualmente. Oracle las genera automáticamente al insertar datos en un nuevo período.

**Consideraciones para sistemas legacy (Oracle 11g):**

- Oracle 11g admite `RANGE`, `LIST`, `HASH` y `COMPOSITE`. La opción `INTERVAL` existe desde 11gR1 pero con menor madurez operativa.
- Para sistemas 11g: evaluar `RANGE` manual con script de mantenimiento mensual que crea la partición del siguiente período via `DBMS_SCHEDULER`.
- La decisión de particionar una tabla existente en un sistema legacy requiere ADR aprobado y coordinación con el DBA para el proceso de reorganización de datos (potencialmente con `DBMS_REDEFINITION` para evitar downtime).

> El particionamiento requiere Oracle Partitioning Option (disponible en Enterprise Edition). Verificar la licencia con la OTI antes de diseñar tablas particionadas.

### 9.3 Planes de ejecución

Antes de todo pase a QA, el desarrollador debe incluir en la documentación del pase la evidencia del plan de ejecución de cada SQL nuevo o modificado con impacto en rendimiento. La evidencia preferida es:

- `DBMS_XPLAN.DISPLAY_CURSOR` cuando el SQL ya fue ejecutado en un ambiente representativo;
- `DBMS_XPLAN.DISPLAY` sobre `EXPLAIN PLAN` cuando aún no existe ejecución real;
- AWR/ASH o trazas equivalentes cuando el cambio es correctivo sobre un problema observado.

Se prohíbe el pase a producción de consultas con:

- Full Table Scan sobre tablas con más de 100.000 filas sin justificación.
- Nested Loops, Hash Join o Merge Join claramente incompatibles con la cardinalidad observada o esperada, sin justificación técnica documentada.
- Operaciones de sort innecesarias por ausencia de índice adecuado.

### 9.3.1 Evidencia mínima para pase técnico de BD

Todo cambio de BD con impacto funcional o de rendimiento debe adjuntar, como mínimo:

- SQL o PL/SQL impactado;
- objetivo del cambio;
- volumen estimado de datos afectados;
- `EXPLAIN PLAN` o evidencia equivalente cuando aplique;
- índices involucrados o justificación de no uso;
- resultado de prueba en QA o ambiente controlado;
- validación o visto bueno del responsable DBA.

### 9.4 Prohibiciones de rendimiento

| Prohibición | Alternativa |
|-------------|-------------|
| `SELECT COUNT(*) INTO v` para verificar existencia | Usar `EXISTS`, `FETCH FIRST 1 ROW ONLY`, cursor o acceso directo con manejo de ausencia |
| `IN ('valor_unico')` en WHERE | Usar `= 'valor_unico'` |
| `SELECT *` en producción | Listar columnas explícitas |
| Alias de columna en `GROUP BY` | Repetir la expresión o usar posición numérica |
| Alias de columna en `ORDER BY` de vistas | Usar posición o expresión completa |
| Conversión implícita de tipos en WHERE | Asegurar que el tipo del filtro coincide con el de la columna |
| Cálculos sobre columnas en WHERE | Calcular sobre la constante, no sobre la columna indexada |

### 9.5 Estadísticas del optimizador (DBMS_STATS)

El optimizador de Oracle basa sus decisiones de plan de ejecución en las estadísticas de objetos. Estadísticas obsoletas o inexistentes son la causa más frecuente de planes subóptimos que pasan desapercibidos en desarrollo pero degradan en producción bajo volumen real.

**Recolección automática**

Oracle 19c recolecta estadísticas automáticamente mediante el job `GATHER_STATS_JOB` durante la ventana de mantenimiento. Verificar que esté activo en producción:

```sql
SELECT job_name, state, enabled
FROM   dba_scheduler_jobs
WHERE  job_name = 'GATHER_STATS_JOB';
```

**Recolección manual obligatoria**

Se debe ejecutar recolección manual en los siguientes escenarios:

| Escenario | Acción |
|---|---|
| Tabla nueva con datos de carga inicial | `GATHER_TABLE_STATS` inmediatamente después de la carga |
| Proceso batch que modifica > 10% de las filas de una tabla | Recolección al finalizar el proceso |
| Índice nuevo sobre tabla con datos existentes | Oracle recoge estadísticas del índice al crearlo; verificar que las de la tabla estén vigentes |
| Partición nueva con datos en tabla particionada | `GATHER_TABLE_STATS` con `granularity => 'PARTITION'` |
| Degradación de rendimiento inexplicable en SQL conocido | Verificar `DBA_TAB_STATISTICS.LAST_ANALYZED` antes de cualquier otro diagnóstico |

```sql
-- Recolección de estadísticas con histogramas automáticos
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS(
        ownname          => 'APORTACIONES',
        tabname          => 'MAE_APORTANTE',
        estimate_percent => DBMS_STATS.AUTO_SAMPLE_SIZE,
        method_opt       => 'FOR ALL COLUMNS SIZE AUTO',
        degree           => 4,
        cascade          => TRUE   -- incluye índices de la tabla
    );
END;
/

-- Verificar vigencia de estadísticas
SELECT table_name,
       last_analyzed,
       num_rows,
       blocks
FROM   dba_tab_statistics
WHERE  owner = 'APORTACIONES'
ORDER  BY last_analyzed;
```

**Restricciones:**

- No usar `estimate_percent => 100` en tablas con más de 10 millones de filas durante horario productivo. Usar `AUTO_SAMPLE_SIZE`.
- No ejecutar `DELETE_TABLE_STATS` ni `LOCK_TABLE_STATS` sin coordinación con el DBA y justificación documentada.
- Los scripts de pase que incluyen carga masiva deben incluir la recolección de estadísticas como paso explícito antes de las comprobaciones finales.

---

## 10 Seguridad

### 10.1 Control de acceso basado en roles (RBAC)

El acceso a objetos de base de datos se gestiona a través de **roles**, nunca mediante permisos directos a usuarios de aplicación:

1. Definir roles con el patrón `ROL_<SISTEMA>_<NOMBRE>` (ej: `ROL_APORTACIONES_CONSULTA`, `ROL_APORTACIONES_MODIFICACION`).
2. Asignar los privilegios necesarios al rol.
3. Asignar el rol al usuario de aplicación o al usuario de acceso.

Niveles de rol recomendados por sistema:

| Rol | Privilegios |
|-----|-------------|
| `ROL_<SIS>_CONSULTA` | SELECT sobre tablas y vistas del esquema |
| `ROL_<SIS>_OPERACION` | SELECT, INSERT, UPDATE (columnas definidas) |
| `ROL_<SIS>_ADMINISTRACION` | SELECT, INSERT, UPDATE, DELETE sobre objetos del esquema |
| `ROL_<SIS>_EJECUCION` | EXECUTE sobre packages y procedures del esquema |

### 10.2 Restricciones de privilegios

- Los privilegios **no deben otorgarse con la opción `WITH GRANT OPTION`**.
- Los roles **no deben otorgarse con la opción `WITH ADMIN OPTION`**.
- Los permisos de `UPDATE` se especifican **a nivel de columna** para minimizar el acceso a datos sensibles. Se documenta en el pase a QA/Producción qué columnas quedan expuestas.
- Las cuentas de aplicación y de servicio no deben tener el rol `DBA` ni roles de sistema (`CONNECT`, `RESOURCE`) más allá de lo estrictamente necesario.
- Ninguna cuenta de aplicación debe conectarse directamente con el usuario dueño del esquema (`SCHEMA OWNER`). Se usa un usuario con los privilegios mínimos necesarios.
- Todo acceso desde software debe usar cuentas diferenciadas por aplicación, integración o proceso batch cuando el riesgo operativo o de auditoría lo requiera. No compartir una misma cuenta genérica entre sistemas distintos salvo excepción documentada.

### 10.3 Cifrado y protección de datos sensibles

De acuerdo con la Ley N° 29733 (Ley de Protección de Datos Personales) y su reglamento.

**Niveles de cifrado en reposo con Oracle TDE:**

| Nivel | Mecanismo | Cuándo aplicar |
|---|---|---|
| **Tablespace** | TDE tablespace cifrado completo | Tablespaces que contienen exclusivamente datos de carácter sensible o personal |
| **Columna** | TDE cifrado a nivel de columna (`ENCRYPT USING AES256`) | Columnas con dato sensible específico en tablespaces de uso mixto |

Criterio de decisión:
- Si el tablespace contiene **solo** datos sensibles: cifrado de tablespace (más simple, mejor rendimiento, protege también índices y undo).
- Si el tablespace es de **uso mixto**: cifrado a nivel de columna para los campos identificados.
- Ambos niveles pueden coexistir: tablespace cifrado + `SALT` adicional en columnas de criticidad máxima.

**Restricciones técnicas del cifrado TDE a nivel de columna (Advertencias Críticas):**
- **Restricciones de indexación:** Las columnas cifradas individualmente no admiten búsquedas por rango (`LIKE`, `>`, `<`) en operaciones indexadas. Solo se permiten búsquedas por igualdad exacta (`=`).
- **Uso de SALT:** Si se define la opción `SALT` (opción de seguridad habilitada por defecto en Oracle), queda prohibido crear cualquier índice sobre dicha columna. Tampoco puede utilizarse como clave primaria o foránea.
- **Rendimiento e impacto en CPU:** El cifrado por columna incrementa significativamente el consumo de CPU por cada fila procesada y degrada el rendimiento de los `JOIN`. En entornos transaccionales OLTP de alta carga, se recomienda priorizar el cifrado completo a nivel de tablespace en lugar del cifrado individual de columnas.

**Columnas de cifrado obligatorio:**

| Dato | Tipo Oracle | Mecanismo |
|---|---|---|
| DNI / documento de identidad | `VARCHAR2` | TDE columna `AES256` |
| Datos de cuenta bancaria (CCI, cuenta) | `VARCHAR2` | TDE columna `AES256` |
| Datos de salud o condición médica | `VARCHAR2` / `CLOB` | TDE columna `AES256` |
| Contraseña de usuario (si se almacena en BD) | `VARCHAR2` | Hash irreversible con sal — **no TDE**, sino `DBMS_CRYPTO` con SHA-256 mínimo |

**Cifrado en tránsito:**

- La comunicación entre la aplicación y la base de datos debe usar **Oracle Native Network Encryption (NNE)** o **TLS** configurado en el listener.
- El tipo y nivel de cifrado de red debe estar documentado en el catálogo de la base de datos.

**Entornos no productivos:**

- TDE no es obligatorio en `ONP_DESA` ni `ONP_PQA`.
- En `ONP_QA`: si se usan datos enmascarados o anonimizados, TDE es opcional.
- **Los datos personales reales no deben copiarse a entornos no productivos** sin proceso de enmascaramiento (Data Masking) aprobado por OTI.
- Si `ONP_QA` usa datos reales de producción (situación a evitar), TDE aplica con las mismas reglas que producción.

**Enmascaramiento y minimización de datos:**

- El uso de datos reales en ambientes no productivos requiere aprobación formal y un procedimiento previo de enmascaramiento o anonimización.
- El enmascaramiento debe preservar únicamente las relaciones mínimas necesarias para pruebas funcionales, evitando exponer atributos personales completos cuando no son imprescindibles.
- Los extractos para pruebas deben ser acotados por propósito, vigencia y responsable.

**Registro y gestión:**

- Las columnas cifradas y los tablespaces cifrados deben documentarse en el catálogo centralizado con su clasificación de sensibilidad.
- La gestión de Oracle Wallet (claves TDE) es responsabilidad exclusiva del DBA. Los desarrolladores no tienen acceso a las claves de cifrado.

### 10.4 Tablespace SYSTEM

Los objetos de aplicación no deben residir en el tablespace SYSTEM. Solo los objetos de las cuentas SYS y SYSTEM pertenecen a ese tablespace. Crear objetos en SYSTEM provoca crecimiento no controlado del tablespace de sistema.

---

## 11 Administración y operación

### 11.1 Tablespaces

- Cada esquema de aplicación debe tener su propio tablespace de datos (`TBS_DAT_`) y su tablespace de índices (`TBS_IDX_`). Si el esquema contiene columnas LOB, debe tener además un tablespace dedicado (`TBS_LOB_`) separado de los anteriores (ver 3.8 y 4.1).
- El tamaño inicial y el autoextend de los tablespaces se define según la proyección de crecimiento anual del sistema, coordinado con el DBA responsable.
- El monitoreo del uso de tablespaces es responsabilidad del DBA y debe estar incluido en las alertas de operación.

### 11.2 Backup y recuperación

La estrategia de backup de cada base de datos debe estar definida y documentada en el catálogo centralizado. Como mínimo:

| Tipo | Frecuencia mínima | Retención mínima |
|------|--------------------|------------------|
| Backup completo | Semanal | 4 semanas |
| Backup incremental | Diario | 7 días |
| Archive logs | Continuo | 3 días |

- Los backups se realizan con Oracle RMAN.
- Se debe probar la recuperación (restore + recovery) al menos una vez por semestre por cada base de datos productiva.
- El RTO (Recovery Time Objective) y RPO (Recovery Point Objective) de cada base de datos deben estar acordados con el área de negocio y documentados.

### 11.3 Monitoreo y diagnóstico

Todas las bases de datos productivas deben estar registradas en la herramienta de monitoreo de la OTI. El monitoreo no es opcional: una base de datos productiva sin monitoreo activo no cumple con los estándares de operación de la OTI.

#### 11.3.1 Métricas operativas mínimas con umbrales

| Métrica | Alerta | Crítico | Fuente |
|---|---|---|---|
| Uso de tablespace | > 85% | > 95% | `DBA_TABLESPACE_USAGE_METRICS` |
| Sesiones activas | > 80% del límite configurado | > 95% | `V$SESSION` |
| Locks sin liberar | > 5 minutos | > 15 minutos | `V$LOCK`, `DBA_BLOCKERS` |
| Tiempo de respuesta de queries críticas | > 2× baseline histórico | > 5× baseline | AWR / ASH |
| Jobs fallidos | Cualquier fallo | — | `DBA_SCHEDULER_JOB_LOG` |
| Archive log space | > 70% del área de recovery | > 85% | `V$RECOVERY_FILE_DEST` |
| Errores críticos en alert log | `ORA-00600`, `ORA-07445`, `ORA-04031` | Inmediato | Alert log de Oracle |

#### 11.3.2 AWR — Automatic Workload Repository

El AWR es la fuente primaria de análisis de rendimiento histórico en Oracle 19c.

- Retención mínima: **30 días** en entornos productivos.
- Intervalo de snapshot: **60 minutos** por defecto; reducir a **30 minutos** en períodos de carga crítica (cierre de mes, procesamiento masivo).
- El DBA debe generar y revisar reportes AWR ante: degradación de rendimiento, cambios en el top de SQLs más pesados y picos de consumo de CPU o I/O.
- Los reportes AWR se adjuntan a los incidentes de rendimiento registrados en el sistema de gestión de la OTI.

> Los queries de diagnóstico AWR y ASH listos para ejecutar se encuentran en **LIN-BD-ORA-001-GIA 6**.

#### 11.3.3 ASH — Active Session History

El ASH permite diagnosticar problemas de rendimiento en tiempo real o reciente.

- `V$ACTIVE_SESSION_HISTORY`: diagnóstico en tiempo real (últimos 30-60 minutos en memoria).
- `DBA_HIST_ACTIVE_SESS_HISTORY`: análisis histórico persistido en AWR.
- El ASH es la primera herramienta a revisar ante: picos de CPU, esperas de I/O, bloqueos y contención de latch.



#### 11.3.4 Alert Log

El alert log de Oracle es el registro primario de eventos de instancia: errores críticos, reinicios, cambios de configuración y mensajes internos del motor.

- El DBA debe revisar el alert log **diariamente** en entornos productivos.
- Errores de atención inmediata:

| ORA | Descripción |
|---|---|
| `ORA-00600` | Error interno del kernel Oracle — reportar a soporte |
| `ORA-07445` | Excepción de proceso (core dump interno) |
| `ORA-04031` | Memoria insuficiente en shared pool o large pool |
| `ORA-01555` | Snapshot too old — problema de undo retention |
| `ORA-00257` | Archiver error — archive log space agotado |

- Se debe configurar una alerta automática que notifique al DBA ante la aparición de estos ORA en el alert log.

#### 11.3.5 Monitoreo de jobs DBMS_SCHEDULER

- El DBA debe revisar **semanalmente** `DBA_SCHEDULER_JOB_LOG` para detectar jobs con ejecuciones anómalas: estado `FAILED`, duración fuera del rango esperado, o estado `STOPPED` sin justificación.
- Los jobs con ejecución fallida generan incidente en el sistema de gestión de la OTI.
- Todo job productivo debe tener notificación de fallo configurada mediante `DBMS_SCHEDULER.SET_ATTRIBUTE` (`on_failure_action`).

---

## Documentos complementarios

Los artefactos de referencia y los materiales de implementación se encuentran en los documentos complementarios de este lineamiento. Estos se actualizan con mayor frecuencia que el cuerpo normativo sin requerir cambios de versión en este documento.

| Documento | Qué contiene | Para quién |
|-----------|--------------|------------|
| **LIN-BD-ORA-001-REF** | Resumen de 20 reglas críticas, tabla de convenciones de nomenclatura, plantillas de scripts (SP, FN, PKG, TRG, tabla, DML), definición de campos de auditoría y checklists de revisión de pase | Desarrollador que revisa o audita |
| **LIN-BD-ORA-001-GIA** | Script de instalación del esquema `OTI_ADMIN`, patrón `CLIENT_IDENTIFIER` en Spring Boot e índice detallado de todas las secciones de este lineamiento | Desarrollador que implementa |

---

## Proceso de excepción a este estándar

Toda desviación de las reglas establecidas en este documento requiere un ADR (Architecture Decision Record) aprobado formalmente por el equipo de Arquitectura de la OTI antes de implementarse.

El ADR debe incluir: contexto, decisión, alternativas evaluadas, consecuencias, vigencia de la excepción, responsable y fecha de revisión.

Casos que siempre requieren ADR en este estándar:

- Nueva lógica de negocio core implementada en PL/SQL
- Uso de Oracle 11g en sistemas nuevos o módulos modernizados
- Acceso directo entre esquemas de distintos dominios funcionales
- Omisión de campos de auditoría en tablas de negocio
- Uso de triggers con lógica de negocio compleja en nuevos desarrollos
- Uso de DBLink cuando no se cumplen alguna de las condiciones establecidas en 4.9.1
- Creación de sinónimos públicos sin aprobación del DBA
- Particionamiento de una tabla existente en un sistema legacy (requiere coordinación con DBA y puede implicar uso de `DBMS_REDEFINITION`)

**No se acepta la urgencia como justificación para omitir este proceso.**

---

*LIN-BD-ORA-001 — Estándar de Base de Datos Oracle ONP v0.1.0*  
*OTI — Oficina de Tecnologías de la Información*
