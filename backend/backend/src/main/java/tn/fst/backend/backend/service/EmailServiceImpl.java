package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.fst.backend.backend.entity.Order;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Minimalistic Sage Green Palette
    private final String BG_COLOR = "#F4F7F6";
    private final String PRIMARY_COLOR = "#8DA399"; // Sage Green
    private final String TEXT_COLOR = "#2C3E50";
    private final String WHITE = "#FFFFFF";

    private String getHtmlTemplate(String title, String content, String iconSvg) {
        return "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: " + BG_COLOR + "; padding: 40px; color: " + TEXT_COLOR + ";\">" +
               "  <div style=\"max-width: 600px; margin: 0 auto; background-color: " + WHITE + "; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);\">" +
               "    <div style=\"background-color: " + PRIMARY_COLOR + "; padding: 30px; text-align: center;\">" +
               "      <div style=\"margin-bottom: 15px;\">" + iconSvg + "</div>" +
               "      <h1 style=\"color: " + WHITE + "; margin: 0; font-size: 24px; font-weight: 300; letter-spacing: 1px;\">" + title + "</h1>" +
               "    </div>" +
               "    <div style=\"padding: 40px; line-height: 1.6; font-size: 16px;\">" +
               content +
               "    </div>" +
               "    <div style=\"padding: 20px; text-align: center; font-size: 12px; color: #95a5a6; border-top: 1px solid #eee;\">" +
               "      &copy; " + java.time.Year.now().getValue() + " Globex - Minimalist Lifestyle" +
               "    </div>" +
               "  </div>" +
               "</div>";
    }

    private String getButton(String text, String url) {
        return "<div style=\"text-align: center; margin-top: 30px;\">" +
               "  <a href=\"" + url + "\" style=\"background-color: " + PRIMARY_COLOR + "; color: " + WHITE + "; padding: 14px 30px; text-decoration: none; border-radius: 6px; font-weight: 500; display: inline-block;\">" + text + "</a>" +
               "</div>";
    }

    @Override
    public void sendOrderStatusEmail(Order order) {
        try {
            String clientEmail = order.getClient().getUser().getEmail();
            String title = "Order Update";
            String icon = "<svg width=\"40\" height=\"40\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"1.5\"><path d=\"M5 13l4 4L19 7\"/></svg>";
            
            String content = "<p>Hello <strong>" + order.getClient().getUser().getFullname() + "</strong>,</p>" +
                             "<p>The status of your order <strong>#" + order.getIdOrder() + "</strong> has been updated.</p>" +
                             "<div style=\"background: #f9f9f9; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid " + PRIMARY_COLOR + ";\">" +
                             "Current Status: <strong>" + order.getStatus().name() + "</strong>" +
                             "</div>" +
                             "<p>Thank you for shopping with us.</p>";

            sendHtmlEmail(clientEmail, "Update for your order #" + order.getIdOrder(), getHtmlTemplate(title, content, icon));
            log.info("Status email sent to {} for order #{}", clientEmail, order.getIdOrder());
        } catch (Exception e) {
            log.error("Failed to send status email: {}", e.getMessage());
        }
    }

    @Override
    public void sendNewOrderAdminEmail(Order order, String adminEmail) {
        try {
            String title = "New Order Received";
            String icon = "<svg width=\"40\" height=\"40\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"1.5\"><circle cx=\"9\" cy=\"21\" r=\"1\"/><circle cx=\"20\" cy=\"21\" r=\"1\"/><path d=\"M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6\"/></svg>";
            
            String clientName = (order.getUser() != null) ? order.getUser().getFullname() : "Guest";
            String content = "<p>A new order has been placed on the platform.</p>" +
                             "<table style=\"width: 100%; border-collapse: collapse; margin-top: 20px;\">" +
                             "<tr><td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Order ID</td><td style=\"text-align: right; font-weight: bold;\">#" + order.getIdOrder() + "</td></tr>" +
                             "<tr><td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Customer</td><td style=\"text-align: right;\">" + clientName + "</td></tr>" +
                             "<tr><td style=\"padding: 8px 0; border-bottom: 1px solid #eee;\">Amount</td><td style=\"text-align: right; color: " + PRIMARY_COLOR + "; font-weight: bold;\">" + order.getTotalAmount() + " TND</td></tr>" +
                             "</table>";

            sendHtmlEmail(adminEmail, "New Order #" + order.getIdOrder() + " Received", getHtmlTemplate(title, content, icon));
        } catch (Exception e) {
            log.error("Failed to send admin email: {}", e.getMessage());
        }
    }

    @Override
    public void sendVerificationEmail(String email, String link) {
        try {
            String title = "Verify Your Email";
            String icon = "<svg width=\"40\" height=\"40\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"1.5\"><path d=\"M22 12h-4l-3 9L9 3l-3 9H2\"/></svg>";
            
            String content = "<p>Welcome to Globex! To activate your account and start shopping, please verify your email address.</p>" +
                             "<div style=\"text-align: center; margin-top: 30px;\">" +
                             "  <a href=\"" + link + "\" style=\"background-color: " + PRIMARY_COLOR + "; color: " + WHITE + "; padding: 14px 30px; text-decoration: none; border-radius: 6px; font-weight: 500; display: inline-block;\">Click Here to Verify</a>" +
                             "</div>" +
                             "<p style=\"margin-top: 30px; font-size: 13px; color: #7f8c8d;\">If you didn't create an account, you can safely ignore this email.</p>";

            sendHtmlEmail(email, "Welcome! Please verify your email address", getHtmlTemplate(title, content, icon));
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String link) {
        try {
            String title = "Reset Password";
            String icon = "<svg width=\"40\" height=\"40\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"1.5\"><path d=\"M12 15V17M12 7V13M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z\"/></svg>";
            
            String content = "<p>We received a request to reset your password. Please use the button below to choose a new password.</p>" +
                             "<div style=\"text-align: center; margin-top: 30px;\">" +
                             "  <a href=\"" + link + "\" style=\"background-color: " + PRIMARY_COLOR + "; color: " + WHITE + "; padding: 14px 30px; text-decoration: none; border-radius: 6px; font-weight: 500; display: inline-block;\">Click Here to Reset Password</a>" +
                             "</div>" +
                             "<p style=\"margin-top: 30px; font-size: 13px; color: #7f8c8d;\">If you didn't request this, please ignore this email.</p>";

            sendHtmlEmail(email, "Password Reset Request", getHtmlTemplate(title, content, icon));
        } catch (Exception e) {
            log.error("Failed to send reset email: {}", e.getMessage());
        }
    }

    @Override
    public void sendNewOrderClientEmail(Order order, String clientEmail) {
        try {
            String title = "Order Confirmed";
            String icon = "<svg width=\"40\" height=\"40\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"white\" stroke-width=\"1.5\"><path d=\"M20 7l-8 5-8-5M5 19h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2z\"/></svg>";
            
            String content = "<p>Thank you for your order <strong>#" + order.getIdOrder() + "</strong>. We've received it and are preparing it for shipment.</p>" +
                             "<div style=\"background: #f9f9f9; padding: 20px; border-radius: 8px; margin: 25px 0;\">" +
                             "  <p style=\"margin: 5px 0;\">Total Amount: <strong>" + order.getTotalAmount() + " TND</strong></p>" +
                             "  <p style=\"margin: 5px 0;\">Payment Method: <strong>" + (order.getPaymentMethodLabel() != null ? order.getPaymentMethodLabel() : "Standard") + "</strong></p>" +
                             "</div>" +
                             "<p>We'll notify you as soon as your package has been shipped. Thank you for choosing Globex!</p>";

            sendHtmlEmail(clientEmail, "Order Confirmation #" + order.getIdOrder(), getHtmlTemplate(title, content, icon));
        } catch (Exception e) {
            log.error("Failed to send client order email: {}", e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws jakarta.mail.MessagingException {
        jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
        org.springframework.mail.javamail.MimeMessageHelper helper = 
            new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        
        mailSender.send(message);
    }
}
