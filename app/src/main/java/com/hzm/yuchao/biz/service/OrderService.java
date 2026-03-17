package com.hzm.yuchao.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzm.yuchao.biz.controller.app.req.BuyTicketRequest;
import com.hzm.yuchao.biz.model.OrderDO;

import javax.validation.Valid;

public interface OrderService extends IService<OrderDO> {

    /**
     * 下单
     * @param buyTicketRequest
     */
    OrderDO buy(@Valid BuyTicketRequest buyTicketRequest);

    OrderDO lockOrder(Long orderId, String orderNo);

    void cancel(long orderId, boolean isUserClose);
}
