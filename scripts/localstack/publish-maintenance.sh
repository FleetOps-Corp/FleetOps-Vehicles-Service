#!/usr/bin/env bash
# Publica evento de mantenimiento CREATED o COMPLETED vía SNS → SQS.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

EVENT="${1:-CREATED}"
VEHICLE_ID="${2:-bc5d79f4-0ef7-43dd-9038-6382d51d58e0}"
MAINTENANCE_ID="${3:-9f26d4de-d43b-4d9e-a8d8-cba72b9d96d1}"

case "${EVENT}" in
  CREATED|created)
    STATUS="CREATED"
    EVENT_TYPE="maintenance_created"
    ;;
  COMPLETED|completed)
    STATUS="COMPLETED"
    EVENT_TYPE="maintenance_completed"
    ;;
  *)
    echo "Uso: $0 [CREATED|COMPLETED] [vehicleId] [maintenanceId]"
    exit 1
    ;;
esac

NOW="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

MESSAGE="$(cat <<EOF
{
  "maintenanceId": "${MAINTENANCE_ID}",
  "vehicleId": "${VEHICLE_ID}",
  "maintenanceType": "CORRECTIVE",
  "status": "${STATUS}",
  "occurredAt": "${NOW}"
}
EOF
)"

echo "Publicando mantenimiento ${STATUS} (${EVENT_TYPE}) para vehículo ${VEHICLE_ID}..."
aws_local sns publish \
  --topic-arn "${MAINTENANCE_TOPIC_ARN}" \
  --message "${MESSAGE}" \
  --message-attributes "{\"event_type\":{\"DataType\":\"String\",\"StringValue\":\"${EVENT_TYPE}\"}}"

echo "OK. Revisa logs: docker compose logs -f vehicles-service | grep -i mantenimiento"
