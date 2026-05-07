package com.auction.backend.scheduler;

import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.service.AuctionSessionLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuctionSessionScheduler {
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionSessionLifecycleService auctionSessionLifecycleService;

    @Scheduled(fixedDelay = 30000)
    public void autoActivateScheduledSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<AuctionSession> sessions = auctionSessionRepository.findByStatusAndStartTimeLessThanEqualAndEndTimeAfter(AuctionSessionStatus.SCHEDULED, now, now);
        log.info("Found {} sessions eligible for auto activation", sessions.size());
        for (AuctionSession session : sessions) {
            try {
                auctionSessionLifecycleService.activateSession(session.getId());
                log.info("Session activated :{}", session.getId());
            } catch (Exception e) {
                log.error("Cannot auto activate sessionId={}", session.getId(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void autoEndActiveSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<AuctionSession> sessions = auctionSessionRepository.findByStatusAndEndTimeLessThanEqual(AuctionSessionStatus.ACTIVE, now);
        log.info("Found {} sessions eligible for auto end", sessions.size());
        for (AuctionSession session : sessions) {
            try {
                auctionSessionLifecycleService.endSession(session.getId());
                log.info("Session ended :{}", session.getId());
            } catch (Exception e) {
                log.error("Cannot auto end sessionId={}", session.getId(), e);
            }
        }
    }
}
