package com.github.rinnn31.motelserver.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.rinnn31.motelserver.entity.User;
import com.github.rinnn31.motelserver.repository.UserDeviceRepository;

@Service
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;

    public UserDeviceService(UserDeviceRepository userDeviceRepository) {
        this.userDeviceRepository = userDeviceRepository;
    }

    @Transactional
    public void registerDeviceToken(String sessionToken, String deviceToken, User user) {
        var userDeviceOpt = userDeviceRepository.findByUser_IdAndSessionToken(user.getId(), sessionToken);
        if (userDeviceOpt.isPresent()) {
            var userDevice = userDeviceOpt.get();
            userDevice.setDeviceToken(deviceToken);
            userDeviceRepository.save(userDevice);
        } else {
            var userDevice = new com.github.rinnn31.motelserver.entity.UserDevice();
            userDevice.setUser(user);
            userDevice.setSessionToken(sessionToken);
            userDevice.setDeviceToken(deviceToken);
            userDeviceRepository.save(userDevice);
        }
    }
    
    @Transactional
    public void unregisterDeviceToken(String sessionToken) {
        userDeviceRepository.deleteBySessionToken(sessionToken);
    }

    @Transactional
    public void unregisterDeviceTokenByDeviceToken(String deviceToken) {
        userDeviceRepository.deleteByDeviceToken(deviceToken);
    }

    public List<String> getDeviceTokensForUser(UUID userId) {
        var userDevices = userDeviceRepository.findByUser_Id(userId);
        return userDevices.stream()
            .map(com.github.rinnn31.motelserver.entity.UserDevice::getDeviceToken)
            .toList();
    }
}
