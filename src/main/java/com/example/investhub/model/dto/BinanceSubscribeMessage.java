package com.example.investhub.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BinanceSubscribeMessage {

    private String method;

    @JsonProperty("params")
    private List<String> params;

    private int id;

    public BinanceSubscribeMessage(String method, List<String> params, int id) {
        this.method = method;
        this.params = params;
        this.id = id;
    }

    public static BinanceSubscribeMessage createDefaultSubscription() {
        List<String> streams = List.of(
                "btcusdt@ticker",
                "ethusdt@ticker",
                "bnbusdt@ticker",
                "adausdt@ticker",
                "dogeusdt@ticker",
                "xrpusdt@ticker",
                "solusdt@ticker"
        );

        return new BinanceSubscribeMessage("SUBSCRIBE", streams, 1);
    }
}
