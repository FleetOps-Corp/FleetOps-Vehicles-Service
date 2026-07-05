package com.fleetops.vehicles.metrics;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Component
public class VehicleMetrics {

    private static final long CACHE_TTL_MS = 10_000;

    private final VehicleRepository vehicleRepository;
    private final ReservaRepository reservaRepository;
    private volatile Map<EstadoVehiculo, Long> countsByEstado = Map.of();
    private volatile long activeReservationsCount = 0;
    private volatile long lastRefreshMs = 0;

    public VehicleMetrics(VehicleRepository vehicleRepository, ReservaRepository reservaRepository,
                          MeterRegistry meterRegistry) {
        this.vehicleRepository = vehicleRepository;
        this.reservaRepository = reservaRepository;
        refreshCounts();

        for (EstadoVehiculo estado : EstadoVehiculo.values()) {
            Gauge.builder("fleetops_vehiculos_por_estado", this, metrics -> metrics.countFor(estado))
                    .tag("estado", estado.name().toLowerCase())
                    .description("Cantidad de vehículos activos por estado")
                    .register(meterRegistry);
        }

        Gauge.builder("fleetops_reservas_activas", this, VehicleMetrics::activeReservations)
                .description("Reservas CONFIRMADAS en curso (fecha actual dentro del rango de la reserva)")
                .register(meterRegistry);
    }

    private double countFor(EstadoVehiculo estado) {
        ensureFresh();
        return countsByEstado.getOrDefault(estado, 0L).doubleValue();
    }

    private double activeReservations() {
        ensureFresh();
        return activeReservationsCount;
    }

    private void ensureFresh() {
        long now = System.currentTimeMillis();
        if (now - lastRefreshMs < CACHE_TTL_MS) {
            return;
        }
        synchronized (this) {
            if (System.currentTimeMillis() - lastRefreshMs < CACHE_TTL_MS) {
                return;
            }
            refreshCounts();
        }
    }

    private void refreshCounts() {
        Map<EstadoVehiculo, Long> fresh = new EnumMap<>(EstadoVehiculo.class);
        for (Object[] row : vehicleRepository.countActiveGroupByEstado()) {
            fresh.put((EstadoVehiculo) row[0], (Long) row[1]);
        }
        countsByEstado = Map.copyOf(fresh);
        activeReservationsCount = reservaRepository.countCurrentlyActiveReservations(LocalDateTime.now());
        lastRefreshMs = System.currentTimeMillis();
    }
}
