package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    Optional<UserDevice> findByUser_IdAndSessionToken(UUID userId, String sessionToken);

    List<UserDevice> findByUser_Id(UUID userId);
    
    void deleteByDeviceToken(String deviceToken);

    void deleteBySessionToken(String sessionToken);
}
