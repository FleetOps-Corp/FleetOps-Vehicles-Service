package com.fleetops.vehicles.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenJwtConfig {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public PublicKey publicKey() {

        try {

            String key = Files.readString(Path.of(publicKeyPath));

            key = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(key);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(spec);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No fue posible cargar la llave pública JWT.", e);
        }
    }

}