// Define la "carpeta" lógica del proyecto donde residen las entidades del modelo.
package com.fleetops.vehicles.models.entities;

// Importa las herramientas de persistencia JPA para mapear Java a tablas.
import jakarta.persistence.*;
// Importa Lombok para reducir código repetitivo (boilerplate).
import lombok.*;
// Importa herramientas para manejar fechas y tiempos.
import java.time.LocalDate;
import java.time.LocalDateTime;
// Importa la herramienta para generar IDs universales.
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: Aggregate Root (Raíz de Agregado)
// ¿Qué hace? Actúa como la entidad central. Cualquier operación en el sistema (Reserva, 
// Historial, Saga) se valida contra esta clase. Si el vehículo no existe o está inactivo, 
// ninguna de las otras acciones puede proceder.
// =========================================================================================

// @Entity: Marca la clase para que Hibernate gestione su ciclo de vida y persistencia.
@Entity
// @Table: Define el nombre real en la BD. 'vehiculos' es el nombre de la tabla central.
@Table(name = "vehiculos")
// Lombok: Genera los métodos para acceder y modificar las variables privadas automáticamente.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    // =====================================================================================
    // idVehiculo: Llave Primaria.
    // @Id: Marca el campo como la PK.
    // @GeneratedValue: Indica que la BD genera el UUID automáticamente.
    // updatable = false: REGLA DE SEGURIDAD. Una vez creado, el ID nunca debe cambiar.
    // =====================================================================================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_vehiculo", updatable = false, nullable = false)
    private UUID idVehiculo;

    // numeroPlaca: Identificador legal del activo.
    // unique = true: REGLA DE NEGOCIO. Dos vehículos no pueden tener la misma placa.
    @Column(name = "numero_placa", unique = true, nullable = false, length = 10)
    private String numeroPlaca;

    // marca, modelo, anioFabricacion: Datos descriptivos básicos del activo físico.
    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(name = "anio_fabricacion", nullable = false)
    private Integer anioFabricacion;

    // color: Dato estético de registro.
    @Column(length = 30)
    private String color;

    // =====================================================================================
    // numeroChasis y numeroMotor: Seriales de fábrica.
    // unique = true: REGLA DE NEGOCIO. Estos números son únicos en el mundo; el sistema 
    // bloquea cualquier intento de duplicarlos.
    // =====================================================================================
    @Column(name = "numero_chasis", unique = true, length = 50)
    private String numeroChasis;

    @Column(name = "numero_motor", unique = true, length = 50)
    private String numeroMotor;

    // kilometraje: Dato clave para mantenimiento.
    @Column(nullable = false)
    private Integer kilometraje;

    // ciudad y sede: Ubicación logística.
    @Column(name = "ciudad_operacion", length = 100)
    private String ciudadOperacion;

    @Column(name = "sede_operacion", length = 100)
    private String sedeOperacion;

    // estadoVehiculo: Máquina de estados (Enum). Define si el vehículo está DISPONIBLE, MANTENIMIENTO, etc.
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_vehiculo", nullable = false, length = 20)
    private EstadoVehiculo estadoVehiculo;

    // Fechas legales: SOAT, RTM, Último Mantenimiento.
    // LocalDate (sin hora) es suficiente porque estos documentos vencen al final del día.
    @Column(name = "fecha_soat")
    private LocalDate fechaSoat;

    @Column(name = "fecha_rtm")
    private LocalDate fechaRtm;

    @Column(name = "fecha_ultimo_mant")
    private LocalDate fechaUltimoMant;

    // =====================================================================================
    // activo: PATRÓN Soft Delete (Borrado Lógico).
    // REGLA DE NEGOCIO: Si es 'false', el vehículo está "oculto" para los usuarios, pero 
    // sus datos persisten en la BD para auditorías fiscales.
    // =====================================================================================
    @Column(nullable = false)
    private Boolean activo;

    // =====================================================================================
    // version: PATRÓN Optimistic Locking.
    // REGLA DE NEGOCIO: Evita sobrescrituras. Si dos despachadores editan el mismo vehículo
    // a la vez, el sistema detectará el conflicto gracias a este número de versión.
    // =====================================================================================
    @Version
    @Column(name = "version")
    private Long version;

    // =====================================================================================
    // tipoVehiculo: Relación Muchos-a-Uno.
    // fetch = FetchType.LAZY: REGLA DE RENDIMIENTO. No traemos los datos de la categoría 
    // a menos que los solicitemos, ahorrando ancho de banda y memoria.
    // =====================================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoVehiculo;

    // creadoEn / actualizadoEn: Auditoría.
    // updatable = false en creadoEn protege la fecha de origen del registro.
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

} 