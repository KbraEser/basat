package io.basat.simulator.reading;

import io.basat.simulator.config.SimulatorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Sends readings to the ingestion service, the way a meter's data logger would. */

@Slf4j
@Component
public class IngestionClient {

    private final RestClient restClient;

    public IngestionClient(RestClient.Builder builder, SimulatorProperties properties) {
        this.restClient = builder.baseUrl(properties.ingestionUrl()).build();
    }

    public void send(Reading reading){
        try {
            restClient.post()
                    .uri("/api/readings")
                    .body(reading)
                    .retrieve()
                    .toBodilessEntity();
        }catch (RestClientException e) {
            // keep simulating: a real meter also keeps measuring while the server is unreachable
            log.warn("Reading of meter {} was not sent: {}", reading.serialNumber(), e.getMessage());
        }

    }
}
