package com.example.investhub.model.dto.response;

public class HoldingResponse {
    private Long id;
    private String assetSymbol;
    private double quantity;
    private double avgBuyPrice;

    public HoldingResponse() {}

    public HoldingResponse(Long id, String assetSymbol, double quantity, double avgBuyPrice) {
        this.id = id;
        this.assetSymbol = assetSymbol;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(double avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }
}