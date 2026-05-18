package com.xarch.starter.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / Knife4j configuration
 */
@Configuration
public class XarchSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("xarch API Documentation")
                .description("Enterprise Backend Framework API")
                .version("1.0.0")
                .contact(new Contact().name("xarch").email("contact@xarch.com"))
                .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
    }
}