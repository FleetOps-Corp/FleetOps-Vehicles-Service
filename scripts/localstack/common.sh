#!/usr/bin/env bash
# Variables compartidas para scripts contra LocalStack.
ENDPOINT_URL="${AWS_ENDPOINT_URL:-http://localhost:4566}"
AWS_REGION="${AWS_REGION:-us-east-1}"
ACCOUNT_ID="000000000000"

INCIDENTS_TOPIC_ARN="arn:aws:sns:${AWS_REGION}:${ACCOUNT_ID}:incidents_topic"
MAINTENANCE_TOPIC_ARN="arn:aws:sns:${AWS_REGION}:${ACCOUNT_ID}:maintenance_topic"

INCIDENTS_QUEUE_URL="${SQS_VEHICLES_QUEUE_URL:-${ENDPOINT_URL}/${ACCOUNT_ID}/queue_vehicles}"
MAINTENANCE_QUEUE_URL="${MAINTENANCE_SQS_QUEUE_URL:-${ENDPOINT_URL}/${ACCOUNT_ID}/queue_vehicles_maintenance}"

aws_local() {
  aws --endpoint-url "${ENDPOINT_URL}" --region "${AWS_REGION}" "$@"
}
