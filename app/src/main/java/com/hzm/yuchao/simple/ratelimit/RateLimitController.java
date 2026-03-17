package com.hzm.yuchao.simple.ratelimit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 限流注解使用示例
 */
@RestController
@RequestMapping("/demo/ratelimit/")
public class RateLimitController {

    /**
     * 示例1：限制每秒最多2个请求，不等待
     */
    @GetMapping("/test1")
    @RateLimit(qps = 2, message = "test1接口请求过于频繁")
    public String test1() {
        return "test1接口调用成功";
    }

    /**
     * 示例2：限制每秒最多5个请求，最大突发10个，最多等待500ms
     */
    @GetMapping("/test2")
    @RateLimit(qps = 5, timeout = 500, timeUnit = TimeUnit.MILLISECONDS)
    public String test2() {
        return "test2接口调用成功";
    }
}
    