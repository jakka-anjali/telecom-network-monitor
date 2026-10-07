package com.telecom.monitor;

import com.telecom.monitor.dto.IncidentResponseDto;
import com.telecom.monitor.dto.IncidentStatusUpdateRequest;
import com.telecom.monitor.exception.InvalidStatusTransitionException;
import com.telecom.monitor.model.*;
import com.telecom.monitor.repository.IncidentRepository;
import com.telecom.monitor.repository.TowerRepository;
import com.telecom.monitor.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private TowerRepository towerRepository;

    private IncidentService incidentService;

    private Incident sampleIncident;
    private Tower sampleTower;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(incidentRepository, towerRepository);
        sampleIncident = new Incident(
                "INC-1001", "HYD-4521", "Hyderabad", IncidentSeverity.HIGH, IncidentStatus.OPEN,
                "HIGH Degradation", "Latency > 200ms", "High latency root cause analysis", 240.0, 16.0
        );
        sampleTower = new Tower("HYD-4521", "Hitec City 5G", "Hyderabad", "5G",
                TowerStatus.DEGRADED, 17.4435, 78.3772, 1000, 12000);
    }

    @Test
    @DisplayName("Status transition from OPEN to IN_PROGRESS should succeed")
    void testValidStatusTransition() {
        when(incidentRepository.findById("INC-1001")).thenReturn(Optional.of(sampleIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));

        IncidentStatusUpdateRequest req = new IncidentStatusUpdateRequest(
                IncidentStatus.IN_PROGRESS, "Operator-Ravi", "Investigating fiber link"
        );

        IncidentResponseDto result = incidentService.updateIncidentStatus("INC-1001", req);

        assertEquals(IncidentStatus.IN_PROGRESS, result.getStatus());
        assertEquals("Investigating fiber link", sampleIncident.getResolutionNotes());
    }

    @Test
    @DisplayName("Resolving an incident should restore Tower status to ACTIVE if no other active incidents")
    void testResolveIncident_RestoresTowerStatus() {
        when(incidentRepository.findById("INC-1001")).thenReturn(Optional.of(sampleIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(i -> i.getArgument(0));
        when(towerRepository.findById("HYD-4521")).thenReturn(Optional.of(sampleTower));
        when(incidentRepository.findByTowerId("HYD-4521")).thenReturn(Collections.singletonList(sampleIncident));

        IncidentStatusUpdateRequest req = new IncidentStatusUpdateRequest(
                IncidentStatus.RESOLVED, "Senior-NOC-Eng", "Spliced broken fiber conduit"
        );

        IncidentResponseDto result = incidentService.updateIncidentStatus("INC-1001", req);

        assertEquals(IncidentStatus.RESOLVED, result.getStatus());
        assertEquals("Senior-NOC-Eng", result.getResolvedBy());
        assertEquals(TowerStatus.ACTIVE, sampleTower.getStatus(), "Tower should be restored to ACTIVE");
        verify(towerRepository, times(1)).save(sampleTower);
    }

    @Test
    @DisplayName("Attempting to transition from RESOLVED back to another state throws InvalidStatusTransitionException")
    void testReopeningResolvedIncident_ThrowsException() {
        sampleIncident.setStatus(IncidentStatus.RESOLVED);
        when(incidentRepository.findById("INC-1001")).thenReturn(Optional.of(sampleIncident));

        IncidentStatusUpdateRequest req = new IncidentStatusUpdateRequest(
                IncidentStatus.OPEN, "Operator-Ravi", "Reopening"
        );

        assertThrows(InvalidStatusTransitionException.class, () -> {
            incidentService.updateIncidentStatus("INC-1001", req);
        });
    }
}
