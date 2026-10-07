package com.telecom.monitor.controller;

import com.telecom.monitor.dto.TowerDto;
import com.telecom.monitor.model.Tower;
import com.telecom.monitor.model.TowerStatus;
import com.telecom.monitor.service.TowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/towers")
@CrossOrigin(origins = "*")
@Tag(name = "Towers", description = "Cell Tower Infrastructure Management APIs")
public class TowerController {

    private final TowerService towerService;

    public TowerController(TowerService towerService) {
        this.towerService = towerService;
    }

    @GetMapping
    @Operation(summary = "Get all cell towers or filter by region")
    public ResponseEntity<List<Tower>> getAllTowers(@RequestParam(required = false) String region) {
        if (region != null && !region.isBlank()) {
            return ResponseEntity.ok(towerService.getTowersByRegion(region));
        }
        return ResponseEntity.ok(towerService.getAllTowers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tower details by unique ID")
    public ResponseEntity<Tower> getTowerById(@PathVariable String id) {
        return ResponseEntity.ok(towerService.getTowerById(id));
    }

    @PostMapping
    @Operation(summary = "Register a new cell tower")
    public ResponseEntity<Tower> createTower(@Valid @RequestBody TowerDto dto) {
        Tower created = towerService.createTower(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update operational status of a tower")
    public ResponseEntity<Tower> updateStatus(@PathVariable String id, @RequestParam TowerStatus status) {
        Tower updated = towerService.updateTowerStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
