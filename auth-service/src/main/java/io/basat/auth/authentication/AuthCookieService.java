package io.basat.auth.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    private final boolean secure;
    private final Duration accessTokenTtl;

    public AuthCookieService(@Value("${app.cookie.secure}")boolean secure,@Value("${app.jwt.access-token-ttl}") Duration accessTokenTtl) {
        this.secure = secure;
        this.accessTokenTtl = accessTokenTtl;
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
}
