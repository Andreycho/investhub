package com.example.investhub.controller;

import com.example.investhub.model.Transaction;
import com.example.investhub.model.dto.CreateTransactionRequest;
import com.example.investhub.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling transaction-related operations.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Get all transactions for the authenticated user.
     *
     * @param userDetails The authenticated user details
     * @return List of user's transactions
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        List<Transaction> transactions = transactionService.getUserTransactions(userDetails.getUsername());
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get a specific transaction by ID for the authenticated user.
     *
     * @param transactionId The transaction ID
     * @param userDetails The authenticated user details
     * @return The transaction details
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long transactionId, @AuthenticationPrincipal UserDetails userDetails) {
        Transaction transaction = transactionService.getTransactionById(transactionId, userDetails.getUsername());
        return ResponseEntity.ok(transaction);
    }

    /**
     * Create a new transaction (buy/sell crypto).
     *
     * @param request The transaction request
     * @param userDetails The authenticated user details
     * @return The created transaction
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody CreateTransactionRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        log.info("=== CREATE TRANSACTION REQUEST ===");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "NULL");
        log.info("Transaction Type: {}", request.getType());
        log.info("Asset Symbol: {}", request.getAssetSymbol());
        log.info("Quantity: {}", request.getQuantity());

        if (userDetails == null) {
            log.error("UserDetails is NULL! Authentication failed!");
            return ResponseEntity.status(401).build();
        }

        try {
            Transaction createdTransaction = transactionService.createTransaction(request.getType(), request.getAssetSymbol(), request.getQuantity(), userDetails.getUsername()
            );
            log.info("Transaction created successfully: ID={}", createdTransaction.getId());
            return ResponseEntity.ok(createdTransaction);
        } catch (Exception e) {
            log.error("ERROR creating transaction: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get transaction history filtered by asset symbol.
     *
     * @param symbol The crypto symbol
     * @param userDetails The authenticated user details
     * @return List of transactions for the specified asset
     */
    @GetMapping("/asset/{symbol}")
    public ResponseEntity<List<Transaction>> getTransactionsByAsset(@PathVariable String symbol, @AuthenticationPrincipal UserDetails userDetails) {
        List<Transaction> transactions = transactionService.getTransactionsByAsset(symbol, userDetails.getUsername());
        return ResponseEntity.ok(transactions);
    }
}

