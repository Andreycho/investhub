package com.example.investhub.service;

import com.example.investhub.exception.ResourceAlreadyExistsException;
import com.example.investhub.exception.ResourceNotFoundException;
import com.example.investhub.model.Asset;
import com.example.investhub.model.User;
import com.example.investhub.model.WatchlistEntry;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.repository.WatchlistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user watchlists.
 */
@Service
@Slf4j
@Transactional
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    public WatchlistService(WatchlistRepository watchlistRepository,
                           UserRepository userRepository,
                           AssetRepository assetRepository) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.assetRepository = assetRepository;
    }

    /**
     * Get user's watchlist.
     *
     * @param username The username
     * @return List of assets in the watchlist
     */
    public List<Asset> getUserWatchlist(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        return watchlistRepository.findByUser_Id(user.getId()).stream()
                .map(WatchlistEntry::getAsset)
                .collect(Collectors.toList());
    }

    /**
     * Add an asset to the user's watchlist.
     *
     * @param assetSymbol The asset symbol
     * @param username The username
     * @return The created watchlist entry
     */
    public WatchlistEntry addToWatchlist(String assetSymbol, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        Asset asset = assetRepository.findBySymbol(assetSymbol)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetSymbol));

        List<WatchlistEntry> existingEntries = watchlistRepository.findByUser_Id(user.getId());
        boolean alreadyExists = existingEntries.stream()
                .anyMatch(entry -> entry.getAsset().getSymbol().equals(assetSymbol));

        if (alreadyExists) {
            throw new ResourceAlreadyExistsException("Watchlist entry", assetSymbol);
        }

        WatchlistEntry entry = new WatchlistEntry();
        entry.setUser(user);
        entry.setAsset(asset);

        return watchlistRepository.save(entry);
    }

    /**
     * Remove an asset from the user's watchlist.
     *
     * @param assetSymbol The asset symbol
     * @param username The username
     */
    public void removeFromWatchlist(String assetSymbol, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WatchlistEntry> entries = watchlistRepository.findByUser_Id(user.getId());
        WatchlistEntry entryToRemove = entries.stream()
                .filter(entry -> entry.getAsset().getSymbol().equals(assetSymbol))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asset not in watchlist"));

        watchlistRepository.delete(entryToRemove);
    }

    /**
     * Check if an asset is in the user's watchlist.
     *
     * @param assetSymbol The asset symbol
     * @param username The username
     * @return True if in watchlist, false otherwise
     */
    public boolean isInWatchlist(String assetSymbol, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WatchlistEntry> entries = watchlistRepository.findByUser_Id(user.getId());
        return entries.stream()
                .anyMatch(entry -> entry.getAsset().getSymbol().equals(assetSymbol));
    }
}

