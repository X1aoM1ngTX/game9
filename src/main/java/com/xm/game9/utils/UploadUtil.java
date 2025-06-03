package com.xm.game9.utils;

import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class UploadUtil {
    // @Value("${aliyun.oss.domain}")
    // private String aliyunOssDomain;

    // @Value("${aliyun.oss.endpoint}")
    // private String aliyunOssEndpoint;

    // @Value("${aliyun.oss.bucketName}")
    // private String aliyunOssBucketName;

    @Value("${cloudflare.r2.domain}")
    private String cloudflareR2Domain;

    @Value("${cloudflare.r2.endpoint}")
    private String cloudflareR2Endpoint;

    @Value("${cloudflare.r2.bucketName}")
    private String cloudflareR2BucketName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private String r2AccessKeyId;
    private String r2SecretAccessKey;

    private S3Client r2Client;

    @PostConstruct
    public void init() {
        log.info("UploadUtil 初始化开始，当前环境: {}", activeProfile);

        // 直接从系统环境变量获取
        r2AccessKeyId = System.getenv("CLOUDFLARE_R2_ACCESS_KEY_ID");
        r2SecretAccessKey = System.getenv("CLOUDFLARE_R2_ACCESS_KEY_SECRET");

        log.info("从系统环境变量获取R2 Access Key ID: {}", r2AccessKeyId != null ? "已设置" : "未设置");
        log.info("从系统环境变量获取R2 Secret Access Key: {}", r2SecretAccessKey != null ? "已设置" : "未设置");

        // 如果环境变量为空，也尝试从Java系统属性获取
        if (r2AccessKeyId == null || r2AccessKeyId.isEmpty()) {
            r2AccessKeyId = System.getProperty("CLOUDFLARE_R2_ACCESS_KEY_ID");
            log.info("从Java系统属性获取R2 Access Key ID: {}", r2AccessKeyId != null ? "已设置" : "未设置");
        }

        if (r2SecretAccessKey == null || r2SecretAccessKey.isEmpty()) {
            r2SecretAccessKey = System.getProperty("CLOUDFLARE_R2_ACCESS_KEY_SECRET");
            log.info("从Java系统属性获取R2 Secret Access Key: {}", r2SecretAccessKey != null ? "已设置" : "未设置");
        }

        // 如果还是为空，则从应用配置文件获取（这通常是开发测试环境的做法）
        if ((r2AccessKeyId == null || r2AccessKeyId.isEmpty()) && "dev".equals(activeProfile)) {
            // 仅在开发环境中尝试硬编码测试密钥，生产环境必须使用环境变量
            r2AccessKeyId = "开发测试密钥ID"; // 开发环境的测试密钥，实际项目中请替换
            r2SecretAccessKey = "开发测试密钥"; // 开发环境的测试密钥，实际项目中请替换
            log.warn("使用开发环境硬编码密钥 - 仅用于测试，生产环境请设置环境变量");
        }

        if (r2AccessKeyId == null || r2AccessKeyId.isEmpty() || r2SecretAccessKey == null || r2SecretAccessKey.isEmpty()) {
            log.error("未配置 Cloudflare R2 凭证，R2上传功能将不可用");
            log.error("请设置系统环境变量 CLOUDFLARE_R2_ACCESS_KEY_ID 和 CLOUDFLARE_R2_ACCESS_KEY_SECRET");
            log.error("可以通过以下方式设置环境变量:");
            log.error("Windows: 系统属性 -> 环境变量");
            log.error("Linux/Mac: export CLOUDFLARE_R2_ACCESS_KEY_ID=your_key_id");
            log.error("           export CLOUDFLARE_R2_ACCESS_KEY_SECRET=your_secret_key");
            log.error("Java: -DCLOUDFLARE_R2_ACCESS_KEY_ID=your_key_id -DCLOUDFLARE_R2_ACCESS_KEY_SECRET=your_secret_key");
            return;
        }

        try {
            log.info("开始初始化 Cloudflare R2 客户端，endpoint: {}, bucketName: {}", cloudflareR2Endpoint, cloudflareR2BucketName);

            // 初始化 Cloudflare R2 客户端
            r2Client = S3Client.builder()
                    .endpointOverride(URI.create(cloudflareR2Endpoint))
                    .region(Region.US_EAST_1) // Cloudflare R2 使用 US-EAST-1 作为占位符
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(r2AccessKeyId, r2SecretAccessKey)))
                    .build();

            log.info("Cloudflare R2 客户端初始化成功");
        } catch (Exception e) {
            log.error("初始化 Cloudflare R2 客户端失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成上传文件的路径
     *
     * @param fileName 文件名
     * @param fileType 文件类型（如 img, video, audio, document 等）
     * @return 上传路径
     */
    public String generateFilePath(String fileName, String fileType) {
        // 获取文件扩展名
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = fileName.substring(dotIndex);
        }

        // 生成日期文件夹路径 yyyy/MM/dd
        LocalDateTime now = LocalDateTime.now();
        String datePathFormat = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成随机文件名
        String randomFileName = UUID.randomUUID().toString().replaceAll("-", "");

        // 返回完整的文件路径
        return fileType + "/" + datePathFormat + "/" + randomFileName + fileExtension;
    }

    /**
     * 上传文件到Cloudflare R2
     *
     * @param file 要上传的文件
     * @return 文件访问URL
     * @throws IOException       如果上传过程中发生I/O错误
     * @throws BusinessException 如果R2客户端未初始化或上传失败
     */
    public String uploadR2(MultipartFile file) throws IOException, BusinessException {
        if (r2Client == null) {
            log.error("Cloudflare R2 客户端未初始化，请检查R2访问密钥配置");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Cloudflare R2 客户端未初始化，请检查环境变量配置");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "未知文件名";
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        // 根据文件类型确定子目录
        String fileType = "other";
        if (contentType.startsWith("image/")) {
            fileType = "img";
        } else if (contentType.startsWith("video/")) {
            fileType = "video";
        } else if (contentType.startsWith("audio/")) {
            fileType = "audio";
        } else if (contentType.startsWith("application/")) {
            fileType = "document";
        }

        // 生成文件路径
        String filePath = generateFilePath(originalFilename, fileType);
        log.info("准备上传文件: {}, 类型: {}, 目标路径: {}", originalFilename, contentType, filePath);

        // 上传到 Cloudflare R2
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(cloudflareR2BucketName)
                    .key(filePath)
                    .contentType(contentType)
                    .build();

            r2Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("文件上传到 Cloudflare R2 成功: {}", filePath);

            // 返回文件URL
            return cloudflareR2Domain + filePath;
        } catch (Exception e) {
            log.error("上传文件到 Cloudflare R2 失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 检查R2客户端是否已初始化
     *
     * @return 是否已初始化
     */
    public boolean isR2ClientInitialized() {
        return r2Client != null;
    }
}
