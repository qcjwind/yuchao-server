package com.hzm.yuchao.simple.utils;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;

public class JsonUtil {

    private static final JSONConfig CONFIG = new JSONConfig();

    static {
        CONFIG.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static String toJsonStr(Object o) {
        return JSONUtil.toJsonStr(o, CONFIG);
    }
}
