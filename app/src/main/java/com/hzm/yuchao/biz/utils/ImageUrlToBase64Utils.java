package com.hzm.yuchao.biz.utils;

import com.hzm.yuchao.simple.BizException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class ImageUrlToBase64Utils {

    /**
     * 将图片URL转换为Base64编码字符串
     * @param imageUrl 图片的网络URL（如"https://example.com/image.jpg"）
     * @return Base64编码字符串
     * @throws IOException 网络连接失败、读取超时或图片无效时抛出
     */
    public static String convert(String imageUrl) {

        try {
            return core(imageUrl);
        } catch (IOException e) {
            throw new BizException(e.getMessage());
        }
    }

    private static String core(String imageUrl) throws IOException {
        // 1. 验证URL非空
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new BizException("图片URL不能为空");
        }

        // 2. 建立网络连接
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 设置连接超时（5秒）和读取超时（10秒）
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);

        // 3. 检查连接响应（200表示成功）
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new BizException("图片URL请求失败，响应码：" + responseCode);
        }

        // 4. 读取图片字节流
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = outputStream.toByteArray();

            // 5. Base64编码
            String base64Encoded = Base64.getEncoder().encodeToString(imageBytes);

            // 6. 可选：添加数据协议头（根据图片MIME类型）
            String mimeType = connection.getContentType(); // 如"image/jpeg"、"image/png"
            if (mimeType == null || !mimeType.startsWith("image/")) {
                throw new BizException("URL指向的不是有效的图片资源");
            }
            return "data:" + mimeType + ";base64," + base64Encoded;
        } finally {
            connection.disconnect(); // 释放连接资源
        }
    }

    // 测试示例
    public static void main(String[] args) {
        String imageUrl = "https://img01.51jobcdn.com/im/images/2023/logo.png"; // 示例图片URL
        String base64WithHeader = convert(imageUrl);
        System.out.println("带协议头的Base64（前50字符）：" + base64WithHeader.substring(0, 50) + "...");

    }
}
