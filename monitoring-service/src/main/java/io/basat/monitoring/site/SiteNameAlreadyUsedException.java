package io.basat.monitoring.site;

public class SiteNameAlreadyUsedException extends RuntimeException {
    public SiteNameAlreadyUsedException(String name) {
        super("A site with this name already exists: " + name);
    }
}