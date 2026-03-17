package com.hzm.yuchao.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.biz.mapper.AccountMapper;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import com.hzm.yuchao.biz.model.AccountDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.service.AccountService;
import com.hzm.yuchao.biz.service.SkuService;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, SkuDO> implements SkuService {
}
