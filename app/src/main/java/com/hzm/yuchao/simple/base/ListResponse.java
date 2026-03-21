package com.hzm.yuchao.simple.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListResponse<T> extends BaseResponse {

    private List<T> data;

    public ListResponse(int code, String msg, List<T> data) {
        super(code, msg);
        this.data = data;
    }

    public static <T> ListResponse<T> ok() {
        return new ListResponse<T>(200, "success", null);
    }

    public static <T> ListResponse<T> ok(List<T> data) {
        ListResponse<T> baseResponse = ListResponse.ok();
        baseResponse.setData(data);
        return baseResponse;
    }

    public static <T> ListResponse<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> ListResponse<T> fail(int code, String msg) {
        return new ListResponse<T>(code, msg, null);
    }


}
