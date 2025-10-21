package com.deliverytech.delivery.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationException {

    private int status;
    private String error;
    private Map<String, String> validationErrors;
    private LocalDateTime timestamp;

    public ValidationException(int status, String error, Map<String, String> validationErrors, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.validationErrors = validationErrors;
        this.timestamp = timestamp;
    }

    // Getters e Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
