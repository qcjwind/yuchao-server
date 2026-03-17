package com.hzm.yuchao.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.biz.enums.UserStatusEnum;
import com.hzm.yuchao.biz.mapper.UserMapper;
import com.hzm.yuchao.biz.model.UserDO;
import com.hzm.yuchao.biz.service.UserService;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.utils.JsonUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

}
