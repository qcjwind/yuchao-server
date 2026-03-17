package com.hzm.yuchao.biz.controller.app;

import com.hzm.yuchao.biz.config.AppConfig;
import com.hzm.yuchao.simple.base.SimpleResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "APP-配置信息")
@CrossOrigin
@RestController
@RequestMapping("/app/config/")
public class ConfigAppController {

    @Resource
    private AppConfig config;

    @ApiOperation("获取配置信息")
    @PostMapping("getConfig")
    public SimpleResponse<AppConfig> config() {

        return SimpleResponse.ok(config);
    }


}
