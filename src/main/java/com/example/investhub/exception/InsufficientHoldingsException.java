package com.example.investhub.exception;

/**
 * Exception thrown when user tries to sell more assets than they own.
 */
public class InsufficientHoldingsException extends RuntimeException {

    private final String assetSymbol;
    private final double owned;
    private final double requested;

    public InsufficientHoldingsException(String assetSymbol, double owned, double requested) {
        super(String.format("Insufficient holdings. You own %.8f %s but trying to sell %.8f",
            owned, assetSymbol, requested));
        this.assetSymbol = assetSymbol;
        this.owned = owned;
        this.requested = requested;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public double getOwned() {
        return owned;
    }

    public double getRequested() {
        return requested;
    }
}

