package com.fleetops.vehicles.security;

import com.fleetops.vehicles.controllers.VehicleController;
import com.fleetops.vehicles.services.application.SagaService;
import com.fleetops.vehicles.services.application.TipoVehiculoService;
import com.fleetops.vehicles.services.application.VehicleService;
import com.fleetops.vehicles.util.JwtTokenGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VehicleController.class)
@Import({
        SpringSecurityConfig.class,
        JwtValidationFilter.class,
        JwtAuthenticationEntryPoint.class,
        TokenJwtConfig.class,
        JwtTokenGenerator.class
})
@TestPropertySource(properties = "jwt.secret=esta_es_una_clave_secreta_muy_larga_para_desarrollo_local_1234567890")
class SpringSecurityConfigWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;

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

    @Test
    void conTokenValidoPermiteAcceso() throws Exception {
        when(vehicleService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/vehiculos")
                        .header("Authorization", "Bearer " + jwtTokenGenerator.generateDevToken()))
                .andExpect(status().isOk());
    }
}
