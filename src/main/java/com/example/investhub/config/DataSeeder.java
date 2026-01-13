package com.example.investhub.config;

import com.example.investhub.model.Asset;
import com.example.investhub.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final AssetRepository assetRepository;

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting database seeding...");
        seedAssets();
        log.info("Database seeding completed!");
    }

    /**
     * Seed cryptocurrency assets.
     * This runs automatically on every startup.
     */
    private void seedAssets() {
        List<AssetData> assetsToSeed = List.of(
                new AssetData("BTCUSDT", "Bitcoin"),
                new AssetData("ETHUSDT", "Ethereum"),
                new AssetData("BNBUSDT", "Binance Coin"),
                new AssetData("ADAUSDT", "Cardano"),
                new AssetData("DOGEUSDT", "Dogecoin"),
                new AssetData("XRPUSDT", "Ripple"),
                new AssetData("SOLUSDT", "Solana")
        );

        for (AssetData assetData : assetsToSeed) {
            if (!assetRepository.existsBySymbol(assetData.symbol)) {
                Asset asset = new Asset();
                asset.setSymbol(assetData.symbol);
                asset.setName(assetData.name);
                assetRepository.save(asset);

                log.info("✓ Seeded asset: {} ({})", assetData.name, assetData.symbol);
            }
        }

        log.info("✓ Assets seeded. Total assets in database: {}", assetRepository.count());
    }

    private record AssetData(String symbol, String name) {}
}

