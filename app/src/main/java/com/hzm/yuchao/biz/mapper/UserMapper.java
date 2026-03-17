package com.hzm.yuchao.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzm.yuchao.biz.model.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
