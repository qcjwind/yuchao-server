package com.hzm.yuchao.biz.controller.app.resp;

import com.hzm.yuchao.biz.model.UserDO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    @ApiModelProperty("token 登录成功返回")
    private String token;

    @ApiModelProperty("静默授权返回，reg接口需要带回来")
    private String sessionKey;

    @ApiModelProperty("静默授权返回，reg接口需要带回来")
    private String openid;

    @ApiModelProperty("是否已经注册，如果未注册需要跳转让用户去录入姓名和身份证号")
    private boolean reg;

    @ApiModelProperty("用户信息、头像昵称等")
    private UserDO userDO;

}
