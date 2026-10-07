package com.telecom.monitor.controller;

import com.telecom.monitor.dto.NetworkEventDto;
import com.telecom.monitor.dto.TelemetrySimulationRequest;
import com.telecom.monitor.model.NetworkEvent;
import com.telecom.monitor.service.NetworkEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/network-events")
@CrossOrigin(origins = "*")
@Tag(name = "Network Events", description = "Network Telemetry Ingestion and Simulator APIs")
public class NetworkEventController {

    private final NetworkEventService networkEventService;

    public NetworkEventController(NetworkEventService networkEventService) {
        this.networkEventService = networkEventService;
    }

    @PostMapping
    @Operation(summary = "Ingest a single network telemetry event")
    public ResponseEntity<NetworkEvent> ingestEvent(@Valid @RequestBody NetworkEventDto dto) {
        NetworkEvent processed = networkEventService.processEvent(dto);
        return new ResponseEntity<>(processed, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get recent network telemetry events stream (latest 50)")
    public ResponseEntity<List<NetworkEvent>> getRecentEvents(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(networkEventService.getRecentEvents(limit));
    }

    @GetMapping("/tower/{towerId}")
    @Operation(summary = "Get historical events for a specific cell tower with pagination")
    public ResponseEntity<List<NetworkEvent>> getEventsByTower(
            @PathVariable String towerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(networkEventService.getEventsByTower(towerId, page, size));
    }

    @PostMapping("/simulate")
    @Operation(summary = "Trigger a simulated telemetry scenario (NORMAL, FIBER_CUT, CONGESTION, OUTAGE)")
    public ResponseEntity<NetworkEvent> simulateEvent(@RequestBody(required = false) TelemetrySimulationRequest request) {
        if (request == null) {
            request = new TelemetrySimulationRequest(null, "NORMAL");
        }
        NetworkEvent simulated = networkEventService.simulateEvent(request);
        return new ResponseEntity<>(simulated, HttpStatus.CREATED);
    }
}
