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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static io.basat.auth.authentication.AuthCookieService.ACCESS_TOKEN_COOKIE;
import static io.basat.auth.authentication.AuthCookieService.REFRESH_TOKEN_COOKIE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for refresh token rotation and logout against TimescaleDB.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RefreshAndLogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Cookie refreshToken;

    @BeforeEach
    void registerAndLogIn() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyName":"Deniz Otelcilik","fullName":"Ayşe Yılmaz","email":"ayse@deniz.com","password":"GucluSifre123"}
                                """))
                .andExpect(status().isCreated());

        refreshToken = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ayse@deniz.com","password":"GucluSifre123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(REFRESH_TOKEN_COOKIE);
    }

    @AfterEach
    void cleanUp() {
        // refresh tokens are removed with their users (ON DELETE CASCADE)
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void refresh_issuesNewTokens_andRejectsTheOldRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(ACCESS_TOKEN_COOKIE))
                .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE));

        // rotation: the old refresh token was revoked and cannot be used again
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_returns401_whenNoRefreshTokenIsSent() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_clearsCookies_andRevokesTheRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout").cookie(refreshToken))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(ACCESS_TOKEN_COOKIE, 0))
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE, 0));

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshToken))
                .andExpect(status().isUnauthorized());
    }
}
