package com.telecom.monitor.service;

import com.telecom.monitor.dto.IncidentResponseDto;
import com.telecom.monitor.dto.IncidentStatusUpdateRequest;
import com.telecom.monitor.exception.InvalidStatusTransitionException;
import com.telecom.monitor.exception.ResourceNotFoundException;
import com.telecom.monitor.model.Incident;
import com.telecom.monitor.model.IncidentSeverity;
import com.telecom.monitor.model.IncidentStatus;
import com.telecom.monitor.model.Tower;
import com.telecom.monitor.model.TowerStatus;
import com.telecom.monitor.repository.IncidentRepository;
import com.telecom.monitor.repository.TowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final TowerRepository towerRepository;

    public IncidentService(IncidentRepository incidentRepository, TowerRepository towerRepository) {
        this.incidentRepository = incidentRepository;
        this.towerRepository = towerRepository;
    }

    public List<IncidentResponseDto> getIncidents(IncidentStatus status, IncidentSeverity severity, String region) {
        List<Incident> incidents;

        if (status != null) {
            incidents = incidentRepository.findByStatus(status);
        } else if (severity != null) {
            incidents = incidentRepository.findBySeverity(severity);
        } else if (region != null && !region.isBlank()) {
            incidents = incidentRepository.findByRegionIgnoreCase(region);
        } else {
            incidents = incidentRepository.findAll();
        }

        return incidents.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public IncidentResponseDto getIncidentById(String id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));
        return mapToDto(incident);
    }

    @Transactional
    public IncidentResponseDto updateIncidentStatus(String id, IncidentStatusUpdateRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        IncidentStatus currentStatus = incident.getStatus();
        IncidentStatus targetStatus = request.getStatus();

        // Validate state machine transitions
        if (currentStatus == IncidentStatus.RESOLVED && targetStatus != IncidentStatus.RESOLVED) {
            throw new InvalidStatusTransitionException("Cannot reopen a RESOLVED incident. Create a new incident if symptoms re-occur.");
        }

        incident.setStatus(targetStatus);
        incident.setUpdatedAt(Instant.now());

        if (targetStatus == IncidentStatus.RESOLVED) {
            incident.setResolvedAt(Instant.now());
            incident.setResolvedBy(request.getOperator());
            incident.setResolutionNotes(request.getNotes());

            // Check if all incidents for this tower are resolved; if so, restore tower status to ACTIVE
            Optional<Tower> towerOpt = towerRepository.findById(incident.getTowerId());
            if (towerOpt.isPresent()) {
                Tower tower = towerOpt.get();
                long otherActiveCount = incidentRepository.findByTowerId(tower.getId()).stream()
                        .filter(i -> !i.getId().equals(incident.getId()) && i.getStatus() != IncidentStatus.RESOLVED)
                        .count();
                if (otherActiveCount == 0) {
                    tower.setStatus(TowerStatus.ACTIVE);
                    towerRepository.save(tower);
                }
            }
        } else if (request.getNotes() != null && !request.getNotes().isBlank()) {
            incident.setResolutionNotes(request.getNotes());
        }

        Incident updated = incidentRepository.save(incident);
        return mapToDto(updated);
    }

    public IncidentResponseDto mapToDto(Incident incident) {
        IncidentResponseDto dto = new IncidentResponseDto();
        dto.setId(incident.getId());
        dto.setTowerId(incident.getTowerId());
        dto.setRegion(incident.getRegion());
        dto.setSeverity(incident.getSeverity());
        dto.setStatus(incident.getStatus());
        dto.setTitle(incident.getTitle());
        dto.setDescription(incident.getDescription());
        dto.setRootCauseAnalysis(incident.getRootCauseAnalysis());
        dto.setTriggerLatencyMs(incident.getTriggerLatencyMs());
        dto.setTriggerPacketLossPct(incident.getTriggerPacketLossPct());
        dto.setCreatedAt(incident.getCreatedAt());
        dto.setUpdatedAt(incident.getUpdatedAt());
        dto.setResolvedAt(incident.getResolvedAt());
        dto.setResolvedBy(incident.getResolvedBy());
        dto.setResolutionNotes(incident.getResolutionNotes());

        towerRepository.findById(incident.getTowerId()).ifPresent(t -> {
            dto.setTowerName(t.getName());
            dto.setTechnology(t.getTechnology());
        });

        Instant end = incident.getResolvedAt() != null ? incident.getResolvedAt() : Instant.now();
        dto.setDurationMinutes(Duration.between(incident.getCreatedAt(), end).toMinutes());

        return dto;
    }
}
