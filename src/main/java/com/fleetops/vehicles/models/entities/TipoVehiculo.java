// Define la "carpeta" lógica del proyecto donde residen las entidades del modelo.
package com.fleetops.vehicles.models.entities;

// Importa las herramientas de persistencia JPA para mapear Java a tablas.
import jakarta.persistence.*;
// Importa Lombok para reducir código repetitivo (boilerplate).
import lombok.*;
// Importa herramientas para manejar fechas y tiempos.
import java.time.LocalDateTime;

// =========================================================================================
// PATRÓN DE DISEÑO: Catalog / Normalization (Catálogo o Maestro de Datos).
// ¿Qué hace? Separa los textos y reglas de negocio repetitivas ("Furgón", "Camioneta") 
// en una tabla aparte para evitar duplicidad y garantizar integridad.
// =========================================================================================

// @Entity: Indica a Hibernate que esta clase debe ser una tabla en la base de datos.
@Entity
// @Table: Define el nombre de la tabla en el esquema de base de datos.
@Table(name = "tipos_vehiculo")
// @Getter / @Setter: Genera automáticamente métodos para acceder a datos privados.
@Getter
@Setter
// @NoArgsConstructor / @AllArgsConstructor: Crea constructores para instanciar fácilmente.
@NoArgsConstructor
@AllArgsConstructor
// @Builder: Permite instanciar objetos con una sintaxis fluida: TipoVehiculo.builder().nombreTipo("Furgón").build();
@Builder
public class TipoVehiculo {

    // =========================================================================================
    // idTipoVehiculo: Identificador.
    // @Id: Marca el campo como la Llave Primaria.
    // @GeneratedValue(strategy = IDENTITY): En catálogos maestros es eficiente usar un número 
    // secuencial (1, 2, 3...) porque facilita índices y búsquedas rápidas en comparaciones.
    // =========================================================================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_vehiculo")
    private Long idTipoVehiculo;

    // =========================================================================================
    // nombreTipo: Nombre comercial de la categoría.
    // unique = true: REGLA DE NEGOCIO. Impide que existan dos categorías llamadas igual.
    // nullable = false: El nombre es obligatorio; un tipo sin nombre no tiene sentido.
    // =========================================================================================
    @Column(name = "nombre_tipo", unique = true, nullable = false, length = 100)
    private String nombreTipo;

    // descripcion: Texto descriptivo de la categoría.
    @Column(length = 255)
    private String descripcion;

    // capacidadCarga: Dato crítico de operación. Define cuánto peso puede llevar este tipo de vehículo.
    @Column(name = "capacidad_carga")
    private Double capacidadCarga;

    // creadoEn: Fecha de auditoría (registra cuándo se añadió este tipo al sistema).
    // updatable = false: Protege la fecha original de creación contra alteraciones.
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    // actualizadoEn: Fecha que registra la última modificación realizada a este catálogo.
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

} // Fin de la clase TipoVehiculo.