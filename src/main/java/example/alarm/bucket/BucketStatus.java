package example.alarm.bucket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import example.alarm.component.RedisCacheable;
import io.github.bucket4j.Bucket;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class BucketStatus implements RedisCacheable {
    @JsonIgnore
    private final Bucket bucket;
    private final String uuid;
    private final BucketDetail bucketDetail;
    private int availableTokens;     // 남은 토큰 수
    private final int maxTokens;           // 최대 토큰 수
    private final LocalDateTime nextReset;
    private final Duration resetInterval;
    private boolean blocked;         // 차단 여부
    private int requestCount;

    @Builder
    public BucketStatus(Bucket bucket,
                        String uuid,
                        BucketDetail bucketDetail,
                        int availableTokens,
                        int maxTokens,
                        Duration resetInterval) {
        this.bucket = bucket;
        this.uuid = uuid;
        this.bucketDetail = bucketDetail;
        this.availableTokens = availableTokens;
        this.maxTokens = maxTokens;
        this.resetInterval = resetInterval;
        this.nextReset = calculateNextResetTime(resetInterval);
        this.blocked = availableTokens <= 0;
    }

    public synchronized void resetToken() {
        this.availableTokens = maxTokens;
        blocked = false;
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

    @Override
    public Duration getRedisExpiration() {
        return resetInterval;
    }
}