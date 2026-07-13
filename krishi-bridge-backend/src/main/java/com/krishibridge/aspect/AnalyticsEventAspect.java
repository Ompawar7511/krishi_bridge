package com.krishibridge.aspect;

import com.krishibridge.entity.Booking;
import com.krishibridge.entity.TransporterDocument;
import com.krishibridge.enums.VerificationStatus;
import com.krishibridge.repository.TransporterDocumentRepository;
import com.krishibridge.service.AnalyticsService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AnalyticsEventAspect {

    private final AnalyticsService analyticsService;
    private final TransporterDocumentRepository documentRepository;

    public AnalyticsEventAspect(AnalyticsService analyticsService, TransporterDocumentRepository documentRepository) {
        this.analyticsService = analyticsService;
        this.documentRepository = documentRepository;
    }

    @AfterReturning(pointcut = "execution(* com.krishibridge.service.BookingService.createBooking(..))", returning = "booking")
    public void trackBookingCreated(JoinPoint joinPoint, Booking booking) {
        if (booking != null) {
            String metadata = String.format("{\"bookingId\": %d, \"price\": %.2f, \"weight\": %.2f, \"cropType\": \"%s\"}", 
                    booking.getId(), booking.getEstimatedPrice(), booking.getWeightTons(), booking.getCropType());
            analyticsService.trackEvent("BookingCreated", booking.getFarmerId(), metadata);
        }
    }

    @AfterReturning("execution(* com.krishibridge.service.BookingService.cancelBooking(..))")
    public void trackBookingCancelled(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length >= 2) {
            Long farmerId = (Long) args[0];
            Long bookingId = (Long) args[1];
            String metadata = String.format("{\"bookingId\": %d}", bookingId);
            analyticsService.trackEvent("BookingCancelled", farmerId, metadata);
        }
    }

    @AfterReturning(pointcut = "execution(* com.krishibridge.service.ComplianceService.approveDocument(..))", returning = "document")
    public void trackTransporterApproved(JoinPoint joinPoint, TransporterDocument document) {
        if (document != null && document.getVerificationStatus() == VerificationStatus.APPROVED) {
            Long transporterId = document.getTransporterId();
            
            // Check if all 5 documents are now approved for this transporter
            long approvedCount = documentRepository.countByTransporterIdAndVerificationStatus(transporterId, VerificationStatus.APPROVED);
            if (approvedCount == 5) {
                String metadata = String.format("{\"transporterId\": %d}", transporterId);
                analyticsService.trackEvent("TransporterApproved", transporterId, metadata);
            }
        }
    }
}
