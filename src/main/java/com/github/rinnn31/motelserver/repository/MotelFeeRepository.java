package com.github.rinnn31.motelserver.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.MotelFee;

@Repository
public interface MotelFeeRepository extends JpaRepository<MotelFee, UUID> {
    boolean existsByMotel_IdAndNameIgnoreCase(UUID motelId, String name);
}
