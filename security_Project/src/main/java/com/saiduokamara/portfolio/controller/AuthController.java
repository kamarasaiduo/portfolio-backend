package com.saiduokamara.portfolio.controller;

import com.saiduokamara.portfolio.model.domain.User;
import com.saiduokamara.portfolio.model.service.UserService;
import com.saiduokamara.portfolio.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("Registration attempt for: " + user.getEmail());
            User savedUser = userService.registerUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully. Please check your email to verify your account.");
            response.put("emailSent", true);
            response.put("user", Map.of(
                    "id", savedUser.getId(),
                    "fullName", savedUser.getFullName(),
                    "email", savedUser.getEmail(),
                    "role", savedUser.getRole(),
                    "enabled", savedUser.isEnabled()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for: " + loginRequest.getEmail());
            Optional<User> userOpt = userService.login(loginRequest.getEmail(), loginRequest.getPassword());

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), loginRequest.isRememberMe());

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("user", Map.of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                ));

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            String errorMessage = e.getMessage();
            HttpStatus status = HttpStatus.BAD_REQUEST;

            if (errorMessage.contains("verify your email")) {
                status = HttpStatus.FORBIDDEN;
                errorMessage = "Please verify your email before logging in. Check your inbox for the verification link.";
            } else if (errorMessage.contains("Invalid credentials")) {
                status = HttpStatus.UNAUTHORIZED;
                errorMessage = "Invalid email or password";
            }

            return ResponseEntity.status(status)
                    .body(Map.of("error", errorMessage));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userService.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "error", "Invalid or expired verification token"
            ));
        }

        User user = userOpt.get();
        if (user.isEnabled()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email already verified. You can login now."
            ));
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully. You can now login."
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email is required"));
            }

            userService.initiatePasswordReset(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Password reset instructions have been sent to your email",
                    "email", email
            ));
        } catch (Exception e) {
            System.out.println("Forgot password error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        try {
            if (resetRequest.getToken() == null || resetRequest.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Reset token is required"));
            }

            if (resetRequest.getNewPassword() == null || resetRequest.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password must be at least 6 characters long"));
            }

            boolean reset = userService.resetPassword(resetRequest.getToken(), resetRequest.getNewPassword());
            if (reset) {
                return ResponseEntity.ok(Map.of(
                        "message", "Password reset successfully. You can now login with your new password."
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid or expired reset token"));
            }
        } catch (Exception e) {
            System.out.println("Reset password error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email is required"));
            }

            userService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Verification email sent successfully. Please check your inbox.",
                    "email", email
            ));
        } catch (Exception e) {
            System.out.println("Resend verification error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Portfolio Backend",
                "timestamp", System.currentTimeMillis()
        ));
    }

    public static class LoginRequest {
        private String email;
        private String password;
        private boolean rememberMe;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public boolean isRememberMe() { return rememberMe; }
        public void setRememberMe(boolean rememberMe) { this.rememberMe = rememberMe; }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}