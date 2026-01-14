package com.example.investhub.model.dto.response;

public class PriceResponse {
    private String symbol;
    private double price;

    public PriceResponse() {}

    public PriceResponse(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}