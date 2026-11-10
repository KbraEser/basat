package io.basat.monitoring.reading;

/** Stores readings that arrive from Kafka. */
public interface ReadingService {

    /**
     * Stores the reading for the meter with the message's serial number.
     * Readings for unknown meters and repeated readings are skipped, not failed.
     */
    void store(ReadingMessage message);
}
