package com.auction.backend.service.impl;

import com.auction.backend.common.CacheNames;
import com.auction.backend.dto.*;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.Bid;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.AuctionSessionMapper;
import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import com.auction.backend.readmodel.CustomerAuctionSessionReadModel;
import com.auction.backend.repository.AuctionSessionReadRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.BidRepository;
import com.auction.backend.service.AuctionSessionQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSessionQueryServiceImpl implements AuctionSessionQueryService {
    private static final Duration SESSION_DETAIL_LOCK_TTL = Duration.ofSeconds(5);
    private static final long SESSION_DETAIL_WAIT_TIMEOUT_MS = 1500;
    private static final long SESSION_DETAIL_WAIT_INTERVAL_MS = 75;
    private static final String SESSION_DETAIL_LOCK_PREFIX = "lock:auction-session-detail:";

    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionSessionReadRepository auctionSessionReadRepository;
    private final AuctionSessionMapper auctionSessionMapper;
    private final BidRepository bidRepository;
    private final CacheManager cacheManager;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public AuctionSessionDetailResponse getSessionDetail(String sessionId) {
        Cache cache = getRequiredCache(CacheNames.AUCTION_SESSION_DETAIL);

        AuctionSessionDetailResponse cached = cache.get(sessionId, AuctionSessionDetailResponse.class);
        if (cached != null) {
            return cached;
        }


        String lockKey = SESSION_DETAIL_LOCK_PREFIX + sessionId;
        String lockValue = UUID.randomUUID().toString();
        boolean lockAcquired = tryAcquireLock(lockKey, lockValue, SESSION_DETAIL_LOCK_TTL);
        if (lockAcquired) {
            try {
                AuctionSessionDetailResponse cachedAfterLock = cache.get(sessionId, AuctionSessionDetailResponse.class);

                if (cachedAfterLock != null) {
                    return cachedAfterLock;
                }

                AuctionSessionDetailResponse detail = loadAuctionSessionFromDb(sessionId);
                cache.put(sessionId, detail);
                return detail;
            } finally {
                releaseLockSafely(lockKey, lockValue);
            }
        }

        AuctionSessionDetailResponse waitedCache = waitForCache(cache, sessionId);

        if (waitedCache != null) {
            return waitedCache;
        }
        log.warn("Cache stampede wait timeout. Fallback to DB. sessionId={}", sessionId);
        return loadAuctionSessionFromDb(sessionId);
    }

    private AuctionSessionDetailResponse loadAuctionSessionFromDb(String sessionId) {
        log.warn("DB HIT for session detail. sessionId={}", sessionId);

        AuctionSessionDetailReadModel detail =
                auctionSessionReadRepository.findSessionDetailById(sessionId)
                        .orElseThrow(() -> AppException.notFound("Không tìm thấy phiên đấu giá"));
        return AuctionSessionDetailResponse.builder()
                .id(detail.getId())
                .licensePlateNumber(detail.getLicensePlateNumber())
                .categoryName(detail.getCategoryName())
                .provinceName(detail.getProvinceName())
                .tags(detail.getTags())
                .status(detail.getStatus())
                .startTime(detail.getStartTime())
                .endTime(detail.getEndTime())
                .startingPrice(detail.getStartingPrice())
                .currentPrice(detail.getCurrentPrice())
                .bidStepAmountSnapshot(detail.getBidStepAmountSnapshot())
                .currentLeaderName(detail.getCurrentLeaderName())
                .winnerName(detail.getWinnerName())
                .pauseReason(detail.getPauseReason())
                .failureReason(detail.getFailureReason())
                .build();
    }

    private Cache getRequiredCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null) {
            throw new IllegalStateException("Cache not found: " + cacheName);
        }

        return cache;
    }

    private boolean tryAcquireLock(String lockKey, String lockValue, Duration timeout) {
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeout);

        return Boolean.TRUE.equals(success);
    }

    private void releaseLockSafely(String lockKey, String lockValue) {
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(lockKey),
                lockValue
        );
    }

    private AuctionSessionDetailResponse waitForCache(Cache cache, String sessionId) {
        long deadline = System.currentTimeMillis() + SESSION_DETAIL_WAIT_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(SESSION_DETAIL_WAIT_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }

            AuctionSessionDetailResponse cached =
                    cache.get(sessionId, AuctionSessionDetailResponse.class);

            if (cached != null) {
                return cached;
            }
        }

        return null;
    }

    @Override
    @Cacheable(value = CacheNames.AUCTION_SESSION_BID_HISTORY, key = "#sessionId")
    public List<BidHistoryItemResponse> getBidHistory(String sessionId) {
        AuctionSession session = getSession(sessionId);
        List<Bid> bids = bidRepository.findByAuctionSessionIdOrderByCreatedAtDesc(session.getId());
        return bids.stream()
                .map(bid -> BidHistoryItemResponse.builder()
                        .bidId(bid.getId())
                        .bidderAccountId(bid.getBidderAccountId())
                        .bidderName(bid.getBidderFullNameSnapshot() != null
                                ? bid.getBidderFullNameSnapshot()
                                : "Unknown User")
                        .amount(bid.getAmount())
                        .status(bid.getStatus())
                        .createdAt(bid.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<CustomerAuctionSessionResponse> getCustomerSessions(SearchSessionRequest request) {
        Page<CustomerAuctionSessionReadModel> pageResult = auctionSessionReadRepository.searchAuctionSessionsDynamic(request);
        log.info("Found {} AuctionSessions", pageResult.getContent().size());
        List<CustomerAuctionSessionResponse> content = pageResult.getContent().stream()
                .map(auctionSessionMapper::toResponseForCustomer)
                .collect(Collectors.toList());

        return PageResponse.of(pageResult, content);
    }

    @Override
    public void validatePublicStreamAccess(String sessionId) {
        AuctionSession session = getSession(sessionId);
        if (!isPublicVisibleStatus(session.getStatus())) {
            throw new AppException("Phiên đấu giá không khả dụng để theo dõi");
        }
    }

    private boolean isPublicVisibleStatus(AuctionSessionStatus status) {
        return status == AuctionSessionStatus.SCHEDULED
                || status == AuctionSessionStatus.ACTIVE
                || status == AuctionSessionStatus.PAUSED
                || status == AuctionSessionStatus.ENDED
                || status == AuctionSessionStatus.FAILED;
    }

    private AuctionSession getSession(String sessionId) {
        return auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy phiên đấu giá"));
    }
}
