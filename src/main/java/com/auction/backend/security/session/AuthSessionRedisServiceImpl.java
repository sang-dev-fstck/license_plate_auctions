package com.auction.backend.security.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthSessionRedisServiceImpl implements AuthSessionRedisService {
    private static final String SESSION_KEY_PREFIX = "auth:session:";

    private static final String ACCOUNT_SESSIONS_KEY_PREFIX = "auth:account:";
    private static final String ACCOUNT_SESSIONS_KEY_SUFFIX = ":sessions";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(AuthSession session, Duration ttl) {
        try {
            String key = buildKey(session.getTokenHash());
            String json = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot save auth session to Redis", e);
        }
    }

    @Override
    public Optional<AuthSession> findByTokenHash(String tokenHash) {
        try {
            String json = stringRedisTemplate.opsForValue().get(buildKey(tokenHash));

            if (json == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, AuthSession.class));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read auth session from Redis", e);
        }
    }

    @Override
    public void deleteByTokenHash(String tokenHash) {
        stringRedisTemplate.delete(buildKey(tokenHash));
    }

    @Override
    public void addToAccountIndex(String accountId, String tokenHash, Duration ttl) {
        try {
            String key = buildAccountSessionsKey(accountId);
            stringRedisTemplate.opsForSet().add(key, tokenHash);
            stringRedisTemplate.expire(key, ttl.plus(Duration.ofMinutes(5)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot add token per account index to Redis", e);
        }
    }

    @Override
    public Set<String> findTokenHashesByAccountId(String accountId) {
        String buildAccountSessionsKey = buildAccountSessionsKey(accountId);
        Set<String> tokenHashes = stringRedisTemplate.opsForSet().members(buildAccountSessionsKey);
        if (tokenHashes == null || tokenHashes.isEmpty()) {
            return Set.of();
        }
        return tokenHashes;
    }

    @Override
    public void removeFromAccountIndex(String accountId, String tokenHash) {
        try {
            String key = buildAccountSessionsKey(accountId);
            stringRedisTemplate.opsForSet().remove(key, tokenHash);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot remove token per account index from Redis", e);
        }
    }

    @Override
    public void deleteAccountIndex(String accountId) {
        stringRedisTemplate.delete(buildAccountSessionsKey(accountId));
    }

    private String buildKey(String tokenHash) {
        return SESSION_KEY_PREFIX + tokenHash;
    }

    private String buildAccountSessionsKey(String accountId) {
        return ACCOUNT_SESSIONS_KEY_PREFIX + accountId + ACCOUNT_SESSIONS_KEY_SUFFIX;
    }

}
