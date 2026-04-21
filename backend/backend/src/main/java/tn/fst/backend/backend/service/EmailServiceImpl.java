package tn.fst.backend.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceImpl(@org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
                            @Value("${app.mail.from:noreply@example.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        if (mailSender == null) {
            log.info("[DEV] Verification link for {}: {}", toEmail, verificationLink);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify your email address");
            message.setText("Please click the link below to verify your email:\n\n" + verificationLink + "\n\nThis link expires in 24 hours.");
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Could not send verification email to {}: {}. Link for dev: {}", toEmail, e.getMessage(), verificationLink);
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (mailSender == null) {
            log.info("[DEV] Password reset link for {}: {}", toEmail, resetLink);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset your password");
            message.setText("Please click the link below to reset your password:\n\n" + resetLink + "\n\nThis link expires in 1 hour. If you did not request this, ignore this email.");
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Could not send password reset email to {}: {}. Link for dev: {}", toEmail, e.getMessage(), resetLink);
        }
    }
}
