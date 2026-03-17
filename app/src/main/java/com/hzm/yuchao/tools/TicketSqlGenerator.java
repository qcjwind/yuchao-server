package com.hzm.yuchao.tools;

import com.hzm.yuchao.simple.BizException;
import com.hzm.yuchao.simple.utils.UuidUtils;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TicketSqlGenerator {

    // 座位范围实体类
    @Data
    @ToString
    public static class SeatRange {
        String area;       // 区域（固定为"A区"）
        String subArea;    // 子区域（A1-A6）
        int row;           // 排数
        int startNumber;   // 起始座位号
        int endNumber;     // 结束座位号

        public SeatRange(String area, String subArea, int row, int startNumber, int endNumber) {
            this.area = area;
            this.subArea = subArea;
            this.row = row;
            this.startNumber = startNumber;
            this.endNumber = endNumber;
        }
    }

    public static List<String> generateSingleArea(String[][] areaArray, String sqlTemplate, String valueTemplate) {

        List<SeatRange> seatRangeList = parseSeatArray(areaArray);

//        seatRangeList.forEach(t -> {
//            System.out.println(t);
//            System.out.println("---------");
//        });

        // 3. 生成SQL文件
        List<String> sqlList = generateInsertSql(seatRangeList, sqlTemplate, valueTemplate);
//        sqlList.forEach(t -> {
//            System.out.println(t);
//        });

        return sqlList;
    }

    public static List<String[][]> splitByArea(String[][] all) {
        List<String[][]> list = new ArrayList<>();

        // 无此票
        if (!ArrayPrinter.print2DArray(all).contains("区")) {
            return list;
        }

        for (int i = 0, j = -1; i < all.length; i++) {
            String[] line = all[i];

            if (line[0] != null && line[0].contains("区")) {
                if (j != -1) {
                    list.add(subArrayByRow(all, j, i));
                }
                j = i;
            }

            // 最后一个
            if (i == all.length - 1) {
                list.add(subArrayByRow(all, Math.max(j, 0), i));
            }
        }
        return list;
    }

    /**
     * 仅根据行范围截取二维数组的子数组（左闭右开区间 [rowStart, rowEnd)）
     * 保留原行的完整数据（不处理列）
     *
     * @param original 原二维数组
     * @param rowStart 起始行索引（包含）
     * @param rowEnd   结束行索引（不包含）
     * @return 截取后的子数组
     */
    private static String[][] subArrayByRow(String[][] original, int rowStart, int rowEnd) {
        // 校验原数组
        if (original == null) {
            throw new IllegalArgumentException("原数组不能为null");
        }
        // 校验行索引范围
        if (rowStart < 0 || rowEnd > original.length || rowStart > rowEnd) {
            throw new IndexOutOfBoundsException("行索引越界：rowStart=" + rowStart
                    + ", rowEnd=" + rowEnd + ", 总行数=" + original.length);
        }

        // 计算子数组行数
        int subRowCount = rowEnd - rowStart;
        String[][] subArray = new String[subRowCount][];

        // 复制指定范围的行（完整保留每行数据）
        for (int i = 0; i < subRowCount; i++) {
            int originalRowIndex = rowStart + i;
            // 复制整行（包括null行的情况）
            subArray[i] = original[originalRowIndex];
        }

        return subArray;
    }

    private static String[] getSubAreas(String[] input) {
        if (!Objects.equals(input[0], "排数")) {
            throw new BizException("数据错误: " + input[0]);
        }

        List<String> aa = new ArrayList<>();

        for (int i = 1; i < input.length; i++) {
            if (!input[i].isEmpty()) {
                aa.add(input[i]);
            }
        }

        return aa.toArray(new String[aa.size()]);
    }

    /**
     * 解析二维数组获取座位范围
     */
    private static List<SeatRange> parseSeatArray(String[][] array) {
        List<SeatRange> ranges = new ArrayList<>();
        if (array == null || array.length < 4) { // 至少需要4行（区域行+标题行+类型行+数据行）
            throw new IllegalArgumentException("数组格式错误，行数不足");
        }

        String area = array[0][0]; // 区域（来自Excel，保持完整文本）
        if (area == null) {
            area = "";
        }
        area = area.trim();
        String[] subAreas = getSubAreas(array[1]); // 子区域列表

        // 从第4行开始是数据行（索引3，对应9排、8排...）
        for (int rowIdx = 3; rowIdx < array.length; rowIdx++) {
            String[] rowData = array[rowIdx];
            if (rowData.length < subAreas.length * 2 + 1) {
                continue;
            }

            // 解析排数（如"9排" -> 9）
            int rowNumber = parseRow(rowData[0]);
            if (rowNumber <= 0) {
                continue;
            }

            // 解析每个子区域的起止号（A1-A6）
            // 列索引规则：A1起号=1, A1止号=2; A2起号=3, A2止号=4...以此类推
            for (int i = 0; i < subAreas.length; i++) {
                int startCol = 1 + i * 2; // 起号列索引
                int endCol = startCol + 1; // 止号列索引

                if (startCol >= rowData.length || endCol >= rowData.length) {
                    continue; // 列索引越界，跳过
                }

                // 解析座位号（去除前导零）
                int startNum = parseSeatNumber(rowData[startCol]);
                int endNum = parseSeatNumber(rowData[endCol]);

                if (startNum > 0 && endNum > 0 && startNum <= endNum) {
                    ranges.add(new SeatRange(area, subAreas[i], rowNumber, startNum, endNum));
                }
            }
        }

        return ranges;
    }

    /**
     * 解析排数字符串（如"9排" -> 9）
     */
    private static int parseRow(String rowStr) {
        if (rowStr == null || rowStr.trim().isEmpty()) {
            return 0;
        }

        if (!rowStr.contains("排")) {
            return 0;
        }

        Matcher matcher = Pattern.compile("(\\d+)").matcher(rowStr);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    /**
     * 解析座位号（如"01" -> 1）
     */
    private static int parseSeatNumber(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return 0;
        }
        String trimmed = numStr.trim();
        String normalized = trimmed.replaceAll("\\s+", "");

        // 兼容 Excel 中出现的表达式（如 "48/2*5+38/2*5+(5+8+10)"）
        if (normalized.matches(".*[+\\-*/()]+.*")) {
            return (int) evalIntExpression(normalized);
        }

        // 去除前导零后转换为整数
        String digits = normalized.replaceFirst("^0+", "");
        if (digits.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }

    /**
     * 仅支持整数四则运算与括号：+ - * / ( )
     * 用于解析座位号列中出现的计算表达式。
     */
    private static long evalIntExpression(String expr) {
        Deque<Long> values = new ArrayDeque<>();
        Deque<Character> ops = new ArrayDeque<>();

        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (c == '(') {
                ops.push(c);
                i++;
                continue;
            }

            if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    applyOp(values, ops.pop());
                }
                if (!ops.isEmpty() && ops.peek() == '(') {
                    ops.pop();
                }
                i++;
                continue;
            }

            if (isOp(c)) {
                // 一元负号：例如 "-3" 或 "(-3"
                if (c == '-' && (i == 0 || expr.charAt(i - 1) == '(' || isOp(expr.charAt(i - 1)))) {
                    int j = i + 1;
                    if (j < expr.length() && Character.isDigit(expr.charAt(j))) {
                        long num = 0;
                        while (j < expr.length() && Character.isDigit(expr.charAt(j))) {
                            num = num * 10 + (expr.charAt(j) - '0');
                            j++;
                        }
                        values.push(-num);
                        i = j;
                        continue;
                    }
                }

                while (!ops.isEmpty() && ops.peek() != '(' && precedence(ops.peek()) >= precedence(c)) {
                    applyOp(values, ops.pop());
                }
                ops.push(c);
                i++;
                continue;
            }

            if (Character.isDigit(c)) {
                long num = 0;
                while (i < expr.length() && Character.isDigit(expr.charAt(i))) {
                    num = num * 10 + (expr.charAt(i) - '0');
                    i++;
                }
                values.push(num);
                continue;
            }

            throw new IllegalArgumentException("非法表达式字符: " + c + ", expr=" + expr);
        }

        while (!ops.isEmpty()) {
            applyOp(values, ops.pop());
        }

        return values.isEmpty() ? 0 : values.pop();
    }

    private static boolean isOp(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static int precedence(char op) {
        if (op == '*' || op == '/') {
            return 2;
        }
        if (op == '+' || op == '-') {
            return 1;
        }
        return 0;
    }

    private static void applyOp(Deque<Long> values, char op) {
        if (values.size() < 2) {
            throw new IllegalArgumentException("表达式不完整，缺少操作数");
        }
        long b = values.pop();
        long a = values.pop();
        long r;
        switch (op) {
            case '+':
                r = a + b;
                break;
            case '-':
                r = a - b;
                break;
            case '*':
                r = a * b;
                break;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("除数为0");
                }
                r = a / b; // 整数除法
                break;
            default:
                throw new IllegalArgumentException("未知操作符: " + op);
        }
        values.push(r);
    }

    /**
     * 生成批量插入SQL
     */
    private static List<String> generateInsertSql(List<SeatRange> ranges, String sqlTemplate, String valueTemplate) {

        List<String> sqlList = new ArrayList<>();

        for (SeatRange range : ranges) {

            StringBuilder sb = new StringBuilder();
            sb.append(sqlTemplate);

            // 生成当前子区域当前排的所有座位号
            for (int num = range.startNumber; num <= range.endNumber; num++) {

                // 生成单条记录
                String record;

                try {
                    record = String.format(valueTemplate, UuidUtils.uuid32(), range.area, range.subArea, range.row, num);
                } catch (Exception e) {
                    System.out.println(valueTemplate);
                    throw e;
                }

                sb.append(record);
                sb.append(", ");
            }

            sb.replace(sb.length() - 2, sb.length(), ";");

            sqlList.add(sb.toString());
        }
        return sqlList;
    }
}
