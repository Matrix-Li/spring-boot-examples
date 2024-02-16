package com.neo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 **/
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
public class RedisService {
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Long timeout, final TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 判断key是否存在
     *
     * @param key 缓存的键值
     * @return boolean
     **/
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection) {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList, long ttlInSeconds) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
        return count == null ? 0 : count;
    }

    /**
     * 入队列
     *
     * @param key  缓存的键值
     * @param data 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long rightPush(final String key, final T data, long ttlInSeconds) {
        Long count = redisTemplate.opsForList().rightPush(key, data);
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
        return count == null ? 0 : count;
    }

    /**
     * 出队列
     *
     * @param key 缓存的键值
     * @return 缓存的对象
     */
    public <T> T leftPop(final String key, Class<T> clazz) {
        T result = (T) redisTemplate.opsForList().leftPop(key);
        return result;
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long replaceCacheList(final String key, final List<T> dataList) {
        redisTemplate.delete(key);
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 缓存data数据
     *
     * @param key  缓存的键值
     * @param data 待缓存的数据
     * @return 缓存的对象
     */
    public <T> long putCacheList(final String key, final T data) {
        Long count = redisTemplate.opsForList().rightPush(key, data);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheFromList(final String key, int index) {
        List<T> list = redisTemplate.opsForList().range(key, index, index);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 缓存Data
     *
     * @param key  缓存键值
     * @param data 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> Long putCacheSet(final String key, final T data) {
        return redisTemplate.opsForSet().add(key, data);
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value, long ttlInSeconds) {
        redisTemplate.opsForHash().put(key, hKey, value);
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 自增
     *
     * @param key
     * @return
     */
    public Long generate(String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return entityIdCounter.getAndIncrement();
    }

    /**
     * 自增
     *
     * @param key
     * @return
     */
    public Long getLong(String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        return entityIdCounter.get();
    }

    public void convertAndSend(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }

    /**
     * 频率限制 移动窗口
     *
     * @param key
     * @param period
     * @param maxCount
     * @return
     */
    public boolean rateLimit(String key, int period, int maxCount) {
        long nowTs = System.currentTimeMillis();
        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.openPipeline();
            // connection.zAdd(key.getBytes(), (double) nowTs, ("" + nowTs).getBytes());  如果写在这表示当前请求也需要统计，表示限定时间内不能再次触发
            connection.zRemRangeByScore(key.getBytes(), 0, nowTs - period * 1000);
            connection.zCard(key.getBytes());
            connection.expire(key.getBytes(), period + 1); // 预防冷数据过期场景
            return null;
        });
        if (CollectionUtils.isEmpty(objects)) {
            return true;
        }
        boolean limit = (Long) objects.get(1) < maxCount;
        if (limit) {
            redisTemplate.opsForZSet().add(key, "" + nowTs, nowTs);
        }
        return limit;
    }

}

