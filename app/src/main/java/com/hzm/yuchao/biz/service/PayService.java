package com.hzm.yuchao.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzm.yuchao.biz.model.OrderDO;

public interface PayService extends IService<OrderDO> {

    /**
     * 支付成功
     */
    void paySuccess(Long orderId, String orderNo);

    /**
     * 退款成功
     * @param orderId
     * @param orderNo
     */
    void refundSuccess(Long orderId, String orderNo);
}
