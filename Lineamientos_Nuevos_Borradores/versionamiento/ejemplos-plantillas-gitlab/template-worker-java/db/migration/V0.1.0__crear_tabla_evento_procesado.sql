-- EJEMPLO DE REFERENCIA — LIN-BD-ORA-001 §8.4 (nomenclatura canónica de Flyway)
-- Tabla técnica de idempotencia del consumidor Kafka (LIN-BUS-001 §8.4).
-- Mapeada por pe.gob.onp.template.worker.messaging.dedup.EventoProcesadoEntity.

CREATE TABLE tb_evento_procesado (
    eve_id             VARCHAR2(36)   NOT NULL,
    eve_procesado_en   TIMESTAMP      NOT NULL,
    CONSTRAINT pk_evento_procesado PRIMARY KEY (eve_id)
);

COMMENT ON TABLE tb_evento_procesado IS 'Registro de idempotencia de eventos Kafka ya procesados por el worker.';
COMMENT ON COLUMN tb_evento_procesado.eve_id IS 'Id del evento CloudEvents (campo "id" del envelope).';
COMMENT ON COLUMN tb_evento_procesado.eve_procesado_en IS 'Momento en que el worker completo el procesamiento.';
