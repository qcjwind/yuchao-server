package com.hzm.yuchao.simple;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthCheck")
    public String healthCheck() {
        return "server is ok";
    }

    @GetMapping("/gift")
    public String gift() {
        return "<h1 style=\"text-align:center; font-size:6vw; margin:50% 0; color:#333; padding:0 20px\"> 请使用微信扫码</h1>";
    }
}
