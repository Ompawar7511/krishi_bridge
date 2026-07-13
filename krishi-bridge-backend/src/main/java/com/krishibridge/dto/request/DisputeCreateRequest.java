package com.krishibridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DisputeCreateRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    private String reason;

    @NotBlank(message = "Description details are required")
    private String description;

    public DisputeCreateRequest() {}

    public DisputeCreateRequest(Long bookingId, String reason, String description) {
        this.bookingId = bookingId;
        this.reason = reason;
        this.description = description;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
