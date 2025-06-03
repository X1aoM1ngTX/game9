package com.xm.game9.utils;

import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtil {
    private volatile static RedisUtil instance;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private RedisUtil() {
        // 私有构造函数
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

    @PostConstruct
    public void init() {
        instance = this;
        log.info("RedisUtil 初始化完成，当前环境: {}", activeProfile);
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
     * 设置位图中的值
     *
     * @param key    键
     * @param offset 偏移量
     * @param value  值
     */
    public void setBit(String key, int offset, boolean value) {
        try {
            stringRedisTemplate.opsForValue().setBit(key, offset, value);
            log.info("Redis设置位图中的值成功: key={}, offset={}, value={}", key, offset, value);
        } catch (Exception e) {
            log.error("Redis设置位图中的值失败: key={}, offset={}, value={}, error={}", key, offset, value, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 获取位图中的值
     *
     * @param key    键
     * @param offset 偏移量
     * @return Boolean 是否签到
     */
    public Boolean getBit(String key, int offset) {
        try {
            Boolean result = stringRedisTemplate.opsForValue().getBit(key, offset);
            if (result == null) {
                log.warn("Redis获取位图中的值为null: key={}, offset={}", key, offset);
                return false;
            }
            return result;
        } catch (Exception e) {
            log.error("Redis获取位图中的值失败: key={}, offset={}, error={}", key, offset, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 获取位图中1的个数
     *
     * @param key 键
     * @return 位图中1的个数
     */
    public long bitCount(String key) {
        try {
            Long result = stringRedisTemplate
                    .execute((RedisCallback<Long>) connection -> connection.stringCommands().bitCount(key.getBytes()));
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.error("Redis bitCount 操作失败，key={}, error={}", key, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis操作失败");
        }
    }

    /**
     * 检查Redis连接
     *
     * @throws BusinessException    如果Redis连接失败，抛出异常
     * @throws NullPointerException 如果StringRedisTemplate未注入，抛出异常
     * @throws NullPointerException 如果RedisConnectionFactory未配置，抛出异常
     * @throws Exception            如果Redis连接失败，抛出异常
     */
    public void checkConnection() {
        if (stringRedisTemplate == null) {
            log.error("Redis配置错误: StringRedisTemplate未注入");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis配置错误");
        }

        RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            log.error("Redis配置错误: RedisConnectionFactory未配置");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis配置错误");
        }

        try {
            connectionFactory.getConnection().ping();
            log.debug("Redis连接检查成功");
        } catch (Exception e) {
            log.error("Redis连接失败，请确保Redis服务已启动: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis服务未启动");
        }
    }

}