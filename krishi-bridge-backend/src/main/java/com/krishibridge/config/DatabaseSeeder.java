package com.krishibridge.config;

import com.krishibridge.entity.PricingRule;
import com.krishibridge.entity.User;
import com.krishibridge.enums.Role;
import com.krishibridge.enums.UserStatus;
import com.krishibridge.repository.PricingRuleRepository;
import com.krishibridge.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final PricingRuleRepository pricingRuleRepository;
    private final UserRepository userRepository;

    public DatabaseSeeder(PricingRuleRepository pricingRuleRepository, UserRepository userRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Default Pricing Rules if not configured
        if (!pricingRuleRepository.existsByVehicleType("TRUCK")) {
            pricingRuleRepository.save(new PricingRule("TRUCK", 15.0, 0.05, 500.0));
        }
        if (!pricingRuleRepository.existsByVehicleType("MINI_TRUCK")) {
            pricingRuleRepository.save(new PricingRule("MINI_TRUCK", 10.0, 0.03, 300.0));
        }
        if (!pricingRuleRepository.existsByVehicleType("TRACTOR")) {
            pricingRuleRepository.save(new PricingRule("TRACTOR", 8.0, 0.02, 200.0));
        }

        // 2. Auto-approve all existing PENDING accounts to make them workable immediately
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getStatus() == UserStatus.PENDING) {
                user.setStatus(UserStatus.APPROVED);
                userRepository.save(user);
                System.out.println("Auto-approved user: " + user.getPhoneNumber() + " (" + user.getRole() + ")");
            }
        }
    }
}
