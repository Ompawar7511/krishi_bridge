package com.krishibridge.repository;

import com.krishibridge.entity.Dispute;
import com.krishibridge.enums.DisputeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findByStatus(DisputeStatus status);
    List<Dispute> findByBookingId(Long bookingId);
}
