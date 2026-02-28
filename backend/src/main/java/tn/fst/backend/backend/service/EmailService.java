package tn.fst.backend.backend.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String verificationLink);

    void sendPasswordResetEmail(String toEmail, String resetLink);
}
