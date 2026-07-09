# Contrato SQS — Mantenimiento → Vehículos

Integración con **FleetOps-Maintenance-Service** según la guía SNS/SQS de la plataforma (julio 2026).

---

## Arquitectura

```
Maintenance-Service  →  SNS (maintenance_topic)  →  SQS queue_vehicles  →  Vehicles-Service
```

- **Fan-out:** cada microservicio consume desde su propia cola sin afectar a los demás servicios.
- **Doble deserialización:** el `Body` de SQS contiene un sobre SNS; el evento real se encuentra en la propiedad `Message` como un JSON serializado.

---

## Cola de Vehículos

| Campo | Valor |
|-------|-------|
| URL | `https://sqs.us-east-1.amazonaws.com/255615880629/queue_vehicles` |
| ARN | `arn:aws:sqs:us-east-1:255615880629:queue_vehicles` |
| Región | `us-east-1` |
| `event_type` | `maintenance_completed` *(también se acepta `maintenanceFinished` por compatibilidad)* |

---

## Configuración (`application.properties`)

```properties
fleetops.sqs.enabled=${SQS_ENABLED:false}
fleetops.sqs.maintenance.queue-url=${MAINTENANCE_SQS_QUEUE_URL}
fleetops.sqs.maintenance.event-type=maintenance_completed

spring.cloud.aws.region.static=us-east-1
```

En **local** dejar `SQS_ENABLED=false` (no requiere credenciales AWS).

En **AWS/EC2** activar con IAM Role o credenciales IAM y `SQS_ENABLED=true`.

---

## Payload del evento (`maintenance_completed`)

| Campo | Tipo | Notas |
|-------|------|-------|
| `maintenance_id` | UUID | Idempotencia (`historial.id_correlacion`) |
| `vehicle_id` | string | **Placa** del vehículo |
| `maintenance_type` | string | Informativo |
| `description` | string | Observaciones del mantenimiento |
| `completion_date` | ISO-8601 | Fecha de finalización |
| `eventType` | string | `maintenance_completed` o `maintenanceFinished` |

También se aceptan los nombres definidos por el productor siempre que sean compatibles con el parser (`MaintenanceSnsMessageParser`).

---

## Comportamiento en Vehículos

| Evento | Acción |
|---------|--------|
| `maintenance_completed` | Cambia el estado operativo del vehículo a **DISPONIBLE** |
| `maintenanceFinished` | Mismo comportamiento (compatibilidad temporal) |
| Sin placa o datos inválidos | Log warn; mensaje descartado |

- **No crea ni modifica mantenimientos.**
- **No cancela ni modifica reservas.**
- El único cambio realizado es el **estado operativo** del vehículo.

- **Idempotencia:** si `maintenance_id` ya fue procesado previamente (por `id_correlacion`), el mensaje se ignora.
- **Origen auditoría:** `MANTENIMIENTO-SQS`.
- **Errores de formato:** mensaje descartado (sin reintentos infinitos para JSON inválido).

---

## Componentes

| Clase | Rol |
|-------|-----|
| `MaintenanceCompletedSqsListener` | `@SqsListener` sobre `queue_vehicles` |
| `MaintenanceSnsMessageParser` | Desempaqueta el sobre SNS y convierte el mensaje al evento de mantenimiento |
| `MaintenanceIntegrationService` | Ejecuta la lógica de negocio y cambia el estado del vehículo a **DISPONIBLE** |

---

## Prueba end-to-end

1. Coordinar con Mantenimiento el envío de un evento de finalización.
2. Verificar en AWS Console → SQS → `queue_vehicles` que el mensaje llegue a la cola.
3. Revisar los logs de `vehicles-service`.
4. Confirmar un mensaje similar a:

```
Mantenimiento aplicado: placa ABC123 → DISPONIBLE
```

5. Consultar:

```
GET /vehiculos/placa/{placa}
```

o el historial del vehículo para verificar el cambio de estado.

---

## Relación con Kafka

| Canal | Responsabilidad en Vehículos |
|-------|------------------------------|
| **SQS (Mantenimiento)** | Cambiar el estado operativo del vehículo a **DISPONIBLE** cuando finaliza un mantenimiento |
| **SQS (Incidentes)** | Cambiar el estado operativo a **EN_MANTENIMIENTO** o **FUERA_DE_SERVICIO** según la gravedad |
| **Kafka `liberar` (Asignaciones)** | Liberación y compensación de reservas |
| **Kafka saga** (`solicitar`, `confirmado`, `completada`) | Orquestación de asignaciones de vehículos |

Cuando un mantenimiento finaliza, el flujo esperado es:

```
Maintenance-Service
        │
        ▼
      SNS
        │
        ▼
 queue_vehicles (SQS)
        │
        ▼
Vehicles-Service
        │
        ▼
Vehículo → DISPONIBLE
```