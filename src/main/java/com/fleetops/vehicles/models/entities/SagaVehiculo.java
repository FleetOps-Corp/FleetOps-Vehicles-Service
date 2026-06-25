// Define la "carpeta" lógica del proyecto donde residen las entidades del modelo.
package com.fleetops.vehicles.models.entities;

// Importaciones de JPA, Lombok y utilidades de tiempo/ID.
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

// @Entity: Marca la clase como una tabla en la base de datos gestionada por Hibernate.
@Entity
// @Table: Nombre de la tabla. 'sagas_vehiculo' ayuda a mantener el orden en PostgreSQL.
@Table(name = "sagas_vehiculo")
// @Getter / @Setter: Genera automáticamente métodos de acceso para todas las variables privadas.
@Getter
@Setter
// @NoArgsConstructor / @AllArgsConstructor: Crea los constructores necesarios para que Spring pueda instanciar la clase.
@NoArgsConstructor
@AllArgsConstructor
// @Builder: Permite construir la saga mediante una sintaxis fluida: SagaVehiculo.builder().payload("...").build();
@Builder
public class SagaVehiculo {

    // =========================================================================================
    // idSaga: Llave Primaria. Es el ID único que identifica este trámite distribuido.
    // =========================================================================================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_saga", updatable = false, nullable = false)
    private UUID idSaga;

    // =========================================================================================
    // vehiculo: Relación con el activo físico. 
    // LAZY: Optimizamos la carga; no traemos el vehículo a memoria a menos que lo necesitemos explícitamente.
    // =========================================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    // =========================================================================================
    // tipoOperacion: Define qué es lo que se está intentando hacer (Ej: "RESERVA", "VENTA").
    // Es vital para que el orquestador sepa qué flujo de lógica ejecutar.
    // =========================================================================================
    @Column(name = "tipo_operacion", nullable = false, length = 50)
    private String tipoOperacion;

    // =========================================================================================
    // estadoSaga: Máquina de estados (Enum). Controla el ciclo de vida de la transacción.
    // @Builder.Default: Inicializa la saga siempre como INICIADA.
    // =========================================================================================
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_saga", nullable = false, length = 20)
    private EstadoSaga estadoSaga = EstadoSaga.INICIADA;

    // =========================================================================================
    // claveIdempotencia: Patrón de Idempotencia.
    // unique = true: Garantiza que si el sistema recibe dos veces la misma orden, 
    // la base de datos bloquee el segundo intento automáticamente.
    // =========================================================================================
    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 100)
    private String claveIdempotencia;

    // =========================================================================================
    // intentos: Contador de reintentos. 
    // Si la red falla al llamar a otro servicio, este número rastrea cuántas veces hemos intentado.
    // =========================================================================================
    @Builder.Default
    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    // =========================================================================================
    // payload: Snapshot del trámite.
    // columnDefinition = "TEXT": Permite almacenar documentos JSON pesados.
    // REGLA DE NEGOCIO: Es la "caja negra" del trámite. Si todo falla, abrimos este payload 
    // para ver exactamente qué datos intentó enviar el cliente.
    // =========================================================================================
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    // =========================================================================================
    // ultimoError: Registro técnico del problema.
    // Si algo sale mal, aquí guardamos el mensaje de error para diagnóstico inmediato.
    // =========================================================================================
    @Column(name = "ultimo_error", columnDefinition = "TEXT")
    private String ultimoError;

    // =========================================================================================
    // compensadoPor: Auditoría de Rollback.
    // Si una saga falla y se ejecutan acciones de reversión, este campo anota quién o qué 
    // proceso ejecutó la compensación.
    // =========================================================================================
    @Column(name = "compensado_por", length = 100)
    private String compensadoPor;

    // =========================================================================================
    // version: PATRÓN Optimistic Locking.
    // Evita la sobrescritura perdida si dos hilos intentan actualizar la misma saga al mismo tiempo.
    // =========================================================================================
    @Version
    @Column(name = "version")
    private Long version;

    // =========================================================================================
    // creadoEn / actualizadoEn: Fechas estándar de auditoría.
    // updatable = false en 'creadoEn' protege la fecha original de creación ante cualquier edición.
    // =========================================================================================
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}