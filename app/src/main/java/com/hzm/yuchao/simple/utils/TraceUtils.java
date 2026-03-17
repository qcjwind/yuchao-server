package com.hzm.yuchao.simple.utils;

import org.slf4j.MDC;

public class TraceUtils {

    public static void initTrace() {
        MDC.put("traceId", UuidUtils.uuid16());
    }

    public static void initTrace(String traceId) {
        MDC.put("traceId", traceId);
    }

    public static String getTraceId() {
        return MDC.get("traceId");
    }

    public static void removeTraceId() {
        MDC.remove("traceId");
    }
}
