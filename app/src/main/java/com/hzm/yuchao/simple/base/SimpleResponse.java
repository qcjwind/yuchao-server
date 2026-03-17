package com.hzm.yuchao.simple.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleResponse<T> extends BaseResponse {

    private T data;

    public SimpleResponse(int code, String msg, T data) {
        super(code, msg);
        this.data = data;
    }

    public static <T> SimpleResponse<T> ok() {
        return new SimpleResponse<T>(200, "success", null);
    }

    public static <T> SimpleResponse<T> ok(T data) {
        SimpleResponse<T> baseResponse = SimpleResponse.ok();
        baseResponse.setData(data);
        return baseResponse;
    }

    public static <T> SimpleResponse<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> SimpleResponse<T> fail(int code, String msg) {
        return new SimpleResponse<T>(code, msg, null);
    }


}
