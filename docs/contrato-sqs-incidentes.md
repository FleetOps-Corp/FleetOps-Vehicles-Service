# Contrato SQS — Incidentes → Vehículos

Integración con **FleetOps-Incidents-Service** según la guía SNS/SQS de la plataforma (julio 2026).

---

## Arquitectura

```
Incidents-Service  →  SNS (incidents_topic)  →  SQS queue_vehicles  →  Vehicles-Service
```

- **Fan-out:** cada microservicio tiene su propia cola; el consumo en Vehículos no afecta a Mantenimiento ni Asignaciones.
- **Doble deserialización:** el `Body` de SQS es un sobre SNS; el evento real está en la clave `Message` (string JSON).

---

## Cola de Vehículos

| Campo | Valor |
|-------|-------|
| URL | `https://sqs.us-east-1.amazonaws.com/255615880629/queue_vehicles` |
| ARN | `arn:aws:sqs:us-east-1:255615880629:queue_vehicles` |
| Región | `us-east-1` |
| `event_type` | `incident_registered` |

---

## Configuración (`application.properties`)

```properties
fleetops.sqs.enabled=${SQS_ENABLED:false}
fleetops.sqs.incidents.queue-url=${SQS_VEHICLES_QUEUE_URL:...}
spring.cloud.aws.region.static=us-east-1
```

En **local** dejar `SQS_ENABLED=false` (no requiere credenciales AWS).  
En **AWS/EC2** activar con IAM Role y `SQS_ENABLED=true`.

---

## Payload del evento (`incident_registered`)

| Campo | Tipo | Notas |
|-------|------|-------|
| `incident_id` | UUID | Idempotencia (`historial.id_correlacion`) |
| `vehicle_id` | string | **Placa** del vehículo |
| `incident_type` | `HUMANO` \| `MECANICO` | |
| `severity` | `LEVE` \| `GRAVE` | |
| `description` | string | Motivo en historial |
| `driver_id` | string | Informativo |
| `event_date` | ISO-8601 | Informativo |

También se aceptan alias en español del productor (`id`, `placa_vehiculo`, `tipo_incidente`, `gravedad`).

---

## Comportamiento en Vehículos

| Tipo | Gravedad | Acción |
|------|----------|--------|
| `HUMANO` | cualquiera | Solo log; no cambia estado |
| `MECANICO` | `GRAVE` | `FUERA_DE_SERVICIO` (**solo estado** del vehículo) |
| `MECANICO` | `LEVE` | `EN_MANTENIMIENTO` (**solo estado** del vehículo) |
| Otro / sin placa | — | Log warn; mensaje descartado |

- **No cancela ni compensa reservas.** La liberación del calendario es responsabilidad exclusiva de **Asignaciones** vía Kafka `fleetops.vehiculos.liberar`.

- **Idempotencia:** si `incident_id` ya existe en `historial_estados_vehiculo.id_correlacion`, se ignora.
- **Origen auditoría:** `INCIDENTES-SQS`.
- **Errores de formato:** mensaje descartado (no reintento infinito por JSON inválido).

---

## Componentes

| Clase | Rol |
|-------|-----|
| `IncidentRegisteredSqsListener` | `@SqsListener` sobre `queue_vehicles` |
| `SnsMessageParser` | Desempaqueta sobre SNS → `IncidentRegisteredEvent` |
| `IncidentIntegrationService` | Reglas de negocio + `VehicleService.changeOperationalStateOnly` |

---

## Prueba end-to-end

1. Coordinar con Incidentes un POST de incidente de prueba.
2. En AWS Console → SQS → `queue_vehicles` → Poll messages.
3. Verificar logs de `vehicles-service`: `Incidente ... aplicado: placa ... → FUERA_DE_SERVICIO`.
4. `GET /vehiculos/placa/{placa}` o historial del vehículo.

---

## Relación con Kafka

| Canal | Responsabilidad en Vehículos |
|-------|------------------------------|
| **SQS (Incidentes)** | Solo cambio de estado operativo (`DISPONIBLE` / `EN_MANTENIMIENTO` / `FUERA_DE_SERVICIO`) |
| **Kafka `liberar` (Asignaciones)** | Única vía para compensar/liberar reservas en el calendario |
| **Kafka saga** (`solicitar`, `confirmado`, `completada`) | Orquestación de asignación de vehículos |

Ante un incidente mecánico grave, el flujo esperado es: Incidentes publica SQS → Vehículos pone el activo fuera de servicio; Asignaciones publica `liberar` → Vehículos cancela la reserva.
