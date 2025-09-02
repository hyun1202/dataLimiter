package example.alarm.bucket.redis;

import example.alarm.bucket.BucketInfo;
import example.alarm.bucket.BucketStatus;
import example.alarm.component.RedisComponent;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BucketRedisCache {
    private static final int CACHE_EXPIRE_MIN = 35;
    private static final int TOKEN_REFILL_MIN = 30;
    public static final int TOKEN_LIMIT = 5;

    private static final String BUCKET_KEY_PREFIX = "bucket:";

    private final LettuceBasedProxyManager proxyManager;
    private final RedisComponent redisComponent; // BucketStatus 저장용

    public String getKey(String... values) {
        String key = String.join("|", values);
        return String.valueOf(key.hashCode());
    }

    public boolean isAllowed(BucketInfo info) {
        String key = getKey(info.filename(), info.message());
        Bucket bucket = getBucket(key);
        boolean allowed = bucket.tryConsume(1);
        saveBucketStatus(key, info, (int) bucket.getAvailableTokens());
        return allowed;
    }

    public Bucket getBucket(String key) {
        // 각 버킷마다 개별 설정 생성
        return proxyManager.builder()
                .build(BUCKET_KEY_PREFIX + key, this::createBucketConfiguration);
    }

    private BucketConfiguration createBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                                  .capacity(TOKEN_LIMIT)
                                  .refillIntervally(TOKEN_LIMIT, Duration.ofMinutes(TOKEN_REFILL_MIN))
                                  .build())
                .build();
    }

    private void saveBucketStatus(String key, BucketInfo info, int availableTokens) {
        BucketStatus status = BucketStatus.builder()
                .filename(info.filename())
                .availableTokens(availableTokens)
                .maxTokens(TOKEN_LIMIT)
                .resetInterval(Duration.ofMinutes(TOKEN_REFILL_MIN))
                .build();

        redisComponent.putValue("status:" + key, status, Duration.ofMinutes(35));
    }

    // 대시보드용 모든 상태 조회
    public Map<String, BucketStatus> getAllBucketStatuses() {
        return redisComponent.getAllBucketsByPrefix("status:");
    }

}
