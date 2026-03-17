package com.hzm.yuchao.biz.jobs;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzm.yuchao.biz.enums.OrderStatusEnum;
import com.hzm.yuchao.biz.model.OrderDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.OrderService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.simple.lock.DatabaseDistributedLock;
import com.hzm.yuchao.simple.utils.JsonUtil;
import com.hzm.yuchao.simple.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 同步至票务平台失败的票据 重新进行补偿
 */
@Slf4j
@Component
public class OrderCloseJob {

    @Resource
    private TicketService ticketService;

    @Resource
    private DatabaseDistributedLock distributedLock;

    @Resource
    private OrderService orderService;

    @Resource
    private MatchService matchService;

    // 每分钟运行一次
    @Scheduled(cron = "0 * * * * ?")
    public void execute() {

        TraceUtils.initTrace();

        // 资源标识
        String resourceKey = "JOB_ORDER_CLOSE";
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

        // 超时15分钟
        queryWrapper.lt(OrderDO::getOrderTime, DateUtil.offset(new Date(), DateField.MINUTE, -15));
        queryWrapper.eq(OrderDO::getOrderStatus, OrderStatusEnum.WAIT_PAY);
        queryWrapper.last("limit 100");

        List<OrderDO> orderDOList = orderService.list(queryWrapper);

        if (CollectionUtils.isEmpty(orderDOList)) {
            return;
        }

        log.info("超时关单定时任务！ size: {}", orderDOList.size());
        for (OrderDO orderDO : orderDOList) {

            try {
                log.info("开始超时关单。 {}", JsonUtil.toJsonStr(orderDO));
                orderService.cancel(orderDO.getId(), false);
            } catch (Exception e) {
                log.info("超时关单失败。 {}", JsonUtil.toJsonStr(orderDO), e);
            }
        }
    }
}
