package com.example.investhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal usdBalance = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public BigDecimal getUsdBalance() { return usdBalance; }
    public void setUsdBalance(BigDecimal usdBalance) { this.usdBalance = usdBalance; }
}