package com.hzm.yuchao.biz.utils;

import com.alibaba.fastjson.JSONObject;
import com.hzm.yuchao.biz.dto.RefundRuleDTO;
import com.hzm.yuchao.simple.BizException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


// 退款费率计算工具类
@Slf4j
public class RefundRateCalculator {

    public static int calculateRefundRate(String refundRule) {
        return calculateRefundRate(JSONObject.parseObject(refundRule, RefundRuleDTO.class));
    }

    /**
     * 根据当前时间（Date）和截止时间（Date）计算退款费率
     *
     * @param ruleDTO 阶梯退款配置
     * @return 对应的退款费率（百分比），返回-1表示配置无效
     */
    public static int calculateRefundRate(RefundRuleDTO ruleDTO) {

        // 验证配置是否有效
        if (ruleDTO == null || ruleDTO.getRuleList() == null || ruleDTO.getRuleList().isEmpty()) {
            throw new BizException("退款配置无效");
        }

        if (System.currentTimeMillis() >= ruleDTO.getEndTime().getTime()) {
            throw new BizException("超过截止时间，无法退款");
        }

        Date currentTime = new Date();
        // 计算当前时间距离截止时间的小时数
        long hoursBeforeEnd = calculateHoursBeforeEnd(currentTime, ruleDTO.getEndTime());

        log.info("当前退款计算的小时：{}", hoursBeforeEnd);

        // 从小到大排序
        Collections.sort(ruleDTO.getRuleList(), Comparator.comparingInt(RefundRuleDTO.LadderRule::getRefundRate));

        // 遍历阶梯规则，找到匹配的费率
        for (RefundRuleDTO.LadderRule rule : ruleDTO.getRuleList()) {
            if (hoursBeforeEnd < rule.getBeforeEndHour()) {
                return rule.getRefundRate();
            }
        }

        // 未找到匹配的规则
//        throw new BizException("未匹配退款规则，无法退款");
        // 没匹配到全额退款
        return 100;
    }

    /**
     * 计算两个Date之间的小时差（基于UTC时间）
     *
     * @param currentTime 当前时间
     * @param endTime     截止时间
     * @return 小时数，正数表示截止前，负数表示已截止
     */
    private static long calculateHoursBeforeEnd(Date currentTime, Date endTime) {
        // 将Date转换为Instant（UTC时间戳），再计算时间差
        Instant currentInstant = currentTime.toInstant();
        Instant endInstant = endTime.toInstant();

        // 计算两个时间点的差值（单位：小时）
        Duration duration = Duration.between(currentInstant, endInstant);
        return duration.toHours();
    }
}
