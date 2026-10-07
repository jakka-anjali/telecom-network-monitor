package com.telecom.monitor.dto;

import com.telecom.monitor.model.IncidentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IncidentStatusUpdateRequest {

    @NotNull(message = "New status is required")
    private IncidentStatus status;

    @NotBlank(message = "Operator name or identifier is required")
    private String operator;

    private String notes;

    public IncidentStatusUpdateRequest() {
    }

    public IncidentStatusUpdateRequest(IncidentStatus status, String operator, String notes) {
        this.status = status;
        this.operator = operator;
        this.notes = notes;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
