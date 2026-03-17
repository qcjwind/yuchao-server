package com.hzm.yuchao.biz.controller.app;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.controller.app.resp.TicketInfoVO;
import com.hzm.yuchao.biz.controller.app.resp.TicketListVO;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.mapper.TicketMapper;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.model.UserDO;
import com.hzm.yuchao.biz.model.VenueDO;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.biz.utils.TicketDataMaskingUtil;
import com.hzm.yuchao.simple.base.PageResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.constant.SessionUtils;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Api(tags = "APP-票据信息")
@CrossOrigin
@RestController
@RequestMapping("/app/ticket/")
public class TicketAppController {

    @Resource
    private MatchService matchService;

    @Resource
    private VenueService venueService;

    @Resource
    private TicketService ticketService;

    @Resource
    private SkuService skuService;

    @Resource
    private UserService userService;

    @Resource
    private TicketMapper ticketMapper;

    @ApiOperation("我的票据列表")
    @PostMapping("myList")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public PageResponse<TicketListVO> myList(Integer pageSize, Integer pageNumber) {

        UserDO userDO = userService.getById(SessionUtils.getUserId());

        if (userDO == null) {
            return PageResponse.fail("用户不存在");
        }

        Page<TicketListVO> page = new Page<>(pageNumber == null ? 1 : pageNumber, pageSize == null ? 10 : pageSize);

        IPage<TicketListVO> pageData = ticketMapper.selectMyTicketByPage(page, userDO.getIdNo(), userDO.getIdType().name());

        // 脱敏
        TicketDataMaskingUtil.ticketVOMasking(pageData.getRecords());

        return PageResponse.ok(pageData);

    }

    @ApiOperation("我的票据详情")
    @PostMapping("myInfo")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<TicketInfoVO> myInfo(long ticketId) {

        UserDO userDO = userService.getById(SessionUtils.getUserId());
        if (userDO == null) {
            return SimpleResponse.fail("用户不存在");
        }

        TicketDO ticketDO = ticketService.getById(ticketId);
        if (ticketDO == null) {
            return SimpleResponse.fail("票据不存在");
        }
        // 因为可以代买 因此userId 和 身份证，有一个匹配就行。
        if (ticketDO.getBuyerId() != SessionUtils.getUserId() && !Objects.equals(ticketDO.getIdNo(), userDO.getIdNo())) {
            return SimpleResponse.fail("非法访问");
        }
        if (ticketDO.getSaleStatus() != TicketSaleStatusEnum.SOLD) {
            return SimpleResponse.fail("票据还未支付");
        }

        MatchDO matchDO = matchService.getById(ticketDO.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛程不存在");
        }

        VenueDO venueDO = venueService.getById(matchDO.getVenueId());
        if (venueDO == null) {
            return SimpleResponse.fail("场馆不存在");
        }

        // 脱敏
        TicketDataMaskingUtil.ticketDataMasking(ticketDO);

        return SimpleResponse.ok(new TicketInfoVO(ticketDO, matchDO, venueDO));
    }

}
