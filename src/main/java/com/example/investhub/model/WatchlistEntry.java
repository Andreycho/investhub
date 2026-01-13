package com.example.investhub.model;

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
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
}