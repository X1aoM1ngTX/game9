package com.xm.xmgame.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public class UploadUtil {

    public static final String ALIYUN_OSS_DOMAIN = "https://xmgame-bucket.oss-cn-guangzhou.aliyuncs.com/";

    public static String uploadAliyunOss(MultipartFile file) throws IOException {
        // 获取文件名
        String originalFileName = file.getOriginalFilename();
        // 获取文件后缀
        String ext = "." + FilenameUtils.getExtension(originalFileName);
        // 生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "_") + ext;

        // 阿里云OSS相关配置
        String ALIYUN_OSS_ENDPOINT = "http://oss-cn-guangzhou.aliyuncs.com";
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(ALIYUN_OSS_ENDPOINT, ALIYUN_OSS_ACCESS_KEY_ID, ALIYUN_OSS_ACCESS_KEY_SECRET);
        // 上传文件
        ossClient.putObject(
                "xmgame-bucket",
                newFileName,
                file.getInputStream());
        ossClient.shutdown();
        // 返回文件访问路径
        return ALIYUN_OSS_DOMAIN + newFileName;
    }
}
