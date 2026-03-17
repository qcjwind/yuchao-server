package com.hzm.yuchao.simple.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class DatabaseDistributedLock {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取分布式锁
     * @param resourceKey 资源标识（如"seat_1001"）
     * @param expireSeconds 锁过期时间（秒），防止死锁
     * @return 锁标识（释放锁时需传入，防止误释放）
     */
    @Transactional
    public String tryLock(String resourceKey, int expireSeconds) {
        // 生成唯一持有者标识（如UUID）
        String holderId = UUID.randomUUID().toString();
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expireSeconds);

        try {
            // 插入锁记录：唯一索引冲突则抛出异常，说明锁已被占用
            jdbcTemplate.update(
                "INSERT INTO distributed_lock (resource_key, holder_id, expire_time) " +
                "VALUES (?, ?, ?)",
                resourceKey, holderId, expireTime
            );
            return holderId; // 成功获取锁，返回持有者标识
        } catch (Exception e) {
            // 唯一索引冲突，说明锁已被占用，尝试续期或直接返回失败
            return tryRenewLock(resourceKey, holderId, expireSeconds);
        }
    }

    /**
     * 尝试续期锁（当锁未过期且持有者是自己时）
     */
    private String tryRenewLock(String resourceKey, String holderId, int expireSeconds) {
        LocalDateTime newExpireTime = LocalDateTime.now().plusSeconds(expireSeconds);
        int updated = jdbcTemplate.update(
            "UPDATE distributed_lock SET expire_time = ?, holder_id = ? " +
            "WHERE resource_key = ? AND expire_time > NOW()",
            newExpireTime, holderId, resourceKey
        );
        return updated > 0 ? holderId : null; // 续期成功返回holderId，否则返回null
    }

    /**
     * 释放分布式锁
     * @param resourceKey 资源标识
     * @param holderId 锁持有者标识（必须与获取时一致，防止误释放）
     * @return 是否释放成功
     */
    @Transactional
    public boolean releaseLock(String resourceKey, String holderId) {
        int deleted = jdbcTemplate.update(
            "DELETE FROM distributed_lock " +
            "WHERE resource_key = ? AND holder_id = ?",
            resourceKey, holderId
        );
        return deleted > 0;
    }

    /**
     * 强制释放过期锁（用于清理异常残留的锁）
     */
    public void forceReleaseExpiredLock() {
        jdbcTemplate.update("DELETE FROM distributed_lock WHERE expire_time <= NOW()");
    }
}