package com.fleetops.vehicles.security;

import com.fleetops.vehicles.util.JwtTokenGenerator;
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
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityComponentsTest {

    private static final String SECRET = "esta_es_una_clave_secreta_muy_larga_para_desarrollo_local_1234567890";

    @Mock private TokenJwtConfig tokenJwtConfig;
    @Mock private FilterChain filterChain;

    private JwtValidationFilter filter;
    private JwtTokenGenerator generator;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtValidationFilter(tokenJwtConfig);
        generator = new JwtTokenGenerator();
        ReflectionTestUtils.setField(generator, "secret", SECRET);

        TokenJwtConfig realConfig = new TokenJwtConfig();
        ReflectionTestUtils.setField(realConfig, "secret", SECRET);
        lenient().when(tokenJwtConfig.getSecretKey()).thenReturn(realConfig.getSecretKey());
    }

    @Test
    void tokenGeneratorYConfig() {
        String token = generator.generateDevToken();
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        TokenJwtConfig config = new TokenJwtConfig();
        ReflectionTestUtils.setField(config, "secret", SECRET);
        SecretKey key1 = config.getSecretKey();
        SecretKey key2 = config.getSecretKey();
        assertSame(key1, key2);
    }

    @Test
    void filtroSinHeaderContinua() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void filtroConTokenValidoRegistraIdentidad() throws Exception {
        String token = generator.generateToken("tester", 1);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals("tester", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void filtroConTokenInvalidoContinuaSinIdentidad() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token.invalido.aqui");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void authenticationEntryPointResponde401() throws Exception {
        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/vehiculos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, null);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("error") || response.getContentAsString().length() > 0);
    }
}
