#!/usr/bin/env bash
# Crea topics SNS, colas SQS y suscripciones fan-out para desarrollo local.
set -euo pipefail

export AWS_DEFAULT_REGION="us-east-1"

echo "[localstack] Creando recursos SNS/SQS para FleetOps Vehículos..."

INCIDENTS_TOPIC_ARN="$(awslocal sns create-topic --name incidents_topic --query 'TopicArn' --output text)"
MAINTENANCE_TOPIC_ARN="$(awslocal sns create-topic --name maintenance_topic --query 'TopicArn' --output text)"

INCIDENTS_QUEUE_URL="$(awslocal sqs create-queue --queue-name queue_vehicles --query 'QueueUrl' --output text)"
MAINTENANCE_QUEUE_URL="$(awslocal sqs create-queue --queue-name queue_vehicles_maintenance --query 'QueueUrl' --output text)"

INCIDENTS_QUEUE_ARN="$(awslocal sqs get-queue-attributes \
  --queue-url "${INCIDENTS_QUEUE_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)"
MAINTENANCE_QUEUE_ARN="$(awslocal sqs get-queue-attributes \
  --queue-url "${MAINTENANCE_QUEUE_URL}" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)"

awslocal sns subscribe \
  --topic-arn "${INCIDENTS_TOPIC_ARN}" \
  --protocol sqs \
  --notification-endpoint "${INCIDENTS_QUEUE_ARN}"

awslocal sns subscribe \
  --topic-arn "${MAINTENANCE_TOPIC_ARN}" \
  --protocol sqs \
  --notification-endpoint "${MAINTENANCE_QUEUE_ARN}"

echo "[localstack] incidents_topic=${INCIDENTS_TOPIC_ARN}"
echo "[localstack] queue_vehicles=${INCIDENTS_QUEUE_URL}"
echo "[localstack] maintenance_topic=${MAINTENANCE_TOPIC_ARN}"
echo "[localstack] queue_vehicles_maintenance=${MAINTENANCE_QUEUE_URL}"
echo "[localstack] Recursos listos."
