package com.fleetops.vehicles.services.application;
// Define la carpeta o paquete del sistema donde vive esta clase, encargada de la lógica principal de la aplicación.

import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperHistorial;
import com.fleetops.vehicles.mapper.DtoMapperVehicle;
import com.fleetops.vehicles.models.entities.*;
import com.fleetops.vehicles.dto.request.EstadoCambioRequest;
import com.fleetops.vehicles.dto.request.VehicleRequest;
import com.fleetops.vehicles.dto.request.VehicleUpdateRequest;
import com.fleetops.vehicles.dto.response.DisponibilidadResponse;
import com.fleetops.vehicles.dto.response.HistorialEstadoResponse;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import com.fleetops.vehicles.repositories.HistorialEstadoRepository;
import com.fleetops.vehicles.repositories.ReservaRepository;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;
import com.fleetops.vehicles.services.domain.AvailabilityPolicy;
import com.fleetops.vehicles.services.domain.StateTransitionValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// @Slf4j: Anotación de Lombok que genera una herramienta para escribir registros (logs) en la consola del servidor.
// Ejemplo: Permite usar log.info("Mensaje") para dejar una "huella" de lo que hace el sistema.
@Slf4j
// @Service: Anotación de Spring que marca esta clase como el "Cerebro"
// (Servicio) de la aplicación.
// Ejemplo: Le avisa a Spring "Guárdame en memoria, los controladores me van a
// llamar para gestionar vehículos".
@Service
// @RequiredArgsConstructor: Anotación de Lombok que genera un constructor
// automático para todos los campos 'final'.
// PATRÓN DE DISEÑO: "Inyección de Dependencias". Spring nos entrega las
// herramientas (repositorios) listas para usar.
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    // Implementa el contrato VehicleService. PATRÓN DE DISEÑO: "Fachada". Oculta la
    // complejidad interna al mundo exterior.

    // Herramienta inyectada para acceder a los datos de los vehículos en la base de
    // datos.
    private final VehicleRepository vehicleRepository;

    // Herramienta inyectada para acceder al catálogo de tipos de vehículos
    // (Camioneta, Furgón, etc.).
    private final TipoVehiculoRepository tipoVehiculoRepository;

    // Herramienta inyectada para consultar información sobre las reservas (viajes)
    // de la flota.
    private final ReservaRepository reservaRepository;

    // Herramienta inyectada para guardar la bitácora de cambios de estado de cada
    // vehículo.
    private final HistorialEstadoRepository historialEstadoRepository;

    // Reglas inyectadas para verificar si un vehículo está "apto" para trabajar
    // (ej: SOAT vigente).
    private final AvailabilityPolicy availabilityPolicy;

    // Validador inyectado que controla que los estados cambien en orden lógico (ej:
    // de Disponible a Mantenimiento).
    private final StateTransitionValidator stateTransitionValidator;

    // Mapper inyectado para transformar entidades (Base de datos) a DTOs (Formato
    // limpio para el usuario).
    private final DtoMapperVehicle dtoMapperVehicle;

    // Mapper inyectado para transformar registros históricos en respuestas
    // legibles.
    private final DtoMapperHistorial dtoMapperHistorial;

    private final SagaService sagaService;

    // @Override: Indica que implementamos el método de la interfaz.
    @Override
    // @Transactional(readOnly = true): Configura esta operación como "Solo
    // Lectura", optimizando el rendimiento.
    // Ejemplo: Le promete a la base de datos que no vamos a cambiar nada,
    // permitiendo optimizaciones de caché.
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findAll(Pageable pageable) {
        // Método que devuelve una lista paginada de vehículos.

        // Registro de traza (log) indicando que la consulta ha comenzado.
        log.debug("Consultando todos los vehículos paginados");

        // Llama al repositorio para obtener solo los vehículos que están activos
        // (borrado lógico),
        // pagina los resultados y los convierte (mapea) a respuestas (DTOs) limpias.
        return vehicleRepository.findAllByActivoTrue(pageable)
                .map(dtoMapperVehicle::toDto);
    }

    // =========================================================================
    // CONSULTAS DE VEHÍCULOS (BORRADO LÓGICO Y BÚSQUEDAS)
    // =========================================================================

    @Override
    @Transactional(readOnly = true) // Transacción optimizada para solo lectura.
    public Page<VehicleResponse> getDeletedVehicles(int page, int size) {
        // Método para listar vehículos que ya no están activos (ej: vendidos o dados de
        // baja).

        // 1. Creamos la configuración de paginación (Página X, Tamaño Y).
        // Esto le indica a Spring Data exactamente cuántos registros traer y cuál
        // "página" mostrar.
        Pageable pageable = PageRequest.of(page, size);

        // 2. Buscamos en el repositorio filtrando solo por los inactivos (activo =
        // false).
        // Esto permite al administrador ver el inventario histórico o vehículos
        // vendidos.
        Page<Vehiculo> vehiculosInactivos = vehicleRepository.findAllByActivoFalse(pageable);

        // 3. Traducimos las entidades (Vehiculo) a DTOs (VehicleResponse).
        // Nunca exponemos la entidad real al usuario final por seguridad y flexibilidad
        // del contrato API.
        return vehiculosInactivos.map(dtoMapperVehicle::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(UUID id) {
        // Método para buscar un vehículo por su UUID (identificador interno único).

        // Registramos en log el ID para monitorear qué vehículo se está consultando.
        log.debug("Consultando vehículo por ID: {}", id);

        // Buscamos el vehículo activo.
        // REGLA DE NEGOCIO: findAllByIdAndActivoTrue garantiza que aunque el ID exista,
        // si el vehículo está "borrado lógicamente", el sistema se comporte como si no
        // existiera.
        Vehiculo vehiculo = vehicleRepository.findByIdVehiculoAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // Retornamos el vehículo convertido a DTO (VehicleResponse).
        return dtoMapperVehicle.toDto(vehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findByPlaca(String placa) {
        // Método de búsqueda por placa: la forma más común en que un humano interactúa
        // con un vehículo.

        // Registramos la placa en el log.
        log.debug("Consultando vehículo por placa: {}", placa);

        // REGLA DE NEGOCIO: Ignoramos mayúsculas y validamos que el vehículo esté
        // activo.
        // IgnoreCase permite que "abc123" y "ABC123" sean tratados igual, mejorando la
        // experiencia del usuario.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // Mapeamos a DTO y retornamos.
        return dtoMapperVehicle.toDto(vehiculo);
    }

    // =========================================================================
    // CONSULTAS POR ESTADO OPERATIVO
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findDisponibles(Pageable pageable) {
        // Método que trae únicamente la lista paginada de vehículos listos para operar.

        // Registra la acción en la consola para monitoreo de operaciones.
        log.debug("Consultando vehículos disponibles");

        // Filtra en BD los que están 'DISPONIBLE' y son 'Activos'.
        // Mapea el resultado a DTOs para el cliente.
        return vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.DISPONIBLE, pageable)
                .map(dtoMapperVehicle::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findReservados(Pageable pageable) {
        log.debug("Consultando vehículos con reserva CONFIRMADA activa en este momento");
        return vehicleRepository.findAllWithActiveReservation(LocalDateTime.now(), pageable)
                .map(dtoMapperVehicle::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findMantenimiento(Pageable pageable) {
        // Método que trae la lista paginada de vehículos que están en taller.

        // Registra la acción en la consola.
        log.debug("Consultando vehículos en mantenimiento");

        // Filtra los que están en 'EN_MANTENIMIENTO' y son 'Activos'.
        return vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.EN_MANTENIMIENTO, pageable)
                .map(dtoMapperVehicle::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findFueraServicio(Pageable pageable) {
        // Método que trae la lista paginada de vehículos que no pueden operar por
        // avería.

        // Registra la acción en la consola.
        log.debug("Consultando vehículos fuera de servicio");

        // Filtra los que están 'FUERA_DE_SERVICIO' y son 'Activos'.
        return vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.FUERA_DE_SERVICIO, pageable)
                .map(dtoMapperVehicle::toDto);
    }

    private void validarVencimientoDocumentos(LocalDate fechaSoat, LocalDate fechaRtm, boolean esRegistro) {
        LocalDate fechaLimite = LocalDate.now().plusDays(7);

        // Determinamos la palabra según la acción
        String accion = esRegistro ? "registrar" : "actualizar";

        // Validar SOAT
        if (fechaSoat != null && fechaSoat.isBefore(fechaLimite)) {
            throw new IllegalArgumentException(
                    "No se puede " + accion + " el vehiculo porque el SOAT vence en menos de 7 dias");
        }

        // Validar RTM
        if (fechaRtm != null && fechaRtm.isBefore(fechaLimite)) {
            throw new IllegalArgumentException(
                    "No se puede " + accion + " el vehiculo porque el RTM vence en menos de 7 dias");
        }
    }

    @Override
    // @Transactional: Envuelve la operación en una transacción. Si algo falla (ej.
    // error de base de datos),
    // nada se guarda, evitando estados inconsistentes en la flota.
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        // Log informativo: permite rastrear quién registró qué vehículo y cuándo en el
        // historial del servidor.
        log.info("Iniciando registro de vehículo con placa: {}", request.numeroPlaca());

        // Unicidad de placa/chasis/motor la garantiza la BD (UNIQUE); el handler global mapea el 409.
        validarVencimientoDocumentos(request.fechaSoat(), request.fechaRtm(), true);

        // BUSQUEDA DE RELACIÓN: Buscamos el catálogo de 'TipoVehiculo'.
        // Si el usuario envía un ID de tipo inexistente, lanzamos 404 inmediatamente.
        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.findById(request.idTipoVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "id", request.idTipoVehiculo()));

        // CREACIÓN DE ENTIDAD: Creamos una hoja en blanco (objeto Vehiculo) para
        // llenar.
        Vehiculo vehiculo = new Vehiculo();

        // PLACA: Forzamos a mayúsculas para mantener consistencia en búsquedas
        // (normalización de datos).
        vehiculo.setNumeroPlaca(request.numeroPlaca().toUpperCase());

        // RELACIÓN: Vinculamos el vehículo a su categoría (TipoVehiculo).
        vehiculo.setTipoVehiculo(tipoVehiculo);

        // MAPPING: Copiamos los atributos técnicos desde el DTO al objeto entidad.
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnioFabricacion(request.anioFabricacion());
        vehiculo.setColor(request.color());
        vehiculo.setNumeroChasis(request.numeroChasis().toUpperCase());
        vehiculo.setNumeroMotor(request.numeroMotor().toUpperCase());
        vehiculo.setKilometraje(request.kilometraje());
        vehiculo.setCiudadOperacion(request.ciudadOperacion());
        vehiculo.setSedeOperacion(request.sedeOperacion());

        // Todo vehículo nuevo nace DISPONIBLE por defecto.
        // Ya no leemos 'request.estadoVehiculo()' para evitar que el sistema
        // explote si el usuario olvida mandarlo en el JSON de Postman.
        vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);

        // FECHAS VENCIMIENTO: Guardamos documentos legales (SOAT, RTM) y último
        // mantenimiento.
        vehiculo.setFechaSoat(request.fechaSoat());
        vehiculo.setFechaRtm(request.fechaRtm());
        vehiculo.setFechaUltimoMant(request.fechaUltimoMant());

        // ESTADO ACTIVO: Por defecto, un vehículo nuevo siempre está activo (true).
        vehiculo.setActivo(true);

        // FECHA CREACIÓN: Sellamos el momento exacto de la creación para auditoría.
        vehiculo.setCreadoEn(LocalDateTime.now());

        // PERSISTENCIA: Guardamos en la base de datos (INSERT).
        Vehiculo vehiculoGuardado = vehicleRepository.save(vehiculo);

        // AUDITORÍA (Historial): Registramos el nacimiento del vehículo en el libro de
        // eventos.
        // Esto permite saber el "estado inicial" ante cualquier inspección futura.
        registrarHistorial(vehiculoGuardado, null, vehiculoGuardado.getEstadoVehiculo(),
                "Registro inicial del vehículo en el sistema", "fleetops-vehicles", null);

        // LOG ÉXITO: Confirmamos el ID generado en los logs.
        log.info("Vehículo registrado exitosamente con ID: {}", vehiculoGuardado.getIdVehiculo());

        // RETORNO: Convertimos la entidad a DTO y la enviamos al cliente.
        return dtoMapperVehicle.toDto(vehiculoGuardado);
    }

@Override
    @Transactional
    public VehicleResponse update(UUID id, VehicleUpdateRequest request) {
        // Registra en consola el ID del vehículo que se va a editar.
        log.info("Iniciando actualización de vehículo ID: {}", id);

        // Busca el vehículo en la base de datos.
        Vehiculo vehiculo = vehicleRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // Unicidad de placa/chasis/motor la garantiza la BD (UNIQUE); el handler global mapea el 409.
        validarVencimientoDocumentos(request.fechaSoat(), request.fechaRtm(), false);

        // 1. Lógica para actualizar el Tipo de Vehículo
        // Comparamos si el ID enviado es diferente al que tiene actualmente
        if (request.idTipoVehiculo() != null && !request.idTipoVehiculo().equals(vehiculo.getTipoVehiculo().getIdTipoVehiculo())) {
            TipoVehiculo nuevoTipo = tipoVehiculoRepository.findById(request.idTipoVehiculo())
                    .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "id", request.idTipoVehiculo()));
            vehiculo.setTipoVehiculo(nuevoTipo);
        }

        // ACTUALIZACIÓN DE DATOS GENERALES
        vehiculo.setNumeroPlaca(request.numeroPlaca().toUpperCase());
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnioFabricacion(request.anioFabricacion());
        vehiculo.setColor(request.color());
        vehiculo.setNumeroChasis(request.numeroChasis().toUpperCase());
        vehiculo.setNumeroMotor(request.numeroMotor().toUpperCase());
        vehiculo.setKilometraje(request.kilometraje());
        vehiculo.setCiudadOperacion(request.ciudadOperacion());
        vehiculo.setSedeOperacion(request.sedeOperacion());

        // FECHAS Y AUDITORÍA DE TABLA
        vehiculo.setFechaSoat(request.fechaSoat());
        vehiculo.setFechaRtm(request.fechaRtm());
        vehiculo.setFechaUltimoMant(request.fechaUltimoMant());

        // Sellamos la fecha de esta modificación
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // Impacta los cambios en la base de datos.
        Vehiculo vehiculoActualizado = vehicleRepository.save(vehiculo);

        // Imprime en consola el éxito de la operación.
        log.info("Vehículo actualizado exitosamente | ID: {}", id);

        // Retorna el vehículo actualizado convertido a DTO.
        return dtoMapperVehicle.toDto(vehiculoActualizado);
    }

    @Override
    // @Transactional: Garantiza que la búsqueda y la actualización se ejecuten como
    // una única unidad atómica.
    @Transactional
    public VehicleResponse updateByPlaca(String placa, VehicleUpdateRequest request) {
        // Método que actúa como una fachada: permite actualizar buscando por placa en
        // lugar de ID.

        // 1. Buscamos el vehículo por su placa actual.
        // Si no existe, ResourceNotFoundException dispara el 404.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // 2. REUTILIZACIÓN (Patrón Delegator):
        // Reutilizamos tu lógica central de 'update' pasándole el UUID que acabamos de
        // obtener.
        // Esto evita tener que escribir la lógica de validación de unicidad dos veces.
        return this.update(vehiculo.getIdVehiculo(), request);
    }

    // =========================================================================
    // BORRADO LÓGICO (SOFT DELETE) POR UUID
    // =========================================================================

    @Override
    // @Transactional: Garantiza que la búsqueda, validación y actualización ocurran
    // como una sola unidad atómica.
    @Transactional
    public boolean softDelete(UUID id) {

        log.info("Iniciando desactivación (soft delete) del vehículo ID: {}", id);

        // 1. Buscamos el vehículo
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // =====================================================================
        // 2. REGLA DE NEGOCIO: Validar reservas activas
        // =====================================================================
        boolean tieneReservasActivas = reservaRepository.existsByVehiculo_IdVehiculoAndEstadoReservaIn(
                id,
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA));

        if (tieneReservasActivas) {
            log.warn("Intento de eliminación fallido. Vehículo ID: {} tiene reservas activas.", id);
            // Esto generará el JSON de error exacto que solicitaste
            throw new IllegalStateException(
                    "no se puede eliminar el vehiculo porque tiene reservas pendientes o confirmadas primero cancele las reservas de ese vehiculo");
        }

        // Variable temporal para guardar el estado que tenía el vehículo antes de
        // borrarlo.
        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        // 3. Aplicar Soft Delete
        vehiculo.setActivo(false);

        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);

        // Si el estado debe cambiar al desactivarse (ej. INACTIVO), se haría aquí.
        // vehiculo.setEstadoVehiculo(EstadoVehiculo.INACTIVO);
        vehiculo.setActualizadoEn(LocalDateTime.now());

        vehicleRepository.save(vehiculo);

        // AUDITORÍA: Registramos en la bitácora la razón de la baja y quién la ejecutó.
        registrarHistorial(vehiculo, estadoAnterior, EstadoVehiculo.FUERA_DE_SERVICIO,
                "Baja lógica del vehículo del sistema operativo", "fleetops-vehicles", null);

        log.info("Vehículo ID: {} desactivado exitosamente", id);

        return true;
    }

    // =========================================================================
    // BORRADO LÓGICO (SOFT DELETE) POR PLACA
    // =========================================================================

    @Override
    @Transactional
    public void deleteByPlaca(String placa) {
        log.info("Iniciando desactivación (soft delete) por placa: {}", placa);

        // Buscamos el vehículo por su placa
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // REUTILIZACIÓN: Delegamos la operación al método por ID.
        // Así la regla de las reservas se ejecuta automáticamente sin duplicar código.
        this.softDelete(vehiculo.getIdVehiculo());
    }

    @Override
    @Transactional
    public VehicleResponse reactivarVehiculo(UUID id, String motivo) {
        log.info("Reactivando vehículo ID: {}", id);

        Vehiculo vehiculo = vehicleRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // Aquí SÍ necesitamos el IF, porque findDetailedById trae el vehículo sin importar su
        // estado.
        // Evita el doble clic si ya está activo.
        if (Boolean.TRUE.equals(vehiculo.getActivo())) {
            log.warn("Rechazo de reactivación: El vehículo ID {} ya está activo.", id);
            throw new BusinessException(
                    "El vehículo ya se encuentra activo en el sistema. No es necesario reactivarlo.");
        }

        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        vehiculo.setActivo(true);
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
        vehiculo.setActualizadoEn(LocalDateTime.now());

        Vehiculo guardado = vehicleRepository.save(vehiculo);
        String motivo_completo = "Reactivacion del vehiculo: " + motivo;

        registrarHistorial(guardado, estadoAnterior, EstadoVehiculo.FUERA_DE_SERVICIO, motivo_completo,
                "fleetops-vehicles", null);

        return dtoMapperVehicle.toDto(guardado);
    }

    @Override
    @Transactional
    public VehicleResponse reactivateByPlaca(String placa, String motivo) {
        // 1. Usamos tu método existente AndActivoFalse.
        // Si el usuario hace doble clic, el segundo clic caerá directamente en este
        // orElseThrow
        // porque la base de datos ya no lo encontrará como "inactivo".
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoFalse(placa)
                .orElseThrow(() -> new BusinessException(
                        "No se pudo reactivar. El vehículo con placa '" + placa.toUpperCase() +
                                "' no existe o YA se encuentra activo en el sistema."));

        // Al usar AndActivoFalse, ya no necesitamos hacer el "if
        // (vehiculo.getActivo())",
        // porque si el código llega a esta línea, es 100% seguro que el vehículo está
        // inactivo.

        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        vehiculo.setActivo(true);
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
        vehiculo.setActualizadoEn(LocalDateTime.now());

        Vehiculo guardado = vehicleRepository.save(vehiculo);
        String motivo_completo = "Reactivacion del vehiculo: " + motivo;

        registrarHistorial(guardado, estadoAnterior, EstadoVehiculo.FUERA_DE_SERVICIO, motivo_completo,
                "fleetops-vehicles", null);

        log.info("Vehículo con placa {} fue reactivado. Motivo: {}", placa, motivo);

        return dtoMapperVehicle.toDto(guardado);
    }

    // =========================================================================
    // CASCADA AUTOMÁTICA: Destrucción o Truncamiento de reservas por emergencia
    // =========================================================================
    private String procesarCascadaEmergencia(Vehiculo vehiculo, EstadoVehiculo estadoAnterior,
            EstadoVehiculo estadoDestino, String motivoCambio) {

        String motivoAuditoria = motivoCambio;
        LocalDateTime ahora = LocalDateTime.now();

        List<EstadoReserva> estadosCriticos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);
        List<ReservaVehiculo> reservasAfectadas = reservaRepository
                .findByVehiculo_IdVehiculoAndEstadoReservaIn(vehiculo.getIdVehiculo(), estadosCriticos);

        if (!reservasAfectadas.isEmpty()) {
            log.warn("ALERTA CASCADA: Vehículo [{}] declarado {}. Procesando {} reservas...",
                    vehiculo.getIdVehiculo(), estadoDestino.name(), reservasAfectadas.size());

            for (ReservaVehiculo reserva : reservasAfectadas) {

                // Evaluamos si el reloj actual entra en el rango de este viaje
                boolean esViajeEnCurso = reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA &&
                        !ahora.isBefore(reserva.getFechaInicio()) &&
                        !ahora.isAfter(reserva.getFechaFin());

                // REGLA: Solo cortamos fecha si hay un viaje CONFIRMADO en curso
                if (esViajeEnCurso) {

                    log.info("Corte de Viaje Activo: La reserva ID [{}] finalizará prematuramente y será cancelada.",
                            reserva.getIdReserva());

                    // Modificamos la fecha final a la hora del sistema y cancelamos
                    reserva.setFechaFin(ahora);
                    reserva.setEstadoReserva(EstadoReserva.CANCELADA);
                    reserva.setActualizadoEn(ahora);
                    reservaRepository.save(reserva);

                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm");

                    // Compensamos la saga para avisar a los demás sistemas
                    sagaService.compensarPorReservaId(reserva.getIdReserva(),
                            "Corte de Viaje: Vehículo " + estadoDestino.name() + ". Operó parcialmente hasta "
                                    + ahora.format(formatter));

                    // Ensamblamos el mensaje automático dinámico
                    motivoAuditoria = motivoCambio
                            + " | [AVISO AUTOMÁTICO: El vehículo estaba operando. Se cortó la reserva activa hasta la fecha "
                            + ahora.format(formatter) + " porque pasó a " + estadoDestino.name() + "].";

                } else {
                    // Futuros, pendientes o reservas aún no iniciadas: cancelación directa.
                    log.info("Cancelación de Viaje: La reserva ID [{}] se anula.", reserva.getIdReserva());

                    reserva.setEstadoReserva(EstadoReserva.CANCELADA);
                    reserva.setActualizadoEn(ahora);
                    reservaRepository.save(reserva);

                    sagaService.compensarPorReservaId(reserva.getIdReserva(),
                            "Fuerza Mayor: Vehículo declarado " + estadoDestino.name() + ". Motivo original: "
                                    + motivoCambio);
                }
            }
        }

        return motivoAuditoria;
    }

    @Override
    @Transactional
    public VehicleResponse changeState(UUID id, String nuevoEstado, String motivoCambio, String servicioOrigen) {
        // Método central para transicionar un vehículo de un estado operativo a otro de
        // manera controlada.

        log.info("Iniciando cambio de estado para vehículo ID: {}", id);

        Vehiculo vehiculo = vehicleRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        EstadoVehiculo estadoActual = vehiculo.getEstadoVehiculo();
        EstadoVehiculo estadoDestino;

        try {
            estadoDestino = EstadoVehiculo.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("El estado proporcionado no es válido: " + nuevoEstado);
        }

        if (estadoActual == estadoDestino) {
            log.warn("Intento de cambio redundante bloqueado. El vehículo ID {} ya está en estado {}", id,
                    estadoActual);
            throw new BusinessException("El vehículo ya se encuentra en el estado " + estadoActual
                    + ". No se generarán registros duplicados en el historial.");
        }

        stateTransitionValidator.validateTransition(estadoActual, estadoDestino);

        vehiculo.setEstadoVehiculo(estadoDestino);

        if (estadoActual == EstadoVehiculo.EN_MANTENIMIENTO && estadoDestino == EstadoVehiculo.DISPONIBLE) {
            vehiculo.setFechaUltimoMant(LocalDateTime.now().toLocalDate());
        }

        vehiculo.setActualizadoEn(LocalDateTime.now());

        String motivoAuditoria = motivoCambio;

        if (estadoDestino == EstadoVehiculo.FUERA_DE_SERVICIO || estadoDestino == EstadoVehiculo.EN_MANTENIMIENTO) {
            motivoAuditoria = procesarCascadaEmergencia(vehiculo, estadoActual, estadoDestino, motivoCambio);
            // Reafirmamos el estado por si la compensación de saga intentó liberar el vehículo.
            vehiculo.setEstadoVehiculo(estadoDestino);
        }

        Vehiculo vehiculoActualizado = vehicleRepository.save(vehiculo);
        registrarHistorial(vehiculoActualizado, estadoActual, estadoDestino, motivoAuditoria, servicioOrigen, null);

        log.info("Cambio de estado exitoso: {} -> {} | Vehículo ID: {}", estadoActual, estadoDestino, id);

        return dtoMapperVehicle.toDto(vehiculoActualizado);
    }

    // =========================================================================
    // FACHADA DE CAMBIO DE ESTADO (BÚSQUEDA POR PLACA)
    // =========================================================================

    @Override
    // @Transactional: Extiende el contexto transaccional al método delegado.
    @Transactional
    public VehicleResponse updateEstadoByPlaca(String placa, EstadoCambioRequest request) {
        // Método de conveniencia (Wrapper): Permite cambiar el estado buscando por el
        // identificador de la placa.

        // 1. Encuentra el vehículo activo cruzando su placa (ignorando
        // mayúsculas/minúsculas).
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // 2. PATRÓN DE DISEÑO: DRY (Don't Repeat Yourself - No repitas código).
        // En lugar de reescribir toda la lógica de validación, delegamos el trabajo al
        // método 'changeState',
        // pasándole el ID interno que acabamos de resolver.
        return changeState(vehiculo.getIdVehiculo(), request.nuevoEstado(), request.motivoCambio(),
                request.servicioOrigen());
    }

    // =========================================================================
    // POLÍTICA DE DISPONIBILIDAD (DOMAIN POLICY)
    // =========================================================================

    // @Override: Indica que estamos implementando el contrato definido en
    // VehicleService.
    @Override
    // @Transactional(readOnly = true): Optimiza la consulta en la base de datos al
    // ser solo de lectura.
    // Evita bloqueos de tablas y mejora el rendimiento general del sistema.
    @Transactional(readOnly = true)
    public boolean isAvailable(UUID id) {
        // Método de consulta rápida (True/False) para saber si un vehículo puede ser
        // asignado a un viaje o reserva.

        // Busca el vehículo por su ID.
        // Si no se encuentra, detiene la ejecución y lanza una excepción que se traduce
        // en un HTTP 404 (Not Found).
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // PATRÓN DE DISEÑO: Política de Dominio (Domain Policy / Specification).
        // En lugar de llenar este servicio de condicionales (if estado == DISPONIBLE &&
        // if SOAT_vigente),
        // delegamos la pregunta a una clase experta ('availabilityPolicy').
        // Esto permite que las reglas de negocio evolucionen sin tener que modificar
        // esta clase.
        return availabilityPolicy.isAvailable(vehiculo);
    }

    // =========================================================================
    // CONSULTAS DE BITÁCORA DE AUDITORÍA (AUDIT TRAIL)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoResponse> getHistorialByVehiculoId(UUID id, Pageable pageable) {
        // Consulta la bitácora histórica de transiciones de estado para un vehículo
        // específico.

        // PATRÓN DE DISEÑO: Fail-Fast (Falla rápido).
        // Usar 'existsById' es una consulta SQL ultra ligera ('SELECT 1 FROM...').
        // Validamos primero si el vehículo existe antes de lanzar la consulta pesada a
        // la tabla de historiales.
        if (!vehicleRepository.existsById(id)) {
            // Si el vehículo no existe, arrojamos un 404 inmediatamente, ahorrando recursos
            // del servidor.
            throw new ResourceNotFoundException("Vehículo", "id", id);
        }

        // Recupera el historial filtrando por el ID del vehículo.
        // 'OrderByRegistradoEnDesc' asegura un orden cronológico inverso (LIFO - el más
        // reciente primero).
        return historialEstadoRepository.findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(id, pageable)
                // Mapea la entidad de base de datos a un formato JSON limpio (DTO) para ocultar
                // detalles técnicos al cliente.
                .map(dtoMapperHistorial::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoResponse> getHistorialByPlaca(String placa, Pageable pageable) {
        // Variante de consulta de bitácora que permite al usuario buscar por la placa
        // del vehículo.

        // Spring Data JPA hace la magia aquí: el guion bajo en 'Vehiculo_NumeroPlaca'
        // ejecuta un JOIN implícito con la tabla de vehículos de forma automática.
        // 'IgnoreCase' hace que la búsqueda sea tolerante a mayúsculas y minúsculas.
        return historialEstadoRepository.findByVehiculo_NumeroPlacaIgnoreCaseOrderByRegistradoEnDesc(placa, pageable)
                // Convierte cada registro histórico a DTO para su correcta serialización.
                .map(dtoMapperHistorial::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoResponse> findAllHistorialGlobal(Pageable pageable) {
        // Devuelve el "CCTV" de la flota: el log global de absolutamente todos los
        // movimientos de la empresa.

        // Consulta toda la tabla de historiales, ordenada desde el evento más reciente
        // al más antiguo.
        // Ideal para alimentar un "Activity Feed" o panel de control en tiempo real
        // para los administradores.
        return historialEstadoRepository.findAllByOrderByRegistradoEnDesc(pageable)
                // Mapea la información a un formato DTO ligero.
                .map(dtoMapperHistorial::toDto);
    }

    // =========================================================================
    // CONSULTAS ULTRA-LIGERAS (MICROSERVICIOS / UI RÁPIDA)
    // =========================================================================

    @Override
    // @Transactional(readOnly = true): Optimiza rendimiento, sin bloqueos de
    // escritura.
    @Transactional(readOnly = true)
    public DisponibilidadResponse getDisponibilidad(UUID id) {
        // Retorna un DTO "ultra ligero" (ej. 3 campos).
        // Ideal para que otro microservicio (ej. Módulo de Reservas) pregunte si un
        // camión está disponible,
        // sin necesidad de descargar toda la ficha técnica (marca, modelo, chasis,
        // etc.).

        // Ubicamos la entidad en la base de datos.
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // PATRÓN DE DOMINIO: Calculamos la disponibilidad real (papeles vigentes +
        // estado operativo).
        boolean disponible = availabilityPolicy.isAvailable(vehiculo);

        // Ensamblamos y retornamos el objeto de respuesta.
        // Se asume que 'DisponibilidadResponse' es un Java Record, lo cual es óptimo en
        // memoria.
        return new DisponibilidadResponse(
                vehiculo.getIdVehiculo(), // 1. Identificador
                vehiculo.getEstadoVehiculo().name(), // 2. Estado actual (String puro, no Enum)
                disponible, // 3. Resultado de la política (True/False)
                // 4. "Fallback" de auditoría: Si 'actualizadoEn' es null (nunca modificado),
                // envía 'creadoEn'.
                vehiculo.getActualizadoEn() != null ? vehiculo.getActualizadoEn() : vehiculo.getCreadoEn());
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponse getDisponibilidadByPlaca(String placa) {
        // Variante de consulta ligera, ideal para ser consumida por dispositivos GPS o
        // tótems en portería
        // donde se digita la placa del vehículo en lugar del ID del sistema.

        // Búsqueda insensible a mayúsculas/minúsculas y validando que el vehículo esté
        // activo.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // Evaluación de reglas de negocio para determinar disponibilidad.
        boolean disponible = availabilityPolicy.isAvailable(vehiculo);

        // Retorno del DTO de bajo peso (Payload reducido en JSON).
        return new DisponibilidadResponse(
                vehiculo.getIdVehiculo(),
                vehiculo.getEstadoVehiculo().name(),
                disponible,
                vehiculo.getActualizadoEn() != null ? vehiculo.getActualizadoEn() : vehiculo.getCreadoEn());
    }

    // =========================================================================
    // BÚSQUEDAS COMPLEJAS OPTIMIZADAS
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findDisponiblesByNombreTipo(String nombreTipo, Pageable pageable) {
        // Búsqueda inteligente: "Encuentra vehículos libres que sean de tipo 'X' (ej:
        // Furgón)".

        // ARQUITECTURA DE RENDIMIENTO (Push-down computation):
        // En lugar de traer miles de registros a la memoria RAM de Java y filtrarlos
        // con "streams" o "ifs",
        // delegamos TODO el trabajo (filtro por estado, filtro por texto LIKE, y
        // paginación)
        // directamente al motor de la base de datos (PostgreSQL/MySQL), que es mucho
        // más rápido para esto.

        return vehicleRepository.findByEstadoVehiculoAndActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
                EstadoVehiculo.DISPONIBLE, // 1er parámetro: Filtra estrictamente los que están libres.
                nombreTipo, // 2do parámetro: El texto parcial (LIKE %nombreTipo%). 'ContainingIgnoreCase'
                            // asegura que "furgon" encuentre "Furgón".
                pageable // 3er parámetro: Control de limit/offset (paginación) en SQL.
        ).map(dtoMapperVehicle::toDto); // Transforma el Page<Vehiculo> resultante a Page<VehicleResponse>.
    }

    // PATRÓN DE DISEÑO: Append-Only Log (Registro de solo adición). Método privado
    // que garantiza la inmutabilidad de la auditoría.
    // Este método actúa como la "caja negra" del sistema, asegurando que cada
    // cambio de estado quede registrado para siempre.
    private void registrarHistorial(Vehiculo vehiculo, EstadoVehiculo estadoAnterior, EstadoVehiculo estadoNuevo,
            String motivo, String servicioOrigen, String idCorrelacion) {

        // 1. INSTANCIACIÓN: Creamos un nuevo objeto de tipo HistorialEstadoVehiculo en
        // memoria.
        // Representa una nueva fila física que se insertará en la tabla de auditoría de
        // la base de datos.
        HistorialEstadoVehiculo historial = new HistorialEstadoVehiculo();

        // 2. VINCULACIÓN DE ENTIDAD: Relacionamos el historial directamente con el
        // vehículo protagonista del evento.
        // Esto crea la relación y la clave foránea (Foreign Key) necesaria a nivel de
        // base de datos relacional.
        historial.setVehiculo(vehiculo);

        // 3. OPERADOR TERNARIO: Extraemos el nombre de texto del estado anterior de
        // forma segura.
        // Si el estado anterior es nulo (como en el registro inicial), guarda null; de
        // lo contrario, guarda su equivalente en String.
        historial.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);

        // 4. ALMACENAMIENTO DE ESTADO DESTINO: Guardamos el nuevo estado operativo al
        // que transicionó el camión.
        // Al usar .name(), convertimos el Enum estricto de Java en un texto plano ideal
        // para almacenar en la columna SQL.
        historial.setEstadoNuevo(estadoNuevo.name());

        // 5. REGISTRO DE MOTIVACIÓN: Almacenamos el motivo o justificación técnica
        // provista por el operador o el sistema.
        // Ejemplo: "Cambio de pastillas de freno en el taller central".
        historial.setMotivoCambio(motivo);

        // 6. TRAZABILIDAD DE INFRAESTRUCTURA: Registramos el nombre del microservicio o
        // aplicación que disparó la acción.
        // Crucial en arquitecturas distribuidas para identificar si el cambio vino de
        // la app móvil, el dashboard o una tarea automática.
        historial.setServicioOrigen(servicioOrigen);

        // 7. PATRÓN DISTRIBUIDO: Guardamos el ID de correlación o ID de traza (Trace
        // ID) para el seguimiento de la petición.
        // Permite enlazar este cambio en la base de datos con los logs globales de
        // otros microservicios que participaron en el flujo.
        historial.setIdCorrelacion(idCorrelacion);

        // 8. ESTAMPA TEMPORAL: Capturamos la fecha y hora exacta del servidor en la que
        // se consolida el cambio de estado.
        // Es la métrica fundamental para calcular tiempos de inactividad (Downtime) y
        // realizar auditorías forenses.
        historial.setRegistradoEn(LocalDateTime.now());

        // 9. PERSISTENCIA: Invocamos al repositorio especializado para guardar de forma
        // definitiva este registro en PostgreSQL.
        // Ejecuta un comando INSERT inmutable que ningún usuario del sistema ordinario
        // debería poder modificar o borrar.
        historialEstadoRepository.save(historial);
    }

}