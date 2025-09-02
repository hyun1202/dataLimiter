package example.alarm;

import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Bucket4jDataLimiter {
    private final BucketCache bucketCache;

    public boolean consumeToken(BucketDetail info) {
        // 해당 키에 대한 버킷 가져오기 (없으면 생성)
        BucketStatus bucketStatus = bucketCache.getBucketOrCreate(info);
        // 1개 토큰 소비
        return bucketStatus.consumeToken();
    }

    // 현재 상태 조회 (남은 토큰 수)
    public long getRemainingTokens(BucketDetail info) {
        int key = bucketCache.getKey(info.filename(), info.message());
        Bucket bucket = bucketCache.getBucket(key);

        if (bucket == null) return BucketCache.TOKEN_LIMIT;
        
        return bucket.getAvailableTokens();
    }
}
