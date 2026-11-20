package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI omegaTitanioOpenAPI() {
        final String esquema = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Omega Titanio API")
                        .version("v1")
                        .description("API REST de la tienda Omega Titanio. Rutas versionadas bajo /api/v1."))
                .components(new Components()
                        .addSecuritySchemes(esquema, new SecurityScheme()
                                .name(esquema)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(esquema));
    }
}