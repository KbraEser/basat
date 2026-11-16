package io.basat.monitoring.tariff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class TariffConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(TariffConfig.class, Properties.class);

    @Test
    void timeOfUseType_createsTimeOfUseTariff() {
        runner.withPropertyValues("app.tariff.type=TIME_OF_USE",
                        "app.tariff.day=5.98", "app.tariff.peak=8.45", "app.tariff.night=4.02")
                .run(context -> assertThat(context).getBean(Tariff.class).isInstanceOf(TimeOfUseTariff.class));
    }

    @Test
    void singleRateType_createsSingleRateTariff() {
        runner.withPropertyValues("app.tariff.type=SINGLE_RATE", "app.tariff.single-rate=5.93")
                .run(context -> assertThat(context).getBean(Tariff.class).isInstanceOf(SingleRateTariff.class));
    }

    @Test
    void missingPriceOfTheChosenType_failsAtStartup() {
        runner.withPropertyValues("app.tariff.type=SINGLE_RATE", "app.tariff.day=5.98")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void negativePrice_failsAtStartup() {
        runner.withPropertyValues("app.tariff.type=SINGLE_RATE", "app.tariff.single-rate=-1")
                .run(context -> assertThat(context).hasFailed());
    }

    @EnableConfigurationProperties(TariffProperties.class)
    static class Properties {
    }
}
