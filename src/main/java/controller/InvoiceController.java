package controller;

import dto.InvoiceDTO;
import entity.Invoice;
import entity.InvoiceStatus;
import entity.Role;
import entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import repository.InvoiceRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    // Existing: View my invoices
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InvoiceDTO>> getMyInvoices(@AuthenticationPrincipal User user) {
        List<Invoice> invoices = invoiceRepository.findByIssuedToId(user.getId());

        List<InvoiceDTO> dtos = invoices.stream().map(inv -> {
            InvoiceDTO dto = new InvoiceDTO();
            dto.setId(inv.getId());
            dto.setAmount(inv.getAmount());
            dto.setIssueDate(inv.getIssueDate());
            dto.setDueDate(inv.getDueDate());
            dto.setStatus(inv.getStatus());
            dto.setRelatedContractId(inv.getContract() != null ? inv.getContract().getId() : null);
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // New: Update invoice status (e.g. mark as PAID or CANCELED)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SPONSOR') or hasRole('ADMIN')")
    public ResponseEntity<Invoice> updateStatus(
            @PathVariable UUID id,
            @RequestBody InvoiceStatus newStatus,
            @AuthenticationPrincipal User user) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Security: only owner or admin
        if (!invoice.getIssuedTo().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized");
        }

        invoice.setStatus(newStatus);
        Invoice updated = invoiceRepository.save(invoice);

        return ResponseEntity.ok(updated);
    }

    // New: Archive invoice (soft delete)
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('SPONSOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> archiveInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getIssuedTo().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)) {
            throw new org.springframework.security.access.AccessDeniedException("Not authorized");
        }

        invoice.setStatus(InvoiceStatus.ARCHIVED);
        invoiceRepository.save(invoice);

        return ResponseEntity.noContent().build();
    }
}