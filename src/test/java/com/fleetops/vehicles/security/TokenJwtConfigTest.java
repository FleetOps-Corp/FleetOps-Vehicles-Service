package com.fleetops.vehicles.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Tests unitarios - TokenJwtConfig")
class TokenJwtConfigTest {

    @Test
    @DisplayName("getSecretKey inicializa y reutiliza la misma clave")
    void lazyInitialization() {
        TokenJwtConfig config = new TokenJwtConfig();
        ReflectionTestUtils.setField(config, "secret",
                "esta_es_una_clave_secreta_muy_larga_para_desarrollo_local_1234567890");

        SecretKey first = config.getSecretKey();
        SecretKey second = config.getSecretKey();

        assertNotNull(first);
        assertSame(first, second);
    }
}
