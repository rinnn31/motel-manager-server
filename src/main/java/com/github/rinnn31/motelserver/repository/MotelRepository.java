package com.github.rinnn31.motelserver.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Motel;

@Repository
public interface MotelRepository extends JpaRepository<Motel, UUID> {
    List<Motel> findByOwner_Id(UUID ownerId);

    boolean existsByOwner_IdAndDisplayName(UUID ownerId, String displayName);

    boolean existsByOwner_IdAndId(UUID requesterId, UUID motelId);
}
