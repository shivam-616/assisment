package com.mittal.shivam.assisment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LockService {
    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String key, Duration timeout) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", timeout));
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
}
