package com.krishibridge.repository;

import com.krishibridge.entity.BookingMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingMatchRepository extends JpaRepository<BookingMatch, BookingMatch.BookingMatchId> {
    List<BookingMatch> findByBookingId(Long bookingId);
    List<BookingMatch> findByTransporterIdAndMatchStatus(Long transporterId, String matchStatus);
    Optional<BookingMatch> findByBookingIdAndTransporterId(Long bookingId, Long transporterId);
}
