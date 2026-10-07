package com.telecom.monitor.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "network_events", indexes = {
    @Index(name = "idx_event_tower_timestamp", columnList = "tower_id, timestamp")
})
public class NetworkEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tower_id", nullable = false, length = 32)
    private String towerId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "latency_ms", nullable = false)
    private Double latencyMs;

    @Column(name = "packet_loss_pct", nullable = false)
    private Double packetLossPct;

    @Column(name = "throughput_mbps", nullable = false)
    private Double throughputMbps;

    @Column(name = "active_users", nullable = false)
    private Integer activeUsers;

    @Column(name = "signal_strength_dbm")
    private Double signalStrengthDbm;

    @Column(name = "jitter_ms")
    private Double jitterMs;

    @Column(name = "tower_operational_status", length = 32)
    private String towerOperationalStatus;

    public NetworkEvent() {
    }

    public NetworkEvent(String towerId, Instant timestamp, Double latencyMs, Double packetLossPct,
                        Double throughputMbps, Integer activeUsers, Double signalStrengthDbm,
                        Double jitterMs, String towerOperationalStatus) {
        this.towerId = towerId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.latencyMs = latencyMs;
        this.packetLossPct = packetLossPct;
        this.throughputMbps = throughputMbps;
        this.activeUsers = activeUsers;
        this.signalStrengthDbm = signalStrengthDbm;
        this.jitterMs = jitterMs;
        this.towerOperationalStatus = towerOperationalStatus != null ? towerOperationalStatus : "ACTIVE";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTowerId() {
        return towerId;
    }

    public void setTowerId(String towerId) {
        this.towerId = towerId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Double getPacketLossPct() {
        return packetLossPct;
    }

    public void setPacketLossPct(Double packetLossPct) {
        this.packetLossPct = packetLossPct;
    }

    public Double getThroughputMbps() {
        return throughputMbps;
    }

    public void setThroughputMbps(Double throughputMbps) {
        this.throughputMbps = throughputMbps;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Double getSignalStrengthDbm() {
        return signalStrengthDbm;
    }

    public void setSignalStrengthDbm(Double signalStrengthDbm) {
        this.signalStrengthDbm = signalStrengthDbm;
    }

    public Double getJitterMs() {
        return jitterMs;
    }

    public void setJitterMs(Double jitterMs) {
        this.jitterMs = jitterMs;
    }

    public String getTowerOperationalStatus() {
        return towerOperationalStatus;
    }

    public void setTowerOperationalStatus(String towerOperationalStatus) {
        this.towerOperationalStatus = towerOperationalStatus;
    }
}
