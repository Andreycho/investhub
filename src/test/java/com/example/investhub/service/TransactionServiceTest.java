package com.example.investhub.service;

import com.example.investhub.exception.InsufficientBalanceException;
import com.example.investhub.exception.ResourceNotFoundException;
import com.example.investhub.exception.ValidationException;
import com.example.investhub.model.Asset;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.User;
import com.example.investhub.model.enumeration.TransactionType;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PortfolioService portfolioService;
    @Mock private MarketDataService marketDataService;
    @Mock private AssetRepository assetRepository;

    @InjectMocks private TransactionService transactionService;

    // ---------- Validation tests ----------

    @Test
    void createTransaction_whenQuantityIsZero_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> {
            transactionService.createTransaction(TransactionType.BUY, "   ", 1, "test1");
        });

        verifyNoInteractions(userRepository, assetRepository, marketDataService, transactionRepository, portfolioService);
    }

    @Test
    void createTransaction_whenAssetSymbolMissing_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () ->
                transactionService.createTransaction(TransactionType.BUY, "   ", 1, "test1")
        );

        verifyNoInteractions(userRepository, assetRepository, marketDataService, transactionRepository, portfolioService);
    }

    // ---------- Not found / price tests ----------

    @Test
    void createTransaction_whenUserNotFound_shouldThrowResourceNotFound() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(TransactionType.BUY, "BTCUSDT", 1, "missing")
        );

        verify(userRepository).findByUsername("missing");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(assetRepository, marketDataService, transactionRepository, portfolioService);
    }

    @Test
    void createTransaction_whenAssetNotFound_shouldThrowResourceNotFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("30000.00"));

        when(userRepository.findByUsername("test1"))
                .thenReturn(Optional.of(user));

        when(assetRepository.findBySymbol("BTCUSDT"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(TransactionType.BUY, "btcusdt", 1, "test1")
        );

        verify(userRepository).findByUsername("test1");
        verify(assetRepository).findBySymbol("BTCUSDT");
        verifyNoInteractions(marketDataService, transactionRepository, portfolioService);
    }

    @Test
    void createTransaction_whenPriceNull_shouldThrowRuntimeException() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("30000.00"));

        Asset asset = new Asset();
        asset.setId(10L);
        asset.setSymbol("BTCUSDT");
        asset.setName("Bitcoin");

        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(asset));
        when(marketDataService.getPriceBySymbol("BTCUSDT")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                transactionService.createTransaction(TransactionType.BUY, "btcusdt", 1, "test1")
        );
        assertTrue(ex.getMessage().contains("Unable to fetch current price"));

        verifyNoInteractions(transactionRepository, portfolioService);
    }

    @Test
    void createTransaction_whenPriceZeroOrNegative_shouldThrowRuntimeException() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("30000.00"));

        Asset asset = new Asset();
        asset.setId(10L);
        asset.setSymbol("BTCUSDT");
        asset.setName("Bitcoin");

        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(asset));
        when(marketDataService.getPriceBySymbol("BTCUSDT")).thenReturn(0.0);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                transactionService.createTransaction(TransactionType.BUY, "BTCUSDT", 1, "test1")
        );
        assertTrue(ex.getMessage().contains("Unable to fetch current price"));

        verifyNoInteractions(transactionRepository, portfolioService);
    }

    // ---------- BUY tests ----------

    @Test
    void createTransaction_buy_whenInsufficientBalance_shouldThrowAndNotSaveAnything() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("100.00"));

        Asset asset = new Asset();
        asset.setId(10L);
        asset.setSymbol("BTCUSDT");
        asset.setName("Bitcoin");

        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(asset));
        when(marketDataService.getPriceBySymbol("BTCUSDT")).thenReturn(1000.0); // 1 * 1000 > 100

        assertThrows(InsufficientBalanceException.class, () ->
                transactionService.createTransaction(TransactionType.BUY, "BTCUSDT", 1, "test1")
        );

        verify(userRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
        verify(portfolioService, never()).updateHoldingAfterTransaction(any(), any());
        verify(portfolioService, never()).validateUserCanSell(anyLong(), anyString(), anyDouble());
    }

    @Test
    void createTransaction_buy_success_shouldSubtractBalance_SaveTransaction_AndUpdateHolding() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("30000.00"));

        Asset asset = new Asset();
        asset.setId(10L);
        asset.setSymbol("BTCUSDT");
        asset.setName("Bitcoin");

        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(asset));
        when(marketDataService.getPriceBySymbol("BTCUSDT")).thenReturn(1000.0); // total = 2000

        // mock save to return same transaction (simulate DB save)
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(TransactionType.BUY, "btcusdt", 2, "test1");

        // balance = 30000 - 2000 = 28000
        assertEquals(new BigDecimal("28000.00"), user.getUsdBalance());

        verify(userRepository).save(user);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());

        Transaction savedTx = txCaptor.getValue();
        assertEquals(TransactionType.BUY, savedTx.getType());
        assertEquals(2.0, savedTx.getQuantity());
        assertEquals(1000.0, savedTx.getPricePerUnit());
        assertNotNull(savedTx.getAsset());
        assertEquals("BTCUSDT", savedTx.getAsset().getSymbol());
        assertEquals(1L, savedTx.getUserId());

        verify(portfolioService).updateHoldingAfterTransaction(any(Transaction.class), eq(user));

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(TransactionType.BUY, result.getType());
    }

    // ---------- SELL tests ----------

    @Test
    void createTransaction_sell_success_shouldValidateSell_AddBalance_SaveTransaction_AndUpdateHolding() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("30000.00"));

        Asset asset = new Asset();
        asset.setId(10L);
        asset.setSymbol("BTCUSDT");
        asset.setName("Bitcoin");

        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(asset));
        when(marketDataService.getPriceBySymbol("BTCUSDT")).thenReturn(1000.0); // total = 1500

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.createTransaction(TransactionType.SELL, "BTCUSDT", 1.5, "test1");

        // validate sell called
        verify(portfolioService).validateUserCanSell(1L, "BTCUSDT", 1.5);

        // balance = 30000 + 1500 = 31500
        assertEquals(new BigDecimal("31500.00"), user.getUsdBalance());

        verify(userRepository).save(user);
        verify(transactionRepository).save(any(Transaction.class));
        verify(portfolioService).updateHoldingAfterTransaction(any(Transaction.class), eq(user));

        assertNotNull(result);
        assertEquals(TransactionType.SELL, result.getType());
        assertEquals(1.5, result.getQuantity());
        assertEquals(1000.0, result.getPricePerUnit());
        assertEquals(1L, result.getUserId());
    }
}