package com.krishibridge.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class VehicleUpdateRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long id;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Capacity in tons is required")
    @Min(value = 0, message = "Capacity must be greater than 0")
    private Double capacityTons;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    private String currentCity;

    public VehicleUpdateRequest() {}

    public VehicleUpdateRequest(Long id, String vehicleType, Double capacityTons, Double latitude, Double longitude, String currentCity) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.capacityTons = capacityTons;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentCity = currentCity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
