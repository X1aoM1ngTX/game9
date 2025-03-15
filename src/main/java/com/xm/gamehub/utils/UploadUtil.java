package com.xm.gamehub.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class UploadUtil {
    private volatile static UploadUtil instance;
    private volatile static OSS ossClient;

    // 阿里云OSS相关配置
    private static String domain;
    private static String endpoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;

    @Value("${aliyun.oss.domain}")
    private String domainTemp;

    @Value("${aliyun.oss.endpoint}")
    private String endpointTemp;

    @Value("${aliyun.oss.bucketName}")
    private String bucketNameTemp;

    private UploadUtil() {
        // 私有构造函数
    }

    public static UploadUtil getInstance() {
        if (instance == null) {
            synchronized (UploadUtil.class) {
                if (instance == null) {
                    instance = new UploadUtil();
                }
            }
        }
        return instance;
    }

    private static OSS getOssClient() {
        if (ossClient == null) {
            synchronized (UploadUtil.class) {
                if (ossClient == null) {
                    ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                }
            }
        }
        return ossClient;
    }

    // 在应用关闭时关闭OSS客户端
    public static void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            ossClient = null;
        }
    }

    @PostConstruct
    public void init() {
        // 从环境变量中获取密钥
        accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");

        // 将注入的配置值赋给静态变量
        domain = domainTemp;
        endpoint = endpointTemp;
        bucketName = bucketNameTemp;

        // 初始化单例实例
        instance = this;

        log.info("UploadUtil 初始化完成，Endpoint: {}", endpoint);
    }

    public String uploadAliyunOss(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取文件名
        String originalFileName = file.getOriginalFilename();
        // 获取文件后缀
        String ext = "." + FilenameUtils.getExtension(originalFileName);
        // 生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "_") + ext;

        try {
            // 获取OSS客户端实例
            OSS oss = getOssClient();
            // 上传文件
            oss.putObject(bucketName, newFileName, file.getInputStream());
            // 返回文件访问路径
            return domain + newFileName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw e;
        }
    }
}
