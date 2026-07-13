package com.krishibridge.dto.response;

import java.time.LocalDateTime;

public class VehicleResponse {
    private Long id;
    private Long transporterId;
    private String vehicleNumber;
    private String vehicleType;
    private Double capacityTons;
    private Double latitude;
    private Double longitude;
    private String currentCity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VehicleResponse() {}

    public VehicleResponse(Long id, Long transporterId, String vehicleNumber, String vehicleType, Double capacityTons, Double latitude, Double longitude, String currentCity, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.transporterId = transporterId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.capacityTons = capacityTons;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentCity = currentCity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(Long transporterId) {
        this.transporterId = transporterId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getCapacityTons() {
        return capacityTons;
    }

    public void setCapacityTons(Double capacityTons) {
        this.capacityTons = capacityTons;
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

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
