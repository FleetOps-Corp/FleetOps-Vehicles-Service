package com.fleetops.vehicles.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

//
// PATRÓN DE DISEÑO DETECTADO: Circuit Breaker (Cortocircuito de resiliencia)
// ¿Qué problema resuelve en FleetOps? El microservicio de Vehículos recibe llamadas
// desde otros microservicios: Asignaciones le pide reservar un vehículo, Mantenimiento
// le pide cambiar el estado a "en mantenimiento", e Incidentes le pide bloquear un vehículo
// accidentado. Si Vehículos empieza a fallar (por ejemplo, la base de datos se cae), esas
// llamadas se acumularían esperando respuesta y podrían tumbar también a los servicios
// que dependen de él.
// ¿Cómo lo soluciona? Funciona como el "totalizador" (disyuntor eléctrico) de una casa:
// si detecta que algo está fallando demasiado, "abre el circuito" y corta el paso de
// corriente (las llamadas) antes de que se queme todo el sistema. Tiene 3 estados:
//   CLOSED    → Todo normal, las llamadas pasan sin problema.
//   OPEN      → Demasiados fallos detectados, bloquea todo y responde error de inmediato.
//   HALF-OPEN → Deja pasar unas pocas llamadas de prueba para ver si ya se recuperó.
// Ejemplo con FleetOps: si Asignaciones llama 5 veces seguidas a
// POST /vehiculos/{id}/reservas y todas fallan, el circuito se "abre" y Asignaciones
// recibe un error rápido en vez de quedarse esperando para siempre, evitando que el
// problema de Vehículos se contagie a Asignaciones.
//
// @Configuration se usa justo aquí, sobre la clase: le dice a Spring "esta clase no es
// un objeto de negocio, es un plano de configuración; revísala al arrancar para crear
// los objetos (Beans) que define adentro". Ejemplo: es como el plano eléctrico de una casa
// que indica dónde debe ir cada fusible antes de que la casa esté lista para habitarse.
@Configuration
public class Resilience4jConfig {

    // @Bean se usa justo aquí, sobre el método: le dice a Spring "el objeto que devuelve
    // este método debe quedar disponible para que cualquier otra parte del sistema lo pueda
    // pedir e inyectar, sin tener que crearlo de nuevo cada vez". Ejemplo con FleetOps: es como
    // dejar la herramienta de "medidor de fallas" en la caja de herramientas común del taller,
    // disponible para cualquier servicio (VehicleService, SagaService, etc.) que la necesite.
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {

        // PATRÓN DE DISEÑO DETECTADO: Builder
        // En vez de crear el objeto CircuitBreakerConfig con un constructor lleno de parámetros
        // difíciles de leer, se va configurando paso a paso con métodos encadenados y al final
        // se llama ".build()" para "cerrar" la construcción y obtener el objeto terminado.
        // Ejemplo con FleetOps: es como armar la ficha técnica de un vehículo paso a paso
        // (primero el tipo, luego la placa, luego la capacidad de carga) y solo cuando todos
        // los datos están listos, se "guarda" el registro completo.
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()

                // slidingWindowSize(10): le decimos que solo analice las últimas 10 llamadas
                // hechas al microservicio de Vehículos para decidir si algo anda mal, en vez
                // de revisar todo el historial desde que arrancó el sistema.
                // Ejemplo: es como revisar solo el clima de los últimos 10 minutos antes de
                // decidir si sales con paraguas, en vez de ver el clima de todo el mes.
                .slidingWindowSize(10)

                // minimumNumberOfCalls(5): exige que haya al menos 5 llamadas registradas antes
                // de empezar a calcular el porcentaje de fallos, para no sacar conclusiones
                // apresuradas con muy pocos datos.
                // Ejemplo: no podemos decir que un vehículo nuevo "consume mucha gasolina"
                // si solo ha recorrido 5 metros; necesitamos varios recorridos para confiar
                // en el dato.
                .minimumNumberOfCalls(5)

                // failureRateThreshold(50): si el 50% o más de esas últimas llamadas fallaron,
                // el circuito se considera "enfermo" y se abre.
                // Ejemplo: si la mitad de las veces que llamas a un compañero del área técnica
                // no contesta, decides dejar de llamarlo por un rato y buscar otra forma de
                // resolver el problema.
                .failureRateThreshold(50)

                // waitDurationInOpenState(Duration.ofSeconds(10)): una vez que el circuito se
                // abrió (detectó muchos fallos), espera 10 segundos antes de intentar de nuevo,
                // dando tiempo a que el servicio que estaba fallando se recupere.
                // Ejemplo: es como cuando un vehículo se sobrecalienta y lo dejas "reposar"
                // unos minutos antes de volver a encenderlo, en vez de forzarlo de inmediato.
                .waitDurationInOpenState(Duration.ofSeconds(10))

                // permittedNumberOfCallsInHalfOpenState(3): pasado ese tiempo de espera, deja
                // pasar solo 3 llamadas de "prueba" para verificar si el servicio ya volvió
                // a la normalidad, en vez de abrir todo de golpe otra vez.
                // Ejemplo: antes de mandar de nuevo un vehículo recién reparado a operación
                // completa, primero se hacen 3 recorridos cortos de prueba para confirmar
                // que ya no tiene fallas.
                .permittedNumberOfCallsInHalfOpenState(3)

                // .build() cierra la configuración armada paso a paso y entrega el objeto
                // final listo para ser usado.
                .build();

        // Se crea el registro central de circuitos usando la configuración anterior.
        // Este registro es el "tablero de fusibles" que controla, por ejemplo, el circuito
        // que protege las llamadas hacia la operación de reservar vehículos (parte de la Saga).
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }
}