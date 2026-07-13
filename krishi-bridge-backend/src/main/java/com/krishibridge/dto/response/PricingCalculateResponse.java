package com.krishibridge.dto.response;

public class PricingCalculateResponse {
    private String vehicleType;
    private Double distanceKm;
    private Double weightKg;
    private Double estimatedPrice;

    public PricingCalculateResponse() {}

    public PricingCalculateResponse(String vehicleType, Double distanceKm, Double weightKg, Double estimatedPrice) {
        this.vehicleType = vehicleType;
        this.distanceKm = distanceKm;
        this.weightKg = weightKg;
        this.estimatedPrice = estimatedPrice;
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

    public Double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }
}
