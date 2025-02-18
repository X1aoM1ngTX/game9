package com.xm.gamehub.utils;

import java.util.Random;

/**
 * 工具类：生成各种验证码
 */
public class CaptchaUtils {

    /**
     * 生成6位字母和数字混合的验证码
     * 
     * @return 生成6位字母和数字混合的验证码(A-Z,0-9)
     */
    public static String generate_6_AZ09() {
        // 字符集（包括大小写字母和数字）
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();

        // 生成8位长度的验证码
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(characters.length());
            captcha.append(characters.charAt(index));
        }

        return captcha.toString();
    }

    /**
     * 生成6位纯数字验证码
     * 
     * @return 生成6位纯数字验证码(0-999999)
     */
    public static String generate_6_09() {
        Random random = new Random();
        int randomNumber = random.nextInt(1000000); // 生成0到999999之间的随机数
        return String.format("%06d", randomNumber); // 返回6位格式化的数字
    }
}
