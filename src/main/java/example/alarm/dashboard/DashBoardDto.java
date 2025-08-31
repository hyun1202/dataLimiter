package example.alarm.dashboard;

import example.alarm.BucketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashBoardDto {
    private int key;
    private BucketStatus bucketStatus;
}
