package io.basat.ingestion.reading;

import io.basat.ingestion.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the readings endpoint. The publisher is mocked, so these tests
 * do not depend on where readings go next (log today, Kafka later).
 */
@WebMvcTest(ReadingController.class)
@Import(GlobalExceptionHandler.class)
class ReadingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingPublisher readingPublisher;

    @Test
    void validReading_isAccepted_andPublishedInCanonicalForm() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"em-2024-001","measuredAt":"2026-11-04T10:15:00Z","energyKwh":12.75}
                                """))
                .andExpect(status().isAccepted());

        verify(readingPublisher).publish(new Reading(
                "EM-2024-001", Instant.parse("2026-11-04T10:15:00Z"), new BigDecimal("12.75")));
    }

    @Test
    void invalidReading_returns400_withEveryInvalidField_andIsNotPublished() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"EM 001","measuredAt":"2099-01-01T00:00:00Z","energyKwh":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.serialNumber").exists())
                .andExpect(jsonPath("$.errors.measuredAt").exists())
                .andExpect(jsonPath("$.errors.energyKwh").exists());

        verify(readingPublisher, never()).publish(any());
    }

    @Test
    void malformedBody_returns400() throws Exception {
        mockMvc.perform(post("/api/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"serialNumber":"EM-2024-001","measuredAt":"yesterday","energyKwh":12.75}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));

        verify(readingPublisher, never()).publish(any());
    }
}
