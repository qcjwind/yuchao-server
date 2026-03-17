package com.hzm.yuchao.biz.enums;

public enum TicketTypeEnum {

    // 购票
    SALE_TICKET("购票"),

    // 赠票
    GIFT_TICKET("赠票"),

    ;

    private final String desc; // 中文描述

    TicketTypeEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }

}
