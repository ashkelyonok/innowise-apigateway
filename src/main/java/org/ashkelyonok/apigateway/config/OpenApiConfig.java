package org.ashkelyonok.apigateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway Documentation")
                        .version("1.0")
                        .description("Centralized entry point and Edge Orchestrator for the Microservices ecosystem.")
                        .contact(new Contact()
                                .name("Anastasia Shkelyonok")
                                .email("anastasia.shkelyonok@gmail.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://api-gateway:8084")
                                .description("Docker Internal Network")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
