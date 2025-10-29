package com.saiduokamara.portfolio.controller;

import com.saiduokamara.portfolio.model.domain.User;
import com.saiduokamara.portfolio.model.dto.CreateUserRequest;
import com.saiduokamara.portfolio.model.dto.UpdateUserRequest;
import com.saiduokamara.portfolio.model.dto.UserResponse;
import com.saiduokamara.portfolio.model.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.findAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to fetch users: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            if (user != null) {
                return ResponseEntity.ok(convertToUserResponse(user));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "User not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch user: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            if (userService.userExists(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "User with this email already exists"));
            }

            User user = new User();
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setPassword(request.getPassword());
            user.setRole(request.getRole() != null ? request.getRole() : "USER");

            User createdUser = userService.registerUser(user);
            return ResponseEntity.ok(convertToUserResponse(createdUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        try {
            User userDetails = new User();
            userDetails.setEmail(request.getEmail());
            userDetails.setFullName(request.getFullName());
            userDetails.setPassword(request.getPassword());
            userDetails.setRole(request.getRole());

            User updatedUser = userService.updateUser(id, userDetails);

            if (updatedUser != null) {
                return ResponseEntity.ok(convertToUserResponse(updatedUser));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "User not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            if (user != null) {
                userService.deleteUser(id);
                return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "User not found with id: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete user: " + e.getMessage()));
        }
    }
}