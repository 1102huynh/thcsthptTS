package com.schoolmanagement.service;

import com.schoolmanagement.entity.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Real SMTP delivery via Spring's JavaMailSender (see spring.mail.* in
 * application.yml, MAIL_* env vars). No SMTP server configured is a normal,
 * expected local-dev state — send() catches that and reports failure per
 * recipient instead of letting it blow up the whole request.
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Override
    public boolean send(String recipientContact, String title, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipientContact);
            message.setSubject(title);
            message.setText(content);
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            log.warn("Failed to send email notification to {}: {}", recipientContact, ex.getMessage());
            return false;
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
