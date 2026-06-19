package com.auction.backend.security.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthSessionRedisServiceImpl implements AuthSessionRedisService {
    private static final String SESSION_KEY_PREFIX = "auth:session:";

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

    private String buildKey(String tokenHash) {
        return SESSION_KEY_PREFIX + tokenHash;
    }
}
