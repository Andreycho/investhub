package com.example.investhub.model.dto;

import java.math.BigDecimal;

public class AuthResponse {
    private String token;
    private long expiresIn;
    private Long userId;
    private String username;
    private BigDecimal usdBalance;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn, Long userId, String username, BigDecimal usdBalance) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.username = username;
        this.usdBalance = usdBalance;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public BigDecimal getUsdBalance() { return usdBalance; }
    public void setUsdBalance(BigDecimal usdBalance) { this.usdBalance = usdBalance; }
}