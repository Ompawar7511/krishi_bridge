package com.krishibridge.repository;

import com.krishibridge.entity.CancellationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CancellationRuleRepository extends JpaRepository<CancellationRule, Long> {
    Optional<CancellationRule> findByBookingStage(String bookingStage);
}
