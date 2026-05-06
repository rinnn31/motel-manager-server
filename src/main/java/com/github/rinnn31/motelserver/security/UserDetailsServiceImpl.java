package com.github.rinnn31.motelserver.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userIdString) throws UsernameNotFoundException {
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Người dùng không hợp lệ");
        }
        var user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));
        return User.builder()
            .username(user.getId().toString())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }
}
