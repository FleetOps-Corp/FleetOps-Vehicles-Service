package com.fleetops.vehicles.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class TokenJwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    public SecretKey getSecretKey() {
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        }
        return secretKey;
    }
}
