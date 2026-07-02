// Define el paquete exclusivo para la capa de servicios de aplicación.
package com.fleetops.vehicles.services.application;

// Importaciones necesarias: Excepciones para manejo de errores, Mappers para DTOs,
// Entidades para el modelo de datos, Repositorios para el acceso a DB y servicios de dominio.
import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ReservaConflictException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperReserva;
import com.fleetops.vehicles.mapper.DtoMapperSaga;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.dto.request.ReservaRequest;
import com.fleetops.vehicles.dto.request.UpdateReservaDatesRequest;
import com.fleetops.vehicles.dto.response.AgendaReservaResponse;
import com.fleetops.vehicles.dto.response.ReservaResponse;
import com.fleetops.vehicles.dto.response.SagaResponse;
import com.fleetops.vehicles.repositories.*;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.IdempotencyValidator;

// Importaciones de Lombok para reducir código repetitivo y facilitar el logging.
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Importaciones de Spring para paginación, seguridad y transacciones.
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones de Java para manejo de tiempos, listas y utilidades.
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// @Slf4j: Genera automáticamente un logger para dejar rastro de lo que ocurre en el servicio.
@Slf4j
// @Service: Registra esta clase en Spring para que pueda ser inyectada en
// controladores.
@Service
// @RequiredArgsConstructor: Genera un constructor con todos los campos 'final',
// permitiendo la inyección de dependencias.
@RequiredArgsConstructor
public class SagaServiceImpl implements SagaService {

    // ─────────────────────────────────────────────────────────────────────────────
    // DEPENDENCIAS: Los colaboradores que necesita este servicio para operar.
    // ─────────────────────────────────────────────────────────────────────────────

    // Mapeadores para transformar entidades complejas en DTOs amigables para el
    // API.
    private final DtoMapperReserva dtoMapperReserva;
    private final DtoMapperSaga dtoMapperSaga;

    // Repositorios: Nuestros puntos de acceso a las tablas de la base de datos.
    private final SagaRepository sagaRepository;
    private final ReservaRepository reservaRepository;
    private final VehicleRepository vehicleRepository;
    private final HistorialEstadoRepository historialEstadoRepository;

    // Servicios de Dominio: Contienen la lógica de negocio pura, fuera de la base
    // de datos.
    private final IdempotencyValidator idempotencyValidator;
    private final AvailabilityPolicy availabilityPolicy;

    // ─────────────────────────────────────────────────────────────────────────────
    // MÉTODO: iniciarReserva (Vía ID)
    // ─────────────────────────────────────────────────────────────────────────────
    private ReservaResponse validarYProcesarReserva(Vehiculo vehiculo, ReservaRequest request) {

        // =====================================================================
        // 🚀 NUEVA REGLA: Vigencia Legal (SOAT y RTM) - VERSIÓN ESTRICTA
        // =====================================================================
        java.time.LocalDate hoy = java.time.LocalDate.now();

        // 1. Validación de SOAT
        if (vehiculo.getFechaSoat() == null) {
            throw new BusinessException("No se puede iniciar la reserva. El vehículo no tiene registrado un SOAT.");
        } else if (vehiculo.getFechaSoat().isBefore(hoy)) {
            throw new BusinessException("No se puede iniciar la reserva. El SOAT del vehículo ya está vencido.");
        } else if (java.time.temporal.ChronoUnit.DAYS.between(hoy, vehiculo.getFechaSoat()) <= 7) {
            throw new BusinessException(
                    "No se puede iniciar la reserva. El SOAT del vehículo vencerá en 7 días o menos.");
        }

        // 2. Validación de Revisión Técnico Mecánica (RTM)
        if (vehiculo.getFechaRtm() == null) {
            throw new BusinessException("No se puede iniciar la reserva. El vehículo no tiene registrada una RTM.");
        } else if (vehiculo.getFechaRtm().isBefore(hoy)) {
            throw new BusinessException(
                    "No se puede iniciar la reserva. La Revisión Técnico Mecánica (RTM) ya está vencida.");
        } else if (java.time.temporal.ChronoUnit.DAYS.between(hoy, vehiculo.getFechaRtm()) <= 7) {
            throw new BusinessException(
                    "No se puede iniciar la reserva. La Revisión Técnico Mecánica (RTM) vencerá en 7 días o menos.");
        }

        // =====================================================================
        // 3. Validar Disponibilidad (Con mensajes dinámicos)
        // =====================================================================
        if (vehiculo.getEstadoVehiculo() != EstadoVehiculo.DISPONIBLE) {

            // 🌟 NUEVA LÓGICA: Determinamos el mensaje exacto según el estado real
            String mensajeError = "";
            switch (vehiculo.getEstadoVehiculo()) {
                case RESERVADO:
                    mensajeError = "El vehículo no está disponible. Inténtelo en los días en que no esté reservado.";
                    break;
                case FUERA_DE_SERVICIO:
                    mensajeError = "El vehículo no está disponible, está fuera de servicio.";
                    break;
                case EN_MANTENIMIENTO:
                    mensajeError = "El vehículo no está disponible, está en mantenimiento.";
                    break;
                default:
                    break;
            }

            // Consultamos la base de datos para ver si tiene viajes programados
            List<EstadoReserva> estadosOcupados = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
            List<ReservaVehiculo> reservasActivas = reservaRepository
                    .findByVehiculo_IdVehiculoAndEstadoReservaIn(vehiculo.getIdVehiculo(), estadosOcupados);

            List<AgendaReservaResponse> agenda = reservasActivas.stream()
                    .map(r -> new AgendaReservaResponse(r.getFechaInicio(), r.getFechaFin(),
                            r.getEstadoReserva().name()))
                    .toList();

            // Lanzamos la excepción inyectando el texto que calculamos en el switch
            throw new ReservaConflictException(mensajeError, agenda);
        }

        // Validación de identificadores externos únicos
        UUID idAsignacion = UUID.fromString(request.idAsignacionExt());
        if (reservaRepository.existsByIdAsignacionExt(idAsignacion)) {
            throw new BusinessException("UUID de asignaciones duplicado, use uno diferente.");
        }

        // 4. Validar Solapamiento de Fechas
        List<ReservaVehiculo> conflictos = reservaRepository.obtenerReservasConflictivas(
                vehiculo.getIdVehiculo(),
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA),
                request.fechaFin(),
                request.fechaInicio());

        if (!conflictos.isEmpty()) {
            List<AgendaReservaResponse> conflictosMapeados = conflictos.stream()
                    .map(r -> new AgendaReservaResponse(r.getFechaInicio(), r.getFechaFin(),
                            r.getEstadoReserva().name()))
                    .toList();
            throw new ReservaConflictException("La reserva se cruza con las siguientes fechas asignadas:",
                    conflictosMapeados);
        }

        // 5. Si todo está correcto legal y operativamente, procesamos la reserva
        return procesarCreacionReserva(vehiculo, request);
    }

    @Override
    @Transactional
    public ReservaResponse iniciarReserva(UUID idVehiculo, ReservaRequest request) {
        log.info("Iniciando proceso de reserva para vehículo ID: {}", idVehiculo);
        idempotencyValidator.validateNotDuplicate(request.claveIdempotencia());

        Vehiculo vehiculo = vehicleRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", "id", idVehiculo));

        return validarYProcesarReserva(vehiculo, request);
    }

    @Override
    @Transactional
    public ReservaResponse iniciarReservaByPlaca(String placa, ReservaRequest request) {
        log.info("Iniciando proceso de reserva para vehículo placa: {}", placa);
        idempotencyValidator.validateNotDuplicate(request.claveIdempotencia());

        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", "placa", placa));

        return validarYProcesarReserva(vehiculo, request);
    }

    // Método privado central que contiene toda la lógica de creación de la reserva.
    private ReservaResponse procesarCreacionReserva(Vehiculo vehiculo, ReservaRequest request) {

        if (!availabilityPolicy.isAvailableForReservation(vehiculo)) {
            throw new BusinessException("El vehículo no cumple las políticas operativas para ser reservado.");
        }

        boolean existeSolapamiento = reservaRepository.existeReservaEnRango(
                vehiculo.getIdVehiculo(),
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA),
                request.fechaFin(),
                request.fechaInicio());

        if (existeSolapamiento) {
            throw new BusinessException("El vehículo ya tiene una reserva que se cruza con las fechas solicitadas.");
        }

        // Creación del "Expediente" de la Saga (Bitácora de transacción distribuida).
        SagaVehiculo saga = new SagaVehiculo();
        saga.setVehiculo(vehiculo);
        saga.setTipoOperacion("RESERVA_VEHICULO");
        saga.setEstadoSaga(EstadoSaga.INICIADA);
        saga.setClaveIdempotencia(request.claveIdempotencia());
        saga.setPayload(request.toString());
        saga.setCreadoEn(LocalDateTime.now());
        saga = sagaRepository.save(saga);

        saga.setEstadoSaga(EstadoSaga.EN_PROGRESO);
        sagaRepository.save(saga);

        // Creamos el objeto Reserva, que es el contrato formal del servicio.
        ReservaVehiculo reserva = new ReservaVehiculo();
        reserva.setVehiculo(vehiculo);
        reserva.setSagaVehiculo(saga);
        reserva.setIdAsignacionExt(UUID.fromString(request.idAsignacionExt()));
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);
        reserva.setClaveIdempotencia(request.claveIdempotencia());
        reserva.setSolicitadoPor(request.solicitadoPor());
        reserva.setFechaInicio(request.fechaInicio());
        reserva.setFechaFin(request.fechaFin());
        reserva.setCreadoEn(LocalDateTime.now());
        reserva = reservaRepository.save(reserva);

        log.info(
                "Proceso de reserva EN_PROGRESO. Reserva creada (Vehículo aguardando tiempo de inicio). Trámite ID: {}",
                saga.getIdSaga());

        return toReservaResponse(reserva);
    }

    @Override
    @Transactional
    public Optional<ReservaResponse> confirmarReserva(UUID idReserva) {
        log.info("Confirmando reserva ID: {}", idReserva);

        return reservaRepository.findById(idReserva).map(reserva -> {

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
            }

            log.info("Proceso COMPLETADO. Reserva {} confirmada exitosamente.", reserva.getIdReserva());

            return toReservaResponse(reserva);
        });
    }

    @Override
    @Transactional
    public List<ReservaResponse> confirmarReservaPorPlaca(String numeroPlaca) {
        log.info("Iniciando confirmación masiva de reservas pendientes para el vehículo con placa: {}", numeroPlaca);

        List<ReservaVehiculo> reservasPendientes = reservaRepository
                .findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva(numeroPlaca, EstadoReserva.PENDIENTE);

        if (reservasPendientes.isEmpty()) {
            throw new BusinessException(
                    "No se encontró ninguna reserva PENDIENTE para el vehículo con placa: " + numeroPlaca);
        }

        reservasPendientes.forEach(reserva -> {
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
            reserva.setActualizadoEn(LocalDateTime.now());

            SagaVehiculo saga = reserva.getSagaVehiculo();
            if (saga != null && saga.getEstadoSaga() == EstadoSaga.EN_PROGRESO) {
                saga.setEstadoSaga(EstadoSaga.COMPLETADA);
                saga.setActualizadoEn(LocalDateTime.now());
                sagaRepository.save(saga);
            }

            log.info("Reserva ID {} CONFIRMADA y su proceso interno COMPLETADO.", reserva.getIdReserva());
        });

        List<ReservaVehiculo> reservasConfirmadas = reservaRepository.saveAll(reservasPendientes);
        log.info("Se confirmaron exitosamente {} reservas para la placa {}.", reservasConfirmadas.size(), numeroPlaca);

        return reservasConfirmadas.stream()
                .map(this::toReservaResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservaResponse actualizarFechasReserva(UUID idReserva, UpdateReservaDatesRequest request) {
        ReservaVehiculo reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "ID", idReserva));

        if (request.fechaFin().isBefore(request.fechaInicio()) || request.fechaFin().isEqual(request.fechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser estrictamente posterior a la fecha de inicio.");
        }

        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE
                && reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new BusinessException(
                    "No se pueden modificar las fechas de una reserva en estado: " + reserva.getEstadoReserva());
        }

        List<EstadoReserva> estadosCriticos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
        List<ReservaVehiculo> colisiones = reservaRepository.findOverlappingReservations(
                reserva.getVehiculo().getIdVehiculo(),
                idReserva,
                request.fechaInicio(),
                request.fechaFin(),
                estadosCriticos);

        if (!colisiones.isEmpty()) {
            List<AgendaReservaResponse> conflictosResponse = colisiones.stream()
                    .map(c -> new AgendaReservaResponse(c.getFechaInicio(), c.getFechaFin(),
                            c.getEstadoReserva().name()))
                    .toList();
            throw new ReservaConflictException("El nuevo rango de fechas colisiona con reservas activas del vehículo.",
                    conflictosResponse);
        }

        reserva.setFechaInicio(request.fechaInicio());
        reserva.setFechaFin(request.fechaFin());
        reserva.setActualizadoEn(LocalDateTime.now());

        ReservaVehiculo reservaActualizada = reservaRepository.save(reserva);

        return dtoMapperReserva.toDto(reservaActualizada);
    }

    @Override
    @Transactional
    public ReservaResponse compensarPorReservaId(UUID reservaId, String motivo) {
        log.info("Iniciando anulación para la Reserva ID: {}", reservaId);

        ReservaVehiculo reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("ReservaVehiculo", "id", reservaId));

        if (reserva.getSagaVehiculo() != null) {
            // El método compensarSaga internamente ya cambia el estado de la reserva a CANCELADA
            compensarSaga(reserva.getSagaVehiculo().getIdSaga(), motivo);
        } else {
            // Respaldo: si por alguna razón no tuviera saga, forzamos la cancelación directa
            reserva.setEstadoReserva(EstadoReserva.CANCELADA);
            reserva.setActualizadoEn(LocalDateTime.now());
            reservaRepository.save(reserva);
        }

        // Retornamos el objeto completo mapeado a DTO con su nuevo estado
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
        
        // Formateador de fecha para que quede exactamente como en tu imagen: dd/MM/yyyy HH:mm
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (ReservaVehiculo reserva : reservas) {
            
            boolean esViajeEnCurso = reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA &&
                                     !ahora.isBefore(reserva.getFechaInicio()) && 
                                     !ahora.isAfter(reserva.getFechaFin());

            String motivoSaga = motivo; // Por defecto, usamos el motivo que escribió el usuario

            // NUEVA REGLA: Interceptamos la reserva en curso, pero NO la omitimos, la cortamos.
            if (vehiculo.getEstadoVehiculo() == EstadoVehiculo.RESERVADO && esViajeEnCurso) {
                log.info("Corte de Viaje Activo: La reserva {} será truncada y cancelada masivamente.", reserva.getIdReserva());
                
                // 1. Truncamos la fecha de fin a la fecha/hora actual del sistema
                reserva.setFechaFin(ahora);
                
                // 2. Modificamos el motivo solo para esta reserva, añadiendo el aviso de corte
                motivoSaga = motivo + " | [AVISO AUTOMÁTICO: El vehículo estaba operando. Se cortó la reserva activa hasta la fecha " + ahora.format(formatter) + "]." + " por un caso de fuerza mayor.";
            }

            // 3. Cancelamos la reserva (Aplica para la que estaba en curso y para las futuras)
            reserva.setEstadoReserva(EstadoReserva.CANCELADA);
            reserva.setActualizadoEn(ahora);
            reservaRepository.save(reserva);

            // 4. Disparamos la compensación enviando el motivo adecuado a los demás microservicios
            if (reserva.getSagaVehiculo() != null) {
                compensarSaga(reserva.getSagaVehiculo().getIdSaga(), motivoSaga);
            }

            reservasCanceladas.add(reserva);
        }

        // Convertimos la lista de entidades canceladas a lista de DTOs y la retornamos
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

        Vehiculo vehiculo = saga.getVehiculo();
        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        String usuarioActual = "SISTEMA_AUTOMATICO";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            usuarioActual = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        if (estadoAnterior == EstadoVehiculo.RESERVADO || estadoAnterior == EstadoVehiculo.DISPONIBLE) {
            vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
            vehiculo.setActualizadoEn(LocalDateTime.now());
            vehicleRepository.save(vehiculo);

            registrarHistorial(vehiculo, estadoAnterior, EstadoVehiculo.DISPONIBLE,
                    "Anulación de reserva: " + motivo, usuarioActual);

            log.info("Vehículo {} devuelto a estado DISPONIBLE.", vehiculo.getNumeroPlaca());
        } else {
            log.warn("El vehículo {} no se devolvió a DISPONIBLE porque se encuentra en estado de emergencia: {}",
                    vehiculo.getNumeroPlaca(), estadoAnterior);
        }

        saga.setEstadoSaga(EstadoSaga.COMPENSADA);
        saga.setCompensadoPor(usuarioActual);
        saga.setUltimoError(motivo);
        saga.setActualizadoEn(LocalDateTime.now());
        sagaRepository.save(saga);

        log.info("Anulación completada exitosamente para el Trámite ID: {}", sagaId);
        return true;
    }

    private void registrarHistorial(Vehiculo vehiculo, EstadoVehiculo estadoAnterior,
            EstadoVehiculo estadoNuevo, String motivo, String servicioOrigen) {

        HistorialEstadoVehiculo historial = new HistorialEstadoVehiculo();
        historial.setVehiculo(vehiculo);
        historial.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);
        historial.setEstadoNuevo(estadoNuevo.name());
        historial.setMotivoCambio(motivo);
        historial.setServicioOrigen(servicioOrigen);
        historial.setRegistradoEn(LocalDateTime.now());
        historialEstadoRepository.save(historial);
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
        log.info("Consultando el historial global paginado de todas las reservas.");
        Page<ReservaVehiculo> paginaReservas = reservaRepository.findAllByOrderByCreadoEnDesc(pageable);
        return paginaReservas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasPendientes(Pageable pageable) {
        log.info("Consultando la bandeja de reservas en estado PENDIENTE.");
        Page<ReservaVehiculo> paginaPendientes = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.PENDIENTE, pageable);
        return paginaPendientes.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasConfirmadas(Pageable pageable) {
        log.info("Consultando la bandeja de reservas en estado CONFIRMADA.");
        Page<ReservaVehiculo> paginaConfirmadas = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.CONFIRMADA, pageable);
        return paginaConfirmadas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasFallidas(Pageable pageable) {
        log.info("Consultando la bandeja de reservas en estado FALLIDA.");
        Page<ReservaVehiculo> paginaFallidas = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.FALLIDA, pageable);
        return paginaFallidas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasCanceladas(Pageable pageable) {
        log.info("Consultando la bandeja de reservas en estado CANCELADA.");
        Page<ReservaVehiculo> paginaCanceladas = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.CANCELADA, pageable);
        return paginaCanceladas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReservaResponse> findReservaById(UUID idReserva) {
        return reservaRepository.findById(idReserva)
                .map(this::toReservaResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasByPlaca(String placa, Pageable pageable) {
        log.info("Consultando historial de reservas para el vehículo con placa: {}", placa);
        Page<ReservaVehiculo> paginaReservas = reservaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(placa, pageable);
        return paginaReservas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasByPlacaAndEstado(String placa, EstadoReserva estado, Pageable pageable) {
        log.info("Consultando reservas para el vehículo con placa: {} filtradas por estado: {}", placa, estado);
        Page<ReservaVehiculo> paginaReservas = reservaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoReservaOrderByCreadoEnDesc(placa, estado, pageable);
        return paginaReservas.map(dtoMapperReserva::toDto);
    }

    // =========================================================================
    // CONSULTAS GLOBALES DE PROCESOS (Anteriormente Sagas)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findAllSagas(Pageable pageable) {
        return sagaRepository.findAllByOrderByCreadoEnDesc(pageable)
                .map(dtoMapperSaga::toDto);
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
}