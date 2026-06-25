// Define el paquete exclusivo para la configuración y control perimetral de seguridad del microservicio.
package com.fleetops.vehicles.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// ─────────────────────────────────────────────────────────────────────────────
// PATRÓN APLICADO: Intercepting Filter / Custom Security Entry Point
//
// ¿Por qué existe aquí?
// Por defecto, cuando una petición anónima o con credenciales corruptas intenta
// consumir un endpoint protegido de Spring Security, el framework responde con un 
// volcado HTML genérico o un error vacío. En una arquitectura de microservicios REST,
// el Frontend o los API Gateways no pueden interpretar HTML; necesitan JSON estricto.
//
// ¿Cómo lo soluciona?
// Actúa como el último muro de contención en la canalización (pipeline) de filtros HTTP.
// Captura el fallo de autenticación (`AuthenticationException`) antes de que llegue 
// al controlador y muta la respuesta HTTP nativa, escribiendo un ticket JSON estructurado 
// y homologado con nuestro formato global de errores (`ErrorResponse`).
//
// Ejemplo de bolsillo:
// Es como la recepción de un edificio corporativo inteligente. Si un visitante intenta
// colarse a los ascensores sin pasar su tarjeta magnética, el torniquete se bloquea 
// automáticamente y una pantalla digital le muestra un mensaje claro: "Acceso denegado, 
// presente una credencial válida".
// ─────────────────────────────────────────────────────────────────────────────

@Component
// @Component: Registra esta clase como un Bean único (Singleton) dentro del contenedor de Spring.
// Permite acoplar este manejador directamente dentro de la cadena de filtros en la SecurityFilterChain.
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Instanciación directa del motor Jackson para serialización rápida sin depender de inyecciones externas.
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gatillo automático de Spring Security invocado inmediatamente cuando una petición 
     * no autenticada viola los privilegios de un recurso protegido.
     *
     * @param request       Abstracción de la petición HTTP entrante.
     * @param response      Abstracción de la respuesta HTTP saliente que modificaremos manualmente.
     * @param authException Excepción nativa de seguridad que describe la naturaleza del fallo de autenticación.
     * @throws IOException  Excepción lanzada si el canal de escritura del buffer del servlet experimenta problemas.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Aseguramos que la cabecera declare explícitamente que el contenido devuelto es un objeto JSON puro.
        response.setContentType("application/json");

        // SC_UNAUTHORIZED (HTTP 401): Estado estándar internacional para indicar que la identidad 
        // del emisor no ha sido verificada o el Token JWT es inválido/expirado.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Construcción manual del mapa de error para replicar exactamente la firma inmutable de ErrorResponse.
        Map<String, Object> errorResponse = new HashMap<>();
        
        // Sello de tiempo ISO para auditorías en el cliente.
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", 401);
        errorResponse.put("error", "Unauthorized");
        
        // REGLA DE NEGOCIO: Mensaje de abstracción defensiva. No le indicamos al cliente el fallo 
        // exacto de criptografía del token por seguridad, sino las tres causas comerciales comunes.
        errorResponse.put("message", "Token JWT inválido, expirado o no proporcionado");
        
        // Captura dinámica del endpoint que fue atacado o consultado de forma anónima.
        errorResponse.put("path", request.getRequestURI());

        // writeValueAsString: Transforma el mapa relacional de Java a una cadena serializada JSON (String).
        // getWriter().write(): Inyecta el String directamente en el payload del flujo HTTP binario del Servlet.
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}