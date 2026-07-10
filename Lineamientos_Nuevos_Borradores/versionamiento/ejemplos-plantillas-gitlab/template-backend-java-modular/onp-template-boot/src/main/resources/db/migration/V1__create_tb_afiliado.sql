-- Migración versionada Flyway (LIN-BD-ORA-001). Ejecutada automáticamente al arrancar
-- porque spring.jpa.hibernate.ddl-auto=validate exige que el esquema exista de antemano.
CREATE TABLE TB_AFILIADO (
    AFI_ID               RAW(16) NOT NULL,
    AFI_DNI               VARCHAR2(8 CHAR) NOT NULL,
    AFI_NOMBRE_COMPLETO   VARCHAR2(200 CHAR) NOT NULL,
    AFI_ESTADO            VARCHAR2(20 CHAR) NOT NULL,
    AFI_FECHA_REGISTRO    TIMESTAMP(6) NOT NULL,
    CONSTRAINT PK_TB_AFILIADO PRIMARY KEY (AFI_ID),
    CONSTRAINT UQ_TB_AFILIADO_DNI UNIQUE (AFI_DNI)
);
