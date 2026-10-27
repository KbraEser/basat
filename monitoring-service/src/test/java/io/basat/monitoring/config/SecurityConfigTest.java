package io.basat.monitoring.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that monitoring-service only lets requests through with a valid access token issued by auth-service.
 */
@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpoint_returns401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/sites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_returns401_withForgedToken() throws Exception {
        mockMvc.perform(get("/api/sites").cookie(new Cookie("access_token", "forged-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_passesSecurity_withValidToken() throws Exception {
        // jwt() stands in for a verified token; 404 means security let the request through to routing
        mockMvc.perform(get("/api/sites").with(jwt()))
                .andExpect(status().isNotFound());
    }
}
