package com.telecom.monitor.controller;

import com.telecom.monitor.dto.IncidentResponseDto;
import com.telecom.monitor.dto.IncidentStatusUpdateRequest;
import com.telecom.monitor.model.Alert;
import com.telecom.monitor.model.IncidentSeverity;
import com.telecom.monitor.model.IncidentStatus;
import com.telecom.monitor.service.AlertService;
import com.telecom.monitor.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*")
@Tag(name = "Incidents", description = "Telecom Incident Lifecycle Management APIs")
public class IncidentController {

    private final IncidentService incidentService;
    private final AlertService alertService;

    public IncidentController(IncidentService incidentService, AlertService alertService) {
        this.incidentService = incidentService;
        this.alertService = alertService;
    }

    @GetMapping
    @Operation(summary = "Query and filter network incidents by status, severity, or region")
    public ResponseEntity<List<IncidentResponseDto>> getIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(incidentService.getIncidents(status, severity, region));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed information for a specific incident")
    public ResponseEntity<IncidentResponseDto> getIncidentById(@PathVariable String id) {
        return ResponseEntity.ok(incidentService.getIncidentById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update incident status (ACKNOWLEDGED, IN_PROGRESS, RESOLVED) with operator notes")
    public ResponseEntity<IncidentResponseDto> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody IncidentStatusUpdateRequest request) {
        return ResponseEntity.ok(incidentService.updateIncidentStatus(id, request));
    }

    @GetMapping("/{id}/alerts")
    @Operation(summary = "Get audit trail of alerts dispatched for this incident")
    public ResponseEntity<List<Alert>> getIncidentAlerts(@PathVariable String id) {
        return ResponseEntity.ok(alertService.getAlertsByIncident(id));
    }
}
