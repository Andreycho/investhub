package com.example.investhub.controller;

import com.example.investhub.model.Asset;
import com.example.investhub.repository.AssetRepository;
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

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class MarketDataControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private AssetRepository assetRepository;

    @MockBean private BinanceWebSocketService binanceWebSocketService;

    @BeforeEach
    void setUp() {
        assetRepository.deleteAll();

        // Seed assets in H2
        Asset btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");
        assetRepository.save(btc);

        Asset eth = new Asset();
        eth.setSymbol("ETHUSDT");
        eth.setName("Ethereum");
        assetRepository.save(eth);

        // Mock prices map
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of(
                "BTCUSDT", 10000.0,
                "ETHUSDT", 2000.0
        ));
    }

    @Test
    void getCurrentPrices_shouldReturnListOfPriceResponses() throws Exception {
        mockMvc.perform(get("/api/market/prices")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].symbol", containsInAnyOrder("BTCUSDT", "ETHUSDT")))
                .andExpect(jsonPath("$[*].price", containsInAnyOrder(10000.0, 2000.0)));
    }

    @Test
    void getPriceBySymbol_shouldReturnPriceResponse() throws Exception {
        mockMvc.perform(get("/api/market/prices/BTCUSDT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.price", is(10000.0)));
    }

    @Test
    void getAllAssets_shouldReturnAssetDtos() throws Exception {
        mockMvc.perform(get("/api/market/assets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].symbol", containsInAnyOrder("BTCUSDT", "ETHUSDT")))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("Bitcoin", "Ethereum")))
                .andExpect(jsonPath("$[*].id", everyItem(notNullValue())));
    }

    @Test
    void getAssetBySymbol_shouldReturnSingleAssetDto() throws Exception {
        mockMvc.perform(get("/api/market/assets/BTCUSDT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$.name", is("Bitcoin")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void searchAssets_shouldFilterBySymbolOrName_caseInsensitive() throws Exception {
        mockMvc.perform(get("/api/market/assets/search")
                        .param("query", "bit")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].symbol", is("BTCUSDT")))
                .andExpect(jsonPath("$[0].name", is("Bitcoin")));
    }

    @Test
    void getPriceBySymbol_whenPriceMissing_shouldReturn4xx() throws Exception {
        mockMvc.perform(get("/api/market/prices/ADAUSDT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAssetBySymbol_whenMissing_shouldReturn4xx() throws Exception {
        mockMvc.perform(get("/api/market/assets/ADAUSDT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}