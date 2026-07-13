package com.krishibridge.service;

import com.krishibridge.dto.request.ScheduleCreateRequest;
import com.krishibridge.dto.request.ScheduleUpdateRequest;
import com.krishibridge.entity.Vehicle;
import com.krishibridge.entity.VehicleSchedule;
import com.krishibridge.enums.ScheduleStatus;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.exception.ScheduleOverlapException;
import com.krishibridge.exception.VehicleNotFoundException;
import com.krishibridge.repository.VehicleRepository;
import com.krishibridge.repository.VehicleScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleScheduleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleScheduleService.class);

    private final VehicleScheduleRepository scheduleRepository;
    private final VehicleRepository vehicleRepository;

    public VehicleScheduleService(VehicleScheduleRepository scheduleRepository, VehicleRepository vehicleRepository) {
        this.scheduleRepository = scheduleRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public VehicleSchedule createScheduleBlock(Long transporterId, ScheduleCreateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found"));

        if (!vehicle.getTransporterId().equals(transporterId)) {
            throw new ComplianceException("Access denied. You do not own this vehicle profile.");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().isEqual(request.getEndTime())) {
            throw new ComplianceException("Start time must be strictly before end time");
        }

        // Overlapping checks (only block schedules that are NOT AVAILABLE)
        if (request.getStatus() != ScheduleStatus.AVAILABLE) {
            boolean overlaps = scheduleRepository.existsOverlappingSchedule(
                    request.getVehicleId(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            if (overlaps) {
                log.warn("SCHEDULE_CONFLICT | Double booking attempt | VehicleId: {} | Proposed: {} to {}", request.getVehicleId(), request.getStartTime(), request.getEndTime());
                throw new ScheduleOverlapException("Vehicle schedule collision. An overlapping schedule block already exists during this period.");
            }
        }

        VehicleSchedule schedule = new VehicleSchedule(
                request.getVehicleId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getStatus()
        );

        VehicleSchedule savedSchedule = scheduleRepository.save(schedule);
        log.info("AUDIT_LOG | SCHEDULE_CREATED | VehicleId: {} | ScheduleId: {} | Status: {}", request.getVehicleId(), savedSchedule.getId(), savedSchedule.getStatus());
        return savedSchedule;
    }

    @Transactional
    public VehicleSchedule updateScheduleBlock(Long transporterId, ScheduleUpdateRequest request) {
        VehicleSchedule schedule = scheduleRepository.findById(request.getId())
                .orElseThrow(() -> new ComplianceException("Schedule block not found"));

        Vehicle vehicle = vehicleRepository.findById(schedule.getVehicleId())
                .orElseThrow(() -> new VehicleNotFoundException("Associated vehicle not found"));

        if (!vehicle.getTransporterId().equals(transporterId)) {
            throw new ComplianceException("Access denied. You do not own this vehicle profile.");
        }

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().isEqual(request.getEndTime())) {
            throw new ComplianceException("Start time must be strictly before end time");
        }

        // Overlapping checks (excluding current block)
        if (request.getStatus() != ScheduleStatus.AVAILABLE) {
            boolean overlaps = scheduleRepository.existsOverlappingScheduleExclude(
                    schedule.getVehicleId(),
                    request.getStartTime(),
                    request.getEndTime(),
                    schedule.getId()
            );

            if (overlaps) {
                log.warn("SCHEDULE_CONFLICT | Double booking attempt | VehicleId: {} | ScheduleId: {} | Proposed: {} to {}", schedule.getVehicleId(), request.getId(), request.getStartTime(), request.getEndTime());
                throw new ScheduleOverlapException("Vehicle schedule collision. An overlapping schedule block already exists during this period.");
            }
        }

        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setStatus(request.getStatus());

        VehicleSchedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("AUDIT_LOG | SCHEDULE_UPDATED | VehicleId: {} | ScheduleId: {} | Status: {}", schedule.getVehicleId(), schedule.getId(), schedule.getStatus());
        return updatedSchedule;
    }

    @Transactional(readOnly = true)
    public List<VehicleSchedule> getVehicleSchedules(Long vehicleId) {
        return scheduleRepository.findByVehicleId(vehicleId);
    }
}
