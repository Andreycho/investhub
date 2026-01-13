package com.example.investhub.service;

import com.example.investhub.model.Asset;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.WatchlistEntry;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.repository.WatchlistRepository;
import com.example.investhub.websocket.BinanceWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TradingService {

    private final BinanceWebSocketService binanceWebSocketService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;

    @Autowired
    public TradingService(BinanceWebSocketService binanceWebSocketService, TransactionRepository transactionRepository, UserRepository userRepository, WatchlistRepository watchlistRepository) {
        this.binanceWebSocketService = binanceWebSocketService;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.watchlistRepository = watchlistRepository;
    }

    /**
     * Get current cryptocurrency prices from Binance WebSocket service.
     * @return Map of cryptocurrency symbols to their current prices.
     */
    public Map<String, Double> getCurrentCryptoPrices() {
        return binanceWebSocketService.getCryptoPrices();
    }

    /**
     * Fetch user transactions from the database.
     * @param userId ID of the user.
     * @return List of transactions for the user.
     */
    public List<Transaction> getUserTransactions(Long userId) {
        log.info("Fetching transactions for user ID: {}", userId);
        return transactionRepository.findByUserId(userId);
    }

    /**
     * Fetch user balance from the database.
     * @param userId ID of the user.
     * @return Current balance of the user.
     */
    public double getUserBalance(Long userId) {
        log.info("Fetching balance for user ID: {}", userId);
        return userRepository.findById(userId)
                .map(user -> user.getUsdBalance().doubleValue())
                .orElse(0.0);
    }

    /**
     * Fetch user watchlist from the database.
     * @param userId ID of the user.
     * @return List of assets in the user's watchlist.
     */
    public List<Asset> getUserWatchlist(Long userId) {
        List<Asset> list = new ArrayList<>();

        for (WatchlistEntry watchlistEntry : watchlistRepository.findByUserId(userId)) {
            Asset asset = watchlistEntry.getAsset();
            list.add(asset);
        }
        return list;
    }


}
