package com.krishibridge.service;

import com.krishibridge.entity.Booking;
import com.krishibridge.entity.Dispute;
import com.krishibridge.enums.BookingStatus;
import com.krishibridge.enums.DisputeStatus;
import com.krishibridge.exception.BookingException;
import com.krishibridge.exception.BookingNotFoundException;
import com.krishibridge.exception.DisputeNotFoundException;
import com.krishibridge.repository.BookingRepository;
import com.krishibridge.repository.DisputeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DisputeService {

    private static final Logger log = LoggerFactory.getLogger(DisputeService.class);

    private final DisputeRepository disputeRepository;
    private final BookingRepository bookingRepository;

    public DisputeService(DisputeRepository disputeRepository, BookingRepository bookingRepository) {
        this.disputeRepository = disputeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public Dispute createDispute(Long bookingId, Long userId, String reason, String description) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        // Enforce associated stakeholder validation check
        boolean isFarmer = booking.getFarmerId().equals(userId);
        boolean isTransporter = booking.getAssignedTransporterId() != null && booking.getAssignedTransporterId().equals(userId);
        
        if (!isFarmer && !isTransporter) {
            log.warn("SECURITY_VIOLATION | CREATE_DISPUTE_FORBIDDEN | ActorId: {} | BookingId: {}", userId, bookingId);
            throw new BookingException("Access denied. You are not a registered party for this booking request.");
        }

        // Active transit status constraint checking: disputes can only occur during active transport stages
        BookingStatus status = booking.getStatus();
        if (status != BookingStatus.IN_TRANSIT && status != BookingStatus.DELIVERED && status != BookingStatus.TRANSPORTER_ACCEPTED) {
            throw new BookingException("Disputes can only be raised for bookings that have been accepted or are currently in transit.");
        }

        Dispute dispute = new Dispute(bookingId, userId, reason, description);
        Dispute savedDispute = disputeRepository.save(dispute);

        // Audit Log Hook: Dispute Created
        log.warn("AUDIT_LOG | DISPUTE_CREATED | DisputeId: {} | BookingId: {} | RaisedBy: {}", savedDispute.getId(), bookingId, userId);
        return savedDispute;
    }

    @Transactional
    public Dispute resolveDispute(Long disputeId, DisputeStatus status, String adminNotes) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("Dispute not found"));

        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.DISMISSED) {
            throw new BookingException("Dispute has already been finalized");
        }

        if (status != DisputeStatus.RESOLVED && status != DisputeStatus.DISMISSED) {
            throw new BookingException("Invalid dispute resolution target status");
        }

        dispute.setStatus(status);
        dispute.setAdminNotes(adminNotes);
        dispute.setResolvedAt(LocalDateTime.now());
        Dispute resolvedDispute = disputeRepository.save(dispute);

        // Update corresponding booking status on resolution overrides
        OptionalToCompleteBooking(dispute.getBookingId(), status);

        // Audit Log Hook: Dispute Resolved
        log.info("AUDIT_LOG | DISPUTE_RESOLVED | DisputeId: {} | BookingId: {} | Resolution: {}", disputeId, dispute.getBookingId(), status);
        return resolvedDispute;
    }

    @Transactional(readOnly = true)
    public List<Dispute> getPendingDisputes() {
        return disputeRepository.findByStatus(DisputeStatus.PENDING);
    }

    private void OptionalToCompleteBooking(Long bookingId, DisputeStatus status) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            if (status == DisputeStatus.RESOLVED) {
                // If resolved in favor of completing, update booking status to COMPLETED
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRepository.save(booking);
                log.info("AUDIT_LOG | BOOKING_COMPLETED_VIA_DISPUTE | BookingId: {}", bookingId);
            } else if (status == DisputeStatus.DISMISSED) {
                // Keep booking or set based on context. For MVP, complete it to release state.
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRepository.save(booking);
            }
        }
    }
}
