package com.telecom.monitor.service;

import com.telecom.monitor.dto.NetworkHealthSummaryDto;
import com.telecom.monitor.model.Incident;
import com.telecom.monitor.model.IncidentSeverity;
import com.telecom.monitor.model.IncidentStatus;
import com.telecom.monitor.model.Tower;
import com.telecom.monitor.model.TowerStatus;
import com.telecom.monitor.repository.IncidentRepository;
import com.telecom.monitor.repository.TowerRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TowerRepository towerRepository;
    private final IncidentRepository incidentRepository;

    public AnalyticsService(TowerRepository towerRepository, IncidentRepository incidentRepository) {
        this.towerRepository = towerRepository;
        this.incidentRepository = incidentRepository;
    }

    public NetworkHealthSummaryDto getNetworkHealthSummary() {
        NetworkHealthSummaryDto summary = new NetworkHealthSummaryDto();

        List<Tower> towers = towerRepository.findAll();
        long totalTowers = towers.size();
        long activeTowers = towers.stream().filter(t -> t.getStatus() == TowerStatus.ACTIVE).count();
        long degradedTowers = towers.stream().filter(t -> t.getStatus() == TowerStatus.DEGRADED).count();
        long downTowers = towers.stream().filter(t -> t.getStatus() == TowerStatus.DOWN).count();

        summary.setTotalTowers(totalTowers);
        summary.setActiveTowers(activeTowers);
        summary.setDegradedTowers(degradedTowers);
        summary.setDownTowers(downTowers);

        List<Incident> allIncidents = incidentRepository.findAll();
        summary.setTotalIncidents(allIncidents.size());

        long openCount = allIncidents.stream().filter(i -> i.getStatus() == IncidentStatus.OPEN).count();
        long ackCount = allIncidents.stream().filter(i -> i.getStatus() == IncidentStatus.ACKNOWLEDGED).count();
        long inProgCount = allIncidents.stream().filter(i -> i.getStatus() == IncidentStatus.IN_PROGRESS).count();
        long resolvedCount = allIncidents.stream().filter(i -> i.getStatus() == IncidentStatus.RESOLVED).count();

        long criticalCount = allIncidents.stream().filter(i -> i.getSeverity() == IncidentSeverity.CRITICAL && i.getStatus() != IncidentStatus.RESOLVED).count();
        long highCount = allIncidents.stream().filter(i -> i.getSeverity() == IncidentSeverity.HIGH && i.getStatus() != IncidentStatus.RESOLVED).count();

        summary.setOpenIncidents(openCount);
        summary.setAcknowledgedIncidents(ackCount);
        summary.setInProgressIncidents(inProgCount);
        summary.setResolvedIncidents(resolvedCount);
        summary.setCriticalIncidents(criticalCount);
        summary.setHighIncidents(highCount);

        // Average Resolution Time (MTTR - Mean Time To Resolution)
        double avgResolutionMinutes = allIncidents.stream()
                .filter(i -> i.getStatus() == IncidentStatus.RESOLVED && i.getResolvedAt() != null)
                .mapToLong(i -> Duration.between(i.getCreatedAt(), i.getResolvedAt()).toMinutes())
                .average()
                .orElse(0.0);
        summary.setAverageResolutionTimeMinutes(Math.round(avgResolutionMinutes * 10.0) / 10.0);

        // Calculate Network Health Index (%)
        double health = 100.0;
        if (totalTowers > 0) {
            double downPenalty = ((double) downTowers / totalTowers) * 60.0;
            double degradedPenalty = ((double) degradedTowers / totalTowers) * 25.0;
            double activeCriticalPenalty = Math.min(criticalCount * 5.0, 15.0);
            health = Math.max(0.0, 100.0 - downPenalty - degradedPenalty - activeCriticalPenalty);
        }
        summary.setNetworkHealthIndex(Math.round(health * 10.0) / 10.0);

        // Group incidents by region
        Map<String, Long> regionalMap = new HashMap<>();
        for (Incident inc : allIncidents) {
            regionalMap.put(inc.getRegion(), regionalMap.getOrDefault(inc.getRegion(), 0L) + 1L);
        }
        summary.setIncidentsByRegion(regionalMap);

        return summary;
    }
}
