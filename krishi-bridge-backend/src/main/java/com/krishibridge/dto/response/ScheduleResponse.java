package com.krishibridge.dto.response;

import com.krishibridge.enums.ScheduleStatus;
import java.time.LocalDateTime;

public class ScheduleResponse {
    private Long id;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ScheduleStatus status;

    public ScheduleResponse() {}

    public ScheduleResponse(Long id, Long vehicleId, LocalDateTime startTime, LocalDateTime endTime, ScheduleStatus status) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
