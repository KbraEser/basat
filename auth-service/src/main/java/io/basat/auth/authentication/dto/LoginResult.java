package io.basat.auth.authentication.dto;

public record LoginResult(String accessToken, LoginResponse user) {
}
