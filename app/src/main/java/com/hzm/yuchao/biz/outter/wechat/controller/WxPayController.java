package com.hzm.yuchao.biz.outter.wechat.controller;

import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Response;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.hzm.yuchao.biz.service.PayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Binary Wang
 * https://pay.weixin.qq.com/doc/v3/merchant/4012791911
 */
@Slf4j
@Api("微信支付")
@RestController
@RequestMapping("/wx/pay")
public class WxPayController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private PayService payService;

    @ApiOperation(value = "支付回调通知处理")
    @PostMapping("/notify/order")
    public String parseOrderNotifyResult(@RequestBody String notifyData, HttpServletRequest request) throws WxPayException {

        // 1. 校验请求体非空
        if (notifyData == null || notifyData.trim().isEmpty()) {
            return "回调数据为空";
        }

        // 2. 提取并校验头部参数
        String signature = request.getHeader("Wechatpay-Signature");
        String timeStamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String serial = request.getHeader("Wechatpay-Serial");

        // 2.1 非空校验
        if (StringUtils.isEmpty(signature)) {
            return "缺少Wechatpay-Signature头部";
        }
        if (StringUtils.isEmpty(timeStamp)) {
            return "缺少Wechatpay-Timestamp头部";
        }
        if (StringUtils.isEmpty(nonce)) {
            return "缺少Wechatpay-Nonce头部";
        }
        if (StringUtils.isEmpty(serial)) {
            return "缺少Wechatpay-Serial头部";
        }

        // 3. 构造SignatureHeader
        SignatureHeader header = new SignatureHeader();
        header.setSignature(signature);
        header.setTimeStamp(timeStamp);
        header.setNonce(nonce);
        header.setSerial(serial);

        try {

            WxPayNotifyV3Result result = this.wxPayService.parseOrderNotifyV3Result(notifyData, header);

            log.info("付款通知-交易号: {}", result.getResult().getOutTradeNo());

            payService.paySuccess(null, result.getResult().getOutTradeNo());

            return WxPayNotifyV3Response.success("成功");
        } catch (Exception e) {

            log.error("支付回调处理失败", e);

            // 返回失败没用，需要返回 400 或者 500状态码
            throw e;
        }
    }

    @ApiOperation(value = "退款回调通知处理")
    @PostMapping("/notify/refund")
    public String parseRefundNotifyResult(@RequestBody String notifyData, HttpServletRequest request) throws WxPayException {

        // 1. 校验请求体非空
        if (notifyData == null || notifyData.trim().isEmpty()) {
            return "回调数据为空";
        }

        // 2. 提取并校验头部参数
        String signature = request.getHeader("Wechatpay-Signature");
        String timeStamp = request.getHeader("Wechatpay-Timestamp");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String serial = request.getHeader("Wechatpay-Serial");

        // 2.1 非空校验
        if (StringUtils.isEmpty(signature)) {
            return "缺少Wechatpay-Signature头部";
        }
        if (StringUtils.isEmpty(timeStamp)) {
            return "缺少Wechatpay-Timestamp头部";
        }
        if (StringUtils.isEmpty(nonce)) {
            return "缺少Wechatpay-Nonce头部";
        }
        if (StringUtils.isEmpty(serial)) {
            return "缺少Wechatpay-Serial头部";
        }

        // 3. 构造SignatureHeader
        SignatureHeader header = new SignatureHeader();
        header.setSignature(signature);
        header.setTimeStamp(timeStamp);
        header.setNonce(nonce);
        header.setSerial(serial);

        try {

            WxPayRefundNotifyV3Result result = this.wxPayService.parseRefundNotifyV3Result(notifyData, header);

            log.info("退款通知-交易号: {}", result.getResult().getOutTradeNo());

            payService.refundSuccess(null, result.getResult().getOutTradeNo());

            return WxPayNotifyV3Response.success("成功");
        } catch (Exception e) {

            log.error("支付回调处理失败", e);

            // 返回失败没用，需要返回 400 或者 500状态码
            throw e;
        }
    }
}

