// Define el paquete exclusivo para la configuración y control perimetral de seguridad del microservicio.
package com.fleetops.vehicles.security;

import com.fleetops.vehicles.security.TokenJwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// ─────────────────────────────────────────────────────────────────────────────
// PATRÓN APLICADO: Intercepting Filter / Stateless Security Gatekeeper
//
// ¿Por qué existe aquí?
// En una arquitectura de microservicios distribuida, el servicio de Vehículos no 
// debe mantener sesiones abiertas en memoria (HttpSession) ni almacenar estados de 
// inicio de sesión de los usuarios. Cada petición HTTP entrante debe demostrar de 
// forma autónoma quién es el emisor y qué permisos ostenta.
//
// ¿Cómo lo soluciona?
// Extiende de 'OncePerRequestFilter' para interceptar de forma obligatoria y exacta 
// una sola vez por petición HTTP el token firmado en la cabecera. Descompone el JWT, 
// verifica su firma matemática y, si es legítimo, inyecta la identidad y los roles 
// del operador dentro del hilo de ejecución actual de Spring.
//
// Ejemplo de bolsillo:
// Es como el control de acceso en un festival de música masivo. No te piden que 
// dejes tu pasaporte en la entrada (sesión con estado); te colocan una pulsera 
// sellada e intransferible (Token JWT). Cada vez que compras algo o entras a una 
// zona VIP, los guardias (este filtro) solo miran la pulsera para darte paso.
// ─────────────────────────────────────────────────────────────────────────────

@Component
// @Component: Spring registra esta clase al arranque del ciclo de vida como un Bean único (Singleton).
// Esto permite que el componente inyecte propiedades globales de entorno mediante constructor.
public class JwtValidationFilter extends OncePerRequestFilter {

    // Inicialización del Logger de SLF4J para auditar intentos de acceso y rastrear tokens malformados.
    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    // Configuración inmutable encapsulada que resguarda la llave criptográfica de verificación.
    private final TokenJwtConfig tokenJwtConfig;

    /**
     * Constructor principal con inyección de dependencias gestionada por el contenedor de Spring.
     * * @param tokenJwtConfig Componente que suministra la SecretKey compartida para descifrar tokens.
     */
    public JwtValidationFilter(TokenJwtConfig tokenJwtConfig) {
        this.tokenJwtConfig = tokenJwtConfig;
    }

    /**
     * Pipeline interno del Servlet que ejecuta la validación del Token al vuelo por cada llamada HTTP.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extrae el string de autenticación oficial desde las cabeceras HTTP de la petición de red.
        String header = request.getHeader("Authorization");

        // Bloque de depuración y auditoría perimetral de cabeceras.
        if (header != null) {
            log.info("Authorization header recibido: {}", header);
        } else {
            log.warn("No se recibió header Authorization en la petición: {}", request.getRequestURI());
        }

        // CONTROL PERIMETRAL: Si la petición es pública o no cumple con el prefijo estandarizado RFC 6750,
        // el filtro delega inmediatamente la petición a la cadena sin configurar el contexto de seguridad.
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Interrumpe la ejecución local de este filtro de manera limpia.
        }

        // Sanitización del string: Remueve el prefijo comercial para aislar las tres firmas del token JWT.
        String token = header.replace("Bearer ", "");
        log.info("Token JWT recibido: {}", token);

        try {
            // VERIFICACIÓN CRIPTOGRÁFICA Jwts:
            // Construye el parser inyectando la SecretKey de verificación. Si el token fue manipulado 
            // en tránsito, se alteró un solo bit, o su tiempo de expiración (exp claim) caducó, 
            // este bloque abortará inmediatamente disparando un JwtException.
            Claims claims = Jwts.parser()
                    .verifyWith(tokenJwtConfig.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extracción del Subject comercial (habitualmente el email o username del operador logístico).
            String username = claims.getSubject();
            log.info("Token válido para el usuario: {}", username);

            // Extracción de autoridades asignadas al token (Roles de usuario).
            @SuppressWarnings("unchecked")
            List<String> authorities = claims.get("authorities", List.class);

            // Programación Defensiva: Si el token carece de roles asignados, inicializa una lista vacía.
            if (authorities == null) {
                authorities = List.of();
            }

            // Transformación estructural: Convierte la colección genérica de Strings a objetos
            // SimpleGrantedAuthority requeridos por la interfaz interna de Spring Security.
            List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Instanciación del token interno de autenticación de Spring (Principio y credenciales).
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);

            // ─────────────────────────────────────────────────────────────────────────────
            // CONCEPTO CLAVE: ThreadLocal Context Isolation (Aislamiento de Hilos)
            //
            // Al invocar 'SecurityContextHolder.getContext().setAuthentication(authentication)', 
            // registramos el token dentro de un contenedor en memoria amarrado exclusivamente 
            // al hilo de ejecución HTTP actual (ThreadLocal). Esto garantiza de forma matemática 
            // que las llamadas concurrentes de otros usuarios jamás mezclen sus identidades o permisos.
            // ─────────────────────────────────────────────────────────────────────────────
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Cede el control de forma exitosa hacia el siguiente filtro de la cadena o al controlador REST.
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            // CAPTURA DE ANOMALÍAS CRIPTOGRÁFICAS:
            // Si el token es inválido, expiró o su firma falló, registramos la traza en el servidor.
            log.error("Error al validar el token JWT: {}", e.getMessage());

            // HTTP 401 Unauthorized: Respuesta inmediata y controlada para repeler la llamada anónima corrupta.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            
            // Retorna un JSON plano y compacto indicando el fallo de autenticación de la llamada.
            response.getWriter().write("{\"error\": \"Token JWT inválido o expirado\"}");
        }
    }
}