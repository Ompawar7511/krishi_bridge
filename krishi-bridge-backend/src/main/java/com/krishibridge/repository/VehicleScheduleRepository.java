package com.krishibridge.repository;

import com.krishibridge.entity.VehicleSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleScheduleRepository extends JpaRepository<VehicleSchedule, Long> {
    List<VehicleSchedule> findByVehicleId(Long vehicleId);

    @Query("SELECT COUNT(vs) > 0 FROM VehicleSchedule vs WHERE vs.vehicleId = :vehicleId " +
           "AND vs.status <> 'AVAILABLE' " +
           "AND :startTime < vs.endTime AND :endTime > vs.startTime")
    boolean existsOverlappingSchedule(@Param("vehicleId") Long vehicleId, 
                                      @Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(vs) > 0 FROM VehicleSchedule vs WHERE vs.vehicleId = :vehicleId " +
           "AND vs.status <> 'AVAILABLE' AND vs.id <> :excludeId " +
           "AND :startTime < vs.endTime AND :endTime > vs.startTime")
    boolean existsOverlappingScheduleExclude(@Param("vehicleId") Long vehicleId, 
                                             @Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime, 
                                             @Param("excludeId") Long excludeId);
}
