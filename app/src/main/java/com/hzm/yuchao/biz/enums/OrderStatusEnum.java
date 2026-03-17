package com.hzm.yuchao.biz.enums;

public enum OrderStatusEnum {

    WAIT_PAY("待支付"),

    CANCEL("已取消"),

    PAY_SUCCESS("支付成功"),

//    REFUND_APPLY("退款申请中"),

    REFUND_ING("微信退款中"),

    REFUND_SUCCESS("已退款"),


    ;

    private final String desc; // 中文描述

    OrderStatusEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }


}
