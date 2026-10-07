package com.telecom.monitor.repository;

import com.telecom.monitor.model.Tower;
import com.telecom.monitor.model.TowerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TowerRepository extends JpaRepository<Tower, String> {
    List<Tower> findByRegionIgnoreCase(String region);
    List<Tower> findByStatus(TowerStatus status);
    List<Tower> findByTechnologyIgnoreCase(String technology);
}
