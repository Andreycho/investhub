package com.example.investhub.service;

import com.example.investhub.exception.InsufficientHoldingsException;
import com.example.investhub.model.Asset;
import com.example.investhub.model.Holding;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.User;
import com.example.investhub.model.enumeration.PerformanceStatus;
import com.example.investhub.model.enumeration.TransactionType;
import com.example.investhub.repository.HoldingRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.websocket.BinanceWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user portfolios.
 */
@Service
@Slf4j
public class PortfolioService {

    private final UserRepository userRepository;
    private final BinanceWebSocketService binanceWebSocketService;
    private final HoldingRepository holdingRepository;

    public PortfolioService(UserRepository userRepository, BinanceWebSocketService binanceWebSocketService, HoldingRepository holdingRepository) {
        this.userRepository = userRepository;
        this.binanceWebSocketService = binanceWebSocketService;
        this.holdingRepository = holdingRepository;
    }

    /**
     * Get complete portfolio for a user.
     *
     * @param username The username
     * @return Portfolio details
     */
    public Map<String, Object> getUserPortfolio(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("balance", user.getUsdBalance());
        portfolio.put("holdings", getUserHoldings(username));
        portfolio.put("totalValue", calculateTotalPortfolioValue(username));

        return portfolio;
    }

    /**
     * Get user's USD balance.
     *
     * @param username The username
     * @return USD balance
     */
    public BigDecimal getUserBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getUsdBalance();
    }

    /**
     * Get all holdings for a user.
     *
     * @param username The username
     * @return List of holdings
     */
    public List<Holding> getUserHoldings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return holdingRepository.findByUser_Id(user.getId()).stream()
                .filter(h -> h.getQuantity() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific holding by symbol.
     *
     * @param symbol The asset symbol
     * @param username The username
     * @return The holding
     */
    public Holding getHoldingBySymbol(String symbol, String username) {
        List<Holding> holdings = getUserHoldings(username);
        return holdings.stream()
                .filter(h -> h.getAssetSymbol().equalsIgnoreCase(symbol))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No holding found for symbol: " + symbol));
    }

    /**
     * Get portfolio statistics.
     *
     * @param username The username
     * @return Portfolio statistics
     */
    public Map<String, Object> getPortfolioStatistics(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Holding> holdings = getUserHoldings(username);
        Map<String, Double> currentPrices = binanceWebSocketService.getCryptoPrices();

        double totalInvested = 0;
        double currentValue = 0;

        for (Holding holding : holdings) {
            double invested = holding.getQuantity() * holding.getAvgBuyPrice();
            totalInvested += invested;

            Double currentPrice = currentPrices.get(holding.getAssetSymbol());
            if (currentPrice != null) {
                currentValue += holding.getQuantity() * currentPrice;
            }
        }

        double netGain = currentValue - totalInvested;
        double returnPercentage = totalInvested > 0 ? (netGain / totalInvested) * 100 : 0;

        PerformanceStatus performanceStatus;
        if (netGain > 0) {
            performanceStatus = PerformanceStatus.WINNING;
        } else if (netGain < 0) {
            performanceStatus = PerformanceStatus.LOSING;
        } else {
            performanceStatus = PerformanceStatus.BREAK_EVEN;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPortfolioValue", currentValue + user.getUsdBalance().doubleValue());
        stats.put("usdBalance", user.getUsdBalance());
        stats.put("totalInvested", totalInvested);
        stats.put("currentValue", currentValue);
        stats.put("netGain", netGain);
        stats.put("returnPercentage", returnPercentage);
        stats.put("performanceStatus", performanceStatus);
        stats.put("holdingsCount", holdings.size());

        return stats;
    }

    /**
     * Calculate total portfolio value (balance + holdings).
     *
     * @param username The username
     * @return Total value
     */
    private double calculateTotalPortfolioValue(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Holding> holdings = getUserHoldings(username);
        Map<String, Double> currentPrices = binanceWebSocketService.getCryptoPrices();

        double holdingsValue = holdings.stream()
                .mapToDouble(h -> {
                    Double price = currentPrices.get(h.getAssetSymbol());
                    return price != null ? h.getQuantity() * price : 0;
                })
                .sum();

        return user.getUsdBalance().doubleValue() + holdingsValue;
    }

    /**
     * Validate that a user owns enough of an asset to sell.
     *
     * @param userId The user ID
     * @param assetSymbol The asset symbol
     * @param quantityToSell The quantity user wants to sell
     * @throws RuntimeException if user doesn't own enough
     */
    public void validateUserCanSell(Long userId, String assetSymbol, double quantityToSell) {
        Optional<Holding> holdingOpt = holdingRepository
                .findByUser_IdAndAsset_Symbol(userId, assetSymbol);

        if (holdingOpt.isEmpty()) {
            throw new InsufficientHoldingsException(assetSymbol, 0, quantityToSell);
        }

        Holding holding = holdingOpt.get();
        if (holding.getQuantity() < quantityToSell) {
            throw new InsufficientHoldingsException(assetSymbol, holding.getQuantity(), quantityToSell);
        }
    }

    /**
     * Update holdings when a transaction occurs.
     * This should be called after every BUY/SELL transaction.
     *
     * @param transaction The transaction that was created
     * @param user The user who made the transaction
     */
    public void updateHoldingAfterTransaction(Transaction transaction, User user) {
        Asset asset = transaction.getAsset();

        Optional<Holding> existingHolding = holdingRepository
                .findByUser_IdAndAsset_Symbol(user.getId(), asset.getSymbol());

        Holding holding;
        if (existingHolding.isPresent()) {
            holding = existingHolding.get();
        } else {
            holding = new Holding();
            holding.setUser(user);
            holding.setAsset(asset);
        }

        if (transaction.getType() == TransactionType.BUY) {
            double currentQty = holding.getQuantity();
            double currentAvg = holding.getAvgBuyPrice();
            double newQuantity = currentQty + transaction.getQuantity();
            double totalCost = (currentQty * currentAvg) + (transaction.getQuantity() * transaction.getPricePerUnit());

            holding.setQuantity(newQuantity);
            holding.setAvgBuyPrice(newQuantity > 0 ? totalCost / newQuantity : 0);
        } else if (transaction.getType() == TransactionType.SELL) {
            holding.setQuantity(holding.getQuantity() - transaction.getQuantity());
        }

        if (holding.getQuantity() > 0) {
            holdingRepository.save(holding);
        } else if (holding.getId() != null) {
            holdingRepository.delete(holding);
        }
    }
}
