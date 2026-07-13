package com.krishibridge.repository;

import com.krishibridge.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    List<AnalyticsEvent> findByEventNameOrderByCreatedAtDesc(String eventName);
    long countByEventName(String eventName);

    @Query("SELECT COUNT(ae) FROM AnalyticsEvent ae WHERE ae.eventName = :eventName AND ae.createdAt >= :since")
    long countByEventNameAndCreatedAtAfter(@Param("eventName") String eventName, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(DISTINCT ae.userId) FROM AnalyticsEvent ae WHERE ae.createdAt >= :since")
    long countDistinctActiveUsers(@Param("since") LocalDateTime since);
}
