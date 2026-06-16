package com.example.TTN_E_Commerce.Service.Impl;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String FROM = "himanshu.sharma9@tothenew.com";

    @Async
    public void sendActivationMail(String toEmail, String token) {

        String activationLink =
                "http://localhost:8080/activate?token=" + token;

        String subject = "Activate Your Account";

        String body = """
                <h3>Welcome to E-Commerce App</h3>
                <p>Please click below to activate your account:</p>
                <a href="%s">Activate Account</a>
                <br><br>
                <p>This link will expire in 24 hours.</p>
                """.formatted(activationLink);

        sendHtmlMail(toEmail, subject, body);
    }

    @Async
    public void resetPassword(String toEmail, String token) {

        String activationLink =
                "http://localhost:8080/reset-passowrd/changePassword?token=" + token;

        String subject = "Reset Password For Your Account";

        String body = """
                <h3>Welcome to E-Commerce App</h3>
                <p>Please click below to change your password:</p>
                <a href="%s">Reset Password</a>
                <br><br>
                <p>This link will expire in 15 Minutes.</p>
                """.formatted(activationLink);

        sendHtmlMail(toEmail, subject, body);
    }

    //Generic Mail hai ye vaali
    @Async
    public void sendHtmlMail(String to, String subject, String htmlContent) {

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
