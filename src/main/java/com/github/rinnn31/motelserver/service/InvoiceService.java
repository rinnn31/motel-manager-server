package com.github.rinnn31.motelserver.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.CreateInvoiceRequest;
import com.github.rinnn31.motelserver.dto.response.InvoiceInfoResponse;
import com.github.rinnn31.motelserver.entity.Invoice;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.InvoiceRepository;
import com.github.rinnn31.motelserver.repository.RoomMemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;


@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    private final RoomRepository roomRepository;

    private final RoomMemberRepository roomMemberRepository;
    
    public InvoiceService(InvoiceRepository invoiceRepository, RoomRepository roomRepository, RoomMemberRepository roomMemberRepository) {
        this.invoiceRepository = invoiceRepository;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
    }

    public List<InvoiceInfoResponse> getInvoicesByRoom(UUID roomId, UUID requesterId, LocalDate fromDate, LocalDate toDate) {
        var room = roomRepository.findById(roomId).orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var memberOpt = roomMemberRepository.findByUser_IdAndRoom_IdAndEndDateIsNull(requesterId, roomId);
        var instantFromDate = fromDate != null ? fromDate.atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        var instantToDate = toDate != null ? toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        if (instantFromDate != null && instantFromDate.isAfter(instantToDate)) {
            throw new AppError(ErrorCode.INVALID_DATE_RANGE);
        }
        if (!memberOpt.isPresent() && !room.getMotel().getOwner().getId().equals(requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);   
        }
        
        if (memberOpt.isPresent()) {
            var member = memberOpt.get();
            var memberStartInstant = member.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            // Member can only see invoices from the date they joined the room
            if (memberStartInstant.isAfter(instantFromDate)) {
                instantFromDate = memberStartInstant;
            }
        }
        
        List<Invoice> invoices = invoiceRepository.findByRoomIdAndCreatedAtBetween(roomId, instantFromDate, instantToDate);
        return invoices.stream().map(
            invoice -> {
                List<InvoiceInfoResponse.InvoiceDetailsInfoResponse> details = invoice.getDetails().stream()
                    .map(detail -> new InvoiceInfoResponse.InvoiceDetailsInfoResponse(
                        detail.getName(),
                        detail.getUnitPrice(),
                        detail.getAmount(),
                        detail.getCalculationType()
                    )).toList();
                return new InvoiceInfoResponse(
                    invoice.getId().toString(),
                    invoice.getCreatedAt().toEpochMilli(),
                    invoice.getPaidAt() != null ? invoice.getPaidAt().toEpochMilli() : null,
                    invoice.isPaid(),
                    details
                );

            }
        ).toList();
    }

    public void createInvoice( UUID requesterId, CreateInvoiceRequest request) {
        var room = roomRepository.findById(UUID.fromString(request.roomId())).orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        if (!room.getMotel().getOwner().getId().equals(requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);   
        }

        var invoice = new Invoice();
        invoice.setRoom(room);
        invoice.setCreatedAt(Instant.now());
        invoice.setPaid(false);
        invoice.setDetails(request.invoiceDetails().stream().map(
            detail -> {
                var entity = new com.github.rinnn31.motelserver.entity.InvoiceDetails();
                entity.setName(detail.name());
                entity.setUnitPrice(detail.unitPrice());
                entity.setAmount(detail.amount());
                entity.setCalculationType(detail.calculationType());
                entity.setInvoice(invoice);
                return entity;
            }
        ).toList());

        invoiceRepository.save(invoice);
    }

    public void payInvoice(UUID invoiceId, UUID requesterId) {
        var invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new AppError(ErrorCode.INVOICE_NOT_FOUND));
        if (!invoice.getRoom().getMotel().getOwner().getId().equals(requesterId)) {
            throw new AppError(ErrorCode.INVALID_OPERATION);   
        }

        invoice.setPaid(true);
        invoice.setPaidAt(Instant.now());
        invoiceRepository.save(invoice);
    }
}
