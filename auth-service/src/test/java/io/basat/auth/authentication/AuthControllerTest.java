package io.basat.auth.authentication;

import io.basat.auth.authentication.dto.RegisterRequest;
import io.basat.auth.authentication.dto.RegisterResponse;
import io.basat.auth.config.SecurityConfig;
import io.basat.auth.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for {@link AuthController}.
 * <p>
 * Only the web layer is started: the controller, JSON conversion, validation, security rules
 * and the global exception handler. {@link AuthService} is replaced by a Mockito mock, so no
 * database, repository or password hashing is involved.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    private static final String REGISTER_URL = "/api/auth/register";

    private static final String VALID_REQUEST = """
            {"companyName":"Deniz Otelcilik","fullName":"Ayşe Yılmaz","email":"ayse@deniz.com","password":"GucluSifre123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_returns201WithWrappedBody_whenRequestIsValid() throws Exception {
        // given
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new RegisterResponse(UUID.randomUUID(), UUID.randomUUID(), "ayse@deniz.com", "ADMIN"));

        // when + then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("ayse@deniz.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.message").value("Company registered successfully"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void register_returns400WithFieldErrors_whenRequestIsInvalid() throws Exception {
        // when + then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyName":"","fullName":"Ali","email":"gecersiz","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.companyName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        // validation stops the request before it reaches the service
        verifyNoInteractions(authService);
    }

    @Test
    void register_returns409_whenEmailIsAlreadyUsed() throws Exception {
        // given
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyUsedException("ayse@deniz.com"));

        // when + then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Email already registered"));
    }
}
