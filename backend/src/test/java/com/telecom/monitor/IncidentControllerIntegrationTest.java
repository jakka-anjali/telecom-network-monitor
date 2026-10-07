package com.telecom.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.monitor.dto.NetworkEventDto;
import com.telecom.monitor.model.IncidentSeverity;
import com.telecom.monitor.model.IncidentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/towers should return seeded telecom towers")
    void testGetTowers() throws Exception {
        mockMvc.perform(get("/api/towers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].region", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analytics/network-health should return health index and tower summary")
    void testGetAnalyticsSummary() throws Exception {
        mockMvc.perform(get("/api/analytics/network-health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.networkHealthIndex", notNullValue()))
                .andExpect(jsonPath("$.totalTowers", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.incidentsByRegion", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/network-events with severe degradation should trigger incident creation")
    void testIngestTelemetryEvent_CreatesIncident() throws Exception {
        NetworkEventDto eventDto = new NetworkEventDto(
                "BLR-1001",
                Instant.now(),
                310.0, // High latency
                26.5,  // Severe packet loss
                7.5,   // Collapsed throughput
                14200, // High traffic users
                -109.0,
                38.0,
                "DEGRADED"
        );

        mockMvc.perform(post("/api/network-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.towerId", is("BLR-1001")))
                .andExpect(jsonPath("$.latencyMs", is(310.0)));

        // Verify incident was created in incidents endpoint
        mockMvc.perform(get("/api/incidents").param("region", "Bengaluru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/network-events with negative latency should trigger 400 Validation Error")
    void testIngestTelemetryEvent_InvalidValidation() throws Exception {
        NetworkEventDto invalidDto = new NetworkEventDto(
                "BLR-1001",
                Instant.now(),
                -50.0, // Invalid negative latency
                150.0, // Invalid > 100% packet loss
                100.0,
                5000,
                -75.0,
                5.0,
                "ACTIVE"
        );

        mockMvc.perform(post("/api/network-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors.latencyMs", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.packetLossPct", notNullValue()));
    }
}
