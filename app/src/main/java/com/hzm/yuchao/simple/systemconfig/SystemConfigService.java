package com.hzm.yuchao.simple.systemconfig;

import com.baomidou.mybatisplus.extension.service.IService;

public interface SystemConfigService extends IService<SystemConfigDO> {

    String getConfigValue(String key);

    String getConfigValue(String key, boolean useCache);
}
