package com.fleetops.vehicles.service;

import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
import com.fleetops.vehicles.models.entities.HistorialEstadoVehiculo;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.models.entities.Vehiculo;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.services.application.VehicleStateScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - VehicleStateScheduler")
class VehicleStateSchedulerTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HistorialEstadoRepository historialEstadoRepository;

    private VehicleStateScheduler scheduler;
    private Vehiculo vehiculo;
    private UUID vehiculoId;

    @BeforeEach
    void setUp() {
        scheduler = new VehicleStateScheduler(vehicleRepository, reservaRepository, historialEstadoRepository);
        vehiculoId = UUID.randomUUID();
        vehiculo = Vehiculo.builder()
                .idVehiculo(vehiculoId)
                .numeroPlaca("TWA101")
                .estadoVehiculo(EstadoVehiculo.DISPONIBLE)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Cambia vehículo DISPONIBLE a RESERVADO cuando inicia ventana de reserva")
    void sincronizarPasaAReservado() {
        ReservaVehiculo reserva = ReservaVehiculo.builder()
                .idReserva(UUID.randomUUID())
                .vehiculo(vehiculo)
                .estadoReserva(EstadoReserva.CONFIRMADA)
                .fechaInicio(LocalDateTime.now().minusHours(1))
                .fechaFin(LocalDateTime.now().plusHours(2))
                .build();

        given(reservaRepository.findCurrentlyActiveReservations(any(), any()))
                .willReturn(List.of(reserva));
        given(vehicleRepository.findAll()).willReturn(List.of());

        scheduler.sincronizarEstadosPorAgenda();

        assertEquals(EstadoVehiculo.RESERVADO, vehiculo.getEstadoVehiculo());
        verify(vehicleRepository).save(vehiculo);
        verify(historialEstadoRepository).save(any(HistorialEstadoVehiculo.class));
    }

    @Test
    @DisplayName("Libera vehículo RESERVADO cuando ya no hay reserva activa")
    void sincronizarLiberaVehiculo() {
        vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);

        given(reservaRepository.findCurrentlyActiveReservations(any(), any()))
                .willReturn(List.of());
        given(vehicleRepository.findAll()).willReturn(List.of(vehiculo));
        when(historialEstadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        scheduler.sincronizarEstadosPorAgenda();

        ArgumentCaptor<Vehiculo> captor = ArgumentCaptor.forClass(Vehiculo.class);
        verify(vehicleRepository).save(captor.capture());
        assertEquals(EstadoVehiculo.DISPONIBLE, captor.getValue().getEstadoVehiculo());
    }
}
