package io.basat.simulator.reading;

import io.basat.simulator.config.SimulatorProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * The ingestion service is replaced by {@link MockRestServiceServer}: no network, and the test checks
 * exactly which request the client sends.
 */
@RestClientTest(IngestionClient.class)
@EnableConfigurationProperties(SimulatorProperties.class)
class IngestionClientTest {

    private static final Reading READING = new Reading(
            "EM-2024-001", Instant.parse("2026-11-05T10:15:00Z"), new BigDecimal("0.4167"));

    @Autowired
    private IngestionClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void send_postsTheReadingAsJson_toTheIngestionService() {
        server.expect(requestTo("http://localhost:8083/api/readings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"serialNumber":"EM-2024-001","measuredAt":"2026-11-05T10:15:00Z","energyKwh":0.4167}
                        """))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        client.send(READING);

        server.verify();
    }

    @Test
    void send_doesNotThrow_whenTheIngestionServiceFails() {
        server.expect(requestTo("http://localhost:8083/api/readings"))
                .andRespond(withServerError().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        assertThatCode(() -> client.send(READING)).doesNotThrowAnyException();
    }
}
