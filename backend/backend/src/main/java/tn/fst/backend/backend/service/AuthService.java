package tn.fst.backend.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.fst.backend.backend.dto.AuthResponse;
import tn.fst.backend.backend.dto.LoginRequest;
import tn.fst.backend.backend.dto.MessageResponse;
import tn.fst.backend.backend.dto.RegisterRequest;
import tn.fst.backend.backend.entity.Client;
import tn.fst.backend.backend.entity.Role;
import tn.fst.backend.backend.entity.User;
import tn.fst.backend.backend.repository.ClientRepository;
import tn.fst.backend.backend.repository.UserRepository;
import tn.fst.backend.backend.security.JwtUtil;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailValidationService emailValidationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate email format and check if domain has valid MX records
        // Temporarily disabled MX validation to prevent blocking legitimate registrations
        // if (!emailValidationService.isValidEmail(request.getEmail())) {
        //     throw new IllegalArgumentException("Invalid email address or email domain does not exist");
        // }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Public registration: only CLIENT role allowed
        Role role = request.getRole() != null ? request.getRole() : Role.CLIENT;
        if (role != Role.CLIENT) {
            role = Role.CLIENT;
        }

        String verificationToken = UUID.randomUUID().toString();
        Instant verificationExpiry = Instant.now().plusSeconds(EMAIL_VERIFICATION_EXPIRY_HOURS * 3600L);

        User user = User.builder()
                .username(request.getUsername())
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(role)
                .isActive(true)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiry(verificationExpiry)
                .build();

        user = userRepository.save(user);

        if (user.getRole() == Role.CLIENT) {
            Client client = new Client();
            client.setUser(user);
            client.setName(user.getFullname());
            client.setPhoneNumber(user.getPhoneNumber());
            clientRepository.save(client);
        }

        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + verificationToken;
        System.out.println("Sending verification email to: " + user.getEmail());
        System.out.println("Verification link: " + verificationLink);
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationLink);
            System.out.println("Verification email sent successfully");
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .userId(user.getIdUser())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new IllegalStateException("User account is deactivated");
        }

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .userId(user.getIdUser())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (user.getEmailVerificationTokenExpiry() != null && user.getEmailVerificationTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        return MessageResponse.builder().message("Email verified successfully").build();
    }

    @Transactional
    public MessageResponse requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found with this email"));

        String resetToken = UUID.randomUUID().toString();
        Instant resetExpiry = Instant.now().plusSeconds(PASSWORD_RESET_EXPIRY_HOURS * 3600L);
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(resetExpiry);
        userRepository.save(user);

        String resetLink = baseUrl + "/api/auth/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        return MessageResponse.builder()
                .message("If an account exists for this email, you will receive a password reset link")
                .build();
    }

    @Transactional
    public MessageResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (user.getPasswordResetTokenExpiry() != null && user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return MessageResponse.builder().message("Password has been reset successfully").build();
    }


    public AuthResponse getCurrentUserResponse(User user) {
        return AuthResponse.builder()
                .token(null)
                .userId(user.getIdUser())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .build();
    }
}