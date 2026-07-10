package com.henheang.securityapi.service.impl;

import com.henheang.securityapi.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Override
    public boolean sendPasswordResetEmail(String email, String name, String resetUrl) {
        try {
            log.info("Preparing to send password reset email to: {}", email);
            log.debug("From Email: {}, From Name: {}", fromEmail, fromName);
            log.debug("Reset URL: {}", resetUrl);

            // Validate inputs
            if (email == null || email.trim().isEmpty()) {
                log.error("Email address is null or empty");
                return false;
            }

            if (fromEmail == null || fromEmail.trim().isEmpty()) {
                log.error("From email is not configured. Please set MAIL_USERNAME environment variable");
                return false;
            }

            // Create the email context with variables for the template
            Context context = new Context();
            context.setVariable("name", name != null ? name : "User");
            context.setVariable("resetUrl", resetUrl);

            // Process the email template
            String htmlContent;
            try {
                htmlContent = templateEngine.process("password-reset-email", context);
                log.debug("Email template processed successfully");
            } catch (Exception e) {
                log.error("Failed to process email template: {}", e.getMessage(), e);
                return false;
            }

            // Create and send the email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(email);
            helper.setSubject("Reset Your AuthHub Password");
            helper.setText(htmlContent, true); // true indicates HTML content

            // Log email details before sending
            log.debug("Email prepared - To: {}, From: {} ({}), Subject: {}",
                    email, fromEmail, fromName, "Reset Your AuthHub Password");

            // Send the email
            mailSender.send(message);

            log.info("Password reset email sent successfully to: {}", email);
            return true;

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {} - MessagingException: {}", email, e.getMessage(), e);
            // Log more details about the messaging exception
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            return false;
        } catch (Exception e) {
            log.error("Unexpected error while sending password reset email to {}: {}", email, e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }
}