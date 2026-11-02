package io.basat.monitoring.site;

import com.jayway.jsonpath.JsonPath;
import io.basat.monitoring.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for site management against a real TimescaleDB started by Testcontainers.
 * <p>
 * Tokens are simulated with {@code jwt()}: two random tenant ids stand for two different companies,
 * and the role claim decides which actions are allowed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SiteIntegrationTest {

    private static final String HOTEL = """
            {"name":"Antalya Otel","type":"HOTEL","floorAreaM2":12500.50,"city":"Antalya"}
            """;

    private final UUID companyAId = UUID.randomUUID();
    private final JwtRequestPostProcessor companyA = tokenFor(companyAId, "ADMIN");
    private final JwtRequestPostProcessor companyB = tokenFor(UUID.randomUUID(), "ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteRepository siteRepository;

    @AfterEach
    void cleanUp() {
        siteRepository.deleteAll();
    }

    @Test
    void site_canBeCreatedListedUpdatedAndDeleted() throws Exception {
        String siteId = createSite(companyA, HOTEL);

        mockMvc.perform(get("/api/sites").with(companyA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Antalya Otel"));

        mockMvc.perform(put("/api/sites/" + siteId).with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Antalya Otel","type":"HOTEL","floorAreaM2":13000,"city":"Alanya"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.city").value("Alanya"));

        mockMvc.perform(delete("/api/sites/" + siteId).with(companyA))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/sites/" + siteId).with(companyA))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherCompany_cannotSeeOrChangeTheSite() throws Exception {
        String siteId = createSite(companyA, HOTEL);

        // the site exists, but for company B it behaves as if it did not
        mockMvc.perform(get("/api/sites").with(companyB))
                .andExpect(jsonPath("$.data.length()").value(0));
        mockMvc.perform(get("/api/sites/" + siteId).with(companyB))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/sites/" + siteId).with(companyB)
                        .contentType(MediaType.APPLICATION_JSON).content(HOTEL))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/sites/" + siteId).with(companyB))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/sites/" + siteId).with(companyA))
                .andExpect(status().isOk());
    }

    @Test
    void siteName_mustBeUniqueWithinACompany_butNotAcrossCompanies() throws Exception {
        createSite(companyA, HOTEL);

        mockMvc.perform(post("/api/sites").with(companyA)
                        .contentType(MediaType.APPLICATION_JSON).content(HOTEL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Site name already used"));

        createSite(companyB, HOTEL);
    }

    @Test
    void update_acceptsTheSitesOwnName_butRejectsAnotherSitesName() throws Exception {
        String hotelId = createSite(companyA, HOTEL);
        createSite(companyA, """
                {"name":"Ankara Ofis","type":"OFFICE"}
                """);

        // keeping its own name is not a conflict
        mockMvc.perform(put("/api/sites/" + hotelId).with(companyA)
                        .contentType(MediaType.APPLICATION_JSON).content(HOTEL))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/sites/" + hotelId).with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ankara Ofis","type":"HOTEL"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void createSite_returns400_withEveryInvalidField() throws Exception {
        mockMvc.perform(post("/api/sites").with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","floorAreaM2":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.type").exists())
                .andExpect(jsonPath("$.errors.floorAreaM2").exists());
    }

    @Test
    void request_returns400_forUnknownSiteTypeOrInvalidId() throws Exception {
        mockMvc.perform(post("/api/sites").with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Kale","type":"CASTLE"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/sites/not-a-uuid").with(companyA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void viewer_canReadSites_butCannotChangeThem() throws Exception {
        String siteId = createSite(companyA, HOTEL);
        JwtRequestPostProcessor viewer = tokenFor(companyAId, "VIEWER");

        mockMvc.perform(get("/api/sites").with(viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(post("/api/sites").with(viewer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bodrum Otel","type":"HOTEL"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"));
        mockMvc.perform(put("/api/sites/" + siteId).with(viewer)
                        .contentType(MediaType.APPLICATION_JSON).content(HOTEL))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/sites/" + siteId).with(viewer))
                .andExpect(status().isForbidden());
    }

    @Test
    void energyManager_canCreateSites() throws Exception {
        createSite(tokenFor(companyAId, "ENERGY_MANAGER"), HOTEL);
    }

    private String createSite(JwtRequestPostProcessor company, String body) throws Exception {
        String response = mockMvc.perform(post("/api/sites").with(company)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdAt", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.id");
    }

    private static JwtRequestPostProcessor tokenFor(UUID tenantId, String role) {
        // jwt() skips the JwtAuthenticationConverter, so the authority is set here the same way it maps "role"
        return jwt()
                .jwt(token -> token.claim("tenantId", tenantId.toString()).claim("role", role))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
