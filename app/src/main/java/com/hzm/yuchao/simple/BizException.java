package com.hzm.yuchao.simple;

import lombok.Data;

@Data
public class BizException extends RuntimeException {

    public int code;

    public String msg;

    public BizException(String msg) {
        super(msg);
        this.code = 500;
        this.msg = msg;
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
