package com.github.rinnn31.motelserver.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}
