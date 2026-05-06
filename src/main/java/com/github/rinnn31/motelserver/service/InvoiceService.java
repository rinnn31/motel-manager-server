package com.github.rinnn31.motelserver.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.rinnn31.motelserver.dto.request.CreateInvoiceRequest;
import com.github.rinnn31.motelserver.dto.response.InvoiceInfoResponse;
import com.github.rinnn31.motelserver.entity.Invoice;
import com.github.rinnn31.motelserver.entity.Member;
import com.github.rinnn31.motelserver.entity.PaymentStatus;
import com.github.rinnn31.motelserver.event.model.InvoiceChangedEvent;
import com.github.rinnn31.motelserver.exception.AppError;
import com.github.rinnn31.motelserver.exception.ErrorCode;
import com.github.rinnn31.motelserver.repository.InvoiceRepository;
import com.github.rinnn31.motelserver.repository.MemberRepository;
import com.github.rinnn31.motelserver.repository.RoomRepository;
import com.github.rinnn31.motelserver.security.Requester;


@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    private final RoomRepository roomRepository;

    private final MemberRepository roomMemberRepository;

    private final ApplicationEventPublisher eventPublisher;

    public InvoiceService(
        InvoiceRepository invoiceRepository, 
        RoomRepository roomRepository, 
        MemberRepository roomMemberRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.invoiceRepository = invoiceRepository;
        this.roomRepository = roomRepository;
        this.roomMemberRepository = roomMemberRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<InvoiceInfoResponse> getInvoicesByRoom(UUID roomId, Requester requester, LocalDate fromDate, LocalDate toDate) {
        var room = roomRepository.findById(roomId).orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        var instantFromDate = fromDate != null ? fromDate.atStartOfDay().toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        var instantToDate = toDate != null ? toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        if (instantFromDate.isAfter(instantToDate)) {
            throw new AppError(ErrorCode.INVALID_DATE_RANGE);
        }

        Optional<Member> memberOpt = requester.isAdmin() ? Optional.empty() :  roomMemberRepository.findByUser_IdAndRoom_IdAndEndDateIsNull(requester.userId(), roomId);
        boolean isLandlordOrAdmin = requester.isAdmin() || room.getMotel().getOwner().getId().equals(requester.userId());

        if (!memberOpt.isPresent() && !isLandlordOrAdmin) {
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
                        detail.getAmount(),
                        detail.getUnitPrice(),
                        detail.getCalculationType()
                    )).toList();
                return new InvoiceInfoResponse(
                    invoice.getId().toString(),
                    invoice.getCreatedAt().toEpochMilli(),
                    invoice.getPaidAt() != null ? invoice.getPaidAt().toEpochMilli() : null,
                    invoice.getPaymentStatus().name(),
                    details
                );

            }
        ).toList();
    }

    public void createInvoice(Requester requester, CreateInvoiceRequest request) {
        var room = roomRepository.findById(UUID.fromString(request.roomId())).orElseThrow(() -> new AppError(ErrorCode.ROOM_NOT_FOUND));
        if (!room.getMotel().getOwner().getId().equals(requester.userId())) {
            throw new AppError(ErrorCode.INVALID_OPERATION);   
        }

        var invoice = new Invoice();
        invoice.setRoom(room);
        invoice.setCreatedAt(Instant.now());
        invoice.setPaymentStatus(PaymentStatus.UNPAID);
        invoice.setDetails(request.details().stream().map(
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

        eventPublisher.publishEvent(new InvoiceChangedEvent.Created(
            invoice.getId().toString(),
            room.getMotel().getId().toString(),
            room.getId().toString()
        ));
    }

    public void payInvoice(Requester requester, UUID invoiceId) {
        var invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new AppError(ErrorCode.INVOICE_NOT_FOUND));
        boolean isLandlord = invoice.getRoom().getMotel().getOwner().getId().equals(requester.userId());
        boolean isTenant = roomMemberRepository.existsByUser_IdAndRoom_IdAndEndDateIsNull(requester.userId(), invoice.getRoom().getId());
        if (!isLandlord && !isTenant) {
            throw new AppError(ErrorCode.INVALID_OPERATION);   
        }
        if (invoice.getPaymentStatus() == PaymentStatus.PAYEE_CONFIRMED) {
            throw new AppError(ErrorCode.PAYMENT_CONFIRMED);
        }
        if (invoice.getPaymentStatus() == PaymentStatus.PAYER_CONFIRMED && isTenant) {
            throw new AppError(ErrorCode.ALREADY_PAID);
        }

        if (isLandlord) {
            invoice.setPaymentStatus(PaymentStatus.PAYEE_CONFIRMED);
        } else {
            invoice.setPaymentStatus(PaymentStatus.PAYER_CONFIRMED);
        }
        invoice.setPaidAt(Instant.now());
        invoiceRepository.save(invoice);

        if (isLandlord) {
            eventPublisher.publishEvent(new InvoiceChangedEvent.PayeeConfirmed(
                invoice.getId().toString(),
                invoice.getRoom().getMotel().getId().toString(),
                invoice.getRoom().getId().toString()
            ));
        } else {
            eventPublisher.publishEvent(new InvoiceChangedEvent.PayerPaid(
                invoice.getId().toString(),
                invoice.getRoom().getMotel().getId().toString(),
                invoice.getRoom().getId().toString()
            ));
        }
    }

    public void deleteInvoice(Requester requester, UUID invoiceId) {
        var invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new AppError(ErrorCode.INVOICE_NOT_FOUND));
        if (!invoice.getRoom().getMotel().getOwner().getId().equals(requester.userId()) || invoice.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new AppError(ErrorCode.INVALID_OPERATION);   
        }

        invoiceRepository.delete(invoice);

        eventPublisher.publishEvent(new InvoiceChangedEvent.Delete(
            invoice.getRoom().getMotel().getId().toString(),
            invoice.getRoom().getId().toString()
        ));
    }
}
