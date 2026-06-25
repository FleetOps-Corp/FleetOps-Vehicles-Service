package com.fleetops.vehicles.controllers;
// Ubicación lógica del archivo. Esta es la Capa de Presentación (Web Layer) de nuestro microservicio.

// Su función es recibir peticiones HTTP, traducirlas y enviarlas al "Cerebro" (Capa de Servicios).

import com.fleetops.vehicles.dto.request.*;
import com.fleetops.vehicles.dto.response.*;
import com.fleetops.vehicles.models.entities.EstadoReserva;
import com.fleetops.vehicles.models.entities.ReservaVehiculo;
import com.fleetops.vehicles.services.application.SagaService;
import com.fleetops.vehicles.services.application.TipoVehiculoService;
import com.fleetops.vehicles.services.application.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

// =========================================================================
// PATRÓN DE ARQUITECTURA: REST Controller Facade (Fachada)
// PRINCIPIO SOLID APLICADO: Single Responsibility Principle (Responsabilidad Única).
// El controlador NUNCA ejecuta reglas de negocio ni interactúa con la base de datos.
// Su único trabajo es "Enrutar": tomar el JSON de entrada, pasarlo al servicio adecuado 
// y devolver un JSON de salida con un código HTTP correcto (200, 201, 400).
// =========================================================================

// @RestController: Marca esta clase como un controlador especializado en APIs REST (devuelve JSON, no vistas HTML).
@RestController
// @RequestMapping: Define el "Prefijo" de la URL. Todos los endpoints aquí
// responderán bajo el path "/vehiculos".
@RequestMapping("/vehiculos")
// @RequiredArgsConstructor: Inyección de Dependencias. Spring provee
// automáticamente los servicios al instanciar la clase.
@RequiredArgsConstructor
// @Tag: Documentación automática OpenAPI (Swagger). Organiza visualmente esta
// API bajo un título claro para los desarrolladores Frontend.
@Tag(name = "Vehículos y Reservas", description = "API central para la gestión operativa de la flota, catálogos técnicos y orquestación de Sagas.")
public class VehicleController {

    // Dependencia: "El cerebro" para las reglas de negocio de los camiones/activos
    // físicos.
    private final VehicleService vehicleService;

    // Dependencia: "El orquestador" para coordinar transacciones distribuidas
    // (Sagas) en reservas complejas.
    private final SagaService sagaService;

    // Dependencia: "El catálogo maestro" para gestionar las dimensiones y tipos
    // técnicos (Bus, Furgón, etc.).
    private final TipoVehiculoService tipoVehiculoService;

    // =========================================================================
    // CATÁLOGO: TIPOS DE VEHÍCULO (ENDPOINTS DE CONFIGURACIÓN MAESTRA)
    // REGLA DE ARQUITECTURA: Normalización de Datos (3FN). Estos endpoints
    // alimentan
    // las listas desplegables del Frontend ("Seleccione un tipo de vehículo").
    // =========================================================================

    // Endpoint: POST /vehiculos/tipos-vehiculo
    @PostMapping("/tipos-vehiculo")
    // SEGURIDAD: Control de Acceso Basado en Roles (RBAC).
    // Solo usuarios con el token JWT que contenga el rol 'ADMIN' pueden crear
    // nuevas categorías.
    @PreAuthorize("hasRole('ADMIN')")
    // DOCUMENTACIÓN SWAGGER: Describe qué hace la ruta para el equipo consumidor.
    @Operation(summary = "Crear nuevo catálogo", description = "Añade una sub-categoría madre a la plataforma logística general.")
    // @Valid: Activa las validaciones definidas en el DTO (ej: @NotBlank, @Min)
    // ANTES de entrar al método.
    // ResponseEntity<T>: Es la forma profesional de devolver datos en Spring,
    // permite controlar el código HTTP.
    public ResponseEntity<TipoVehiculoResponse> createTipoVehiculo(@Valid @RequestBody TipoVehiculoRequest request) {
        // Ejecuta el servicio y envuelve la respuesta en un código HTTP 201 (CREATED),
        // que es el estándar para 'POST'.
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoVehiculoService.create(request));
    }

    // Endpoint: PUT /vehiculos/tipos-vehiculo/{id}
    @PutMapping("/tipos-vehiculo/{id}")
    @PreAuthorize("hasRole('ADMIN')") // RBAC: Solo administradores pueden modificar especificaciones técnicas.
    @Operation(summary = "Actualizar capacidades del catálogo", description = "Modifica atributos dimensionales propagando indirectamente el sentido hacia sus activos heredados.")
    // @PathVariable: Extrae el '{id}' de la URL (ej: /vehiculos/tipos-vehiculo/5).
    // @RequestBody: Convierte el JSON recibido en un objeto de Java.
    public ResponseEntity<TipoVehiculoResponse> updateTipoVehiculo(@PathVariable Long id,
            @Valid @RequestBody TipoVehiculoRequest request) {
        // Ejecuta la actualización y devuelve HTTP 200 (OK) con los datos nuevos.
        return ResponseEntity.ok(tipoVehiculoService.update(id, request));
    }

    // Endpoint: DELETE /vehiculos/tipos-vehiculo/{id}
    @DeleteMapping("/tipos-vehiculo/{id}")
    @PreAuthorize("hasRole('ADMIN')") // RBAC: Acción destructiva reservada a perfiles gerenciales.
    @Operation(summary = "Suprimir línea de catálogo", description = "Gatilla un protocolo de validación cruzada prohibiendo el borrado de categorías con vehículos vinculados.")
    public ResponseEntity<Void> deleteTipoVehiculo(@PathVariable Long id) {
        // REGLA DE NEGOCIO EN ACCIÓN: El servicio tipoVehiculoService.delete() lanzará
        // un error interno
        // si se rompe la Integridad Referencial (hay camiones usando la categoría).
        tipoVehiculoService.delete(id);

        // Si el borrado fue exitoso, devuelve HTTP 204 (NO CONTENT).
        // Este es el estándar REST: "La operación se hizo, pero no tengo nada que
        // devolverte".
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // CONSULTAS DE CATÁLOGO (LECTURA DE TIPOS DE VEHÍCULO)
    // =========================================================================

    // Endpoint: GET /vehiculos/tipos-vehiculo
    @GetMapping("/tipos-vehiculo")
    // SEGURIDAD: Control de acceso amplio.
    // A diferencia de la creación/edición, listar el catálogo es una operación
    // segura.
    // Se permite el acceso a usuarios estándar, operadores de patio y
    // administradores.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    @Operation(summary = "Listar catálogo", description = "Extrae las tipologías funcionales del sistema (ej. Furgón, Camioneta) de forma paginada para menús desplegables.")
    // Pageable de Spring Boot intercepta automáticamente los parámetros de la URL
    // (?page=0&size=10&sort=nombreTipo).
    public ResponseEntity<Page<TipoVehiculoResponse>> findAllTiposVehiculo(Pageable pageable) {
        // Llama al servicio y devuelve la lista paginada con un código HTTP 200 (OK).
        return ResponseEntity.ok(tipoVehiculoService.findAll(pageable));
    }

    // Endpoint: GET /vehiculos/tipos-vehiculo/{id}
    @GetMapping("/tipos-vehiculo/{id}")
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    @Operation(summary = "Consultar tipología específica", description = "Devuelve los detalles técnicos (capacidad de carga, descripción) asociados a una categoría específica.")
    // @PathVariable: Vincula la variable 'id' en la URL (ej: /tipos-vehiculo/3) al
    // parámetro de la función.
    public ResponseEntity<TipoVehiculoResponse> findTipoVehiculoById(@PathVariable Long id) {
        // Ejecuta la búsqueda puntual y devuelve el DTO con HTTP 200 (OK).
        // Si el ID no existe, el GlobalExceptionHandler (ControllerAdvice) capturará el
        // error del servicio y devolverá un 404 automáticamente.
        return ResponseEntity.ok(tipoVehiculoService.findById(id));
    }

    // =========================================================================
    // CONSULTAS ANIDADAS: VEHÍCULOS POR TIPO (BÚSQUEDA CRUZADA)
    // =========================================================================

    // Endpoint: GET /vehiculos/disponibles/tipo/nombre/{nombreTipo}
    // REGLA DE DISEÑO REST: Uso de URLs descriptivas.
    // Aunque este método devuelve vehículos, está filtrando por una propiedad del
    // catálogo ("tipo").
    @GetMapping("/disponibles/tipo/nombre/{nombreTipo}")
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    @Operation(summary = "Buscar vehículos disponibles filtrados por categoría", description = "Busca vehículos físicos listos para operar mediante coincidencia flexible de texto (ej. 'furgon'). Retorna resultados paginados.")
    public ResponseEntity<Page<VehicleResponse>> getDisponiblesByNombreTipo(
            @PathVariable String nombreTipo,
            Pageable pageable) {

        // Llama al servicio de vehículos (vehicleService) para realizar la búsqueda
        // optimizada en base de datos.
        // Retorna la página exacta de los vehículos aptos (HTTP 200).
        return ResponseEntity.ok(vehicleService.findDisponiblesByNombreTipo(nombreTipo, pageable));
    }

    // =====================================================
    // ==================== VEHÍCULOS =====================
    // =====================================================

    // @PostMapping: Anotación de Spring que mapea las peticiones HTTP de tipo POST
    // a este método.
    // Se utiliza exclusivamente para la creación de nuevos recursos en el sistema.
    @PostMapping
    // @PreAuthorize: Filtro de seguridad que evalúa el token JWT antes de permitir
    // la ejecución del método.
    // REGLA DE SEGURIDAD: Restringe el acceso únicamente a usuarios con el rol
    // institucional 'ADMIN'.
    // Si un usuario con rol 'OPERADOR' o 'CONDUCTOR' intenta consumir este
    // endpoint, el sistema retornará un HTTP 403 (Forbidden).
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Anotación de OpenAPI/Swagger para la documentación interactiva de
    // la API.
    // summary: Define un título corto para la acción. description: Detalla el
    // comportamiento técnico y de negocio.
    @Operation(summary = "Registrar un vehículo", description = "Crea un nuevo vehículo en la flota tras validar reglas de unicidad documental. Requiere rol ADMIN.")
    // public: Modificador de acceso para que Spring Web pueda invocar el método de
    // forma remota.
    // ResponseEntity<VehicleResponse>: Envoltura profesional que controla el cuerpo
    // del JSON y el código de estado HTTP.
    // @Valid: Activa el motor de validación de Jakarta Bean Validation para
    // comprobar las restricciones dentro del DTO (@NotBlank, @NotNull).
    // @RequestBody: Indica a Spring que debe deserializar el JSON del cuerpo de la
    // petición HTTP directamente en el objeto de Java 'VehicleRequest'.
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {

        // Invocación a la capa lógica: Enviamos el DTO al servicio especializado para
        // aplicar las reglas de negocio.
        // El servicio validará la vigencia de los documentos (SOAT/RTM) y la unicidad
        // de placa, chasis y motor antes de persistir.
        VehicleResponse created = vehicleService.create(request);

        // return: Retorna la respuesta estructurada hacia la red.
        // ResponseEntity.status(HttpStatus.CREATED): Establece explícitamente el código
        // de estado HTTP 201 (Created),
        // que es el estándar internacional para indicar que un recurso fue creado
        // exitosamente.
        // .body(created): Adjunta el DTO de respuesta que contiene el ID generado y los
        // datos limpios en formato JSON.
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // @PutMapping("/{id}"): Mapea peticiones HTTP de tipo PUT a la ruta base
    // combinada con una variable de ruta dinámica.
    // El formato PUT se utiliza bajo el estándar REST para reemplazar o actualizar
    // de forma completa un recurso existente.
    @PutMapping("/{id}")
    // @PreAuthorize: Garantiza que solo los perfiles de administración con rol
    // 'ADMIN' tengan permitido modificar datos maestros.
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Agrega metadatos para la consola de Swagger, explicando los
    // límites operativos de la actualización.
    @Operation(summary = "Actualizar ficha de vehículo", description = "Reemplaza los datos operacionales de un vehículo. Impide cambiar la placa si existen compromisos activos.")
    // @PathVariable UUID id: Extrae el identificador único de tipo UUID
    // directamente de la URL (ej: /vehiculos/a1b2c3d4-...) y lo asigna a la
    // variable.
    // @Valid @RequestBody: Comprueba que los campos del payload cumplan con las
    // reglas sintácticas antes de mapearlos al DTO de actualización.
    public ResponseEntity<VehicleResponse> update(@PathVariable UUID id,
            @Valid @RequestBody VehicleUpdateRequest request) {

        // Ejecuta la actualización en el servicio de aplicación y encapsula el
        // resultado en un ResponseEntity.
        // El servicio implícitamente disparará un HTTP 404 si el UUID no existe, o un
        // 409 si la nueva placa colisiona con la de un tercero.
        // ResponseEntity.ok(...): Retorna un código de estado HTTP 200 (OK), indicando
        // que el proceso de edición fue exitoso.
        return ResponseEntity.ok(vehicleService.update(id, request));
    }

    // @PutMapping("/placa/{placa}"): Define una ruta alternativa para la
    // actualización utilizando la placa como llave natural en la URL.
    // Útil para integraciones con terminales de patio o dispositivos periféricos
    // que leen placas físicas en lugar de IDs de sistema.
    @PutMapping("/placa/{placa}")
    // @PreAuthorize: Restringe la acción destructiva o modificatoria exclusivamente
    // al perfil 'ADMIN'.
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Documenta el endpoint en Swagger detallando que busca mitigar
    // colisiones o duplicidades accidentales de datos.
    @Operation(summary = "Actualizar ficha de vehículo por placa", description = "Reemplaza los datos operacionales de un vehículo buscando por su placa. Impide colisiones de datos.")
    // @PathVariable String placa: Captura la cadena de texto de la placa desde la
    // URL (ej: /vehiculos/placa/XYZ123).
    public ResponseEntity<VehicleResponse> updateByPlaca(
            @PathVariable String placa,
            @Valid @RequestBody VehicleUpdateRequest request) {

        // Delega la petición al servicio, el cual resolverá la placa de forma
        // insensible a mayúsculas y minúsculas.
        // Reutiliza la lógica interna de validación mediante el patrón Delegator y
        // devuelve un HTTP 200 (OK) con el JSON fresco.
        return ResponseEntity.ok(vehicleService.updateByPlaca(placa, request));
    }

    // @DeleteMapping("/{id}"): Indica que este endpoint responde a peticiones HTTP
    // de tipo DELETE para remover recursos.
    // Arquitectónicamente mapea la baja operativa mediante un identificador UUID.
    @DeleteMapping("/{id}")
    // @PreAuthorize: Control de acceso estricto. El borrado o inactivación de
    // activos de la empresa requiere credenciales de nivel 'ADMIN'.
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Registra formalmente en Swagger que este proceso no destruye
    // información física, sino que gatilla un borrado lógico.
    @Operation(summary = "Baja lógica de vehículo (Soft-Delete)", description = "Oculta un vehículo de la operación diaria conservando su histórico. Protegido por integridad de reservas.")
    // ResponseEntity<Void>: Retorna una respuesta HTTP sin cuerpo en el JSON, ya
    // que la acción de borrado no requiere devolver datos.
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {

        // Se invoca el método del servicio encargado del ocultamiento.
        // El servicio internamente validará que el vehículo no posea contratos o
        // reservas comerciales pendientes antes de apagarlo.
        boolean deleted = vehicleService.softDelete(id);

        // Operador Ternario para la gestión limpia de flujos alternos:
        // Si 'deleted' es true, construye un ResponseEntity con estado HTTP 204 (No
        // Content), indicando éxito total sin datos de retorno.
        // Si 'deleted' es false, construye un estado HTTP 404 (Not Found) notificando
        // que el recurso objetivo no existía en el inventario.
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // @DeleteMapping("/placa/{placa}"): Mapea la petición de eliminación utilizando
    // la placa visible del automotor.
    // Facilita la operación manual por parte de supervisores que gestionan flotas
    // desde consolas CLI o terminales rápidas.
    @DeleteMapping("/placa/{placa}")
    // @PreAuthorize: Asegura el perímetro de la API restringiendo el borrado lógico
    // únicamente a administradores autorizados.
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Notifica en Swagger la restricción crítica de negocio: el activo
    // debe estar estrictamente libre o disponible para ser apagado.
    @Operation(summary = "Borrado lógico por placa", description = "Inactiva un vehículo buscando por su placa. Solo permitido si el vehículo está DISPONIBLE.")
    public ResponseEntity<Void> deleteByPlaca(@PathVariable String placa) {

        // Transmite la orden de apagado lógico directo a la capa lógica empresarial.
        // Si el estado operativo del camión es diferente de 'DISPONIBLE', el servicio
        // interrumpirá el flujo lanzando una excepción de negocio.
        vehicleService.deleteByPlaca(placa);

        // .build(): Consolida la respuesta definitiva enviando un HTTP 204 (No Content)
        // hacia el Frontend.
        // Este código confirma al cliente que la operación concluyó exitosamente y que
        // puede refrescar las tablas visuales de la interfaz.
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // CONSULTAS DE INVENTARIO Y RECUPERACIÓN DE ACTIVOS (READ)
    // =========================================================================

    // @GetMapping: Mapea peticiones HTTP de tipo GET hacia la raíz de este
    // controlador (/vehiculos).
    // Su semántica REST indica que es una operación idempotente y segura (solo lee,
    // no modifica datos).
    @GetMapping
    // @PreAuthorize: Implementa seguridad de lectura abierta a múltiples roles
    // operativos.
    // A diferencia de la escritura, la visualización de la flota está permitida
    // para personal operativo y administrativo.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta el contrato de la API en Swagger para los
    // desarrolladores cliente (Frontend/Mobile).
    @Operation(summary = "Listar todos los vehículos", description = "Retorna una lista paginada del catálogo completo de vehículos activos.")
    // ResponseEntity<Page<VehicleResponse>>: Envuelve la respuesta en una
    // estructura HTTP que soporta metadatos de paginación.
    // Pageable pageable: Spring Boot captura automáticamente los Query Params de la
    // URL (ej: ?page=0&size=10) y los inyecta aquí.
    public ResponseEntity<Page<VehicleResponse>> findAll(Pageable pageable) {

        // PATRÓN DE DISEÑO: Paginación a nivel de Presentación.
        // Pasa el objeto de control de página directo al servicio para que la consulta
        // SQL use LIMIT y OFFSET.
        // Retorna HTTP 200 (OK) asegurando que el servidor nunca colapse por
        // desbordamiento de memoria RAM (OOM) al consultar flotas masivas.
        return ResponseEntity.ok(vehicleService.findAll(pageable));
    }

    // @GetMapping("/inactivos"): Define una sub-ruta específica para auditar el
    // "cementerio" o papelera de vehículos.
    @GetMapping("/inactivos")
    // @PreAuthorize: Restringe la visualización de activos dados de baja únicamente
    // al perfil gerencial/administrador.
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Detalla en la documentación interactiva que este endpoint expone
    // el registro de borrados lógicos.
    @Operation(summary = "Listar vehículos inactivos", description = "Muestra un listado paginado de los vehículos que han sido dados de baja (Soft Delete).")
    // @RequestParam: Extrae variables explícitas de la URL. Si el cliente no las
    // envía, asume los valores por defecto (Página 0, Tamaño 10).
    public ResponseEntity<Page<VehicleResponse>> listarInactivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Delega la recuperación del inventario inactivo al servicio de aplicación.
        // Envuelve el resultado en un código de estado HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.getDeletedVehicles(page, size));
    }

    // =========================================================================
    // BÚSQUEDAS ESPECÍFICAS (POR ID Y PLACA)
    // =========================================================================

    // @GetMapping("/{id}"): Captura peticiones GET directas a un recurso específico
    // mediante su llave primaria en la URL.
    @GetMapping("/{id}")
    // @PreAuthorize: Mantiene la política de lectura transversal para los roles
    // operativos.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Clarifica que este endpoint devuelve la "ficha técnica" o detalle
    // en profundidad de un solo automotor.
    @Operation(summary = "Consultar vehículo por ID", description = "Obtiene la ficha técnica completa de un vehículo mediante su identificador interno (UUID).")
    // @PathVariable UUID id: Transforma dinámicamente el segmento de la ruta en un
    // objeto UUID de Java de forma segura.
    public ResponseEntity<VehicleResponse> findById(@PathVariable UUID id) {

        // Ejecuta la búsqueda de alta precisión por ID.
        // Si el ID no existe, el servicio lanzará una excepción que el ControllerAdvice
        // convertirá en HTTP 404.
        // Si tiene éxito, retorna HTTP 200 (OK) con el JSON del vehículo.
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    // @GetMapping("/placa/{placa}"): Habilita la búsqueda natural. Los operadores
    // humanos piensan en placas, no en UUIDs.
    @GetMapping("/placa/{placa}")
    // @PreAuthorize: Accesible por los roles base para facilitar consultas rápidas
    // en porterías o terminales de despacho.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Anuncia en Swagger la disponibilidad de esta consulta acelerada.
    @Operation(summary = "Consultar vehículo por placa", description = "Localiza rápidamente la información de un vehículo usando su número de placa.")
    // @PathVariable String placa: Captura el parámetro alfanumérico (ej:
    // /vehiculos/placa/XYZ-789).
    public ResponseEntity<VehicleResponse> findByPlaca(@PathVariable String placa) {

        // Llama a la lógica de negocio especializada en resolver placas (insensible a
        // mayúsculas) y responde HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.findByPlaca(placa));
    }

    // =========================================================================
    // LOGÍSTICA INVERSA: REACTIVACIÓN DE ACTIVOS (UNDO SOFT-DELETE)
    // =========================================================================

    // @PostMapping("/{id}/reactivar"): Utiliza el verbo POST porque la reactivación
    // es una acción que altera el estado del sistema,
    // y no encaja puramente en la semántica de un PUT (reemplazo) o PATCH
    // (modificación parcial simple).
    @PostMapping("/{id}/reactivar")
    // @PreAuthorize: Seguridad restrictiva. Revivir un vehículo es una decisión
    // administrativa de alto impacto (requiere 'ADMIN').
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Documenta el efecto colateral de seguridad: el vehículo no vuelve
    // a estar DISPONIBLE, sino que pasa a FUERA_DE_SERVICIO.
    @Operation(summary = "Reincorporar vehículo a la flota", description = "Levanta un vehículo del archivo histórico. Obliga una transición hacia FUERA_DE_SERVICIO para forzar revisión mecánica.")
    // @RequestParam String motivo: Obliga al cliente a enviar un motivo
    // justificante en la URL (?motivo=...) para la auditoría forense.
    public ResponseEntity<VehicleResponse> reactivar(@PathVariable UUID id, @RequestParam String motivo) {

        // Ejecuta el flujo transaccional de reactivación que actualizará banderas de
        // estado y grabará el historial.
        // Retorna HTTP 200 (OK) con la nueva estructura del vehículo revivido.
        return ResponseEntity.ok(vehicleService.reactivarVehiculo(id, motivo));
    }

    // @PostMapping("/placa/{placa}/reactivar"): Expone la misma funcionalidad
    // crítica pero resolviendo el activo mediante su placa comercial.
    @PostMapping("/placa/{placa}/reactivar")
    // @PreAuthorize: Mantiene el cerco de seguridad perimetral solo para el rol
    // 'ADMIN'.
    @PreAuthorize("hasRole('ADMIN')")
    // @Operation: Explica en la documentación que este endpoint facilita la
    // logística inversa para administradores en campo.
    @Operation(summary = "Reactivar vehículo inactivo por placa", description = "Vuelve a poner operativo un vehículo exigiendo un motivo. Pasa a FUERA_DE_SERVICIO para revisión.")
    public ResponseEntity<VehicleResponse> reactivateByPlaca(
            @PathVariable String placa,
            @RequestParam String motivo) {

        // Delega la ejecución de la reactivación lógica, pasando la placa y la
        // justificación obligatoria.
        // Si la placa no existe en la papelera lógica, fallará con HTTP 400/404; si
        // tiene éxito, retorna HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.reactivateByPlaca(placa, motivo));
    }
    //////////////////////////////////////

    // =====================================================
    // ================== ESTADOS Y CICLO DE VIDA =========
    // =====================================================
    // @PatchMapping("/{id}/estado"): Mapea peticiones HTTP de tipo PATCH hacia la
    // ruta específica del estado.
    // Semántica REST: PATCH se usa para "Parches" o actualizaciones parciales de un
    // solo atributo (en este caso, el estado),
    // ahorrando ancho de banda y evitando colisiones de datos concurrentes en otros
    // campos.
    @PatchMapping("/{id}/estado")
    // @PreAuthorize: Expande inteligentemente los permisos.
    // REGLA DE NEGOCIO: Un 'OPERADOR' (ej. jefe de patio o mecánico) no puede
    // borrar un camión ni cambiarle el motor (eso es de 'ADMIN'),
    // pero SÍ debe tener el poder de cambiar su estado operativo en el día a día.
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Documenta en Swagger que este endpoint no es una simple
    // actualización, sino un motor de reglas (FSM).
    @Operation(summary = "Transición de estado operativo", description = "Ejecuta una mutación parcial validando las fronteras permitidas por la Máquina de Estados Finita (FSM).")
    // ResponseEntity<VehicleResponse>: Retorna la versión actualizada del recurso.
    // @PathVariable UUID id: Atrapa el ID interno de la URL.
    // @Valid @RequestBody EstadoCambioRequest request: Valida que el JSON traiga
    // obligatoriamente el nuevo estado y el motivo.
    public ResponseEntity<VehicleResponse> changeState(@PathVariable UUID id,
            @Valid @RequestBody EstadoCambioRequest request) {

        // REGLAS DE NEGOCIO APLICADAS: El controlador delega al servicio la ejecución
        // de la Máquina de Estados (FSM).
        // El servicio verificará si el salto es legal (Ej: De EN_MANTENIMIENTO a
        // DISPONIBLE) o lo bloqueará con una excepción.

        // Ejecuta la mutación pasando los atributos individuales extraídos del DTO
        // (Java Record).
        VehicleResponse response = vehicleService.changeState(id,
                request.nuevoEstado(),
                request.motivoCambio(),
                request.servicioOrigen());

        // Retorna HTTP 200 (OK) con el JSON del vehículo reflejando su nuevo estado
        // operativo y la fecha de actualización.
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // TRANSICIÓN DE ESTADO POR PLACA (LOGÍSTICA DE PATIO)
    // =========================================================================

    // @PatchMapping("/placa/{placa}/estado"): Ruta alternativa optimizada para la
    // operación de campo.
    @PatchMapping("/placa/{placa}/estado")
    // @PreAuthorize: Mantiene el acceso dual para personal de patio (OPERADOR) y
    // mesa de control (ADMIN).
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Detalla en la documentación técnica el propósito de esta ruta por
    // llave natural (placa).
    @Operation(summary = "Transición de estado por placa", description = "Igual a la transición por ID, pero apuntando directamente a la placa del vehículo.")
    // @PathVariable String placa: Extrae la identificación alfanumérica del
    // automotor desde la URL.
    public ResponseEntity<VehicleResponse> updateEstadoByPlaca(@PathVariable String placa,
            @Valid @RequestBody EstadoCambioRequest request) {

        // Delega la responsabilidad de buscar el vehículo por placa y aplicar el cambio
        // de estado de forma segura.
        // Envuelve el resultado exitoso en un código HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.updateEstadoByPlaca(placa, request));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////

    // =====================================================
    // ==================== HISTORIAL =====================
    // =====================================================

    // Ruta para consultar el historial de auditoría de todos los vehículos
    // mezclados.

    // =========================================================================
    // ENDPOINTS DE TRAZABILIDAD Y AUDITORÍA (AUDIT TRAIL)
    // =========================================================================

    // @GetMapping("/historial"): Mapea peticiones HTTP de tipo GET a la sub-ruta
    // "/historial" (URL completa: /vehiculos/historial).
    // Semántica REST: Consulta masiva de lectura que extrae registros transversales
    // del sistema.
    @GetMapping("/historial")
    // @PreAuthorize: Control de seguridad perimetral. Permite el acceso a roles
    // base, operadores y administradores.
    // Garantiza que la bitácora global pueda ser consultada por personal de control
    // logístico o auditores internos.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Agrega metadatos para la especificación OpenAPI (Swagger),
    // describiendo el propósito de este reporte global.
    @Operation(summary = "Auditoría global de flota", description = "Obtiene la bitácora completa y paginada de todos los cambios de estado operativos en el sistema.")
    // public: Permite la exposición pública del método dentro del contexto web de
    // Spring.
    // ResponseEntity<Page<HistorialEstadoResponse>>: Envuelve el resultado paginado
    // del historial en una respuesta HTTP estructurada.
    // Pageable pageable: Intercepta de forma automática los parámetros de
    // paginación y ordenamiento enviados en los Query Params de la URL.
    public ResponseEntity<Page<HistorialEstadoResponse>> getHistorialGlobal(Pageable pageable) {

        // Invocación al servicio: Recupera la bitácora global consolidada y la envuelve
        // en un código de estado HTTP 200 (OK).
        // Los resultados se sirven en orden cronológico inverso (el evento más reciente
        // primero) gracias al diseño del repositorio.
        return ResponseEntity.ok(vehicleService.findAllHistorialGlobal(pageable));
    }

    // @GetMapping("/{id}/historial"): Asigna peticiones GET apuntando a un UUID
    // específico en la ruta intermedia.
    // Sigue las mejores prácticas de diseño REST al estructurar sub-recursos (el
    // historial pertenece jerárquicamente a un vehículo).
    @GetMapping("/{id}/historial")
    // @PreAuthorize: Asegura que el personal operativo autorizado tenga acceso
    // visual al diario de vida técnico del camión.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta el carácter estricto y seguro de la ruta en la consola
    // interactiva de Swagger.
    @Operation(summary = "Trazabilidad de un vehículo específico", description = "Extrae el log inmutable (Append-Only) con el ciclo de vida operativo del activo.")
    // @PathVariable UUID id: Resuelve el segmento dinámico '{id}' de la URL y lo
    // convierte en un objeto UUID inmutable en Java.
    public ResponseEntity<Page<HistorialEstadoResponse>> getHistorial(@PathVariable UUID id, Pageable pageable) {

        // PATRÓN DE DISEÑO: Append-Only Log.
        // Transmite el identificador único al servicio para extraer exclusivamente el
        // historial de este activo.
        // Si el UUID no existe, el flujo se interrumpe lanzando un error 404, de lo
        // contrario retorna HTTP 200 (OK) con la página de resultados.
        return ResponseEntity.ok(vehicleService.getHistorialByVehiculoId(id, pageable));
    }

    // @GetMapping("/placa/{placa}/historial"): Define un endpoint de conveniencia
    // basado en la clave natural visible del automotor (la placa).
    // Facilita auditorías de campo rápidas cuando los supervisores investigan
    // incidencias de un camión específico usando su identificador físico.
    @GetMapping("/placa/{placa}/historial")
    // @PreAuthorize: Control de acceso basado en roles que permite consultas
    // rápidas a usuarios autorizados de la mesa de control.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Detalla el comportamiento táctico del endpoint para mitigar la
    // necesidad de conocer identificadores técnicos (UUIDs).
    @Operation(summary = "Trazabilidad mediante número de placa", description = "Alternativa táctica para auditar la historia de un vehículo sin conocer su UUID.")
    // @PathVariable String placa: Captura la secuencia de caracteres alfanuméricos
    // de la placa directamente desde la URL.
    public ResponseEntity<Page<HistorialEstadoResponse>> getHistorialByPlaca(@PathVariable String placa,
            Pageable pageable) {

        // Transmite la placa al servicio de aplicación, el cual realizará un JOIN
        // optimizado en la base de datos relacional.
        // Retorna la colección paginada de mutaciones de estado de ese vehículo exacto
        // envuelta en un HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.getHistorialByPlaca(placa, pageable));
    }

    // =====================================================
    // ================== DISPONIBILIDAD ==================
    // =====================================================

    // =========================================================================
    // CONSULTAS ULTRALIGERAS DE APTITUD OPERATIVA
    // =========================================================================

    // @GetMapping("/{id}/disponibilidad"): Mapea peticiones HTTP de tipo GET a la
    // sub-ruta de disponibilidad por ID.
    // Semántica REST: Consulta de lectura rápida diseñada para responder de forma
    // inmediata a otros componentes o microservicios.
    @GetMapping("/{id}/disponibilidad")
    // @PreAuthorize: Mantiene el acceso transversal para roles con permisos de
    // lectura y consulta de la flota.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta en Swagger el propósito de optimización de rendimiento
    // de este endpoint en particular.
    @Operation(summary = "Evaluación de aptitud operativa", description = "Devuelve un DTO ultraligero que permite a otros sistemas saber inmediatamente si un activo está en condiciones de ser bloqueado.")
    // public: Permite la exposición del método en el ecosistema web de Spring.
    // ResponseEntity<DisponibilidadResponse>: Envuelve un DTO optimizado (Java
    // Record) que reduce drásticamente el tamaño del JSON de respuesta.
    // @PathVariable UUID id: Captura el identificador único del vehículo directo
    // desde la URL.
    public ResponseEntity<DisponibilidadResponse> getDisponibilidad(@PathVariable UUID id) {

        // REGLA DE NEGOCIO / EFICIENCIA:
        // Evita descargar toda la ficha técnica (marca, modelo, chasis, SOAT) si el
        // cliente solo requiere validar un booleano (True/False).
        // Envuelve la respuesta del servicio en un código de estado HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.getDisponibilidad(id));
    }

    // @GetMapping("/placa/{placa}/disponibilidad"): Mapea peticiones GET utilizando
    // la clave natural alfanumérica de la placa.
    @GetMapping("/placa/{placa}/disponibilidad")
    // @PreAuthorize: Accesible por los roles autorizados para consultas operativas
    // rápidas desde terminales de campo.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Registra en la consola de OpenAPI que el endpoint evalúa la
    // aptitud del camión basándose en políticas de negocio vigentes.
    @Operation(summary = "Evaluación de aptitud por placa", description = "Permite inspeccionar la salud operativa del vehículo cruzando su placa contra las políticas de negocio.")
    // @PathVariable String placa: Extrae el texto de la placa directamente del
    // segmento dinámico de la URL.
    public ResponseEntity<DisponibilidadResponse> getDisponibilidadByPlaca(@PathVariable String placa) {

        // Delega la validación liviana al servicio pasándole la placa y responde con un
        // código HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.getDisponibilidadByPlaca(placa));
    }

    // =========================================================================
    // ENDPOINTS DE FILTRADO PARA DASHBOARDS (TABLEROS DE CONTROL)
    // =========================================================================

    // @GetMapping("/disponibles"): Mapea peticiones GET para aislar los camiones
    // listos y libres para trabajar.
    @GetMapping("/disponibles")
    // @PreAuthorize: Permite el acceso a los perfiles que planifican rutas o
    // asignan despachos diarios.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Detalla en Swagger que esta ruta sirve como fuente de datos para
    // el tablero de vehículos ociosos.
    @Operation(summary = "Inventario de línea activa", description = "Punto de anclaje para cuadros de mando rápidos visualizando inventario ocioso.")
    // Pageable pageable: Inyecta los controles de paginación y ordenación de Spring
    // Data (?page=0&size=10) desde la petición web.
    public ResponseEntity<Page<VehicleResponse>> findDisponibles(Pageable pageable) {

        // Llama al método del servicio que filtra directamente en la base de datos los
        // activos con estado 'DISPONIBLE'.
        return ResponseEntity.ok(vehicleService.findDisponibles(pageable));
    }

    // @GetMapping("/reservados"): Mapea peticiones GET destinadas a visualizar los
    // camiones comprometidos comercialmente.
    @GetMapping("/reservados")
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Mapea la documentación interactiva (Nota: Aunque la descripción
    // original se repitió, este endpoint expone activos bloqueados por viajes).
    @Operation(summary = "Inventario de línea activa", description = "Punto de anclaje para cuadros de mando rápidos visualizando inventario ocioso.")
    public ResponseEntity<Page<VehicleResponse>> findReservados(Pageable pageable) {

        // Solicita al servicio la lista paginada de vehículos en estado 'RESERVADO' y
        // responde HTTP 200 (OK).
        return ResponseEntity.ok(vehicleService.findReservados(pageable));
    }

    // @GetMapping("/mantenimiento"): Concentra las peticiones GET para auditar los
    // activos que se encuentran bajo revisión técnica.
    @GetMapping("/mantenimiento")
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Provee metadatos en Swagger útiles para que el equipo de taller y
    // mantenimiento consuma este listado.
    @Operation(summary = "Inventario de línea activa", description = "Punto de anclaje para cuadros de mando rápidos visualizando inventario ocioso.")
    public ResponseEntity<Page<VehicleResponse>> findMantenimiento(Pageable pageable) {

        // Retorna un HTTP 200 (OK) con la porción pagitada de camiones en estado
        // 'EN_MANTENIMIENTO'.
        return ResponseEntity.ok(vehicleService.findMantenimiento(pageable));
    }

    // @GetMapping("/fueradeservicio"): Mapea peticiones GET para identificar
    // vehículos críticos fuera de operación por fallas o siniestros.
    @GetMapping("/fueradeservicio")
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta en el contrato OpenAPI la ruta destinada a la revisión
    // de pérdidas operacionales o bajas provisionales.
    @Operation(summary = "Inventario de línea activa", description = "Punto de anclaje para cuadros de mando rápidos visualizando inventario ocioso.")
    public ResponseEntity<Page<VehicleResponse>> findFueraServicio(Pageable pageable) {

        // Ejecuta la consulta optimizada y devuelve un HTTP 200 (OK) con los activos
        // marcados como 'FUERA_DE_SERVICIO'.
        return ResponseEntity.ok(vehicleService.findFueraServicio(pageable));
    }

    // =====================================================
    // ==================== RESERVAS (SAGA) ===============
    // =====================================================

    // ─────────────────────────────────────────────────────────────────────────────
    // PATRÓN DE DISEÑO APLICADO: Orchestration Saga (Saga Orquestada).
    // ¿Qué hace? Permite reservar algo en varios sistemas al mismo tiempo.
    // Ejemplo: Como comprar boletos de avión por Despegar. Primero Despegar
    // "bloquea" tus
    // sillas (iniciarReserva), luego te cobra en el banco, y si todo sale bien, te
    // imprime los boletos (confirmarReserva). Si tu tarjeta falla, libera las
    // sillas (compensarReserva).
    // ─────────────────────────────────────────────────────────────────────────────

    // =========================================================================
    // ORQUESTACIÓN DE MICROSERVICIOS: PATRÓN SAGA (DISTRIBUTED TRANSACTIONS)
    // =========================================================================

    // @PostMapping("/{id}/reservas"): Mapea la petición POST para iniciar el
    // bloqueo de un vehículo.
    // Paso 1 de la SAGA: Iniciar el bloqueo temporal (Estado PENDIENTE).
    @PostMapping("/{id}/reservas")
    // @PreAuthorize: Permite que tanto los operadores de logística como los
    // administradores inicien reservas.
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Documenta que este es el primer paso de una transacción
    // distribuida.
    @Operation(summary = "Comando Saga: Solicitar Bloqueo", description = "Fase 1: Crea un registro PENDIENTE sobre el activo, protegiendo las fronteras operativas ante solapamientos.")
    public ResponseEntity<ReservaResponse> iniciarReserva(@PathVariable UUID id,
            @Valid @RequestBody ReservaRequest request) {

        // REGLA DE NEGOCIO: Seguridad e Idempotencia (Evitar doble clic).
        // El servicio de Saga garantizará que si llegan dos peticiones concurrentes
        // para el mismo camión,
        // solo la primera logrará bloquearlo.
        // Se retorna HTTP 201 (CREATED) indicando que el "Expediente de Reserva" fue
        // creado con éxito.
        return ResponseEntity.status(HttpStatus.CREATED).body(sagaService.iniciarReserva(id, request));
    }

    // @PostMapping("/placa/{placa}/reservas"): Variante operativa para iniciar la
    // Saga usando la placa comercial.
    @PostMapping("/placa/{placa}/reservas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Documenta que facilita la integración con sistemas satélite (ej.
    // lectores de patio).
    @Operation(summary = "Comando Saga: Solicitar Bloqueo mediante placa", description = "Atajo logístico (HU11) para bloquear temporalmente el vehículo desde sistemas satélite de bodega.")
    public ResponseEntity<ReservaResponse> iniciarReservaByPlaca(@PathVariable String placa,
            @Valid @RequestBody ReservaRequest request) {

        // Delega el inicio de la orquestación al servicio usando la placa y retorna
        // HTTP 201 (CREATED).
        return ResponseEntity.status(HttpStatus.CREATED).body(sagaService.iniciarReservaByPlaca(placa, request));
    }

    // =========================================================================
    // CONSULTA DE ESTADO DE LA SAGA (POLLING)
    // =========================================================================

    // @GetMapping("/reservas/{reservaId}"): Permite a otros microservicios
    // consultar en qué va el trámite.
    @GetMapping("/reservas/{reservaId}")
    // @PreAuthorize: Lectura abierta para todo el personal autorizado.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Define este endpoint como el monitor de la transacción.
    @Operation(summary = "Consultar estado del expediente", description = "Permite inspeccionar la bitácora viva de la Saga de reserva.")
    public ResponseEntity<ReservaResponse> obtenerReserva(@PathVariable UUID reservaId) {

        // PATRÓN FUNCIONAL: Programación Declarativa con Optionals.
        // Intenta buscar la reserva. Si la encuentra (.map), la envuelve en un HTTP 200
        // (OK).
        // Si el Optional viene vacío (.orElse), retorna un HTTP 404 (Not Found).
        return sagaService.findReservaById(reservaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    // SAGA COMMIT: CONFIRMACIÓN DEFINITIVA
    // =========================================================================

    // @PostMapping("/reservas/{reservaId}/confirmar"): Endpoint para consolidar la
    // transacción si todos los microservicios tuvieron éxito.
    // Paso 2 (FINAL) de la SAGA: Asentar permanentemente la reserva.
    @PostMapping("/reservas/{reservaId}/confirmar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @Operation(summary = "Comando Saga: Consolidación (Commit)", description = "Fase Final: Asienta definitivamente la reserva si la ventana operativa de tiempo es legal.")
    // Retorna un Map<String, Object> para construir un JSON dinámico y altamente
    // personalizado.
    public ResponseEntity<Map<String, Object>> confirmarReserva(@PathVariable UUID reservaId) {

        // Ejecutamos la lógica de confirmación (Commit) que pasará la reserva de
        // PENDIENTE a CONFIRMADA. Ahora recibe un ReservaResponse (DTO Record).
        return sagaService.confirmarReserva(reservaId).map(reserva -> {

            // ARQUITECTURA DE RESPUESTA: Uso de LinkedHashMap.
            // A diferencia de un HashMap normal, LinkedHashMap garantiza que las llaves del
            // JSON
            // se impriman exactamente en el orden en que las insertamos.
            Map<String, Object> response = new LinkedHashMap<>();

            // Accedemos a los datos directamente a través de los métodos del Record
            // inmutable.
            response.put("message",
                    "La reserva del vehículo con placa " + reserva.numeroPlaca() + " ha sido confirmada con éxito.");
            response.put("idAsignacionExt", reserva.idAsignacionExt());
            response.put("idVehiculo", reserva.idVehiculo());
            response.put("numeroPlaca", reserva.numeroPlaca());
            response.put("timestamp", LocalDateTime.now().toString());

            // Retorna HTTP 200 (OK) con la estructura ensamblada.
            return ResponseEntity.ok(response);

            // Si la reserva no existe para confirmar, retorna HTTP 404 (Not Found).
        }).orElse(ResponseEntity.notFound().build());
    }

    // @PostMapping("/reservas/placa/{numeroPlaca}/confirmar"): Commit de la saga
    // apuntando a la llave natural (placa).

   // Endpoint para confirmar todas las reservas pendientes de una placa de golpe
    @PostMapping("/placa/{numeroPlaca}/reservas/confirmar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @Operation(summary = "Comando Masivo: Consolidación por Placa", description = "Confirma de una sola vez todas las reservas que se encuentren PENDIENTES para un vehículo específico.")
    public ResponseEntity<Map<String, Object>> confirmarReservaPorPlaca(@PathVariable String numeroPlaca) {

        // Llamamos al servicio. Nos devuelve la lista de DTOs ya armada.
        List<ReservaResponse> reservasConfirmadas = sagaService.confirmarReservaPorPlaca(numeroPlaca);

        // Armamos un JSON dinámico para mayor claridad en el Frontend
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Se confirmaron exitosamente " + reservasConfirmadas.size() + " reserva(s) para el vehículo " + numeroPlaca.toUpperCase());
        response.put("placa", numeroPlaca.toUpperCase());
        response.put("timestamp", LocalDateTime.now().toString());
        
        // Aquí pasamos la lista completa de reservas confirmadas
        response.put("reservasAfectadas", reservasConfirmadas); 

        return ResponseEntity.ok(response);
    }

    @PutMapping("/reservas/{idReserva}/fechas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    @Operation(summary = "Actualizar fechas de una reserva", description = "Modifica el rango de tiempo de una reserva validando colisiones a través del servicio orquestador de Sagas.")
    public ResponseEntity<ReservaResponse> actualizarFechasReserva(
            @PathVariable UUID idReserva,
            @Valid @RequestBody UpdateReservaDatesRequest request) {

        // Delegación directa a la capa de Sagas
        ReservaResponse response = sagaService.actualizarFechasReserva(idReserva, request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // SAGA ROLLBACK: COMPENSACIÓN ANTE FALLOS
    // =========================================================================

    // @PostMapping("/reservas/{reservaId}/compensar"): Endpoint de rescate si un
    // microservicio externo falla.
    // Paso Alterno (FALLO) de la SAGA: Revertir todo para no dejar vehículos
    // bloqueados inútilmente.
    @PostMapping("/reservas/{reservaId}/compensar")
    // @PreAuthorize: Restricción crítica. Solo un ADMIN (o el sistema automatizado)
    // debería poder cancelar flujos en progreso.
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Comando Saga: Anulación (Rollback)", description = "Fase Alterna: Destruye el compromiso de reserva liberando nuevamente el activo si la orquestación fracasa.")
    // @RequestParam String motivo: Obliga a dejar una evidencia técnica del porqué
    // falló la transacción.
    public ResponseEntity<String> compensarReserva(@PathVariable UUID reservaId, @RequestParam String motivo) {

        // Ejemplo de Negocio: El microservicio de pagos rechazó la tarjeta del cliente.
        // Facturación le avisa a FleetOps que debe liberar ("compensar") el camión que
        // había apartado temporalmente.
        boolean compensado = sagaService.compensarPorReservaId(reservaId, motivo);

        // Operador ternario: Retorna HTTP 200 (OK) con un mensaje claro si se liberó el
        // activo,
        // o HTTP 404 (Not Found) si la reserva no existía para ser compensada.
        return compensado
                ? ResponseEntity.ok("Reserva compensada exitosamente")
                : ResponseEntity.notFound().build();
    }

    // LISTAR TODAS LAS RESERVAS GLOBALES
    // @GetMapping("/reservas"): Mapea peticiones HTTP de tipo GET a la ruta
    // principal de reservas.
    @GetMapping("/reservas")
    // @PreAuthorize: Permite el acceso transversal. Es seguro que un
    // 'USUARIO_AUTORIZADO' (ej. auditoría) vea este listado global.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta en Swagger que este es el reporte maestro y absoluto de
    // todas las transacciones.
    @Operation(summary = "Listar todas las reservas", description = "Obtiene el historial global paginado de todas las reservas del sistema, ordenadas de la más reciente a la más antigua.")
    // ResponseEntity<Page<...>>: Envuelve los resultados en un objeto de paginación
    // para evitar desbordamientos de memoria RAM.
    // Pageable pageable: Spring Boot extrae automáticamente los parámetros de
    // paginación desde la URL (ej: ?page=0&size=20).
    public ResponseEntity<Page<ReservaResponse>> getAllReservas(Pageable pageable) {

        // Delega la consulta global al servicio, el cual traduce el 'Pageable' a
        // comandos SQL (LIMIT/OFFSET).
        // Retorna un código HTTP 200 (OK) con la página de resultados.
        return ResponseEntity.ok(sagaService.findAllReservas(pageable));
    }

    // LISTAR RESERVAS PENDIENTES (BANDEJA DE TAREAS)
    // @GetMapping("/reservas/pendientes"): Mapea la consulta de expedientes que
    // están a la espera de resolución.
    @GetMapping("/reservas/pendientes")
    // @PreAuthorize: Restringe la vista a roles operativos ('OPERADOR', 'ADMIN') ya
    // que son ellos quienes aprueban o rechazan.
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Documenta que aquí residen las reservas en el "Paso 1" del Patrón
    // SAGA.
    @Operation(summary = "Listar reservas pendientes", description = "Obtiene la bandeja de entrada de reservas en estado PENDIENTE esperando confirmación, ordenadas de la más reciente a la más antigua.")
    public ResponseEntity<Page<ReservaResponse>> getReservasPendientes(Pageable pageable) {

        // Delega la consulta al servicio para buscar estrictamente el estado PENDIENTE.
        // Retorna HTTP 200 (OK) con la información lista para ser pintada en el
        // Frontend.
        return ResponseEntity.ok(sagaService.findReservasPendientes(pageable));
    }

    // LISTAR RESERVAS CONFIRMADAS (BANDEJA DE ÉXITOS)
    // @GetMapping("/reservas/confirmadas"): Endpoint para auditar las transacciones
    // distribuidas que tuvieron éxito (Commit).
    @GetMapping("/reservas/confirmadas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: (Texto corregido) Se ajustó para reflejar que lista reservas
    // exitosas, no pendientes.
    @Operation(summary = "Listar reservas confirmadas", description = "Obtiene el historial de reservas en estado CONFIRMADA que concluyeron la orquestación exitosamente.")
    public ResponseEntity<Page<ReservaResponse>> getReservasConfirmadas(Pageable pageable) {

        // Delega la consulta al servicio filtrando por estado CONFIRMADA y retorna HTTP
        // 200 (OK).
        return ResponseEntity.ok(sagaService.findReservasConfirmadas(pageable));
    }

    // LISTAR RESERVAS FALLIDAS (BANDEJA DE ERRORES TÉCNICOS)
    // @GetMapping("/reservas/fallidas"): Mapea el monitor de errores técnicos o
    // caídas de red durante la orquestación SAGA.
    @GetMapping("/reservas/fallidas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: (Texto corregido) Aclara que esta es una bandeja de diagnóstico
    // para sistemas caídos.
    @Operation(summary = "Listar reservas fallidas", description = "Obtiene el registro de auditoría de reservas en estado FALLIDA debido a errores técnicos o caídas de microservicios externos.")
    public ResponseEntity<Page<ReservaResponse>> getReservasFallidas(Pageable pageable) {

        // Ejecuta la consulta de diagnóstico y retorna la colección de transacciones
        // rotas mediante HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findReservasFallidas(pageable));
    }

    // LISTAR RESERVAS CANCELADAS (BANDEJA DE COMPENSACIONES OPERATIVAS)
    // @GetMapping("/reservas/canceladas"): Mapea la consulta para la bandeja de
    // anulaciones y reversiones.
    @GetMapping("/reservas/canceladas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: (Texto corregido) Documenta que expone recursos liberados tras un
    // Rollback (ej. cliente canceló o pago rechazado).
    @Operation(summary = "Listar reservas canceladas", description = "Obtiene la bandeja de entrada de reservas en estado CANCELADA (Compensadas/Rollback) ordenadas de la más reciente a la más antigua.")
    public ResponseEntity<Page<ReservaResponse>> getReservasCanceladas(Pageable pageable) {

        // Delega al servicio la recuperación de los históricos de reversión y retorna
        // la página con un HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findReservasCanceladas(pageable));
    }

    // LISTAR RESERVAS POR PLACA (HISTORIAL DEL VEHÍCULO)
    // @GetMapping("/reservas/placa/{numeroPlaca}"): Mapea peticiones HTTP de tipo
    // GET utilizando la llave natural del vehículo (la placa) en la URL.
    @GetMapping("/reservas/placa/{numeroPlaca}")
    // @PreAuthorize: Define el perímetro de seguridad. Permite que cualquier
    // usuario autenticado y con un rol base pueda auditar el historial de un
    // automotor físico.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta en Swagger este endpoint de conveniencia, ideal para
    // consultas rápidas en terminales logísticas o aplicaciones móviles.
    @Operation(summary = "Listar reservas por placa", description = "Obtiene el historial paginado de todas las reservas asociadas a un vehículo mediante su placa, ordenadas de la más reciente a la más antigua.")
    // public ResponseEntity<Page<...>>: Expone el método a la red. Retorna una
    // respuesta HTTP que envuelve una "Página" (Page) de resultados, protegiendo la
    // memoria del servidor.
    public ResponseEntity<Page<ReservaResponse>> getReservasByPlaca(
            // @PathVariable String numeroPlaca: Extrae de forma dinámica el número de placa
            // directamente desde el segmento de la ruta en la URL.
            @PathVariable String numeroPlaca,
            // Pageable pageable: Inyectado automáticamente por Spring Boot leyendo los
            // parámetros de paginación que envíe el cliente (ej. ?page=0&size=10).
            Pageable pageable) {

        // Delega la ejecución al servicio orquestador (SagaService), el cual resolverá
        // la placa de forma insensible a mayúsculas/minúsculas.
        // Retorna un código de estado HTTP 200 (OK) con el bloque JSON de la página de
        // resultados.
        return ResponseEntity.ok(sagaService.findReservasByPlaca(numeroPlaca, pageable));
    }

    // LISTAR RESERVAS POR PLACA Y ESTADO ESPECÍFICO
    // @GetMapping("/reservas/placa/{numeroPlaca}/estado/{estado}"): Define una ruta
    // anidada compleja. Cruza dos dimensiones de búsqueda (el vehículo físico y la
    // etapa transaccional).
    @GetMapping("/reservas/placa/{numeroPlaca}/estado/{estado}")
    // @PreAuthorize: Mantiene la política de lectura transversal para el personal
    // operativo y administrativo.
    @PreAuthorize("hasAnyRole('USUARIO_AUTORIZADO', 'OPERADOR', 'ADMIN')")
    // @Operation: Documenta en la interfaz interactiva la capacidad de filtrar con
    // precisión quirúrgica el estado de la reserva para un activo específico.
    @Operation(summary = "Listar reservas por placa y estado", description = "Obtiene el historial paginado de reservas de un vehículo (por su placa), filtrado exclusivamente por un estado (PENDIENTE, CONFIRMADA, FALLIDA o CANCELADA).")
    // Retorna una página de resultados óptima para alimentar interfaces de usuario
    // complejas (como la pestaña de "Viajes Exitosos" en el perfil de un camión).
    public ResponseEntity<Page<ReservaResponse>> getReservasByPlacaAndEstado(
            // @PathVariable String numeroPlaca: Captura el identificador alfanumérico del
            // vehículo en texto plano.
            @PathVariable String numeroPlaca,
            // @PathVariable EstadoReserva estado: ¡Magia de Spring Boot! El framework lee
            // el texto de la URL (ej. "CONFIRMADA") y lo convierte de forma automática y
            // segura al Enum 'EstadoReserva' de Java.
            @PathVariable EstadoReserva estado,
            // Pageable pageable: Maneja el límite y desplazamiento (Limit/Offset) de la
            // base de datos de forma nativa.
            Pageable pageable) {

        // Ejecuta la consulta combinada (un 'AND' lógico en SQL gestionado por JPA)
        // dentro del servicio.
        // Envuelve la lista filtrada de transacciones en un código HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findReservasByPlacaAndEstado(numeroPlaca, estado, pageable));
    }

    // =========================================================================
    // ENDPOINTS DE AUDITORÍA DE SAGAS (SOLO OPERADORES Y ADMINS)
    // =========================================================================

    // @GetMapping("/sagas"): Mapea peticiones GET para obtener la bitácora absoluta
    // de orquestaciones en la red.
    @GetMapping("/sagas")
    // @PreAuthorize: Restringe la visualización del estado de red a operadores
    // técnicos y administradores.
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Documenta en Swagger el punto de entrada principal del monitor de
    // transacciones.
    @Operation(summary = "Listar todas las Sagas", description = "Obtiene el historial global paginado de todas las transacciones distribuidas (Sagas).")
    // Inyecta el objeto Pageable para manejar volúmenes masivos de datos
    // (LIMIT/OFFSET).
    public ResponseEntity<Page<SagaResponse>> getAllSagas(Pageable pageable) {

        // Delega la consulta global al motor de base de datos y retorna la página
        // empaquetada en un HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findAllSagas(pageable));
    }

    // @GetMapping("/sagas/iniciadas"): Endpoint para rastrear transacciones que
    // acaban de nacer.
    @GetMapping("/sagas/iniciadas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation (Agregado): Documenta la bandeja de Sagas que acaban de iniciar su
    // orquestación.
    @Operation(summary = "Listar Sagas iniciadas", description = "Obtiene las transacciones distribuidas que acaban de comenzar (Paso 1) y esperan propagación en la red.")
    public ResponseEntity<Page<SagaResponse>> getSagasIniciadas(Pageable pageable) {

        // Ejecuta la consulta filtrando por el estado INICIADA y retorna HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findSagasIniciadas(pageable));
    }

    // @GetMapping("/sagas/en-progreso"): Monitor de cuellos de botella. Muestra
    // transacciones bloqueadas o lentas.
    @GetMapping("/sagas/en-progreso")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation (Agregado): Expone las Sagas que están en plena comunicación con
    // sistemas externos (ej. Pasarela de pagos).
    @Operation(summary = "Listar Sagas en progreso", description = "Obtiene transacciones que actualmente están procesando respuestas asíncronas de otros microservicios.")
    public ResponseEntity<Page<SagaResponse>> getSagasEnProgreso(Pageable pageable) {

        // Retorna HTTP 200 (OK) con las transacciones que están literalmente "volando"
        // por la red.
        return ResponseEntity.ok(sagaService.findSagasEnProgreso(pageable));
    }

    // @GetMapping("/sagas/completadas"): Bandeja de éxito total en la arquitectura
    // distribuida.
    @GetMapping("/sagas/completadas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation (Agregado): Documenta la bandeja de transacciones con "Commit"
    // global exitoso.
    @Operation(summary = "Listar Sagas completadas", description = "Obtiene las transacciones donde todos los microservicios involucrados respondieron con éxito absoluto.")
    public ResponseEntity<Page<SagaResponse>> getSagasCompletadas(Pageable pageable) {

        // Retorna HTTP 200 (OK) con el historial de triunfos del orquestador.
        return ResponseEntity.ok(sagaService.findSagasCompletadas(pageable));
    }

    // @GetMapping("/sagas/fallidas"): Tablero crítico para soporte nivel 3
    // (Sistemas rotos o caídos).
    @GetMapping("/sagas/fallidas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation (Agregado): Alerta visual para los ingenieros sobre flujos que se
    // rompieron irrecuperablemente.
    @Operation(summary = "Listar Sagas fallidas", description = "Monitor de diagnóstico para transacciones distribuidas que colapsaron y requieren atención humana o reintentos.")
    public ResponseEntity<Page<SagaResponse>> getSagasFallidas(Pageable pageable) {

        // Retorna HTTP 200 (OK) con la lista paginada de fallos de red o de lógica de
        // negocio externa.
        return ResponseEntity.ok(sagaService.findSagasFallidas(pageable));
    }

    // @GetMapping("/sagas/compensadas"): Tablero de resiliencia (Rollbacks
    // automáticos o manuales).
    @GetMapping("/sagas/compensadas")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation (Agregado): Documenta que el sistema funcionó bien al echar hacia
    // atrás algo que había fallado.
    @Operation(summary = "Listar Sagas compensadas", description = "Obtiene transacciones que fallaron pero fueron exitosamente revertidas (Rollback distribuido), liberando los recursos.")
    public ResponseEntity<Page<SagaResponse>> getSagasCompensadas(Pageable pageable) {

        // Retorna HTTP 200 (OK) demostrando que el patrón Saga previno bloqueos
        // fantasma en la base de datos.
        return ResponseEntity.ok(sagaService.findSagasCompensadas(pageable));
    }

    // @GetMapping("/sagas/placa/{numeroPlaca}"): Auditoría de red cruzada con el
    // mundo físico (la placa del camión).
    @GetMapping("/sagas/placa/{numeroPlaca}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Detalla que esto permite buscar qué le pasó a nivel de red a un
    // vehículo en específico.
    @Operation(summary = "Listar sagas por placa", description = "Obtiene los trámites de Saga de un vehículo específico mediante su placa.")
    public ResponseEntity<Page<SagaResponse>> getSagasByPlaca(
            // Extrae la placa dinámica desde la URL.
            @PathVariable String numeroPlaca,
            // Inyecta el objeto de paginación.
            Pageable pageable) {

        // Ejecuta la búsqueda cruzada y empaqueta el historial de red de ese camión en
        // un HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findSagasByPlaca(numeroPlaca, pageable));
    }

    // @GetMapping("/sagas/placa/{numeroPlaca}/estado/{estado}"): Búsqueda granular
    // y multidimensional.
    @GetMapping("/sagas/placa/{numeroPlaca}/estado/{estado}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    // @Operation: Documenta este endpoint avanzado capaz de responder preguntas
    // como: "¿Este camión tiene transacciones colgadas (EN_PROGRESO)?"
    @Operation(summary = "Listar sagas por placa y estado", description = "Filtra las Sagas de una placa por un estado exacto de red (INICIADA, EN_PROGRESO, COMPLETADA, FALLIDA, COMPENSADA).")
    public ResponseEntity<Page<SagaResponse>> getSagasByPlacaAndEstado(
            // Captura la llave natural (placa) en formato de texto.
            @PathVariable String numeroPlaca,
            // Spring Boot convierte dinámicamente el String de la URL directamente al Enum
            // 'EstadoSaga' para validación tipada estricta.
            @PathVariable com.fleetops.vehicles.models.entities.EstadoSaga estado,
            // Manejador nativo de límites (LIMIT/OFFSET).
            Pageable pageable) {

        // Delega esta consulta compleja al servicio y devuelve los resultados precisos
        // con HTTP 200 (OK).
        return ResponseEntity.ok(sagaService.findSagasByPlacaAndEstado(numeroPlaca, estado, pageable));
    }

}