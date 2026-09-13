package com.uber.bg.uber.bg.Services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class BlacklistTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    public BlacklistTokenService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void blacklistToken(String token, long remainingTimeMillis) {
        String redisKey = "blacklist:" + token;

        redisTemplate.opsForValue().set(
                redisKey,
                "true",
                Duration.ofMillis(remainingTimeMillis)
        );
    }


    public boolean isTokenBlacklisted(String token) {
        Boolean hasKey = redisTemplate.hasKey("blacklist:" + token);
        return hasKey != null && hasKey;
    }
}
