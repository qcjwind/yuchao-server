package com.hzm.yuchao.simple.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseAppRequest extends BaseRequest {

//    private String token;

    @ApiModelProperty("客户端id, 前端生成，缓存localstorage, 追溯端侧用户行为")
    private String clientId;

}
