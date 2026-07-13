package com.krishibridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pricing_rules")
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_type", nullable = false, unique = true, length = 50)
    private String vehicleType;

    @Column(name = "base_rate_per_km", nullable = false)
    private Double baseRatePerKm;

    @Column(name = "price_per_kg", nullable = false)
    private Double pricePerKg;

    @Column(name = "minimum_charge", nullable = false)
    private Double minimumCharge;

    public PricingRule() {}

    public PricingRule(String vehicleType, Double baseRatePerKm, Double pricePerKg, Double minimumCharge) {
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
