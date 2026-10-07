package com.telecom.monitor.repository;

import com.telecom.monitor.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByIncidentIdOrderBySentAtDesc(String incidentId);
    List<Alert> findTop20ByOrderBySentAtDesc();
}
