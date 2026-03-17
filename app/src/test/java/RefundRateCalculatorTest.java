//import com.hzm.yuchao.biz.dto.RefundRuleDTO;
//import com.hzm.yuchao.biz.utils.RefundRateCalculator;
//import com.hzm.yuchao.simple.BizException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//public class RefundRateCalculatorTest {
//
//    // 测试用的阶梯配置
//    private RefundRuleDTO validConfig;
//
//    @BeforeEach
//    void setUp() {
//        // 初始化一个有效的阶梯退款配置（对应之前的JSON规则）
//        validConfig = new RefundRuleDTO();
//
//        List<RefundRuleDTO.LadderRule> rules = new ArrayList<>();
//
//        // 规则1：截止前30天以上（≥720小时）
//        RefundRuleDTO.LadderRule rule1 = new RefundRuleDTO.LadderRule();
//        rule1.setBeforeEndHour(720);
//        rule1.setRuleDesc("截止前30天以上");
//        rule1.setRefundRate(100);
//        rules.add(rule1);
//
//        // 规则2：截止前7-30天（168-720小时）
//        RefundRuleDTO.LadderRule rule2 = new RefundRuleDTO.LadderRule();
//        rule2.setBeforeEndHour(168);
//        rule2.setRuleDesc("截止前7-30天");
//        rule2.setRefundRate(80);
//        rules.add(rule2);
//
//        // 规则3：截止前24小时-7天（24-168小时）
//        RefundRuleDTO.LadderRule rule3 = new RefundRuleDTO.LadderRule();
//        rule3.setBeforeEndHour(24);
//        rule3.setRuleDesc("截止前24小时-7天");
//        rule3.setRefundRate(50);
//        rules.add(rule3);
//
//        // 规则4：截止前24小时内（0-24小时）
//        RefundRuleDTO.LadderRule rule4 = new RefundRuleDTO.LadderRule();
//        rule4.setBeforeEndHour(0);
//        rule4.setRuleDesc("截止前24小时内");
//        rule4.setRefundRate(30);
//        rules.add(rule4);
//
//        // 规则5：已过截止时间（≤0小时）
////        RefundLadderConfig.LadderRule rule5 = new RefundLadderConfig.LadderRule();
////        rule5.setMinBeforeEndHour(null);
////        rule5.setMaxBeforeEndHour(0);
////        rule5.setTimeDesc("已过截止时间");
////        rule5.setRefundRate(0);
////        rules.add(rule5);
//
//        validConfig.setRuleList(rules);
//    }
//
//
//    /**
//     * 测试截止前30天以上的场景（费率100%）
//     */
//    @Test
//    void testMoreThan30DaysBeforeEnd() {
//        // 当前时间 = 截止时间 - 35天（840小时）
//        Date endTime = getFutureDate(35 * 24);
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(100, rate);
//    }
//
//    /**
//     * 测试截止前10天的场景（费率80%）
//     */
//    @Test
//    void test10DaysBeforeEnd() {
//        // 当前时间 = 截止时间 - 10天（240小时）
//        Date endTime = getFutureDate(10 * 24);
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(80, rate);
//    }
//
//    /**
//     * 测试截止前3天的场景（费率50%）
//     */
//    @Test
//    void test3DaysBeforeEnd() {
//        // 当前时间 = 截止时间 - 3天（72小时）
//        Date endTime = getFutureDate(3 * 24);
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(50, rate);
//    }
//
//    /**
//     * 测试截止前12小时的场景（费率30%）
//     */
//    @Test
//    void test12HoursBeforeEnd() {
//        // 当前时间 = 截止时间 - 12小时
//        Date endTime = getFutureDate(12);
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(30, rate);
//    }
//
//    /**
//     * 测试已过截止时间的场景（费率0%）
//     */
//    @Test
//    void testAfterEndTime() {
//        // 当前时间 = 截止时间 + 1小时（已过期）
//        Date endTime = getPastDate(1);
//        validConfig.setEndTime(endTime);
//
//
//        assertThrows(BizException.class, () -> {
//            RefundRateCalculator.calculateRefundRate(validConfig);
//        });
//    }
//
//    /**
//     * 测试边界值：刚好30天（720小时）
//     */
//    @Test
//    void testBoundary30Days() {
//        Date endTime = getFutureDate(720); // 刚好30天
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(80, rate); // 属于7-30天区间
//    }
//
//    /**
//     * 测试边界值：刚好7天（168小时）
//     */
//    @Test
//    void testBoundary7Days() {
//        Date endTime = getFutureDate(168); // 刚好7天
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(50, rate); // 属于24小时-7天区间
//    }
//
//    /**
//     * 测试边界值：刚好24小时
//     */
//    @Test
//    void testBoundary24Hours() {
//        Date endTime = getFutureDate(24); // 刚好24小时
//        validConfig.setEndTime(endTime);
//
//        int rate = RefundRateCalculator.calculateRefundRate(validConfig);
//        assertEquals(50, rate); // 属于24小时-7天区间
//    }
//
//    /**
//     * 辅助方法：获取未来指定小时数的日期
//     */
//    private Date getFutureDate(int hours) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, hours);
//        return calendar.getTime();
//    }
//
//    /**
//     * 辅助方法：获取过去指定小时数的日期
//     */
//    private Date getPastDate(int hours) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY, -hours);
//        return calendar.getTime();
//    }
//}
