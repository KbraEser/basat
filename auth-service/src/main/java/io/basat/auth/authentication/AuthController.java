package io.basat.auth.authentication;

import io.basat.auth.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Company registration and sign-in")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;

        @Operation(
                summary = "Register a company",
                description = "Creates a new company (tenant) and its first user with the ADMIN role. "
                        + "The email address is stored in lowercase and must not be registered before.")

        @ApiResponses({
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201", description = "Company and admin user created"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400", description = "Validation failed; the `errors` field lists invalid fields",
                        content = @Content(mediaType = "application/problem+json",
                                schema = @Schema(implementation = ProblemDetail.class))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409", description = "Email is already registered",
                        content = @Content(mediaType = "application/problem+json",
                                schema = @Schema(implementation = ProblemDetail.class)))
        })


        @PostMapping("/register")
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
            return ApiResponse.of(authService.register(request),"Company registered successfully");
        }

}
