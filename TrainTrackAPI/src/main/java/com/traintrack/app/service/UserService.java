package com.traintrack.app.service;

import com.traintrack.app.model.User;
import com.traintrack.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());

        if (userDetails.getNotificationPreferences() != null) {
            user.setNotificationPreferences(userDetails.getNotificationPreferences());
        }

        if (userDetails.getEmailVerified() != null) {
            user.setEmailVerified(userDetails.getEmailVerified());
        }

        if (userDetails.getVerificationCode() != null) {
            user.setVerificationCode(userDetails.getVerificationCode());
        }

        if (userDetails.getVerificationCodeExpiry() != null) {
            user.setVerificationCodeExpiry(userDetails.getVerificationCodeExpiry());
        }

        if (userDetails.getResetToken() != null) {
            user.setResetToken(userDetails.getResetToken());
        }

        if (userDetails.getResetTokenExpiry() != null) {
            user.setResetTokenExpiry(userDetails.getResetTokenExpiry());
        }

        if (userDetails.getPasswordHash() != null &&
                !userDetails.getPasswordHash().isEmpty() &&
                !userDetails.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
        }

        return userRepository.save(user);
    }

    public User resetUserPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        return userRepository.save(user);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // NEW 
    public void updateEmail(Long userId, String newEmail) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = optionalUser.get();
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = optionalUser.get();

        // Check current password using the hash field
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        // Encode and update new password (save to passwordHash)
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
