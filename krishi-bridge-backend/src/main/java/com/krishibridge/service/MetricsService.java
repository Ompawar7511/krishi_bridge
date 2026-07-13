package com.krishibridge.service;

import com.krishibridge.entity.UserMetrics;
import com.krishibridge.repository.RatingRepository;
import com.krishibridge.repository.UserMetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final UserMetricsRepository metricsRepository;
    private final RatingRepository ratingRepository;

    public MetricsService(UserMetricsRepository metricsRepository, RatingRepository ratingRepository) {
        this.metricsRepository = metricsRepository;
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public void updateUserMetricsAfterDelivery(Long userId) {
        UserMetrics metrics = metricsRepository.findById(userId)
                .orElseGet(() -> new UserMetrics(userId, 0, 0, 5.0));

        metrics.setSuccessfulDeliveries(metrics.getSuccessfulDeliveries() + 1);
        
        // Compute new average rating
        Double avgRating = ratingRepository.getAverageRatingForUser(userId);
        if (avgRating != null) {
            metrics.setAverageRating(avgRating);
        }

        metricsRepository.save(metrics);
        log.info("METRICS_LOG | COMPILATION | UserId: {} | TripsCompleted: {} | AvgRating: {}", 
                userId, metrics.getSuccessfulDeliveries(), metrics.getAverageRating());
    }

    @Transactional
    public void incrementCancelledBookings(Long userId) {
        UserMetrics metrics = metricsRepository.findById(userId)
                .orElseGet(() -> new UserMetrics(userId, 0, 0, 5.0));

        metrics.setCancelledBookings(metrics.getCancelledBookings() + 1);
        metricsRepository.save(metrics);
        log.info("METRICS_LOG | COMPILATION | UserId: {} | Cancellations: {}", userId, metrics.getCancelledBookings());
    }
}
