package example.alarm.bucket;

public record BucketInfo(
        String filename,
        String message,
        String trace,
        String uri,
        String wpJson
) {
}
