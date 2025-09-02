package example.alarm.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DashBoardController {
    private final DashBoardService dashBoardService;

    @GetMapping("/api/report/all")
    public ResponseEntity<?> getAllBucket() {
        List<DashBoardDto> result = dashBoardService.getAllBucket();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/report/redis/all")
    public ResponseEntity<?> getAllBucketRedis() {
        List<DashBoardDto> result = dashBoardService.getAllBucketRedis();
        return ResponseEntity.ok(result);
    }
}
