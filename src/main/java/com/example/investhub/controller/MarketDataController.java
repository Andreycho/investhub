package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.service.MarketDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling market data and cryptocurrency prices.
 */
@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Get current prices for all tracked cryptocurrencies.
     *
     * @return Map of cryptocurrency symbols to their current prices
     */
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getCurrentPrices() {
        Map<String, Double> prices = marketDataService.getCurrentPrices();
        return ResponseEntity.ok(prices);
    }

    /**
     * Get the current price of a specific cryptocurrency.
     *
     * @param symbol The cryptocurrency symbol
     * @return The current price
     */
    @GetMapping("/prices/{symbol}")
    public ResponseEntity<Double> getPriceBySymbol(@PathVariable String symbol) {
        Double price = marketDataService.getPriceBySymbol(symbol);
        return ResponseEntity.ok(price);
    }

    /**
     * Get all available assets/cryptocurrencies.
     *
     * @return List of all available assets
     */
    @GetMapping("/assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        List<Asset> assets = marketDataService.getAllAssets();
        return ResponseEntity.ok(assets);
    }

    /**
     * Get detailed information about a specific asset.
     *
     * @param symbol The asset symbol
     * @return The asset details
     */
    @GetMapping("/assets/{symbol}")
    public ResponseEntity<Asset> getAssetBySymbol(@PathVariable String symbol) {
        Asset asset = marketDataService.getAssetBySymbol(symbol);
        return ResponseEntity.ok(asset);
    }

    /**
     * Search for assets by name or symbol.
     *
     * @param query The search query
     * @return List of matching assets
     */
    @GetMapping("/assets/search")
    public ResponseEntity<List<Asset>> searchAssets(@RequestParam String query) {
        List<Asset> assets = marketDataService.searchAssets(query);
        return ResponseEntity.ok(assets);
    }
}

