package com.example.investhub.controller;

import com.example.investhub.mapper.DtoMapper;
import com.example.investhub.model.Asset;
import com.example.investhub.model.WatchlistEntry;
import com.example.investhub.model.dto.response.WatchlistAssetResponse;
import com.example.investhub.service.WatchlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final DtoMapper dtoMapper;

    public WatchlistController(WatchlistService watchlistService, DtoMapper dtoMapper) {
        this.watchlistService = watchlistService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<WatchlistAssetResponse>> getWatchlist(@AuthenticationPrincipal UserDetails userDetails) {
        List<Asset> watchlist = watchlistService.getUserWatchlist(userDetails.getUsername());

        List<WatchlistAssetResponse> response = watchlist.stream()
                .map(dtoMapper::toWatchlistAssetResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{assetSymbol}")
    public ResponseEntity<WatchlistAssetResponse> addToWatchlist(
            @PathVariable String assetSymbol,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        WatchlistEntry entry = watchlistService.addToWatchlist(assetSymbol, userDetails.getUsername());

        Asset asset = entry.getAsset();
        WatchlistAssetResponse response = (asset != null)
                ? dtoMapper.toWatchlistAssetResponse(asset)
                : new WatchlistAssetResponse(null, assetSymbol, null);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{assetSymbol}")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable String assetSymbol,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        watchlistService.removeFromWatchlist(assetSymbol, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contains/{assetSymbol}")
    public ResponseEntity<Boolean> isInWatchlist(
            @PathVariable String assetSymbol,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        boolean isInWatchlist = watchlistService.isInWatchlist(assetSymbol, userDetails.getUsername());
        return ResponseEntity.ok(isInWatchlist);
    }
}