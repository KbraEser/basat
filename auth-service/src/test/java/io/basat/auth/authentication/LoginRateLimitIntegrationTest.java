package io.basat.auth.authentication;

import io.basat.auth.TestcontainersConfiguration;
import io.basat.auth.tenant.TenantRepository;
import io.basat.auth.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for login rate limiting against real Redis and TimescaleDB.
 * <p>
 * The limit is 5 failed attempts within 15 minutes (see {@code app.login} in application.yml).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginRateLimitIntegrationTest {

    private static final String EMAIL = "ayse@deniz.com";
    private static final String PASSWORD = "GucluSifre123";
    private static final String WRONG_PASSWORD = "WrongPassword1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private StringRedisTemplate redis;

    @BeforeEach
    void setUp() throws Exception {
        // other test classes share the same Redis, so start every test with a clean counter
        clearLoginAttempts();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyName":"Deniz Otelcilik","fullName":"Ayşe Yılmaz","email":"%s","password":"%s"}
                                """.formatted(EMAIL, PASSWORD)))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void cleanUp() {
        clearLoginAttempts();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void login_returns429_afterFiveFailedAttempts_evenWithTheCorrectPassword() throws Exception {
        for (int attempt = 1; attempt <= 5; attempt++) {
            login(WRONG_PASSWORD).andExpect(status().isUnauthorized());
        }

        login(WRONG_PASSWORD).andExpect(status().isTooManyRequests());
        // once blocked, even the right password is refused until the window passes
        login(PASSWORD).andExpect(status().isTooManyRequests());
    }

    @Test
    void successfulLogin_resetsTheFailedAttemptCounter() throws Exception {
        for (int attempt = 1; attempt <= 4; attempt++) {
            login(WRONG_PASSWORD).andExpect(status().isUnauthorized());
        }
        login(PASSWORD).andExpect(status().isOk());

        // the counter starts again from zero, so one more mistake is not blocked
        login(WRONG_PASSWORD).andExpect(status().isUnauthorized());
    }

    private ResultActions login(String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(EMAIL, password)));
    }

    private void clearLoginAttempts() {
        redis.delete(redis.keys("login:attempts:*"));
    }
}
