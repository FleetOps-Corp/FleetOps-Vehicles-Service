package com.fleetops.vehicles.integration;

import com.fleetops.vehicles.util.JwtTokenGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Base para tests de integración: Spring Boot completo contra PostgreSQL real
 * (localhost:5432 como en CI, o variables SPRING_DATASOURCE_*).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> System.getenv().getOrDefault(
                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/fleetops_vehicles"));
        registry.add("spring.datasource.username",
                () -> System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "postgres"));
        registry.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "root"));
    }

    @BeforeAll
    static void requirePostgres() {
        assumeTrue(DatabaseAvailability.isPostgresAvailable(),
                "PostgreSQL no disponible — levanta la BD con docker compose o el service de CI");
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JwtTokenGenerator jwtTokenGenerator;

    protected HttpEntity<Void> getWithAuth(String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        return new HttpEntity<>(headers);
    }

    protected <T> HttpEntity<T> jsonWithAuth(T body, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bearerToken);
        return new HttpEntity<>(body, headers);
    }

    protected String adminToken() {
        return jwtTokenGenerator.generateAdminToken();
    }

    protected String operadorToken() {
        return jwtTokenGenerator.generateOperadorToken();
    }

    protected String usuarioToken() {
        return jwtTokenGenerator.generateUsuarioAutorizadoToken();
    }
}
