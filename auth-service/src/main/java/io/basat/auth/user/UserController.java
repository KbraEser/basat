package io.basat.auth.user;

import io.basat.auth.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "Users",description = "Profile of the signed-in user")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get the current user",
    description = "Returns the profile of the user identified by the access token cookie.")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString((Objects.requireNonNull(jwt.getSubject())));
        return ApiResponse.of(userService.getProfile(userId));
    }
}
