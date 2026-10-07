package io.basat.auth.authentication;

import java.util.UUID;

public record RegisterResponse(
        UUID tenantId,
        UUID userId,
        String email,
        String role
) {
}
