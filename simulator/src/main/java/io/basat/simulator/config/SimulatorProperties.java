package io.basat.simulator.config;

import io.basat.simulator.profile.ProfileType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/** The {@code simulator.*} settings from application.yaml: where to send readings, how often, for which meters. */
@Validated
@ConfigurationProperties("simulator")
public record SimulatorProperties(
        @NotNull URI ingestionUrl,
        @NotNull Duration interval,
        @NotEmpty List<@Valid MeterConfig> meters
) {

    public record MeterConfig(
            @NotBlank String serialNumber,
            @NotNull ProfileType profile,
            @Positive double installedKw
    ) {
    }
}