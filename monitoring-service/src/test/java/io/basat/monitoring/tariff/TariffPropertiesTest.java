package io.basat.monitoring.tariff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TariffPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(Config.class);

    @Test
    void pricesAreReadFromProperties() {
        runner.withPropertyValues("app.tariff.day=3.1", "app.tariff.peak=4.7", "app.tariff.night=1.9")
                .run(context -> {
                    TariffProperties tariff = context.getBean(TariffProperties.class);
                    assertThat(tariff.priceOf(TariffBand.PEAK)).isEqualByComparingTo(new BigDecimal("4.7"));
                });
    }

    @Test
    void missingPrice_failsAtStartup() {
        runner.withPropertyValues("app.tariff.day=3.1", "app.tariff.night=1.9")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(TariffProperties.class)
    static class Config {
    }
}
