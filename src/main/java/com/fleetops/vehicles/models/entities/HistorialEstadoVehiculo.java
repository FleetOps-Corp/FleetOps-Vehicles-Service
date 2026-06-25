// Define la "carpeta" lógica del proyecto donde residen las entidades del modelo.
package com.fleetops.vehicles.models.entities;

// Importaciones de JPA para persistencia, Lombok para boilerplate y utilidades de tiempo.
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

// @Entity: Marca la clase para que Hibernate gestione su ciclo de vida y persistencia.
@Entity
// @Table: Define el nombre real en la BD. La convención 'snake_case' (historial_estados_vehiculo)
// es crucial para evitar problemas de case-sensitivity en diferentes sistemas operativos (Linux vs Windows).
@Table(name = "historial_estados_vehiculo")

// Lombok: Reduce drásticamente el código repetitivo, manteniendo el foco en el negocio.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// =========================================================================================
// PATRÓN DE DISEÑO APLICADO: Append-Only Log (Bitácora de Solo Anexar).
// ¿Qué hace? Garantiza que el historial sea inmutable. Una vez guardado, el evento ya sucedió 
// y no puede ser alterado. Esto es vital para la auditoría técnica y financiera.
// =========================================================================================
public class HistorialEstadoVehiculo {

    // =========================================================================================
    // idHistorial: El identificador único de este evento específico.
    // Usamos UUID en lugar de un número incremental (1, 2, 3...) para evitar que se puedan 
    // adivinar cuántas operaciones ha hecho el sistema o causar conflictos en migraciones.
    // =========================================================================================
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_historial", updatable = false, nullable = false)
    private UUID idHistorial;

    // =========================================================================================
    // vehiculo: La relación con el activo físico.
    // fetch = FetchType.LAZY: REGLA DE RENDIMIENTO. No traemos el objeto 'Vehiculo' completo 
    // a menos que realmente lo necesitemos. Esto evita cargar toda la flota en memoria 
    // cada vez que solo queremos leer un historial.
    // =========================================================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    // Estado antes del cambio. Es nullable porque el primer registro de la vida de un vehículo 
    // no tiene un "antes".
    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    // Estado después del cambio. Es obligatorio.
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private String estadoNuevo;

    // motivoCambio: REGLA DE NEGOCIO (Contexto).
    // Permite al administrador o al sistema explicar el "por qué". 
    // Ej: "Recolección de emergencia", "Mantenimiento preventivo".
    @Column(name = "motivo_cambio", length = 255)
    private String motivoCambio;

    // servicioOrigen: Rastreo de Microservicios.
    // Identifica qué parte del sistema fue el "culpable" o responsable del cambio.
    // Ej: "servicio-asignaciones", "servicio-taller".
    @Column(name = "servicio_origen", length = 100)
    private String servicioOrigen;

    // idCorrelacion: El "hilo conductor".
    // Permite seguir una transacción que salta por varios microservicios. 
    // Si la reserva falló, usas este ID para rastrear todo el flujo en los logs.
    @Column(name = "id_correlacion", length = 100)
    private String idCorrelacion;

    // registradoEn: Timestamp inmutable.
    // updatable = false: REGLA DE SEGURIDAD. Garantiza que la fecha no pueda ser alterada 
    // una vez guardada, protegiendo la integridad del registro de auditoría.
    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn;
}