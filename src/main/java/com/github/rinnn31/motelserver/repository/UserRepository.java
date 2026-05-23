package com.github.rinnn31.motelserver.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.User;
import com.github.rinnn31.motelserver.entity.UserRole;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findAllByPhoneNumberContainingIgnoreCaseOrFullNameContainingIgnoreCase(String phoneNumber, String fullName, Pageable pageable);

    Page<User> findAllByPhoneNumberContainingAndRole(String phoneNumber, UserRole role, Pageable pageable);

    Page<User> findAllByRole(UserRole role, Pageable pageable);
}
