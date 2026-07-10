-- Tabla técnica de idempotencia de consumidores Kafka (LIN-BUS-001 §8.4).
-- No pertenece al dominio de negocio de ningún componente — la usa
-- onp-afiliacion-messaging (y cualquier futuro consumidor de otros componentes).
CREATE TABLE TB_EVENTO_PROCESADO (
    EVE_ID              VARCHAR2(36 CHAR) NOT NULL,
    EVE_PROCESADO_EN    TIMESTAMP(6) NOT NULL,
    CONSTRAINT PK_TB_EVENTO_PROCESADO PRIMARY KEY (EVE_ID)
);
