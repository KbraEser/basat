package io.basat.monitoring.reading;

import io.basat.monitoring.live.LiveReading;
import io.basat.monitoring.live.LiveReadingPublisher;
import io.basat.monitoring.meter.Meter;
import io.basat.monitoring.meter.MeterRepository;
import io.basat.monitoring.site.Site;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingServiceImpl implements ReadingService{

    private final ReadingRepository readingRepository;
    private final MeterRepository meterRepository;
    private final LiveReadingPublisher liveReadingPublisher;

    @Override
    public void store(ReadingMessage message){
        Meter meter = meterRepository.findBySerialNumber(message.serialNumber()).orElseThrow(()->new UnknownMeterException(message.serialNumber()));

        boolean stored = readingRepository.save(meter.getId(), message.measuredAt(),message.energyKwh());

        if(!stored){
            log.info("Ignored repeated reading for meter {} at {}", message.serialNumber(), message.measuredAt());
            return;
        }

        Site site = meter.getSite();
        liveReadingPublisher.publish(site.getTenantId(),site.getId(),new LiveReading(meter.getId(),meter.getSerialNumber(),meter.getType(),message.measuredAt(),message.energyKwh()));

    }
}
