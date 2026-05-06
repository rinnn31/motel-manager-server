package com.github.rinnn31.motelserver.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.CreateUserRequest;
import com.github.rinnn31.motelserver.dto.request.UpdateProfileRequest;
import com.github.rinnn31.motelserver.dto.response.MediaPresignedUrlResponse;
import com.github.rinnn31.motelserver.dto.response.UserInfoResponse;
import com.github.rinnn31.motelserver.entity.UserRole;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.UserRepository;
import com.github.rinnn31.motelserver.security.Requester;
import com.github.rinnn31.motelserver.service.external.ObjectStorageService;

@Service
public class AccountService {
    public static final String VERIFY_CONTACTPOINT_ACTION = "contactpoint_verification";

    public static final int PENDING_PHONE_CHANGE_TTL_MINUTES = 30;

    private final UserRepository userRepository;

    private final OtpService otpService;

    private final StringRedisTemplate redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final ObjectStorageService objectStorageService;

    public static final int MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    public static final String[] ALLOWED_AVATAR_TYPES = new String[] {"image/jpeg", "image/png"};

    public AccountService(
        UserRepository userRepository, 
        OtpService otpService, 
        StringRedisTemplate redisTemplate, 
        PasswordEncoder passwordEncoder,
        ObjectStorageService objectStorageService
    ) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.objectStorageService = objectStorageService;
    }   

    public void deleteAccount(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    public void changePassword(Requester requester, String oldPassword, String newPassword) {
        var user = userRepository.findById(requester.userId()).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppError(ErrorCode.OLD_PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    public void changeContactpoint(Requester requester, String newPhoneNumber) {
        var user = userRepository.findById(requester.userId()).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        }
        
        // If the user has already verified their account,
        // we need to verify the new phone number before changing it
        if (user.isVerified()) {
            redisTemplate.opsForValue().set("pending_phone_change:" + user.getId(), newPhoneNumber, java.time.Duration.ofMinutes(PENDING_PHONE_CHANGE_TTL_MINUTES));
        } else {
            user.setPhoneNumber(newPhoneNumber);
            userRepository.save(user);
        }

    }

    public UserInfoResponse getUserInfo(UUID userId, boolean includesPrivateInfo) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        return new UserInfoResponse(
            user.getId().toString(),
            includesPrivateInfo ? user.getPhoneNumber() : null,
            user.getFullName(),
            user.getGender(),
            user.getRole().name(),
            includesPrivateInfo ? user.isVerified() : null,
            user.getAvatarUrl()
        );
    }

    public void updateProfile(Requester requester, UpdateProfileRequest input) {
        var user = userRepository.findById(requester.userId()).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        if (input.fullName() != null && !input.fullName().isBlank()) {
            user.setFullName(input.fullName());
        }
        if (input.gender() != null) {
            user.setGender(input.gender());
        }

        userRepository.save(user);
    }

    public void sendContactpointVerificationCode(Requester requester, String phoneNumber, Locale locale) {
        var user = userRepository.findById(requester.userId())
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.isVerified()) {
            String pendingPhoneNumber = redisTemplate.opsForValue().get("pending_phone_change:" + user.getId().toString());
            if (pendingPhoneNumber == null || !pendingPhoneNumber.equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        } else {
            if (!user.getPhoneNumber().equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        }
        otpService.sendOtp(user.getId().toString(), phoneNumber, VERIFY_CONTACTPOINT_ACTION, locale);
    }   

    public void verifyContactpoint(Requester requester, String phoneNumber, String otp) {
        var user = userRepository.findById(requester.userId())
            .orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));

        if (user.isVerified()) {
            String pendingPhoneNumber = redisTemplate.opsForValue().get("pending_phone_change:" + user.getId());
            if (pendingPhoneNumber == null || !pendingPhoneNumber.equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        } else {
            if (!user.getPhoneNumber().equals(phoneNumber)) {
                throw new AppError(ErrorCode.INVALID_OPERATION);
            }
        }
        if (!otpService.verifyOtp(user.getId().toString(), phoneNumber, VERIFY_CONTACTPOINT_ACTION, otp, true)) {
            throw new AppError(ErrorCode.VERIFY_FAILED);
        }

        user.setVerified(true);
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
    }

    public MediaPresignedUrlResponse getAvatarUploadPresignedUrl(Requester requester, String imageType) {
        if (!java.util.Arrays.asList(ALLOWED_AVATAR_TYPES).contains(imageType)) {
            throw new AppError(ErrorCode.INVALID_FILE_TYPE);
        }
        return objectStorageService.generatePresignedUrl(imageType, "avatars", MAX_AVATAR_SIZE);
    }

    public void updateAvatarUrl(Requester requester, String avatarKey) {
        if (!objectStorageService.objectExists(avatarKey)) {
            throw new AppError(ErrorCode.FILE_NOT_FOUND);
        }

        var user = userRepository.findById(requester.userId()).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        user.setAvatarUrl(avatarKey);
        userRepository.save(user);
    }


    /* Admin-only operations */
    public Page<UserInfoResponse> getUsers(String phoneNumberFilter, int page, int size) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var usersPage = userRepository.findAllByPhoneNumberContaining(phoneNumberFilter, pageable);
        return usersPage.map(user -> new UserInfoResponse(
            user.getId().toString(),
            user.getPhoneNumber(),
            user.getFullName(),
            user.getGender(),
            user.getRole().name(),
            user.isVerified(),
            user.getAvatarUrl()
        ));
    }

    public void verifyUser(UUID userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new AppError(ErrorCode.USER_NOT_FOUND));
        user.setVerified(true);
        userRepository.save(user);
    }

    public String createUser(CreateUserRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new AppError(ErrorCode.PHONE_NUMBER_USED);
        }

        var user = new com.github.rinnn31.motelserver.entity.User();
        user.setPhoneNumber(request.phoneNumber());
        user.setFullName(request.fullName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setGender(request.gender());
        user.setRole(request.role() == 0 ? UserRole.LANDLORD : UserRole.TENANT);
        user.setVerified(request.isVerified());
        user = userRepository.save(user);

        return user.getId().toString();
    }
}
