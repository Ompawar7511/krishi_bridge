package com.krishibridge.dto.response;

import com.krishibridge.enums.BookingStatus;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private Long farmerId;
    private String cropType;
    private Double weightTons;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private Double estimatedDistanceKm;
    private Double estimatedPrice;
    private String idempotencyKey;
    private BookingStatus status;
    private Long assignedTransporterId;
    private Long assignedVehicleId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BookingResponse() {}

    public BookingResponse(Long id, Long farmerId, String cropType, Double weightTons, Double pickupLatitude, Double pickupLongitude, Double destinationLatitude, Double destinationLongitude, Double estimatedDistanceKm, Double estimatedPrice, String idempotencyKey, BookingStatus status, Long assignedTransporterId, Long assignedVehicleId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.farmerId = farmerId;
        this.cropType = cropType;
        this.weightTons = weightTons;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.estimatedDistanceKm = estimatedDistanceKm;
        this.estimatedPrice = estimatedPrice;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.assignedTransporterId = assignedTransporterId;
        this.assignedVehicleId = assignedVehicleId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
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

    public Double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Long getAssignedTransporterId() {
        return assignedTransporterId;
    }

    public void setAssignedTransporterId(Long assignedTransporterId) {
        this.assignedTransporterId = assignedTransporterId;
    }

    public Long getAssignedVehicleId() {
        return assignedVehicleId;
    }

    public void setAssignedVehicleId(Long assignedVehicleId) {
        this.assignedVehicleId = assignedVehicleId;
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
