package com.fleetops.vehicles.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests unitarios - JwtTokenGenerator")
class JwtTokenGeneratorTest {

    private static final String TEST_SECRET = "mi-clave-secreta-de-prueba-muy-larga-para-hmac-sha256";

    private JwtTokenGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JwtTokenGenerator();
        ReflectionTestUtils.setField(generator, "secret", TEST_SECRET);
    }

    @Test
    @DisplayName("generateToken produce JWT válido con subject y authorities")
    void generateTokenValido() {
        String token = generator.generateToken("testuser", List.of("ROLE_ADMIN"), 1);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertEquals("testuser", claims.getSubject());
        assertEquals(List.of("ROLE_ADMIN"), claims.get("authorities"));
    }

    @Test
    @DisplayName("generateAdminToken incluye ROLE_ADMIN")
    void generateAdminToken() {
        String token = generator.generateAdminToken();
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertEquals("admin", claims.getSubject());
        assertEquals(List.of("ROLE_ADMIN"), claims.get("authorities"));
    }

    @Test
    @DisplayName("generateOperadorToken incluye ROLE_OPERADOR")
    void generateOperadorToken() {
        String token = generator.generateOperadorToken();
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertEquals("operador", claims.getSubject());
        assertEquals(List.of("ROLE_OPERADOR"), claims.get("authorities"));
    }

    @Test
    @DisplayName("generateUsuarioAutorizadoToken incluye ROLE_USUARIO_AUTORIZADO")
    void generateUsuarioToken() {
        String token = generator.generateUsuarioAutorizadoToken();
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertEquals("usuario", claims.getSubject());
        assertEquals(List.of("ROLE_USUARIO_AUTORIZADO"), claims.get("authorities"));
    }
}
