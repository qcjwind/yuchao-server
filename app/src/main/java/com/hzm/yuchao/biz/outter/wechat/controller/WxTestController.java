package com.hzm.yuchao.biz.outter.wechat.controller;

import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.hzm.yuchao.biz.model.OrderDO;
import com.hzm.yuchao.biz.outter.wechat.config.WxPayProperties;
import com.hzm.yuchao.biz.service.OrderService;
import com.hzm.yuchao.biz.service.PayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Binary Wang
 * https://pay.weixin.qq.com/doc/v3/merchant/4012791911
 */
@Slf4j
@Api("微信支付")
@RestController
//@RequestMapping("/wx/test")
public class WxTestController {

    @Resource
    private WxPayService wxPayService;

    @Resource
    private PayService payService;

    @Resource
    private WxPayProperties wxPayProperties;

    @Resource
    private OrderService orderService;

    // http://127.0.0.1/wx/test/refund?orderNo=dbe3e420a1054df0a67c4bcd10c901a1
    @ApiOperation(value = "支付回调通知处理")
    @GetMapping("/refund")
    public String refund(Long orderId, String orderNo) throws WxPayException {

        OrderDO lockDO = orderService.lockOrder(orderId, orderNo);

        WxPayRefundV3Request request = new WxPayRefundV3Request();
        request.setOutTradeNo(lockDO.getOrderNo());
        request.setOutRefundNo(lockDO.getOrderNo());
        WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
        amount.setTotal(lockDO.getTotalPrice());
        amount.setRefund(lockDO.getTotalPrice());
        amount.setCurrency(WxPayConstants.CurrencyType.CNY);
        request.setAmount(amount);
        request.setNotifyUrl(wxPayProperties.getRefundNotifyUrl());
        // 查询微信
        WxPayRefundV3Result result = wxPayService.refundV3(request);

        return result.getRefundId();
    }
}

