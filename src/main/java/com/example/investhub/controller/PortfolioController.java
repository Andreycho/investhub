package com.example.investhub.controller;

import com.example.investhub.model.Holding;
import com.example.investhub.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling portfolio and user account operations.
 */
@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Get the authenticated user's complete portfolio.
     *
     * @param userDetails The authenticated user details
     * @return Portfolio details including balance and holdings
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPortfolio(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> portfolio = portfolioService.getUserPortfolio(userDetails.getUsername());
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Get the authenticated user's USD balance.
     *
     * @param userDetails The authenticated user details
     * @return Current USD balance
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        BigDecimal balance = portfolioService.getUserBalance(userDetails.getUsername());
        return ResponseEntity.ok(balance);
    }

    /**
     * Get all cryptocurrency holdings for the authenticated user.
     *
     * @param userDetails The authenticated user details
     * @return List of holdings
     */
    @GetMapping("/holdings")
    public ResponseEntity<List<Holding>> getHoldings(@AuthenticationPrincipal UserDetails userDetails) {
        List<Holding> holdings = portfolioService.getUserHoldings(userDetails.getUsername());
        return ResponseEntity.ok(holdings);
    }

    /**
     * Get a specific holding by asset symbol.
     *
     * @param symbol The crypto symbol
     * @param userDetails The authenticated user details
     * @return The holding details
     */
    @GetMapping("/holdings/{symbol}")
    public ResponseEntity<Holding> getHoldingBySymbol(@PathVariable String symbol, @AuthenticationPrincipal UserDetails userDetails) {
        Holding holding = portfolioService.getHoldingBySymbol(symbol, userDetails.getUsername());
        return ResponseEntity.ok(holding);
    }

    /**
     * Get portfolio statistics (total value, profit/loss, etc.).
     *
     * @param userDetails The authenticated user details
     * @return Portfolio statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPortfolioStats(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> stats = portfolioService.getPortfolioStatistics(userDetails.getUsername());
        return ResponseEntity.ok(stats);
    }
}

