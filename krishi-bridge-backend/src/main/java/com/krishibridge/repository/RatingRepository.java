package com.krishibridge.repository;

import com.krishibridge.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByBookingId(Long bookingId);
    List<Rating> findByRatedUser(Long ratedUser);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser = :userId")
    Double getAverageRatingForUser(@Param("userId") Long userId);
}
