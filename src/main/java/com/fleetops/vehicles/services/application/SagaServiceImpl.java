package com.fleetops.vehicles.services.application;

import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleRequestEvent;
import com.fleetops.vehicles.infrastructure.messaging.dto.VehicleReleaseEvent;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
import com.fleetops.vehicles.dto.response.VehicleAssignmentResult;
import com.fleetops.vehicles.dto.response.VehicleReleaseResult;
import com.fleetops.vehicles.repositories.*;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
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

    private Optional<VehicleAssignmentResult> resolverRespuestaIdempotente(
            VehicleRequestEvent event, String claveIdempotencia) {

        Optional<ReservaVehiculo> reservaExistente = reservaRepository.findByClaveIdempotencia(claveIdempotencia);
        if (reservaExistente.isEmpty() && event.getIdAsignacion() != null) {
            reservaExistente = reservaRepository.findByIdAsignacionExt(event.getIdAsignacion());
        }

        if (reservaExistente.isEmpty()) {
            return Optional.empty();
        }

        ReservaVehiculo reserva = reservaExistente.get();
        UUID idAsignacion = event.getIdAsignacion() != null ? event.getIdAsignacion() : reserva.getIdAsignacionExt();

        if (reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA) {
            log.info("Reintento idempotente: asignación {} ya confirmada con vehículo {}",
                    idAsignacion, reserva.getVehiculo().getIdVehiculo());
            return Optional.of(VehicleAssignmentResult.builder()
                    .success(true)
                    .idAsignacion(idAsignacion)
                    .idVehiculo(reserva.getVehiculo().getIdVehiculo())
                    .idempotentReplay(true)
                    .build());
        }

        String motivo = switch (reserva.getEstadoReserva()) {
            case CANCELADA -> "La asignación fue cancelada previamente";
            case FALLIDA -> "La asignación falló previamente";
            case PENDIENTE -> "La asignación tiene una reserva pendiente legacy sin confirmar";
            case CONFIRMADA -> "La asignación ya está confirmada";
        };

        log.warn("Reintento idempotente rechazado para asignación {}: {}", idAsignacion, motivo);
        return Optional.of(VehicleAssignmentResult.builder()
                .success(false)
                .idAsignacion(idAsignacion)
                .motivo(motivo)
                .idempotentReplay(true)
                .build());
    }

    private Optional<Vehiculo> asignarConLock(
            List<Vehiculo> candidatos, LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        List<Vehiculo> ordenados = candidatos.stream()
                .sorted(Comparator.comparing(Vehiculo::getNumeroPlaca, String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (Vehiculo candidato : ordenados) {
            if (!availabilityPolicy.isAssignable(candidato, fechaInicio, fechaFin)) {
                continue;
            }

            Optional<Vehiculo> bloqueado = vehicleRepository.findByIdForUpdate(candidato.getIdVehiculo());
            if (bloqueado.isEmpty()) {
                continue;
            }

            Vehiculo vehiculo = bloqueado.get();
            if (!availabilityPolicy.isAssignable(vehiculo, fechaInicio, fechaFin)) {
                log.debug("Vehículo {} dejó de ser asignable tras el lock (concurrencia)", vehiculo.getNumeroPlaca());
                continue;
            }

            return Optional.of(vehiculo);
        }

        return Optional.empty();
    }

    private VehicleAssignmentResult crearReservaConfirmada(
            Vehiculo vehiculo,
            VehicleRequestEvent event,
            String claveIdempotencia,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

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

        try {
            reservaRepository.save(reserva);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Colisión al insertar reserva para asignación {}: reintentando idempotencia",
                    event.getIdAsignacion());
            return resolverRespuestaIdempotente(event, claveIdempotencia)
                    .orElse(VehicleAssignmentResult.builder()
                            .success(false)
                            .idAsignacion(event.getIdAsignacion())
                            .motivo("Conflicto de concurrencia al confirmar la asignación")
                            .build());
        }

        log.info("Asignación confirmada vía Kafka: vehículo {} para asignación {}",
                vehiculo.getNumeroPlaca(), event.getIdAsignacion());

        return VehicleAssignmentResult.builder()
                .success(true)
                .idAsignacion(event.getIdAsignacion())
                .idVehiculo(vehiculo.getIdVehiculo())
                .build();
    }

    @Override
    @Transactional
    public VehicleAssignmentResult procesarSolicitudAsignacion(VehicleRequestEvent event) {
        if (event.getIdSaga() == null || event.getIdAsignacion() == null) {
            return VehicleAssignmentResult.builder()
                    .success(false)
                    .idAsignacion(event != null ? event.getIdAsignacion() : null)
                    .motivo("Evento incompleto: idSaga e idAsignacion son obligatorios")
                    .build();
        }

        if (event.getTipoVehiculo() == null || event.getTipoVehiculo().isBlank()) {
            return VehicleAssignmentResult.builder()
                    .success(false)
                    .idAsignacion(event.getIdAsignacion())
                    .motivo("tipoVehiculo es obligatorio")
                    .build();
        }

        if (event.getFechaInicio() == null || event.getFechaFin() == null
                || !event.getFechaFin().isAfter(event.getFechaInicio())) {
            return VehicleAssignmentResult.builder()
                    .success(false)
                    .idAsignacion(event.getIdAsignacion())
                    .motivo("Rango de fechas inválido: fechaFin debe ser posterior a fechaInicio")
                    .build();
        }

        String claveIdempotencia = event.getIdSaga().toString();

        Optional<VehicleAssignmentResult> idempotente = resolverRespuestaIdempotente(event, claveIdempotencia);
        if (idempotente.isPresent()) {
            return idempotente.get();
        }

        LocalDateTime fechaInicio = event.getFechaInicio().atStartOfDay();
        LocalDateTime fechaFin = event.getFechaFin().atTime(23, 59, 59);

        List<Vehiculo> candidatos = vehicleRepository
                .findByActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(event.getTipoVehiculo());

        Optional<Vehiculo> vehiculoAsignable = asignarConLock(candidatos, fechaInicio, fechaFin);

        if (vehiculoAsignable.isEmpty()) {
            return VehicleAssignmentResult.builder()
                    .success(false)
                    .idAsignacion(event.getIdAsignacion())
                    .motivo("Sin vehículos disponibles del tipo " + event.getTipoVehiculo())
                    .build();
        }

        return crearReservaConfirmada(vehiculoAsignable.get(), event, claveIdempotencia, fechaInicio, fechaFin);
    }

    @Override
    @Transactional
    public VehicleReleaseResult procesarLiberacionAsignacion(VehicleReleaseEvent event) {
        if (event == null) {
            log.warn("Evento de liberación nulo ignorado");
            return VehicleReleaseResult.ignored("Evento nulo");
        }

        if (event.getIdAsignacion() == null && event.getIdSaga() == null) {
            log.warn("Evento de liberación inválido: falta idAsignacion o idSaga");
            return VehicleReleaseResult.ignored("Debe incluir idAsignacion o idSaga");
        }

        if (event.getMotivo() == null || event.getMotivo().isBlank()) {
            log.warn("Evento de liberación inválido: motivo vacío para asignación {}", event.getIdAsignacion());
            return VehicleReleaseResult.ignored("El motivo es obligatorio");
        }

        Optional<ReservaVehiculo> reservaOpt = Optional.empty();
        if (event.getIdAsignacion() != null) {
            reservaOpt = reservaRepository.findByIdAsignacionExt(event.getIdAsignacion());
        }
        if (reservaOpt.isEmpty() && event.getIdSaga() != null) {
            reservaOpt = reservaRepository.findBySagaVehiculo_IdSaga(event.getIdSaga());
        }

        if (reservaOpt.isEmpty()) {
            log.warn("Liberación sin reserva local (asignación={}, saga={}). Puede ser cancelación previa a confirmación.",
                    event.getIdAsignacion(), event.getIdSaga());
            return VehicleReleaseResult.ignored("No existe reserva local para la asignación indicada");
        }

        ReservaVehiculo reserva = reservaOpt.get();
        UUID idAsignacion = reserva.getIdAsignacionExt();

        if (reserva.getEstadoReserva() == EstadoReserva.CANCELADA) {
            log.info("Liberación idempotente: reserva {} ya cancelada (asignación {})",
                    reserva.getIdReserva(), idAsignacion);
            return VehicleReleaseResult.idempotent(idAsignacion, reserva.getIdReserva());
        }

        if (reserva.getSagaVehiculo() != null
                && reserva.getSagaVehiculo().getEstadoSaga() == EstadoSaga.COMPENSADA) {
            log.info("Liberación idempotente: saga {} ya compensada (asignación {})",
                    reserva.getSagaVehiculo().getIdSaga(), idAsignacion);
            return VehicleReleaseResult.idempotent(idAsignacion, reserva.getIdReserva());
        }

        String motivo = event.getMotivo().trim();
        if (event.getOrigen() != null && !event.getOrigen().isBlank()) {
            motivo = "[" + event.getOrigen().trim() + "] " + motivo;
        }

        compensarPorReservaId(reserva.getIdReserva(), motivo);
        log.info("Reserva {} liberada por evento Kafka (asignación {})", reserva.getIdReserva(), idAsignacion);

        return VehicleReleaseResult.processed(idAsignacion, reserva.getIdReserva());
    }

    @Override
    @Transactional
    public void confirmarReservaPorAsignacion(UUID idAsignacion) {

        ReservaVehiculo reserva = reservaRepository
                .findByIdAsignacionExt(idAsignacion)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reserva",
                                "idAsignacion",
                                idAsignacion));

        //confirmarReserva(reserva.getIdReserva());
        SagaVehiculo saga = reserva.getSagaVehiculo();

        if (saga != null && saga.getEstadoSaga() != EstadoSaga.EN_PROGRESO) {
            throw new BusinessException(
                    "El proceso de reserva no se encuentra en un estado válido para ser confirmado. Estado actual: "
                            + saga.getEstadoSaga());
        }

        reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
        reserva.setActualizadoEn(LocalDateTime.now());
        reservaRepository.save(reserva);

        if (saga != null) {
            saga.setEstadoSaga(EstadoSaga.COMPLETADA);
            saga.setActualizadoEn(LocalDateTime.now());
            sagaRepository.save(saga);
            log.info(
                "Saga {} marcada como COMPLETADA.",
                saga.getIdSaga()
            );
        }

        log.info(
            "Reserva {} confirmada exitosamente para la asignación {}.",
            reserva.getIdReserva(),
            idAsignacion
        );

    }
}
