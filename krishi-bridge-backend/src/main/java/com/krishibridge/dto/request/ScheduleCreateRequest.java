package com.krishibridge.dto.request;

import com.krishibridge.enums.ScheduleStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ScheduleCreateRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Schedule status is required")
    private ScheduleStatus status;

    public ScheduleCreateRequest() {}

    public ScheduleCreateRequest(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, ScheduleStatus status) {
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }
}
