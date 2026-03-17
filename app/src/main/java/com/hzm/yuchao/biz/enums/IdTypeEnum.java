package com.hzm.yuchao.biz.enums;

public enum IdTypeEnum {

    // 身份证
    ID_CARD("身份证"),

    // 护照
    PASSPORT("护照"),

    // 港澳台通行证
    GAT_TXZ("港澳台通行证"),

    // 港澳台居民居住证
//    GAT_JM_JZZ,

    // 港澳居民来往内地通行证
//    GA_JM_LWND_TXZ,

    // 台湾居民来往大陆通行证
//    TW_JM_LWDL_TXZ,

    // 外国人永久居留身份证
//    WGR_YJJL_SFZ,

    // 往来港澳通行证
//    WL_GG_TXZ,

    ;

    private final String desc; // 中文描述

    IdTypeEnum(String desc) {
        this.desc = desc;
    }

    // 获取中文描述的方法
    public String getDesc() {
        return desc;
    }


}
