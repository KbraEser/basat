package io.basat.simulator.profile;

import java.time.LocalDateTime;

/**
 * Describes how a type of building uses electricity over time.
 * <p>
 * Each building type (office, hotel, solar plant...) is one implementation; the scheduler that sends
 * readings only talks to this interface, so a new type can be added without changing it.
 */
public interface ConsumptionProfile {

    /** The building type this profile describes; meters in the configuration refer to it by this value. */
    ProfileType type();

    /**
     * Share of the meter's installed power in use at the given local time, between 0 and 1.
     * The result is deterministic; random noise is added by the caller.
     */
    double loadFactor(LocalDateTime time);
}