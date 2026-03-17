package com.hzm.yuchao.biz.jobs;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.biz.model.OrderDO;
import com.hzm.yuchao.biz.outter.wechat.config.WxPayProperties;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.OrderService;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.lock.DatabaseDistributedLock;
import com.hzm.yuchao.simple.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 退款补偿
 */
@Slf4j
@Component
public class RefundJob {

    @Resource
    private OrderService orderService;

    @Resource
    private DatabaseDistributedLock distributedLock;

    @Resource
    private ThreadService threadService;

    @Resource
    private MatchService matchService;

    @Resource
    private WxPayProperties wxPayProperties;

    @Resource
    private WxPayService wxPayService;

    @Resource
    private RefundJob self;

    // 每小时 10分运行
    @Scheduled(cron = "0 */10 * * * ?")
    public void execute() {

        TraceUtils.initTrace();

        log.info("退款失败订单 重新进行补偿！");

        // 资源标识
        String resourceKey = "JOB_REFUND";
        String holderId = null;

        try {
            // 1. 获取锁（过期时间30秒，防止死锁）
            holderId = distributedLock.tryLock(resourceKey, 30);
            if (holderId == null) {
                // 未获取到锁，返回失败（或重试）
                return;
            }

            doCore();

        } finally {
            // 3. 释放锁（必须在finally中执行，确保锁被释放）
            if (holderId != null) {
                distributedLock.releaseLock(resourceKey, holderId);
            }

            MDC.remove("traceId");
        }
    }

    // 查询需要补偿的记录
    private void doCore() {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.REFUND_ING);
        queryWrapper.isNull(OrderDO::getWxRefundId);
        queryWrapper.last("limit 100");

        List<OrderDO> orderDOList = orderService.list(queryWrapper);

        if (CollectionUtils.isEmpty(orderDOList)) {
            return;
        }

        log.info("退款失败订单补偿任务！ size: {}", orderDOList.size());
        for (OrderDO orderDO : orderDOList) {
            self.doSingle(orderDO.getId());
        }

    }

    @Transactional
    public void doSingle(long orderId) {
        OrderDO lockDO = orderService.lockOrder(orderId, null);
        if (lockDO == null) {
            return;
        }
        if (lockDO.getOrderStatus() != OrderStatusEnum.REFUND_ING) {
            return;
        }
        if (StringUtils.isNotEmpty(lockDO.getWxRefundId())) {
            return;
        }


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
            updateWrapper.set(OrderDO::getWxRefundId, lockDO.getWxRefundId());
            updateWrapper.eq(OrderDO::getId, lockDO.getId());
            updateWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.REFUND_ING);
            boolean update = orderService.update(updateWrapper);

            if (!update) {
                log.error("理论上退款提交成功，但数据库更新失败。 {}", JSONUtil.toJsonStr(lockDO));
            }

        } catch (WxPayException e) {
            log.error("补偿退款失败, 订单：{}", JSONUtil.toJsonStr(lockDO), e);
        }
    }
}
