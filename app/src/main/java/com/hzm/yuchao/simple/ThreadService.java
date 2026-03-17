package com.hzm.yuchao.simple;

import com.hzm.yuchao.simple.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Slf4j
@Component
public class ThreadService implements InitializingBean {

    @Value("${threadPool.corePoolSize:2}")
    private int corePoolSize;

    @Value("${threadPool.maximumPoolSize:20}")
    private int maximumPoolSize;

    @Value("${threadPool.queueCapacity:200}")
    private int queueCapacity;

    private ThreadPoolExecutor executor;

    private ScheduledExecutorService scheduledExecutorService;

    public void execute(Runnable runnable) {
        TraceUtils.initTrace();
        executor.execute(runnable);
    }

    public void schedule(Runnable runnable, long delay) {
        TraceUtils.initTrace();
        scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                5, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.AbortPolicy());

        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    }

    @Scheduled(fixedRate = 300_000, initialDelay = 10_000)
    public void monitor() {

        TraceUtils.initTrace();

        long taskCount = executor.getTaskCount();
        long completedTaskCount = executor.getCompletedTaskCount();
        // 活跃线程数
        long activeCount = executor.getActiveCount();
        // 当前线程数
        long poolSize = executor.getPoolSize();
        int queueSize = executor.getQueue().size();

        log.info("线程池监控， 任务总数: {}, 完成数: {}, 当前线程数: {}, 活跃线程数: {}, 队列大小: {}",
                taskCount, completedTaskCount, poolSize, activeCount, queueSize);

    }
}
