package com.krishibridge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "booking_matches")
@IdClass(BookingMatch.BookingMatchId.class)
public class BookingMatch {

    @Id
    @Column(name = "booking_id")
    private Long bookingId;

    @Id
    @Column(name = "transporter_id")
    private Long transporterId;

    @Column(name = "match_status", nullable = false, length = 20)
    private String matchStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public BookingMatch() {}

    public BookingMatch(Long bookingId, Long transporterId, String matchStatus) {
        this.bookingId = bookingId;
        this.transporterId = transporterId;
        this.matchStatus = matchStatus;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(Long transporterId) {
        this.transporterId = transporterId;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class BookingMatchId implements Serializable {
        private Long bookingId;
        private Long transporterId;

        public BookingMatchId() {}

        public BookingMatchId(Long bookingId, Long transporterId) {
            this.bookingId = bookingId;
            this.transporterId = transporterId;
        }

        public Long getBookingId() {
            return bookingId;
        }

        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }

        public Long getTransporterId() {
            return transporterId;
        }

        public void setTransporterId(Long transporterId) {
            this.transporterId = transporterId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BookingMatchId that = (BookingMatchId) o;
            return Objects.equals(bookingId, that.bookingId) &&
                    Objects.equals(transporterId, that.transporterId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bookingId, transporterId);
        }
    }
}
