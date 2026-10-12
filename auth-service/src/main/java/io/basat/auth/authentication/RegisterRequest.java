package io.basat.auth.authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Schema(description = "Company name", example = "Deniz Otelcilik")
        @NotBlank
        @Size(max = 200)
        String companyName,

        @Schema(description = "Full name of the first admin user", example = "Ayşe Yılmaz")
        @NotBlank
        @Size(max = 200)
        String fullName,

        @Schema(description = "Login email; stored in lowercase and must be unique", example = "ayse@deniz.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Password, 8 to 72 characters (BCrypt uses at most 72 bytes)", example = "GucluSifre123")
        @NotBlank
        // BCrypt only uses the first 72 bytes of a password
        @Size(min = 8, max = 72)
        String password
) {
}
