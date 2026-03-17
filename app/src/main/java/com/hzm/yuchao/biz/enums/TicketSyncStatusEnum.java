package com.hzm.yuchao.biz.enums;

public enum TicketSyncStatusEnum {

    // 未同步闸机
    NOT_SYNC("未同步闸机"),

    // 已同步
    SYNCED("已同步闸机"),

    ;

    private final String desc; // 中文描述

    TicketSyncStatusEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }


}
