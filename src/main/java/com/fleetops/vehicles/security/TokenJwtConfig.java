// Define el paquete exclusivo para la configuración y control perimetral de seguridad del microservicio.
package com.fleetops.vehicles.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

// ─────────────────────────────────────────────────────────────────────────────
// PATRÓN APLICADO: Provider Configuration / Lazy Initialization Component
//
// ¿Por qué existe aquí?
// La clave secreta para firmar y validar tokens JWT se almacena habitualmente como una 
// cadena de texto plano (String) en las variables de entorno o archivos de configuración. 
// Sin embargo, las librerías criptográficas modernas (como JJWT) requieren un objeto de 
// tipo 'SecretKey' tipado y estructurado bajo el algoritmo HMAC-SHA para procesar firmas.
//
// ¿Cómo lo soluciona?
// Encapsula el String crudo inyectado por Spring y expone un método roscado con el 
// patrón 'Lazy Initialization' (Inicialización Tardía). Esto transforma la cadena en una 
// llave criptográfica real únicamente en el instante en que el filtro de seguridad lo 
// solicita por primera vez, reutilizando la instancia en memoria para peticiones posteriores.
//
// Ejemplo de bolsillo:
// Es exactamente igual a tener un cupón de descuento digital en tu teléfono (String secreto). 
// El cupón es solo texto, pero en el milisegundo exacto en que llegas a la caja de la tienda, 
// la aplicación web genera un código de barras de alta seguridad (SecretKey) listo para 
// ser escaneado y validado por el sistema de cobro.
// ─────────────────────────────────────────────────────────────────────────────

@Component
// @Component: Spring registra esta clase al arranque de la aplicación como un Bean único 
// de lectura global (Singleton) dentro del contenedor de inversión de control (IoC).
public class TokenJwtConfig {

    @Value("${jwt.secret}")
    // @Value: Acopla la propiedad declarada externamente en la infraestructura (application.yml) 
    // e inyecta dinámicamente el valor del secreto al instanciarse la clase.
    private String secret;

    // secretKey: Almacén intermedio que retendrá la clave procesada en memoria.
    private SecretKey secretKey;

    /**
     * Resuelve y retorna la clave de verificación criptográfica simétrica adaptada para JJWT.
     * Implementa un bloqueo preventivo simple para mitigar re-procesamientos de bytes innecesarios.
     *
     * @return Objeto SecretKey configurado bajo especificaciones seguras de HMAC-SHA.
     */
    public SecretKey getSecretKey() {

        // ─────────────────────────────────────────────────────────────────────────────
        // MECANISMO TÉCNICO: Lazy Initialization (Inicialización Perezosa)
        //
        // Evitamos transformar el String a bytes repetidamente en cada petición HTTP concurrente. 
        // Evaluamos si el contenedor de la clave se encuentra vacío (null). Si es afirmativo, 
        // el método invoca la factoría criptográfica para forjar la clave una sola vez y 
        // guardarla en la propiedad de la clase. Las llamadas siguientes leerán la variable 
        // directamente desde la memoria RAM, optimizando la latencia de respuesta.
        // ─────────────────────────────────────────────────────────────────────────────
        if (secretKey == null) {
            
            // hmacShaKeyFor: Analiza la longitud del arreglo de bytes y levanta una clave simétrica 
            // robusta apta para firmar algoritmos HS256, HS384 o HS512 de forma transparente.
            secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        }
        
        return secretKey;
    }
}