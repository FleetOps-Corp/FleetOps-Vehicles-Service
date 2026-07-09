#!/usr/bin/env bash
# Genera un JWT de desarrollo firmado con HS256 (mismo algoritmo que Seguridad)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SUBJECT="${JWT_SUBJECT:-dev-local@fleetops.com}"
EXPIRES_DAYS="${JWT_EXPIRES_DAYS:-30}"
JWT_SECRET="${JWT_SECRET:-esta_es_una_clave_secreta_muy_larga_para_desarrollo_local_1234567890}"
CLASSPATH_FILE="${ROOT_DIR}/target/dev-jwt.classpath"

cd "${ROOT_DIR}"

echo "Compilando DevJwtGenerator (primera vez ~30s)..." >&2
bash mvnw -q -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile="${CLASSPATH_FILE}"

TOKEN="$(java -cp "target/test-classes:target/classes:$(cat "${CLASSPATH_FILE}")" \
  com.fleetops.vehicles.support.DevJwtGenerator \
  "${SUBJECT}" "${EXPIRES_DAYS}" "${JWT_SECRET}")"

if [[ -z "${TOKEN}" ]]; then
  echo "Error: no se pudo generar el token." >&2
  exit 1
fi

echo "${TOKEN}"
