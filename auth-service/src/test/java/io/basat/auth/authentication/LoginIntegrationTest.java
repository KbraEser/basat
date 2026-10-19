package io.basat.auth.authentication;

import io.basat.auth.TestcontainersConfiguration;
import io.basat.auth.tenant.TenantRepository;
import io.basat.auth.user.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for login and the current user endpoint.
 * <p>
 * Follows the real user journey against TimescaleDB: register, log in, receive the
 * HttpOnly cookie and use it to call a protected endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginIntegrationTest {

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String ME_URL = "/api/users/me";

    private static final String EMAIL = "ayse@deniz.com";
    private static final String PASSWORD = "GucluSifre123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeEach
    void registerCompany() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyName":"Deniz Otelcilik","fullName":"Ayşe Yılmaz","email":"%s","password":"%s"}
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void cleanUp() {
        // users reference tenants, so users must be deleted first
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void login_setsHttpOnlyAccessTokenCookie_whenCredentialsAreValid() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(AuthCookieService.ACCESS_TOKEN_COOKIE))
                .andExpect(cookie().httpOnly(AuthCookieService.ACCESS_TOKEN_COOKIE, true))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                // the token travels only in the cookie, never in the body
                .andExpect(jsonPath("$.data.accessToken").doesNotExist());
    }

    @Test
    void login_returns401WithSameMessage_whenPasswordIsWrongOrEmailIsUnknown() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, "WrongPassword1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"))
                .andExpect(cookie().doesNotExist(AuthCookieService.ACCESS_TOKEN_COOKIE));

        // an unknown email must get the very same answer, so attackers cannot tell which emails exist
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("unknown@deniz.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid email or password"));
    }

    @Test
    void me_returns401_whenNoTokenIsSent() throws Exception {
        mockMvc.perform(get(ME_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsProfile_whenCookieFromLoginIsSent() throws Exception {
        // given: log in and keep the cookie, as a browser would
        Cookie accessToken = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(AuthCookieService.ACCESS_TOKEN_COOKIE);

        // when + then
        mockMvc.perform(get(ME_URL).cookie(accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(EMAIL))
                .andExpect(jsonPath("$.data.companyName").value("Deniz Otelcilik"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    private static String loginRequest(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }
}
