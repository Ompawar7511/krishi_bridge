package com.krishibridge.controller;

import com.krishibridge.dto.request.ScheduleCreateRequest;
import com.krishibridge.dto.request.ScheduleUpdateRequest;
import com.krishibridge.dto.request.VehicleAddRequest;
import com.krishibridge.dto.request.VehicleUpdateRequest;
import com.krishibridge.dto.response.ScheduleResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.dto.response.VehicleResponse;
import com.krishibridge.entity.Vehicle;
import com.krishibridge.entity.VehicleSchedule;
import com.krishibridge.service.VehicleScheduleService;
import com.krishibridge.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/vehicle")
@PreAuthorize("hasRole('TRANSPORTER')")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleScheduleService scheduleService;

    public VehicleController(VehicleService vehicleService, VehicleScheduleService scheduleService) {
        this.vehicleService = vehicleService;
        this.scheduleService = scheduleService;
    }

    @PostMapping("/add")
    public ResponseEntity<StandardResponse<VehicleResponse>> addVehicle(@Valid @RequestBody VehicleAddRequest request) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Vehicle vehicle = vehicleService.addVehicle(transporterId, request);
        VehicleResponse responseDto = mapToVehicleDto(vehicle);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Vehicle registered successfully"));
    }

    @PutMapping("/update")
    public ResponseEntity<StandardResponse<VehicleResponse>> updateVehicle(@Valid @RequestBody VehicleUpdateRequest request) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Vehicle vehicle = vehicleService.updateVehicle(transporterId, request);
        VehicleResponse responseDto = mapToVehicleDto(vehicle);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Vehicle profile updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<StandardResponse<String>> deleteVehicle(@PathVariable("id") Long id) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        vehicleService.deleteVehicle(transporterId, id);
        return ResponseEntity.ok(StandardResponse.success("Vehicle deleted successfully", "Delete complete"));
    }

    @GetMapping("/my-fleet")
    public ResponseEntity<StandardResponse<List<VehicleResponse>>> getMyFleet() {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Vehicle> fleet = vehicleService.getTransporterVehicles(transporterId);
        List<VehicleResponse> responseList = fleet.stream()
                .map(this::mapToVehicleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    @PostMapping("/schedule/create")
    public ResponseEntity<StandardResponse<ScheduleResponse>> createScheduleBlock(@Valid @RequestBody ScheduleCreateRequest request) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        VehicleSchedule schedule = scheduleService.createScheduleBlock(transporterId, request);
        ScheduleResponse responseDto = mapToScheduleDto(schedule);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Schedule block reserved successfully"));
    }

    @PutMapping("/schedule/update")
    public ResponseEntity<StandardResponse<ScheduleResponse>> updateScheduleBlock(@Valid @RequestBody ScheduleUpdateRequest request) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        VehicleSchedule schedule = scheduleService.updateScheduleBlock(transporterId, request);
        ScheduleResponse responseDto = mapToScheduleDto(schedule);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Schedule block updated successfully"));
    }

    @GetMapping("/{id}/schedules")
    public ResponseEntity<StandardResponse<List<ScheduleResponse>>> getVehicleSchedules(@PathVariable("id") Long id) {
        List<VehicleSchedule> schedules = scheduleService.getVehicleSchedules(id);
        List<ScheduleResponse> responseList = schedules.stream()
                .map(this::mapToScheduleDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
    }

    private VehicleResponse mapToVehicleDto(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getTransporterId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getCapacityTons(),
                vehicle.getLatitude(),
                vehicle.getLongitude(),
                vehicle.getCurrentCity(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }

    private ScheduleResponse mapToScheduleDto(VehicleSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getVehicleId(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getStatus()
        );
    }
}
