package com.hzm.yuchao.simple.ratelimit;

import com.google.common.util.concurrent.RateLimiter;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 限流切面（AOP实现）
 * 优先级小于其他切面
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    /**
     * 存储不同方法的限流器（key: 方法签名，value: 限流器）
     */
    private final ConcurrentMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    private final RateLimiter globalRateLimiter = RateLimiter.create(Constants.GLOBAL_QPS);

    /**
     * 环绕通知：在方法执行前进行限流判断
     */
    @Around("@annotation(com.hzm.yuchao.simple.ratelimit.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!globalRateLimiter.tryAcquire()) {
            // 获取令牌失败，抛出限流异常
            throw new BizException("系统繁忙");
        }

        // 1. 获取方法上的限流注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 2. 生成唯一标识（类名+方法名）
        String key = method.getDeclaringClass().getName() + "#" + method.getName();

        // 3. 获取或创建限流器
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(key,
            k -> RateLimiter.create(rateLimit.qps()));

        // 4. 尝试获取令牌
        boolean acquired;
        if (rateLimit.timeout() <= 0) {
            // 非阻塞模式：立即返回
            acquired = rateLimiter.tryAcquire();
        } else {
            // 阻塞模式：等待超时时间
            acquired = rateLimiter.tryAcquire(rateLimit.timeout(), rateLimit.timeUnit());
        }

        // 5. 根据结果处理
        if (acquired) {
            // 获取令牌成功，执行原方法
            return joinPoint.proceed();
        } else {
            // 获取令牌失败，抛出限流异常
            throw new BizException(rateLimit.message());
        }
    }

}
    