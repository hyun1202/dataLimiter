package example.alarm.component;

import java.time.Duration;

public interface RedisCacheable {
    Duration getResetInteval();
}
