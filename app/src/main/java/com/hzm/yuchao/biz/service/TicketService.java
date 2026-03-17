package com.hzm.yuchao.biz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzm.yuchao.biz.controller.app.req.GiftTicketRequest;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.TicketDO;

import javax.validation.Valid;

public interface TicketService extends IService<TicketDO> {

    /**
     * 同步购票信息
     * @param ticketId
     */
    void pushPerson(MatchDO matchDO, Long ticketId);

    /**
     * 同步退票信息
     */
    void refundPush(MatchDO matchDO, Long ticketId);

    /**
     * 赠票激活
     * @param giftTicketRequest
     * @return
     */
    TicketDO giftTicket(@Valid GiftTicketRequest giftTicketRequest);
}
