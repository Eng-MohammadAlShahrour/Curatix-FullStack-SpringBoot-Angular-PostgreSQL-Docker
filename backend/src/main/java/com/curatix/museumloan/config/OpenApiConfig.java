package com.curatix.museumloan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI curatixOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Curatix API")
                        .version("1.0.0")
                        .description("Enterprise-grade museum artifact loan management API. Features: JWT auth, state machine, audit trail, user management.")
                        .contact(new Contact().name("Curatix").email("admin@curatix.test"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("/").description("Current host (works on 8081, 4200 proxy, and production)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste JWT token (without 'Bearer' prefix)")));
    }
}