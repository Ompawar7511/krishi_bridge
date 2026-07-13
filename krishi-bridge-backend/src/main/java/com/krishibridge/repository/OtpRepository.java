package com.krishibridge.repository;

import com.krishibridge.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
