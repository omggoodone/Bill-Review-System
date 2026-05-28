package com.zsc.module.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from;

    @Async
    public void sendCredentials(String to, String username, String password, String role) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            helper.setSubject("票据审核系统 - 账号信息");
            helper.setText(String.format("""
                您好，您的票据审核系统账号已创建：

                    用户名：%s
                    密码：%s
                    角色：%s
                    创建时间：%s

                请尽快登录并修改初始密码。
                """, username, password, role, now), false);
            mailSender.send(message);
            log.info("邮件发送成功: {} -> {}", from, to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: {} -> {}", from, to, e);
        }
    }

    @Async
    public void sendAccountDisabled(String to, String username, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("票据审核系统 - 账号变更通知");
            helper.setText(String.format("""
                您好，%s：

                    %s

                如有疑问，请联系系统管理员。
                """, username, reason), false);
            mailSender.send(message);
            log.info("邮件发送成功: {} -> {}", from, to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: {} -> {}", from, to, e);
        }
    }
}
