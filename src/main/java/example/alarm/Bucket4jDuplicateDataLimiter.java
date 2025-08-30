package example.alarm;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class Bucket4jDuplicateDataLimiter {
    private final BucketMap bucketMap;

    public boolean isAllowed(String filename, String message) {
        // 키 생성 (파일명 + 메시지)
        int key = (filename + "|" + message).hashCode();

        // 해당 키에 대한 버킷 가져오기 (없으면 생성)
        Bucket bucket = bucketMap.getBucketOrCreate(key);

        // 1개 토큰 소비
        return bucket.tryConsume(1);
    }

    // 현재 상태 조회 (남은 토큰 수)
    public long getRemainingTokens(String filename, String message) {
        int key = (filename + "|" + message).hashCode();
        Bucket bucket = bucketMap.getBucket(key);

        if (bucket == null) return BucketMap.TOKEN_LIMIT;
        
        return bucket.getAvailableTokens();
    }
}
