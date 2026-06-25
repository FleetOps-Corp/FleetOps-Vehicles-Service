# FleetOps - Microservicio de Gestión de Vehículos

## Descripción General

**FleetOps Vehicles** es un microservicio backend construido con **Spring Boot 3.3.5** que gestiona operaciones de vehículos en una flota. Forma parte de la arquitectura de microservicios del sistema FleetOps y proporciona APIs REST para:

- Gestión de vehículos
- Gestión de tipos de vehículos
- Reservas de vehículos
- Historial de estados
- Patrones Saga distribuidos
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

Métricas disponibles:
- jvm_memory_used_bytes - Memoria JVM
- http_requests_total - Total de requests HTTP
- vehicle_operations_total - Operaciones de vehículos

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
- Última actualización: 2026-06-24

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
