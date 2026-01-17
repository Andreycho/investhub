package com.example.investhub.mapper;

import com.example.investhub.model.Asset;
import com.example.investhub.model.Holding;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperTest {

    private DtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DtoMapper();
    }

    // ---------------- AssetResponse ----------------

    @Test
    void toAssetResponse_whenAssetIsNull_shouldReturnNull() {
        assertNull(mapper.toAssetResponse(null));
    }

    @Test
    void toAssetResponse_shouldMapFields() {
        Asset asset = new Asset();
        asset.setId(1L);
        asset.setSymbol("BTCUSDT");
        asset.setName("Bitcoin");

        AssetResponse dto = mapper.toAssetResponse(asset);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("BTCUSDT", dto.getSymbol());
        assertEquals("Bitcoin", dto.getName());
    }

    // ---------------- WatchlistAssetResponse ----------------

    @Test
    void toWatchlistAssetResponse_whenAssetIsNull_shouldReturnNull() {
        assertNull(mapper.toWatchlistAssetResponse(null));
    }

    @Test
    void toWatchlistAssetResponse_shouldMapFields() {
        Asset asset = new Asset();
        asset.setId(2L);
        asset.setSymbol("ETHUSDT");
        asset.setName("Ethereum");

        WatchlistAssetResponse dto = mapper.toWatchlistAssetResponse(asset);

        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("ETHUSDT", dto.getSymbol());
        assertEquals("Ethereum", dto.getName());
    }

    // ---------------- HoldingResponse ----------------

    @Test
    void toHoldingResponse_whenHoldingIsNull_shouldReturnNull() {
        assertNull(mapper.toHoldingResponse(null));
    }

    @Test
    void toHoldingResponse_shouldMapFields() {
        Asset asset = new Asset();
        asset.setSymbol("BTCUSDT");

        Holding holding = new Holding();
        holding.setId(5L);
        holding.setAsset(asset);
        holding.setQuantity(0.25);
        holding.setAvgBuyPrice(50000.0);

        HoldingResponse dto = mapper.toHoldingResponse(holding);

        assertNotNull(dto);
        assertEquals(5L, dto.getId());
        assertEquals("BTCUSDT", dto.getAssetSymbol());
        assertEquals(0.25, dto.getQuantity(), 1e-9);
        assertEquals(50000.0, dto.getAvgBuyPrice(), 1e-9);
    }

    // ---------------- TransactionResponse ----------------

    @Test
    void toTransactionResponse_whenTransactionIsNull_shouldReturnNull() {
        assertNull(mapper.toTransactionResponse(null));
    }

    @Test
    void toTransactionResponse_shouldMapFields_whenAssetPresent() {
        Asset asset = new Asset();
        asset.setSymbol("BTCUSDT");

        Transaction tx = new Transaction();
        tx.setId(10L);
        tx.setType(Transaction.Type.BUY);
        tx.setAsset(asset);
        tx.setQuantity(0.1);
        tx.setPricePerUnit(60000.0);
        Instant ts = Instant.now();
        tx.setTimestamp(ts);

        TransactionResponse dto = mapper.toTransactionResponse(tx);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("BUY", dto.getType());
        assertEquals("BTCUSDT", dto.getAssetSymbol());
        assertEquals(0.1, dto.getQuantity(), 1e-9);
        assertEquals(60000.0, dto.getPricePerUnit(), 1e-9);
        assertEquals(ts, dto.getTimestamp());
    }

    @Test
    void toTransactionResponse_shouldHandleNullAsset() {
        Transaction tx = new Transaction();
        tx.setId(11L);
        tx.setType(Transaction.Type.SELL);
        tx.setAsset(null);
        tx.setQuantity(0.2);
        tx.setPricePerUnit(65000.0);
        Instant ts = Instant.now();
        tx.setTimestamp(ts);

        TransactionResponse dto = mapper.toTransactionResponse(tx);

        assertNotNull(dto);
        assertEquals(11L, dto.getId());
        assertEquals("SELL", dto.getType());
        assertNull(dto.getAssetSymbol());
        assertEquals(0.2, dto.getQuantity(), 1e-9);
        assertEquals(65000.0, dto.getPricePerUnit(), 1e-9);
        assertEquals(ts, dto.getTimestamp());
    }

    @Test
    void toTransactionResponse_shouldHandleNullType() {
        Asset asset = new Asset();
        asset.setSymbol("XRPUSDT");

        Transaction tx = new Transaction();
        tx.setId(12L);
        tx.setType(null);
        tx.setAsset(asset);
        tx.setQuantity(100);
        tx.setPricePerUnit(0.5);
        Instant ts = Instant.now();
        tx.setTimestamp(ts);

        TransactionResponse dto = mapper.toTransactionResponse(tx);

        assertNotNull(dto);
        assertEquals(12L, dto.getId());
        assertNull(dto.getType());
        assertEquals("XRPUSDT", dto.getAssetSymbol());
    }
}