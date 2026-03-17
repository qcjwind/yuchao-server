package com.hzm.yuchao.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.biz.mapper.BannerMapper;
import com.hzm.yuchao.biz.model.BannerDO;
import com.hzm.yuchao.biz.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, BannerDO> implements BannerService {
}

