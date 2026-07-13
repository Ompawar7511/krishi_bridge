package com.krishibridge.controller;

import com.krishibridge.dto.request.LoginRequest;
import com.krishibridge.dto.request.RegisterRequest;
import com.krishibridge.dto.response.AuthResponse;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.entity.User;
import com.krishibridge.service.AuthService;
import com.krishibridge.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    public AuthController(AuthService authService, CookieUtil cookieUtil) {
        this.authService = authService;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse responseDto = authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(responseDto, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletResponse response) {
        
        AuthService.TokenPair tokenPair = authService.loginUser(request);
        cookieUtil.setTokenCookies(response, tokenPair.getAccessToken(), tokenPair.getRefreshToken());

        User user = tokenPair.getUser();
        AuthResponse responseDto = new AuthResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                "Login successful"
        );

        return ResponseEntity.ok(StandardResponse.success(responseDto, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(StandardResponse.failure("Refresh token cookie is missing"));
        }

        AuthService.TokenPair tokenPair = authService.refreshAccessToken(refreshToken);
        cookieUtil.setTokenCookies(response, tokenPair.getAccessToken(), tokenPair.getRefreshToken());

        User user = tokenPair.getUser();
        AuthResponse responseDto = new AuthResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                "Token refreshed successfully"
        );

        return ResponseEntity.ok(StandardResponse.success(responseDto, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<String>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        
        authService.logoutUser(refreshToken);
        cookieUtil.clearTokenCookies(response);

        return ResponseEntity.ok(StandardResponse.success("Logout successful", "Logout successful"));
    }
}
