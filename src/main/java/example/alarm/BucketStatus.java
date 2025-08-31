package example.alarm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import example.alarm.report.AlarmInfo;
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

    @Builder
    public BucketStatus(Bucket bucket,
                        AlarmInfo alarmInfo,
                        int availableTokens,
                        int maxTokens,
                        LocalDateTime nextReset) {
        this.bucket = bucket;
        this.alarmInfo = alarmInfo;
        this.availableTokens = availableTokens;
        this.maxTokens = maxTokens;
        this.nextReset = nextReset;
        this.blocked = maxTokens - availableTokens <= 0;
    }

    public synchronized void resetToken() {
        this.availableTokens = maxTokens;
        blocked = false;
    }

    public synchronized void consumeToken() {
        if (availableTokens < 0) {
            blocked = true;
            return;
        }
        this.availableTokens -= 1;
    }

    public void resetExpiredTime(Duration duration) {
        this.nextReset = LocalDateTime.now().plus(duration);
    }
}