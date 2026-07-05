package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.repositories.*;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.IdempotencyValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaServiceImpl implements SagaService {

    private final DtoMapperReserva dtoMapperReserva;
    private final DtoMapperSaga dtoMapperSaga;
    private final SagaRepository sagaRepository;
    private final ReservaRepository reservaRepository;
    private final VehicleRepository vehicleRepository;
    private final IdempotencyValidator idempotencyValidator;
    private final AvailabilityPolicy availabilityPolicy;

    @Override
    @Transactional
    public ReservaResponse compensarPorReservaId(UUID reservaId, String motivo) {
        log.info("Iniciando anulación para la Reserva ID: {}", reservaId);

        ReservaVehiculo reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("ReservaVehiculo", "id", reservaId));

        if (reserva.getSagaVehiculo() != null) {
            compensarSaga(reserva.getSagaVehiculo().getIdSaga(), motivo);
        } else {
            reserva.setEstadoReserva(EstadoReserva.CANCELADA);
            reserva.setActualizadoEn(LocalDateTime.now());
            reservaRepository.save(reserva);
        }

        return toReservaResponse(reserva);
    }

    @Override
    @Transactional
    public List<ReservaResponse> cancelarReservasPorPlaca(String placa, String motivo) {
        log.info("Iniciando cancelación masiva de reservas para la placa: {}", placa);

        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        List<EstadoReserva> estadosCriticos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
        List<ReservaVehiculo> reservas = reservaRepository.findByVehiculo_IdVehiculoAndEstadoReservaIn(
                vehiculo.getIdVehiculo(), estadosCriticos);

        LocalDateTime ahora = LocalDateTime.now();
        java.util.List<ReservaVehiculo> reservasCanceladas = new java.util.ArrayList<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (ReservaVehiculo reserva : reservas) {
            boolean esViajeEnCurso = reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA
                    && !ahora.isBefore(reserva.getFechaInicio())
                    && !ahora.isAfter(reserva.getFechaFin());

            String motivoSaga = motivo;

            if (esViajeEnCurso) {
                log.info("Corte de Viaje Activo: La reserva {} será truncada y cancelada masivamente.",
                        reserva.getIdReserva());
                reserva.setFechaFin(ahora);
                motivoSaga = motivo + " | [AVISO AUTOMÁTICO: El vehículo estaba operando. Se cortó la reserva activa hasta la fecha "
                        + ahora.format(formatter) + "]. por un caso de fuerza mayor.";
            }

            reserva.setEstadoReserva(EstadoReserva.CANCELADA);
            reserva.setActualizadoEn(ahora);
            reservaRepository.save(reserva);

            if (reserva.getSagaVehiculo() != null) {
                compensarSaga(reserva.getSagaVehiculo().getIdSaga(), motivoSaga);
            }

            reservasCanceladas.add(reserva);
        }

        return reservasCanceladas.stream()
                .map(this::toReservaResponse)
                .toList();
    }

    @Override
    @Transactional
    public boolean compensarSaga(UUID sagaId, String motivo) {
        log.info("Iniciando reversión de datos (anulación) para el Trámite ID: {}. Motivo: {}", sagaId, motivo);

        SagaVehiculo saga = sagaRepository.findById(sagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Trámite de Reserva", "id", sagaId));

        if (saga.getEstadoSaga() == EstadoSaga.COMPENSADA) {
            log.info("El proceso de reserva {} ya estaba anulado previamente.", sagaId);
            return true;
        }

        if (saga.getEstadoSaga() == EstadoSaga.COMPLETADA) {
            long diasTranscurridos = ChronoUnit.DAYS.between(saga.getActualizadoEn(), LocalDateTime.now());
            if (diasTranscurridos > 15) {
                throw new BusinessException(
                        "No se puede revertir un proceso de reserva completado hace más de 15 días.");
            }
        }

        ReservaVehiculo reserva = reservaRepository.findBySagaVehiculo_IdSaga(sagaId).orElse(null);

        if (reserva != null) {
            reserva.setEstadoReserva(EstadoReserva.CANCELADA);
            reserva.setActualizadoEn(LocalDateTime.now());
            reservaRepository.save(reserva);
            log.info("Reserva ID {} amarrada al proceso fue cancelada exitosamente.", reserva.getIdReserva());
        }

        String usuarioActual = "SISTEMA_AUTOMATICO";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            usuarioActual = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        saga.setEstadoSaga(EstadoSaga.COMPENSADA);
        saga.setCompensadoPor(usuarioActual);
        saga.setUltimoError(motivo);
        saga.setActualizadoEn(LocalDateTime.now());
        sagaRepository.save(saga);

        log.info("Anulación completada exitosamente para el Trámite ID: {}", sagaId);
        return true;
    }

    private ReservaResponse toReservaResponse(ReservaVehiculo reserva) {
        Vehiculo v = reserva.getVehiculo();

        return new ReservaResponse(
                reserva.getIdReserva(),
                v != null ? v.getIdVehiculo() : null,
                reserva.getEstadoReserva() != null ? reserva.getEstadoReserva().name() : null,
                reserva.getIdAsignacionExt() != null ? reserva.getIdAsignacionExt().toString() : null,
                reserva.getSolicitadoPor(),
                reserva.getFechaInicio(),
                reserva.getFechaFin(),
                reserva.getClaveIdempotencia(),
                reserva.getSagaVehiculo() != null ? reserva.getSagaVehiculo().getIdSaga() : null,
                v != null ? v.getNumeroPlaca() : null,
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getNombreTipo() : null,
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getDescripcion() : null,
                v != null ? v.getKilometraje() : null,
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getCapacidadCarga() : null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findAllReservas(Pageable pageable) {
        return reservaRepository.findAllByOrderByCreadoEnDesc(pageable).map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasPendientes(Pageable pageable) {
        return reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.PENDIENTE, pageable)
                .map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasConfirmadas(Pageable pageable) {
        return reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.CONFIRMADA, pageable)
                .map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasFallidas(Pageable pageable) {
        return reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.FALLIDA, pageable)
                .map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasCanceladas(Pageable pageable) {
        return reservaRepository.findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.CANCELADA, pageable)
                .map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaResponse> findReservaById(UUID idReserva) {
        return reservaRepository.findById(idReserva).map(this::toReservaResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasByPlaca(String placa, Pageable pageable) {
        return reservaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(placa, pageable)
                .map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasByPlacaAndEstado(String placa, EstadoReserva estado, Pageable pageable) {
        return reservaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoReservaOrderByCreadoEnDesc(placa, estado, pageable)
                .map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findAllSagas(Pageable pageable) {
        return sagaRepository.findAllByOrderByCreadoEnDesc(pageable).map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasIniciadas(Pageable pageable) {
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.INICIADA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasEnProgreso(Pageable pageable) {
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.EN_PROGRESO, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasCompletadas(Pageable pageable) {
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.COMPLETADA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasFallidas(Pageable pageable) {
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.FALLIDA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasCompensadas(Pageable pageable) {
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.COMPENSADA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasByPlaca(String placa, Pageable pageable) {
        return sagaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(placa, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasByPlacaAndEstado(String placa, EstadoSaga estado, Pageable pageable) {
        return sagaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc(placa, estado, pageable)
                .map(dtoMapperSaga::toDto);
    }

    private Optional<Vehiculo> buscarVehiculoAsignable(String tipoVehiculo, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);

        List<Vehiculo> candidatos = vehicleRepository
                .findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(tipoVehiculo);

        return candidatos.stream()
                .filter(v -> availabilityPolicy.isAssignable(v, inicio, fin))
                .findFirst();
    }

    @Override
    @Transactional
    public VehicleAssignmentResult procesarSolicitudAsignacion(VehicleRequestEvent event) {
        String claveIdempotencia = event.getIdSaga().toString();
        idempotencyValidator.validateNotDuplicate(claveIdempotencia);

        Optional<Vehiculo> vehiculoAsignable = buscarVehiculoAsignable(
                event.getTipoVehiculo(),
                event.getFechaInicio(),
                event.getFechaFin());

        if (vehiculoAsignable.isEmpty()) {
            return VehicleAssignmentResult.builder()
                    .success(false)
                    .idAsignacion(event.getIdAsignacion())
                    .motivo("Sin vehículos disponibles del tipo " + event.getTipoVehiculo())
                    .build();
        }

        Vehiculo vehiculo = vehiculoAsignable.get();
        LocalDateTime fechaInicio = event.getFechaInicio().atStartOfDay();
        LocalDateTime fechaFin = event.getFechaFin().atTime(23, 59, 59);

        SagaVehiculo saga = new SagaVehiculo();
        saga.setVehiculo(vehiculo);
        saga.setTipoOperacion("RESERVA_VEHICULO");
        saga.setEstadoSaga(EstadoSaga.COMPLETADA);
        saga.setClaveIdempotencia(claveIdempotencia);
        saga.setPayload(event.toString());
        saga.setCreadoEn(LocalDateTime.now());
        saga.setActualizadoEn(LocalDateTime.now());
        saga = sagaRepository.save(saga);

        ReservaVehiculo reserva = new ReservaVehiculo();
        reserva.setVehiculo(vehiculo);
        reserva.setSagaVehiculo(saga);
        reserva.setIdAsignacionExt(event.getIdAsignacion());
        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setClaveIdempotencia(claveIdempotencia);
        reserva.setSolicitadoPor("Asignaciones-Service");
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setCreadoEn(LocalDateTime.now());
        reserva.setActualizadoEn(LocalDateTime.now());
        reservaRepository.save(reserva);

        log.info("Asignación confirmada vía Kafka: vehículo {} para asignación {}",
                vehiculo.getNumeroPlaca(), event.getIdAsignacion());

        return VehicleAssignmentResult.builder()
                .success(true)
                .idAsignacion(event.getIdAsignacion())
                .idVehiculo(vehiculo.getIdVehiculo())
                .build();
    }
}
