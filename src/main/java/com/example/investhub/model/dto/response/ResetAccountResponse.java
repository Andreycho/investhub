package com.example.investhub.model.dto.response;

import java.math.BigDecimal;

public class ResetAccountResponse {
    private BigDecimal usdBalance;
    private String message;

    public ResetAccountResponse() {}

    public ResetAccountResponse(BigDecimal usdBalance, String message) {
        this.usdBalance = usdBalance;
        this.message = message;
    }

    public BigDecimal getUsdBalance() { return usdBalance; }
    public void setUsdBalance(BigDecimal usdBalance) { this.usdBalance = usdBalance; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}