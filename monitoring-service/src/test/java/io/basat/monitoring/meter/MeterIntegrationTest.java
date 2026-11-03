package io.basat.monitoring.meter;

import com.jayway.jsonpath.JsonPath;
import io.basat.monitoring.TestcontainersConfiguration;
import io.basat.monitoring.site.SiteRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for meter management against a real TimescaleDB started by Testcontainers.
 * <p>
 * Meters have no tenant column of their own, so these tests check that the company boundary
 * set by the site also protects its meters.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MeterIntegrationTest {

    private static final String MAIN_PANEL = """
            {"name":"Ana Pano","serialNumber":"em-2024-001","type":"ELECTRICITY"}
            """;

    private final UUID companyAId = UUID.randomUUID();
    private final JwtRequestPostProcessor companyA = tokenFor(companyAId, "ADMIN");
    private final JwtRequestPostProcessor companyB = tokenFor(UUID.randomUUID(), "ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRepository meterRepository;

    @Autowired
    private SiteRepository siteRepository;

    @AfterEach
    void cleanUp() {
        // meters first: the foreign key does not let a site with meters be deleted
        meterRepository.deleteAll();
        siteRepository.deleteAll();
    }

    @Test
    void meter_canBeCreatedListedUpdatedAndDeleted() throws Exception {
        String siteId = createSite(companyA, "Antalya Otel");
        String meterId = createMeter(companyA, siteId, MAIN_PANEL);

        mockMvc.perform(get("/api/sites/" + siteId + "/meters").with(companyA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].siteId").value(siteId))
                // stored in one canonical form, so "em-2024-001" and "EM-2024-001" are the same device
                .andExpect(jsonPath("$.data[0].serialNumber").value("EM-2024-001"));

        mockMvc.perform(put("/api/meters/" + meterId).with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"GES Sayacı","serialNumber":"EM-2024-001","type":"SOLAR"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("GES Sayacı"))
                .andExpect(jsonPath("$.data.type").value("SOLAR"));

        mockMvc.perform(delete("/api/meters/" + meterId).with(companyA))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/meters/" + meterId).with(companyA))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherCompany_cannotSeeOrChangeTheMeters() throws Exception {
        String siteId = createSite(companyA, "Antalya Otel");
        String meterId = createMeter(companyA, siteId, MAIN_PANEL);

        mockMvc.perform(get("/api/sites/" + siteId + "/meters").with(companyB))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/sites/" + siteId + "/meters").with(companyB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sahte","serialNumber":"FAKE-1","type":"ELECTRICITY"}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/meters/" + meterId).with(companyB))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/meters/" + meterId).with(companyB)
                        .contentType(MediaType.APPLICATION_JSON).content(MAIN_PANEL))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/meters/" + meterId).with(companyB))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/meters/" + meterId).with(companyA))
                .andExpect(status().isOk());
    }

    @Test
    void serialNumber_mustBeUniqueAcrossAllCompanies() throws Exception {
        String siteA = createSite(companyA, "Antalya Otel");
        String siteB = createSite(companyB, "Bursa Fabrika");
        createMeter(companyA, siteA, MAIN_PANEL);

        // a physical device can only be installed in one place, whatever the letter case
        mockMvc.perform(post("/api/sites/" + siteB + "/meters").with(companyB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana Pano","serialNumber":"EM-2024-001","type":"ELECTRICITY"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Serial number already used"));
    }

    @Test
    void update_acceptsTheMetersOwnSerialNumber_butRejectsAnotherMetersSerialNumber() throws Exception {
        String siteId = createSite(companyA, "Antalya Otel");
        String mainPanelId = createMeter(companyA, siteId, MAIN_PANEL);
        createMeter(companyA, siteId, """
                {"name":"Mutfak","serialNumber":"EM-2024-002","type":"ELECTRICITY"}
                """);

        mockMvc.perform(put("/api/meters/" + mainPanelId).with(companyA)
                        .contentType(MediaType.APPLICATION_JSON).content(MAIN_PANEL))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/meters/" + mainPanelId).with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana Pano","serialNumber":"EM-2024-002","type":"ELECTRICITY"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void siteWithMeters_cannotBeDeleted_untilItsMetersAreRemoved() throws Exception {
        String siteId = createSite(companyA, "Antalya Otel");
        String meterId = createMeter(companyA, siteId, MAIN_PANEL);

        mockMvc.perform(delete("/api/sites/" + siteId).with(companyA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Site has meters"));

        mockMvc.perform(delete("/api/meters/" + meterId).with(companyA))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/sites/" + siteId).with(companyA))
                .andExpect(status().isNoContent());
    }

    @Test
    void createMeter_returns400_withEveryInvalidField() throws Exception {
        String siteId = createSite(companyA, "Antalya Otel");

        mockMvc.perform(post("/api/sites/" + siteId + "/meters").with(companyA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","serialNumber":"EM 2024/001"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.serialNumber").exists())
                .andExpect(jsonPath("$.errors.type").exists());
    }

    @Test
    void viewer_canReadMeters_butCannotChangeThem() throws Exception {
        String siteId = createSite(companyA, "Antalya Otel");
        String meterId = createMeter(companyA, siteId, MAIN_PANEL);
        JwtRequestPostProcessor viewer = tokenFor(companyAId, "VIEWER");

        mockMvc.perform(get("/api/sites/" + siteId + "/meters").with(viewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(post("/api/sites/" + siteId + "/meters").with(viewer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mutfak","serialNumber":"EM-2024-002","type":"ELECTRICITY"}
                                """))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/meters/" + meterId).with(viewer)
                        .contentType(MediaType.APPLICATION_JSON).content(MAIN_PANEL))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/meters/" + meterId).with(viewer))
                .andExpect(status().isForbidden());
    }

    private String createSite(JwtRequestPostProcessor company, String name) throws Exception {
        String response = mockMvc.perform(post("/api/sites").with(company)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"type\":\"HOTEL\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.data.id");
    }

    private String createMeter(JwtRequestPostProcessor company, String siteId, String body) throws Exception {
        String response = mockMvc.perform(post("/api/sites/" + siteId + "/meters").with(company)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
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
