package com.telecom.monitor.service;

import com.telecom.monitor.dto.NetworkEventDto;
import com.telecom.monitor.dto.TelemetrySimulationRequest;
import com.telecom.monitor.exception.ResourceNotFoundException;
import com.telecom.monitor.model.Incident;
import com.telecom.monitor.model.NetworkEvent;
import com.telecom.monitor.model.Tower;
import com.telecom.monitor.repository.NetworkEventRepository;
import com.telecom.monitor.repository.TowerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class NetworkEventService {

    private final NetworkEventRepository networkEventRepository;
    private final TowerRepository towerRepository;
    private final IncidentDetectionEngine incidentDetectionEngine;
    private final Random random = new Random();

    public NetworkEventService(NetworkEventRepository networkEventRepository,
                               TowerRepository towerRepository,
                               IncidentDetectionEngine incidentDetectionEngine) {
        this.networkEventRepository = networkEventRepository;
        this.towerRepository = towerRepository;
        this.incidentDetectionEngine = incidentDetectionEngine;
    }

    @Transactional
    public NetworkEvent processEvent(NetworkEventDto dto) {
        // Validate tower existence
        Tower tower = towerRepository.findById(dto.getTowerId())
                .orElseThrow(() -> new ResourceNotFoundException("Tower not found with ID: " + dto.getTowerId()));

        NetworkEvent event = new NetworkEvent(
                dto.getTowerId(),
                dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now(),
                dto.getLatencyMs(),
                dto.getPacketLossPct(),
                dto.getThroughputMbps(),
                dto.getActiveUsers(),
                dto.getSignalStrengthDbm(),
                dto.getJitterMs(),
                dto.getTowerOperationalStatus() != null ? dto.getTowerOperationalStatus() : "ACTIVE"
        );

        NetworkEvent saved = networkEventRepository.save(event);

        // Run incident detection pipeline
        incidentDetectionEngine.evaluate(saved);

        return saved;
    }

    public List<NetworkEvent> getRecentEvents(int limit) {
        return networkEventRepository.findTop50ByOrderByTimestampDesc();
    }

    public List<NetworkEvent> getEventsByTower(String towerId, int page, int size) {
        return networkEventRepository.findByTowerIdOrderByTimestampDesc(towerId, PageRequest.of(page, size));
    }

    @Transactional
    public NetworkEvent simulateEvent(TelemetrySimulationRequest request) {
        String towerId = request.getTowerId();
        if (towerId == null || towerId.isBlank()) {
            List<Tower> towers = towerRepository.findAll();
            if (towers.isEmpty()) {
                throw new ResourceNotFoundException("No telecom towers available to simulate telemetry");
            }
            towerId = towers.get(random.nextInt(towers.size())).getId();
        }

        String scenario = request.getScenario() != null ? request.getScenario().toUpperCase() : "NORMAL";

        double latency;
        double packetLoss;
        double throughput;
        int activeUsers;
        double signalStrength;
        double jitter;
        String status = "ACTIVE";

        switch (scenario) {
            case "FIBER_CUT" -> {
                latency = 280.0 + (random.nextDouble() * 120.0);
                packetLoss = 22.0 + (random.nextDouble() * 18.0);
                throughput = 8.0 + (random.nextDouble() * 10.0);
                activeUsers = 12000 + random.nextInt(4000);
                signalStrength = -108.0;
                jitter = 45.0 + (random.nextDouble() * 20.0);
                status = "DEGRADED";
            }
            case "CONGESTION" -> {
                latency = 210.0 + (random.nextDouble() * 50.0);
                packetLoss = 13.0 + (random.nextDouble() * 8.0);
                throughput = 14.0 + (random.nextDouble() * 15.0);
                activeUsers = 16000 + random.nextInt(6000);
                signalStrength = -95.0;
                jitter = 28.0 + (random.nextDouble() * 15.0);
                status = "DEGRADED";
            }
            case "OUTAGE" -> {
                latency = 999.0;
                packetLoss = 100.0;
                throughput = 0.0;
                activeUsers = 0;
                signalStrength = -130.0;
                jitter = 0.0;
                status = "DOWN";
            }
            default -> { // NORMAL
                latency = 25.0 + (random.nextDouble() * 45.0);
                packetLoss = 0.1 + (random.nextDouble() * 1.5);
                throughput = 95.0 + (random.nextDouble() * 110.0);
                activeUsers = 3000 + random.nextInt(5000);
                signalStrength = -75.0 + (random.nextDouble() * 15.0);
                jitter = 3.0 + (random.nextDouble() * 6.0);
                status = "ACTIVE";
            }
        }

        NetworkEventDto dto = new NetworkEventDto(
                towerId,
                Instant.now(),
                Math.round(latency * 10.0) / 10.0,
                Math.round(packetLoss * 10.0) / 10.0,
                Math.round(throughput * 10.0) / 10.0,
                activeUsers,
                Math.round(signalStrength * 10.0) / 10.0,
                Math.round(jitter * 10.0) / 10.0,
                status
        );

        return processEvent(dto);
    }
}
