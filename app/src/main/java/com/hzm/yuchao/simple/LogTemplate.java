package com.hzm.yuchao.simple;

import com.hzm.yuchao.simple.utils.JsonUtil;
import com.hzm.yuchao.simple.utils.MonitorUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class LogTemplate {

    public static <T> T execute(Supplier<T> supplier, Object request, StringBuilder realResponse,
                                String type, String subType, StringBuilder digest, String ...args) {

        boolean success = true;
        long startTime = System.currentTimeMillis();

        T response = null;
        String responseStr = "";

        try {
            response = supplier.get();

            return response;

        } catch (Exception e) {
            success = false;
            responseStr = e.getMessage();
            throw e;
        } finally {

            long timeConsuming = System.currentTimeMillis() - startTime;

            if (response != null) {
                responseStr = (response instanceof String) ? response.toString() : JsonUtil.toJsonStr(response);
            } else if (realResponse != null) {
                responseStr = realResponse.toString();
            }

            log.info("{}-{}, success: {}, 耗时: {}, request: {}, response: {}", type, subType, success, timeConsuming,
                    (request instanceof String) ? request : JsonUtil.toJsonStr(request),
                    responseStr);

            MonitorUtils.log(type, subType, success, timeConsuming, digest.toString(), args);
        }
    }

}
