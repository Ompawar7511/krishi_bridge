package com.krishibridge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"booking_id"})
})
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "rated_user", nullable = false)
    private Long ratedUser;

    @Column(name = "rating_user", nullable = false)
    private Long ratingUser;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Rating() {}

    public Rating(Long bookingId, Long ratedUser, Long ratingUser, Integer rating, String review) {
        this.bookingId = bookingId;
        this.ratedUser = ratedUser;
        this.ratingUser = ratingUser;
        this.rating = rating;
        this.review = review;
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
