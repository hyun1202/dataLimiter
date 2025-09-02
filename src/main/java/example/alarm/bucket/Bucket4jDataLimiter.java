package example.alarm.bucket;

import example.alarm.bucket.caffeine.BucketLocalCache;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Bucket4jDataLimiter {
    private final BucketLocalCache bucketLocalCache;

    public BucketStatus consumeToken(BucketInfo info) {
        // 해당 키에 대한 버킷 가져오기 (없으면 생성)
        BucketStatus bucketStatus = bucketLocalCache.getBucketOrCreate(info);
        // 1개 토큰 소비
        if (!bucketStatus.consumeToken()) {
            return null;
        }

        bucketStatus.addBucketDetail(BucketDetail.from(info));

        return bucketStatus;
    }

    // 현재 상태 조회 (남은 토큰 수)
    public long getRemainingTokens(BucketInfo info) {
        int key = bucketLocalCache.getKey(info.filename(), info.message());
        Bucket bucket = bucketLocalCache.getBucket(key);

        if (bucket == null) return BucketLocalCache.TOKEN_LIMIT;
        
        return bucket.getAvailableTokens();
    }
}
