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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

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

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

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

        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
        System.out.println("=== Email Sending Debug ===");
        System.out.println("Sending verification email to: " + user.getEmail());
        System.out.println("Verification link: " + verificationLink);
        System.out.println("Frontend URL: " + frontendUrl);
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationLink);
            System.out.println("Verification email sent successfully");
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            System.err.println("Email sending failed, but user registration will continue");
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
                .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                        .orElseThrow(() -> new BadCredentialsException("Invalid username or email")));

        System.out.println("=== Login Debug ===");
        System.out.println("User: " + user.getUsername());
        System.out.println("Is Active: " + user.getIsActive());
        System.out.println("Email Verified: " + user.getEmailVerified());

        if (!user.getIsActive()) {
            throw new LockedException("User account is deactivated");
        }
        
        if (user.getEmailVerified() != null && !user.getEmailVerified()) {
            throw new DisabledException("Email not verified");
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
        System.out.println("=== Email Verification Debug ===");
        System.out.println("Received token: " + token);

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Invalid or expired verification token - token not found in database");
                    return new IllegalArgumentException("Invalid or expired verification token");
                });

        System.out.println("Found user: " + user.getUsername());
        System.out.println("Current emailVerified status: " + user.getEmailVerified());
        System.out.println("Token expiry: " + user.getEmailVerificationTokenExpiry());

        if (user.getEmailVerificationTokenExpiry() != null && user.getEmailVerificationTokenExpiry().isBefore(Instant.now())) {
            System.out.println("ERROR: Verification token has expired");
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);

        System.out.println("Saving user with emailVerified=true");
        User savedUser = userRepository.save(user);
        System.out.println("User saved successfully. New emailVerified status: " + savedUser.getEmailVerified());

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

        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
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