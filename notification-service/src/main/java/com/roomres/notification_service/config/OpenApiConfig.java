package com.roomres.notification_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Notification Service API")
                        .description("Serviço Reativo (WebFlux) responsável pelo envio de notificações em tempo real via Server-Sent Events (SSE).")
                        .version("v1.0.0"));
    }
}