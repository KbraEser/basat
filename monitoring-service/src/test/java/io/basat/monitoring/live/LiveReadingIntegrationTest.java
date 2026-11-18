package io.basat.monitoring.live;

import io.basat.monitoring.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class LiveReadingIntegrationTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID OTHER_COMPANY_ID = UUID.randomUUID();

    @LocalServerPort
    private int port;

    @Value("${app.kafka.readings-topic}")
    private String readingsTopic;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private SimpUserRegistry userRegistry;

    // the real decoder would fetch auth-service's public key; here each test token maps to a company
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private WebSocketStompClient stompClient;
    private UUID siteId;

    @BeforeEach
    void setUp() {
        when(jwtDecoder.decode("company-token")).thenReturn(tokenOf(COMPANY_ID));
        when(jwtDecoder.decode("other-company-token")).thenReturn(tokenOf(OTHER_COMPANY_ID));

        siteId = jdbcClient.sql("INSERT INTO sites (tenant_id, name, site_type) VALUES (:t, 'Otel', 'HOTEL') RETURNING id")
                .param("t", COMPANY_ID).query(UUID.class).single();
        jdbcClient.sql("""
                        INSERT INTO meters (site_id, name, serial_number, meter_type)
                        VALUES (:s, 'Ana Pano', 'EM-LIVE-001', 'ELECTRICITY')
                        """)
                .param("s", siteId).update();

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    }

    @AfterEach
    void cleanUp() {
        stompClient.stop();
        jdbcClient.sql("DELETE FROM readings").update();
        jdbcClient.sql("DELETE FROM meters").update();
        jdbcClient.sql("DELETE FROM sites").update();
    }

    @Test
    void readingFromKafka_reachesTheSubscribedBrowser() throws Exception {
        StompSession session = connect("company-token", new StompSessionHandlerAdapter() { });
        String destination = "/topic/tenant/" + COMPANY_ID + "/site/" + siteId;
        BlockingQueue<LiveReading> received = new LinkedBlockingQueue<>();
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LiveReading.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                received.add((LiveReading) payload);
            }
        });
        // SUBSCRIBE is asynchronous: wait until the server has registered it before sending the reading
        await().atMost(Duration.ofSeconds(5))
                .until(() -> !userRegistry.findSubscriptions(s -> s.getDestination().equals(destination)).isEmpty());

        kafkaTemplate.send(readingsTopic, "EM-LIVE-001",
                """
                {"serialNumber":"EM-LIVE-001","measuredAt":"2026-11-19T10:30:00Z","energyKwh":0.1150}
                """);

        LiveReading reading = received.poll(15, TimeUnit.SECONDS);
        assertThat(reading).isNotNull();
        assertThat(reading.serialNumber()).isEqualTo("EM-LIVE-001");
        assertThat(reading.measuredAt()).isEqualTo(Instant.parse("2026-11-19T10:30:00Z"));
        assertThat(reading.energyKwh()).isEqualByComparingTo(new BigDecimal("0.1150"));
    }

    @Test
    void subscribingToAnotherCompanysSite_isRejected() throws Exception {
        CompletableFuture<String> error = new CompletableFuture<>();
        StompSession session = connect("other-company-token", new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // the server answers a rejected SUBSCRIBE with an ERROR frame
                error.complete(headers.getFirst("message"));
            }
        });

        session.subscribe("/topic/tenant/" + COMPANY_ID + "/site/" + siteId, new StompSessionHandlerAdapter() { });

        assertThat(error.get(5, TimeUnit.SECONDS)).isNotBlank();
        await().atMost(Duration.ofSeconds(5)).until(() -> !session.isConnected());
    }

    @Test
    void connectingWithoutToken_isRejected() {
        assertThatThrownBy(() -> stompClient.connectAsync(url(), new WebSocketHttpHeaders(), new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS))
                .hasMessageContaining("401");
    }

    private StompSession connect(String token, StompSessionHandlerAdapter handler) throws Exception {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        // the browser sends the access token cookie with the WebSocket handshake, just like with REST calls
        headers.add("Cookie", "access_token=" + token);
        return stompClient.connectAsync(url(), headers, handler).get(5, TimeUnit.SECONDS);
    }

    private String url() {
        return "ws://localhost:" + port + "/ws";
    }

    private static Jwt tokenOf(UUID tenantId) {
        return Jwt.withTokenValue("token").header("alg", "none")
                .subject(UUID.randomUUID().toString())
                .claim("tenantId", tenantId.toString())
                .claim("role", "VIEWER")
                .build();
    }
}
