package example.alarm.dashboard;

import example.alarm.bucket.caffeine.BucketLocalCache;
import example.alarm.bucket.BucketStatus;
import example.alarm.bucket.redis.BucketRedisCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashBoardService {
    private final BucketLocalCache bucketLocalCache;
    private final BucketRedisCache bucketRedisCache;

    public List<DashBoardDto> getAllBucket() {
        Map<Integer, BucketStatus> bucketMap = bucketLocalCache.getBucketMap();

        List<DashBoardDto> dashBoardDtos = getDashBoardDtos(bucketMap);

        return dashBoardDtos;
    }

    public List<DashBoardDto> getAllBucketRedis() {
        Map<String, BucketStatus> bucketMap = bucketRedisCache.getAllBucketStatuses();

        return getDashBoardDtos(bucketMap);
    }

    private <T> List<DashBoardDto> getDashBoardDtos(Map<T, BucketStatus> bucketMap) {
        List<DashBoardDto> dashBoardDtos = new ArrayList<>();

        for (Map.Entry<T, BucketStatus> entry : bucketMap.entrySet()) {
            T key = entry.getKey();
            BucketStatus value = entry.getValue();

            DashBoardDto dashBoardDto = new DashBoardDto(key, value);
            dashBoardDtos.add(dashBoardDto);
        }
        return dashBoardDtos;
    }
}
