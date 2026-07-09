# FleetOps - Microservicio de Gestión de Vehículos

## Descripción General

**FleetOps Vehicles** es un microservicio backend construido con **Spring Boot 3.3.5** que gestiona operaciones de vehículos en una flota. Forma parte de la arquitectura de microservicios del sistema FleetOps y expone:

- APIs REST para gestión de vehículos, tipos, reservas e historial
- Integración Kafka con **Asignaciones** (solicitar / liberar vehículo)
- Patrón Saga distribuido para asignaciones
- Monitoreo con métricas Prometheus

## Stack Tecnológico

| Componente | Versión/Tecnología |
|------------|---|
| Framework | Spring Boot 3.3.5 |
| Java | JDK 21 |
| Base de Datos | PostgreSQL 14+ |
| ORM | Spring Data JPA / Hibernate 6.x |
| Migraciones | Flyway 10.x |
| Autenticación | JWT (jjwt 0.12.6) |
| Validación | Spring Validation |
| Documentación API | Swagger/OpenAPI 3 (springdoc 2.6.0) |
| Monitoreo | Spring Actuator + Micrometer Prometheus |
| Mensajería | Apache Kafka (Spring Kafka) |
| Resilencia | Resilience4j |
| Build | Maven |

## Estructura del Proyecto

```
vehiculos/
├── src/
│   ├── main/
│   │   ├── java/com/fleetops/vehicles/
│   │   │   ├── VehiclesApplication.java          # Punto de entrada
│   │   │   ├── config/                            # Configuraciones
│   │   │   │   ├── Resilience4jConfig.java
│   │   │   │   └── DevTokenPrinter.java
│   │   │   ├── controllers/                       # Capa REST (Presentación)
│   │   │   │   └── VehicleController.java
│   │   │   ├── services/                          # Lógica de Negocio
│   │   │   │   ├── application/
│   │   │   │   │   ├── VehicleService.java
│   │   │   │   │   ├── VehicleServiceImpl.java
│   │   │   │   │   ├── TipoVehiculoService.java
│   │   │   │   │   ├── TipoVehiculoServiceImpl.java
│   │   │   │   │   ├── SagaService.java
│   │   │   │   │   └── SagaServiceImpl.java
│   │   │   │   └── domain/                        # Lógica de dominio
│   │   │   ├── models/
│   │   │   │   └── entities/                      # Entidades JPA
│   │   │   │       ├── Vehiculo.java
│   │   │   │       ├── TipoVehiculo.java
│   │   │   │       ├── ReservaVehiculo.java
│   │   │   │       ├── SagaVehiculo.java
│   │   │   │       ├── HistorialEstadoVehiculo.java
│   │   │   │       ├── EstadoVehiculo.java (Enum)
│   │   │   │       ├── EstadoReserva.java (Enum)
│   │   │   │       └── EstadoSaga.java (Enum)
│   │   │   ├── repositories/                     # Acceso a datos (JPA)
│   │   │   │   ├── VehicleRepository.java
│   │   │   │   ├── TipoVehiculoRepository.java
│   │   │   │   ├── ReservaRepository.java
│   │   │   │   ├── HistorialEstadoRepository.java
│   │   │   │   └── SagaRepository.java
│   │   │   ├── dto/                              # Objetos de Transferencia de Datos
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/                           # Conversión Entity ↔ DTO
│   │   │   │   ├── DtoMapperVehicle.java
│   │   │   │   ├── DtoMapperTipoVehiculo.java
│   │   │   │   ├── DtoMapperReserva.java
│   │   │   │   ├── DtoMapperHistorial.java
│   │   │   │   └── DtoMapperSaga.java
│   │   │   ├── exception/                        # Manejo de Excepciones
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ReservaConflictException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── security/                         # Configuración de Seguridad
│   │   │   ├── metrics/                          # Métricas Prometheus
│   │   │   │   └── VehicleMetrics.java
│   │   │   └── util/                             # Utilidades
│   │   └── resources/
│   │       ├── application.properties             # Configuración base
│   │       └── db/migration/                      # Scripts de Flyway
│   │           ├── V1__create_initial_schema.sql
│   │           └── V2__insert_test_data.sql
│   └── test/
│       └── java/com/fleetops/vehicles/
│           └── VehiclesApplicationTests.java
├── pom.xml                                        # Dependencias Maven
├── mvnw / mvnw.cmd                               # Maven Wrapper
└── target/                                        # Compilados (generado)
```

## Configuración

### Configuración Base: application.properties

```properties
# Servidor
server.port=8081

# Base de Datos
spring.datasource.url=jdbc:postgresql://localhost:5432/fleetops_vehicles
spring.datasource.username=postgres
spring.datasource.password=root

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=none  # ⚠️ DDL manejado por Flyway
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false       # Evita problemas de lazy-loading
```

**Nota Importante:** 
- ddl-auto=none — Las migraciones de esquema se hacen SOLO con Flyway, no con Hibernate
- Esto asegura control de versiones y evita conflictos

### Prerequisitos del Sistema

Necesitas instalar:

#### Java 21 JDK (No JRE)

IMPORTANTE: Se necesita **JDK (Java Development Kit)**, NO solo JRE (Java Runtime).
- JDK = Compilador + Runtime (lo que necesitas)
- JRE = Solo Runtime (no funciona para compilar)

Descargar Java 21 JDK:

- Windows/macOS/Linux: https://www.oracle.com/java/technologies/downloads/#java21
- Alternativa (Eclipse Adoptium - Gratuito): https://adoptium.net/temurin/releases/?version=21

Verificar instalación:
```bash
java -version       # Debe mostrar "21.x.x" (o similar)
javac -version      # Debe funcionar y mostrar versión del compilador
```

Si obtienes error `javac: comando no encontrado` o similar — No tienes JDK, necesitas instalarlo.

Configurar JAVA_HOME (si es necesario):

Windows (PowerShell como Admin):
```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
```

macOS/Linux:
```bash
export JAVA_HOME=/usr/libexec/java_home -v 21
# O agregar a ~/.bashrc o ~/.zshrc
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
source ~/.zshrc
```

#### PostgreSQL 14+
```bash
psql --version

# Si no está instalado: https://www.postgresql.org/download/
```

#### Maven (incluido en el proyecto con mvnw)
```bash
# No es necesario instalar, el proyecto lo incluye
```

### Configurar Base de Datos

```sql
-- Conectarse a PostgreSQL (como admin)
psql -U postgres

-- Crear la base de datos
CREATE DATABASE fleetops_vehicles;

-- (Opcional) Crear usuario específico
CREATE USER fleetops WITH PASSWORD 'root';
GRANT ALL PRIVILEGES ON DATABASE fleetops_vehicles TO fleetops;
```

---

## Cómo Correr el Proyecto

### Opción 1: Con Maven Wrapper (Recomendado)

```bash
# Desde la raíz del proyecto
cd vehiculos

# Compilar y ejecutar
./mvnw clean install
./mvnw spring-boot:run
```

### Opción 2: Con Maven instalado

```bash
mvn clean install
mvn spring-boot:run
```

### Opción 3: Ejecutar el JAR compilado

```bash
# Después de hacer `mvn clean install`
java -jar target/fleetops-vehicles-0.0.1-SNAPSHOT.jar
```

---

## Verificar que está corriendo

La aplicación estará disponible en:

```
http://localhost:8081
```

### Endpoints útiles:

| Endpoint | Descripción |
|----------|-------------|
| GET http://localhost:8081/vehiculos | Listar vehículos |
| GET http://localhost:8081/vehiculos/{id}/disponibilidad/rango?fechaInicio=&fechaFin= | Disponibilidad por calendario |
| GET http://localhost:8081/vehiculos/disponibles/rango?nombreTipo=&fechaInicio=&fechaFin= | Vehículos asignables en rango |
| GET http://localhost:8081/swagger-ui.html | Documentación Swagger (UI interactiva) |
| GET http://localhost:8081/v3/api-docs | OpenAPI JSON |
| GET http://localhost:8081/actuator | Métricas y salud |
| GET http://localhost:8081/actuator/prometheus | Métricas Prometheus |

---

## Arquitectura y Flujo de Datos

### Patrón de Capas (Layered Architecture)

```
┌─────────────────────────────────────┐
│   Capa REST (Controllers)           │  ← HTTP Requests
│   VehicleController.java            │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Capa de Servicios (Services)      │  ← Lógica de Negocio
│   VehicleService                    │
│   TipoVehiculoService               │
│   SagaService                       │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Capa de Repositorios (DAOs)       │  ← Acceso a Datos
│   VehicleRepository                 │
│   TipoVehiculoRepository            │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│   Base de Datos                     │  ← Persistencia
│   PostgreSQL                        │
└─────────────────────────────────────┘
```

### Flujo Típico de una Solicitud HTTP

```
1. Cliente hace GET /vehiculos
   ↓
2. VehicleController.getVehiculos()
   ↓
3. VehicleServiceImpl.getAllVehiculos()
   ↓
4. VehicleRepository.findAll()
   ↓
5. PostgreSQL retorna datos
   ↓
6. VehicleRepository → Entidades Vehiculo
   ↓
7. Mapper convierte Vehiculo → VehiculoResponseDTO
   ↓
8. Controller retorna ResponseEntity<List<VehiculoResponseDTO>>
   ↓
9. Spring serializa a JSON y retorna al cliente
```

---

## Modelo de Datos (Entidades Principales)

### Tabla: tipos_vehiculo
Catálogo maestro de tipos de vehículos

```sql
id_tipo_vehiculo  BIGSERIAL PRIMARY KEY
nombre_tipo       VARCHAR(100) UNIQUE NOT NULL
descripcion       VARCHAR(255)
capacidad_carga   DOUBLE PRECISION  -- En kilogramos
creado_en         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
actualizado_en    TIMESTAMP
```

### Tabla: vehiculos
Registro de vehículos individuales

```sql
id_vehiculo          UUID PRIMARY KEY
numero_placa         VARCHAR(20) UNIQUE NOT NULL
marca                VARCHAR(100)
modelo               VARCHAR(100)
anio_fabricacion     INTEGER
color                VARCHAR(50)
numero_chasis        VARCHAR(100) UNIQUE
numero_motor         VARCHAR(100) UNIQUE
kilometraje          INTEGER DEFAULT 0
ciudad_operacion     VARCHAR(100)
sede_operacion       VARCHAR(100)
-- ... más campos
```

### Tabla: reservas_vehiculo
Reservas de vehículos

```sql
id_reserva           UUID PRIMARY KEY
id_vehiculo          UUID FOREIGN KEY → vehiculos
id_usuario           UUID
fecha_inicio_reserva TIMESTAMP
fecha_fin_reserva    TIMESTAMP
estado               ENUM(PENDIENTE, CONFIRMADA, CANCELADA)
```

### Tabla: saga_vehiculo
Patrón Saga para transacciones distribuidas

```sql
id_saga              UUID PRIMARY KEY
id_reserva           UUID
estado               ENUM(INICIADO, COMPLETADO, FALLIDO)
-- ... campos para rastrear steps del saga
```

---

## Seguridad

### Autenticación JWT
- Cada solicitud debe incluir un token JWT en el header:
```bash
Authorization: Bearer <token-jwt>
```

### Rutas Protegidas
La mayoría de endpoints requieren autenticación y ciertos permisos.

Ejemplo:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<?> createVehiculo(@Valid @RequestBody CreateVehiculoRequest request) {
    // Solo usuarios con rol ADMIN pueden crear vehículos
}
```

---

## Integración SQS (Incidentes y Mantenimiento)

| Contrato | Documento |
|----------|-----------|
| Incidentes | [docs/contrato-sqs-incidentes.md](docs/contrato-sqs-incidentes.md) |
| Mantenimiento | [docs/contrato-sqs-manteneance.md](docs/contrato-sqs-manteneance.md) |
| **Pruebas locales (LocalStack)** | [docs/localstack-sqs.md](docs/localstack-sqs.md) |

| Cola | Rol de Vehículos |
|------|------------------|
| `queue_vehicles` | **Consume** — incidentes vía SNS fan-out |
| `queue_vehicles_maintenance` | **Consume** — creación/fin de mantenimiento |

**Local:** `docker compose up` incluye LocalStack (`:4566`) + scripts en `scripts/localstack/`.  
**AWS:** `SQS_ENABLED=true` + IAM Role sobre las colas reales.

---

## Integración Kafka (Contrato A+)

Contrato completo: [docs/contrato-kafka.md](docs/contrato-kafka.md)

| Topic | Rol de Vehículos |
|-------|------------------|
| `fleetops.vehiculos.solicitar` | **Consume** — crea reserva `CONFIRMADA` + publica `confirmado` (tx atómica) |
| `fleetops.asignaciones.vehiculo.confirmado` | **Publica** — asignación exitosa |
| `fleetops.asignaciones.vehiculo.fallido` | **Publica** — asignación rechazada |
| `fleetops.vehiculos.liberar` | **Consume** — compensa reserva (tolerante al payload de Asignaciones) |
| `fleetops.asignaciones.completada` | **Consume** — ACK (no confirma reserva; ya está CONFIRMADA) |

Las asignaciones **no** se crean por REST; solo vía Kafka. El job `SagaReconciliationJob` republica `confirmado` o auto-compensa si Asignaciones no envía ACK (`completada`) en el tiempo configurado.

### Disponibilidad por rango (REST)

Los endpoints `GET .../disponibilidad` evalúan solo estado operativo y documentos. Para consultar el **calendario** (reservas CONFIRMADAS):

```
GET /vehiculos/{id}/disponibilidad/rango?fechaInicio=2026-07-10&fechaFin=2026-07-15
GET /vehiculos/placa/{placa}/disponibilidad/rango?fechaInicio=...&fechaFin=...
GET /vehiculos/disponibles/rango?nombreTipo=furgon&fechaInicio=...&fechaFin=...
```

La respuesta incluye `operativo`, `documentosVigentes`, `disponibleEnRango` y `motivo` si no es asignable.

---

## Monitoreo y Métricas

### Spring Actuator
Acceso a información del sistema:

```
http://localhost:8081/actuator
http://localhost:8081/actuator/health       # Salud de la app
http://localhost:8081/actuator/env          # Variables de entorno
http://localhost:8081/actuator/metrics      # Métricas disponibles
```

### Prometheus
Si tienes Prometheus configurado:

```
http://localhost:8081/actuator/prometheus
```

Métricas custom (Prometheus):

| Métrica | Descripción |
|---------|-------------|
| `fleetops_vehiculos_por_estado{estado="..."}` | Conteo de vehículos activos por estado |
| `fleetops_reservas_activas` | Reservas CONFIRMADAS en curso (ahora ∈ [inicio, fin]) |
| `fleetops_reservas_sin_ack_asignaciones` | CONFIRMADAS sin ACK de Asignaciones (`completada`) |

Otras métricas estándar: `jvm_memory_used_bytes`, `http_server_requests_seconds`, etc.

---

## Testing

### Ejecutar Tests
```bash
./mvnw test
```

### Archivo de Tests
- [VehiclesApplicationTests.java](src/test/java/com/fleetops/vehicles/VehiclesApplicationTests.java)

---

## Migraciones de Base de Datos (Flyway)

Las migraciones se encuentran en [db/migration/](src/main/resources/db/migration/)

### Proceso:
1. Al iniciar la aplicación, Flyway busca scripts en `db/migration/`
2. Aplica cualquier script no ejecutado (rastreado en tabla `flyway_schema_history`)
3. Garantiza consistencia de esquema en todos los ambientes

### Crear nueva migración:
```sql
-- Archivo: src/main/resources/db/migration/V3__add_new_column.sql
ALTER TABLE vehiculos ADD COLUMN temperatura_storage INT;
```

---

## Comandos Útiles

```bash
# Compilar sin ejecutar tests
./mvnw clean compile

# Compilar y ejecutar todos los tests
./mvnw clean test

# Compilar y generar JAR (sin ejecutar)
./mvnw clean package -DskipTests

# Ver información del proyecto
./mvnw help:describe

# Limpiar target/
./mvnw clean

# Ejecutar en modo debug (puerto 5005)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"
```

---

## Troubleshooting

### Error: "No compiler is provided in this environment"

Causa: No tienes JDK instalado, solo JRE.

Verificar:
```bash
javac -version
```

Si da error, necesitas instalar JDK 21:

1. Descargar: https://adoptium.net/temurin/releases/?version=21
2. Instalar el JDK (no el JRE)
3. Reiniciar el terminal/IDE
4. Verificar: `javac -version`

Solución Windows:
```powershell
# Verificar qué Java tienes
java -version
Get-Command javac  # Si da error, no tienes JDK

# Descargar e instalar desde: https://adoptium.net/
# Luego configurar JAVA_HOME:
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")
# Reiniciar PowerShell
```

### Error: "Connection refused" a PostgreSQL

Solución:
```bash
# Verifica que PostgreSQL está corriendo
psql -U postgres -h localhost

# Si no está instalado, instalalo según tu SO
# Windows: https://www.postgresql.org/download/windows/
# macOS: brew install postgresql
# Linux: sudo apt install postgresql
```

### Error: "Database does not exist"

Solución:
```sql
CREATE DATABASE fleetops_vehicles;
```

### Error: "ddl-auto = update/create"

Solución:
- Asegúrate que `spring.jpa.hibernate.ddl-auto=none` en `application.properties`
- Las migraciones SIEMPRE van en Flyway, no en Hibernate

### Puerto 8081 en uso

Solución:
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/macOS
lsof -i :8081
kill -9 <PID>
```

---

## Recursos Adicionales

- Spring Boot Docs: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Flyway: https://flywaydb.org/
- JWT: https://tools.ietf.org/html/rfc7519
- Swagger/OpenAPI: https://swagger.io/

---

## Equipo

- Arquitecto: FleetOps Team
- Framework: Spring Boot 3.3.5
- Última actualización: 2026-07-05

---

## Notas Importantes

Buenas prácticas implementadas:
- Separación clara de capas (Controllers → Services → Repositories)
- Uso de DTOs para aislamiento del modelo de datos
- Mappers para conversión Entity ↔ DTO
- Manejo global de excepciones
- Validación con @Valid y Bean Validation
- Documentación con Swagger/OpenAPI
- Migraciones versionadas con Flyway
- Seguridad con JWT
- Métricas y monitoreo con Prometheus

Recordar:
- NUNCA uses `ddl-auto=update` en producción
- SIEMPRE valida los datos de entrada en los DTOs
- Los mappers son cruciales para desacoplamiento
- Las excepciones personalizadas mejoran el debugging

---

## Docker

El proyecto incluye un `Dockerfile` multi-stage (build con `eclipse-temurin:21-jdk-jammy`, runtime con `eclipse-temurin:21-jre-jammy`), optimizado para producción:

- **Stage 1 (builder):** descarga dependencias, compila y empaqueta el jar con Maven Wrapper.
- **Stage 2 (runtime):** imagen final mínima, sin código fuente ni caché de Maven, ejecutando la app con un **usuario no-root** dedicado (`fleetops`).
- Incluye **healthcheck** integrado contra `/actuator/health`.

### Construir y ejecutar solo el contenedor de la app

```bash
docker build -t fleetops/vehicles-service:local .
docker run -p 8081:8081 --env-file .env fleetops/vehicles-service:local
```

> Requiere que exista un `.env` (basado en `.env.example`) y una base de datos PostgreSQL accesible.

---

## Docker Compose

Para levantar el microservicio junto con su base de datos PostgreSQL en un entorno de desarrollo local completo, se usa `docker-compose.yml`, que define:

- **`postgres`**: PostgreSQL 15 (Alpine), con volumen persistente (`postgres-data`) y healthcheck (`pg_isready`).
- **`vehicles-service`**: construido desde el `Dockerfile` local, depende de que `postgres` esté saludable (`depends_on: condition: service_healthy`), conectado a la red dedicada `fleetops-network`.

### Cómo ejecutar el proyecto completo con Docker Compose

```bash
# 1. Copiar el archivo de variables de entorno de ejemplo
cp .env.example .env
# Editar .env con tus valores (usuario/clave de DB, JWT_SECRET_KEY, etc.)

# 2. Levantar todo el stack (DB + API)
docker compose up -d --build

# 3. Verificar que ambos servicios estén healthy
docker compose ps

# 4. Ver logs de la API
docker compose logs -f vehicles-service

# 5. Probar el health check
curl http://localhost:8081/actuator/health

# 6. Apagar el stack (conservando el volumen de datos)
docker compose down

# 7. Apagar y borrar también el volumen de datos (reinicio completo)
docker compose down -v
```

---

## GitHub Actions (CI)

El pipeline de Integración Continua está definido en [.github/workflows/ci.yml](.github/workflows/ci.yml) y se ejecuta automáticamente en cada `push`/`pull_request` contra `main` y `develop`. Pasos actuales:

1. Checkout del repositorio (historial completo).
2. Validación y permisos de ejecución del Maven Wrapper (`chmod +x mvnw`).
3. Configuración de JDK 21 (Temurin).
4. Cache de dependencias Maven.
5. Compilación del proyecto (`./mvnw clean compile`).
6. Ejecución de tests contra un contenedor real de PostgreSQL (service container de GitHub Actions).
7. Generación de reporte de cobertura con JaCoCo.
8. Análisis de SonarCloud — **deshabilitado actualmente** (ver sección [SonarCloud](#sonarcloud) más abajo).
9. Verificación de estilo con Checkstyle.
10. Empaquetado del jar ejecutable.
11. Subida del jar como *artifact* de GitHub Actions (descargable desde la pestaña "Actions" de cada ejecución).

### Cómo validar el CI

- Cualquier `push` o Pull Request contra `main`/`develop` dispara el pipeline automáticamente — revisa la pestaña **Actions** del repositorio en GitHub.
- También se puede disparar manualmente desde GitHub → Actions → "CI - FleetOps Vehicles" → **Run workflow** (`workflow_dispatch`).
- Localmente, puedes reproducir los pasos principales del pipeline con:
  ```bash
  ./mvnw -B clean compile
  ./mvnw -B test
  ./mvnw -B jacoco:report
  ./mvnw checkstyle:check
  ./mvnw -B clean package -DskipTests
  ```

---

## Deploy preparado para AWS EC2

Existe un workflow de Continuous Deployment listo en [.github/workflows/deploy.yml](.github/workflows/deploy.yml), **intencionalmente deshabilitado** (`if: false` en el job `deploy`) porque todavía no existe una instancia EC2 real ni los GitHub Secrets asociados.

El workflow, una vez activado, se dispara automáticamente al finalizar el CI (`workflow_run`) sobre `main`, y hace lo siguiente vía SSH (usando `appleboy/ssh-action`):

1. Se conecta a la instancia EC2 por SSH.
2. Ejecuta `docker compose pull` para traer la imagen más reciente.
3. Ejecuta `docker compose up -d` para desplegar.
4. Verifica el estado con `docker compose ps` y un `curl` contra `/actuator/health`.

### Qué falta para activarlo (fuera de alcance de esta fase)

- Crear la instancia EC2.
- Configurar los GitHub Secrets: `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`.
- Quitar/cambiar la condición `if: false` del job `deploy` en `deploy.yml`.

> Nota: esta fase **no crea infraestructura AWS ni modifica `deploy.yml`** — solo se documenta su estado actual.

---

## SonarCloud

El proyecto tiene SonarCloud **preparado pero deshabilitado a nivel de CI**, de forma intencional:

- La organización **FleetOps-Corp** en SonarCloud usa **"Automatic Analysis"** (vía GitHub App), que analiza el repositorio automáticamente sin necesidad de un paso explícito en el pipeline.
- SonarCloud no permite tener activos simultáneamente "Automatic Analysis" y "CI-based Analysis" para el mismo proyecto — por eso los steps de SonarCloud en `ci.yml` están **comentados (no eliminados)**, con instrucciones claras de cómo reactivarlos si en el futuro la organización cambia a modo CI-based.
- `sonar-project.properties` y el `sonar-maven-plugin` en `pom.xml` siguen presentes en el repo, listos para cuando se decida reactivar el análisis desde CI.

**En resumen:** SonarCloud sigue analizando el código automáticamente en segundo plano; no se pierde cobertura de calidad, solo se evita un conflicto de configuración.

---

## Calidad del proyecto

Herramientas de calidad ya integradas al pipeline de CI:

| Herramienta | Propósito | Estado |
|---|---|---|
| **JaCoCo** | Cobertura de tests | ✅ Activo, genera reporte en cada build |
| **Checkstyle** | Estilo y convenciones de código | ✅ Activo (`failOnViolation=false`, no bloquea el build todavía) |
| **SonarCloud** | Análisis estático de calidad y seguridad | ✅ Activo vía Automatic Analysis (ver sección anterior) |
| **Surefire** | Ejecución de tests unitarios | ✅ Activo |

Para ejecutar estas verificaciones localmente antes de abrir un PR:

```bash
./mvnw clean test              # Tests + JaCoCo
./mvnw checkstyle:check         # Estilo de código
```

---

## Versionamiento

Este proyecto sigue (o seguirá, a medida que se estabilice) **[Semantic Versioning (SemVer)](https://semver.org/lang/es/)**: `MAJOR.MINOR.PATCH`.

- **MAJOR**: cambios incompatibles en la API pública (rutas REST, contratos de request/response).
- **MINOR**: nueva funcionalidad compatible con versiones anteriores.
- **PATCH**: correcciones de bugs compatibles hacia atrás.

### SNAPSHOT vs. versión estable

- Mientras el proyecto está en desarrollo activo, la versión en `pom.xml` se mantiene como `X.Y.Z-SNAPSHOT` (actualmente `0.0.1-SNAPSHOT`). Un `SNAPSHOT` indica una versión **en construcción**, que puede cambiar en cualquier momento y no debe considerarse estable ni desplegarse a producción como definitiva.
- Una **versión estable** (sin sufijo `-SNAPSHOT`, ej. `1.0.0`) representa un punto congelado y probado del código, apto para tag y release.

### Cuándo crear tags y generar Releases

- Se crea un **tag de git** (`vX.Y.Z`) únicamente cuando una versión pasa CI en `main` y el equipo la considera lista para marcar como hito (por ejemplo, fin de un sprint, entrega parcial del curso, o antes de un despliegue real a EC2).
- Un **GitHub Release** se genera a partir de ese tag, incluyendo notas de la versión (idealmente basadas en las entradas correspondientes del [CHANGELOG.md](CHANGELOG.md)).
- Esta fase **no crea tags ni modifica `pom.xml`** — solo documenta el criterio para cuando el equipo decida hacerlo.

---

## Cómo ejecutar localmente usando Docker

Resumen rápido (ver también la sección [Docker Compose](#docker-compose) más arriba):

```bash
# 1. Preparar variables de entorno
cp .env.example .env

# 2. Levantar todo (DB + API) con un solo comando
docker compose up -d --build

# 3. Confirmar que la API responde
curl http://localhost:8081/actuator/health

# 4. Ver documentación interactiva de la API
# http://localhost:8081/swagger-ui.html
```

Esta es la forma recomendada de correr el proyecto sin instalar Java, Maven ni PostgreSQL localmente — solo requiere Docker y Docker Compose.

---

## Seguridad del repositorio (configuración manual en GitHub)

Las siguientes protecciones **no se pueden configurar desde código** — deben habilitarse manualmente en **GitHub → Settings** del repositorio:

- **Branch Protection** para `main`: exigir que el CI pase y al menos una revisión aprobada antes de hacer merge.
- **Secret Scanning**: detecta automáticamente credenciales o tokens commiteados por error.
- **Push Protection**: bloquea pushes que contengan secretos detectados, antes de que lleguen al repositorio remoto.
- **Dependabot Alerts**: notifica sobre dependencias con vulnerabilidades conocidas.
- **Dependabot Security Updates**: genera automáticamente PRs para corregir dependencias vulnerables.

> Esta sección es solo documentación del estado deseado; ningún archivo de código puede activar estas configuraciones, requieren acceso de administrador al repositorio en GitHub.

---

## Variables de entorno

El proyecto usa un archivo `.env` (no versionado, ver `.gitignore`) basado en la plantilla [.env.example](.env.example). Variables principales:

| Variable | Descripción | Ejemplo |
|---|---|---|
| `API_HOST_PORT` | Puerto expuesto en el host para la API | `8081` |
| `DB_HOST` | Host de PostgreSQL | `localhost` (o `postgres` dentro de Docker Compose) |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `fleetops_vehicles` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de la base de datos | — |
| `JWT_SECRET_KEY` | Clave secreta para firmar/validar JWT (debe coincidir con el microservicio de Seguridad) | mínimo 256 bits |
| `MAINTENANCE_SERVICE_URL` | URL del microservicio de Mantenimiento | `http://localhost:8083` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos por CORS | `http://localhost:5173,...` |

### Cómo configurarlas

```bash
cp .env.example .env
# Editar .env con los valores reales de tu entorno
```

> **Nunca** subas el archivo `.env` al repositorio — ya está excluido en `.gitignore`. Solo `.env.example` (con valores placeholder) debe versionarse.

---

## Monitoreo (Prometheus)

Además de las métricas expuestas por Spring Actuator (ver sección [Monitoreo y Métricas](#monitoreo-y-métricas)), el repositorio incluye una configuración de referencia para Prometheus en [monitoring/prometheus.yml](monitoring/prometheus.yml), con tres `scrape_configs`:

| Job | Descripción | Endpoint |
|---|---|---|
| `ec2-node` | Métricas del sistema operativo (node_exporter) | `<EC2_HOST>:19100` |
| `ec2-docker` | Métricas de contenedores (cAdvisor) | `<EC2_HOST>:18080` |
| `api-metrics` | Métricas de la aplicación Spring Boot | `<EC2_HOST>:18000/actuator/prometheus` |

**Importante:** este archivo usa el placeholder `<EC2_HOST>` en lugar de una IP real, para no exponer información de infraestructura en el repositorio (ver `CHANGELOG.md`). Antes de usarlo contra un servidor real, sustituye `<EC2_HOST>` por la IP/hostname correspondiente. Este archivo **no está conectado a ningún pipeline de CI/CD todavía** — es solo una plantilla de referencia para cuando exista una instancia de monitoreo real.
