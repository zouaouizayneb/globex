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

    @Override
    public void sendOrderStatusEmail(Order order) {
        try {
            String clientEmail = order.getClient().getUser().getEmail();
            String clientName = order.getClient().getUser().getFullname();
            String orderStatus = order.getStatus().name();
            Long orderId = order.getIdOrder();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(clientEmail);
            message.setSubject("Mise à jour de votre commande #" + orderId);
            
            String content = String.format(
                "Bonjour %s,\n\n" +
                "Le statut de votre commande #%d a été mis à jour.\n" +
                "Nouveau statut : %s\n\n" +
                "Merci de votre confiance.\n" +
                "L'équipe Cadence",
                clientName, orderId, orderStatus
            );
            
            message.setText(content);
            mailSender.send(message);
            
            log.info("Email sent successfully to {} for order #{}", clientEmail, orderId);
        } catch (Exception e) {
            log.error("Failed to send email for order #{}: {}", order.getIdOrder(), e.getMessage());
        }
    }

    @Override
    public void sendNewOrderAdminEmail(Order order, String adminEmail) {
        try {
            String clientName = (order.getUser() != null) ? order.getUser().getFullname() : "Guest";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("🛒 Nouvelle commande #" + order.getIdOrder() + " reçue");
            String content = String.format(
                "Une nouvelle commande vient d'être passée.\n\n" +
                "Commande : #%d\n" +
                "Client    : %s\n" +
                "Montant   : %.2f TND\n" +
                "Statut    : %s\n\n" +
                "Connectez-vous au tableau de bord pour la traiter.\n" +
                "L'équipe Cadence",
                order.getIdOrder(), clientName,
                order.getTotalAmount(), order.getStatus().name()
            );
            message.setText(content);
            mailSender.send(message);
            log.info("Admin new-order email sent to {} for order #{}", adminEmail, order.getIdOrder());
        } catch (Exception e) {
            log.error("Failed to send admin new-order email for order #{}: {}", order.getIdOrder(), e.getMessage());
        }
    }

    @Override
    public void sendVerificationEmail(String email, String link) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Vérifiez votre adresse email");
            message.setText("Merci de vous être inscrit. Veuillez cliquer sur le lien suivant pour vérifier votre email : " + link);
            mailSender.send(message);
            log.info("Verification email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String email, String link) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText("Vous avez demandé une réinitialisation de mot de passe. Veuillez cliquer sur le lien suivant : " + link);
            mailSender.send(message);
            log.info("Password reset email sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }
}
