package example.alarm.dashboard;

import example.alarm.BucketCache;
import example.alarm.BucketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final BucketCache bucketCache;

    public List<DashBoardDto> getAllBucket() {
        Map<Integer, BucketStatus> bucketMap = bucketCache.getBucketMap();

        List<DashBoardDto> dashBoardDtos = new ArrayList<>();

        for (Map.Entry<Integer, BucketStatus> entry : bucketMap.entrySet()) {
            Integer key = entry.getKey();
            BucketStatus value = entry.getValue();

            DashBoardDto dashBoardDto = new DashBoardDto(key, value);
            dashBoardDtos.add(dashBoardDto);
        }

        return dashBoardDtos;
    }
}
