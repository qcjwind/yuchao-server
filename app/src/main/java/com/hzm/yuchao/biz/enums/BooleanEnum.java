package com.hzm.yuchao.biz.enums;

public enum BooleanEnum {

    Y("是"),

    N("否"),


    ;

    private final String desc; // 中文描述

    BooleanEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }


}
