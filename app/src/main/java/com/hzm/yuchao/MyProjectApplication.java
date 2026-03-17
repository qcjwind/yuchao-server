package com.hzm.yuchao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

// @MapperScan 与 @Mapper 注解2选1，有一个即可
//@MapperScan(basePackages = {"com.hzm.yuchao.biz.mapper", "com.hzm.yuchao.simple.systemconfig.mapper"})
@EnableSwagger2
@EnableScheduling
@ServletComponentScan
@SpringBootApplication
public class MyProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyProjectApplication.class, args);
    }
}
