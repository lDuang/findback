package com.lostfound.backend.service;

import com.lostfound.backend.entity.User;
import com.lostfound.backend.exception.NotFoundException;
import com.lostfound.backend.model.UserContext;
import com.lostfound.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Username and password are required");
        }
        return userRepository.findByUsernameAndPassword(username, password)
                .map(user -> {
                    return toPayload(user.getId(), user.getUsername(), normalizeRole(user.getRole()));
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
    }

    public Map<String, Object> register(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Username and password are required");
        }
        userRepository.findByUsername(username).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists");
        });
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        User saved = userRepository.save(user);
        return toPayload(saved.getId(), saved.getUsername(), saved.getRole());
    }

    public UserContext extractContext(String userIdHeader, String roleHeader) {
        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(roleHeader)) {
            throw new IllegalArgumentException("Missing authentication headers");
        }
        try {
            Long userId = Long.parseLong(userIdHeader);
            return new UserContext(userId, normalizeRole(roleHeader));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid user id header");
        }
    }

    public UserContext extractOptionalContext(String userIdHeader, String roleHeader) {
        if (!StringUtils.hasText(userIdHeader) || !StringUtils.hasText(roleHeader)) {
            return null;
        }
        return extractContext(userIdHeader, roleHeader);
    }

    public void ensureAdmin(UserContext context) {
        if (!context.isAdmin()) {
            throw new SecurityException("Admin role required");
        }
    }

    public void ensureOwnershipOrAdmin(UserContext context, Long ownerId) {
        if (!context.isAdmin() && (ownerId == null || !ownerId.equals(context.getUserId()))) {
            throw new SecurityException("Operation not permitted for this user");
        }
    }

    public User ensureUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return role;
        }
        return role.toUpperCase();
    }

    private Map<String, Object> toPayload(Long userId, String username, String role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("role", normalizeRole(role));
        return payload;
    }
}
