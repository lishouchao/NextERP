package com.nexterp.platform.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * 邮件发送服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:nexterp@system.com}")
    private String fromEmail;

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);

            mailSender.send(message);
            log.info("发送邮件成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("发送邮件失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("发送邮件失败", e);
        }
    }

    /**
     * 发送HTML邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content HTML内容
     */
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("发送HTML邮件成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("发送HTML邮件失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("发送HTML邮件失败", e);
        }
    }

    /**
     * 批量发送邮件
     *
     * @param toList  收件人列表
     * @param subject 主题
     * @param content 内容
     */
    public void sendBatchEmail(List<String> toList, String subject, String content) {
        for (String to : toList) {
            try {
                sendHtmlEmail(to, subject, content);
            } catch (Exception e) {
                log.error("批量发送邮件失败: to={}", to, e);
            }
        }
    }
}
