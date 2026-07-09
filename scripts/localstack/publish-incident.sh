#!/usr/bin/env bash
# Publica un incidente mecánico grave vía SNS → SQS (como Incidentes en AWS).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=common.sh
source "${SCRIPT_DIR}/common.sh"

PLACA="${1:-ABC123}"
INCIDENT_ID="${2:-inc-local-$(date +%s)}"
SEVERITY="${3:-GRAVE}"
TYPE="${4:-MECANICO}"

MESSAGE="$(cat <<EOF
{
  "incident_id": "${INCIDENT_ID}",
  "vehicle_id": "${PLACA}",
  "incident_type": "${TYPE}",
  "severity": "${SEVERITY}",
  "description": "Prueba LocalStack - falla mecánica"
}
EOF
)"

echo "Publicando incidente ${INCIDENT_ID} (placa ${PLACA}, ${TYPE}/${SEVERITY})..."
aws_local sns publish \
  --topic-arn "${INCIDENTS_TOPIC_ARN}" \
  --message "${MESSAGE}" \
  --message-attributes '{"event_type":{"DataType":"String","StringValue":"incident_registered"}}'

echo "OK. Revisa logs: docker compose logs -f vehicles-service | grep -i incidente"
