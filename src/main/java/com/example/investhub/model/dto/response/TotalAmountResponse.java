package com.example.investhub.model.dto.response;

import com.example.investhub.model.enumeration.TransactionType;

import java.math.BigDecimal;

public class TotalAmountResponse {
    private TransactionType type;
    private BigDecimal totalAmount;

    public TotalAmountResponse() {}

    public TotalAmountResponse(TransactionType type, BigDecimal totalAmount) {
        this.type = type;
        this.totalAmount = totalAmount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
