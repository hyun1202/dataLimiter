package example.alarm;

import example.alarm.bucket.Bucket4jDataLimiter;
import example.alarm.bucket.BucketDetail;
import example.alarm.bucket.BucketRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ErrorReportController {
    
    private final Bucket4jDataLimiter limiter;
    private final BucketRedisCacheService cacheService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception e) {
        log.error("", e);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "message", "동일한 에러가 너무 많이 발생했습니다.",
                        "limit", "30분간 최대 5회",
                        "remaining", 0,
                        "retryAfter", "30분 후"
                ));
    }

    @GetMapping("/api/error-cache")
    public ResponseEntity<?> reportErrorCache(BucketDetail info) {
        if (!cacheService.consumeToken(info)) {
            throw new RuntimeException("too many request.. fileName: " + info.filename());
        }

        // 에러 처리
        processError(info);
        long remainingTokens = cacheService.getRemainingTokens(info);


        return ResponseEntity.ok(Map.of(
                "message", "에러 접수 완료",
                "remaining", remainingTokens
        ));
    }

    @GetMapping("/api/error")
    public ResponseEntity<?> reportError(BucketDetail info) {
        if (!limiter.consumeToken(info)) {
            throw new RuntimeException("too many request.. fileName: " + info.filename());
        }
        
        // 에러 처리
        processError(info);
        long remainingTokens = limiter.getRemainingTokens(info);


        return ResponseEntity.ok(Map.of(
            "message", "에러 접수 완료",
            "remaining", remainingTokens
        ));
    }

    void processError(BucketDetail info) {
        log.info("filename={}, message={}", info.filename(), info.message());
    }
}