package io.basat.auth.token;

import io.basat.auth.user.User;

/** Issues tokens for authenticated users. */
public interface TokenService {

    String createAccessToken(User user);
}
