package io.basat.auth.authentication.dto;

import java.util.UUID;

public record LoginResponse(UUID userId, UUID tenantId, String role) {
}
