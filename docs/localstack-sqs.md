# LocalStack — pruebas SQS en local

Entorno local que emula **SNS + SQS** de AWS para probar Incidentes y Mantenimiento sin credenciales reales.

---

## Arquitectura local

```
scripts/publish-*.sh  →  SNS (LocalStack)  →  SQS  →  vehicles-service
Kafka UI :8082        →  Kafka             →  vehicles-service (Asignaciones)
```

| Servicio | URL |
|----------|-----|
| LocalStack | http://localhost:4566 |
| Kafka UI | http://localhost:8082 |
| Vehicles API | http://localhost:8081 |

---

## Levantar el stack

```bash
cp .env.example .env
# Edita .env si hace falta (JWT, etc.)

docker compose up -d --build
docker compose ps
```

LocalStack crea automáticamente:

| Recurso | Nombre |
|---------|--------|
| Topic SNS incidentes | `incidents_topic` |
| Cola SQS incidentes | `queue_vehicles` |
| Topic SNS mantenimiento | `maintenance_topic` |
| Cola SQS mantenimiento | `queue_vehicles_maintenance` |

---

## Variables (`.env` / docker-compose)

```properties
SQS_ENABLED=true
AWS_ENDPOINT_URL=http://localstack:4566
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=test
AWS_SECRET_ACCESS_KEY=test
SQS_VEHICLES_QUEUE_URL=http://localstack:4566/000000000000/queue_vehicles
MAINTENANCE_SQS_QUEUE_URL=http://localstack:4566/000000000000/queue_vehicles_maintenance
```

Desde el **host** (scripts), usa `AWS_ENDPOINT_URL=http://localhost:4566`.

En **docker compose**, el contenedor usa `AWS_ENDPOINT_URL_CONTAINER` (por defecto `http://localstack:4566`). En **EC2/AWS** debe estar vacío en `.env`:

```properties
AWS_ENDPOINT_URL_CONTAINER=
```

Sin esa variable, `docker-compose.yml` seguiría apuntando a LocalStack aunque las URLs SQS sean de AWS real.

---

## Publicar eventos de prueba

Requisito: [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) instalado.

```bash
chmod +x scripts/localstack/*.sh

# Incidente mecánico grave (placa debe existir en BD)
./scripts/localstack/publish-incident.sh ABC123

# Mantenimiento: vehículo entra en taller
./scripts/localstack/publish-maintenance.sh CREATED <uuid-vehiculo>

# Mantenimiento: vehículo sale a disponible
./scripts/localstack/publish-maintenance.sh COMPLETED <uuid-vehiculo>
```

---

## Verificar

```bash
# Logs del consumidor SQS
docker compose logs -f vehicles-service | grep -iE "incidente|mantenimiento|Cambio de estado"

# Cola (mensajes pendientes)
aws --endpoint-url http://localhost:4566 sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/queue_vehicles \
  --attribute-names ApproximateNumberOfMessages

# Estado del vehículo (con JWT)
curl -H "Authorization: Bearer <token>" http://localhost:8081/vehiculos/placa/ABC123
```

---

## Kafka (Asignaciones) en el mismo compose

1. Abre http://localhost:8082  
2. Publica en `fleetops.vehiculos.solicitar`  
3. Observa `confirmado` / `fallido` y reservas en la API  

Ver [contrato-kafka.md](contrato-kafka.md).

---

## Sin Docker (solo Mockito)

```bash
bash mvnw test -Dtest=MaintenanceIntegrationServiceTest,IncidentIntegrationServiceTest
```

---

## Troubleshooting

| Problema | Solución |
|----------|----------|
| `Connection refused` a LocalStack | `docker compose logs localstack` — espera healthcheck verde |
| Listener SQS no arranca | `SQS_ENABLED=true` y cola URL con `localstack:4566` dentro de Docker |
| Incidente sin efecto | Placa debe existir en seed (`GET /vehiculos`) |
| Mantenimiento sin efecto | `vehicleId` debe ser UUID de un vehículo activo |
| Scripts fallan | `export AWS_ENDPOINT_URL=http://localhost:4566` |
