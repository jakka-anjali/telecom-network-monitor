package com.telecom.monitor.service;

import com.telecom.monitor.dto.TowerDto;
import com.telecom.monitor.exception.ResourceNotFoundException;
import com.telecom.monitor.model.Tower;
import com.telecom.monitor.model.TowerStatus;
import com.telecom.monitor.repository.TowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TowerService {

    private final TowerRepository towerRepository;

    public TowerService(TowerRepository towerRepository) {
        this.towerRepository = towerRepository;
    }

    public List<Tower> getAllTowers() {
        return towerRepository.findAll();
    }

    public Tower getTowerById(String id) {
        return towerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telecom Tower not found with ID: " + id));
    }

    public List<Tower> getTowersByRegion(String region) {
        return towerRepository.findByRegionIgnoreCase(region);
    }

    @Transactional
    public Tower createTower(TowerDto dto) {
        Tower tower = new Tower(
                dto.getId(),
                dto.getName(),
                dto.getRegion(),
                dto.getTechnology(),
                dto.getStatus() != null ? dto.getStatus() : TowerStatus.ACTIVE,
                dto.getLatitude(),
                dto.getLongitude(),
                dto.getCapacityBandwidthMbps(),
                dto.getActiveUsersBaseline()
        );
        return towerRepository.save(tower);
    }

    @Transactional
    public Tower updateTowerStatus(String id, TowerStatus status) {
        Tower tower = getTowerById(id);
        tower.setStatus(status);
        return towerRepository.save(tower);
    }
}
