package com.github.rinnn31.motelserver.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class UserSecurityService {
    private final UserRepository userRepository;

    public UserSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(u -> new CustomUserDetails(u.getId(), u.isVerified()))
                .orElse(null);
    }
}
