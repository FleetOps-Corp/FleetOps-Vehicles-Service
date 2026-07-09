package com.fleetops.vehicles.security;

import com.fleetops.vehicles.controllers.VehicleController;
import com.fleetops.vehicles.services.application.SagaService;
import com.fleetops.vehicles.services.application.TipoVehiculoService;
import com.fleetops.vehicles.services.application.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VehicleController.class)
@Import({
        SpringSecurityConfig.class,
        JwtValidationFilter.class,
        JwtAuthenticationEntryPoint.class,
        TokenJwtConfig.class,
})
class SpringSecurityConfigWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;
    @MockBean
    private SagaService sagaService;
    @MockBean
    private TipoVehiculoService tipoVehiculoService;

    @Test
    void sinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/vehiculos"))
                .andExpect(status().isUnauthorized());
    }

}
