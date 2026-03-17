package com.hzm.yuchao.simple.utils;

import java.util.UUID;

public class UuidUtils {

    public static String uuid32() {
        UUID uuid = UUID.randomUUID();
        // 将UUID转换为字符串
        String str = uuid.toString();
        // 移除连字符
        return str.replace("-", "");
    }

    public static String uuid16() {
        // 取前16个字符
        return uuid32().substring(0, 16);
    }

}
