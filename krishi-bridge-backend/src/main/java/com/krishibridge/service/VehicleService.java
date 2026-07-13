package com.krishibridge.service;

import com.krishibridge.dto.request.VehicleAddRequest;
import com.krishibridge.dto.request.VehicleUpdateRequest;
import com.krishibridge.entity.User;
import com.krishibridge.entity.Vehicle;
import com.krishibridge.enums.Role;
import com.krishibridge.enums.UserStatus;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.exception.VehicleNotFoundException;
import com.krishibridge.repository.UserRepository;
import com.krishibridge.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Vehicle addVehicle(Long transporterId, VehicleAddRequest request) {
        // Enforce approved transporter constraint
        User transporter = userRepository.findById(transporterId)
                .orElseThrow(() -> new ComplianceException("Transporter user not found"));

        if (transporter.getRole() != Role.TRANSPORTER || transporter.getStatus() != UserStatus.APPROVED) {
            log.warn("SECURITY_VIOLATION | ADD_VEHICLE_BLOCKED | TransporterId: {} | Status: {}", transporterId, transporter.getStatus());
            throw new ComplianceException("Only approved transporters with verified compliance documentation can register vehicles");
        }

        if (vehicleRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new ComplianceException("Vehicle with number " + request.getVehicleNumber() + " is already registered in the marketplace");
        }

        Vehicle vehicle = new Vehicle(
                transporterId,
                request.getVehicleNumber(),
                request.getVehicleType(),
                request.getCapacityTons(),
                request.getLatitude(),
                request.getLongitude(),
                request.getCurrentCity()
        );

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("AUDIT_LOG | VEHICLE_ADDED | TransporterId: {} | VehicleId: {} | Plate: {}", transporterId, savedVehicle.getId(), savedVehicle.getVehicleNumber());
        return savedVehicle;
    }

    @Transactional
    public Vehicle updateVehicle(Long transporterId, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getId())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID " + request.getId() + " not found"));

        if (!vehicle.getTransporterId().equals(transporterId)) {
            log.warn("SECURITY_VIOLATION | UPDATE_VEHICLE_FORBIDDEN | ActorId: {} | OwnerId: {}", transporterId, vehicle.getTransporterId());
            throw new ComplianceException("Access denied. You do not own this vehicle profile.");
        }

        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setCapacityTons(request.getCapacityTons());
        vehicle.setLatitude(request.getLatitude());
        vehicle.setLongitude(request.getLongitude());
        vehicle.setCurrentCity(request.getCurrentCity());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        log.info("AUDIT_LOG | VEHICLE_UPDATED | TransporterId: {} | VehicleId: {}", transporterId, vehicle.getId());
        return updatedVehicle;
    }

    @Transactional
    public void deleteVehicle(Long transporterId, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle with ID " + vehicleId + " not found"));

        if (!vehicle.getTransporterId().equals(transporterId)) {
            log.warn("SECURITY_VIOLATION | DELETE_VEHICLE_FORBIDDEN | ActorId: {} | OwnerId: {}", transporterId, vehicle.getTransporterId());
            throw new ComplianceException("Access denied. You do not own this vehicle profile.");
        }

        vehicleRepository.delete(vehicle);
        log.info("AUDIT_LOG | VEHICLE_DELETED | TransporterId: {} | VehicleId: {}", transporterId, vehicleId);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getTransporterVehicles(Long transporterId) {
        return vehicleRepository.findByTransporterId(transporterId);
    }
}
