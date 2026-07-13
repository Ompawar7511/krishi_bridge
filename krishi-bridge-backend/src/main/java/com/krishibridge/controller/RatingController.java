package com.krishibridge.controller;

import com.krishibridge.dto.request.RatingCreateRequest;
import com.krishibridge.dto.response.RatingResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.Rating;
import com.krishibridge.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rating")
@PreAuthorize("hasRole('FARMER')")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/create")
    public ResponseEntity<StandardResponse<RatingResponse>> createRating(@Valid @RequestBody RatingCreateRequest request) {
        Long farmerId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Rating rating = ratingService.createRating(farmerId, request);
        
        RatingResponse responseDto = new RatingResponse(
                rating.getId(),
                rating.getBookingId(),
                rating.getRatedUser(),
                rating.getRatingUser(),
                rating.getRating(),
                rating.getReview(),
                rating.getCreatedAt()
        );
        return ResponseEntity.ok(StandardResponse.success(responseDto, "Review submitted successfully"));
    }
}
