package com.devteria.Demo_Spring_boot.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {
    RedisTemplate<String, Object> redisTemplate;

    public void setValue(String key, Object value, long timeOutInMinutes){
        redisTemplate.opsForValue().set(key, value, timeOutInMinutes, TimeUnit.MINUTES);
    }

    public Object getValue(String key){
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteValue(String key){
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
