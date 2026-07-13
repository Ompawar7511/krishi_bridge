package com.krishibridge.repository;

import com.krishibridge.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleMatchingRepository extends JpaRepository<Vehicle, Long> {

    @Query(value = "SELECT v.* FROM vehicles v " +
           "JOIN users u ON v.transporter_id = u.id " +
           "WHERE u.role = 'TRANSPORTER' AND u.status = 'APPROVED' " +
           "AND v.capacity_tons >= :weight " +
           "AND ST_Distance_Sphere(point(v.longitude, v.latitude), point(:pickupLon, :pickupLat)) <= 20000 " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM vehicle_schedule vs " +
           "  WHERE vs.vehicle_id = v.id " +
           "  AND vs.status IN ('BOOKED', 'MAINTENANCE', 'BLOCKED') " +
           "  AND :startTime < vs.end_time AND :endTime > vs.start_time" +
           ")", nativeQuery = true)
    List<Vehicle> findMatchingVehicles(@Param("pickupLon") Double pickupLon, 
                                       @Param("pickupLat") Double pickupLat, 
                                       @Param("weight") Double weight, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
}
