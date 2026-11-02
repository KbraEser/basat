package io.basat.monitoring.config;

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
    public OpenAPI monitoringServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Basat Monitoring Service API")
                .description("Sites, meters and energy consumption for the Basat energy monitoring platform.")
                .version("v1"));
    }
}