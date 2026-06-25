// Define el paquete exclusivo para las utilidades transversales (Helper classes / Utility Layer).
package com.fleetops.vehicles.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// PATRÓN APLICADO: Security Token Factory / Development Security Provider
//
// ¿Por qué existe aquí?
// Durante el ciclo de desarrollo, la automatización de pruebas de integración (CI/CD) 
// o la emulación local de microservicios, depender de un servidor externo de identidad 
// (como Keycloak o Auth0) introduce latencia, inestabilidad por caídas de red y 
// complejidad innecesaria en los entornos locales de los programadores.
//
// ¿Cómo lo soluciona?
// Centraliza la lógica de generación acelerada de JSON Web Tokens (JWT) firmados 
// criptográficamente. Permite forjar credenciales válidas al vuelo simulando identidades 
// de diversos escalafones jerárquicos (ADMIN, OPERADOR) con una firma homóloga a la 
// que validará perimetralmente nuestro 'JwtValidationFilter'.
//
// Ejemplo de bolsillo:
// Es exactamente igual a la máquina que emite pases temporales de visitante en la 
// garita de seguridad de una central de carga. No necesitas llamar a la junta directiva 
// nacional para autorizar un recorrido de mantenimiento local; el software genera una 
// tarjeta magnética válida por 90 días con los permisos exactos que requiere el técnico.
// ─────────────────────────────────────────────────────────────────────────────

@Component
// @Component: Registra esta fábrica automatizada como un Bean administrado en la memoria 
// RAM del contenedor IoC de Spring, permitiendo su inyección limpia en scripts de semillas de datos (Seeders).
public class JwtTokenGenerator {

    @Value("${jwt.secret}")
    // @Value: Sincroniza el secreto criptográfico compartido inyectado desde la infraestructura local.
    private String secret;

    /**
     * Factoría interna para transformar el String de configuración en una clave secreta balanceada.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Compone y firma digitalmente un JSON Web Token (JWT) bajo la especificación RFC 7519.
     *
     * @param username       Subject o identidad principal del operador de la flota.
     * @param roles          Colección de autoridades comerciales asignadas.
     * @param expirationDays Ventana temporal de vigencia del token expresada en días enteros.
     * @return Cadena compacta alfanumérica estructurada en tres bloques separados por puntos (Header.Payload.Signature).
     */
    public String generateToken(String username, List<String> roles, long expirationDays) {

        // Transforma la constante temporal de días de negocio a milisegundos primitivos de la JVM.
        long expirationTime = expirationDays * 24 * 60 * 60 * 1000L;

        return Jwts.builder()
                .subject(username)
                // claim("authorities"): Inyecta de forma exacta la firma de privilegios requerida 
                // por el SimpleGrantedAuthority del filtro de seguridad del microservicio.
                .claim("authorities", roles)
                .issuedAt(new Date())
                // expiration(): Calcula el hito de caducidad sumando la ventana temporal al milisegundo actual del sistema.
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey())
                .compact(); // Ejecuta la serialización base64url y concatena las firmas.
    }

    /**
     * Método de conveniencia para la suplantación atómica del rol supremo de Infraestructura.
     * Genera un token inalterable con una validez extendida de 90 días.
     */
    public String generateAdminToken() {
        return generateToken("admin", List.of("ROLE_ADMIN"), 90);
    }

    /**
     * Método de conveniencia para la simulación del rol operativo de Despacho Logístico.
     */
    public String generateOperadorToken() {
        return generateToken("operador", List.of("ROLE_OPERADOR"), 90);
    }

    /**
     * Método de conveniencia para emular peticiones provenientes de clientes o conductores autorizados.
     */
    public String generateUsuarioAutorizadoToken() {
        return generateToken("usuario", List.of("ROLE_USUARIO_AUTORIZADO"), 90);
    }
}