package com.hzm.yuchao.biz.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

// 阶梯退款配置类
@Data
public class RefundRuleDTO {
    // 退费截止时间
    private Date endTime;
    // 顺序根据 beforeEndHour 由大到小排序，理论上此时 refundRate 也是由大到小。
    private List<LadderRule> ruleList;

    // 阶梯规则类
    @Data
    public static class LadderRule {
        // 距离截止时间的最小小时数（含）。
        private Integer beforeEndHour;
        // 退款费率（百分比）
        private int refundRate;
        private String ruleDesc;
    }
}
