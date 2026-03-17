package com.hzm.yuchao.simple.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据脱敏工具类
 * 支持姓名（中文/英文）、手机号、身份证号（含护照、港澳台证件）的脱敏处理
 */
public class DataMaskingUtil {

    // 中文姓名正则（包含常见复姓）
    private static final Pattern CHINESE_NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5]{2,20}$|^[\\u4e00-\\u9fa5]{1,2}[·\\u00b7][\\u4e00-\\u9fa5]{1,20}$");
    // 英文姓名正则（支持空格、连字符）
    private static final Pattern ENGLISH_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s-]{2,50}$");
    // 大陆手机号正则
    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    // 国际手机号正则（简化版）
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    // 大陆身份证号正则（18位）
    private static final Pattern MAINLAND_ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");
    // 护照号正则（以G、E、P、S、W开头，后跟8位数字）
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("^[GEPSW]\\d{8}$");
    // 港澳居民来往内地通行证（9位）
    private static final Pattern HK_MO_PERMIT_PATTERN = Pattern.compile("^[HM]\\d{8}$");
    // 台湾居民来往大陆通行证（10位，含英文字母）
    private static final Pattern TW_PERMIT_PATTERN = Pattern.compile("^\\d{8}|[A-Z]\\d{9}$");

    /**
     * 姓名脱敏
     * 中文：保留姓氏，名字用*代替（如：张**、欧阳**、李·**）
     * 英文：保留首字母，其余用*代替（如：J*** D***、A***-S***）
     * 其他：保留前1位，其余用*代替
     */
    /**
     * 姓名脱敏（新规则）
     * 展示第一个和最后一个字符，中间所有字符用*代替
     * 例：*三 → *三（长度2，无中间字符）
     * 例：张三丰 → 张*丰
     * 例：John → J*n
     * 例：Anna-Smith → A*****h
     * 例：欧阳锋 → 欧*锋
     * 例：李 → 李（长度1，不脱敏）
     */
    public static String maskName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        name = name.trim();
        
        // 处理中文姓名
        if (CHINESE_NAME_PATTERN.matcher(name).matches()) {
            // 处理带点的姓名（如：司马·相如）
            if (name.contains("·") || name.contains("•")) {
                String[] parts = name.split("[·•]");
                if (parts.length == 2) {
                    return parts[0] + "·" + createAsterisks(parts[1].length());
                }
            }

            int length = name.length();

            // 长度为1：不脱敏
            if (length == 1) {
                return name;
            }
            // 长度为2：脱敏姓
            if (length == 2) {
                return "*" + name.charAt(length - 1);
            }
            // 长度>2：首尾字符 + 中间*
            char first = name.charAt(0);
            char last = name.charAt(length - 1);
            return first + createAsterisks(length - 2) + last;
        }
        
        // 处理英文姓名
        if (ENGLISH_NAME_PATTERN.matcher(name).matches()) {
            StringBuilder masked = new StringBuilder();
            String[] parts = name.split(" ");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    masked.append(part.charAt(0));
                    if (part.contains("-")) {
                        String[] subParts = part.split("-");
                        for (int i = 1; i < subParts.length; i++) {
                            masked.append("-").append(subParts[i].charAt(0))
                                  .append(createAsterisks(subParts[i].length() - 1));
                        }
                    } else {
                        masked.append(createAsterisks(part.length() - 1));
                    }
                    masked.append(" ");
                }
            }
            return masked.toString().trim();
        }
        
        // 其他类型姓名（保留首字符，其余用*代替）
        return name.charAt(0) + createAsterisks(name.length() - 1);
    }

    /**
     * 手机号脱敏
     * 大陆手机号：保留前3位和后4位（如：138****5678）
     * 国际手机号：保留前3位和后4位，中间用*代替
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        phone = phone.trim();
        
        // 处理大陆手机号
        if (MAINLAND_PHONE_PATTERN.matcher(phone).matches()) {
            return phone.substring(0, 3) + "****" + phone.substring(9);
        }
        
        // 处理国际手机号
        if (INTERNATIONAL_PHONE_PATTERN.matcher(phone).matches()) {
            if (phone.length() <= 7) {
                return phone; // 过短的国际号码不脱敏
            }
            return phone.substring(0, 3) + createAsterisks(phone.length() - 7) + 
                   phone.substring(phone.length() - 4);
        }
        
        // 其他格式手机号（保留前3位和后4位，中间用*代替）
        if (phone.length() <= 7) {
            return phone;
        }
        return phone.substring(0, 3) + createAsterisks(phone.length() - 7) + 
               phone.substring(phone.length() - 4);
    }

    /**
     * 身份证号脱敏
     * 大陆身份证：保留前6位和后4位（如：110101********1234）
     * 护照：保留前2位和后2位（如：E1******23）
     * 港澳通行证：保留前2位和后2位（如：H1******23）
     * 台湾通行证：保留前2位和后2位（如：12******34）
     * 其他证件：保留前2位和后2位，中间用*代替
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return "";
        }
        idCard = idCard.trim();
        
        // 处理大陆身份证
        if (MAINLAND_ID_CARD_PATTERN.matcher(idCard).matches()) {
            return idCard.substring(0, 1) + "********" + idCard.substring(17);
        }
        
        // 处理护照
        if (PASSPORT_PATTERN.matcher(idCard).matches()) {
            return idCard.substring(0, 2) + "******" + idCard.substring(6);
        }
        
        // 处理港澳通行证
        if (HK_MO_PERMIT_PATTERN.matcher(idCard).matches()) {
            return idCard.substring(0, 2) + "******" + idCard.substring(6);
        }
        
        // 处理台湾通行证
        if (TW_PERMIT_PATTERN.matcher(idCard).matches()) {
            if (idCard.length() == 8) {
                return idCard.substring(0, 2) + "****" + idCard.substring(6);
            } else {
                return idCard.substring(0, 2) + "*******" + idCard.substring(9);
            }
        }
        
        // 其他证件（保留前2位和后2位，中间用*代替）
        if (idCard.length() <= 4) {
            return idCard; // 过短的证件号不脱敏
        }
        return idCard.substring(0, 2) + createAsterisks(idCard.length() - 4) + 
               idCard.substring(idCard.length() - 2);
    }

    /**
     * 创建指定长度的星号字符串
     */
    private static String createAsterisks(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }

    // 测试方法
    public static void main(String[] args) {
        // 测试姓名脱敏
        System.out.println("=== 姓名脱敏测试 ===");
        System.out.println(maskName("张三"));               // 张*
        System.out.println(maskName("张三丰"));             // 张**
        System.out.println(maskName("欧阳锋"));             // 欧**
        System.out.println(maskName("司马·相如"));          // 司·**
        System.out.println(maskName("John Doe"));           // J*** D**
        System.out.println(maskName("Anna-Smith"));         // A***-S****
        
        // 测试手机号脱敏
        System.out.println("\n=== 手机号脱敏测试 ===");
        System.out.println(maskPhone("13812345678"));       // 138****5678
        System.out.println(maskPhone("+12125551234"));      // +12****51234
        System.out.println(maskPhone("+85212345678"));      // +85*******678
        
        // 测试身份证号脱敏
        System.out.println("\n=== 身份证号脱敏测试 ===");
        System.out.println(maskIdCard("110101199001011234")); // 110101********1234
        System.out.println(maskIdCard("E12345678"));          // E1******78
        System.out.println(maskIdCard("H12345678"));          // H1******78
        System.out.println(maskIdCard("12345678"));           // 12****78
        System.out.println(maskIdCard("A123456789"));         // A1*******89
    }
}
    