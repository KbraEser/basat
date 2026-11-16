package io.basat.monitoring.consumption;

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
@Import({TestcontainersConfiguration.class, ConsumptionIntegrationTest.FixedClock.class})
class ConsumptionIntegrationTest {

    // 12:30 in Istanbul; "today" started at 2026-11-09T21:00Z
    private static final Instant NOW = Instant.parse("2026-11-10T09:30:00Z");

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
    private UUID officeId;

    @BeforeEach
    void createSitesAndReadings() {
        hotelId = createSite(companyId, "Otel");
        officeId = createSite(companyId, "Ofis");
        UUID hotelMeter = createMeter(hotelId, "EM-TEST-001", "ELECTRICITY");
        UUID hotelSolar = createMeter(hotelId, "GES-TEST-001", "SOLAR");
        UUID officeMeter = createMeter(officeId, "EM-TEST-002", "ELECTRICITY");

        save(hotelMeter, "2026-11-10T06:05:00Z", "1.0");   // 09:05 today
        save(hotelMeter, "2026-11-10T06:20:00Z", "2.0");   // same hour
        save(hotelSolar, "2026-11-10T06:10:00Z", "0.5");   // solar, same hour
        save(officeMeter, "2026-11-10T07:15:00Z", "4.0");  // 10:15 today
        save(hotelMeter, "2026-11-09T20:30:00Z", "8.0");   // 23:30 yesterday
    }

    @AfterEach
    void cleanUp() {
        jdbcClient.sql("DELETE FROM readings").update();
        jdbcClient.sql("DELETE FROM meters").update();
        jdbcClient.sql("DELETE FROM sites").update();
    }

    @Test
    void today_returnsHourlyConsumptionAndSolarOfAllSites() throws Exception {
        mockMvc.perform(get("/api/consumption").with(company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("TODAY"))
                .andExpect(jsonPath("$.data.resolution").value("HOUR"))
                .andExpect(jsonPath("$.data.from").value("2026-11-09T21:00:00Z"))
                .andExpect(jsonPath("$.data.points.length()").value(2))
                .andExpect(jsonPath("$.data.points[0].time").value("2026-11-10T06:00:00Z"))
                .andExpect(jsonPath("$.data.points[0].consumptionKwh").value(3.0))
                .andExpect(jsonPath("$.data.points[0].solarKwh").value(0.5))
                .andExpect(jsonPath("$.data.points[1].consumptionKwh").value(4.0));
    }

    @Test
    void siteId_limitsThePointsToThatSite() throws Exception {
        mockMvc.perform(get("/api/consumption").param("siteId", officeId.toString()).with(company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.points.length()").value(1))
                .andExpect(jsonPath("$.data.points[0].consumptionKwh").value(4.0))
                .andExpect(jsonPath("$.data.points[0].solarKwh").value(0));
    }

    @Test
    void lastSevenDays_includeYesterday() throws Exception {
        mockMvc.perform(get("/api/consumption").param("period", "LAST_7_DAYS").with(company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.points.length()").value(3))
                .andExpect(jsonPath("$.data.points[0].consumptionKwh").value(8.0));
    }

    @Test
    void lastThirtyDays_returnsIstanbulDays() throws Exception {
        mockMvc.perform(get("/api/consumption").param("period", "LAST_30_DAYS").with(company))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolution").value("DAY"))
                .andExpect(jsonPath("$.data.points.length()").value(2))
                .andExpect(jsonPath("$.data.points[0].time").value("2026-11-08T21:00:00Z"))
                .andExpect(jsonPath("$.data.points[1].consumptionKwh").value(7.0));
    }

    @Test
    void anotherCompanysSite_isNotFound() throws Exception {
        mockMvc.perform(get("/api/consumption").param("siteId", hotelId.toString()).with(otherCompany))
                .andExpect(status().isNotFound());
    }

    @Test
    void anotherCompany_seesNoPoints() throws Exception {
        mockMvc.perform(get("/api/consumption").with(otherCompany))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.points.length()").value(0));
    }

    @Test
    void unknownPeriod_isRejected() throws Exception {
        mockMvc.perform(get("/api/consumption").param("period", "LAST_YEAR").with(company))
                .andExpect(status().isBadRequest());
    }

    private UUID createSite(UUID tenantId, String name) {
        return jdbcClient.sql("INSERT INTO sites (tenant_id, name, site_type) VALUES (:tenantId, :name, 'HOTEL') RETURNING id")
                .param("tenantId", tenantId).param("name", name).query(UUID.class).single();
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
