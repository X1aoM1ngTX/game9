package com.xm.game9.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class UploadUtil {
    private volatile static UploadUtil instance;
    private volatile static OSS ossClient;
    private volatile static S3Client r2Client;

    // Aliyun OSS Config
    private static String domain;
    private static String endpoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;

    // Cloudflare R2 Config
    private static String r2Domain;
    private static String r2Endpoint;
    private static String r2AccessKeyId;
    private static String r2AccessKeySecret;
    private static String r2BucketName;

    @Value("${aliyun.oss.domain}")
    private String domainTemp;
    @Value("${aliyun.oss.endpoint}")
    private String endpointTemp;
    @Value("${aliyun.oss.bucketName}")
    private String bucketNameTemp;

    @Value("${cloudflare.r2.domain}")
    private String r2DomainTemp;
    @Value("${cloudflare.r2.endpoint}")
    private String r2EndpointTemp;
    @Value("${cloudflare.r2.bucketName}")
    private String r2BucketNameTemp;

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

    private static S3Client getR2Client() {
        if (r2Client == null) {
            synchronized (UploadUtil.class) {
                if (r2Client == null) {
                    // 添加调试日志
                    log.info("创建R2客户端，访问密钥ID: {}", r2AccessKeyId != null ? "已设置" : "未设置");
                    log.info("创建R2客户端，访问密钥密码: {}", r2AccessKeySecret != null ? "已设置" : "未设置");

                    if (r2AccessKeyId == null || r2AccessKeyId.isEmpty()) {
                        log.error("Cloudflare R2访问密钥ID为空，无法创建R2客户端");
                        throw new IllegalStateException("Cloudflare R2访问密钥ID为空，无法创建R2客户端");
                    }

                    if (r2AccessKeySecret == null || r2AccessKeySecret.isEmpty()) {
                        log.error("Cloudflare R2访问密钥密码为空，无法创建R2客户端");
                        throw new IllegalStateException("Cloudflare R2访问密钥密码为空，无法创建R2客户端");
                    }

                    AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(r2AccessKeyId, r2AccessKeySecret);
                    r2Client = S3Client.builder()
                            .endpointOverride(java.net.URI.create(r2Endpoint))
                            .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                            .region(Region.US_EAST_1) // R2使用任意区域，这里使用默认值
                            .build();
                }
            }
        }
        return r2Client;
    }

    // 在应用关闭时关闭客户端
    public static void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            ossClient = null;
        }
        if (r2Client != null) {
            r2Client.close();
            r2Client = null;
        }
    }

    @PostConstruct
    public void init() {
        // 从环境变量中获取阿里云OSS密钥
        accessKeyId = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID");
        accessKeySecret = System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET");

        // 从环境变量中获取Cloudflare R2密钥
        r2AccessKeyId = System.getenv("CLOUDFLARE_R2_ACCESS_KEY_ID");
        r2AccessKeySecret = System.getenv("CLOUDFLARE_R2_ACCESS_KEY_SECRET");

        // 添加调试日志
        log.info("阿里云OSS访问密钥ID: {}", accessKeyId != null ? "已设置" : "未设置");
        log.info("阿里云OSS访问密钥密码: {}", accessKeySecret != null ? "已设置" : "未设置");
        log.info("Cloudflare R2访问密钥ID: {}", r2AccessKeyId != null ? "已设置" : "未设置");
        log.info("Cloudflare R2访问密钥密码: {}", r2AccessKeySecret != null ? "已设置" : "未设置");

        // 将注入的配置值赋给静态变量
        domain = domainTemp;
        endpoint = endpointTemp;
        bucketName = bucketNameTemp;

        r2Domain = r2DomainTemp;
        r2Endpoint = r2EndpointTemp;
        r2BucketName = r2BucketNameTemp;

        // 初始化单例实例
        instance = this;

        log.info("UploadUtil 初始化完成，阿里云OSS Endpoint: {}, Cloudflare R2 Endpoint: {}", endpoint, r2Endpoint);
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

        // 3. 校验文件类型
        String contentType = file.getContentType();
        String extension = FilenameUtils.getExtension(originalFileName);

        // 检查 MIME 类型和文件扩展名
        boolean isValidMimeType = contentType != null && contentType.startsWith("image/");
        boolean isValidExtension = extension != null && extension.matches("(?i)^(jpg|jpeg|png|gif|webp)$");

        if (!isValidMimeType && !isValidExtension) {
            throw new IllegalArgumentException("文件类型必须是图片");
        }

        try {
            // 获取OSS客户端实例
            OSS oss = getOssClient();
            // 设置 Content-Type
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            // 上传文件
            oss.putObject(bucketName, newFileName, file.getInputStream(), metadata);
            // 返回文件访问路径
            return domain + newFileName;
        } catch (IOException e) {
            log.error("阿里云OSS文件上传失败", e);
            throw e;
        }
    }

    public String uploadR2(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 添加调试日志
        log.info("开始上传文件到Cloudflare R2，访问密钥ID: {}", r2AccessKeyId != null ? "已设置" : "未设置");
        log.info("开始上传文件到Cloudflare R2，访问密钥密码: {}", r2AccessKeySecret != null ? "已设置" : "未设置");

        if (r2AccessKeyId == null || r2AccessKeyId.isEmpty()) {
            log.error("Cloudflare R2访问密钥ID为空，无法上传文件");
            throw new IllegalStateException("Cloudflare R2访问密钥ID为空，无法上传文件");
        }

        if (r2AccessKeySecret == null || r2AccessKeySecret.isEmpty()) {
            log.error("Cloudflare R2访问密钥密码为空，无法上传文件");
            throw new IllegalStateException("Cloudflare R2访问密钥密码为空，无法上传文件");
        }

        // 获取文件名
        String originalFileName = file.getOriginalFilename();
        // 获取文件后缀
        String ext = "." + FilenameUtils.getExtension(originalFileName);
        // 生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "_") + ext;

        // 校验文件类型
        String contentType = file.getContentType();
        String extension = FilenameUtils.getExtension(originalFileName);

        // 检查 MIME 类型和文件扩展名
        boolean isValidMimeType = contentType != null && contentType.startsWith("image/");
        boolean isValidExtension = extension != null && extension.matches("(?i)^(jpg|jpeg|png|gif|webp)$");

        if (!isValidMimeType && !isValidExtension) {
            throw new IllegalArgumentException("文件类型必须是图片");
        }

        try {
            // 获取R2客户端实例
            S3Client r2 = getR2Client();

            // 创建上传请求
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(newFileName)
                    .contentType(contentType)
                    .build();

            // 上传文件
            r2.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 返回文件访问路径
            return r2Domain + newFileName;
        } catch (IOException e) {
            log.error("Cloudflare R2文件上传失败", e);
            throw e;
        }
    }
}
