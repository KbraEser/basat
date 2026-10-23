package io.basat.auth.authentication;

/** Tracks failed login attempts per email to slow down password guessing. */
public interface LoginAttemptService {

    /** Returns true if the email has too many recent failed attempts. */
    boolean isBlocked(String email);

    void recordFailure(String email);

    /** Clears the failed attempts, e.g. after a successful login. */
    void reset(String email);
}
