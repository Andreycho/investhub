package com.example.investhub.service;

import com.example.investhub.exception.InsufficientBalanceException;
import com.example.investhub.exception.ResourceNotFoundException;
import com.example.investhub.exception.ValidationException;
import com.example.investhub.model.Asset;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.User;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing transactions.
 */
@Service
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PortfolioService portfolioService;
    private final MarketDataService marketDataService;
    private final AssetRepository assetRepository;

    public TransactionService(TransactionRepository transactionRepository,
                             UserRepository userRepository,
                             @Lazy PortfolioService portfolioService,
                             MarketDataService marketDataService,
                             AssetRepository assetRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.portfolioService = portfolioService;
        this.marketDataService = marketDataService;
        this.assetRepository = assetRepository;
    }

    /**
     * Get all transactions for a user.
     *
     * @param username The username
     * @return List of user's transactions
     */
    public List<Transaction> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return transactionRepository.findByUser_Id(user.getId());
    }

    /**
     * Get a specific transaction by ID.
     *
     * @param transactionId The transaction ID
     * @param username The username
     * @return The transaction
     */
    public Transaction getTransactionById(Long transactionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to transaction");
        }

        return transaction;
    }

    /**
     * Create a new transaction and update holdings.
     *
     * @param type The transaction type
     * @param assetSymbol The asset symbol
     * @param quantity The quantity
     * @param username The username
     * @return The created transaction
     */
    public Transaction createTransaction(Transaction.Type type, String assetSymbol, double quantity, String username) {
        if (quantity <= 0) {
            throw new ValidationException("quantity", "Transaction quantity must be greater than 0");
        }
        if (assetSymbol == null || assetSymbol.trim().isEmpty()) {
            throw new ValidationException("assetSymbol", "Asset symbol must be specified");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        Asset asset = assetRepository.findBySymbol(assetSymbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Asset", assetSymbol));

        Double currentPrice = marketDataService.getPriceBySymbol(asset.getSymbol());
        if (currentPrice == null || currentPrice <= 0) {
            throw new RuntimeException("Unable to fetch current price for " + asset.getSymbol());
        }

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setPricePerUnit(currentPrice);
        transaction.setAsset(asset);

        double transactionTotal = quantity * currentPrice;

        if (type == Transaction.Type.BUY) {
            if (user.getUsdBalance().doubleValue() < transactionTotal) {
                throw new InsufficientBalanceException(transactionTotal, user.getUsdBalance().doubleValue());
            }

            user.setUsdBalance(user.getUsdBalance().subtract(java.math.BigDecimal.valueOf(transactionTotal)));

        } else if (type == Transaction.Type.SELL) {
            portfolioService.validateUserCanSell(user.getId(), asset.getSymbol(), quantity);

            user.setUsdBalance(user.getUsdBalance().add(java.math.BigDecimal.valueOf(transactionTotal)));
        }

        userRepository.save(user);

        transaction.setUserId(user.getId());

        Transaction savedTransaction = transactionRepository.save(transaction);

        portfolioService.updateHoldingAfterTransaction(savedTransaction, user);

        return savedTransaction;
    }

    /**
     * Get transactions filtered by asset symbol.
     *
     * @param symbol The asset symbol
     * @param username The username
     * @return List of transactions for the asset
     */
    public List<Transaction> getTransactionsByAsset(String symbol, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUser_Id(user.getId()).stream()
                .filter(t -> t.getAssetSymbol().equalsIgnoreCase(symbol))
                .toList();
    }
}

