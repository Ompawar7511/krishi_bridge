package com.krishibridge.exception;

public class OtpMaxAttemptsReachedException extends RuntimeException {
    public OtpMaxAttemptsReachedException(String message) {
        super(message);
    }
}
