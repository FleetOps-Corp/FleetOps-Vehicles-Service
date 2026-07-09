package com.fleetops.vehicles.support;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Generador de JWT local para desarrollo (HS256). Invocado por scripts/generate-dev-jwt.sh
 */
public final class DevJwtGenerator {

    private static final String DEFAULT_SECRET =
            "esta_es_una_clave_secreta_muy_larga_para_desarrollo_local_1234567890";

    private DevJwtGenerator() {
    }

    public static void main(String[] args) throws Exception {
        String subject = args.length > 0 ? args[0] : "dev-local@fleetops.com";
        int expiresDays = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        String secret = args.length > 2 ? args[2] : System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = DEFAULT_SECRET;
        }

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        Instant now = Instant.now();

        String token = Jwts.builder()
                .subject(subject)
                .claim("role", "EMPLEADO_VEHICULOS")
                .claim("email", subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiresDays, ChronoUnit.DAYS)))
                .signWith(key)
                .compact();

        System.out.println(token);
    }
}
