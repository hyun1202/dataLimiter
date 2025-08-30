package example.alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ErrorReportController {
    
    private final Bucket4jDuplicateDataLimiter limiter;
    
    @GetMapping("/api/error")
    public ResponseEntity<?> reportError(
            @RequestParam String filename,
            @RequestParam String message) {
        
        if (!limiter.isAllowed(filename, message)) {
            long remaining = limiter.getRemainingTokens(filename, message);
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                        "message", "동일한 에러가 너무 많이 발생했습니다.",
                        "limit", "30분간 최대 5회",
                        "remaining", remaining,
                        "retryAfter", "30분 후"
                    ));
        }
        
        // 에러 처리
        processError(filename, message);
        
        return ResponseEntity.ok(Map.of(
            "message", "에러 접수 완료",
            "remaining", limiter.getRemainingTokens(filename, message)
        ));
    }

    void processError(String filename, String message) {
        log.info("filename={}, message={}", filename, message);
    }
}