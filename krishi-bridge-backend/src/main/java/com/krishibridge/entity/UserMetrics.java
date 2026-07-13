package com.krishibridge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_metrics")
public class UserMetrics {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "successful_deliveries", nullable = false)
    private Integer successfulDeliveries;

    @Column(name = "cancelled_bookings", nullable = false)
    private Integer cancelledBookings;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UserMetrics() {}

    public UserMetrics(Long userId, Integer successfulDeliveries, Integer cancelledBookings, Double averageRating) {
        this.userId = userId;
        this.successfulDeliveries = successfulDeliveries;
        this.cancelledBookings = cancelledBookings;
        this.averageRating = averageRating;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getSuccessfulDeliveries() {
        return successfulDeliveries;
    }

    public void setSuccessfulDeliveries(Integer successfulDeliveries) {
        this.successfulDeliveries = successfulDeliveries;
    }

    public Integer getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(Integer cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
