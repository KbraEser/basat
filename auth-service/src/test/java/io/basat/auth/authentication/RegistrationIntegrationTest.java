package io.basat.auth.authentication;

import io.basat.auth.TestcontainersConfiguration;
import io.basat.auth.tenant.TenantRepository;
import io.basat.auth.user.Role;
import io.basat.auth.user.User;
import io.basat.auth.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for company registration.
 * <p>
 * Runs the whole application against a real TimescaleDB started by Testcontainers.
 * Nothing is mocked: the request goes through the controller, service and repositories
 * into the database, and the test then checks what was actually stored.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RegistrationIntegrationTest {

    private static final String REGISTER_URL = "/api/auth/register";

    private static final String REGISTER_REQUEST = """
            {"companyName":"Deniz Otelcilik","fullName":"Ayşe Yılmaz","email":"Ayse@Deniz.com","password":"GucluSifre123"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @AfterEach
    void cleanUp() {
        // users reference tenants, so users must be deleted first
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @Test
    void register_storesTenantAndAdminWithHashedPassword() throws Exception {
        // when
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("ayse@deniz.com"));

        // then
        User user = userRepository.findByEmail("ayse@deniz.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getPasswordHash())
                .isNotEqualTo("GucluSifre123")
                .startsWith("$2a$");
        assertThat(tenantRepository.findAll())
                .singleElement()
                .satisfies(tenant -> assertThat(tenant.getName()).isEqualTo("Deniz Otelcilik"));
    }

    @Test
    void register_returns409AndCreatesNothingNew_whenEmailIsAlreadyUsed() throws Exception {
        // given
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_REQUEST))
                .andExpect(status().isCreated());

        // when + then
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REGISTER_REQUEST))
                .andExpect(status().isConflict());

        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
