package com.hzm.yuchao.biz.outter.wechat.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaKefuMessage;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.binarywang.wx.miniapp.message.WxMaMessageHandler;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WxMaProperties.class)
public class WxMaConfiguration {

    private final WxMaProperties properties;

    @Autowired
    public WxMaConfiguration(WxMaProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WxMaService wxMaService() {

        WxMaService maService = new WxMaServiceImpl();

        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(this.properties.getAppid());
        config.setSecret(this.properties.getAppsecret());
        config.setToken(this.properties.getToken());
        config.setAesKey(this.properties.getAesKey());
        config.setMsgDataFormat(this.properties.getMsgDataFormat());
        maService.setWxMaConfig(config);

        return maService;
    }

    @Bean
    public WxMaMessageRouter wxMaMessageRouter(WxMaService wxMaService) {
        final WxMaMessageRouter router = new WxMaMessageRouter(wxMaService);
        router
            .rule().handler(logHandler).next()
            // user_info_modified：用户资料变更，user_authorization_revoke：用户撤回，user_authorization_cancellation：用户完成注销；
            .rule().async(false).event("user_info_modified").handler(authRevokeMsgHandler).end()
            .rule().async(false).event("user_authorization_revoke").handler(authRevokeMsgHandler).end()
            .rule().async(false).event("user_authorization_cancellation").handler(authRevokeMsgHandler).end()
//            .rule().async(false).content("订阅消息").handler(subscribeMsgHandler).end()
//            .rule().async(false).content("文本").handler(textHandler).end()
//            .rule().async(false).content("图片").handler(picHandler).end()
//            .rule().async(false).content("二维码").handler(qrcodeHandler).end()
            ;
        return router;
    }

    // https://developers.weixin.qq.com/miniprogram/dev/framework/security.html#%E6%8E%88%E6%9D%83%E7%94%A8%E6%88%B7%E4%BF%A1%E6%81%AF%E5%8F%98%E6%9B%B4
    private final WxMaMessageHandler authRevokeMsgHandler = (wxMessage, context, service, sessionManager) -> {

        // 用户撤回的授权信息，1:车牌号,2:地址,3:发票信息,4:蓝牙,5:麦克风,6:昵称和头像,7:摄像头,8:手机号,12:微信运动步数,13:位置信息,14:选中的图片或视频,15:选中的文件,16:邮箱地址,18:选择的位置信息,19:昵称输入键盘中选择的微信昵称,20:获取用户头像组件中选择的微信头像
        String revokeInfo = wxMessage.getRevokeInfo();

        log.info("用户撤回授权 revokeInfo: {}", revokeInfo);

        // todo 删除用户信息

        return null;
    };

    private final WxMaMessageHandler logHandler = (wxMessage, context, service, sessionManager) -> {
        log.info("收到消息：{}", wxMessage.toString());
        return null;
    };

    private final WxMaMessageHandler subscribeMsgHandler = (wxMessage, context, service, sessionManager) -> {
        service.getMsgService().sendSubscribeMsg(WxMaSubscribeMessage.builder()
            .templateId("此处更换为自己的模板id")
            .data(Lists.newArrayList(
                new WxMaSubscribeMessage.MsgData("keyword1", "339208499")))
            .toUser(wxMessage.getFromUser())
            .build());
        return null;
    };


    private final WxMaMessageHandler textHandler = (wxMessage, context, service, sessionManager) -> {
        service.getMsgService().sendKefuMsg(WxMaKefuMessage.newTextBuilder().content("回复文本消息")
            .toUser(wxMessage.getFromUser()).build());
        return null;
    };

    private final WxMaMessageHandler picHandler = (wxMessage, context, service, sessionManager) -> {
        try {
            WxMediaUploadResult uploadResult = service.getMediaService()
                .uploadMedia("image", "png",
                    ClassLoader.getSystemResourceAsStream("tmp.png"));
            service.getMsgService().sendKefuMsg(
                WxMaKefuMessage
                    .newImageBuilder()
                    .mediaId(uploadResult.getMediaId())
                    .toUser(wxMessage.getFromUser())
                    .build());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        return null;
    };

    private final WxMaMessageHandler qrcodeHandler = (wxMessage, context, service, sessionManager) -> {
        try {
            final File file = service.getQrcodeService().createQrcode("123", 430);
            WxMediaUploadResult uploadResult = service.getMediaService().uploadMedia("image", file);
            service.getMsgService().sendKefuMsg(
                WxMaKefuMessage
                    .newImageBuilder()
                    .mediaId(uploadResult.getMediaId())
                    .toUser(wxMessage.getFromUser())
                    .build());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        return null;
    };

}
