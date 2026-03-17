package com.hzm.yuchao.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.biz.mapper.AccountMapper;
import com.hzm.yuchao.biz.model.AccountDO;
import com.hzm.yuchao.biz.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDO> implements AccountService {
}
