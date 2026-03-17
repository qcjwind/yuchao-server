package com.hzm.yuchao.biz.controller.app.req;

import com.hzm.yuchao.biz.enums.IdTypeEnum;
import com.hzm.yuchao.simple.base.BaseAppRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class RegisterRequest extends BaseAppRequest {

    @Size(max = 16)
    @ApiModelProperty("真实姓名，选填")
    private String name;

    @ApiModelProperty("证件类型，选填")
    private IdTypeEnum idType;

    @Size(max = 32)
    @ApiModelProperty("证件号，选填")
    private String idNo;

    @NotNull
    @ApiModelProperty("手机号加密数据(微信返回的)")
    private String encryptedData;

    @NotNull
    @ApiModelProperty("手机号iv(微信返回的)")
    private String iv;

    @NotNull
    @ApiModelProperty("上一个接口返回的")
    private String sessionKey;

    @NotNull
    @ApiModelProperty("上一个接口返回的")
    private String openid;
}
