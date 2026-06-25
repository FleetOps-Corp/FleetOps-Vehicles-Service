-- =============================================================================
-- BASELINE MIGRATION: V1__create_initial_schema.sql
-- Microservicio de Vehículos (FleetOps - Core Storage Layer)
-- Engine: PostgreSQL 14+ | Strategy: Database-First Evolution Baseline
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- Habilita la extensión pgcrypto nativa para dar soporte criptográfico seguro 
-- y proveer el algoritmo gen_random_uuid() para identificadores inmutables v4.

-- =============================================================================
-- TABLA: tipos_vehiculo (Catálogo Maestro Lookup / Normalización Perimetral)
-- =============================================================================
CREATE TABLE tipos_vehiculo (
    id_tipo_vehiculo  BIGSERIAL PRIMARY KEY,
    -- BIGSERIAL: Clave primaria autoincremental de 8 bytes, óptima para indexación 
    -- y consultas rápidas JOIN en tablas de catálogos con bajo volumen de filas.

    nombre_tipo       VARCHAR(100) NOT NULL UNIQUE,
    -- UNIQUE: Restricción de unicidad estricta para mitigar duplicación ortográfica.

    descripcion       VARCHAR(255) NOT NULL,
    
    -- === CAMBIO APLICADO AQUÍ ===
    capacidad_carga   DOUBLE PRECISION NOT NULL, -- Expresado en kilogramos (Kg), soporta valores decimales (ej. 1500.5)
    
    creado_en         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en    TIMESTAMP
);

-- =============================================================================
-- TABLA: vehiculos (Agregado Raíz / Core Domain Active Assets)
-- =============================================================================
CREATE TABLE vehiculos (
    id_vehiculo        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- UUID v4: Garantiza identificadores globales descentralizados, anulando la 
    -- predictibilidad de registros y facilitando la escalabilidad transaccional.

    numero_placa       VARCHAR(20)  NOT NULL UNIQUE,
    marca              VARCHAR(100) NOT NULL,
    modelo             VARCHAR(100) NOT NULL,
    anio_fabricacion   INTEGER NOT NULL,
    color              VARCHAR(50) NOT NULL,
    
    numero_chasis      VARCHAR(100) UNIQUE NOT NULL, -- REGLA DE NEGOCIO #9
    numero_motor       VARCHAR(100) UNIQUE NOT NULL, -- REGLA DE NEGOCIO #13
    
    kilometraje        INTEGER      DEFAULT 0 NOT NULL,
    ciudad_operacion   VARCHAR(100) NOT NULL,
    sede_operacion     VARCHAR(100) NOT NULL,
    estado_vehiculo    VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    
    fecha_soat         DATE NOT NULL,
    fecha_rtm          DATE NOT NULL,
    fecha_ultimo_mant  DATE NOT NULL,
    
    activo             BOOLEAN      NOT NULL DEFAULT TRUE, -- Bandera de Soft Delete
    creado_en          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en     TIMESTAMP,
    
    version            BIGINT       NOT NULL DEFAULT 0, -- Token de Concurrencia Optimista
    id_tipo_vehiculo   BIGINT       NOT NULL,

    CONSTRAINT fk_vehiculo_tipo FOREIGN KEY (id_tipo_vehiculo)
        REFERENCES tipos_vehiculo(id_tipo_vehiculo),
        
    CONSTRAINT chk_estado_vehiculo CHECK (
        estado_vehiculo IN ('DISPONIBLE', 'RESERVADO', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO')
    )
);

COMMENT ON TABLE vehiculos IS 'Tabla central del microservicio. Almacena los activos físicos de la flota con reglas de negocio #9, #13 y soporte nativo para Optimistic Locking.';

-- Índices B-Tree estratégicos para optimizar el rendimiento de lectura (ISO 25010 - Eficiencia de Rendimiento)
CREATE INDEX idx_vehiculos_placa   ON vehiculos(numero_placa);
CREATE INDEX idx_vehiculos_estado  ON vehiculos(estado_vehiculo);
CREATE INDEX idx_vehiculos_activo  ON vehiculos(activo);

-- =============================================================================
-- TABLA: historial_estados_vehiculo (Auditoría Forense Append-Only)
-- =============================================================================
CREATE TABLE historial_estados_vehiculo (
    id_historial     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    id_vehiculo      UUID         NOT NULL,
    estado_anterior  VARCHAR(30) NOT NULL,
    estado_nuevo     VARCHAR(30)  NOT NULL,
    motivo_cambio    VARCHAR(255) NOT NULL,
    servicio_origen  VARCHAR(100) NOT NULL,
    id_correlacion   VARCHAR(100) ,
    registrado_en    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo) ON DELETE CASCADE
    -- ON DELETE CASCADE: Si el Agregado Raíz es destruido físicamente, sus trazas 
    -- históricas se purgan de forma automática para evitar registros huérfanos.
);

-- =============================================================================
-- TABLA: sagas_vehiculo (Distributed Saga State Machine Log / Orchestration Log)
-- =============================================================================
CREATE TABLE sagas_vehiculo (
    id_saga             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    id_vehiculo         UUID         NOT NULL,
    tipo_operacion      VARCHAR(50)  NOT NULL,
    estado_saga         VARCHAR(20)  NOT NULL DEFAULT 'INICIADA',
    clave_idempotencia  VARCHAR(100) NOT NULL UNIQUE, -- Barrera contra ataques de Replay
    intentos            INTEGER      DEFAULT 1,
    payload             TEXT,        -- Almacenamiento del contexto operativo original
    ultimo_error        TEXT,
    compensado_por      VARCHAR(100), -- REGLA DE NEGOCIO 
    version             BIGINT       NOT NULL DEFAULT 0,
    creado_en           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMP,

    CONSTRAINT fk_saga_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo),
        
    CONSTRAINT chk_estado_saga CHECK (
        estado_saga IN ('INICIADA', 'EN_PROGRESO', 'COMPLETADA', 'FALLIDA', 'COMPENSADA')
    )
);

COMMENT ON COLUMN sagas_vehiculo.compensado_por IS 'REGLA DE NEGOCIO: Almacena de forma inmutable el rol del usuario que firmó el Rollback de la Saga.';

-- =============================================================================
-- TABLA: reservas_vehiculo (Transactional Contracts / Frontera Transaccional)
-- =============================================================================
CREATE TABLE reservas_vehiculo (
    id_reserva          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    id_vehiculo         UUID         NOT NULL,
    id_saga             UUID,
    id_asignacion_ext   UUID,        -- Llave lógica de correlación con microservicios satélites
    estado_reserva      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    clave_idempotencia  VARCHAR(100) NOT NULL UNIQUE,
    solicitado_por      VARCHAR(100) NOT NULL,
    fecha_inicio        TIMESTAMP NOT NULL,
    fecha_fin           TIMESTAMP NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 0,
    creado_en           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMP,

    CONSTRAINT fk_reserva_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo),
        
    CONSTRAINT fk_reserva_saga FOREIGN KEY (id_saga)
        REFERENCES sagas_vehiculo(id_saga),
        
    CONSTRAINT chk_estado_reserva CHECK (
        estado_reserva IN ('PENDIENTE', 'CONFIRMADA', 'FALLIDA', 'CANCELADA')
    )
);

COMMENT ON TABLE reservas_vehiculo IS 'Frontera transaccional de contratos de asignación de activos. Resguarda las reglas de solapamiento temporal coordinadas por la capa Java.';

-- Índices optimizados para búsquedas operacionales cruzadas de alta frecuencia
CREATE INDEX idx_reserva_vehiculo ON reservas_vehiculo(id_vehiculo);
CREATE INDEX idx_reserva_clave    ON reservas_vehiculo(clave_idempotencia);

-- =============================================================================
-- FIN DEL SCRIPT DE BASELINE DATA EVOLUTION V1
-- =============================================================================