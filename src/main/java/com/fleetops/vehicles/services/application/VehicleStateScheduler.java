package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleStateScheduler {

    private final VehicleRepository vehicleRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void auditarVencimientoDocumentosLegales() {
        LocalDate hoy = LocalDate.now();

        List<Vehiculo> candidatosAuditoria = vehicleRepository
                .findAllByActivoTrueAndEstadoVehiculoNot(EstadoVehiculo.FUERA_DE_SERVICIO);

        for (Vehiculo vehiculo : candidatosAuditoria) {
            if (Boolean.TRUE.equals(vehiculo.getActivo())
                    && vehiculo.getEstadoVehiculo() != EstadoVehiculo.FUERA_DE_SERVICIO) {

                String mensajeSoat = "";
                String mensajeRtm = "";

                if (vehiculo.getFechaSoat() == null || vehiculo.getFechaSoat().isBefore(hoy)) {
                    mensajeSoat = "el SOAT ya se encuentra vencido (o no está registrado)";
                } else if (ChronoUnit.DAYS.between(hoy, vehiculo.getFechaSoat()) <= 7) {
                    mensajeSoat = "el SOAT está próximo a vencer en menos de 7 días";
                }

                if (vehiculo.getFechaRtm() == null || vehiculo.getFechaRtm().isBefore(hoy)) {
                    mensajeRtm = "la RTM ya se encuentra vencida (o no está registrada)";
                } else if (ChronoUnit.DAYS.between(hoy, vehiculo.getFechaRtm()) <= 7) {
                    mensajeRtm = "la RTM está próxima a vencer en menos de 7 días";
                }

                if (!mensajeSoat.isEmpty() || !mensajeRtm.isEmpty()) {
                    String mensajeMotivo;
                    if (!mensajeSoat.isEmpty() && !mensajeRtm.isEmpty()) {
                        mensajeMotivo = "Inmovilización por múltiples causas legales: " + mensajeSoat + " y "
                                + mensajeRtm + ".";
                    } else if (!mensajeSoat.isEmpty()) {
                        mensajeMotivo = "Inmovilización por restricción legal: " + mensajeSoat + ".";
                    } else {
                        mensajeMotivo = "Inmovilización por restricción legal: " + mensajeRtm + ".";
                    }

                    EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

                    log.warn("AUTOMATIZACIÓN: Inmovilizando vehículo [{}]. Motivo: {}", vehiculo.getNumeroPlaca(),
                            mensajeMotivo);

                    vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
                    vehiculo.setActualizadoEn(LocalDateTime.now());
                    vehicleRepository.save(vehiculo);

                    historialEstadoRepository.save(HistorialEstadoVehiculo.builder()
                            .vehiculo(vehiculo)
                            .estadoAnterior(estadoAnterior.name())
                            .estadoNuevo(EstadoVehiculo.FUERA_DE_SERVICIO.name())
                            .motivoCambio(mensajeMotivo)
                            .servicioOrigen("fleetops-legal-scheduler")
                            .registradoEn(LocalDateTime.now())
                            .build());
                }
            }
        }
    }
}
