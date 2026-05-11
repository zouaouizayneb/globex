package tn.fst.backend.backend.service;

import tn.fst.backend.backend.entity.Order;

public interface EmailService {
    void sendOrderStatusEmail(Order order);
    void sendNewOrderAdminEmail(Order order, String adminEmail);
    void sendVerificationEmail(String email, String link);
    void sendPasswordResetEmail(String email, String link);
}
