package io.basat.ingestion.reading;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingReadingPublisher implements ReadingPublisher {

    @Override
    public void publish(Reading reading) {
        log.info("Reading received: meter {}, {} kwh at {}", reading.serialNumber(),reading.energyKwh(),reading.measuredAt());
    }
}
