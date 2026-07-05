# Optimizaciones de rendimiento — FleetOps Vehicles Service

Documento de las mejoras aplicadas al microservicio de vehículos para reducir consultas constantes a la base de datos, N+1 en listados y trabajo redundante en la API REST.

---

## Resumen

| Ronda | Enfoque principal | Impacto estimado (app idle / lecturas) |
|-------|-------------------|----------------------------------------|
| **1** | Schedulers, métricas, logging SQL/JWT | De ~27+ consultas/min a ~3–4/min en idle |
| **2** | N+1 en listados, índices, paginación, saves/validaciones | Menos queries por request de listado y menos round-trips en escrituras |

---

## Ronda 1 — Consultas constantes y ruido de fondo

### Problema

El servicio ejecutaba jobs programados muy frecuentes, hacía `findAll()` de toda la flota, generaba N+1 en reservas activas, y Prometheus/logging amplificaban la carga aunque no hubiera tráfico HTTP.

### Cambios

#### 1. `VehicleStateScheduler.java`

| Job | Antes | Después (ronda 1) | Refactor dominio (2026) |
|-----|-------|-------------------|-------------------------|
| `sincronizarEstadosPorAgenda` | Cada 60 s + `findAll()` | Cada 60 s + filtro por `RESERVADO` | **Eliminado** — la ocupación vive en `reservas_vehiculo` |
| `cancelarReservasExpiradas` | Cada **30 s** | Cada **2 min** | **Eliminado** — asignaciones vía Kafka crean reserva `CONFIRMADA` directa |
| `auditarVencimientoDocumentosLegales` | Cada **60 s** + `findAll()` | **1 vez al día** a las 06:00 | Sin cambio |

La regla de negocio del timeout de reservas PENDIENTE ya no aplica: las asignaciones se confirman en un solo evento Kafka.

#### 2. `ReservaRepository.java`

- `findCurrentlyActiveReservations` usa **`JOIN FETCH r.vehiculo`** para evitar N+1 al sincronizar estados por agenda.

#### 3. `VehicleRepository.java`

Métodos nuevos orientados a jobs y métricas:

- `findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo)` (sin paginación, para scheduler)
- `findAllByActivoTrueAndEstadoVehiculoNot(EstadoVehiculo)` (auditoría legal)
- `countActiveGroupByEstado()` (`GROUP BY` en una sola query)

#### 4. `VehicleMetrics.java`

- Antes: un `COUNT` por cada estado en cada scrape de Prometheus (~5 queries × frecuencia de scrape).
- Después: **una sola consulta `GROUP BY`** con **caché de 10 s** compartida entre todos los gauges.

#### 5. `application.properties` (logging / SQL)

- `spring.jpa.show-sql` y `format_sql` controlados por **`SHOW_SQL`** (por defecto `false`).
- Logging general en **INFO**; detalle de JWT en **DEBUG**.

#### 6. `JwtValidationFilter.java`

- Logs de header/token/usuario movidos de **INFO → DEBUG** (menos I/O bajo polling o muchas peticiones).

### Archivos tocados (ronda 1)

- `services/application/VehicleStateScheduler.java`
- `repositories/ReservaRepository.java`
- `repositories/VehicleRepository.java`
- `metrics/VehicleMetrics.java`
- `resources/application.properties`
- `security/JwtValidationFilter.java`

---

## Ronda 2 — API REST (listados, índices, escrituras)

### Problema

Con la API en uso real, el costo principal pasaba a ser:

1. N+1 al mapear listados paginados (`tipoVehiculo`, `vehiculo`, `saga`).
2. Falta de índices en consultas frecuentes de reservas/sagas/historial.
3. Páginas sin tope (`?size=100000`).
4. Validaciones y `save` duplicados en reservas/sagas.
5. `saveAndFlush` y pre-checks `existsBy` innecesarios en vehículos.

### Cambios

#### 1. N+1 en listados — `@EntityGraph`

| Recurso | Relaciones cargadas en la misma query |
|---------|----------------------------------------|
| Vehículos (listados y detalle) | `tipoVehiculo` |
| Reservas (listados y `findById`) | `vehiculo`, `vehiculo.tipoVehiculo`, `sagaVehiculo` |
| Sagas (listados y `findById`) | `vehiculo` |
| Historial (listados) | `vehiculo`, `vehiculo.tipoVehiculo` |

Método auxiliar: `VehicleRepository.findDetailedById(UUID)` para respuestas que necesitan el tipo sin lazy load.

#### 2. Índices — Flyway `V3__performance_indexes.sql`

```sql
idx_reserva_estado_creado          -- listados y timeout de pendientes
idx_reserva_vehiculo_fechas        -- solapamiento y agenda (parcial PENDIENTE/CONFIRMADA)
idx_saga_estado_creado             -- listados de sagas
idx_historial_vehiculo_fecha       -- historial por vehículo
idx_vehiculos_activo_estado        -- filtros de flota
uq_reservas_id_asignacion_ext      -- unicidad de correlación con Asignaciones
```

#### 3. Paginación acotada (`application.properties`)

```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=50
```

#### 4. Reservas / sagas sin trabajo duplicado (`SagaServiceImpl`)

- Eliminadas validaciones repetidas en `procesarCreacionReserva` (SOAT/RTM, solapamiento y disponibilidad ya se validan en `validarYProcesarReserva`).
- Saga se crea directamente en **`EN_PROGRESO`** (un solo `save` en lugar de INICIADA + UPDATE).
- Unicidad de `idAsignacionExt` delegada a la BD + `GlobalExceptionHandler` (HTTP 409).

#### 5. Cambio de estado (`VehicleServiceImpl.changeState`)

- `saveAndFlush` reemplazado por un único **`save`** al final de la transacción.
- Carga con `findDetailedById` para el DTO de respuesta.

#### 6. Unicidad de vehículos vía BD

- Eliminados los tres `existsBy` previos en create/update (placa, chasis, motor).
- Placa, chasis y motor se normalizan a **mayúsculas** antes de persistir.
- `GlobalExceptionHandler` mapea el constraint de PostgreSQL a mensajes claros (placa / chasis / motor / idempotencia / asignación externa).

#### 7. Logging de consultas

- Mensajes del tipo `Consultando...` pasan de **INFO → DEBUG** en servicios de vehículos, reservas y tipos.
- Las mutaciones relevantes siguen en **INFO**.

#### 8. Pool HikariCP

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
```

#### 9. Código muerto / ruido de arranque

- Eliminado **Resilience4j** (no había cliente HTTP real hacia Mantenimiento): dependencia en `pom.xml`, `Resilience4jConfig.java` y propiedades asociadas.
- `DevTokenPrinter` limitado a profiles `dev`, `local` o `default`.

### Archivos tocados (ronda 2)

- `repositories/VehicleRepository.java`
- `repositories/ReservaRepository.java`
- `repositories/SagaRepository.java`
- `repositories/HistorialEstadoRepository.java`
- `services/application/VehicleServiceImpl.java`
- `services/application/SagaServiceImpl.java`
- `services/application/TipoVehiculoServiceImpl.java`
- `exception/GlobalExceptionHandler.java`
- `resources/db/migration/V3__performance_indexes.sql`
- `resources/application.properties`
- `pom.xml`
- `config/DevTokenPrinter.java`
- Eliminado: `config/Resilience4jConfig.java`

---

## Qué no se cambió (a propósito)

| Tema | Motivo |
|------|--------|
| JWT / Spring Security (`permitAll` + filtro opcional) | Se mantiene para pruebas; otro microservicio gestionará la autenticación |
| Unificar endpoints por query param (`GET /reservas?estado=...`) | Cambiaría el contrato REST y rompería clientes actuales |
| Migración a Kafka / eventos | Fuera del alcance de estas optimizaciones REST |

---

## Cómo verificar

1. Reiniciar el servicio para que Flyway aplique **`V3__performance_indexes.sql`**.
2. Si la BD ya tiene `id_asignacion_ext` duplicados, la migración del índice único fallará: hay que limpiar duplicados antes.
3. Probar listados con `?size=100` (debe caparse a **50**).
4. Crear vehículo con placa/chasis/motor duplicado: debe responder **409** con mensaje específico.
5. Con `SHOW_SQL=true` en `.env`, revisar que los listados no disparen una query por fila de relación.

---

## Referencia rápida de configuración

| Variable / propiedad | Efecto |
|----------------------|--------|
| `SHOW_SQL=true` | Activa log de SQL (solo desarrollo) |
| `spring.data.web.pageable.max-page-size` | Tope de página (50) |
| `spring.datasource.hikari.maximum-pool-size` | Conexiones máximas al pool (10) |
| Profile `prod` | No imprime tokens de desarrollo |

---

*Última actualización: optimizaciones de schedulers/métricas (ronda 1) y API REST (ronda 2).*
