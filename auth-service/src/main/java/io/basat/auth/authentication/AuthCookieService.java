package io.basat.auth.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
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

    public ResponseCookie accessTokenCookie(String token){
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE,token)
                .httpOnly(true)
                .secure(secure)
        // SameSite=Strict keeps the cookie off cross-site requests, which prevents CSRF
                .sameSite("Strict")
                .path("/")
                .maxAge(accessTokenTtl)
                .build();
    }

    public ResponseCookie refreshTokenCookie(String token){
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE,token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
        // Only sent to /api/auth/** (refresh and logout), not to every request
                .path("/api/auth")
                .maxAge(refreshTokenTtl)
                .build();
    }
}
