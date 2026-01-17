package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.model.Holding;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.User;
import com.example.investhub.model.enumeration.TransactionType;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.HoldingRepository;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.service.JwtService;
import com.example.investhub.websocket.BinanceWebSocketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test: Controller + Security(JWT) + Service + Repo + H2 DB.
 */

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private HoldingRepository holdingRepository;
    @Autowired private TransactionRepository transactionRepository;

    @SuppressWarnings("deprecation")
    @MockBean private BinanceWebSocketService binanceWebSocketService;

    private String token;
    private User user;
    private Asset btc;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        holdingRepository.deleteAll();
        assetRepository.deleteAll();
        userRepository.deleteAll();

        // Mock prices (MarketDataService -> BinanceWebSocketService)
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of(
                "BTCUSDT", 10000.0,
                "ETHUSDT", 2000.0
        ));

        // Seed user
        user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setUsdBalance(new BigDecimal("30000.00"));
        user = userRepository.save(user);

        // Seed asset
        btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");
        btc = assetRepository.save(btc);

        // Real JWT (User implements UserDetails)
        token = jwtService.generateToken(user);
    }

    private String bearer() {
        return "Bearer " + token;
    }

    @Test
    void getUserTransactions_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void createBuyTransaction_shouldSucceed_decreaseBalance_createHolding_andAppearInList() throws Exception {
        String body = """
                {
                  "type": "BUY",
                  "assetSymbol": "BTCUSDT",
                  "quantity": 1.0
                }
                """;

        // POST (controller returns 200 OK)
        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("BUY")))
                .andExpect(jsonPath("$.assetSymbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.quantity", is(1.0)))
                .andExpect(jsonPath("$.pricePerUnit", is(10000.0)))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.id", notNullValue()));

        // balance: 30000 - 1*10000 = 20000
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(0, updated.getUsdBalance().compareTo(new BigDecimal("20000.00")));

        Holding holding = holdingRepository.findByUser_IdAndAsset_Symbol(user.getId(), "BTCUSDT").orElseThrow();
        assertEquals(1.0, holding.getQuantity(), 1e-9);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("BUY")))
                .andExpect(jsonPath("$[0].assetSymbol", is("BTCUSDT")));
    }

    @Test
    void createBuyTransaction_whenInsufficientBalance_shouldReturnBadRequest() throws Exception {
        // 10 BTC * 10000 = 100000 > 30000
        String body = """
                {
                  "type": "BUY",
                  "assetSymbol": "BTCUSDT",
                  "quantity": 10.0
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void createSellTransaction_shouldSucceed_increaseBalance_decreaseHolding() throws Exception {
        // Seed holding 2 BTC
        Holding h = new Holding();
        h.setUser(user);
        h.setAsset(btc);
        h.setQuantity(2.0);
        h.setAvgBuyPrice(9000.0);
        holdingRepository.save(h);

        String body = """
                {
                  "type": "SELL",
                  "assetSymbol": "BTCUSDT",
                  "quantity": 1.0
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("SELL")))
                .andExpect(jsonPath("$.assetSymbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.quantity", is(1.0)))
                .andExpect(jsonPath("$.pricePerUnit", is(10000.0)));

        // balance: 30000 + 10000 = 40000
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(0, updated.getUsdBalance().compareTo(new BigDecimal("40000.00")));

        // holding: 2 - 1 = 1
        Holding updatedHolding = holdingRepository.findByUser_IdAndAsset_Symbol(user.getId(), "BTCUSDT").orElseThrow();
        assertEquals(1.0, updatedHolding.getQuantity(), 1e-9);
    }

    @Test
    void getTransactionsByAsset_shouldReturnOnlyMatchingSymbol() throws Exception {
        // Seed 2 assets + 2 transactions
        Asset eth = new Asset();
        eth.setSymbol("ETHUSDT");
        eth.setName("Ethereum");
        eth = assetRepository.save(eth);

        Transaction t1 = new Transaction();
        t1.setUserId(user.getId());
        t1.setType(TransactionType.BUY);
        t1.setAsset(btc);
        t1.setQuantity(1.0);
        t1.setPricePerUnit(10000.0);
        transactionRepository.save(t1);

        Transaction t2 = new Transaction();
        t2.setUserId(user.getId());
        t2.setType(TransactionType.BUY);
        t2.setAsset(eth);
        t2.setQuantity(2.0);
        t2.setPricePerUnit(2000.0);
        transactionRepository.save(t2);

        mockMvc.perform(get("/api/transactions/asset/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].assetSymbol", is("BTCUSDT")));
    }

    @Test
    void getTransactionById_shouldReturnTransaction_whenOwnedByUser() throws Exception {
        Transaction t = new Transaction();
        t.setUserId(user.getId());
        t.setType(TransactionType.BUY);
        t.setAsset(btc);
        t.setQuantity(1.0);
        t.setPricePerUnit(10000.0);
        t = transactionRepository.save(t);

        mockMvc.perform(get("/api/transactions/" + t.getId())
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(t.getId().intValue())))
                .andExpect(jsonPath("$.assetSymbol", is("BTCUSDT")));
    }

    @Test
    void endpoints_withoutToken_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/transactions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    void getTransactionsByType_shouldReturnOnlyBuyTransactions() throws Exception {
        // Seed BUY and SELL transactions
        Transaction buyTx = new Transaction();
        buyTx.setUserId(user.getId());
        buyTx.setType(TransactionType.BUY);
        buyTx.setAsset(btc);
        buyTx.setQuantity(1.0);
        buyTx.setPricePerUnit(10000.0);
        transactionRepository.save(buyTx);

        Transaction sellTx = new Transaction();
        sellTx.setUserId(user.getId());
        sellTx.setType(TransactionType.SELL);
        sellTx.setAsset(btc);
        sellTx.setQuantity(0.5);
        sellTx.setPricePerUnit(11000.0);
        transactionRepository.save(sellTx);

        // Get only BUY transactions
        mockMvc.perform(get("/api/transactions?type=BUY")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("BUY")))
                .andExpect(jsonPath("$[0].quantity", is(1.0)));

        // Get only SELL transactions
        mockMvc.perform(get("/api/transactions?type=SELL")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("SELL")))
                .andExpect(jsonPath("$[0].quantity", is(0.5)));
    }

    @Test
    void getTotalAmountByType_shouldReturnCorrectTotals() throws Exception {
        // Seed multiple BUY transactions
        Transaction buy1 = new Transaction();
        buy1.setUserId(user.getId());
        buy1.setType(TransactionType.BUY);
        buy1.setAsset(btc);
        buy1.setQuantity(1.0);
        buy1.setPricePerUnit(10000.0);
        transactionRepository.save(buy1);

        Transaction buy2 = new Transaction();
        buy2.setUserId(user.getId());
        buy2.setType(TransactionType.BUY);
        buy2.setAsset(btc);
        buy2.setQuantity(0.5);
        buy2.setPricePerUnit(12000.0);
        transactionRepository.save(buy2);

        // Get total BUY amount: (1.0 * 10000) + (0.5 * 12000) = 16000
        mockMvc.perform(get("/api/transactions/total?type=BUY")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("BUY")))
                .andExpect(jsonPath("$.totalAmount", is(16000.0)));
    }

    @Test
    void getTransactionCountByAsset_shouldReturnCorrectCount() throws Exception {
        // Seed multiple transactions for BTC
        for (int i = 0; i < 3; i++) {
            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setType(TransactionType.BUY);
            tx.setAsset(btc);
            tx.setQuantity(0.1);
            tx.setPricePerUnit(10000.0);
            transactionRepository.save(tx);
        }

        // Get count for BTC
        mockMvc.perform(get("/api/transactions/count/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetSymbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.transactionCount", is(3)));

        // Get count for non-existent asset
        mockMvc.perform(get("/api/transactions/count/ETHUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetSymbol", is("ETHUSDT")))
                .andExpect(jsonPath("$.transactionCount", is(0)));
    }

    @Test
    void createTransaction_withoutToken_shouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"BUY","assetSymbol":"BTCUSDT","quantity":1}
                                """))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }
}