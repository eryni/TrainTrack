package com.traintrack.app.controller;

import com.traintrack.app.model.SavedSchedule;
import com.traintrack.app.repository.SavedScheduleRepository;
import java.util.stream.Collectors;
import com.traintrack.app.model.User;
import com.traintrack.app.service.UserService;
import com.traintrack.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SavedScheduleRepository savedScheduleRepository;

    private static final String UPLOAD_DIR = "uploads/profile-images/";

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("Registration attempt for email: " + user.getEmail());

            if (userService.getUserByEmail(user.getEmail()).isPresent()) {
                System.out.println("Email already registered: " + user.getEmail());
                response.put("error", "Email already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            String verificationCode = String.format("%06d", new Random().nextInt(999999));
            user.setVerificationCode(verificationCode);
            user.setEmailVerified(false);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));

            System.out.println("Generated verification code: " + verificationCode);

            User createdUser = userService.createUser(user);
            System.out.println("User created with ID: " + createdUser.getUserId());

            try {
                emailService.sendVerificationEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        verificationCode
                );
                System.out.println("Verification email sent to: " + user.getEmail());
            } catch (Exception emailError) {
                System.err.println("Failed to send email: " + emailError.getMessage());
                emailError.printStackTrace();
            }

            response.put("message", "Registration successful! Check your email for verification code.");
            response.put("email", createdUser.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String email = credentials.get("email");
        String password = credentials.get("password");

        System.out.println("Login attempt for email: " + email);

        User user = userService.getUserByEmail(email).orElse(null);

        if (user == null) {
            System.out.println("User not found: " + email);
            response.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        System.out.println("User found - Email verified: " + user.getEmailVerified());

        if (!user.getEmailVerified()) {
            System.out.println("Email not verified for: " + email);
            response.put("error", "Please verify your email before logging in");
            response.put("emailNotVerified", true);
            response.put("email", user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (userService.validatePassword(password, user.getPasswordHash())) {
            System.out.println("Login successful for: " + email);
            user.setPasswordHash(null);
            response.put("user", user);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        }

        System.out.println("Invalid password for: " + email);
        response.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String code = request.get("code");

            System.out.println("Email verification attempt for: " + email + " with code: " + code);

            User user = userService.getUserByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("User not found: " + email);
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (user.getEmailVerified()) {
                System.out.println("Email already verified: " + email);
                response.put("message", "Email already verified");
                return ResponseEntity.ok(response);
            }

            if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
                System.out.println("Verification code expired for: " + email);
                response.put("error", "Verification code expired. Please request a new one.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            System.out.println("Stored code: " + user.getVerificationCode() + ", Provided code: " + code);

            if (!user.getVerificationCode().equals(code)) {
                System.out.println("Invalid verification code for: " + email);
                response.put("error", "Invalid verification code");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            user.setEmailVerified(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiry(null);
            userService.updateUser(user.getUserId(), user);

            System.out.println("Email verified successfully for: " + email);

            response.put("message", "Email verified successfully!");
            response.put("verified", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Verification failed: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            System.out.println("Resending verification code to: " + email);

            User user = userService.getUserByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("User not found: " + email);
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (user.getEmailVerified()) {
                System.out.println("Email already verified: " + email);
                response.put("message", "Email already verified");
                return ResponseEntity.ok(response);
            }

            String verificationCode = String.format("%06d", new Random().nextInt(999999));
            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
            userService.updateUser(user.getUserId(), user);

            System.out.println("New verification code generated: " + verificationCode);

            try {
                emailService.sendVerificationEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        verificationCode
                );
                System.out.println("Verification code resent to: " + user.getEmail());
            } catch (Exception emailError) {
                System.err.println("Failed to send email: " + emailError.getMessage());
                emailError.printStackTrace();
                response.put("error", "Failed to send email. Please try again later.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("message", "Verification code resent successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to resend code: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Failed to resend code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            System.out.println("Password reset request for: " + email);

            User user = userService.getUserByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("User not found: " + email);
                response.put("error", "No account found with that email address");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String resetToken = String.format("%06d", new Random().nextInt(999999));
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userService.updateUser(user.getUserId(), user);

            System.out.println("Reset token generated: " + resetToken);

            try {
                emailService.sendPasswordResetEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        resetToken
                );
                System.out.println("Password reset email sent to: " + user.getEmail());
            } catch (Exception emailError) {
                System.err.println("Failed to send reset email: " + emailError.getMessage());
                emailError.printStackTrace();
                response.put("error", "Failed to send reset email. Please try again later.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("message", "Password reset code sent to your email!");
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to process request: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Failed to process request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            System.out.println("Password reset attempt - Email: " + email + ", Token: " + token);

            if (email == null || token == null || newPassword == null) {
                System.out.println("Missing parameters");
                response.put("error", "Missing required parameters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.getUserByEmail(email).orElse(null);

            if (user == null) {
                System.out.println("User not found: " + email);
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            System.out.println("Stored token: " + user.getResetToken() + ", Expiry: " + user.getResetTokenExpiry());

            if (user.getResetToken() == null || !user.getResetToken().equals(token)) {
                System.out.println("Invalid reset code");
                response.put("error", "Invalid reset code");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                System.out.println("Reset code has expired");
                response.put("error", "Reset code has expired. Please request a new one.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            userService.resetUserPassword(user.getUserId(), newPassword);

            System.out.println("Password reset successfully for: " + email);

            response.put("message", "Password reset successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to reset password: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/username")
    public ResponseEntity<Map<String, Object>> changeUsername(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String newUsername = request.get("newUsername");

            if (newUsername == null || newUsername.trim().isEmpty()) {
                response.put("error", "Username cannot be empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (newUsername.length() < 3) {
                response.put("error", "Username must be at least 3 characters long");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Check if username is already taken
            if (userService.isUsernameTaken(newUsername.trim(), id)) {
                response.put("error", "Username is already taken");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            user.setFirstName(newUsername.trim());
            userService.updateUser(id, user);

            response.put("message", "Username updated successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to update username: " + e.getMessage());
            response.put("error", "Failed to update username: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                response.put("error", "Both current and new passwords are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (newPassword.length() < 8) {
                response.put("error", "New password must be at least 8 characters long");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!userService.validatePassword(currentPassword, user.getPasswordHash())) {
                response.put("error", "Current password is incorrect");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            userService.resetUserPassword(id, newPassword);

            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to update password: " + e.getMessage());
            response.put("error", "Failed to update password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}/bio")
    public ResponseEntity<Map<String, Object>> updateBio(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String bio = request.get("bio");

            if (bio == null) {
                response.put("error", "Bio is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (bio.length() > 500) {
                response.put("error", "Bio must be less than 500 characters");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            user.setLastName(bio);
            userService.updateUser(id, user);

            response.put("message", "Bio updated successfully");
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to update bio: " + e.getMessage());
            response.put("error", "Failed to update bio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/profile-image")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("error", "Please select a file to upload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("error", "Only image files are allowed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("error", "File size must not exceed 5MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                response.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update user profile image URL
            String imageUrl = "/uploads/profile-images/" + uniqueFilename;
            user.setProfileImageUrl(imageUrl);
            userService.updateUser(id, user);

            response.put("message", "Profile image updated successfully");
            response.put("imageUrl", imageUrl);
            response.put("user", user);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to upload profile image: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Failed to upload profile image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        boolean isTaken = userService.isUsernameTaken(username, null);
        response.put("available", !isTaken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("createdAt", user.getCreatedAt());

            // Get most searched station (from saved schedules)
            String mostSearchedStation = "N/A";
            List<SavedSchedule> schedules = savedScheduleRepository.findByUserId(id);
            if (!schedules.isEmpty()) {
                Map<String, Long> stationCounts = schedules.stream()
                        .collect(Collectors.groupingBy(
                                s -> s.getStation().getName(),
                                Collectors.counting()
                        ));
                mostSearchedStation = stationCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("N/A");
            }
            stats.put("mostSearchedStation", mostSearchedStation);

            stats.put("mostSearchedDays", "Mon - Fri");

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}