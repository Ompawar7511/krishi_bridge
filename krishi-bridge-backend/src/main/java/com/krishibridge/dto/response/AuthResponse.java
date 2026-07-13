package com.krishibridge.dto.response;

import com.krishibridge.enums.Role;
import com.krishibridge.enums.UserStatus;

public class AuthResponse {
    private Long userId;
    private String name;
    private String phoneNumber;
    private Role role;
    private UserStatus status;
    private String message;

    public AuthResponse() {}

    public AuthResponse(Long userId, String name, String phoneNumber, Role role, UserStatus status, String message) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
