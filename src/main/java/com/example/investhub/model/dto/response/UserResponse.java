package com.example.investhub.model.dto.response;

import java.math.BigDecimal;

public class UserResponse {
    private Long id;
    private String username;
    private BigDecimal usdBalance;

    public UserResponse() {}

    public UserResponse(Long id, String username, BigDecimal usdBalance) {
        this.id = id;
        this.username = username;
        this.usdBalance = usdBalance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public BigDecimal getUsdBalance() { return usdBalance; }
    public void setUsdBalance(BigDecimal usdBalance) { this.usdBalance = usdBalance; }
}