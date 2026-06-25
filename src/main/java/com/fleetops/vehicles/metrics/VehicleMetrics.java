// Define el paquete exclusivo para la instrumentación de telemetría y monitoreo del microservicio.
package com.fleetops.vehicles.metrics;

// Importa el enumerado que define los estados posibles (DISPONIBLE, EN_USO, etc.).
import com.fleetops.vehicles.models.entities.EstadoVehiculo;
// Importa el repositorio para poder consultar la base de datos y contar los vehículos.
import com.fleetops.vehicles.repositories.VehicleRepository;
// Importa la clase Gauge de Micrometer para crear métricas que suben y bajan (como el combustible).
import io.micrometer.core.instrument.Gauge;
// Importa el registro central donde se inscribirán todas las métricas para que Prometheus las vea.
import io.micrometer.core.instrument.MeterRegistry;
// Importa la anotación de Spring para convertir esta clase en un componente gestionado.
import org.springframework.stereotype.Component;

// =========================================================================================
// PATRÓN DE DISEÑO: Metrics Instrumentation (Observabilidad)
// ¿Qué hace? Instala "sensores digitales" dentro del sistema para medir el estado real de la flota
// sin tener que generar reportes manuales.
// =========================================================================================

// @Component: Registra esta clase como un Bean Singleton en Spring al arrancar.
// Esto garantiza que los sensores se creen una sola vez al inicio.
@Component
public class VehicleMetrics {

    // Referencia al repositorio de datos; es nuestra fuente de verdad.
    // 'final' asegura que la conexión no sea reemplazada durante la vida de la app.
    private final VehicleRepository vehicleRepository;

    // Constructor: Ejecutado por Spring (Inyección de Dependencias) al iniciar.
    // Recibe el repositorio de datos y el registro central de métricas de Micrometer.
    public VehicleMetrics(VehicleRepository vehicleRepository, MeterRegistry meterRegistry) {

        // Asignamos el repositorio recibido a nuestra variable de clase.
        this.vehicleRepository = vehicleRepository;

        // =========================================================================================
        // PATRÓN DE DISEÑO: DRY (Don't Repeat Yourself)
        // En lugar de escribir 5 bloques de código para cada estado, iteramos sobre el Enum.
        // =========================================================================================
        
        // Iniciamos un bucle que recorre todos los estados definidos en el sistema (Ej: DISPONIBLE, MANTENIMIENTO).
        for (EstadoVehiculo estado : EstadoVehiculo.values()) {

            // =========================================================================================
            // CONCEPTO TÉCNICO: Gauge (Medidor)
            // A diferencia de un contador, el Gauge es para medir un estado actual que fluctúa.
            // =========================================================================================
            
            // Gauge.builder: Inicia la configuración del sensor.
            // Argumento 1: "fleetops_vehiculos_por_estado" (Nombre que verá Prometheus en su base de datos).
            // Argumento 2: vehicleRepository (El objeto que sabe cómo consultar la base de datos).
            // Argumento 3: (Lambda) La lógica exacta para obtener el valor cuando se consulte.
            Gauge.builder("fleetops_vehiculos_por_estado", vehicleRepository,
                            // REGLA DE NEGOCIO: Filtrado de flota.
                            // Aquí consultamos a la base de datos filtrando por estado Y por 'activo = true'.
                            // Si un camión está dado de baja, no debe aparecer en las métricas operativas.
                            repo -> repo.countByEstadoVehiculoAndActivoTrue(estado))
                    
                    // .tag(): Añade una dimensión. Permite a Grafana agrupar métricas por estado.
                    // Ejemplo: El resultado será 'fleetops_vehiculos_por_estado{estado="disponible"} 42.0'.
                    .tag("estado", estado.name().toLowerCase())

                    // .description(): Texto descriptivo para que el ingeniero de DevOps sepa qué mide este sensor.
                    .description("Cantidad de vehículos activos por estado")

                    // .register(meterRegistry): Inscribe el medidor en el registro oficial de Spring Actuator.
                    // Sin esta línea, Prometheus nunca vería estos datos.
                    .register(meterRegistry);
            
            // Fin del bucle for.
        }
    }
}