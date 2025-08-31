package example.alarm.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/api/report/all")
    public ResponseEntity<?> getAllBucket() {
        List<ReportDto> result = reportService.getAllBucket();
        return ResponseEntity.ok(result);
    }
}
