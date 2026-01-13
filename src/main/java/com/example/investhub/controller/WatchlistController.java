package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.model.WatchlistEntry;
import com.example.investhub.service.WatchlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling watchlist operations.
 */
@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    /**
     * Get the authenticated user's watchlist.
     *
     * @param userDetails The authenticated user details
     * @return List of assets in the watchlist
     */
    @GetMapping
    public ResponseEntity<List<Asset>> getWatchlist(@AuthenticationPrincipal UserDetails userDetails) {
        List<Asset> watchlist = watchlistService.getUserWatchlist(userDetails.getUsername());
        return ResponseEntity.ok(watchlist);
    }

    /**
     * Add an asset to the watchlist.
     *
     * @param assetSymbol The symbol of the asset to add
     * @param userDetails The authenticated user details
     * @return The created watchlist entry
     */
    @PostMapping("/{assetSymbol}")
    public ResponseEntity<WatchlistEntry> addToWatchlist(@PathVariable String assetSymbol, @AuthenticationPrincipal UserDetails userDetails) {
        WatchlistEntry entry = watchlistService.addToWatchlist(assetSymbol, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    /**
     * Remove an asset from the watchlist.
     *
     * @param assetSymbol The symbol of the asset to remove
     * @param userDetails The authenticated user details
     * @return No content response
     */
    @DeleteMapping("/{assetSymbol}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String assetSymbol, @AuthenticationPrincipal UserDetails userDetails) {
        watchlistService.removeFromWatchlist(assetSymbol, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if an asset is in the user's watchlist.
     *
     * @param assetSymbol The symbol to check
     * @param userDetails The authenticated user details
     * @return Boolean indicating if the asset is in the watchlist
     */
    @GetMapping("/contains/{assetSymbol}")
    public ResponseEntity<Boolean> isInWatchlist(@PathVariable String assetSymbol, @AuthenticationPrincipal UserDetails userDetails) {
        boolean isInWatchlist = watchlistService.isInWatchlist(assetSymbol, userDetails.getUsername());
        return ResponseEntity.ok(isInWatchlist);
    }
}

