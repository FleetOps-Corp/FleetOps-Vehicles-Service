package com.fleetops.vehicles.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SpringSecurityConfigTest {

    @Test
    void corsPermiteOrigenesYMetodos() {
        JwtValidationFilter filter = mock(JwtValidationFilter.class);
        JwtAuthenticationEntryPoint entryPoint = mock(JwtAuthenticationEntryPoint.class);
        SpringSecurityConfig config = new SpringSecurityConfig(filter, entryPoint);

        CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source);

        var cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/vehiculos"));
        assertNotNull(cors);
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowedMethods().contains("POST"));
    }
}
