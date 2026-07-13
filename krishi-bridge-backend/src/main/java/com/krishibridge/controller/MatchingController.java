package com.krishibridge.controller;

import com.krishibridge.dto.response.MatchedTransporterResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.service.BookingAssignmentService;
import com.krishibridge.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/matching")
@PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
public class MatchingController {

    private final MatchingService matchingService;
    private final BookingAssignmentService bookingAssignmentService;

    public MatchingController(MatchingService matchingService, BookingAssignmentService bookingAssignmentService) {
        this.matchingService = matchingService;
        this.bookingAssignmentService = bookingAssignmentService;
    }

    @GetMapping("/find/{bookingId}")
    public ResponseEntity<StandardResponse<List<MatchedTransporterResponse>>> findMatches(@PathVariable("bookingId") Long bookingId) {
        Long farmerId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MatchedTransporterResponse> matches = matchingService.findMatchedTransporters(bookingId, farmerId);
        return ResponseEntity.ok(StandardResponse.success(matches, "Matching engine execution completed"));
    }

    @PostMapping("/accept/{bookingId}/transporter/{transporterId}/vehicle/{vehicleId}")
    public ResponseEntity<StandardResponse<String>> acceptMatch(
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("transporterId") Long transporterId,
            @PathVariable("vehicleId") Long vehicleId) {
        bookingAssignmentService.assignTransporter(bookingId, transporterId, vehicleId);
        return ResponseEntity.ok(StandardResponse.success("Transporter assigned successfully", "Match accepted"));
    }

    @PostMapping("/reject/{bookingId}/transporter/{transporterId}")
    public ResponseEntity<StandardResponse<String>> rejectMatch(
            @PathVariable("bookingId") Long bookingId,
            @PathVariable("transporterId") Long transporterId) {
        bookingAssignmentService.rejectMatch(bookingId, transporterId);
        return ResponseEntity.ok(StandardResponse.success("Match rejected successfully", "Match rejected"));
    }
}
