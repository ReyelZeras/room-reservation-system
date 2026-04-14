package com.roomres.room_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI roomServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Room Service API")
                        .description("API responsável pelo cadastro e gerenciamento das salas físicas.")
                        .version("v1.0.0"));
    }
}