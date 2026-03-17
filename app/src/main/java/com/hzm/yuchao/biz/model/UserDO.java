package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hzm.yuchao.biz.enums.IdTypeEnum;
import com.hzm.yuchao.biz.enums.UserStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("t_user")
public class UserDO extends BaseDO {

    @ApiModelProperty("微信用户唯一标识")
    private String openid;

    @ApiModelProperty("用户昵称")
    private String nickname;

    @ApiModelProperty("用户头像URL")
    private String avatarUrl;

    @ApiModelProperty("用户真实姓名")
    private String name;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("身份类型")
    private IdTypeEnum idType;

    @ApiModelProperty("证件号")
    private String idNo;


    /**
     * ------------不持久化的字段--------------
     */
    @TableField(exist = false)
    @ApiModelProperty("用户真实姓名-脱敏")
    private String maskName;


}
