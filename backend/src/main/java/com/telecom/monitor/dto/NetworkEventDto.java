package com.telecom.monitor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class NetworkEventDto {

    @NotBlank(message = "Tower ID is required")
    private String towerId;

    private Instant timestamp;

    @NotNull(message = "Latency is required")
    @Min(value = 0, message = "Latency cannot be negative")
    private Double latencyMs;

    @NotNull(message = "Packet loss is required")
    @Min(value = 0, message = "Packet loss cannot be negative")
    @Max(value = 100, message = "Packet loss cannot exceed 100%")
    private Double packetLossPct;

    @NotNull(message = "Throughput is required")
    @Min(value = 0, message = "Throughput cannot be negative")
    private Double throughputMbps;

    @NotNull(message = "Active users is required")
    @Min(value = 0, message = "Active users cannot be negative")
    private Integer activeUsers;

    private Double signalStrengthDbm;
    private Double jitterMs;
    private String towerOperationalStatus;

    public NetworkEventDto() {
    }

    public NetworkEventDto(String towerId, Instant timestamp, Double latencyMs, Double packetLossPct,
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
