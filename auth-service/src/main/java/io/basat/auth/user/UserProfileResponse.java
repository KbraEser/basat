package io.basat.auth.user;

import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        UUID tenantId,
        String companyName,
        String email,
        String fullName,
        String role
) {
}
