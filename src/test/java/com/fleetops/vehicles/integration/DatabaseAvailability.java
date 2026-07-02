package com.fleetops.vehicles.integration;

import java.sql.DriverManager;

/**
 * Verifica si PostgreSQL está accesible antes de ejecutar tests de integración.
 */
public final class DatabaseAvailability {

    private static final String URL = System.getenv().getOrDefault(
            "SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/fleetops_vehicles");
    private static final String USER = System.getenv().getOrDefault(
            "SPRING_DATASOURCE_USERNAME", "postgres");
    private static final String PASS = System.getenv().getOrDefault(
            "SPRING_DATASOURCE_PASSWORD", "root");

    private DatabaseAvailability() {
    }

    public static boolean isPostgresAvailable() {
        try (var conn = DriverManager.getConnection(URL, USER, PASS)) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
