package com.hzm.yuchao.simple.constant;

import org.slf4j.MDC;

public class SessionUtils {

    public static String getUser() {
        return MDC.get("userId");
    }

    public static long getUserId() {
        return Long.parseLong(MDC.get("userId"));
    }

    public static void putUser(String user) {
        MDC.put("userId", user);
    }

    public static void removeUser() {
        MDC.remove("userId");
    }

}
