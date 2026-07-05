# Contrato Kafka — FleetOps Vehículos

Documento de integración entre **Asignaciones** y **Vehículos**.  
Última actualización: julio 2026.

---

## Resumen de topics

| Topic | Productor | Consumidor | Propósito |
|-------|-----------|------------|-----------|
| `fleetops.vehiculos.solicitar` | Asignaciones | Vehículos | Pedir asignación de vehículo |
| `fleetops.asignaciones.vehiculo.confirmado` | Vehículos | Asignaciones | Reserva CONFIRMADA + vehículo asignado |
| `fleetops.asignaciones.vehiculo.fallido` | Vehículos | Asignaciones | No se pudo asignar (motivo) |
| `fleetops.vehiculos.liberar` | Asignaciones | Vehículos | Cancelar/liberar calendario |

**Group ID consumidor (Vehículos):** valor de `spring.kafka.consumer.group-id` (por defecto `fleetops-vehicles-group`).

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

- Crea reserva en estado `CONFIRMADA` y saga `COMPLETADA` en un solo paso (contrato A).
- Reintento con misma `idSaga` / `idAsignacion` ya confirmada → republica `confirmado` (idempotente).
- Reserva previa `CANCELADA` → responde `fallido` con motivo *"La asignación fue cancelada previamente"*.

---

## 2. Liberar vehículo (cancelación)

**Topic:** `fleetops.vehiculos.liberar`  
**Dirección:** Asignaciones → Vehículos  
**Semántica:** at-least-once; Vehículos compensa de forma idempotente.

> **Estado del contrato:** JSON **provisional** acordado con Asignaciones.  
> Campos marcados con ⚠️ pueden cambiar antes del corte a producción.

### Payload propuesto (`VehicleReleaseEvent`)

```json
{
  "idAsignacion": "660e8400-e29b-41d4-a716-446655440002",
  "idSaga": "550e8400-e29b-41d4-a716-446655440001",
  "motivo": "Cancelación solicitada por el cliente",
  "origen": "ASIGNACIONES"
}
```

| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `idAsignacion` | UUID | Condicional* | ID en Asignaciones |
| `idSaga` | UUID | Condicional* | ID saga distribuida |
| `motivo` | string | Sí | Razón de negocio |
| `origen` | string | No ⚠️ | Ej. `ASIGNACIONES`, `INCIDENTES` |

\* Al menos uno de `idAsignacion` o `idSaga` debe estar presente.

### Comportamiento en Vehículos

1. Busca reserva por `idAsignacion` (preferido) o por `idSaga`.
2. Si no existe reserva local → log de advertencia, evento ignorado (sin error fatal).
3. Si ya `CANCELADA` / saga `COMPENSADA` → idempotente, sin cambios.
4. Si `CONFIRMADA` → `compensarPorReservaId`: reserva `CANCELADA`, saga `COMPENSADA`.
5. **No** publica evento de respuesta (flujo unidireccional).

### Cuándo NO usar este topic

- Cancelación originada en **Vehículos** (mantenimiento, cascada de emergencia): Asignaciones debe enterarse por su propio canal (futuro evento saliente o integración acordada).
- Asignación que nunca llegó a confirmarse: no aplica liberación; el flujo correcto es no reenviar `solicitar` o manejar `fallido`.

---

## 3. Correlación e idempotencia

| Clave | Uso |
|-------|-----|
| `idSaga` | Idempotencia en `solicitar`; lookup alternativo en `liberar` |
| `idAsignacion` | Índice único `id_asignacion_ext` en BD; lookup principal en `liberar` |
| `clave_idempotencia` | Persistida como `idSaga.toString()` en reserva |

---

## 4. Errores y reintentos

| Escenario | `solicitar` | `liberar` |
|-----------|-------------|-----------|
| Error de validación | Publica `fallido` | Ignora evento (log warn) |
| Excepción interna | Publica `fallido` | Relanza excepción → retry Kafka |
| Reintento duplicado | Republica resultado previo | No-op idempotente |

---

## 5. Referencia de código

| Componente | Clase |
|------------|-------|
| Consumer solicitar | `VehicleRequestConsumer` |
| Consumer liberar | `VehicleReleaseConsumer` |
| Producer respuestas | `VehicleEventProducer` |
| Lógica saga | `SagaServiceImpl` |
| Topics | `KafkaTopics` |

---

## Changelog

| Fecha | Cambio |
|-------|--------|
| 2026-07 | Contrato A: confirmación directa en `solicitar` |
| 2026-07 | Consumer `fleetops.vehiculos.liberar` (payload provisional) |
