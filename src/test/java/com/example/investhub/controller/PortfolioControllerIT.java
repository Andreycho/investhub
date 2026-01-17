package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.model.Holding;
import com.example.investhub.model.User;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.HoldingRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private HoldingRepository holdingRepository;

    @MockBean private BinanceWebSocketService binanceWebSocketService;

    private String token;
    private User user;
    private Asset btc;
    private Asset eth;

    @BeforeEach
    void setUp() {
        holdingRepository.deleteAll();
        assetRepository.deleteAll();
        userRepository.deleteAll();

        // mock prices used by PortfolioService stats + total value
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of(
                "BTCUSDT", 10000.0,
                "ETHUSDT", 2000.0
        ));

        user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setUsdBalance(new BigDecimal("30000.00"));
        user = userRepository.save(user);

        btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");
        btc = assetRepository.save(btc);

        eth = new Asset();
        eth.setSymbol("ETHUSDT");
        eth.setName("Ethereum");
        eth = assetRepository.save(eth);

        token = jwtService.generateToken(user);
    }

    private String bearer() {
        return "Bearer " + token;
    }

    @Test
    void getBalance_shouldReturnBalanceDto() throws Exception {
        mockMvc.perform(get("/api/portfolio/balance")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usdBalance", is(30000.00)));
    }

    @Test
    void getHoldings_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/portfolio/holdings")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void getHoldings_whenHasHoldings_returnsDtos() throws Exception {
        // Seed holdings (1 BTC, 2 ETH)
        Holding h1 = new Holding();
        h1.setUser(user);
        h1.setAsset(btc);
        h1.setQuantity(1.0);
        h1.setAvgBuyPrice(9000.0);
        holdingRepository.save(h1);

        Holding h2 = new Holding();
        h2.setUser(user);
        h2.setAsset(eth);
        h2.setQuantity(2.0);
        h2.setAvgBuyPrice(1500.0);
        holdingRepository.save(h2);

        mockMvc.perform(get("/api/portfolio/holdings")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].assetSymbol", containsInAnyOrder("BTCUSDT", "ETHUSDT")))
                .andExpect(jsonPath("$[*].avgBuyPrice", containsInAnyOrder(9000.0, 1500.0)));
    }

    @Test
    void getHoldingBySymbol_shouldReturnSingleHoldingDto() throws Exception {
        Holding h = new Holding();
        h.setUser(user);
        h.setAsset(btc);
        h.setQuantity(1.5);
        h.setAvgBuyPrice(8000.0);
        h = holdingRepository.save(h);

        mockMvc.perform(get("/api/portfolio/holdings/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(h.getId().intValue())))
                .andExpect(jsonPath("$.assetSymbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.quantity", is(1.5)))
                .andExpect(jsonPath("$.avgBuyPrice", is(8000.0)));
    }

    @Test
    void getPortfolio_shouldReturnPortfolioResponse_balanceAndHoldings() throws Exception {
        Holding h = new Holding();
        h.setUser(user);
        h.setAsset(btc);
        h.setQuantity(1.0);
        h.setAvgBuyPrice(9500.0);
        holdingRepository.save(h);

        mockMvc.perform(get("/api/portfolio")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usdBalance", is(30000.00)))
                .andExpect(jsonPath("$.holdings", hasSize(1)))
                .andExpect(jsonPath("$.holdings[0].assetSymbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.holdings[0].quantity", is(1.0)))
                .andExpect(jsonPath("$.holdings[0].avgBuyPrice", is(9500.0)));
    }

    @Test
    void getStats_shouldReturnStatsWrapper() throws Exception {
        // Holding: 1 BTC avg 9000, current 10000 => invested 9000, currentValue 10000, netGain 1000
        Holding h = new Holding();
        h.setUser(user);
        h.setAsset(btc);
        h.setQuantity(1.0);
        h.setAvgBuyPrice(9000.0);
        holdingRepository.save(h);

        mockMvc.perform(get("/api/portfolio/stats")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // PortfolioStatsResponse { stats: { ... } }
                .andExpect(jsonPath("$.stats", notNullValue()))
                .andExpect(jsonPath("$.stats.usdBalance", is(30000.00)))
                .andExpect(jsonPath("$.stats.totalInvested", is(9000.0)))
                .andExpect(jsonPath("$.stats.currentValue", is(10000.0)))
                .andExpect(jsonPath("$.stats.netGain", is(1000.0)))
                .andExpect(jsonPath("$.stats.holdingsCount", is(1)))
                .andExpect(jsonPath("$.stats.performanceStatus", notNullValue()));
    }

    @Test
    void endpoints_withoutToken_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/portfolio").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));

        mockMvc.perform(get("/api/portfolio/balance").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));

        mockMvc.perform(get("/api/portfolio/holdings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));

        mockMvc.perform(get("/api/portfolio/stats").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }
}