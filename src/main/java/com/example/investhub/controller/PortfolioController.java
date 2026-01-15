package com.example.investhub.controller;

import com.example.investhub.mapper.DtoMapper;
import com.example.investhub.model.Holding;
import com.example.investhub.model.dto.response.BalanceResponse;
import com.example.investhub.model.dto.response.HoldingResponse;
import com.example.investhub.model.dto.response.PortfolioResponse;
import com.example.investhub.model.dto.response.PortfolioStatsResponse;
import com.example.investhub.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final DtoMapper dtoMapper;

    public PortfolioController(PortfolioService portfolioService, DtoMapper dtoMapper) {
        this.portfolioService = portfolioService;
        this.dtoMapper = dtoMapper;
    }

    /**
     * Get the authenticated user's complete portfolio (DTO).
     * Returns balance + holdings list.
     */
    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        BigDecimal balance = portfolioService.getUserBalance(username);
        List<Holding> holdings = portfolioService.getUserHoldings(username);

        List<HoldingResponse> holdingDtos = holdings.stream()
                .map(dtoMapper::toHoldingResponse)
                .toList();

        return ResponseEntity.ok(new PortfolioResponse(balance, holdingDtos));
    }

    /**
     * Get the authenticated user's USD balance (DTO).
     */
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        BigDecimal balance = portfolioService.getUserBalance(userDetails.getUsername());
        return ResponseEntity.ok(new BalanceResponse(balance));
    }

    /**
     * Get all holdings (DTO).
     */
    @GetMapping("/holdings")
    public ResponseEntity<List<HoldingResponse>> getHoldings(@AuthenticationPrincipal UserDetails userDetails) {
        List<Holding> holdings = portfolioService.getUserHoldings(userDetails.getUsername());

        List<HoldingResponse> response = holdings.stream()
                .map(dtoMapper::toHoldingResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific holding by asset symbol (DTO).
     */
    @GetMapping("/holdings/{symbol}")
    public ResponseEntity<HoldingResponse> getHoldingBySymbol(
            @PathVariable String symbol,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Holding holding = portfolioService.getHoldingBySymbol(symbol, userDetails.getUsername());
        return ResponseEntity.ok(dtoMapper.toHoldingResponse(holding));
    }

    /**
     * Get portfolio statistics (DTO).
     */
    @GetMapping("/stats")
    public ResponseEntity<PortfolioStatsResponse> getPortfolioStats(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> stats = portfolioService.getPortfolioStatistics(userDetails.getUsername());
        return ResponseEntity.ok(new PortfolioStatsResponse(stats));
    }
}