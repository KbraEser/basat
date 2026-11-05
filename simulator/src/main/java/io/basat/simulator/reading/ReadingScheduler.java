package io.basat.simulator.reading;

import io.basat.simulator.config.SimulatorProperties;
import io.basat.simulator.config.SimulatorProperties.MeterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Every interval, produces one reading per configured meter and sends it. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadingScheduler {

    private final SimulatorProperties properties;
    private final ReadingGenerator generator;
    private final IngestionClient client;

    @Scheduled(fixedRateString = "${simulator.interval}")
    public void sendReadings() {
        // one timestamp for all meters, so readings of the same moment line up on the charts
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        for (MeterConfig meter : properties.meters()) {
            client.send(generator.generate(meter, now, properties.interval()));
        }
        log.info("Sent {} readings for {}", properties.meters().size(), now);
    }
}
