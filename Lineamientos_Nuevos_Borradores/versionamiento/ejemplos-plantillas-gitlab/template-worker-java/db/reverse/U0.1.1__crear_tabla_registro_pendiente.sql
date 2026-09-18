-- EJEMPLO DE REFERENCIA — LIN-BD-ORA-001 §8.4 (reversa de V0.1.1__crear_tabla_registro_pendiente.sql)
-- No se aplica automaticamente por Flyway (Flyway Community no ejecuta undo): queda documentada
-- para el plan de reversa manual del pase (ver .gitlab/merge_request_templates/default.md).

DROP INDEX ix_registro_pendiente_estado;
DROP TABLE tb_registro_pendiente PURGE;
