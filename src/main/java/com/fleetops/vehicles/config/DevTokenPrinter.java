// Define el paquete donde vive nuestra configuración de arranque para el microservicio de Vehículos.
package com.fleetops.vehicles.config;

// Trae la herramienta que fabrica las "llaves" de seguridad (Tokens) para entrar al sistema.
import com.fleetops.vehicles.util.JwtTokenGenerator;
// Trae el registro para imprimir mensajes en la pantalla del servidor (la consola).
import org.slf4j.Logger;
// Trae la fábrica para crear el objeto que escribe esos mensajes.
import org.slf4j.LoggerFactory;
// Trae una regla de Spring que nos permite ejecutar código automáticamente al encender la aplicación.
import org.springframework.boot.CommandLineRunner;
// Trae la capacidad de activar o desactivar clases según el entorno (desarrollo o producción).
import org.springframework.context.annotation.Profile;
// Registra esta clase como un trabajador dentro del sistema de Spring.
import org.springframework.stereotype.Component;

// @Component le dice a Spring "esta clase debe existir como un objeto único que tú administras,
// no hace falta que el programador la cree a mano con 'new'". Es como decirle al área de
// mantenimiento de FleetOps "este chequeo de arranque siempre debe estar listo en el sistema,
// no hay que armarlo cada vez que alguien lo necesite".
@Component
// @Profile("!prod") le dice a Spring "solo crea y ejecuta esta clase si el ambiente activo
// NO es 'prod' (producción)". El signo "!" significa "negación", es decir "diferente de".
// Ejemplo con FleetOps: es como decir "estos tokens de prueba solo se imprimen en el taller
// de pruebas de la empresa, nunca en el sistema real donde trabajan los conductores y administradores".
@Profile("!prod")
//
// PATRÓN DE DISEÑO DETECTADO: Command (Comando de Arranque)
// ¿Qué hace esta clase? Implementa la interfaz "CommandLineRunner", que es la forma que tiene
// Spring de decir "esta clase representa una tarea (un comando) que se debe ejecutar automáticamente
// una sola vez, justo cuando el sistema termine de encender".
// Ejemplo sencillo con FleetOps: imagina que cada mañana, al abrir las oficinas de FleetCorp,
// el guardia de seguridad automáticamente imprime las claves de acceso temporales del día para
// los empleados que van a hacer pruebas del sistema de vehículos. Nadie tiene que pedírselo:
// es una tarea programada que se dispara sola al "abrir la tienda" (encender la aplicación).
public class DevTokenPrinter implements CommandLineRunner {

    // Creamos un "logger", que es el objeto encargado de imprimir mensajes ordenados en la consola
    // (en vez de usar System.out.println, que es menos profesional y menos configurable).
    private static final Logger log = LoggerFactory.getLogger(DevTokenPrinter.class);

    // Guardamos una referencia a la herramienta que genera tokens JWT.
    // "final" significa que, una vez asignada en el constructor, esta referencia no se puede cambiar.
    private final JwtTokenGenerator jwtTokenGenerator;

    //
    // PATRÓN DE DISEÑO DETECTADO: Inyección de Dependencias
    // En vez de que esta clase escriba "new JwtTokenGenerator()" para crear su propia herramienta
    // de generar tokens, Spring se la entrega ya construida a través del constructor.
    // Ejemplo con FleetOps: es como cuando el área de Vehículos necesita un escáner de placas;
    // en vez de fabricarlo ellos mismos, el departamento de logística ya les entrega el escáner
    // listo para usar. Esto permite que, si un día cambia la forma de generar tokens (por ejemplo,
    // se cambia de JWT a otro mecanismo), esta clase no tenga que modificarse, solo se cambia
    // la pieza que se inyecta.
    public DevTokenPrinter(JwtTokenGenerator jwtTokenGenerator) {
        // Guardamos la herramienta recibida en el atributo de la clase para poder usarla luego en "run".
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    // @Override significa que este método ya existe definido en la interfaz CommandLineRunner
    // y aquí estamos escribiendo nuestra propia versión de qué hacer cuando se ejecute.
    // Ejemplo sencillo: es como llenar un formulario que ya viene con una pregunta impresa
    // ("¿Qué se debe hacer al arrancar el microservicio de Vehículos?") y aquí escribimos la respuesta.
    @Override
    // Este es el método que Spring ejecuta automáticamente al iniciar la aplicación.
    // "String... args" significa que puede recibir cero, uno o varios argumentos de texto
    // (por ejemplo, parámetros que se pasen al ejecutar el programa desde la terminal).
    public void run(String... args) {

        // Imprime una línea decorativa para que el bloque de tokens se vea ordenado en consola.
        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        
        // Imprime un título que avisa al desarrollador qué información viene a continuación.
        log.info("║                    TOKENS JWT PARA DESARROLLO - FLEETOPS                   ║");
        
        // Imprime otra línea divisoria para separar visualmente el título del contenido.
        log.info("╠════════════════════════════════════════════════════════════════════════════╣");

        // Le pedimos al generador que cree un token con permisos de Administrador.
        // Esto sirve para probar en Postman como si fuéramos, por ejemplo, el administrador
        // de flota de FleetCorp que puede crear, editar o eliminar vehículos sin restricciones.
        String adminToken = jwtTokenGenerator.generateAdminToken();
        
        // Imprime una etiqueta indicando que lo que sigue es el token de administrador.
        log.info("║  ADMIN TOKEN:                                                              ║");
        // Imprime el valor real del token generado para el administrador.
        log.info("║  {} ║", adminToken);

        // Le pedimos al generador que cree un token con permisos de Operador, es decir,
        // alguien como el encargado del taller que solo puede, por ejemplo, registrar
        // cambios de estado de un vehículo (en mantenimiento, disponible, etc.) pero no
        // borrar información sensible del sistema.
        String operadorToken = jwtTokenGenerator.generateOperadorToken();
        
        // Imprime una línea vacía solo para dar espacio y que la consola sea más legible.
        log.info("║                                                                            ║");
        // Etiqueta que indica que sigue el token de operador.
        log.info("║  OPERADOR TOKEN:                                                           ║");
        // Imprime el token de operador generado.
        log.info("║  {} ║", operadorToken);

        // Le pedimos al generador un token para un Usuario Autorizado, por ejemplo
        // un empleado de FleetCorp que solo necesita consultar la disponibilidad
        // de un vehículo para solicitarlo, sin poder modificar nada.
        String usuarioToken = jwtTokenGenerator.generateUsuarioAutorizadoToken();
        
        // Espacio en blanco para mantener el formato ordenado.
        log.info("║                                                                            ║");
        // Etiqueta que indica que sigue el token de usuario autorizado.
        log.info("║  USUARIO AUTORIZADO TOKEN:                                                  ║");
        // Imprime el token del usuario autorizado.
        log.info("║  {} ║", usuarioToken);

        // Mensaje final que le dice al desarrollador exactamente qué hacer con esos tokens:
        // copiarlos y pegarlos en Postman en la pestaña de autenticación "Bearer Token"
        // para poder probar, por ejemplo, los endpoints de creación o consulta de vehículos.
        log.info(">>> Copia los tokens de arriba y úsalos en Postman como Bearer Token <<<");
    }
}