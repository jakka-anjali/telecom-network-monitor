package com.telecom.monitor.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incidents", indexes = {
    @Index(name = "idx_incident_status_severity", columnList = "status, severity"),
    @Index(name = "idx_incident_tower", columnList = "tower_id")
})
public class Incident {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id; // e.g. "INC-1001"

    @Column(name = "tower_id", nullable = false, length = 32)
    private String towerId;

    @Column(name = "region", nullable = false, length = 64)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 16)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private IncidentStatus status;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "root_cause_analysis", length = 1024)
    private String rootCauseAnalysis;

    @Column(name = "trigger_latency_ms")
    private Double triggerLatencyMs;

    @Column(name = "trigger_packet_loss_pct")
    private Double triggerPacketLossPct;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by", length = 64)
    private String resolvedBy;

    @Column(name = "resolution_notes", length = 1024)
    private String resolutionNotes;

    public Incident() {
    }

    public Incident(String id, String towerId, String region, IncidentSeverity severity, IncidentStatus status,
                    String title, String description, String rootCauseAnalysis,
                    Double triggerLatencyMs, Double triggerPacketLossPct) {
        this.id = id;
        this.towerId = towerId;
        this.region = region;
        this.severity = severity;
        this.status = status;
        this.title = title;
        this.description = description;
        this.rootCauseAnalysis = rootCauseAnalysis;
        this.triggerLatencyMs = triggerLatencyMs;
        this.triggerPacketLossPct = triggerPacketLossPct;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTowerId() {
        return towerId;
    }

    public void setTowerId(String towerId) {
        this.towerId = towerId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRootCauseAnalysis() {
        return rootCauseAnalysis;
    }

    public void setRootCauseAnalysis(String rootCauseAnalysis) {
        this.rootCauseAnalysis = rootCauseAnalysis;
    }

    public Double getTriggerLatencyMs() {
        return triggerLatencyMs;
    }

    public void setTriggerLatencyMs(Double triggerLatencyMs) {
        this.triggerLatencyMs = triggerLatencyMs;
    }

    public Double getTriggerPacketLossPct() {
        return triggerPacketLossPct;
    }

    public void setTriggerPacketLossPct(Double triggerPacketLossPct) {
        this.triggerPacketLossPct = triggerPacketLossPct;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
