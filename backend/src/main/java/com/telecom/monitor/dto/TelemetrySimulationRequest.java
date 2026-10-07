package com.telecom.monitor.dto;

public class TelemetrySimulationRequest {

    private String towerId;
    private String scenario; // "NORMAL", "FIBER_CUT", "CONGESTION", "OUTAGE"

    public TelemetrySimulationRequest() {
    }

    public TelemetrySimulationRequest(String towerId, String scenario) {
        this.towerId = towerId;
        this.scenario = scenario;
    }

    public String getTowerId() {
        return towerId;
    }

    public void setTowerId(String towerId) {
        this.towerId = towerId;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }
}
