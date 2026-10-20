package io.basat.auth.token;

import io.basat.auth.user.User;

public record RotationResult(User user, String refreshToken) {
}
