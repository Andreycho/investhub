package com.example.investhub.service;

import com.example.investhub.model.Asset;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.websocket.BinanceWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for managing market data and asset information.
 */
@Service
@Slf4j
public class MarketDataService {

    private final BinanceWebSocketService binanceWebSocketService;
    private final AssetRepository assetRepository;

    public MarketDataService(BinanceWebSocketService binanceWebSocketService, AssetRepository assetRepository) {
        this.binanceWebSocketService = binanceWebSocketService;
        this.assetRepository = assetRepository;
    }

    /**
     * Get current prices for all cryptocurrencies.
     *
     * @return Map of symbols to prices
     */
    public Map<String, Double> getCurrentPrices() {
        return binanceWebSocketService.getCryptoPrices();
    }

    /**
     * Get current price for a specific cryptocurrency.
     *
     * @param symbol The cryptocurrency symbol
     * @return The current price
     */
    public Double getPriceBySymbol(String symbol) {
        Map<String, Double> prices = binanceWebSocketService.getCryptoPrices();
        Double price = prices.get(symbol.toUpperCase());

        if (price == null) {
            throw new RuntimeException("Price not available for symbol: " + symbol);
        }

        return price;
    }

    /**
     * Get all available assets.
     *
     * @return List of all assets
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    /**
     * Get asset by symbol.
     *
     * @param symbol The asset symbol
     * @return The asset
     */
    public Asset getAssetBySymbol(String symbol) {
        return assetRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + symbol));
    }

    /**
     * Search assets by name or symbol.
     *
     * @param query The search query
     * @return List of matching assets
     */
    public List<Asset> searchAssets(String query) {
        String upperQuery = query.toUpperCase();

        return assetRepository.findAll().stream()
                .filter(asset ->
                    asset.getSymbol().toUpperCase().contains(upperQuery) ||
                    asset.getName().toUpperCase().contains(upperQuery)
                )
                .toList();
    }
}

