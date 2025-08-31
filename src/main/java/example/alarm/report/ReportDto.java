package example.alarm.report;

import example.alarm.BucketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportDto {
    private int key;
    private BucketStatus bucketStatus;
}
