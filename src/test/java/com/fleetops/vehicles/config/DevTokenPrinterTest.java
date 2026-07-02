package com.fleetops.vehicles.config;

import com.fleetops.vehicles.util.JwtTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios - DevTokenPrinter")
class DevTokenPrinterTest {

    @Mock
    private JwtTokenGenerator jwtTokenGenerator;

    @Test
    @DisplayName("run imprime tokens de desarrollo para los tres roles")
    void runGeneraTokens() {
        when(jwtTokenGenerator.generateAdminToken()).thenReturn("admin-token");
        when(jwtTokenGenerator.generateOperadorToken()).thenReturn("operador-token");
        when(jwtTokenGenerator.generateUsuarioAutorizadoToken()).thenReturn("usuario-token");

        DevTokenPrinter printer = new DevTokenPrinter(jwtTokenGenerator);
        printer.run();

        verify(jwtTokenGenerator).generateAdminToken();
        verify(jwtTokenGenerator).generateOperadorToken();
        verify(jwtTokenGenerator).generateUsuarioAutorizadoToken();
    }
}
