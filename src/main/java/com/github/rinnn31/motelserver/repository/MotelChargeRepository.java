package com.github.rinnn31.motelserver.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.MotelCharge;

@Repository
public interface MotelChargeRepository extends JpaRepository<MotelCharge, UUID> {
    boolean existsByMotel_IdAndTypeIsNot(UUID motelId, String type);
}
