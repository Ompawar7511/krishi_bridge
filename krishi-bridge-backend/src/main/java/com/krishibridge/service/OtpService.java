package com.krishibridge.service;

import com.krishibridge.entity.OtpVerification;
import com.krishibridge.entity.User;
import com.krishibridge.enums.Role;
import com.krishibridge.enums.UserStatus;
import com.krishibridge.exception.OtpExpiredException;
import com.krishibridge.exception.OtpInvalidException;
import com.krishibridge.exception.OtpMaxAttemptsReachedException;
import com.krishibridge.repository.OtpRepository;
import com.krishibridge.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int MAX_RETRIES = 5;
    private static final int EXPIRY_MINUTES = 5;
    private static final int LOCK_MINUTES = 15;

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(OtpRepository otpRepository, UserRepository userRepository) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendOtp(String phoneNumber) {
        // Redis-ready sliding window hook for future API rate-limiting limits
        checkRateLimit(phoneNumber);

        // Brute force protection check
        Optional<OtpVerification> lastOtpOpt = otpRepository.findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber);
        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            if (lastOtp.getLockedUntil() != null && lastOtp.getLockedUntil().isAfter(LocalDateTime.now())) {
                log.warn("OTP_LOG | SEND_FAILED | Locked Phone: {} | Expiry: {}", phoneNumber, lastOtp.getLockedUntil());
                throw new OtpMaxAttemptsReachedException("Phone number is temporarily locked. Try again later.");
            }
        }

        // Generate 6-digit cryptographically secure OTP code
        String otpCode = String.format("%06d", secureRandom.nextInt(1000000));

        OtpVerification otpVerification = new OtpVerification(
                phoneNumber,
                otpCode,
                LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)
        );

        otpRepository.save(otpVerification);

        // Dispatch Simulation (e.g. print payload)
        log.info("SMS_GATEWAY | OTP_SENT | Phone: {} | Code: {}", phoneNumber, otpCode);
        log.info("AUDIT_LOG | OTP_DISPATCHED | Phone: {}", phoneNumber);
    }

    @Transactional
    public void verifyOtp(String phoneNumber, String otpCode) {
        OtpVerification otpVerification = otpRepository.findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
                .orElseThrow(() -> new OtpInvalidException("No OTP requested for this phone number"));

        // Brute force protection check
        if (otpVerification.getLockedUntil() != null && otpVerification.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new OtpMaxAttemptsReachedException("Phone number is temporarily locked. Try again later.");
        }

        if (otpVerification.isVerified()) {
            throw new OtpInvalidException("OTP already verified");
        }

        // Check expiration
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP_LOG | VERIFY_FAILED | Phone: {} | Reason: Expired", phoneNumber);
            throw new OtpExpiredException("OTP has expired");
        }

        // Verify OTP code
        if (!otpVerification.getOtpCode().equals(otpCode)) {
            int newRetryCount = otpVerification.getRetryCount() + 1;
            otpVerification.setRetryCount(newRetryCount);

            if (newRetryCount >= MAX_RETRIES) {
                otpVerification.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                otpRepository.save(otpVerification);
                log.warn("OTP_LOG | BRUTE_FORCE_DETECTED | Locked Phone: {}", phoneNumber);
                throw new OtpMaxAttemptsReachedException("Maximum verification attempts exceeded. Locked for 15 minutes.");
            }

            otpRepository.save(otpVerification);
            log.warn("OTP_LOG | VERIFY_FAILED | Phone: {} | Attempts: {}", phoneNumber, newRetryCount);
            throw new OtpInvalidException("Invalid OTP code. Remaining attempts: " + (MAX_RETRIES - newRetryCount));
        }

        // Finalize validation
        otpVerification.setVerified(true);
        otpRepository.save(otpVerification);
        log.info("AUDIT_LOG | OTP_VERIFIED | Phone: {}", phoneNumber);

        // Account Activation check
        activateAccount(phoneNumber);
    }

    private void activateAccount(String phoneNumber) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole() == Role.FARMER) {
                user.setStatus(UserStatus.APPROVED);
                userRepository.save(user);
                log.info("AUDIT_LOG | USER_ACTIVATED | Farmer UserId: {}", user.getId());
            } else if (user.getRole() == Role.TRANSPORTER) {
                // Transporter phone is verified, but status remains PENDING for document verification reviews
                log.info("AUDIT_LOG | PHONE_VERIFIED | Transporter UserId: {} | Waiting document upload review pipeline", user.getId());
            }
        }
    }

    private void checkRateLimit(String phoneNumber) {
        // Future Redis sliding window rate-limiter check stub
        log.debug("OTP_LIMIT_CHECK | Phone: {} | Status: PASSED", phoneNumber);
    }
}
