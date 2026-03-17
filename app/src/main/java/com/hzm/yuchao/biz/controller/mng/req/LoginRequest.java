package com.hzm.yuchao.biz.controller.mng.req;

import com.hzm.yuchao.simple.base.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class LoginRequest extends BaseRequest {

    @NotEmpty
    @Size(max = 20)
    private String username;

    @NotEmpty
    @Size(max = 20)
    private String password;

}
