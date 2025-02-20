package com.xm.gamehub.utils;

import com.xm.gamehub.common.ErrorCode;
import com.xm.gamehub.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {
    private volatile static RedisUtil instance;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private RedisUtil() {
        // 私有构造函数
    }

    @PostConstruct
    public void init() {
        instance = this;
        log.info("RedisUtil 初始化完成");
    }

    public static RedisUtil getInstance() {
        if (instance == null) {
            synchronized (RedisUtil.class) {
                if (instance == null) {
                    instance = new RedisUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 设置带过期时间的字符串值
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void setWithExpire(String key, String value, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("Redis设置值失败: key={}, value={}, error={}", key, value, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis获取值失败: key={}, error={}", key, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 删除键
     *
     * @param key 键
     */
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis删除键失败: key={}, error={}", key, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("Redis设置过期时间失败: key={}, timeout={}, error={}", key, timeout, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis检查键是否存在失败: key={}, error={}", key, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 检查Redis连接
     */
    public void checkConnection() {
        try {
            stringRedisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            log.error("Redis连接失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis服务未启动");
        }
    }
} 