package example.alarm.dashboard;

import example.alarm.bucket.BucketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashBoardDto {
    private Object key;
    private BucketStatus bucketStatus;
}
