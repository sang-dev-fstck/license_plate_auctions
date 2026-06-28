package com.auction.backend.security.ratelimit;

public interface RateLimiterService {
    void checkLoginLimit(String ipAddress, String email);
}
