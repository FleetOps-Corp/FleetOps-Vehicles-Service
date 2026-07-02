package com.fleetops.vehicles;

import com.fleetops.vehicles.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Carga del contexto Spring Boot")
class VehiclesApplicationTests extends BaseIntegrationTest {

    @Test
    @DisplayName("El contexto de la aplicación arranca correctamente")
    void contextLoads() {
        // Si Spring no puede inicializar beans, Flyway o seguridad, este test falla.
    }
}
