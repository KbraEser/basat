package io.basat.ingestion.reading;

/** Kafka could not store the reading in time; the sender should try again later. */
public class ReadingNotPublishedException extends RuntimeException {

    public ReadingNotPublishedException(String serialNumber, Throwable cause) {
        super("Reading of meter " + serialNumber + " could not be published", cause);
    }


}