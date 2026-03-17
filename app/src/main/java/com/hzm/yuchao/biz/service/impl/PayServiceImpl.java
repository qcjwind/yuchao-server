package com.hzm.yuchao.biz.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.service.WxPayService;
import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.mapper.OrderMapper;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import com.hzm.yuchao.biz.mapper.TicketMapper;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.OrderDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.simple.lock.DatabaseDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PayServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements PayService {

    @Resource
    private MatchService matchService;

    @Resource
    private VenueService venueService;

    @Resource
    private TicketService ticketService;

    @Resource
    private SkuService skuService;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private TicketMapper ticketMapper;

    @Resource
    private DatabaseDistributedLock distributedLock;

    @Resource
    private WxPayService wxPayService;

    @Resource
    private UserService userService;

    @Resource
    private OrderService orderService;

    @Override
    @Transactional
    public void paySuccess(Long orderId, String orderNo) {

        OrderDO lockDO = orderService.lockOrder(orderId, orderNo);

        // 更新订单状态
        if (lockDO.getOrderStatus() == OrderStatusEnum.WAIT_PAY) {
            LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();

            updateWrapper.set(OrderDO::getOrderStatus, OrderStatusEnum.PAY_SUCCESS);
            updateWrapper.set(OrderDO::getPayTime, new Date());
            updateWrapper.eq(OrderDO::getId, lockDO.getId());
            updateWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.WAIT_PAY);

            boolean update = this.update(updateWrapper);

            if (!update) {
                log.error("订单更新失败，理论上不应该存在。 {}", JSONUtil.toJsonStr(lockDO));
                return;
            }

            // 更新sku 状态
            LambdaUpdateWrapper<TicketDO> ticketUpdateWrapper = new LambdaUpdateWrapper<>();
            ticketUpdateWrapper.set(TicketDO::getSaleStatus, TicketSaleStatusEnum.SOLD);
            ticketUpdateWrapper.eq(TicketDO::getOrderId, lockDO.getId());
            ticketService.update(ticketUpdateWrapper);

            // 更新 通知闸机
            MatchDO matchDO = matchService.getById(lockDO.getMatchId());
            LambdaQueryWrapper<TicketDO> ticketQueryWrapper = new LambdaQueryWrapper<>();
            ticketQueryWrapper.eq(TicketDO::getOrderId, lockDO.getId());
            List<TicketDO> ticketDOList = ticketService.list(ticketQueryWrapper);
            for (TicketDO ticketDO : ticketDOList) {
                ticketService.pushPerson(matchDO, ticketDO.getId());
            }
        } else {
            log.warn("计划更新订单未支付成功，但订单状态已经是。 {}", JSONUtil.toJsonStr(lockDO));
        }
    }

    @Override
    public void refundSuccess(Long orderId, String orderNo) {

        OrderDO lockDO = orderService.lockOrder(orderId, orderNo);

        // 更新订单状态
        if (lockDO.getOrderStatus() == OrderStatusEnum.REFUND_ING) {
            LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();

            updateWrapper.set(OrderDO::getOrderStatus, OrderStatusEnum.REFUND_SUCCESS);
            updateWrapper.set(OrderDO::getPayInfo, null);
            updateWrapper.eq(OrderDO::getId, lockDO.getId());
            updateWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.REFUND_ING);

            boolean update = this.update(updateWrapper);
            if (!update) {
                log.error("订单更新失败，理论上不应该存在。 {}", JSONUtil.toJsonStr(lockDO));
                return;
            }

            // 释放座位
            LambdaUpdateWrapper<TicketDO> ticketUpdateWrapper = new LambdaUpdateWrapper<>();
            ticketUpdateWrapper.set(TicketDO::getSaleStatus, TicketSaleStatusEnum.UNSOLD);
            ticketUpdateWrapper.set(TicketDO::getOrderId, null);
            ticketUpdateWrapper.set(TicketDO::getName, null);
            ticketUpdateWrapper.set(TicketDO::getMobile, null);
            ticketUpdateWrapper.set(TicketDO::getIdType, null);
            ticketUpdateWrapper.set(TicketDO::getIdNo, null);
            ticketUpdateWrapper.set(TicketDO::getBuyerId, null);
            ticketUpdateWrapper.set(TicketDO::getSaleTime, null);
            ticketUpdateWrapper.eq(TicketDO::getOrderId, lockDO.getId());
            ticketService.update(ticketUpdateWrapper);

            // 统计库存
            skuMapper.updateSkuStock(lockDO.getSkuId());

        } else {
            log.warn("计划更新订单未退款成功，但订单状态已经是。 {}", JSONUtil.toJsonStr(lockDO));
        }
    }
}
