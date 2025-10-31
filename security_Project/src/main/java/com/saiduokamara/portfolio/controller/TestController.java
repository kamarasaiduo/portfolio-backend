package com.saiduokamara.portfolio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/test-email")
    public String testEmail() {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("your-test-email@gmail.com"); // Replace with your actual email
            message.setSubject("Test Email from Portfolio");
            message.setText("This is a test email from your application.");
            message.setFrom("kamerasst8z606@gmail.com");

            mailSender.send(message);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Email failed: " + e.getMessage();
        }
    }
}