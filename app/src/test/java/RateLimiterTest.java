import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.RateLimiter;
import com.hzm.yuchao.simple.TokenDTO;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.utils.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class RateLimiterTest {

    private static RestTemplate restTemplate = new RestTemplate();

    static volatile boolean start = false;

    private static AtomicInteger ai = new AtomicInteger();

    public static void main(String[] args) {
        // 关键：动态设置日志级别，屏蔽 DEBUG
        disableRestTemplateDebugLog();

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (!start) {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                for (int j = 0; j < 50; j++) {
                    testRateLimit();

                    if (ai.get() > 1000) {
                        return;
                    }
                }
            }).start();
        }

        start = true;
    }

    public static String testRateLimit() {

        int ticketNum = (int) (Math.random() * 2) + 1;

        // 1. 构建 form-data 参数（关键：嵌套列表用 "list[索引].属性" 格式）
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

        // 2. 设置请求头（form-data格式）
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA); // 关键：指定为multipart/form-data

        // 3. 构建请求实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

        long startTime = System.currentTimeMillis();

        // 4. 发送POST请求（替换为你的接口路径）
        ResponseEntity<String> response = restTemplate.postForEntity(
//                "http://127.0.0.1/app/order/buySaleTicket",
                "https://yuchao2025.zszlchina.com/app/match/list",
                requestEntity,
                String.class
        );

        // 5. 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("【{}】张票，耗时: {}, 接口响应：{}", ticketNum, System.currentTimeMillis() - startTime, response.getBody());

        if (response.getBody().contains(":500,")) {
            ai.incrementAndGet();
        }

        return response.getBody();
    }

    /**
     * 关闭 RestTemplate 及底层 HTTP 客户端的 DEBUG 日志
     */
    private static void disableRestTemplateDebugLog() {
        // 1. 关闭 RestTemplate 自身日志
        Logger restTemplateLogger = (Logger) LoggerFactory.getLogger("org.springframework.web.client.RestTemplate");
        restTemplateLogger.setLevel(Level.INFO);

        // 2. 关闭 JDK HttpURLConnection 日志（默认客户端）
        Logger jdkHttpLogger = (Logger) LoggerFactory.getLogger("sun.net.www.protocol.http.HttpURLConnection");
        jdkHttpLogger.setLevel(Level.INFO);

        // 3. 若使用 Apache HttpClient，关闭其日志
        Logger apacheHttpLogger = (Logger) LoggerFactory.getLogger("org.apache.http");
        apacheHttpLogger.setLevel(Level.INFO);

        // 3. 若使用 Apache HttpClient，关闭其日志
        Logger httpLogging = (Logger) LoggerFactory.getLogger("org.springframework.web.HttpLogging");
        httpLogging.setLevel(Level.INFO);
    }


    public static void main2(String[] args) throws Exception {

        // 如果qps是5，则没0.2s一个请求
        RateLimiter r1 = RateLimiter.create(5); // 每秒最多5个请求
        Thread.sleep((long) 2000);
        long startTime = System.nanoTime();
        int requests = 15; // 总共需要发送的请求数
        for (int i = 0; i < requests; i++) {
            boolean result = r1.tryAcquire(); // 获取许可并等待
            log.info("Request {}, result: {} sent at {} seconds", i + 1, result, (System.nanoTime() - startTime) / 1_000_000_000.0);
//            Thread.sleep((long) (Math.random() * 15));

            if (i==5) {
                Thread.sleep((long) 2000);
            }
        }
        System.out.println("--------------");


        // 如果qps是5，则没0.2s一个请求
        RateLimiter r2 = RateLimiter.create(5, 1, TimeUnit.SECONDS); // 每秒最多5个请求
        Thread.sleep((long) 2000);
        startTime = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            boolean result = r2.tryAcquire(); // 获取许可并等待
            log.info("Request {}, result: {} sent at {} seconds", i + 1, result, (System.nanoTime() - startTime) / 1_000_000_000.0);


            if (i==5) {
                Thread.sleep((long) 2000);
            }
        }

//        new RateLimitAspect().testSmoothBursty3();
    }

    private void log(Object o) {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()) + " " + o);
    }

    public void testSmoothBursty3() throws InterruptedException {
        RateLimiter r = RateLimiter.create(5);
//        RateLimiter r = RateLimiter.create(5, 1, TimeUnit.SECONDS);
        Thread.sleep(5L);
        log("");
        while (true) {
            log("get 5 tokens: " + r.acquire(5)+ "s");
            log("get 1 tokens: " + r.acquire(1) + "s");
            log("get 1 tokens: " + r.acquire(1) + "s");
            log("get 1 tokens: " + r.acquire(1) + "s");
            log("end");
            /**
             * output:
             * 2020-12-31 15:23:38:353 get 5 tokens: 0.0s
             * 2020-12-31 15:23:39:307 get 1 tokens: 0.944744s 滞后效应，需要替前一个请求进行等待
             * 2020-12-31 15:23:39:503 get 1 tokens: 0.191202s
             * 2020-12-31 15:23:39:699 get 1 tokens: 0.195371s
             * 2020-12-31 15:23:39:699 end
             * 2020-12-31 15:23:39:903 get 5 tokens: 0.199419s
             * 2020-12-31 15:23:40:904 get 1 tokens: 0.995646s 滞后效应，需要替前一个请求进行等待
             * 2020-12-31 15:23:41:101 get 1 tokens: 0.1936s
             * 2020-12-31 15:23:41:305 get 1 tokens: 0.197227s
             */
        }
    }
}
    