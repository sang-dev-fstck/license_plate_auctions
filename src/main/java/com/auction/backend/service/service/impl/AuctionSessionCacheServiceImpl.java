package com.auction.backend.service.service.impl;

import com.auction.backend.common.CacheNames;
import com.auction.backend.service.AuctionSessionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuctionSessionCacheServiceImpl implements AuctionSessionCacheService {
    private final CacheManager cacheManager;

    @Override
    public void evictSessionDetail(String sessionId) {
        Cache cache = cacheManager.getCache(CacheNames.AUCTION_SESSION_DETAIL);

        if (cache != null) {
            cache.evict(sessionId);
        }

    }

    @Override
    public void evictBidHistory(String sessionId) {
        Cache cache = cacheManager.getCache(CacheNames.AUCTION_SESSION_BID_HISTORY);
        if (cache != null) {
            cache.evict(sessionId);
        }
    }
}
