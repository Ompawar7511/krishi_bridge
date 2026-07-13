package com.krishibridge.dto.response;

import java.time.LocalDateTime;

public class RatingResponse {
    private Long id;
    private Long bookingId;
    private Long ratedUser;
    private Long ratingUser;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;

    public RatingResponse() {}

    public RatingResponse(Long id, Long bookingId, Long ratedUser, Long ratingUser, Integer rating, String review, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.ratedUser = ratedUser;
        this.ratingUser = ratingUser;
        this.rating = rating;
        this.review = review;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getRatedUser() {
        return ratedUser;
    }

    public void setRatedUser(Long ratedUser) {
        this.ratedUser = ratedUser;
    }

    public Long getRatingUser() {
        return ratingUser;
    }

    public void setRatingUser(Long ratingUser) {
        this.ratingUser = ratingUser;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
