package com.hzm.yuchao.biz.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_account")
public class AccountDO extends BaseDO {

    private String name;

    private String username;

    private String password;

    private String role;

    private int tenantId;

    private String remark;

}
