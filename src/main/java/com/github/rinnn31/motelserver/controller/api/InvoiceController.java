package com.github.rinnn31.motelserver.controller.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.rinnn31.motelserver.dto.request.CreateInvoiceRequest;
import com.github.rinnn31.motelserver.dto.response.InvoiceInfoResponse;
import com.github.rinnn31.motelserver.security.UserExtractor;
import com.github.rinnn31.motelserver.service.InvoiceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceInfoResponse> getInvoicesByRoom(
        @RequestParam UUID roomId, 
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate, 
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        return invoiceService.getInvoicesByRoom(roomId, requesterId, fromDate, toDate);
    }

    @PostMapping
    public void createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        invoiceService.createInvoice(requesterId, request);
    }

    @PatchMapping("/{invoiceId}/pay")
    public void payInvoice(@PathVariable UUID invoiceId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        invoiceService.payInvoice(requesterId, invoiceId);
    }

    @DeleteMapping("/{invoiceId}")
    public void deleteInvoice(@PathVariable UUID invoiceId) {
        UUID requesterId = UserExtractor.extractUserIdFromContext();
        invoiceService.deleteInvoice(requesterId, invoiceId);
    }
}
