package com.example.investhub.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.investhub.model.dto.BinanceSubscribeMessage;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ClientEndpoint
@Service
public class BinanceWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(BinanceWebSocketService.class);

    @Value("${binance.websocket.url}")
    private String binanceWebSocketUrl;

    private final ObjectMapper objectMapper;
    private final Map<String, Double> cryptoPrices = new ConcurrentHashMap<>();
    private Session session;

    public BinanceWebSocketService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        connectToBinanceWebSocket();
    }

    private void connectToBinanceWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, new URI(binanceWebSocketUrl));
            log.info("Connected to Binance WebSocket");
        } catch (Exception e) {
            log.error("Error connecting to Binance WebSocket", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
                log.info("Binance WebSocket connection closed");
            } catch (Exception e) {
                log.error("Error closing WebSocket session", e);
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        try {
            BinanceSubscribeMessage subscribeMessage = BinanceSubscribeMessage.createDefaultSubscription();
            String jsonMessage = objectMapper.writeValueAsString(subscribeMessage);
            session.getAsyncRemote().sendText(jsonMessage);
            log.info("Subscribed to Binance ticker channel: {}", jsonMessage);
        } catch (Exception e) {
            log.error("Error sending subscription message", e);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            log.debug("Received message: {}", message);

            JsonNode jsonNode = objectMapper.readTree(message);

            if (jsonNode.has("e") && "24hrTicker".equals(jsonNode.get("e").asText()) && jsonNode.has("s") && jsonNode.has("c")) {
                String symbol = jsonNode.get("s").asText();
                double price = jsonNode.get("c").asDouble();

                if (!symbol.isEmpty()) {
                    cryptoPrices.put(symbol, price);
                    log.info("Updated price for {}: {}", symbol, price);
                }
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
        }
    }

    public Map<String, Double> getCryptoPrices() {
        return Collections.unmodifiableMap(cryptoPrices);
    }
}
