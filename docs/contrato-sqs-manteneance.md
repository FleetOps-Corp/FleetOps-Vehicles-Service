# Contrato SQS — Mantenimiento → Vehículos

Integración con **FleetOps-Maintenance-Service** según la guía SNS/SQS de la plataforma (julio 2026).

---

## Arquitectura

```
Maintenance-Service  →  SNS (maintenance_topic)  →  SQS queue_vehicles_maintenance  →  Vehicles-Service
```

- **Fan-out:** cada microservicio consume desde su propia cola sin afectar a los demás servicios.
- **Doble deserialización:** el `Body` de SQS contiene un sobre SNS; el evento real se encuentra en la propiedad `Message` como un JSON serializado.
- **Tipo de evento:** no hay topics Kafka; se distingue por `event_type` en `MessageAttributes` de SNS (igual que Incidentes).

---

## Cola de Vehículos

| Campo | Valor |
|-------|-------|
| URL | `https://sqs.us-east-2.amazonaws.com/088538334491/queue_vehicles_maintenance` |
| Región | `us-east-2` (configurable vía `AWS_REGION`) |
| `event_type` creación | `maintenance_created` |
| `event_type` finalización | `maintenance_completed` *(también `maintenanceFinished` por compatibilidad)* |

---

## Configuración (`application.properties`)

```properties
fleetops.sqs.enabled=${SQS_ENABLED:true}
fleetops.sqs.maintenance.queue-url=${MAINTENANCE_SQS_QUEUE_URL}
fleetops.sqs.maintenance.event-type-created=maintenance_created
fleetops.sqs.maintenance.event-type=maintenance_completed
fleetops.sqs.maintenance.legacy-event-type-completed=maintenanceFinished

spring.cloud.aws.region.static=${AWS_REGION:us-east-1}
```

En **local** dejar `SQS_ENABLED=false` si no hay credenciales AWS.

En **AWS/EC2** activar con IAM Role o credenciales IAM y `SQS_ENABLED=true`.

---

## Payload del evento (`MaintenanceEvent`)

Formato acordado con Mantenimiento (camelCase):

### Creación (`maintenance_created`)

```json
{
  "maintenanceId": "9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1",
  "vehicleId": "bc5d79f4-0ef7-43dd-9038-6382d51d58e0",
  "maintenanceType": "CORRECTIVE",
  "status": "CREATED",
  "occurredAt": "2026-07-08T10:30:00Z"
}
```

### Finalización (`maintenance_completed`)

```json
{
  "maintenanceId": "9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1",
  "vehicleId": "bc5d79f4-0ef7-43dd-9038-6382d51d58e0",
  "maintenanceType": "CORRECTIVE",
  "status": "COMPLETED",
  "occurredAt": "2026-07-08T12:45:00Z"
}
```

| Campo | Tipo | Notas |
|-------|------|-------|
| `maintenanceId` | UUID | Correlación; idempotencia en historial |
| `vehicleId` | UUID | ID del vehículo en Vehículos |
| `maintenanceType` | string | Informativo (ej. `CORRECTIVE`) |
| `status` | string | `CREATED` o `COMPLETED` (fallback si falta `event_type` SNS) |
| `occurredAt` | ISO-8601 UTC | Acepta sufijo `Z` |

---

## Comportamiento en Vehículos

| `event_type` SNS | `status` (fallback) | Acción |
|------------------|---------------------|--------|
| `maintenance_created` | `CREATED` | Vehículo → **EN_MANTENIMIENTO** |
| `maintenance_completed` | `COMPLETED` | Vehículo → **DISPONIBLE** |
| `maintenanceFinished` | — | Igual que `maintenance_completed` (legacy) |

- **No crea ni modifica registros de mantenimiento** en Vehículos.
- **No cancela ni modifica reservas.** La liberación del calendario es vía Kafka `fleetops.vehiculos.liberar`.
- **Idempotencia:** `maintenanceId:CREATED` y `maintenanceId:COMPLETED` en `historial.id_correlacion`.
- **Origen auditoría:** `MANTENIMIENTO-SQS`.
- **Cambio de estado:** `VehicleService.changeOperationalStateOnly` (historial incluido).
- **Errores de formato:** mensaje descartado (sin reintentos infinitos para JSON inválido).

---

## Componentes

| Clase | Rol |
|-------|-----|
| `MaintenanceSqsListener` | `@SqsListener` sobre la cola de mantenimiento |
| `MaintenanceSnsMessageParser` | Desempaqueta el sobre SNS |
| `MaintenanceIntegrationService` | Reglas de negocio + cambio de estado operativo |

---

## Prueba end-to-end

1. Mantenimiento publica creación con `event_type=maintenance_created`.
2. Verificar vehículo en `EN_MANTENIMIENTO`:

```
GET /vehiculos/{id}
```

3. Mantenimiento publica finalización con `event_type=maintenance_completed`.
4. Verificar vehículo en `DISPONIBLE` y entrada en historial con origen `MANTENIMIENTO-SQS`.

---

## Relación con Kafka

| Canal | Responsabilidad en Vehículos |
|-------|------------------------------|
| **SQS creación** | `EN_MANTENIMIENTO` |
| **SQS finalización** | `DISPONIBLE` |
| **SQS (Incidentes)** | `EN_MANTENIMIENTO` o `FUERA_DE_SERVICIO` según gravedad |
| **Kafka `liberar` (Asignaciones)** | Compensar/liberar reservas en calendario |

Flujo esperado cuando un vehículo entra y sale de taller:

```
Maintenance (CREATED)  → SQS → Vehículo EN_MANTENIMIENTO
Maintenance (COMPLETED) → SQS → Vehículo DISPONIBLE
```
