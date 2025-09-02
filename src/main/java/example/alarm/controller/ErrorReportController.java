package example.alarm.controller;

import example.alarm.bucket.Bucket4jDataLimiter;
import example.alarm.bucket.BucketInfo;
import example.alarm.bucket.redis.BucketRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> reportErrorCache(BucketInfo info) {
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
    public ResponseEntity<?> reportError(BucketInfo info) {
        if (limiter.consumeToken(info) == null) {
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

    void processError(BucketInfo info) {
        log.info("filename={}, message={}", info.filename(), info.message());
    }
}