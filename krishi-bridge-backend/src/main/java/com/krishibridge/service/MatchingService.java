package com.krishibridge.service;

import com.krishibridge.dto.response.MatchedTransporterResponse;
import com.krishibridge.entity.Booking;
import com.krishibridge.entity.BookingMatch;
import com.krishibridge.entity.User;
import com.krishibridge.entity.Vehicle;
import com.krishibridge.enums.BookingStatus;
import com.krishibridge.exception.BookingException;
import com.krishibridge.exception.BookingNotFoundException;
import com.krishibridge.repository.BookingMatchRepository;
import com.krishibridge.repository.BookingRepository;
import com.krishibridge.repository.UserRepository;
import com.krishibridge.repository.VehicleMatchingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final BookingRepository bookingRepository;
    private final VehicleMatchingRepository matchingRepository;
    private final BookingMatchRepository matchRepository;
    private final UserRepository userRepository;

    public MatchingService(BookingRepository bookingRepository,
                           VehicleMatchingRepository matchingRepository,
                           BookingMatchRepository matchRepository,
                           UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.matchingRepository = matchingRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<MatchedTransporterResponse> findMatchedTransporters(Long bookingId, Long farmerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found"));

        if (!booking.getFarmerId().equals(farmerId)) {
            throw new BookingException("Access denied. You do not own this booking request.");
        }

        // Set booking status to MATCHING
        booking.setStatus(BookingStatus.MATCHING);
        bookingRepository.save(booking);

        // Calculate travel window (Assumes avg speed of 40 km/h + 2 hours loading/unloading buffer)
        LocalDateTime startTime = LocalDateTime.now();
        double travelDurationHours = (booking.getEstimatedDistanceKm() / 40.0) + 2.0;
        LocalDateTime endTime = startTime.plusMinutes((long) (travelDurationHours * 60));

        // Spatial query matching nearby vehicles (distance <= 20km, capacity >= weight, no schedule conflicts)
        List<Vehicle> matchedVehicles = matchingRepository.findMatchingVehicles(
                booking.getPickupLongitude(),
                booking.getPickupLatitude(),
                booking.getWeightTons(),
                startTime,
                endTime
        );

        List<MatchedTransporterResponse> responses = new ArrayList<>();

        for (Vehicle vehicle : matchedVehicles) {
            // Save match junction table mapping
            Optional<BookingMatch> existingMatch = matchRepository.findByBookingIdAndTransporterId(bookingId, vehicle.getTransporterId());
            if (existingMatch.isEmpty()) {
                BookingMatch match = new BookingMatch(bookingId, vehicle.getTransporterId(), "PENDING");
                matchRepository.save(match);
            }

            // Retrieve Transporter Name
            User transporter = userRepository.findById(vehicle.getTransporterId()).orElse(null);
            String transporterName = (transporter != null) ? transporter.getName() : "Unknown Transporter";

            // Calculate precise geodistance using Haversine algorithm
            double distanceKm = calculateHaversineDistance(
                    booking.getPickupLatitude(), booking.getPickupLongitude(),
                    vehicle.getLatitude(), vehicle.getLongitude()
            );

            responses.add(new MatchedTransporterResponse(
                    vehicle.getTransporterId(),
                    transporterName,
                    vehicle.getId(),
                    vehicle.getVehicleNumber(),
                    vehicle.getVehicleType(),
                    vehicle.getCapacityTons(),
                    distanceKm
            ));
        }

        // Update booking state
        if (!responses.isEmpty()) {
            booking.setStatus(BookingStatus.WAITING_TRANSPORTER);
            log.info("AUDIT_LOG | MATCHING_SUCCESS | BookingId: {} | MatchesFound: {}", bookingId, responses.size());
        } else {
            booking.setStatus(BookingStatus.CREATED);
            log.warn("AUDIT_LOG | MATCHING_EMPTY | BookingId: {} | Reason: No vehicles found within criteria", bookingId);
        }
        bookingRepository.save(booking);

        return responses;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
