package com.telecom.monitor;

import com.telecom.monitor.model.*;
import com.telecom.monitor.repository.IncidentRepository;
import com.telecom.monitor.repository.TowerRepository;
import com.telecom.monitor.service.AlertService;
import com.telecom.monitor.service.IncidentDetectionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentDetectionEngineTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private TowerRepository towerRepository;

    @Mock
    private AlertService alertService;

    private IncidentDetectionEngine detectionEngine;

    private Tower sampleTower;

    @BeforeEach
    void setUp() {
        detectionEngine = new IncidentDetectionEngine(incidentRepository, towerRepository, alertService);
        sampleTower = new Tower("HYD-4521", "Hitec City 5G", "Hyderabad", "5G",
                TowerStatus.ACTIVE, 17.4435, 78.3772, 1000, 12000);
    }

    @Test
    @DisplayName("Normal telemetry should NOT generate any incident")
    void testNormalTelemetry_NoIncident() {
        when(towerRepository.findById("HYD-4521")).thenReturn(Optional.of(sampleTower));

        NetworkEvent normalEvent = new NetworkEvent(
                "HYD-4521", Instant.now(), 45.0, 0.4, 180.0, 5000, -78.0, 3.5, "ACTIVE"
        );

        Optional<Incident> result = detectionEngine.evaluate(normalEvent);

        assertFalse(result.isPresent());
        verify(incidentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Abnormal telemetry (high latency & packet loss) should create CRITICAL incident")
    void testAbnormalTelemetry_CreatesCriticalIncident() {
        when(towerRepository.findById("HYD-4521")).thenReturn(Optional.of(sampleTower));
        when(incidentRepository.findFirstByTowerIdAndStatusNotOrderByCreatedAtDesc("HYD-4521", IncidentStatus.RESOLVED))
                .thenReturn(Optional.empty());

        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NetworkEvent severeEvent = new NetworkEvent(
                "HYD-4521", Instant.now(), 320.0, 28.5, 8.0, 14000, -108.0, 42.0, "DEGRADED"
        );

        Optional<Incident> result = detectionEngine.evaluate(severeEvent);

        assertTrue(result.isPresent());
        Incident incident = result.get();
        assertEquals(IncidentSeverity.CRITICAL, incident.getSeverity());
        assertEquals(IncidentStatus.OPEN, incident.getStatus());
        assertEquals("HYD-4521", incident.getTowerId());
        assertEquals("Hyderabad", incident.getRegion());

        verify(incidentRepository, times(1)).save(any(Incident.class));
        verify(alertService, atLeastOnce()).dispatchAlert(any(Incident.class), eq(AlertChannel.PAGERDUTY), anyString());
    }

    @Test
    @DisplayName("Repeated abnormal event should correlate to existing incident without creating duplicates")
    void testDeduplication_CorrelatesToExistingIncident() {
        when(towerRepository.findById("HYD-4521")).thenReturn(Optional.of(sampleTower));

        Incident existingIncident = new Incident(
                "INC-1001", "HYD-4521", "Hyderabad", IncidentSeverity.HIGH, IncidentStatus.OPEN,
                "HIGH Degradation", "Initial trigger", "Latency spike above threshold", 220.0, 14.0
        );

        when(incidentRepository.findFirstByTowerIdAndStatusNotOrderByCreatedAtDesc("HYD-4521", IncidentStatus.RESOLVED))
                .thenReturn(Optional.of(existingIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NetworkEvent subsequentEvent = new NetworkEvent(
                "HYD-4521", Instant.now(), 340.0, 29.0, 6.0, 13000, -112.0, 50.0, "DEGRADED"
        );

        Optional<Incident> result = detectionEngine.evaluate(subsequentEvent);

        assertTrue(result.isPresent());
        Incident correlated = result.get();
        assertEquals("INC-1001", correlated.getId());
        assertEquals(IncidentSeverity.CRITICAL, correlated.getSeverity(), "Severity should escalate to CRITICAL");

        verify(incidentRepository, times(1)).save(existingIncident);
    }
}
