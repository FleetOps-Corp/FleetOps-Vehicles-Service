package com.fleetops.vehicles.security;

import com.fleetops.vehicles.util.JwtTokenGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - JwtValidationFilter")
class JwtValidationFilterTest {

    private static final String SECRET = "esta_es_una_clave_secreta_muy_larga_para_desarrollo_local_1234567890";

    @Mock
    private FilterChain filterChain;

    private TokenJwtConfig tokenJwtConfig;
    private JwtValidationFilter filter;
    private JwtTokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        tokenJwtConfig = new TokenJwtConfig();
        ReflectionTestUtils.setField(tokenJwtConfig, "secret", SECRET);
        filter = new JwtValidationFilter(tokenJwtConfig);
        tokenGenerator = new JwtTokenGenerator();
        ReflectionTestUtils.setField(tokenGenerator, "secret", SECRET);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sin header Authorization continúa la cadena sin autenticar")
    void sinHeaderContinuaCadena() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehiculos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Header Bearer inválido retorna 401")
    void tokenInvalidoRetorna401() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehiculos");
        request.addHeader("Authorization", "Bearer token.invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNotNull(response.getContentAsString());
    }

    @Test
    @DisplayName("Token JWT válido establece autenticación en SecurityContext")
    void tokenValidoAutentica() throws ServletException, IOException {
        String token = tokenGenerator.generateToken("operador", List.of("ROLE_OPERADOR"), 1);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehiculos");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("operador", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("Token válido sin claim de authorities autentica sin roles asignados")
    void tokenValidoSinAuthoritiesAsignaListaVacia() throws ServletException, IOException {
        String token = Jwts.builder()
                .subject("sin-roles")
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehiculos");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("sin-roles", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(0, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
    }

    @Test
    @DisplayName("Header sin prefijo Bearer no autentica")
    void headerSinBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehiculos");
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
