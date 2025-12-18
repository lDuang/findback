package com.lostfound.backend.service;

import com.lostfound.backend.entity.User;
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
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("userId", user.getId());
                    payload.put("role", normalizeRole(user.getRole()));
                    return payload;
                })
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
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
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return role;
        }
        return role.toUpperCase();
    }
}
