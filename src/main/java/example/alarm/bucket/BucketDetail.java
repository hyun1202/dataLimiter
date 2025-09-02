package example.alarm.bucket;

import java.time.LocalDateTime;
import java.util.UUID;

public record BucketDetail(
        String uuid,
        String trace,
        String uri,
        String wpJson,
        LocalDateTime createdAt
) {
    public static BucketDetail from(BucketInfo info) {
        return new BucketDetail(
                UUID.randomUUID().toString().substring(0, 7),
                info.trace(),
                info.uri(),
                info.wpJson(),
                LocalDateTime.now()
        );
    }
}
