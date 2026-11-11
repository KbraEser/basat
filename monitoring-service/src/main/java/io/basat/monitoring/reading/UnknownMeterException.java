package io.basat.monitoring.reading;

public class UnknownMeterException extends RuntimeException {

    public UnknownMeterException(String serialNumber){
        super("No meter with serial number: " + serialNumber);
    }
}
