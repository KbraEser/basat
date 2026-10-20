package io.basat.auth.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final String ACCESS_TOKEN_PATH = "/";
    // Only sent to /api/auth/** (refresh and logout), not to every request
    private static final String REFRESH_TOKEN_PATH = "/api/auth";

    private final boolean secure;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public AuthCookieService(@Value("${app.cookie.secure}") boolean secure,
                             @Value("${app.jwt.access-token-ttl}") Duration accessTokenTtl,
                             @Value("${app.jwt.refresh-token-ttl}") Duration refreshTokenTtl) {
        this.secure = secure;
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public ResponseCookie accessTokenCookie(String token) {
        return buildCookie(ACCESS_TOKEN_COOKIE, token, ACCESS_TOKEN_PATH, accessTokenTtl);
    }

    public ResponseCookie refreshTokenCookie(String token) {
        return buildCookie(REFRESH_TOKEN_COOKIE, token, REFRESH_TOKEN_PATH, refreshTokenTtl);
    }

    public ResponseCookie clearAccessTokenCookie() {
        return buildCookie(ACCESS_TOKEN_COOKIE, "", ACCESS_TOKEN_PATH, Duration.ZERO);
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return buildCookie(REFRESH_TOKEN_COOKIE, "", REFRESH_TOKEN_PATH, Duration.ZERO);
    }

    private ResponseCookie buildCookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                // SameSite=Strict keeps the cookie off cross-site requests, which prevents CSRF
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}