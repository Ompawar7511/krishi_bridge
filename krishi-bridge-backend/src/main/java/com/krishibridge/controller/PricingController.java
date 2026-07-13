package com.krishibridge.controller;

import com.krishibridge.dto.request.PricingCalculateRequest;
import com.krishibridge.dto.request.PricingRuleRequest;
import com.krishibridge.dto.response.PricingCalculateResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.PricingRule;
import com.krishibridge.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/pricing/calculate")
    public ResponseEntity<StandardResponse<PricingCalculateResponse>> calculatePrice(
            @Valid @RequestBody PricingCalculateRequest request) {
        
        Double estimatedPrice = pricingService.calculatePrice(request);
        PricingCalculateResponse responseDto = new PricingCalculateResponse(
                request.getVehicleType(),
                request.getDistanceKm(),
                request.getWeightKg(),
                estimatedPrice
        );
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Price calculated successfully"));
    }

    @PostMapping("/admin/pricing/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<PricingRule>> createPricingRule(
            @Valid @RequestBody PricingRuleRequest request) {
        
        PricingRule rule = pricingService.createPricingRule(request);
        return ResponseEntity.ok(StandardResponse.success(rule, "Pricing rule created successfully"));
    }

    @PutMapping("/admin/pricing/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<PricingRule>> updatePricingRule(
            @Valid @RequestBody PricingRuleRequest request) {
        
        PricingRule rule = pricingService.updatePricingRule(request);
        return ResponseEntity.ok(StandardResponse.success(rule, "Pricing rule updated successfully"));
    }
}
