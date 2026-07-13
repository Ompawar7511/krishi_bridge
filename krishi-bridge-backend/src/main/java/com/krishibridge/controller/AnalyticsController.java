package com.krishibridge.controller;

import com.krishibridge.dto.response.AnalyticsDashboardResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StandardResponse<AnalyticsDashboardResponse>> getAnalyticsDashboard() {
        AnalyticsDashboardResponse summary = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(StandardResponse.success(summary, "Dashboard statistics loaded successfully"));
    }
}
