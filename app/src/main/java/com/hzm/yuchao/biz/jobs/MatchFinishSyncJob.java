package com.hzm.yuchao.biz.jobs;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hzm.yuchao.biz.enums.MatchSaleStatusEnum;
import com.hzm.yuchao.biz.model.MatchDO;
import com.hzm.yuchao.biz.service.MatchService;
import com.hzm.yuchao.simple.ThreadService;
import com.hzm.yuchao.simple.lock.DatabaseDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 赛程结束任务
 */
@Slf4j
@Component
public class MatchFinishSyncJob {

    @Resource
    private MatchService matchService;

    @Resource
    private DatabaseDistributedLock distributedLock;

    @Resource
    private ThreadService threadService;

    // 每 10分钟 运行一次
    @Scheduled(cron = "0 */10 * * * ?")
    public void execute() {

        log.info("赛程结束任务！");

        LambdaUpdateWrapper<MatchDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(MatchDO::getSaleStatus, MatchSaleStatusEnum.FINISHED);
        updateWrapper.eq(MatchDO::getSaleStatus, MatchSaleStatusEnum.NOT_FINISH);
        updateWrapper.lt(MatchDO::getEndTime, new Date());

        matchService.update(updateWrapper);

    }
}
