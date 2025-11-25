package com.petadoption;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main Spring Boot Application Class for Pet Adoption System
 */
@SpringBootApplication
public class PetAdoptionApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PetAdoptionApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PetAdoptionApplication.class, args);
    }
}
