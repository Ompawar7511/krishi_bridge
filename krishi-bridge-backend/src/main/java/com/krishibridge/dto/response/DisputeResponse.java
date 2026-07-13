package com.krishibridge.dto.response;

import com.krishibridge.enums.DisputeStatus;
import java.time.LocalDateTime;

public class DisputeResponse {
    private Long id;
    private Long bookingId;
    private Long raisedBy;
    private String reason;
    private String description;
    private DisputeStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public DisputeResponse() {}

    public DisputeResponse(Long id, Long bookingId, Long raisedBy, String reason, String description, DisputeStatus status, String adminNotes, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.raisedBy = raisedBy;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.adminNotes = adminNotes;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getRaisedBy() {
        return raisedBy;
    }

    public void setRaisedBy(Long raisedBy) {
        this.raisedBy = raisedBy;
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

    public DisputeStatus getStatus() {
        return status;
    }

    public void setStatus(DisputeStatus status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
