package com.xm.game9.model.entity.email;

public class VerifyCodeEmail {
    public static final String organization = "Game Hub";
    public static final String title = "Game Hub 验证码";
    public static final String content =
            "<div style='background-color: #f8f9fa; padding: 40px; text-align:center; border-radius: 10px; max-width: 500px; margin: 0 auto;'>" +
                    "<img src='E:\\Local\\desktop\\Code\\game9\\src\\main\\static\\assets\\GameHub_500px.png' style='width: 80px; margin-bottom: 20px;' alt='logo'>" +
                    "<h2 style='color: #333; margin-bottom: 30px;'>%s</h2>" +
                    "<p style='font-size: 16px; color: #666;'>您的验证码是：</p>" +
                    "<p style='margin: 20px 0;'><strong style='color: #3370ff; font-size: 32px; letter-spacing: 5px;'>%s</strong></p>" +
                    "<p style='color: #999; font-size: 14px; margin-bottom: 10px;'>验证码有效期为5分钟，请尽快使用</p>" +
                    "<p style='color: #999; font-size: 14px;'>如果这不是您的操作，请忽略此邮件</p>" +
                    "</div>";
}
