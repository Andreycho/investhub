package com.example.investhub.model.dto;

import com.example.investhub.model.Transaction;

/**
 * DTO for creating a transaction request.
 */
public class CreateTransactionRequest {

    private Transaction.Type type;
    private String assetSymbol;
    private double quantity;

    public CreateTransactionRequest() {
    }

    public Transaction.Type getType() {
        return type;
    }

    public void setType(Transaction.Type type) {
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

