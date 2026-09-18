-- EJEMPLO DE REFERENCIA — LIN-BD-ORA-001 §8.4 (nomenclatura canónica de Flyway)
-- Tabla de ejemplo para el Job puntual/recurrente (LIN-DIS-001 §4.1, patron Table Module).
-- Leida y actualizada en lote por
-- pe.gob.onp.template.worker.batch.ActualizacionMasivaJobRunner.

CREATE TABLE tb_registro_pendiente (
    reg_id                  NUMBER(19)     NOT NULL,
    reg_estado              VARCHAR2(20)   NOT NULL,
    reg_fecha_creacion      TIMESTAMP      NOT NULL,
    reg_fecha_actualizacion TIMESTAMP,
    CONSTRAINT pk_registro_pendiente PRIMARY KEY (reg_id),
    CONSTRAINT ck_registro_pendiente_estado CHECK (reg_estado IN ('PENDIENTE', 'EXPIRADO'))
);

CREATE INDEX ix_registro_pendiente_estado ON tb_registro_pendiente (reg_estado);

COMMENT ON TABLE tb_registro_pendiente IS 'Ejemplo generico de tabla procesada en lote por el Job puntual/recurrente del worker.';
COMMENT ON COLUMN tb_registro_pendiente.reg_estado IS 'PENDIENTE o EXPIRADO — ver RegistroPendienteTableModule.';
