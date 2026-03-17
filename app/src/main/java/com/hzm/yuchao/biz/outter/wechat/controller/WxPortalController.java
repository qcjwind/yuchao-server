package com.hzm.yuchao.biz.outter.wechat.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import cn.binarywang.wx.miniapp.message.WxMaMessageRouter;
import cn.binarywang.wx.miniapp.message.WxMaXmlOutMessage;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.biz.outter.wechat.controller.bak.WXBizMsgCrypt;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.util.crypto.SHA1;
import me.chanjar.weixin.common.util.crypto.WxCryptUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 * https://developers.weixin.qq.com/apiExplorer?type=messagePush
 */
@RestController
@RequestMapping("/wx/portal")
@Slf4j
public class WxPortalController {

    @Resource
    private WxMaService wxMaService;

    @Resource
    private WxMaMessageRouter wxMaMessageRouter;

    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("接收到来自微信服务器的认证消息：signature = [{}], timestamp = [{}], nonce = [{}], echostr = [{}]",
            signature, timestamp, nonce, echostr);

        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            return "请求参数非法，请核实!";
        }

        if (wxMaService.checkSignature(timestamp, nonce, signature)) {
            WxMaConfigHolder.remove();//清理ThreadLocal
            return echostr;
        }
        WxMaConfigHolder.remove();//清理ThreadLocal
        return "非法请求";
    }

    public static void main(String[] args) {

        String replyMsg = "success";
        String nonce = "2013274345";
        String token = "e4ec15d2";

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

        WXBizMsgCrypt pc = new WXBizMsgCrypt(
                "e4ec15d2",
                "s9gyqtNNhWz00iBCdI14BXamgIOzRVhF5LJHF4sk0xZ",
                "wx5c89ad708794b0bc"
        );

        WxCryptUtil cryptUtil = new WxCryptUtil(
                "e4ec15d2",
                "s9gyqtNNhWz00iBCdI14BXamgIOzRVhF5LJHF4sk0xZ",
                "wx5c89ad708794b0bc"
        );

        String randomStr = pc.getRandomStr();

        String encrypt = cryptUtil.encrypt(randomStr, replyMsg);
        log.info(encrypt);

        String encrypt2 = pc.encrypt(randomStr, replyMsg);
        log.info(encrypt2);

        WxCryptUtil.EncryptContext encryptContent = pc.encryptMsg2(randomStr, replyMsg, timestamp, nonce);
        log.info(encryptContent.getEncrypt());

        log.info("=========");

        String signature = SHA1.gen(token, timestamp, nonce, encrypt);
        log.info(signature);
        log.info(encryptContent.getSignature());


    }

    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature,
                       @RequestParam(name = "encrypt_type", required = false) String encryptType,
                       @RequestParam(name = "signature", required = false) String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce) {

        log.info("接收微信请求：[msg_signature=[{}], encrypt_type=[{}], signature=[{}]," +
                " timestamp=[{}], nonce=[{}], requestBody=[{}] ",
            msgSignature, encryptType, signature, timestamp, nonce, requestBody);

        final boolean isJson = Objects.equals(wxMaService.getWxMaConfig().getMsgDataFormat(),
            WxMaConstants.MsgDataFormat.JSON);
        if (StringUtils.isBlank(encryptType)) {
            // 明文传输的消息
            WxMaMessage inMessage;
            if (isJson) {
                inMessage = WxMaMessage.fromJson(requestBody);
            } else {
                //xml
                inMessage = WxMaMessage.fromXml(requestBody);
            }

            this.route(inMessage);
            WxMaConfigHolder.remove();//清理ThreadLocal
            return "success";
        }

        if ("aes".equals(encryptType)) {
            // 是aes加密的消息
            WxMaMessage inMessage;
            if (isJson) {
                inMessage = WxMaMessage.fromEncryptedJson(requestBody, wxMaService.getWxMaConfig());
            } else {//xml
                inMessage = WxMaMessage.fromEncryptedXml(requestBody, wxMaService.getWxMaConfig(),
                    timestamp, nonce, msgSignature);
            }

            this.route(inMessage);
            WxMaConfigHolder.remove();//清理ThreadLocal

            return generateEncryptedResponse("success");
        }

        WxMaConfigHolder.remove();//清理ThreadLocal
        throw new RuntimeException("不可识别的加密类型：" + encryptType);
    }

    private WxMaXmlOutMessage route(WxMaMessage message) {
        try {
            return wxMaMessageRouter.route(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 生成加密回包（JSON格式）
     * @return 加密后的JSON回包
     */
    public String generateEncryptedResponse(String replyMsg) {
        try {

            WxCryptUtil cryptUtil = new WxCryptUtil(
                    wxMaService.getWxMaConfig().getToken(),
                    wxMaService.getWxMaConfig().getAesKey(),
                    wxMaService.getWxMaConfig().getAppid()
            );

            WxCryptUtil.EncryptContext encryptContent = cryptUtil.encryptContext(replyMsg);

            // 5. 组装JSON回包
            Map<String, Object> responseMap = new HashMap<>(4);
            responseMap.put("Encrypt", encryptContent.getEncrypt());
            responseMap.put("MsgSignature", encryptContent.getSignature());
            responseMap.put("TimeStamp", encryptContent.getTimeStamp());
            responseMap.put("Nonce", encryptContent.getNonce());

            return JSONObject.toJSONString(responseMap);
        } catch (Exception e) {
            throw new RuntimeException("生成加密回包失败", e);
        }
    }

    /**
     * 生成加密回包（JSON格式）
     * @param nonce URL参数中的nonce
     * @return 加密后的JSON回包
     */
    public String generateEncryptedResponse2(String replyMsg, String nonce) {
        try {

            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            WXBizMsgCrypt pc = new WXBizMsgCrypt(
                    wxMaService.getWxMaConfig().getToken(),
                    wxMaService.getWxMaConfig().getAesKey(),
                    wxMaService.getWxMaConfig().getAppid()
            );

            WxCryptUtil cryptUtil = new WxCryptUtil(
                    wxMaService.getWxMaConfig().getToken(),
                    wxMaService.getWxMaConfig().getAesKey(),
                    wxMaService.getWxMaConfig().getAppid()
            );

            String randomStr = pc.getRandomStr();

            String encrypt = cryptUtil.encrypt(randomStr, replyMsg);
            log.info(encrypt);
            String signature = SHA1.gen(wxMaService.getWxMaConfig().getToken(), timestamp, nonce, encrypt);

            WxCryptUtil.EncryptContext encryptContent = pc.encryptMsg2(randomStr, replyMsg, timestamp, nonce);
            log.info(JSONObject.toJSONString(encryptContent));

            encryptContent = new WxCryptUtil.EncryptContext(encrypt, signature, timestamp, nonce);

            // 5. 组装JSON回包
            Map<String, Object> responseMap = new HashMap<>(4);
            responseMap.put("Encrypt", encryptContent.getEncrypt());
            responseMap.put("MsgSignature", encryptContent.getSignature());
            responseMap.put("TimeStamp", encryptContent.getTimeStamp());
            responseMap.put("Nonce", encryptContent.getNonce());

            return JSONObject.toJSONString(responseMap);
        } catch (Exception e) {
            throw new RuntimeException("生成加密回包失败", e);
        }
    }
}
