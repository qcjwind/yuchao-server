package com.hzm.yuchao.simple.constant;

import java.util.function.Supplier;

public class Constants {
    
    public static final int QPS_SIMPLE = 200;

    public static final int GLOBAL_QPS = 1000;

    public static final byte[] AES_PASSWORD = "hJ3zK3pX8wV1vM6y".getBytes();

    public static final String MOBILE_REGEX = "^1[3-9]\\d{9}$";

    public static final <T> T lockExecute(Object obj, Supplier<T> supplier) {
        synchronized (obj) {
            return supplier.get();
        }
    }

}
