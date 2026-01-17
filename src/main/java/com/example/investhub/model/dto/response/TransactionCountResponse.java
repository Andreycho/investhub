package com.example.investhub.model.dto.response;

public class TransactionCountResponse {
    private String assetSymbol;
    private Long transactionCount;

    public TransactionCountResponse() {}

    public TransactionCountResponse(String assetSymbol, Long transactionCount) {
        this.assetSymbol = assetSymbol;
        this.transactionCount = transactionCount;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
