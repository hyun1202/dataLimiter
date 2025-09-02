package example.alarm.bucket.redis;

import example.alarm.bucket.BucketInfo;
import example.alarm.bucket.caffeine.BucketLocalCache;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BucketRedisCacheService {
    private final BucketRedisCache bucketRedisCache;

    public boolean consumeToken(BucketInfo info) {
        return bucketRedisCache.isAllowed(info);
    }

    // 현재 상태 조회 (남은 토큰 수)
    public long getRemainingTokens(BucketInfo info) {
        String key = bucketRedisCache.getKey(info.filename(), info.message());
        Bucket bucket = bucketRedisCache.getBucket(key);

        if (bucket == null) return BucketLocalCache.TOKEN_LIMIT;
        
        return bucket.getAvailableTokens();
    }
}
