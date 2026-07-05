-- Elimina el estado RESERVADO: la ocupación vive en reservas_vehiculo, no en estado_vehiculo.
UPDATE vehiculos
SET estado_vehiculo = 'DISPONIBLE'
WHERE estado_vehiculo = 'RESERVADO';

ALTER TABLE vehiculos DROP CONSTRAINT chk_estado_vehiculo;

ALTER TABLE vehiculos ADD CONSTRAINT chk_estado_vehiculo CHECK (
    estado_vehiculo IN ('DISPONIBLE', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO')
);
