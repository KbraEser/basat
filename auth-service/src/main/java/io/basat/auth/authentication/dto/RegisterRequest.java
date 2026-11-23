package io.basat.auth.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

        @Schema(description = "Password, 8 to 72 characters with an upper-case letter, a lower-case letter and a digit "
                + "(BCrypt uses at most 72 bytes)", example = "GucluSifre123")
        @NotBlank
        // BCrypt only uses the first 72 bytes of a password
        @Size(min = 8, max = 72)
        // \p{Lu} and \p{Ll} also accept Turkish letters such as Ş and ğ
        @Pattern(regexp = "^(?=.*\\p{Lu})(?=.*\\p{Ll})(?=.*\\d).*$",
                message = "must contain an upper-case letter, a lower-case letter and a digit")
        String password
) {
}
