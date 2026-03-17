package com.hzm.yuchao.simple.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j(topic = "monitor")
public class MonitorUtils {

    public static void log(String type, String subType, boolean success, long timeConsuming, String digest, String ...args) {
        log.info("{}|{}|{}|{}|{}|{}", type, subType,
                success ? "T" : "F", timeConsuming, digest, Arrays.toString(args));
    }

    public static void log(String type, String subType, boolean reqSuccess, boolean bizSuccess, long timeConsuming, String digest, String ...args) {
        log.info("{}|{}|{}|{}|{}|{}|{}", type, subType,
                reqSuccess ? "T" : "F",
                bizSuccess ? "T" : "F",
                timeConsuming, digest, Arrays.toString(args));
    }
}
