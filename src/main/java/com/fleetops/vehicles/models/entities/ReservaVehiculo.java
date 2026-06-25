// Define la "carpeta" lógica del proyecto donde se almacenan las entidades de base de datos.
package com.fleetops.vehicles.models.entities;

// Importa herramientas de persistencia para mapear Java a la Base de Datos.
import jakarta.persistence.*;
// Importa Lombok para reducir código repetitivo (boilerplate).
import lombok.*;
// Importa herramientas para manejar fechas y tiempos.
import java.time.LocalDateTime;
// Importa la herramienta para generar IDs universales.
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO: Entity (ORM)
// =========================================================================================

// @Entity: Marca esta clase para que Hibernate sepa que debe crear una tabla equivalente en la BD.
@Entity
// @Table: Define el nombre real de la tabla. 'reservas_vehiculo' sigue la convención snake_case.
@Table(name = "reservas_vehiculo")
// @Getter / @Setter: Lombok genera automáticamente los métodos para acceder a datos privados.
@Getter
@Setter
// @NoArgsConstructor / @AllArgsConstructor: Crea constructores para instanciar la clase fácilmente.
@NoArgsConstructor
@AllArgsConstructor
// @Builder: Permite crear objetos complejos paso a paso: ReservaVehiculo.builder().solicitadoPor("Juan").build();
@Builder
public class ReservaVehiculo {

    // =========================================================================================
    // idReserva: Identificador interno del sistema.
    // @Id: Marca este campo como la Llave Primaria (PK).
    // @GeneratedValue: Indica que el valor lo genera automáticamente la BD (usando UUID).
    // =========================================================================================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_reserva", updatable = false, nullable = false)
    private UUID idReserva;

    // =========================================================================================
    // vehiculo: La relación con el activo físico.
    // @ManyToOne: Muchas reservas pueden apuntar a un mismo vehículo (a lo largo del tiempo).
    // fetch = FetchType.LAZY: REGLA DE RENDIMIENTO. Solo trae los datos del vehículo si los pedimos.
    // =========================================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    // =========================================================================================
    // sagaVehiculo: Relación con la "Carpeta" del proceso (Orquestador de Saga).
    // Permite rastrear qué transacción distribuida creó esta reserva.
    // =========================================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_saga")
    private SagaVehiculo sagaVehiculo;

    // =========================================================================================
    // idAsignacionExt: El ID que conoce el sistema externo.
    // REGLA DE INTEGRACIÓN: Sirve para que otros servicios reconozcan este registro en su propio sistema.
    // =========================================================================================
    @Column(name = "id_asignacion_ext", nullable = false)
    private UUID idAsignacionExt;

    // =========================================================================================
    // estadoReserva: Máquina de estados de la reserva.
    // @Builder.Default: Si no se especifica, empieza en PENDIENTE.
    // @Enumerated(EnumType.STRING): Guarda la palabra ("PENDIENTE") en BD, no un número (0,1).
    // =========================================================================================
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_reserva", nullable = false, length = 20)
    private EstadoReserva estadoReserva = EstadoReserva.PENDIENTE;

    // =========================================================================================
    // claveIdempotencia: Patrón de Idempotencia.
    // REGLA DE NEGOCIO: Evita duplicados. Si el cliente pulsa "Reservar" 2 veces, este campo 
    // unique=true hará que la BD rechace el segundo intento, evitando cobrar dos veces.
    // =========================================================================================
    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 100)
    private String claveIdempotencia;

    // solicitadoPor: Nombre o identificador del usuario que realizó la reserva.
    @Column(name = "solicitado_por", nullable = false, length = 100)
    private String solicitadoPor;

    // fechaInicio: Momento exacto en que empieza la disponibilidad del vehículo.
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    // fechaFin: Momento exacto en que se libera el vehículo (es nullable para reservas abiertas).
    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;
    
    // =========================================================================================
    // version: PATRÓN Optimistic Locking.
    // ¿Qué hace? Hibernate incrementa este número automáticamente en cada actualización.
    // Si dos hilos intentan actualizar el mismo registro a la vez, el que tenga una versión 
    // vieja fallará, evitando que un usuario sobrescriba los cambios del otro ("Lost Update").
    // =========================================================================================
    @Version
    @Column(name = "version")
    private Long version;

    // creadoEn: Fecha de registro. updatable = false garantiza que esta fecha nunca cambie.
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    // actualizadoEn: Fecha de última modificación. Se actualiza cada vez que el estado cambia.
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}