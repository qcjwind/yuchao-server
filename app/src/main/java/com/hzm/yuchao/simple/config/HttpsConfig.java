package com.hzm.yuchao.simple.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 同时开启 http 和 https
 */
@Configuration
public class HttpsConfig {

    /**
     * HTTP 端口。
     * 生产环境默认 80，本地开发在 application-dev.yml 中覆盖为 18080。
     */
    @Value("${app.http.port:80}")
    private int httpPort;

    /**
     * 配置 HTTP 端口（如 8080）
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(org.apache.catalina.Context context) {
                // 可选：设置安全约束，强制部分路径使用 HTTPS
            }
        };
        // 添加 HTTP 连接器
        tomcat.addAdditionalTomcatConnectors(createStandardConnector());
        return tomcat;
    }

    /**
     * 创建 HTTP 连接器
     */
    private Connector createStandardConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        return connector;
    }
}
    