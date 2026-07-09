package com.fleetops.vehicles.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;

/**
 * Generador de JWT local para desarrollo. Invocado por scripts/generate-dev-jwt.sh
 */
public final class DevJwtGenerator {

    private DevJwtGenerator() {
    }

    public static void main(String[] args) throws Exception {
        String subject = args.length > 0 ? args[0] : "dev-local@fleetops.com";
        int expiresDays = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        String privateKeyPath = args.length > 2 ? args[2] : "secrets/jwt_private.pem";

        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        Instant now = Instant.now();

        String token = Jwts.builder()
                .subject(subject)
                .claim("role", "EMPLEADO_VEHICULOS")
                .claim("email", subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiresDays, ChronoUnit.DAYS)))
                .signWith(privateKey)
                .compact();

        System.out.println(token);
    }

    private static PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
