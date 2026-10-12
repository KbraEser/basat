package io.basat.auth.authentication;

import io.basat.auth.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;

        @PostMapping("/register")
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<RegisterResponse> registerResponse(@Valid @RequestBody RegisterRequest request){
            return ApiResponse.of(authService.register(request),"Company registered successfully");
        }

}
