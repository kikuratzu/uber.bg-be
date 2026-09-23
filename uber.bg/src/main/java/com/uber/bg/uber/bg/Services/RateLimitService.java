package com.uber.bg.uber.bg.Services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;
import java.time.Duration;



@Service
public class RateLimitService {
    private final Cache<String, Bucket> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(30))   // should be >= your longest refill period, see below
            .maximumSize(50000)
            .build();

    private Bucket createNewBucket(int capacity, Duration refillPeriod) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(capacity, refillPeriod)
                        .build())
                .build();
    }

    public boolean tryConsume(String key, int capacity, Duration refillPeriod) {
        Bucket bucket = cache.get(key, k -> createNewBucket(capacity, refillPeriod));
        return bucket != null && bucket.tryConsume(1);
    }
}
