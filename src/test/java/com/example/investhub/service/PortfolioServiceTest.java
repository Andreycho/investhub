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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BinanceWebSocketService binanceWebSocketService;

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private User user;
    private Asset btc;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("test1");
        user.setUsdBalance(new BigDecimal("30000.00"));

        btc = new Asset();
        btc.setId(10L);
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");
    }

    // ---------------- getUserBalance ----------------

    @Test
    void getUserBalance_returnsBalance() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        BigDecimal balance = portfolioService.getUserBalance("test1");

        assertEquals(new BigDecimal("30000.00"), balance);
        verify(userRepository).findByUsername("test1");
    }

    @Test
    void getUserBalance_userNotFound_throwsRuntime() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> portfolioService.getUserBalance("missing"));

        assertEquals("User not found", ex.getMessage());
    }

    // ---------------- getUserHoldings ----------------

    @Test
    void getUserHoldings_filtersZeroQuantity() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        Holding h1 = holding(100L, user, btc, 2.0, 100.0); // keep
        Holding h2 = holding(101L, user, btc, 0.0, 100.0); // filter out

        when(holdingRepository.findByUser_Id(1L)).thenReturn(List.of(h1, h2));

        List<Holding> result = portfolioService.getUserHoldings("test1");

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    // ---------------- getHoldingBySymbol ----------------

    @Test
    void getHoldingBySymbol_findsHoldingIgnoringCase() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        Holding h1 = holding(100L, user, btc, 2.0, 100.0);
        when(holdingRepository.findByUser_Id(1L)).thenReturn(List.of(h1));

        Holding found = portfolioService.getHoldingBySymbol("btcusdt", "test1");

        assertEquals(100L, found.getId());
        assertEquals("BTCUSDT", found.getAssetSymbol());
    }

    @Test
    void getHoldingBySymbol_notFound_throwsRuntime() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(holdingRepository.findByUser_Id(1L)).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> portfolioService.getHoldingBySymbol("BTCUSDT", "test1"));

        assertTrue(ex.getMessage().contains("No holding found for symbol"));
    }

    // ---------------- getUserPortfolio (covers calculateTotalPortfolioValue private) ----------------

    @Test
    void getUserPortfolio_returnsBalanceHoldingsAndTotalValue() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        Holding h1 = holding(100L, user, btc, 2.0, 100.0);
        when(holdingRepository.findByUser_Id(1L)).thenReturn(List.of(h1));

        // price for BTC in websocket map
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of("BTCUSDT", 200.0));

        Map<String, Object> portfolio = portfolioService.getUserPortfolio("test1");

        assertNotNull(portfolio.get("balance"));
        assertNotNull(portfolio.get("holdings"));
        assertNotNull(portfolio.get("totalValue"));

        assertEquals(new BigDecimal("30000.00"), portfolio.get("balance"));

        @SuppressWarnings("unchecked")
        List<Holding> holdings = (List<Holding>) portfolio.get("holdings");
        assertEquals(1, holdings.size());

        // totalValue = usdBalance + holdingsValue = 30000 + (2 * 200) = 30400
        double totalValue = (double) portfolio.get("totalValue");
        assertEquals(30400.0, totalValue, 0.0001);
    }

    // ---------------- getPortfolioStatistics ----------------

    @Test
    void getPortfolioStatistics_calculatesWinning() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));

        // invested = 2 * 100 = 200
        Holding h1 = holding(100L, user, btc, 2.0, 100.0);
        when(holdingRepository.findByUser_Id(1L)).thenReturn(List.of(h1));

        // currentValue = 2 * 200 = 400
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of("BTCUSDT", 200.0));

        Map<String, Object> stats = portfolioService.getPortfolioStatistics("test1");

        assertEquals(new BigDecimal("30000.00"), stats.get("usdBalance"));
        assertEquals(200.0, (double) stats.get("totalInvested"), 0.0001);
        assertEquals(400.0, (double) stats.get("currentValue"), 0.0001);
        assertEquals(200.0, (double) stats.get("netGain"), 0.0001);
        assertEquals(100.0, (double) stats.get("returnPercentage"), 0.0001);

        assertEquals(PerformanceStatus.WINNING, stats.get("performanceStatus"));

        // totalPortfolioValue = currentValue + usdBalance
        assertEquals(30400.0, (double) stats.get("totalPortfolioValue"), 0.0001);
        assertEquals(1, stats.get("holdingsCount"));
    }

    @Test
    void getPortfolioStatistics_breakEvenWhenNoHoldings() {
        when(userRepository.findByUsername("test1")).thenReturn(Optional.of(user));
        when(holdingRepository.findByUser_Id(1L)).thenReturn(List.of());
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of());

        Map<String, Object> stats = portfolioService.getPortfolioStatistics("test1");

        assertEquals(0.0, (double) stats.get("totalInvested"), 0.0001);
        assertEquals(0.0, (double) stats.get("currentValue"), 0.0001);
        assertEquals(0.0, (double) stats.get("netGain"), 0.0001);
        assertEquals(0.0, (double) stats.get("returnPercentage"), 0.0001);
        assertEquals(PerformanceStatus.BREAK_EVEN, stats.get("performanceStatus"));
        assertEquals(30000.0, (double) stats.get("totalPortfolioValue"), 0.0001);
    }

    // ---------------- validateUserCanSell ----------------

    @Test
    void validateUserCanSell_ok() {
        Holding h1 = holding(100L, user, btc, 2.0, 100.0);
        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.of(h1));

        assertDoesNotThrow(() -> portfolioService.validateUserCanSell(1L, "BTCUSDT", 1.5));
    }

    @Test
    void validateUserCanSell_holdingMissing_throws() {
        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.empty());

        InsufficientHoldingsException ex = assertThrows(InsufficientHoldingsException.class,
                () -> portfolioService.validateUserCanSell(1L, "BTCUSDT", 1.0));

        assertEquals("BTCUSDT", ex.getAssetSymbol());
        assertEquals(0.0, ex.getOwned(), 0.0001);
        assertEquals(1.0, ex.getRequested(), 0.0001);
    }

    @Test
    void validateUserCanSell_notEnoughQty_throws() {
        Holding h1 = holding(100L, user, btc, 0.5, 100.0);
        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.of(h1));

        InsufficientHoldingsException ex = assertThrows(InsufficientHoldingsException.class,
                () -> portfolioService.validateUserCanSell(1L, "BTCUSDT", 1.0));

        assertEquals(0.5, ex.getOwned(), 0.0001);
        assertEquals(1.0, ex.getRequested(), 0.0001);
    }

    // ---------------- updateHoldingAfterTransaction ----------------

    @Test
    void updateHoldingAfterTransaction_buy_existingHolding_updatesQtyAndAvg() {
        Holding existing = holding(100L, user, btc, 2.0, 100.0);

        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.of(existing));

        Transaction tx = buyTx(user, btc, 1.0, 200.0); // buy 1 at 200

        portfolioService.updateHoldingAfterTransaction(tx, user);

        // new qty = 3
        // totalCost = (2*100) + (1*200) = 400
        // avg = 400/3 = 133.333...
        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());
        Holding saved = captor.getValue();

        assertEquals(3.0, saved.getQuantity(), 0.0001);
        assertEquals(400.0 / 3.0, saved.getAvgBuyPrice(), 0.0001);
        verify(holdingRepository, never()).delete(any());
    }

    @Test
    void updateHoldingAfterTransaction_buy_newHolding_createsAndSaves() {
        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.empty());

        Transaction tx = buyTx(user, btc, 2.0, 150.0);

        portfolioService.updateHoldingAfterTransaction(tx, user);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());
        Holding saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(btc, saved.getAsset());
        assertEquals(2.0, saved.getQuantity(), 0.0001);
        assertEquals(150.0, saved.getAvgBuyPrice(), 0.0001);
    }

    @Test
    void updateHoldingAfterTransaction_sell_reducesQty_andSavesIfStillPositive() {
        Holding existing = holding(100L, user, btc, 2.0, 100.0);

        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.of(existing));

        Transaction tx = sellTx(user, btc, 0.5, 200.0);

        portfolioService.updateHoldingAfterTransaction(tx, user);

        ArgumentCaptor<Holding> captor = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(captor.capture());

        Holding saved = captor.getValue();
        assertEquals(1.5, saved.getQuantity(), 0.0001);
        verify(holdingRepository, never()).delete(any());
    }

    @Test
    void updateHoldingAfterTransaction_sell_toZero_deletesHoldingIfHasId() {
        Holding existing = holding(100L, user, btc, 1.0, 100.0);

        when(holdingRepository.findByUser_IdAndAsset_Symbol(1L, "BTCUSDT"))
                .thenReturn(Optional.of(existing));

        Transaction tx = sellTx(user, btc, 1.0, 200.0);

        portfolioService.updateHoldingAfterTransaction(tx, user);

        verify(holdingRepository, never()).save(any());
        verify(holdingRepository).delete(existing);
    }

    // ---------------- helpers ----------------

    private static Holding holding(Long id, User user, Asset asset, double qty, double avgBuy) {
        Holding h = new Holding();
        h.setId(id);
        h.setUser(user);
        h.setAsset(asset);
        h.setQuantity(qty);
        h.setAvgBuyPrice(avgBuy);
        return h;
    }

    private static Transaction buyTx(User user, Asset asset, double qty, double pricePerUnit) {
        Transaction t = new Transaction();
        t.setUserId(user.getId());
        t.setAsset(asset);
        t.setType(TransactionType.BUY);
        t.setQuantity(qty);
        t.setPricePerUnit(pricePerUnit);
        return t;
    }

    private static Transaction sellTx(User user, Asset asset, double qty, double pricePerUnit) {
        Transaction t = new Transaction();
        t.setUserId(user.getId());
        t.setAsset(asset);
        t.setType(TransactionType.SELL);
        t.setQuantity(qty);
        t.setPricePerUnit(pricePerUnit);
        return t;
    }
}