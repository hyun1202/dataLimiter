package example.alarm.bucket;

import io.github.bucket4j.Bandwidth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public enum RatePlan {

    TEST("test") {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofSeconds(10))
                    .build();

        }
    },
    LOCAL("local") {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofSeconds(30))
                    .build();
        }
    },
    PRODUCTION("production") {
        @Override
        public Bandwidth getLimit() {
            return Bandwidth.builder()
                    .capacity(5)
                    .refillIntervally(5, Duration.ofMinutes(30))
                    .build();
        }
    };

    public abstract Bandwidth getLimit();

    private final String planName;

    public static Bandwidth resolvePlan(String targetPlan) {
        if(targetPlan.equals(TEST.getPlanName())) return TEST.getLimit();
        else if(targetPlan.equals(LOCAL.getPlanName())) return LOCAL.getLimit();

        throw new IllegalArgumentException("존재하지 않는 plan입니다.");
    }
}