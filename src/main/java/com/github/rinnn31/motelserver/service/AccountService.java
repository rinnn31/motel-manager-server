package com.github.rinnn31.motelserver.service;

import java.util.UUID;

import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.UserRepository;

public class AccountService {
    private final UserRepository userRepository;

    public AccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isPhoneNumberInUse(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public void deleteAccount(UUID userId) {
        userRepository.deleteById(userId);
    }

    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public void changeContactpoint(UUID userId, String newPhoneNumber) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        }
        user.setPhoneNumber(newPhoneNumber);
        userRepository.save(user);
    }
}
