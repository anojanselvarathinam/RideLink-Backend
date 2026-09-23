package com.ridelink.account_service.config;

import java.io.IOException;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SwaggerBrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void openSwagger() {
        String swaggerUrl =
                "http://localhost:8081/swagger-ui/index.html";

        try {
            new ProcessBuilder(
                    "explorer.exe",
                    swaggerUrl
            ).start();

            System.out.println("Swagger browser opened successfully.");

        } catch (IOException exception) {
            System.out.println("Swagger browser open error:");
            exception.printStackTrace();
        }
    }
}