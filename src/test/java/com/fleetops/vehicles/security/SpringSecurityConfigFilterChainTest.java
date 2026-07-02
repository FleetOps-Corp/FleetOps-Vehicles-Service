package com.fleetops.vehicles.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Construye el contexto mínimo de Spring necesario para ejecutar el método
 * {@code securityFilterChain(HttpSecurity)} de {@link SpringSecurityConfig}.
 * Ese método no se ejercita con un simple "new SpringSecurityConfig(...)" porque
 * necesita el {@code HttpSecurity} que solo la infraestructura de Spring Security
 * puede construir (requiere ApplicationContext, ObjectPostProcessor, etc.).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SpringSecurityConfig.class)
@DisplayName("Tests unitarios - SpringSecurityConfig (filterChain)")
class SpringSecurityConfigFilterChainTest {

    @MockBean
    private JwtValidationFilter jwtValidationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    @DisplayName("securityFilterChain se construye con las reglas de autorización definidas")
    void securityFilterChainSeConstruye() {
        assertNotNull(securityFilterChain);
        assertNotNull(securityFilterChain.getFilters());
    }
}
