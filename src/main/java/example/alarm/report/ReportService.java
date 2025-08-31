package example.alarm.report;

import example.alarm.BucketCache;
import example.alarm.BucketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final BucketCache bucketCache;

    public List<ReportDto> getAllBucket() {
        Map<Integer, BucketStatus> bucketMap = bucketCache.getBucketMap();

        List<ReportDto> reportDtos = new ArrayList<>();

        for (Map.Entry<Integer, BucketStatus> entry : bucketMap.entrySet()) {
            Integer key = entry.getKey();
            BucketStatus value = entry.getValue();

            ReportDto reportDto = new ReportDto(key, value);
            reportDtos.add(reportDto);
        }

        return reportDtos;
    }
}
