package com.fleetops.vehicles.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
// @Configuration: Indica que esta clase contiene definiciones de Beans de Spring.
@EnableWebSecurity
// @EnableWebSecurity: Activa la configuración de seguridad web de Spring Security.
@EnableMethodSecurity(prePostEnabled = true)
// @EnableMethodSecurity: Permite usar anotaciones como @PreAuthorize en los controladores.
public class SpringSecurityConfig {

    private final JwtValidationFilter jwtValidationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SpringSecurityConfig(JwtValidationFilter jwtValidationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtValidationFilter = jwtValidationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    // @Bean: Registra este método como un Bean que Spring administrará.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Método que configura toda la cadena de filtros de seguridad.

        return http
                .csrf(csrf -> csrf.disable())
                // Deshabilita CSRF (Cross-Site Request Forgery).
                // Es seguro hacerlo porque usamos JWT stateless (sin sesiones).

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configura la aplicación como STATELESS (sin sesiones en servidor).
                // Cada petición debe traer su propio token JWT.

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // Cuando un usuario no autenticado intenta acceder a un recurso protegido,
                // se usa JwtAuthenticationEntryPoint para devolver un error JSON 401.

                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/vehiculos/*/disponibilidad").permitAll()
                        .requestMatchers(HttpMethod.GET, "/vehiculos/**").hasAnyRole("USUARIO_AUTORIZADO", "OPERADOR", "ADMIN")

                        // ==================== TIPOS DE VEHÍCULO ====================
                        .requestMatchers(HttpMethod.POST, "/vehiculos/tipos-vehiculo").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/vehiculos/tipos-vehiculo/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/vehiculos/tipos-vehiculo/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/vehiculos/tipos-vehiculo/**")
                        .hasAnyRole("USUARIO_AUTORIZADO", "OPERADOR", "ADMIN")

                        // ==================== VEHÍCULOS ====================
                        .requestMatchers(HttpMethod.POST, "/vehiculos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/vehiculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/vehiculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/vehiculos/*/historial").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/vehiculos/*/estado").hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/vehiculos/**")
                        .hasAnyRole("USUARIO_AUTORIZADO", "OPERADOR", "ADMIN")

                        // ==================== RESERVAS (SAGA) ====================
                        .requestMatchers(HttpMethod.POST, "/vehiculos/*/reservas/**").hasAnyRole("OPERADOR", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/vehiculos/*/reservas/**")
                        .hasAnyRole("USUARIO_AUTORIZADO", "OPERADOR", "ADMIN")

                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated())

                // Agrega el filtro JWT antes del filtro de autenticación por usuario/contraseña
                .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}