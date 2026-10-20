package io.basat.auth.token;

import io.basat.auth.user.User;

/** Issues, rotates and revokes refresh tokens. Only a hash of each token is stored. */
public interface RefreshTokenService {
    /** Creates a refresh token for the user and returns its raw value. */
    String create(User user);

    /**
     * Revokes the given token and issues a new one.
     *
     * @throws InvalidRefreshTokenException if the token is unknown, revoked or expired
     */
    RotationResult rotate(String rawToken);

    /** Revokes the token if it exists; unknown tokens are ignored. */
    void revoke(String rawToken);
}
