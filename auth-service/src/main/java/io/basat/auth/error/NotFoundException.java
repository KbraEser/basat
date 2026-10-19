package io.basat.auth.error;

/** Base class for "resource not found" errors; mapped to HTTP 404 */
public abstract class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}
