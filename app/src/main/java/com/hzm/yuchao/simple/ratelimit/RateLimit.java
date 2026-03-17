package com.hzm.yuchao.simple.ratelimit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流QPS（每秒允许的请求数）
     */
    double qps();

    /**
     * 获取令牌超时时间
     */
    long timeout() default 0;

    /**
     * 超时时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 限流提示信息
     */
    String message() default "系统繁忙，请稍后再试";
}
    