package com.telecom.monitor.dto;

import java.util.Map;

public class NetworkHealthSummaryDto {

    private double networkHealthIndex; // 0 - 100%
    private long totalTowers;
    private long activeTowers;
    private long degradedTowers;
    private long downTowers;
    private long totalIncidents;
    private long openIncidents;
    private long acknowledgedIncidents;
    private long inProgressIncidents;
    private long resolvedIncidents;
    private long criticalIncidents;
    private long highIncidents;
    private double averageResolutionTimeMinutes;
    private Map<String, Long> incidentsByRegion;

    public NetworkHealthSummaryDto() {
    }

    public double getNetworkHealthIndex() {
        return networkHealthIndex;
    }

    public void setNetworkHealthIndex(double networkHealthIndex) {
        this.networkHealthIndex = networkHealthIndex;
    }

    public long getTotalTowers() {
        return totalTowers;
    }

    public void setTotalTowers(long totalTowers) {
        this.totalTowers = totalTowers;
    }

    public long getActiveTowers() {
        return activeTowers;
    }

    public void setActiveTowers(long activeTowers) {
        this.activeTowers = activeTowers;
    }

    public long getDegradedTowers() {
        return degradedTowers;
    }

    public void setDegradedTowers(long degradedTowers) {
        this.degradedTowers = degradedTowers;
    }

    public long getDownTowers() {
        return downTowers;
    }

    public void setDownTowers(long downTowers) {
        this.downTowers = downTowers;
    }

    public long getTotalIncidents() {
        return totalIncidents;
    }

    public void setTotalIncidents(long totalIncidents) {
        this.totalIncidents = totalIncidents;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public void setOpenIncidents(long openIncidents) {
        this.openIncidents = openIncidents;
    }

    public long getAcknowledgedIncidents() {
        return acknowledgedIncidents;
    }

    public void setAcknowledgedIncidents(long acknowledgedIncidents) {
        this.acknowledgedIncidents = acknowledgedIncidents;
    }

    public long getInProgressIncidents() {
        return inProgressIncidents;
    }

    public void setInProgressIncidents(long inProgressIncidents) {
        this.inProgressIncidents = inProgressIncidents;
    }

    public long getResolvedIncidents() {
        return resolvedIncidents;
    }

    public void setResolvedIncidents(long resolvedIncidents) {
        this.resolvedIncidents = resolvedIncidents;
    }

    public long getCriticalIncidents() {
        return criticalIncidents;
    }

    public void setCriticalIncidents(long criticalIncidents) {
        this.criticalIncidents = criticalIncidents;
    }

    public long getHighIncidents() {
        return highIncidents;
    }

    public void setHighIncidents(long highIncidents) {
        this.highIncidents = highIncidents;
    }

    public double getAverageResolutionTimeMinutes() {
        return averageResolutionTimeMinutes;
    }

    public void setAverageResolutionTimeMinutes(double averageResolutionTimeMinutes) {
        this.averageResolutionTimeMinutes = averageResolutionTimeMinutes;
    }

    public Map<String, Long> getIncidentsByRegion() {
        return incidentsByRegion;
    }

    public void setIncidentsByRegion(Map<String, Long> incidentsByRegion) {
        this.incidentsByRegion = incidentsByRegion;
    }
}
