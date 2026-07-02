# Guía de tests — FleetOps Vehicles


## 1. Cómo correr los tests

### Requisitos

| Requisito | Unitarios | Integración |
|-----------|-----------|-------------|
| **JDK 21** | Sí | Sí |
| **Maven Wrapper** (`./mvnw`) | Sí | Sí |
| **PostgreSQL** en `localhost:5432` | No | Sí |

Credenciales por defecto (igual que `application.properties` y el CI de GitHub Actions):

- Base de datos: `fleetops_vehicles`
- Usuario: `postgres`
- Contraseña: `root`

Puedes sobreescribirlas con variables de entorno:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fleetops_vehicles
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=root
```

### Levantar PostgreSQL con Docker

```bash
# Desde la raíz del proyecto
docker compose up postgres -d
```

### Comandos principales

```bash
# Dar permisos al wrapper (solo la primera vez, si hace falta)
chmod +x ./mvnw

# Todos los tests
./mvnw test

# Tests + reporte HTML/XML de cobertura JaCoCo (recomendado: verify ya lo hace)
./mvnw test jacoco:report

# Tests + empaquetado + validación de cobertura mínima (80% líneas) + reporte JaCoCo
./mvnw verify

# Un solo archivo de test
./mvnw test -Dtest=SagaServiceTest

# Un solo método
./mvnw test -Dtest=SagaServiceTest#iniciarReservaExitoso

# Solo tests de integración (requiere PostgreSQL)
./mvnw test -Dtest="*IntegrationTest,VehiclesApplicationTests"
```

### Dónde ver resultados

| Artefacto | Ruta |
|-----------|------|
| Reporte Surefire (por clase) | `target/surefire-reports/` |
| Cobertura HTML JaCoCo | `target/site/jacoco/index.html` |
| Cobertura XML (SonarCloud) | `target/site/jacoco/jacoco.xml` |

### Desde el IDE 

1. Abre la carpeta `src/test/java`.
2. Clic derecho en una clase o paquete → **Run Tests**.
3. Para integración: asegúrate de que Postgres esté corriendo antes.

---

## 2. Estrategia de testing

```
┌─────────────────────────────────────────────────────────────┐
│  Tests de integración (@SpringBootTest + PostgreSQL real)   │
│  HTTP real, Flyway, JWT, seguridad, datos sembrados V2      │
└─────────────────────────────────────────────────────────────┘
                              ▲
┌─────────────────────────────────────────────────────────────┐
│  Tests de controller (MockMvc standalone + mocks servicios) │
│  Verifican rutas, códigos HTTP y JSON sin levantar BD       │
└─────────────────────────────────────────────────────────────┘
                              ▲
┌─────────────────────────────────────────────────────────────┐
│  Tests unitarios (JUnit 5 + Mockito)                        │
│  Servicios, dominio, seguridad, mappers, excepciones        │
└─────────────────────────────────────────────────────────────┘
```

### Tecnologías

- **JUnit 5** — framework de tests
- **Mockito** — mocks de repositorios y servicios
- **Spring Boot Test** — contexto completo en integración
- **MockMvc** — peticiones HTTP simuladas en controllers
- **JaCoCo** — cobertura de código (mínimo **80% líneas** en `verify`)
- **PostgreSQL** — BD real en integración (sin Testcontainers en runtime actual)

### Perfil de test

Archivo: `src/test/resources/application-test.properties`

- Perfil activo: `test`
- Flyway habilitado (mismas migraciones que producción)
- Puerto aleatorio (`server.port=0`)
- JWT de prueba con clave fija para generar tokens en tests

---

## 3. Resumen numérico

| Métrica | Valor aproximado |
|---------|------------------|
| **Archivos de test** | 28 clases Java |
| **Tests unitarios + controller** | ~132 (siempre se ejecutan) |
| **Tests de integración** | ~22 adicionales (requieren PostgreSQL) |
| **Cobertura de líneas** | ~94% |
| **Cobertura de instrucciones** | ~89% |
| **Umbral mínimo en CI** | 80% líneas (`jacoco:check` en `verify`) |

> Si PostgreSQL **no** está disponible, las clases que extienden `BaseIntegrationTest` se **omiten** (`Assumptions`) y solo corren los ~132 tests unitarios. El umbral del 80% sigue cumpliéndose con la suite unitaria.

---

## 4. Estructura de carpetas

```
src/test/java/com/fleetops/vehicles/
├── VehiclesApplicationTests.java      # Smoke: contexto Spring arranca
├── config/
│   └── DevTokenPrinterTest.java
├── controller/
│   ├── VehicleControllerTest.java
│   └── VehicleControllerExtendedTest.java
├── exception/
│   ├── GlobalExceptionHandlerTest.java
│   └── ExceptionAndErrorResponseTest.java
├── integration/
│   ├── BaseIntegrationTest.java       # Base: JWT, RestTemplate, Postgres
│   ├── DatabaseAvailability.java      # Comprueba si hay BD
│   ├── VehicleIntegrationTest.java
│   ├── TipoVehiculoIntegrationTest.java
│   ├── ReservaIntegrationTest.java
│   └── SecurityIntegrationTest.java
├── mapper/
│   └── DtoMapperTest.java
├── metrics/
│   └── VehicleMetricsTest.java
├── security/
│   ├── JwtValidationFilterTest.java
│   ├── JwtAuthenticationEntryPointTest.java
│   ├── TokenJwtConfigTest.java
│   └── SpringSecurityConfigTest.java
├── service/
│   ├── SagaServiceTest.java
│   ├── VehicleServiceTest.java
│   ├── VehicleServiceCrudTest.java
│   ├── TipoVehiculoServiceTest.java
│   ├── AvailabilityPolicyTest.java
│   ├── StateTransitionValidatorTest.java
│   ├── IdempotencyValidatorTest.java
│   ├── DateRangeValidatorTest.java
│   └── VehicleStateSchedulerTest.java
└── util/
    └── JwtTokenGeneratorTest.java
```

---

## 5. Tests unitarios — Servicios de aplicación

### `SagaServiceTest` (25 tests)

Prueba el orquestador de **reservas y sagas** (`SagaServiceImpl`) con Mockito (repositorios y validadores simulados).

| Área | Casos cubiertos |
|------|-----------------|
| **Iniciar reserva** | Éxito, vehículo no existe (404), idempotencia duplicada, vehículo no DISPONIBLE, solapamiento de fechas, SOAT vencido, por placa, solapamiento en `procesarCreacionReserva` |
| **Confirmar** | Por ID, saga en estado inválido, confirmación masiva por placa, sin pendientes |
| **Compensar (rollback)** | Saga exitosa, ya compensada (idempotente), saga antigua (>15 días), por ID de reserva, reserva sin saga |
| **Actualizar fechas** | Éxito, fechas inválidas, estado no modificable |
| **Consultas** | Por ID, listados paginados por estado (PENDIENTE, CONFIRMADA, FALLIDA, CANCELADA), por placa, sagas por estado y placa |

### `VehicleServiceTest` (11 tests)

Consultas y cambio de estado básico:

- Buscar por placa e ID (éxito y 404)
- Disponibilidad (DISPONIBLE vs EN_MANTENIMIENTO)
- `changeState`: transición válida, estado inválido, transición FSM prohibida, actualización de fecha de mantenimiento, vehículo inexistente

### `VehicleServiceCrudTest` (22 tests)

CRUD y operaciones extendidas de `VehicleServiceImpl`:

- Crear (éxito, placa duplicada, estado inválido)
- Actualizar, soft delete, delete por placa
- Reactivar por ID y por placa
- Listados: todos, disponibles, reservados, mantenimiento, fuera de servicio, inactivos, por nombre de tipo
- Historial global y por vehículo/placa
- `updateByPlaca`, `updateEstadoByPlaca`, `getDisponibilidadByPlaca`

### `TipoVehiculoServiceTest` (11 tests)

Catálogo de tipos de vehículo:

- CRUD completo con reglas de negocio (nombre duplicado, tipo con vehículos activos, 404)

### `VehicleStateSchedulerTest` (2 tests)

Scheduler que mueve vehículos **DISPONIBLE ↔ RESERVADO** según la agenda:

- Pasa a RESERVADO cuando inicia ventana de reserva
- Libera a DISPONIBLE cuando expira la reserva

---

## 6. Tests unitarios — Dominio y políticas

### `AvailabilityPolicyTest` (6 tests)

Reglas de disponibilidad operativa (estado, SOAT, RTM, activo).

### `StateTransitionValidatorTest` (6 tests)

Máquina de estados finitos (FSM) de vehículos:

- Transiciones permitidas y prohibidas (ej. EN_MANTENIMIENTO → RESERVADO)

### `IdempotencyValidatorTest` (4 tests)

Clave de idempotencia duplicada en reservas/sagas.

### `DateRangeValidatorTest` (4 tests)

Validación `@ValidDateRange`: fin posterior al inicio, fechas nulas delegadas.

---

## 7. Tests unitarios — Seguridad

### `JwtValidationFilterTest` (4 tests)

Filtro que valida el Bearer token en cada petición:

- Sin header → continúa sin autenticar
- Token inválido → HTTP 401
- Token válido → `SecurityContext` poblado
- Header sin prefijo `Bearer `

### `JwtAuthenticationEntryPointTest` (1 test)

Respuesta JSON 401 cuando un endpoint protegido no tiene credenciales.

### `TokenJwtConfigTest` (1 test)

Inicialización lazy de la `SecretKey` HMAC.

### `SpringSecurityConfigTest` (1 test)

Bean `corsConfigurationSource` (orígenes, métodos).

### `JwtTokenGeneratorTest` (4 tests)

Generación de tokens para ADMIN, OPERADOR y USUARIO_AUTORIZADO; verificación de claims JWT.

---

## 8. Tests unitarios — API, mappers y errores

### `VehicleControllerTest` (6 tests)

MockMvc sobre endpoints representativos:

- GET placa, disponibilidad
- POST reserva por placa
- GET/POST confirmar reserva

### `VehicleControllerExtendedTest` (4 tests agrupados)

Cobertura amplia del `VehicleController` en bloques:

1. **CRUD vehículos** — listados, crear, actualizar, borrar, reactivar
2. **Estado e historial** — PATCH estado, historial por ID/placa/global
3. **Tipos de vehículo** — CRUD del catálogo
4. **Reservas y sagas** — todos los listados, compensar, confirmar por placa, sagas por estado

### `DtoMapperTest` (5 tests)

Mapeo entidad → DTO: `DtoMapperReserva`, `DtoMapperSaga`, `DtoMapperVehicle`, `DtoMapperTipoVehiculo`.

### `GlobalExceptionHandlerTest` (7 tests)

Manejador global: 404, 409, 422, 400 validación, 500, conflicto de reserva.

### `ExceptionAndErrorResponseTest` (6 tests)

Excepciones de dominio y getters de `ErrorResponse`.

### `DevTokenPrinterTest` (1 test)

Impresión de tokens de desarrollo al arranque (`@Profile("!prod")`).

### `VehicleMetricsTest` (1 test)

Registro de gauges Micrometer por estado de vehículo.

---

## 9. Tests de integración

Todos extienden `BaseIntegrationTest`:

- Levantan **Spring Boot completo** en puerto aleatorio
- Usan **PostgreSQL real** (Flyway aplica `V1`, `V2`… con datos semilla como placa `TWA101`)
- Autenticación con **JWT real** generado por `JwtTokenGenerator`

### `VehiclesApplicationTests` (1 test)

Smoke test: el contexto de Spring arranca sin errores.

### `VehicleIntegrationTest` (7 tests)

- `GET /actuator/health` público
- `GET /vehiculos/placa/TWA101` con token
- Placa inexistente → 404
- Disponibilidad por placa
- Listado de disponibles
- Sin token → 401
- Token inválido → 401

### `TipoVehiculoIntegrationTest` (6 tests)

CRUD HTTP del catálogo de tipos (GET semilla, 404, POST/PUT/DELETE con token ADMIN).

### `ReservaIntegrationTest` (4 tests)

Flujo saga/reserva:

- Crear reserva por placa → 201
- Flujo completo: crear → confirmar → consultar
- Listar pendientes
- `idAsignacionExt` inválido → 500

### `SecurityIntegrationTest` (4 tests)

- Health público
- `/vehiculos` sin token → 401
- `/vehiculos` con JWT válido → 200
- Disponibilidad por placa pública (sin auth)

---

## 10. Cobertura y SonarCloud

### JaCoCo en `pom.xml`

```xml
<jacoco.minimum.coverage>0.80</jacoco.minimum.coverage>
```

- `test` → genera reporte
- `verify` → `jacoco:check` falla si líneas < 80%

### SonarCloud

El XML que consume Sonar está en:

```
target/site/jacoco/jacoco.xml
```

Propiedad Maven: `sonar.coverage.jacoco.xmlReportPaths`

Flujo recomendado antes de analizar en Sonar:

```bash
./mvnw clean verify
# verify ya ejecuta test + jacoco:report + jacoco:check (80%)
# Luego el paso sonar:sonar con SONAR_TOKEN
```

> **Importante:** `jacoco:report` **no ejecuta tests**. Si lo lanzas solo (o tras `clean` sin `test`), Maven imprime `Skipping JaCoCo execution due to missing execution data file` y **no genera** `target/site/jacoco/`. Los tests deben correr **con Maven** (`./mvnw test` o `verify`), no solo desde el IDE.

---

## 11. CI (GitHub Actions)

El workflow `.github/workflows/ci.yml` ya incluye:

1. Service container **PostgreSQL 15** en `localhost:5432`
2. `./mvnw test` (corre unitarios **e** integración)
3. Reporte JaCoCo

En CI los tests de integración **sí** se ejecutan porque Postgres está disponible.

---

## 12. Datos útiles para probar manualmente

Semilla Flyway (`V2`):

| Dato | Valor |
|------|-------|
| Placa ejemplo | `TWA101` |
| Tipo vehículo id | `1` — "Camion Carga Pesada" |
| Estado inicial | `DISPONIBLE` |

Reservas en tests de integración:

- `idAsignacionExt` debe ser **UUID** válido
- Fechas formato: `YYYY-MM-DDTHH:mm:ss`
- `claveIdempotencia` única por petición

Tokens en integración (generados en runtime):

```java
adminToken()    // ROLE_ADMIN
operadorToken() // ROLE_OPERADOR
usuarioToken()  // ROLE_USUARIO_AUTORIZADO
```

---

## 13. Troubleshooting

| Problema | Solución |
|----------|----------|
| `./mvnw: Permission denied` | `chmod +x ./mvnw` |
| Integración omitida (0 tests en `*IntegrationTest`) | Levantar Postgres: `docker compose up postgres -d` |
| `Connection refused` en 5432 | Verificar que el contenedor esté healthy |
| `jacoco:report` no genera `target/site/jacoco/` | Ejecutar primero `./mvnw test` o `./mvnw verify`. No uses `clean jacoco:report` sin `test`. Si corriste tests solo en el IDE, vuelve a correrlos con Maven |
| `Skipping JaCoCo execution due to missing execution data file` | Falta `target/jacoco.exec` — el agente JaCoCo solo se activa en `./mvnw test` / `verify` |
| `verify` falla por cobertura | Revisar `target/site/jacoco/index.html` — clases en rojo |
| Tests de controller con Page → 500 en MockMvc | Usar `PageImpl` con `PageRequest.of(0, 10)` (ya aplicado en `VehicleControllerExtendedTest`) |


*Última actualización: suite con ~132 tests unitarios + ~22 de integración, umbral JaCoCo 80% líneas.*
