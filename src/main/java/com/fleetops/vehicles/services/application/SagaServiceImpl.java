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
    private final IdempotencyValidator idempotencyValidator; // Protege contra peticiones duplicadas.
    private final AvailabilityPolicy availabilityPolicy; // Reglas de negocio (¿Se puede rentar?).

    // ─────────────────────────────────────────────────────────────────────────────
    // MÉTODO: iniciarReserva (Vía ID)
    // ─────────────────────────────────────────────────────────────────────────────

    private ReservaResponse validarYProcesarReserva(Vehiculo vehiculo, ReservaRequest request) {

        // 1. Validar Disponibilidad (La lógica que pediste para que no reserve si no
        // está DISPONIBLE)
        if (vehiculo.getEstadoVehiculo() != EstadoVehiculo.DISPONIBLE) {
            List<EstadoReserva> estadosOcupados = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
            List<ReservaVehiculo> reservasActivas = reservaRepository
                    .findByVehiculo_IdVehiculoAndEstadoReservaIn(vehiculo.getIdVehiculo(), estadosOcupados);

            List<AgendaReservaResponse> agenda = reservasActivas.stream()
                    .map(r -> new AgendaReservaResponse(r.getFechaInicio(), r.getFechaFin(),
                            r.getEstadoReserva().name()))
                    .toList();

            throw new ReservaConflictException(
                    "El vehículo no está disponible. intentelo en los dias en que no este reservado.",
                    agenda);
        }

        // 2. Validar Solapamiento de Fechas (La lógica original que ya tenías)
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
            throw new ReservaConflictException("La reserva se cruza con estas fechas", conflictosMapeados);
        }

        // 3. Si todo está correcto, procesamos la reserva
        return procesarCreacionReserva(vehiculo, request);
    }

    @Override
    @Transactional
    public ReservaResponse iniciarReserva(UUID idVehiculo, ReservaRequest request) {
        log.info("Iniciando saga de reserva para vehículo ID: {}", idVehiculo);
        idempotencyValidator.validateNotDuplicate(request.claveIdempotencia());

        Vehiculo vehiculo = vehicleRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", "id", idVehiculo));

        // Llamada unificada
        return validarYProcesarReserva(vehiculo, request);
    }

    @Override
    @Transactional
    public ReservaResponse iniciarReservaByPlaca(String placa, ReservaRequest request) {
        log.info("Iniciando saga de reserva para vehículo placa: {}", placa);
        idempotencyValidator.validateNotDuplicate(request.claveIdempotencia());

        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", "placa", placa));

        // Llamada unificada
        return validarYProcesarReserva(vehiculo, request);
    }

    // Método privado central que contiene toda la lógica de creación de la reserva.
    private ReservaResponse procesarCreacionReserva(Vehiculo vehiculo, ReservaRequest request) {

        // REGLA DE NEGOCIO: "Políticas de Disponibilidad".
        // Utiliza el servicio de dominio para verificar SOAT, RTM y estado mecánico del
        // camión.
        if (!availabilityPolicy.isAvailableForReservation(vehiculo)) {
            // Si el vehículo no es apto (ej: SOAT vencido), abortamos la operación lanzando
            // una excepción de negocio.
            throw new BusinessException("El vehículo no cumple las políticas para ser reservado.");
        }

        // REGLA DE NEGOCIO: "Anti-Solapamiento de Fechas".
        // Ejecuta una consulta a la base de datos para detectar si existe alguna otra
        // reserva
        // que choque con el intervalo [fechaInicio, fechaFin] solicitado.
        boolean existeSolapamiento = reservaRepository.existeReservaEnRango(
                vehiculo.getIdVehiculo(), // ID del camión.
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA), // Solo nos importan reservas activas.
                request.fechaFin(), // Fecha de fin solicitada.
                request.fechaInicio()); // Fecha de inicio solicitada.

        // Si la consulta retorna 'true', significa que el camión está ocupado en ese
        // periodo.
        if (existeSolapamiento) {
            // Bloqueamos la operación para evitar el "double-booking" o sobre-reserva.
            throw new BusinessException("El vehículo ya tiene una reserva que se cruza con las fechas solicitadas.");
        }

        // Creación del "Expediente" de la Saga (Bitácora de transacción distribuida).
        // Representa la "carpeta" donde documentaremos todos los pasos de este trámite.
        SagaVehiculo saga = new SagaVehiculo();

        // Asociamos la carpeta al camión físico que estamos reservando.
        saga.setVehiculo(vehiculo);

        // Definimos la naturaleza del trámite para el orquestador.
        saga.setTipoOperacion("RESERVA_VEHICULO");

        // Estado inicial del trámite: "INICIADA".
        saga.setEstadoSaga(EstadoSaga.INICIADA);

        // Guardamos la clave de idempotencia (el ticket único) para evitar
        // procesamiento duplicado.
        saga.setClaveIdempotencia(request.claveIdempotencia());

        // Guardamos el JSON original de la petición como "foto" para auditoría forense
        // si algo falla.
        saga.setPayload(request.toString());

        // Registramos el momento exacto en que nació este expediente.
        saga.setCreadoEn(LocalDateTime.now());

        // Persistimos la Saga en base de datos para obtener su ID único (idSaga).
        saga = sagaRepository.save(saga);

        // ====================================================================================
        // NOTA DE ARQUITECTURA:
        // El vehículo se mantiene en estado DISPONIBLE. El componente asíncrono
        // (VehicleStateScheduler) se encargará de pasarlo a RESERVADO cuando el reloj
        // del servidor alcance la fechaInicio de esta reserva.
        // ====================================================================================

        // Actualizamos el estado de la Saga a "EN_PROGRESO".
        saga.setEstadoSaga(EstadoSaga.EN_PROGRESO);

        // Guardamos la actualización de la Saga.
        sagaRepository.save(saga);

        // Creamos el objeto Reserva, que es el contrato formal del servicio.
        ReservaVehiculo reserva = new ReservaVehiculo();

        // Vinculamos la reserva al vehículo.
        reserva.setVehiculo(vehiculo);

        // Vinculamos la reserva a la Saga (para saber en qué trámite está metida).
        reserva.setSagaVehiculo(saga);

        // Guardamos el ID externo del sistema de Asignaciones (para correlación de
        // servicios).
        reserva.setIdAsignacionExt(UUID.fromString(request.idAsignacionExt()));

        // Iniciamos la reserva como PENDIENTE (espera confirmación de los demás
        // servicios).
        reserva.setEstadoReserva(EstadoReserva.PENDIENTE);

        // Copiamos la clave de idempotencia al recibo para consistencia.
        reserva.setClaveIdempotencia(request.claveIdempotencia());

        // Guardamos el dato de quién solicitó el servicio.
        reserva.setSolicitadoPor(request.solicitadoPor());

        // Guardamos la fecha de inicio del viaje.
        reserva.setFechaInicio(request.fechaInicio());

        // Guardamos la fecha de fin del viaje.
        reserva.setFechaFin(request.fechaFin());

        // Registramos el momento de creación del recibo.
        reserva.setCreadoEn(LocalDateTime.now());

        // Persistimos el recibo final en la base de datos.
        reserva = reservaRepository.save(reserva);

        // Log informativo: indicamos éxito en la fase de orquestación.
        log.info("Saga de reserva EN_PROGRESO. Reserva creada (Vehículo aguardando tiempo de inicio). SagaID: {}",
                saga.getIdSaga());

        // Retornamos el DTO de respuesta transformado para el cliente.
        return toReservaResponse(reserva);
    }

    // @Override: Sobrescribe el método definido en la interfaz SagaService.
    @Override
    // @Transactional: Asegura que todos los cambios a la base de datos (reserva y
    // saga) ocurran
    // como una sola unidad atómica: si algo falla, no se confirma nada.
    @Transactional
    public Optional<ReservaResponse> confirmarReserva(UUID idReserva) {
        // Log de información: rastreamos el evento de confirmación en la consola del
        // servidor.
        log.info("Confirmando reserva ID: {}", idReserva);

        // Utilizamos Optional.map() para ejecutar lógica solo si la reserva existe,
        // evitando errores de "NullPointerException".
        return reservaRepository.findById(idReserva).map(reserva -> {

            // Extraemos el expediente (Saga) asociado a esta reserva para verificar su
            // estado global.
            SagaVehiculo saga = reserva.getSagaVehiculo();

            // REGLA DE NEGOCIO: Validamos que la Saga esté en "EN_PROGRESO".
            // No podemos confirmar una saga que ya fue cancelada, falló o ya terminó.
            if (saga != null && saga.getEstadoSaga() != EstadoSaga.EN_PROGRESO) {
                // Lanzamos una excepción de negocio si la saga no está en el estado correcto.
                throw new BusinessException(
                        "La Saga no está en un estado válido para ser confirmada: " + saga.getEstadoSaga());
            }

            // Cambiamos el estado del recibo de la reserva de PENDIENTE a CONFIRMADA.
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);

            // Registramos la fecha exacta en la que se realizó esta confirmación.
            reserva.setActualizadoEn(LocalDateTime.now());

            // Persistimos el cambio en la tabla de reservas.
            reservaRepository.save(reserva);

            // Si el expediente (Saga) existe, lo marcamos como COMPLETADA (fin exitoso).
            if (saga != null) {
                // Actualizamos el estado del orquestador.
                saga.setEstadoSaga(EstadoSaga.COMPLETADA);
                // Registramos la fecha de cierre.
                saga.setActualizadoEn(LocalDateTime.now());
                // Guardamos el estado final del expediente en base de datos.
                sagaRepository.save(saga);
            }

            // Log de éxito: confirma que la transacción distribuida finalizó correctamente.
            log.info("Saga COMPLETADA. Reserva {} confirmada exitosamente.", reserva.getIdReserva());

            // Retornamos la entidad confirmada para que el controlador pueda usarla.
            return toReservaResponse(reserva);
        });
    }

    @Override
    @Transactional
    public List<ReservaResponse> confirmarReservaPorPlaca(String numeroPlaca) {
        log.info("Iniciando confirmación masiva de reservas pendientes para el vehículo con placa: {}", numeroPlaca);

        // 1. BÚSQUEDA MASIVA:
        List<ReservaVehiculo> reservasPendientes = reservaRepository
                .findAllByVehiculoNumeroPlacaIgnoreCaseAndEstadoReserva(numeroPlaca, EstadoReserva.PENDIENTE);

        if (reservasPendientes.isEmpty()) {
            throw new BusinessException(
                    "No se encontró ninguna reserva PENDIENTE para el vehículo con placa: " + numeroPlaca);
        }

        // 2. PROCESAMIENTO EN BUCLE (Reservas y Sagas):
        reservasPendientes.forEach(reserva -> {

            // A. Confirmamos el recibo de la reserva
            reserva.setEstadoReserva(EstadoReserva.CONFIRMADA);
            reserva.setActualizadoEn(LocalDateTime.now());

            // B. Buscamos el expediente (Saga) de esta reserva y lo cerramos
            SagaVehiculo saga = reserva.getSagaVehiculo();
            if (saga != null && saga.getEstadoSaga() == EstadoSaga.EN_PROGRESO) {
                saga.setEstadoSaga(EstadoSaga.COMPLETADA);
                saga.setActualizadoEn(LocalDateTime.now());
                sagaRepository.save(saga); // Guardamos la saga individual
            }

            log.info("Reserva ID {} CONFIRMADA y su Saga COMPLETADA.", reserva.getIdReserva());
        });

        // NOTA: Eliminamos la lógica de pasar el vehículo a RESERVADO aquí,
        // ya que el VehicleStateScheduler lo hará automáticamente por tiempo.

        // 3. PERSISTENCIA EN BLOQUE (BATCH):
        List<ReservaVehiculo> reservasConfirmadas = reservaRepository.saveAll(reservasPendientes);
        log.info("Se confirmaron exitosamente {} reservas para la placa {}.", reservasConfirmadas.size(), numeroPlaca);

        // 4. PREVENCIÓN DEL ERROR LAZY (Mapeo a DTO dentro de la transacción):
        // Convertimos la lista de entidades en una lista de Records (DTOs) listos para
        // enviar al JSON.
        return reservasConfirmadas.stream()
                .map(this::toReservaResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservaResponse actualizarFechasReserva(UUID idReserva, UpdateReservaDatesRequest request) {
        // 1. Buscamos la reserva o disparamos 404
        ReservaVehiculo reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", "ID", idReserva));

        // 2. Validación cronológica manual interna (coherencia física)
        if (request.fechaFin().isBefore(request.fechaInicio()) || request.fechaFin().isEqual(request.fechaInicio())) {
            throw new BusinessException("La fecha de fin debe ser estrictamente posterior a la fecha de inicio.");
        }

        // 3. REGLA DE NEGOCIO: Comprobar estado apto para modificación
        if (reserva.getEstadoReserva() != EstadoReserva.PENDIENTE
                && reserva.getEstadoReserva() != EstadoReserva.CONFIRMADA) {
            throw new BusinessException(
                    "No se pueden modificar las fechas de una reserva en estado: " + reserva.getEstadoReserva());
        }

        // 4. REGLA DE NEGOCIO: Evitar solapamientos en agendas activas
        List<EstadoReserva> estadosCriticos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
        List<ReservaVehiculo> colisiones = reservaRepository.findOverlappingReservations(
                reserva.getVehiculo().getIdVehiculo(),
                idReserva,
                request.fechaInicio(),
                request.fechaFin(),
                estadosCriticos);

        // 5. Si existen colisiones, disparamos el flujo estructurado de conflictos
        // (HTTP 400 con arreglo de reservas)
        if (!colisiones.isEmpty()) {
            List<AgendaReservaResponse> conflictosResponse = colisiones.stream()
                    .map(c -> new AgendaReservaResponse(c.getFechaInicio(), c.getFechaFin(),
                            c.getEstadoReserva().name()))
                    .toList();
            throw new ReservaConflictException("El nuevo rango de fechas colisiona con reservas activas del vehículo.",
                    conflictosResponse);
        }

        // 6. Mutación segura de datos y auditoría de bloqueo optimista (@Version)
        reserva.setFechaInicio(request.fechaInicio());
        reserva.setFechaFin(request.fechaFin());
        reserva.setActualizadoEn(LocalDateTime.now());

        ReservaVehiculo reservaActualizada = reservaRepository.save(reserva);

        // 7. Mapeo y retorno desacoplado por medio de tu DtoMapperReserva
        return dtoMapperReserva.toDto(reservaActualizada);
    }

    // @Override: Indica que estamos implementando la lógica definida en el contrato
    // SagaService.
    @Override
    // @Transactional: Esencial para operaciones de rollback. Si la compensación
    // falla,
    // la base de datos no quedará en un estado inconsistente.
    @Transactional
    public boolean compensarPorReservaId(UUID reservaId, String motivo) {

        // Log para auditoría: permite rastrear en los logs qué reserva se intenta
        // revertir y cuál es el motivo.
        log.info("Iniciando compensación para Reserva ID: {}", reservaId);

        // Paso 1: Localizamos el recibo (reserva) en la base de datos.
        // Si no existe, ResourceNotFoundException dispara un error 404.
        ReservaVehiculo reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("ReservaVehiculo", "id", reservaId));

        // Paso 2: Verificamos si existe un expediente (Saga) ligado a esta reserva.
        // El Orquestador de Sagas es el único que conoce los pasos que se tomaron,
        // así que es él quien sabe cómo deshacerlos.
        if (reserva.getSagaVehiculo() != null) {

            // Paso 3: Delegación.
            // En lugar de duplicar lógica aquí, le pasamos la responsabilidad al método
            // maestro
            // 'compensarSaga', que sabe cómo liberar vehículos, cancelar recibos y auditar
            // el rollback.
            return compensarSaga(reserva.getSagaVehiculo().getIdSaga(), motivo);
        }

        // Si la reserva no tiene saga (es decir, no fue una transacción distribuida),
        // no hay nada "distribuido" que compensar. Retornamos 'false' indicando que no
        // se aplicó rollback.
        return false;
    }

    // @Override: Indica que implementamos la lógica definida en la interfaz.
    @Override
    // @Transactional: Envuelve todo el proceso en una sola transacción. Si falla
    // una línea,
    // el estado de la base de datos no se altera (Rollback total).
    @Transactional
    public boolean compensarSaga(UUID sagaId, String motivo) {
        // Log para auditoría: permite rastrear qué expediente se está revirtiendo y por
        // qué causa.
        log.info("Iniciando compensación (rollback) para SagaID: {}. Motivo: {}", sagaId, motivo);

        // Paso 1: Localizamos el expediente de la Saga. Si no existe,
        // ResourceNotFoundException lanza un 404.
        SagaVehiculo saga = sagaRepository.findById(sagaId)
                .orElseThrow(() -> new ResourceNotFoundException("SagaVehiculo", "id", sagaId));

        // Paso 2: Idempotencia. Si la saga ya está compensada, no hacemos nada.
        // Esto evita que reintentos externos ejecuten la lógica de compensación varias
        // veces.
        if (saga.getEstadoSaga() == EstadoSaga.COMPENSADA) {
            log.info("La saga {} ya estaba compensada previamente.", sagaId);
            return true;
        }

        // Paso 3: Regla de Negocio - Límite de retroactividad (Auditoría contable).
        if (saga.getEstadoSaga() == EstadoSaga.COMPLETADA) {
            // Calculamos cuántos días han pasado desde que el trámite finalizó
            // exitosamente.
            long diasTranscurridos = ChronoUnit.DAYS.between(saga.getActualizadoEn(), LocalDateTime.now());

            // Si es un trámite muy viejo, prohibimos revertirlo para proteger registros
            // contables/fiscales.
            if (diasTranscurridos > 15) {
                throw new BusinessException("No se puede compensar una saga completada hace más de 15 días.");
            }
        }

        // Paso 4: Búsqueda del recibo asociado a este vehículo.
        // Buscamos si hay alguna reserva "en espera" (pendiente) para este camión.
        ReservaVehiculo reserva = reservaRepository.findReservaPendienteByVehiculoId(saga.getVehiculo().getIdVehiculo())
                .orElse(null);

        // Si encontramos un recibo, lo anulamos físicamente marcándolo como CANCELADA.
        if (reserva != null) {
            reserva.setEstadoReserva(EstadoReserva.CANCELADA);
            reserva.setActualizadoEn(LocalDateTime.now());
            reservaRepository.save(reserva);
        }

        // Paso 5: Recuperamos el activo físico (el camión).
        Vehiculo vehiculo = saga.getVehiculo();

        // Guardamos el estado anterior (ej: RESERVADO) para dejar rastro en el
        // historial.
        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        // Liberamos el camión: lo regresamos a DISPONIBLE para que pueda ser rentado de
        // nuevo.
        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
        vehiculo.setActualizadoEn(LocalDateTime.now());
        vehicleRepository.save(vehiculo);

        // Paso 6: Obtenemos el usuario que ejecutó la compensación mediante el contexto
        // de seguridad.
        String usuarioActual = SecurityContextHolder.getContext().getAuthentication().getName();

        // Paso 7: Registramos en el Historial (Bitácora).
        // Documentamos que el vehículo cambió de estado debido a un rollback.
        registrarHistorial(vehiculo, estadoAnterior, EstadoVehiculo.DISPONIBLE,
                "Compensación de Saga: " + motivo, usuarioActual);

        // Paso 8: Finalizamos el expediente de la Saga.
        // Marcamos el expediente como COMPENSADA (revertida exitosamente).
        saga.setEstadoSaga(EstadoSaga.COMPENSADA);
        // Anotamos quién fue el responsable de la anulación.
        saga.setCompensadoPor(usuarioActual);
        // Guardamos el motivo técnico del fallo.
        saga.setUltimoError(motivo);
        saga.setActualizadoEn(LocalDateTime.now());
        // Persistimos el cierre del expediente.
        sagaRepository.save(saga);

        // Log final: confirmamos que la transacción se deshizo correctamente.
        log.info("Compensación completada exitosamente para SagaID: {}", sagaId);
        return true;
    }

    // Método privado (interno): Este método actúa como una "caja negra" de
    // auditoría.
    // Centraliza la creación de registros en la bitácora para asegurar
    // consistencia.
    private void registrarHistorial(Vehiculo vehiculo, EstadoVehiculo estadoAnterior,
            EstadoVehiculo estadoNuevo, String motivo, String servicioOrigen) {

        // Crea una instancia vacía de la entidad HistorialEstadoVehiculo (la página en
        // el libro de auditoría).
        HistorialEstadoVehiculo historial = new HistorialEstadoVehiculo();

        // Enlaza el registro con el vehículo al que le ocurrió el cambio (Relación
        // Foreign Key).
        historial.setVehiculo(vehiculo);

        // REGLA DE SEGURIDAD (Null Check): Si es el primer estado del vehículo,
        // estadoAnterior es null.
        // Convertimos el Enum a String usando .name(). Si es nulo, guardamos null
        // explícitamente en la BD.
        historial.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);

        // Convertimos el estado nuevo (Enum) a texto plano (String) para persistirlo en
        // la tabla.
        historial.setEstadoNuevo(estadoNuevo.name());

        // Guardamos el motivo del cambio (ej: "Mantenimiento", "Cancelación por falta
        // de pago").
        // Es vital para que un humano pueda entender el porqué de la historia.
        historial.setMotivoCambio(motivo);

        // Guardamos el origen (ej: "microservicio-asignaciones").
        // Esto permite rastrear qué parte del sistema fue el "culpable" o responsable
        // del cambio.
        historial.setServicioOrigen(servicioOrigen);

        // Capturamos el timestamp exacto del servidor en el momento de la ejecución.
        // Al usar LocalDateTime.now(), garantizamos un sello de tiempo preciso para la
        // línea de tiempo.
        historial.setRegistradoEn(LocalDateTime.now());

        // Persistimos (guardamos) la hoja en la tabla 'historial_estados_vehiculo'.
        // Una vez guardado aquí, el registro es inmutable para efectos de auditoría.
        historialEstadoRepository.save(historial);
    }

    // Método privado (interno): Encapsula la lógica de transformación de Entidad a
    // DTO.
    // Al ser privado, garantizas que solo este servicio conoce cómo se construye la
    // respuesta.
    private ReservaResponse toReservaResponse(ReservaVehiculo reserva) {

        // Extraemos el vehículo a una variable local (v) para evitar llamados anidados
        // profundos.
        // Esto hace que el código sea más legible y manejable.
        Vehiculo v = reserva.getVehiculo();

        // Creamos y retornamos el "Record" (DTO) inmutable de Java 21.
        // El uso de un Record garantiza que, una vez creado, nadie pueda modificar
        // estos datos.
        return new ReservaResponse(
                reserva.getIdReserva(), // ID único de la reserva.

                // Mapeo defensivo: Si el vehículo es nulo, enviamos null en lugar de romper el
                // sistema.
                v != null ? v.getIdVehiculo() : null,

                // Convertimos el Enum a String (.name()) para que el JSON lo lea como texto
                // (ej: "CONFIRMADA").
                reserva.getEstadoReserva() != null ? reserva.getEstadoReserva().name() : null,

                // Convertimos el UUID a String para compatibilidad universal con navegadores.
                reserva.getIdAsignacionExt() != null ? reserva.getIdAsignacionExt().toString() : null,

                reserva.getSolicitadoPor(), // Nombre del solicitante.
                reserva.getFechaInicio(), // Fecha de inicio.
                reserva.getFechaFin(), // Fecha de fin.
                reserva.getClaveIdempotencia(), // Clave de control.

                // Extraemos el ID de la Saga si existe (puede ser nulo si la reserva no es
                // parte de una saga).
                reserva.getSagaVehiculo() != null ? reserva.getSagaVehiculo().getIdSaga() : null,

                // =========================================================================
                // EXTRACCIÓN PLANA (FLATTENING) DE DATOS DEL VEHÍCULO
                // =========================================================================
                // En lugar de enviar un objeto "vehiculo" complejo al cliente, "aplanamos"
                // los datos para que el Frontend reciba una estructura simple y fácil de leer.

                // 1. Placa del vehículo.
                v != null ? v.getNumeroPlaca() : null,

                // 2. Nombre del tipo de vehículo (Acceso a la entidad relacionada).
                // Verificamos dos niveles de nulos (v y TipoVehiculo) para evitar
                // NullPointerExceptions.
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getNombreTipo() : null,

                // 3. Descripción del catálogo del tipo de vehículo.
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getDescripcion() : null,

                // 4. Kilometraje actual (Dato operativo clave).
                v != null ? v.getKilometraje() : null,

                // 5. Capacidad de carga legal (Dato extraído del catálogo del vehículo).
                v != null && v.getTipoVehiculo() != null ? v.getTipoVehiculo().getCapacidadCarga() : null);
    }

    // @Override: Indica que estamos implementando el contrato definido en la
    // interfaz SagaService.
    @Override
    // @Transactional(readOnly = true): Optimizamos la consulta. Al ser solo
    // lectura, Hibernate no necesita
    // realizar "Dirty Checking" (verificar si los datos cambiaron), reduciendo el
    // consumo de CPU y memoria.
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findAllReservas(Pageable pageable) {

        // Log de auditoría: Deja rastro de la petición en la consola del servidor para
        // monitoreo.
        log.info("Consultando el historial global paginado de todas las reservas.");

        // 1. BUSQUEDA PAGINADA:
        // Solicitamos a la base de datos solo una porción de los registros (ej: página
        // 0, tamaño 20).
        // 'findAllByOrderByCreadoEnDesc' ordena los resultados de forma descendente
        // (los más recientes primero).
        // Esto es mucho más eficiente que traer todo el historial a la memoria del
        // microservicio.
        Page<ReservaVehiculo> paginaReservas = reservaRepository.findAllByOrderByCreadoEnDesc(pageable);

        // 2. TRANSFORMACIÓN (MAPPING):
        // Usamos el 'dtoMapperReserva' para convertir cada entidad (ReservaVehiculo) en
        // un DTO (ReservaResponse).
        // .map() aplica esta conversión a cada elemento de la página automáticamente,
        // preservando la información de paginación.
        return paginaReservas.map(dtoMapperReserva::toDto);
    }

    // Operación de solo lectura: optimiza rendimiento.
    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasPendientes(Pageable pageable) {
        // Corregido: Log específico para este estado.
        log.info("Consultando la bandeja de reservas en estado PENDIENTE.");

        // Buscamos las reservas con estado PENDIENTE, ordenadas por creación (más
        // reciente primero).
        Page<ReservaVehiculo> paginaPendientes = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.PENDIENTE, pageable);

        // Mapeamos a DTO para ocultar detalles técnicos de la BD al Frontend.
        return paginaPendientes.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasConfirmadas(Pageable pageable) {
        // Corregido: Log específico para este estado.
        log.info("Consultando la bandeja de reservas en estado CONFIRMADA.");

        // Buscamos solo aquellas que ya fueron aprobadas.
        Page<ReservaVehiculo> paginaConfirmadas = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.CONFIRMADA, pageable);

        return paginaConfirmadas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasFallidas(Pageable pageable) {
        // Corregido: Log específico para este estado.
        log.info("Consultando la bandeja de reservas en estado FALLIDA.");

        // Buscamos las que tuvieron problemas técnicos o de negocio (necesitan
        // revisión).
        Page<ReservaVehiculo> paginaFallidas = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.FALLIDA, pageable);

        return paginaFallidas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasCanceladas(Pageable pageable) {
        // Corregido: Log específico para este estado.
        log.info("Consultando la bandeja de reservas en estado CANCELADA.");

        // Buscamos las que fueron anuladas por el cliente o el sistema.
        Page<ReservaVehiculo> paginaCanceladas = reservaRepository
                .findAllByEstadoReservaOrderByCreadoEnDesc(EstadoReserva.CANCELADA, pageable);

        return paginaCanceladas.map(dtoMapperReserva::toDto);
    }

    // =========================================================================
    // CONSULTAS POR ID (Puntual)
    // =========================================================================

    @Override
    @Transactional(readOnly = true) // Optimizado para lectura.
    public Optional<ReservaResponse> findReservaById(UUID idReserva) {
        // Busca en base de datos la reserva por su identificador único.
        // El uso de .map() nos permite transformar la entidad (si existe) en un DTO
        // en una sola línea de código, manteniendo la fluidez funcional.
        return reservaRepository.findById(idReserva)
                .map(this::toReservaResponse);
    }

    // =========================================================================
    // CONSULTAS POR PLACA (Forense / Historial)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasByPlaca(String placa, Pageable pageable) {
        // Log para auditoría: permite rastrear qué vehículo está siendo investigado.
        log.info("Consultando historial de reservas para el vehículo con placa: {}", placa);

        // 1. Buscamos todas las reservas asociadas a la placa.
        // Al usar 'IgnoreCase', evitamos errores de búsqueda si el operador escribe
        // 'abc123' en lugar de 'ABC123'.
        // Al usar 'Pageable', garantizamos que si el camión tiene 500 viajes, el
        // sistema paginará los resultados.
        Page<ReservaVehiculo> paginaReservas = reservaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(placa, pageable);

        // 2. Mapeamos la página completa (transformamos cada elemento dentro de la
        // página).
        return paginaReservas.map(dtoMapperReserva::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaResponse> findReservasByPlacaAndEstado(String placa, EstadoReserva estado, Pageable pageable) {
        // Log específico: ayuda a saber si estamos buscando, por ejemplo, reservas
        // canceladas de un camión específico.
        log.info("Consultando reservas para el vehículo con placa: {} filtradas por estado: {}", placa, estado);

        // 1. Búsqueda compuesta: Filtramos en BD por dos criterios simultáneos.
        // Esto es mucho más rápido que traer todas las reservas del camión y filtrar en
        // memoria.
        Page<ReservaVehiculo> paginaReservas = reservaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoReservaOrderByCreadoEnDesc(placa, estado, pageable);

        // 2. Mapeo a DTO para entregar al cliente una respuesta limpia y sin detalles
        // técnicos de la BD.
        return paginaReservas.map(dtoMapperReserva::toDto);
    }

    // =========================================================================
    // CONSULTA GLOBAL DE SAGAS
    // =========================================================================

    @Override
    @Transactional(readOnly = true) // Optimizado: solo lectura, sin Dirty Checking.
    public Page<SagaResponse> findAllSagas(Pageable pageable) {
        // Recupera todos los expedientes de Saga, ordenados del más reciente al más
        // antiguo.
        // Convertimos cada entidad en un DTO para enviar al Frontend.
        return sagaRepository.findAllByOrderByCreadoEnDesc(pageable)
                .map(dtoMapperSaga::toDto);
    }

    // =========================================================================
    // FILTROS DE ESTADO (Monitoreo de flujo de Saga)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasIniciadas(Pageable pageable) {
        // Filtra expedientes recién creados. Útil para verificar si el orquestador está
        // recibiendo carga.
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.INICIADA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasEnProgreso(Pageable pageable) {
        // Filtra expedientes que están procesándose activamente.
        // Es la métrica clave para detectar latencia en procesos distribuidos.
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.EN_PROGRESO, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasCompletadas(Pageable pageable) {
        // Filtra expedientes exitosos. Útil para auditoría de finalización de procesos.
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.COMPLETADA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasFallidas(Pageable pageable) {
        // Filtra expedientes con error. Estos registros requieren atención manual (o
        // reintento automático).
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.FALLIDA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasCompensadas(Pageable pageable) {
        // Filtra expedientes que fueron revertidos. Vital para análisis de causas raíz
        // (¿por qué falló?).
        return sagaRepository.findAllByEstadoSagaOrderByCreadoEnDesc(EstadoSaga.COMPENSADA, pageable)
                .map(dtoMapperSaga::toDto);
    }

    // =========================================================================
    // BÚSQUEDAS ESPECÍFICAS POR VEHÍCULO (Trazabilidad)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasByPlaca(String placa, Pageable pageable) {
        // Busca todo el historial de sagas de un camión específico.
        // Ideal para ver qué procesos han afectado a un vehículo concreto a lo largo
        // del tiempo.
        return sagaRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByCreadoEnDesc(placa, pageable)
                .map(dtoMapperSaga::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SagaResponse> findSagasByPlacaAndEstado(String placa, EstadoSaga estado, Pageable pageable) {
        // Búsqueda avanzada: Cruza vehículo y estado.
        // Ejemplo: "¿Tiene este camión alguna saga en estado FALLIDA que deba revisar?"
        return sagaRepository
                .findByVehiculo_NumeroPlacaIgnoreCaseAndEstadoSagaOrderByCreadoEnDesc(placa, estado, pageable)
                .map(dtoMapperSaga::toDto);
    }

}