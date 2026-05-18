package com.xarch.cache.redis;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis cache helper
 */
@Component
public class RedisHelper {

    private final RedissonClient redissonClient;

    public RedisHelper(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void set(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, unit);
    }

    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public void delete(String key) {
        redissonClient.getBucket(key).delete();
    }

    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    public boolean tryLock(String key) {
        RLock lock = redissonClient.getLock(key);
        return lock.tryLock();
    }

    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public long increment(String key) {
        return redissonClient.getAtomicLong(key).incrementAndGet();
    }

    public long decrement(String key) {
        return redissonClient.getAtomicLong(key).decrementAndGet();
    }
}