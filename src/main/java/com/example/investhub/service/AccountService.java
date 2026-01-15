package com.example.investhub.service;

import com.example.investhub.exception.ResourceNotFoundException;
import com.example.investhub.model.User;
import com.example.investhub.model.dto.response.ResetAccountResponse;
import com.example.investhub.repository.HoldingRepository;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private static final BigDecimal START_BALANCE = new BigDecimal("30000.00");

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionRepository transactionRepository;
    private final WatchlistRepository watchlistRepository;

    public AccountService(
            UserRepository userRepository,
            HoldingRepository holdingRepository,
            TransactionRepository transactionRepository,
            WatchlistRepository watchlistRepository
    ) {
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
        this.transactionRepository = transactionRepository;
        this.watchlistRepository = watchlistRepository;
    }

    @Transactional
    public ResetAccountResponse resetAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        Long userId = user.getId();

        holdingRepository.deleteByUser_Id(userId);
        transactionRepository.deleteByUser_Id(userId);
        watchlistRepository.deleteByUser_Id(userId);

        user.setUsdBalance(START_BALANCE);
        userRepository.save(user);

        return new ResetAccountResponse(user.getUsdBalance(), "Account reset successful");
    }
}