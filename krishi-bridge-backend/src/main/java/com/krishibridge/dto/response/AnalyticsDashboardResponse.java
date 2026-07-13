package com.krishibridge.dto.response;

public class AnalyticsDashboardResponse {
    private Long totalBookingsCreated;
    private Long totalBookingsCancelled;
    private Long totalDeliveriesCompleted;
    private Long totalTransportersApproved;
    private Long activeUsersCount;
    private Double cancellationRate;

    public AnalyticsDashboardResponse() {}

    public AnalyticsDashboardResponse(Long totalBookingsCreated, Long totalBookingsCancelled, Long totalDeliveriesCompleted, Long totalTransportersApproved, Long activeUsersCount, Double cancellationRate) {
        this.totalBookingsCreated = totalBookingsCreated;
        this.totalBookingsCancelled = totalBookingsCancelled;
        this.totalDeliveriesCompleted = totalDeliveriesCompleted;
        this.totalTransportersApproved = totalTransportersApproved;
        this.activeUsersCount = activeUsersCount;
        this.cancellationRate = cancellationRate;
    }

    public Long getTotalBookingsCreated() {
        return totalBookingsCreated;
    }

    public void setTotalBookingsCreated(Long totalBookingsCreated) {
        this.totalBookingsCreated = totalBookingsCreated;
    }

    public Long getTotalBookingsCancelled() {
        return totalBookingsCancelled;
    }

    public void setTotalBookingsCancelled(Long totalBookingsCancelled) {
        this.totalBookingsCancelled = totalBookingsCancelled;
    }

    public Long getTotalDeliveriesCompleted() {
        return totalDeliveriesCompleted;
    }

    public void setTotalDeliveriesCompleted(Long totalDeliveriesCompleted) {
        this.totalDeliveriesCompleted = totalDeliveriesCompleted;
    }

    public Long getTotalTransportersApproved() {
        return totalTransportersApproved;
    }

    public void setTotalTransportersApproved(Long totalTransportersApproved) {
        this.totalTransportersApproved = totalTransportersApproved;
    }

    public Long getActiveUsersCount() {
        return activeUsersCount;
    }

    public void setActiveUsersCount(Long activeUsersCount) {
        this.activeUsersCount = activeUsersCount;
    }

    public Double getCancellationRate() {
        return cancellationRate;
    }

    public void setCancellationRate(Double cancellationRate) {
        this.cancellationRate = cancellationRate;
    }
}
