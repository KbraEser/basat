package io.basat.ingestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ingestionServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Basat Ingestion Service API")
                .description("Receives energy readings from meters and passes them on for processing.")
                .version("v1"));
    }
}