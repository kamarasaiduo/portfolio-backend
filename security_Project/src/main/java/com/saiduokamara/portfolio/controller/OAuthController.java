package com.saiduokamara.portfolio.controller;

import com.saiduokamara.portfolio.model.domain.User;
import com.saiduokamara.portfolio.model.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/success")
    public ResponseEntity<?> oauthSuccess(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            if (oauth2User == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "OAuth authentication failed",
                        "message", "User not authenticated via OAuth"
                ));
            }

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String login = oauth2User.getAttribute("login");

            if (email == null) {
                if (login != null) {
                    email = login + "@github.com";
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "Email not provided by OAuth provider"));
                }
            }

            User existingUser = userService.findByEmail(email).orElse(null);
            User user;

            if (existingUser == null) {
                user = new User();
                user.setEmail(email);
                user.setFullName(name != null ? name : (login != null ? login : "OAuth User"));
                user.setPassword(UUID.randomUUID().toString());
                user.setRole("USER");
                user = userService.registerUser(user);
            } else {
                user = existingUser;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OAuth login successful");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("OAuth2 ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "OAuth login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testOAuth() {
        return ResponseEntity.ok(Map.of(
                "message", "OAuth endpoint is reachable",
                "timestamp", System.currentTimeMillis()
        ));
    }
}