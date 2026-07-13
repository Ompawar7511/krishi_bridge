package com.krishibridge.controller;

import com.krishibridge.dto.request.OtpSendRequest;
import com.krishibridge.dto.request.OtpVerifyRequest;
import com.krishibridge.dto.response.StandardResponse;
import com.krishibridge.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<StandardResponse<String>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        otpService.sendOtp(request.getPhoneNumber());
        return ResponseEntity.ok(StandardResponse.success("OTP sent successfully", "OTP dispatched to phone"));
    }

    @PostMapping("/verify")
    public ResponseEntity<StandardResponse<String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        otpService.verifyOtp(request.getPhoneNumber(), request.getOtpCode());
        return ResponseEntity.ok(StandardResponse.success("OTP verified successfully and account processed", "Verification complete"));
    }
}
