package com.telecom.monitor.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "towers")
public class Tower {

    @Id
    @Column(name = "id", length = 32, nullable = false)
    private String id; // e.g. "HYD-4521"

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "region", nullable = false, length = 64)
    private String region; // e.g. "Hyderabad", "Bengaluru", "Mumbai", "Delhi"

    @Column(name = "technology", nullable = false, length = 16)
    private String technology; // "5G", "4G"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TowerStatus status;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "capacity_bandwidth_mbps")
    private Integer capacityBandwidthMbps;

    @Column(name = "active_users_baseline")
    private Integer activeUsersBaseline;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Tower() {
    }

    public Tower(String id, String name, String region, String technology, TowerStatus status,
                 Double latitude, Double longitude, Integer capacityBandwidthMbps, Integer activeUsersBaseline) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.technology = technology;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.capacityBandwidthMbps = capacityBandwidthMbps;
        this.activeUsersBaseline = activeUsersBaseline;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public TowerStatus getStatus() {
        return status;
    }

    public void setStatus(TowerStatus status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getCapacityBandwidthMbps() {
        return capacityBandwidthMbps;
    }

    public void setCapacityBandwidthMbps(Integer capacityBandwidthMbps) {
        this.capacityBandwidthMbps = capacityBandwidthMbps;
    }

    public Integer getActiveUsersBaseline() {
        return activeUsersBaseline;
    }

    public void setActiveUsersBaseline(Integer activeUsersBaseline) {
        this.activeUsersBaseline = activeUsersBaseline;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
