package com.telecom.monitor.controller;

import com.telecom.monitor.dto.NetworkHealthSummaryDto;
import com.telecom.monitor.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
@Tag(name = "Analytics", description = "Telecom Network Performance and Health Analytics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/network-health")
    @Operation(summary = "Get executive network health summary, active incident counts, and MTTR")
    public ResponseEntity<NetworkHealthSummaryDto> getNetworkHealth() {
        return ResponseEntity.ok(analyticsService.getNetworkHealthSummary());
    }
}
