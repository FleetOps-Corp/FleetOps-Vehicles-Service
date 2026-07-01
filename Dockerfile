# syntax=docker/dockerfile:1

# =============================================================================
# STAGE 1: BUILDER
# Compila el proyecto y genera el jar ejecutable con Maven + JDK 21.
# Esta capa NO se incluye en la imagen final.
# =============================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# 1. Copiamos primero solo los archivos necesarios para resolver dependencias.
#    Esto permite que Docker cachee la capa de descarga de Maven: mientras el
#    pom.xml no cambie, los rebuilds siguientes reutilizan esta capa y no
#    vuelven a descargar el repositorio .m2 completo.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw \
    && ./mvnw -B -q dependency:go-offline

# 2. Copiamos el código fuente recién ahora (cambia con más frecuencia que
#    el pom.xml), para no invalidar la capa de dependencias en cada build.
COPY src/ src/

# 3. Compilamos y empaquetamos el jar ejecutable (spring-boot-maven-plugin),
#    sin ejecutar tests aquí (se asume que el pipeline de CI ya los ejecutó).
RUN ./mvnw -B -q clean package -DskipTests

# =============================================================================
# STAGE 2: RUNTIME
# Imagen final, mínima, solo con el JRE y el jar generado.
# =============================================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

# Metadata OCI de la imagen
LABEL org.opencontainers.image.title="fleetops-vehicles" \
      org.opencontainers.image.description="Microservicio de gestion de vehiculos - FleetOps" \
      org.opencontainers.image.vendor="FleetOps" \
      org.opencontainers.image.source="https://github.com/fleetops/vehiculos"

# Creamos un grupo y usuario NO ROOT dedicados para ejecutar la aplicacion.
RUN groupadd --system fleetops && \
    useradd --system --gid fleetops --no-create-home --shell /usr/sbin/nologin fleetops

WORKDIR /app

# Copiamos únicamente el jar generado en el stage anterior (nada de código
# fuente, pom.xml ni caché de Maven llegan a esta imagen).
COPY --from=builder --chown=fleetops:fleetops /build/target/fleetops-vehicles-0.0.1-SNAPSHOT.jar app.jar

# Variables de entorno esperadas por la aplicacion (valores por defecto para
# desarrollo local; deben sobreescribirse en runtime vía --env / --env-file /
# docker-compose / orquestador en EC2). Ningún secreto real se hardcodea aquí.
ENV SERVER_PORT=8081 \
    DB_HOST=localhost \
    DB_PORT=5432 \
    DB_NAME=fleetops_vehicles \
    DB_USERNAME=fleetops \
    DB_PASSWORD="" \
    JWT_SECRET_KEY="" \
    MAINTENANCE_SERVICE_URL=http://localhost:8083 \
    CORS_ALLOWED_ORIGINS="" \
    SHOW_SQL=false \
    JAVA_OPTS=""

EXPOSE 8081

USER fleetops

# Healthcheck contra el endpoint de Actuator ya expuesto por la aplicacion.
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- "http://localhost:${SERVER_PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

# exec form + variable expansion vía shell para permitir JAVA_OPTS dinámico.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
