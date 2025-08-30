package example.alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AlarmController {
    private final Bucket4jDuplicateDataLimiter bucket4jDuplicateDataLimiter;

    @GetMapping("/api/alarm")
    public ResponseEntity<?> test(
            @RequestParam String filename,
            @RequestParam String message) {
        long remainingTokens = bucket4jDuplicateDataLimiter.getRemainingTokens(filename, message);
        log.info("남은 토큰 수={}", remainingTokens);
        return null;
    }
}
