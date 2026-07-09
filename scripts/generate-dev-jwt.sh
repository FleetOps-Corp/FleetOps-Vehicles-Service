#!/usr/bin/env bash
# Genera un JWT de desarrollo firmado con secrets/jwt_private.pem
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SECRETS_DIR="${ROOT_DIR}/secrets"
PRIVATE_KEY="${SECRETS_DIR}/jwt_private.pem"
PUBLIC_KEY="${SECRETS_DIR}/jwt_public.pem"
SUBJECT="${JWT_SUBJECT:-dev-local@fleetops.com}"
EXPIRES_DAYS="${JWT_EXPIRES_DAYS:-30}"
CLASSPATH_FILE="${ROOT_DIR}/target/dev-jwt.classpath"

mkdir -p "${SECRETS_DIR}"

if [[ ! -f "${PRIVATE_KEY}" ]]; then
  echo "Generando par RSA de desarrollo en ${SECRETS_DIR}..." >&2
  openssl genrsa -out "${PRIVATE_KEY}" 2048 2>/dev/null
  openssl rsa -in "${PRIVATE_KEY}" -pubout -out "${PUBLIC_KEY}" 2>/dev/null
  echo "Claves creadas. Reinicia vehicles-service:" >&2
  echo "  docker compose restart vehicles-service" >&2
fi

if [[ ! -f "${PUBLIC_KEY}" ]]; then
  openssl rsa -in "${PRIVATE_KEY}" -pubout -out "${PUBLIC_KEY}" 2>/dev/null
fi

cd "${ROOT_DIR}"

echo "Compilando DevJwtGenerator (primera vez ~30s)..." >&2
bash mvnw -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile="${CLASSPATH_FILE}"

TOKEN="$(java -cp "target/test-classes:target/classes:$(cat "${CLASSPATH_FILE}")" \
  com.fleetops.vehicles.support.DevJwtGenerator \
  "${SUBJECT}" "${EXPIRES_DAYS}" "${PRIVATE_KEY}")"

if [[ -z "${TOKEN}" ]]; then
  echo "Error: no se pudo generar el token." >&2
  exit 1
fi

echo "${TOKEN}"
