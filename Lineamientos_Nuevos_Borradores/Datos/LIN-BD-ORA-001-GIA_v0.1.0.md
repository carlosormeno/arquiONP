# LIN-BD-ORA-001-GIA — Guía de Implementación y Aplicación
## Oficina de Normalización Previsional — OTI
### Código: LIN-BD-ORA-001-GIA | Versión 0.1.1 | Estado: Borrador | Complementa: LIN-BD-ORA-001 v0.1.9

---

## Control de versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 0.1.0 | 2026-05-29 | OTI | Versión inicial: script de instalación OTI_ADMIN, patrón CLIENT_IDENTIFIER en Spring Boot, índice detallado de LIN-BD-ORA-001 |

---

## Tabla de contenidos

- [1 Propósito y relación con LIN-BD-ORA-001](#1-propósito-y-relación-con-lin-bd-ora-001)
- [2 Esquema OTI_ADMIN — script de instalación completo](#2-esquema-oti_admin--script-de-instalación-completo)
- [3 Patrón de CLIENT_IDENTIFIER en Spring Boot](#3-patrón-de-client_identifier-en-spring-boot)
- [4 Índice detallado de LIN-BD-ORA-001 v0.1.9](#4-índice-detallado-de-lin-bd-ora-001-v019)
- [5 Adapter PL/SQL legacy en Spring Boot](#5-adapter-plsql-legacy-en-spring-boot)
- [6 Queries de diagnóstico AWR y ASH](#6-queries-de-diagnóstico-awr-y-ash)

---

## 1 Propósito y relación con LIN-BD-ORA-001

Este documento es el complemento operativo de **LIN-BD-ORA-001 — Estándar de Base de Datos Oracle ONP**. Contiene los artefactos de implementación que apoyan la aplicación del estándar normativo sin extender su cuerpo.

| Sección | Contenido |
|---------|-----------|
| 2 | Script completo de instalación del esquema `OTI_ADMIN`: tablespaces, secuencias, tablas de catálogo con índices, comentarios y grants |
| 3 | Patrón de implementación de `CLIENT_IDENTIFIER` en Spring Boot con HikariCP |
| 4 | Índice detallado de todas las secciones y subsecciones de LIN-BD-ORA-001 |
| 5 | Adapter PL/SQL legacy en Spring Boot: diagrama de capas, código Java, reglas y pruebas de caracterización |
| 6 | Queries de diagnóstico AWR y ASH listos para ejecutar |

> Este documento puede actualizarse con mayor frecuencia que el lineamiento normativo. Los cambios en LIN-BD-ORA-001 pueden requerir actualizaciones aquí. La versión vigente de LIN-BD-ORA-001 referenciada es la indicada en el encabezado.

---

## 2 Esquema OTI_ADMIN — script de instalación completo

El esquema `OTI_ADMIN` aloja los catálogos institucionales de la OTI. Su creación, mantenimiento y acceso son responsabilidad exclusiva del equipo DBA de la OTI.

> Ejecutar este script con un usuario con privilegios de DBA. Ajustar el nombre del esquema y las rutas de datafiles según la configuración de almacenamiento del entorno.

### 2.1 Tablespaces

```sql
-- Tablespace de datos
CREATE TABLESPACE TBS_DAT_OTI_ADMIN_01
    DATAFILE '/u01/app/oracle/oradata/ONPDB/tbs_dat_oti_admin_01.dbf'
    SIZE 100M AUTOEXTEND ON NEXT 50M MAXSIZE 2G
    EXTENT MANAGEMENT LOCAL AUTOALLOCATE
    SEGMENT SPACE MANAGEMENT AUTO;

-- Tablespace de índices
CREATE TABLESPACE TBS_IDX_OTI_ADMIN_01
    DATAFILE '/u01/app/oracle/oradata/ONPDB/tbs_idx_oti_admin_01.dbf'
    SIZE 50M AUTOEXTEND ON NEXT 25M MAXSIZE 1G
    EXTENT MANAGEMENT LOCAL AUTOALLOCATE
    SEGMENT SPACE MANAGEMENT AUTO;
```

### 2.2 Secuencias

```sql
CREATE SEQUENCE OTI_ADMIN.SQ_CAT_BASE_DATOS_1
    START WITH 1 INCREMENT BY 1
    NOCYCLE CACHE 20 NOORDER NOMINVALUE NOMAXVALUE;

CREATE SEQUENCE OTI_ADMIN.SQ_CATALOGO_PLSQL_1
    START WITH 1 INCREMENT BY 1
    NOCYCLE CACHE 20 NOORDER NOMINVALUE NOMAXVALUE;

CREATE SEQUENCE OTI_ADMIN.SQ_CAT_DBLINK_1
    START WITH 1 INCREMENT BY 1
    NOCYCLE CACHE 20 NOORDER NOMINVALUE NOMAXVALUE;

CREATE SEQUENCE OTI_ADMIN.SQ_CAT_JOB_SCHEDULER_1
    START WITH 1 INCREMENT BY 1
    NOCYCLE CACHE 20 NOORDER NOMINVALUE NOMAXVALUE;
```

### 2.3 Catálogo de bases de datos

El DDL base está definido en LIN-BD-ORA-001 2.3. Este script agrega índices y comentarios:

```sql
-- Índices operativos
CREATE INDEX IDX_CAT_BDATOS_01
    ON OTI_ADMIN.CAT_BASE_DATOS (C_AMBIENTE)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

CREATE INDEX IDX_CAT_BDATOS_02
    ON OTI_ADMIN.CAT_BASE_DATOS (C_ESTADO)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

-- Comentarios
COMMENT ON TABLE  OTI_ADMIN.CAT_BASE_DATOS IS
    'Catálogo centralizado de todas las bases de datos administradas por la OTI-ONP.';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.ID_BASE_DATOS IS
    'Identificador técnico único del registro de base de datos';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_NOMBRE IS
    'Nombre identificador único de la base de datos Oracle';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_VERSION_ORACLE IS
    'Versión de Oracle: 19c o 11g';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_PROPOSITO IS
    'Sistema o sistemas a los que da soporte esta base de datos';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_ESQUEMAS_ACTIVOS IS
    'Lista de esquemas activos y sus responsables';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_AMBIENTE IS
    'Ambiente: PRODUCCION, QA, PRECALIDAD, DESARROLLO';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_RESPONSABLE_DBA IS
    'Contacto del DBA responsable';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_RESPONSABLE_DESA IS
    'Contacto del líder de desarrollo';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_ESTADO IS
    'Estado: ACTIVO, LEGADO, EN_MIGRACION';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.C_CIFRADO_RED IS
    'Tipo de cifrado de red configurado: NNE, TLS o ninguno';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.FE_REGISTRO IS
    'Fecha y hora de registro en el catálogo';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.ID_USUA_CREA IS
    'Usuario que registró la base de datos en el catálogo';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.FE_USUA_CREA IS
    'Fecha y hora de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.DE_TERM_CREA IS
    'Terminal o IP de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.ID_USUA_MODI IS
    'Usuario que realizó la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.FE_USUA_MODI IS
    'Fecha y hora de la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CAT_BASE_DATOS.DE_TERM_MODI IS
    'Terminal o IP de la última modificación';
```

### 2.4 Catálogo de objetos PL/SQL legacy

LIN-BD-ORA-001 6.0 define los campos mínimos de referencia. Esta es la implementación productiva alineada con las convenciones del estándar (prefijos 3.4, auditoría 5.1, TIMESTAMP 3.7):

```sql
CREATE TABLE OTI_ADMIN.CATALOGO_PLSQL_LEGACY (
    ID_CATALOGO           NUMBER(19)     CONSTRAINT NN_CAT_PLSQL_ID    NOT NULL,
    C_ESQUEMA             VARCHAR2(30)   CONSTRAINT NN_CAT_PLSQL_ESQ   NOT NULL,
    C_NOMBRE_OBJETO       VARCHAR2(128)  CONSTRAINT NN_CAT_PLSQL_NOM   NOT NULL,
    C_TIPO_OBJETO         VARCHAR2(20)   CONSTRAINT NN_CAT_PLSQL_TIPO  NOT NULL,
    C_SISTEMA_CONSUMIDOR  VARCHAR2(100)  CONSTRAINT NN_CAT_PLSQL_SIS   NOT NULL,
    C_RESP_TECNICO        VARCHAR2(100)  CONSTRAINT NN_CAT_PLSQL_RESP  NOT NULL,
    C_DESC_FUNCIONAL      VARCHAR2(1000) CONSTRAINT NN_CAT_PLSQL_DESC  NOT NULL,
    C_CATEGORIA_USO       VARCHAR2(30)   CONSTRAINT NN_CAT_PLSQL_CAT   NOT NULL,
    C_ESTADO_REGISTRO     VARCHAR2(20)   CONSTRAINT NN_CAT_PLSQL_EST   NOT NULL,
    FE_REGISTRO           TIMESTAMP      CONSTRAINT NN_CAT_PLSQL_FER   NOT NULL,
    C_OBSERVACIONES       VARCHAR2(1000),
    ID_USUA_CREA          VARCHAR2(30)   CONSTRAINT NN_CAT_PLSQL_UCREA NOT NULL,
    FE_USUA_CREA          TIMESTAMP      CONSTRAINT NN_CAT_PLSQL_FCREA NOT NULL,
    DE_TERM_CREA          VARCHAR2(39)   CONSTRAINT NN_CAT_PLSQL_TCREA NOT NULL,
    ID_USUA_MODI          VARCHAR2(30),
    FE_USUA_MODI          TIMESTAMP,
    DE_TERM_MODI          VARCHAR2(39),
    CONSTRAINT PK_CATALOGO_PLSQL    PRIMARY KEY (ID_CATALOGO),
    CONSTRAINT CK_CAT_PLSQL_TIPO    CHECK (C_TIPO_OBJETO IN (
        'PACKAGE','STORED_PROCEDURE','FUNCTION')),
    CONSTRAINT CK_CAT_PLSQL_CAT     CHECK (C_CATEGORIA_USO IN (
        'TECNICO_PERMITIDO','BATCH_PERMITIDO','LEGACY_CRITICO',
        'NUEVA_LOGICA','PROHIBIDO')),
    CONSTRAINT CK_CAT_PLSQL_EST     CHECK (C_ESTADO_REGISTRO IN (
        'ACTIVO','INACTIVO','EN_REVISION'))
)
TABLESPACE TBS_DAT_OTI_ADMIN_01;

-- Índices operativos
CREATE INDEX IDX_CATALOGO_PLSQL_01
    ON OTI_ADMIN.CATALOGO_PLSQL_LEGACY (C_ESQUEMA)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

CREATE INDEX IDX_CATALOGO_PLSQL_02
    ON OTI_ADMIN.CATALOGO_PLSQL_LEGACY (C_CATEGORIA_USO)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

-- Comentarios
COMMENT ON TABLE  OTI_ADMIN.CATALOGO_PLSQL_LEGACY IS
    'Catálogo de objetos PL/SQL con lógica de negocio activa en producción.';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.ID_CATALOGO IS
    'Identificador técnico único del registro';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_ESQUEMA IS
    'Esquema Oracle propietario del objeto';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_NOMBRE_OBJETO IS
    'Nombre completo: ESQUEMA.PKG_NOMBRE o ESQUEMA.PKG_NOMBRE.SP_PROC';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_TIPO_OBJETO IS
    'Tipo: PACKAGE, STORED_PROCEDURE, FUNCTION';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_SISTEMA_CONSUMIDOR IS
    'Aplicación, batch, job o integración que invoca el objeto';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_RESP_TECNICO IS
    'Equipo o persona responsable técnica del objeto';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_DESC_FUNCIONAL IS
    'Descripción de la regla o proceso de negocio que ejecuta';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_CATEGORIA_USO IS
    'Categoría de uso según LIN-BD-ORA-001 6.0';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_ESTADO_REGISTRO IS
    'Estado del registro: ACTIVO, INACTIVO, EN_REVISION';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.FE_REGISTRO IS
    'Fecha y hora de registro en el catálogo';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.C_OBSERVACIONES IS
    'Notas adicionales: estrategia futura, dependencias o restricciones';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.ID_USUA_CREA IS
    'Usuario que registró el objeto en el catálogo';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.FE_USUA_CREA IS
    'Fecha y hora de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.DE_TERM_CREA IS
    'Terminal o IP de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.ID_USUA_MODI IS
    'Usuario que realizó la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.FE_USUA_MODI IS
    'Fecha y hora de la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CATALOGO_PLSQL_LEGACY.DE_TERM_MODI IS
    'Terminal o IP de la última modificación';
```

### 2.5 Catálogo de DBLinks

El DDL base está definido en LIN-BD-ORA-001 4.9.1. Este script agrega índices y comentarios:

```sql
-- Índices operativos
CREATE INDEX IDX_CAT_DBLINK_02
    ON OTI_ADMIN.CAT_DBLINK (C_BD_DESTINO)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

CREATE INDEX IDX_CAT_DBLINK_03
    ON OTI_ADMIN.CAT_DBLINK (C_ESTADO)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

-- Comentarios
COMMENT ON TABLE  OTI_ADMIN.CAT_DBLINK IS
    'Catálogo centralizado de DBLinks activos y registrados en la OTI-ONP.';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.ID_DBLINK IS
    'Identificador técnico único del registro de DBLink';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_NOMBRE IS
    'Nombre del DBLink: DBL_<BD_DESTINO>_<ESQUEMA_DESTINO>';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_BD_DESTINO IS
    'Base de datos destino del DBLink';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_PROPOSITO IS
    'Justificación arquitectónica del uso del DBLink';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_OBJETOS_ACCEDIDOS IS
    'Tablas o vistas consultadas a través del DBLink';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_RESP_TECNICO IS
    'Responsable técnico declarado del DBLink';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.FE_CREACION IS
    'Fecha y hora de creación del DBLink en base de datos';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.FE_REVISION_VIGENTE IS
    'Fecha de la última revisión de necesidad del DBLink (revisión anual obligatoria)';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_ESTADO IS
    'Estado: ACTIVO, EN_REVISION, DESACTIVADO';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.C_OBSERVACIONES IS
    'Notas adicionales: plan de reemplazo, restricciones o acuerdos con sistemas destino';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.ID_USUA_CREA IS
    'Usuario que registró el DBLink en el catálogo';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.FE_USUA_CREA IS
    'Fecha y hora de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.DE_TERM_CREA IS
    'Terminal o IP de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.ID_USUA_MODI IS
    'Usuario que realizó la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.FE_USUA_MODI IS
    'Fecha y hora de la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CAT_DBLINK.DE_TERM_MODI IS
    'Terminal o IP de la última modificación';
```

### 2.6 Catálogo de Jobs

El DDL base está definido en LIN-BD-ORA-001 4.12. Este script agrega índices y comentarios:

```sql
-- Índices operativos
CREATE INDEX IDX_CAT_JOB_02
    ON OTI_ADMIN.CAT_JOB_SCHEDULER (C_ESQUEMA)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

CREATE INDEX IDX_CAT_JOB_03
    ON OTI_ADMIN.CAT_JOB_SCHEDULER (C_ESTADO)
    TABLESPACE TBS_IDX_OTI_ADMIN_01;

-- Comentarios
COMMENT ON TABLE  OTI_ADMIN.CAT_JOB_SCHEDULER IS
    'Catálogo de jobs productivos registrados en DBMS_SCHEDULER por la OTI-ONP.';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.ID_JOB IS
    'Identificador técnico único del registro de job';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_NOMBRE IS
    'Nombre del job: JOB_<ESQUEMA>_<NOMBRE>_<FRECUENCIA>';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_ESQUEMA IS
    'Esquema Oracle propietario del job';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_PROPOSITO IS
    'Descripción del proceso que ejecuta el job';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_FRECUENCIA IS
    'Frecuencia de ejecución: DIAR, SEMA, MENS, ANUA, HORA, DEMA';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.N_TIEMPO_ESPERADO_SEG IS
    'Tiempo máximo esperado de ejecución en segundos. Superar este umbral genera alerta.';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_RESP_TECNICO IS
    'Responsable técnico del job';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_RESP_FUNCIONAL IS
    'Responsable funcional del proceso que ejecuta el job';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_ESTADO IS
    'Estado: ACTIVO, DESACTIVADO, EN_REVISION';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.C_OBSERVACIONES IS
    'Notas adicionales: dependencias entre jobs, ventanas de ejecución o restricciones';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.ID_USUA_CREA IS
    'Usuario que registró el job en el catálogo';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.FE_USUA_CREA IS
    'Fecha y hora de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.DE_TERM_CREA IS
    'Terminal o IP de creación del registro';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.ID_USUA_MODI IS
    'Usuario que realizó la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.FE_USUA_MODI IS
    'Fecha y hora de la última modificación';
COMMENT ON COLUMN OTI_ADMIN.CAT_JOB_SCHEDULER.DE_TERM_MODI IS
    'Terminal o IP de la última modificación';
```

### 2.7 Grants mínimos sobre el esquema OTI_ADMIN

```sql
-- Rol de consulta sobre todos los catálogos OTI
CREATE ROLE ROL_OTI_ADMIN_CONSULTA;

GRANT SELECT ON OTI_ADMIN.CAT_BASE_DATOS        TO ROL_OTI_ADMIN_CONSULTA;
GRANT SELECT ON OTI_ADMIN.CATALOGO_PLSQL_LEGACY TO ROL_OTI_ADMIN_CONSULTA;
GRANT SELECT ON OTI_ADMIN.CAT_DBLINK            TO ROL_OTI_ADMIN_CONSULTA;
GRANT SELECT ON OTI_ADMIN.CAT_JOB_SCHEDULER     TO ROL_OTI_ADMIN_CONSULTA;

-- Asignar el rol según necesidad operativa
-- GRANT ROL_OTI_ADMIN_CONSULTA TO <usuario_dba_responsable>;
-- GRANT ROL_OTI_ADMIN_CONSULTA TO <usuario_lider_tecnico>;
```

---

## 3 Patrón de CLIENT_IDENTIFIER en Spring Boot

LIN-BD-ORA-001 5.3 exige que todo componente de acceso a datos establezca `CLIENT_IDENTIFIER` al inicio de cada unidad de trabajo y lo limpie al liberar la conexión. El patrón recomendado para Spring Boot con HikariCP es un aspecto AOP que intercepta métodos anotados con `@Transactional`.

### 3.1 Dependencias requeridas

```xml
<!-- Spring AOP (incluido en spring-boot-starter) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<!-- Spring Security para obtener el usuario autenticado -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 3.2 Interceptor AOP

```java
package pe.gob.onp.infrastructure.persistence.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;

@Aspect
@Component
@Order(1)
public class OracleClientIdentifierAspect {

    private static final Logger log =
        LoggerFactory.getLogger(OracleClientIdentifierAspect.class);

    private final DataSource dataSource;

    public OracleClientIdentifierAspect(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)" +
            " || @within(org.springframework.transaction.annotation.Transactional)")
    public Object setAndClearClientIdentifier(ProceedingJoinPoint pjp)
            throws Throwable {

        String username = resolveUsername();
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            setIdentifier(conn, username);
            return pjp.proceed();
        } finally {
            clearIdentifier(conn);
            // No cerrar conn aquí: Spring gestiona su ciclo de vida
        }
    }

    private String resolveUsername() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }

    private void setIdentifier(Connection conn, String username) {
        try (CallableStatement cs = conn.prepareCall(
                "BEGIN DBMS_SESSION.SET_IDENTIFIER(:1); END;")) {
            cs.setString(1, username);
            cs.execute();
        } catch (Exception e) {
            log.warn("No se pudo establecer CLIENT_IDENTIFIER para '{}'",
                username, e);
        }
    }

    private void clearIdentifier(Connection conn) {
        try (CallableStatement cs = conn.prepareCall(
                "BEGIN DBMS_SESSION.CLEAR_IDENTIFIER; END;")) {
            cs.execute();
        } catch (Exception e) {
            log.warn("No se pudo limpiar CLIENT_IDENTIFIER", e);
        }
    }
}
```

**Notas de implementación:**

- `DataSourceUtils.getConnection(dataSource)` obtiene la conexión vinculada a la transacción activa — es la misma que usan los repositorios JPA/JDBC en ese hilo.
- El aspecto no cierra la conexión: Spring gestiona su ciclo de vida a través del pool.
- `@Order(1)` asegura que el aspecto se ejecuta antes que otros aspectos transaccionales para que el `CLIENT_IDENTIFIER` esté disponible desde el primer DML.
- Para procesos batch o jobs sin contexto de seguridad, `resolveUsername()` retorna `"SISTEMA"` como fallback. Ajustar al identificador funcional del proceso (nombre del job, servicio de integración, etc.).

### 3.3 Verificación en base de datos

```sql
-- Consultar sesiones activas con CLIENT_IDENTIFIER establecido
SELECT s.sid,
       s.serial#,
       s.username,
       s.client_identifier,
       s.machine,
       s.program
FROM   v$session s
WHERE  s.client_identifier IS NOT NULL
  AND  s.status = 'ACTIVE';
```

---

## 4 Índice detallado de LIN-BD-ORA-001 v0.1.7

Índice completo con todas las secciones y subsecciones del lineamiento normativo para navegación rápida.

| Sección | Título |
|---------|--------|
| 1 | Alcance y vigencia |
| 1.1 | Propósito |
| 1.2 | Ámbito de aplicación |
| 1.3 | Relación con otros documentos |
| 2 | Plataforma de base de datos |
| 2.1 | Oracle 19c — estándar vigente |
| 2.2 | Oracle 11g — legado con restricción de uso |
| 2.3 | Catálogo centralizado de bases de datos |
| 2.4 | Redefinición basada en ediciones (EBR) |
| 3 | Diseño del modelo de datos |
| 3.1 | Normalización |
| 3.2 | Esquemas por dominio funcional |
| 3.3 | Tipos de tablas — prefijos |
| 3.4 | Prefijos de columnas |
| 3.5 | Claves primarias, claves foráneas y claves de negocio |
| 3.6 | Borrado lógico y borrado físico |
| 3.7 | Temporalidad y zona horaria |
| 3.8 | Tipos de datos LOB |
| 4 | Nomenclatura de objetos |
| 4.1 | Tablespaces |
| 4.2 | Tablas |
| 4.3 | Vistas y Vistas Materializadas |
| 4.4 | Índices |
| 4.5 | Constraints |
| 4.6 | Secuencias |
| 4.7 | Packages, Stored Procedures y Functions |
| 4.8 | Triggers |
| 4.9 | DBLinks y Directorios |
| 4.9.1 | Condiciones para el uso de DBLinks |
| 4.10 | Roles y ACLs |
| 4.11 | Sinónimos |
| 4.12 | Jobs de DBMS_SCHEDULER |
| 5 | Campos de auditoría |
| 5.1 | Definición |
| 5.2 | Excepciones |
| 5.3 | Reglas de llenado |
| 6 | Objetos programables PL/SQL |
| 6.0 | Gobierno de lógica de negocio en PL/SQL |
| 6.1 | Cuándo usar cada tipo de objeto |
| 6.2 | Reglas de diseño de objetos programables |
| 7 | Estándares de codificación PL/SQL |
| 7.1 | Formato y estilo |
| 7.2 | Bind variables |
| 7.3 | Documentación de encabezado |
| 7.4 | Manejo de excepciones |
| 7.5 | Buenas prácticas de codificación |
| 7.6 | Sentencia MERGE (UPSERT) |
| 8 | Scripts de despliegue y control de cambios |
| 8.1 | Nomenclatura de scripts |
| 8.2 | Estructura obligatoria de un script |
| 8.2.1 | Estrategia de reversa obligatoria |
| 8.2.2 | Modelo operativo según tipo de sistema |
| 8.3 | Entornos de despliegue |
| 9 | Optimización y rendimiento |
| 9.1 | Diseño de índices |
| 9.2 | Particionamiento |
| 9.3 | Planes de ejecución |
| 9.3.1 | Evidencia mínima para pase técnico de BD |
| 9.4 | Prohibiciones de rendimiento |
| 9.5 | Estadísticas del optimizador (DBMS_STATS) |
| 10 | Seguridad |
| 10.1 | Control de acceso basado en roles (RBAC) |
| 10.2 | Restricciones de privilegios |
| 10.3 | Cifrado y protección de datos sensibles |
| 10.4 | Tablespace SYSTEM |
| 11 | Administración y operación |
| 11.1 | Tablespaces |
| 11.2 | Backup y recuperación |
| 11.3 | Monitoreo y diagnóstico |
| 11.3.1 | Métricas operativas mínimas con umbrales |
| 11.3.2 | AWR — Automatic Workload Repository |
| 11.3.3 | ASH — Active Session History |
| 11.3.4 | Alert Log |
| 11.3.5 | Monitoreo de jobs DBMS_SCHEDULER |
| Anexo A | Tabla de convenciones de nomenclatura |
| Anexo B | Plantillas simplificadas de scripts |
| Anexo C | Campos de auditoría — definición y reglas de llenado |
| Anexo D | Checklist de revisión de base de datos |

---

## 5 Adapter PL/SQL legacy en Spring Boot

Cuando un servicio Java necesite invocar lógica de negocio que reside en un procedure/package legacy, debe hacerlo a través de un **adapter de infraestructura** claramente identificado. La lógica legacy no debe invocarse directamente desde capas de aplicación o dominio.

### 5.1 Diagrama de capas

```
Application Service (Java)
        ↓
Port de salida (interfaz — capa domain)
        ↓
Adapter PL/SQL (capa infrastructure)
        ↓
Package / Procedure Oracle legacy
```

### 5.2 Ejemplo de implementación

```java
// En domain/port/out
public interface CalculoPensionPort {
    ResultadoCalculo calcular(String dni, String periodo);
}

// En infrastructure/persistence
@Repository
public class CalculoPensionOracleAdapter implements CalculoPensionPort {

    private final JdbcTemplate jdbc;

    @Override
    public ResultadoCalculo calcular(String dni, String periodo) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbc)
            .withSchemaName("PENSIONES")
            .withProcedureName("SP_CALCULAR_PENSION");

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("P_DNI", dni)
            .addValue("P_PERIODO", periodo);

        Map<String, Object> result = call.execute(params);
        return new ResultadoCalculo(
            (BigDecimal) result.get("P_MONTO"),
            (String) result.get("P_CODIGO_RESULTADO")
        );
    }
}
```

### 5.3 Reglas para adapters que invocan PL/SQL legacy

- El adapter es la única clase que conoce el nombre del procedure/package.
- Los errores Oracle (`ORA-XXXXX`) se traducen a excepciones de dominio antes de salir del adapter.
- La duración de la llamada se registra con log estructurado (`DEBUG` en desarrollo, `INFO` si supera umbral de latencia).
- El adapter tiene pruebas de integración con `OracleContainer` (Testcontainers) que validan el contrato de entrada/salida.

### 5.4 Pruebas de caracterización — guía de implementación

Un procedure legacy crítico no tiene especificación formal — su comportamiento real **es** la especificación. Las pruebas de caracterización documentan ese comportamiento como *ground truth* antes de tocar el código.

| Tipo de prueba | Pregunta que responde | Cuándo se escribe |
|---|---|---|
| **Caracterización** | ¿Qué hace exactamente el procedure hoy, con datos reales de ONP? | **Antes** de cualquier cambio funcional |
| **Integración** | ¿El adapter Java invoca el procedure con el contrato correcto? | Al crear o modificar el adapter |

**Mínimo exigible para un procedure en categoría "Legacy crítico":**

- Al menos 3 casos de prueba con datos representativos del dominio ONP (casos base, borde y error conocido).
- Los resultados esperados se toman de la ejecución real en producción o QA, no de supuestos.
- Deben fallar si el procedure cambia su comportamiento de salida — son la red de seguridad del refactor.

> **Por qué es crítico en ONP:** procedures como `SP_CALCULAR_PENSION` acumulan años de reglas de negocio que nadie conoce completamente. Sin pruebas de caracterización, cualquier modificación —incluso una corrección de bug— puede alterar silenciosamente cálculos de beneficios previsionales. Ver **LIN-TEST-001 13** para la implementación con Testcontainers + OracleContainer.

**Checklist mínimo para un procedure legacy crítico:**

- [ ] Registrado en el catálogo PL/SQL con todos los campos requeridos
- [ ] Dueño técnico y funcional identificados
- [ ] Su invocación desde Java está encapsulada en un adapter de infraestructura
- [ ] Los errores Oracle se traducen a excepciones de aplicación en el adapter
- [ ] **Pruebas de caracterización escritas** (mínimo 3 casos) antes de cualquier cambio funcional
- [ ] Existe al menos una prueba de integración que valida el contrato del adapter
- [ ] La llamada genera log estructurado con duración y resultado

---

## 6 Queries de diagnóstico AWR y ASH

Queries listos para ejecutar en entornos productivos Oracle 19c. Requieren acceso a vistas `DBA_` o `V$`. Ejecutar con usuario DBA o con los privilegios correspondientes.

### 6.1 Generar reporte AWR HTML

Genera un reporte AWR en HTML para un rango de snapshots. Útil para analizar degradaciones de rendimiento en un período específico.

```sql
-- Obtener los snapshot IDs disponibles para el rango deseado
SELECT snap_id,
       TO_CHAR(begin_interval_time, 'DD-MON-YYYY HH24:MI') AS inicio,
       TO_CHAR(end_interval_time,   'DD-MON-YYYY HH24:MI') AS fin
FROM   dba_hist_snapshot
ORDER  BY snap_id DESC
FETCH FIRST 20 ROWS ONLY;

-- Generar reporte AWR HTML para un período específico
SELECT output FROM TABLE(
    DBMS_WORKLOAD_REPOSITORY.AWR_REPORT_HTML(
        l_dbid     => (SELECT dbid FROM v$database),
        l_inst_num => 1,
        l_bid      => :snap_id_inicio,
        l_eid      => :snap_id_fin
    )
);
```

### 6.2 Top SQLs por tiempo de espera (ASH)

Identifica los SQL más costosos en las últimas N horas por tiempo de espera activo. Primera herramienta a revisar ante picos de CPU, I/O o bloqueos.

```sql
-- Top 10 SQLs por tiempo de espera en las últimas 2 horas
SELECT sql_id,
       COUNT(*)                        AS muestras,
       ROUND(COUNT(*) / 120 * 100, 1) AS pct_activo
FROM   v$active_session_history
WHERE  sample_time > SYSDATE - 2/24
  AND  session_state = 'WAITING'
GROUP  BY sql_id
ORDER  BY muestras DESC
FETCH  FIRST 10 ROWS ONLY;
```

### 6.3 Sesiones bloqueadas activas

Identifica bloqueos activos con su SQL origen. Revisar ante el umbral de alerta de 5 minutos definido en LIN-BD-ORA-001 11.3.1.

```sql
SELECT w.sid                              AS sid_bloqueado,
       w.serial#,
       b.sid                              AS sid_bloqueante,
       w.seconds_in_wait                  AS segundos_esperando,
       s.sql_text                         AS sql_bloqueado
FROM   v$lock      lw
JOIN   v$lock      lb  ON lb.id1 = lw.id1 AND lb.id2 = lw.id2
JOIN   v$session   w   ON w.sid  = lw.sid
JOIN   v$session   b   ON b.sid  = lb.sid
LEFT   JOIN v$sql  s   ON s.sql_id = w.sql_id
WHERE  lw.block = 0
  AND  lb.block = 1
ORDER  BY w.seconds_in_wait DESC;
```

---

*LIN-BD-ORA-001-GIA — Guía de Implementación y Aplicación v0.1.1*
*OTI — Oficina de Tecnologías de la Información*
