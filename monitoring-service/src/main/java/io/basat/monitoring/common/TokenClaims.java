package io.basat.monitoring.common;
import java.util.Objects;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/** Reads Basat-specific claims from an access token issued by auth-service. */
public final class TokenClaims {

    private static final String TENANT_ID = "tenantId";

    private TokenClaims() {
    }

    public static UUID tenantId(Jwt jwt) {
        String tenantId = Objects.requireNonNull(jwt.getClaimAsString(TENANT_ID),
                "Access token has no tenantId claim");
        return UUID.fromString(tenantId);
    }
}