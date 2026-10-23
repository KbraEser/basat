package io.basat.auth.authentication;

import io.basat.auth.common.Hashing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RedisLoginAttemptService implements LoginAttemptService {

    private static final String KEY_PREFIX = "login:attempts:";

    private final StringRedisTemplate redis;
    private final int maxAttempts;
    private final Duration window;

    public RedisLoginAttemptService(StringRedisTemplate redis,
                                    @Value("${app.login.max-attempts}") int maxAttempts,
                                    @Value("${app.login.window}") Duration window) {
        this.redis = redis;
        this.maxAttempts = maxAttempts;
        this.window = window;
    }

    @Override
    public boolean isBlocked(String email) {
        String key = key(email);
        long windowStart = Instant.now().minus(window).toEpochMilli();
        // Sliding window: drop attempts older than the window, then count the rest
        redis.opsForZSet().removeRangeByScore(key, 0, windowStart);
        Long attempts = redis.opsForZSet().zCard(key);
        return attempts != null && attempts >= maxAttempts;
    }

    @Override
    public void recordFailure(String email) {
        String key = key(email);
        long now = Instant.now().toEpochMilli();
        // Score = time of the attempt; the member must be unique so attempts in the same millisecond all count
        redis.opsForZSet().add(key, now + ":" + UUID.randomUUID(), now);
        redis.expire(key, window);
    }

    @Override
    public void reset(String email) {
        redis.delete(key(email));
    }

    // Hash the email so personal data does not appear in Redis keys
    private static String key(String email) {
        return KEY_PREFIX + Hashing.sha256Hex(email);
    }
}