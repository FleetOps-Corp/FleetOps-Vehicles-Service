-- Contrato A+: seguimiento de ACK de Asignaciones y reasignación tras liberar.

ALTER TABLE sagas_vehiculo
    ADD COLUMN IF NOT EXISTS asignaciones_ack BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE sagas_vehiculo
    ADD COLUMN IF NOT EXISTS permite_reasignacion BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE sagas_vehiculo
    ADD COLUMN IF NOT EXISTS reconfirmaciones INTEGER NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_saga_sin_ack
    ON sagas_vehiculo (estado_saga, asignaciones_ack, creado_en)
    WHERE asignaciones_ack = FALSE AND estado_saga = 'COMPLETADA';
