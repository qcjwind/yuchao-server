package com.hzm.yuchao.biz.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.model.BannerDO;
import com.hzm.yuchao.biz.service.BannerService;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Api(tags = "APP-Banner")
@CrossOrigin
@RestController
@RequestMapping("/app/banner/")
public class BannerAppController {

    @Resource
    private BannerService bannerService;

    @ApiOperation("首页 Banner 列表")
    @PostMapping("list")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<List<BannerDO>> list() {
        Date now = new Date();
        LambdaQueryWrapper<BannerDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BannerDO::getStatus, BooleanEnum.Y)
                .ne(BannerDO::getDeleted, BooleanEnum.Y)
                // (start_time IS NULL OR start_time <= now)
                .and(w -> w.isNull(BannerDO::getStartTime)
                        .or()
                        .le(BannerDO::getStartTime, now))
                // (end_time IS NULL OR end_time >= now)
                .and(w -> w.isNull(BannerDO::getEndTime)
                        .or()
                        .ge(BannerDO::getEndTime, now))
                .orderByAsc(BannerDO::getSortNumber)
                .orderByDesc(BannerDO::getGmtCreate);
        List<BannerDO> list = bannerService.list(queryWrapper);
        return SimpleResponse.ok(list);
    }
}

