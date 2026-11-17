package io.basat.monitoring.kpi;

import io.basat.monitoring.TestcontainersConfiguration;
import io.basat.monitoring.reading.ReadingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, KpiIntegrationTest.FixedClock.class})
// round prices make the expected values easy to check by hand
@TestPropertySource(properties = {
        "app.tariff.type=TIME_OF_USE", "app.tariff.day=5", "app.tariff.peak=8", "app.tariff.night=4",
        "app.carbon.grid-factor=0.5"
})
class KpiIntegrationTest {

    // 20:30 in Istanbul; "today" started at 2026-11-09T21:00Z
    private static final Instant NOW = Instant.parse("2026-11-10T17:30:00Z");

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClock {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    private final UUID companyId = UUID.randomUUID();
    private final JwtRequestPostProcessor company = jwt().jwt(token -> token.claim("tenantId", companyId.toString()));
    private final JwtRequestPostProcessor otherCompany = jwt().jwt(token -> token.claim("tenantId", UUID.randomUUID().toString()));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private ReadingRepository readingRepository;

    private UUID hotelId;

    @BeforeEach
    void createReadings() {
        hotelId = createSite("Otel");
        UUID hotelMeter = createMeter(hotelId, "EM-TEST-001", "ELECTRICITY");
        UUID hotelSolar = createMeter(hotelId, "GES-TEST-001", "SOLAR");

        save(hotelMeter, "2026-11-10T00:30:00Z", "10");  // 03:30 night: 10 x 4 = 40
        save(hotelMeter, "2026-11-10T06:30:00Z", "20");  // 09:30 day:   20 x 5 = 100
        save(hotelMeter, "2026-11-10T15:30:00Z", "5");   // 18:30 peak:   5 x 8 = 40
        save(hotelSolar, "2026-11-10T09:30:00Z", "12");  // 12:30 solar, no cost
    }

    @AfterEach
    void cleanUp() {
        jdbcClient.sql("DELETE FROM readings").update();
        jdbcClient.sql("DELETE FROM meters").update();
        jdbcClient.sql("DELETE FROM sites").update();
    }

    @Test
    void today_returnsConsumptionCostCo2AndSolar() throws Exception {
        mockMvc.perform(get("/api/kpis").with(company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("TODAY"))
                .andExpect(jsonPath("$.data.consumptionKwh").value(35.0))
                .andExpect(jsonPath("$.data.costTry").value(180.0))
                .andExpect(jsonPath("$.data.co2Kg").value(17.5))
                .andExpect(jsonPath("$.data.solarKwh").value(12.0));
    }

    @Test
    void cost_isSplitIntoTariffBands() throws Exception {
        mockMvc.perform(get("/api/kpis").with(company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bands.length()").value(3))
                .andExpect(jsonPath("$.data.bands[0].band").value("DAY"))
                .andExpect(jsonPath("$.data.bands[0].consumptionKwh").value(20.0))
                .andExpect(jsonPath("$.data.bands[0].costTry").value(100.0))
                .andExpect(jsonPath("$.data.bands[1].band").value("PEAK"))
                .andExpect(jsonPath("$.data.bands[1].costTry").value(40.0))
                .andExpect(jsonPath("$.data.bands[2].band").value("NIGHT"))
                .andExpect(jsonPath("$.data.bands[2].costTry").value(40.0));
    }

    @Test
    void noReadings_returnsZeros() throws Exception {
        mockMvc.perform(get("/api/kpis").with(otherCompany))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consumptionKwh").value(0))
                .andExpect(jsonPath("$.data.costTry").value(0))
                .andExpect(jsonPath("$.data.bands[0].costTry").value(0));
    }

    @Test
    void anotherCompanysSite_isNotFound() throws Exception {
        mockMvc.perform(get("/api/kpis").param("siteId", hotelId.toString()).with(otherCompany))
                .andExpect(status().isNotFound());
    }

    private UUID createSite(String name) {
        return jdbcClient.sql("INSERT INTO sites (tenant_id, name, site_type) VALUES (:tenantId, :name, 'HOTEL') RETURNING id")
                .param("tenantId", companyId).param("name", name).query(UUID.class).single();
    }

    private UUID createMeter(UUID siteId, String serialNumber, String type) {
        return jdbcClient.sql("""
                        INSERT INTO meters (site_id, name, serial_number, meter_type)
                        VALUES (:siteId, 'Sayaç', :serialNumber, :type) RETURNING id
                        """)
                .param("siteId", siteId).param("serialNumber", serialNumber).param("type", type)
                .query(UUID.class).single();
    }

    private void save(UUID meterId, String measuredAt, String energyKwh) {
        readingRepository.save(meterId, Instant.parse(measuredAt), new BigDecimal(energyKwh));
    }
}
