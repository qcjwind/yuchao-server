package com.hzm.yuchao.biz.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.hzm.yuchao.biz.controller.app.req.BuyTicketRequest;
import com.hzm.yuchao.biz.dto.SubAreaStockDTO;
import com.hzm.yuchao.biz.enums.*;
import com.hzm.yuchao.biz.mapper.OrderMapper;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import com.hzm.yuchao.biz.mapper.TicketMapper;
import com.hzm.yuchao.biz.model.*;
import com.hzm.yuchao.biz.outter.wechat.config.WxPayProperties;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.biz.utils.ConsecutiveTicketUtils;
import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.constant.SessionUtils;
import com.hzm.yuchao.simple.lock.DatabaseDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements OrderService {

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
    private WxPayProperties wxPayProperties;

    @Resource
    private ThreadService threadService;

    @Resource
    private PayService payService;

    @Override
    @Transactional
    public OrderDO buy(BuyTicketRequest buyTicketRequest) {

        SkuDO skuDO = skuService.getById(buyTicketRequest.getSkuId());
        if (skuDO == null) {
            throw new BizException("商品不存在");
        }
        // 校验库存
        if (buyTicketRequest.getList().size() > skuDO.getStockTicket()) {
            throw new BizException("库存不足");
        }
        if (skuDO.getSkuType() != TicketTypeEnum.SALE_TICKET) {
            throw new BizException("非卖票");
        }
        if (skuDO.getSkuStatus() != SkuStatusEnum.ENABLE) {
            throw new BizException("商品暂停售卖");
        }

        MatchDO matchDO = matchService.getById(skuDO.getMatchId());
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
        // 单次限购 x 张（无论是否需要身份证）
        if (buyTicketRequest.getList().size() > matchDO.getBuyLimit()) {
            throw new BizException("单次超过限购数量");
        }

        // 当前赛事是否需要身份证购票
        boolean needIdForTicket = matchDO.getNeedIdForTicket() == BooleanEnum.Y;

        if (needIdForTicket) {
            // 原有按用户维度的累计限购逻辑
            int totalBuyNum = this.baseMapper.getTotalBuyNum(SessionUtils.getUserId(), matchDO.getId());
            if (buyTicketRequest.getList().size() + totalBuyNum > matchDO.getBuyLimit()) {
                throw new BizException("超过限购数量，限购" + matchDO.getBuyLimit() + "张。已购 " + totalBuyNum + " 张");
            }
        } else {
            // 不需要身份证时，按手机号限购：每个手机号限购一张本场赛事的票
            for (BuyTicketRequest.BuyTicket buyTicket : buyTicketRequest.getList()) {
                String mobile = buyTicket.getMobile();
                if (mobile == null || mobile.trim().isEmpty()) {
                    continue; // 控制层已校验手机号必填
                }
                LambdaQueryWrapper<TicketDO> phoneLimitWrapper = new LambdaQueryWrapper<>();
                phoneLimitWrapper.eq(TicketDO::getMatchId, matchDO.getId());
                phoneLimitWrapper.eq(TicketDO::getMobile, mobile);
                phoneLimitWrapper.in(TicketDO::getSaleStatus,
                        TicketSaleStatusEnum.WAIT_PAY,
                        TicketSaleStatusEnum.SOLD);
                TicketDO existsByMobile = ticketService.getOne(phoneLimitWrapper, false);
                if (existsByMobile != null) {
                    throw new BizException("每个手机号限购一张。【" + mobile + "】已有本场赛事的票据");
                }
            }
        }

        int waitPayNum = this.baseMapper.getWaitPayNum(SessionUtils.getUserId(), matchDO.getId());
        if (waitPayNum > 0) {
            throw new BizException("已有待支付订单，无法继续下单。");
        }

        // 资源标识
        String resourceKey = "BUY_TICKET_" + SessionUtils.getUserId();
        String holderId = null;

        try {
            // 1. 获取锁（过期时间30秒，防止死锁）
            holderId = distributedLock.tryLock(resourceKey, 30);
            if (holderId == null) {
                // 未获取到锁，返回失败（或重试）
                throw new BizException("加锁失败，请稍后再试");
            }

            return buyCore(buyTicketRequest, matchDO, skuDO);

        } catch (DuplicateKeyException e) {
            throw new BizException("重复请求，请重新进入小程序");
        } catch (DeadlockLoserDataAccessException e) {
            throw new BizException("系统负载较大，请稍后再试。");
        } catch (WxPayException e) {
            log.error("微信下单失败", e);
            throw new BizException("下单失败，请稍后再试。");
        } finally {
            // 3. 释放锁（必须在finally中执行，确保锁被释放）
            if (holderId != null) {
                distributedLock.releaseLock(resourceKey, holderId);
            }
        }
    }

    @Override
    @Transactional
    public OrderDO lockOrder(Long orderId, String orderNo) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(orderId != null, OrderDO::getId, orderId);
        queryWrapper.eq(StringUtils.isNotEmpty(orderNo), OrderDO::getOrderNo, orderNo);
        queryWrapper.last("for update");

        OrderDO orderDO = this.getOne(queryWrapper);

        if (orderDO == null) {
            throw new BizException("订单不存在，id: " + orderId + ", orderNo: " + orderNo);
        }
        return orderDO;
    }

    @Override
    @Transactional
    public void cancel(long orderId, boolean isUserClose) {

        OrderDO lockDO = this.lockOrder(orderId, null);
        if (lockDO == null) {
            throw new BizException("订单不存在");
        }
        if (isUserClose && lockDO.getUserId() != SessionUtils.getUserId()) {
            throw new BizException("非法访问");
        }
        if (lockDO.getOrderStatus() != OrderStatusEnum.WAIT_PAY) {
            if (isUserClose) {
                throw new BizException("当前订单状态，无法进行取消");
            } else {
                return;
            }
        }

        try {
            // 查询微信
            WxPayOrderQueryV3Result result = wxPayService.queryOrderV3(null, lockDO.getOrderNo());

            if (WxPayConstants.WxpayTradeStatus.NOTPAY.equals(result.getTradeState())) {

                long timeDiff = System.currentTimeMillis() - lockDO.getOrderTime().getTime();
                // 超过5分钟
                if (timeDiff > 5 * 60 * 1000) {
                    log.info("调用微信关闭订单，id: {}", lockDO.getId());
                    wxPayService.closeOrderV3(lockDO.getOrderNo());
                } else {
                    threadService.schedule(() -> {
                        // 必须间隔5分钟才能调用。
                        try {
                            log.info("调用微信关闭订单，id: {}", lockDO.getId());
                            wxPayService.closeOrderV3(lockDO.getOrderNo());
                        } catch (WxPayException e) {
                            log.error("微信关单失败, id: {}", lockDO.getId(), e);
                        }
                    }, Math.max(1, (5 * 60 * 1000 - timeDiff) + 10));
                }

                // 修改订单
                LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(OrderDO::getOrderStatus, OrderStatusEnum.CANCEL);
                updateWrapper.set(OrderDO::getWxPrepayId, null);
                updateWrapper.set(OrderDO::getPayInfo, null);
                updateWrapper.eq(OrderDO::getId, orderId);
                updateWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.WAIT_PAY);
                this.update(updateWrapper);

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
                ticketUpdateWrapper.eq(TicketDO::getOrderId, orderId);
                ticketService.update(ticketUpdateWrapper);

                // 统计库存
                skuMapper.updateSkuStock(lockDO.getSkuId());

            } else {
                if (isUserClose) {
                    throw new BizException("订单已支付，无法进行取消");
                } else {
                    if (WxPayConstants.WxpayTradeStatus.SUCCESS.equals(result.getTradeState())) {
                        payService.paySuccess(orderId, null);
                    }
                }
            }
        } catch (WxPayException e) {
            log.error("查询微信订单失败, {}", lockDO.getOrderNo(), e);
            throw new BizException("查询微信订单失败, 请稍后再试");
        }
    }

    private OrderDO buyCore(BuyTicketRequest buyTicketRequest, MatchDO matchDO, SkuDO skuDO) throws WxPayException {

        boolean needPay = skuDO.getPrice() > 0;
        int totalPrice = skuDO.getPrice() * buyTicketRequest.getList().size();

        // 创建订单
        OrderDO orderDO = new OrderDO();
        orderDO.setOrderNo(buyTicketRequest.getRequestNo());
        orderDO.setUserId(SessionUtils.getUserId());
        orderDO.setMatchId(matchDO.getId());
        orderDO.setVenueId(matchDO.getVenueId());
        orderDO.setSkuId(skuDO.getId());
        orderDO.setBuyNum(buyTicketRequest.getList().size());
        orderDO.setTotalPrice(totalPrice);
        orderDO.setOrderStatus(needPay ? OrderStatusEnum.WAIT_PAY : OrderStatusEnum.PAY_SUCCESS);
        orderDO.setOrderTime(new Date());
        orderDO.setPayTime(needPay ? null : new Date());
        orderDO.setOrderInfo(JSONUtil.toJsonStr(buyTicketRequest.getList()));
        UserDO userDO = userService.getById(SessionUtils.getUserId());
        orderDO.setName(userDO.getName());
        orderDO.setSkuName(skuDO.getSkuName());
        this.save(orderDO);

        if (buyTicketRequest.getList().size() == 1) {
            // 极速出票
            TicketDO ticketDO = getOne(orderDO, skuDO, buyTicketRequest.getList().get(0), null);

            if (!needPay) {
                ticketService.pushPerson(matchDO, ticketDO.getId());
            }
        } else {
            List<TicketDO> multi = getMulti(orderDO, skuDO, buyTicketRequest.getList());
            if (!needPay) {
                for (TicketDO ticketDO : multi) {
                    ticketService.pushPerson(matchDO, ticketDO.getId());
                }
            }
        }

        // 更新区域库存
        skuMapper.updateSkuStock(skuDO.getId());

        if (needPay) {
            WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
            request.setDescription(matchDO.getName());
            request.setOutTradeNo(orderDO.getOrderNo());
            request.setNotifyUrl(wxPayProperties.getPayNotifyUrl());
            WxPayUnifiedOrderV3Request.Amount amount = new WxPayUnifiedOrderV3Request.Amount();
            amount.setTotal(orderDO.getTotalPrice());
            request.setAmount(amount);
            WxPayUnifiedOrderV3Request.Payer payer = new WxPayUnifiedOrderV3Request.Payer();
            payer.setOpenid(userService.getById(SessionUtils.getUserId()).getOpenid());
            request.setPayer(payer);

            WxPayUnifiedOrderV3Result unifiedOrderV3 = wxPayService.unifiedOrderV3(TradeTypeEnum.JSAPI, request);
            WxPayUnifiedOrderV3Result.JsapiResult payInfo = unifiedOrderV3.getPayInfo(TradeTypeEnum.JSAPI,
                    wxPayService.getConfig().getAppId(),
                    wxPayService.getConfig().getMchId(),
                    wxPayService.getConfig().getPrivateKey());
            orderDO.setWxPrepayId(unifiedOrderV3.getPrepayId());
            orderDO.setPayInfo(JSONUtil.toJsonStr(payInfo));

            LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderDO::getWxPrepayId, orderDO.getWxPrepayId());
            updateWrapper.set(OrderDO::getPayInfo, orderDO.getPayInfo());
            updateWrapper.eq(OrderDO::getId, orderDO.getId());
            this.update(updateWrapper);
        }

        return orderDO;
    }

    private TicketDO getOne(OrderDO orderDO, SkuDO skuDO, BuyTicketRequest.BuyTicket buyTicket, Long ticketId) {

        // 当填写了证件类型和证件号时，校验每人限购一张（同一证件在本场只能买一张）
        // 注意：空字符串不算“填写”
        if (buyTicket.getIdType() != null && StringUtils.isNotBlank(buyTicket.getIdNo())) {
            LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketDO::getMatchId, skuDO.getMatchId());
            queryWrapper.eq(TicketDO::getIdType, buyTicket.getIdType());
            queryWrapper.eq(TicketDO::getIdNo, buyTicket.getIdNo());
            TicketDO exists = ticketService.getOne(queryWrapper, false);
            if (exists != null) {
                String who = StringUtils.isNotBlank(buyTicket.getName()) ? buyTicket.getName() : buyTicket.getMobile();
                log.info("每人限购一张。{}, 已有本场赛事的票据. ticketId: {}", JSONUtil.toJsonStr(buyTicket), JSONUtil.toJsonStr(exists));
                throw new BizException("每人限购一张。【" + who + "】已有本场赛事的票据");
            }
        }

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getSkuId, skuDO.getId());
        queryWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.UNSOLD);
        queryWrapper.eq(ticketId != null, TicketDO::getId, ticketId);
        queryWrapper.orderByAsc(TicketDO::getId);
        // 指定票的时候，不可跳过
        queryWrapper.last("limit 1 for update " + (ticketId != null ? "SKIP LOCKED" : ""));
        TicketDO ticketDO = ticketService.getOne(queryWrapper);

        if (ticketDO == null) {
            // 已售罄
            String msg = ticketId == null ? "票已售罄" : ("指定票id【" + ticketId + "】已售出");
            throw new BizException(msg);
        }

        LambdaUpdateWrapper<TicketDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(TicketDO::getOrderId, orderDO.getId());
        updateWrapper.set(TicketDO::getBuyerId, SessionUtils.getUserId());
        updateWrapper.set(TicketDO::getName, buyTicket.getName());
        updateWrapper.set(TicketDO::getIdType, buyTicket.getIdType());
        updateWrapper.set(TicketDO::getIdNo, buyTicket.getIdNo());
        updateWrapper.set(TicketDO::getMobile, buyTicket.getMobile());
        // 为每张票生成唯一二维码内容 YC-UUID
        updateWrapper.set(TicketDO::getQrcode, "YC-" + UUID.randomUUID());
        updateWrapper.set(TicketDO::getPrice, skuDO.getPrice());
        updateWrapper.set(TicketDO::getSaleStatus, skuDO.getPrice() > 0 ? TicketSaleStatusEnum.WAIT_PAY : TicketSaleStatusEnum.SOLD);
        updateWrapper.set(TicketDO::getSaleTime, new Date());
        updateWrapper.eq(TicketDO::getId, ticketDO.getId());
        updateWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.UNSOLD);

        boolean update = ticketService.update(updateWrapper);

        if (!update) {
            log.info("理论出票成功，但出票失败");
            throw new BizException("系统繁忙");
        }

        return ticketService.getById(ticketDO.getId());
    }

    private List<TicketDO> getMulti(OrderDO orderDO, SkuDO skuDO, List<BuyTicketRequest.BuyTicket> buyTicketList) {

        int ticketNum = buyTicketList.size();

        List<SubAreaStockDTO> subAreaStockList = ticketMapper.selectUnsoldStock(skuDO.getId(), ticketNum);
        if (CollectionUtils.isEmpty(subAreaStockList)) {
            // 无连座 当前已无法连座
            log.info("当前已无法连座");
            return getNonConsecutiveSeats(orderDO, skuDO, buyTicketList);
        }

        List<TicketDO> targetTicketList = null;
        int retryTimes = 3;
        for (int i = 1; i <= retryTimes; i++) {

            SubAreaStockDTO stockDTO = getRandomElement(subAreaStockList);

            log.info("【第{}次出票】尝试从 {}区-{}排 进行出票。购票张数: {}, 当前库存：{}。", i, stockDTO.getSeatRow(), stockDTO.getSeatRow(), ticketNum, stockDTO.getStock());

            // 查询出该sku下所有未售的票
            LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TicketDO::getSkuId, skuDO.getId());
            queryWrapper.eq(TicketDO::getSubArea, stockDTO.getSubArea());
            queryWrapper.eq(TicketDO::getSeatRow, stockDTO.getSeatRow());
            queryWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.UNSOLD);
            List<TicketDO> ticketList = ticketService.list(queryWrapper);

            if (CollectionUtils.isEmpty(ticketList) || ticketList.size() < ticketNum) {
                log.info("当前排无法连座，进行重试。 库存: {}", ticketList.size());
                continue;
            }

            List<TicketDO> consecutiveInRow = ConsecutiveTicketUtils.findConsecutiveInRow(ticketList, ticketNum);
            if (CollectionUtils.isEmpty(consecutiveInRow) || consecutiveInRow.size() < ticketNum) {
                if (i == retryTimes) {
                    log.info("连座出票失败，降级为同排票。");
                    targetTicketList = consecutiveInRow.subList(0, ticketNum);
                } else {
                    log.info("连座检查失败，重新随机排数，进行重试。");
                }
                continue;
            }

            targetTicketList = consecutiveInRow;
            break;
        }

        if (targetTicketList != null) {
            try {
                for (int i = 0; i < targetTicketList.size(); i++) {
                    getOne(orderDO, skuDO, buyTicketList.get(i), targetTicketList.get(i).getId());
                }
                return targetTicketList;
            } catch (BizException e) {
                if (e.getMsg().contains("已有本场赛事的票据")) {
                    throw e;
                }
            } catch (Exception e) {
                log.info("连座/同排票出票失败，降级随机出票。失败原因：{}", e.getMessage());
            }
        }

        log.info("当前已无法连座");
        return getNonConsecutiveSeats(orderDO, skuDO, buyTicketList);
    }


    public static <T> T getRandomElement(List<T> list) {
        // 处理空列表或null的情况
        if (list == null || list.isEmpty()) {
            return null;
        }

        // 生成0到list.size()-1之间的随机索引
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());

        // 返回随机索引对应的元素
        return list.get(randomIndex);
    }


    // 获取非连座座位
    public List<TicketDO> getNonConsecutiveSeats(OrderDO orderDO, SkuDO skuDO, List<BuyTicketRequest.BuyTicket> buyTicketList) {
        List<TicketDO> list = new ArrayList<>();

        for (BuyTicketRequest.BuyTicket buyTicket : buyTicketList) {
            TicketDO one = getOne(orderDO, skuDO, buyTicket, null);
            list.add(one);
        }

        return list;
    }
}
