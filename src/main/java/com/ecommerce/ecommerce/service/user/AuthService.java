package com.ecommerce.ecommerce.service.user;

import com.ecommerce.ecommerce.config.JwtUtil;
import com.ecommerce.ecommerce.dto.user.ResetPasswordDTO;
import com.ecommerce.ecommerce.dto.user.UserLoginDTO;
import com.ecommerce.ecommerce.dto.user.UserRegisterDTO;
import com.ecommerce.ecommerce.model.user.AuthResponse;
import com.ecommerce.ecommerce.model.user.ConfirmationToken;
import com.ecommerce.ecommerce.model.user.Role;
import com.ecommerce.ecommerce.model.user.User;
import com.ecommerce.ecommerce.repository.user.ConfirmationTokenRepository;
import com.ecommerce.ecommerce.repository.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder , EmailService emailService, ConfirmationTokenRepository confirmationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public User saveUser(UserRegisterDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : Role.BUYER);
        user.setEnabled(false); // Disable until email is confirmed

        User savedUser = userRepository.save(user);

        // Generate confirmation token with expiration
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmationToken(UUID.randomUUID().toString());
        confirmationToken.setUserEntity(savedUser);
        confirmationToken.setCreatedDate(new Date());
        confirmationToken.setExpiryDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15-minute expiry

        confirmationTokenRepository.save(confirmationToken);

        // Send verification email
        sendVerificationEmail(savedUser.getEmail(), confirmationToken.getConfirmationToken());

        return savedUser;
    }

    private void sendVerificationEmail(String email, String token) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Complete Registration!");
        mailMessage.setText("To confirm your account, please click here: "
                + "http://localhost:8080/api/auth/confirm-account?token=" + token);

        emailService.sendEmail(mailMessage);
    };

    public ResponseEntity<?> confirmEmail(String confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);

        if (token == null) {
            return ResponseEntity.badRequest().body("Error: Invalid token");
        }

        // Check if token is expired
        if (new Date().after(token.getExpiryDate())) {
            return ResponseEntity.badRequest().body("Error: Token has expired. Please register again.");
        }

        User user = userRepository.findByEmailIgnoreCase(token.getUserEntity().getEmail()).get();

        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("Email verified successfully!");
    }

    public AuthResponse authenticateUser(UserLoginDTO loginDTO) {
        Optional<User> userOptional = userRepository.findByUsername(loginDTO.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {

                // Check if user email is verified
                if (!user.isEnabled()) {

                    // Check if a token already exists
                    ConfirmationToken existingToken = confirmationTokenRepository.findByUserEntity(user);

                    if (existingToken == null || new Date().after(existingToken.getExpiryDate())) {
                        ConfirmationToken newToken = new ConfirmationToken();
                        newToken.setConfirmationToken(UUID.randomUUID().toString());
                        newToken.setUserEntity(user);
                        newToken.setCreatedDate(new Date());
                        newToken.setExpiryDate(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15-minute expiry

                        confirmationTokenRepository.save(newToken);

                        // Send verification email
                        sendVerificationEmail(user.getEmail(), newToken.getConfirmationToken());
                    } else {
                        // Resend the existing token
                        sendVerificationEmail(user.getEmail(), existingToken.getConfirmationToken());
                    }

                    return new AuthResponse(null, "Account is not verified. A new verification email has been sent.");
                }

                // Generate JWT token for verified users
                String token = JwtUtil.generateToken(user.getUsername(), String.valueOf(user.getRole()));
                return new AuthResponse(token, String.valueOf(user.getRole()));
            }
        }

        return null; // Invalid login credentials
    }


    public boolean forgetPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Check if an existing token is present for the user
            ConfirmationToken existingToken = confirmationTokenRepository.findByUserEntity(user);

            ConfirmationToken resetToken = null;
            if (existingToken != null) {
                // Update the existing token with a new one
                existingToken.setConfirmationToken(UUID.randomUUID().toString());
                existingToken.setCreatedDate(new Date());
                existingToken.setExpiryDate(new Date(System.currentTimeMillis() + 30 * 60 * 1000)); // 30-minute expiry
                confirmationTokenRepository.save(existingToken);
            } else {
                // Create a new token if no existing one is found
                resetToken = new ConfirmationToken(user);
                confirmationTokenRepository.save(resetToken);
            }

            // Send password reset email
            sendResetPasswordEmail(user.getEmail(), existingToken != null ? existingToken.getConfirmationToken() : resetToken.getConfirmationToken());

            return true;
        }

        return false; // Email not found
    }

    public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // Find the token in the database
        ConfirmationToken tokenEntity = confirmationTokenRepository.findByConfirmationToken(resetPasswordDTO.getToken());

        if (tokenEntity == null) {
            return false; // Token is invalid
        }

        // Check if the token is expired
        if (new Date().after(tokenEntity.getExpiryDate())) {
            confirmationTokenRepository.delete(tokenEntity); // Delete expired token
            return false; // Token expired
        }

        // Fetch the user associated with the token
        User user = tokenEntity.getUserEntity();

        // Validate password confirmation
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match!");
        }

        // Update user password
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRepository.save(user);

        // Delete the token after successful password reset
        confirmationTokenRepository.delete(tokenEntity);

        return true;
    }

    private void sendResetPasswordEmail(String email, String token) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset Your Password!");
        mailMessage.setText("To reset your password, please click the link below:\n"
                + "http://localhost:8080/api/auth/reset-password?token=" + token
                + "\n\nThis link will expire in 30 minutes.");

        emailService.sendEmail(mailMessage);
    }

}
