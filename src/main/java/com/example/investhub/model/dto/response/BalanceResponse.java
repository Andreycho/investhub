package com.example.investhub.model.dto.response;

import java.math.BigDecimal;

public class BalanceResponse {
    private BigDecimal usdBalance;

    public BalanceResponse() {}

    public BalanceResponse(BigDecimal usdBalance) {
        this.usdBalance = usdBalance;
    }

    public BigDecimal getUsdBalance() {
        return usdBalance;
    }

    public void setUsdBalance(BigDecimal usdBalance) {
        this.usdBalance = usdBalance;
    }
}