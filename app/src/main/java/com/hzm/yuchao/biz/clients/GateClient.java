package com.hzm.yuchao.biz.clients;

import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.LogTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GateClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gate.nameVerify.url}")
    private String nameVerifyUrl;

    @Value("${gate.nameVerify.token}")
    private String nameVerifyToken;

    /**
     * 实名认证
     * @param request
     * @return
     */
    public boolean nameVerify(GateNameVerifyRequest request) {
         send(nameVerifyUrl, nameVerifyToken, "NAME_VERIFY", JSONObject.toJSONString(request));

        return true;
    }

    /**
     * 推送闸机
     * @param request
     * demo: http://yc2.zszlchina.com/es-server/api/push/person
     * @return
     */
    public void pushPerson(String url, String token, GatePushPersonRequest request) {
        send(url + "/api/push/person", token, "PUSH_PERSON", JSONObject.toJSONString(request));
    }

    /**
     * 推送闸机
     * @param request
     * @return
     */
    public void refundPush(String url, String token, GateRefundPushRequest request) {
        send(url + "/api/push/refund", token, "REFUND_PUSH", JSONObject.toJSONString(request));
    }

    /**
     *
     * @return
     */
    private String send(String url, String token, String type, String bodyStr) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("Accept", "application/json");
        httpHeaders.add("Token", token);

        StringBuilder digest = new StringBuilder();
        StringBuilder realResponse = new StringBuilder();

        return LogTemplate.execute(() -> {

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(bodyStr, httpHeaders),
                    String.class
            );

            int statusCodeValue = response.getStatusCodeValue();

            if (statusCodeValue != 200) {
                digest.append("闸机平台调用失败, statusCodeValue: " + statusCodeValue);
                throw new BizException("闸机平台调用失败, statusCodeValue: " + statusCodeValue);
            }

            String str = response.getBody();
            realResponse.append(str);

            if (str == null) {
                digest.append("闸机平台调用失败, response is null");
                throw new BizException("闸机平台调用失败, response is null");
            }

            JSONObject jsonObject = JSONObject.parseObject(str);
            Integer code = jsonObject.getInteger("code");

            if (code == null || code != 0) {
                digest.append(jsonObject.getString("msg"));
                throw new BizException("业务失败。" + jsonObject.getString("msg"));
            }

            return str;

        }, bodyStr, realResponse, "GATE", type, digest);

    }

//    public static void main(String[] args) {
//        GateClient client = new GateClient();
//
//        client.nameVerifyUrl = "http://smrz.zszlchina.com/enroll-server/api/realName/verify";
//        client.nameVerifyToken = "9EC6A1B7C07A4576B48DFE62A0E69DF0";
//
////        client.nameVerify(new GateNameVerifyRequest("张三", "510824199206073615"));
////        client.nameVerify(new GateNameVerifyRequest("何治明", "510824199206073615"));
//
//
//        String ticketNo = String.format("1%03d%07d", 1, 244);
//
//        GatePushPersonRequest request = new GatePushPersonRequest();
//        request.setThirdId("dfdafdafdakfhjdkahfjkdafd");
//        request.setOrderNo(ticketNo);
//        request.setPrice("0");
//        request.setBigArea("A区");
//        request.setSmallArea("A1区");
//        request.setSeatNo("3排5号");
//        request.setName("何治明");
//        request.setMobile("18112345678");
//        request.setCertificateType("1");
//        request.setCertificateNo("510824199206073615");
//
//        client.pushPerson("http://yc1.zszlchina.com/es-server/api/push/person", "b095bb12d6b844c995be85473cd45cc5", request);
//    }
}
