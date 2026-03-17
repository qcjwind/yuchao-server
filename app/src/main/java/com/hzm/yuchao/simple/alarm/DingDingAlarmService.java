package com.hzm.yuchao.simple.alarm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class DingDingAlarmService implements AlarmService {

    private static final String WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=";

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void alarm(String content) {

        try {

            DingTalkMessage message = new DingTalkMessage();
            message.setText(new TextContent("告警：" + content));

            // 3. 处理请求头（设置为JSON格式）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 5. 发送POST请求
            HttpEntity<DingTalkMessage> request = new HttpEntity<>(message, headers);
            String response = restTemplate.postForObject(WEBHOOK, request, String.class);

            // 6. 打印响应结果（errcode=0表示成功）
            log.info("dingding告警 response：" + response);

        } catch (Exception e) {
            log.error("告警失败");
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DingTalkMessage {

        // 消息类型：文本
        private String msgtype = "text";

        private TextContent text;

        private AtInfo at;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TextContent {
        // 消息内容（需包含关键词，若设置）
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AtInfo {
        // 被@的手机号列表
        private List<String> atMobiles;

        // 是否@所有人
        private boolean isAtAll = false;
    }

}
