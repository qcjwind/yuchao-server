package com.hzm.yuchao.simple.systemconfig;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hzm.yuchao.biz.model.BaseDO;
import lombok.Data;

@Data
@TableName("t_system_config")
public class SystemConfigDO extends BaseDO {

    private String name;

    private String configKey;

    private String configValue;

}
