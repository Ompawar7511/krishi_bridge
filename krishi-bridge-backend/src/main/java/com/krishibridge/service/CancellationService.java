package com.krishibridge.service;

import com.krishibridge.entity.CancellationRule;
import com.krishibridge.repository.CancellationRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class CancellationService {

    private final CancellationRuleRepository ruleRepository;

    public CancellationService(CancellationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional(readOnly = true)
    public Double calculateCancellationPenalty(String bookingStage, Double estimatedPrice) {
        Optional<CancellationRule> ruleOpt = ruleRepository.findByBookingStage(bookingStage);
        if (ruleOpt.isEmpty()) {
            return 0.00;
        }
        
        CancellationRule rule = ruleOpt.get();
        return estimatedPrice * (rule.getPenaltyPercentage() / 100.0);
    }
}
