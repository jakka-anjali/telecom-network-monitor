package com.telecom.monitor.repository;

import com.telecom.monitor.model.NetworkEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkEventRepository extends JpaRepository<NetworkEvent, Long> {
    List<NetworkEvent> findByTowerIdOrderByTimestampDesc(String towerId, Pageable pageable);
    List<NetworkEvent> findTop50ByOrderByTimestampDesc();
    long countByLatencyMsGreaterThan(Double latencyThreshold);
}
