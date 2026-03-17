package com.hzm.yuchao.biz.enums;

public enum MatchTagEnum {

    DISABLE_SEAT("不展示座位号"),


    ;

    private final String desc; // 中文描述

    MatchTagEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }


}
