package com.hzm.yuchao.biz.controller.app;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.hzm.yuchao.biz.controller.app.req.BuyTicketRequest;
import com.hzm.yuchao.biz.controller.app.req.GiftTicketRequest;
import com.hzm.yuchao.biz.controller.app.resp.OrderInfoVO;
import com.hzm.yuchao.biz.controller.app.resp.OrderListVO;
import com.hzm.yuchao.biz.enums.BooleanEnum;
import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSyncStatusEnum;
import com.hzm.yuchao.biz.mapper.OrderMapper;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import com.hzm.yuchao.biz.model.*;
import com.hzm.yuchao.biz.outter.wechat.config.WxPayProperties;
import com.hzm.yuchao.biz.service.*;
import com.hzm.yuchao.biz.utils.RefundRateCalculator;
import com.hzm.yuchao.biz.utils.TicketDataMaskingUtil;
import com.hzm.yuchao.simple.base.PageResponse;
import com.hzm.yuchao.simple.base.SimpleResponse;
import com.hzm.yuchao.simple.constant.Constants;
import com.hzm.yuchao.simple.constant.SessionUtils;
import com.hzm.yuchao.simple.ratelimit.RateLimit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@Slf4j
@Api(tags = "APP-订单信息")
@CrossOrigin
@RestController
@RequestMapping("/app/order/")
public class OrderAppController {

    @Resource
    private MatchService matchService;

    @Resource
    private VenueService venueService;

    @Resource
    private TicketService ticketService;

    @Resource
    private OrderService orderService;

    @Resource
    private SkuService skuService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private WxPayService wxPayService;

    @Resource
    private PayService payService;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private UserService userService;

    @Resource
    private WxPayProperties wxPayProperties;

    @ApiOperation("我的订单列表")
    @PostMapping("myList")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public PageResponse<OrderListVO> myList(Integer pageSize, Integer pageNumber) {

        Page<OrderListVO> page = new Page<>(pageNumber == null ? 1 : pageNumber, pageSize == null ? 10 : pageSize);

        IPage<OrderListVO> pageData = orderMapper.selectMyOrderByPage(page, SessionUtils.getUserId());

        // 脱敏
        pageData.getRecords().forEach(t -> {
            t.setOrderInfo(null);
            t.setPayInfo(null);
        });

        return PageResponse.ok(pageData);
    }

    @ApiOperation("我的订单详情")
    @PostMapping("myInfo")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<OrderInfoVO> myInfo(long orderId) {

        OrderDO orderDO = orderService.getById(orderId);
        if (orderDO == null) {
            return SimpleResponse.fail("订单不存在");
        }
        if (orderDO.getUserId() != SessionUtils.getUserId()) {
            return SimpleResponse.fail("非法访问");
        }

        MatchDO matchDO = matchService.getById(orderDO.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛程不存在");
        }

        VenueDO venueDO = venueService.getById(matchDO.getVenueId());
        if (venueDO == null) {
            return SimpleResponse.fail("场馆不存在");
        }

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getOrderId, orderId);
        queryWrapper.last("limit 100");
        List<TicketDO> list = ticketService.list(queryWrapper);

        // 脱敏
        orderDO.setOrderInfo(null);
        orderDO.setName(null);
        TicketDataMaskingUtil.ticketDataMasking(list);

        return SimpleResponse.ok(new OrderInfoVO(orderDO, matchDO, venueDO, list));
    }


    @ApiOperation("支付后轮询支付结果")
    @PostMapping("payLoop")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<OrderInfoVO> payLoop(long orderId) {

        OrderDO orderDO = orderService.getById(orderId);
        if (orderDO == null) {
            return SimpleResponse.fail("订单不存在");
        }
        if (orderDO.getUserId() != SessionUtils.getUserId()) {
            return SimpleResponse.fail("非法访问");
        }

        if (orderDO.getOrderStatus() == OrderStatusEnum.WAIT_PAY) {
            try {
                // 查询微信
                WxPayOrderQueryV3Result result = wxPayService.queryOrderV3(null, orderDO.getOrderNo());

                if (WxPayConstants.WxpayTradeStatus.SUCCESS.equals(result.getTradeState())) {
                    // 更新状态为成功。一锁二判定三更新
                    payService.paySuccess(orderDO.getId(), null);
                    orderDO = orderService.getById(orderId);
                }
            } catch (WxPayException e) {
                log.error("查询微信订单失败", e);
            }
        }

        // 脱敏
        orderDO.setOrderInfo(null);
        orderDO.setName(null);

        return SimpleResponse.ok(new OrderInfoVO(orderDO, null, null, null));
    }

    @ApiOperation("计算退款金额")
    @PostMapping("calcRefundPrice")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<Integer> calcRefundPrice(long orderId) {

        OrderDO orderDO = orderService.getById(orderId);
        if (orderDO == null) {
            return SimpleResponse.fail("订单不存在");
        }
        if (orderDO.getUserId() != SessionUtils.getUserId()) {
            return SimpleResponse.fail("非法访问");
        }
        if (orderDO.getOrderStatus() != OrderStatusEnum.PAY_SUCCESS) {
            return SimpleResponse.fail("当前订单状态，无法计算退款金额");
        }

        MatchDO matchDO = matchService.getById(orderDO.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛事不存在");
        }
        if (matchDO.getAllowRefund() != BooleanEnum.Y) {
            return SimpleResponse.fail("赛事不允许退款");
        }
        // 退款限制判断
        int refundRate = RefundRateCalculator.calculateRefundRate(matchDO.getRefundRule());
        if (refundRate > 100) {
            return SimpleResponse.fail("退款规则配置错误");
        }
        if (refundRate <= 0) {
            return SimpleResponse.fail("无法退款");
        }

        return SimpleResponse.ok(orderDO.getTotalPrice() * refundRate / 100);
    }

    @ApiOperation("取消订单")
    @PostMapping("cancel")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<Object> cancel(long orderId) {

        OrderDO orderDO = orderService.getById(orderId);
        if (orderDO == null) {
            return SimpleResponse.fail("订单不存在");
        }
        if (orderDO.getUserId() != SessionUtils.getUserId()) {
            return SimpleResponse.fail("非法访问");
        }
        if (orderDO.getOrderStatus() != OrderStatusEnum.WAIT_PAY) {
            return SimpleResponse.fail("当前订单状态，无法进行取消");
        }

        orderService.cancel(orderId, true);

        return SimpleResponse.ok();
    }

    @ApiOperation("退款申请")
    @PostMapping("refundApply")
    @RateLimit(qps = 10)
    @Transactional
    public SimpleResponse<Object> refundApply(long orderId) {

        OrderDO lockDO = orderService.lockOrder(orderId, null);
        if (lockDO == null) {
            return SimpleResponse.fail("订单不存在");
        }
        if (lockDO.getUserId() != SessionUtils.getUserId()) {
            return SimpleResponse.fail("非法访问");
        }
        if (lockDO.getOrderStatus() != OrderStatusEnum.PAY_SUCCESS) {
            return SimpleResponse.fail("当前订单状态，无法进行退款");
        }
        if (lockDO.getTotalPrice() <= 0) {
            return SimpleResponse.fail("0元订单，不支持退款");
        }

        MatchDO matchDO = matchService.getById(lockDO.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛事不存在");
        }
        if (matchDO.getAllowRefund() != BooleanEnum.Y) {
            return SimpleResponse.fail("赛事不允许退款");
        }

        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getOrderId, orderId);
        queryWrapper.eq(TicketDO::getVerificationStatus, BooleanEnum.Y);
        queryWrapper.last("limit 1");
        TicketDO verificationTicket = ticketService.getOne(queryWrapper);
        if (verificationTicket != null) {
            return SimpleResponse.fail("已有票码核销，不能进行退款");
        }

        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TicketDO::getOrderId, orderId);
        queryWrapper.eq(TicketDO::getSyncStatus, TicketSyncStatusEnum.NOT_SYNC);
        queryWrapper.last("limit 1");
        TicketDO notSyncTicket = ticketService.getOne(queryWrapper);
        if (notSyncTicket != null) {
            return SimpleResponse.fail("票码还未推送，不能进行退款");
        }

        // 退款限制判断
        int refundRate = RefundRateCalculator.calculateRefundRate(matchDO.getRefundRule());
        if (refundRate > 100) {
            return SimpleResponse.fail("退款规则配置错误");
        }
        if (refundRate <= 0) {
            return SimpleResponse.fail("无法退款");
        }

        // 退款金额计算
        int refundPrice = lockDO.getTotalPrice() * refundRate / 100;
        log.info("订单 id: {} 退款计算，退款费率：{}%，订单金额：{}，退款金额：{}",
                lockDO.getId(), refundRate, lockDO.getTotalPrice(), refundPrice);
        if (refundPrice <= 0) {
            return SimpleResponse.fail("退款金额为0，无法进行退款");
        }
        lockDO.setRefundPrice(refundPrice);

        // 先退票
        ticketService.refundPush(matchDO, lockDO.getId());

        try {

            WxPayRefundV3Request request = new WxPayRefundV3Request();
            request.setOutTradeNo(lockDO.getOrderNo());
            request.setOutRefundNo(lockDO.getOrderNo());
            WxPayRefundV3Request.Amount amount = new WxPayRefundV3Request.Amount();
            amount.setTotal(lockDO.getTotalPrice());
            amount.setRefund(lockDO.getRefundPrice());
            amount.setCurrency(WxPayConstants.CurrencyType.CNY);
            request.setAmount(amount);
            request.setNotifyUrl(wxPayProperties.getRefundNotifyUrl());
            // 发起退款申请
            WxPayRefundV3Result result = wxPayService.refundV3(request);
            lockDO.setWxRefundId(result.getRefundId());

            // 更新DB
            LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderDO::getOrderStatus, OrderStatusEnum.REFUND_ING);
            updateWrapper.set(OrderDO::getWxRefundId, lockDO.getWxRefundId());
            updateWrapper.set(OrderDO::getRefundPrice, lockDO.getRefundPrice());
            updateWrapper.set(OrderDO::getRefundTime, new Date());
            updateWrapper.eq(OrderDO::getId, lockDO.getId());
            updateWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.PAY_SUCCESS);
            boolean update = orderService.update(updateWrapper);

            if (!update) {
                log.error("理论上退款提交成功，但数据库更新失败。 {}", JSONUtil.toJsonStr(lockDO));
            }

            return SimpleResponse.ok();

        } catch (WxPayException e) {
            log.error("发起退款申请失败, 订单：{}", JSONUtil.toJsonStr(lockDO), e);

            // 更新DB
            LambdaUpdateWrapper<OrderDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(OrderDO::getOrderStatus, OrderStatusEnum.REFUND_ING);
            updateWrapper.set(OrderDO::getRefundPrice, lockDO.getRefundPrice());
            updateWrapper.set(OrderDO::getRefundTime, new Date());
            updateWrapper.eq(OrderDO::getId, lockDO.getId());
            updateWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.PAY_SUCCESS);
            orderService.update(updateWrapper);

            return SimpleResponse.fail("发起退款申请失败");
        }
    }

    /** 正则表达式：验证港澳居民通行证 H/M + 10位或8位数字 */
    public static final String REGEX_HK_CARD = "^[HMhm]{1}([0-9]{10}|[0-9]{8})$";
    /** 正则表达式：验证台湾居民通行证 新版8位或18位数字,旧版10位数字 + 英文字母 */
    public static final String REGEX_TW_CARD = "^\\d{8}|^[a-zA-Z0-9]{10}|^\\d{18}$";

    @ApiOperation("购票")
    @PostMapping("buySaleTicket")
    @RateLimit(qps = 100)
    public SimpleResponse<OrderDO> buySaleTicket(@Valid BuyTicketRequest buyTicketRequest) {

        if (StringUtils.isNotEmpty(buyTicketRequest.getListJsonStr())) {
            buyTicketRequest.setList(JSONArray.parseArray(buyTicketRequest.getListJsonStr(), BuyTicketRequest.BuyTicket.class));
        }

        if (CollectionUtils.isEmpty(buyTicketRequest.getList())) {
            return SimpleResponse.fail("无购票人信息");
        }

        // 根据 skuId 查询赛事，判断购票是否需要身份证
        SkuDO skuDO = skuService.getById(buyTicketRequest.getSkuId());
        if (skuDO == null) {
            return SimpleResponse.fail("票档不存在");
        }
        MatchDO matchDO = matchService.getById(skuDO.getMatchId());
        if (matchDO == null) {
            return SimpleResponse.fail("赛事不存在");
        }

        boolean needIdForTicket = matchDO.getNeedIdForTicket() == BooleanEnum.Y;

        for (BuyTicketRequest.BuyTicket buyTicket : buyTicketRequest.getList()) {

            // 添加购票人自己
            if (buyTicket.isMySelf()) {
                UserDO userDO = userService.getById(SessionUtils.getUserId());
                buyTicket.setName(userDO != null ? userDO.getName() : null);
                buyTicket.setMobile(userDO != null ? userDO.getMobile() : null);
                buyTicket.setIdNo(userDO != null ? userDO.getIdNo() : null);
                buyTicket.setIdType(userDO != null ? userDO.getIdType() : null);
            }

            // 若赛事不需要身份证，忽略前端传入的姓名/证件信息，避免后续逻辑误判
            if (!needIdForTicket) {
                buyTicket.setName(null);
                buyTicket.setIdType(null);
                buyTicket.setIdNo(null);
            }

            // 手机号必填并校验
            if (StringUtils.isEmpty(buyTicket.getMobile())) {
                return SimpleResponse.fail("购票人手机号不能为空");
            }
            if (!buyTicket.getMobile().matches(Constants.MOBILE_REGEX)) {
                return SimpleResponse.fail("【" + buyTicket.getMobile() + "】手机号不正确");
            }

            // 购票需要身份证时，校验姓名、证件类型、证件号
            if (needIdForTicket) {
                if (StringUtils.isEmpty(buyTicket.getName()) ||
                        buyTicket.getIdType() == null ||
                        StringUtils.isEmpty(buyTicket.getIdNo())) {
                    return SimpleResponse.fail("购票人信息不全");
                }
                if (buyTicket.getName().length() < 2 || buyTicket.getName().length() > 16) {
                    return SimpleResponse.fail("【" + buyTicket.getName() + "】姓名不正确");
                }
                if (buyTicket.getIdNo().length() < 8 || buyTicket.getIdNo().length() > 32) {
                    return SimpleResponse.fail("【" + buyTicket.getIdNo() + "】证件信息不正确");
                }
            }
        }

        OrderDO result = orderService.buy(buyTicketRequest);

        // 脱敏
        result.setOrderInfo(null);
        result.setName(null);

        return SimpleResponse.ok(result);
    }

    @ApiOperation("赠票，返回ID代表赠票激活成功")
    @PostMapping("buyGiftTicket")
    @RateLimit(qps = Constants.QPS_SIMPLE)
    public SimpleResponse<TicketDO> buyGiftTicket(@Valid GiftTicketRequest giftTicketRequest) {

        TicketDO ticketDO = ticketService.giftTicket(giftTicketRequest);

        // 脱敏
        TicketDataMaskingUtil.ticketDataMasking(ticketDO);

        return SimpleResponse.ok(ticketDO);
    }

}
