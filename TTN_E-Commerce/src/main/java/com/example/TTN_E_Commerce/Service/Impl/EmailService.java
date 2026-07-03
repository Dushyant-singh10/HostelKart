package com.example.TTN_E_Commerce.Service.Impl;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String FROM;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out.println("\n==================================================");
        System.out.println("SMTP Configured Username: '" + FROM + "'");
        System.out.println("SMTP Configured Password Length: " + (mailPassword != null ? mailPassword.length() : 0));
        if (mailPassword != null) {
            System.out.println("SMTP Configured Password: '" + mailPassword + "'");
        }
        System.out.println("==================================================\n");
    }

    @Async
    public void sendActivationMail(String toEmail, String token) {

        String activationLink =
                backendBaseUrl + "/activate?token=" + token;

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
    public void sendActivationOtpMail(String toEmail, String otp) {
        String subject = "Activate Your Account - OTP Verification";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e4e4e7; rounded: 12px;">
                    <h3 style="color: #4f46e5; margin-bottom: 10px;">Welcome to HostelKart</h3>
                    <p style="color: #3f3f46; font-size: 14px; line-height: 1.5;">Please use the following 6-digit OTP to verify and activate your account:</p>
                    <div style="background-color: #f4f4f5; padding: 15px; border-radius: 8px; text-align: center; margin: 20px 0;">
                        <span style="font-size: 28px; font-weight: bold; letter-spacing: 4px; color: #18181b;">%s</span>
                    </div>
                    <p style="color: #71717a; font-size: 12px;">This OTP will expire in 5 minutes. If you did not request this email, you can safely ignore it.</p>
                </div>
                """.formatted(otp);
        sendHtmlMail(toEmail, subject, body);
    }

    @Async
    public void sendLoginOtpMail(String toEmail, String otp) {
        String subject = "Login Verification Code";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e4e4e7; rounded: 12px;">
                    <h3 style="color: #4f46e5; margin-bottom: 10px;">HostelKart Login Verification</h3>
                    <p style="color: #3f3f46; font-size: 14px; line-height: 1.5;">Please use the following 6-digit OTP to complete your login request:</p>
                    <div style="background-color: #f4f4f5; padding: 15px; border-radius: 8px; text-align: center; margin: 20px 0;">
                        <span style="font-size: 28px; font-weight: bold; letter-spacing: 4px; color: #18181b;">%s</span>
                    </div>
                    <p style="color: #71717a; font-size: 12px;">This OTP will expire in 5 minutes. If you did not make this request, please change your password immediately.</p>
                </div>
                """.formatted(otp);
        sendHtmlMail(toEmail, subject, body);
    }

    @Async
    public void resetPassword(String toEmail, String token) {

        String activationLink =
                backendBaseUrl + "/reset-passowrd/changePassword?token=" + token;

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
            System.out.println("[EmailService] Email sent successfully to " + to);

        } catch (Exception e) {
            System.out.println("\n==================================================");
            System.out.println("[EmailService] Failed to send email to " + to);
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Note: For local development, check the printed OTP/token in the logs above.");
            System.out.println("==================================================\n");
        }
    }
}
