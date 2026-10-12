package io.basat.auth.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General information shown at the top of the Swagger UI page.
 */

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Basat Auth Service API")
                .description("Company registration and authentication for the Basat energy monitoring platform.")
                .version("v1")
        );
    }
}
