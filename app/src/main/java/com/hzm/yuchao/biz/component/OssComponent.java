package com.hzm.yuchao.biz.component;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class OssComponent {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.accessKeyId}")
    private String accessKeyId;

    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到阿里云OSS
     *
     * @param localFilePath 本地文件路径
     * @param ossFilePath   OSS上保存的文件名
     * @return 上传后的文件URL
     */
    public String uploadToOSS(String localFilePath, String ossFilePath) {

        return uploadToOSS(new File(localFilePath), ossFilePath);
    }

    /**
     * 上传文件到阿里云OSS
     *
     * @param file 本地文件路径
     * @param ossFilePath   OSS上保存的文件名
     * @return 上传后的文件URL
     */
    public String uploadToOSS(File file, String ossFilePath) {
        // 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(
                endpoint,
                accessKeyId,
                accessKeySecret);

        try {
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, ossFilePath, file);
            ossClient.putObject(putObjectRequest);

            // 生成文件URL
            return "https://" + bucketName + "." + endpoint + "/" + ossFilePath;
        } finally {
            // 关闭OSS客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
