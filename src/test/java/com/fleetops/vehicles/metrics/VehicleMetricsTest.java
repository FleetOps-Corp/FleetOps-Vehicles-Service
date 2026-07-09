package com.fleetops.vehicles.metrics;

import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class VehicleMetricsTest {

    @Test
    void registraGaugesYUsaCache() {
        VehicleRepository repository = mock(VehicleRepository.class);
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        List<Object[]> initial = List.<Object[]>of(
                new Object[]{EstadoVehiculo.DISPONIBLE, 5L},
                new Object[]{EstadoVehiculo.EN_MANTENIMIENTO, 2L});
        when(repository.countActiveGroupByEstado()).thenReturn(initial);
        when(reservaRepository.countCurrentlyActiveReservations(any())).thenReturn(3L);
        when(reservaRepository.countConfirmadasSinAck()).thenReturn(1L);

        MeterRegistry registry = new SimpleMeterRegistry();
        VehicleMetrics metrics = new VehicleMetrics(repository, reservaRepository, registry);

        Double disponible = registry.get("fleetops_vehiculos_por_estado")
                .tag("estado", "disponible")
                .gauge()
                .value();
        assertEquals(5.0, disponible);

        Double mantenimiento = registry.get("fleetops_vehiculos_por_estado")
                .tag("estado", "en_mantenimiento")
                .gauge()
                .value();
        assertEquals(2.0, mantenimiento);

        assertEquals(3.0, registry.get("fleetops_reservas_activas").gauge().value());

        // Segunda lectura dentro del TTL no debe volver a consultar la BD.
        registry.get("fleetops_vehiculos_por_estado").tag("estado", "disponible").gauge().value();
        verify(repository, times(1)).countActiveGroupByEstado();
        verify(reservaRepository, times(1)).countCurrentlyActiveReservations(any());

        // Estado sin filas en el GROUP BY usa default 0.
        assertEquals(0.0, registry.get("fleetops_vehiculos_por_estado")
                .tag("estado", "fuera_de_servicio").gauge().value());

        // Expirar caché fuerza un nuevo refresh.
        ReflectionTestUtils.setField(metrics, "lastRefreshMs", 0L);
        List<Object[]> refreshed = List.<Object[]>of(new Object[]{EstadoVehiculo.DISPONIBLE, 9L});
        when(repository.countActiveGroupByEstado()).thenReturn(refreshed);
        when(reservaRepository.countCurrentlyActiveReservations(any())).thenReturn(5L);
        when(reservaRepository.countConfirmadasSinAck()).thenReturn(2L);
        assertEquals(9.0, registry.get("fleetops_vehiculos_por_estado")
                .tag("estado", "disponible").gauge().value());
        assertEquals(5.0, registry.get("fleetops_reservas_activas").gauge().value());
        verify(repository, times(2)).countActiveGroupByEstado();
    }

    @Test
    void ensureFreshEsThreadSafeAlExpirarCache() throws Exception {
        VehicleRepository repository = mock(VehicleRepository.class);
        ReservaRepository reservaRepository = mock(ReservaRepository.class);
        List<Object[]> rows = List.<Object[]>of(new Object[]{EstadoVehiculo.DISPONIBLE, 1L});
        when(repository.countActiveGroupByEstado()).thenReturn(rows);
        when(reservaRepository.countCurrentlyActiveReservations(any())).thenReturn(0L);
        when(reservaRepository.countConfirmadasSinAck()).thenReturn(0L);

        MeterRegistry registry = new SimpleMeterRegistry();
        VehicleMetrics metrics = new VehicleMetrics(repository, reservaRepository, registry);
        ReflectionTestUtils.setField(metrics, "lastRefreshMs", 0L);

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    registry.get("fleetops_vehiculos_por_estado")
                            .tag("estado", "disponible")
                            .gauge()
                            .value();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        pool.shutdownNow();

        // Constructor (1) + al menos un refresh por expiración; el synchronized evita ráfagas.
        verify(repository, atLeast(2)).countActiveGroupByEstado();
        verify(repository, atMost(threads + 1)).countActiveGroupByEstado();
    }
}
