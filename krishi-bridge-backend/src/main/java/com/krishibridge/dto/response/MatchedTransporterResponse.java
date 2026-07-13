package com.krishibridge.dto.response;

public class MatchedTransporterResponse {
    private Long transporterId;
    private String transporterName;
    private Long vehicleId;
    private String vehicleNumber;
    private String vehicleType;
    private Double capacityTons;
    private Double distanceKm;

    public MatchedTransporterResponse() {}

    public MatchedTransporterResponse(Long transporterId, String transporterName, Long vehicleId, String vehicleNumber, String vehicleType, Double capacityTons, Double distanceKm) {
        this.transporterId = transporterId;
        this.transporterName = transporterName;
        this.vehicleId = vehicleId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.capacityTons = capacityTons;
        this.distanceKm = distanceKm;
    }

    public Long getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(Long transporterId) {
        this.transporterId = transporterId;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
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

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
