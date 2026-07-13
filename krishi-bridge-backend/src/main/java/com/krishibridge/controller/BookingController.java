package com.krishibridge.controller;

import com.krishibridge.dto.request.BookingCreateRequest;
import com.krishibridge.dto.response.BookingResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.Booking;
import com.krishibridge.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/booking")
@PreAuthorize("hasRole('FARMER')")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public ResponseEntity<StandardResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        Long farmerId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Booking booking = bookingService.createBooking(farmerId, request);
        BookingResponse responseDto = mapToDto(booking);
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Booking request created successfully"));
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<StandardResponse<String>> cancelBooking(@PathVariable("id") Long id) {
        Long farmerId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookingService.cancelBooking(farmerId, id);
        return ResponseEntity.ok(StandardResponse.success("Booking request cancelled successfully", "Cancellation processed"));
    }

    @GetMapping("/history")
    public ResponseEntity<StandardResponse<List<BookingResponse>>> getBookingHistory() {
        Long farmerId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Booking> bookings = bookingService.getFarmerBookingHistory(farmerId);
        List<BookingResponse> responseList = bookings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(responseList));
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
