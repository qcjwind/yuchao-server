package com.hzm.yuchao.simple.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BidGenerator {
    // 序列号（每毫秒内自增）
    private final AtomicInteger sequence = new AtomicInteger(0);
    // 上次生成ID的时间戳（毫秒）
    private long lastTimestamp = -1L;
    // 时间戳掩码（48位 → 2^48-1，确保只取后48位）
    private static final long TIMESTAMP_MASK = 0xFFFFFFFFFFFFL;
    // 序列号最大值（12位 → 4095）
    private static final int MAX_SEQUENCE = 0xFFF;
    // 随机数掩码（4位 → 15）
    private static final int RANDOM_MASK = 0xF;

    // 单例模式（避免多实例导致的序列号冲突）
    private static final BidGenerator INSTANCE = new BidGenerator();

    private BidGenerator() {}

    public static BidGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * 生成16位不重复ID（16进制字符串，0-9、a-f）
     */
    public String generate(String prefix) {
        return prefix + generate();
    }

    /**
     * 生成16位不重复ID（16进制字符串，0-9、a-f）
     */
    public synchronized String generate() {
        long timestamp = System.currentTimeMillis();

        // 处理时钟回拨（若当前时间小于上次时间，等待至下一毫秒）
        if (timestamp < lastTimestamp) {
            try {
                Thread.sleep(lastTimestamp - timestamp);
                timestamp = System.currentTimeMillis();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("时钟回拨导致ID生成失败", e);
            }
        }

        // 同一毫秒内序列号自增，超过最大值则等待下一毫秒
        if (timestamp == lastTimestamp) {
            int currentSeq = sequence.incrementAndGet();
            if (currentSeq > MAX_SEQUENCE) {
                // 序列号用尽，等待至下一毫秒
                do {
                    timestamp = System.currentTimeMillis();
                } while (timestamp == lastTimestamp);
                sequence.set(0); // 重置序列号
            }
        } else {
            // 新的毫秒，重置序列号
            sequence.set(0);
        }

        lastTimestamp = timestamp;

        // 提取时间戳后48位（12位16进制）
        long timePart = timestamp & TIMESTAMP_MASK;
        // 序列号（12位 → 3位16进制）
        int seqPart = sequence.get() & MAX_SEQUENCE;
        // 随机数（4位 → 1位16进制）
        int randomPart = (int) (Math.random() * (RANDOM_MASK + 1));

        // 组合为64位数字：timePart(48) | seqPart(12) | randomPart(4)
        long id = (timePart << 16) | (seqPart << 4) | randomPart;

        // 转换为16位16进制字符串（不足补0）
        return String.format("%016x", id);
    }

    // 测试：验证高并发下的唯一性
    public static void main(String[] args) {
        BidGenerator generator = BidGenerator.getInstance();
        ConcurrentMap<String, Integer> idMap = new ConcurrentHashMap<>();

        // 启动20个线程，每个线程生成10000个ID
        Runnable task = () -> {
            for (int i = 0; i < 10000; i++) {
                String id = generator.generate();
                idMap.put(id, idMap.getOrDefault(id, 0) + 1);
            }
        };

        for (int i = 0; i < 20; i++) {
            new Thread(task).start();
        }

        // 等待所有线程完成
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 统计结果
        long total = idMap.size();
        long duplicates = idMap.values().stream().filter(count -> count > 1).count();
        System.out.println("生成ID总数：" + total);
        System.out.println("重复ID数量：" + duplicates);
        System.out.println("示例ID：" + generator.generate());
    }
}
    