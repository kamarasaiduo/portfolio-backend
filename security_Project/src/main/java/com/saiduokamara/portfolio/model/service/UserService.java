package com.saiduokamara.portfolio.model.service;

import com.saiduokamara.portfolio.model.domain.User;
import com.saiduokamara.portfolio.model.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public User registerUser(User user) {
        try {
            System.out.println("🔵 USER SERVICE: Starting registration for: " + user.getEmail());

            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                System.out.println("🔵 USER SERVICE: User already exists with email: " + user.getEmail());
                throw new RuntimeException("User with this email already exists");
            }

            // Set default role if not provided
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("USER");
            }

            // Encode password
            String rawPassword = user.getPassword();
            user.setPassword(passwordEncoder.encode(rawPassword));
            System.out.println("🔵 USER SERVICE: Password encoded successfully");

            // Generate verification token
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setEnabled(false); // User must verify email first

            // Save user
            User savedUser = userRepository.save(user);
            System.out.println("🔵 USER SERVICE: User saved with ID: " + savedUser.getId());

            // Send verification email
            try {
                emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
                System.out.println("✅ Verification email sent successfully");
            } catch (Exception e) {
                System.err.println("⚠️ Verification email could not be sent: " + e.getMessage());
                // Don't throw exception - user is created but email failed
            }

            return savedUser;
        } catch (Exception e) {
            System.out.println("🔵 USER SERVICE: Error during registration: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean verifyUser(String token) {
        try {
            Optional<User> userOpt = userRepository.findByVerificationToken(token);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(true);
                user.setVerificationToken(null);
                userRepository.save(user);

                // Send welcome email (best-effort)
                try {
                    emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
                } catch (Exception e) {
                    System.err.println("⚠️ Welcome email could not be sent: " + e.getMessage());
                }

                System.out.println("✅ User verified: " + user.getEmail());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error verifying user token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            System.out.println("✅ Password reset initiated for: " + email);
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                userRepository.save(user);
                System.out.println("✅ Password reset successful for: " + user.getEmail());
                return true;
            } else {
                throw new RuntimeException("Reset token has expired");
            }
        }
        return false;
    }

    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if user is verified
            if (!user.isEnabled()) {
                throw new RuntimeException("Please verify your email before logging in");
            }

            // Check password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return userOpt;
            }
        }
        return Optional.empty();
    }

    public User registerOAuthUser(String email, String fullName) {
        try {
            System.out.println("🔵 USER SERVICE: Registering OAuth user: " + email);

            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                System.out.println("🔵 USER SERVICE: OAuth user already exists: " + email);
                return existingUser.get();
            }

            // Create new OAuth user
            User user = new User();
            user.setEmail(email);
            user.setFullName(fullName != null ? fullName : "OAuth User");

            // Generate a random password for OAuth users
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(passwordEncoder.encode(randomPassword));

            user.setRole("USER");
            user.setEnabled(true); // OAuth users are automatically verified

            User savedUser = userRepository.save(user);
            System.out.println("✅ USER SERVICE: OAuth user registered successfully: " + savedUser.getId());

            return savedUser;
        } catch (Exception e) {
            System.err.println("❌ USER SERVICE: Error registering OAuth user: " + e.getMessage());
            throw e;
        }
    }

    // CRUD methods
    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("🔵 USER SERVICE: Found " + users.size() + " users in database");
        return users;
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            // Update email if provided and different
            if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
                // Check if new email already exists
                Optional<User> existingUser = userRepository.findByEmail(userDetails.getEmail());
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    throw new RuntimeException("Email already exists");
                }
                user.setEmail(userDetails.getEmail());
            }

            // Update full name if provided
            if (userDetails.getFullName() != null) {
                user.setFullName(userDetails.getFullName());
            }

            // Update password only if provided and not empty
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            // Update role if provided
            if (userDetails.getRole() != null) {
                user.setRole(userDetails.getRole());
            }

            return userRepository.save(user);
        }).orElse(null);
    }

    // ✅ NEW: updateUser(User user) for direct updates
    public User updateUser(User user) {
        return userRepository.save(user);
    }


    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Additional utility methods
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public long countUsers() {
        return userRepository.count();
    }

    public List<User> findUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .toList();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    public Optional<User> findByResetToken(String token) {
        return userRepository.findByResetToken(token);
    }

    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Email is already verified");
            }

            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            userRepository.save(user);

            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }
}