package com.hzm.yuchao.biz.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Security;
import java.util.Map;

/**
 * 微信手机号解密工具类
 */
@Slf4j
public class WechatPhoneDecryptUtil {

    /**
     * 解密微信加密的手机号数据
     * @param encryptedData 加密的用户数据（小程序端getPhoneNumber返回）
     * @param sessionKey 登录时获取的session_key
     * @param iv 加密算法的初始向量（小程序端getPhoneNumber返回）
     * @return 解密后的手机号信息（包含phoneNumber等字段）
     * @throws Exception 解密过程中的异常
     */
    public static String decryptPhoneNumber(String encryptedData, String sessionKey, String iv) throws Exception {
        // 1. 初始化加密算法提供者
        Security.addProvider(new BouncyCastleProvider());

        // 2. 解码数据（Base64）
        byte[] sessionKeyBytes = Base64.decode(sessionKey);
        byte[] encryptedDataBytes = Base64.decode(encryptedData);
        byte[] ivBytes = Base64.decode(iv);

        // 3. 处理sessionKey长度异常（防止无效数据）
        if (sessionKeyBytes.length != 16) {
            throw new IllegalArgumentException("sessionKey长度不正确");
        }

        // 4. 初始化加密算法（AES/CBC/PKCS7Padding）
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(ivBytes));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, params);

        // 5. 执行解密
        byte[] decrypted = cipher.doFinal(encryptedDataBytes);
        String decryptedStr = new String(decrypted, "UTF-8");

        log.info("手机号解密：{}", decryptedStr);

        // 6. 解析JSON（使用fastjson示例）
        return (String) JSONObject.parseObject(decryptedStr, Map.class).get("phoneNumber");
    }

    // 测试方法
    public static void main(String[] args) {
        try {
            // 以下数据需替换为实际从小程序端获取的值
            String encryptedData = "小程序端返回的encryptedData";
            String sessionKey = "用户登录时获取的session_key";
            String iv = "小程序端返回的iv";

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}