package com.example.investhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "holdings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "asset_id"}))
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    @JsonIgnore
    private Asset asset;

    @Column(nullable = false)
    private double quantity = 0.0;

    @Column(nullable = false)
    private double avgBuyPrice = 0.0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    @JsonProperty("assetSymbol")
    public String getAssetSymbol() {
        return asset != null ? asset.getSymbol() : null;
    }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(double avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }
}
