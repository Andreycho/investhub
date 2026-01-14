package com.example.investhub.model.dto.response;

import java.time.Instant;

public class TransactionResponse {
    private Long id;
    private String type;
    private String assetSymbol;
    private double quantity;
    private double pricePerUnit;
    private Instant timestamp;

    public TransactionResponse() {}

    public TransactionResponse(Long id, String type, String assetSymbol,
                               double quantity, double pricePerUnit, Instant timestamp) {
        this.id = id;
        this.type = type;
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}