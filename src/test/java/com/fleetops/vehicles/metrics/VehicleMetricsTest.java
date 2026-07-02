package com.fleetops.vehicles.metrics;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.repositories.VehicleRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - VehicleMetrics")
class VehicleMetricsTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Test
    @DisplayName("Registra gauges por cada estado de vehículo")
    void registraGaugesPorEstado() {
        MeterRegistry registry = new SimpleMeterRegistry();

        new VehicleMetrics(vehicleRepository, registry);

        for (EstadoVehiculo estado : EstadoVehiculo.values()) {
            assertNotNull(registry.find("fleetops_vehiculos_por_estado")
                    .tag("estado", estado.name().toLowerCase())
                    .gauge());
        }
    }

    @Test
    @DisplayName("El valor del gauge consulta el repositorio filtrando por estado activo")
    void gaugeConsultaRepositorioPorEstado() {
        given(vehicleRepository.countByEstadoVehiculoAndActivoTrue(EstadoVehiculo.DISPONIBLE)).willReturn(7L);
        MeterRegistry registry = new SimpleMeterRegistry();

        new VehicleMetrics(vehicleRepository, registry);

        double valor = registry.find("fleetops_vehiculos_por_estado")
                .tag("estado", EstadoVehiculo.DISPONIBLE.name().toLowerCase())
                .gauge()
                .value();

        assertEquals(7.0, valor);
    }
}
