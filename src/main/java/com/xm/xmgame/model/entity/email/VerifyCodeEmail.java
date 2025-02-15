package com.xm.xmgame.model.entity.email;

public class VerifyCodeEmail {
    public static final String organization = "GameHub";
    public static final String title = "GameHub 验证码";
    public static final String content = 
        "<div style='text-align:center'>" +
        "<h2>%s</h2>" +
        "<p>您的验证码是：<strong style='color:#1890ff;font-size:20px'>%s</strong></p>" +
        "<p>验证码有效期为5分钟，请尽快使用</p>" +
        "</div>";
}
