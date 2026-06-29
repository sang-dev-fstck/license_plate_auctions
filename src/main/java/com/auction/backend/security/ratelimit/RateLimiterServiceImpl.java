package com.auction.backend.security.ratelimit;

import com.auction.backend.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterServiceImpl implements RateLimiterService {
    private static final int LOGIN_IP_LIMIT = 10;
    private static final Duration LOGIN_IP_WINDOW = Duration.ofMinutes(1);
    private static final int BID_LIMIT = 5;
    private static final Duration BID_WINDOW = Duration.ofSeconds(3);

    private static final int LOGIN_EMAIL_LIMIT = 5;
    private static final Duration LOGIN_EMAIL_WINDOW = Duration.ofMinutes(5);

    private static final String LOGIN_IP_KEY_PREFIX = "rate:login:ip:";
    private static final String LOGIN_EMAIL_KEY_PREFIX = "rate:login:email:";
    private static final String BID_KEY_PREFIX = "rate:bid:account:";

    private static final DefaultRedisScript<Long> FIXED_WINDOW_SCRIPT;
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT;

    static {
        FIXED_WINDOW_SCRIPT = new DefaultRedisScript<>();
        FIXED_WINDOW_SCRIPT.setScriptText("""
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('PEXPIRE', KEYS[1], ARGV[1])
                end
                return current
                """);
        FIXED_WINDOW_SCRIPT.setResultType(Long.class);

        //---------//
        SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
        SLIDING_WINDOW_SCRIPT.setScriptText("""
                local key = KEYS[1]
                local now = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local limit = tonumber(ARGV[3])
                local member = ARGV[4]
                
                redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
                
                local current = redis.call('ZCARD', key)
                
                if current >= limit then
                    redis.call('PEXPIRE', key, window)
                    return 0
                end
                
                redis.call('ZADD', key, now, member)
                redis.call('PEXPIRE', key, window)
                
                return current + 1
                """);
        SLIDING_WINDOW_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void checkLoginLimit(String ipAddress, String email) {
        String ipKey = LOGIN_IP_KEY_PREFIX + normalizeKeyPart(ipAddress);
        String emailKey = LOGIN_EMAIL_KEY_PREFIX + sha256(email).trim().toLowerCase();

        checkFixedWindow(
                ipKey,
                LOGIN_IP_WINDOW,
                LOGIN_IP_LIMIT,
                "Quá nhiều lần đăng nhập từ IP này, vui lòng thử lại sau"
        );

        checkFixedWindow(
                emailKey,
                LOGIN_EMAIL_WINDOW,
                LOGIN_EMAIL_LIMIT,
                "Tài khoản này đang bị đăng nhập quá nhiều lần, vui lòng thử lại sau"
        );
    }

    @Override
    public void checkBidLimit(String accountId, String sessionId) {
        String key = buildBidKey(accountId, sessionId);
        long result = checkSlidingWindow(key, BID_WINDOW, BID_LIMIT);
        if (result == 0) {
            throw new RateLimitExceededException(
                    "Bạn đang gửi giá quá nhanh, vui lòng thử lại sau",
                    BID_WINDOW
            );
        }
    }

    private long incrementKeyAndExpire(String key, Duration window) {
        return stringRedisTemplate.execute(
                FIXED_WINDOW_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(window.toMillis())
        );
    }

    private long checkSlidingWindow(String key, Duration window, int limit) {
        long now = System.currentTimeMillis();
        String member = now + ":" + UUID.randomUUID();

        return stringRedisTemplate.execute(
                SLIDING_WINDOW_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(window.toMillis()),
                String.valueOf(limit),
                member
        );
    }

    private void checkFixedWindow(String key, Duration window, int limit, String message) {
        long current = incrementKeyAndExpire(key, window);
        if (current > limit) {
            throw new RateLimitExceededException(message, window);
        }
    }

    private String normalizeKeyPart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value.trim()
                .replace(":", "_")
                .replace("/", "_")
                .replace(" ", "_");
    }

    private String buildBidKey(String accountId, String sessionId) {
        return BID_KEY_PREFIX
                + normalizeKeyPart(accountId)
                + ":session:"
                + normalizeKeyPart(sessionId);
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash rate limit key", e);
        }
    }
}
