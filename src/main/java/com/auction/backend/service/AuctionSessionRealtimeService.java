package com.auction.backend.service;

import com.auction.backend.dto.AuctionSessionRealtimeEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AuctionSessionRealtimeService {
    SseEmitter subscribe(String sessionId);

    void publish(String sessionId, AuctionSessionRealtimeEvent event);
}
