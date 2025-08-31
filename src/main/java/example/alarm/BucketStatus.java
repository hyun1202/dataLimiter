package example.alarm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.bucket4j.Bucket;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class BucketStatus {
    @JsonIgnore
    private final Bucket bucket;
    private final AlarmInfo alarmInfo;
    private int availableTokens;     // 남은 토큰 수
    private final int maxTokens;           // 최대 토큰 수
    private LocalDateTime nextReset;
    private boolean blocked;         // 차단 여부
    private int requestCount;

    @Builder
    public BucketStatus(Bucket bucket,
                        AlarmInfo alarmInfo,
                        int availableTokens,
                        int maxTokens,
                        Duration expiredTime) {
        this.bucket = bucket;
        this.alarmInfo = alarmInfo;
        this.availableTokens = availableTokens;
        this.maxTokens = maxTokens;
        this.nextReset = resetExpiredTime(expiredTime);
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

        this.availableTokens -= 1;
        return true;
    }

    public LocalDateTime resetExpiredTime(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }
}