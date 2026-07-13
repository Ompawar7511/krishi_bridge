package com.krishibridge.service;

import com.krishibridge.entity.Booking;
import com.krishibridge.entity.BookingMatch;
import com.krishibridge.entity.VehicleSchedule;
import com.krishibridge.enums.BookingStatus;
import com.krishibridge.enums.ScheduleStatus;
import com.krishibridge.exception.BookingException;
import com.krishibridge.exception.BookingNotFoundException;
import com.krishibridge.repository.BookingMatchRepository;
import com.krishibridge.repository.BookingRepository;
import com.krishibridge.repository.VehicleScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(BookingAssignmentService.class);

    private final BookingRepository bookingRepository;
    private final BookingMatchRepository matchRepository;
    private final VehicleScheduleRepository scheduleRepository;

    public BookingAssignmentService(BookingRepository bookingRepository,
                                    BookingMatchRepository matchRepository,
                                    VehicleScheduleRepository scheduleRepository) {
        this.bookingRepository = bookingRepository;
        this.matchRepository = matchRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public void assignTransporter(Long bookingId, Long transporterId, Long vehicleId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.WAITING_TRANSPORTER && booking.getStatus() != BookingStatus.MATCHING) {
            throw new BookingException("Booking is not open for assignment. Status: " + booking.getStatus());
        }

        // Verify match exists in database
        BookingMatch match = matchRepository.findByBookingIdAndTransporterId(bookingId, transporterId)
                .orElseThrow(() -> new BookingException("Transporter is not a valid match for this booking"));

        // Update match state
        match.setMatchStatus("ACCEPTED");
        matchRepository.save(match);

        // Reject other pending matches for this booking
        List<BookingMatch> otherMatches = matchRepository.findByBookingId(bookingId);
        for (BookingMatch other : otherMatches) {
            if (!other.getTransporterId().equals(transporterId)) {
                other.setMatchStatus("REJECTED");
                matchRepository.save(other);
            }
        }

        // Update booking assigned details & status
        booking.setAssignedTransporterId(transporterId);
        booking.setAssignedVehicleId(vehicleId);
        booking.setStatus(BookingStatus.TRANSPORTER_ACCEPTED);
        bookingRepository.save(booking);

        // Create vehicle schedule BOOKED slot block
        LocalDateTime startTime = LocalDateTime.now();
        double travelDurationHours = (booking.getEstimatedDistanceKm() / 40.0) + 2.0;
        LocalDateTime endTime = startTime.plusMinutes((long) (travelDurationHours * 60));

        VehicleSchedule schedule = new VehicleSchedule(
                vehicleId,
                startTime,
                endTime,
                ScheduleStatus.BOOKED
        );
        scheduleRepository.save(schedule);

        // Audit Log Hook: Transporter Assigned & Schedule Reserved
        log.info("AUDIT_LOG | TRANSPORTER_ASSIGNED | BookingId: {} | TransporterId: {} | VehicleId: {}", bookingId, transporterId, vehicleId);
    }

    @Transactional
    public void rejectMatch(Long bookingId, Long transporterId) {
        BookingMatch match = matchRepository.findByBookingIdAndTransporterId(bookingId, transporterId)
                .orElseThrow(() -> new BookingException("Match not found"));

        match.setMatchStatus("REJECTED");
        matchRepository.save(match);

        log.info("AUDIT_LOG | MATCH_REJECTED | BookingId: {} | TransporterId: {}", bookingId, transporterId);

        // Check if all offers for this booking have been rejected. If yes, reset booking back to CREATED
        List<BookingMatch> matches = matchRepository.findByBookingId(bookingId);
        boolean allRejected = matches.stream().allMatch(m -> "REJECTED".equals(m.getMatchStatus()));

        if (allRejected) {
            Booking booking = bookingRepository.findById(bookingId).orElse(null);
            if (booking != null && booking.getStatus() == BookingStatus.WAITING_TRANSPORTER) {
                booking.setStatus(BookingStatus.CREATED);
                bookingRepository.save(booking);
                log.warn("AUDIT_LOG | ALL_MATCHES_REJECTED | BookingId: {} | Reset status to CREATED", bookingId);
            }
        }
    }
}
