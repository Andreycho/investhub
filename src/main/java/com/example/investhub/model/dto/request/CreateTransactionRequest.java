package com.example.investhub.model.dto.request;

import com.example.investhub.model.enumeration.TransactionType;

/**
 * DTO for creating a transaction request.
 */
public class CreateTransactionRequest {

    private TransactionType type;
    private String assetSymbol;
    private double quantity;

    public CreateTransactionRequest() {
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}

