package com.krishibridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cancellation_rules")
public class CancellationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_stage", nullable = false, unique = true, length = 50)
    private String bookingStage;

    @Column(name = "penalty_percentage", nullable = false)
    private Double penaltyPercentage;

    public CancellationRule() {}

    public CancellationRule(String bookingStage, Double penaltyPercentage) {
        this.bookingStage = bookingStage;
        this.penaltyPercentage = penaltyPercentage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingStage() {
        return bookingStage;
    }

    public void setBookingStage(String bookingStage) {
        this.bookingStage = bookingStage;
    }

    public Double getPenaltyPercentage() {
        return penaltyPercentage;
    }

    public void setPenaltyPercentage(Double penaltyPercentage) {
        this.penaltyPercentage = penaltyPercentage;
    }
}
