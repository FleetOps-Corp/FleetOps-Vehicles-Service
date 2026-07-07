# Contrato Kafka — FleetOps Vehículos (Contrato A+)

Documento de integración entre **Asignaciones** (`develop`) y **Vehículos**.  
Última actualización: julio 2026.

**Evolución del contrato:** [contrato-a-plus-vs-dos-pasos.md](contrato-a-plus-vs-dos-pasos.md) — comparación con el contrato dos pasos y motivos de la adopción de A+.

---

## Resumen

**Contrato A+** = Saga coreografiada alineada con Asignaciones:

1. `solicitar` → reserva **CONFIRMADA** + saga **COMPLETADA** + publicar `confirmado` (misma transacción DB+Kafka).
2. `completada` → solo **ACK** local (no cambia el estado de la reserva).
3. `liberar` → compensación (`CANCELADA` + `COMPENSADA`) + permite re-solicitar misma `idSaga`.
4. Job de reconciliación → republica `confirmado` si no hay ACK; auto-compensa tras N reintentos.

| Topic | Productor | Consumidor | Propósito |
|-------|-----------|------------|-----------|
| `fleetops.vehiculos.solicitar` | Asignaciones | Vehículos | Pedir asignación de vehículo |
| `fleetops.asignaciones.vehiculo.confirmado` | Vehículos | Asignaciones | Reserva CONFIRMADA + vehículo asignado |
| `fleetops.asignaciones.vehiculo.fallido` | Vehículos | Asignaciones | No se pudo asignar (motivo) |
| `fleetops.vehiculos.liberar` | Asignaciones | Vehículos | Cancelar/liberar calendario |
| `fleetops.asignaciones.completada` | Asignaciones | Vehículos | **ACK** — confirma que Asignaciones procesó `confirmado` |

**Group ID consumidor (Vehículos):** `spring.kafka.consumer.group-id` (por defecto `vehicles-service`).

---

## 1. Solicitar vehículo

**Topic:** `fleetops.vehiculos.solicitar`  
**Dirección:** Asignaciones → Vehículos  
**Semántica:** at-least-once; Vehículos es idempotente por `idSaga`.

### Payload (`VehicleRequestEvent`)

```json
{
  "idSaga": "550e8400-e29b-41d4-a716-446655440001",
  "idAsignacion": "660e8400-e29b-41d4-a716-446655440002",
  "tipoVehiculo": "Furgon",
  "fechaInicio": "2026-07-10",
  "fechaFin": "2026-07-15",
  "kilometros": 120
}
```

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `idSaga` | UUID | Sí | Correlación saga; clave de idempotencia |
| `idAsignacion` | UUID | Sí | ID de asignación en Asignaciones |
| `tipoVehiculo` | string | Sí | Búsqueda parcial insensible a mayúsculas |
| `fechaInicio` | date (ISO) | Sí | Inicio del rango (00:00:00 local) |
| `fechaFin` | date (ISO) | Sí | Fin del rango (23:59:59 local) |
| `kilometros` | int | No | Metadato opcional |

### Respuestas (Vehículos → Asignaciones)

#### Confirmado — `fleetops.asignaciones.vehiculo.confirmado`

```json
{
  "idAsignacion": "660e8400-e29b-41d4-a716-446655440002",
  "idVehiculo": "770e8400-e29b-41d4-a716-446655440003"
}
```

#### Fallido — `fleetops.asignaciones.vehiculo.fallido`

```json
{
  "idAsignacion": "660e8400-e29b-41d4-a716-446655440002",
  "motivo": "Sin vehículos disponibles del tipo Furgon"
}
```

### Reglas de negocio (Vehículos)

- Crea reserva `CONFIRMADA` y saga `COMPLETADA` en un solo paso.
- Publica `confirmado` en la **misma transacción encadenada** DB + Kafka.
- Reintento con misma `idSaga` ya confirmada → republica `confirmado` (idempotente).
- Tras `liberar` con `permiteReasignacion=true` → nuevo `solicitar` con misma `idSaga` asigna otro vehículo.
- Reserva `CANCELADA` sin reasignación permitida → `fallido` *"cancelada previamente"*.

---

## 2. ACK de Asignaciones (`completada`)

**Topic:** `fleetops.asignaciones.completada`  
**Dirección:** Asignaciones → Vehículos  
**Semántica:** Asignaciones publica esto al cerrar su saga tras recibir `confirmado`.

### Payload (`AssignmentCompletedEvent`)

```json
{
  "idSaga": "550e8400-e29b-41d4-a716-446655440001",
  "idAsignacion": "660e8400-e29b-41d4-a716-446655440002",
  "idVehiculo": "770e8400-e29b-41d4-a716-446655440003",
  "idConductor": "880e8400-e29b-41d4-a716-446655440004"
}
```

### Comportamiento en Vehículos

- Marca `asignaciones_ack = true` en la saga local.
- **No** modifica `estado_reserva` (ya es `CONFIRMADA`).
- Idempotente: ACK duplicado se ignora.

---

## 3. Liberar vehículo (compensación)

**Topic:** `fleetops.vehiculos.liberar`  
**Dirección:** Asignaciones → Vehículos  
**Semántica:** at-least-once; compensación idempotente.

### Payload (`VehicleReleaseEvent`)

Asignaciones envía hoy (incidente mecánico):

```json
{
  "idSaga": "550e8400-e29b-41d4-a716-446655440001",
  "idVehiculo": "770e8400-e29b-41d4-a716-446655440003"
}
```

Payload extendido (recomendado):

```json
{
  "idAsignacion": "660e8400-e29b-41d4-a716-446655440002",
  "idSaga": "550e8400-e29b-41d4-a716-446655440001",
  "idVehiculo": "770e8400-e29b-41d4-a716-446655440003",
  "motivo": "Incidente mecánico grave",
  "origen": "ASIGNACIONES"
}
```

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `idAsignacion` | UUID | Condicional* | ID en Asignaciones |
| `idSaga` | UUID | Condicional* | ID saga distribuida |
| `idVehiculo` | UUID | No | Vehículo afectado (verificación opcional) |
| `motivo` | string | No** | Default: *"Compensación solicitada por Asignaciones"* |
| `origen` | string | No | Ej. `ASIGNACIONES` |

\* Al menos uno de `idAsignacion` o `idSaga`.  
\** Vehículos tolera ausencia de `motivo` (compatibilidad con Asignaciones actual).

### Comportamiento en Vehículos

1. Busca reserva por `idAsignacion` o `idSaga`.
2. Si no existe → log warn, evento ignorado.
3. Si ya `CANCELADA` / saga `COMPENSADA` → idempotente.
4. Si `CONFIRMADA` → `CANCELADA` + saga `COMPENSADA` + `permiteReasignacion=true`.
5. No publica evento de respuesta.

---

## 4. Reconciliación (solo Vehículos)

Job programado (`SagaReconciliationJob`) para el caso *"confirmamos pero Asignaciones no recibió/procesó"*:

| Fase | Condición | Acción |
|------|-----------|--------|
| Republicar | `CONFIRMADA` + sin ACK + antigüedad > 30 min + reintentos < 3 | Republica `confirmado` |
| Auto-compensar | Sin ACK + antigüedad > 4 h + reintentos ≥ 3 | `CANCELADA` + alerta en logs |

Configuración (`application.properties`):

```properties
fleetops.saga.reconciliacion.gracia-minutos=30
fleetops.saga.reconciliacion.compensar-despues-minutos=240
fleetops.saga.reconciliacion.max-reconfirmaciones=3
fleetops.saga.reconciliacion.intervalo-ms=900000
```

---

## 5. Correlación e idempotencia

| Clave | Uso |
|-------|-----|
| `idSaga` | Idempotencia en `solicitar`; lookup en `liberar` |
| `idAsignacion` | Índice único `id_asignacion_ext`; lookup principal |
| `clave_idempotencia` | Persistida como `idSaga.toString()` |
| `asignaciones_ack` | ACK de `completada` en saga local |
| `permite_reasignacion` | Habilita nuevo `solicitar` tras `liberar` |

---

## 6. Errores y reintentos

| Escenario | `solicitar` | `liberar` | `completada` |
|-----------|-------------|-----------|--------------|
| Error de validación | `fallido` | Ignora (log warn) | — |
| Excepción interna | `fallido` | Relanza → retry Kafka | Relanza → retry |
| Reintento duplicado | Republica resultado | No-op idempotente | ACK idempotente |
| Sin ACK tras tiempo | Job republica `confirmado` | — | — |

---

## 7. Referencia de código

| Componente | Clase |
|------------|-------|
| Consumer solicitar | `VehicleRequestConsumer` |
| Coordinator DB+Kafka | `VehicleAssignmentCoordinator` |
| Consumer liberar | `VehicleReleaseConsumer` |
| Consumer ACK completada | `AssignmentCompletedConsumer` |
| Reconciliación | `SagaReconciliationJob` |
| Lógica saga | `SagaServiceImpl` |
| Kafka config | `KafkaConfig` |
| Topics | `KafkaTopics` |

---

## Changelog

| Fecha | Cambio |
|-------|--------|
| 2026-07 | **Contrato A+**: CONFIRMADA en solicitar, completada como ACK, reconciliación, liberar tolerante |
| 2026-07 | Consumer `fleetops.vehiculos.liberar` |
| 2026-07 | Contrato A inicial (documentado) |
