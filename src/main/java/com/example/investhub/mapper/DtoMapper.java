package com.example.investhub.mapper;

import com.example.investhub.model.Asset;
import com.example.investhub.model.Holding;
import com.example.investhub.model.Transaction;
import com.example.investhub.model.dto.response.AssetResponse;
import com.example.investhub.model.dto.response.HoldingResponse;
import com.example.investhub.model.dto.response.TransactionResponse;
import com.example.investhub.model.dto.response.WatchlistAssetResponse;
import org.springframework.stereotype.Component;

/**
 * Universal mapper for converting entities to DTOs.
 */
@Component
public class DtoMapper {

    public AssetResponse toAssetResponse(Asset asset) {
        if (asset == null) {
            return null;
        }

        AssetResponse dto = new AssetResponse();
        dto.setId(asset.getId());
        dto.setSymbol(asset.getSymbol());
        dto.setName(asset.getName());
        return dto;
    }

    public WatchlistAssetResponse toWatchlistAssetResponse(Asset asset) {
        if (asset == null) {
            return null;
        }

        WatchlistAssetResponse dto = new WatchlistAssetResponse();
        dto.setId(asset.getId());
        dto.setSymbol(asset.getSymbol());
        dto.setName(asset.getName());
        return dto;
    }

    public HoldingResponse toHoldingResponse(Holding holding) {
        if (holding == null) {
            return null;
        }

        HoldingResponse dto = new HoldingResponse();
        dto.setId(holding.getId());
        dto.setAssetSymbol(holding.getAssetSymbol());
        dto.setQuantity(holding.getQuantity());
        dto.setAvgBuyPrice(holding.getAvgBuyPrice());
        return dto;
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        String assetSymbol = (transaction.getAsset() != null) ? transaction.getAsset().getSymbol() : null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType() != null ? transaction.getType().name() : null,
                assetSymbol,
                transaction.getQuantity(),
                transaction.getPricePerUnit(),
                transaction.getTimestamp()
        );
    }
}
