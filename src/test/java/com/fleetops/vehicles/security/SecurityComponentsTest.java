package com.fleetops.vehicles.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityComponentsTest {

    @Mock private PublicKey publicKey;
    @Mock private FilterChain filterChain;

    private JwtValidationFilter filter;
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        authenticationEntryPoint = new JwtAuthenticationEntryPoint();

        filter = new JwtValidationFilter(
                publicKey,
                authenticationEntryPoint
        );
    }

    @Test
    void filtroSinHeaderContinua() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // @Test
    // void filtroConTokenValidoRegistraIdentidad() throws Exception {
    //     String token = generator.generateToken("tester", 1);
    //     MockHttpServletRequest request = new MockHttpServletRequest();
    //     request.addHeader("Authorization", "Bearer " + token);
    //     MockHttpServletResponse response = new MockHttpServletResponse();

    //     filter.doFilterInternal(request, response, filterChain);

    //     verify(filterChain).doFilter(request, response);
    //     assertEquals("tester", SecurityContextHolder.getContext().getAuthentication().getName());
    // }

    @Test
    void authenticationEntryPointResponde401() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/vehiculos");

        MockHttpServletResponse response = new MockHttpServletResponse();

        authenticationEntryPoint.commence(request, response, null);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Unauthorized"));
        assertTrue(response.getContentAsString().contains("/vehiculos"));
    }

    @Test
    void filtroConTokenInvalidoRetorna401() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token.invalido");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        verify(filterChain, never()).doFilter(request, response);
    }
}
