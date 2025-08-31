package example.alarm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import example.alarm.report.AlarmInfo;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class BucketCache {
    private static final int CACHE_EXPIRE_MIN = 35;
    private static final int TOKEN_REFILL_MIN = 30;
    public static final int TOKEN_LIMIT = 5;

    // 각 파라미터 조합별로 개별 버킷 관리
    private final Cache<Integer, BucketStatus> buckets = Caffeine.newBuilder()
            .maximumSize(1000)
//            .expireAfterWrite(Duration.ofMinutes(CACHE_EXPIRE_MIN))
            .expireAfterWrite(Duration.ofSeconds(5))
            .build();

    public int getKey(String... values) {
        String key = String.join("|", values);
        return key.hashCode();
    }

    public Bucket getBucketOrCreate(AlarmInfo info) {
        int key = getKey(info.filename(), info.message());

        BucketStatus bucketStatus = buckets.get(key, k -> createBucketStatus(info));
        return bucketStatus.getBucket();
    }

    public BucketStatus createBucketStatus(AlarmInfo info) {
        return BucketStatus.builder()
                .bucket(createBucket())
                .alarmInfo(info)
                .availableTokens(TOKEN_LIMIT)
                .maxTokens(TOKEN_LIMIT)
                .build();
    }

    public Bucket createBucket() {
        // 30분 동안 5개 허용하는 버킷 생성
        Bandwidth limit = Bandwidth.builder()
                .capacity(TOKEN_LIMIT)
//                .refillIntervally(TOKEN_LIMIT, Duration.ofMinutes(TOKEN_REFILL_MIN))
                .refillIntervally(TOKEN_LIMIT, Duration.ofSeconds(5))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket getBucket(int key) {
        return buckets.getIfPresent(key).getBucket();
    }

    public Map<Integer, BucketStatus> getBucketMap() {
        return buckets.asMap();
    }
}
