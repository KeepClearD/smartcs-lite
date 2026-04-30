package com.smartcs.lite.model.dto;

public record AnalyticsVO(
        long totalConversations,
        long todayConversations,
        long totalMessages,
        long todayMessages,
        long activeConversations,
        long onlineAgents,
        double botResolutionRate
) {}
