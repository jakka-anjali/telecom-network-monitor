package com.telecom.monitor.dto;

import com.telecom.monitor.model.IncidentSeverity;
import com.telecom.monitor.model.IncidentStatus;
import java.time.Instant;

public class IncidentResponseDto {

    private String id;
    private String towerId;
    private String towerName;
    private String region;
    private String technology;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String title;
    private String description;
    private String rootCauseAnalysis;
    private Double triggerLatencyMs;
    private Double triggerPacketLossPct;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private String resolvedBy;
    private String resolutionNotes;
    private Long durationMinutes;

    public IncidentResponseDto() {
    }

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

    public String getTowerName() {
        return towerName;
    }

    public void setTowerName(String towerName) {
        this.towerName = towerName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
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

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
