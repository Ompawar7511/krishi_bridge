package com.krishibridge.repository;

import com.krishibridge.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findByVehicleType(String vehicleType);
    boolean existsByVehicleType(String vehicleType);
}
