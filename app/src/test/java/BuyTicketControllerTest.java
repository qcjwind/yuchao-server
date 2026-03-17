import ch.qos.logback.classic.Logger;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.simple.TokenDTO;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.utils.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;


import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class BuyTicketControllerTest {

    private static RestTemplate restTemplate = new RestTemplate();

    static volatile boolean start = false;

    private static AtomicInteger ai = new AtomicInteger();

    public static void main(String[] args) {
        // 关键：动态设置日志级别，屏蔽 DEBUG
        disableRestTemplateDebugLog();

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                while (!start) {
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                for (int j = 0; j < 500; j++) {
                    testBuyTicketWithFormData();

                    if (ai.get() > 1000) {
                        return;
                    }
                }
            }).start();
        }

        start = true;
    }

    public static String testBuyTicketWithFormData() {

        int ticketNum = (int) (Math.random() * 2) + 1;

        // 1. 构建 form-data 参数（关键：嵌套列表用 "list[索引].属性" 格式）
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        // 简单字段
        formData.add("skuId", 79 + (int)(Math.random() * 6));
        formData.add("requestNo", UuidUtils.uuid16());

        for (int i = 0; i < ticketNum; i++) {
            // 嵌套列表：list[0] 第一个购票人
            formData.add("list[" + i + "].name", "张三");
            formData.add("list[" + i + "].idType", "PASSPORT");
            formData.add("list[" + i + "].idNo", "A" + Math.random());
            formData.add("list[" + i + "].mobile", "13800138000");
        }


        TokenDTO tokenDTO = new TokenDTO((long) (Math.random() * 99999999), "", TokenDTO.PlatformEnum.APP, System.currentTimeMillis());
        String token = SecureUtil.aes(Constants.AES_PASSWORD).encryptBase64(JSONObject.toJSONBytes(tokenDTO));

        // 2. 设置请求头（form-data格式）
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA); // 关键：指定为multipart/form-data
        headers.set("token", token);

        // 3. 构建请求实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, headers);

        long startTime = System.currentTimeMillis();

        // 4. 发送POST请求（替换为你的接口路径）
        ResponseEntity<String> response = restTemplate.postForEntity(
//                "http://127.0.0.1/app/order/buySaleTicket",
                "https://yuchao2025.zszlchina.com/app/order/buySaleTicket",
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
}
    