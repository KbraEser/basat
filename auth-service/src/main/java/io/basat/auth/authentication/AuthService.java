package io.basat.auth.authentication;

/** Registration and authentication use cases. */
public interface AuthService {

    /**
     * Registers a company and its first ADMIN user.
     *
     * @throws EmailAlreadyUsedException if the email is already registered
     */
    RegisterResponse register(RegisterRequest request);
}
