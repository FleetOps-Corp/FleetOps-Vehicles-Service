package com.fleetops.vehicles.config;

import com.fleetops.vehicles.util.JwtTokenGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class DevTokenPrinterTest {

    @Test
    void imprimeTokenDeDesarrollo() {
        JwtTokenGenerator generator = mock(JwtTokenGenerator.class);
        when(generator.generateDevToken()).thenReturn("token.dev");

        DevTokenPrinter printer = new DevTokenPrinter(generator);
        assertDoesNotThrow(() -> printer.run());
        verify(generator).generateDevToken();
    }
}
