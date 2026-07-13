package com.krishibridge.repository;

import com.krishibridge.entity.UserMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMetricsRepository extends JpaRepository<UserMetrics, Long> {
}
