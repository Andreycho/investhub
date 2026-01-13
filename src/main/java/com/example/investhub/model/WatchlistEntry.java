package com.example.investhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "watchlist",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "asset_id"}))
public class WatchlistEntry {

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

    @JsonProperty("assetName")
    public String getAssetName() {
        return asset != null ? asset.getName() : null;
    }
}