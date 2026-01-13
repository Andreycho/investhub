package com.example.investhub;

import com.example.investhub.model.Transaction;
import com.example.investhub.service.TradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    /**
     * Endpoint to get the latest cryptocurrency prices.
     *
     * @return A map of cryptocurrency symbols to their latest prices.
     */
    @GetMapping("/prices")
    public ResponseEntity<Map<String, Double>> getPrices() {
        Map<String, Double> latestPrices = tradingService.getCurrentCryptoPrices();
        return ResponseEntity.ok(latestPrices);
    }

    /**
     * Endpoint to get all transactions for a specific user.
     *
     * @param userId The ID of the user whose transactions to retrieve.
     * @return A list of transactions for the user.
     */
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = tradingService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Endpoint to get the balance for a specific user.
     *
     * @param userId The ID of the user whose balance to retrieve.
     * @return The user's current balance.
     */
    @GetMapping("/users/{userId}/balance")
    public ResponseEntity<Double> getUserBalance(@PathVariable Long userId) {
        double balance = tradingService.getUserBalance(userId);
        return ResponseEntity.ok(balance);
    }
}
