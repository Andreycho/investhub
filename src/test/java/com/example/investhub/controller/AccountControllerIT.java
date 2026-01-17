package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.model.Holding;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.User;
import com.example.investhub.model.WatchlistEntry;
import com.example.investhub.model.enumeration.TransactionType;
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
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    @Autowired private UserRepository userRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private HoldingRepository holdingRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private WatchlistRepository watchlistRepository;

    private String token;
    private User user;

    @BeforeEach
    void setUp() {
        // clean
        watchlistRepository.deleteAll();
        transactionRepository.deleteAll();
        holdingRepository.deleteAll();
        assetRepository.deleteAll();
        userRepository.deleteAll();

        // seed user
        user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setUsdBalance(new BigDecimal("12345.67"));
        user = userRepository.save(user);

        // seed asset
        Asset btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");
        btc = assetRepository.save(btc);

        // seed holding
        Holding h = new Holding();
        h.setUser(user);
        h.setAsset(btc);
        h.setQuantity(2.0);
        h.setAvgBuyPrice(100.0);
        holdingRepository.save(h);

        // seed transaction
        Transaction t = new Transaction();
        t.setUserId(user.getId());
        t.setAsset(btc);
        t.setType(TransactionType.BUY);
        t.setQuantity(2.0);
        t.setPricePerUnit(100.0);
        t.setTimestamp(Instant.now());
        transactionRepository.save(t);

        // seed watchlist entry
        WatchlistEntry w = new WatchlistEntry();
        w.setUser(user);
        w.setAsset(btc);
        watchlistRepository.save(w);

        // jwt (User implements UserDetails)
        token = jwtService.generateToken(user);
    }

    private String bearer() {
        return "Bearer " + token;
    }

    @Test
    void resetAccount_shouldDeleteAllUserData_andResetBalance() throws Exception {
        // sanity: data exists
        assertThat(holdingRepository.findByUser_Id(user.getId())).hasSize(1);
        assertThat(transactionRepository.findByUser_Id(user.getId())).hasSize(1);
        assertThat(watchlistRepository.findByUser_Id(user.getId())).hasSize(1);

        mockMvc.perform(post("/api/account/reset")
                        .header("Authorization", bearer())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usdBalance", is(30000.00)))
                .andExpect(jsonPath("$.message", not(emptyOrNullString())))
                .andExpect(jsonPath("$.message", containsString("reset")));

        // verify DB side effects
        assertThat(holdingRepository.findByUser_Id(user.getId())).isEmpty();
        assertThat(transactionRepository.findByUser_Id(user.getId())).isEmpty();
        assertThat(watchlistRepository.findByUser_Id(user.getId())).isEmpty();

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getUsdBalance()).isEqualByComparingTo("30000.00");
    }

    @Test
    void resetAccount_withoutToken_shouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/account/reset")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(anyOf(is(401), is(403))));
    }
}