package com.telecom.monitor.service;

import com.telecom.monitor.model.*;
import com.telecom.monitor.repository.IncidentRepository;
import com.telecom.monitor.repository.TowerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IncidentDetectionEngine {

    private static final Logger log = LoggerFactory.getLogger(IncidentDetectionEngine.class);

    // Configurable Telecom Thresholds
    public static final double LATENCY_CRITICAL_MS = 300.0;
    public static final double LATENCY_HIGH_MS = 200.0;
    public static final double LATENCY_MED_MS = 120.0;

    public static final double PACKET_LOSS_CRITICAL_PCT = 25.0;
    public static final double PACKET_LOSS_HIGH_PCT = 12.0;
    public static final double PACKET_LOSS_MED_PCT = 5.0;

    public static final double THROUGHPUT_MIN_THRESHOLD_MBPS = 15.0;
    public static final int HIGH_TRAFFIC_USERS_THRESHOLD = 10000;

    private final IncidentRepository incidentRepository;
    private final TowerRepository towerRepository;
    private final AlertService alertService;
    private final AtomicLong incidentSequence = new AtomicLong(1000);

    public IncidentDetectionEngine(IncidentRepository incidentRepository,
                                  TowerRepository towerRepository,
                                  AlertService alertService) {
        this.incidentRepository = incidentRepository;
        this.towerRepository = towerRepository;
        this.alertService = alertService;
    }

    @Transactional
    public Optional<Incident> evaluate(NetworkEvent event) {
        // 1. Fetch tower details
        Optional<Tower> towerOpt = towerRepository.findById(event.getTowerId());
        String region = towerOpt.map(Tower::getRegion).orElse("Unknown Region");

        // 2. Evaluate rules and calculate composite abnormality score
        List<String> triggers = new ArrayList<>();
        int compositeScore = 0;

        boolean isTowerDown = "DOWN".equalsIgnoreCase(event.getTowerOperationalStatus());

        if (isTowerDown) {
            compositeScore = 100;
            triggers.add("Cell Tower reports complete DOWN outage");
        } else {
            // Latency analysis
            if (event.getLatencyMs() >= LATENCY_CRITICAL_MS) {
                compositeScore += 45;
                triggers.add(String.format("Critical Latency Spike (%.1f ms > %.0f ms)", event.getLatencyMs(), LATENCY_CRITICAL_MS));
            } else if (event.getLatencyMs() >= LATENCY_HIGH_MS) {
                compositeScore += 25;
                triggers.add(String.format("High Latency detected (%.1f ms > %.0f ms)", event.getLatencyMs(), LATENCY_HIGH_MS));
            } else if (event.getLatencyMs() >= LATENCY_MED_MS) {
                compositeScore += 10;
                triggers.add(String.format("Elevated Latency (%.1f ms)", event.getLatencyMs()));
            }

            // Packet Loss analysis
            if (event.getPacketLossPct() >= PACKET_LOSS_CRITICAL_PCT) {
                compositeScore += 45;
                triggers.add(String.format("Severe Packet Loss (%.1f%% > %.0f%%)", event.getPacketLossPct(), PACKET_LOSS_CRITICAL_PCT));
            } else if (event.getPacketLossPct() >= PACKET_LOSS_HIGH_PCT) {
                compositeScore += 25;
                triggers.add(String.format("High Packet Loss (%.1f%% > %.0f%%)", event.getPacketLossPct(), PACKET_LOSS_HIGH_PCT));
            } else if (event.getPacketLossPct() >= PACKET_LOSS_MED_PCT) {
                compositeScore += 10;
                triggers.add(String.format("Moderate Packet Loss (%.1f%%)", event.getPacketLossPct()));
            }

            // Throughput degradation check
            if (event.getThroughputMbps() < THROUGHPUT_MIN_THRESHOLD_MBPS && event.getActiveUsers() > 2000) {
                compositeScore += 15;
                triggers.add(String.format("Throughput collapse under load (%.1f Mbps for %d users)", event.getThroughputMbps(), event.getActiveUsers()));
            }

            // High subscriber congestion multiplier
            if (event.getActiveUsers() > HIGH_TRAFFIC_USERS_THRESHOLD && compositeScore > 20) {
                compositeScore += 15;
                triggers.add(String.format("High subscriber volume congestion (%d active users)", event.getActiveUsers()));
            }
        }

        // If composite abnormality is below threshold, network is healthy
        if (compositeScore < 25 && !isTowerDown) {
            return Optional.empty();
        }

        // 3. Determine incident severity
        IncidentSeverity severity;
        if (compositeScore >= 70 || isTowerDown) {
            severity = IncidentSeverity.CRITICAL;
        } else if (compositeScore >= 45) {
            severity = IncidentSeverity.HIGH;
        } else {
            severity = IncidentSeverity.MEDIUM;
        }

        String triggersSummary = String.join(" | ", triggers);

        // 4. Incident Deduplication & Correlation Window
        // Check if there is an active incident for this tower (status != RESOLVED)
        Optional<Incident> existingIncidentOpt = incidentRepository
                .findFirstByTowerIdAndStatusNotOrderByCreatedAtDesc(event.getTowerId(), IncidentStatus.RESOLVED);

        if (existingIncidentOpt.isPresent()) {
            Incident existing = existingIncidentOpt.get();
            log.info("Correlating telemetry event with existing active Incident: {} for Tower: {}", existing.getId(), event.getTowerId());

            // Escalate severity if new event is worse
            boolean escalated = false;
            if (severity.ordinal() > existing.getSeverity().ordinal()) {
                log.warn("Escalating Incident {} severity from {} to {}", existing.getId(), existing.getSeverity(), severity);
                existing.setSeverity(severity);
                escalated = true;
            }

            existing.setUpdatedAt(Instant.now());
            existing.setTriggerLatencyMs(event.getLatencyMs());
            existing.setTriggerPacketLossPct(event.getPacketLossPct());
            existing.setRootCauseAnalysis(triggersSummary + " (Latest telemetry update: " + Instant.now() + ")");
            Incident saved = incidentRepository.save(existing);

            if (escalated) {
                alertService.dispatchAlert(saved, AlertChannel.PAGERDUTY, "noc-oncall-leads@telecom.net");
            }
            return Optional.of(saved);
        }

        // 5. Create New Incident
        String incidentId = "INC-" + incidentSequence.incrementAndGet();
        String title = isTowerDown
                ? "Total Outage on Tower " + event.getTowerId()
                : severity + " Network Degradation on Tower " + event.getTowerId();

        Incident incident = new Incident(
                incidentId,
                event.getTowerId(),
                region,
                severity,
                IncidentStatus.OPEN,
                title,
                triggersSummary,
                "Automated detection rule triggered with score " + compositeScore + ": " + triggersSummary,
                event.getLatencyMs(),
                event.getPacketLossPct()
        );

        Incident saved = incidentRepository.save(incident);
        log.warn("NEW INCIDENT CREATED: {} on Tower {} with Severity {}", saved.getId(), saved.getTowerId(), saved.getSeverity());

        // Dispatch immediate alerts for HIGH and CRITICAL incidents
        if (severity == IncidentSeverity.CRITICAL) {
            alertService.dispatchAlert(saved, AlertChannel.PAGERDUTY, "noc-critical-leads@telecom.net");
            alertService.dispatchAlert(saved, AlertChannel.SLACK, "#telco-critical-incidents");
        } else if (severity == IncidentSeverity.HIGH) {
            alertService.dispatchAlert(saved, AlertChannel.SLACK, "#telco-network-alerts");
        }

        // Also mark tower status as DEGRADED or DOWN if applicable
        towerOpt.ifPresent(tower -> {
            if (isTowerDown && tower.getStatus() != TowerStatus.DOWN) {
                tower.setStatus(TowerStatus.DOWN);
                towerRepository.save(tower);
            } else if (tower.getStatus() == TowerStatus.ACTIVE) {
                tower.setStatus(TowerStatus.DEGRADED);
                towerRepository.save(tower);
            }
        });

        return Optional.of(saved);
    }
}
