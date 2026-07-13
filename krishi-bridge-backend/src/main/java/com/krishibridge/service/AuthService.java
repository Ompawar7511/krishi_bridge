package com.krishibridge.service;

import com.krishibridge.dto.request.LoginRequest;
import com.krishibridge.dto.request.RegisterRequest;
import com.krishibridge.dto.response.AuthResponse;
import com.krishibridge.entity.RefreshToken;
import com.krishibridge.entity.User;
import com.krishibridge.enums.UserStatus;
import com.krishibridge.exception.InvalidCredentialsException;
import com.krishibridge.exception.InvalidTokenException;
import com.krishibridge.exception.UserAlreadyExistsException;
import com.krishibridge.repository.RefreshTokenRepository;
import com.krishibridge.repository.UserRepository;
import com.krishibridge.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            // Audit Log Hook: Registration Failed
            log.warn("AUDIT_LOG | REGISTER_FAILED | Phone: {} | Reason: User already exists", request.getPhoneNumber());
            throw new UserAlreadyExistsException("User with this phone number already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(UserStatus.APPROVED);

        User savedUser = userRepository.save(user);

        // Audit Log Hook: Registration Successful
        log.info("AUDIT_LOG | REGISTER_SUCCESS | UserId: {} | Role: {}", savedUser.getId(), savedUser.getRole());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getPhoneNumber(),
                savedUser.getRole(),
                savedUser.getStatus(),
                "Registration successful. Verification pending."
        );
    }

    @Transactional
    public TokenPair loginUser(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(request.getPhoneNumber());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            // Audit Log Hook: Failed Login
            log.warn("AUDIT_LOG | LOGIN_FAILED | Phone: {} | Reason: Invalid credentials", request.getPhoneNumber());
            throw new InvalidCredentialsException("Invalid phone number or password");
        }

        User user = userOpt.get();

        if (user.getStatus() == UserStatus.BLOCKED) {
            // Audit Log Hook: Blocked Login attempt
            log.warn("AUDIT_LOG | LOGIN_FAILED | UserId: {} | Reason: Account blocked", user.getId());
            throw new InvalidCredentialsException("Account is blocked. Please contact support.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String refreshTokenString = UUID.randomUUID().toString();

        // Revoke existing refresh tokens for user (token rotation security)
        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setToken(refreshTokenString);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        // Audit Log Hook: Successful Login
        log.info("AUDIT_LOG | LOGIN_SUCCESS | UserId: {} | Role: {}", user.getId(), user.getRole());

        return new TokenPair(accessToken, refreshTokenString, user);
    }

    @Transactional
    public TokenPair refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> {
                    log.warn("AUDIT_LOG | REFRESH_FAILED | Token not found in database");
                    return new InvalidTokenException("Invalid refresh token");
                });

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            log.warn("AUDIT_LOG | REFRESH_FAILED | UserId: {} | Reason: Token expired", refreshToken.getUserId());
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new InvalidTokenException("User account is blocked");
        }

        // Generate new token pair (Refresh Token Rotation)
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole().name());
        String newRefreshTokenString = UUID.randomUUID().toString();

        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUserId(user.getId());
        newRefreshToken.setToken(newRefreshTokenString);
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(newRefreshToken);

        log.info("AUDIT_LOG | TOKEN_REFRESHED | UserId: {}", user.getId());

        return new TokenPair(newAccessToken, newRefreshTokenString, user);
    }

    @Transactional
    public void logoutUser(String refreshTokenString) {
        if (refreshTokenString != null) {
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
            if (tokenOpt.isPresent()) {
                Long userId = tokenOpt.get().getUserId();
                refreshTokenRepository.deleteByToken(refreshTokenString);
                // Audit Log Hook: Logout
                log.info("AUDIT_LOG | LOGOUT_SUCCESS | UserId: {}", userId);
                return;
            }
        }
        log.warn("AUDIT_LOG | LOGOUT_FAILED | Reason: Invalid or null refresh token");
    }

    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;
        private final User user;

        public TokenPair(String accessToken, String refreshToken, User user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public User getUser() { return user; }
    }
}
