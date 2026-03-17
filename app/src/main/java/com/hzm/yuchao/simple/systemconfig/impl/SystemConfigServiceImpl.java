package com.hzm.yuchao.simple.systemconfig.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.simple.systemconfig.SystemConfigDO;
import com.hzm.yuchao.simple.systemconfig.SystemConfigService;
import com.hzm.yuchao.simple.systemconfig.mapper.SystemConfigMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfigDO> implements SystemConfigService {

    private Map<String, SystemConfigDO> map = new HashMap<>();

    @Override
    public String getConfigValue(String key) {
        return getConfigValue(key, true);
    }

    @Override
    public String getConfigValue(String key, boolean useCache) {
        if (!useCache) {
            map = this.list().stream().collect(Collectors.toMap(SystemConfigDO::getConfigKey, v -> v));
        }
        if (map.containsKey(key)) {
            return map.get(key).getConfigValue();
        }
        return null;
    }
}

