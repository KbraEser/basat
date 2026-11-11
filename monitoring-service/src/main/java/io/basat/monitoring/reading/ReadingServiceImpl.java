package io.basat.monitoring.reading;

import io.basat.monitoring.meter.Meter;
import io.basat.monitoring.meter.MeterRepository;
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

    @Override
    public void store(ReadingMessage message){
        Meter meter = meterRepository.findBySerialNumber(message.serialNumber()).orElseThrow(()->new UnknownMeterException(message.serialNumber()));

        boolean stored = readingRepository.save(meter.getId(), message.measuredAt(),message.energyKwh());

        if(!stored){
            log.info("Ignored repeated reading for meter {} at {}", message.serialNumber(), message.measuredAt());
        }
    }
}
