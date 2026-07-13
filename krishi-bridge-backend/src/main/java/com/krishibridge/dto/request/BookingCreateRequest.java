package com.krishibridge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookingCreateRequest {

    @NotBlank(message = "Crop type is required")
    private String cropType;

    @NotNull(message = "Weight in tons is required")
    @Min(value = 0, message = "Weight must be greater than 0")
    private Double weightTons;

    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;

    @NotNull(message = "Destination latitude is required")
    private Double destinationLatitude;

    @NotNull(message = "Destination longitude is required")
    private Double destinationLongitude;

    @NotNull(message = "Estimated distance in km is required")
    @Min(value = 0, message = "Distance must be greater than 0")
    private Double estimatedDistanceKm;

    @NotBlank(message = "Vehicle type is required for quote estimation")
    private String vehicleType;

    @NotBlank(message = "Idempotency key is required")
    @Size(min = 10, max = 100, message = "Idempotency key must be between 10 and 100 characters")
    private String idempotencyKey;

    public BookingCreateRequest() {}

    public BookingCreateRequest(String cropType, Double weightTons, Double pickupLatitude, Double pickupLongitude, Double destinationLatitude, Double destinationLongitude, Double estimatedDistanceKm, String vehicleType, String idempotencyKey) {
        this.cropType = cropType;
        this.weightTons = weightTons;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.vehicleType = vehicleType;
        this.idempotencyKey = idempotencyKey;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public Double getWeightTons() {
        return weightTons;
    }

    public void setWeightTons(Double weightTons) {
        this.weightTons = weightTons;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(Double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(Double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public Double getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public void setEstimatedDistanceKm(Double estimatedDistanceKm) {
        this.estimatedDistanceKm = estimatedDistanceKm;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
