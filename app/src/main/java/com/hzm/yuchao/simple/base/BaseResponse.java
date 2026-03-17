package com.hzm.yuchao.simple.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {

    private int code;

    private String msg;

    private String traceId;

    public BaseResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static BaseResponse success() {
        return new BaseResponse(200, "success");
    }

    public static BaseResponse failure(String msg) {
        return failure(500, msg);
    }

    public static BaseResponse failure(int code, String msg) {
        return new BaseResponse(code, msg);
    }
}
