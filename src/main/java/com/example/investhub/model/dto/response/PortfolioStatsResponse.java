package com.example.investhub.model.dto.response;

import java.util.Map;

public class PortfolioStatsResponse {
    private Map<String, Object> stats;

    public PortfolioStatsResponse() {}

    public PortfolioStatsResponse(Map<String, Object> stats) {
        this.stats = stats;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}