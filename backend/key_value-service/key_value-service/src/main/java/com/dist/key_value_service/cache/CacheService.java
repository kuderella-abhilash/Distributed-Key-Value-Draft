package com.dist.key_value_service.cache;

import com.dist.key_value_service.dto.KVResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final RedisTemplate<String, KVResponse> redisTemplate;

    public void put(String key, KVResponse response) {
        redisTemplate.opsForValue().set(
                "kv:value:" + key, response, 5, TimeUnit.MINUTES
        );
    }

    public Optional<KVResponse> get(String key) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get("kv:value:" + key)
        );
    }

    public void evict(String key) {
        redisTemplate.delete("kv:value:" + key);
    }

//    private final RedisTemplate<String, KVResponse> redisTemplate;
//
//    public void put(String key, KVResponse value) {
//        redisTemplate.opsForValue().set(key, value);
//    }
//
//    public Optional<KVResponse> get(String key) {
//        return Optional.ofNullable(
//                redisTemplate.opsForValue().get(key)
//        );
//    }
//
//    public void evict(String key) {
//        redisTemplate.delete(key);
//    }
}