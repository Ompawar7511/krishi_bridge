package com.krishibridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PricingRuleRequest {

    private Long id; // Optional, required only for update

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotNull(message = "Base rate per km is required")
    @Min(value = 0, message = "Base rate per km must be greater than 0")
    private Double baseRatePerKm;

    @NotNull(message = "Price per kg is required")
    @Min(value = 0, message = "Price per kg must be greater than 0")
    private Double pricePerKg;

    @NotNull(message = "Minimum charge is required")
    @Min(value = 0, message = "Minimum charge must be greater than 0")
    private Double minimumCharge;

    public PricingRuleRequest() {}

    public PricingRuleRequest(Long id, String vehicleType, Double baseRatePerKm, Double pricePerKg, Double minimumCharge) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.baseRatePerKm = baseRatePerKm;
        this.pricePerKg = pricePerKg;
        this.minimumCharge = minimumCharge;
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

    public Double getBaseRatePerKm() {
        return baseRatePerKm;
    }

    public void setBaseRatePerKm(Double baseRatePerKm) {
        this.baseRatePerKm = baseRatePerKm;
    }

    public Double getPricePerKg() {
        return pricePerKg;
    }

    public void setPricePerKg(Double pricePerKg) {
        this.pricePerKg = pricePerKg;
    }

    public Double getMinimumCharge() {
        return minimumCharge;
    }

    public void setMinimumCharge(Double minimumCharge) {
        this.minimumCharge = minimumCharge;
    }
}
