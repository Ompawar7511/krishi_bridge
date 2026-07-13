package com.krishibridge.dto.request;

import com.krishibridge.enums.ScheduleStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ScheduleUpdateRequest {

    @NotNull(message = "Schedule ID is required")
    private Long id;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Schedule status is required")
    private ScheduleStatus status;

    public ScheduleUpdateRequest() {}

    public ScheduleUpdateRequest(Long id, LocalDateTime startTime, LocalDateTime endTime, ScheduleStatus status) {
        this.id = id;
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
