package com.krishibridge.service;

import com.krishibridge.dto.request.PricingCalculateRequest;
import com.krishibridge.dto.request.PricingRuleRequest;
import com.krishibridge.entity.PricingRule;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.exception.PricingRuleNotFoundException;
import com.krishibridge.repository.PricingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final PricingRuleRepository pricingRuleRepository;

    public PricingService(PricingRuleRepository pricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
    }

    @Transactional
    public PricingRule createPricingRule(PricingRuleRequest request) {
        if (pricingRuleRepository.existsByVehicleType(request.getVehicleType())) {
            throw new ComplianceException("Pricing rule for vehicle type " + request.getVehicleType() + " already exists");
        }

        PricingRule pricingRule = new PricingRule(
                request.getVehicleType(),
                request.getBaseRatePerKm(),
                request.getPricePerKg(),
                request.getMinimumCharge()
        );

        PricingRule savedRule = pricingRuleRepository.save(pricingRule);
        log.info("AUDIT_LOG | PRICING_RULE_CREATED | VehicleType: {} | BaseRate: {}", request.getVehicleType(), request.getBaseRatePerKm());
        return savedRule;
    }

    @Transactional
    public PricingRule updatePricingRule(PricingRuleRequest request) {
        PricingRule pricingRule = pricingRuleRepository.findByVehicleType(request.getVehicleType())
                .orElseThrow(() -> new PricingRuleNotFoundException("Pricing rule for vehicle type " + request.getVehicleType() + " not found"));

        pricingRule.setBaseRatePerKm(request.getBaseRatePerKm());
        pricingRule.setPricePerKg(request.getPricePerKg());
        pricingRule.setMinimumCharge(request.getMinimumCharge());

        PricingRule updatedRule = pricingRuleRepository.save(pricingRule);
        log.info("AUDIT_LOG | PRICING_RULE_UPDATED | VehicleType: {} | BaseRate: {}", request.getVehicleType(), request.getBaseRatePerKm());
        return updatedRule;
    }

    @Transactional(readOnly = true)
    public Double calculatePrice(PricingCalculateRequest request) {
        PricingRule rule = pricingRuleRepository.findByVehicleType(request.getVehicleType())
                .orElseThrow(() -> new PricingRuleNotFoundException("Pricing rules not configured for vehicle type: " + request.getVehicleType()));

        // Formula check: price = baseRate * distance * weight
        double calculatedPrice = rule.getBaseRatePerKm() * request.getDistanceKm() * request.getWeightKg();

        // Enforce minimum charge guard limit
        if (calculatedPrice < rule.getMinimumCharge()) {
            calculatedPrice = rule.getMinimumCharge();
        }

        log.debug("PRICING_CALCULATED | VehicleType: {} | Dist: {} | Weight: {} | Price: {}", 
                request.getVehicleType(), request.getDistanceKm(), request.getWeightKg(), calculatedPrice);
        return calculatedPrice;
    }
}
