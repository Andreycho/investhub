package com.example.investhub.service;

import com.example.investhub.model.Asset;
import com.example.investhub.repository.AssetRepository;
import com.example.investhub.websocket.BinanceWebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketDataServiceTest {

    @Mock private BinanceWebSocketService binanceWebSocketService;
    @Mock private AssetRepository assetRepository;

    @InjectMocks private MarketDataService marketDataService;

    @Test
    void getCurrentPrices_shouldReturnPricesFromWebSocketService() {
        Map<String, Double> prices = Map.of("BTCUSDT", 50000.0, "ETHUSDT", 2500.0);
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(prices);

        Map<String, Double> result = marketDataService.getCurrentPrices();

        assertEquals(prices, result);
        verify(binanceWebSocketService).getCryptoPrices();
        verifyNoInteractions(assetRepository);
    }

    @Test
    void getPriceBySymbol_shouldReturnPrice_caseInsensitive() {
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of("BTCUSDT", 123.45));

        Double price = marketDataService.getPriceBySymbol("btcusdt");

        assertEquals(123.45, price);
        verify(binanceWebSocketService).getCryptoPrices();
        verifyNoInteractions(assetRepository);
    }

    @Test
    void getPriceBySymbol_whenMissing_shouldThrow() {
        when(binanceWebSocketService.getCryptoPrices()).thenReturn(Map.of("ETHUSDT", 2000.0));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> marketDataService.getPriceBySymbol("BTCUSDT"));

        assertTrue(ex.getMessage().toLowerCase().contains("price not available"));
        verify(binanceWebSocketService).getCryptoPrices();
        verifyNoInteractions(assetRepository);
    }

    @Test
    void getAllAssets_shouldReturnRepositoryFindAll() {
        Asset a1 = new Asset();
        Asset a2 = new Asset();
        when(assetRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Asset> result = marketDataService.getAllAssets();

        assertEquals(2, result.size());
        verify(assetRepository).findAll();
        verifyNoInteractions(binanceWebSocketService);
    }

    @Test
    void getAssetBySymbol_shouldUppercaseSymbolAndReturnAsset() {
        Asset btc = new Asset();
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.of(btc));

        Asset result = marketDataService.getAssetBySymbol("btcusdt");

        assertSame(btc, result);
        verify(assetRepository).findBySymbol("BTCUSDT");
        verifyNoInteractions(binanceWebSocketService);
    }

    @Test
    void getAssetBySymbol_whenMissing_shouldThrow() {
        when(assetRepository.findBySymbol("BTCUSDT")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> marketDataService.getAssetBySymbol("btcusdt"));

        assertTrue(ex.getMessage().toLowerCase().contains("asset not found"));
        verify(assetRepository).findBySymbol("BTCUSDT");
        verifyNoInteractions(binanceWebSocketService);
    }

    @Test
    void searchAssets_shouldMatchBySymbolOrName_caseInsensitive() {
        Asset btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");

        Asset eth = new Asset();
        eth.setSymbol("ETHUSDT");
        eth.setName("Ethereum");

        when(assetRepository.findAll()).thenReturn(List.of(btc, eth));

        List<Asset> result1 = marketDataService.searchAssets("btc");
        assertEquals(1, result1.size());
        assertEquals("BTCUSDT", result1.get(0).getSymbol());

        List<Asset> result2 = marketDataService.searchAssets("ETHERE");
        assertEquals(1, result2.size());
        assertEquals("ETHUSDT", result2.get(0).getSymbol());

        verify(assetRepository, times(2)).findAll();
        verifyNoInteractions(binanceWebSocketService);
    }

    @Test
    void searchAssets_whenNoMatch_shouldReturnEmptyList() {
        Asset btc = new Asset();
        btc.setSymbol("BTCUSDT");
        btc.setName("Bitcoin");

        when(assetRepository.findAll()).thenReturn(List.of(btc));

        List<Asset> result = marketDataService.searchAssets("XRP");

        assertTrue(result.isEmpty());
        verify(assetRepository).findAll();
        verifyNoInteractions(binanceWebSocketService);
    }
}