package io.basat.monitoring.error;

public class SerialNumberAlreadyUsedException extends RuntimeException{
    public SerialNumberAlreadyUsedException(String serialNumber) {
        super("A meter with this serial number already exists: " + serialNumber);
    }
}
