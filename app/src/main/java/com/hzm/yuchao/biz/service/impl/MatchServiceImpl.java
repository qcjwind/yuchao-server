package com.hzm.yuchao.biz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.biz.mapper.MatchMapper;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.service.MatchService;
import org.springframework.stereotype.Service;

@Service
public class MatchServiceImpl extends ServiceImpl<MatchMapper, MatchDO> implements MatchService {
}
