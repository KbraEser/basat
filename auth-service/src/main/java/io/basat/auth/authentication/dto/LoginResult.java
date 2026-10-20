package io.basat.auth.authentication.dto;

public record LoginResult(String accessToken, String refreshToken, LoginResponse user) {
}
