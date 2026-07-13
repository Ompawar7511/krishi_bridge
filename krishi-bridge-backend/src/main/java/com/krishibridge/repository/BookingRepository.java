package com.krishibridge.repository;

import com.krishibridge.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByFarmerIdOrderByCreatedAtDesc(Long farmerId);
    List<Booking> findByAssignedTransporterIdOrderByCreatedAtDesc(Long assignedTransporterId);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
