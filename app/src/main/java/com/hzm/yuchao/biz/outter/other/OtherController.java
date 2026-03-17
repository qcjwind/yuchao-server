package com.hzm.yuchao.biz.outter.other;

import com.hzm.yuchao.biz.jobs.TicketSyncJob;
import com.hzm.yuchao.biz.mapper.SkuMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Binary Wang
 */
@Slf4j
@Api("系统内部调用api")
@RestController
@RequestMapping("/other")
public class OtherController {

    @Resource
    private TicketSyncJob ticketSyncJob;

    @Resource
    private SkuMapper skuMapper;

    @ApiOperation(value = "主动重试购票推送")
    @RequestMapping("/ticketSyncJob")
    public String ticketSync() {

        ticketSyncJob.execute();

        return "success";
    }

    /**
     * https://yuchao2025.zszlchina.com/other/updateStock?skuId=36
     * @param skuId
     * @return
     */
    @ApiOperation(value = "更新库存")
    @RequestMapping("/updateStock")
    public String updateSkuTotalAndStock(long skuId) {

        skuMapper.updateSkuTotalAndStock(skuId);

        return "success";
    }
}

