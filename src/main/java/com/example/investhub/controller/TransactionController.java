package com.example.investhub.controller;

import com.example.investhub.mapper.DtoMapper;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.dto.request.CreateTransactionRequest;
import com.example.investhub.model.dto.response.TotalAmountResponse;
import com.example.investhub.model.dto.response.TransactionCountResponse;
import com.example.investhub.model.dto.response.TransactionResponse;
import com.example.investhub.model.enumeration.TransactionType;
import com.example.investhub.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final DtoMapper dtoMapper;

    public TransactionController(TransactionService transactionService, DtoMapper dtoMapper) {
        this.transactionService = transactionService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(@RequestParam(required = false) TransactionType type, @AuthenticationPrincipal UserDetails userDetails) {

        List<TransactionResponse> transactions;

        if (type != null) {
            transactions = transactionService
                    .getUserTransactionsByType(userDetails.getUsername(), type)
                    .stream()
                    .map(dtoMapper::toTransactionResponse)
                    .toList();
        } else {
            transactions = transactionService
                    .getUserTransactions(userDetails.getUsername())
                    .stream()
                    .map(dtoMapper::toTransactionResponse)
                    .toList();
        }

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long transactionId, @AuthenticationPrincipal UserDetails userDetails) {
        Transaction transaction = transactionService.getTransactionById(transactionId, userDetails.getUsername());
        return ResponseEntity.ok(dtoMapper.toTransactionResponse(transaction));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody CreateTransactionRequest request, @AuthenticationPrincipal UserDetails userDetails) {

        log.info("=== CREATE TRANSACTION REQUEST ===");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "NULL");
        log.info("Transaction Type: {}", request.getType());
        log.info("Asset Symbol: {}", request.getAssetSymbol());
        log.info("Quantity: {}", request.getQuantity());

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Transaction created = transactionService.createTransaction(
                request.getType(),
                request.getAssetSymbol(),
                request.getQuantity(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok(dtoMapper.toTransactionResponse(created));
    }

    @GetMapping("/asset/{symbol}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAsset(@PathVariable String symbol, @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions = transactionService
                .getTransactionsByAsset(symbol, userDetails.getUsername())
                .stream()
                .map(dtoMapper::toTransactionResponse)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/total")
    public ResponseEntity<TotalAmountResponse> getTotalAmountByType(@RequestParam TransactionType type, @AuthenticationPrincipal UserDetails userDetails) {
        BigDecimal total = transactionService.getTotalAmountByType(userDetails.getUsername(), type);
        TotalAmountResponse response = new TotalAmountResponse(type, total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/{symbol}")
    public ResponseEntity<TransactionCountResponse> getTransactionCountByAsset(@PathVariable String symbol, @AuthenticationPrincipal UserDetails userDetails) {
        Long count = transactionService.getTransactionCountByAsset(userDetails.getUsername(), symbol);
        TransactionCountResponse response = new TransactionCountResponse(symbol.toUpperCase(), count);
        return ResponseEntity.ok(response);
    }
}