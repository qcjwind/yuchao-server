package com.hzm.yuchao.biz.utils;

import com.hzm.yuchao.biz.controller.app.resp.TicketListVO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.simple.utils.DataMaskingUtil;

import java.util.List;

/**
 * 脱敏工具雷
 */
public class TicketDataMaskingUtil {

    public static void ticketDataMasking(TicketDO ticketDO) {
        ticketDO.setName(DataMaskingUtil.maskName(ticketDO.getName()));
        ticketDO.setIdNo(DataMaskingUtil.maskIdCard(ticketDO.getIdNo()));
        ticketDO.setMobile(DataMaskingUtil.maskPhone(ticketDO.getMobile()));
    }

    public static void ticketDataMasking(List<TicketDO> list) {
        list.forEach(TicketDataMaskingUtil::ticketDataMasking);
    }

    public static void ticketVOMasking(List<TicketListVO> list) {
        list.forEach(t -> {
            t.setName(DataMaskingUtil.maskName(t.getName()));
            t.setIdNo(DataMaskingUtil.maskIdCard(t.getIdNo()));
            t.setMobile(DataMaskingUtil.maskPhone(t.getMobile()));
        });
    }

}
