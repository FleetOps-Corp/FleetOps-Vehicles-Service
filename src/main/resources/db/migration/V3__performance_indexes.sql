-- Índices para listados, scheduler y solapamiento de reservas (consultas de alta frecuencia).

CREATE INDEX IF NOT EXISTS idx_reserva_estado_creado
    ON reservas_vehiculo (estado_reserva, creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_reserva_vehiculo_fechas
    ON reservas_vehiculo (id_vehiculo, fecha_inicio, fecha_fin)
    WHERE estado_reserva IN ('PENDIENTE', 'CONFIRMADA');

CREATE INDEX IF NOT EXISTS idx_saga_estado_creado
    ON sagas_vehiculo (estado_saga, creado_en DESC);

CREATE INDEX IF NOT EXISTS idx_historial_vehiculo_fecha
    ON historial_estados_vehiculo (id_vehiculo, registrado_en DESC);

CREATE INDEX IF NOT EXISTS idx_vehiculos_activo_estado
    ON vehiculos (activo, estado_vehiculo);

-- Unicidad de correlación con Asignaciones (permite confiar en la BD en lugar de exists previos).
CREATE UNIQUE INDEX IF NOT EXISTS uq_reservas_id_asignacion_ext
    ON reservas_vehiculo (id_asignacion_ext)
    WHERE id_asignacion_ext IS NOT NULL;
