package com.krishibridge.service;

import com.krishibridge.dto.response.AnalyticsDashboardResponse;
import com.krishibridge.entity.AnalyticsEvent;
import com.krishibridge.repository.AnalyticsEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsEventRepository eventRepository;

    public AnalyticsService(AnalyticsEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void trackEvent(String eventName, Long userId, String metadataJson) {
        AnalyticsEvent event = new AnalyticsEvent(eventName, userId, metadataJson);
        eventRepository.save(event);
        log.info("ANALYTICS_EVENT | Event: {} | User: {}", eventName, userId);
    }

    @Transactional(readOnly = true)
    public AnalyticsDashboardResponse getAnalyticsSummary() {
        LocalDateTime since30Days = LocalDateTime.now().minusDays(30);

        long createdCount = eventRepository.countByEventName("BookingCreated");
        long cancelledCount = eventRepository.countByEventName("BookingCancelled");
        long completedCount = eventRepository.countByEventName("DeliveryCompleted");
        long approvedTransporters = eventRepository.countByEventName("TransporterApproved");
        
        long activeUsers = eventRepository.countDistinctActiveUsers(since30Days);

        double cancellationRate = 0.0;
        if (createdCount > 0) {
            cancellationRate = ((double) cancelledCount / createdCount) * 100.0;
        }

        return new AnalyticsDashboardResponse(
                createdCount,
                cancelledCount,
                completedCount,
                approvedTransporters,
                activeUsers,
                cancellationRate
        );
    }
}
