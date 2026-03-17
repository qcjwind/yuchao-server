package com.hzm.yuchao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * swagger to pdf
 * http://docway.net/dashboard?personal
 * hzm1~6
 */
@Configuration
//@Profile("swagger")
public class Swagger2Config {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                //为当前包路径,控制器类包
                .apis(RequestHandlerSelectors.basePackage("com.hzm.yuchao.biz.controller"))
//                .apis(RequestHandlerSelectors.withMethodAnnotation(GetMapping.class))
//                .apis(RequestHandlerSelectors.withMethodAnnotation(PostMapping.class))
//                .paths(PathSelectors.ant("/app/**"))
//                .paths(PathSelectors.ant("/mng/**"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    // 构建 api文档的详细信息函数
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 页面标题
//                .title("呼和浩特铁路局职教培训演练系统")
                .title("渝超")
                // 版本号
                .version("7.0")
                // 描述
                .description("app/管理后台 统一header token; token通过分别的登录接口获取。</br>请求类型 Content-Type: application/x-www-form-urlencoded")
                .licenseUrl("")
                .termsOfServiceUrl("")
                .contact(new Contact("PRD", "https://www.yuque.com/alipayhosintr1d6/bec9ze/hrcc9l69b549tewd", ""))
                .build();
//                .extensions(Lists.newArrayList(new StringVendorExtension("NodeMediaServer", "https://gitee.com/mirrors/node-media-server#node" +
//                        "-media-server")))
//                .contact(new Contact("推流地址", "rtmp://47.109.88.197:11935/live/test", ""))
//                .contact(new Contact("播放地址", "http://10.113.1.137:11800/live/test.flv", ""))
//                .contact(new Contact("直播回放地址", "http://127.0.0.1:11800/live/test/xxxx.mp4", ""))
    }
}
