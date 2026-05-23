package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionRealtimeEvent;
import com.auction.backend.service.AuctionSessionRealtimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j

public class AuctionSessionRealtimeServiceImpl implements AuctionSessionRealtimeService {

    private final Map<String, List<SseEmitter>> emittersBySessionId = new ConcurrentHashMap<>();

    @Override
    public SseEmitter subscribe(String sessionId) {
        SseEmitter emitter = new SseEmitter(0L);

        emittersBySessionId
                .computeIfAbsent(sessionId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError(error -> {
            log.warn("SSE emitter error. sessionId={}", sessionId, error);
            removeEmitter(sessionId, emitter);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Connected to auction session stream: " + sessionId));
        } catch (IOException e) {
            log.warn("Failed to send initial SSE connected event. sessionId={}", sessionId, e);
            removeEmitter(sessionId, emitter);
        }

        return emitter;
    }

    @Override
    public void publish(String sessionId, AuctionSessionRealtimeEvent event) {
        List<SseEmitter> emitters = emittersBySessionId.get(sessionId);

        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.getType().name())
                        .data(event));
            } catch (IOException | IllegalStateException e) {
                log.warn("Failed to send SSE event. sessionId={}, eventType={}",
                        sessionId,
                        event.getType(),
                        e
                );
                removeEmitter(sessionId, emitter);
            }
        }
    }

    private void removeEmitter(String sessionId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersBySessionId.get(sessionId);

        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);

        if (emitters.isEmpty()) {
            emittersBySessionId.remove(sessionId);
        }
    }
}
