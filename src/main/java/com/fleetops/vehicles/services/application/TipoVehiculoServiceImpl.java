package com.fleetops.vehicles.services.application;
// Define la ubicación de esta clase dentro del proyecto (capa de servicios de aplicación).

// Ejemplo: Es como la carpeta física "Servicios" dentro del cajón "Vehículos" en un archivador.

import com.fleetops.vehicles.exception.DuplicateResourceException;
import com.fleetops.vehicles.exception.ResourceNotFoundException;
import com.fleetops.vehicles.mapper.DtoMapperTipoVehiculo;
import com.fleetops.vehicles.models.entities.TipoVehiculo;
import com.fleetops.vehicles.dto.request.TipoVehiculoRequest;
import com.fleetops.vehicles.dto.response.TipoVehiculoResponse;
import com.fleetops.vehicles.repositories.TipoVehiculoRepository;
import com.fleetops.vehicles.repositories.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/// @Slf4j: Genera automáticamente un logger de SLF4J para rastrear la ejecución del servicio en consola/logs.
@Slf4j
// @Service: Marca esta clase como un Bean de Spring, permitiendo su inyección
// en controladores.
@Service
// @RequiredArgsConstructor: Genera un constructor para todos los campos
// 'final', permitiendo inyección de dependencias por constructor.
@RequiredArgsConstructor
public class TipoVehiculoServiceImpl implements TipoVehiculoService {

    // Repositorio para gestionar el catálogo de tipos de vehículos.
    private final TipoVehiculoRepository tipoVehiculoRepository;

    // Repositorio para consultar los vehículos físicos (necesario para verificar
    // integridad al borrar).
    private final VehicleRepository vehicleRepository;

    // Mapper para transformar la entidad interna 'TipoVehiculo' al DTO
    // 'TipoVehiculoResponse'.
    private final DtoMapperTipoVehiculo dtoMapperTipoVehiculo;

    // ─────────────────────────────────────────────────────────────────────────────
    // MÉTODO: findAll (Listado paginado)
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    // @Transactional(readOnly = true): Optimiza la consulta indicando que no habrá
    // modificaciones, mejorando rendimiento.
    @Transactional(readOnly = true)
    public Page<TipoVehiculoResponse> findAll(Pageable pageable) {
        // Registro en log: Trazabilidad de la operación.
        log.info("Consultando catálogo de tipos de vehículo (paginado)");

        // Buscamos todas las categorías, paginamos los resultados y los mapeamos a
        // DTOs.
        return tipoVehiculoRepository.findAll(pageable)
                .map(dtoMapperTipoVehiculo::toDto);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MÉTODO: findById (Búsqueda única)
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public TipoVehiculoResponse findById(Long id) {
        // Log para saber qué ID se está consultando.
        log.info("Consultando tipo de vehículo por ID: {}", id);

        // Intentamos encontrar el tipo; si no existe, lanzamos excepción 404.
        TipoVehiculo tipo = tipoVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "id", id));

        // Retornamos el DTO transformado.
        return dtoMapperTipoVehiculo.toDto(tipo);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // MÉTODO: create (Creación de registro)
    // ─────────────────────────────────────────────────────────────────────────────
    @Override
    // @Transactional: Transacción de escritura. Si algo falla, se revierte.
    @Transactional
    public TipoVehiculoResponse create(TipoVehiculoRequest request) {
        // Log: Iniciando el proceso de creación.
        log.info("Iniciando creación de nuevo tipo de vehículo: {}", request.nombreTipo());

        // REGLA DE NEGOCIO: Validamos que el nombre no exista ya (Unicidad).
        if (tipoVehiculoRepository.existsByNombreTipoIgnoreCase(request.nombreTipo())) {
            log.warn("Intento de creación fallido: El tipo '{}' ya existe", request.nombreTipo());

            // Lanzamos excepción si el nombre está duplicado.
            throw new DuplicateResourceException("Tipo de Vehículo", "nombreTipo", request.nombreTipo());
        }

        // Creamos la nueva entidad.
        TipoVehiculo tipo = new TipoVehiculo();

        // Asignamos datos del request (Request DTO).
        tipo.setNombreTipo(request.nombreTipo());
        tipo.setDescripcion(request.descripcion());
        tipo.setCapacidadCarga(request.capacidadCarga());

        // Asignamos la fecha de creación.
        tipo.setCreadoEn(LocalDateTime.now());

        // Guardamos en la base de datos.
        TipoVehiculo guardado = tipoVehiculoRepository.save(tipo);

        // Log de éxito: confirmamos la creación con el ID generado.
        log.info("Tipo de vehículo creado exitosamente con ID: {}", guardado.getIdTipoVehiculo());

        // Retornamos el DTO correspondiente.
        return dtoMapperTipoVehiculo.toDto(guardado);
    }

    @Override
    // @Transactional: Marca este método como una transacción. Si algo falla durante
    // la edición,
    // los cambios no se guardan en la base de datos (evita datos corruptos).
    @Transactional
    public TipoVehiculoResponse update(Long id, TipoVehiculoRequest request) {
        // Método para editar un tipo de vehículo que ya existe.

        // Registra en consola el ID de la categoría que el administrador intenta
        // editar.
        log.info("Iniciando actualización del tipo de vehículo ID: {}", id);

        // Busca el tipo de vehículo en la base de datos.
        // Si no existe, lanzamos ResourceNotFoundException que el Controller convertirá
        // en un error 404.
        TipoVehiculo tipo = tipoVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "id", id));

        // REGLA DE NEGOCIO: Unicidad con auto-exclusión.
        // Verificamos si el nombre que el usuario quiere poner ya existe en OTRO
        // registro.
        // 1. !tipo.getNombreTipo().equalsIgnoreCase(...): Si el nombre es el mismo que
        // el actual, dejamos pasar.
        // 2. tipoVehiculoRepository.existsByNombreTipoIgnoreCase(...): Si el nombre es
        // nuevo, verificamos si ya existe en otro ID.
        // Ejemplo: Si edito "Furgón" y lo guardo como "Furgón", el sistema no lanza
        // error (es el mismo).
        // Pero si edito "Furgón" y lo cambio a "Camioneta" (que ya existe), el sistema
        // bloquea.
        if (!tipo.getNombreTipo().equalsIgnoreCase(request.nombreTipo()) &&
                tipoVehiculoRepository.existsByNombreTipoIgnoreCase(request.nombreTipo())) {

            // Registra la advertencia en consola para que los desarrolladores detecten el
            // conflicto.
            log.warn("Actualización fallida: El nombre '{}' ya está en uso", request.nombreTipo());

            // Lanza el error de recurso duplicado (409 Conflict), impidiendo el guardado.
            throw new DuplicateResourceException("Tipo de Vehículo", "nombreTipo", request.nombreTipo());
        }

        // Actualiza el nombre en la entidad con el valor recibido en el request.
        tipo.setNombreTipo(request.nombreTipo());

        // Actualiza la descripción del vehículo.
        tipo.setDescripcion(request.descripcion());

        // Actualiza la capacidad de carga (dato operativo).
        tipo.setCapacidadCarga(request.capacidadCarga());

        // Actualiza la marca de tiempo 'actualizadoEn' con la fecha y hora actual para
        // auditoría.
        tipo.setActualizadoEn(LocalDateTime.now());

        // Guarda los cambios en la base de datos mediante un UPDATE SQL.
        TipoVehiculo actualizado = tipoVehiculoRepository.save(tipo);

        // Confirma en la consola que la actualización terminó con éxito.
        log.info("Tipo de vehículo actualizado exitosamente | ID: {}", id);

        // Retorna el objeto actualizado mapeado a DTO para no exponer la entidad
        // interna al usuario final.
        return dtoMapperTipoVehiculo.toDto(actualizado);
    }

    @Override
    // @Transactional: Transacción de escritura. Si el borrado falla (por error de BD), se revierte.
    @Transactional
    public void delete(Long id) {
        // Método para eliminar un tipo de vehículo del catálogo.

        // Registra en consola el inicio del proceso de eliminación para auditoría técnica.
        log.info("Iniciando eliminación del tipo de vehículo ID: {}", id);

        // Busca el tipo en la BD. Si el ID no existe, lanza una excepción 404 (Not Found).
        TipoVehiculo tipo = tipoVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoVehiculo", "id", id));

        // REGLA DE NEGOCIO: Validamos si hay vehículos físicos usando esta categoría.
        // Contamos cuántos vehículos están marcados como 'activo = true' y vinculados a este tipo.
        long cantidadVehiculos = vehicleRepository.countByTipoVehiculoAndActivoTrue(tipo);

        // REGLA DE NEGOCIO: Integridad Referencial. 
        // Si hay vehículos activos, bloqueamos la eliminación para evitar dejar datos huérfanos.
        if (cantidadVehiculos > 0) {

            // Avisa en consola que el borrado fue bloqueado por seguridad.
            log.warn("No se puede eliminar el tipo de vehículo ID: {} porque tiene {} vehículos activos asociados", id,
                    cantidadVehiculos);

            // Lanza una excepción de negocio que el controlador atrapará para mostrar un error 400 o 409 al usuario.
            // Esto es mucho más profesional que permitir un error de clave foránea de la base de datos.
            throw new com.fleetops.vehicles.exception.BusinessException(
                    "No se puede eliminar el tipo de vehículo porque tiene vehículos activos asociados");
        }

        // Si la validación pasa (cantidadVehiculos == 0), procedemos al borrado físico (DELETE en SQL).
        tipoVehiculoRepository.delete(tipo);

        // Registra en consola el éxito de la operación.
        log.info("Tipo de vehículo eliminado exitosamente | ID: {}", id);
    }
}