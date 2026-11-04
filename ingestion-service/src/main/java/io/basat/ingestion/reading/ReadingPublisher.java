package io.basat.ingestion.reading;

/**
 * Hands a validated reading over for processing.
 * <p>
 * The controller only knows this interface, so the way readings leave the service
 * can change without touching the endpoint.
 */
public interface ReadingPublisher {
    void publish(Reading reading);
}
