package com.fleetops.vehicles.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - SpringSecurityConfig")
class SpringSecurityConfigTest {

    @Mock
    private JwtValidationFilter jwtValidationFilter;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("corsConfigurationSource permite orígenes y métodos estándar")
    void corsConfiguration() {
        SpringSecurityConfig config = new SpringSecurityConfig(jwtValidationFilter, jwtAuthenticationEntryPoint);

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/vehiculos"));

        assertNotNull(cors);
        assertTrue(cors.getAllowedOriginPatterns().contains("*"));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
    }
}
