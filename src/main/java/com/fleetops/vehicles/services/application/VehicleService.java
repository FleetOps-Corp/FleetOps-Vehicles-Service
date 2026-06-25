package com.fleetops.vehicles.services.application;
// Ubicación lógica: Capa de aplicación de los vehículos.

import com.fleetops.vehicles.dto.request.EstadoCambioRequest;
import com.fleetops.vehicles.dto.request.VehicleRequest;
import com.fleetops.vehicles.dto.request.VehicleUpdateRequest;
import com.fleetops.vehicles.dto.response.DisponibilidadResponse;
import com.fleetops.vehicles.dto.response.HistorialEstadoResponse;
import com.fleetops.vehicles.dto.response.VehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

// PATRÓN DE DISEÑO APLICADO: Application Facade (Fachada de Aplicación).
// Actúa como una ventanilla única. Los controladores solo hablan con esta interfaz 
// y no con la base de datos directa.
public interface VehicleService {

    // Trae todos los vehículos del sistema separados en páginas.
    // Evita saturar la RAM del servidor solicitando bloques pequeños de datos.
    Page<VehicleResponse> findAll(Pageable pageable);

    // Lista todos los vehículos que han sido ocultados (borrado lógico).
    Page<VehicleResponse> getDeletedVehicles(int page, int size);

    // Busca la ficha técnica de un vehículo usando su código único (UUID).
    VehicleResponse findById(UUID id);

    // Busca un vehículo usando su placa, útil para lectura humana o sistemas externos (peajes, GPS).
    VehicleResponse findByPlaca(String placa);

    // Filtra la base de datos para traer únicamente vehículos disponibles para reserva.
    Page<VehicleResponse> findDisponibles(Pageable pageable);

    // Filtra para traer los vehículos bloqueados por una reserva activa.
    Page<VehicleResponse> findReservados(Pageable pageable);

    // Filtra para traer los vehículos que están en taller.
    Page<VehicleResponse> findMantenimiento(Pageable pageable);

    // Filtra para traer los vehículos que están fuera de servicio por avería o baja técnica.
    Page<VehicleResponse> findFueraServicio(Pageable pageable);

    // Matricula un camión nuevo.
    // REGLA: Valida unicidad de placa/chasis y vigencia de SOAT antes de guardar.
    VehicleResponse create(VehicleRequest request);

    // Edita los datos de un camión existente.
    // REGLA: Bloquea cambios si el vehículo tiene viajes programados para evitar inconsistencias.
    VehicleResponse update(UUID id, VehicleUpdateRequest request);

    // Actualiza la ficha técnica de un vehículo buscando por su placa (versión práctica para operadores).
    VehicleResponse updateByPlaca(String placa, VehicleUpdateRequest request);

    // Transiciona el vehículo entre estados (ej: Disponible -> Mantenimiento).
    // REGLA: Máquina de estados (no permite saltos ilógicos, como de 'Destruido' a 'Reservado').
    VehicleResponse changeState(UUID id, String nuevoEstado, String motivoCambio, String servicioOrigen);

    // Idem al anterior, pero usando placa para identificar al activo.
    VehicleResponse updateEstadoByPlaca(String placa, EstadoCambioRequest request);

    // Borrado Lógico. Oculta el camión del sistema sin perder su historial.
    // REGLA: Prohibido si el vehículo tiene una reserva activa (integridad referencial).
    boolean softDelete(UUID id);

    // Borrado lógico buscando por placa.
    void deleteByPlaca(String placa);

    // Reincorpora a la flota un camión dado de baja.
    // REGLA: Al revivir, el camión entra en 'Fuera de Servicio' obligatoriamente para inspección.
    VehicleResponse reactivarVehiculo(UUID id, String motivo);

    // Reincorpora un vehículo dado de baja buscando por su placa.
    VehicleResponse reactivateByPlaca(String placa, String motivo);

    // Validador rápido: Verifica si el camión es apto para operar.
    boolean isAvailable(UUID id);

    // Extrae la bitácora de auditoría de un vehículo (de más nuevo a más viejo).
    Page<HistorialEstadoResponse> getHistorialByVehiculoId(UUID id, Pageable pageable);

    // Bitácora de auditoría buscando por placa.
    Page<HistorialEstadoResponse> getHistorialByPlaca(String placa, Pageable pageable);

    // Historial global de todos los vehículos (para reportes de flota nacional).
    Page<HistorialEstadoResponse> findAllHistorialGlobal(Pageable pageable);

    // Consulta ultraligera: Solo devuelve estado (Disponible/No disponible). 
    // Ideal para optimizar rendimiento en pantallas donde no se necesitan detalles técnicos.
    DisponibilidadResponse getDisponibilidad(UUID id);

    // Idem al anterior, pero usando placa.
    DisponibilidadResponse getDisponibilidadByPlaca(String placa);

    // Búsqueda inteligente: Texto libre que ignora mayúsculas/tildes. 
    // Facilita la experiencia de usuario (ej: buscar 'camion' y encontrar 'Camión Frigorífico').
    Page<VehicleResponse> findDisponiblesByNombreTipo(String nombreTipo, Pageable pageable);
}