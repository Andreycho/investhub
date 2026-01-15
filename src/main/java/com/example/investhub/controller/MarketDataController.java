package com.example.investhub.controller;

import com.example.investhub.mapper.DtoMapper;
import com.example.investhub.model.Asset;
import com.example.investhub.model.dto.response.AssetResponse;
import com.example.investhub.model.dto.response.PriceResponse;
import com.example.investhub.service.MarketDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling market data and cryptocurrency prices.
 */
@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataService marketDataService;
    private final DtoMapper dtoMapper;

    public MarketDataController(MarketDataService marketDataService, DtoMapper dtoMapper) {
        this.marketDataService = marketDataService;
        this.dtoMapper = dtoMapper;
    }

    /**
     * Get current prices for all tracked cryptocurrencies.
     *
     * @return List of PriceResponse (symbol + price)
     */
    @GetMapping("/prices")
    public ResponseEntity<List<PriceResponse>> getCurrentPrices() {
        var pricesMap = marketDataService.getCurrentPrices();

        List<PriceResponse> response = pricesMap.entrySet().stream()
                .map(e -> new PriceResponse(e.getKey(), e.getValue()))
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get the current price of a specific cryptocurrency.
     *
     * @param symbol The cryptocurrency symbol
     * @return PriceResponse (symbol + price)
     */
    @GetMapping("/prices/{symbol}")
    public ResponseEntity<PriceResponse> getPriceBySymbol(@PathVariable String symbol) {
        Double price = marketDataService.getPriceBySymbol(symbol);
        return ResponseEntity.ok(new PriceResponse(symbol, price));
    }

    /**
     * Get all available assets/cryptocurrencies.
     *
     * @return List of all available assets (DTO)
     */
    @GetMapping("/assets")
    public ResponseEntity<List<AssetResponse>> getAllAssets() {
        List<Asset> assets = marketDataService.getAllAssets();

        List<AssetResponse> response = assets.stream()
                .map(dtoMapper::toAssetResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed information about a specific asset.
     *
     * @param symbol The asset symbol
     * @return The asset details (DTO)
     */
    @GetMapping("/assets/{symbol}")
    public ResponseEntity<AssetResponse> getAssetBySymbol(@PathVariable String symbol) {
        Asset asset = marketDataService.getAssetBySymbol(symbol);
        return ResponseEntity.ok(dtoMapper.toAssetResponse(asset));
    }

    /**
     * Search for assets by name or symbol.
     *
     * @param query The search query
     * @return List of matching assets (DTO)
     */
    @GetMapping("/assets/search")
    public ResponseEntity<List<AssetResponse>> searchAssets(@RequestParam String query) {
        List<Asset> assets = marketDataService.searchAssets(query);

        List<AssetResponse> response = assets.stream()
                .map(dtoMapper::toAssetResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}