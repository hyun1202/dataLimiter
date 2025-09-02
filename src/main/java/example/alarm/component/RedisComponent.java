package example.alarm.component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import example.alarm.bucket.BucketStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Component
@Slf4j
public class RedisComponent {
    private RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisComponent(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // null 값 무시하여 JSON 크기 줄이기
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void putValue(String key, Object value, Duration expiration) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue, expiration);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize BucketStatus", e);
        }
    }

    public <T> T getValue(String key, Class<T> type) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);

            if (jsonValue == null) {
                return null;
            }

            return objectMapper.readValue(jsonValue, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize BucketStatus", e);
        }
    }

    // prefix에 해당하는 모든 BucketStatus 가져오기
    public Map<String, BucketStatus> getAllBucketsByPrefix(String prefix) {
        return getAllByPrefix(prefix, BucketStatus.class);
    }

    public <T> Map<String, T> getAllByPrefix(String keyPrefix, Class<T> valueType) {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        Map<String, T> result = new HashMap<>();

        if (keys != null && !keys.isEmpty()) {
            List<String> values = redisTemplate.opsForValue().multiGet(keys);

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.toArray(new String[0])[i];
                String jsonValue = values.get(i);

                if (jsonValue != null) {
                    try {
                        T value = objectMapper.readValue(jsonValue, valueType);
                        result.put(key, value);
                    } catch (JsonProcessingException e) {
                        // 로그 남기고 해당 키는 스킵
                        log.warn("Failed to deserialize value for key: {}", key, e);
                    }
                }
            }
        }

        return result;
    }

    public <T extends RedisCacheable> T getValue(String key, Class<T> type, Function<String, T> mappingFunction) {
        T cached = getValue(key, type);
        if (cached != null) {
            return cached;
        }

        T newValue = mappingFunction.apply(key);
        putValue(key, newValue, newValue.getResetInteval());
        return newValue;
    }

    // 데이터 존재 여부 확인
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 데이터 삭제
    public void remove(String key) {
        redisTemplate.delete(key);
    }

    // 모든 key 데이터 삭제
    public void clearAll(String key) {
        Set<String> keys = redisTemplate.keys( key + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

}
