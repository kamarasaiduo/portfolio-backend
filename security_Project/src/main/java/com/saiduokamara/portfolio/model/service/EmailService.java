package com.saiduokamara.portfolio.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = frontendUrl + "/email-verification?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Verify Your Email Address - Portfolio App");
            message.setText(
                    "Welcome to Portfolio App!\n\n" +
                            "Please click the link below to verify your email address:\n\n" +
                            verificationUrl + "\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you didn't create an account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Portfolio App Team"
            );

            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Reset Your Password - Portfolio App");
            message.setText(
                    "You have requested to reset your password.\n\n" +
                            "Please click the link below to reset your password:\n\n" +
                            resetUrl + "\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request a password reset, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "Portfolio App Team"
            );

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to Portfolio App!");
            message.setText(
                    "Hello " + fullName + ",\n\n" +
                            "Welcome to Portfolio App! Your account has been successfully verified.\n\n" +
                            "You can now login and start using all the features of our application.\n\n" +
                            "If you have any questions, feel free to contact us.\n\n" +
                            "Best regards,\n" +
                            "Portfolio App Team"
            );

            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }
}