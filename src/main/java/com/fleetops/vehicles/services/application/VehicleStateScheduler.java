// Declaración del paquete lógico donde se agrupan los servicios de aplicación del proyecto.
package com.fleetops.vehicles.services.application;

// Importa todas las entidades del modelo de base de datos (Vehículos, Reservas, Historiales, etc.).
import com.fleetops.vehicles.models.entities.*;
// Importa todos los repositorios para interactuar con la base de datos.
import com.fleetops.vehicles.repositories.*;
// Importa la anotación de Lombok que genera el constructor con las dependencias finales.
import lombok.RequiredArgsConstructor;
// Importa la anotación de Lombok que nos permite escribir mensajes en la consola del servidor.
import lombok.extern.slf4j.Slf4j;
// Importa la anotación de Spring para programar tareas automáticas (Cron Jobs).
import org.springframework.scheduling.annotation.Scheduled;
// Importa la anotación para decirle a Spring que esta clase es un componente que debe gestionar.
import org.springframework.stereotype.Component;
// Importa la anotación para envolver nuestras funciones en transacciones seguras de base de datos.
import org.springframework.transaction.annotation.Transactional;

// Importa la clase para manejar solo fechas (día, mes, año) sin importar la hora.
import java.time.LocalDate;
// Importa la clase para manejar fechas completas con hora exacta, minutos y segundos.
import java.time.LocalDateTime;
// Importa la herramienta matemática de Java para calcular diferencias de tiempo (ej. contar días).
import java.time.temporal.ChronoUnit;
// Importa la estructura de datos para manejar listas.
import java.util.List;
// Importa la estructura de datos para manejar conjuntos sin elementos repetidos.
import java.util.Set;
// Importa el tipo de dato que maneja los identificadores únicos universales.
import java.util.UUID;
// Importa la herramienta de manipulación de flujos de datos (Streams) para filtrar y transformar listas.
import java.util.stream.Collectors;

// Le indica a Spring que esta clase es un robot o motor que debe arrancar junto con el sistema.
@Component
// Crea un constructor oculto inyectando todos los repositorios y servicios que marcamos como "final".
@RequiredArgsConstructor
// Habilita la variable reservada "log" para poder imprimir advertencias y mensajes de éxito en consola.
@Slf4j
// Declaración pública de la clase que funciona como el orquestador de tiempos del sistema.
public class VehicleStateScheduler {

    // Repositorio central que permite leer, guardar o modificar los vehículos en la base de datos.
    private final VehicleRepository vehicleRepository;
    // Repositorio que maneja todo el tema de agendas y compromisos de la flota.
    private final ReservaRepository reservaRepository;
    // Servicio de alto nivel encargado de orquestar los trámites distribuidos y compensaciones.
    private final SagaService sagaService;
    // Repositorio que funciona como la caja negra o bitácora de auditoría de los cambios de estado.
    private final HistorialEstadoRepository historialEstadoRepository;

    /**
     * PATRÓN: Background State Synchronizer
     * Se ejecuta automáticamente cada 60 segundos (fixedRate = 60000 ms).
     * Revisa el reloj del servidor y ajusta la flota de manera transparente.
     */
    // Programa esta tarea para que se repita cada 60 milisegundos ininterrumpidamente.
    @Scheduled(fixedRate = 60000)
    // Asegura que los cambios en vehículos y auditoría se guarden juntos, o no se guarde ninguno.
    @Transactional
    // Método encargado de que la flota coincida con la agenda de los clientes.
    public void sincronizarEstadosPorAgenda() {
        // Capturamos el instante exacto del servidor para comparar.
        LocalDateTime ahora = LocalDateTime.now();
        // Definimos los dos estados en los que una reserva retiene legítimamente un camión.
        List<EstadoReserva> estadosOperativos = List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA);

        // Buscamos todas las reservas que deberían estar activas en este mismo segundo.
        List<ReservaVehiculo> reservasActivasAhora = reservaRepository
                .findCurrentlyActiveReservations(ahora, estadosOperativos);

        // Extraemos únicamente los IDs de los camiones cuya reserva ya fue confirmada.
        Set<UUID> vehiculosQueDebenEstarReservados = reservasActivasAhora.stream()
                // Filtramos la lista descartando cualquier reserva que siga pendiente.
                .filter(reserva -> reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA)
                // Obtenemos el ID físico del vehículo.
                .map(reserva -> reserva.getVehiculo().getIdVehiculo())
                // Convertimos el resultado en una lista sin repetidos.
                .collect(Collectors.toSet());

        // Iniciamos el barrido de todas las reservas vigentes encontradas.
        for (ReservaVehiculo reserva : reservasActivasAhora) {
            // Extraemos el objeto físico del vehículo de la reserva.
            Vehiculo vehiculo = reserva.getVehiculo();
            
            // Evaluamos si esta reserva específica ya fue confirmada oficialmente.
            if (reserva.getEstadoReserva() == EstadoReserva.CONFIRMADA) {
                // Evaluamos si el vehículo está encendido (activo) y estacionado libremente (DISPONIBLE).
                if (Boolean.TRUE.equals(vehiculo.getActivo()) && vehiculo.getEstadoVehiculo() == EstadoVehiculo.DISPONIBLE) {
                    // Imprimimos en consola que el sistema bloqueará el vehículo para su cliente.
                    log.info("AUTOMATIZACIÓN: El vehículo {} ha entrado en su rango de reserva CONFIRMADA. Cambiando a RESERVADO.", vehiculo.getNumeroPlaca());

                    // Guardamos la huella imborrable del cambio en la bitácora del sistema.
                    historialEstadoRepository.save(HistorialEstadoVehiculo.builder()
                            // Le asociamos el camión afectado.
                            .vehiculo(vehiculo)
                            // Documentamos el estado antiguo.
                            .estadoAnterior(EstadoVehiculo.DISPONIBLE.name())
                            // Documentamos el estado nuevo y restrictivo.
                            .estadoNuevo(EstadoVehiculo.RESERVADO.name())
                            // Dejamos el motivo por el cual la agenda bloqueó el camión.
                            .motivoCambio("Inicio automático de ventana de tiempo de la reserva ID: " + reserva.getIdReserva())
                            // Marcamos que fue un robot el que hizo este cambio.
                            .servicioOrigen("fleetops-time-scheduler")
                            // Guardamos la hora del servidor en la que ocurrió esto.
                            .registradoEn(ahora)
                            // Ensamblamos el objeto de auditoría.
                            .build());

                    // Cambiamos la propiedad del vehículo para indicarle a toda la red que está apartado.
                    vehiculo.setEstadoVehiculo(EstadoVehiculo.RESERVADO);
                    // Actualizamos su reloj de control interno.
                    vehiculo.setActualizadoEn(ahora);
                    // Hacemos el UPDATE físico en la tabla de vehículos de la base de datos.
                    vehicleRepository.save(vehiculo);
                }
            // Por el contrario, si llegó la hora del viaje pero la reserva sigue pendiente de confirmación...
            } else if (reserva.getEstadoReserva() == EstadoReserva.PENDIENTE) {
                // Lanzamos una alerta al administrador en la consola advirtiendo el incumplimiento del cliente.
                log.warn("AUTOMATIZACIÓN: La reserva ID [{}] alcanzó su fecha de inicio pero sigue PENDIENTE. Cancelando trámite (Saga)...", reserva.getIdReserva());
                
                // Llamamos a nuestro patrón de Sagas para destruir el trámite y liberar el camión correctamente.
                sagaService.compensarPorReservaId(
                        // Pasamos el ID exacto de la reserva a abortar.
                        reserva.getIdReserva(),
                        // Escribimos la justificación oficial para la auditoría contable.
                        "Cancelación automática: Se alcanzó la fecha de inicio del viaje sin recibir confirmación previa.");
            }
        }

        // Buscamos ahora todos los vehículos que en este momento aparecen como "RESERVADOS" en todo el sistema.
        List<Vehiculo> vehiculosReservadosEnBd = vehicleRepository.findAll().stream()
                // Filtramos para ignorar a los dados de baja lógicamente y a los que tienen estados distintos.
                .filter(v -> Boolean.TRUE.equals(v.getActivo()) && v.getEstadoVehiculo() == EstadoVehiculo.RESERVADO)
                // Retornamos una lista inmutable con los resultados.
                .toList();

        // Iniciamos un ciclo para revisar camión por camión los que están retenidos.
        for (Vehiculo vehiculo : vehiculosReservadosEnBd) {
            // Verificamos si este camión NO está en la lista de los que deberían estar retenidos en este instante.
            if (!vehiculosQueDebenEstarReservados.contains(vehiculo.getIdVehiculo())) {
                // Imprimimos un mensaje de éxito avisando que el viaje terminó y el camión queda libre.
                log.info("AUTOMATIZACIÓN: La ventana de tiempo de reserva para el vehículo {} ha expirado. Volviendo a DISPONIBLE.", vehiculo.getNumeroPlaca());

                // Abrimos un nuevo registro en la bitácora para auditar la liberación.
                historialEstadoRepository.save(HistorialEstadoVehiculo.builder()
                        // Amarramos el registro al vehículo.
                        .vehiculo(vehiculo)
                        // Aclaramos que antes estaba ocupado.
                        .estadoAnterior(EstadoVehiculo.RESERVADO.name())
                        // Aclaramos que ahora quedó libre para ser rentado de nuevo.
                        .estadoNuevo(EstadoVehiculo.DISPONIBLE.name())
                        // Explicamos el motivo comercial del cambio de estado.
                        .motivoCambio("Finalización automática del bloque de tiempo programado en agenda")
                        // Identificamos que el cronjob ejecutó la acción.
                        .servicioOrigen("fleetops-time-scheduler")
                        // Marcamos el momento de la liberación.
                        .registradoEn(ahora)
                        // Construimos el comprobante de auditoría.
                        .build());

                // Asignamos al vehículo el estado oficial de disponibilidad.
                vehiculo.setEstadoVehiculo(EstadoVehiculo.DISPONIBLE);
                // Le sellamos la fecha de actualización reciente.
                vehiculo.setActualizadoEn(ahora);
                // Ejecutamos la instrucción de guardado en PostgreSQL.
                vehicleRepository.save(vehiculo);
            }
        }
    }


    // =========================================================================
    // AUTOMATIZACIÓN: CANCELACIÓN POR TIMEOUT (4 MINUTOS)
    // =========================================================================

    // Se ejecuta cada 30 segundos (30000 milisegundos) para garantizar alta precisión.
    @Scheduled(fixedRate = 30000)
    // Protege la compensación dentro de una transacción.
    @Transactional
    // Método que actúa como perro guardián para abortar reservas inconclusas.
    public void cancelarReservasExpiradas() {

        // Calculamos la línea de tiempo límite: retrocedemos exactamente 4 minutos desde el reloj actual.
        LocalDateTime tiempoLimite = LocalDateTime.now().minusMinutes(4);

        // Consultamos a la base de datos por los trámites que jamás pasaron a confirmados y ya excedieron el tiempo.
        List<ReservaVehiculo> expiradas = reservaRepository.findByEstadoReservaAndCreadoEnBefore(EstadoReserva.PENDIENTE, tiempoLimite);

        // Si la lista de infractores no está vacía...
        if (!expiradas.isEmpty()) {
            // Imprimimos la advertencia indicando la cantidad de transacciones muertas encontradas.
            log.info("CRON TIMEOUT: Se encontraron {} reservas PENDIENTES vencidas. Iniciando limpieza...",
                    expiradas.size());
        }

        // Recorremos una por una las reservas a eliminar.
        for (ReservaVehiculo reserva : expiradas) {
            // Escribimos una advertencia crítica (Warning) notificando el identificador abortado.
            log.warn("Reserva ID [{}] expiró (Superó los 4 min). Compensando...", reserva.getIdReserva());

            // Ordenamos al motor de Sagas destruir el trámite y restaurar el ecosistema de microservicios.
            sagaService.compensarPorReservaId(
                    // Enviamos el ID de la transacción atascada.
                    reserva.getIdReserva(),
                    // Justificamos el Rollback por el incumplimiento de los tiempos comerciales.
                    "Timeout: Reserva no confirmada en la ventana de 4 minutos");
        }
    }

    // =========================================================================
    // AUTOMATIZACIÓN: REVISIÓN LEGAL DE VEHÍCULOS (VENCIDOS Y PRÓXIMOS)
    // =========================================================================

    // Se ejecuta cada 60 segundos (60000 ms). Ideal para protección activa de la flota.
    @Scheduled(fixedRate = 60000)
    // Ejecuta las operaciones en bloque. Si ocurre un fallo, los cambios se revierten para proteger la base.
    @Transactional
    // Método que audita y sanciona vehículos que incumplen las leyes de tránsito.
    public void auditarVencimientoDocumentosLegales() {
        // Inicializamos la variable 'hoy' obteniendo la fecha exacta del sistema operativo.
        LocalDate hoy = LocalDate.now();

        // Extraemos un listado general de absolutamente todos los vehículos matriculados en la base de datos.
        List<Vehiculo> todosLosVehiculos = vehicleRepository.findAll();

        // Iniciamos un bucle repetitivo para analizar la carpeta legal de cada uno de los camiones.
        for (Vehiculo vehiculo : todosLosVehiculos) {

            // Creamos una regla: Sólo revisaremos camiones activos y que no estén ya castigados (FUERA_DE_SERVICIO).
            if (Boolean.TRUE.equals(vehiculo.getActivo()) && vehiculo.getEstadoVehiculo() != EstadoVehiculo.FUERA_DE_SERVICIO) {

                // Preparamos contenedores de texto limpios para evaluar individualmente cada documento.
                String mensajeSoat = "";
                String mensajeRtm = "";

                // ==========================================
                // 1. EVALUACIÓN DEL SEGURO OBLIGATORIO (SOAT)
                // ==========================================
                // Verificamos primero el peor caso: ¿El documento no existe o su fecha ya quedó en el pasado?
                if (vehiculo.getFechaSoat() == null || vehiculo.getFechaSoat().isBefore(hoy)) {
                    // Asignamos el mensaje indicando que la irregularidad es actual y definitiva.
                    mensajeSoat = "el SOAT ya se encuentra vencido (o no está registrado)";
                
                // Si el documento está vigente, verificamos si su margen de vida útil es menor o igual a 7 días.
                } else if (ChronoUnit.DAYS.between(hoy, vehiculo.getFechaSoat()) <= 7) {
                    // Asignamos el mensaje preventivo advirtiendo la proximidad de la caducidad.
                    mensajeSoat = "el SOAT está próximo a vencer en menos de 7 días";
                }

                // ==========================================
                // 2. EVALUACIÓN DE LA REVISIÓN TÉCNICO MECÁNICA (RTM)
                // ==========================================
                // Verificamos si la mecánica carece de registro o si matemáticamente ya expiró.
                if (vehiculo.getFechaRtm() == null || vehiculo.getFechaRtm().isBefore(hoy)) {
                    // Reportamos que el vehículo es mecánicamente ilegal para circular.
                    mensajeRtm = "la RTM ya se encuentra vencida (o no está registrada)";
                
                // Si la mecánica aún sirve, evaluamos si está dentro de la peligrosa ventana de los 7 días.
                } else if (ChronoUnit.DAYS.between(hoy, vehiculo.getFechaRtm()) <= 7) {
                    // Asignamos la alerta de vencimiento inminente de los chequeos mecánicos.
                    mensajeRtm = "la RTM está próxima a vencer en menos de 7 días";
                }

                // ==========================================
                // 3. TOMA DE DECISIÓN Y ENSAMBLAJE DE CASTIGO
                // ==========================================
                // Si cualquiera de los dos documentos generó un texto de alerta, procedemos a inmovilizar.
                if (!mensajeSoat.isEmpty() || !mensajeRtm.isEmpty()) {

                    // Preparamos un contenedor para la sentencia final que irá a la base de datos.
                    String mensajeMotivo = "";

                    // Evaluamos si el vehículo violó AMBAS regulaciones (SOAT y RTM) al mismo tiempo.
                    if (!mensajeSoat.isEmpty() && !mensajeRtm.isEmpty()) {
                        // Concatenamos las dos sentencias para dar al auditor un reporte perfectamente detallado.
                        mensajeMotivo = "Inmovilización por múltiples causas legales: " + mensajeSoat + " y " + mensajeRtm + ".";
                    
                    // Si llegamos aquí, evaluamos si el causante de la inmovilización fue únicamente el Seguro (SOAT).
                    } else if (!mensajeSoat.isEmpty()) {
                        // Construimos el mensaje exclusivo para la infracción del seguro.
                        mensajeMotivo = "Inmovilización por restricción legal: " + mensajeSoat + ".";
                    
                    // Por descarte absoluto, si no fueron ambas ni fue el SOAT, el castigo es únicamente por la RTM.
                    } else {
                        // Construimos el mensaje exclusivo para la caducidad mecánica.
                        mensajeMotivo = "Inmovilización por restricción legal: " + mensajeRtm + ".";
                    }

                    // Hacemos una copia de seguridad temporal del estado original del vehículo (Ej: DISPONIBLE).
                    EstadoVehiculo estadoAnterior = vehiculo.getEstadoVehiculo();

                    // Disparamos un reporte tipo 'WARNING' en la consola para notificar de inmediato al centro de mando.
                    log.warn("AUTOMATIZACIÓN: Inmovilizando vehículo [{}]. Motivo: {}", vehiculo.getNumeroPlaca(), mensajeMotivo);

                    // Reasignamos el atributo oficial del vehículo para castigarlo sacándolo de servicio.
                    vehiculo.setEstadoVehiculo(EstadoVehiculo.FUERA_DE_SERVICIO);
                    
                    // Actualizamos el sello de tiempo nativo del vehículo para reflejar que acaba de ser bloqueado.
                    vehiculo.setActualizadoEn(LocalDateTime.now());
                    
                    // Enviamos la orden final a Hibernate para forzar el UPDATE de castigo en PostgreSQL.
                    vehicleRepository.save(vehiculo);

                    // Desplegamos el constructor (Builder) de Lombok para generar un recibo de auditoría impecable.
                    HistorialEstadoVehiculo historial = HistorialEstadoVehiculo.builder()
                            // Enganchamos este documento de bitácora al vehículo que acabamos de inmovilizar.
                            .vehiculo(vehiculo)
                            // Documentamos forensemente cómo estaba operando el camión antes del bloqueo legal.
                            .estadoAnterior(estadoAnterior.name())
                            // Indicamos el estado exacto (Fuera de Servicio) en el que queda confinado el vehículo.
                            .estadoNuevo(EstadoVehiculo.FUERA_DE_SERVICIO.name())
                            // Depositamos la variable de texto que ensamblamos minuciosamente unas líneas atrás.
                            .motivoCambio(mensajeMotivo)
                            // Acusamos al robot automatizado de ser el autor estricto de esta inmovilización.
                            .servicioOrigen("fleetops-legal-scheduler")
                            // Sincronizamos la fecha del reporte con el mismo milisegundo de ejecución actual.
                            .registradoEn(LocalDateTime.now())
                            // Ordenamos que se acople y ensamble la ficha completa de historial.
                            .build();

                    // Procedemos a guardar la ficha recién creada dentro del archivo permanente (Tabla Historial).
                    historialEstadoRepository.save(historial);

                // Cerramos el bloque if que ejecuta la sentencia.
                }

            // Cerramos el bloque if que filtra camiones válidos.
            }

        // Cerramos el bucle general de inspección de flota.
        }

    // Cerramos formalmente el método automático de auditoría legal de documentos.
    }
// Cerramos la declaración principal del orquestador (VehicleStateScheduler).
}