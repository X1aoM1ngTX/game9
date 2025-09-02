package com.xm.game9.utils;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 加密工具类
 */
@Component
public class EncryptionUtil {
    
    // 加密密钥（需要与前端保持一致）- 16字节
    private static final String SECRET_KEY = "game9_secret_key";
    
    /**
     * AES解密
     * @param encryptedData 加密的数据
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData) {
        try {
            System.out.println("开始解密，密钥长度: " + SECRET_KEY.length());
            System.out.println("加密数据: " + encryptedData);
            
            // 创建AES密钥
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            System.out.println("密钥创建成功");
            
            // 创建Cipher实例
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            System.out.println("Cipher初始化成功");
            
            // 解密数据
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            String result = new String(decryptedBytes);
            System.out.println("解密成功，结果: " + result);
            
            return result;
        } catch (Exception e) {
            System.out.println("解密失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("解密失败", e);
        }
    }
}