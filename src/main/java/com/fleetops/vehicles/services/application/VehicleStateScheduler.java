package com.fleetops.vehicles.services.application;


import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleStateScheduler {

    private final VehicleRepository vehicleRepository;
    private final ReservaRepository reservaRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    /**
     * PATRÓN: Background State Synchronizer
     * Se ejecuta automáticamente cada 60 segundos (fixedRate = 60000 ms).
     * Revisa el reloj del servidor y ajusta la flota de manera transparente.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sincronizarEstadosPorAgenda() {
        LocalDateTime ahora = LocalDateTime.now();
        List<EstadoReserva> estadosOperativos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

        // 1. Conseguir contratos de reserva vigentes en este segundo
        List<ReservaVehiculo> reservasActivasAhora = reservaRepository
                .findCurrentlyActiveReservations(ahora, estadosOperativos);

        // Mapeamos los IDs de los camiones que obligatoriamente deben estar en estado RESERVADO
        Set<UUID> vehiculosQueDebenEstarReservados = reservasActivasAhora.stream()
                .map(reserva -> reserva.getVehiculo().getIdVehiculo())
                .collect(Collectors.toSet());

        // 2. PROCESO AUTOMÁTICO A: Cambiar a RESERVADO los camiones que ya iniciaron viaje
        for (ReservaVehiculo reserva : reservasActivasAhora) {
            Vehiculo vehiculo = reserva.getVehiculo();
            
            // Regla defensiva: solo alteramos si el camión está marcado activo y sigue en estado DISPONIBLE
            if (Boolean.TRUE.equals(vehiculo.getActivo()) && vehiculo.getEstadoVehiculo() == EstadoVehiculo.DISPONIBLE) {
                log.info("AUTOMATIZACIÓN: El vehículo {} ha entrado en su rango de reserva. Cambiando a RESERVADO.", vehiculo.getNumeroPlaca());

                // Persistir auditoría obligatoria en el Append-Only Log
                historialEstadoRepository.save(HistorialEstadoVehiculo.builder()
                        .vehiculo(vehiculo)
                        .estadoAnterior(EstadoVehiculo.DISPONIBLE.name())
                        .estadoNuevo(EstadoVehiculo.RESERVADO.name())
                        .motivoCambio("Inicio automático de ventana de tiempo de la reserva ID: " + reserva.getIdReserva())
                        .servicioOrigen("fleetops-time-scheduler")
                        .registradoEn(ahora)
                        .build());

                vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
                vehiculo.setActualizadoEn(ahora);
                vehicleRepository.save(vehiculo);
            }
        }

        // 3. PROCESO AUTOMÁTICO B: Liberar a DISPONIBLE los camiones cuya reserva ya expiró
        // Buscamos los camiones que están actualmente como RESERVADO en la BD
        List<Vehiculo> vehiculosReservadosEnBd = vehicleRepository.findAll().stream()
                .filter(v -> Boolean.TRUE.equals(v.getActivo()) && v.getEstadoVehiculo() == EstadoVehiculo.RESERVADO)
                .toList();

        for (Vehiculo vehiculo : vehiculosReservadosEnBd) {
            // Si el camión está RESERVADO pero ya ninguna reserva activa lo reclama en este minuto, se libera
            if (!vehiculosQueDebenEstarReservados.contains(vehiculo.getIdVehiculo())) {
                log.info("AUTOMATIZACIÓN: La ventana de tiempo de reserva para el vehículo {} ha expirado. Volviendo a DISPONIBLE.", vehiculo.getNumeroPlaca());

                // Registrar liberación en la bitácora
                historialEstadoRepository.save(HistorialEstadoVehiculo.builder()
                        .vehiculo(vehiculo)
                        .estadoAnterior(EstadoVehiculo.RESERVADO.name())
                        .estadoNuevo(EstadoVehiculo.DISPONIBLE.name())
                        .motivoCambio("Finalización automática del bloque de tiempo programado en agenda")
                        .servicioOrigen("fleetops-time-scheduler")
                        .registradoEn(ahora)
                        .build());

                vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
                vehiculo.setActualizadoEn(ahora);
                vehicleRepository.save(vehiculo);
            }
        }
    }
}