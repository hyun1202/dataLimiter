package example.alarm.bucket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import example.alarm.component.RedisCacheable;
import io.github.bucket4j.Bucket;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BucketStatus implements RedisCacheable {
    @JsonIgnore
    private Bucket bucket;  // localCache만 사용
    private String key;
    private String filename;
    private String message;
    private int availableTokens;     // 남은 토큰 수
    private int maxTokens;           // 최대 토큰 수
    private LocalDateTime nextReset;
    private Duration resetInterval;
    private boolean blocked;         // 차단 여부
    private int requestCount;
    private List<BucketDetail> bucketDetails;

    @Builder
    public BucketStatus(Bucket bucket,
                        String key,
                        String filename,
                        String message,
                        int availableTokens,
                        int maxTokens,
                        Duration resetInterval) {
        this.bucket = bucket;
        this.key = key;
        this.filename = filename;
        this.message = message;
        this.bucketDetails = new ArrayList<>();
        this.availableTokens = availableTokens;
        this.maxTokens = maxTokens;
        this.resetInterval = resetInterval;
        this.nextReset = calculateNextResetTime(resetInterval);
        this.blocked = availableTokens <= 0;
    }

    public synchronized boolean consumeToken() {
        requestCount += 1;
        if (!bucket.tryConsume(1)) {
            blocked = true;
            return false;
        }

        this.availableTokens = (int) bucket.getAvailableTokens();
        this.blocked = this.availableTokens <= 0;

        return true;
    }

    public LocalDateTime calculateNextResetTime(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }

    public void addBucketDetail(BucketDetail detail) {
        bucketDetails.add(detail);
    }

    @Override
    public Duration getResetInteval() {
        return resetInterval;
    }
}