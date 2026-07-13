package com.krishibridge.controller;

import com.krishibridge.dto.response.BookingResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.Booking;
import com.krishibridge.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transporter/booking")
@PreAuthorize("hasRole('TRANSPORTER')")
public class TransporterBookingController {

    private final BookingService bookingService;

    public TransporterBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/history")
    public ResponseEntity<StandardResponse<List<BookingResponse>>> getBookingHistory() {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Booking> bookings = bookingService.getTransporterBookingHistory(transporterId);
        List<BookingResponse> responseList = bookings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList, "Transporter booking history fetched successfully"));
    }

    @PostMapping("/complete/{bookingId}")
    public ResponseEntity<StandardResponse<String>> completeBooking(@PathVariable("bookingId") Long bookingId) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookingService.completeBooking(transporterId, bookingId);
        return ResponseEntity.ok(StandardResponse.success("Trip marked as completed successfully", "Delivery finalized"));
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<StandardResponse<String>> cancelBooking(@PathVariable("bookingId") Long bookingId) {
        Long transporterId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookingService.cancelTripByTransporter(transporterId, bookingId);
        return ResponseEntity.ok(StandardResponse.success("Trip has been cancelled/removed successfully", "Trip cancelled"));
    }

    private BookingResponse mapToDto(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getFarmerId(),
                booking.getCropType(),
                booking.getWeightTons(),
                booking.getPickupLatitude(),
                booking.getPickupLongitude(),
                booking.getDestinationLatitude(),
                booking.getDestinationLongitude(),
                booking.getEstimatedDistanceKm(),
                booking.getEstimatedPrice(),
                booking.getIdempotencyKey(),
                booking.getStatus(),
                booking.getAssignedTransporterId(),
                booking.getAssignedVehicleId(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
