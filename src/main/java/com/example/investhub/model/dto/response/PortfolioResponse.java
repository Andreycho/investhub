package com.example.investhub.model.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {
    private BigDecimal usdBalance;
    private List<HoldingResponse> holdings;

    public PortfolioResponse() {}

    public PortfolioResponse(BigDecimal usdBalance, List<HoldingResponse> holdings) {
        this.usdBalance = usdBalance;
        this.holdings = holdings;
    }

    public BigDecimal getUsdBalance() { return usdBalance; }
    public void setUsdBalance(BigDecimal usdBalance) { this.usdBalance = usdBalance; }

    public List<HoldingResponse> getHoldings() { return holdings; }
    public void setHoldings(List<HoldingResponse> holdings) { this.holdings = holdings; }
}