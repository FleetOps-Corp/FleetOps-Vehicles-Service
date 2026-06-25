package com.fleetops.vehicles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// motor de tareas programadas 
@EnableScheduling
public class VehiclesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiclesApplication.class, args);
    }
}