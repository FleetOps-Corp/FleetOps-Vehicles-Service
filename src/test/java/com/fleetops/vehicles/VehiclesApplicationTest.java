package com.fleetops.vehicles;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@DisplayName("Tests unitarios - VehiclesApplication")
class VehiclesApplicationTest {

    @Test
    @DisplayName("main invoca SpringApplication.run con la clase de arranque")
    void mainArrancaContextoDeSpring() {
        String[] args = {"--spring.profiles.active=test"};
        ConfigurableApplicationContext contextoFalso = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(VehiclesApplication.class, args))
                    .thenReturn(contextoFalso);

            VehiclesApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(VehiclesApplication.class, args));
        }
    }

    @Test
    @DisplayName("La clase de arranque se puede instanciar")
    void instanciaClaseDeArranque() {
        new VehiclesApplication();
    }
}
