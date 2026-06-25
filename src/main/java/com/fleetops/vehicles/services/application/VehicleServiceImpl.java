package com.fleetops.vehicles.services.application;
// Define la carpeta o paquete del sistema donde vive esta clase, encargada de la lógica principal de la aplicación.

import com.fleetops.vehicles.exception.BusinessException;
import com.fleetops.vehicles.exception.DuplicateResourceException;
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

import java.text.Normalizer;
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
        log.info("Consultando todos los vehículos paginados");

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
        log.info("Consultando vehículo por ID: {}", id);

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
        log.info("Consultando vehículo por placa: {}", placa);

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
        log.info("Consultando vehículos disponibles");

        // Filtra en BD los que están 'DISPONIBLE' y son 'Activos'.
        // Mapea el resultado a DTOs para el cliente.
        return vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.DISPONIBLE, pageable)
                .map(dtoMapperVehicle::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findReservados(Pageable pageable) {
        // Método que trae la lista paginada de vehículos que tienen un viaje asignado.

        // Registra la acción en la consola.
        log.info("Consultando vehículos reservados");

        // Filtra los que están 'RESERVADO' y son 'Activos'.
        return vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.RESERVADO, pageable)
                .map(dtoMapperVehicle::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findMantenimiento(Pageable pageable) {
        // Método que trae la lista paginada de vehículos que están en taller.

        // Registra la acción en la consola.
        log.info("Consultando vehículos en mantenimiento");

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
        log.info("Consultando vehículos fuera de servicio");

        // Filtra los que están 'FUERA_DE_SERVICIO' y son 'Activos'.
        return vehicleRepository.findAllByEstadoVehiculoAndActivoTrue(EstadoVehiculo.FUERA_DE_SERVICIO, pageable)
                .map(dtoMapperVehicle::toDto);
    }

   
@Override
    // @Transactional: Envuelve la operación en una transacción. Si algo falla (ej. error de base de datos), 
    // nada se guarda, evitando estados inconsistentes en la flota.
    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        // Log informativo: permite rastrear quién registró qué vehículo y cuándo en el historial del servidor.
        log.info("Iniciando registro de vehículo con placa: {}", request.numeroPlaca());

        // REGLA DE NEGOCIO: Validamos que los datos únicos (placa, chasis, motor) no existan ya.
        // Esto previene errores operativos y datos duplicados en el sistema.
        validarUnicidadParaCreacion(request);

        // BUSQUEDA DE RELACIÓN: Buscamos el catálogo de 'TipoVehiculo'.
        // Si el usuario envía un ID de tipo inexistente, lanzamos 404 inmediatamente.
        TipoVehiculo tipoVehiculo = tipoVehiculoRepository.findById(request.idTipoVehiculo())
                .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "id", request.idTipoVehiculo()));

        // CREACIÓN DE ENTIDAD: Creamos una hoja en blanco (objeto Vehiculo) para llenar.
        Vehiculo vehiculo = new Vehiculo();

        // PLACA: Forzamos a mayúsculas para mantener consistencia en búsquedas (normalización de datos).
        vehiculo.setNumeroPlaca(request.numeroPlaca().toUpperCase());

        // RELACIÓN: Vinculamos el vehículo a su categoría (TipoVehiculo).
        vehiculo.setTipoVehiculo(tipoVehiculo);

        // MAPPING: Copiamos los atributos técnicos desde el DTO al objeto entidad.
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnioFabricacion(request.anioFabricacion());
        vehiculo.setColor(request.color());
        vehiculo.setNumeroChasis(request.numeroChasis());
        vehiculo.setNumeroMotor(request.numeroMotor());
        vehiculo.setKilometraje(request.kilometraje());
        vehiculo.setCiudadOperacion(request.ciudadOperacion());
        vehiculo.setSedeOperacion(request.sedeOperacion());

        // MANEJO DE ESTADO (ENUM): Convertimos el String enviado a un Enum estricto.
        try {
            // Intentamos convertir el texto (ej. "DISPONIBLE") al Enum.
            vehiculo.setEstadoVehiculo(EstadoVehiculo.valueOf(request.estadoVehiculo().toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Si el estado enviado no coincide con ningún valor permitido, lanzamos excepción de negocio.
            throw new BusinessException("Estado de vehículo no válido: " + request.estadoVehiculo());
        }

        // FECHAS VENCIMIENTO: Guardamos documentos legales (SOAT, RTM) y último mantenimiento.
        vehiculo.setFechaSoat(request.fechaSoat());
        vehiculo.setFechaRtm(request.fechaRtm());
        vehiculo.setFechaUltimoMant(request.fechaUltimoMant());

        // ESTADO ACTIVO: Por defecto, un vehículo nuevo siempre está activo (true).
        vehiculo.setActivo(true);

        // FECHA CREACIÓN: Sellamos el momento exacto de la creación para auditoría.
        vehiculo.setCreadoEn(LocalDateTime.now());

        // PERSISTENCIA: Guardamos en la base de datos (INSERT).
        Vehiculo vehiculoGuardado = vehicleRepository.save(vehiculo);

        // AUDITORÍA (Historial): Registramos el nacimiento del vehículo en el libro de eventos.
        // Esto permite saber el "estado inicial" ante cualquier inspección futura.
        registrarHistorial(vehiculoGuardado, null, vehiculoGuardado.getEstadoVehiculo(),
                "Registro inicial del vehículo en el sistema", "fleetops-vehicles", null);

        // LOG ÉXITO: Confirmamos el ID generado en los logs.
        log.info("Vehículo registrado exitosamente con ID: {}", vehiculoGuardado.getIdVehiculo());

        // RETORNO: Convertimos la entidad a DTO y la enviamos al cliente.
        return dtoMapperVehicle.toDto(vehiculoGuardado);
    }

   @Override
    // @Transactional: Transacción de escritura. Garantiza que si la actualización falla a la mitad, 
    // la base de datos no quede con datos inconsistentes (Rollback).
    @Transactional
    public VehicleResponse update(UUID id, VehicleUpdateRequest request) {
        // Método que actualiza todos los datos de un vehículo existente.

        // Registra en consola el ID del vehículo que se va a editar para tener rastro en los logs.
        log.info("Iniciando actualización de vehículo ID: {}", id);

        // Busca el vehículo en la base de datos.
        // Si no existe, lanzamos ResourceNotFoundException para que el controlador devuelva un 404 al usuario.
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // REGLA DE NEGOCIO: Validar Unicidad con "Exclusión propia".
        // Esta regla verifica que, si cambiaste la placa, esa nueva placa no pertenezca a OTRO vehículo.
        // Es vital para no romper la integridad de la base de datos.
        validarUnicidadParaActualizacion(id, request);

        // Actualiza la placa y la fuerza a MAYÚSCULAS para mantener un formato estándar en la BD.
        vehiculo.setNumeroPlaca(request.numeroPlaca().toUpperCase());

        // Actualiza los datos técnicos (Marca, Modelo, Color) según el request.
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setColor(request.color());

        // Actualiza los datos identificadores únicos del motor y chasis.
        vehiculo.setNumeroChasis(request.numeroChasis());
        vehiculo.setNumeroMotor(request.numeroMotor());

        // Actualiza el kilometraje (dato operativo).
        vehiculo.setKilometraje(request.kilometraje());

        // Actualiza la ubicación geográfica de operación y la sede física.
        vehiculo.setCiudadOperacion(request.ciudadOperacion());
        vehiculo.setSedeOperacion(request.sedeOperacion());

        // Actualiza las fechas de cumplimiento normativo (SOAT y RTM).
        vehiculo.setFechaSoat(request.fechaSoat());
        vehiculo.setFechaRtm(request.fechaRtm());

        // Actualiza la fecha del último mantenimiento preventivo.
        vehiculo.setFechaUltimoMant(request.fechaUltimoMant());

        // Graba la hora exacta en la que se realizó esta edición (Audit Trail).
        // Muy importante para saber cuándo fue la última vez que alguien tocó este registro.
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // Impacta los cambios en la base de datos (Ejecuta un UPDATE SQL).
        Vehiculo vehiculoActualizado = vehicleRepository.save(vehiculo);

        // Imprime en consola el éxito de la operación.
        log.info("Vehículo actualizado exitosamente | ID: {}", id);

        // Retorna el vehículo actualizado convertido a DTO (VehicleResponse).
        // Esto oculta las entidades internas y envía al cliente solo lo permitido.
        return dtoMapperVehicle.toDto(vehiculoActualizado);
    }
    
    @Override
    // @Transactional: Garantiza que la búsqueda y la actualización se ejecuten como una única unidad atómica.
    @Transactional
    public VehicleResponse updateByPlaca(String placa, VehicleUpdateRequest request) {
        // Método que actúa como una fachada: permite actualizar buscando por placa en lugar de ID.

        // 1. Buscamos el vehículo por su placa actual.
        // Si no existe, ResourceNotFoundException dispara el 404.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // 2. REUTILIZACIÓN (Patrón Delegator):
        // Reutilizamos tu lógica central de 'update' pasándole el UUID que acabamos de obtener.
        // Esto evita tener que escribir la lógica de validación de unicidad dos veces.
        return this.update(vehiculo.getIdVehiculo(), request);
    }


    // =========================================================================
    // BORRADO LÓGICO (SOFT DELETE) POR UUID
    // =========================================================================

    @Override
    // @Transactional: Garantiza que la búsqueda, validación y actualización ocurran como una sola unidad atómica.
    @Transactional
    public boolean softDelete(UUID id) {
        // Implementación del Borrado Lógico (PATRÓN: Soft Delete). No elimina datos de la BD, solo los oculta.

        // Registro de auditoría: iniciamos el proceso de baja lógica para este ID.
        log.info("Iniciando baja lógica de vehículo ID: {}", id);

        // Busca el vehículo objetivo. Si el ID no existe en la BD, lanza un error 404.
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // Consulta si el vehículo está atado a alguna reserva pendiente.
        // Ejemplo: Evita borrar un camión que tiene un viaje agendado para mañana.
        boolean tieneReservaPendiente = reservaRepository.findReservaPendienteByVehiculoId(id).isPresent();

        // REGLA DE NEGOCIO: Prohíbe dar de baja el activo si tiene compromisos comerciales activos.
        if (tieneReservaPendiente) {
            // Log de advertencia: el borrado fue bloqueado por una restricción de negocio.
            log.warn("No se puede eliminar el vehículo ID: {} porque tiene reservas pendientes.", id);
            
            // Lanza una excepción de negocio que el controlador traducirá a un error explicativo (400/409).
            throw new BusinessException("No se puede dar de baja un vehículo con una reserva pendiente o activa.");
        }

        // Variable temporal para guardar el estado que tenía el vehículo antes de borrarlo.
        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        // BORRADO LÓGICO: Marcamos 'activo' como falso. 
        // A partir de este cambio, ninguna búsqueda normal en el sistema devolverá este vehículo.
        vehiculo.setActivo(false);

        // CAMBIO DE ESTADO: Forzamos el estado a "FUERA_DE_SERVICIO" por seguridad operativa.
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);

        // Guardamos la fecha y hora exacta del cambio para fines de auditoría.
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // Persistimos el cambio en la base de datos (UPDATE en SQL).
        vehicleRepository.save(vehiculo);

        // AUDITORÍA: Registramos en la bitácora la razón de la baja y quién la ejecutó.
        registrarHistorial(vehiculo, estadoAnterior, EstadoVehiculo.FUERA_DE_SERVICIO,
                "Baja lógica del vehículo del sistema operativo", "fleetops-vehicles", null);

        // Log de éxito: confirmamos que la baja lógica se completó.
        log.info("Vehículo dado de baja (borrado lógico) exitosamente | ID: {}", id);

        // Retornamos verdadero, confirmando el éxito de la operación.
        return true;
    }

    // =========================================================================
    // BORRADO LÓGICO (SOFT DELETE) POR PLACA
    // =========================================================================

    @Override
    @Transactional
    public void deleteByPlaca(String placa) {
        // Método de conveniencia para dar de baja un vehículo usando su placa.

        // 1. Buscamos el vehículo por su placa (verificando que esté activo actualmente).
        // Si no se encuentra, se lanza una excepción de recurso no encontrado.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // REGLA DE NEGOCIO: Protección de Integridad Operativa.
        // Solo permitimos dar de baja vehículos que estén en estado "DISPONIBLE".
        // Si está reservado, en mantenimiento o en ruta, se impide la acción para evitar errores humanos.
        if (!vehiculo.getEstadoVehiculo().name().equals("DISPONIBLE")) {
            throw new BusinessException(
                    "No se puede eliminar un vehículo que no esté DISPONIBLE. Estado actual: "
                            + vehiculo.getEstadoVehiculo());
        }

        // 2. PATRÓN APLICADO: Soft Delete.
        // Cambiamos el flag 'activo' a falso. El registro sigue en la BD, pero oculto al usuario.
        vehiculo.setActivo(false);
        // Marcamos la fecha de actualización.
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // 3. Guardamos el cambio en la base de datos.
        vehicleRepository.save(vehiculo);

        // Registro de auditoría: dejamos constancia del borrado lógico por placa en los logs.
        log.info("Vehículo con placa {} fue eliminado lógicamente (Soft Delete)", placa);
    }

    @Override
    // @Transactional: Transacción de escritura para asegurar que los cambios se guarden de forma íntegra.
    @Transactional
    public VehicleResponse reactivarVehiculo(UUID id, String motivo) {
        // Método que permite reincorporar un vehículo que había sido dado de baja lógica (oculto).

        // Registra el inicio del proceso de reactivación en los logs.
        log.info("Reactivando vehículo ID: {}", id);

        // Busca el vehículo en la base de datos (incluso si está oculto/inactivo).
        // Si no existe, lanza excepción 404.
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // Guarda temporalmente el estado previo para los reportes de auditoría (trazabilidad).
        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        // REGLA DE NEGOCIO: Reactivamos la bandera 'activo' para que sea visible en el sistema.
        vehiculo.setActivo(true);

        // REGLA DE NEGOCIO (Seguridad): Al revivir, pasa a "FUERA_DE_SERVICIO".
        // Esto obliga a que el equipo de mantenimiento realice una inspección antes de volver a operar.
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);

        // Actualiza el reloj de modificaciones para auditoría.
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // Persiste la reactivación en la base de datos (UPDATE SQL).
        Vehiculo guardado = vehicleRepository.save(vehiculo);

        // Concatenamos el motivo de la reactivación para la bitácora.
        String motivo_completo = "Reactivacion del vehiculo: " + motivo;

        // AUDITORÍA: Registra en el historial quién y por qué se reactivó el vehículo.
        // Esto es esencial para cumplir con normativas de seguridad de flota.
        registrarHistorial(guardado, estadoAnterior, EstadoVehiculo.FUERA_DE_SERVICIO, motivo_completo,
                "fleetops-vehicles",
                null);

        // Retorna el objeto reactivado convertido a formato DTO para el cliente.
        return dtoMapperVehicle.toDto(guardado);
    }

    @Override
    @Transactional
    public VehicleResponse reactivateByPlaca(String placa, String motivo) {
        // Método de conveniencia: busca el vehículo por placa (en la "papelera") y lo reactiva.

        // 1. Buscamos el vehículo específicamente en los registros inactivos (ActivoFalse).
        // Si no está ahí (o no existe), lanzamos un error de negocio.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoFalse(placa)
                .orElseThrow(
                        () -> new BusinessException("No se encontró ningún vehículo inactivo con la placa: " + placa));

        // Guardamos el estado anterior para la bitácora de auditoría.
        EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

        // 2. REGLA DE NEGOCIO: Lo reactivamos (activo=true) y forzamos a inspección (FUERA_DE_SERVICIO).
        vehiculo.setActivo(true);
        vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // 3. Guardamos los cambios en la base de datos.
        Vehiculo guardado = vehicleRepository.save(vehiculo);

        // Preparamos el motivo para el historial.
        String motivo_completo = "Reactivacion del vehiculo: " + motivo;

        // 4. AUDITORÍA: Escribimos en el libro de eventos quién lo revivió y por qué.
        registrarHistorial(guardado, estadoAnterior, EstadoVehiculo.FUERA_DE_SERVICIO, motivo_completo,
                "fleetops-vehicles",
                null);

        // Log de éxito.
        log.info("Vehículo con placa {} fue reactivado. Motivo: {}", placa, motivo);

        // 5. Retornamos el vehículo convertido a formato JSON (VehicleResponse).
        return dtoMapperVehicle.toDto(guardado);
    }

    @Override
    // @Transactional: Abre una transacción de escritura. Si la validación de estado falla, 
    // nada se guarda, garantizando que el vehículo no quede en un estado corrupto.
    @Transactional
    public VehicleResponse changeState(UUID id, String nuevoEstado, String motivoCambio, String servicioOrigen) {
        // Método central para transicionar un vehículo de un estado operativo a otro de manera controlada.

        // Registra en los logs el inicio de la operación para mantener la trazabilidad.
        log.info("Iniciando cambio de estado para vehículo ID: {}", id);

        // Busca el vehículo en la base de datos. Si no existe, lanza un error 404 (Not Found).
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // Captura el estado actual antes de modificarlo (necesario para validaciones y auditoría).
        EstadoVehiculo estadoActual = vehiculo.getEstadoVehiculo();

        // Variable para almacenar el estado al que deseamos transicionar.
        EstadoVehiculo estadoDestino;

        // MANEJO DE ENUMERADORES: 
        // Transformamos el String (JSON) enviado por el cliente al Enum estricto de Java.
        try {
            // .toUpperCase() evita fallos si el Frontend envía "disponible" en minúsculas.
            estadoDestino = EstadoVehiculo.valueOf(nuevoEstado.toUpperCase());

        } catch (IllegalArgumentException e) {
            // Si el texto enviado no existe en el Enum (ej: "ESTADO_INVENTADO"), 
            // lanzamos una excepción de negocio clara en lugar de un error 500 del servidor.
            throw new BusinessException("El estado proporcionado no es válido: " + nuevoEstado);
        }

        // REGLA DE NEGOCIO CRÍTICA: Validador de Transiciones (Finite State Machine).
        // Delegamos la lógica a un componente especializado que conoce las reglas del negocio.
        // Ejemplo: Si intentas pasar de "FUERA_DE_SERVICIO" directo a "RESERVADO", este método lanzará un error.
        stateTransitionValidator.validateTransition(estadoActual, estadoDestino);

        // Si la validación es exitosa (no arrojó error), aplicamos el nuevo estado al vehículo.
        vehiculo.setEstadoVehiculo(estadoDestino);

        // REGLA DE NEGOCIO: Automatización Operativa.
        // Si el vehículo sale exitosamente del taller, actualizamos automáticamente su bitácora técnica.
        if (estadoActual == EstadoVehiculo.EN_MANTENIMIENTO && estadoDestino == EstadoVehiculo.DISPONIBLE) {
            // Resetea la fecha de último mantenimiento al día de hoy, reduciendo la carga de datos manual.
            vehiculo.setFechaUltimoMant(LocalDateTime.now().toLocalDate());
        }

        // Sellamos el registro con la marca de tiempo exacta de esta modificación.
        vehiculo.setActualizadoEn(LocalDateTime.now());

        // Impactamos los cambios operativos en la base de datos (UPDATE).
        Vehiculo vehiculoActualizado = vehicleRepository.save(vehiculo);

        // AUDITORÍA (Audit Trail):
        // Registramos el evento detallado en la tabla de historial para que el equipo de operaciones 
        // sepa exactamente cuándo, por qué y desde dónde se realizó este cambio.
        registrarHistorial(vehiculoActualizado, estadoActual, estadoDestino, motivoCambio, servicioOrigen, null);

        // Log de éxito visualizando el salto de estado (ej: DISPONIBLE -> EN_MANTENIMIENTO).
        log.info("Cambio de estado exitoso: {} -> {} | Vehículo ID: {}", estadoActual, estadoDestino, id);

        // Retornamos el DTO actualizado al cliente para que la interfaz gráfica se refresque.
        return dtoMapperVehicle.toDto(vehiculoActualizado);
    }

    // =========================================================================
    // FACHADA DE CAMBIO DE ESTADO (BÚSQUEDA POR PLACA)
    // =========================================================================

    @Override
    // @Transactional: Extiende el contexto transaccional al método delegado.
    @Transactional
    public VehicleResponse updateEstadoByPlaca(String placa, EstadoCambioRequest request) {
        // Método de conveniencia (Wrapper): Permite cambiar el estado buscando por el identificador de la placa.

        // 1. Encuentra el vehículo activo cruzando su placa (ignorando mayúsculas/minúsculas).
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // 2. PATRÓN DE DISEÑO: DRY (Don't Repeat Yourself - No repitas código).
        // En lugar de reescribir toda la lógica de validación, delegamos el trabajo al método 'changeState', 
        // pasándole el ID interno que acabamos de resolver.
        return changeState(vehiculo.getIdVehiculo(), request.nuevoEstado(), request.motivoCambio(),
                request.servicioOrigen());
    }



    // =========================================================================
    // POLÍTICA DE DISPONIBILIDAD (DOMAIN POLICY)
    // =========================================================================

    // @Override: Indica que estamos implementando el contrato definido en VehicleService.
    @Override
    // @Transactional(readOnly = true): Optimiza la consulta en la base de datos al ser solo de lectura.
    // Evita bloqueos de tablas y mejora el rendimiento general del sistema.
    @Transactional(readOnly = true)
    public boolean isAvailable(UUID id) {
        // Método de consulta rápida (True/False) para saber si un vehículo puede ser asignado a un viaje o reserva.

        // Busca el vehículo por su ID. 
        // Si no se encuentra, detiene la ejecución y lanza una excepción que se traduce en un HTTP 404 (Not Found).
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // PATRÓN DE DISEÑO: Política de Dominio (Domain Policy / Specification).
        // En lugar de llenar este servicio de condicionales (if estado == DISPONIBLE && if SOAT_vigente),
        // delegamos la pregunta a una clase experta ('availabilityPolicy'). 
        // Esto permite que las reglas de negocio evolucionen sin tener que modificar esta clase.
        return availabilityPolicy.isAvailable(vehiculo);
    }

    // =========================================================================
    // CONSULTAS DE BITÁCORA DE AUDITORÍA (AUDIT TRAIL)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoResponse> getHistorialByVehiculoId(UUID id, Pageable pageable) {
        // Consulta la bitácora histórica de transiciones de estado para un vehículo específico.

        // PATRÓN DE DISEÑO: Fail-Fast (Falla rápido).
        // Usar 'existsById' es una consulta SQL ultra ligera ('SELECT 1 FROM...').
        // Validamos primero si el vehículo existe antes de lanzar la consulta pesada a la tabla de historiales.
        if (!vehicleRepository.existsById(id)) {
            // Si el vehículo no existe, arrojamos un 404 inmediatamente, ahorrando recursos del servidor.
            throw new ResourceNotFoundException("Vehículo", "id", id);
        }

        // Recupera el historial filtrando por el ID del vehículo.
        // 'OrderByRegistradoEnDesc' asegura un orden cronológico inverso (LIFO - el más reciente primero).
        return historialEstadoRepository.findByVehiculo_IdVehiculoOrderByRegistradoEnDesc(id, pageable)
                // Mapea la entidad de base de datos a un formato JSON limpio (DTO) para ocultar detalles técnicos al cliente.
                .map(dtoMapperHistorial::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialEstadoResponse> getHistorialByPlaca(String placa, Pageable pageable) {
        // Variante de consulta de bitácora que permite al usuario buscar por la placa del vehículo.

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
        // Devuelve el "CCTV" de la flota: el log global de absolutamente todos los movimientos de la empresa.
    
        // Consulta toda la tabla de historiales, ordenada desde el evento más reciente al más antiguo.
        // Ideal para alimentar un "Activity Feed" o panel de control en tiempo real para los administradores.
        return historialEstadoRepository.findAllByOrderByRegistradoEnDesc(pageable)
                // Mapea la información a un formato DTO ligero.
                .map(dtoMapperHistorial::toDto);
    }

   // =========================================================================
    // CONSULTAS ULTRA-LIGERAS (MICROSERVICIOS / UI RÁPIDA)
    // =========================================================================

    @Override
    // @Transactional(readOnly = true): Optimiza rendimiento, sin bloqueos de escritura.
    @Transactional(readOnly = true)
    public DisponibilidadResponse getDisponibilidad(UUID id) {
        // Retorna un DTO "ultra ligero" (ej. 3 campos). 
        // Ideal para que otro microservicio (ej. Módulo de Reservas) pregunte si un camión está disponible, 
        // sin necesidad de descargar toda la ficha técnica (marca, modelo, chasis, etc.).

        // Ubicamos la entidad en la base de datos.
        Vehiculo vehiculo = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "id", id));

        // PATRÓN DE DOMINIO: Calculamos la disponibilidad real (papeles vigentes + estado operativo).
        boolean disponible = availabilityPolicy.isAvailable(vehiculo);

        // Ensamblamos y retornamos el objeto de respuesta.
        // Se asume que 'DisponibilidadResponse' es un Java Record, lo cual es óptimo en memoria.
        return new DisponibilidadResponse(
                vehiculo.getIdVehiculo(),                 // 1. Identificador
                vehiculo.getEstadoVehiculo().name(),      // 2. Estado actual (String puro, no Enum)
                disponible,                               // 3. Resultado de la política (True/False)
                // 4. "Fallback" de auditoría: Si 'actualizadoEn' es null (nunca modificado), envía 'creadoEn'.
                vehiculo.getActualizadoEn() != null ? vehiculo.getActualizadoEn() : vehiculo.getCreadoEn()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponse getDisponibilidadByPlaca(String placa) {
        // Variante de consulta ligera, ideal para ser consumida por dispositivos GPS o tótems en portería 
        // donde se digita la placa del vehículo en lugar del ID del sistema.

        // Búsqueda insensible a mayúsculas/minúsculas y validando que el vehículo esté activo.
        Vehiculo vehiculo = vehicleRepository.findByNumeroPlacaIgnoreCaseAndActivoTrue(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo", "placa", placa));

        // Evaluación de reglas de negocio para determinar disponibilidad.
        boolean disponible = availabilityPolicy.isAvailable(vehiculo);

        // Retorno del DTO de bajo peso (Payload reducido en JSON).
        return new DisponibilidadResponse(
                vehiculo.getIdVehiculo(),
                vehiculo.getEstadoVehiculo().name(),
                disponible,
                vehiculo.getActualizadoEn() != null ? vehiculo.getActualizadoEn() : vehiculo.getCreadoEn()
        );
    }

    // =========================================================================
    // BÚSQUEDAS COMPLEJAS OPTIMIZADAS 
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findDisponiblesByNombreTipo(String nombreTipo, Pageable pageable) {
        // Búsqueda inteligente: "Encuentra vehículos libres que sean de tipo 'X' (ej: Furgón)".

        // ARQUITECTURA DE RENDIMIENTO (Push-down computation):
        // En lugar de traer miles de registros a la memoria RAM de Java y filtrarlos con "streams" o "ifs",
        // delegamos TODO el trabajo (filtro por estado, filtro por texto LIKE, y paginación)
        // directamente al motor de la base de datos (PostgreSQL/MySQL), que es mucho más rápido para esto.

        return vehicleRepository.findByEstadoVehiculoAndActivoTrueAndTipoVehiculo_NombreTipoContainingIgnoreCase(
                EstadoVehiculo.DISPONIBLE, // 1er parámetro: Filtra estrictamente los que están libres.
                nombreTipo,                // 2do parámetro: El texto parcial (LIKE %nombreTipo%). 'ContainingIgnoreCase' asegura que "furgon" encuentre "Furgón".
                pageable                   // 3er parámetro: Control de limit/offset (paginación) en SQL.
        ).map(dtoMapperVehicle::toDto);    // Transforma el Page<Vehiculo> resultante a Page<VehicleResponse>.
    }

    // PATRÓN DE DISEÑO: Append-Only Log (Registro de solo adición). Método privado que garantiza la inmutabilidad de la auditoría.
    // Este método actúa como la "caja negra" del sistema, asegurando que cada cambio de estado quede registrado para siempre.
    private void registrarHistorial(Vehiculo vehiculo, EstadoVehiculo estadoAnterior, EstadoVehiculo estadoNuevo,
                                    String motivo, String servicioOrigen, String idCorrelacion) {

        // 1. INSTANCIACIÓN: Creamos un nuevo objeto de tipo HistorialEstadoVehiculo en memoria.
        // Representa una nueva fila física que se insertará en la tabla de auditoría de la base de datos.
        HistorialEstadoVehiculo historial = new HistorialEstadoVehiculo();

        // 2. VINCULACIÓN DE ENTIDAD: Relacionamos el historial directamente con el vehículo protagonista del evento.
        // Esto crea la relación y la clave foránea (Foreign Key) necesaria a nivel de base de datos relacional.
        historial.setVehiculo(vehiculo);

        // 3. OPERADOR TERNARIO: Extraemos el nombre de texto del estado anterior de forma segura.
        // Si el estado anterior es nulo (como en el registro inicial), guarda null; de lo contrario, guarda su equivalente en String.
        historial.setEstadoAnterior(estadoAnterior != null ? estadoAnterior.name() : null);

        // 4. ALMACENAMIENTO DE ESTADO DESTINO: Guardamos el nuevo estado operativo al que transicionó el camión.
        // Al usar .name(), convertimos el Enum estricto de Java en un texto plano ideal para almacenar en la columna SQL.
        historial.setEstadoNuevo(estadoNuevo.name());

        // 5. REGISTRO DE MOTIVACIÓN: Almacenamos el motivo o justificación técnica provista por el operador o el sistema.
        // Ejemplo: "Cambio de pastillas de freno en el taller central".
        historial.setMotivoCambio(motivo);

        // 6. TRAZABILIDAD DE INFRAESTRUCTURA: Registramos el nombre del microservicio o aplicación que disparó la acción.
        // Crucial en arquitecturas distribuidas para identificar si el cambio vino de la app móvil, el dashboard o una tarea automática.
        historial.setServicioOrigen(servicioOrigen);

        // 7. PATRÓN DISTRIBUIDO: Guardamos el ID de correlación o ID de traza (Trace ID) para el seguimiento de la petición.
        // Permite enlazar este cambio en la base de datos con los logs globales de otros microservicios que participaron en el flujo.
        historial.setIdCorrelacion(idCorrelacion);

        // 8. ESTAMPA TEMPORAL: Capturamos la fecha y hora exacta del servidor en la que se consolida el cambio de estado.
        // Es la métrica fundamental para calcular tiempos de inactividad (Downtime) y realizar auditorías forenses.
        historial.setRegistradoEn(LocalDateTime.now());

        // 9. PERSISTENCIA: Invocamos al repositorio especializado para guardar de forma definitiva este registro en PostgreSQL.
        // Ejecuta un comando INSERT inmutable que ningún usuario del sistema ordinario debería poder modificar o borrar.
        historialEstadoRepository.save(historial);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE VALIDACIÓN DE INTEGRIDAD DE DATOS
    // =========================================================================

    // Método privado: Solo puede ser invocado desde dentro de esta misma clase Service.
    // Centraliza y encapsula las reglas estrictas de validación para nuevos registros ("CREATE").
    private void validarUnicidadParaCreacion(VehicleRequest request) {
        // REGLA DE NEGOCIO: Prevención absoluta de clones en el inventario físico.

        // 1. VALIDACIÓN DE PLACA: 
        // Consulta ágil a la BD (retorna un simple boolean, no la entidad completa).
        // 'IgnoreCase' asegura que "ABC123" y "abc123" se consideren el mismo registro, previniendo contaminación de datos.
        if (vehicleRepository.existsByNumeroPlacaIgnoreCase(request.numeroPlaca())) {
            // Si la placa ya existe, aborta la transacción inmediatamente lanzando un error 409 (Conflict).
            // Identifica exactamente qué campo causó el conflicto para retroalimentar al usuario en el Frontend.
            throw new DuplicateResourceException("Vehículo", "numeroPlaca", request.numeroPlaca());
        }

        // 2. VALIDACIÓN DE CHASIS (VIN):
        // Verifica la huella dactilar física del camión. Un chasis duplicado indica fraude o error de digitación grave.
        if (vehicleRepository.existsByNumeroChasisIgnoreCase(request.numeroChasis())) {
            // Detiene el flujo con una excepción clara y específica.
            throw new DuplicateResourceException("Vehículo", "numeroChasis", request.numeroChasis());
        }

        // 3. VALIDACIÓN DE MOTOR:
        // Protege la integridad legal del bloque del motor, evitando que dos vehículos compartan el mismo serial.
        if (vehicleRepository.existsByNumeroMotorIgnoreCase(request.numeroMotor())) {
            // Dispara el error detallando el campo exacto del conflicto.
            throw new DuplicateResourceException("Vehículo", "numeroMotor", request.numeroMotor());
        }
    }

    // Método privado que aplica el patrón de "Auto-Exclusión" vital para las ediciones ("UPDATE").
    private void validarUnicidadParaActualizacion(UUID idVehiculo, VehicleUpdateRequest request) {
        // REGLA DE NEGOCIO: Proteger contra duplicados, pero reconociendo la identidad propia del vehículo.
        // Permite que un vehículo "conserve" sus propios datos únicos sin que el sistema lance un falso error de duplicidad.

        // 1. VALIDACIÓN DE PLACA CON AUTO-EXCLUSIÓN:
        // Spring Data JPA traduce 'AndIdVehiculoNot' a SQL como: WHERE placa = ? AND id != ?
        // Esto significa: "¿Hay alguien MÁS en la base de datos con esta placa que NO sea yo?"
        if (vehicleRepository.existsByNumeroPlacaIgnoreCaseAndIdVehiculoNot(request.numeroPlaca(), idVehiculo)) {
            // Si la respuesta es sí (un tercero tiene la placa), lanzamos el error de colisión de datos.
            throw new DuplicateResourceException("Vehículo", "numeroPlaca", request.numeroPlaca());
        }

        // 2. VALIDACIÓN DE CHASIS CON AUTO-EXCLUSIÓN:
        // Verifica si el serial del chasis que intentamos guardar ya está apropiado por otro activo distinto en la flota.
        if (vehicleRepository.existsByNumeroChasisIgnoreCaseAndIdVehiculoNot(request.numeroChasis(), idVehiculo)) {
            // Lanza el rechazo de actualización por conflicto de identificador físico.
            throw new DuplicateResourceException("Vehículo", "numeroChasis", request.numeroChasis());
        }

        // 3. VALIDACIÓN DE MOTOR CON AUTO-EXCLUSIÓN:
        // Comprueba si el número de motor proporcionado ya está vinculado a un UUID de vehículo diferente.
        if (vehicleRepository.existsByNumeroMotorIgnoreCaseAndIdVehiculoNot(request.numeroMotor(), idVehiculo)) {
            // Falla la transacción indicando que el motor le pertenece a un tercero.
            throw new DuplicateResourceException("Vehículo", "numeroMotor", request.numeroMotor());
        }
    }
}