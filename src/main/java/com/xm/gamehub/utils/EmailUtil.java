package com.xm.gamehub.utils;

import com.xm.gamehub.common.ErrorCode;
import com.xm.gamehub.exception.BusinessException;
import com.xm.gamehub.model.entity.email.VerifyCodeEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class EmailUtil {
    private volatile static EmailUtil instance;
    
    @Resource
    private JavaMailSender javaMailSender;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Value("${spring.mail.username}")
    private String emailFrom;

    private static String EMAIL_FROM;
    private static final String VERIFY_CODE_PREFIX = "verify:code:";

    private EmailUtil() {
        // 私有构造函数
    }

    @PostConstruct
    public void init() {
        EMAIL_FROM = emailFrom;
        instance = this;
    }

    public static EmailUtil getInstance() {
        if (instance == null) {
            synchronized (EmailUtil.class) {
                if (instance == null) {
                    instance = new EmailUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 发送验证码邮件
     *
     * @param toEmail 目标邮箱
     */
    public void sendVerificationCode(String toEmail) {
        // 1. 校验邮箱格式
        if (!toEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 2. 检查Redis连接
        checkRedisConnection();

        try {
            // 3. 生成6位随机验证码
            String verifyCode = CaptchaUtils.generate_6_AZ09();
            String key = VERIFY_CODE_PREFIX + toEmail;
            stringRedisTemplate.opsForValue().set(key, verifyCode, 5, TimeUnit.MINUTES);
            log.info("为邮箱 {} 生成验证码: {}", toEmail, verifyCode);

            // 4. 创建邮件消息
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 5. 使用 VerifyCodeEmail 中的配置
            helper.setFrom(EMAIL_FROM, VerifyCodeEmail.organization);
            helper.setTo(toEmail);
            helper.setSubject(VerifyCodeEmail.title);
            
            // 6. 使用 VerifyCodeEmail 中的模板
            String content = String.format(
                VerifyCodeEmail.content,
                VerifyCodeEmail.title,
                verifyCode
            );
            helper.setText(content, true);

            // 7. 发送邮件
            log.info("开始发送验证码邮件到: {}", toEmail);
            javaMailSender.send(message);
            log.info("验证码邮件发送成功: {}", toEmail);

        } catch (MailException | MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件服务异常");
        } catch (Exception e) {
            log.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送失败");
        }
    }

    // 检查 Redis 连接
    private void checkRedisConnection() {
        if (stringRedisTemplate == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis配置错误");
        }

        try {
            stringRedisTemplate.getConnectionFactory().getConnection().close();
        } catch (Exception e) {
            log.error("Redis连接失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Redis服务未启动");
        }
    }
} 