package com.hzm.yuchao.biz.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzm.yuchao.biz.clients.GateClient;
import com.hzm.yuchao.biz.clients.GatePushPersonRequest;
import com.hzm.yuchao.biz.clients.GateRefundPushRequest;
import com.hzm.yuchao.biz.controller.app.req.GiftTicketRequest;
import com.hzm.yuchao.biz.dto.TicketShowInfoDTO;
import com.hzm.yuchao.biz.enums.*;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import com.hzm.yuchao.biz.mapper.TicketMapper;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.SkuDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.model.UserDO;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.biz.utils.AmountConverter;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.alarm.AlarmService;
import com.hzm.yuchao.simple.constant.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, TicketDO> implements TicketService {

    @Resource
    private VenueService venueService;

    @Resource
    private SkuService skuService;

    @Resource
    private MatchService matchService;

    @Resource
    private ThreadService threadService;

    @Resource
    private GateClient gateClient;

    @Resource
    private UserService userService;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private AlarmService alarmService;

    @Override
    public void pushPerson(MatchDO matchDO, Long ticketId) {

        threadService.schedule(() -> {

            TicketDO ticketDO = this.getById(ticketId);

            if (ticketDO.getSaleStatus() != TicketSaleStatusEnum.SOLD) {
                log.warn("同步ticket，id: {}, 但是状态为：{}", ticketId, ticketDO.getSaleStatus());
                return;
            }

            try {
                String ticketNo = String.format("1%03d%09d", matchDO.getId(), ticketDO.getId());

                GatePushPersonRequest request = new GatePushPersonRequest();
                request.setThirdId(ticketDO.getBid());
                request.setOrderNo(ticketNo);
                request.setPrice(AmountConverter.toYuan(ticketDO.getPrice()));

                TicketShowInfoDTO showInfoDTO = JSONObject.parseObject(matchDO.getTicketShowInfo(), TicketShowInfoDTO.class);
                // 历史数据为空
                if (showInfoDTO == null) {
                    showInfoDTO = new TicketShowInfoDTO();
                }
                if (showInfoDTO.isArea()) {
                    request.setBigArea(ticketDO.getArea());
                }
                if (showInfoDTO.isSubArea()) {
                    request.setBigArea(ticketDO.getSubArea());
                }
                if (showInfoDTO.isSeatRow() || showInfoDTO.isSeatNo()) {
                    String seatInfo = showInfoDTO.isSeatRow() ? ticketDO.getSeatRow() + "排" : "";
                    seatInfo += showInfoDTO.isSeatNo() ? ticketDO.getSeatNo() + "号" : "";
                    request.setSeatNo(seatInfo);
                }
                // 若姓名、证件类型、证件号为空，为闸机默认分配随机标识，避免下游校验失败
                String randomTag = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                String name = ticketDO.getName();
                if (name == null || name.trim().isEmpty()) {
                    name = randomTag;
                }
                request.setName(name);
                request.setMobile(ticketDO.getMobile());
                String type = "3";
                if (ticketDO.getIdType() == IdTypeEnum.ID_CARD) {
                    type = "1";
                } else if (ticketDO.getIdType() == IdTypeEnum.PASSPORT) {
                    type = "2";
                }
                request.setCertificateType(type);
                String certificateNo = ticketDO.getIdNo();
                if (certificateNo == null || certificateNo.trim().isEmpty()) {
                    certificateNo = randomTag;
                }
                request.setCertificateNo(certificateNo);

                // 通知闸机
                gateClient.pushPerson(matchDO.getGateUrl(), matchDO.getGateToken(), request);

                // 更新 DB
                LambdaUpdateWrapper<TicketDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(TicketDO::getSyncStatus, TicketSyncStatusEnum.SYNCED);
                updateWrapper.eq(TicketDO::getId, ticketDO.getId());
                this.update(updateWrapper);


            } catch (Exception e) {
                log.error("购票-通知闸机人员信息失败，ticket id: {}", ticketDO.getId(), e);

                // 超过3个小时及以上
                if (ticketDO.getSaleTime().getTime() - System.currentTimeMillis() > 3 * 3600 * 1000) {
                    alarmService.alarm("购票-通知闸机人员信息失败，ticket id: " + ticketDO.getId());
                }
            }
        }, 10_000);
    }

    @Override
    public void refundPush(MatchDO matchDO, Long orderId) {

        // 释放座位
        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getOrderId, orderId);
        List<TicketDO> ticketDOList = this.list(queryWrapper);

        List<String> thirdIdList = ticketDOList.stream().map(TicketDO::getBid).collect(Collectors.toList());

        GateRefundPushRequest request = new GateRefundPushRequest(thirdIdList);

        // 通知闸机
        gateClient.refundPush(matchDO.getGateUrl(), matchDO.getGateToken(), request);
    }

    @Override
    @Transactional
    public TicketDO giftTicket(GiftTicketRequest giftTicketRequest) {

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getBid, giftTicketRequest.getTicketBid());
        queryWrapper.last(" for update ");
        TicketDO ticketDO = this.getOne(queryWrapper);
        if (ticketDO == null) {
            throw new BizException("赠票不存在");
        }
        if (ticketDO.getTicketType() != TicketTypeEnum.GIFT_TICKET) {
            throw new BizException("非法访问");
        }
        if (ticketDO.getSaleStatus() != TicketSaleStatusEnum.UNSOLD) {
            throw new BizException("赠票已被预订");
        }

        SkuDO skuDO = skuService.getById(ticketDO.getSkuId());
        if (skuDO == null) {
            throw new BizException("商品不存在");
        }
        if (skuDO.getSkuType() != TicketTypeEnum.GIFT_TICKET) {
            throw new BizException("非赠票");
        }
        if (skuDO.getSkuStatus() != SkuStatusEnum.ENABLE) {
            throw new BizException("赠票暂停售卖");
        }

        MatchDO matchDO = matchService.getById(ticketDO.getMatchId());
        if (matchDO == null || matchDO.getStatus() == MatchStatusEnum.DISABLE) {
            throw new BizException("赛程不存在或已下架");
        }
        if (System.currentTimeMillis() > matchDO.getEndTime().getTime()) {
            throw new BizException("赛程已结束");
        }
        if (matchDO.getStartSaleTime() != null && System.currentTimeMillis() < matchDO.getStartSaleTime().getTime()) {
            throw new BizException("还未开始售卖");
        }
        if (matchDO.getEndSaleTime() != null && System.currentTimeMillis() > matchDO.getEndSaleTime().getTime()) {
            throw new BizException("已经停止售卖");
        }

        UserDO userDO = userService.getById(SessionUtils.getUserId());
        if (userDO == null) {
            throw new BizException("用户不存在");
        }
        if (userDO.getIdNo() == null) {
            throw new BizException("用户身份信息不存在");
        }

        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getMatchId, matchDO.getId());
        queryWrapper.eq(TicketDO::getIdType, userDO.getIdType());
        queryWrapper.eq(TicketDO::getIdNo, userDO.getIdNo());
        TicketDO exists = this.getOne(queryWrapper, false);
        if (exists != null) {
            throw new BizException("您已有本场赛事的票据，每人限购一张。");
        }

        LambdaUpdateWrapper<TicketDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(TicketDO::getBuyerId, userDO.getId());
        updateWrapper.set(TicketDO::getName, userDO.getName());
        updateWrapper.set(TicketDO::getIdType, userDO.getIdType());
        updateWrapper.set(TicketDO::getIdNo, userDO.getIdNo());
        updateWrapper.set(TicketDO::getMobile, userDO.getMobile());
        updateWrapper.set(TicketDO::getSaleStatus, TicketSaleStatusEnum.SOLD);
        updateWrapper.set(TicketDO::getSaleTime, new Date());
        updateWrapper.eq(TicketDO::getId, ticketDO.getId());
        updateWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.UNSOLD);
        boolean update = this.update(updateWrapper);

        if (!update) {
            log.info("理论赠票出票成功，但出票失败. ticketId: {}", ticketDO.getId());
            throw new BizException("系统繁忙");
        }

        // 更新区域库存
        skuMapper.updateSkuStock(ticketDO.getSkuId());

        this.pushPerson(matchDO, ticketDO.getId());

        return this.getById(ticketDO.getId());
    }
}