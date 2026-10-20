package io.basat.auth.authentication;

import io.basat.auth.authentication.dto.LoginRequest;
import io.basat.auth.authentication.dto.LoginResult;
import io.basat.auth.authentication.dto.RegisterRequest;
import io.basat.auth.authentication.dto.RegisterResponse;

/** Registration and authentication use cases. */
public interface AuthService {

    /**
     * Registers a company and its first ADMIN user.
     *
     * @throws EmailAlreadyUsedException if the email is already registered
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Checks the credentials and issues an access token.
     *
     * @throws InvalidCredentialsException if the email is unknown or the password is wrong
     */
    LoginResult login(LoginRequest request);

    /**
     * Exchanges a valid refresh token for a new access token and a new refresh token
     * @throws InvalidRefreshTokenException if the token is unknown,revoked or expired
     */
    LoginResult refresh(String refreshToken);

}
