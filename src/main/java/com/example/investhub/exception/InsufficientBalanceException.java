package com.example.investhub.exception;

/**
 * Exception thrown when user has insufficient balance to complete a transaction.
 */
public class InsufficientBalanceException extends RuntimeException {

    private final double required;
    private final double available;

    public InsufficientBalanceException(double required, double available) {
        super(String.format("Insufficient balance. Required: $%.2f, Available: $%.2f", required, available));
        this.required = required;
        this.available = available;
    }

    public double getRequired() {
        return required;
    }

    public double getAvailable() {
        return available;
    }
}

