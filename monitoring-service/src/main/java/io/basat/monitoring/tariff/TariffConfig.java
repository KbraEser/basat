package io.basat.monitoring.tariff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TariffConfig {
    @Bean
    public Tariff tariff(TariffProperties properties) {
        return properties.toTariff();
    }
}
