package com.telecom.monitor.dto;

import com.telecom.monitor.model.TowerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TowerDto {

    @NotBlank(message = "Tower ID is required")
    private String id;

    @NotBlank(message = "Tower name is required")
    private String name;

    @NotBlank(message = "Region is required")
    private String region;

    @NotBlank(message = "Technology is required (e.g., 4G, 5G)")
    private String technology;

    @NotNull(message = "Status is required")
    private TowerStatus status;

    private Double latitude;
    private Double longitude;
    private Integer capacityBandwidthMbps;
    private Integer activeUsersBaseline;

    public TowerDto() {
    }

    public TowerDto(String id, String name, String region, String technology, TowerStatus status,
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
    }

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
}
