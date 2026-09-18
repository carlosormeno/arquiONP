-- EJEMPLO DE REFERENCIA — LIN-BD-ORA-001 §8.4 (nomenclatura canónica de Flyway)
-- Esquema propio de este microservicio (soberanía de datos, ARQ-R-001 criterio #2,
-- LIN-ARQ-001 §2.1) — ninguna otra aplicación ONP debe leer o escribir esta tabla directamente.
-- Mapeada por pe.gob.onp.template.defuncion.infrastructure.persistence.ConsultaDefuncionEntity.

CREATE TABLE tb_consulta_defuncion (
    cdf_id                RAW(16)        NOT NULL,
    cdf_dni                VARCHAR2(8)    NOT NULL,
    cdf_estado             VARCHAR2(20)   NOT NULL,
    cdf_fecha_consulta     TIMESTAMP      NOT NULL,
    CONSTRAINT pk_consulta_defuncion PRIMARY KEY (cdf_id)
);

CREATE INDEX ix_consulta_defuncion_dni ON tb_consulta_defuncion (cdf_dni, cdf_fecha_consulta);

COMMENT ON TABLE tb_consulta_defuncion IS
    'Historial de verificaciones de estado de defunción consultadas contra el Registro Civil (RENIEC).';
COMMENT ON COLUMN tb_consulta_defuncion.cdf_id IS 'Identificador de la consulta (UUID).';
COMMENT ON COLUMN tb_consulta_defuncion.cdf_dni IS 'DNI de la persona verificada.';
COMMENT ON COLUMN tb_consulta_defuncion.cdf_estado IS 'Resultado: VIVO, FALLECIDO o NO_DETERMINADO.';
COMMENT ON COLUMN tb_consulta_defuncion.cdf_fecha_consulta IS
    'Momento en que se obtuvo el resultado (propio o del Registro Civil).';
