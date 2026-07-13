package com.krishibridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PricingCalculateRequest {

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Distance in kilometers is required")
    @Min(value = 0, message = "Distance must be greater than 0")
    private Double distanceKm;

    @NotNull(message = "Weight in kilograms is required")
    @Min(value = 0, message = "Weight must be greater than 0")
    private Double weightKg;

    public PricingCalculateRequest() {}

    public PricingCalculateRequest(String vehicleType, Double distanceKm, Double weightKg) {
        this.vehicleType = vehicleType;
        this.distanceKm = distanceKm;
        this.weightKg = weightKg;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
}
