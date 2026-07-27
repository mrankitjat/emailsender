package com.example.recruitment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3.0 Documentation Configuration.
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Recruitment Email Service API")
                        .version("1.0.0")
                        .description("Production-grade Spring Boot 3 microservice designed to send bulk recruitment emails with resume attachments. Features automatic fallback to Google Drive assets.")
                        .contact(new Contact()
                                .name("Engineering Architecture Team")
                                .email("engineering@company.com"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
