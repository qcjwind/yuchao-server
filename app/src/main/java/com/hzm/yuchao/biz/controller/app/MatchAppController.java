package com.hzm.yuchao.biz.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzm.yuchao.biz.controller.app.req.MatchListRequest;
import com.hzm.yuchao.biz.controller.app.resp.MatchInfoVO;
import com.hzm.yuchao.biz.controller.app.resp.MatchListVO;
import com.hzm.yuchao.biz.controller.app.resp.TicketSeatListVO;
import com.hzm.yuchao.biz.enums.SkuStatusEnum;
import com.hzm.yuchao.biz.enums.TicketTypeEnum;
import com.hzm.yuchao.biz.mapper.MatchMapper;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.model.VenueDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.SkuService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.biz.service.VenueService;
import com.hzm.yuchao.simple.base.ListResponse;
import com.hzm.yuchao.simple.base.PageResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.constant.SessionUtils;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import com.hzm.yuchao.simple.systemconfig.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Api(tags = "APP-赛事信息")
@CrossOrigin
@RestController
@RequestMapping("/app/match/")
public class MatchAppController {

    @Resource
    private MatchService matchService;

    @Resource
    private MatchMapper matchMapper;

    @Resource
    private VenueService venueService;

    @Resource
    private SkuService skuService;

    @Resource
    private TicketService ticketService;

    @Resource
    private SystemConfigService systemConfigService;

    @ApiOperation("获取赛事列表")
    @PostMapping("list")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public PageResponse<MatchListVO> list(MatchListRequest request) {

        Page<MatchDO> page = new Page<>(request.getPageNumber(), request.getPageSize());

        if (SessionUtils.getUser() != null &&
                systemConfigService.getConfigValue("gray_user_ids", false).contains("," + SessionUtils.getUser() + ",")) {
            request.setGrayUser(true);
        } else {
            request.setGrayUser(false);
        }

        IPage<MatchListVO> pageData = matchMapper.selectByPage(page, request);

        //        如果模型转换，可以用这个转。
        //        IPage<MatchInfoVO> resultPage = pageData.convert(t -> {
        //            MatchInfoVO matchInfoVO = new MatchInfoVO();
        //            matchInfoVO.setMatch(t);
        //
        //            return matchInfoVO;
        //        });

        return PageResponse.ok(pageData);
    }

    @ApiOperation("赛事详情, 两个查询ID 二选一，分别是从列表进入查询 和 赠票扫码查询")
    @PostMapping("info")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<MatchInfoVO> info(Long matchId, String ticketBid) {

        TicketDO ticketDO = null;
        if (ticketBid != null) {
            LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketDO::getBid, ticketBid);
            ticketDO = ticketService.getOne(queryWrapper);
            if (ticketDO == null) {
                return SimpleResponse.fail("票据不存在");
            }
            matchId = ticketDO.getMatchId();
        }

        if (matchId == null) {
            return SimpleResponse.fail("查询条件有误");
        }

        MatchDO matchDO = matchService.getById(matchId);
        if (matchDO == null) {
            return SimpleResponse.fail("赛程不存在");
        }

        VenueDO venueDO = venueService.getById(matchDO.getVenueId());
        if (venueDO == null) {
            return SimpleResponse.fail("场馆不存在");
        }

        List<SkuDO> list = null;
        if (ticketBid == null) {
            LambdaQueryWrapper<SkuDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SkuDO::getMatchId, matchId);
            queryWrapper.eq(SkuDO::getSkuStatus, SkuStatusEnum.ENABLE);
            queryWrapper.eq(SkuDO::getSkuType, TicketTypeEnum.SALE_TICKET);
            queryWrapper.orderByAsc(SkuDO::getSortNumber);
            list = skuService.list(queryWrapper);
        }

        return SimpleResponse.ok(new MatchInfoVO(matchDO, venueDO, list, ticketDO));
    }

    @ApiOperation("获取座位列表")
    @PostMapping("/tick/list")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public ListResponse<TicketSeatListVO> info(Long matchId) {
        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getMatchId, matchId);
        queryWrapper.isNull(TicketDO::getBuyerId);
        List<TicketDO> ticketList = ticketService.list(queryWrapper);
        return ListResponse.ok(TicketSeatListVO.build(ticketList));
    }

}
