package com.hzm.yuchao.biz.jobs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hzm.yuchao.biz.enums.MatchSaleStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSaleStatusEnum;
import com.hzm.yuchao.biz.enums.TicketSyncStatusEnum;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.model.TicketDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.biz.service.TicketService;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.lock.DatabaseDistributedLock;
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
public class TicketSyncJob {

    @Resource
    private TicketService ticketService;

    @Resource
    private DatabaseDistributedLock distributedLock;

    @Resource
    private ThreadService threadService;

    @Resource
    private MatchService matchService;

    // 每小时 10分运行
    @Scheduled(cron = "0 10 * * * ?")
    public void execute() {

        TraceUtils.initTrace();

        log.info("同步票务平台闸机补偿任务 重新进行补偿！");

        // 资源标识
        String resourceKey = "JOB_TICKET_SYNC";
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
        LambdaQueryWrapper<TicketDO> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.lt(TicketDO::getSaleTime, new Date());
        queryWrapper.eq(TicketDO::getSaleStatus, TicketSaleStatusEnum.SOLD);
        queryWrapper.eq(TicketDO::getSyncStatus, TicketSyncStatusEnum.NOT_SYNC);
        queryWrapper.last("limit 100");

        List<TicketDO> ticketDOList = ticketService.list(queryWrapper);

        if (CollectionUtils.isEmpty(ticketDOList)) {
            return;
        }

        log.info("同步票务平台闸机补偿任务！ size: {}", ticketDOList.size());
        for (TicketDO ticketDO : ticketDOList) {

            MatchDO matchDO = matchService.getById(ticketDO.getMatchId());

            // 已结束任务，直接停止推送
            if (matchDO.getSaleStatus() == MatchSaleStatusEnum.FINISHED) {

                log.info("赛程已结束 不再进行推送闸机！ id: {}", ticketDO.getId());

                LambdaUpdateWrapper<TicketDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(TicketDO::getSyncStatus, TicketSyncStatusEnum.SYNCED);
                updateWrapper.eq(TicketDO::getId, ticketDO.getId());
                ticketService.update(updateWrapper);

            } else {
                ticketService.pushPerson(matchDO, ticketDO.getId());
            }
        }
    }
}
