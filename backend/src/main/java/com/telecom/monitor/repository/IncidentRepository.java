package com.telecom.monitor.repository;

import com.telecom.monitor.model.Incident;
import com.telecom.monitor.model.IncidentSeverity;
import com.telecom.monitor.model.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {
    List<Incident> findByStatus(IncidentStatus status);
    List<Incident> findBySeverity(IncidentSeverity severity);
    List<Incident> findByRegionIgnoreCase(String region);
    List<Incident> findByTowerId(String towerId);

    // Find any existing active (non-RESOLVED) incident for a given tower (for deduplication)
    Optional<Incident> findFirstByTowerIdAndStatusNotOrderByCreatedAtDesc(String towerId, IncidentStatus status);

    long countByStatus(IncidentStatus status);
    long countBySeverity(IncidentSeverity severity);

    @Query("SELECT i.region, COUNT(i) FROM Incident i GROUP BY i.region")
    List<Object[]> countIncidentsByRegion();

    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countIncidentsBySeverityGroup();
}
