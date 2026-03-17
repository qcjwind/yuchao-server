package com.hzm.yuchao.biz.clients;

import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.biz.dto.WechatSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WechatClient {

    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.appsecret}")
    private String appsecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 微信code2Session接口地址
     */
    private static final String CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    public WechatSession getSessionInfo(String code) {
        // 构建请求URL
        String url = String.format(CODE2SESSION_URL, appid, appsecret, code);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );

        // 解析响应
        JSONObject jsonObject = JSONObject.parseObject(response.getBody());
        WechatSession session = new WechatSession();
        session.setOpenid(jsonObject.getString("openid"));
        session.setSession_key(jsonObject.getString("session_key"));
        session.setUnionid(jsonObject.getString("unionid"));
        session.setErrcode(jsonObject.getInteger("errcode"));
        session.setErrmsg(jsonObject.getString("errmsg"));

        return session;
    }
}
