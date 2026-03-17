package com.hzm.yuchao.biz.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class AmountConverter {

    // 初始化DecimalFormat，模式"0.##"表示：
    // - 至少1位整数（0-9）
    // - 最多2位小数，末尾的0会自动省略
    private static final DecimalFormat FORMATTER = new DecimalFormat("0.##");

    /**
     * 将分（int）转换为元（String），自动省略末尾的0
     * @param centAmount 以分为单位的金额（可null）
     * @return 格式化后的元金额字符串
     */
    public static String toYuan(Integer centAmount) {
        if (centAmount == null) {
            return "0";
        }
        return convert(centAmount);
    }

    /**
     * 重载：处理long类型分金额（支持大额）
     */
    public static String toYuan(long centAmount) {
        return convert(centAmount);
    }

    /**
     * 核心转换逻辑
     */
    private static String convert(long centAmount) {
        // 分转元：除以100，使用BigDecimal保证精度
        BigDecimal yuan = new BigDecimal(centAmount)
                .divide(new BigDecimal(100));
        
        // 使用DecimalFormat自动处理格式：
        // - 100分 → 1.00 → 格式化为"1"
        // - 120分 → 1.20 → 格式化为"1.2"
        // - 123分 → 1.23 → 格式化为"1.23"
        return FORMATTER.format(yuan);
    }
}
