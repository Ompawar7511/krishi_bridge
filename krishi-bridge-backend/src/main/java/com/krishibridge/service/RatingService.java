package com.krishibridge.service;

import com.krishibridge.dto.request.RatingCreateRequest;
import com.krishibridge.entity.Booking;
import com.krishibridge.entity.Rating;
import com.krishibridge.enums.BookingStatus;
import com.krishibridge.exception.BookingException;
import com.krishibridge.exception.BookingNotFoundException;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.repository.BookingRepository;
import com.krishibridge.repository.RatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class RatingService {

    private static final Logger log = LoggerFactory.getLogger(RatingService.class);

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final MetricsService metricsService;
    private final AnalyticsService analyticsService;

    public RatingService(RatingRepository ratingRepository,
                         BookingRepository bookingRepository,
                         MetricsService metricsService,
                         AnalyticsService analyticsService) {
        this.ratingRepository = ratingRepository;
        this.bookingRepository = bookingRepository;
        this.metricsService = metricsService;
        this.analyticsService = analyticsService;
    }

    @Transactional
    public Rating createRating(Long farmerId, RatingCreateRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (!booking.getFarmerId().equals(farmerId)) {
            throw new BookingException("Access denied. You did not register this booking order.");
        }

        // Verify status is completed or delivered
        BookingStatus status = booking.getStatus();
        if (status != BookingStatus.COMPLETED && status != BookingStatus.DELIVERED) {
            throw new BookingException("Ratings can only be submitted for completed or delivered transport orders.");
        }

        if (booking.getAssignedTransporterId() == null) {
            throw new BookingException("No transporter was assigned to this booking");
        }

        // Check unique constraint per booking
        Optional<Rating> existingRating = ratingRepository.findByBookingId(request.getBookingId());
        if (existingRating.isPresent()) {
            throw new BookingException("A review has already been submitted for this booking");
        }

        Rating rating = new Rating(
                request.getBookingId(),
                booking.getAssignedTransporterId(),
                farmerId,
                request.getRating(),
                request.getReview()
        );

        Rating savedRating = ratingRepository.save(rating);

        // Recalculate and cache metrics
        metricsService.updateUserMetricsAfterDelivery(booking.getAssignedTransporterId());

        // Track Analytics Event: DeliveryCompleted
        String metadata = String.format("{\"bookingId\": %d, \"rating\": %d}", booking.getId(), request.getRating());
        analyticsService.trackEvent("DeliveryCompleted", booking.getAssignedTransporterId(), metadata);

        log.info("AUDIT_LOG | RATING_SUBMITTED | BookingId: {} | RatedUser: {} | Score: {}", 
                request.getBookingId(), booking.getAssignedTransporterId(), request.getRating());
        
        return savedRating;
    }
}
