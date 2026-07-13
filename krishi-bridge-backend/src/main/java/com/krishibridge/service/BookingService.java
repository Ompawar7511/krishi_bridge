package com.krishibridge.service;

import com.krishibridge.dto.request.BookingCreateRequest;
import com.krishibridge.dto.request.PricingCalculateRequest;
import com.krishibridge.entity.Booking;
import com.krishibridge.entity.User;
import com.krishibridge.enums.BookingStatus;
import com.krishibridge.enums.Role;
import com.krishibridge.enums.UserStatus;
import com.krishibridge.exception.BookingException;
import com.krishibridge.exception.BookingNotFoundException;
import com.krishibridge.exception.ComplianceException;
import com.krishibridge.repository.BookingRepository;
import com.krishibridge.repository.UserRepository;
import com.krishibridge.repository.BookingMatchRepository;
import com.krishibridge.repository.VehicleScheduleRepository;
import com.krishibridge.entity.BookingMatch;
import com.krishibridge.entity.VehicleSchedule;
import com.krishibridge.enums.ScheduleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final NotificationService notificationService;
    private final BookingMatchRepository matchRepository;
    private final VehicleScheduleRepository scheduleRepository;

    public BookingService(BookingRepository bookingRepository, 
                          UserRepository userRepository, 
                          PricingService pricingService, 
                          NotificationService notificationService,
                          BookingMatchRepository matchRepository,
                          VehicleScheduleRepository scheduleRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.pricingService = pricingService;
        this.notificationService = notificationService;
        this.matchRepository = matchRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional
    public Booking createBooking(Long farmerId, BookingCreateRequest request) {
        // Enforce role and status validation checks
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ComplianceException("Farmer user not found"));

        if (farmer.getRole() != Role.FARMER || farmer.getStatus() != UserStatus.APPROVED) {
            log.warn("SECURITY_VIOLATION | CREATE_BOOKING_BLOCKED | FarmerId: {} | Status: {}", farmerId, farmer.getStatus());
            throw new ComplianceException("Only approved Farmers with verified profiles can create booking requests");
        }

        // Idempotency check logic to prevent duplicate creations
        Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingBooking.isPresent()) {
            log.info("BOOKING_LOG | DUPLICATE_PREVENTED | Key: {}", request.getIdempotencyKey());
            return existingBooking.get();
        }

        // Quote calculation via reusable PricingService
        PricingCalculateRequest calcRequest = new PricingCalculateRequest(
                request.getVehicleType(),
                request.getEstimatedDistanceKm(),
                request.getWeightTons() * 1000.0 // Convert tons to kilograms
        );
        Double estimatedPrice = pricingService.calculatePrice(calcRequest);

        Booking booking = new Booking(
                farmerId,
                request.getCropType(),
                request.getWeightTons(),
                request.getPickupLatitude(),
                request.getPickupLongitude(),
                request.getDestinationLatitude(),
                request.getDestinationLongitude(),
                request.getEstimatedDistanceKm(),
                estimatedPrice,
                request.getIdempotencyKey()
        );

        Booking savedBooking = bookingRepository.save(booking);
        log.info("AUDIT_LOG | BOOKING_CREATED | FarmerId: {} | BookingId: {} | Price: {}", farmerId, savedBooking.getId(), estimatedPrice);
        return savedBooking;
    }

    @Transactional
    public void cancelBooking(Long farmerId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));

        if (!booking.getFarmerId().equals(farmerId)) {
            log.warn("SECURITY_VIOLATION | CANCEL_BOOKING_FORBIDDEN | ActorId: {} | OwnerId: {}", farmerId, booking.getFarmerId());
            throw new BookingException("Access denied. You do not own this booking request.");
        }

        BookingStatus currentStatus = booking.getStatus();

        // Cancellation Policy Engine Business Rules checks
        switch (currentStatus) {
            case CREATED:
            case MATCHING:
            case WAITING_TRANSPORTER:
            case TRANSPORTER_REJECTED:
                // Rule 1: Free cancellation before acceptance
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                log.info("AUDIT_LOG | BOOKING_CANCELLED_FREE | FarmerId: {} | BookingId: {}", farmerId, bookingId);
                break;

            case TRANSPORTER_ACCEPTED:
                // Rule 2: Penalty fee applies after acceptance
                releaseVehicleScheduleIfAssigned(booking);
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                log.warn("AUDIT_LOG | BOOKING_CANCELLED_WITH_PENALTY | FarmerId: {} | BookingId: {} | Penalty: 15%", farmerId, bookingId);
                break;

            case PICKUP_STARTED:
                // Rule 3: Higher penalty fee applies during driver transit to pick up load
                releaseVehicleScheduleIfAssigned(booking);
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                log.warn("AUDIT_LOG | BOOKING_CANCELLED_WITH_PENALTY | FarmerId: {} | BookingId: {} | Penalty: 30%", farmerId, bookingId);
                break;

            case IN_TRANSIT:
            case DELIVERED:
            case COMPLETED:
                // Rule 4: Active transit cargo states are blocked from user cancellations
                log.error("BOOKING_LOG | CANCEL_FAILED | BookingId: {} | Status: {}", bookingId, currentStatus);
                throw new BookingException("Cancellation is blocked. Cargo is already in transit or completed. Raise a dispute if necessary.");

            case CANCELLED:
                throw new BookingException("Booking is already cancelled");

            default:
                throw new BookingException("Unknown booking status sequence");
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> getFarmerBookingHistory(Long farmerId) {
        return bookingRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId);
    }

    @Transactional(readOnly = true)
    public List<Booking> getTransporterBookingHistory(Long transporterId) {
        return bookingRepository.findByAssignedTransporterIdOrderByCreatedAtDesc(transporterId);
    }

    @Transactional
    public void completeBooking(Long transporterId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));

        if (booking.getAssignedTransporterId() == null || !booking.getAssignedTransporterId().equals(transporterId)) {
            log.warn("SECURITY_VIOLATION | COMPLETE_BOOKING_FORBIDDEN | ActorId: {} | AssignedTransporterId: {}", transporterId, booking.getAssignedTransporterId());
            throw new BookingException("Access denied. You are not assigned to this booking order.");
        }

        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus != BookingStatus.TRANSPORTER_ACCEPTED) {
            log.error("BOOKING_LOG | COMPLETE_FAILED | BookingId: {} | Status: {}", bookingId, currentStatus);
            throw new BookingException("Only trips that have been accepted by the transporter can be marked as completed.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        // Send notification/message to the Farmer dashboard
        String msg = String.format("Trip Completed: Transporter has successfully completed delivery for Booking #%d (%s, %.2f tons).", 
                booking.getId(), booking.getCropType(), booking.getWeightTons());
        notificationService.createNotification(booking.getFarmerId(), msg, "TRIP_COMPLETED");

        log.info("AUDIT_LOG | BOOKING_COMPLETED_BY_TRANSPORTER | TransporterId: {} | BookingId: {}", transporterId, bookingId);
    }

    @Transactional
    public void cancelTripByTransporter(Long transporterId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));

        if (booking.getAssignedTransporterId() == null || !booking.getAssignedTransporterId().equals(transporterId)) {
            log.warn("SECURITY_VIOLATION | CANCEL_TRIP_FORBIDDEN | ActorId: {} | AssignedTransporterId: {}", transporterId, booking.getAssignedTransporterId());
            throw new BookingException("Access denied. You are not assigned to this booking order.");
        }

        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus != BookingStatus.TRANSPORTER_ACCEPTED) {
            log.error("BOOKING_LOG | CANCEL_TRIP_FAILED | BookingId: {} | Status: {}", bookingId, currentStatus);
            throw new BookingException("Only trips that are booked and in TRANSPORTER_ACCEPTED state can be cancelled/removed by the transporter.");
        }

        // Release/Delete vehicle schedule booked slot block
        if (booking.getAssignedVehicleId() != null) {
            List<VehicleSchedule> schedules = scheduleRepository.findByVehicleId(booking.getAssignedVehicleId());
            for (VehicleSchedule s : schedules) {
                if (s.getStatus() == ScheduleStatus.BOOKED) {
                    scheduleRepository.delete(s);
                }
            }
        }

        // Set match status to REJECTED so transporter won't immediately get matched back
        Optional<BookingMatch> matchOpt = matchRepository.findByBookingIdAndTransporterId(bookingId, transporterId);
        if (matchOpt.isPresent()) {
            BookingMatch match = matchOpt.get();
            match.setMatchStatus("REJECTED");
            matchRepository.save(match);
        }

        // Reset booking assigned transporter/vehicle details and status back to CREATED
        booking.setAssignedTransporterId(null);
        booking.setAssignedVehicleId(null);
        booking.setStatus(BookingStatus.CREATED);
        bookingRepository.save(booking);

        // Notify the farmer that their booked transporter has cancelled the trip
        String msg = String.format("Trip Cancelled: Transporter has cancelled and removed the booked trip for Booking #%d.", bookingId);
        notificationService.createNotification(booking.getFarmerId(), msg, "TRIP_CANCELLED");

        log.info("AUDIT_LOG | TRIP_CANCELLED_BY_TRANSPORTER | TransporterId: {} | BookingId: {}", transporterId, bookingId);
    }

    private void releaseVehicleScheduleIfAssigned(Booking booking) {
        if (booking.getAssignedVehicleId() != null) {
            List<VehicleSchedule> schedules = scheduleRepository.findByVehicleId(booking.getAssignedVehicleId());
            for (VehicleSchedule s : schedules) {
                if (s.getStatus() == ScheduleStatus.BOOKED) {
                    scheduleRepository.delete(s);
                }
            }
        }
    }
}
