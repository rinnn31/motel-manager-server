package com.github.rinnn31.motelserver.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.rinnn31.motelserver.entity.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByRoomId(UUID roomId);

    List<Invoice> findByRoomIdAndCreatedAtBetween(UUID roomId, Instant startTime, Instant endTime);
}
