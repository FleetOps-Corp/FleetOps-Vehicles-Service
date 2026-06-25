// Define la "carpeta lógica" donde agrupamos los sobres de respuesta de nuestra API.
package com.fleetops.vehicles.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

// =========================================================================================
// PATRÓN DE DISEÑO DETECTADO: DTO (Data Transfer Object) de Respuesta
// ¿Qué hace? Funciona como un empaque o "sobre" de datos que preparamos para enviar al 
// cliente (Frontend o Microservicio). Su objetivo es mostrar solo lo necesario.
//
// PATRÓN DE DISEÑO: Inmutabilidad (Java Record)
// Al usar 'record', garantizamos que una vez que el servidor llena este sobre, ningún 
// proceso intermedio pueda alterar los datos antes de que lleguen al destino final.
// Ejemplo: Es como una factura impresa; una vez emitida, no se puede cambiar el monto ni el producto.
// =========================================================================================
public record DisponibilidadResponse(
    
    // Devuelve el ID único (UUID) para identificar el vehículo sin revelar datos internos de la BD.
    // Ejemplo: "550e8400-e29b-41d4-a716-446655440000"
    UUID idVehiculo,
    
    // Devuelve el estado actual en formato texto.
    // Ejemplo: "EN_MANTENIMIENTO" (permite al frontend decidir si muestra un icono gris o rojo).
    String estadoVehiculo,
    
    // Bandera lógica para toma de decisiones rápida.
    // Ejemplo: Si el sistema dice 'true', el botón de "Reservar" se activa en la web del cliente.
    Boolean disponible,
    
    // Marca de tiempo exacta del cambio.
    // Ejemplo: "2026-06-19T10:00:00" (sirve para que el usuario sepa qué tan antigua es la información).
    LocalDateTime actualizadoEn
) {}