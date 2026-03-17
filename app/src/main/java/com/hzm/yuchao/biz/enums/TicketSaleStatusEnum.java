package com.hzm.yuchao.biz.enums;

public enum TicketSaleStatusEnum {

    // 未售
    UNSOLD("未售"),

    // 待支付
    WAIT_PAY("待支付"),

    // 已售
    SOLD("已售"),

    ;

    private final String desc; // 中文描述

    TicketSaleStatusEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }

}
