package io.basat.auth.token;

import io.basat.auth.authentication.AuthCookieService;
import io.basat.auth.config.JwtConfig;
import io.basat.auth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JwksController.class)
@Import({SecurityConfig.class, JwtConfig.class, AuthCookieService.class})
class JwksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void jwks_isPublic_andPublishesTheRsaPublicKey() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].kid").exists())
                .andExpect(jsonPath("$.keys[0].n").exists());
    }

    @Test
    void jwks_neverExposesThePrivateKey() throws Exception {
        // "d" (and the CRT parameters) belong to the private key; publishing them would let anyone sign tokens
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(jsonPath("$.keys[0].d").doesNotExist())
                .andExpect(jsonPath("$.keys[0].p").doesNotExist())
                .andExpect(jsonPath("$.keys[0].q").doesNotExist());
    }
}
