package example.alarm;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class BucketMap {
    private static final int CACHE_EXPIRE_MIN = 35;
    private static final int TOKEN_REFILL_MIN = 30;
    public static final int TOKEN_LIMIT = 5;

    // 각 파라미터 조합별로 개별 버킷 관리
    private final Cache<Integer, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(CACHE_EXPIRE_MIN))
//            .expireAfterWrite(Duration.ofSeconds(5))
            .build();


    public Bucket getBucketOrCreate(int key) {
        return buckets.get(key, k -> createBucket());
    }

    public Bucket createBucket() {
        // 30분 동안 5개 허용하는 버킷 생성
        Bandwidth limit = Bandwidth.builder()
                .capacity(TOKEN_LIMIT)
                .refillIntervally(TOKEN_LIMIT, Duration.ofMinutes(TOKEN_REFILL_MIN))
//                .refillIntervally(TOKEN_LIMIT, Duration.ofSeconds(5))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public Bucket getBucket(int key) {
        return buckets.getIfPresent(key);
    }
}
