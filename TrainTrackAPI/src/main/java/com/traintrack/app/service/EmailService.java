package com.traintrack.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String firstName, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("traintrackph@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your TrainTrack PH Account");

            String htmlContent = buildVerificationEmailHtml(firstName, verificationCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("traintrackph@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Reset Your TrainTrack PH Password");

            String htmlContent = buildPasswordResetEmailHtml(firstName, resetToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildVerificationEmailHtml(String firstName, String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%);">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(10px); border-radius: 20px; border: 1px solid rgba(255, 255, 255, 0.08); padding: 50px 40px;">
                                
                                <tr>
                                    <td align="center" style="padding-bottom: 30px;">
                                        <h1 style="margin: 0; color: #fff; font-size: 32px;">
                                            <span style="color: #e94560;">tr</span><span style="color: #bb86fc;">▲</span><span style="color: #fff;">inTrack</span>
                                        </h1>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding-bottom: 20px;">
                                        <h2 style="color: #fff; font-size: 24px; margin: 0;">Hi %s! 👋</h2>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding-bottom: 30px;">
                                        <p style="color: rgba(255, 255, 255, 0.8); font-size: 16px; line-height: 1.6; margin: 0;">
                                            Welcome to TrainTrack PH! To complete your registration, please verify your email address using the code below:
                                        </p>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td align="center" style="padding: 30px 0;">
                                        <div style="background: rgba(255, 255, 255, 0.1); border: 2px solid #bb86fc; border-radius: 12px; padding: 20px; display: inline-block;">
                                            <p style="color: rgba(255, 255, 255, 0.7); font-size: 14px; margin: 0 0 10px 0;">Your verification code:</p>
                                            <h1 style="color: #bb86fc; font-size: 36px; letter-spacing: 8px; margin: 0;">%s</h1>
                                        </div>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding: 20px 0;">
                                        <p style="color: rgba(255, 255, 255, 0.6); font-size: 14px; text-align: center; margin: 0;">
                                            This code will expire in 24 hours.
                                        </p>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding-top: 30px; border-top: 1px solid rgba(255, 255, 255, 0.1);">
                                        <p style="color: rgba(255, 255, 255, 0.5); font-size: 13px; text-align: center; margin: 0;">
                                            If you didn't create this account, please ignore this email.
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, firstName, code);
    }

    private String buildPasswordResetEmailHtml(String firstName, String resetToken) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%);">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background: linear-gradient(135deg, #1a1a2e 0%%, #16213e 100%%); padding: 40px 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background: rgba(255, 255, 255, 0.05); backdrop-filter: blur(10px); border-radius: 20px; border: 1px solid rgba(255, 255, 255, 0.08); padding: 50px 40px;">
                                
                                <tr>
                                    <td align="center" style="padding-bottom: 30px;">
                                        <h1 style="margin: 0; color: #fff; font-size: 32px;">
                                            <span style="color: #e94560;">tr</span><span style="color: #bb86fc;">▲</span><span style="color: #fff;">inTrack</span>
                                        </h1>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding-bottom: 20px;">
                                        <h2 style="color: #fff; font-size: 24px; margin: 0;">Hi %s! 👋</h2>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding-bottom: 30px;">
                                        <p style="color: rgba(255, 255, 255, 0.8); font-size: 16px; line-height: 1.6; margin: 0;">
                                            We received a request to reset your password. Use the code below to reset your password:
                                        </p>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td align="center" style="padding: 30px 0;">
                                        <div style="background: rgba(255, 255, 255, 0.1); border: 2px solid #e94560; border-radius: 12px; padding: 20px; display: inline-block;">
                                            <p style="color: rgba(255, 255, 255, 0.7); font-size: 14px; margin: 0 0 10px 0;">Your reset code:</p>
                                            <h1 style="color: #e94560; font-size: 36px; letter-spacing: 8px; margin: 0;">%s</h1>
                                        </div>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding: 20px 0;">
                                        <p style="color: rgba(255, 255, 255, 0.6); font-size: 14px; text-align: center; margin: 0;">
                                            This code will expire in 1 hour.
                                        </p>
                                    </td>
                                </tr>
                                
                                <tr>
                                    <td style="padding-top: 30px; border-top: 1px solid rgba(255, 255, 255, 0.1);">
                                        <p style="color: rgba(255, 255, 255, 0.5); font-size: 13px; text-align: center; margin: 0;">
                                            If you didn't request a password reset, please ignore this email or contact support if you're concerned.
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, firstName, resetToken);
    }
}