package com.hzm.yuchao.tools;

public class ArrayPrinter {

    /**
     * 打印二维数组，输出格式可直接作为Java变量声明
     * @param array 要打印的二维数组
     */
    public static String print2DArray(String[][] array) {
        if (array == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n"); // 外层数组开始

        for (int i = 0; i < array.length; i++) {
            String[] row = array[i];
            sb.append("    {"); // 内层数组开始（带缩进）

            if (row != null) {
                for (int j = 0; j < row.length; j++) {
                    // 拼接元素（添加双引号）
                    sb.append("\"").append(escapeDoubleQuotes(row[j])).append("\"");
                    // 最后一个元素不加逗号
                    if (j != row.length - 1) {
                        sb.append(", ");
                    }
                }
            }

            sb.append("}");
            // 最后一行不加逗号
            if (i != array.length - 1) {
                sb.append(",");
            }
            sb.append("\n"); // 换行
        }

        sb.append("}"); // 外层数组结束

        return sb.toString();
    }

    /**
     * 转义字符串中的双引号，避免破坏格式
     */
    private static String escapeDoubleQuotes(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\"", "\\\"");
    }

    // 测试示例
    public static void main(String[] args) {
        // 测试数组（用户提供的示例）
        String[][] testArray = {
            {"A区", "", "", "", "", "", "", "", "", "", "", "", "", ""},
            {"排数", "A1", "", "A2", "", "A3", "", "A4", "", "A5", "", "A6", "", ""},
            {"", "起号", "止号", "起号", "止号", "起号", "止号", "起号", "止号", "起号", "止号", "起号", "止号", ""},
            {"9排", "01", "16", "01", "20", "01", "28", "01", "27", "01", "25", "01", "11", ""},
            {"8排", "01", "15", "01", "20", "01", "28", "01", "27", "01", "25", "01", "11", ""}
        };

        // 打印数组
        print2DArray(testArray);
    }
}
