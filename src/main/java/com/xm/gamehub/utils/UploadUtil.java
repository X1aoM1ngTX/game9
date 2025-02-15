package com.xm.gamehub.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class UploadUtil {
    // 阿里云OSS相关配置
    @Value("${aliyun.oss.domain}")
    private String domain;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    private String accessKeyId;
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    private static String ALIYUN_OSS_DOMAIN;
    private static String ALIYUN_OSS_ENDPOINT;
    private static String ALIYUN_OSS_ACCESS_KEY_ID;
    private static String ALIYUN_OSS_ACCESS_KEY_SECRET;
    private static String ALIYUN_OSS_BUCKET_NAME;

    @PostConstruct
    public void init() {
        // 从环境变量中获取密钥
        accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");

        // 将注入的配置值赋给静态变量
        ALIYUN_OSS_DOMAIN = domain;
        ALIYUN_OSS_ENDPOINT = endpoint;
        ALIYUN_OSS_ACCESS_KEY_ID = accessKeyId;
        ALIYUN_OSS_ACCESS_KEY_SECRET = accessKeySecret;
        ALIYUN_OSS_BUCKET_NAME = bucketName;
    }

    public String uploadAliyunOss(MultipartFile file) throws IOException {
        // 获取文件名
        String originalFileName = file.getOriginalFilename();
        // 获取文件后缀
        String ext = "." + FilenameUtils.getExtension(originalFileName);
        // 生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "_") + ext;

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(ALIYUN_OSS_ENDPOINT, ALIYUN_OSS_ACCESS_KEY_ID, ALIYUN_OSS_ACCESS_KEY_SECRET);
        // 上传文件
        ossClient.putObject(
                ALIYUN_OSS_BUCKET_NAME,
                newFileName,
                file.getInputStream());
        ossClient.shutdown();
        // 返回文件访问路径
        return ALIYUN_OSS_DOMAIN + newFileName;
    }
}
