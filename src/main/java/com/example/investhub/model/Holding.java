package com.example.investhub.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "holdings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "asset_id"}))
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(BigDecimal avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }
}