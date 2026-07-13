package com.krishibridge.dto.response;

public class StandardResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public StandardResponse() {}

    public StandardResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(true, data, null);
    }

    public static <T> StandardResponse<T> success(T data, String message) {
        return new StandardResponse<>(true, data, message);
    }

    public static <T> StandardResponse<T> failure(String message) {
        return new StandardResponse<>(false, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
