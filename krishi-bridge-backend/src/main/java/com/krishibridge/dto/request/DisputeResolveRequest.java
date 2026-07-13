package com.krishibridge.dto.request;

import com.krishibridge.enums.DisputeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DisputeResolveRequest {

    @NotNull(message = "Resolution status is required")
    private DisputeStatus status; // Should be RESOLVED or DISMISSED

    @NotBlank(message = "Admin resolution notes are required")
    private String adminNotes;

    public DisputeResolveRequest() {}

    public DisputeResolveRequest(DisputeStatus status, String adminNotes) {
        this.status = status;
        this.adminNotes = adminNotes;
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
}
