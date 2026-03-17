package com.hzm.yuchao.simple.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LockDemoService {

    @Autowired
    private DatabaseDistributedLock distributedLock;

    /**
     * 购票（需要保证对座位的并发安全）
     */
    public boolean buyTicket(Long seatId, Long userId) {
        String resourceKey = "seat_" + seatId; // 资源标识：座位ID
        String holderId = null;

        try {
            // 1. 获取锁（过期时间30秒，防止死锁）
            holderId = distributedLock.tryLock(resourceKey, 30);
            if (holderId == null) {
                // 未获取到锁，返回失败（或重试）
                return false;
            }

            // 2. 业务逻辑：检查座位状态、扣减库存、创建订单
            if (!checkSeatAvailable(seatId)) {
                return false;
            }
            return createOrder(seatId, userId);

        } finally {
            // 3. 释放锁（必须在finally中执行，确保锁被释放）
            if (holderId != null) {
                distributedLock.releaseLock(resourceKey, holderId);
            }
        }
    }

    // 检查座位是否可用（实际项目中查询数据库）
    private boolean checkSeatAvailable(Long seatId) {
        // ... 业务逻辑 ...
        return true;
    }

    // 创建订单（实际项目中插入数据库）
    private boolean createOrder(Long seatId, Long userId) {
        // ... 业务逻辑 ...
        return true;
    }
}