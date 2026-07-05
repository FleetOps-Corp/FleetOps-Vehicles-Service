package com.fleetops.vehicles.config;

import com.fleetops.vehicles.util.JwtTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "local", "default"})
public class DevTokenPrinter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevTokenPrinter.class);

    private final JwtTokenGenerator jwtTokenGenerator;

    public DevTokenPrinter(JwtTokenGenerator jwtTokenGenerator) {
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    @Override
    public void run(String... args) {
        String devToken = jwtTokenGenerator.generateDevToken();

        log.info("╔════════════════════════════════════════════════════════════════════════════╗");
        log.info("║                    TOKEN JWT PARA DESARROLLO - FLEETOPS                    ║");
        log.info("╠════════════════════════════════════════════════════════════════════════════╣");
        log.info("║  {} ║", devToken);
        log.info("╚════════════════════════════════════════════════════════════════════════════╝");
        log.info(">>> Usa este token en Postman: Authorization: Bearer <token>. Sin roles. <<<");
    }
}
