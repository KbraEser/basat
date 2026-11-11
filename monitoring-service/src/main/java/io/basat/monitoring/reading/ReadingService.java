package io.basat.monitoring.reading;

/** Stores readings that arrive from Kafka. */
public interface ReadingService {

    /**
     * Stores the reading for the meter with the message's serial number.
     * A repeated reading (same meter and time) is skipped, not failed.
     *
     * @throws UnknownMeterException if no meter has the message's serial number
     */
    void store(ReadingMessage message);

}
