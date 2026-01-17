package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.model.User;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.repository.HoldingRepository;
import com.example.investhub.repository.TransactionRepository;
import com.example.investhub.repository.UserRepository;
import com.example.investhub.repository.WatchlistRepository;
import com.example.investhub.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full integration test: Controller + Security(JWT) + Service + Repo + H2 DB.
 */

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class WatchlistControllerIT {

    @Autowired private MockMvc mockMvc;

    @Autowired private JwtService jwtService;

    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private WatchlistRepository watchlistRepository;
    @Autowired private HoldingRepository holdingRepository;
    @Autowired private TransactionRepository transactionRepository;

    private String token;
    private User user;

    @BeforeEach
    void setUp() {
        watchlistRepository.deleteAll();
        transactionRepository.deleteAll();
        holdingRepository.deleteAll();

        userRepository.deleteAll();

        assetRepository.deleteAll();

        // Seed user
        user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setUsdBalance(new BigDecimal("30000.00"));
        user = userRepository.save(user);

        // Seed asset
        Asset btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");
        assetRepository.save(btc);

        // Real JWT (User implements UserDetails)
        token = jwtService.generateToken(user);
    }

    private String bearer() {
        return "Bearer " + token;
    }

    @Test
    void getWatchlist_whenEmpty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/watchlist")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", is(empty())));
    }

    @Test
    void addToWatchlist_thenGetWatchlist_containsAsset() throws Exception {
        // POST add
        mockMvc.perform(post("/api/watchlist/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.name", is("Bitcoin")))
                .andExpect(jsonPath("$.id", notNullValue()));

        // GET watchlist
        mockMvc.perform(get("/api/watchlist")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$[0].name", is("Bitcoin")));
    }

    @Test
    void containsEndpoint_shouldReturnTrueAfterAdd_andFalseAfterDelete() throws Exception {
        // add
        mockMvc.perform(post("/api/watchlist/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // contains -> true
        mockMvc.perform(get("/api/watchlist/contains/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // delete
        mockMvc.perform(delete("/api/watchlist/BTCUSDT")
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());

        // contains -> false
        mockMvc.perform(get("/api/watchlist/contains/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void addDuplicate_shouldReturnConflict() throws Exception {
        // 1st add
        mockMvc.perform(post("/api/watchlist/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // 2nd add -> 409
        mockMvc.perform(post("/api/watchlist/BTCUSDT")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Resource already exists")));
    }

    @Test
    void endpoints_withoutToken_shouldBeRejected() throws Exception {
        mockMvc.perform(get("/api/watchlist")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }
}